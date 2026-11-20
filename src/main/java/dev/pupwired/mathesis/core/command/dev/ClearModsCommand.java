package dev.pupwired.mathesis.core.command.dev;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.StatSubcommand;
import dev.pupwired.mathesis.core.message.Message;
import dev.pupwired.mathesis.core.stats.StatMap;
import org.bukkit.entity.Player;

final class ClearModsCommand extends StatSubcommand {

    ClearModsCommand(MathesisPlugin plugin) { super(plugin); }

    @Override public String name() { return "clearmods"; }

    @Override
    protected void run(Player player, StatMap map, String[] args) {
        map.clearSource(DevCommand.SOURCE);
        Message.COMMAND_DEV_CLEARMOD_MODS_CLEARED.sendMessage(player);
    }
}