package traffic.model;

public class EtatRouge implements EtatFeu {
    @Override // vérifier par le compilateur
    public CouleurFeu getCouleur() {
        return CouleurFeu.ROUGE;
    }

    @Override
    public int getDuree() {
        return 30; // 30 ticks/secondes
    }

    @Override
    public EtatFeu suivant() {
        return new EtatVert();
    }
}
