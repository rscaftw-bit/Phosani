package com.phosanihelper;

import net.runelite.api.Client;
import net.runelite.api.Prayer;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Top-left HUD: the protection prayer to use. While cursed (detected from chat)
 * the big icon is the prayer to physically CLICK, remapped per config.
 */
public class PrayerHelperOverlay extends Overlay {

    private final Client client;
    private final PhosaniHelperPlugin plugin;
    private final PhosaniHelperConfig config;
    private final SpriteManager spriteManager;

    @Inject
    public PrayerHelperOverlay(Client client,
                               PhosaniHelperPlugin plugin,
                               PhosaniHelperConfig config,
                               SpriteManager spriteManager) {
        this.client        = client;
        this.plugin        = plugin;
        this.config        = config;
        this.spriteManager = spriteManager;

        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGHEST);
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public Dimension render(Graphics2D g) {
        PhosaniState state = plugin.getState();
        if (!config.showPrayerHelper() || !state.isActive()) return null;

        boolean cursed = state.isCursed();
        Prayer needed = PhosaniLogic.indicatorPrayer(cursed, state.getTicksSinceAttack(), state.getRecommendedPrayer());
        if (needed == null && !cursed) return null;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // While cursed the big icon is the prayer you physically CLICK.
        Prayer shown = cursed ? PhosaniLogic.prayerToClick(needed, config.curseRotation()) : needed;

        int iconSize = config.prayerIconSize();
        int padding  = 8;
        int panelW   = Math.max(iconSize, 150) + padding * 2;
        int panelH   = padding * 2
            + 20                                   // header
            + (shown != null ? iconSize + 24 : 0)  // icon + label
            + (cursed && needed != null ? 16 : 0)  // "blocks X" line
            + (cursed && needed == null ? 48 : 0)  // mapping table
            + (config.showAutoCountdown() ? 16 : 0);

        int x = padding, y = padding;
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(x, y, panelW, panelH, 10, 10);
        g.setColor(cursed ? new Color(255, 60, 0, 230) : new Color(255, 165, 0, 220));
        g.setStroke(new BasicStroke(cursed ? 3 : 2));
        g.drawRoundRect(x, y, panelW, panelH, 10, 10);

        int cx = x + padding;
        int cy = y + padding;

        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.setColor(cursed ? new Color(255, 90, 0) : Color.LIGHT_GRAY);
        g.drawString(cursed ? "CURSED - CLICK:" : "PRAY:", cx, cy + 14);
        cy += 20;

        if (shown != null) {
            renderPrayerIcon(g, shown, cx, cy, iconSize);
            if (config.showAttackStyleText()) {
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.setColor(prayerColor(shown));
                String label = prayerLabel(shown);
                g.drawString(label, cx + iconSize / 2 - g.getFontMetrics().stringWidth(label) / 2, cy + iconSize + 16);
            }
            cy += iconSize + 24;
        }

        if (cursed && needed != null) {
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("(blocks her " + prayerLabel(needed).toLowerCase() + " attack)", cx, cy + 11);
            cy += 16;
        } else if (cursed) {
            // No attack seen yet: show the full remap so you can pre-pray
            g.setFont(new Font("Arial", Font.BOLD, 12));
            Prayer[] all = {Prayer.PROTECT_FROM_MELEE, Prayer.PROTECT_FROM_MAGIC, Prayer.PROTECT_FROM_MISSILES};
            for (Prayer need : all) {
                Prayer click = PhosaniLogic.prayerToClick(need, config.curseRotation());
                g.setColor(prayerColor(click));
                g.drawString(prayerLabel(need) + " attack \u2192 click " + prayerLabel(click), cx, cy + 12);
                cy += 16;
            }
        }

        if (config.showAutoCountdown()) {
            int autosLeft = 4 - (state.getAutoAttackCount() % 4);
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            g.setColor(autosLeft == 1 ? Color.BLUE : Color.WHITE);
            g.drawString(autosLeft + " auto" + (autosLeft == 1 ? "" : "s") + " \u2192 special", cx, cy + 12);
        }

        return new Dimension(panelW + padding * 2, panelH + padding * 2);
    }

    // ── Icon rendering ────────────────────────────────────────────────────────

    private void renderPrayerIcon(Graphics2D g, Prayer prayer, int x, int y, int size) {
        int spriteId = prayerSpriteId(prayer);
        BufferedImage sprite = spriteManager.getSprite(spriteId, 0);

        // Draw coloured glow behind icon
        g.setColor(new Color(prayerColor(prayer).getRed(),
            prayerColor(prayer).getGreen(),
            prayerColor(prayer).getBlue(), 60));
        g.fillRoundRect(x - 2, y - 2, size + 4, size + 4, 8, 8);
        g.setColor(prayerColor(prayer));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x - 2, y - 2, size + 4, size + 4, 8, 8);

        if (sprite != null) {
            g.drawImage(sprite, x, y, size, size, null);
        } else {
            // Fallback: draw a coloured box with first letter
            g.setColor(prayerColor(prayer).darker());
            g.fillRoundRect(x, y, size, size, 6, 6);
            g.setFont(new Font("Arial", Font.BOLD, size / 2));
            g.setColor(Color.WHITE);
            String letter = prayerLabel(prayer).substring(0, 1);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(letter, x + (size - fm.stringWidth(letter)) / 2,
                y + (size + fm.getAscent()) / 2 - 2);
        }
    }

    // ── Sprite IDs ────────────────────────────────────────────────────────────

    private int prayerSpriteId(Prayer prayer) {
        // RuneLite SpriteID constants for protect prayers
        if (prayer == Prayer.PROTECT_FROM_MELEE)    return SpriteID.PRAYER_PROTECT_FROM_MELEE;
        if (prayer == Prayer.PROTECT_FROM_MAGIC)    return SpriteID.PRAYER_PROTECT_FROM_MAGIC;
        if (prayer == Prayer.PROTECT_FROM_MISSILES) return SpriteID.PRAYER_PROTECT_FROM_MISSILES;
        return -1;
    }

    private String prayerLabel(Prayer prayer) {
        if (prayer == Prayer.PROTECT_FROM_MELEE)    return "MELEE";
        if (prayer == Prayer.PROTECT_FROM_MAGIC)    return "MAGIC";
        if (prayer == Prayer.PROTECT_FROM_MISSILES) return "RANGED";
        return "???";
    }

    private Color prayerColor(Prayer prayer) {
        if (prayer == Prayer.PROTECT_FROM_MELEE)    return new Color(255, 80,  80);  // red
        if (prayer == Prayer.PROTECT_FROM_MAGIC)    return new Color(80,  160, 255); // blue
        if (prayer == Prayer.PROTECT_FROM_MISSILES) return new Color(80,  220, 80);  // green
        return Color.WHITE;
    }
}
