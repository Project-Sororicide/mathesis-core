package dev.pupwired.mathesis.core.rpcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Pure RP check math. Roll 2d12, plus one extra d12 per point of proficiency. Positive proficiency keeps the
 * two highest dice, negative keeps the two lowest. Half the associated attribute is added to the kept dice.
 * At the minimum proficiency the check fails without rolling.
 */
public final class RpCheck {

    public static final int SIDES = 12;
    public static final int MIN_PROFICIENCY = -3;
    public static final int MAX_PROFICIENCY = 2;

    /**
     * @param rolled every die rolled, in roll order (empty on an automatic failure)
     * @param kept   the two dice that count
     * @param total  kept dice + attribute bonus (0 on an automatic failure)
     */
    public record Result(int proficiency, List<Integer> rolled, List<Integer> kept,
                         double attributeBonus, double total, boolean autoFail) {}

    private RpCheck() {}

    /** @param die supplies one d12 roll (1-12) per call, so tests can script the dice */
    public static Result roll(int proficiency, double attributeValue, IntSupplier die) {
        int prof = Math.max(MIN_PROFICIENCY, Math.min(MAX_PROFICIENCY, proficiency));
        if (prof == MIN_PROFICIENCY) return new Result(prof, List.of(), List.of(), 0, 0, true);

        List<Integer> rolled = new ArrayList<>();
        for (int i = 0; i < 2 + Math.abs(prof); i++) rolled.add(die.getAsInt());

        List<Integer> sorted = new ArrayList<>(rolled);
        Collections.sort(sorted);
        List<Integer> kept = prof >= 0
                ? List.of(sorted.get(sorted.size() - 2), sorted.get(sorted.size() - 1)) // two highest
                : List.of(sorted.get(0), sorted.get(1));                                // two lowest

        double bonus = attributeValue / 2.0;
        double total = kept.get(0) + kept.get(1) + bonus;
        return new Result(prof, List.copyOf(rolled), kept, bonus, total, false);
    }

    /** Against a fixed DC: meeting it is a success. */
    public static boolean passes(Result result, int dc) {
        return !result.autoFail() && result.total() >= dc;
    }

    /** Opposed roll: the attacker must beat the defender, so ties go to the defender. */
    public static boolean beats(Result attacker, Result defender) {
        if (attacker.autoFail()) return false;
        if (defender.autoFail()) return true;
        return attacker.total() > defender.total();
    }
}