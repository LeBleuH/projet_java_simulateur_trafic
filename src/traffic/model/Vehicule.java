package traffic.model;

import java.awt.Color;

public class Vehicule {
    private Route routeActuelle;
    private double position; // Position sur la route (0 à route.longueur)
    private double vitesseCible;
    private double vitesseActuelle;
    private double acceleration;
    private double deceleration;
    private Color couleur;
    private boolean estArrete;
    private TypeVehicule type;

    public Vehicule(double vitesse, Color couleur, TypeVehicule type) {
        this.vitesseCible = vitesse;
        this.vitesseActuelle = 0;
        this.acceleration = 0.2;
        this.deceleration = 0.3;
        this.couleur = couleur;
        this.type = type;
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
            double min = -50.0;
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
        estArrete = true;
        vitesseActuelle = 0;
    }

    public void redemarrer() {
        estArrete = false;
    }

    public void mettreAJourVitesse(boolean doitFreiner) {
        if (doitFreiner) {
            vitesseActuelle -= deceleration;
            if (vitesseActuelle < 0) {
                vitesseActuelle = 0;
            }
            if (vitesseActuelle == 0) {
                estArrete = true;
            }
        } else {
            estArrete = false;
            vitesseActuelle += acceleration;
            if (vitesseActuelle > vitesseCible) {
                vitesseActuelle = vitesseCible;
            }
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
    
    public boolean estArriveAuBout() {
        return routeActuelle != null && position >= routeActuelle.getLongueur();
    }
}
