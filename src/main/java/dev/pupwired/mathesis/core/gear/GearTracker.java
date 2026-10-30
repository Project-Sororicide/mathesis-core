package dev.pupwired.mathesis.core.gear;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import dev.pupwired.mathesis.core.items.GearSlot;
import dev.pupwired.mathesis.core.items.GearStats;
import dev.pupwired.mathesis.core.items.ItemTemplate;
import dev.pupwired.mathesis.core.stats.StatMap;
import dev.pupwired.mathesis.core.stats.StatModifier;
import dev.pupwired.mathesis.core.stats.bukkit.PlayerStatManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import dev.pupwired.mathesis.core.items.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Keeps each player's {@code gear} stat source in sync with what they hold and wear. Events trigger a
 * recompute on the next tick (at most one pending per player); a slow poll is a safety net for anything the
 * events miss. A recompute only touches the stat map when the resulting modifiers actually differ.
 */
public final class GearTracker implements Listener {

    public static final String SOURCE = "gear";
    private static final long POLL_TICKS = 20;

    private final JavaPlugin plugin;
    private final PlayerStatManager stats;
    private final ItemFactory items;
    private final Set<UUID> pending = new HashSet<>();

    public GearTracker(JavaPlugin plugin, PlayerStatManager stats, ItemFactory items) {
        this.plugin = plugin;
        this.stats = stats;
        this.items = items;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::recomputeAll, POLL_TICKS, POLL_TICKS);
        recomputeAll();
    }

    public void recomputeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) recompute(player);
    }

    public void recompute(Player player) {
        StatMap map = stats.of(player).orElse(null);
        if (map == null) return;

        PlayerInventory inventory = player.getInventory();
        Map<GearSlot, ItemTemplate> equipped = new EnumMap<>(GearSlot.class);
        put(equipped, GearSlot.MAIN_HAND, inventory.getItemInMainHand());
        put(equipped, GearSlot.OFF_HAND, inventory.getItemInOffHand());
        put(equipped, GearSlot.HEAD, inventory.getHelmet());
        put(equipped, GearSlot.CHEST, inventory.getChestplate());
        put(equipped, GearSlot.LEGS, inventory.getLeggings());
        put(equipped, GearSlot.FEET, inventory.getBoots());

        List<StatModifier> modifiers = GearStats.compute(equipped);
        if (!modifiers.equals(map.modifiersOf(SOURCE))) map.setSource(SOURCE, modifiers);
    }

    private void put(Map<GearSlot, ItemTemplate> equipped, GearSlot slot, ItemStack stack) {
        items.templateOf(stack).ifPresent(template -> equipped.put(slot, template));
    }

    private void schedule(Player player) {
        if (!pending.add(player.getUniqueId())) return;
        Bukkit.getScheduler().runTask(plugin, () -> {
            pending.remove(player.getUniqueId());
            if (player.isOnline()) recompute(player);
        });
    }

    @EventHandler public void onArmor(PlayerArmorChangeEvent e) { schedule(e.getPlayer()); }
    @EventHandler public void onHeld(PlayerItemHeldEvent e) { schedule(e.getPlayer()); }
    @EventHandler public void onSwap(PlayerSwapHandItemsEvent e) { schedule(e.getPlayer()); }
    @EventHandler public void onDrop(PlayerDropItemEvent e) { schedule(e.getPlayer()); }
    @EventHandler public void onBreak(PlayerItemBreakEvent e) { schedule(e.getPlayer()); }
    @EventHandler public void onJoin(PlayerJoinEvent e) { schedule(e.getPlayer()); }
    @EventHandler public void onRespawn(PlayerRespawnEvent e) { schedule(e.getPlayer()); }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player player) schedule(player);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player player) schedule(player);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player player) schedule(player);
    }
}
