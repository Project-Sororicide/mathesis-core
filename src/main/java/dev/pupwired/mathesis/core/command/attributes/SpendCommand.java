package dev.pupwired.mathesis.core.command.attributes;

import dev.pupwired.luckylib.api.command.Completions;
import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.PlayerSubcommand;
import dev.pupwired.mathesis.core.message.Message;
import dev.pupwired.mathesis.core.rpcheck.CharacterAttribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/** /attributes spend <attribute> [amount] */
final class SpendCommand extends PlayerSubcommand {

    private final MathesisPlugin plugin;

    SpendCommand(MathesisPlugin plugin) { this.plugin = plugin; }

    @Override public String name() { return "spend"; }

    @Override
    protected void run(Player player, String label, String[] args) {
        if (args.length < 1) {
            Message.COMMAND_ATTRIBUTES_USAGE.sendMessage(player, label);
            return;
        }
        CharacterAttribute attribute;
        try {
            attribute = CharacterAttribute.fromConfig(args[0]);
        } catch (IllegalArgumentException e) {
            Message.COMMAND_ATTRIBUTES_UNKNOWN.sendMessage(player, args[0], String.join(", ", ids()));
            return;
        }
        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 1) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Message.COMMAND_ATTRIBUTES_AMOUNT_INVALID.sendMessage(player);
                return;
            }
        }
        if (!plugin.attributes().invest(player, attribute, amount)) {
            Message.COMMAND_ATTRIBUTES_NOT_ENOUGH_POINTS.sendMessage(player, plugin.attributes().of(player).unspent());
            return;
        }
        Message.COMMAND_ATTRIBUTES_SPENT.sendMessage(player, amount, attribute.label(),
                plugin.attributes().value(player, attribute));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) return Completions.filter(ids(), args[0]);
        return List.of();
    }

    private static List<String> ids() {
        return Arrays.stream(CharacterAttribute.values()).map(CharacterAttribute::id).toList();
    }
}