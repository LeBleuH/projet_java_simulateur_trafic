package traffic.model;

import java.util.ArrayList;
import java.util.List;

public class Route {
    private Intersection depart;
    private Intersection arrivee;
    private List<Vehicule> vehicules;
    private int longueur; // Pour la simulation de déplacement
    private Direction direction; // Direction de la route (pour l'affichage)

    public Route(Intersection depart, Intersection arrivee, Direction direction) {
        this.depart = depart;
        this.arrivee = arrivee;
        this.direction = direction;
        this.vehicules = new ArrayList<>();
        this.longueur = 100; // Unité arbitraire de distance entre intersections
    }

    public Intersection getDepart() {
        return depart;
    }

    public Intersection getArrivee() {
        return arrivee;
    }

    public List<Vehicule> getVehicules() {
        return vehicules;
    }
    
    public void ajouterVehicule(Vehicule v) {
        vehicules.add(v);
        v.setRouteActuelle(this);
    }
    
    public void retirerVehicule(Vehicule v) {
        vehicules.remove(v);
    }

    public int getLongueur() {
        return longueur;
    }
    
    public Direction getDirection() {
        return direction;
    }
}
