package obd.commands.protocol;

import obd.commands.PersistentCommand;
import obd.enums.AvailableCommandNames;

/**
 * Retrieve available PIDs.
 */
public class AvailablePidsCommand extends PersistentCommand {

    /**
     * Default ctor.
     */
    public AvailablePidsCommand() {
        super("01 00");
    }

    /**
     * Copy ctor.
     *
     * @param other a {@link AvailablePidsCommand} object.
     */
    public AvailablePidsCommand(AvailablePidsCommand other) {
        super(other);
    }

    @Override
    protected void performCalculations() {

    }

    @Override
    public String getName() {
        return AvailableCommandNames.PIDS.getValue();
    }

    @Override
    public String getFormattedResult() {
        return getCalculatedResult();
    }

    @Override
    public String getCalculatedResult() {
        return String.valueOf(rawData);
    }
}
