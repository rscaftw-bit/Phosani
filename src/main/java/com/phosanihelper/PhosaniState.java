package com.phosanihelper;

import lombok.Data;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.api.coords.WorldPoint;

/**
 * Tracks Phosani's current state so both overlays read a single source of truth.
 */
@Data
public class PhosaniState {

    public enum AttackStyle { NONE, MELEE, RANGED, MAGIC }
    public enum SpecialAttack { NONE, DASH, PARASITES, MUSHROOMS, HUSKS, DARKNESS, CURSE }

    // The boss NPC (null when not in the fight)
    private NPC nightmare;

    // What her last detected auto-attack was
    private AttackStyle lastAttack = AttackStyle.NONE;

    // Which special is currently active
    private SpecialAttack activeSpecial = SpecialAttack.NONE;

    // The prayer the player SHOULD be on right now
    private Prayer recommendedPrayer = null;

    // Tick on which the last attack animation fired
    private int lastAttackTick = -1;

    // Countdown: how many ticks ago the attack started (for fade-out of indicator)
    private int ticksSinceAttack = 0;

    // During a DASH: the tile she is charging FROM (behind = safe side)
    private WorldPoint dashOrigin = null;

    // Dangerous tiles (darkness zones, parasite orbs) → game tick on which they expire
    private java.util.Map<WorldPoint, Integer> dangerTiles = new java.util.HashMap<>();

    // Mushroom tiles that should be clicked
    private java.util.Set<WorldPoint> mushroomTiles = new java.util.HashSet<>();

    // Count of attacks in current 4-attack cycle (resets on special)
    private int autoAttackCount = 0;

    // True while she is doing a dash animation
    private boolean dashing = false;

    // --------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------

    public boolean isActive() {
        return nightmare != null && !nightmare.isDead();
    }

    public void clear() {
        nightmare = null;
        lastAttack = AttackStyle.NONE;
        activeSpecial = SpecialAttack.NONE;
        recommendedPrayer = null;
        lastAttackTick = -1;
        ticksSinceAttack = 0;
        dashOrigin = null;
        dangerTiles.clear();
        mushroomTiles.clear();
        autoAttackCount = 0;
        dashing = false;
    }

    public void incrementTicksSinceAttack() {
        ticksSinceAttack++;
    }

    /**
     * True if the prayer indicator should still be visible
     * (fades after 3 ticks to avoid screen clutter).
     */
    public boolean isPrayerIndicatorActive() {
        return ticksSinceAttack <= 3 && recommendedPrayer != null;
    }
}
