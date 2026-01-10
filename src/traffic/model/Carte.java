package traffic.model;

import java.util.ArrayList;
import java.util.List;

public class Carte {
    private int rows;
    private int cols;
    private Intersection[][] intersections;
    private List<Route> routes;

    public Carte(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.intersections = new Intersection[rows][cols];
        this.routes = new ArrayList<>();
        initialiserCarte();
    }

    private void initialiserCarte() {
        // 1. Créer les intersections
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                intersections[i][j] = new Intersection(i, j);
                
                // Ajouter des feux aléatoirement ou à chaque intersection
                // Le sujet dit "Placer 4 à 6 intersections avec feux"
                // Pour l'instant, on en met un peu partout pour tester, ou on fait une méthode séparée
            }
        }

        // Placer quelques feux (exemple: 5 feux aléatoires)
        // Ou pattern fixe pour déterminisme
        intersections[1][1].setFeu(new FeuSignalisation());
        intersections[1][3].setFeu(new FeuSignalisation());
        intersections[3][1].setFeu(new FeuSignalisation());
        intersections[3][3].setFeu(new FeuSignalisation());
        intersections[2][2].setFeu(new FeuSignalisation());

        // 2. Créer les routes (One way alternating)
        // Rows: Pair -> Est, Impair -> Ouest
        // Cols: Pair -> Sud, Impair -> Nord
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Intersection current = intersections[i][j];

                // Connexion Horizontale
                if (i % 2 == 0) { // Est
                    Intersection next = intersections[i][(j + 1) % cols];
                    creerRoute(current, next, Direction.EST);
                } else { // Ouest
                    Intersection next = intersections[i][(j - 1 + cols) % cols];
                    creerRoute(current, next, Direction.OUEST);
                }

                // Connexion Verticale
                if (j % 2 == 0) { // Sud
                    Intersection next = intersections[(i + 1) % rows][j];
                    creerRoute(current, next, Direction.SUD);
                } else { // Nord
                    Intersection next = intersections[(i - 1 + rows) % rows][j];
                    creerRoute(current, next, Direction.NORD);
                }
            }
        }
    }

    private void creerRoute(Intersection depart, Intersection arrivee, Direction dir) {
        Route route = new Route(depart, arrivee, dir);
        depart.ajouterRouteSortante(dir, route);
        // Pour l'arrivée, la route arrive de la direction OPPOSÉE
        Direction dirEntree = getOppositeDirection(dir);
        arrivee.ajouterRouteEntrante(dirEntree, route);
        routes.add(route);
    }
    
    private Direction getOppositeDirection(Direction dir) {
        switch (dir) {
            case NORD: return Direction.SUD;
            case SUD: return Direction.NORD;
            case EST: return Direction.OUEST;
            case OUEST: return Direction.EST;
            default: return Direction.NORD;
        }
    }

    public Intersection getIntersection(int row, int col) {
        return intersections[row][col];
    }

    public List<Route> getRoutes() {
        return routes;
    }
    
    public int getRows() { return rows; }
    public int getCols() { return cols; }
}
