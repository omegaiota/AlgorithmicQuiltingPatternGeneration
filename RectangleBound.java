package jackiequiltpatterndeterminaiton;

/**
 * Created by JacquelineLi on 10/29/17.
 */
public class RectangleBound {
    private final Point center;
    private double width;
    private double height;

    public RectangleBound(Point center, double width, double height) {
        this.width = width;
        this.height = height;
        this.center = center;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHalfWidth() {
        return width * 0.5;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getHalfHeight() {
        return height * 0.5;
    }

    public Point getCenter() {
        return center;
    }

    public double getLeft() {
        return center.x - width * 0.5;
    }

    public double getRight() {
        return center.x + width * 0.5;
    }

    public double getUp() {
        return center.y - height * 0.5;
    }

    public double getDown() {
        return center.y + height * 0.5;
    }

    public Point getUpperLeft() {
        return new Point(getLeft(), getUp());
    }

    public Point getUpperRight() {
        return new Point(getRight(), getUp());
    }

    public Point getLowerLeft() {
        return new Point(getLeft(), getDown());
    }

    public Point getLowerRight() {
        return new Point(getRight(), getDown());
    }

    public boolean touches(RectangleBound other) {
        // check if this's right border is inside
        if (isInsideBox(other.getCenter())) {
            System.out.println("other.center in inside");
            return true;
        }
        if (isInsideBox(other.getUpperLeft())) {
            System.out.println("other.getUpperLeft in inside");
            return true;
        }
        if (isInsideBox(other.getUpperRight())) {
            System.out.println("other.getUpperRight in inside");
            return true;
        }
        if (isInsideBox(other.getLowerLeft())) {
            System.out.println("other.getLowerLeft in inside");
            return true;
        }
        if (other.isInsideBox(getUpperLeft())) {
            System.out.println("my getUpperLeft() inside other ");
            return true;
        }
        if (other.isInsideBox(getUpperRight())) {
            System.out.println("my getUpperRight() inside other ");
            return true;
        }
        if (other.isInsideBox(getLowerLeft())) {
            System.out.println("my getLowerLeft() inside other ");
            return true;
        }
        if (other.isInsideBox(getLowerRight())) {
            System.out.println("my getLowerRight() inside other ");
            return true;
        }
        if (other.isInsideBox(getCenter())) {
            System.out.println("my getCenter() inside other ");
            return true;
        }
        return false;
    }

    private boolean isInsideBox(Point testPoint) {
        return isBetween(testPoint.x, getLeft(), getRight()) && isBetween(testPoint.y, getUp(), getDown());

    }

    private boolean isBetween(double testNum, double boundA, double boundB) {
        if (boundA < boundB) {
            return (testNum >= boundA) && (testNum <= boundB);
        } else {
            return (testNum >= boundB) && (testNum <= boundA);
        }
    }

    // return the tightest bound for an argument bound using this bound as a constraint
    public void modifyToTightestBound(RectangleBound initialBound) {
        assert (!isInsideBox(initialBound.getCenter()));        // a determined bounding box should never contain any other tree nodes
        Point testCenter = initialBound.getCenter();
        int count = 0;
        while (touches(initialBound)) {
            count++;
            if (count > 100) {
                System.out.println("here");
            }
            double tentativeWidth = (Math.abs(center.x - initialBound.getCenter().x) - width * 0.5) * 2 - 0.01,
                    tentativeHeight = (Math.abs(center.y - initialBound.getCenter().y) - height * 0.5) * 2 - 0.01;
            if (tentativeWidth < 0 && tentativeHeight < 0) {
                assert false;
            }
            tentativeHeight = Math.abs(tentativeHeight);
            tentativeWidth = Math.abs(tentativeWidth);

            if ((testCenter.y < getUp() || testCenter.y > getDown()) && isBetween(testCenter.x, getLeft(), getRight())) {
                System.out.println("A   ");
                initialBound.setHeight(tentativeHeight);
            } else if ((testCenter.x > getRight() || testCenter.x < getLeft()) && isBetween(testCenter.y, getUp(), getDown())) {
                initialBound.setWidth(tentativeWidth);
                System.out.println("   B");
            } else {

                if (tentativeWidth * initialBound.getHeight() > tentativeHeight * initialBound.getWidth()) {
                    System.out.println("          C1");
                    initialBound.setWidth(tentativeWidth);
                } else {
                    initialBound.setHeight(tentativeHeight);
                    System.out.println("             C2");
//
                }
            }


        }


    }


}

