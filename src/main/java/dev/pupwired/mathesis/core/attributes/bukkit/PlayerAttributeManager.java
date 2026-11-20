package dev.pupwired.mathesis.core.attributes.bukkit;

import dev.pupwired.mathesis.core.attributes.AttributeBonuses;
import dev.pupwired.mathesis.core.attributes.AttributeSet;
import dev.pupwired.mathesis.core.attributes.AttributeSettings;
import dev.pupwired.mathesis.core.rpcheck.CharacterAttribute;
import dev.pupwired.mathesis.core.stats.StatMap;
import dev.pupwired.mathesis.core.stats.bukkit.PlayerStatManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Loads, saves and applies each online player's {@link AttributeSet}. Points live in the player's persistent
 * data, saved on every change. Main thread only.
 */
public final class PlayerAttributeManager implements Listener {

    private final PlayerStatManager stats;
    private final AttributeSettings settings;
    private final Logger logger;
    private final NamespacedKey grantedKey;
    private final Map<CharacterAttribute, NamespacedKey> investedKeys = new EnumMap<>(CharacterAttribute.class);
    private final Map<UUID, AttributeSet> loaded = new HashMap<>();

    public PlayerAttributeManager(JavaPlugin plugin, PlayerStatManager stats, AttributeSettings settings) {
        this.stats = stats;
        this.settings = settings;
        this.logger = plugin.getLogger();
        this.grantedKey = new NamespacedKey(plugin, "attribute_points");
        for (CharacterAttribute attribute : CharacterAttribute.values()) {
            investedKeys.put(attribute, new NamespacedKey(plugin, "attribute_" + attribute.id()));
        }
    }

    /** The player's live set. Read it freely; change it only through this manager so it is saved and applied. */
    public AttributeSet of(Player player) {
        return loaded.computeIfAbsent(player.getUniqueId(), id -> load(player));
    }

    /** Invested points in an attribute: the number RP checks halve and add. */
    public int value(Player player, CharacterAttribute attribute) {
        return of(player).get(attribute);
    }

    public AttributeBonuses bonuses() { return settings.bonuses(); }

    public void grant(Player player, int amount) {
        AttributeSet set = of(player);
        set.grant(amount);
        commit(player, set);
    }

    /** @return false if the player does not have enough unspent points */
    public boolean invest(Player player, CharacterAttribute attribute, int amount) {
        AttributeSet set = of(player);
        if (!set.invest(attribute, amount)) return false;
        commit(player, set);
        return true;
    }

    public void reset(Player player) {
        AttributeSet set = of(player);
        set.reset();
        commit(player, set);
    }

    /** Recomputes the player's {@code attributes} stat source from their points and the current bonuses. */
    public void apply(Player player) {
        StatMap map = stats.of(player).orElse(null);
        if (map == null) return;
        try {
            map.setSource(AttributeBonuses.SOURCE, settings.bonuses().modifiersFor(of(player)));
        } catch (IllegalArgumentException e) {
            logger.severe("Could not apply attribute bonuses to " + player.getName() + ": " + e.getMessage());
        }
    }

    /** After a reload, or after stat maps were rebuilt. */
    public void applyAll() {
        for (Player player : Bukkit.getOnlinePlayers()) apply(player);
    }

    private void commit(Player player, AttributeSet set) {
        save(player, set);
        apply(player);
    }

    private AttributeSet load(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        Map<CharacterAttribute, Integer> invested = new EnumMap<>(CharacterAttribute.class);
        for (CharacterAttribute attribute : CharacterAttribute.values()) {
            invested.put(attribute, data.getOrDefault(investedKeys.get(attribute), PersistentDataType.INTEGER, 0));
        }
        return new AttributeSet(data.getOrDefault(grantedKey, PersistentDataType.INTEGER, 0), invested);
    }

    private void save(Player player, AttributeSet set) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(grantedKey, PersistentDataType.INTEGER, set.granted());
        for (CharacterAttribute attribute : CharacterAttribute.values()) {
            data.set(investedKeys.get(attribute), PersistentDataType.INTEGER, set.get(attribute));
        }
    }

    // HIGH, so PlayerStatManager (NORMAL) has already attached the player's stat map
    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        apply(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        loaded.remove(event.getPlayer().getUniqueId());
    }
}