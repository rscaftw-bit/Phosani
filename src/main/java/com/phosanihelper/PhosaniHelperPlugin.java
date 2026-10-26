package com.phosanihelper;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@PluginDescriptor(
    name       = "Phosani Helper",
    description = "Tile overlays, prayer helper & dash indicator for Phosani's Nightmare",
    tags       = {"phosani", "nightmare", "prayer", "overlay", "boss", "helper"}
)
public class PhosaniHelperPlugin extends Plugin {

    // ── Injected deps ─────────────────────────────────────────────────────────

    @Inject private Client client;
    @Inject private OverlayManager overlayManager;
    @Inject private PhosaniHelperOverlay tileOverlay;
    @Inject private PrayerHelperOverlay prayerOverlay;
    @Inject private PhosaniHelperConfig config;

    // ── Shared state ──────────────────────────────────────────────────────────

    @Getter private final PhosaniState state = new PhosaniState();

    /** Parsed set of Phosani NPC IDs from config (supports multiple phase IDs). */
    private Set<Integer> phosaniIds;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void startUp() {
        phosaniIds = parseIds(config.npcIdPhosani());
        overlayManager.add(tileOverlay);
        overlayManager.add(prayerOverlay);
        log.info("Phosani Helper started – watching NPC IDs: {}", phosaniIds);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(tileOverlay);
        overlayManager.remove(prayerOverlay);
        state.clear();
        log.info("Phosani Helper stopped");
    }

