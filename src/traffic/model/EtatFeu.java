package traffic.model;

public interface EtatFeu {
    CouleurFeu getCouleur();
    int getDuree();
    EtatFeu suivant();
}
