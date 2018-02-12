package jackiequiltpatterndeterminaiton;

import java.util.List;

/**
 * Created by JacquelineLi on 6/13/17.
 */
public class Point {
    public static final int PRECISION = 4;
    public final double x, y;
    /** generate a point with absolute coordinates*/
    public Point(double x, double y) {
        this.x = truncateDouble(x, PRECISION);
        this.y = truncateDouble(y, PRECISION);
    }

    public Point(Point other) {
        this.x = other.x;
        this.y = other.y;
    }
    public Point(String strWithDelimiter) {
        String[] coordinateStr = strWithDelimiter.split(",");
        assert coordinateStr.length == 2;
        this.x = truncateDouble(Double.parseDouble(coordinateStr[0]), PRECISION);
        this.y = truncateDouble(Double.parseDouble(coordinateStr[1]), PRECISION);
    }

    /** constructs a point with relative coordinates*/
    public Point(Point current, double x, double y) {
        this.x = truncateDouble(x + current.x, PRECISION);
        this.y = truncateDouble(y + current.y, PRECISION);
    }

    /** constructs point (x,y) from string s = "x,y" **/
    public Point(Point current, String strWithDelimiter) {
        String[] coordinateStr = strWithDelimiter.split(",");
        assert coordinateStr.length == 2;

        this.x = truncateDouble(Double.parseDouble(coordinateStr[0]) + current.x, PRECISION);
        this.y = truncateDouble(Double.parseDouble(coordinateStr[1]) + current.y, PRECISION);
    }

    /**
     * generate a NUL point
     */
    public Point() {
        this.x = -12345;
        this.y = -12345;
    }

    /**
     * return a point that's in the circle with radius around input point
     *
     * @param radius
     * @return new point
     */
    public static Point randomNearby(Point origin, double radius) {
        double r = Math.random() * radius;
        double theta = Math.random() * Math.PI;
        return origin.add(new Point(r * Math.cos(theta), 3 * Math.sin(theta)));
    }

    /** truncateDouble: truncates value so that it fits precision
     *  requires: none
     *  ensures: function rounts value to floor into at most precision digits*/
    public static double truncateDouble(double value, int precision) {
        return Math.round(value * Math.pow(10, precision)) / Math.pow(10, precision);
//        return BigDecimal.valueOf(value).setScale(precision, RoundingMode.FLOOR).doubleValue();
    }

    /** given two endpoints of a line, return the retPoint on the line such that dist(retPoint,startPoint) == dist */
    public static Point intermediatePointWithLen(Point start, Point end, double dist) {
//        System.out.println(start.toString() + end.toString() + " :" + dist);
        if (Math.abs(end.x - start.x) < 0.01)
            return new Point(start.x, start.y + dist * (start.y < end.y ?  1 : -1));
        if (dist == 0)
            return start;
        double k = (end.y - start.y) / (end.x - start.x);
        double b = start.y - k * start.x;
        double x = start.x, y = start.y;
        double   A = k * k + 1,
                BB = 2 * (b - y) * k - 2 * x,
                 C = Math.pow(b - y, 2) + x * x - dist * dist;
        int sign = start.x < end.x ? 1 : -1;
        double interX = (-1 * BB + sign * Math.sqrt(BB * BB - 4 * A * C))/ (2 * A);
        double interY = k * interX + b;
//        System.out.println(interX + " " + interY);

        return new Point(truncateDouble(interX, PRECISION), truncateDouble(interY, PRECISION));
    }

    public static Point interMediatePointWithX(Point L1, Point L2, double x) {
        /* find the standard form of line (L1, L2)
            Ax + By + C = 0 */
        double A = 0,B = 0,C = 0, k = 0, b = 0, distanceToLine = 0;
        if (Math.abs(L2.x - L1.x) < 0.01) {
            /*
            A = 1;
            B = 0;
            C = -1 * (L2.getX());
            */
            return new Point(L1);
        } else {
            /* y = kx + b
            * - kx + y - b = 0*/
            k = (L2.y - L1.y) / (L2.x - L1.x);
            b = L1.y - k * L1.x;
            A = -1 * k;
            B = 1;
            C = -1 * b;
        }
        /* y = (-C-Ax) / B*/
        double y = (-1 * (C + A * x)) / B;
        return new Point(x, y);

    }

    public static Point intermediatePointWithProportion(Point start, Point end, double propertion) {
        double dist = Point.getDistance(start, end);
        System.out.println("Dist: " + dist);
        System.out.println("Proportion: " + propertion);
        System.out.println("Len: " + dist * propertion);
        return  intermediatePointWithLen(start, end, dist * propertion);
    }

    /** given two endpoints of a line, return true if the argument point is in the middle of the line */
    public static boolean onLine(Point start, Point end, Point testPoint) {
        double minX = Double.compare(start.x, end.x) < 0 ? start.x : end.x;
        double maxX = start.x + end.x - minX;

        double minY = Double.compare(start.y, end.y) < 0 ? start.y : end.y;
        double maxY = start.y + end.y - minY;

        return (Double.compare(testPoint.x, maxX) <= 0) && (Double.compare(testPoint.x, minX) >= 0) &&
                (Double.compare(testPoint.y, maxY) <= 0) && (Double.compare(testPoint.y, minY) >= 0);

    }

    public static Point midPoint(List<Point> pointList) {
        double thisX = 0.0, thisY = 0.0;
        for (Point p : pointList) {
            thisX += p.x;
            thisY += p.y;
        }

        return new Point(thisX / pointList.size(), thisY / pointList.size());

    }

    /**
     * return the angle between two points in radians
     */
    public static double getAngle(Point start, Point end) {
        double delta_y = end.y - start.y;
        double delta_x = end.x - start.x;
        double angle = Math.atan2(delta_y, delta_x);
        while (angle < 0)
            angle += Math.PI * 2;
        while (angle > Math.PI * 2)
            angle -= Math.PI * 2;

        return angle;
    }

