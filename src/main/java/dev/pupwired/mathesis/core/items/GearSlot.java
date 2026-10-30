package dev.pupwired.mathesis.core.items;

/** Where an item must be worn or held for its stats to count. */
public enum GearSlot {
    MAIN_HAND("hand"), OFF_HAND("offhand"), HEAD("head"), CHEST("chest"), LEGS("legs"), FEET("feet");

    private final String configName;

    GearSlot(String configName) {
        this.configName = configName;
    }

    public static GearSlot fromConfig(String name) {
        for (GearSlot slot : values()) {
            if (slot.configName.equalsIgnoreCase(name)) return slot;
        }
        throw new IllegalArgumentException("unknown slot '" + name + "' (use hand, offhand, head, chest, legs or feet)");
    }
}
