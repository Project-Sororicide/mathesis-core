package dev.pupwired.mathesis.core.command;

import dev.pupwired.mathesis.core.message.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** A subcommand that only players can run. */
public abstract class PlayerSubcommand implements Subcommand {

    @Override
    public final void execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Message.COMMAND_SENDER_NOT_PLAYER.sendMessage(sender);
            return;
        }
        run(player, label, args);
    }

    protected abstract void run(Player player, String label, String[] args);
}