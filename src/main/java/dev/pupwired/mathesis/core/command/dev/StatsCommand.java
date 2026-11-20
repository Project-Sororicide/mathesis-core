package dev.pupwired.mathesis.core.command.dev;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.StatSubcommand;
import dev.pupwired.mathesis.core.stats.StatMap;
import org.bukkit.entity.Player;

import java.util.Locale;

final class StatsCommand extends StatSubcommand {

    StatsCommand(MathesisPlugin plugin) { super(plugin); }

    @Override public String name() { return "stats"; }

    @Override
    protected void run(Player player, StatMap map, String[] args) {
        map.all().forEach((stat, value) ->
                player.sendMessage(String.format(Locale.ROOT, "%s = %.3f", stat, value)));
    }
}