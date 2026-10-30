package dev.pupwired.mathesis.core.damage;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageCalculatorTest {

    private static final double EPS = 1e-9;
    private static final DefenseFormula FLAT = DefenseFormula.compile("damage - defense");
    private static final ToDoubleFunction<String> NO_STATS = stat -> 0;

    private static ToDoubleFunction<String> stats(Map<String, Double> values) {
        return stat -> values.getOrDefault(stat, 0.0);
    }

    @Test
    void noStatsMeansUnmodifiedDamage() {
        var r = DamageCalculator.calculate(Map.of("blunt", 10.0), NO_STATS, NO_STATS, FLAT);
        assertEquals(10, r.total(), EPS);
    }

    @Test
    void attackerOffenseAndDefenderResistMultiply() {
        var attacker = stats(Map.of("blunt_damage", 50.0));
        var defender = stats(Map.of("blunt_resist", 20.0));
        var r = DamageCalculator.calculate(Map.of("blunt", 10.0), attacker, defender, FLAT);
        assertEquals(12, r.total(), EPS); // 10 * 1.5 * 0.8
        assertEquals(12, r.beforeDefense(), EPS);
    }

    @Test
    void onlyTheMatchingTypesStatsApply() {
        var attacker = stats(Map.of("fire_damage", 100.0));
        var defender = stats(Map.of("edged_resist", 50.0));
        var r = DamageCalculator.calculate(Map.of("blunt", 10.0), attacker, defender, FLAT);
        assertEquals(10, r.total(), EPS);
    }

    @Test
    void multipleTypesSumAndDefenseAppliesOnce() {
        Map<String, Double> packet = new LinkedHashMap<>();
        packet.put("edged", 6.0);
        packet.put("fire", 6.0);
        var defender = stats(Map.of("defense", 5.0));
        var r = DamageCalculator.calculate(packet, NO_STATS, defender, FLAT);
        assertEquals(12, r.beforeDefense(), EPS);
        assertEquals(7, r.total(), EPS); // 12 - 5, not (6-5)+(6-5)
        assertEquals(6, r.perType().get("edged"), EPS);
    }

    @Test
    void defenseUsesTheConfiguredFormula() {
        var defender = stats(Map.of("defense", 50.0));
        var r = DamageCalculator.calculate(Map.of("blunt", 10.0), NO_STATS, defender, DefenseFormula.defaultFormula());
        assertEquals(5, r.total(), EPS);
    }

    @Test
    void negativeMultipliersClampToZeroInsteadOfHealing() {
        var attacker = stats(Map.of("blunt_damage", -150.0));
        var r = DamageCalculator.calculate(Map.of("blunt", 10.0), attacker, NO_STATS, FLAT);
        assertEquals(0, r.total(), EPS);
    }

    @Test
    void negativeResistanceIncreasesDamage() {
        var defender = stats(Map.of("fire_resist", -50.0));
        var r = DamageCalculator.calculate(Map.of("fire", 10.0), NO_STATS, defender, FLAT);
        assertEquals(15, r.total(), EPS);
    }

    @Test
    void nonPositiveAndNaNAmountsAreIgnored() {
        Map<String, Double> packet = new LinkedHashMap<>();
        packet.put("blunt", -5.0);
        packet.put("fire", Double.NaN);
        packet.put("edged", 4.0);
        var r = DamageCalculator.calculate(packet, NO_STATS, NO_STATS, FLAT);
        assertEquals(4, r.total(), EPS);
        assertEquals(1, r.perType().size());
    }

    @Test
    void emptyPacketDealsNothing() {
        var r = DamageCalculator.calculate(Map.of(), NO_STATS, NO_STATS, FLAT);
        assertEquals(0, r.total(), EPS);
    }
}
