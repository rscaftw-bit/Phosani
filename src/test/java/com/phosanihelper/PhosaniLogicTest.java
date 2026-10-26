package com.phosanihelper;

import net.runelite.api.Prayer;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import java.util.Set;

import static com.phosanihelper.PhosaniHelperConfig.CurseRotation.NEED_MELEE_CLICK_MAGIC;
import static com.phosanihelper.PhosaniHelperConfig.CurseRotation.NEED_MELEE_CLICK_RANGE;
import static org.junit.Assert.*;

public class PhosaniLogicTest {

    @Test
    public void curseUserMapping() {
        assertEquals(Prayer.PROTECT_FROM_MAGIC,    PhosaniLogic.prayerToClick(Prayer.PROTECT_FROM_MELEE, NEED_MELEE_CLICK_MAGIC));
        assertEquals(Prayer.PROTECT_FROM_MISSILES, PhosaniLogic.prayerToClick(Prayer.PROTECT_FROM_MAGIC, NEED_MELEE_CLICK_MAGIC));
        assertEquals(Prayer.PROTECT_FROM_MELEE,    PhosaniLogic.prayerToClick(Prayer.PROTECT_FROM_MISSILES, NEED_MELEE_CLICK_MAGIC));
    }

    @Test
    public void curseWikiMapping() {
        // Wiki: Melee button gives Magic, Magic button gives Missiles, Missiles button gives Melee
        assertEquals(Prayer.PROTECT_FROM_MISSILES, PhosaniLogic.prayerToClick(Prayer.PROTECT_FROM_MELEE, NEED_MELEE_CLICK_RANGE));
        assertEquals(Prayer.PROTECT_FROM_MELEE,    PhosaniLogic.prayerToClick(Prayer.PROTECT_FROM_MAGIC, NEED_MELEE_CLICK_RANGE));
        assertEquals(Prayer.PROTECT_FROM_MAGIC,    PhosaniLogic.prayerToClick(Prayer.PROTECT_FROM_MISSILES, NEED_MELEE_CLICK_RANGE));
        assertNull(PhosaniLogic.prayerToClick(null, NEED_MELEE_CLICK_RANGE));
    }

    @Test
    public void curseChatMessagesMatch() {
        assertTrue("The Nightmare has cursed you, shuffling your prayers!".contains(PhosaniLogic.CURSE_START));
        assertTrue("You feel the effects of the Nightmare's curse wear off.".contains(PhosaniLogic.CURSE_END));
    }

    @Test
    public void surgeFromWestEdgeGoesEast() {
        WorldPoint middle = new WorldPoint(100, 100, 0);
        WorldPoint westEdge = new WorldPoint(90, 101, 0);
        assertTrue(PhosaniLogic.tileDistance(middle, westEdge) >= 5);
        assertArrayEquals(new int[]{1, 0}, PhosaniLogic.surgeDirection(middle, westEdge));

        Set<WorldPoint> lane = PhosaniLogic.surgeLane(westEdge, new int[]{1, 0}, 5, 20);
        assertTrue(lane.contains(new WorldPoint(110, 101, 0)));   // far side, centre row
        assertTrue(lane.contains(new WorldPoint(100, 103, 0)));   // 2 rows off centre = inside 5-wide lane
        assertFalse(lane.contains(new WorldPoint(100, 104, 0)));  // 3 rows off = safe
        assertEquals(5 * (20 + 2 + 1), lane.size());
    }

    @Test
    public void surgeFromNorthEdgeGoesSouth() {
        assertArrayEquals(new int[]{0, -1},
            PhosaniLogic.surgeDirection(new WorldPoint(100, 100, 0), new WorldPoint(99, 110, 0)));
    }

    @Test
    public void orientation() {
        assertArrayEquals(new int[]{0, -1}, PhosaniLogic.directionFromOrientation(0));
        assertArrayEquals(new int[]{-1, 0}, PhosaniLogic.directionFromOrientation(512));
        assertArrayEquals(new int[]{0, 1},  PhosaniLogic.directionFromOrientation(1024));
        assertArrayEquals(new int[]{1, 0},  PhosaniLogic.directionFromOrientation(1536));
    }
}
