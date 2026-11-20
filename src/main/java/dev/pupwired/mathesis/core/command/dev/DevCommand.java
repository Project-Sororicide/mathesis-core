package dev.pupwired.mathesis.core.command.dev;

import dev.pupwired.luckylib.api.command.Subcommand;
import dev.pupwired.luckylib.api.command.SubcommandRouter;
import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.message.Message;
import org.bukkit.command.CommandSender;

import java.util.List;

/** /mathesis dev ...: testing tools. Each child lives in its own class; add new ones to the router below. */
public final class DevCommand implements Subcommand {

    /** Stat source that /mathesis dev mod writes to and clearmods clears. */
    static final String SOURCE = "dev";

    private final SubcommandRouter router;

    public DevCommand(MathesisPlugin plugin) {
        this.router = new SubcommandRouter(
                new ArmorSpikeCommand(plugin),
                new HitlogCommand(plugin),
                new StatsCommand(plugin),
                new ModCommand(plugin),
                new ClearModsCommand(plugin),
                new CalcCommand(plugin),
                new CheckCommand(plugin),
                new AttrGrantCommand(plugin),
                new AttrResetCommand(plugin)
        );
    }

    @Override public String name() { return "dev"; }

    @Override
    public void execute(CommandSender sender, String label, String[] args) {
        if (!router.dispatch(sender, label, args)) Message.COMMAND_USAGE.sendMessage(sender, label);
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return router.complete(sender, args);
    }
}