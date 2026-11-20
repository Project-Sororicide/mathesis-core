package dev.pupwired.mathesis.core.stats.bukkit;

import dev.pupwired.mathesis.core.stats.definitions.BuiltinStats;
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

    /** attribute value = offset + scale * stat value */
    private record Binding(Attribute attribute, double scale, double offset) {
        static Binding direct(Attribute attribute) { return new Binding(attribute, 1, 0); }

        double desired(double stat) { return offset + scale * stat; }
    }

    private static final Map<String, Binding> BINDINGS = Map.of(
            BuiltinStats.MAX_HEALTH, Binding.direct(Attribute.MAX_HEALTH),
            BuiltinStats.MOVEMENT_SPEED, Binding.direct(Attribute.MOVEMENT_SPEED),
            BuiltinStats.ATTACK_SPEED, Binding.direct(Attribute.ATTACK_SPEED),
            BuiltinStats.JUMP_STRENGTH, Binding.direct(Attribute.JUMP_STRENGTH),
            BuiltinStats.SAFE_FALL_DISTANCE, Binding.direct(Attribute.SAFE_FALL_DISTANCE),
            // 0-100 points -> 0-1
            BuiltinStats.KNOCKBACK_RESIST, new Binding(Attribute.KNOCKBACK_RESISTANCE, 0.01, 0),
            // resist 0 -> multiplier 1.0 (full fall damage), resist 100 -> 0.0 (none)
            BuiltinStats.FALL_RESIST, new Binding(Attribute.FALL_DAMAGE_MULTIPLIER, -0.01, 1)
    );

    private final Player player;
    private final NamespacedKey key;

    public AttributeBridge(Player player, NamespacedKey key) {
        this.player = player;
        this.key = key;
    }

    @Override
    public void onChange(StatMap map, String stat, double value) {
        Binding binding = BINDINGS.get(stat);
        if (binding == null) return;
        AttributeInstance instance = player.getAttribute(binding.attribute());
        if (instance == null) return;

        removeOurs(instance);
        double amount = binding.desired(value) - instance.getBaseValue();
        if (Math.abs(amount) > 1e-9) {
            instance.addTransientModifier(new AttributeModifier(
                    key, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
        }
    }

    /** Removes everything this bridge applied (player quit, plugin disable). */
    public void detach() {
        for (Binding binding : BINDINGS.values()) {
            AttributeInstance instance = player.getAttribute(binding.attribute());
            if (instance != null) removeOurs(instance);
        }
    }

    private void removeOurs(AttributeInstance instance) {
        for (AttributeModifier modifier : List.copyOf(instance.getModifiers())) {
            if (key.equals(modifier.getKey())) instance.removeModifier(modifier);
        }
    }
}
