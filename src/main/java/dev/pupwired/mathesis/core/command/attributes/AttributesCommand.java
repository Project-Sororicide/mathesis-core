package dev.pupwired.mathesis.core.command.attributes;

import dev.pupwired.luckylib.api.command.SubcommandRouter;
import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.message.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

/** /attributes [show | spend <attribute> [amount]] */
public final class AttributesCommand implements CommandExecutor, TabCompleter {

    private final SubcommandRouter router;

    public AttributesCommand(MathesisPlugin plugin) {
        this.router = new SubcommandRouter(new ShowCommand(plugin), new SpendCommand(plugin));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String[] effective = args.length == 0 ? new String[]{"show"} : args;
        if (!router.dispatch(sender, label, effective)) Message.COMMAND_ATTRIBUTES_USAGE.sendMessage(sender, label);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return router.complete(sender, args);
    }
}