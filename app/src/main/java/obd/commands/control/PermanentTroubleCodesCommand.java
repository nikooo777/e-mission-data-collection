package obd.commands.control;

import java.io.IOException;
import java.io.InputStream;

import obd.commands.ObdCommand;
import obd.enums.AvailableCommandNames;

/**
 * It is not needed no know how many DTC are stored.
 * Because when no DTC are stored response will be NO DATA
 * And where are more messages it will be stored in frames that have 7 bytes.
 * In one frame are stored 3 DTC.
 * If we find out DTC P0000 that mean no message are we can end.
 *
 * @author pires
 * @version $Id: $Id
 */
public class PermanentTroubleCodesCommand extends ObdCommand {

    /**
     * Constant <code>dtcLetters={'P', 'C', 'B', 'U'}</code>
     */
    protected final static char[] dtcLetters = {'P', 'C', 'B', 'U'};
    /**
     * Constant <code>hexArray="0123456789ABCDEF".toCharArray()</code>
     */
    protected final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    protected StringBuilder codes = null;

    /**
     * <p>Constructor for PermanentTroubleCodesCommand.</p>
     */
    public PermanentTroubleCodesCommand() {
        super("0A");
        this.codes = new StringBuilder();
    }

    /**
     * Copy ctor.
     *
     * @param other a {@link obd.commands.control.PermanentTroubleCodesCommand} object.
     */
    public PermanentTroubleCodesCommand(PermanentTroubleCodesCommand other) {
        super(other);
        this.codes = new StringBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillBuffer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCalculations() {
        final String result = getResult();
        String workingData;
        int startIndex = 0;//Header size.

        String canOneFrame = result.replaceAll("[\r\n]", "");
        int canOneFrameLength = canOneFrame.length();
        if (canOneFrameLength <= 16 && canOneFrameLength % 4 == 0) {//CAN(ISO-15765) protocol one frame.
            workingData = canOneFrame;//4Ayy{codes}
            startIndex = 4;//Header is 4Ayy, yy showing the number of data items.
        }
        else if (result.contains(":")) {//CAN(ISO-15765) protocol two and more frames.
            workingData = result.replaceAll("[\r\n].:", "");//xxx4Ayy{codes}
            startIndex = 7;//Header is xxx4Ayy, xxx is bytes of information to follow, yy showing the number of data items.
        }
        else {//ISO9141-2, KWP2000 Fast and KWP2000 5Kbps (ISO15031) protocols.
            workingData = result.replaceAll("^4A|[\r\n]4A|[\r\n]", "");
        }
        for (int begin = startIndex; begin < workingData.length(); begin += 4) {
            String dtc = "";
            byte b1 = hexStringToByteArray(workingData.charAt(begin));
            int ch1 = ((b1 & 0xC0) >> 6);
            int ch2 = ((b1 & 0x30) >> 4);
            dtc += dtcLetters[ch1];
            dtc += hexArray[ch2];
            dtc += workingData.substring(begin + 1, begin + 4);
            if (dtc.equals("P0000")) {
                return;
            }
            this.codes.append(dtc);
            this.codes.append('\n');
        }
    }

    private byte hexStringToByteArray(char s) {
        return (byte) ((Character.digit(s, 16) << 4));
    }

    /**
     * <p>formatResult.</p>
     *
     * @return the formatted result of this command in string representation.
     * @deprecated use #getCalculatedResult instead
     */
    public String formatResult() {
        return this.codes.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCalculatedResult() {
        return String.valueOf(this.codes);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void readRawData(InputStream in) throws IOException {
        byte b;
        StringBuilder res = new StringBuilder();

        // read until '>' arrives OR end of stream reached (and skip ' ')
        char c;
        while (true) {
            b = (byte) in.read();
            if (b == -1) // -1 if the end of the stream is reached
            {
                break;
            }
            c = (char) b;
            if (c == '>') // read until '>' arrives
            {
                break;
            }
            if (c != ' ') // skip ' '
            {
                res.append(c);
            }
        }

        this.rawData = res.toString().trim();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormattedResult() {
        return this.codes.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return AvailableCommandNames.PERMANENT_TROUBLE_CODES.getValue();
    }

}