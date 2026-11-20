package dev.pupwired.mathesis.core.command.attributes;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.attributes.AttributeSet;
import dev.pupwired.mathesis.core.command.PlayerSubcommand;
import dev.pupwired.mathesis.core.message.Message;
import dev.pupwired.mathesis.core.rpcheck.CharacterAttribute;
import dev.pupwired.mathesis.core.stats.StatFormat;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

final class ShowCommand extends PlayerSubcommand {

    private final MathesisPlugin plugin;

    ShowCommand(MathesisPlugin plugin) { this.plugin = plugin; }

    @Override public String name() { return "show"; }

    @Override
    protected void run(Player player, String label, String[] args) {
        AttributeSet set = plugin.attributes().of(player);
        Message.COMMAND_ATTRIBUTES_HEADER.sendMessage(player, set.unspent());
        for (CharacterAttribute attribute : CharacterAttribute.values()) {
            String perPoint = plugin.attributes().bonuses().perPoint(attribute).stream()
                    .map(m -> StatFormat.value(m) + " " + StatFormat.label(m.stat()))
                    .collect(Collectors.joining(", "));
            Message.COMMAND_ATTRIBUTES_LINE.sendMessage(player, attribute.label(), set.get(attribute),
                    perPoint.isEmpty() ? "no bonuses" : perPoint);
        }
    }
}