    @Provides
    PhosaniHelperConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(PhosaniHelperConfig.class);
    }

    // ── Event handlers ────────────────────────────────────────────────────────

    /**
     * Every game tick: age the prayer indicator, scan for danger tiles,
     * check if she has died (clear state).
     */
    @Subscribe
    public void onGameTick(GameTick event) {
        // If we tracked her but she's gone/dead, clear everything
        if (state.getNightmare() != null) {
            if (state.getNightmare().isDead() || !isPhosaniNearby()) {
                log.debug("Phosani gone – clearing state");
                state.clear();
                return;
            }
        }

        // Increment tick counter for prayer indicator fade
        state.incrementTicksSinceAttack();

        // Expire old danger tiles
        final int now = client.getTickCount();
        state.getDangerTiles().values().removeIf(expiry -> expiry <= now);

        // Keep auto-attack counter display sane
        if (state.getAutoAttackCount() > 4) {
            state.setAutoAttackCount(0);
        }
    }

    /**
     * Detect Phosani's NPC spawn (entering the fight room).
     */
    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        NPC npc = event.getNpc();
        if (phosaniIds.contains(npc.getId())) {
            log.debug("Phosani detected (NPC id {})", npc.getId());
            state.clear();
            state.setNightmare(npc);
        }
    }

    /**
     * Handle NPC ID changes (Phosani changes ID between phases).
     */
    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        NPC npc = event.getNpc();
        if (phosaniIds.contains(npc.getId())) {
            state.setNightmare(npc);
            log.debug("Phosani phase change → NPC id {}", npc.getId());
        }
    }

    /**
     * Core: detect Phosani's attack animation and classify it.
     */
    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (!(event.getActor() instanceof NPC)) return;
        NPC npc = (NPC) event.getActor();
        if (!phosaniIds.contains(npc.getId())) return;

        int anim = npc.getAnimation();
        int tick  = client.getTickCount();

        log.debug("Phosani animation: {}", anim);

        // ── Classify the animation ─────────────────────────────────────────

        if (anim == config.animMelee()) {
            state.setLastAttack(PhosaniState.AttackStyle.MELEE);
            state.setRecommendedPrayer(Prayer.PROTECT_FROM_MELEE);
            state.setActiveSpecial(PhosaniState.SpecialAttack.NONE);
            countAutoAttack(tick);

        } else if (anim == config.animMagic()) {
            state.setLastAttack(PhosaniState.AttackStyle.MAGIC);
            state.setRecommendedPrayer(Prayer.PROTECT_FROM_MAGIC);
            state.setActiveSpecial(PhosaniState.SpecialAttack.NONE);
            countAutoAttack(tick);

        } else if (anim == config.animRanged()) {
            state.setLastAttack(PhosaniState.AttackStyle.RANGED);
            state.setRecommendedPrayer(Prayer.PROTECT_FROM_MISSILES);
            state.setActiveSpecial(PhosaniState.SpecialAttack.NONE);
            countAutoAttack(tick);

        } else if (anim == config.animDash()) {
            state.setActiveSpecial(PhosaniState.SpecialAttack.DASH);
            state.setDashing(true);
            // Record where she is right now so the overlay can draw the safe side
            // Scene coordinates (instance-safe) to match the overlay's LocalPoint.fromWorld
            WorldPoint origin = WorldPoint.fromLocal(client, npc.getLocalLocation());
            state.setDashOrigin(origin);
            state.setAutoAttackCount(0);   // specials reset the auto cycle
            log.debug("Dash detected – origin {}", origin);

        } else if (anim == config.animMushroom()) {
            state.setActiveSpecial(PhosaniState.SpecialAttack.MUSHROOMS);
            state.setDashing(false);
            state.setAutoAttackCount(0);

        } else if (anim == config.animParasite()) {
            state.setActiveSpecial(PhosaniState.SpecialAttack.PARASITES);
            state.setDashing(false);
            state.setAutoAttackCount(0);
        }

        // Clear dash flag if she started a new, non-dash animation
        if (anim != config.animDash() && state.isDashing()) {
            state.setDashing(false);
        }
    }

    /**
     * When a game object (mushroom spore) spawns, record its tile.
     * Object ID is configurable (default 37739 – verify in-game).
     */
    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if (event.getGameObject().getId() == config.objMushroom()) {
            WorldPoint wp = event.getTile().getWorldLocation();
            state.getMushroomTiles().add(wp);
            log.debug("Mushroom tile added: {}", wp);
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if (event.getGameObject().getId() == config.objMushroom()) {
            state.getMushroomTiles().remove(event.getTile().getWorldLocation());
        }
    }

    /**
     * GraphicsObjectCreated fires for projectiles / AOE graphics.
     * Darkness / parasite IDs are configurable (defaults 1767 / 1765 – verify in-game).
     * Tiles expire after config.dangerTileTicks() game ticks.
     */
    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (!state.isActive()) return;
        int id = event.getGraphicsObject().getId();
        if (id == config.gfxDarkness() || id == config.gfxParasite()) {
            WorldPoint wp = WorldPoint.fromLocal(
                client,
                event.getGraphicsObject().getLocation()
            );
            state.getDangerTiles().put(wp, client.getTickCount() + config.dangerTileTicks());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void countAutoAttack(int tick) {
        if (tick != state.getLastAttackTick()) {
            state.setLastAttackTick(tick);
            state.setTicksSinceAttack(0);
            state.setAutoAttackCount(state.getAutoAttackCount() + 1);
        }
    }

    private boolean isPhosaniNearby() {
        return client.getNpcs().stream()
            .anyMatch(n -> phosaniIds.contains(n.getId()));
    }

    /** Parses a comma-separated ID list; ignores malformed entries instead of throwing. */
    static Set<Integer> parseIds(String csv) {
        Set<Integer> ids = new HashSet<>();
        if (csv == null) return ids;
        for (String part : csv.split(",")) {
            String t = part.trim();
            if (t.isEmpty()) continue;
            try {
                ids.add(Integer.parseInt(t));
            } catch (NumberFormatException e) {
                log.debug("Ignoring invalid NPC id '{}'", t);
            }
        }
        return ids;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if ("phosanihelper".equals(event.getGroup()) && "npcIdPhosani".equals(event.getKey())) {
            phosaniIds = parseIds(config.npcIdPhosani());
        }
    }

    // ── Curse detection ───────────────────────────────────────────────────────

    /**
     * When Phosani's curse graphic appears on the local player's tile, notify
     * the prayer overlay so it remaps the prayer recommendation for 5 ticks.
     * Curse spot-anim ID is configurable (default 1769 – verify in-game).
     */
    @Subscribe
    public void onGraphicChanged(GraphicChanged event) {
        if (!(event.getActor() instanceof net.runelite.api.Player)) return;
        net.runelite.api.Player p = (net.runelite.api.Player) event.getActor();
        // Only care about the local player
        if (!p.equals(client.getLocalPlayer())) return;
        if (p.hasSpotAnim(config.gfxCurse())) {
            prayerOverlay.onCurseApplied();
        }
    }
}
