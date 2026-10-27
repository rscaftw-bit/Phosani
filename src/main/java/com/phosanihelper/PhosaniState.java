package com.phosanihelper;

import lombok.Data;
import net.runelite.api.NPC;
import net.runelite.api.Prayer;
import net.runelite.api.coords.WorldPoint;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks Phosani's current state so both overlays read a single source of truth.
 */
@Data
public class PhosaniState {

    public enum AttackStyle { NONE, MELEE, RANGED, MAGIC }
    public enum SpecialAttack { NONE, SURGE, PARASITES, MUSHROOMS }

    // The boss NPC (null when not in the fight)
    private NPC nightmare;

    private AttackStyle lastAttack = AttackStyle.NONE;
    private SpecialAttack activeSpecial = SpecialAttack.NONE;

    // Protection needed against her last attack (NOT remapped for the curse)
    private Prayer recommendedPrayer = null;

    private int lastAttackTick = -1;
    private int ticksSinceAttack = 0;

    // Dangerous tiles (darkness zones, parasite orbs) → game tick on which they expire
    private Map<WorldPoint, Integer> dangerTiles = new HashMap<>();
    private Set<WorldPoint> mushroomTiles = new HashSet<>();
    private int autoAttackCount = 0;

    // ── Surge ────────────────────────────────────────────────────────────────
    // Boss centre tile on the previous game tick (used to detect her edge teleport)
    private WorldPoint lastBossTile = null;
    // Tiles of the lane she is about to surge through
    private Set<WorldPoint> surgeTiles = new HashSet<>();
    // Her centre tile right after the edge teleport
    private WorldPoint surgeStartTile = null;
    private int surgeExpiryTick = -1;
    // Ignore further position jumps until this tick (her landing / walking back)
    private int surgeCooldownUntil = -1;
    // Her tile when the fight was first seen = middle of the arena (kept across phases)
    private WorldPoint arenaCentre = null;

    // ── Curse ────────────────────────────────────────────────────────────────
    private boolean cursed = false;
    // Safety timeout in case the "wear off" chat message is missed
    private int curseExpiryTick = -1;

    // ── Parasite ─────────────────────────────────────────────────────────────
    // Show "PARASITE IN YOU" until this tick (set from the chat message)
    private int parasiteWarningUntil = -1;
    private boolean invHasBalm = false;
    private boolean invHasSanfew = false;

    public boolean isActive() {
        return nightmare != null && !nightmare.isDead();
    }

    public boolean isSurgeActive() {
        return !surgeTiles.isEmpty();
    }

    public void clear() {
        nightmare = null;
        lastAttack = AttackStyle.NONE;
        activeSpecial = SpecialAttack.NONE;
        recommendedPrayer = null;
        lastAttackTick = -1;
        ticksSinceAttack = 0;
        dangerTiles.clear();
        mushroomTiles.clear();
        autoAttackCount = 0;
        lastBossTile = null;
        surgeTiles.clear();
        surgeStartTile = null;
        parasiteWarningUntil = -1;
        surgeExpiryTick = -1;
        surgeCooldownUntil = -1;
        arenaCentre = null;
        cursed = false;
        curseExpiryTick = -1;
    }

    public void incrementTicksSinceAttack() {
        ticksSinceAttack++;
    }

    /** True if the prayer indicator should still be visible (fades after 3 ticks). */
    public boolean isPrayerIndicatorActive() {
        return ticksSinceAttack <= 3 && recommendedPrayer != null;
    }
}
