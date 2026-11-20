package dev.pupwired.mathesis.core.command.dev;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.PlayerSubcommand;
import org.bukkit.entity.Player;

final class HitlogCommand extends PlayerSubcommand {

    private final MathesisPlugin plugin;

    HitlogCommand(MathesisPlugin plugin) { this.plugin = plugin; }

    @Override public String name() { return "hitlog"; }

    @Override
    protected void run(Player player, String label, String[] args) {
        boolean on = plugin.hitLog().toggle(player.getUniqueId());
        player.sendMessage("Hit log " + (on ? "enabled" : "disabled") + ".");
    }
}