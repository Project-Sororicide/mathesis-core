package dev.pupwired.mathesis.core.stats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatMapTest {

    private static final double EPS = 1e-9;

    private StatRegistry registry;
    private StatMap map;
    private List<String> fired;

    @BeforeEach
    void setUp() {
        registry = new StatRegistry();
        registry.replaceAll(List.of(
                new StatDefinition("hp", 20, 1, 100),
                new StatDefinition("speed", 0.1, 0, 1024),
                new StatDefinition("blunt_resist", 0, -100, 90)
        ));
        map = new StatMap(registry);
        fired = new ArrayList<>();
        map.addListener((m, stat, value) -> fired.add(stat));
    }

    @Test
    void defaultsToBase() {
        assertEquals(20, map.get("hp"), EPS);
        assertEquals(0.1, map.get("speed"), EPS);
    }

    @Test
    void flatModifiersAdd() {
        map.setSource("class", List.of(StatModifier.flat("hp", 5)));
        map.setSource("gear", List.of(StatModifier.flat("hp", 3)));
        assertEquals(28, map.get("hp"), EPS);
    }

    @Test
    void percentModifiersAreAdditiveAcrossSources() {
        map.setSource("a", List.of(StatModifier.percent("hp", 10)));
        map.setSource("b", List.of(StatModifier.percent("hp", 10)));
        assertEquals(24, map.get("hp"), EPS); // 20 * 1.20, not 20 * 1.1 * 1.1
    }

    @Test
    void flatIsAppliedBeforePercent() {
        map.setSource("a", List.of(StatModifier.flat("hp", 10), StatModifier.percent("hp", 50)));
        assertEquals(45, map.get("hp"), EPS); // (20 + 10) * 1.5
    }

    @Test
    void valueIsClampedToDefinitionRange() {
        map.setSource("a", List.of(StatModifier.flat("hp", 500)));
        assertEquals(100, map.get("hp"), EPS);
        map.setSource("a", List.of(StatModifier.flat("hp", -500)));
        assertEquals(1, map.get("hp"), EPS);
    }

    @Test
    void percentBelowMinus100DoesNotFlipTheSign() {
        map.setSource("a", List.of(StatModifier.percent("speed", -150)));
        assertEquals(0, map.get("speed"), EPS);
    }

    @Test
    void setSourceReplacesPreviousModifiersFromThatSource() {
        map.setSource("class", List.of(StatModifier.flat("hp", 10)));
        map.setSource("class", List.of(StatModifier.flat("hp", 4)));
        assertEquals(24, map.get("hp"), EPS);
    }

    @Test
    void replacingASourceCanDropStatsItUsedToTouch() {
        map.setSource("class", List.of(StatModifier.flat("hp", 10), StatModifier.flat("speed", 0.05)));
        map.setSource("class", List.of(StatModifier.flat("hp", 10)));
        assertEquals(0.1, map.get("speed"), EPS);
        assertTrue(fired.contains("speed"));
    }

    @Test
    void clearSourceOnlyRemovesThatSource() {
        map.setSource("class", List.of(StatModifier.flat("hp", 10)));
        map.setSource("gear", List.of(StatModifier.flat("hp", 5)));
        map.clearSource("class");
        assertEquals(25, map.get("hp"), EPS);
        assertTrue(map.modifiersOf("class").isEmpty());
    }

    @Test
    void listenersFireOnlyForStatsThatChanged() {
        map.setSource("a", List.of(StatModifier.flat("hp", 5), StatModifier.flat("speed", 0)));
        assertEquals(List.of("hp"), fired); // speed +0 changed nothing
    }

    @Test
    void reapplyingIdenticalModifiersFiresNothing() {
        List<StatModifier> mods = List.of(StatModifier.flat("hp", 5));
        map.setSource("a", mods);
        fired.clear();
        map.setSource("a", mods);
        assertTrue(fired.isEmpty());
    }

    @Test
    void listenerReceivesTheNewFinalValue() {
        List<Double> values = new ArrayList<>();
        map.addListener((m, stat, value) -> values.add(value));
        map.setSource("a", List.of(StatModifier.flat("hp", 7)));
        assertEquals(List.of(27.0), values);
    }

    @Test
    void noNotificationWhenClampHidesTheChange() {
        map.setSource("a", List.of(StatModifier.flat("blunt_resist", 100)));
        fired.clear();
        map.setSource("b", List.of(StatModifier.flat("blunt_resist", 50)));
        assertEquals(90, map.get("blunt_resist"), EPS);
        assertTrue(fired.isEmpty());
    }

    @Test
    void unknownStatIsRejectedAndNothingChanges() {
        map.setSource("class", List.of(StatModifier.flat("hp", 10)));
        fired.clear();
        assertThrows(IllegalArgumentException.class, () -> map.setSource("class",
                List.of(StatModifier.flat("hp", 99), StatModifier.flat("typo_stat", 1))));
        assertEquals(30, map.get("hp"), EPS);
        assertTrue(fired.isEmpty());
    }

    @Test
    void getOnUnknownStatThrows() {
        assertThrows(IllegalArgumentException.class, () -> map.get("nope"));
    }

    @Test
    void getOrDefaultFallsBackOnlyForUnknownStats() {
        assertEquals(20, map.getOrDefault("hp", -1), EPS);
        assertEquals(-1, map.getOrDefault("nope", -1), EPS);
    }

    @Test
    void statIdsAreCaseInsensitive() {
        map.setSource("a", List.of(StatModifier.flat("HP", 5)));
        assertEquals(25, map.get("Hp"), EPS);
    }

    @Test
    void refreshNotifiesEveryRegisteredStat() {
        map.refresh();
        assertEquals(List.of("hp", "speed", "blunt_resist"), fired);
    }

    @Test
    void refreshPicksUpChangedDefinitions() {
        map.setSource("a", List.of(StatModifier.flat("hp", 5)));
        registry.replaceAll(List.of(new StatDefinition("hp", 30, 1, 100)));
        map.refresh();
        assertEquals(35, map.get("hp"), EPS);
    }

    @Test
    void modifiersForRemovedStatsStayInertAfterRefresh() {
        map.setSource("a", List.of(StatModifier.flat("speed", 0.2)));
        registry.replaceAll(List.of(new StatDefinition("hp", 20, 1, 100)));
        map.refresh();
        assertEquals(List.of("hp"), new ArrayList<>(map.all().keySet()));
        assertThrows(IllegalArgumentException.class, () -> map.get("speed"));
    }

    @Test
    void nonFiniteModifierValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> StatModifier.flat("hp", Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> StatModifier.percent("hp", Double.POSITIVE_INFINITY));
    }

    @Test
    void definitionRejectsInvertedRange() {
        assertThrows(IllegalArgumentException.class, () -> new StatDefinition("x", 0, 10, 5));
    }

    @Test
    void registryRejectsDuplicatesAndKeepsOldContents() {
        assertThrows(IllegalArgumentException.class, () -> registry.replaceAll(List.of(
                new StatDefinition("a", 0, 0, 1), new StatDefinition("A", 0, 0, 1))));
        assertTrue(registry.contains("hp"));
    }

    @Test
    void damageTypeStatsUseDerivedNamesAndCapResist() {
        var defs = DamageTypeStats.definitionsFor("fire", 75);
        assertEquals("fire_damage", defs.get(0).id());
        assertEquals("fire_resist", defs.get(1).id());
        assertEquals(75, defs.get(1).max(), EPS);
        assertEquals(-100, defs.get(1).min(), EPS);
    }
}
