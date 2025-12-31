public class Intersection {
    private int x; // coordonnées de l'intersection
    private int y;
    private FeuSignalisation feu;

    public Intersection(int x, int y) {
        this.x = x;
        this.y = y;
        this.feu = new FeuSignalisation();
    }

    public void update() {
        feu.update();
    }

    public FeuSignalisation getFeu() {
        return feu;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}
