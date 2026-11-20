package dev.pupwired.mathesis.core.command.dev;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.PlayerSubcommand;
import dev.pupwired.mathesis.core.message.Message;
import org.bukkit.entity.Player;

final class ArmorSpikeCommand extends PlayerSubcommand {

    private final MathesisPlugin plugin;

    ArmorSpikeCommand(MathesisPlugin plugin) { this.plugin = plugin; }

    @Override public String name() { return "armorspike"; }

    @Override
    protected void run(Player player, String label, String[] args) {
        boolean on = plugin.armorSpike().toggle(player.getUniqueId());
        Message.COMMAND_DEV_ARMORSPIKE_TOGGLE.sendMessage(player, on ? "enabled" : "disabled");
    }
}