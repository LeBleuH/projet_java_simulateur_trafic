public class FeuSignalisation {
    private EtatFeu etat;
    private int timer; // en secondes

    public FeuSignalisation() {
        this.etat = EtatFeu.VERT;
        this.timer = 0;
    }

    public void update() {
        timer++; // temps s'écoule à chaque appel

        switch (etat) {
            case VERT:
                if (timer >= 30) {
                    etat = EtatFeu.ORANGE;
                    timer = 0;
                }
                break;
            case ORANGE:
                if (timer >= 5) {
                    etat = EtatFeu.ROUGE;
                    timer = 0;
                }
                break;
            case ROUGE:
                if (timer >= 30) {
                    etat = EtatFeu.VERT;
                    timer = 0;
                }
                break;
        }
    }

    public EtatFeu getEtat() {
        return etat;
    }
}
