package traffic.view;

import traffic.controller.Simulateur;
import traffic.model.Carte;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private MapPanel mapPanel;
    private Simulateur simulateur;
    private JButton startPauseButton;
    private JButton resetButton;
    private JSlider speedSlider;
    private JLabel vehicleCountLabel;

    public MainFrame(Carte carte, Simulateur simulateur) {
        this.simulateur = simulateur;
        
        setTitle("Simulateur de Trafic Urbain");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Carte au centre
        mapPanel = new MapPanel(carte);
        add(new JScrollPane(mapPanel), BorderLayout.CENTER);

        // Contrôles en bas
        JPanel controlPanel = new JPanel();
        
        startPauseButton = new JButton("Démarrer");
        startPauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleSimulation();
            }
        });
        controlPanel.add(startPauseButton);

        resetButton = new JButton("Réinitialiser");
        resetButton.addActionListener(e -> simulateur.reset());
        controlPanel.add(resetButton);

        controlPanel.add(new JLabel("Vitesse:"));
        speedSlider = new JSlider(1, 4, 2); // 1=0.5x, 2=1x, 4=2x roughly
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.addChangeListener(e -> {
            int val = speedSlider.getValue();
            // Ajuster le délai du timer (inversement proportionnel)
            // Base delay = 100ms
            // val=1 -> 200ms, val=2 -> 100ms, val=4 -> 50ms
            int delay = 200 / val;
            simulateur.setDelay(delay);
        });
        controlPanel.add(speedSlider);
        
        vehicleCountLabel = new JLabel("Véhicules: 0");
        controlPanel.add(vehicleCountLabel);

        add(controlPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void toggleSimulation() {
        if (simulateur.isRunning()) {
            simulateur.pause();
            startPauseButton.setText("Reprendre");
        } else {
            simulateur.start();
            startPauseButton.setText("Pause");
        }
    }

    public void refresh() {
        mapPanel.repaint();
        vehicleCountLabel.setText("Véhicules: " + simulateur.getVehicleCount());
    }
}
