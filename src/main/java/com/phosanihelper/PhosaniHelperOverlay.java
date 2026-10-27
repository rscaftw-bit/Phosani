package com.phosanihelper;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;

/**
 * Renders tile-space overlays:
 *  • Magenta lane for the SURGE (she charges across the room)
 *  • Orange highlight on mushroom tiles
 *  • Red highlight on danger tiles (darkness / parasite AOE)
 *  • "3 autos until special" countdown text on Phosani
 */
public class PhosaniHelperOverlay extends Overlay {

    private final Client client;
    private final PhosaniHelperPlugin plugin;
    private final PhosaniHelperConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    public PhosaniHelperOverlay(Client client,
                                PhosaniHelperPlugin plugin,
                                PhosaniHelperConfig config,
                                ModelOutlineRenderer modelOutlineRenderer) {
        this.client               = client;
        this.plugin               = plugin;
        this.config               = config;
        this.modelOutlineRenderer = modelOutlineRenderer;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D g) {
        PhosaniState state = plugin.getState();
        if (!state.isActive()) return null;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ── 1. Danger tiles ──────────────────────────────────────────────────
        if (config.showDangerTiles()) {
            for (WorldPoint wp : state.getDangerTiles().keySet()) {
                renderTileFilled(g, wp, config.dangerColor(),
                    new Color(config.dangerColor().getRed(),
                        config.dangerColor().getGreen(),
                        config.dangerColor().getBlue(), 255), 1);
            }
        }

        // ── 2. Mushroom tiles ─────────────────────────────────────────────────
        if (config.showMushroomOverlay()) {
            for (WorldPoint wp : state.getMushroomTiles()) {
                renderTileFilled(g, wp, config.mushroomColor(),
                    config.mushroomColor().darker(), 2);
            }
        }

        // ── 2b. Adds: parasites, sleepwalkers, husks (by NPC name) ──────────────
        if (config.highlightNpcs()) {
            renderAdds(g);
        }

        // ── 2c. Parasite-in-you warning (from chat message) ───────────────────
        if (client.getTickCount() < state.getParasiteWarningUntil() && client.getLocalPlayer() != null) {
            String t = PhosaniLogic.parasiteText(state.isInvHasBalm(), state.isInvHasSanfew());
            net.runelite.api.Point loc = client.getLocalPlayer()
                .getCanvasTextLocation(g, t, client.getLocalPlayer().getLogicalHeight() + 60);
            if (loc != null) {
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.setColor(Color.BLACK);
                g.drawString(t, loc.getX() + 1, loc.getY() + 1);
                g.setColor((state.isInvHasBalm() || state.isInvHasSanfew()) ? Color.YELLOW : Color.RED);
                g.drawString(t, loc.getX(), loc.getY());
            }
        }

        // ── 3. Surge lane ─────────────────────────────────────────────────────
        if (config.showSurgeOverlay() && state.isSurgeActive()) {
            renderSurgeLane(g, state);
        }

        // ── 4. Auto-attack countdown on Phosani ───────────────────────────────
        if (config.showAutoCountdown() && state.getNightmare() != null && !state.isSurgeActive()) {
            int autosLeft = 4 - (state.getAutoAttackCount() % 4);
            renderNpcText(g, state.getNightmare(),
                autosLeft + " auto" + (autosLeft == 1 ? "" : "s") + " to special",
                autosLeft == 1 ? Color.BLUE : Color.WHITE);
        }

        return null;
    }

    // ── Adds rendering ────────────────────────────────────────────────────────

    private void renderAdds(Graphics2D g) {
        Color c = config.highlightColor();
        Color fill = new Color(c.getRed(), c.getGreen(), c.getBlue(), 50);
        for (NPC npc : client.getNpcs()) {
            if (npc == null || npc.isDead()) continue;
            if (!PhosaniLogic.nameMatches(npc.getName(), config.highlightNames())) continue;

            LocalPoint lp = npc.getLocalLocation();
            if (lp != null) {
                int size = npc.getComposition() != null ? npc.getComposition().getSize() : 1;
                Polygon tile = Perspective.getCanvasTileAreaPoly(client, lp, size);
                if (tile != null) {
                    g.setColor(fill);
                    g.fill(tile);
                    g.setColor(c);
                    g.setStroke(new BasicStroke(2));
                    g.draw(tile);
                }
            }
            Shape hull = npc.getConvexHull();
            if (hull != null) {
                g.setColor(c);
                g.setStroke(new BasicStroke(2));
                g.draw(hull);
            }
        }
    }

    // ── Surge rendering ───────────────────────────────────────────────────────

    /** Fills the lane she will charge through; stand outside it. */
    private void renderSurgeLane(Graphics2D g, PhosaniState state) {
        Color fill = config.surgeColor();
        Color border = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 255);
        for (WorldPoint wp : state.getSurgeTiles()) {
            renderTileFilled(g, wp, fill, border, 1);
        }
        NPC boss = state.getNightmare();
        if (boss != null) {
            renderNpcText(g, boss, "SURGE - GET OUT OF THE LANE", Color.MAGENTA);
        }
    }

    // ── Tile helpers ──────────────────────────────────────────────────────────

    private void renderTileFilled(Graphics2D g, WorldPoint wp,
                                  Color fill, Color border, int strokeWidth) {
        LocalPoint lp = LocalPoint.fromWorld(client, wp);
        if (lp == null) return;

        Polygon poly = Perspective.getCanvasTilePoly(client, lp);
        if (poly == null) return;

        // Fill
        g.setColor(fill);
        g.fill(poly);

        // Border
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(strokeWidth));
        g.setColor(border);
        g.draw(poly);
        g.setStroke(old);
    }

    private void renderNpcText(Graphics2D g, NPC npc, String text, Color color) {
        net.runelite.api.Point loc = npc.getCanvasTextLocation(g, text, npc.getLogicalHeight() + 40);
        if (loc == null) return;
        int[] xy = {loc.getX(), loc.getY()};

        // Shadow
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.setColor(Color.BLACK);
        g.drawString(text, xy[0] + 1, xy[1] + 1);
        g.setColor(color);
        g.drawString(text, xy[0], xy[1]);
    }
}
