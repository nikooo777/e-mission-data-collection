package obd.commands.control;

import obd.commands.ObdCommand;
import obd.commands.SystemOfUnits;
import obd.enums.AvailableCommandNames;

/**
 * Distance traveled since codes cleared-up.
 *
 * @author pires
 * @version $Id: $Id
 */
public class DistanceSinceCCCommand extends ObdCommand
        implements SystemOfUnits {

    private int km = 0;

    /**
     * Default ctor.
     */
    public DistanceSinceCCCommand() {
        super("01 31");
    }

    /**
     * Copy ctor.
     *
     * @param other a {@link obd.commands.control.DistanceSinceCCCommand} object.
     */
    public DistanceSinceCCCommand(
            DistanceSinceCCCommand other) {
        super(other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [01 31] of the response
        this.km = this.buffer.get(2) * 256 + this.buffer.get(3);
    }

    /**
     * <p>getFormattedResult.</p>
     *
     * @return a {@link String} object.
     */
    public String getFormattedResult() {
        return this.useImperialUnits ? String.format("%.2f%s", getImperialUnit(), getResultUnit())
                : String.format("%d%s", this.km, getResultUnit());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCalculatedResult() {
        return this.useImperialUnits ? String.valueOf(getImperialUnit()) : String.valueOf(this.km);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResultUnit() {
        return this.useImperialUnits ? "m" : "km";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getImperialUnit() {
        return this.km * 0.621371192F;
    }

    /**
     * <p>Getter for the field <code>km</code>.</p>
     *
     * @return a int.
     */
    public int getKm() {
        return this.km;
    }

    /**
     * <p>Setter for the field <code>km</code>.</p>
     *
     * @param km a int.
     */
    public void setKm(int km) {
        this.km = km;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return AvailableCommandNames.DISTANCE_TRAVELED_AFTER_CODES_CLEARED
                .getValue();
    }

}
