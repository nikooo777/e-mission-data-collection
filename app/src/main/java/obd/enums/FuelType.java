package obd.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * MODE 1 PID 0x51 will return one of the following values to identify the fuel
 * type of the vehicle.
 *
 * @author pires
 * @version $Id: $Id
 */
public enum FuelType {
    GASOLINE(0x01, "Gasoline"),
    METHANOL(0x02, "Methanol"),
    ETHANOL(0x03, "Ethanol"),
    DIESEL(0x04, "Diesel"),
    LPG(0x05, "GPL/LGP"),
    CNG(0x06, "Natural Gas"),
    PROPANE(0x07, "Propane"),
    ELECTRIC(0x08, "Electric"),
    BIFUEL_GASOLINE(0x09, "Biodiesel + Gasoline"),
    BIFUEL_METHANOL(0x0A, "Biodiesel + Methanol"),
    BIFUEL_ETHANOL(0x0B, "Biodiesel + Ethanol"),
    BIFUEL_LPG(0x0C, "Biodiesel + GPL/LGP"),
    BIFUEL_CNG(0x0D, "Biodiesel + Natural Gas"),
    BIFUEL_PROPANE(0x0E, "Biodiesel + Propane"),
    BIFUEL_ELECTRIC(0x0F, "Biodiesel + Electric"),
    BIFUEL_GASOLINE_ELECTRIC(0x10, "Biodiesel + Gasoline/Electric"),
    HYBRID_GASOLINE(0x11, "Hybrid Gasoline"),
    HYBRID_ETHANOL(0x12, "Hybrid Ethanol"),
    HYBRID_DIESEL(0x13, "Hybrid Diesel"),
    HYBRID_ELECTRIC(0x14, "Hybrid Electric"),
    HYBRID_MIXED(0x15, "Hybrid Mixed"),
    HYBRID_REGENERATIVE(0x16, "Hybrid Regenerative");

    /**
     * Constant <code>map</code>
     */
    private static Map<Integer, FuelType> map = new HashMap<>();

    static {
        for (FuelType error : FuelType.values())
            map.put(error.getValue(), error);
    }

    private final int value;
    private final String description;

    private FuelType(final int value, final String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * <p>fromValue.</p>
     *
     * @param value a int.
     * @return a {@link obd.enums.FuelType} object.
     */
    public static FuelType fromValue(final int value) {
        return map.get(value);
    }

    /**
     * <p>Getter for the field <code>value</code>.</p>
     *
     * @return a int.
     */
    public int getValue() {
        return this.value;
    }

    /**
     * <p>Getter for the field <code>description</code>.</p>
     *
     * @return a {@link String} object.
     */
    public String getDescription() {
        return this.description;
    }

}
