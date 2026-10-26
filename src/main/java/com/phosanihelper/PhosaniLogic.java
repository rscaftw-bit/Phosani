package com.phosanihelper;

import net.runelite.api.Prayer;
import net.runelite.api.coords.WorldPoint;

import java.util.HashSet;
import java.util.Set;

/** Pure, game-independent logic (unit-tested). */
final class PhosaniLogic {

    private PhosaniLogic() {}

    static final String CURSE_START = "has cursed you";
    static final String CURSE_END   = "curse wear off";

    /** The prayer to physically click while cursed to get the {@code needed} protection. */
    static Prayer prayerToClick(Prayer needed, PhosaniHelperConfig.CurseRotation rotation) {
        if (needed == null) return null;
        boolean userMapping = rotation == PhosaniHelperConfig.CurseRotation.NEED_MELEE_CLICK_MAGIC;
        switch (needed) {
            case PROTECT_FROM_MELEE:
                return userMapping ? Prayer.PROTECT_FROM_MAGIC : Prayer.PROTECT_FROM_MISSILES;
            case PROTECT_FROM_MAGIC:
                return userMapping ? Prayer.PROTECT_FROM_MISSILES : Prayer.PROTECT_FROM_MELEE;
            case PROTECT_FROM_MISSILES:
                return userMapping ? Prayer.PROTECT_FROM_MELEE : Prayer.PROTECT_FROM_MAGIC;
            default:
                return needed;
        }
    }

    /** Chebyshev (king-move) tile distance. */
    static int tileDistance(WorldPoint a, WorldPoint b) {
        return Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY()));
    }

    /**
     * Cardinal surge direction after she teleports from {@code before} to the edge tile {@code after}:
     * she crosses back over the room, i.e. towards where she came from, snapped to the dominant axis.
     * Returns {dx, dy}, or null if no movement.
     */
    static int[] surgeDirection(WorldPoint before, WorldPoint after) {
        int dx = before.getX() - after.getX();
        int dy = before.getY() - after.getY();
        if (dx == 0 && dy == 0) return null;
        return Math.abs(dx) >= Math.abs(dy)
            ? new int[]{Integer.signum(dx), 0}
            : new int[]{0, Integer.signum(dy)};
    }

    /** Direction from NPC orientation (0 = south, 512 = west, 1024 = north, 1536 = east). */
    static int[] directionFromOrientation(int orientation) {
        int o = ((orientation % 2048) + 2048) % 2048;
        int q = ((o + 256) / 512) % 4;
        switch (q) {
            case 0:  return new int[]{0, -1};
            case 1:  return new int[]{-1, 0};
            case 2:  return new int[]{0, 1};
            default: return new int[]{1, 0};
        }
    }

    /**
     * Lane tiles: starting at her centre tile, {@code length} tiles along {@code dir},
     * {@code width} tiles wide (her size).
     */
    static Set<WorldPoint> surgeLane(WorldPoint centre, int[] dir, int width, int length) {
        Set<WorldPoint> lane = new HashSet<>();
        int half = Math.max(0, width / 2);
        int px = -dir[1], py = dir[0]; // perpendicular
        for (int i = -half; i <= length; i++) {
            for (int j = -half; j <= half; j++) {
                lane.add(new WorldPoint(
                    centre.getX() + dir[0] * i + px * j,
                    centre.getY() + dir[1] * i + py * j,
                    centre.getPlane()));
            }
        }
        return lane;
    }
}
