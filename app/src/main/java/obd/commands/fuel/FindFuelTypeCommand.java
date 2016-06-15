package obd.commands.fuel;

import obd.commands.ObdCommand;
import obd.enums.AvailableCommandNames;
import obd.enums.FuelType;

/**
 * This command is intended to determine the vehicle fuel type.
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
     * @param other a {@link FindFuelTypeCommand} object.
     */
    public FindFuelTypeCommand(FindFuelTypeCommand other) {
        super(other);
    }

    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        fuelType = buffer.get(2);
    }

    @Override
    public String getFormattedResult() {
        try {
            return FuelType.fromValue(fuelType).getDescription();
        } catch (NullPointerException e) {
            return "-";
        }
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(fuelType);
    }

    @Override
    public String getName() {
        return AvailableCommandNames.FUEL_TYPE.getValue();
    }

}
