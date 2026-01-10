package traffic.model;

import java.util.HashMap;
import java.util.Map;

public class Intersection {
    private int row;
    private int col;
    private FeuSignalisation feu;
    // Les routes sortant de cette intersection, indexées par direction
    private Map<Direction, Route> routesSortantes;
    // Les routes entrant dans cette intersection, indexées par direction (d'où elles viennent)
    private Map<Direction, Route> routesEntrantes;

    public Intersection(int row, int col) {
        this.row = row;
        this.col = col;
        this.routesSortantes = new HashMap<>();
        this.routesEntrantes = new HashMap<>();
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setFeu(FeuSignalisation feu) {
        this.feu = feu;
    }

    public FeuSignalisation getFeu() {
        return feu;
    }
    
    public boolean aUnFeu() {
        return feu != null;
    }

    public void ajouterRouteSortante(Direction direction, Route route) {
        routesSortantes.put(direction, route);
    }
    
    public void ajouterRouteEntrante(Direction direction, Route route) {
        routesEntrantes.put(direction, route);
    }

    public Route getRouteSortante(Direction direction) {
        return routesSortantes.get(direction);
    }
    
    public Route getRouteEntrante(Direction direction) {
        return routesEntrantes.get(direction);
    }
}
