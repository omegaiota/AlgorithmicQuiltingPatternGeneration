package jackiequiltpatterndeterminaiton;

import java.util.Collection;

/**
 *
 */
public final class CircleBound {
    private static final double MULTIPLICATIVE_EPSILON = 1 + 1e-14;
    private double radii;
    private Point center;
    public CircleBound(double radii, Point center) {
        this.radii = radii;
        this.center = center;
    }

    public double getRadii() {
        return radii;
    }

    public void setRadii(double radii) {
        this.radii = radii;
    }

    public Point getCenter() {
        return center;
    }

    public boolean touches(CircleBound other) {
        return Double.compare(Point.getDistance(center, other.getCenter()), radii + other.getRadii() + 0.05) < 0;
    }

    public boolean contains(Point p) {
        return center.distance(p) <= radii * MULTIPLICATIVE_EPSILON;
    }


    public boolean contains(Collection<Point> ps) {
        for (Point p : ps) {
            if (!contains(p))
                return false;
        }
        return true;
    }

}
