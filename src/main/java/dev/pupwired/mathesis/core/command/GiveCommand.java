package dev.pupwired.mathesis.core.command;

import dev.pupwired.luckylib.api.command.Subcommand;
import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.items.ItemTemplate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/** /mathesis give <player> <item> [amount]: works from console too, so shops and NPCs can run it. */
public final class GiveCommand implements Subcommand {

    private final MathesisPlugin plugin;

    public GiveCommand(MathesisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public String name() { return "give"; }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /" + label + " give <player> <item> [amount]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("Player '" + args[0] + "' is not online.");
            return;
        }
        ItemTemplate template = plugin.items().get(args[1]).orElse(null);
        if (template == null) {
            sender.sendMessage("Unknown item '" + args[1] + "'.");
            return;
        }
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
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

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) return Completions.filter(
                Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[0]);
        if (args.length == 2) return Completions.filter(
                plugin.items().all().stream().map(ItemTemplate::id).toList(), args[1]);
        return List.of();
    }
}