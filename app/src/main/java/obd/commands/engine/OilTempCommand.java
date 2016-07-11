package obd.commands.engine;

import obd.commands.temperature.TemperatureCommand;
import obd.enums.AvailableCommandNames;

/**
 * Displays the current engine Oil temperature.
 *
 * @author pires
 * @version $Id: $Id
 */
public class OilTempCommand extends TemperatureCommand {

    /**
     * Default ctor.
     */
    public OilTempCommand() {
        super("01 5C");
    }

    /**
     * Copy ctor.
     *
     * @param other a {@link obd.commands.engine.OilTempCommand} object.
     */
    public OilTempCommand(OilTempCommand other) {
        super(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return AvailableCommandNames.ENGINE_OIL_TEMP.getValue();
    }

}
