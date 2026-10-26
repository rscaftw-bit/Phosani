package com.phosanihelper;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import java.awt.Color;

/** Simple live-status side panel for the PNM sidebar button. */
public class PhosaniPanel extends PluginPanel {
    private final JLabel fight = new JLabel();
    private final JLabel curse = new JLabel();
    private final JLabel attack = new JLabel();
    private final JLabel autos = new JLabel();
    private final JLabel surge = new JLabel();
    private final JLabel parasite = new JLabel();

    PhosaniPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Phosani's Nightmare");
        title.setForeground(Color.WHITE);
        add(title);
        for (JLabel l : new JLabel[]{fight, curse, attack, autos, surge, parasite}) {
            l.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
            add(l);
        }
        update(false, false, "NONE", "-", false, "no");
    }

    void update(boolean active, boolean cursed, String lastAttack, String autosLeft,
                boolean surgeOn, String parasiteTxt) {
        fight.setText("Fight: " + (active ? "active" : "not detected"));
        curse.setText("Curse: " + (cursed ? "ON (prayers shuffled)" : "off"));
        attack.setText("Last attack: " + lastAttack);
        autos.setText("Autos to special: " + autosLeft);
        surge.setText("Surge: " + (surgeOn ? "ACTIVE" : "no"));
        parasite.setText("<html>Parasite: " + parasiteTxt + "</html>");
    }
}
