package obd.commands;

/**
 * Abstract class for percentage commands.
 *
 * @author pires
 * @version $Id: $Id
 */
public abstract class PercentageObdCommand extends ObdCommand {

    protected float percentage = 0f;

    /**
     * <p>Constructor for PercentageObdCommand.</p>
     *
     * @param command a {@link String} object.
     */
    public PercentageObdCommand(String command) {
        super(command);
    }

    /**
     * <p>Constructor for PercentageObdCommand.</p>
     *
     * @param other a {@link obd.commands.PercentageObdCommand} object.
     */
    public PercentageObdCommand(PercentageObdCommand other) {
        super(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        this.percentage = (this.buffer.get(2) * 100.0f) / 255.0f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormattedResult() {
        return String.format("%.1f%s", this.percentage, getResultUnit());
    }

    /**
     * <p>Getter for the field <code>percentage</code>.</p>
     *
     * @return a float.
     */
    public float getPercentage() {
        return this.percentage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResultUnit() {
        return "%";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCalculatedResult() {
        return String.valueOf(this.percentage);
    }

}
