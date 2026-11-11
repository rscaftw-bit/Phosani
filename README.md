# Phosani Helper — RuneLite plugin

Visual aids for Phosani's Nightmare. **Display only — it never clicks or sends input.**

## Features

### Prayer box
- A compact box (60 px wide by default) that stays up for the **whole fight**.
- Shows the protection prayer for her last attack the moment her attack animation starts;
  the icon stays until her next attack.
- **Default spot:** just above the inventory, on its left side (bottom-right of the game view
  in fixed mode).
- **Movable:** hold **Alt** and drag it anywhere. Alt + right-click → *Reset* puts it back.

### Curse
- Turned on by the game's chat message ("…has cursed you, shuffling your prayers!").
- While cursed the box header changes to **CLICK** and the icon is the prayer you must
  physically click, with "vs X" (the attack it blocks) underneath.
- Before her first cursed attack, the box shows the full remap (e.g. `MEL>RNG`).
- A **Curse N/5** counter tracks her attacks; the curse ends after her **5th attack**
  (or on the "…curse wear off" chat message, whichever comes first).
- Mapping (default is the wiki mapping):
  - **wiki (default):** need Melee → click **Missiles**, Magic → **Melee**, Missiles → **Magic**
  - old: need Melee → click **Magic**, Magic → **Missiles**, Missiles → **Melee**

### Surge lane
- When she teleports to an edge (a big one-tick position jump), a 5-wide, faint magenta lane
  is drawn straight across the room through the arena centre, with
  "SURGE - GET OUT OF THE LANE" above her. Stand outside it.
- Once she starts moving, the lane snaps to her actual charge direction.

### Adds & parasites
- Parasites, Sleepwalkers and Husks get a cyan outline + tile (matched by NPC name).
- After the "impregnated you" message, text above your player says to click the
  **Hitpoints orb**, and whether that will use **Relicym's balm** (used first) or
  **Sanfew serum** — or warns in red if you have neither.
- Prefer RuneLite's own NPC highlighting? Turn off the hidden `highlightNpcs` option.

### Tiles
- Orange mushroom/spore tiles, red darkness tiles.

## Settings
The settings panel only shows:
- Colours: surge lane, mushroom tiles, danger tiles, add highlight
- Prayer box size (50–150 px; the icon scales with it)

Everything else (on/off toggles, curse mapping, surge tuning, animation/NPC/object IDs)
is hidden and uses its default or previously saved value.

## Build & run (developer mode)
Requires JDK 11+ (the Gradle wrapper is included).

On Windows, just double-click **`start.bat`**. Or from a terminal:
```
./gradlew build     # compile + run tests
./gradlew run       # launch RuneLite in developer mode with Phosani Helper loaded
```
Log in via the Jagex-account dev-client instructions:
https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts

## ⚠ IDs not fully verified
Animation, NPC, object and graphic IDs (defaults: attack anims 8594–8598, Phosani NPCs
9416–9424 and 11153–11155, mushroom object 37739, darkness graphic 1767) are best-effort.
Phosani herself is also matched by name, so the boss is found in every phase. Prayer
detection depends on the animation IDs; curse and surge detection do not.

## Rules note
Jagex's third-party client guidelines restrict boss-helper features (prayer indicators,
attack counters, hazard warnings). These features would not be accepted on the RuneLite
Plugin Hub, and using them on the live game is your own risk.
