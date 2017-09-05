package jackiequiltpatterndeterminaiton;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by JacquelineLi on 6/13/17.
 */
public class Point {
    private double x,y;
    /** generate a point with absolute coordinates*/
    public Point(double x, double y) {
        this.x = truncateDouble(x, 3);
        this.y = truncateDouble(y, 3);
    }

    public Point(Point other) {
        this.x = other.getX();
        this.y = other.getY();
    }
    public Point(String strWithDelimiter) {
        String[] coordinateStr = strWithDelimiter.split(",");
        assert coordinateStr.length == 2;
        this.x = truncateDouble(Double.parseDouble(coordinateStr[0]), 3);
        this.y = truncateDouble(Double.parseDouble(coordinateStr[1]), 3);
    }

    /** constructs a point with relative coordinates*/
    public Point(Point current, double x, double y) {
        this.x = truncateDouble(x + current.getX(), 3);
        this.y = truncateDouble(y + current.getY(), 3);
    }

    /** constructs point (x,y) from string s = "x,y" **/
    public Point(Point current, String strWithDelimiter) {
        String[] coordinateStr = strWithDelimiter.split(",");
        assert coordinateStr.length == 2;

        this.x = truncateDouble(Double.parseDouble(coordinateStr[0]) + current.getX(), 3);
        this.y = truncateDouble(Double.parseDouble(coordinateStr[1]) + current.getY(), 3);
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public void setX(double x) {
        this.x = x;
    }
    public void setY(double y) {
        this.y = y;
    }

    public boolean equals(Point compare) {
        return  ((Math.abs(x - compare.getX()) < 0.000001) && (Math.abs(x - compare.getX()) < 0.000001));
    }

    @Override
    public String toString() {
        return "Point{" + "x=" + x + ", y=" + y + '}';
    }

    /** truncateDouble: truncates value so that it fits precision
     *  requires: none
     *  ensures: function rounts value to floor into at most precision digits*/
    public static double truncateDouble(double value, int precision) {
        return BigDecimal.valueOf(value).setScale(precision, RoundingMode.FLOOR).doubleValue();
    }

    /** given two endpoints of a line, return the retPoint on the line such that dist(retPoint,startPoint) == dist */
    public static Point intermediatePointWithLen(Point start, Point end, double dist) {
        System.out.println(start.toString() + end.toString() + " :" + dist);
        if (Math.abs(end.getX() - start.getX()) < 0.01)
            return new Point(start.getX(), start.getY() + dist * (start.getY() < end.getY() ?  1 : -1));
        if (dist == 0)
            return start;
        double k = (end.getY() - start.getY()) / (end.getX() - start.getX());
        double b = start.getY() - k * start.getX();
        double x = start.getX(), y = start.getY();
        double   A = k * k + 1,
                BB = 2 * (b - y) * k - 2 * x,
                 C = Math.pow(b - y, 2) + x * x - dist * dist;
        int sign = start.getX() < end.getX() ? 1 : -1;
        double interX = (-1 * BB + sign * Math.sqrt(BB * BB - 4 * A * C))/ (2 * A);
        double interY = k * interX + b;
        System.out.println(interX + " " + interY);

        return new Point(truncateDouble(interX, 3), truncateDouble(interY, 3));
    }

    public static Point interMediatePointWithX(Point L1, Point L2, double x) {
        /* find the standard form of line (L1, L2)
            Ax + By + C = 0 */
        double A = 0,B = 0,C = 0, k = 0, b = 0, distanceToLine = 0;
        if (Math.abs(L2.getX() - L1.getX()) < 0.01) {
            /*
            A = 1;
            B = 0;
            C = -1 * (L2.getX());
            */
            return new Point(L1);
        } else {
            /* y = kx + b
            * - kx + y - b = 0*/
            k = (L2.getY() - L1.getY()) / (L2.getX() - L1.getX());
            b = L1.getY() - k * L1.getX();
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
        double minX = start.getX() < end.getX() ? start.getX() : end.getX();
        double maxX = start.getX() + end.getX() - minX;

        double minY = start.getY() < end.getY() ? start.getY() : end.getY();
        double maxY = start.getY() + end.getY() - minY;

        return (Double.compare(testPoint.getX(), maxX) <= 0) && (Double.compare(testPoint.getX(), minX) >= 0) &&
                (Double.compare(testPoint.getY(), maxY) <= 0) && (Double.compare(testPoint.getY(), minY) >= 0);

    }

    /** generate a NUL point */
    public Point() {
        this.x = -12345;
        this.y = -12345;
    }
    /** return the angle between two points */
    public static double getAngle(Point start, Point end) {
        double delta_y = end.getY() - start.getY();
        double delta_x = end.getX() - start.getX();
        double angle = Math.atan2(delta_y, delta_x);
        while (angle < 0)
            angle += Math.PI * 2;
        while (angle > Math.PI * 2)
            angle -= Math.PI * 2;

        return angle;
    }

    /** return the distance between two points */
    public static double getDistance(Point start, Point end) {
        double delta_x_sqr = Math.pow(end.getX() - start.getX(), 2);
        double delta_y_sqr = Math.pow(end.getY() - start.getY(), 2);
        double distance = Math.sqrt(delta_x_sqr + delta_y_sqr);
        return truncateDouble(distance, 3);
    }

    /** rotate a point with angle radian around an origin */
    public static void rotateAroundOrigin(Point point, Double angle) {
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);
        double originalX = point.getX();
        double originalY = point.getY();
        point.setX(originalX * cosA - originalY * sinA);
        point.setY(originalX * sinA + originalY * cosA);
    }

    /** rotate a point with angle radian around a central point */
    public static void rotateAroundCenter(Point point, Point center, Double angle) {
        Point temporary = new Point(point.getX() - center.getX(),point.getY() - center.getY());
        Point.rotateAroundOrigin(temporary, angle);
        point.setX(truncateDouble(temporary.getX() + center.getX(), 3));
        point.setY(truncateDouble(temporary.getY() + center.getY(),3));
    }

    /** scale a point around origin */
    public static void scaleAroundOrigin(Point point, Double proportion) {
        point.setX(truncateDouble(point.getX() * proportion, 3));
        point.setY(truncateDouble(point.getY() * proportion, 3));
    }

    public static void scaleAroundCenter(Point point, Point center, Double proportion) {
        Point temporary = new Point(point.getX() - center.getX(),point.getY() - center.getY());
        Point.scaleAroundOrigin(temporary, proportion);
        point.setX(temporary.getX() + center.getX());
        point.setY(temporary.getY() + center.getY());
    }

    public static void minusPoint(Point finalPoint, Point shiftPoint) {
        finalPoint.setX(finalPoint.getX() - shiftPoint.getX());
        finalPoint.setY(finalPoint.getY() - shiftPoint.getY());
    }

    public static void addPoint(Point finalPoint, Point shiftPoint) {
        finalPoint.setX(finalPoint.getX() + shiftPoint.getX());
        finalPoint.setY(finalPoint.getY() + shiftPoint.getY());
    }

    public static Point pointAdd(Point finalPoint, Point shiftPoint) {

        return new Point(finalPoint.getX() + shiftPoint.getX(), finalPoint.getY() + shiftPoint.getY());
    }

    public static Point vertOffset(Point dest, Point src, double offset) {
        Point srcRotated = new Point(src);
        if (offset > 0)
            rotateAroundCenter(srcRotated, dest, Math.PI / 2);
        else
            rotateAroundCenter(srcRotated, dest, Math.PI/ 2 * 3);
        return intermediatePointWithLen(dest, srcRotated, Math.abs(offset));
    }


    /** perpendicularFoot: returns point P2 such that PP2 is perpendicular to L1L2
     *  requires: P is not on L1L2
     *  ensures: function returns P2 **/
    public static Point perpendicularFoot(Point P, Point L1, Point L2) {
        /* find the standard form of line (L1, L2)
            Ax + By + C = 0 */
        double A = 0,B = 0,C = 0, k = 0, b = 0, distanceToLine = 0;
        if (Math.abs(L2.getX() - L1.getX()) < 0.01) {
            A = 1;
            B = 0;
            C = -1 * (L2.getX());
        } else {
            /* y = kx + b
            * - kx + y - b = 0*/
            k = (L2.getY() - L1.getY()) / (L2.getX() - L1.getX());
            b = L1.getY() - k * L1.getX();
            A = -1 * k;
            B = 1;
            C = -1 * b;
        }
        /* distance = |ax0+by0+c| / sqrt(a^2+b^2)*/
        /* closest point x=(b(bx0-ay0)-ac)/(a^2+b^2) y=(b(-bx0+ay0)-bc)/(a^2+b^2) */
        double divisor = A * A + B * B;
        double X = (B * (B * P.getX() - A * P.getY()) - A * C) / divisor,
                Y = (A * (-1 * B * P.getX() + A * P.getY()) - B * C) / divisor;
        return new Point(X, Y);

    }

    /** intersectionPoint: returns the intersection point of two line segments L1:AB and L2:CD using Cramer's rule
     *  requires: AB, CD are not co-linear
     *  ensures: function returns the intersection point **/
    public static Point intersectionPoint(Point A, Point B, Point C, Point D) {
       double x3_x4 = C.getX() - D.getX(),
               y3_y4 = C.getY() - D.getY(),
               x1_x2 = A.getX() - B.getX(),
               y1_y2 = A.getY() - B.getY(),
               x1y2_y1x2 = A.getX() * B.getY() - A.getY() * B.getX(),
               x3y4_y3x4 = C.getX() * D.getY() - C.getY() * D.getX();
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
        return ((C.getY() - A.getY())*(B.getX() - A.getX()) > (B.getY() - A.getY()) * (C.getX() - A.getX()));
    }
}
