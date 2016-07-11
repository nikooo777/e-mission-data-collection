package obd.commands.engine;

import obd.commands.ObdCommand;
import obd.enums.AvailableCommandNames;

/**
 * Displays the current engine revolutions per minute (RPM).
 *
 * @author pires
 * @version $Id: $Id
 */
public class RPMCommand extends ObdCommand {

    private int rpm = -1;

    /**
     * Default ctor.
     */
    public RPMCommand() {
        super("01 0C");
    }

    /**
     * Copy ctor.
     *
     * @param other a {@link obd.commands.engine.RPMCommand} object.
     */
    public RPMCommand(RPMCommand other) {
        super(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [41 0C] of the response((A*256)+B)/4
        this.rpm = (this.buffer.get(2) * 256 + this.buffer.get(3)) / 4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormattedResult() {
        return String.format("%d%s", this.rpm, getResultUnit());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCalculatedResult() {
        return String.valueOf(this.rpm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResultUnit() {
        return "RPM";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return AvailableCommandNames.ENGINE_RPM.getValue();
    }

    /**
     * <p>getRPM.</p>
     *
     * @return a int.
     */
    public int getRPM() {
        return this.rpm;
    }

}
