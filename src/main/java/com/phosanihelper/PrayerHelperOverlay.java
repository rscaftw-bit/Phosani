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
 * Top-left HUD overlay showing:
 *  ┌─────────────────────────────────┐
 *  │  [CURSED]  ← orange banner if cursed
 *  │  [Prayer icon]  MAGIC           │
 *  │  Click: [icon you actually click]│  ← differs when cursed
 *  │  3 autos until special          │
 *  └─────────────────────────────────┘
 *
 * CURSE REMAP:
 *  When the player is cursed, Phosani's curse rotates prayer overhead icons
 *  one position counter-clockwise:
 *    To overhead MELEE  → you must click MAGIC
 *    To overhead MAGIC  → you must click RANGED
 *    To overhead RANGED → you must click MELEE
 *
 *  The overlay always shows:
 *   • Top row  = what overhead is needed (correct protection)
 *   • Bottom row = what icon you physically click in the prayer book
 */
public class PrayerHelperOverlay extends Overlay {

    private final Client client;
    private final PhosaniHelperPlugin plugin;
    private final PhosaniHelperConfig config;
    private final SpriteManager spriteManager;

    // Curse tracking
    private int curseExpiryTick = -1;

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

    // ── Called by plugin when the curse graphic is detected on the local player ─

    public void onCurseApplied() {
        curseExpiryTick = client.getTickCount() + config.curseDurationTicks();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    public Dimension render(Graphics2D g) {
        PhosaniState state = plugin.getState();
        if (!config.showPrayerHelper() || !state.isActive()) return null;
        if (!state.isPrayerIndicatorActive() && !isCursed()) return null;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean cursed = isCursed();
        Prayer needed  = state.getRecommendedPrayer();
        Prayer toClick = cursed ? remapForCurse(needed) : needed;

        int iconSize  = config.prayerIconSize();
        int padding   = 8;
        int panelW    = iconSize + padding * 2 + (cursed ? 20 : 0);
        int panelH    = (cursed ? 28 : 0)       // curse banner
                      + (needed != null ? iconSize + 24 : 0)   // needed icon + label
                      + (cursed && toClick != null ? iconSize / 2 + 20 : 0) // click icon
                      + padding * 2;

        int x = padding;
        int y = padding;

        // Panel background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(x, y, panelW, panelH, 10, 10);
        g.setColor(new Color(255, 165, 0, 220));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(x, y, panelW, panelH, 10, 10);

        int cx = x + padding;
        int cy = y + padding;

        // ── CURSED banner ──────────────────────────────────────────────────
        if (cursed) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(new Color(255, 80, 0));
            String curseText = "! CURSED - PRAYERS ROTATED";
            g.drawString(curseText, cx, cy + 16);
            cy += 28;
        }

        // ── Needed prayer icon (what protection you need) ─────────────────
        if (needed != null) {
            // Header label
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("PROTECT FROM:", cx, cy + 13);
            cy += 18;

            // Prayer icon
            renderPrayerIcon(g, needed, cx, cy, iconSize);

            // Attack-style text
            if (config.showAttackStyleText()) {
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.setColor(prayerColor(needed));
                String label = prayerLabel(needed);
                g.drawString(label, cx + iconSize / 2 - g.getFontMetrics().stringWidth(label) / 2, cy + iconSize + 16);
            }
            cy += iconSize + 22;
        }

        // ── Click hint (different from needed when cursed) ────────────────
        if (cursed && toClick != null && toClick != needed) {
            g.setFont(new Font("Arial", Font.BOLD, 11));
            g.setColor(new Color(255, 220, 50));
            g.drawString("CLICK THIS ICON →", cx, cy + 11);
            cy += 15;

            int smallSize = iconSize / 2;
            renderPrayerIcon(g, toClick, cx, cy, smallSize);

            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(prayerColor(toClick));
            String clickLabel = prayerLabel(toClick);
            g.drawString(clickLabel, cx + smallSize + 4, cy + smallSize / 2 + 5);
            cy += smallSize + 6;
        }

        // ── Auto-attack countdown ─────────────────────────────────────────
        if (config.showAutoCountdown() && state.isActive()) {
            int autosLeft = 4 - (state.getAutoAttackCount() % 4);
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            g.setColor(autosLeft == 1 ? Color.RED : Color.WHITE);
            String cntText = autosLeft + " auto" + (autosLeft == 1 ? "" : "s") + " → special";
            g.drawString(cntText, cx, cy + 12);
        }

        return new Dimension(panelW + padding * 2, panelH + padding * 2);
    }

    // ── Curse logic ───────────────────────────────────────────────────────────

    public boolean isCursed() {
        return client.getTickCount() < curseExpiryTick;
    }

    /**
     * Phosani's curse rotates overhead prayers one slot counter-clockwise:
     *   Overhead MELEE  = prayer slot that was MAGIC
     *   Overhead MAGIC  = prayer slot that was RANGED
     *   Overhead RANGED = prayer slot that was MELEE
     *
     * So to achieve the needed overhead, you click the prayer one step "back":
     *   Need MELEE  → click MAGIC
     *   Need MAGIC  → click RANGED
     *   Need RANGED → click MELEE
     */
    private Prayer remapForCurse(Prayer needed) {
        if (needed == null) return null;
        if (needed == Prayer.PROTECT_FROM_MELEE)    return Prayer.PROTECT_FROM_MAGIC;
        if (needed == Prayer.PROTECT_FROM_MAGIC)    return Prayer.PROTECT_FROM_MISSILES;
        if (needed == Prayer.PROTECT_FROM_MISSILES) return Prayer.PROTECT_FROM_MELEE;
        return needed;
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
