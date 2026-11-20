package dev.pupwired.mathesis.core.rpcheck;

import java.util.Locale;

/** The 20 RP skills and the attribute whose half-value each one adds to a check. */
public enum Skill {
    BREAKING(CharacterAttribute.MIGHT), ENDURANCE(CharacterAttribute.MIGHT),
    FORCING(CharacterAttribute.MIGHT), INTIMIDATING(CharacterAttribute.MIGHT),

    BARTERING(CharacterAttribute.PRESENCE), DECEPTION(CharacterAttribute.PRESENCE),
    LEADERSHIP(CharacterAttribute.PRESENCE), PERSUASION(CharacterAttribute.PRESENCE),

    ACROBATICS(CharacterAttribute.REFLEX), AIMING(CharacterAttribute.REFLEX),
    QUICK_HANDS(CharacterAttribute.REFLEX), STEALTH(CharacterAttribute.REFLEX),

    COERCION(CharacterAttribute.RESOLVE), COMPOSURE(CharacterAttribute.RESOLVE),
    INTERROGATION(CharacterAttribute.RESOLVE), RESISTANCE(CharacterAttribute.RESOLVE),

    ANALYSIS(CharacterAttribute.WITS), ENGINEERING(CharacterAttribute.WITS),
    HACKING(CharacterAttribute.WITS), MEDICINE(CharacterAttribute.WITS);

    private final CharacterAttribute attribute;

    Skill(CharacterAttribute attribute) { this.attribute = attribute; }

    public CharacterAttribute attribute() { return attribute; }

    /** "Quick Hands", "quick_hands" and "quick-hands" all work. */
    public static Skill fromConfig(String name) {
        String key = name.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        for (Skill skill : values()) if (skill.name().equals(key)) return skill;
        throw new IllegalArgumentException("unknown skill '" + name + "'");
    }

    /** "QUICK_HANDS" becomes "Quick Hands". */
    public String label() {
        StringBuilder out = new StringBuilder();
        for (String word : name().split("_")) {
            if (out.length() > 0) out.append(' ');
            out.append(word.charAt(0)).append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return out.toString();
    }
}