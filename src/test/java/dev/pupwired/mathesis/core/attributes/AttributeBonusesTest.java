package dev.pupwired.mathesis.core.attributes;

import dev.pupwired.mathesis.core.stats.StatDefinition;
import dev.pupwired.mathesis.core.stats.StatMap;
import dev.pupwired.mathesis.core.stats.StatModifier;
import dev.pupwired.mathesis.core.stats.StatRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static dev.pupwired.mathesis.core.rpcheck.CharacterAttribute.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AttributeBonusesTest {

    private static final double EPS = 1e-9;

    private static final AttributeBonuses BONUSES = new AttributeBonuses(Map.of(
            MIGHT, List.of(StatModifier.flat("max_health", 2), StatModifier.percent("melee_damage", 1)),
            PRESENCE, List.of(StatModifier.percent("status_resist", 0.5))));

    private static AttributeSet set(int granted, Object... investments) {
        var set = new AttributeSet();
        set.grant(granted);
        for (int i = 0; i < investments.length; i += 2)
            set.invest((dev.pupwired.mathesis.core.rpcheck.CharacterAttribute) investments[i], (Integer) investments[i + 1]);
        return set;
    }

    @Test
    void perPointBonusesScaleWithInvestedPoints() {
        assertEquals(List.of(StatModifier.flat("max_health", 6), StatModifier.percent("melee_damage", 3)),
                BONUSES.modifiersFor(set(5, MIGHT, 3)));
    }

    @Test
    void attributesWithoutPointsContributeNothing() {
        assertEquals(List.of(), BONUSES.modifiersFor(set(5)));
        assertEquals(List.of(), BONUSES.modifiersFor(set(5, REFLEX, 2))); // no bonuses configured for reflex
    }

    @Test
    void orderFollowsAttributeOrderSoEqualSetsCompareEqual() {
        var a = BONUSES.modifiersFor(set(2, PRESENCE, 1, MIGHT, 1));
        var b = BONUSES.modifiersFor(set(2, MIGHT, 1, PRESENCE, 1));
        assertEquals(a, b);
        assertEquals("max_health", a.get(0).stat());
    }

    @Test
    void bonusesFlowIntoTheStatMapAndComeBackOut() {
        var registry = new StatRegistry();
        registry.replaceAll(List.of(
                new StatDefinition("max_health", 20, 1, 1024),
                new StatDefinition("melee_damage", 0, -100, Double.POSITIVE_INFINITY, true),
                new StatDefinition("status_resist", 0, 0, 50, true)));
        var map = new StatMap(registry);

        var points = set(5, MIGHT, 3);
        map.setSource(AttributeBonuses.SOURCE, BONUSES.modifiersFor(points));
        assertEquals(26, map.get("max_health"), EPS);
        assertEquals(3, map.get("melee_damage"), EPS);

        points.reset();
        map.setSource(AttributeBonuses.SOURCE, BONUSES.modifiersFor(points));
        assertEquals(20, map.get("max_health"), EPS);
        assertEquals(0, map.get("melee_damage"), EPS);
    }
}