package traffic.controller;

import traffic.model.*;
import traffic.view.MainFrame;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.Queue;
import java.util.LinkedList;

public class Simulateur {
    private Carte carte;
    private MainFrame mainFrame;
    private Timer timer;
    private int delay = 100;
    private Random random = new Random();
    private int nombreVehicules = 8;
    private static final double ZONE_ARRET = 50.0;
    private static final double DISTANCE_MIN = 140.0;
    private static final double LONGUEUR_REFERENCE = 180.0;
    private static final double SPEED_MULTIPLIER = 1.5;
    private long elapsedMillis = 0;
    private double distanceTotaleParcourue = 0;
    private java.util.Set<Intersection> intersectionsOccupees = new java.util.HashSet<>();

    public Simulateur() {
        carte = new Carte(5, 5);
        verifierReseau();
        genererVehicules(nombreVehicules);
        mainFrame = new MainFrame(carte, this);
        mainFrame.setVisible(true);
        timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tick();
            }
        });
    }

    private boolean isEntreeDepuisBord(Route route) {
        Intersection depart = route.getDepart();
        Direction dir = route.getDirection();
        int rows = carte.getRows();
        int cols = carte.getCols();
        if (dir == Direction.EST && depart.getCol() == 0) {
            return true;
        } else if (dir == Direction.OUEST && depart.getCol() == cols - 1) {
            return true;
        } else if (dir == Direction.SUD && depart.getRow() == 0) {
            return true;
        } else if (dir == Direction.NORD && depart.getRow() == rows - 1) {
            return true;
        }
        return false;
    }

    private void genererVehicules(int count) {
        List<Route> routes = new ArrayList<>();
        for (Route r : carte.getRoutes()) {
            if (isEntreeDepuisBord(r)) {
                routes.add(r);
            }
        }
        if (routes.isEmpty()) {
            routes.addAll(carte.getRoutes());
        }
        Collections.shuffle(routes, random);
        for (int i = 0; i < count; i++) {
            if (routes.isEmpty()) break;
            Route route = routes.get(i % routes.size());

            boolean isVoiture;
            if (count >= 2 && i == 0) {
                isVoiture = true;
            } else if (count >= 2 && i == 1) {
                isVoiture = false;
            } else {
                isVoiture = random.nextBoolean();
            }
            double baseVitesse = (isVoiture ? 2.0 : 1.0) * SPEED_MULTIPLIER;
            double facteurLongueur = route.getLongueur() / LONGUEUR_REFERENCE;
            double vitesse = baseVitesse * facteurLongueur;
            Color couleur = isVoiture ? Color.BLUE : Color.ORANGE;
            TypeVehicule type = isVoiture ? TypeVehicule.VOITURE : TypeVehicule.BUS;
            Vehicule v = new Vehicule(vitesse, couleur, type);
            route.ajouterVehicule(v);
            double departOffset = -100.0;
            v.setPosition(departOffset);
        }
    }

    private void tick() {
        elapsedMillis += delay;
        // 1) Mise à jour des feux
        for (int i = 0; i < carte.getRows(); i++) {
            for (int j = 0; j < carte.getCols(); j++) {
                Intersection inter = carte.getIntersection(i, j);
                if (inter.aUnFeu()) {
                    inter.getFeu().mettreAJour();
                }
            }
        }

        // 2) Tri des véhicules par route (du plus avancé au plus proche)
        Map<Route, List<Vehicule>> vehiculesParRoute = new HashMap<>();

        for (Route route : carte.getRoutes()) {
            List<Vehicule> vehicules = new ArrayList<>(route.getVehicules());
            vehicules.sort(Comparator.comparingDouble(Vehicule::getPosition).reversed());
            vehiculesParRoute.put(route, vehicules);
        }

        for (Route route : carte.getRoutes()) {
            List<Vehicule> vehicules = vehiculesParRoute.get(route);
            if (vehicules == null || vehicules.isEmpty()) {
                continue;
            }

            Vehicule precedent = null;
            double longueur = route.getLongueur();
            double zoneArret = getZoneArret(route);
            double distanceMin = getDistanceMin(route);
            double positionArret = longueur - zoneArret;
            Intersection arrivee = route.getArrivee();
            boolean aUnFeuArrivee = arrivee != null && arrivee.aUnFeu();
            boolean intersectionLibre = arrivee == null || !intersectionsOccupees.contains(arrivee);

            for (Vehicule v : vehicules) {
                if (v.getRouteActuelle() != route) {
                    continue;
                }

                double positionDepart = v.getPosition();
                double position = v.getPosition();
                boolean doitFreiner = false;

                // 2a) Entrée depuis le bord: respecter le premier feu (position < 0)
                boolean entreeDepuisBord = isEntreeDepuisBord(route);
                if (entreeDepuisBord && position < 0) {
                    Intersection departInter = route.getDepart();
                    if (departInter != null && departInter.aUnFeu()) {
                        double distanceAvantIntersection = -position;
                        double zoneArretDepart = getZoneArret(route);
                        double positionArretDepart = -zoneArretDepart;
                        if (distanceAvantIntersection <= zoneArretDepart) {
                            boolean feuVertDepart = isFeuVert(departInter, route.getDirection());
                            if (!feuVertDepart) {
                                doitFreiner = true;
                                if (position > positionArretDepart) {
                                    v.setPosition(positionArretDepart);
                                    v.fixerVitesseActuelle(0);
                                    position = v.getPosition();
                                }
                            }
                        }
                    }
                }

                // 2b) Approche du feu d'arrivée: arrêt à la ligne si rouge ou intersection occupée
                if (aUnFeuArrivee && !v.aPasseLigneFeu()) {
                    double distanceRestante = longueur - position;
                    if (distanceRestante <= zoneArret) {
                        boolean feuVertArrivee = isFeuVert(arrivee, route.getDirection());
                        if (!feuVertArrivee || !intersectionLibre) {
                            doitFreiner = true;
                            if (position >= positionArret) {
                                v.setPosition(positionArret);
                                v.fixerVitesseActuelle(0);
                                position = v.getPosition();
                            }
                        } else {
                            if (position >= positionArret) {
                                v.setPasseLigneFeu(true);
                                if (arrivee != null) {
                                    intersectionsOccupees.add(arrivee);
                                    intersectionLibre = false;
                                }
                            }
                        }
                    }
                }

                // 2c) Distance de sécurité avec le véhicule précédent
                if (precedent != null) {
                    double distanceAvecPrecedent = precedent.getPosition() - position;
                    if (distanceAvecPrecedent <= distanceMin) {
                        doitFreiner = true;
                    } else if (distanceAvecPrecedent < 2 * distanceMin) {
                        if (v.getVitesseActuelle() > precedent.getVitesseActuelle()) {
                            v.fixerVitesseActuelle(precedent.getVitesseActuelle());
                        }
                    }
                }

                // 3) Mise à jour vitesse et avancée
                v.mettreAJourVitesse(doitFreiner);
                v.avancer();

                position = v.getPosition();

                // 3a) Re-vérification à la ligne d'arrêt (limite de jitter)
                if (aUnFeuArrivee && !v.aPasseLigneFeu()) {
                    if (position >= positionArret) {
                        boolean feuVertArrivee = isFeuVert(arrivee, route.getDirection());
                        if (!feuVertArrivee || !intersectionLibre) {
                            v.setPosition(positionArret);
                            v.fixerVitesseActuelle(0);
                            position = v.getPosition();
                        } else {
                            v.setPasseLigneFeu(true);
                            if (arrivee != null) {
                                intersectionsOccupees.add(arrivee);
                                intersectionLibre = false;
                            }
                        }
                    }
                }

                // 3b) Position max imposée par la distance de sécurité
                if (precedent != null) {
                    double positionMax = precedent.getPosition() - distanceMin;
                    if (position > positionMax) {
                        v.setPosition(positionMax);
                        v.fixerVitesseActuelle(0);
                        position = positionMax;
                    }
                }

                // 4) Passage d'intersection et libération de l'occupation
                if (position >= longueur) {
                    double nouvellePosition = v.getPosition();
                    double delta = nouvellePosition - positionDepart;
                    if (delta > 0) {
                        distanceTotaleParcourue += delta;
                    }
                    boolean avaitPasseFeu = v.aPasseLigneFeu();
                    boolean passe = passerIntersection(v, route);
                    if (!passe) {
                        v.setPosition(longueur);
                        v.fixerVitesseActuelle(0);
                    }
                    if (avaitPasseFeu && arrivee != null) {
                        intersectionsOccupees.remove(arrivee);
                    }
                    v.setPasseLigneFeu(false);
                    continue;
                }

                double nouvellePosition = v.getPosition();
                double delta = nouvellePosition - positionDepart;
                if (delta > 0) {
                    distanceTotaleParcourue += delta;
                }

                precedent = v;
            }
        }

        mainFrame.refresh();
    }

    private boolean passerIntersection(Vehicule v, Route currentRoute) {
        Intersection nextInter = currentRoute.getArrivee();
        java.util.List<Route> candidates = new java.util.ArrayList<>(nextInter.getRoutesSortantes());
        if (candidates.isEmpty()) {
            return false;
        }
        Route nextRoute = candidates.get(random.nextInt(candidates.size()));
        if (nextRoute != null) {
            double distanceMinNext = getDistanceMin(nextRoute);
            double minPos = Double.POSITIVE_INFINITY;
            for (Vehicule autre : nextRoute.getVehicules()) {
                double pos = autre.getPosition();
                if (pos >= 0 && pos < minPos) {
                    minPos = pos;
                }
            }
            if (minPos < distanceMinNext) {
                return false;
            }
            currentRoute.retirerVehicule(v);
            nextRoute.ajouterVehicule(v);
            Intersection depart = nextRoute.getDepart();
            Direction dir = nextRoute.getDirection();
            int rows = carte.getRows();
            int cols = carte.getCols();
            boolean entreeDepuisBord = false;
            if (dir == Direction.EST && depart.getCol() == 0) {
                entreeDepuisBord = true;
            } else if (dir == Direction.OUEST && depart.getCol() == cols - 1) {
                entreeDepuisBord = true;
            } else if (dir == Direction.SUD && depart.getRow() == 0) {
                entreeDepuisBord = true;
            } else if (dir == Direction.NORD && depart.getRow() == rows - 1) {
                entreeDepuisBord = true;
            }
            if (entreeDepuisBord) {
                v.setPosition(-100.0);
            } else {
                v.setPosition(0.0);
            }
            v.setPasseLigneFeu(false);
            return true;
        }
        return false;
    }

    public void start() {
        timer.start();
    }

    public void pause() {
        timer.stop();
    }
    
    public void reset() {
        pause();
        for (Route r : carte.getRoutes()) {
            r.getVehicules().clear();
        }
        genererVehicules(nombreVehicules);
        elapsedMillis = 0;
        distanceTotaleParcourue = 0;
        mainFrame.refresh();
    }

    public boolean isRunning() {
        return timer.isRunning();
    }
    
    public void setDelay(int delay) {
        this.delay = delay;
        timer.setDelay(delay);
    }
    
    public int getVehicleCount() {
        int count = 0;
        for (Route r : carte.getRoutes()) {
            count += r.getVehicules().size();
        }
        return count;
    }

    public void setNombreVehicules(int nombreVehicules) {
        if (nombreVehicules < 0) {
            nombreVehicules = 0;
        }
        this.nombreVehicules = nombreVehicules;
    }

    public long getElapsedSeconds() {
        return elapsedMillis / 1000;
    }

    public double getDistanceTotaleParcourue() {
        return distanceTotaleParcourue;
    }

    public int getMovingVehicleCount() {
        int count = 0;
        for (Route r : carte.getRoutes()) {
            for (Vehicule v : r.getVehicules()) {
                if (v.getVitesseActuelle() > 0) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getStoppedVehicleCount() {
        int count = 0;
        for (Route r : carte.getRoutes()) {
            for (Vehicule v : r.getVehicules()) {
                if (v.getVitesseActuelle() <= 0) {
                    count++;
                }
            }
        }
        return count;
    }

    // Suppression: priorité direction non utilisée (simplification)

    private double getZoneArret(Route route) {
        return ZONE_ARRET * route.getLongueur() / LONGUEUR_REFERENCE;
    }

    private double getDistanceMin(Route route) {
        return DISTANCE_MIN * route.getLongueur() / LONGUEUR_REFERENCE;
    }

    private boolean isFeuVert(Intersection inter, Direction dir) {
        if (inter == null || !inter.aUnFeu()) {
            return true;
        }
        CouleurFeu couleurNordSud = inter.getFeu().getCouleur();
        boolean nordSudVert = (couleurNordSud == CouleurFeu.VERT);
        if (dir == Direction.NORD || dir == Direction.SUD) {
            return nordSudVert;
        }
        return !nordSudVert;
    }

    private void verifierReseau() {
        int rows = carte.getRows();
        int cols = carte.getCols();
        Set<Intersection> visitesIntersections = new HashSet<>();
        Set<Route> visitesRoutes = new HashSet<>();
        Queue<Intersection> file = new LinkedList<>();
        Intersection depart = carte.getIntersection(0, 0);
        visitesIntersections.add(depart);
        file.add(depart);
        while (!file.isEmpty()) {
            Intersection courant = file.poll();
            for (Route route : courant.getRoutesSortantes()) {
                visitesRoutes.add(route);
                Intersection suivant = route.getArrivee();
                if (!visitesIntersections.contains(suivant)) {
                    visitesIntersections.add(suivant);
                    file.add(suivant);
                }
            }
        }
        boolean toutesIntersections = visitesIntersections.size() == rows * cols;
        boolean toutesRoutes = visitesRoutes.size() == carte.getRoutes().size();
        boolean ok = toutesIntersections && toutesRoutes;
        System.out.println("Verification du reseau routier, connectivite=" + ok);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Simulateur());
    }
}
