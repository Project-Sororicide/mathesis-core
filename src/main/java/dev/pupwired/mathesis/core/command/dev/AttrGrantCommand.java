package dev.pupwired.mathesis.core.command.dev;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.PlayerSubcommand;
import dev.pupwired.mathesis.core.message.Message;
import org.bukkit.entity.Player;

/** /mathesis dev attrgrant <amount>: gives yourself attribute points, standing in for level-ups. */
final class AttrGrantCommand extends PlayerSubcommand {

    private final MathesisPlugin plugin;

    AttrGrantCommand(MathesisPlugin plugin) { this.plugin = plugin; }

    @Override public String name() { return "attrgrant"; }

    @Override
    protected void run(Player player, String label, String[] args) {
        if (args.length < 1) {
            Message.COMMAND_DEV_ATTR_USAGE.sendMessage(player);
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
            if (amount < 1) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Message.COMMAND_ATTRIBUTES_AMOUNT_INVALID.sendMessage(player);
            return;
        }
        plugin.attributes().grant(player, amount);
        Message.COMMAND_DEV_ATTR_GRANTED.sendMessage(player, amount, plugin.attributes().of(player).unspent());
    }
}