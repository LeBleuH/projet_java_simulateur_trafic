package traffic.view;

import traffic.controller.Simulateur;
import traffic.model.Carte;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

public class MainFrame extends JFrame {
    private MapPanel mapPanel;
    private Simulateur simulateur;
    private JButton startPauseButton;
    private JButton resetButton;
    private JSlider speedSlider;
    private JLabel vehicleCountLabel;
    private JLabel timeLabel;
    private JLabel movingStoppedLabel;
    private JLabel distanceLabel;
    private JSpinner vehicleSpinner;

    public MainFrame(Carte carte, Simulateur simulateur) {
        this.simulateur = simulateur;
        
        setTitle("Simulateur de Trafic Urbain");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mapPanel = new MapPanel(carte);
        JScrollPane scrollPane = new JScrollPane(mapPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(4, 1));

        vehicleCountLabel = new JLabel("Véhicules: 0");
        timeLabel = new JLabel("Temps: 00:00");
        movingStoppedLabel = new JLabel("Mouvement: 0 / Arrêt: 0");
        distanceLabel = new JLabel("Distance totale: 0.0");

        infoPanel.add(vehicleCountLabel);
        infoPanel.add(timeLabel);
        infoPanel.add(movingStoppedLabel);
        infoPanel.add(distanceLabel);
        infoPanel.setPreferredSize(new Dimension(180, 100));
        add(infoPanel, BorderLayout.EAST);

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
        speedSlider = new JSlider(0, 2, 1);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        Hashtable<Integer, JLabel> labels = new Hashtable<>();
        labels.put(0, new JLabel("x0.5"));
        labels.put(1, new JLabel("x1"));
        labels.put(2, new JLabel("x2"));
        speedSlider.setLabelTable(labels);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> {
            int val = speedSlider.getValue();
            int delay;
            if (val == 0) {
                delay = 200;
            } else if (val == 1) {
                delay = 100;
            } else {
                delay = 50;
            }
            simulateur.setDelay(delay);
        });
        controlPanel.add(speedSlider);

        controlPanel.add(new JLabel("Nombre de véhicules:"));
        vehicleSpinner = new JSpinner(new SpinnerNumberModel(8, 0, 200, 1));
        controlPanel.add(vehicleSpinner);
        JButton applyVehicleButton = new JButton("Appliquer");
        applyVehicleButton.addActionListener(e -> {
            int n = (int) vehicleSpinner.getValue();
            simulateur.setNombreVehicules(n);
            simulateur.reset();
        });
        controlPanel.add(applyVehicleButton);

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
        long secondes = simulateur.getElapsedSeconds();
        long minutes = secondes / 60;
        long resteSecondes = secondes % 60;
        String tempsTexte = String.format("Temps: %02d:%02d", minutes, resteSecondes);
        timeLabel.setText(tempsTexte);
        int enMouvement = simulateur.getMovingVehicleCount();
        int aArret = simulateur.getStoppedVehicleCount();
        movingStoppedLabel.setText("Mouvement: " + enMouvement + " / Arrêt: " + aArret);
        double distance = simulateur.getDistanceTotaleParcourue();
        distanceLabel.setText(String.format("Distance totale: %.1f", distance));
    }
}
