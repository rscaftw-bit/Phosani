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
    static final String PARASITE_START = "impregnated you";

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
     * Surge axis after her edge teleport from {@code before} to {@code after}.
     * Returns {1,0} (east-west lane) or {0,1} (north-south lane), or null if no movement.
     * In-game testing showed the charge runs PERPENDICULAR to the teleport jump,
     * so that is the default; {@code perpendicular=false} uses the jump axis.
     */
    static int[] surgeAxis(WorldPoint before, WorldPoint after, boolean perpendicular) {
        int dx = Math.abs(after.getX() - before.getX());
        int dy = Math.abs(after.getY() - before.getY());
        if (dx == 0 && dy == 0) return null;
        boolean jumpIsHorizontal = dx >= dy;
        boolean laneHorizontal = perpendicular ? !jumpIsHorizontal : jumpIsHorizontal;
        return laneHorizontal ? new int[]{1, 0} : new int[]{0, 1};
    }

    /** Axis of actual movement between two tiles (ground truth once she starts charging). */
    static int[] movementAxis(WorldPoint from, WorldPoint to) {
        return surgeAxis(from, to, false);
    }

    /** Axis from NPC orientation (0 = south, 512 = west, 1024 = north, 1536 = east). */
    static int[] axisFromOrientation(int orientation) {
        int[] d = directionFromOrientation(orientation);
        return new int[]{Math.abs(d[0]), Math.abs(d[1])};
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
     * Lane tiles: a full line through her centre tile along {@code axis}, {@code length} tiles
     * each way (covers the whole room whichever way she charges), {@code width} tiles wide.
     */
    static Set<WorldPoint> surgeLane(WorldPoint centre, int[] axis, int width, int length) {
        Set<WorldPoint> lane = new HashSet<>();
        int half = Math.max(0, width / 2);
        int px = axis[1], py = axis[0]; // perpendicular
        for (int i = -length; i <= length; i++) {
            for (int j = -half; j <= half; j++) {
                lane.add(new WorldPoint(
                    centre.getX() + axis[0] * i + px * j,
                    centre.getY() + axis[1] * i + py * j,
                    centre.getPlane()));
            }
        }
        return lane;
    }

    /** Case-insensitive match of an NPC name against a comma-separated list. */
    static boolean nameMatches(String npcName, String csv) {
        if (npcName == null || csv == null) return false;
        String n = npcName.trim().toLowerCase();
        for (String part : csv.split(",")) {
            if (!part.trim().isEmpty() && n.equals(part.trim().toLowerCase())) return true;
        }
        return false;
    }

    /**
     * Text shown above the player while a parasite is inside them.
     * Clicking the Hitpoints orb uses Relicym's balm before Sanfew serum.
     */
    static String parasiteText(boolean hasBalm, boolean hasSanfew) {
        if (hasBalm) return "PARASITE: click HITPOINTS orb (uses Relicym's balm)";
        if (hasSanfew) return "PARASITE: click HITPOINTS orb (drinks Sanfew serum)";
        return "PARASITE: no Sanfew / Relicym's in inventory!";
    }

    static boolean isBalm(String name) {
        return name != null && name.toLowerCase().startsWith("relicym's balm");
    }

    static boolean isSanfew(String name) {
        return name != null && name.toLowerCase().startsWith("sanfew serum");
    }

    /**
     * Which attack-prayer the overlay is based on. Normally it fades after 3 ticks;
     * while cursed it must stay up (until her next attack) or the box falls back to the
     * generic mapping table instead of the prayer to click.
     */
    static Prayer indicatorPrayer(boolean cursed, int ticksSinceAttack, Prayer recommended) {
        if (recommended == null) return null;
        return (cursed || ticksSinceAttack <= 3) ? recommended : null;
    }

    static boolean isPhosaniName(String name) {
        return name != null && net.runelite.client.util.Text.removeTags(name).trim()
            .equalsIgnoreCase("Phosani's Nightmare");
    }

    /**
     * She teleports to an edge and charges straight across to the opposite edge.
     * The edge she is on (relative to the arena centre) fixes the charge axis:
     * north/south edge = north-south lane {0,1}; east/west edge = east-west lane {1,0}.
     */
    static int[] surgeAxisFromCentre(WorldPoint centre, WorldPoint edge) {
        if (centre == null || edge == null) return null;
        int dx = Math.abs(edge.getX() - centre.getX());
        int dy = Math.abs(edge.getY() - centre.getY());
        if (dx == dy) return null; // corner / no offset: let the fallback decide
        return dx > dy ? new int[]{1, 0} : new int[]{0, 1};
    }

    static final int CURSE_ATTACKS = 5;

    /** Attacks still to come before the curse lifts (never negative). */
    static int curseAttacksLeft(int cursedAttacks) {
        return Math.max(0, CURSE_ATTACKS - Math.max(0, cursedAttacks));
    }
}
