package obd.commands.pressure;

import obd.commands.ObdCommand;
import obd.commands.SystemOfUnits;

/**
 * Abstract pressure command.
 *
 * @author pires
 * @version $Id: $Id
 */
public abstract class PressureCommand extends ObdCommand implements
        SystemOfUnits {

    protected int tempValue = 0;
    protected int pressure = 0;

    /**
     * Default ctor
     *
     * @param cmd a {@link String} object.
     */
    public PressureCommand(String cmd) {
        super(cmd);
    }

    /**
     * Copy ctor.
     *
     * @param other a {@link obd.commands.pressure.PressureCommand} object.
     */
    public PressureCommand(PressureCommand other) {
        super(other);
    }

    /**
     * Some PressureCommand subclasses will need to implement this method in
     * order to determine the final kPa value.
     * <p/>
     * *NEED* to read tempValue
     *
     * @return a int.
     */
    protected int preparePressureValue() {
        return this.buffer.get(2);
    }

    /**
     * <p>performCalculations.</p>
     */
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        this.pressure = preparePressureValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormattedResult() {
        return this.useImperialUnits ? String.format("%.1f%s", getImperialUnit(), getResultUnit())
                : String.format("%d%s", this.pressure, getResultUnit());
    }

    /**
     * <p>getMetricUnit.</p>
     *
     * @return the pressure in kPa
     */
    public int getMetricUnit() {
        return this.pressure;
    }

    /**
     * <p>getImperialUnit.</p>
     *
     * @return the pressure in psi
     */
    public float getImperialUnit() {
        return this.pressure * 0.145037738F;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCalculatedResult() {
        return this.useImperialUnits ? String.valueOf(getImperialUnit()) : String.valueOf(this.pressure);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResultUnit() {
        return this.useImperialUnits ? "psi" : "kPa";
    }

}
