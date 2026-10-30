package dev.pupwired.mathesis.core.damage;

import dev.pupwired.mathesis.core.items.GearSlot;
import dev.pupwired.mathesis.core.items.GearStats;
import dev.pupwired.mathesis.core.items.ItemTemplate;
import dev.pupwired.mathesis.core.items.StatValueParser;
import dev.pupwired.mathesis.core.stats.BuiltinStats;
import dev.pupwired.mathesis.core.stats.DamageTypeStats;
import dev.pupwired.mathesis.core.stats.StatDefinition;
import dev.pupwired.mathesis.core.stats.StatMap;
import dev.pupwired.mathesis.core.stats.StatModifier;
import dev.pupwired.mathesis.core.stats.StatRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Items.yml value -> item template -> gear modifiers -> stat map -> damage math, exactly the path a real hit takes,
 * using the registry the plugin builds (built-in stats plus two per damage type).
 */
class DamageEndToEndTest {

    private static final double EPS = 1e-9;

    private static StatMap playerWith(Object resistValueFromYaml) {
        StatRegistry registry = new StatRegistry();
        List<StatDefinition> definitions = new ArrayList<>(BuiltinStats.all());
        definitions.addAll(DamageTypeStats.definitionsFor("blunt", 90));
        registry.replaceAll(definitions);

        var vest = new ItemTemplate("riot_vest", "IRON_CHESTPLATE", "Riot Vest", GearSlot.CHEST, 0, null, null,
                List.of(StatValueParser.parse("blunt_resist", resistValueFromYaml)), List.of());
        StatMap map = new StatMap(registry);
        map.setSource("gear", GearStats.compute(Map.of(GearSlot.CHEST, vest)));
        return map;
    }

    private static double bluntHit10(StatMap defender) {
        return DamageCalculator.calculate(Map.of("blunt", 10.0), stat -> 0,
                stat -> defender.getOrDefault(stat, 0), DefenseFormula.compile("damage")).total();
    }

    @Test
    void percentStringFromYamlReducesDamage() {   // blunt_resist: '15%'
        StatMap player = playerWith("15%");
        assertEquals(15, player.get("blunt_resist"), EPS);
        assertEquals(8.5, bluntHit10(player), EPS);
    }

    @Test
    void plainNumberFromYamlReducesDamageTheSame() {   // blunt_resist: 15
        StatMap player = playerWith(15);
        assertEquals(15, player.get("blunt_resist"), EPS);
        assertEquals(8.5, bluntHit10(player), EPS);
    }

    @Test
    void flatAndPercentOnAResistStatBothCountAsPoints() {
        StatMap player = playerWith("15%");
        player.setSource("dev", List.of(StatModifier.flat("blunt_resist", 10)));
        assertEquals(25, player.get("blunt_resist"), EPS);
    }
}
