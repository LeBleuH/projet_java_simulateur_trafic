package traffic.model;

import java.awt.Color;

public class Vehicule {
    private Route routeActuelle;
    private double position; // Position sur la route (0 à route.longueur)
    private double vitesse;
    private Color couleur;
    private boolean estArrete;

    public Vehicule(double vitesse, Color couleur) {
        this.vitesse = vitesse;
        this.couleur = couleur;
        this.position = 0;
        this.estArrete = false;
    }

    public void setRouteActuelle(Route route) {
        this.routeActuelle = route;
        this.position = 0; // Réinitialiser la position au début de la nouvelle route
    }

    public Route getRouteActuelle() {
        return routeActuelle;
    }

    public double getPosition() {
        return position;
    }

    public double getVitesse() {
        return vitesse;
    }
    
    public Color getCouleur() {
        return couleur;
    }

    public void avancer() {
        if (!estArrete) {
            position += vitesse;
        }
    }

    public void arreter() {
        estArrete = true;
    }

    public void redemarrer() {
        estArrete = false;
    }
    
    public boolean estArriveAuBout() {
        return routeActuelle != null && position >= routeActuelle.getLongueur();
    }
}
