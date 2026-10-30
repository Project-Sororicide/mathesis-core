package dev.pupwired.mathesis.core.damage;

import dev.pupwired.mathesis.core.config.ConfigRegistry;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;
import java.util.logging.Logger;

public final class DamageTypeRegistry extends ConfigRegistry<DamageType> {

    public DamageTypeRegistry(Logger logger) {
        super(logger, "damage type");
    }

    @Override
    protected DamageType parse(String id, ConfigurationSection section) {
        String name = section.getString("name", id);
        String colorName = section.getString("color", "white").toLowerCase(Locale.ROOT);
        TextColor color = NamedTextColor.NAMES.value(colorName);
        if (color == null) color = NamedTextColor.WHITE;
        return new DamageType(id, name, color);
    }
}
