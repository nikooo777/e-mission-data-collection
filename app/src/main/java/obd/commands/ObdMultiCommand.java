package obd.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Container for multiple {@link obd.commands.ObdMultiCommand} instances.
 *
 * @author pires
 * @version $Id: $Id
 */
public class ObdMultiCommand {

    private ArrayList<ObdCommand> commands;

    /**
     * Default ctor.
     */
    public ObdMultiCommand() {
        this.commands = new ArrayList<>();
    }

    /**
     * Add ObdCommand to list of ObdCommands.
     *
     * @param command a {@link obd.commands.ObdCommand} object.
     */
    public void add(ObdCommand command) {
        this.commands.add(command);
    }

    /**
     * Removes ObdCommand from the list of ObdCommands.
     *
     * @param command a {@link obd.commands.ObdCommand} object.
     */
    public void remove(ObdCommand command) {
        this.commands.remove(command);
    }

    /**
     * Iterate all commands, send them and read response.
     *
     * @param in  a {@link InputStream} object.
     * @param out a {@link OutputStream} object.
     * @throws IOException          if any.
     * @throws InterruptedException if any.
     */
    public void sendCommands(InputStream in, OutputStream out)
            throws IOException, InterruptedException {
        for (ObdCommand command : this.commands)
            command.run(in, out);
    }

    /**
     * <p>getFormattedResult.</p>
     *
     * @return a {@link String} object.
     */
    public String getFormattedResult() {
        StringBuilder res = new StringBuilder();
        for (ObdCommand command : this.commands)
            res.append(command.getFormattedResult()).append(",");

        return res.toString();
    }

}
