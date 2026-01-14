package traffic.view;

import traffic.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MapPanel extends JPanel {
    private Carte carte;
    private final int CELL_SIZE = 100;
    private final int INTERSECTION_SIZE = 20;
    private final int ROAD_WIDTH = 10;
    private final int MARGIN = 70;

    public MapPanel(Carte carte) {
        this.carte = carte;
        int width = (carte.getCols() - 1) * CELL_SIZE + 2 * MARGIN;
        int height = (carte.getRows() - 1) * CELL_SIZE + 2 * MARGIN;
        setPreferredSize(new Dimension(width, height));
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
        g.setStroke(new BasicStroke(ROAD_WIDTH));

        int extension = CELL_SIZE / 2;

        for (int i = 0; i < carte.getRows(); i++) {
            Intersection first = carte.getIntersection(i, 0);
            Intersection last = carte.getIntersection(i, carte.getCols() - 1);
            Point pFirst = getIntersectionCenter(first);
            Point pLast = getIntersectionCenter(last);
            int y = pFirst.y;
            int x1 = pFirst.x - extension;
            int x2 = pLast.x + extension;
            g.drawLine(x1, y, x2, y);
        }

        for (int j = 0; j < carte.getCols(); j++) {
            Intersection top = carte.getIntersection(0, j);
            Intersection bottom = carte.getIntersection(carte.getRows() - 1, j);
            Point pTop = getIntersectionCenter(top);
            Point pBottom = getIntersectionCenter(bottom);
            int x = pTop.x;
            int y1 = pTop.y - extension;
            int y2 = pBottom.y + extension;
            g.drawLine(x, y1, x, y2);
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
                    if (feu.getCouleur() == CouleurFeu.VERT) {
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
                    if (v.getType() == TypeVehicule.BUS) {
                        g.fillRect(p.x - 6, p.y - 6, 12, 12);
                    } else {
                        int[] xs = {p.x, p.x - 6, p.x + 6};
                        int[] ys = {p.y - 7, p.y + 5, p.y + 5};
                        g.fillPolygon(xs, ys, 3);
                    }
                }
            }
        }
    }

    private Point getIntersectionCenter(Intersection inter) {
        return new Point(inter.getCol() * CELL_SIZE + MARGIN, inter.getRow() * CELL_SIZE + MARGIN);
    }
    
    private Point getVehiculePosition(Vehicule v) {
        Route r = v.getRouteActuelle();
        if (r == null) return null;
        
        Point p1 = getIntersectionCenter(r.getDepart());
        Point p2 = getIntersectionCenter(r.getArrivee());
        
        if (p1.distance(p2) > CELL_SIZE * 1.5) {
            Direction dir = r.getDirection();
            int dx = 0, dy = 0;
            int extension = CELL_SIZE / 2;
            if (dir == Direction.EST) dx = extension;
            else if (dir == Direction.OUEST) dx = -extension;
            else if (dir == Direction.SUD) dy = extension;
            else if (dir == Direction.NORD) dy = -extension;
            
            p2 = new Point(p1.x + dx, p1.y + dy);
        }

        double progress = v.getPosition() / r.getLongueur();
        int x = (int) (p1.x + (p2.x - p1.x) * progress);
        int y = (int) (p1.y + (p2.y - p1.y) * progress);
        return new Point(x, y);
    }
}
