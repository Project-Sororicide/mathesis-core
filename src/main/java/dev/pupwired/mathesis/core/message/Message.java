package dev.pupwired.mathesis.core.message;

import dev.pupwired.luckylib.api.message.Color;
import dev.pupwired.luckylib.api.message.LuckyMessage;

public enum Message implements LuckyMessage {
    // --- Commands ---
    // Common
    COMMAND_USAGE("Usage: /%s dev <armorspike|stats|mod|clearmods|calc>", Color.WARNING),
    COMMAND_RELOADED("Mathesis reloaded.", Color.PASS),
    COMMAND_SENDER_NOT_PLAYER("Player only command.", Color.WARNING ),

    // Dev
    COMMAND_DEV_VALUE_NON_FINITE("Value must be a finite number.", Color.ERROR),

    COMMAND_DEV_ARMORSPIKE_TOGGLE("Armor spike %s.", Color.PASS),

    COMMAND_DEV_MOD_USAGE("Usage: /mathesis dev mod <stat> <flat|percent> <value>", Color.WARNING),
    COMMAND_DEV_MOD_STATMAP_MISSING("No stat map exists for user (try rejoining).", Color.ERROR),
    COMMAND_DEV_MOD_STAT_UNKNOWN("Unknown stat '%s'.", Color.ERROR),
    COMMAND_DEV_MOD_OPERATION_INVALID("Operation must be flat or percent.", Color.ERROR),

    COMMAND_DEV_CALC_USAGE("Usage: /mathesis dev calc <damage type> <amount>", Color.WARNING),
    COMMAND_DEV_CALC_DAMAGE_TYPE_UNKNOWN("Unknown damage type '%s'.", Color.ERROR),

    COMMAND_DEV_CLEARMOD_MODS_CLEARED("Dev modifiers cleared.", Color.PASS),

    COMMAND_DEV_CHECK_USAGE("Usage: /mathesis dev check <skill> <dc|difficulty> [proficiency] [attribute]", Color.WARNING),
    COMMAND_DEV_CHECK_SKILL_UNKNOWN("Unknown skill '%s'.", Color.ERROR),
    COMMAND_DEV_CHECK_DC_INVALID("DC must be a whole number.", Color.ERROR),
    COMMAND_DEV_CHECK_PROFICIENCY_INVALID("Proficiency must be a whole number from -3 to 2.", Color.ERROR),
    ;

    private final String string;
    private final Color color;

    Message(String string, Color color) {
        this.string = string;
        this.color = color;
    }

    public String getRaw() {
        return "<" + this.color + ">" + this.string + "</" + this.color + ">";
    }
}
