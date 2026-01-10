package traffic.controller;

import traffic.model.*;
import traffic.view.MainFrame;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Simulateur {
    private Carte carte;
    private MainFrame mainFrame;
    private Timer timer;
    private int delay = 100; // ms
    private Random random = new Random();

    public Simulateur() {
        // 1. Initialiser la carte (5x5)
        carte = new Carte(5, 5);

        // 2. Générer des véhicules
        genererVehicules(8); // 8 véhicules par exemple

        // 3. Initialiser l'interface
        mainFrame = new MainFrame(carte, this);
        mainFrame.setVisible(true);

        // 4. Timer de simulation
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
            
            // Vitesse aléatoire: voiture (rapide) ou bus (lent)
            double vitesse = (random.nextBoolean()) ? 2.0 : 1.0; 
            Color couleur = (vitesse > 1.5) ? Color.BLUE : Color.ORANGE; // Bleu=Voiture, Orange=Bus
            
            Vehicule v = new Vehicule(vitesse, couleur);
            route.ajouterVehicule(v);
        }
    }

    private void tick() {
        // 1. Mettre à jour les feux
        for (int i = 0; i < carte.getRows(); i++) {
            for (int j = 0; j < carte.getCols(); j++) {
                Intersection inter = carte.getIntersection(i, j);
                if (inter.aUnFeu()) {
                    inter.getFeu().mettreAJour();
                }
            }
        }

        // 2. Déplacer les véhicules
        List<Vehicule> vehiclesToMove = new ArrayList<>();
        for (Route route : carte.getRoutes()) {
            vehiclesToMove.addAll(route.getVehicules());
        }

        for (Vehicule v : vehiclesToMove) {
            Route currentRoute = v.getRouteActuelle();
            if (currentRoute == null) continue;

            // Vérifier si on peut avancer (distance de sécurité, feu, etc.)
            // Simplification: on vérifie juste le feu à la fin de la route
            
            boolean peutAvancer = true;
            
            if (v.estArriveAuBout()) {
                // Arrivé à l'intersection
                Intersection arrivee = currentRoute.getArrivee();
                
                // Gestion du feu
                if (arrivee.aUnFeu()) {
                    EtatFeu etat = arrivee.getFeu().getEtat();
                    Direction dirRoute = currentRoute.getDirection();
                    
                    // Logique: VERT = Axe NORD/SUD passe, ROUGE = Axe EST/OUEST passe (ou l'inverse)
                    // Disons: VERT -> NORD/SUD OK.
                    // Donc si je viens du NORD ou du SUD, il faut VERT.
                    // Si je viens de l'EST ou OUEST, il faut ROUGE (car ROUGE pour N/S = VERT pour E/W).
                    
                    boolean axeVertical = (dirRoute == Direction.NORD || dirRoute == Direction.SUD);
                    
                    if (axeVertical) {
                        if (etat != EtatFeu.VERT) peutAvancer = false;
                    } else {
                        // Axe horizontal
                        if (etat != EtatFeu.ROUGE) peutAvancer = false;
                    }
                }
                
                if (peutAvancer) {
                    passerIntersection(v, currentRoute);
                } else {
                    v.arreter();
                }
            } else {
                v.redemarrer();
                v.avancer();
            }
        }

        // 3. Rafraîchir l'affichage
        mainFrame.refresh();
    }

    private void passerIntersection(Vehicule v, Route currentRoute) {
        Intersection nextInter = currentRoute.getArrivee();
        Direction dir = currentRoute.getDirection();
        
        // Trouver la route suivante (tout droit)
        // Comme c'est une grille simple et one-way, on continue dans la même direction
        // La route sortante de l'intersection 'nextInter' dans la direction 'dir'
        
        Route nextRoute = nextInter.getRouteSortante(dir);
        
        if (nextRoute != null) {
            currentRoute.retirerVehicule(v);
            nextRoute.ajouterVehicule(v);
        } else {
            // Cas impossible dans notre grille parfaite, mais au cas où
            // On pourrait faire tourner le véhicule
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
        // Recréer la simulation
        // Pour simplifier, on vide les routes et on recrée les véhicules
        for (Route r : carte.getRoutes()) {
            r.getVehicules().clear(); // On devrait faire une copie pour éviter ConcurrentModification si on itérait
            // Mais ici on fait juste un clear.
            // Mieux: recréer les listes
        }
        // Hack: recréer les véhicules
        genererVehicules(8);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Simulateur());
    }
}
