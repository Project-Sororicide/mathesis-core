package dev.pupwired.mathesis.core.items;

import dev.pupwired.mathesis.core.damage.DamageType;
import dev.pupwired.mathesis.core.damage.DamageTypeRegistry;
import dev.pupwired.mathesis.core.stats.StatFormat;
import dev.pupwired.mathesis.core.stats.StatModifier;
import com.google.common.collect.ImmutableMultimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Builds item stacks from templates and recognizes them again by the template id stored in PDC. */
public final class ItemFactory {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final NamespacedKey key;
    private final ItemRegistry items;
    private final DamageTypeRegistry damageTypes;

    public ItemFactory(JavaPlugin plugin, ItemRegistry items, DamageTypeRegistry damageTypes) {
        this.key = new NamespacedKey(plugin, "item");
        this.items = items;
        this.damageTypes = damageTypes;
    }

    public ItemStack create(ItemTemplate template, int amount) {
        Material material = Material.matchMaterial(template.material());
        if (material == null) throw new IllegalStateException("Template '" + template.id() + "' has unknown material");

        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(plain(MINI.deserialize(template.name())));
        meta.lore(lore(template));
        // Replace the item's built-in attribute modifiers (armor, attack damage...) with none, so every number
        // comes from this plugin. Verify in game: raw vs final in /mathesis dev armorspike should match.
        meta.setAttributeModifiers(ImmutableMultimap.of());
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, template.id());
        stack.setItemMeta(meta);
        return stack;
    }

    /** The template a stack was created from, if it is one of ours and the template still exists. */
    public Optional<ItemTemplate> templateOf(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) return Optional.empty();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return Optional.empty();
        String id = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        return id == null ? Optional.empty() : items.get(id);
    }

    private List<Component> lore(ItemTemplate template) {
        List<Component> lines = new ArrayList<>();
        for (String line : template.lore()) lines.add(plain(MINI.deserialize(line)));

        if (template.isWeapon()) {
            DamageType type = damageTypes.get(template.damageType()).orElse(null);
            Component typeName = type == null
                    ? Component.text(template.damageType(), NamedTextColor.WHITE)
                    : Component.text(type.displayName(), type.color());
            lines.add(plain(Component.text("Damage: ", NamedTextColor.GRAY)
                    .append(Component.text(StatFormat.number(template.damage()) + " ", NamedTextColor.WHITE))
                    .append(typeName)));
        }
        if (template.attackSpeed() != null) {
            lines.add(plain(Component.text("Attack Speed: ", NamedTextColor.GRAY)
                    .append(Component.text(StatFormat.number(template.attackSpeed()), NamedTextColor.WHITE))));
        }
        for (StatModifier modifier : template.stats()) {
            lines.add(plain(Component.text(
                    StatFormat.value(modifier) + " " + StatFormat.label(modifier.stat()),
                    modifier.value() >= 0 ? NamedTextColor.GREEN : NamedTextColor.RED)));
        }
        return lines;
    }

    private static Component plain(Component component) {
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
