package obd.commands.protocol;

import obd.enums.ObdProtocols;

/**
 * Select the protocol to use.
 *
 * @author pires
 * @version $Id: $Id
 */
public class SelectProtocolCommand extends ObdProtocolCommand {

    private final ObdProtocols protocol;

    /**
     * <p>Constructor for SelectProtocolCommand.</p>
     *
     * @param protocol a {@link obd.enums.ObdProtocols} object.
     */
    public SelectProtocolCommand(final ObdProtocols protocol) {
        super("AT SP " + protocol.getValue());
        this.protocol = protocol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormattedResult() {
        return getResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Select Protocol " + this.protocol.name();
    }

}
