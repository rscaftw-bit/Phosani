package com.phosanihelper;

import net.runelite.client.config.*;

import java.awt.Color;

@ConfigGroup("phosanihelper")
public interface PhosaniHelperConfig extends Config {

    // ── Overlays on/off ──────────────────────────────────────────────────────

    @ConfigItem(keyName = "showPrayerHelper", name = "Prayer helper",
        description = "Show large prayer icon with 1-tick early warning", position = 0)
    default boolean showPrayerHelper() { return true; }

    @ConfigItem(keyName = "showDashOverlay", name = "Dash safe-side overlay",
        description = "Red line + green tile showing the safe side during her dash", position = 1)
    default boolean showDashOverlay() { return true; }

    @ConfigItem(keyName = "showMushroomOverlay", name = "Mushroom tiles",
        description = "Highlight mushroom tiles during spore phase", position = 2)
    default boolean showMushroomOverlay() { return true; }

    @ConfigItem(keyName = "showDangerTiles", name = "Danger tile overlay",
        description = "Highlight darkness / parasite danger tiles", position = 3)
    default boolean showDangerTiles() { return true; }

    @ConfigItem(keyName = "showAttackStyleText", name = "Attack style text",
        description = "Display 'MAGIC / RANGE / MELEE' text above prayer icon", position = 4)
    default boolean showAttackStyleText() { return true; }

    @ConfigItem(keyName = "showAutoCountdown", name = "Auto-attack counter",
        description = "Show '3 autos until special' countdown", position = 5)
    default boolean showAutoCountdown() { return true; }

    // ── Colours ──────────────────────────────────────────────────────────────

    @Alpha
    @ConfigItem(keyName = "dashLineColor", name = "Dash line color",
        description = "Color of the dash-direction indicator line", position = 10)
    default Color dashLineColor() { return new Color(255, 30, 30, 220); }

    @Alpha
    @ConfigItem(keyName = "safeTileColor", name = "Safe tile color",
        description = "Color of the safe tile during dash", position = 11)
    default Color safeTileColor() { return new Color(30, 255, 80, 180); }

    @Alpha
    @ConfigItem(keyName = "mushroomColor", name = "Mushroom tile color",
        description = "Color of mushroom tiles to click", position = 12)
    default Color mushroomColor() { return new Color(255, 165, 0, 200); }

    @Alpha
    @ConfigItem(keyName = "dangerColor", name = "Danger tile color",
        description = "Color of dangerous tiles (darkness, parasites)", position = 13)
    default Color dangerColor() { return new Color(200, 0, 0, 160); }

    // ── Prayer indicator size ─────────────────────────────────────────────────

    @Range(min = 40, max = 200)
    @ConfigItem(keyName = "prayerIconSize", name = "Prayer icon size (px)",
        description = "Size of the prayer recommendation icon", position = 20)
    default int prayerIconSize() { return 90; }

    // ── Animation IDs (adjustable if Jagex changes them) ─────────────────────

    @ConfigItem(keyName = "animMelee", name = "Melee animation ID",
        description = "Phosani melee attack animation", position = 30)
    default int animMelee() { return 8594; }

    @ConfigItem(keyName = "animMagic", name = "Magic animation ID",
        description = "Phosani magic attack animation", position = 31)
    default int animMagic() { return 8595; }

    @ConfigItem(keyName = "animRanged", name = "Ranged animation ID",
        description = "Phosani ranged attack animation", position = 32)
    default int animRanged() { return 8596; }

    @ConfigItem(keyName = "animParasite", name = "Parasite animation ID",
        description = "Phosani parasite attack animation", position = 33)
    default int animParasite() { return 8597; }

    @ConfigItem(keyName = "animDash", name = "Dash animation ID",
        description = "Phosani dash/charge animation", position = 34)
    default int animDash() { return 8600; }

    @ConfigItem(keyName = "animMushroom", name = "Mushroom phase animation ID",
        description = "Phosani mushroom-phase animation", position = 35)
    default int animMushroom() { return 8598; }

    // ── NPC IDs ───────────────────────────────────────────────────────────────

    @ConfigItem(keyName = "npcIdPhosani", name = "Phosani NPC ID",
        description = "Main Phosani's Nightmare NPC ID (comma-list for all phases)", position = 40)
    default String npcIdPhosani() { return "9416,9417,9418,9419"; }

    // ── Object / graphic IDs (verify in-game with Developer Tools) ────────────

    @ConfigItem(keyName = "objMushroom", name = "Mushroom object ID",
        description = "Game object ID of the mushroom/spore", position = 41)
    default int objMushroom() { return 37739; }

    @ConfigItem(keyName = "gfxDarkness", name = "Darkness graphic ID",
        description = "Graphics object ID of the darkness / shadow zone", position = 42)
    default int gfxDarkness() { return 1767; }

    @ConfigItem(keyName = "gfxParasite", name = "Parasite graphic ID",
        description = "Graphics object ID of the parasite orb", position = 43)
    default int gfxParasite() { return 1765; }

    @ConfigItem(keyName = "gfxCurse", name = "Curse spot-anim ID",
        description = "Spot animation ID on the local player when cursed", position = 44)
    default int gfxCurse() { return 1769; }

    @Range(min = 1, max = 50)
    @ConfigItem(keyName = "curseDurationTicks", name = "Curse duration (ticks)",
        description = "How long the curse remap stays active after detection", position = 45)
    default int curseDurationTicks() { return 5; }

    @Range(min = 1, max = 20)
    @ConfigItem(keyName = "dangerTileTicks", name = "Danger tile duration (ticks)",
        description = "How long a darkness/parasite tile stays highlighted", position = 46)
    default int dangerTileTicks() { return 4; }
}
