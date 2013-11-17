package sheepdog.sim;

public class Point {
    public double x;
    public double y;

    public Point() { x = 0; y = 0; }

    public Point(double xx, double yy) {
        x = xx;
        y = yy;
    }

    public boolean equals(Point o) {
        return o.x == x && o.y == y;
    }
}
