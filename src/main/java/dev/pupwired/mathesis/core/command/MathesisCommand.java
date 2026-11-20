package dev.pupwired.mathesis.core.command;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.dev.DevCommand;
import dev.pupwired.mathesis.core.message.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class MathesisCommand implements CommandExecutor, TabCompleter {

    private final SubcommandRouter router;

    public MathesisCommand(MathesisPlugin plugin) {
        this.router = new SubcommandRouter(
                new ReloadCommand(plugin),
                new GiveCommand(plugin),
                new DevCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!router.dispatch(sender, label, args)) Message.COMMAND_USAGE.sendMessage(sender, label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return router.complete(sender, args);
    }
}