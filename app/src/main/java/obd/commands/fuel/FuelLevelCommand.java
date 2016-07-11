package obd.commands.fuel;

import obd.commands.PercentageObdCommand;
import obd.enums.AvailableCommandNames;

/**
 * Get fuel level in percentage
 *
 * @author pires
 * @version $Id: $Id
 */
public class FuelLevelCommand extends PercentageObdCommand {

    /**
     * <p>Constructor for FuelLevelCommand.</p>
     */
    public FuelLevelCommand() {
        super("01 2F");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        this.percentage = 100.0f * this.buffer.get(2) / 255.0f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return AvailableCommandNames.FUEL_LEVEL.getValue();
    }

    /**
     * <p>getFuelLevel.</p>
     *
     * @return a float.
     */
    public float getFuelLevel() {
        return this.percentage;
    }

}
