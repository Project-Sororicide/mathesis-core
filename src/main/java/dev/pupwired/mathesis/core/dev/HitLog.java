package dev.pupwired.mathesis.core.dev;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Who has the per-hit damage breakdown switched on (/mathesis dev hitlog). The damage listener checks
 * {@link #anyEnabled()} first, so it costs nothing when nobody is using it.
 */
public final class HitLog {

    private final Set<UUID> enabled = ConcurrentHashMap.newKeySet();

    /** @return true if the log is now enabled for this player */
    public boolean toggle(UUID id) {
        if (enabled.remove(id)) return false;
        enabled.add(id);
        return true;
    }

    public boolean isEnabled(UUID id) {
        return enabled.contains(id);
    }

    public boolean anyEnabled() {
        return !enabled.isEmpty();
    }
}
