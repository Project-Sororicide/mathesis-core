package dev.pupwired.mathesis.core.damage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefenseFormulaTest {

    private static final double EPS = 1e-9;

    @Test
    void defaultFormulaGivesDiminishingReturns() {
        DefenseFormula f = DefenseFormula.defaultFormula();
        assertEquals(10, f.apply(10, 0), EPS);
        assertEquals(5, f.apply(10, 50), EPS);      // defense = 5x the hit halves it
        assertTrue(f.apply(10, 1000) > 0);           // never fully immune
        assertTrue(f.apply(10, 100) < f.apply(10, 10));
    }

    @Test
    void bigHitsAreMitigatedLessThanSmallOnes() {
        DefenseFormula f = DefenseFormula.defaultFormula();
        double smallRatio = f.apply(5, 25) / 5;
        double bigRatio = f.apply(50, 25) / 50;
        assertTrue(bigRatio > smallRatio);
    }

    @Test
    void flatSubtractionFormulaIsClampedAtZero() {
        DefenseFormula f = DefenseFormula.compile("damage - defense");
        assertEquals(7, f.apply(10, 3), EPS);
        assertEquals(0, f.apply(10, 30), EPS);
    }

    @Test
    void zeroDamageStaysZero() {
        assertEquals(0, DefenseFormula.defaultFormula().apply(0, 10), EPS);
        assertEquals(0, DefenseFormula.compile("damage + 5").apply(0, 0), EPS);
    }

    @Test
    void compileRejectsFormulasThatBlowUpOnOrdinaryInputs() {
        var e = assertThrows(IllegalArgumentException.class, () -> DefenseFormula.compile("damage / defense"));
        assertTrue(e.getMessage().contains("non-finite") || e.getMessage().contains("Infinity") || e.getMessage().contains("NaN"));
    }

    @Test
    void compileRejectsUnknownVariablesAndSyntaxErrors() {
        assertThrows(IllegalArgumentException.class, () -> DefenseFormula.compile("damage - armor"));
        assertThrows(IllegalArgumentException.class, () -> DefenseFormula.compile("damage - (defense"));
    }

    @Test
    void unusualInputThatBreaksTheFormulaLetsTheHitThrough() {
        // finite for every sample input, but divides by zero when defense == 5
        DefenseFormula f = DefenseFormula.compile("damage / (defense - 5)");
        assertEquals(10, f.apply(10, 5), EPS);
    }

    @Test
    void sourceIsKept() {
        assertEquals("damage - defense", DefenseFormula.compile("damage - defense").source());
    }
}
