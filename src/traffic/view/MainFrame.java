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
    private JSpinner vehicleSpinner;

    public MainFrame(Carte carte, Simulateur simulateur) {
        this.simulateur = simulateur;
        
        setTitle("Simulateur de Trafic Urbain");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mapPanel = new MapPanel(carte);
        JScrollPane scrollPane = new JScrollPane(mapPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new GridLayout(2, 1));

        JPanel voitureLegend = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = 15;
                int cy = getHeight() / 2;
                g2d.setColor(Color.BLUE);
                int[] xs = {cx, cx - 6, cx + 6};
                int[] ys = {cy - 7, cy + 5, cy + 5};
                g2d.fillPolygon(xs, ys, 3);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Voiture", 30, cy + 4);
            }
        };

        JPanel busLegend = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = 15;
                int cy = getHeight() / 2;
                g2d.setColor(Color.ORANGE);
                g2d.fillRect(cx - 6, cy - 6, 12, 12);
                g2d.setColor(Color.BLACK);
                g2d.drawString("Bus", 30, cy + 4);
            }
        };

        legendPanel.add(voitureLegend);
        legendPanel.add(busLegend);
        legendPanel.setPreferredSize(new Dimension(120, 80));
        add(legendPanel, BorderLayout.EAST);

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
            int delay = 200 / val;
            simulateur.setDelay(delay);
        });
        controlPanel.add(speedSlider);
        
        vehicleCountLabel = new JLabel("Véhicules: 0");
        controlPanel.add(vehicleCountLabel);

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
    }
}
