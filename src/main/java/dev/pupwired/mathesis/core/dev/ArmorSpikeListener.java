package dev.pupwired.mathesis.core.dev;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Diagnostic for the "does vanilla armor still reduce damage?" question.
 * Toggle with /mathesis dev armorspike, get hit while wearing different gear, and compare
 * raw vs final damage. Only uses getDamage()/getFinalDamage(), which are stable API.
 * Remove once the damage pipeline owns damage calculation.
 */
public final class ArmorSpikeListener implements Listener {

    private final Set<UUID> enabled = ConcurrentHashMap.newKeySet();

    /** @return true if the spike is now enabled for this player */
    public boolean toggle(UUID id) {
        if (enabled.remove(id)) return false;
        enabled.add(id);
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !enabled.contains(player.getUniqueId())) return;
        double raw = event.getDamage();
        double fin = event.getFinalDamage();
        player.sendMessage(String.format("[armor spike] cause=%s raw=%.2f final=%.2f reduction=%.1f%%",
                event.getCause(), raw, fin, raw <= 0 ? 0 : (1 - fin / raw) * 100));
    }
}
