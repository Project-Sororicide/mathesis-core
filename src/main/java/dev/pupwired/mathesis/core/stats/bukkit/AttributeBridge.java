package dev.pupwired.mathesis.core.stats.bukkit;

import dev.pupwired.mathesis.core.stats.BuiltinStats;
import dev.pupwired.mathesis.core.stats.StatListener;
import dev.pupwired.mathesis.core.stats.StatMap;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.List;
import java.util.Map;

/**
 * Pushes the attribute-bound stats onto a player's vanilla attributes as ONE keyed, transient modifier each:
 * amount = desired final value - the attribute's base value. Transient means it is never written to the
 * player's save data, so nothing is left behind if the plugin is removed or the server crashes.
 *
 * <p>attack_damage is deliberately not bound: the damage pipeline will compute damage from stats itself,
 * and binding it too would count it twice.
 */
public final class AttributeBridge implements StatListener {

    private static final Map<String, Attribute> BINDINGS = Map.of(
            BuiltinStats.MAX_HEALTH, Attribute.MAX_HEALTH,
            BuiltinStats.MOVEMENT_SPEED, Attribute.MOVEMENT_SPEED,
            BuiltinStats.ATTACK_SPEED, Attribute.ATTACK_SPEED
    );

    private final Player player;
    private final NamespacedKey key;

    public AttributeBridge(Player player, NamespacedKey key) {
        this.player = player;
        this.key = key;
    }

    @Override
    public void onChange(StatMap map, String stat, double value) {
        Attribute attribute = BINDINGS.get(stat);
        if (attribute == null) return;
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        removeOurs(instance);
        double amount = value - instance.getBaseValue();
        if (Math.abs(amount) > 1e-9) {
            instance.addTransientModifier(new AttributeModifier(
                    key, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
        }
    }

    /** Removes everything this bridge applied (player quit, plugin disable). */
    public void detach() {
        for (Attribute attribute : BINDINGS.values()) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) removeOurs(instance);
        }
    }

    private void removeOurs(AttributeInstance instance) {
        for (AttributeModifier modifier : List.copyOf(instance.getModifiers())) {
            if (key.equals(modifier.getKey())) instance.removeModifier(modifier);
        }
    }
}
