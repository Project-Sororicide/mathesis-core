package dev.pupwired.mathesis.core.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Optional;

/**
 * Decides which damage type a Bukkit damage event is, from config:
 * <ul>
 *   <li>a player's melee hit: by the item in their main hand ({@code weapons})</li>
 *   <li>any other entity, including projectiles: by entity type ({@code entities})</li>
 *   <li>no entity involved (fire, poison, falling...): by damage cause ({@code causes})</li>
 * </ul>
 * Entity-caused damage with no mapping falls back to {@code default-type}. Environmental damage with no
 * mapping is left alone (empty result), so vanilla handles it.
 */
public final class DamageSourceResolver {

    private final String defaultType; // nullable
    private final NameMap weapons;
    private final NameMap entities;
    private final NameMap causes;

    public DamageSourceResolver(String defaultType, NameMap weapons, NameMap entities, NameMap causes) {
        this.defaultType = defaultType;
        this.weapons = weapons;
        this.entities = entities;
        this.causes = causes;
    }

    public Optional<String> typeOf(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent byEntity) {
            Entity damager = byEntity.getDamager();
            if (damager instanceof Player player) {
                String weapon = player.getInventory().getItemInMainHand().getType().name();
                return Optional.ofNullable(weapons.find(weapon).orElse(defaultType));
            }
            return Optional.ofNullable(entities.find(damager.getType().name()).orElse(defaultType));
        }
        return causes.find(event.getCause().name());
    }

    /** The entity responsible for the damage (a projectile's shooter, otherwise the damager), or null. */
    public static Entity attackerOf(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent byEntity)) return null;
        Entity damager = byEntity.getDamager();
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) return shooter;
        return damager;
    }
}
