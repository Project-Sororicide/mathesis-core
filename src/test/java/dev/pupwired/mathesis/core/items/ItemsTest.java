package dev.pupwired.mathesis.core.items;

import dev.pupwired.mathesis.core.stats.BuiltinStats;
import dev.pupwired.mathesis.core.stats.StatFormat;
import dev.pupwired.mathesis.core.stats.StatModifier;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ItemsTest {

    private static ItemTemplate armor(String id, GearSlot slot, StatModifier... stats) {
        return new ItemTemplate(id, "IRON_CHESTPLATE", id, slot, 0, null, null, List.of(stats), List.of());
    }

    @Test
    void parserReadsNumbersAsFlatAndPercentStringsAsPercent() {
        assertEquals(StatModifier.flat("defense", 5), StatValueParser.parse("defense", 5));
        assertEquals(StatModifier.flat("defense", 2.5), StatValueParser.parse("defense", 2.5));
        assertEquals(StatModifier.percent("blunt_resist", 10), StatValueParser.parse("blunt_resist", "10%"));
        assertEquals(StatModifier.percent("blunt_resist", -5), StatValueParser.parse("blunt_resist", " -5 % "));
        assertEquals(StatModifier.percent("blunt_resist", 10), StatValueParser.parse("blunt_resist", "+10%"));
        assertEquals(StatModifier.flat("max_health", 4), StatValueParser.parse("max_health", "4"));
    }

    @Test
    void parserRejectsGarbage() {
        assertThrows(IllegalArgumentException.class, () -> StatValueParser.parse("defense", "lots"));
        assertThrows(IllegalArgumentException.class, () -> StatValueParser.parse("defense", null));
        assertThrows(IllegalArgumentException.class, () -> StatValueParser.parse("defense", List.of(1)));
        assertThrows(IllegalArgumentException.class, () -> StatValueParser.parse("defense", "NaN"));
    }

    @Test
    void slotNamesAreCaseInsensitiveAndUnknownOnesExplainThemselves() {
        assertEquals(GearSlot.MAIN_HAND, GearSlot.fromConfig("HAND"));
        assertEquals(GearSlot.FEET, GearSlot.fromConfig("feet"));
        var e = assertThrows(IllegalArgumentException.class, () -> GearSlot.fromConfig("hat"));
        assertTrue(e.getMessage().contains("offhand"));
    }

    @Test
    void weaponNeedsDamageTypeAndMainHandSlot() {
        assertThrows(IllegalArgumentException.class, () ->
                new ItemTemplate("pipe", "IRON_SHOVEL", "Pipe", GearSlot.MAIN_HAND, 6, null, null, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                new ItemTemplate("pipe", "IRON_SHOVEL", "Pipe", GearSlot.OFF_HAND, 6, "blunt", null, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                new ItemTemplate("pipe", "IRON_SHOVEL", "Pipe", GearSlot.MAIN_HAND, -1, "blunt", null, List.of(), List.of()));
        var pipe = new ItemTemplate("PIPE", "IRON_SHOVEL", null, GearSlot.MAIN_HAND, 6, "Blunt", null, List.of(), List.of());
        assertTrue(pipe.isWeapon());
        assertEquals("pipe", pipe.id());
        assertEquals("blunt", pipe.damageType());
        assertEquals("pipe", pipe.name());
    }

    @Test
    void gearStatsCollectsOnlyItemsInTheirOwnSlot() {
        var vest = armor("vest", GearSlot.CHEST, StatModifier.flat("defense", 8));
        var boots = armor("boots", GearSlot.FEET, StatModifier.flat("movement_speed", 0.01));
        Map<GearSlot, ItemTemplate> equipped = new EnumMap<>(GearSlot.class);
        equipped.put(GearSlot.CHEST, vest);
        equipped.put(GearSlot.HEAD, boots); // boots on the head do nothing
        assertEquals(List.of(StatModifier.flat("defense", 8)), GearStats.compute(equipped));
    }

    @Test
    void gearStatsOrderIsStableSoEqualGearComparesEqual() {
        var vest = armor("vest", GearSlot.CHEST, StatModifier.flat("defense", 8));
        var boots = armor("boots", GearSlot.FEET, StatModifier.flat("defense", 1));
        Map<GearSlot, ItemTemplate> a = new EnumMap<>(GearSlot.class);
        a.put(GearSlot.FEET, boots);
        a.put(GearSlot.CHEST, vest);
        Map<GearSlot, ItemTemplate> b = new EnumMap<>(GearSlot.class);
        b.put(GearSlot.CHEST, vest);
        b.put(GearSlot.FEET, boots);
        assertEquals(GearStats.compute(a), GearStats.compute(b));
        assertEquals(List.of(), GearStats.compute(Map.of()));
    }

    @Test
    void statFormatting() {
        assertEquals("Max Health", StatFormat.label("max_health"));
        assertEquals("Blunt Resist", StatFormat.label("blunt_resist"));
        assertEquals("+10", StatFormat.value(StatModifier.flat("a", 10)));
        assertEquals("-3.5", StatFormat.value(StatModifier.flat("a", -3.5)));
        assertEquals("+15%", StatFormat.value(StatModifier.percent("a", 15)));
        assertEquals("+0.01", StatFormat.value(StatModifier.flat("a", 0.01)));
        assertEquals("+2.5%", StatFormat.value(StatModifier.percent("a", 2.5)));
    }

    @Test
    void attackSpeedMustBePositiveAndOnlyForHeldItems() {
        assertThrows(IllegalArgumentException.class, () ->
                new ItemTemplate("x", "IRON_SWORD", "x", GearSlot.MAIN_HAND, 5, "edged", 0.0, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                new ItemTemplate("x", "IRON_SWORD", "x", GearSlot.MAIN_HAND, 5, "edged", Double.NaN, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                new ItemTemplate("x", "IRON_CHESTPLATE", "x", GearSlot.CHEST, 0, null, 1.6, List.of(), List.of()));
        var ok = new ItemTemplate("x", "IRON_SWORD", "x", GearSlot.MAIN_HAND, 5, "edged", 1.6, List.of(), List.of());
        assertEquals(1.6, ok.attackSpeed(), 1e-9);
    }

    @Test
    void attackSpeedBecomesAFlatModifierRelativeToTheBaseOfFour() {
        var machete = new ItemTemplate("machete", "IRON_SWORD", "m", GearSlot.MAIN_HAND, 5, "edged", 1.6,
                List.of(StatModifier.flat("attack_speed", 0.2)), List.of());
        var mods = GearStats.compute(Map.of(GearSlot.MAIN_HAND, machete));
        assertEquals(2, mods.size());
        assertEquals(0.2, mods.get(0).value(), 1e-9);                         // listed stat first
        assertEquals(BuiltinStats.ATTACK_SPEED, mods.get(1).stat());
        assertEquals(-2.4, mods.get(1).value(), 1e-9);                        // 1.6 - 4.0
        assertEquals(StatModifier.Operation.FLAT, mods.get(1).operation());
    }

    @Test
    void attackSpeedOnlyCountsWhileHeldInTheMainHand() {
        var machete = new ItemTemplate("machete", "IRON_SWORD", "m", GearSlot.MAIN_HAND, 5, "edged", 1.6, List.of(), List.of());
        assertEquals(List.of(), GearStats.compute(Map.of(GearSlot.OFF_HAND, machete)));
    }

    @Test
    void finalAttackSpeedEndsUpAtTheTemplateValue() {
        // end to end through the stat engine: base 4.0 + (1.6 - 4.0) = 1.6
        var registry = new dev.pupwired.mathesis.core.stats.StatRegistry();
        registry.replaceAll(BuiltinStats.all());
        var map = new dev.pupwired.mathesis.core.stats.StatMap(registry);
        var machete = new ItemTemplate("machete", "IRON_SWORD", "m", GearSlot.MAIN_HAND, 5, "edged", 1.6, List.of(), List.of());
        map.setSource("gear", GearStats.compute(Map.of(GearSlot.MAIN_HAND, machete)));
        assertEquals(1.6, map.get(BuiltinStats.ATTACK_SPEED), 1e-9);
        map.clearSource("gear");
        assertEquals(4.0, map.get(BuiltinStats.ATTACK_SPEED), 1e-9);
    }
}
