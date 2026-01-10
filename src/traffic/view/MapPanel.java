package traffic.view;

import traffic.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MapPanel extends JPanel {
    private Carte carte;
    private final int CELL_SIZE = 100; // Taille d'une cellule de la grille en pixels
    private final int INTERSECTION_SIZE = 20;
    private final int ROAD_WIDTH = 10;

    public MapPanel(Carte carte) {
        this.carte = carte;
        setPreferredSize(new Dimension(carte.getCols() * CELL_SIZE + 50, carte.getRows() * CELL_SIZE + 50));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Dessiner les routes
        drawRoutes(g2d);

        // Dessiner les intersections
        drawIntersections(g2d);
        
        // Dessiner les véhicules
        drawVehicules(g2d);
    }

    private void drawRoutes(Graphics2D g) {
        g.setColor(Color.GRAY);
        for (Route route : carte.getRoutes()) {
            Point p1 = getIntersectionCenter(route.getDepart());
            Point p2 = getIntersectionCenter(route.getArrivee());
            
            // Gestion de l'affichage pour le wrapping (si la distance est trop grande, ne pas dessiner la ligne directe)
            if (p1.distance(p2) > CELL_SIZE * 1.5) {
                // C'est une route qui wrap, on peut dessiner des bouts si on veut, 
                // mais pour simplifier on ne dessine pas le trait qui traverse tout l'écran
                continue; 
            }

            g.setStroke(new BasicStroke(ROAD_WIDTH));
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
            
            // Dessiner une flèche pour la direction ?
        }
    }

    private void drawIntersections(Graphics2D g) {
        for (int i = 0; i < carte.getRows(); i++) {
            for (int j = 0; j < carte.getCols(); j++) {
                Intersection inter = carte.getIntersection(i, j);
                Point p = getIntersectionCenter(inter);
                
                g.setColor(Color.DARK_GRAY);
                g.fillOval(p.x - INTERSECTION_SIZE/2, p.y - INTERSECTION_SIZE/2, INTERSECTION_SIZE, INTERSECTION_SIZE);

                // Dessiner le feu s'il y en a un
                if (inter.aUnFeu()) {
                    FeuSignalisation feu = inter.getFeu();
                    if (feu.getEtat() == EtatFeu.VERT) {
                        g.setColor(Color.GREEN);
                    } else {
                        g.setColor(Color.RED);
                    }
                    g.fillOval(p.x - 6, p.y - 6, 12, 12);
                }
            }
        }
    }
    
    private void drawVehicules(Graphics2D g) {
        for (Route route : carte.getRoutes()) {
            for (Vehicule v : route.getVehicules()) {
                Point p = getVehiculePosition(v);
                if (p != null) {
                    g.setColor(v.getCouleur());
                    g.fillRect(p.x - 5, p.y - 5, 10, 10);
                }
            }
        }
    }

    private Point getIntersectionCenter(Intersection inter) {
        return new Point(inter.getCol() * CELL_SIZE + 50, inter.getRow() * CELL_SIZE + 50);
    }
    
    private Point getVehiculePosition(Vehicule v) {
        Route r = v.getRouteActuelle();
        if (r == null) return null;
        
        Point p1 = getIntersectionCenter(r.getDepart());
        Point p2 = getIntersectionCenter(r.getArrivee());
        
        // Gestion du wrapping pour l'affichage
        if (p1.distance(p2) > CELL_SIZE * 1.5) {
            // Si wrapping, on doit calculer différemment.
            // Pour simplifier l'affichage temporaire : on ne l'affiche que si pas au milieu du saut
            // Ou on calcule la position virtuelle.
            // Si c'est un saut de bordure, p2 est "loin" de p1.
            // On peut imaginer que p2 est juste à côté de p1 dans la direction donnée.
            
            Direction dir = r.getDirection();
            int dx = 0, dy = 0;
            if (dir == Direction.EST) dx = CELL_SIZE;
            else if (dir == Direction.OUEST) dx = -CELL_SIZE;
            else if (dir == Direction.SUD) dy = CELL_SIZE;
            else if (dir == Direction.NORD) dy = -CELL_SIZE;
            
            p2 = new Point(p1.x + dx, p1.y + dy);
        }

        double progress = v.getPosition() / r.getLongueur();
        int x = (int) (p1.x + (p2.x - p1.x) * progress);
        int y = (int) (p1.y + (p2.y - p1.y) * progress);
        return new Point(x, y);
    }
}
