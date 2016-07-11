package obd.commands.fuel;

import obd.commands.ObdCommand;
import obd.enums.AvailableCommandNames;

/**
 * AFR
 *
 * @author pires
 * @version $Id: $Id
 */
public class AirFuelRatioCommand extends ObdCommand {

    private float afr = 0;

    /**
     * <p>Constructor for AirFuelRatioCommand.</p>
     */
    public AirFuelRatioCommand() {
        super("01 44");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [01 44] of the response
        float A = this.buffer.get(2);
        float B = this.buffer.get(3);
        this.afr = (((A * 256) + B) / 32768) * 14.7f;//((A*256)+B)/32768
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormattedResult() {
        return String.format("%.2f", getAirFuelRatio()) + ":1 AFR";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCalculatedResult() {
        return String.valueOf(getAirFuelRatio());
    }

    /**
     * <p>getAirFuelRatio.</p>
     *
     * @return a double.
     */
    public double getAirFuelRatio() {
        return this.afr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return AvailableCommandNames.AIR_FUEL_RATIO.getValue();
    }

}
