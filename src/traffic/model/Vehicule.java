package traffic.model;

import java.awt.Color;

public class Vehicule {
    private Route routeActuelle;
    private double position; // Position sur la route (0 à route.longueur)
    private double vitesseCible;
    private double vitesseActuelle;
    private Color couleur;
    private TypeVehicule type;
    private boolean aPasseLigneFeu;

    public Vehicule(double vitesse, Color couleur, TypeVehicule type) {
        this.vitesseCible = vitesse;
        this.vitesseActuelle = 0;
        this.couleur = couleur;
        this.type = type;
        this.position = 0;
        this.aPasseLigneFeu = false;
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
        return vitesseCible;
    }

    public double getVitesseActuelle() {
        return vitesseActuelle;
    }
    
    public Color getCouleur() {
        return couleur;
    }

    public TypeVehicule getType() {
        return type;
    }

    public void setPosition(double position) {
        if (routeActuelle != null) {
            double min = -routeActuelle.getLongueur() * 3.0;
            if (position < min) {
                position = min;
            }
            int max = routeActuelle.getLongueur();
            if (position > max) {
                position = max;
            }
        }
        this.position = position;
    }

    public void avancer() {
        position += vitesseActuelle;
    }

    public void arreter() {
        vitesseActuelle = 0;
    }

    public void redemarrer() {
        // Pas d'état interne à réinitialiser: la vitesse sera fixée par la logique d'appel
    }

    public void mettreAJourVitesse(boolean doitFreiner) {
        if (doitFreiner) {
            vitesseActuelle = 0;
        } else {
            vitesseActuelle = vitesseCible;
        }
    }

    public void fixerVitesseActuelle(double valeur) {
        if (valeur < 0) {
            valeur = 0;
        }
        if (valeur > vitesseCible) {
            valeur = vitesseCible;
        }
        this.vitesseActuelle = valeur;
    }
    
    public boolean aPasseLigneFeu() {
        return aPasseLigneFeu;
    }
    
    public void setPasseLigneFeu(boolean valeur) {
        this.aPasseLigneFeu = valeur;
    }
    
    public boolean estArriveAuBout() {
        return routeActuelle != null && position >= routeActuelle.getLongueur();
    }
}
