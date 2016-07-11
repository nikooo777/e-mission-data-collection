package obd.commands.control;

import obd.commands.ObdCommand;
import obd.enums.AvailableCommandNames;

/**
 * <p>ModuleVoltageCommand class.</p>
 *
 * @author pires
 * @version $Id: $Id
 */
public class ModuleVoltageCommand extends ObdCommand {

    // Equivalent ratio (V)
    private double voltage = 0.00;

    /**
     * Default ctor.
     */
    public ModuleVoltageCommand() {
        super("01 42");
    }

    /**
     * Copy ctor.
     *
     * @param other a {@link obd.commands.control.ModuleVoltageCommand} object.
     */
    public ModuleVoltageCommand(ModuleVoltageCommand other) {
        super(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        int a = this.buffer.get(2);
        int b = this.buffer.get(3);
        this.voltage = (a * 256 + b) / 1000;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormattedResult() {
        return String.format("%.1f%s", this.voltage, getResultUnit());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResultUnit() {
        return "V";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCalculatedResult() {
        return String.valueOf(this.voltage);
    }

    /**
     * <p>Getter for the field <code>voltage</code>.</p>
     *
     * @return a double.
     */
    public double getVoltage() {
        return this.voltage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return AvailableCommandNames.CONTROL_MODULE_VOLTAGE.getValue();
    }

}
