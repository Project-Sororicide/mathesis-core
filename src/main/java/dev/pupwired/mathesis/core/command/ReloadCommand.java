package dev.pupwired.mathesis.core.command;

import dev.pupwired.luckylib.api.command.Subcommand;
import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.message.Message;
import org.bukkit.command.CommandSender;

public final class ReloadCommand implements Subcommand {

    private final MathesisPlugin plugin;

    public ReloadCommand(MathesisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public String name() { return "reload"; }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        plugin.reload();
        Message.COMMAND_RELOADED.sendMessage(sender);
    }
}