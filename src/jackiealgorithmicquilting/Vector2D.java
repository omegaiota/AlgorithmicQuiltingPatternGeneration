package src.jackiealgorithmicquilting;

/**
 * Created by JacquelineLi on 10/28/18.
 */
public class Vector2D {
    public final double x;
    public final double y;

    Vector2D() {
        this.x = 0;
        this.y = 0;
    }

    Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    Vector2D(Point A, Point B) {
        x = B.minus(A).x;
        y = B.minus(A).y;
    }

    /**
     * @param A
     * @param B
     * @return the signed angle between the two vector in radius. value is from 0 to pi
     */
    static double getAngle(Vector2D A, Vector2D B) {
        double angle = Math.atan2(B.y, B.x) - Math.atan2(A.y, A.x);
        if (angle < 0)
            angle += Math.PI * 2;
        return angle;
//        return Math.acos(A.dotProduct(B) / (A.getLength() * B.getLength()));
    }

    Vector2D add(Vector2D B) {
        return new Vector2D(x + B.x, y + B.y);
    }

    Vector2D negative() {
        return new Vector2D(-x, -y);
    }

    double getLength() {
        return Math.sqrt(x * x + y * y);
    }

    Vector2D unit() {
        return this.scale(1.0 / getLength());
    }

    Vector2D minus(Vector2D B) {
        return add(B.negative());
    }

    Vector2D scale(double k) {
        return new Vector2D(x * k, y * k);
    }

    double dotProduct(Vector2D B) {
        return (B.x * x + B.y * y);
    }

    double getAngle() {
        return Math.atan2(y, x);
    }

    double getAngleV2() {
        Vector2D vector1 = new Vector2D(1.0, 0.0);
        Vector2D vector2 = this;

//        return Math.atan2(y, x);
        return Math.atan2(vector2.y, vector2.x) - Math.atan2(vector1.y, vector1.x);
    }

}
