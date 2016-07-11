package obd.commands.fuel;

import obd.commands.ObdCommand;
import obd.enums.AvailableCommandNames;
import obd.enums.FuelType;

/**
 * This command is intended to determine the vehicle fuel type.
 *
 * @author pires
 * @version $Id: $Id
 */
public class FindFuelTypeCommand extends ObdCommand {

    private int fuelType = 0;

    /**
     * Default ctor.
     */
    public FindFuelTypeCommand() {
        super("01 51");
    }

    /**
     * Copy ctor
     *
     * @param other a {@link obd.commands.fuel.FindFuelTypeCommand} object.
     */
    public FindFuelTypeCommand(FindFuelTypeCommand other) {
        super(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        this.fuelType = this.buffer.get(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormattedResult() {
        try {
            return FuelType.fromValue(this.fuelType).getDescription();
        } catch (NullPointerException e) {
            return "-";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCalculatedResult() {
        return String.valueOf(this.fuelType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return AvailableCommandNames.FUEL_TYPE.getValue();
    }

}
