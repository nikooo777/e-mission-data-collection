package obd.commands.temperature;

import obd.commands.ObdCommand;
import obd.commands.SystemOfUnits;

/**
 * Abstract temperature command.
 *
 * @author pires
 * @version $Id: $Id
 */
public abstract class TemperatureCommand extends ObdCommand implements
        SystemOfUnits {

    private float temperature = 0.0f;

    /**
     * Default ctor.
     *
     * @param cmd a {@link String} object.
     */
    public TemperatureCommand(String cmd) {
        super(cmd);
    }

    /**
     * Copy ctor.
     *
     * @param other a {@link obd.commands.temperature.TemperatureCommand} object.
     */
    public TemperatureCommand(TemperatureCommand other) {
        super(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        this.temperature = this.buffer.get(2) - 40;
    }


    /**
     * {@inheritDoc}
     * <p/>
     * Get values from 'buff', since we can't rely on char/string for
     * calculations.
     */
    @Override
    public String getFormattedResult() {
        return this.useImperialUnits ? String.format("%.1f%s", getImperialUnit(), getResultUnit())
                : String.format("%.0f%s", this.temperature, getResultUnit());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCalculatedResult() {
        return this.useImperialUnits ? String.valueOf(getImperialUnit()) : String.valueOf(this.temperature);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResultUnit() {
        return this.useImperialUnits ? "F" : "C";
    }

    /**
     * <p>Getter for the field <code>temperature</code>.</p>
     *
     * @return the temperature in Celsius.
     */
    public float getTemperature() {
        return this.temperature;
    }

    /**
     * <p>getImperialUnit.</p>
     *
     * @return the temperature in Fahrenheit.
     */
    public float getImperialUnit() {
        return this.temperature * 1.8f + 32;
    }

    /**
     * <p>getKelvin.</p>
     *
     * @return the temperature in Kelvin.
     */
    public float getKelvin() {
        return this.temperature + 273.15f;
    }

    /**
     * <p>getName.</p>
     *
     * @return the OBD command name.
     */
    public abstract String getName();

}
