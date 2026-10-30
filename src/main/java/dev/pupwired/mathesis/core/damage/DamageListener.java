package dev.pupwired.mathesis.core.damage;

import dev.pupwired.mathesis.core.dev.HitLog;
import dev.pupwired.mathesis.core.items.GearSlot;
import dev.pupwired.mathesis.core.items.ItemFactory;
import dev.pupwired.mathesis.core.items.ItemTemplate;
import dev.pupwired.mathesis.core.stats.BuiltinStats;
import dev.pupwired.mathesis.core.stats.DamageTypeStats;
import dev.pupwired.mathesis.core.stats.StatMap;
import dev.pupwired.mathesis.core.stats.bukkit.PlayerStatManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

/**
 * Runs the damage math on every hit that involves a player.
 *
 * <p>Starting amount: a direct melee hit with a plugin weapon in hand uses the weapon's own damage and damage
 * type, scaled by vanilla's attack-strength and crit factor. Everything else (mobs, fire, projectiles for now)
 * starts from the event's own damage, typed by the config mappings. Entities without stats (mobs) count as
 * zero offense, zero resistance and zero defense.
 */
public final class DamageListener implements Listener {

    private final DamageSettings settings;
    private final PlayerStatManager stats;
    private final ItemFactory items;
    private final HitLog hitLog;

    public DamageListener(DamageSettings settings, PlayerStatManager stats, ItemFactory items, HitLog hitLog) {
        this.settings = settings;
        this.stats = stats;
        this.items = items;
        this.hitLog = hitLog;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        Entity victim = event.getEntity();
        Entity attacker = DamageSourceResolver.attackerOf(event);
        if (!(victim instanceof Player) && !(attacker instanceof Player)) return;

        Optional<String> type = settings.resolver().typeOf(event);
        double amount = event.getDamage();
        String source = "event damage";

        if (attacker instanceof Player player && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            ItemTemplate weapon = items.templateOf(player.getInventory().getItemInMainHand())
                    .filter(t -> t.isWeapon() && t.slot() == GearSlot.MAIN_HAND)
                    .orElse(null);
            if (weapon != null) {
                type = Optional.of(weapon.damageType());
                amount = weapon.damage() * WeaponDamage.scale(event.getDamage(), attackDamageOf(player));
                source = "weapon " + weapon.id();
            }
        }
        if (type.isEmpty()) {
            if (hitLog.anyEnabled()) reportSkipped(event, attacker, victim);
            return;
        }

        ToDoubleFunction<String> attackerStats = statsOf(attacker);
        ToDoubleFunction<String> victimStats = statsOf(victim);
        DamageCalculator.Result result = DamageCalculator.calculate(
                Map.of(type.get(), amount), attackerStats, victimStats, settings.formula());
        event.setDamage(result.total());

        if (hitLog.anyEnabled()) report(event, attacker, victim, type.get(), amount, source, attackerStats, victimStats, result);
    }

    private static double attackDamageOf(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.ATTACK_DAMAGE);
        return attribute == null ? 1 : attribute.getValue();
    }

    private ToDoubleFunction<String> statsOf(Entity entity) {
        if (entity instanceof Player player) {
            StatMap map = stats.of(player).orElse(null);
            if (map != null) return stat -> map.getOrDefault(stat, 0);
        }
        return stat -> 0;
    }

    // ---- /mathesis dev hitlog ----

    private void report(EntityDamageEvent event, Entity attacker, Entity victim, String type, double amount,
                        String source, ToDoubleFunction<String> attackerStats, ToDoubleFunction<String> victimStats,
                        DamageCalculator.Result result) {
        String line = String.format(Locale.ROOT,
                "[hit] %s -> %s | %s %.2f (%s) | offense %+.0f%%, resist %+.0f%% -> %.2f | defense %.1f -> final %.2f",
                actorName(event, attacker), victim.getName(), type, amount, source,
                attackerStats.applyAsDouble(DamageTypeStats.offense(type)),
                victimStats.applyAsDouble(DamageTypeStats.resist(type)),
                result.beforeDefense(),
                victimStats.applyAsDouble(BuiltinStats.DEFENSE), result.total());
        send(line, attacker, victim);
    }

    private void reportSkipped(EntityDamageEvent event, Entity attacker, Entity victim) {
        String line = String.format(Locale.ROOT, "[hit] %s -> %s | %.2f, cause %s: no damage type mapped, left to vanilla",
                actorName(event, attacker), victim.getName(), event.getDamage(), event.getCause());
        send(line, attacker, victim);
    }

    private void send(String line, Entity attacker, Entity victim) {
        if (attacker instanceof Player a && hitLog.isEnabled(a.getUniqueId())) a.sendMessage(line);
        if (victim instanceof Player v && v != attacker && hitLog.isEnabled(v.getUniqueId())) v.sendMessage(line);
    }

    private static String actorName(EntityDamageEvent event, Entity attacker) {
        return attacker == null ? event.getCause().name() : attacker.getName();
    }
}
