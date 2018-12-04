package jackiealgorithmicquilting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 2/17/18.
 */
public class Spline {
    /**
     * Split a bezier curve to half it's length
     *
     * @param start
     * @param c1
     * @param c2
     * @param end
     * @return
     */
    public static SvgPathCommand splineSplittingAtHalf(Point start, Point c1, Point c2, Point end) {
        double len = 0;
        List<Double> lengths = new ArrayList<>();
        for (double i = 0; i < 0.99; i += 0.1) {
            len += Point.getDistance(evaluate(start, c1, c2, end, i), evaluate(start, c1, c2, end, i + 0.1));
            lengths.add(len);
        }

        for (int i = 0; i < lengths.size(); i++) {
            if (lengths.get(i) > len * 0.5) {
                return splineSplitting(start, c1, c2, end, i * 0.1);
            }
        }
        return splineSplitting(start, c1, c2, end, 0.9);
    }
    /**
     * Break a Bezier Spline at a point using DeCasteljau's algorithm
     *
     * @param start start point of the bezier curve
     * @param t     a value between 0,1 specifying the position to break the curve
     * @param c1
     * @param c2
     * @param end
     * @return
     */
    public static SvgPathCommand splineSplitting(Point start, Point c1, Point c2, Point end, double t) {
        double x1 = start.x, y1 = start.y,
                x2 = c1.x, y2 = c1.y,
                x3 = c2.x, y3 = c2.y,
                x4 = end.x, y4 = end.y;
        double x12 = (x2 - x1) * t + x1,
                y12 = (y2 - y1) * t + y1,

                x23 = (x3 - x2) * t + x2,
                y23 = (y3 - y2) * t + y2,

                x34 = (x4 - x3) * t + x3,
                y34 = (y4 - y3) * t + y3,

                x123 = (x23 - x12) * t + x12,
                y123 = (y23 - y12) * t + y12,

                x234 = (x34 - x23) * t + x23,
                y234 = (y34 - y23) * t + y23,

                x1234 = (x234 - x123) * t + x123,
                y1234 = (y234 - y123) * t + y123;
        return new SvgPathCommand(new Point(x12, y12),
                new Point(x123, y123),
                new Point(x1234, y1234),
                SvgPathCommand.CommandType.CURVE_TO);

    }

    public static double getLen(Point start, Point c1, Point c2, Point end) {
        double len = 0;
        for (double i = 0; i <= 0.99; i += 0.1) {
            len += Point.getDistance(evaluate(start, c1, c2, end, i), evaluate(start, c1, c2, end, i + 0.1));
        }

        return len;
    }

    public static Point evaluate(Point start, Point c1, Point c2, Point end, double t) {
        double k0 = Math.pow(1 - t, 3),
                k1 = 3.0 * Math.pow(1 - t, 2) * t,
                k2 = 3 * (1 - t) * t * t,
                k3 = Math.pow(t, 3);
        return start.multiply(k0).add(c1.multiply(k1)).add(c2.multiply(k2)).add(end.multiply(k3));
    }

    public static boolean collideAt(Point start, Point c1, Point c2, Point end, ConvexHullBound bound, double t) {
        Point A = evaluate(start, c1, c2, end, t), B = (t < 0.98) ? evaluate(start, c1, c2, end, t + 0.01) : end;
        // line line intersection
        if (bound.collidesWith(A, B)) {
            return true;
        }

        return false;

    }
    public static boolean collidesWith(Point start, Point c1, Point c2, Point end, ConvexHullBound bound) {
        // optimization: test bbox collisions first, if boxes don't collide early out
        RectangleBound lineBox = RectangleBound.valueOf(start, end);
        if (!lineBox.collidesWith(bound.getBox()))
            return false;

        for (double i = 0; i < 1.01; i += 0.2) {
            Point A = evaluate(start, c1, c2, end, i), B = (i < 1.00) ? evaluate(start, c1, c2, end, i + 0.1) : end;

            // line line intersection
            if (bound.collidesWith(A, B))
                return true;
        }
        return false;
    }

    private static boolean contains(Point upperLeft, Point lowerRight, Point testPoint) {
        double xLeft = Double.min(upperLeft.x, lowerRight.x), xRight = Double.max(upperLeft.x, lowerRight.x),
                yUp = Double.max(upperLeft.y, lowerRight.y), yDown = Double.min(upperLeft.y, lowerRight.y);
        if (testPoint.x > xRight || testPoint.x < xLeft)
            return false;
        if (testPoint.y < yDown || testPoint.y > yUp)
            return false;
        return true;
    }

    public static double getTangentAngle(Point p0, Point p1, Point p2, Point p3) {
        /* P(t) = (1 - t)^3 * P0 + 3t(1-t)^2 * P1 + 3t^2 (1-t) * P2 + t^3 * P3 */
        return Point.getAngle(Spline.evaluate(p0, p1, p2, p3, 0.97), p3);
    }
}
