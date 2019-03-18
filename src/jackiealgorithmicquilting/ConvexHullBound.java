package src.jackiealgorithmicquilting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 2/12/18.
 */
public class ConvexHullBound {

    private List<Point> boundary;
    private RectangleBound box;
    private Region region;
    public ConvexHullBound(List<Point> points) {
        this.boundary = new ArrayList<>(points);
        this.region = new Region(boundary);
        this.box = RectangleBound.valueOf(boundary);

    }


    private static int orientation(Point p, Point q, Point r) {
        final int COLINEAR = 0, CW = 1, CCW = 2;
        double val = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y);

        if (val == 0) return COLINEAR;  // colinear
        return (val > 0) ? CW : CCW; // clock or counterclock wise
    }

    public static ConvexHullBound fromCommands(List<SvgPathCommand> commands) {
        List<Point> points = new ArrayList<>();
        if (commands == null || commands.size() == 0)
            return ConvexHullBound.valueOf(new ArrayList<>());

        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).getCommandType() == SvgPathCommand.CommandType.CURVE_TO) {
                points.add(commands.get(i).getControlPoint1());
                points.add(commands.get(i).getControlPoint2());

            }
            points.add(commands.get(i).getDestinationPoint());
        }

        return ConvexHullBound.valueOf(points);
    }

    /**
     * Get a convex hull out of a list of points using the quick hull algorithm
     *
     * @param
     * @return
     */
    public static ConvexHullBound valueOf(List<Point> points) {
        // There must be at least 3 points
        int n = points.size();
        if (n < 3) return new ConvexHullBound(points);

        // Initialize Result
        List<Point> hull = new ArrayList<>();

        // Find the leftmost point
        int l = 0;
        for (int i = 1; i < n; i++)
            if (points.get(i).x < points.get(l).x)
                l = i;
        int maxIteration = n * 10, it = 0;
        // Start from leftmost point, keep moving counterclockwise
        // until reach the start point again.  This loop runs O(h)
        // times where h is number of points in result or output.
        int p = l, q;
        do {
            it++;
            // Add current point to result
            hull.add(points.get(p));

            // Search for a point 'q' such that orientation(p, x,
            // q) is counterclockwise for all points 'x'. The idea
            // is to keep track of last visited most counterclock-
            // wise point in q. If any point 'i' is more counterclock-
            // wise than q, then update q.
            q = (p + 1) % n;
            for (int i = 0; i < n; i++) {
                // If i is more counterclockwise than current q, then
                // update q
                if (orientation(points.get(p), points.get(i), points.get(q)) == 2)
                    q = i;
            }

            // Now q is the most counterclockwise with respect to p
            // Set p as q for next iteration, so that q is added to
            // result 'hull'
            p = q;

        } while (p != l && it < maxIteration);  // While we don't come to first point

        return new ConvexHullBound(hull);

    }


    // referenced from
    // https://www.geeksforgeeks.org/convex-hull-set-1-jarviss-algorithm-or-wrapping/

    public RectangleBound getBox() {
        return box;
    }

    // also referenced from the web, this code looks weird so might want to refactor
    //

    public Region getRegion() {
        return region;
    }

    public boolean collidesWith(ConvexHullBound other) {
        if (other.boundary.size() <= 0)
            return false;
        if (!this.box.collidesWith(other.box))
            return false;

//        if (other.region.insideRegion(box.getCenter()))
//            return true;
//
//        if (region.insideRegion(other.box.getCenter()))
//            return true;

        // Use line line intersection test
        int i, j, p, q;
        for (i = 0, j = boundary.size() - 1; i < boundary.size(); j = i++) {
            for (p = 0, q = other.boundary.size() - 1; p < other.boundary.size(); q = p++) {
                if (Point.intersect(boundary.get(i), boundary.get(j), other.boundary.get(p), other.boundary.get(q)))
                    return true;
            }
        }
        return false;

    }

    public boolean collidesWith(Point A, Point B) {
        // optimization: test with bbox first
        if (!box.isInsideBox(A) && !box.isInsideBox(B))
            return false;

        // test if cross boundary
        int i, j;
        for (i = 0, j = boundary.size() - 1; i < boundary.size(); j = i++) {
            if (Point.intersect(boundary.get(i), boundary.get(j), A, B))
                return true;
        }
        return false;
    }




    public boolean collidesWith(List<ConvexHullBound> bounds) {
        for (ConvexHullBound b : bounds) {
            if (this.collidesWith(b)) {
//                System.out.println("Colliding bounds:" + b + " " + this);
                return true;
            }
        }
        return false;
    }

    public List<Point> getBoundary() {
        return boundary;
    }

}
