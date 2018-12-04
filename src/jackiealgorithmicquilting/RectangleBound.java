package src.jackiealgorithmicquilting;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by JacquelineLi on 10/29/17.
 */
public class RectangleBound {
    private final Point center;
    private double width;
    private double height;


    public RectangleBound(Point center, double width, double height) {
        this.width = width;
        this.height = height;
        this.center = center;
    }

    public RectangleBound(double minx, double miny, double maxx, double maxy) {
        width = maxx - minx;
        height = maxy - miny;
        center = new Point(minx + width * 0.5, miny + height * 0.5);
    }

    public static RectangleBound valueOf(Point start, Point end) {
        return new RectangleBound(start.add(end).multiply(0.5), Math.abs(start.x - end.x), Math.abs(start.y - end.y));
    }

    public static RectangleBound valueOf(List<Point> points) {
        double maxX = points.parallelStream().map(p -> p.x).max(Double::compareTo).get(),
                minX = points.parallelStream().map(p -> p.x).min(Double::compareTo).get(),
                maxY = points.parallelStream().map(p -> p.y).max(Double::compareTo).get(),
                minY = points.parallelStream().map(p -> p.y).min(Double::compareTo).get();

        return new RectangleBound(minX, minY, maxX, maxY);
    }

    static boolean isBetween(double testNum, double boundA, double boundB) {
        if (boundA < boundB) {
            return (testNum > boundA) && (testNum < boundB);
//            return (Double.compare(testNum - boundA, 0.05) > 0 && (Double.compare(boundB - testNum, 0.05) > 0));
        } else {
            return (testNum > boundB) && (testNum < boundA);
//            return (Double.compare(testNum - boundB, 0.05) > 0 && (Double.compare(boundA - testNum, 0.05) > 0));
        }
    }

    public static RectangleBound getBoundingBox(List<SvgPathCommand> commands) {
        int n = commands.size();
        List<Point> points = commands.stream().map(p -> p.getDestinationPoint()).collect(Collectors.toList());
        points.sort(Comparator.comparingDouble(p -> p.x));

        double minx = points.get(0).x, maxx = points.get(n - 1).x;
        points.sort(Comparator.comparingDouble(p -> p.y));
        double miny = points.get(0).y, maxy = points.get(n - 1).y;
        RectangleBound bound = new RectangleBound(minx, miny, maxx, maxy);
        return bound;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHalfWidth() {
        return width * 0.5;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getHalfHeight() {
        return height * 0.5;
    }

    public Point getCenter() {
        return center;
    }

    public double getLeft() {
        return center.x - width * 0.5;
    }

    public double getRight() {
        return center.x + width * 0.5;
    }

    public double getUp() {
        return center.y - height * 0.5;
    }

    public double getDown() {
        return center.y + height * 0.5;
    }

    public Point getUpperLeft() {
        return new Point(getLeft(), getUp());
    }

    public Point getUpperRight() {
        return new Point(getRight(), getUp());
    }

    public Point getLowerLeft() {
        return new Point(getLeft(), getDown());
    }

    public Point getLowerRight() {
        return new Point(getRight(), getDown());
    }

    /**
     * Test if a given rectangle bound collides with any boundary in bounds
     *
     * @param bounds
     * @return
     */
    public boolean collidesWith(Collection<RectangleBound> bounds) {
        for (RectangleBound b : bounds) {
            if (b.collidesWith(this)) {
                return true;
            }
        }
        return false;
    }

    public boolean collidesWith(RectangleBound other) {
        return (Math.abs(center.x - other.center.x) * 2.0 < (width + other.width)) &&
                (Math.abs(center.y - other.center.y) * 2.0 < (height + other.height));
    }

    public boolean isInsideBox(Point testPoint) {
        return isBetween(testPoint.x, getLeft(), getRight()) && isBetween(testPoint.y, getUp(), getDown());

    }

    // return the tightest bound for an argument bound using this bound as a constraint
    public void modifyToTightestBound(RectangleBound initialBound) {
        Point testCenter = initialBound.getCenter();
        if (collidesWith(initialBound)) {
            double tentativeWidth = (Math.abs(center.x - initialBound.getCenter().x) - width * 0.5) * 2 - 0.0000001,
                    tentativeHeight = (Math.abs(center.y - initialBound.getCenter().y) - height * 0.5) * 2 - 0.0000001;
            tentativeHeight = Math.abs(tentativeHeight);
            tentativeWidth = Math.abs(tentativeWidth);
            if ((testCenter.y < getUp() || testCenter.y > getDown()) && isBetween(testCenter.x, getLeft(), getRight())) {
                initialBound.setHeight(tentativeHeight);
            } else if ((testCenter.x > getRight() || testCenter.x < getLeft()) && isBetween(testCenter.y, getUp(), getDown())) {
                initialBound.setWidth(tentativeWidth);
            } else {
                if (tentativeWidth * initialBound.getHeight() > tentativeHeight * initialBound.getWidth()) {
                    initialBound.setWidth(tentativeWidth);
                } else {
                    initialBound.setHeight(tentativeHeight);
                }
            }

            if (tentativeWidth < 0.01 || tentativeHeight < 0.01)
                return;
        }

//        if (collidesWith(initialBound)) {
//            double tentativeWidth = 0.1, tentativeHeight = 0.1;
////            double distX = Math.abs(center.x - initialBound.getCenter().x) - width * 0.5, distY = Math.abs(center.y - initialBound.getCenter().y) - height *0.5;
//            double distX = Math.abs(center.x - initialBound.getCenter().x) - width * 0.5 - tentativeWidth * 0.5, distY = Math.abs(center.y - initialBound.getCenter().y) - height *0.5 - tentativeHeight * 0.5;
////            while ((tentativeWidth * 2) / 2 < distX) {
////                tentativeWidth *= 2;
////            }
////            while (tentativeHeight * 2 / 2 < distY) {
////                tentativeHeight *= 2;
////            }
//
//            while (distX > 0.05) {
//                tentativeWidth += distX * 0.25;
//                distX = Math.abs(center.x - initialBound.getCenter().x) - width * 0.5 - tentativeWidth * 0.5;
//            }
//
//            while (distY > 0.05) {
//                tentativeHeight += distY * 0.25;
//                distY = Math.abs(center.y - initialBound.getCenter().y) - height * 0.5 - tentativeHeight * 0.5;
//            }
//
//
//            initialBound.setWidth(tentativeWidth);
//            initialBound.setHeight(tentativeHeight);
//
//    }
    }


    @Override
    public String toString() {
        return "RectangleBound{" +
                "center=" + center +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}

