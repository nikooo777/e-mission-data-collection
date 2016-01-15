package obd.commands.engine;

import obd.commands.PercentageObdCommand;
import obd.enums.AvailableCommandNames;

/**
 * Calculated Engine Load value.
 */
public class LoadCommand extends PercentageObdCommand {

    public LoadCommand() {
        super("01 04");
    }

    /**
     * <p>Constructor for LoadCommand.</p>
     *
     * @param other a {@link LoadCommand} object.
     */
    public LoadCommand(LoadCommand other) {
        super(other);
    }

    /*
     * (non-Javadoc)
     *
     * @see pt.lighthouselabs.obd.commands.ObdCommand#getName()
     */
    @Override
    public String getName() {
        return AvailableCommandNames.ENGINE_LOAD.getValue();
    }

}
