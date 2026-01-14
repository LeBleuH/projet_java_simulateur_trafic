package traffic.model;

public class FeuSignalisation {
    private EtatFeu etat; // Maintenant c'est une instance de l'interface EtatFeu
    private int tempsRestant;

    public FeuSignalisation() {
        this.etat = new EtatRouge(); // Etat initial
        this.tempsRestant = etat.getDuree();
    }

     public FeuSignalisation(EtatFeu etatInitial, int tempsRestantInitial) {
         this.etat = etatInitial;
         this.tempsRestant = tempsRestantInitial;
     }
    
    public CouleurFeu getCouleur() {
        return etat.getCouleur();
    }

    // pour changer manuellement les etats  
   /* public void setEtat(EtatFeu etat) {
        this.etat = etat;
        this.tempsRestant = etat.getDuree();
    } */

    /*
    Met à jour le feu de signalisation (appelé à chaque tick de simulation)
    return true si l'état a changé
     */
    public boolean mettreAJour() {
        tempsRestant--;
        if (tempsRestant <= 0) {
            changerEtat();
            return true;
        }
        return false;
    }

    private void changerEtat() {
        this.etat = this.etat.suivant();
        this.tempsRestant = this.etat.getDuree();
    }
}
