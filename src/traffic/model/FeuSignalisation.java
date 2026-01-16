package traffic.model;

public class FeuSignalisation {
    private EtatFeu etat; // Etat courant du feu (implémentation de EtatFeu)
    private int tempsRestant;

    public FeuSignalisation() {
        this.etat = new EtatRouge(); // Etat initial rouge
        this.tempsRestant = etat.getDuree();
    }

     public FeuSignalisation(EtatFeu etatInitial, int tempsRestantInitial) {
         this.etat = etatInitial;
         this.tempsRestant = tempsRestantInitial;
     }
    
    public CouleurFeu getCouleur() {
        return etat.getCouleur();
    }

    // Met à jour le feu (appelé à chaque tick). Retourne true si l'état change.
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
