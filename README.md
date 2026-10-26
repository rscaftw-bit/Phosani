# Phosani Helper — RuneLite plugin

Visual aids for Phosani's Nightmare. **Display only — it never clicks or sends input.**

## Features
- **Prayer helper (top-left HUD):** large protect-prayer icon the moment her attack animation starts, giving you the full wind-up window to react.
- **Curse-aware remap:** while cursed, shows a `CURSED – PRAYERS ROTATED` banner plus the icon you must *physically click*:
  - Need Melee → click **Magic**
  - Need Magic → click **Ranged**
  - Need Ranged → click **Melee**
- **Dash overlay:** red line through her + green safe tile behind her during the charge.
- **Mushroom / danger tiles:** orange spore tiles, red darkness/parasite tiles.
- **Auto-attack counter:** "N autos → special".
- All colours, icon size, and animation/NPC/graphic IDs configurable.

## Build & run (developer mode)
Requires JDK 11+ (the Gradle wrapper is included):
```
./gradlew build     # compile
./gradlew run       # launches RuneLite in developer mode with Phosani Helper loaded
```
Log in via the Jagex-account dev-client instructions:
https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts

## ⚠ Verify IDs before first use
Animation, NPC, object and graphic IDs (defaults in config: anims 8594–8600, NPCs 9416–9419,
mushroom object 37739, darkness 1767, parasite 1765, curse 1769) are **best-effort and not verified in-game**.
Turn on RuneLite's **Developer Tools → Animation/NPC inspector**, fight her once, and correct
any wrong values in the plugin config panel. Prayer and curse detection depend on them.

## Rules note
Jagex's third-party client guidelines restrict some boss-helper features (especially those
that tell you exactly what to pray). Using this on the live game is your call — check the
current RuneLite/Jagex guidelines.
