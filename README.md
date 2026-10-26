# Phosani Helper — RuneLite plugin

Visual aids for Phosani's Nightmare. **Display only — it never clicks or sends input.**

## Features
- **Prayer helper (top-left HUD):** large protect-prayer icon the moment her attack animation starts, giving you the full wind-up window to react.
- **Curse mode:** turned on/off by the game's chat messages ("…has cursed you, shuffling your prayers!" /
  "…curse wear off"). While cursed the big icon is the prayer you must **click**, with "(blocks her X attack)" underneath.
  Mapping is selectable in config (`Curse: needed → click`):
  - default: Need Melee → click **Magic**, Magic → **Ranged**, Ranged → **Melee**
  - wiki: Need Melee → click **Ranged**, Magic → **Melee**, Ranged → **Magic**
- **Surge lane:** when she teleports to an edge (detected as a big one-tick position jump), a 5-wide
  magenta lane is drawn through her across the whole room, perpendicular to the teleport jump
  (as observed in-game; toggle "Surge: lane across teleport" to flip). As soon as she starts moving
  the lane snaps to her real charge axis. Stand outside it.
- **Adds highlight:** Parasites, Sleepwalkers and Husks get a cyan outline + tile (matched by NPC name,
  editable list). "PARASITE - drink sanfew / relicym" appears above you after the "impregnated you" message.
- **Mushroom / danger tiles:** orange spore tiles, red darkness tiles.
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
Animation, NPC, object and graphic IDs (defaults in config: anims 8594–8598, NPCs 9416–9419,
mushroom object 37739, darkness 1767) are **best-effort and not verified in-game**.
Turn on RuneLite's **Developer Tools → Animation/NPC inspector**, fight her once, and correct
any wrong values in the plugin config panel. Prayer detection depends on them; curse and surge detection do not.

## Rules note
Jagex's third-party client guidelines restrict some boss-helper features (especially those
that tell you exactly what to pray). Using this on the live game is your call — check the
current RuneLite/Jagex guidelines.

## Parasite text & sidebar
- **Parasite text:** when a parasite is detected, text appears above your player telling you to click the
  **Hitpoints orb**, with **Relicym's balm** used before **Sanfew serum**.
- **PNM sidebar panel:** a Phosani's Nightmare button in the RuneLite sidebar opens a live status panel.
- **True NPC Highlight:** turn off the `highlightNpcs` ("Highlight adds") option to use RuneLite's own
  True NPC Highlight for sleepwalkers and other adds.
