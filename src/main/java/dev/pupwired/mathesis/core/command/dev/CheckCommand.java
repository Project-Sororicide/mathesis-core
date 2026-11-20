package dev.pupwired.mathesis.core.command.dev;

import dev.pupwired.mathesis.core.MathesisPlugin;
import dev.pupwired.mathesis.core.command.Completions;
import dev.pupwired.mathesis.core.command.PlayerSubcommand;
import dev.pupwired.mathesis.core.message.Message;
import dev.pupwired.mathesis.core.rpcheck.RpCheck;
import dev.pupwired.mathesis.core.rpcheck.Skill;
import dev.pupwired.mathesis.core.stats.StatFormat;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/** /mathesis dev check <skill> <dc> [proficiency] [attribute]: rolls one RP check for testing. */
final class CheckCommand extends PlayerSubcommand {

    CheckCommand(MathesisPlugin plugin) {}

    @Override public String name() { return "check"; }

    @Override
    protected void run(Player player, String label, String[] args) {
        if (args.length < 2) {
            Message.COMMAND_DEV_CHECK_USAGE.sendMessage(player);
            return;
        }
        Skill skill;
        try {
            skill = Skill.fromConfig(args[0]);
        } catch (IllegalArgumentException e) {
            Message.COMMAND_DEV_CHECK_SKILL_UNKNOWN.sendMessage(player, args[0]);
            return;
        }
        int dc;
        try {
            dc = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Message.COMMAND_DEV_CHECK_DC_INVALID.sendMessage(player);
            return;
        }
        int proficiency = 0;
        try {
            if (args.length >= 3) proficiency = Integer.parseInt(args[2]);
            if (proficiency < RpCheck.MIN_PROFICIENCY || proficiency > RpCheck.MAX_PROFICIENCY)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Message.COMMAND_DEV_CHECK_PROFICIENCY_INVALID.sendMessage(player);
            return;
        }
        double attribute = 0;
        if (args.length >= 4) {
            try {
                attribute = Double.parseDouble(args[3]);
                if (!Double.isFinite(attribute)) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Message.COMMAND_DEV_VALUE_NON_FINITE.sendMessage(player);
                return;
            }
        }

        var rng = ThreadLocalRandom.current();
        var result = RpCheck.roll(proficiency, attribute, () -> rng.nextInt(1, RpCheck.SIDES + 1));
        boolean pass = RpCheck.passes(result, dc);
        String dice = result.autoFail() ? "automatic failure"
                : String.format(Locale.ROOT, "dice %s keep %s + %s (%s %s) = %s",
                result.rolled(), result.kept(), StatFormat.number(result.attributeBonus()),
                skill.attribute(), StatFormat.number(attribute), StatFormat.number(result.total()));
        player.sendMessage(String.format(Locale.ROOT, "[check] %s (prof %+d) vs DC %d | %s -> %s",
                skill.label(), result.proficiency(), dc, dice, pass ? "SUCCESS" : "FAILURE"));
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) return Completions.filter(
                Arrays.stream(Skill.values()).map(s -> s.name().toLowerCase(Locale.ROOT)).toList(), args[0]);
        return List.of();
    }
}