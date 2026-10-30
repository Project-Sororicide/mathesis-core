package dev.pupwired.mathesis.core.expression;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionTest {

    private static final double EPS = 1e-9;

    private static double eval(String source) {
        return Expression.compile(source, List.of()).evaluate();
    }

    private static double eval(String source, double damage, double defense) {
        return Expression.compile(source, List.of("damage", "defense")).evaluate(damage, defense);
    }

    @Test void precedence() { assertEquals(14, eval("2 + 3 * 4"), EPS); }
    @Test void parentheses() { assertEquals(20, eval("(2 + 3) * 4"), EPS); }
    @Test void subtractionIsLeftAssociative() { assertEquals(5, eval("10 - 3 - 2"), EPS); }
    @Test void divisionIsLeftAssociative() { assertEquals(2, eval("16 / 4 / 2"), EPS); }
    @Test void modulo() { assertEquals(1, eval("7 % 3"), EPS); }
    @Test void unaryMinus() { assertEquals(-3, eval("-3"), EPS); assertEquals(5, eval("--5"), EPS); }
    @Test void powerBindsTighterThanUnaryMinus() { assertEquals(-4, eval("-2^2"), EPS); }
    @Test void powerIsRightAssociative() { assertEquals(512, eval("2^3^2"), EPS); }
    @Test void powerAcceptsNegativeExponent() { assertEquals(0.5, eval("2^-1"), EPS); }
    @Test void decimalsAndExponents() {
        assertEquals(0.5, eval(".5"), EPS);
        assertEquals(1500, eval("1.5e3"), EPS);
        assertEquals(0.02, eval("2E-2"), EPS);
    }
    @Test void whitespaceIsIgnored() { assertEquals(7, eval("  1+  2 *3 "), EPS); }

    @Test
    void variablesAreCaseInsensitive() {
        assertEquals(15, eval("DAMAGE + Defense", 10, 5), EPS);
    }

    @Test
    void functions() {
        assertEquals(3, eval("min(3, 9)"), EPS);
        assertEquals(9, eval("max(3, 9)"), EPS);
        assertEquals(4, eval("abs(-4)"), EPS);
        assertEquals(3, eval("sqrt(9)"), EPS);
        assertEquals(8, eval("pow(2, 3)"), EPS);
        assertEquals(5, eval("clamp(7, 1, 5)"), EPS);
        assertEquals(1, eval("clamp(-7, 1, 5)"), EPS);
        assertEquals(2, eval("floor(2.9)"), EPS);
        assertEquals(3, eval("ceil(2.1)"), EPS);
        assertEquals(3, eval("round(2.5)"), EPS);
        assertEquals(1, eval("ln(2.718281828459045)"), 1e-9);
        assertEquals(3, eval("max(1, min(5, 3))"), EPS);
    }

    @Test
    void realDefenseFormulaEvaluates() {
        String formula = "damage * (1 - defense / (5 * damage + defense))";
        assertEquals(5, eval(formula, 10, 50), EPS);   // defense = 5x damage halves the hit
        assertEquals(10, eval(formula, 10, 0), EPS);
    }

    @Test
    void divisionByZeroIsNotAnError() {
        assertTrue(Double.isInfinite(eval("1 / 0")));
        assertTrue(Double.isNaN(eval("0 / 0")));
    }

    @Test
    void unknownVariableNamesTheAvailableOnes() {
        var e = assertThrows(IllegalArgumentException.class, () -> eval("damage + armor", 1, 1));
        assertTrue(e.getMessage().contains("armor"));
        assertTrue(e.getMessage().contains("damage, defense"));
    }

    @Test void unknownFunction() { assertThrows(IllegalArgumentException.class, () -> eval("frobnicate(1)")); }
    @Test void wrongArity() {
        assertThrows(IllegalArgumentException.class, () -> eval("min(1)"));
        assertThrows(IllegalArgumentException.class, () -> eval("abs(1, 2)"));
        assertThrows(IllegalArgumentException.class, () -> eval("clamp(1, 2)"));
    }
    @Test void unbalancedParentheses() {
        assertThrows(IllegalArgumentException.class, () -> eval("(1 + 2"));
        assertThrows(IllegalArgumentException.class, () -> eval("1 + 2)"));
    }
    @Test void trailingGarbage() { assertThrows(IllegalArgumentException.class, () -> eval("1 2")); }
    @Test void emptyAndDanglingOperators() {
        assertThrows(IllegalArgumentException.class, () -> eval(""));
        assertThrows(IllegalArgumentException.class, () -> eval("1 +"));
        assertThrows(IllegalArgumentException.class, () -> eval("* 2"));
    }
    @Test void badNumber() { assertThrows(IllegalArgumentException.class, () -> eval("1.2.3")); }

    @Test
    void evaluateRejectsWrongNumberOfValues() {
        Expression expression = Expression.compile("damage", List.of("damage", "defense"));
        assertThrows(IllegalArgumentException.class, () -> expression.evaluate(1));
    }

    @Test
    void errorMessagesCarryTheSourceAndPosition() {
        var e = assertThrows(IllegalArgumentException.class, () -> eval("1 + * 2"));
        assertTrue(e.getMessage().contains("1 + * 2"));
        assertTrue(e.getMessage().contains("position"));
    }
}
