package dev.pupwired.mathesis.core.stats.bukkit;

import dev.pupwired.mathesis.core.stats.StatMap;
import dev.pupwired.mathesis.core.stats.StatRegistry;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Owns each online player's {@link StatMap} and its attribute bridge. Main thread only. */
public final class PlayerStatManager implements Listener {

    private record Entry(StatMap map, AttributeBridge bridge) {}

    private final StatRegistry registry;
    private final NamespacedKey key;
    private final Map<UUID, Entry> entries = new HashMap<>();

    public PlayerStatManager(JavaPlugin plugin, StatRegistry registry) {
        this.registry = registry;
        this.key = new NamespacedKey(plugin, "stats");
    }

    public Optional<StatMap> of(Player player) {
        Entry entry = entries.get(player.getUniqueId());
        return entry == null ? Optional.empty() : Optional.of(entry.map());
    }

    public void attach(Player player) {
        detach(player);
        StatMap map = new StatMap(registry);
        AttributeBridge bridge = new AttributeBridge(player, key);
        map.addListener(bridge);
        entries.put(player.getUniqueId(), new Entry(map, bridge));
        map.refresh(); // initial push of every stat
    }

    public void detach(Player player) {
        Entry entry = entries.remove(player.getUniqueId());
        if (entry != null) entry.bridge().detach();
    }

    /** For when the plugin is enabled while players are already online (e.g. /reload). */
    public void attachOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) attach(player);
    }

    public void detachAll() {
        for (Entry entry : entries.values()) entry.bridge().detach();
        entries.clear();
    }

    /** Call after the stat registry changed. */
    public void refreshAll() {
        for (Entry entry : entries.values()) entry.map().refresh();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        attach(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        detach(event.getPlayer());
    }
}
