package dev.pupwired.mathesis.core.rpcheck;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** One character's skill proficiencies, summed from sources (race, free pick, feats, scars) and clamped. */
public final class SkillProficiencies {

    private final Map<String, Map<Skill, Integer>> sources = new LinkedHashMap<>();

    /** Replaces everything {@code source} contributes. */
    public void setSource(String source, Map<Skill, Integer> values) {
        if (values.isEmpty()) sources.remove(source);
        else sources.put(source, new EnumMap<>(values));
    }

    public int get(Skill skill) {
        int sum = 0;
        for (Map<Skill, Integer> values : sources.values()) sum += values.getOrDefault(skill, 0);
        return Math.max(RpCheck.MIN_PROFICIENCY, Math.min(RpCheck.MAX_PROFICIENCY, sum));
    }
}