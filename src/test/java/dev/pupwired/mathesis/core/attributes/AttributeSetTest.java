package dev.pupwired.mathesis.core.attributes;

import dev.pupwired.mathesis.core.rpcheck.CharacterAttribute;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.pupwired.mathesis.core.rpcheck.CharacterAttribute.*;
import static org.junit.jupiter.api.Assertions.*;

class AttributeSetTest {

    @Test
    void startsEmpty() {
        var set = new AttributeSet();
        assertEquals(0, set.granted());
        assertEquals(0, set.unspent());
        assertEquals(0, set.get(MIGHT));
    }

    @Test
    void grantedPointsAreSpentOneAttributeAtATime() {
        var set = new AttributeSet();
        set.grant(3);
        assertTrue(set.invest(MIGHT, 2));
        assertEquals(2, set.get(MIGHT));
        assertEquals(1, set.unspent());
        assertEquals(2, set.spent());
    }

    @Test
    void cannotSpendMoreThanUnspentAndNothingChanges() {
        var set = new AttributeSet();
        set.grant(1);
        assertFalse(set.invest(MIGHT, 2));
        assertEquals(0, set.get(MIGHT));
        assertEquals(1, set.unspent());
    }

    @Test
    void invalidAmountsAreRejected() {
        var set = new AttributeSet();
        assertThrows(IllegalArgumentException.class, () -> set.grant(0));
        assertThrows(IllegalArgumentException.class, () -> set.invest(MIGHT, 0));
    }

    @Test
    void resetRefundsEverythingButKeepsTheGrantedTotal() {
        var set = new AttributeSet();
        set.grant(4);
        set.invest(WITS, 3);
        set.reset();
        assertEquals(0, set.get(WITS));
        assertEquals(4, set.unspent());
    }

    @Test
    void restoredDataIsRepaired() {
        var set = new AttributeSet(1, Map.of(MIGHT, 3, REFLEX, -2));
        assertEquals(0, set.get(REFLEX));
        assertEquals(3, set.get(MIGHT));
        assertEquals(3, set.granted()); // spent points cannot exceed what was granted
        assertEquals(0, set.unspent());
    }

    @Test
    void snapshotIsACopy() {
        var set = new AttributeSet();
        set.grant(2);
        set.invest(MIGHT, 1);
        var copy = set.snapshot();
        set.invest(MIGHT, 1);
        assertEquals(1, copy.get(MIGHT));
    }

    @Test
    void attributeNamesAreFriendly() {
        assertEquals(CharacterAttribute.MIGHT, CharacterAttribute.fromConfig(" Might "));
        assertEquals("Presence", PRESENCE.label());
        assertEquals("presence", PRESENCE.id());
        assertThrows(IllegalArgumentException.class, () -> CharacterAttribute.fromConfig("luck"));
    }
}