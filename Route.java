public class Route {
    private Intersection fin;
    private Direction direction;

    public Route(Intersection debut, Intersection fin, Direction direction) {
        this.fin = fin;
        this.direction = direction;
    }

    public Intersection getFin() {
        return fin;
    }

    public Direction getDirection() {
        return direction;
    }
}
