package net.sabafly.slotmachine.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ScreenManager {
    private static final Map<Integer, Set<UUID>> viewerMap = new HashMap<>();

    public static Map<Integer, Set<UUID>> getViewerMap() {
        return viewerMap;
    }

    public static final double MAX_DIST = Math.pow(48, 2);
}
