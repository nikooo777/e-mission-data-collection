package obd.commands.temperature;

import obd.enums.AvailableCommandNames;

/**
 * Temperature of intake air.
 */
public class AirIntakeTemperatureCommand extends TemperatureCommand {

    public AirIntakeTemperatureCommand() {
        super("01 0F");
    }

    /**
     * @param other a {@link AirIntakeTemperatureCommand} object.
     */
    public AirIntakeTemperatureCommand(AirIntakeTemperatureCommand other) {
        super(other);
    }

    @Override
    public String getName() {
        return AvailableCommandNames.AIR_INTAKE_TEMP.getValue();
    }

}
