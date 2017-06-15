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
    /** generate a path coomand fron an old command whose center point is shifted*/
    public svgPathCommands(svgPathCommands oldCommand, Point originalStart, Point finalStart) {
        double shiftX = finalStart.getX() - originalStart.getX();
        double shiftY = finalStart.getY() - originalStart.getY();
        this.commandType = oldCommand.getCommandType();
        this.destinationPoint = new Point(oldCommand.getDestinationPoint().getX() + shiftX, oldCommand.getDestinationPoint().getY() + shiftY);
        this.controlPoint1 = new Point(oldCommand.getControlPoint1().getX() + shiftX, oldCommand.getControlPoint1().getY() + shiftY);
        this.controlPoint2 = new Point(oldCommand.getControlPoint2().getX() + shiftX, oldCommand.getControlPoint2().getY() + shiftY);

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
