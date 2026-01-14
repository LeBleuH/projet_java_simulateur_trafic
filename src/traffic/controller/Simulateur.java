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
import java.util.Random;
import java.util.Set;

public class Simulateur {
    private Carte carte;
    private MainFrame mainFrame;
    private Timer timer;
    private int delay = 100;
    private Random random = new Random();
    private int nombreVehicules = 8;
    private static final double ZONE_ARRET = 12.0;
    private static final double DISTANCE_MIN = 10.0;

    public Simulateur() {
        carte = new Carte(5, 5);
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

    private void genererVehicules(int count) {
        List<Route> routes = carte.getRoutes();
        for (int i = 0; i < count; i++) {
            if (routes.isEmpty()) break;
            Route route = routes.get(random.nextInt(routes.size()));

            boolean isVoiture = random.nextBoolean();
            double vitesse = isVoiture ? 2.0 : 1.0;
            Color couleur = isVoiture ? Color.BLUE : Color.ORANGE;
            TypeVehicule type = isVoiture ? TypeVehicule.VOITURE : TypeVehicule.BUS;
            Vehicule v = new Vehicule(vitesse, couleur, type);
            route.ajouterVehicule(v);
            double departOffset = -50.0;
            v.setPosition(departOffset);
        }
    }

    private void tick() {
        for (int i = 0; i < carte.getRows(); i++) {
            for (int j = 0; j < carte.getCols(); j++) {
                Intersection inter = carte.getIntersection(i, j);
                if (inter.aUnFeu()) {
                    inter.getFeu().mettreAJour();
                }
            }
        }

        Map<Route, List<Vehicule>> vehiculesParRoute = new HashMap<>();
        Map<Intersection, Vehicule> vehiculePrioritaire = new HashMap<>();
        Map<Intersection, Double> distancePrioritaire = new HashMap<>();
        Map<Intersection, Integer> prioriteDirection = new HashMap<>();

        for (Route route : carte.getRoutes()) {
            List<Vehicule> vehicules = new ArrayList<>(route.getVehicules());
            vehicules.sort(Comparator.comparingDouble(Vehicule::getPosition).reversed());
            vehiculesParRoute.put(route, vehicules);

            if (vehicules.isEmpty()) {
                continue;
            }

            Vehicule leader = vehicules.get(0);
            if (leader.getRouteActuelle() == null) {
                continue;
            }
            double longueur = route.getLongueur();
            double positionLeader = leader.getPosition();
            double distanceRestanteLeader = longueur - positionLeader;
            if (distanceRestanteLeader >= 0 && distanceRestanteLeader <= ZONE_ARRET) {
                Intersection arrivee = route.getArrivee();
                Double meilleure = distancePrioritaire.get(arrivee);
                Integer prioriteExistante = prioriteDirection.get(arrivee);
                int prioriteNouvelle = getDirectionPriority(route.getDirection());
                boolean choisirNouveau = false;
                if (prioriteExistante == null) {
                    choisirNouveau = true;
                } else if (prioriteNouvelle < prioriteExistante) {
                    choisirNouveau = true;
                } else if (prioriteNouvelle == prioriteExistante &&
                           (meilleure == null || distanceRestanteLeader < meilleure)) {
                    choisirNouveau = true;
                }
                if (choisirNouveau) {
                    distancePrioritaire.put(arrivee, distanceRestanteLeader);
                    vehiculePrioritaire.put(arrivee, leader);
                    prioriteDirection.put(arrivee, prioriteNouvelle);
                }
            }
        }

        Set<Intersection> intersectionsOccupees = new HashSet<>();

        for (Route route : carte.getRoutes()) {
            List<Vehicule> vehicules = vehiculesParRoute.get(route);
            if (vehicules == null || vehicules.isEmpty()) {
                continue;
            }

            Vehicule precedent = null;

            for (Vehicule v : vehicules) {
                if (v.getRouteActuelle() == null) {
                    continue;
                }

                double longueur = route.getLongueur();
                double position = v.getPosition();
                double distanceRestante = longueur - position;

                boolean doitFreiner = false;

                if (position < 0) {
                    Intersection departInter = route.getDepart();
                    boolean feuVertDepart = true;
                    if (departInter.aUnFeu()) {
                        CouleurFeu couleurDepart = departInter.getFeu().getCouleur();
                        feuVertDepart = (couleurDepart == CouleurFeu.VERT);
                    }
                    if (!feuVertDepart && position > -ZONE_ARRET) {
                        v.fixerVitesseActuelle(0);
                        v.setPosition(-ZONE_ARRET);
                        precedent = v;
                        continue;
                    }
                }

                Intersection arrivee = route.getArrivee();
                if (distanceRestante <= ZONE_ARRET) {
                    boolean feuVert = true;
                    if (arrivee.aUnFeu()) {
                        CouleurFeu couleurFeu = arrivee.getFeu().getCouleur();
                        feuVert = (couleurFeu == CouleurFeu.VERT);
                    }
                    Vehicule prioritaire = vehiculePrioritaire.get(arrivee);
                    boolean estPrioritaire = prioritaire == null || prioritaire == v;
                    boolean intersectionLibre = !intersectionsOccupees.contains(arrivee);
                    if (!feuVert || !estPrioritaire || !intersectionLibre) {
                        doitFreiner = true;
                        double positionArret = longueur - ZONE_ARRET;
                        if (position > positionArret) {
                            v.setPosition(positionArret);
                            v.fixerVitesseActuelle(0);
                            position = positionArret;
                        }
                    }
                }

                if (!doitFreiner && precedent != null) {
                    double distanceAvecPrecedent = precedent.getPosition() - position;
                    if (distanceAvecPrecedent < DISTANCE_MIN) {
                        doitFreiner = true;
                    }
                }

                v.mettreAJourVitesse(doitFreiner);
                v.avancer();

                position = v.getPosition();

                if (precedent != null) {
                    double positionMax = precedent.getPosition() - DISTANCE_MIN;
                    if (position > positionMax) {
                        v.setPosition(positionMax);
                        v.fixerVitesseActuelle(0);
                        position = positionMax;
                    }
                }

                if (position >= longueur) {
                    boolean peutPasser = true;
                    if (arrivee.aUnFeu()) {
                        CouleurFeu couleurFeu = arrivee.getFeu().getCouleur();
                        peutPasser = (couleurFeu == CouleurFeu.VERT);
                    }
                    Vehicule prioritaire = vehiculePrioritaire.get(arrivee);
                    boolean estPrioritaire = prioritaire == null || prioritaire == v;
                    if (peutPasser && estPrioritaire && !intersectionsOccupees.contains(arrivee)) {
                        intersectionsOccupees.add(arrivee);
                        passerIntersection(v, route);
                        continue;
                    } else {
                        double positionArret = longueur - ZONE_ARRET;
                        v.setPosition(positionArret);
                        v.fixerVitesseActuelle(0);
                        position = positionArret;
                    }
                }

                precedent = v;
            }
        }

        mainFrame.refresh();
    }

    private void passerIntersection(Vehicule v, Route currentRoute) {
        Intersection nextInter = currentRoute.getArrivee();
        java.util.List<Route> candidates = new java.util.ArrayList<>(nextInter.getRoutesSortantes());
        if (candidates.isEmpty()) {
            return;
        }
        Route nextRoute = candidates.get(random.nextInt(candidates.size()));
        if (nextRoute != null) {
            currentRoute.retirerVehicule(v);
            nextRoute.ajouterVehicule(v);

            Intersection depart = nextRoute.getDepart();
            Direction dir = nextRoute.getDirection();
            boolean entreeDepuisBord = false;
            int rows = carte.getRows();
            int cols = carte.getCols();

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
                v.setPosition(-50.0);
            }
        }
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

    private int getDirectionPriority(Direction dir) {
        if (dir == Direction.NORD || dir == Direction.SUD) {
            return 0;
        }
        return 1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Simulateur());
    }
}
