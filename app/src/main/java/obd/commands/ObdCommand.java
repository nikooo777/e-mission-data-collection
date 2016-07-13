package obd.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import obd.exceptions.BusInitException;
import obd.exceptions.MisunderstoodCommandException;
import obd.exceptions.NoDataException;
import obd.exceptions.NonNumericResponseException;
import obd.exceptions.ResponseException;
import obd.exceptions.StoppedException;
import obd.exceptions.UnableToConnectException;
import obd.exceptions.UnknownErrorException;
import obd.exceptions.UnsupportedCommandException;

/**
 * Base OBD command.
 *
 * @author pires
 * @version $Id: $Id
 */
public abstract class ObdCommand {

    /**
     * Error classes to be tested in order
     */
    private final Class[] ERROR_CLASSES = {
            UnableToConnectException.class,
            BusInitException.class,
            MisunderstoodCommandException.class,
            NoDataException.class,
            StoppedException.class,
            UnknownErrorException.class,
            UnsupportedCommandException.class
    };
    protected ArrayList<Integer> buffer = null;
    protected String cmd = null;
    protected boolean useImperialUnits = false;
    protected String rawData = null;
    protected Long responseDelayInMs = null;
    private long start;
    private long end;
    private boolean ready = false;

    public boolean isReady() {
        return this.ready;
    }

    /**
     * Default ctor to use
     *
     * @param command the command to send
     */
    public ObdCommand(String command) {
        this.cmd = command;
        this.buffer = new ArrayList<>();
    }

    /**
     * Prevent empty instantiation
     */
    private ObdCommand() {
    }

    /**
     * Copy ctor.
     *
     * @param other the ObdCommand to copy.
     */
    public ObdCommand(ObdCommand other) {
        this(other.cmd);
    }

    /**
     * Sends the OBD-II request and deals with the response.
     * <p/>
     * This method CAN be overriden in fake commands.
     *
     * @param in  a {@link InputStream} object.
     * @param out a {@link OutputStream} object.
     * @throws IOException          if any.
     * @throws InterruptedException if any.
     */
    public void run(InputStream in, OutputStream out) throws IOException,
            InterruptedException {
        synchronized (ObdCommand.class) {//Only one command can write and read a data in one time.
            this.start = System.currentTimeMillis();
            this.ready = false;
            sendCommand(out);
            readResult(in);
            this.ready = true;
            this.end = System.currentTimeMillis();
        }
    }

    protected void setReady(boolean readyness) {
        this.ready = readyness;
    }

    /**
     * Sends the OBD-II request.
     * <p/>
     * This method may be overriden in subclasses, such as ObMultiCommand or
     * TroubleCodesCommand.
     *
     * @param out The output stream.
     * @throws IOException          if any.
     * @throws InterruptedException if any.
     */
    protected void sendCommand(OutputStream out) throws IOException,
            InterruptedException {
        // write to OutputStream (i.e.: a BluetoothSocket) with an added
        // Carriage return
        out.write((this.cmd + "\r").getBytes());
        out.flush();
        if (this.responseDelayInMs != null && this.responseDelayInMs > 0) {
            Thread.sleep(this.responseDelayInMs);
        }
    }

    /**
     * Resends this command.
     *
     * @param out a {@link OutputStream} object.
     * @throws IOException          if any.
     * @throws InterruptedException if any.
     */
    protected void resendCommand(OutputStream out) throws IOException,
            InterruptedException {
        out.write("\r".getBytes());
        out.flush();
        if (this.responseDelayInMs != null && this.responseDelayInMs > 0) {
            Thread.sleep(this.responseDelayInMs);
        }
    }

    /**
     * Reads the OBD-II response.
     * <p/>
     * This method may be overriden in subclasses, such as ObdMultiCommand.
     *
     * @param in a {@link InputStream} object.
     * @throws IOException if any.
     */
    protected void readResult(InputStream in) throws IOException {
        readRawData(in);
        checkForErrors();
        fillBuffer();
        performCalculations();
    }

    /**
     * This method exists so that for each command, there must be a method that is
     * called only once to perform calculations.
     */
    protected abstract void performCalculations();

    /**
     * <p>fillBuffer.</p>
     */
    protected void fillBuffer() {
        this.rawData = this.rawData.replaceAll("\\s", ""); //removes all [ \t\n\x0B\f\r]
        this.rawData = this.rawData.replaceAll("(BUS INIT)|(BUSINIT)|(\\.)", "");

        if (!this.rawData.matches("([0-9A-F])+")) {
            throw new NonNumericResponseException(this.rawData);
        }

        // read string each two chars
        this.buffer.clear();
        int begin = 0;
        int end = 2;
        while (end <= this.rawData.length()) {
            this.buffer.add(Integer.decode("0x" + this.rawData.substring(begin, end)));
            begin = end;
            end += 2;
        }
    }

