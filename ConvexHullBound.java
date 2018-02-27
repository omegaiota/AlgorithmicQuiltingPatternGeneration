package jackiequiltpatterndeterminaiton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Created by JacquelineLi on 2/12/18.
 */
public class ConvexHullBound {

    private List<Point> boundary;
    private Region region;

    public ConvexHullBound(List<Point> points) {
        this.boundary = new ArrayList<>(points);
        this.region = new Region(boundary);
    }

    private static int orientation(Point p, Point q, Point r) {
        double val = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y);

        if (val == 0) return 0;  // colinear
        return (val > 0) ? 1 : 2; // clock or counterclock wise
    }

    public static ConvexHullBound fromCommands(List<SvgPathCommand> commands) {
        List<Point> points = new ArrayList<>();
        for (int i = 1; i < commands.size(); i++) {
            if (commands.get(i).getCommandType() == SvgPathCommand.CommandType.CURVE_TO) {
                Point start = commands.get(i - 1).getDestinationPoint(),
                        c1 = commands.get(i).getControlPoint1(),
                        c2 = commands.get(i).getControlPoint2(),
                        end = commands.get(i).getDestinationPoint();
                points.add(start);
                points.add(Spline.evaluate(start, c1, c2, end, 0.2));
                points.add(Spline.evaluate(start, c1, c2, end, 0.4));
                points.add(Spline.evaluate(start, c1, c2, end, 0.6));
                points.add(Spline.evaluate(start, c1, c2, end, 0.8));
                points.add(end);
            } else
                points.add(commands.get(i).getDestinationPoint());

        }

        return ConvexHullBound.valueOf(points);
    }


    // referenced from
    // https://www.geeksforgeeks.org/convex-hull-set-1-jarviss-algorithm-or-wrapping/

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

        // Start from leftmost point, keep moving counterclockwise
        // until reach the start point again.  This loop runs O(h)
        // times where h is number of points in result or output.
        int p = l, q;
        do {
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

        } while (p != l);  // While we don't come to first point

        return new ConvexHullBound(hull);

    }

    // also referenced from the web, this code looks weird so might want to refactor
    //

    public Region getRegion() {
        return region;
    }

    public boolean collidesWith(ConvexHullBound other) {
        for (Point p : other.boundary) {
            if (region.insideRegion(p))
                return true;
        }

        for (Point p : boundary) {
            if (other.region.insideRegion(p))
                return true;
        }

        return false;
    }

    public boolean collidesWith(List<ConvexHullBound> bounds) {
        for (ConvexHullBound b : bounds) {
            if (this.collidesWith(b))
                return true;
        }
        return false;
    }

    public List<Point> getBoundary() {
        return boundary;
    }

}
