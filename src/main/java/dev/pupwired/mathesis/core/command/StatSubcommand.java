package dev.pupwired.mathesis.core.command;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.message.Message;
import dev.pupwired.mathesis.core.stats.StatMap;
import org.bukkit.entity.Player;

/** A player-only subcommand that also needs the player's stat map. */
public abstract class StatSubcommand extends PlayerSubcommand {

    private final MathesisPlugin plugin;

    protected StatSubcommand(MathesisPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected final void run(Player player, String label, String[] args) {
        StatMap map = plugin.playerStats().of(player).orElse(null);
        if (map == null) {
            Message.COMMAND_DEV_MOD_STATMAP_MISSING.sendMessage(player);
            return;
        }
        run(player, map, args);
    }

    protected abstract void run(Player player, StatMap map, String[] args);
}