    /**
     * <p>
     * readRawData.</p>
     *
     * @param in a {@link InputStream} object.
     * @throws IOException if any.
     */
    protected void readRawData(InputStream in) throws IOException {
        byte b = 0;
        StringBuilder res = new StringBuilder();

        // read until '>' arrives OR end of stream reached
        char c;
        // -1 if the end of the stream is reached
        while (((b = (byte) in.read()) > -1)) {
            c = (char) b;
            if (c == '>') // read until '>' arrives
            {
                break;
            }
            res.append(c);
        }

    /*
     * Imagine the following response 41 0c 00 0d.
     *
     * ELM sends strings!! So, ELM puts spaces between each "byte". And pay
     * attention to the fact that I've put the word byte in quotes, because 41
     * is actually TWO bytes (two chars) in the socket. So, we must do some more
     * processing..
     */
        this.rawData = res.toString().replaceAll("SEARCHING", "");

    /*
     * Data may have echo or informative text like "INIT BUS..." or similar.
     * The response ends with two carriage return characters. So we need to take
     * everything from the last carriage return before those two (trimmed above).
     */
        //kills multiline.. rawData = rawData.substring(rawData.lastIndexOf(13) + 1);
        this.rawData = this.rawData.replaceAll("\\s", "");//removes all [ \t\n\x0B\f\r]
    }

    void checkForErrors() {
        for (Class<? extends ResponseException> errorClass : this.ERROR_CLASSES) {
            ResponseException messageError;

            try {
                messageError = errorClass.newInstance();
                messageError.setCommand(this.cmd);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (messageError.isError(this.rawData)) {
                throw messageError;
            }
        }
    }

    /**
     * <p>getResult.</p>
     *
     * @return the raw command response in string representation.
     */
    public String getResult() {
        return this.rawData;
    }

    /**
     * <p>getFormattedResult.</p>
     *
     * @return a formatted command response in string representation.
     */
    public abstract String getFormattedResult();

    /**
     * <p>getCalculatedResult.</p>
     *
     * @return the command response in string representation, without formatting.
     */
    public abstract String getCalculatedResult();

    /**
     * <p>Getter for the field <code>buffer</code>.</p>
     *
     * @return a list of integers
     */
    protected ArrayList<Integer> getBuffer() {
        return this.buffer;
    }

    /**
     * <p>useImperialUnits.</p>
     *
     * @return true if imperial units are used, or false otherwise
     */
    public boolean useImperialUnits() {
        return this.useImperialUnits;
    }

    /**
     * The unit of the result, as used in {@link #getFormattedResult()}
     *
     * @return a String representing a unit or "", never null
     */
    public String getResultUnit() {
        return "";//no unit by default
    }

    /**
     * Set to 'true' if you want to use imperial units, false otherwise. By
     * default this value is set to 'false'.
     *
     * @param isImperial a boolean.
     */
    public void useImperialUnits(boolean isImperial) {
        this.useImperialUnits = isImperial;
    }

    /**
     * <p>getName.</p>
     *
     * @return the OBD command name.
     */
    public abstract String getName();

    /**
     * Time the command waits before returning from #sendCommand()
     *
     * @return delay in ms (may be null)
     */
    public Long getResponseTimeDelay() {
        return this.responseDelayInMs;
    }

    /**
     * Time the command waits before returning from #sendCommand()
     *
     * @param responseDelayInMs a Long (can be null)
     */
    public void setResponseTimeDelay(Long responseDelayInMs) {
        this.responseDelayInMs = responseDelayInMs;
    }

    //fixme resultunit

    /**
     * <p>Getter for the field <code>start</code>.</p>
     *
     * @return a long.
     */
    public long getStart() {
        return this.start;
    }

    /**
     * <p>Setter for the field <code>start</code>.</p>
     *
     * @param start a long.
     */
    public void setStart(long start) {
        this.start = start;
    }

    /**
     * <p>Getter for the field <code>end</code>.</p>
     *
     * @return a long.
     */
    public long getEnd() {
        return this.end;
    }

    /**
     * <p>Setter for the field <code>end</code>.</p>
     *
     * @param end a long.
     */
    public void setEnd(long end) {
        this.end = end;
    }

    /**
     * <p>getCommandPID.</p>
     *
     * @return a {@link String} object.
     * @since 1.0-RC12
     */
    public final String getCommandPID() {
        return this.cmd.substring(3);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ObdCommand that = (ObdCommand) o;

        return this.cmd != null ? this.cmd.equals(that.cmd) : that.cmd == null;
    }

    @Override
    public int hashCode() {
        return this.cmd != null ? this.cmd.hashCode() : 0;
    }

}
