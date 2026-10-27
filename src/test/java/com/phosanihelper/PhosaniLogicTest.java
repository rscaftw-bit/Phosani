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
    public void surgeLaneIsPerpendicularToTeleportJumpByDefault() {
        WorldPoint middle = new WorldPoint(100, 100, 0);
        // Horizontal jump (west) -> user observed a VERTICAL charge
        assertArrayEquals(new int[]{0, 1}, PhosaniLogic.surgeAxis(middle, new WorldPoint(90, 101, 0), true));
        // Vertical jump (north) -> HORIZONTAL charge
        assertArrayEquals(new int[]{1, 0}, PhosaniLogic.surgeAxis(middle, new WorldPoint(99, 110, 0), true));
        // Option off -> jump axis
        assertArrayEquals(new int[]{1, 0}, PhosaniLogic.surgeAxis(middle, new WorldPoint(90, 101, 0), false));
        assertNull(PhosaniLogic.surgeAxis(middle, middle, true));
    }

    @Test
    public void movementAxisIsGroundTruth() {
        WorldPoint start = new WorldPoint(90, 100, 0);
        assertArrayEquals(new int[]{0, 1}, PhosaniLogic.movementAxis(start, new WorldPoint(90, 97, 0)));
        assertArrayEquals(new int[]{1, 0}, PhosaniLogic.movementAxis(start, new WorldPoint(93, 100, 0)));
    }

    @Test
    public void verticalLaneCoversBothDirectionsAndIsFiveWide() {
        WorldPoint c = new WorldPoint(100, 100, 0);
        Set<WorldPoint> lane = PhosaniLogic.surgeLane(c, new int[]{0, 1}, 5, 20);
        assertTrue(lane.contains(new WorldPoint(100, 120, 0)));
        assertTrue(lane.contains(new WorldPoint(100, 80, 0)));
        assertTrue(lane.contains(new WorldPoint(102, 110, 0)));
        assertFalse(lane.contains(new WorldPoint(103, 110, 0)));   // 3 columns off = safe
        assertFalse(lane.contains(new WorldPoint(110, 100, 0)));   // NOT a horizontal lane
        assertEquals(5 * 41, lane.size());
    }

    @Test
    public void addNames() {
        String names = "Parasite,Sleepwalker,Husk";
        assertTrue(PhosaniLogic.nameMatches("Parasite", names));
        assertTrue(PhosaniLogic.nameMatches("sleepwalker", names));
        assertFalse(PhosaniLogic.nameMatches("Phosani's Nightmare", names));
        assertFalse(PhosaniLogic.nameMatches(null, names));
        assertTrue("The Nightmare has impregnated you with a deadly parasite!".contains(PhosaniLogic.PARASITE_START));
    }

    @Test
    public void orientation() {
        assertArrayEquals(new int[]{0, -1}, PhosaniLogic.directionFromOrientation(0));
        assertArrayEquals(new int[]{-1, 0}, PhosaniLogic.directionFromOrientation(512));
        assertArrayEquals(new int[]{0, 1},  PhosaniLogic.directionFromOrientation(1024));
        assertArrayEquals(new int[]{1, 0},  PhosaniLogic.directionFromOrientation(1536));
        assertArrayEquals(new int[]{0, 1},  PhosaniLogic.axisFromOrientation(1024));
    }

    @org.junit.Test
    public void parasiteTextPrefersBalm() {
        org.junit.Assert.assertTrue(PhosaniLogic.parasiteText(true, true).contains("Relicym"));
        org.junit.Assert.assertTrue(PhosaniLogic.parasiteText(false, true).contains("Sanfew"));
        org.junit.Assert.assertTrue(PhosaniLogic.parasiteText(false, false).contains("no Sanfew"));
        org.junit.Assert.assertTrue(PhosaniLogic.isBalm("Relicym's balm(3)"));
        org.junit.Assert.assertTrue(PhosaniLogic.isSanfew("Sanfew serum(4)"));
        org.junit.Assert.assertFalse(PhosaniLogic.isSanfew("Super restore(4)"));
    }

    @org.junit.Test
    public void cursedIndicatorDoesNotFade() {
        net.runelite.api.Prayer m = net.runelite.api.Prayer.PROTECT_FROM_MELEE;
        org.junit.Assert.assertNull(PhosaniLogic.indicatorPrayer(false, 5, m));
        org.junit.Assert.assertEquals(m, PhosaniLogic.indicatorPrayer(false, 2, m));
        org.junit.Assert.assertEquals(m, PhosaniLogic.indicatorPrayer(true, 9, m));
        org.junit.Assert.assertNull(PhosaniLogic.indicatorPrayer(true, 0, null));
    }

    @org.junit.Test
    public void phosaniMatchedByNameInEveryPhase() {
        org.junit.Assert.assertTrue(PhosaniLogic.isPhosaniName("Phosani's Nightmare"));
        org.junit.Assert.assertTrue(PhosaniLogic.isPhosaniName("<col=ffff00>Phosani's Nightmare</col>"));
        org.junit.Assert.assertFalse(PhosaniLogic.isPhosaniName("The Nightmare"));
        org.junit.Assert.assertFalse(PhosaniLogic.isPhosaniName(null));
    }

    @org.junit.Test
    public void surgeAxisFollowsEdge() {
        net.runelite.api.coords.WorldPoint c = new net.runelite.api.coords.WorldPoint(100, 100, 0);
        // teleported to north edge -> charges south: north-south lane
        org.junit.Assert.assertArrayEquals(new int[]{0, 1}, PhosaniLogic.surgeAxisFromCentre(c, new net.runelite.api.coords.WorldPoint(101, 107, 0)));
        // west edge -> east-west lane
        org.junit.Assert.assertArrayEquals(new int[]{1, 0}, PhosaniLogic.surgeAxisFromCentre(c, new net.runelite.api.coords.WorldPoint(93, 98, 0)));
        org.junit.Assert.assertNull(PhosaniLogic.surgeAxisFromCentre(c, c));
    }

    @Test
    public void defaultCurseMappingMatchesWiki() {
        // Wiki: Melee button -> Magic, Magic -> Missiles, Missiles -> Melee
        PhosaniHelperConfig.CurseRotation def = new PhosaniHelperConfig() {}.curseRotation();
        assertEquals(Prayer.PROTECT_FROM_MISSILES, PhosaniLogic.prayerToClick(Prayer.PROTECT_FROM_MELEE, def));
        assertEquals(Prayer.PROTECT_FROM_MELEE,    PhosaniLogic.prayerToClick(Prayer.PROTECT_FROM_MAGIC, def));
        assertEquals(Prayer.PROTECT_FROM_MAGIC,    PhosaniLogic.prayerToClick(Prayer.PROTECT_FROM_MISSILES, def));
    }

    @Test
    public void curseCounter() {
        assertEquals(5, PhosaniLogic.curseAttacksLeft(0));
        assertEquals(1, PhosaniLogic.curseAttacksLeft(4));
        assertEquals(0, PhosaniLogic.curseAttacksLeft(5));
        assertEquals(0, PhosaniLogic.curseAttacksLeft(7));
        assertEquals(5, PhosaniLogic.curseAttacksLeft(-1));
    }
}
