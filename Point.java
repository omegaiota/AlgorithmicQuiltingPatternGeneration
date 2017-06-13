package jackiesvgprocessor;

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

    /** generate a point with absolute coordinates*/
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(String strWithDelimiter) {
        String[] coordinateStr = strWithDelimiter.split(",");
        assert coordinateStr.length == 2;
        this.x = Double.parseDouble(coordinateStr[0]);
        this.y = Double.parseDouble(coordinateStr[1]);
    }

    /** generate a point with relative coordinates*/
    public Point(Point current, double x, double y) {
        this.x = x + current.getX();
        this.y = y + current.getY();
    }

    public Point(Point current, String strWithDelimiter) {
        String[] coordinateStr = strWithDelimiter.split(",");
        assert coordinateStr.length == 2;
        this.x = Double.parseDouble(coordinateStr[0]) + current.getX();
        this.y = Double.parseDouble(coordinateStr[1]) + current.getY();
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
