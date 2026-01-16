package traffic.view;

import traffic.model.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MapPanel extends JPanel {
    private Carte carte;
    private final int CELL_SIZE = 180;
    private final int INTERSECTION_SIZE = 36;
    private final int ROAD_WIDTH = 32;
    private final int MARGIN = 70;
    private final int CROSSWALK_OFFSET = 18;
    private final int CROSSWALK_THICKNESS = 4;
    private final int TROTTOIR_LENGTH = 28;
    private final int BUS_MAX_SIZE = 54;
    private final int CAR_MAX_SIZE = 29;
    private final int FLECHE_MAX_SIZE = CELL_SIZE / 3;
    private final int DECOR_MAX_SIZE = 80;

    private BufferedImage busImage;
    private BufferedImage vehiculeImage;
    private BufferedImage trottoirEW;
    private BufferedImage trottoirNS;
    private BufferedImage flecheNord;
    private BufferedImage flecheSud;
    private BufferedImage flecheEst;
    private BufferedImage flecheOuest;
    private BufferedImage treeImage;
    private BufferedImage houseImage;
    private final List<Decoration> decorations = new ArrayList<>();
    private final Random decorRandom = new Random();

    public MapPanel(Carte carte) {
        this.carte = carte;
        int width = (carte.getCols() - 1) * CELL_SIZE + 2 * MARGIN;
        int height = (carte.getRows() - 1) * CELL_SIZE + 2 * MARGIN;
        setPreferredSize(new Dimension(width, height));
        busImage = loadAndScale("figure/bus.png", BUS_MAX_SIZE);
        vehiculeImage = loadAndScale("figure/vehicule.png", CAR_MAX_SIZE);
        BufferedImage trottoirRaw = loadImage("figure/trottoir.png");
        if (trottoirRaw != null) {
            BufferedImage trimmed = trimTransparent(trottoirRaw);
            trottoirEW = scaleToFit(trimmed, ROAD_WIDTH, TROTTOIR_LENGTH);
            trottoirNS = scaleToFit(rotate90(trimmed), TROTTOIR_LENGTH, ROAD_WIDTH);
        }
        BufferedImage flecheRaw = loadImage("figure/fleche.png");
        if (flecheRaw != null) {
            BufferedImage trimmed = trimTransparent(flecheRaw);
            BufferedImage single = extractCentralArrow(trimmed);
            BufferedImage nord = scaleToMax(single, FLECHE_MAX_SIZE);
            BufferedImage est = rotate90(nord);
            BufferedImage sud = rotate90(est);
            BufferedImage ouest = rotate90(sud);
            flecheNord = nord;
            flecheEst = est;
            flecheSud = sud;
            flecheOuest = ouest;
        }
        treeImage = loadAndScale("figure/tree.png", DECOR_MAX_SIZE);
        houseImage = loadAndScale("figure/maison.png", DECOR_MAX_SIZE);
        initDecorations();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawRoutes(g2d);
        drawDecorations(g2d);
        drawArrows(g2d);
        drawIntersections(g2d);
        drawVehicules(g2d);
    }

    private void initDecorations() {
        decorations.clear();
        if (treeImage == null && houseImage == null) {
            return;
        }
        int rows = carte.getRows();
        int cols = carte.getCols();
        decorations.clear();
        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < cols - 1; j++) {
                BufferedImage img;
                if (treeImage != null && houseImage != null) {
                    img = decorRandom.nextBoolean() ? treeImage : houseImage;
                } else if (treeImage != null) {
                    img = treeImage;
                } else {
                    img = houseImage;
                }
                if (img == null) {
                    continue;
                }
                decorations.add(new Decoration(i, j, img));
            }
        }
    }

    private void drawRoutes(Graphics2D g) {
        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke(ROAD_WIDTH));

        int extension = CELL_SIZE * 2;

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

    private void drawDecorations(Graphics2D g) {
        int rows = carte.getRows();
        int cols = carte.getCols();
        for (Decoration d : decorations) {
            if (d.image == null) {
                continue;
            }
            if (d.row < 0 || d.row >= rows - 1 || d.col < 0 || d.col >= cols - 1) {
                continue;
            }
            Intersection topLeft = carte.getIntersection(d.row, d.col);
            Intersection bottomRight = carte.getIntersection(d.row + 1, d.col + 1);
            Point pTL = getIntersectionCenter(topLeft);
            Point pBR = getIntersectionCenter(bottomRight);
            int margin = ROAD_WIDTH + 10;
            int left = pTL.x + margin;
            int right = pBR.x - margin;
            int top = pTL.y + margin;
            int bottom = pBR.y - margin;
            if (right <= left || bottom <= top) {
                continue;
            }
            int regionWidth = right - left;
            int regionHeight = bottom - top;
            if (d.image.getWidth() > regionWidth || d.image.getHeight() > regionHeight) {
                continue;
            }
            int centerX = (left + right) / 2;
            int centerY = (top + bottom) / 2;
            int x = centerX - d.image.getWidth() / 2;
            int y = centerY - d.image.getHeight() / 2;
            g.drawImage(d.image, x, y, null);
        }
    }

    private void drawArrows(Graphics2D g) {
        if (flecheEst == null || flecheNord == null || flecheSud == null || flecheOuest == null) {
            return;
        }
        int rows = carte.getRows();
        int cols = carte.getCols();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols - 1; j++) {
                Intersection a = carte.getIntersection(i, j);
                Intersection b = carte.getIntersection(i, j + 1);
                Direction dir = null;
                Route rEst = a.getRouteSortante(Direction.EST);
                if (rEst != null && rEst.getArrivee() == b) {
                    dir = Direction.EST;
                } else {
                    Route rOuest = b.getRouteSortante(Direction.OUEST);
                    if (rOuest != null && rOuest.getArrivee() == a) {
                        dir = Direction.OUEST;
                    }
                }
                if (dir != null) {
                    Point p1 = getIntersectionCenter(a);
                    Point p2 = getIntersectionCenter(b);
                    int cx = (p1.x + p2.x) / 2;
                    int cy = (p1.y + p2.y) / 2;
                    BufferedImage img = (dir == Direction.EST) ? flecheEst : flecheOuest;
                    int w = img.getWidth();
                    int h = img.getHeight();
                    int x = cx - w / 2;
                    int y = cy - h / 2;
                    g.drawImage(img, x, y, null);
                }
            }
        }

        for (int i = 0; i < rows - 1; i++) {
            for (int j = 0; j < cols; j++) {
                Intersection a = carte.getIntersection(i, j);
                Intersection b = carte.getIntersection(i + 1, j);
                Direction dir = null;
                Route rSud = a.getRouteSortante(Direction.SUD);
                if (rSud != null && rSud.getArrivee() == b) {
                    dir = Direction.SUD;
                } else {
                    Route rNord = b.getRouteSortante(Direction.NORD);
                    if (rNord != null && rNord.getArrivee() == a) {
                        dir = Direction.NORD;
                    }
                }
                if (dir != null) {
                    Point p1 = getIntersectionCenter(a);
                    Point p2 = getIntersectionCenter(b);
                    int cx = (p1.x + p2.x) / 2;
                    int cy = (p1.y + p2.y) / 2;
                    BufferedImage img = (dir == Direction.SUD) ? flecheSud : flecheNord;
                    int w = img.getWidth();
                    int h = img.getHeight();
                    int x = cx - w / 2;
                    int y = cy - h / 2;
                    g.drawImage(img, x, y, null);
                }
            }
        }
    }

    private void drawIntersections(Graphics2D g) {
        for (int i = 0; i < carte.getRows(); i++) {
            for (int j = 0; j < carte.getCols(); j++) {
                Intersection inter = carte.getIntersection(i, j);
                Point p = getIntersectionCenter(inter);

                g.setColor(Color.WHITE);
                int halfRoad = ROAD_WIDTH / 2;
                int t = CROSSWALK_THICKNESS;

                // Passages piétons pour la route verticale (nord/sud)
                if (trottoirEW != null) {
                    int w = trottoirEW.getWidth();
                    int h = trottoirEW.getHeight();
                    int x = p.x - w / 2;
                    // Nord: prolongement vers le haut depuis l'intersection
                    int yTop = p.y - halfRoad - h;
                    // Sud: prolongement vers le bas depuis l'intersection
                    int yBottom = p.y + halfRoad;
                    g.drawImage(trottoirEW, x, yTop, null);
                    g.drawImage(trottoirEW, x, yBottom, null);
                } else {
                    g.fillRect(p.x - halfRoad, p.y - CROSSWALK_OFFSET - t / 2, ROAD_WIDTH, t);
                    g.fillRect(p.x - halfRoad, p.y + CROSSWALK_OFFSET - t / 2, ROAD_WIDTH, t);
                }

                // Passages piétons pour la route horizontale (est/ouest)
                if (trottoirNS != null) {
                    int w = trottoirNS.getWidth();
                    int h = trottoirNS.getHeight();
                    int y = p.y - h / 2;
                    // Ouest: prolongement vers la gauche depuis l'intersection
                    int xLeft = p.x - halfRoad - w;
                    // Est: prolongement vers la droite depuis l'intersection
                    int xRight = p.x + halfRoad;
                    g.drawImage(trottoirNS, xLeft, y, null);
                    g.drawImage(trottoirNS, xRight, y, null);
                } else {
                    g.fillRect(p.x - CROSSWALK_OFFSET - t / 2, p.y - halfRoad, t, ROAD_WIDTH);
                    g.fillRect(p.x + CROSSWALK_OFFSET - t / 2, p.y - halfRoad, t, ROAD_WIDTH);
                }

                if (inter.aUnFeu()) {
                    FeuSignalisation feu = inter.getFeu();
                    CouleurFeu couleurNordSud = feu.getCouleur();
                    CouleurFeu couleurEstOuest = (couleurNordSud == CouleurFeu.VERT) ? CouleurFeu.ROUGE : CouleurFeu.VERT;
                    int thickness = 4;

                    int nsLength = ROAD_WIDTH / 2;
                    int nsX = p.x - nsLength / 2;
                    int northY = p.y - CROSSWALK_OFFSET + thickness;
                    int southY = p.y + CROSSWALK_OFFSET - thickness;
                    Route inNord = inter.getRouteEntrante(Direction.NORD);
                    Route inSud = inter.getRouteEntrante(Direction.SUD);
                    g.setColor(couleurNordSud == CouleurFeu.VERT ? Color.GREEN : Color.RED);
                    if (inNord != null) {
                        g.fillRect(nsX, southY - thickness / 2, nsLength, thickness);
                    }
                    if (inSud != null) {
                        g.fillRect(nsX, northY - thickness / 2, nsLength, thickness);
                    }

                    int ewLength = ROAD_WIDTH / 2;
                    int ewY = p.y - ewLength / 2;
                    int westX = p.x - CROSSWALK_OFFSET + thickness;
                    int eastX = p.x + CROSSWALK_OFFSET - thickness;
                    Route inOuest = inter.getRouteEntrante(Direction.OUEST);
                    Route inEst = inter.getRouteEntrante(Direction.EST);
                    g.setColor(couleurEstOuest == CouleurFeu.VERT ? Color.GREEN : Color.RED);
                    if (inOuest != null) {
                        g.fillRect(eastX - thickness / 2, ewY, thickness, ewLength);
                    }
                    if (inEst != null) {
                        g.fillRect(westX - thickness / 2, ewY, thickness, ewLength);
                    }
                }
            }
        }
    }
    
    private BufferedImage loadAndScale(String path, int maxSize) {
        try {
            BufferedImage raw = ImageIO.read(new File(path));
            if (raw == null) return null;
            BufferedImage trimmed = trimTransparent(raw);
            return scaleToMax(trimmed, maxSize);
        } catch (IOException e) {
            return null;
        }
    }

    private BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            return null;
        }
    }

    private BufferedImage rotate90(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage rotated = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rotated.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.translate(h, 0);
        g2.rotate(Math.PI / 2);
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return rotated;
    }

    private BufferedImage scaleToFit(BufferedImage img, int targetWidth, int targetHeight) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (w <= 0 || h <= 0) return img;
        double scale = Math.min((double) targetWidth / w, (double) targetHeight / h);
        int newW = Math.max(1, (int) Math.round(w * scale));
        int newH = Math.max(1, (int) Math.round(h * scale));
        BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(img, 0, 0, newW, newH, null);
        g2.dispose();
        return scaled;
    }

    private BufferedImage trimTransparent(BufferedImage img) {
        if (!img.getColorModel().hasAlpha()) return img;
        int w = img.getWidth();
        int h = img.getHeight();
        int minX = w;
        int minY = h;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = img.getRGB(x, y);
                int a = (argb >>> 24) & 0xFF;
                if (a > 10) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < minX || maxY < minY) return img;
        return img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private BufferedImage extractCentralArrow(BufferedImage img) {
        return img;
    }


    private BufferedImage scaleToMax(BufferedImage img, int maxSize) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (w <= 0 || h <= 0) return img;
        double scale = (double) maxSize / Math.max(w, h);
        int newW = Math.max(1, (int) Math.round(w * scale));
        int newH = Math.max(1, (int) Math.round(h * scale));
        BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(img, 0, 0, newW, newH, null);
        g2.dispose();
        return scaled;
    }

    private void drawVehicules(Graphics2D g) {
        for (Route route : carte.getRoutes()) {
            for (Vehicule v : route.getVehicules()) {
                Point p = getVehiculePosition(v);
                if (p != null) {
                    Direction dir = route.getDirection();
                    if (v.getType() == TypeVehicule.BUS) {
                        int offset = 8;
                        if (dir == Direction.NORD) {
                            p.y += offset;
                        } else if (dir == Direction.SUD) {
                            p.y -= offset;
                        } else if (dir == Direction.EST) {
                            p.x -= offset;
                        } else if (dir == Direction.OUEST) {
                            p.x += offset;
                        }
                    }
                    double angle = 0.0;
                    if (dir == Direction.NORD) {
                        angle = -Math.PI / 2;
                    } else if (dir == Direction.SUD) {
                        angle = Math.PI / 2;
                    } else if (dir == Direction.OUEST) {
                        angle = Math.PI;
                    }
                    AffineTransform old = g.getTransform();
                    g.rotate(angle, p.x, p.y);
                    if (v.getType() == TypeVehicule.BUS) {
                        if (busImage != null) {
                            int w = busImage.getWidth();
                            int h = busImage.getHeight();
                            int x = p.x - w / 2;
                            int y = p.y - h / 2;
                            g.drawImage(busImage, x, y, w, h, null);
                        } else {
                            g.setColor(Color.ORANGE);
                            g.fillRect(p.x - 6, p.y - 6, 12, 12);
                        }
                    } else {
                        if (vehiculeImage != null) {
                            int w = vehiculeImage.getWidth();
                            int h = vehiculeImage.getHeight();
                            int x = p.x - w / 2;
                            int y = p.y - h / 2;
                            g.drawImage(vehiculeImage, x, y, w, h, null);
                        } else {
                            g.setColor(Color.BLUE);
                            int[] xs = {p.x, p.x - 6, p.x + 6};
                            int[] ys = {p.y - 7, p.y + 5, p.y + 5};
                            g.fillPolygon(xs, ys, 3);
                        }
                    }
                    g.setTransform(old);
                }
            }
        }
    }

    private Point getIntersectionCenter(Intersection inter) {
        int cols = carte.getCols();
        int rows = carte.getRows();
        int width = getWidth() > 0 ? getWidth() : getPreferredSize().width;
        int height = getHeight() > 0 ? getHeight() : getPreferredSize().height;
        int usableWidth = Math.max(1, width - 2 * MARGIN);
        int usableHeight = Math.max(1, height - 2 * MARGIN);
        int col = inter.getCol();
        int row = inter.getRow();
        int x = MARGIN;
        int y = MARGIN;
        if (cols > 1) {
            x += (int) Math.round((double) col * usableWidth / (cols - 1));
        }
        if (rows > 1) {
            y += (int) Math.round((double) row * usableHeight / (rows - 1));
        }
        return new Point(x, y);
    }
    
    private Point getVehiculePosition(Vehicule v) {
        Route r = v.getRouteActuelle();
        if (r == null) return null;

        Point p1 = getIntersectionCenter(r.getDepart());
        Point p2 = getIntersectionCenter(r.getArrivee());
        Direction dir = r.getDirection();
        
        if (p1.distance(p2) > CELL_SIZE * 1.5) {
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

    private static class Decoration {
        final int row;
        final int col;
        final BufferedImage image;

        Decoration(int row, int col, BufferedImage image) {
            this.row = row;
            this.col = col;
            this.image = image;
        }
    }
}
