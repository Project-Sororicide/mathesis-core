package dev.pupwired.mathesis.core.command.dev;

import dev.pupwired.luckylib.api.command.Completions;
import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.StatSubcommand;
import dev.pupwired.mathesis.core.damage.DamageCalculator;
import dev.pupwired.mathesis.core.damage.DamageType;
import dev.pupwired.mathesis.core.message.Message;
import dev.pupwired.mathesis.core.stats.StatMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** /mathesis dev calc <type> <amount>: runs a hit of that type against YOUR stats with the current formula. */
final class CalcCommand extends StatSubcommand {

    private final MathesisPlugin plugin;

    CalcCommand(MathesisPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override public String name() { return "calc"; }

    @Override
    protected void run(Player player, StatMap map, String[] args) {
        if (args.length < 2) {
            Message.COMMAND_DEV_CALC_USAGE.sendMessage(player);
            return;
        }
        String type = args[0].toLowerCase(Locale.ROOT);
        if (plugin.damageTypes().get(type).isEmpty()) {
            Message.COMMAND_DEV_CALC_DAMAGE_TYPE_UNKNOWN.sendMessage(player, type);
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (!Double.isFinite(amount)) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Message.COMMAND_DEV_VALUE_NON_FINITE.sendMessage(player);
            return;
        }
        var result = DamageCalculator.calculate(Map.of(type, amount), stat -> 0,
                stat -> map.getOrDefault(stat, 0), plugin.damageSettings().formula());
        player.sendMessage(String.format(Locale.ROOT, "%s %.2f -> %.2f after %s modifiers -> %.2f after defense",
                type, amount, result.beforeDefense(), type, result.total()));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) return Completions.filter(
                plugin.damageTypes().all().stream().map(DamageType::id).toList(), args[0]);
        return List.of();
    }
}