package dev.pupwired.mathesis.core.rpcheck;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RpCheckTest {

    private static final double EPS = 1e-9;

    private static java.util.function.IntSupplier dice(Integer... values) {
        Iterator<Integer> it = List.of(values).iterator();
        return it::next;
    }

    @Test
    void unproficientRollsTwoDiceAndAddsHalfTheAttribute() {
        var r = RpCheck.roll(0, 5, dice(3, 9));
        assertEquals(List.of(3, 9), r.kept());
        assertEquals(14.5, r.total(), EPS); // 3 + 9 + 2.5
    }

    @Test
    void positiveProficiencyKeepsTheTwoHighest() {
        var r = RpCheck.roll(2, 0, dice(4, 12, 1, 7));
        assertEquals(4, r.rolled().size());
        assertEquals(List.of(7, 12), r.kept());
        assertEquals(19, r.total(), EPS);
    }

    @Test
    void negativeProficiencyKeepsTheTwoLowest() {
        var r = RpCheck.roll(-2, 0, dice(4, 12, 1, 7));
        assertEquals(List.of(1, 4), r.kept());
        assertEquals(5, r.total(), EPS);
    }

    @Test
    void minimumProficiencyFailsWithoutRolling() {
        var r = RpCheck.roll(-3, 40, () -> { throw new AssertionError("must not roll"); });
        assertTrue(r.autoFail());
        assertFalse(RpCheck.passes(r, 0));
    }

    @Test
    void proficiencyIsClampedToTheAllowedRange() {
        assertEquals(4, RpCheck.roll(9, 0, dice(1, 2, 3, 4)).rolled().size());
        assertTrue(RpCheck.roll(-9, 0, dice()).autoFail());
    }

    @Test
    void meetingTheDcPasses() {
        var r = RpCheck.roll(0, 0, dice(7, 8));
        assertTrue(RpCheck.passes(r, 15));
        assertFalse(RpCheck.passes(r, 16));
    }

    @Test
    void opposedTiesGoToTheDefender() {
        var a = RpCheck.roll(0, 0, dice(5, 5));
        var d = RpCheck.roll(0, 0, dice(4, 6));
        assertFalse(RpCheck.beats(a, d));
        assertTrue(RpCheck.beats(RpCheck.roll(0, 2, dice(5, 5)), d));
    }

    @Test
    void opposedAutoFailLosesToAnythingButBothAutoFailingLoses() {
        var fail = RpCheck.roll(-3, 0, dice());
        var normal = RpCheck.roll(0, 0, dice(1, 1));
        assertTrue(RpCheck.beats(normal, fail));
        assertFalse(RpCheck.beats(fail, normal));
        assertFalse(RpCheck.beats(fail, fail));
    }

    @Test
    void skillLookupAcceptsFriendlyNames() {
        assertEquals(Skill.QUICK_HANDS, Skill.fromConfig("Quick Hands"));
        assertEquals(Skill.QUICK_HANDS, Skill.fromConfig("quick-hands"));
        assertEquals("Quick Hands", Skill.QUICK_HANDS.label());
        assertThrows(IllegalArgumentException.class, () -> Skill.fromConfig("juggling"));
        assertEquals(CharacterAttribute.WITS, Skill.HACKING.attribute());
    }

    @Test
    void proficienciesSumAcrossSourcesAndClamp() {
        var p = new SkillProficiencies();
        p.setSource("race", Map.of(Skill.ENDURANCE, -1, Skill.LEADERSHIP, 1));
        p.setSource("free", Map.of(Skill.LEADERSHIP, 1));
        p.setSource("feats", Map.of(Skill.LEADERSHIP, 5));
        assertEquals(RpCheck.MAX_PROFICIENCY, p.get(Skill.LEADERSHIP));
        assertEquals(-1, p.get(Skill.ENDURANCE));
        p.setSource("feats", Map.of());
        assertEquals(2, p.get(Skill.LEADERSHIP)); // 1 + 1
        assertEquals(0, p.get(Skill.STEALTH));
    }
}