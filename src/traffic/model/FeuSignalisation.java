package traffic.model;

public class FeuSignalisation {
    private EtatFeu etat;
    private int tempsRestant; // Temps restant dans l'état actuel (en ticks ou secondes)
    private final int DUREE_VERT = 30;
    private final int DUREE_ROUGE = 30;

    public FeuSignalisation() {
        this.etat = EtatFeu.ROUGE; // Commence par rouge par défaut
        this.tempsRestant = DUREE_ROUGE;
    }

    public EtatFeu getEtat() {
        return etat;
    }

    public void setEtat(EtatFeu etat) {
        this.etat = etat;
        // Réinitialiser le temps si on change manuellement (optionnel)
        if (etat == EtatFeu.VERT) {
            tempsRestant = DUREE_VERT;
        } else {
            tempsRestant = DUREE_ROUGE;
        }
    }

    /**
     * Met à jour le feu de signalisation (appelé à chaque tick de simulation)
     * @return true si l'état a changé
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
        if (this.etat == EtatFeu.VERT) {
            this.etat = EtatFeu.ROUGE;
            this.tempsRestant = DUREE_ROUGE;
        } else {
            this.etat = EtatFeu.VERT;
            this.tempsRestant = DUREE_VERT;
        }
    }
}
