package dev.pupwired.mathesis.core.damage;

import dev.pupwired.mathesis.core.expression.Expression;

import java.util.List;

/**
 * The configurable defense formula. Variables: {@code damage} (damage left after per-type offense and
 * resistance) and {@code defense} (the defender's defense stat); the result is the damage actually dealt.
 */
public final class DefenseFormula {

    /** Diminishing returns: defense equal to 5x the hit halves it. */
    public static final String DEFAULT_SOURCE = "damage * (1 - defense / (5 * damage + defense))";

    private static final List<String> VARIABLES = List.of("damage", "defense");
    private static final double[] SAMPLE_DAMAGE = {0.5, 1, 10, 1000};
    private static final double[] SAMPLE_DEFENSE = {0, 1, 10, 1000};

    private final String source;
    private final Expression expression;

    private DefenseFormula(String source, Expression expression) {
        this.source = source;
        this.expression = expression;
    }

    /**
     * Compiles and sanity-checks a formula so mistakes surface at load time.
     * @throws IllegalArgumentException if it does not parse, or gives a non-finite result for ordinary inputs
     */
    public static DefenseFormula compile(String source) {
        Expression expression = Expression.compile(source, VARIABLES);
        for (double damage : SAMPLE_DAMAGE) {
            for (double defense : SAMPLE_DEFENSE) {
                double result = expression.evaluate(damage, defense);
                if (!Double.isFinite(result))
                    throw new IllegalArgumentException("Formula '" + source + "' gives " + result
                            + " for damage=" + damage + ", defense=" + defense);
            }
        }
        return new DefenseFormula(source, expression);
    }

    public static DefenseFormula defaultFormula() {
        return compile(DEFAULT_SOURCE);
    }

    public String source() {
        return source;
    }

    /**
     * @return damage after defense, never negative. Zero damage stays zero. If the formula yields a
     *         non-finite number for some unusual input, the hit goes through unmitigated rather than breaking combat.
     */
    public double apply(double damage, double defense) {
        if (!(damage > 0)) return 0;
        double result = expression.evaluate(damage, defense);
        if (!Double.isFinite(result)) return damage;
        return Math.max(0, result);
    }
}
