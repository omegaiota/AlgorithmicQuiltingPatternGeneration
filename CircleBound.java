package jackiequiltpatterndeterminaiton;

/**
 *
 */
public final class CircleBound {
    private double radii;
    private Point center;

    public CircleBound(double radii, Point center) {
        this.radii = radii;
        this.center = center;
    }

    public double getRadii() {
        return radii;
    }

    public Point getCenter() {
        return center;
    }

    public boolean touches(CircleBound other) {
        return Double.compare(Point.getDistance(center, other.getCenter()), radii + other.getRadii() + 0.05) < 0;
    }


}
