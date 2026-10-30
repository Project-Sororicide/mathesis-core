package dev.pupwired.mathesis.core.items;

import dev.pupwired.mathesis.core.damage.WeaponDamage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WeaponDamageTest {

    private static final double EPS = 1e-9;

    @Test void fullSwingIsFullDamage() { assertEquals(1.0, WeaponDamage.scale(1.0, 1.0), EPS); }
    @Test void spamClickedSwingIsScaledDown() { assertEquals(0.2, WeaponDamage.scale(0.2, 1.0), EPS); }
    @Test void critKeepsItsMultiplier() { assertEquals(1.5, WeaponDamage.scale(1.5, 1.0), EPS); }
    @Test void scalingIsRelativeToTheAttribute() { assertEquals(0.5, WeaponDamage.scale(2.0, 4.0), EPS); }
    @Test void absurdRatiosAreCapped() { assertEquals(2.0, WeaponDamage.scale(100, 1.0), EPS); }
    @Test void brokenAttributeFallsBackToFullDamage() {
        assertEquals(1.0, WeaponDamage.scale(1.0, 0), EPS);
        assertEquals(1.0, WeaponDamage.scale(Double.NaN, 1.0), EPS);
    }
    @Test void negativeEventDamageIsClampedToZero() { assertEquals(0.0, WeaponDamage.scale(-1, 1.0), EPS); }
}
