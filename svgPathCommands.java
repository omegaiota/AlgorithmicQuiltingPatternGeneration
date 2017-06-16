package jackiesvgprocessor;

/**
 * Created by JacquelineLi on 6/13/17.
 */
public class svgPathCommands {
    private Point destinationPoint, controlPoint1, controlPoint2;
    private int commandType;


    public svgPathCommands(Point destinationPoint) {
        this.destinationPoint = destinationPoint;
        this.controlPoint1 = new Point();
        this.controlPoint2 = new Point();
        this.commandType = -1;
    }

    public svgPathCommands(Point destinationPoint, int commandType) {
        this.destinationPoint = destinationPoint;
        this.controlPoint1 = new Point();
        this.controlPoint2 = new Point();
        this.commandType = commandType;
    }
    /** generate a path command from an old command whose center point is shifted*/
    public svgPathCommands(svgPathCommands oldCommand, Point originalStart, Point finalStart) {
        this.commandType = oldCommand.getCommandType();
        this.destinationPoint = new Point(oldCommand.getDestinationPoint());
        this.controlPoint1 = new Point(oldCommand.getControlPoint1());
        this.controlPoint2 = new Point(oldCommand.getControlPoint2());
        Point.addPoint(destinationPoint, finalStart);
        Point.minusPoint(destinationPoint, originalStart);
        Point.addPoint(controlPoint1, finalStart);
        Point.minusPoint(controlPoint1, originalStart);
        Point.addPoint(controlPoint2, finalStart);
        Point.minusPoint(controlPoint2, originalStart);
    }

    /** generate a path command from an old command whose center point is shifted and rotated*/
    public svgPathCommands(svgPathCommands oldCommand, Point originalStart, Point finalStart, double angle) {
        this.commandType = oldCommand.getCommandType();
        this.destinationPoint = new Point(oldCommand.getDestinationPoint());
        this.controlPoint1 = new Point(oldCommand.getControlPoint1());
        this.controlPoint2 = new Point(oldCommand.getControlPoint2());
        Point.minusPoint(destinationPoint, originalStart);
        Point.minusPoint(controlPoint1, originalStart);
        Point.minusPoint(controlPoint2, originalStart);

        Point.rotateAroundOrigin(destinationPoint, angle);
        Point.rotateAroundOrigin(controlPoint1, angle);
        Point.rotateAroundOrigin(controlPoint2, angle);

        Point.addPoint(destinationPoint, finalStart);
        Point.addPoint(controlPoint1, finalStart);
        Point.addPoint(controlPoint2, finalStart);
    }



    public svgPathCommands(Point controlPoint1, Point controlPoint2, Point destinationPoint, int commandType) {
        this.destinationPoint = destinationPoint;
        this.controlPoint1 = controlPoint1;
        this.controlPoint2  = controlPoint2;
        this.commandType = commandType;
        assert commandType > 1;
    }



    public boolean isMoveTo() {
        return commandType == 0;
    }

    public boolean isLineTo() {
        return commandType == 1;
    }

    public boolean isCurveTo() {
        return commandType == 2;
    }

    public Point getDestinationPoint() {
        return destinationPoint;
    }

    public Point getControlPoint1() {
        return controlPoint1;
    }

    public Point getControlPoint2() {
        return controlPoint2;
    }

    public int getCommandType() {
        return commandType;
    }

    public void setCommandType(int commandType) {
        this.commandType = commandType;
    }

    @Override
    public String toString() {
        if (isCurveTo())
            return "svgPathCommands{" +
                    "destinationPoint=" + destinationPoint +
                    ", controlPoint1=" + controlPoint1 +
                    ", controlPoint2=" + controlPoint2 +
                    ", commandType=" + commandType +
                    '}';
        else
            return "svgPathCommands{" +
                    "destinationPoint=" + destinationPoint +
                    ", commandType=" + commandType +
                    '}';
    }

    public String toSvgCode() {
        if (isMoveTo()) {
            return "M " + destinationPoint.getX() + "," + destinationPoint.getY() + " ";
        } else if (isLineTo()) {
            return "L " + destinationPoint.getX() + "," + destinationPoint.getY() + " ";
        } else if (isCurveTo()) {
            return "C " + controlPoint1.getX() + "," + controlPoint1.getY() + " " + controlPoint2.getX() + "," + controlPoint2.getY() + " " + destinationPoint.getX() + "," + destinationPoint.getY() + " ";
        } else
            return "";
    }
}