    /** return the distance between two points */
    public static double getDistance(Point start, Point end) {
        double delta_x_sqr = Math.pow(end.x - start.x, 2);
        double delta_y_sqr = Math.pow(end.y - start.y, 2);
        double distance = Math.sqrt(truncateDouble(delta_x_sqr, PRECISION) + truncateDouble(delta_y_sqr, PRECISION));
        return truncateDouble(distance, PRECISION);
    }

    public static Point sumOfPoint(Point finalPoint, Point shiftPoint) {
        return finalPoint.add(shiftPoint);
    }

    public static Point vertOffset(Point dest, Point src, double offset) {
        Point srcRotated = new Point(src);
        if (offset > 0)
            srcRotated = srcRotated.rotateAroundCenter(dest, Math.PI / 2);
        else
            srcRotated = srcRotated.rotateAroundCenter(dest, Math.PI/ 2 * 3);
        return intermediatePointWithLen(dest, srcRotated, Math.abs(offset));
    }

    /** perpendicularFoot: returns point P2 such that PP2 is perpendicular to L1L2
     *  requires: P is not on L1L2
     *  ensures: function returns P2 **/
    public static Point perpendicularFoot(Point P, Point L1, Point L2) {
        /* find the standard form of line (L1, L2)
            Ax + By + C = 0 */
        double A = 0,B = 0,C = 0, k = 0, b = 0, distanceToLine = 0;
        if (Math.abs(L2.x - L1.x) < 0.01) {
            A = 1;
            B = 0;
            C = -1 * (L2.x);
        } else {
            /* y = kx + b
            * - kx + y - b = 0*/
            k = (L2.y - L1.y) / (L2.x - L1.x);
            b = L1.y - k * L1.x;
            A = -1 * k;
            B = 1;
            C = -1 * b;
        }
        /* distance = |ax0+by0+c| / sqrt(a^2+b^2)*/
        /* closest point x=(b(bx0-ay0)-ac)/(a^2+b^2) y=(b(-bx0+ay0)-bc)/(a^2+b^2) */
        double divisor = A * A + B * B;
        double X = (B * (B * P.x - A * P.y) - A * C) / divisor,
                Y = (A * (-1 * B * P.x + A * P.y) - B * C) / divisor;
        return new Point(X, Y);

    }

    /** intersectionPoint: returns the intersection point of two line segments L1:AB and L2:CD using Cramer's rule
     *  requires: AB, CD are not co-linear
     *  ensures: function returns the intersection point **/
    public static Point intersectionPoint(Point A, Point B, Point C, Point D) {
        double x3_x4 = C.x - D.x,
                y3_y4 = C.y - D.y,
                x1_x2 = A.x - B.x,
                y1_y2 = A.y - B.y,
                x1y2_y1x2 = A.x * B.y - A.y * B.x,
                x3y4_y3x4 = C.x * D.y - C.y * D.x;
       double x = (x1y2_y1x2 * x3_x4 - x1_x2 * x3y4_y3x4) / (x1_x2 * y3_y4 - y1_y2 * x3_x4),
               y = (x1y2_y1x2 * y3_y4 - y1_y2 * x3y4_y3x4) / (x1_x2 * y3_y4 - y1_y2 * x3_x4);
       return new Point(x, y);

    }

    /** intersect: determines whether two segments L1:AB, L2:CD intersects.
     *  requires: L1,L2 are not co-linear
     *  ensures: function returns true if L1,L2 intersects, false otherwise **/
    public static boolean intersect(Point A, Point B, Point C, Point D) {
        return ((CCW(A, C, D) != CCW(B, C, D)) && (CCW(A, B, C) != CCW(A, B, D)));
    }

    /** CCW: determines if point A,B,C are lists in counterclockwise order
     *  requires: none
     *  ensures: function returns true if A,B,C are listed in CCW order, false otherwise **/
    private static boolean CCW(Point A, Point B, Point C) {
        return ((C.y - A.y) * (B.x - A.x) > (B.y - A.y) * (C.x - A.x));
    }

    public Point subtract(Point p) {
        return new Point(x - p.x, y - p.y);
    }

    public double distance(Point p) {
        return Math.hypot(x - p.x, y - p.y);
    }

    // Signed area / determinant thing
    public double cross(Point p) {
        return x * p.y - y * p.x;
    }

    public boolean equals(Point compare) {
        return ((Math.abs(x - compare.x) < 0.000001) && (Math.abs(x - compare.x) < 0.000001));
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }

    /**
     * rotate a point with angle radian around an origin
     */
    public Point rotateAroundOrigin(double angle) {
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        return new Point(x * cosA - y * sinA, x * sinA + y * cosA);
    }

    /**
     * rotate a point with a radian angle around a central point
     */
    public Point rotateAroundCenter(Point center, double angle) {
        return this.minus(center).rotateAroundOrigin(angle).add(center);
    }

    /**
     * scale a point around origin
     */
    public Point multiply(Double proportion) {
        return new Point(x * proportion, y * proportion);
    }

    public Point scaleAroundCenter(Point center, Double proportion) {
        return this.minus(center).multiply(proportion).add(center);
    }

    public Point minus(Point shiftPoint) {
        return new Point(x - shiftPoint.x, y - shiftPoint.y);
    }

    public Point add(Point shiftPoint) {
        return new Point(x + shiftPoint.x, y + shiftPoint.y);
    }
    public Point divide(double factor) {
        return new Point(x / factor, y / factor);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;

        Point point = (Point) o;

        if (Double.compare(point.x, x) != 0) return false;
        return Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
