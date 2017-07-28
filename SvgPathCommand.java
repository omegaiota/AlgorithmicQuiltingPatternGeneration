package jackiesvgprocessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/13/17.
 */
public class SvgPathCommand {
    private Point destinationPoint, controlPoint1, controlPoint2;
    private CommandType commandType;
    public enum CommandType {
        MOVE_TO, LINE_TO, CURVE_TO, SMOOTH_TO, DEFAULT
    }

    public SvgPathCommand(Point destinationPoint) {
        this.destinationPoint = new Point(destinationPoint);
        this.controlPoint1 = new Point();
        this.controlPoint2 = new Point();
        this.commandType = CommandType.DEFAULT;
    }

    public SvgPathCommand(SvgPathCommand oldCommand) {
        this.destinationPoint = new Point(oldCommand.getDestinationPoint());
        this.controlPoint1 = new Point(oldCommand.getControlPoint1());
        this.controlPoint2 = new Point(oldCommand.getControlPoint2());
        this.commandType = oldCommand.getCommandType();
    }

    public SvgPathCommand(Point destinationPoint, CommandType commandType) {
        this.destinationPoint = new Point(destinationPoint);
        this.controlPoint1 = new Point();
        this.controlPoint2 = new Point();
        this.commandType = commandType;
    }
    /** generate a path command from an old command whose center point is shifted*/
    public static SvgPathCommand commandFromShift(SvgPathCommand oldCommand, Point originalStart, Point finalStart) {
        SvgPathCommand copy = new SvgPathCommand(oldCommand);
        Point.addPoint(copy.getDestinationPoint(), finalStart);
        Point.minusPoint(copy.getDestinationPoint(), originalStart);

        Point.addPoint(copy.getControlPoint1(), finalStart);
        Point.minusPoint(copy.getControlPoint1(), originalStart);

        Point.addPoint(copy.getControlPoint2(), finalStart);
        Point.minusPoint(copy.getControlPoint2(), originalStart);
        return copy;

    }

    public void setLineTo() {
        this.commandType = CommandType.LINE_TO;
    }
    public SvgPathCommand(SvgPathCommand oldCommand, Point originalStart, Point finalStart) {
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
    public SvgPathCommand(SvgPathCommand oldCommand, Point originalStart, Point finalStart, double angle) {
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

    /** generate a path command from an old command who is rotated around the center point for angle*/
    public SvgPathCommand(SvgPathCommand oldCommand, Point center, double angle) {
        this.commandType = oldCommand.getCommandType();
        this.destinationPoint = new Point(oldCommand.getDestinationPoint());
        this.controlPoint1 = new Point(oldCommand.getControlPoint1());
        this.controlPoint2 = new Point(oldCommand.getControlPoint2());
        Point.rotateAroundCenter(destinationPoint, center, angle);
        Point.rotateAroundCenter(controlPoint1, center, angle);
        Point.rotateAroundCenter(controlPoint2, center, angle);
    }

    /** generate a path command from an old command who scales around center for proportion*/
    public SvgPathCommand(SvgPathCommand oldCommand, Point center, double proportion, int type) {
        System.out.println("Generating a command from scaling:" + proportion);
        this.commandType = oldCommand.getCommandType();
        this.destinationPoint = new Point(oldCommand.getDestinationPoint());
        this.controlPoint1 = new Point(oldCommand.getControlPoint1());
        this.controlPoint2 = new Point(oldCommand.getControlPoint2());
        Point.scaleAroundCenter(destinationPoint, center, proportion);
        Point.scaleAroundCenter(controlPoint1, center, proportion);
        Point.scaleAroundCenter(controlPoint2, center, proportion);
    }


    public SvgPathCommand(Point controlPoint1, Point controlPoint2, Point destinationPoint, CommandType commandType) {
        this.destinationPoint = destinationPoint;
        this.controlPoint1 = controlPoint1;
        this.controlPoint2  = controlPoint2;
        this.commandType = commandType;
    }

    public static List<SvgPathCommand> commandsScaling(List<SvgPathCommand> commands, double proportion, Point center) {
        List<SvgPathCommand> returnCommands = new ArrayList<>();
        for (SvgPathCommand command : commands) {
            returnCommands.add(new SvgPathCommand(command, center, proportion, 1));
        }

        return returnCommands;
    }

    public static List<SvgPathCommand> commandsShift(List<SvgPathCommand> commands, Point newStart) {
        List<SvgPathCommand> returnCommands = new ArrayList<>();
        for (SvgPathCommand command : commands) {
            returnCommands.add(new SvgPathCommand(command, commands.get(0).getDestinationPoint(), newStart));
        }

        return returnCommands;
    }


    public boolean isMoveTo() {
        return commandType == commandType.MOVE_TO;
    }

    public boolean isLineTo() {
        return commandType == commandType.LINE_TO;
    }

    public boolean isCurveTo() {
        return commandType ==  commandType.CURVE_TO;
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

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    @Override
    public String toString() {
        if (isCurveTo())
            return "SvgPathCommand{" +
                    "destinationPoint=" + destinationPoint +
                    ", controlPoint1=" + controlPoint1 +
                    ", controlPoint2=" + controlPoint2 +
                    ", commandType=" + commandType +
                    '}';
        else
            return "SvgPathCommand{" +
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
