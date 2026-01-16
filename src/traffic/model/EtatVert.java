package traffic.model;

public class EtatVert implements EtatFeu {
    @Override
    public CouleurFeu getCouleur() {
        return CouleurFeu.VERT;
    }

    @Override
    public int getDuree() {
        return 60;
    }

    @Override
    public EtatFeu suivant() {
        return new EtatRouge();
    }
}
