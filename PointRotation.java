package jackiesvgprocessor;

/**
 * Created by JacquelineLi on 6/22/17.
 */
public class PointRotation {
    private Point point;
    private double angle;

    public PointRotation(Point point, double angle) {
        this.point = point;
        this.angle = angle;
    }

    public Point getPoint() {
        return point;
    }

    public double getAngle() {
        return angle;
    }

    public boolean equals(PointRotation other) {
        return (point.equals(other.getPoint()) && (Math.abs(angle - other.getAngle()) < 0.00001));
    }


}
