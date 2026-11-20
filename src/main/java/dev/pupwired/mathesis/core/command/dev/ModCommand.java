package dev.pupwired.mathesis.core.command.dev;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.Completions;
import dev.pupwired.mathesis.core.command.StatSubcommand;
import dev.pupwired.mathesis.core.message.Message;
import dev.pupwired.mathesis.core.stats.StatDefinition;
import dev.pupwired.mathesis.core.stats.StatMap;
import dev.pupwired.mathesis.core.stats.StatModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** /mathesis dev mod <stat> <flat|percent> <value>: stacks onto the "dev" source for manual testing. */
final class ModCommand extends StatSubcommand {

    private final MathesisPlugin plugin;

    ModCommand(MathesisPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override public String name() { return "mod"; }

    @Override
    protected void run(Player player, StatMap map, String[] args) {
        if (args.length < 3) {
            Message.COMMAND_DEV_MOD_USAGE.sendMessage(player);
            return;
        }
        String stat = args[0];
        if (!plugin.statRegistry().contains(stat)) {
            Message.COMMAND_DEV_MOD_STAT_UNKNOWN.sendMessage(player, stat);
            return;
        }
        StatModifier.Operation operation;
        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "flat" -> operation = StatModifier.Operation.FLAT;
            case "percent" -> operation = StatModifier.Operation.PERCENT;
            default -> {
                Message.COMMAND_DEV_MOD_OPERATION_INVALID.sendMessage(player);
                return;
            }
        }
        double value;
        try {
            value = Double.parseDouble(args[2]);
            if (!Double.isFinite(value)) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Message.COMMAND_DEV_VALUE_NON_FINITE.sendMessage(player);
            return;
        }
        List<StatModifier> modifiers = new ArrayList<>(map.modifiersOf(DevCommand.SOURCE));
        modifiers.add(new StatModifier(stat, operation, value));
        map.setSource(DevCommand.SOURCE, modifiers);
        player.sendMessage(String.format(Locale.ROOT, "%s is now %.3f", stat.toLowerCase(Locale.ROOT), map.get(stat)));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) return Completions.filter(
                plugin.statRegistry().all().stream().map(StatDefinition::id).toList(), args[0]);
        if (args.length == 2) return Completions.filter(List.of("flat", "percent"), args[1]);
        return List.of();
    }
}