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
import java.awt.geom.Line2D;

/**
 * Renders tile-space overlays:
 *  • Red line + green tile for the DASH safe-side indicator
 *  • Orange highlight on mushroom tiles
 *  • Red highlight on danger tiles (darkness / parasite AOE)
 *  • "3 autos until special" countdown text on Phosani
 */
public class PhosaniHelperOverlay extends Overlay {

    private static final int TILE_SIZE        = Perspective.LOCAL_TILE_SIZE;
    private static final int DASH_LINE_RADIUS = 10; // tiles either side of the line

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

        // ── 3. Dash indicator ─────────────────────────────────────────────────
        if (config.showDashOverlay() && state.isDashing() && state.getDashOrigin() != null) {
            renderDashIndicator(g, state);
        }

        // ── 4. Auto-attack countdown on Phosani ───────────────────────────────
        if (config.showAutoCountdown() && state.getNightmare() != null) {
            int autosLeft = 4 - (state.getAutoAttackCount() % 4);
            renderNpcText(g, state.getNightmare(),
                autosLeft + " auto" + (autosLeft == 1 ? "" : "s") + " to special",
                autosLeft == 1 ? Color.RED : Color.WHITE);
        }

        return null;
    }

    // ── Dash rendering ────────────────────────────────────────────────────────

    /**
     * Draws a red line through Phosani from her dash origin direction,
     * and highlights the tile BEHIND her (the safe side) in green.
     */
    private void renderDashIndicator(Graphics2D g, PhosaniState state) {
        NPC boss = state.getNightmare();
        // Scene (not template) coordinates so LocalPoint.fromWorld works inside the instance
        WorldPoint bossWp = WorldPoint.fromLocal(client, boss.getLocalLocation());
        WorldPoint originWp = state.getDashOrigin();

        if (bossWp == null || originWp == null) return;

        // Vector from dash origin to boss = dash direction
        int dx = bossWp.getX() - originWp.getX();
        int dy = bossWp.getY() - originWp.getY();

        // Safe tile = 2 tiles behind boss (opposite to dash direction)
        int safeX = bossWp.getX() - (int) Math.signum(dx) * 2;
        int safeY = bossWp.getY() - (int) Math.signum(dy) * 2;
        WorldPoint safeWp = new WorldPoint(safeX, safeY, bossWp.getPlane());

        // Draw safe tile in green
        renderTileFilled(g, safeWp, config.safeTileColor(), config.safeTileColor().darker(), 2);

        // Draw a thick red line through the boss perpendicular to the dash
        Point bossCenter = getCanvasCenter(bossWp);
        if (bossCenter == null) return;

        // Perpendicular direction (rotate 90°)
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;
        double perpX = -dy / len;
        double perpY =  dx / len;

        int lineExtent = DASH_LINE_RADIUS * 20; // approx pixels
        int x1 = (int)(bossCenter.x + perpX * lineExtent);
        int y1 = (int)(bossCenter.y + perpY * lineExtent);
        int x2 = (int)(bossCenter.x - perpX * lineExtent);
        int y2 = (int)(bossCenter.y - perpY * lineExtent);

        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(config.dashLineColor());
        g.draw(new Line2D.Float(x1, y1, x2, y2));
        g.setStroke(old);

        // Label
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("MOVE BEHIND", bossCenter.x - 45, bossCenter.y - 20);
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

    private Point getCanvasCenter(WorldPoint wp) {
        LocalPoint lp = LocalPoint.fromWorld(client, wp);
        if (lp == null) return null;
        net.runelite.api.Point p = Perspective.localToCanvas(client, lp, client.getPlane());
        return p == null ? null : new Point(p.getX(), p.getY());
    }
}
