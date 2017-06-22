package jackiesvgprocessor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by JacquelineLi on 6/13/17.
 */
public class Point {
    private double x,y;

    /** round a double value  */
    public static double truncateDouble(double value, int precision) {
//        System.out.println("calling truncateDouble:\n" + value);
        return BigDecimal.valueOf(value)
                .setScale(precision, RoundingMode.FLOOR).doubleValue();


    }

    /** given two endpoints of a line, return the retPoint on the line such that dist(retPoint,startPoint) == dist */
    public static Point intermediatePointWithLen(Point start, Point end, double dist) {

        if (Math.abs(end.getX() - start.getX()) < 0.000001)
            return new Point(start.getX(), start.getY() + dist * (start.getY() < end.getY() ?  1 : -1));
        if (dist == 0)
            return start;
        double k = (end.getY() - start.getY()) / (end.getX() - start.getX());
        double b = start.getY() - k * start.getX();
        /*(y2-y1)^2 + (x2-x1)^2 = l^2
         *(kx2 + b -y1)^2 + (x2-x1)^2 = l^2
           * (k^2+1)x2^2 + [2(b-y1)k-2x1]x2 + (b - y1)^2 + x1^2 - l^2 = 0*/
        double x = start.getX(), y = start.getY();
        double   A = k * k + 1,
                BB = 2 * (b - y) * k - 2 * x,
                 C = Math.pow(b - y, 2) + x * x - dist * dist;
        int sign = start.getX() < end.getX() ? 1 : -1;
        double interX = (-1 * BB + sign * Math.sqrt(BB * BB - 4 * A * C))/ (2 * A);
        double interY = k * interX + b;
       /* System.out.println();
        System.out.println("startPoint:" + start.toString() + " endPoint:" + end.toString() + " length:" + dist + "distBtw:" + getDistance(start, end));
        System.out.println("A=" + A + " B=" + BB + "C=" + C + " k=" + k + " b=" + b);
        System.out.println("middlePWLen:" +  interX + " " + interY);*/
        return new Point(truncateDouble(interX, 3), truncateDouble(interY, 3));
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

    /** generate a point with relative coordinates*/
    public Point(Point current, double x, double y) {
        this.x = truncateDouble(x + current.getX(), 3);
        this.y = truncateDouble(y + current.getY(), 3);
    }

    public Point(Point current, String strWithDelimiter) {
        String[] coordinateStr = strWithDelimiter.split(",");
        assert coordinateStr.length == 2;

        this.x = truncateDouble(Double.parseDouble(coordinateStr[0]) + current.getX(), 3);
        this.y = truncateDouble(Double.parseDouble(coordinateStr[1]) + current.getY(), 3);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean equals(Point compare) {
        return  ((Math.abs(x - compare.getX()) < 0.000001) && (Math.abs(x - compare.getX()) < 0.000001));
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }



}
