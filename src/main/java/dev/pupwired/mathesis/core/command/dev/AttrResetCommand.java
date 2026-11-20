package dev.pupwired.mathesis.core.command.dev;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.PlayerSubcommand;
import dev.pupwired.mathesis.core.message.Message;
import org.bukkit.entity.Player;

/** /mathesis dev attrreset: refunds every spent attribute point. */
final class AttrResetCommand extends PlayerSubcommand {

    private final MathesisPlugin plugin;

    AttrResetCommand(MathesisPlugin plugin) { this.plugin = plugin; }

    @Override public String name() { return "attrreset"; }

    @Override
    protected void run(Player player, String label, String[] args) {
        plugin.attributes().reset(player);
        Message.COMMAND_DEV_ATTR_RESET.sendMessage(player, plugin.attributes().of(player).unspent());
    }
}