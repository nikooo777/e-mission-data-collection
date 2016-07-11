package obd.commands.fuel;

import obd.commands.PercentageObdCommand;
import obd.enums.FuelTrim;

/**
 * Fuel Trim.
 *
 * @author pires
 * @version $Id: $Id
 */
public class FuelTrimCommand extends PercentageObdCommand {

    private final FuelTrim bank;

    /**
     * Default ctor.
     * <p/>
     * Will read the bank from parameters and construct the command accordingly.
     * Please, see FuelTrim enum for more details.
     *
     * @param bank a {@link obd.enums.FuelTrim} object.
     */
    public FuelTrimCommand(final FuelTrim bank) {
        super(bank.buildObdCommand());
        this.bank = bank;
    }

    /**
     * <p>Constructor for FuelTrimCommand.</p>
     */
    public FuelTrimCommand() {
        this(FuelTrim.SHORT_TERM_BANK_1);
    }

    /**
     * @param value
     * @return
     */
    private float prepareTempValue(final int value) {
        return (value - 128) * (100.0F / 128);
    }

    /**
     * <p>performCalculations.</p>
     */
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        this.percentage = prepareTempValue(this.buffer.get(2));
    }

    /**
     * <p>getValue.</p>
     *
     * @return the readed Fuel Trim percentage value.
     * @deprecated use #getCalculatedResult()
     */
    public final float getValue() {
        return this.percentage;
    }

    /**
     * <p>Getter for the field <code>bank</code>.</p>
     *
     * @return the name of the bank in string representation.
     */
    public final String getBank() {
        return this.bank.getBank();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.bank.getBank();
    }

}
