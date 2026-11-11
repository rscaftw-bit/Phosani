package com.phosanihelper;

import net.runelite.client.config.*;

import java.awt.Color;

/**
 * Only the colours and the prayer box size are shown in the settings panel.
 * Everything else is hidden (still read, using its saved or default value).
 */
@ConfigGroup("phosanihelper")
public interface PhosaniHelperConfig extends Config {

    // ── Colours (visible) ────────────────────────────────────────────────────

    @Alpha
    @ConfigItem(keyName = "surgeColor", name = "Surge lane color",
        description = "Color of the surge lane (do not stand here)", position = 10)
    default Color surgeColor() { return new Color(255, 0, 255, 35); }

    @Alpha
    @ConfigItem(keyName = "mushroomColor", name = "Mushroom tile color",
        description = "Color of mushroom tiles to click", position = 12)
    default Color mushroomColor() { return new Color(255, 165, 0, 200); }

    @Alpha
    @ConfigItem(keyName = "dangerColor", name = "Danger tile color",
        description = "Color of dangerous tiles (darkness, parasites)", position = 13)
    default Color dangerColor() { return new Color(200, 0, 0, 160); }

    @Alpha
    @ConfigItem(keyName = "highlightColor", name = "Add highlight color",
        description = "Color for highlighted adds", position = 14)
    default Color highlightColor() { return new Color(0, 255, 255, 220); }

    // ── Prayer box size (visible) ────────────────────────────────────────────

    @Range(min = 50, max = 150)
    @ConfigItem(keyName = "prayerBoxSize", name = "Prayer box size (px)",
        description = "Width of the prayer box; the icon scales with it", position = 20)
    default int prayerBoxSize() { return 60; }

    // ── Overlays on/off (hidden) ─────────────────────────────────────────────

    @ConfigItem(keyName = "showPrayerHelper", name = "Prayer helper",
        description = "Show large prayer icon", position = 0, hidden = true)
    default boolean showPrayerHelper() { return true; }

    @ConfigItem(keyName = "showSurgeOverlay", name = "Surge lane overlay",
        description = "Lane across the room where she will surge after teleporting to an edge", position = 1, hidden = true)
    default boolean showSurgeOverlay() { return true; }

    @ConfigItem(keyName = "showMushroomOverlay", name = "Mushroom tiles",
        description = "Highlight mushroom tiles during spore phase", position = 2, hidden = true)
    default boolean showMushroomOverlay() { return true; }

    @ConfigItem(keyName = "showDangerTiles", name = "Danger tile overlay",
        description = "Highlight darkness / parasite danger tiles", position = 3, hidden = true)
    default boolean showDangerTiles() { return true; }

    @ConfigItem(keyName = "showAttackStyleText", name = "Attack style text",
        description = "Display 'MAGIC / RANGE / MELEE' text under the prayer icon", position = 4, hidden = true)
    default boolean showAttackStyleText() { return true; }

    // ── Animation IDs (hidden) ───────────────────────────────────────────────

    @ConfigItem(keyName = "animMelee", name = "Melee animation ID",
        description = "Phosani melee attack animation", position = 30, hidden = true)
    default int animMelee() { return 8594; }

    @ConfigItem(keyName = "animMagic", name = "Magic animation ID",
        description = "Phosani magic attack animation", position = 31, hidden = true)
    default int animMagic() { return 8595; }

    @ConfigItem(keyName = "animRanged", name = "Ranged animation ID",
        description = "Phosani ranged attack animation", position = 32, hidden = true)
    default int animRanged() { return 8596; }

    @ConfigItem(keyName = "animParasite", name = "Parasite animation ID",
        description = "Phosani parasite attack animation", position = 33, hidden = true)
    default int animParasite() { return 8597; }

    @ConfigItem(keyName = "animMushroom", name = "Mushroom phase animation ID",
        description = "Phosani mushroom-phase animation", position = 35, hidden = true)
    default int animMushroom() { return 8598; }

    // ── NPC / object IDs (hidden) ────────────────────────────────────────────

    @ConfigItem(keyName = "npcIdPhosani", name = "Phosani NPC ID",
        description = "Main Phosani's Nightmare NPC ID (comma-list for all phases)", position = 40, hidden = true)
    default String npcIdPhosani() { return "9416,9417,9418,9419,9420,9421,9422,9423,9424,11153,11154,11155"; }

