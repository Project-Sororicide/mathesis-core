package dev.pupwired.mathesis.core.command;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.damage.DamageCalculator;
import dev.pupwired.mathesis.core.message.Message;
import dev.pupwired.mathesis.core.stats.StatMap;
import dev.pupwired.mathesis.core.stats.StatModifier;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MathesisCommand implements CommandExecutor, TabCompleter {

    private static final String DEV_SOURCE = "dev";

    private final MathesisPlugin plugin;

    public MathesisCommand(MathesisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return usage(sender, label);
        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            plugin.reload();
            Message.COMMAND_RELOADED.sendMessage(sender);
            return true;
        }
        if (sub.equals("give")) {
            give(sender, args);
            return true;
        }
        if (sub.equals("dev")) {
            dev(sender, label, args);
            return true;
        }
        return usage(sender, label);
    }

    /** /rpg give <player> <template> [amount]: works from console too, so shops and NPCs can run it. */
    private void give(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /rpg give <player> <item> [amount]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Player '" + args[1] + "' is not online.");
            return;
        }
        var template = plugin.items().get(args[2]).orElse(null);
        if (template == null) {
            sender.sendMessage("Unknown item '" + args[2] + "'.");
            return;
        }
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Amount must be a whole number.");
                return;
            }
            if (amount < 1 || amount > 64) {
                sender.sendMessage("Amount must be between 1 and 64.");
                return;
            }
        }
        ItemStack stack = plugin.itemFactory().create(template, amount);
        target.getInventory().addItem(stack).values()
                .forEach(left -> target.getWorld().dropItemNaturally(target.getLocation(), left));
        sender.sendMessage("Gave " + amount + "x " + template.id() + " to " + target.getName() + ".");
    }

    private void dev(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Message.COMMAND_SENDER_NOT_PLAYER.sendMessage(sender);
            return;
        }
        String sub = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "";

        if (sub.equals("armorspike")) {
            boolean on = plugin.armorSpike().toggle(player.getUniqueId());
            Message.COMMAND_DEV_ARMORSPIKE_TOGGLE.sendMessage(sender, on ? "enabled" : "disabled");
            return;
        }

        if (sub.equals("hitlog")) {
            boolean on = plugin.hitLog().toggle(player.getUniqueId());
            player.sendMessage("Hit log " + (on ? "enabled" : "disabled") + ".");
            return;
        }

        StatMap map = plugin.playerStats().of(player).orElse(null);
        if (map == null) {
            Message.COMMAND_DEV_MOD_STATMAP_MISSING.sendMessage(sender);
            return;
        }

        switch (sub) {
            case "stats" -> map.all().forEach((stat, value) ->
                    player.sendMessage(String.format(Locale.ROOT, "%s = %.3f", stat, value)));
            case "mod" -> addDevModifier(player, map, args);
            case "calc" -> calc(player, map, args);
            case "clearmods" -> {
                map.clearSource(DEV_SOURCE);
                Message.COMMAND_DEV_CLEARMOD_MODS_CLEARED.sendMessage(player);
            }
            default -> usage(player, label);
        }
    }

    /** /mathesis dev mod <stat> <flat|percent> <value>: stacks onto the "dev" source for manual testing. */
    private void addDevModifier(Player player, StatMap map, String[] args) {
        if (args.length < 5) {
            Message.COMMAND_DEV_MOD_USAGE.sendMessage(player);
            return;
        }
        String stat = args[2];
        if (!plugin.statRegistry().contains(stat)) {
            Message.COMMAND_DEV_MOD_STAT_UNKNOWN.sendMessage(player, stat);
            return;
        }
        StatModifier.Operation operation;
        switch (args[3].toLowerCase(Locale.ROOT)) {
            case "flat" -> operation = StatModifier.Operation.FLAT;
            case "percent" -> operation = StatModifier.Operation.PERCENT;
            default -> {
                Message.COMMAND_DEV_MOD_OPERATION_INVALID.sendMessage(player);
                return;
            }
        }
        double value;
        try {
            value = Double.parseDouble(args[4]);
            if (!Double.isFinite(value)) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Message.COMMAND_DEV_VALUE_NON_FINITE.sendMessage(player);
            return;
        }
        List<StatModifier> modifiers = new ArrayList<>(map.modifiersOf(DEV_SOURCE));
        modifiers.add(new StatModifier(stat, operation, value));
        map.setSource(DEV_SOURCE, modifiers);
        player.sendMessage(String.format(Locale.ROOT, "%s is now %.3f", stat.toLowerCase(Locale.ROOT), map.get(stat)));
    }

    /** /mathesis dev calc <type> <amount>: runs a hit of that type against YOUR stats with the current formula. */
    private void calc(Player player, StatMap map, String[] args) {
        if (args.length < 4) {
            Message.COMMAND_DEV_CALC_USAGE.sendMessage(player);
            return;
        }
        String type = args[2].toLowerCase(Locale.ROOT);
        if (plugin.damageTypes().get(type).isEmpty()) {
            Message.COMMAND_DEV_CALC_DAMAGE_TYPE_UNKNOWN.sendMessage(player, type);
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[3]);
            if (!Double.isFinite(amount)) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Message.COMMAND_DEV_VALUE_NON_FINITE.sendMessage(player);
            return;
        }
        var result = DamageCalculator.calculate(Map.of(type, amount), stat -> 0,
                stat -> map.getOrDefault(stat, 0), plugin.damageSettings().formula());
        player.sendMessage(String.format(Locale.ROOT, "%s %.2f -> %.2f after %s modifiers -> %.2f after defense",
                type, amount, result.beforeDefense(), type, result.total()));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("reload", "give", "dev");
        if (args[0].equalsIgnoreCase("give")) {
            String typed = args[args.length - 1].toLowerCase(Locale.ROOT);
            if (args.length == 2) return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName).filter(n -> n.toLowerCase(Locale.ROOT).startsWith(typed)).toList();
            if (args.length == 3) return plugin.items().all().stream()
                    .map(item -> item.id()).filter(id -> id.startsWith(typed)).toList();
            return List.of();
        }
        if (args[0].equalsIgnoreCase("dev")) {
            if (args.length == 2) return List.of("armorspike", "stats", "mod", "clearmods", "calc");
            if (args[1].equalsIgnoreCase("calc") && args.length == 3) return plugin.damageTypes().all().stream()
                    .map(type -> type.id())
                    .filter(id -> id.startsWith(args[2].toLowerCase(Locale.ROOT)))
                    .toList();
            if (args[1].equalsIgnoreCase("mod")) {
                if (args.length == 3) return plugin.statRegistry().all().stream()
                        .map(definition -> definition.id())
                        .filter(id -> id.startsWith(args[2].toLowerCase(Locale.ROOT)))
                        .toList();
                if (args.length == 4) return List.of("flat", "percent");
            }
        }
        return List.of();
    }

    private boolean usage(CommandSender sender, String label) {
        Message.COMMAND_USAGE.sendMessage(sender, label);
        return true;
    }
}
