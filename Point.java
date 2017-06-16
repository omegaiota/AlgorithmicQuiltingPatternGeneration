package jackiesvgprocessor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by JacquelineLi on 6/13/17.
 */
public class Point {
    double x,y;

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
        double truncDist = BigDecimal.valueOf(distance)
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        return truncDist;
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

    public static void minusPoint(Point finalPoint, Point shiftPoint) {
        finalPoint.setX(finalPoint.getX() - shiftPoint.getX());
        finalPoint.setY(finalPoint.getY() - shiftPoint.getY());
    }

    public static void addPoint(Point finalPoint, Point shiftPoint) {
        finalPoint.setX(finalPoint.getX() + shiftPoint.getX());
        finalPoint.setY(finalPoint.getY() + shiftPoint.getY());
    }
    /** generate a point with absolute coordinates*/
    public Point(double x, double y) {
        Double truncatedx = BigDecimal.valueOf(x)
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        Double truncatedy = BigDecimal.valueOf(y)
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();

        this.x = truncatedx;
        this.y = truncatedy;
    }

    public Point(Point other) {
        this.x = other.getX();
        this.y = other.getY();
    }
    public Point(String strWithDelimiter) {
        String[] coordinateStr = strWithDelimiter.split(",");
        assert coordinateStr.length == 2;
        Double truncatedx = BigDecimal.valueOf(Double.parseDouble(coordinateStr[0]))
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        Double truncatedy = BigDecimal.valueOf(Double.parseDouble(coordinateStr[1]))
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();

        this.x = truncatedx;
        this.y = truncatedy;
    }

    /** generate a point with relative coordinates*/
    public Point(Point current, double x, double y) {
        Double truncatedx = BigDecimal.valueOf(x + current.getX())
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        Double truncatedy = BigDecimal.valueOf(y + current.getY())
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();

        this.x = truncatedx;
        this.y = truncatedy;
    }

    public Point(Point current, String strWithDelimiter) {
        String[] coordinateStr = strWithDelimiter.split(",");
        assert coordinateStr.length == 2;
        Double truncatedx = BigDecimal.valueOf(Double.parseDouble(coordinateStr[0]) + current.getX())
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();
        Double truncatedy = BigDecimal.valueOf(Double.parseDouble(coordinateStr[1]) + current.getY())
                .setScale(3, RoundingMode.FLOOR)
                .doubleValue();

        this.x = truncatedx;
        this.y = truncatedy;
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