    @ConfigItem(keyName = "objMushroom", name = "Mushroom object ID",
        description = "Game object ID of the mushroom/spore", position = 41, hidden = true)
    default int objMushroom() { return 37739; }

    @ConfigItem(keyName = "gfxDarkness", name = "Darkness graphic ID",
        description = "Graphics object ID of the darkness / shadow zone", position = 42, hidden = true)
    default int gfxDarkness() { return 1767; }

    // ── Adds (hidden) ────────────────────────────────────────────────────────

    @ConfigItem(keyName = "highlightNpcs", name = "Highlight adds",
        description = "Outline + tile on parasites, sleepwalkers and husks (matched by NPC name)", position = 6, hidden = true)
    default boolean highlightNpcs() { return true; }

    @ConfigItem(keyName = "highlightNames", name = "Add NPC names",
        description = "Comma-separated NPC names to highlight", position = 7, hidden = true)
    default String highlightNames() { return "Parasite,Sleepwalker,Husk"; }

    @Range(min = 5, max = 60)
    @ConfigItem(keyName = "parasiteWarningTicks", name = "Parasite warning (ticks)",
        description = "How long 'PARASITE - drink sanfew/relicym' shows after 'impregnated you' message", position = 43, hidden = true)
    default int parasiteWarningTicks() { return 25; }

    // ── Curse (hidden) ───────────────────────────────────────────────────────

    enum CurseRotation {
        NEED_MELEE_CLICK_MAGIC("Need Melee: click Magic (old)"),
        NEED_MELEE_CLICK_RANGE("Need Melee: click Missiles (wiki)");

        private final String label;
        CurseRotation(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    @ConfigItem(keyName = "curseRotationV2", name = "Curse: needed → click",
        description = "Which prayer to click while cursed, per needed protection. "
            + "Detected from the 'has cursed you' / 'curse wear off' chat messages.", position = 44, hidden = true)
    default CurseRotation curseRotation() { return CurseRotation.NEED_MELEE_CLICK_RANGE; }

    @Range(min = 10, max = 200)
    @ConfigItem(keyName = "curseTimeoutTicks", name = "Curse safety timeout (ticks)",
        description = "Drop the curse mode after this many ticks if the wear-off message is missed", position = 45, hidden = true)
    default int curseTimeoutTicks() { return 50; }

    // ── Surge (hidden) ───────────────────────────────────────────────────────

    @ConfigItem(keyName = "surgePerpendicular", name = "Surge: lane across teleport",
        description = "On = lane perpendicular to her teleport jump. "
            + "The lane also snaps to her real movement once she starts charging.", position = 46, hidden = true)
    default boolean surgePerpendicular() { return true; }

    @ConfigItem(keyName = "surgeFromCentre", name = "Surge: lane from arena centre",
        description = "On = she charges from the edge she teleported to, straight across through the middle "
            + "(lane axis = which edge she is on). Off = use the teleport-jump rule.", position = 45, hidden = true)
    default boolean surgeFromCentre() { return true; }

    @Range(min = 3, max = 20)
    @ConfigItem(keyName = "surgeJumpTiles", name = "Surge: teleport distance",
        description = "A position jump of at least this many tiles in one tick counts as her edge teleport", position = 47, hidden = true)
    default int surgeJumpTiles() { return 5; }

    @Range(min = 2, max = 20)
    @ConfigItem(keyName = "surgeShowTicks", name = "Surge: lane duration (ticks)",
        description = "How long the surge lane stays drawn after the teleport", position = 48, hidden = true)
    default int surgeShowTicks() { return 5; }

    @Range(min = 10, max = 40)
    @ConfigItem(keyName = "surgeLaneLength", name = "Surge: lane length (tiles)",
        description = "How far across the room the lane is drawn", position = 49, hidden = true)
    default int surgeLaneLength() { return 20; }

    @ConfigItem(keyName = "animSurge", name = "Surge animation ID (optional)",
        description = "-1 = off. If set, this animation also triggers the lane (direction from her facing)", position = 50, hidden = true)
    default int animSurge() { return -1; }

    @Range(min = 1, max = 20)
    @ConfigItem(keyName = "dangerTileTicks", name = "Danger tile duration (ticks)",
        description = "How long a darkness/parasite tile stays highlighted", position = 46, hidden = true)
    default int dangerTileTicks() { return 4; }
}
