package dev.pupwired.mathesis.core.damage;

import dev.pupwired.mathesis.core.stats.StatMap;
import dev.pupwired.mathesis.core.stats.bukkit.PlayerStatManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

/**
 * Runs the damage math on every hit that involves a player. For now the starting amount is the event's own
 * damage (vanilla weapon damage, cooldown and crits included); item-defined base damage comes with the item system.
 * Entities without stats (mobs) count as zero offense, zero resistance and zero defense.
 */
public final class DamageListener implements Listener {

    private final DamageSettings settings;
    private final PlayerStatManager stats;

    public DamageListener(DamageSettings settings, PlayerStatManager stats) {
        this.settings = settings;
        this.stats = stats;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        Entity attacker = DamageSourceResolver.attackerOf(event);
        if (!(victim instanceof Player) && !(attacker instanceof Player)) return;

        Optional<String> type = settings.resolver().typeOf(event);
        if (type.isEmpty()) return;

        DamageCalculator.Result result = DamageCalculator.calculate(
                Map.of(type.get(), event.getDamage()), statsOf(attacker), statsOf(victim), settings.formula());
        event.setDamage(result.total());
    }

    private ToDoubleFunction<String> statsOf(Entity entity) {
        if (entity instanceof Player player) {
            StatMap map = stats.of(player).orElse(null);
            if (map != null) return stat -> map.getOrDefault(stat, 0);
        }
        return stat -> 0;
    }
}
