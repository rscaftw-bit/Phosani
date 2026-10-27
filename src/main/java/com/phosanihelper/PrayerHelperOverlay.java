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

        // Resizable mode: bottom-right of the game view = just left of the inventory (Alt+drag to move)
        setPosition(OverlayPosition.BOTTOM_RIGHT);
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

        // Compact box: exactly 60 px wide. Icon shrinks to fit (config size is an upper limit).
        final int panelW  = 60;
        final int padding = 4;
        final int inner   = panelW - padding * 2;
        int iconSize = Math.max(16, Math.min(config.prayerIconSize(), inner - 4));
        boolean showLabel = shown != null && config.showAttackStyleText();

        int panelH = padding * 2
            + 14                                       // header
            + (shown != null ? iconSize + 6 : 0)       // icon (+glow border)
            + (showLabel ? 13 : 0)                     // style label
            + (cursed && needed != null ? 12 : 0)      // "vs X" line
            + (cursed && needed == null ? 36 : 0)      // mapping table (3 lines)
            + (cursed ? 12 : 0)                        // curse attack counter
            + (config.showAutoCountdown() ? 12 : 0);

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(0, 0, panelW, panelH, 8, 8);
        g.setColor(cursed ? new Color(255, 60, 0, 230) : new Color(255, 165, 0, 220));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(1, 1, panelW - 2, panelH - 2, 8, 8);

        int cy = padding;
        FontMetrics fm;
        String text;

        g.setFont(new Font("Arial", Font.BOLD, 11));
        fm = g.getFontMetrics();
        text = cursed ? "CLICK" : "PRAY";
        g.setColor(cursed ? new Color(255, 90, 0) : Color.LIGHT_GRAY);
        g.drawString(text, (panelW - fm.stringWidth(text)) / 2, cy + 11);
        cy += 14;

        if (shown != null) {
            renderPrayerIcon(g, shown, (panelW - iconSize) / 2, cy + 2, iconSize);
            cy += iconSize + 6;
            if (showLabel) {
                g.setFont(new Font("Arial", Font.BOLD, 10));
                fm = g.getFontMetrics();
                text = prayerLabel(shown);
                g.setColor(prayerColor(shown));
                g.drawString(text, (panelW - fm.stringWidth(text)) / 2, cy + 10);
                cy += 13;
            }
        }

        g.setFont(new Font("Arial", Font.BOLD, 9));
        fm = g.getFontMetrics();
        if (cursed && needed != null) {
            text = "vs " + prayerLabel(needed).substring(0, 3);
            g.setColor(Color.LIGHT_GRAY);
            g.drawString(text, (panelW - fm.stringWidth(text)) / 2, cy + 9);
            cy += 12;
        } else if (cursed) {
            // No attack seen yet: compact remap, e.g. "MEL>RNG" = melee attack -> click Missiles
            g.setFont(new Font("Arial", Font.BOLD, 8));
            fm = g.getFontMetrics();
            Prayer[] all = {Prayer.PROTECT_FROM_MELEE, Prayer.PROTECT_FROM_MAGIC, Prayer.PROTECT_FROM_MISSILES};
            for (Prayer need : all) {
                Prayer click = PhosaniLogic.prayerToClick(need, config.curseRotation());
                text = prayerLabel(need).substring(0, 3) + ">" + prayerLabel(click).substring(0, 3);
                g.setColor(prayerColor(click));
                g.drawString(text, (panelW - fm.stringWidth(text)) / 2, cy + 9);
                cy += 12;
            }
        }

        g.setFont(new Font("Arial", Font.BOLD, 9));
        fm = g.getFontMetrics();
        if (cursed) {
            text = "Curse " + Math.min(state.getCursedAttacks(), PhosaniLogic.CURSE_ATTACKS)
                + "/" + PhosaniLogic.CURSE_ATTACKS;
            g.setColor(new Color(255, 120, 200));
            g.drawString(text, (panelW - fm.stringWidth(text)) / 2, cy + 9);
            cy += 12;
        }

        if (config.showAutoCountdown()) {
            int autosLeft = 4 - (state.getAutoAttackCount() % 4);
            text = autosLeft + " \u2192 spec";
            g.setColor(autosLeft == 1 ? Color.BLUE : Color.WHITE);
            g.drawString(text, (panelW - fm.stringWidth(text)) / 2, cy + 9);
        }

        return new Dimension(panelW, panelH);
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
