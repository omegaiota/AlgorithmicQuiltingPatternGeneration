package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/13/17.
 */
public class SvgPathCommand {
    private Point destinationPoint;
    private Point controlPoint1;
    private Point controlPoint2;
    private CommandType commandType;

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
    public SvgPathCommand(SvgPathCommand oldCommand, Point originalStart, Point finalStart) {
        this.commandType = oldCommand.getCommandType();
        destinationPoint = oldCommand.getDestinationPoint().addPoint(finalStart).minusPoint(originalStart);
        controlPoint1 = oldCommand.getControlPoint1().addPoint(finalStart).minusPoint(originalStart);
        controlPoint2 = oldCommand.getControlPoint2().addPoint(finalStart).minusPoint(originalStart);
    }

    /** generate a path command from an old command whose center point is shifted and rotated*/
    public SvgPathCommand(SvgPathCommand oldCommand, Point originalStart, Point finalStart, double angle) {
        this.commandType = oldCommand.getCommandType();
        destinationPoint = oldCommand.getDestinationPoint().minusPoint(originalStart).rotateAroundOrigin(angle).addPoint(finalStart);
        controlPoint1 = oldCommand.getControlPoint1().minusPoint(originalStart).rotateAroundOrigin(angle).addPoint(finalStart);
        controlPoint2 = oldCommand.getControlPoint2().minusPoint(originalStart).rotateAroundOrigin(angle).addPoint(finalStart);
    }

    /** generate a path command from an old command who is rotated around the center point for angle*/
    public SvgPathCommand(SvgPathCommand oldCommand, Point center, double angle) {
        this.commandType = oldCommand.getCommandType();
        destinationPoint = oldCommand.getDestinationPoint().rotateAroundCenter(center, angle);
        controlPoint1 = oldCommand.getControlPoint1().rotateAroundCenter(center, angle);
        controlPoint2 = oldCommand.getControlPoint2().rotateAroundCenter(center, angle);
    }

    /** generate a path command from an old command who scales around center for proportion*/
    public SvgPathCommand(SvgPathCommand oldCommand, Point center, double proportion, int type) {
        this.commandType = oldCommand.getCommandType();
        destinationPoint = oldCommand.getDestinationPoint().scaleAroundCenter(center, proportion);
        controlPoint1 = oldCommand.getControlPoint1().scaleAroundCenter(center, proportion);
        controlPoint2 = oldCommand.getControlPoint2().scaleAroundCenter(center, proportion);
    }
    public SvgPathCommand(Point controlPoint1, Point controlPoint2, Point destinationPoint, CommandType commandType) {
        this.destinationPoint = destinationPoint;
        this.controlPoint1 = controlPoint1;
        this.controlPoint2  = controlPoint2;
        this.commandType = commandType;
    }

    /**
     * generate a path command from an old command whose center point is shifted
     */
    public static SvgPathCommand commandFromShift(SvgPathCommand oldCommand, Point originalStart, Point finalStart) {
        SvgPathCommand copy = new SvgPathCommand(oldCommand);
        copy.setDestinationPoint(copy.getDestinationPoint().addPoint(finalStart).minusPoint(originalStart));
        copy.setControlPoint1(copy.getControlPoint1().addPoint(finalStart).minusPoint(originalStart));
        copy.setControlPoint2(copy.getControlPoint2().addPoint(finalStart).minusPoint(originalStart));
        return copy;

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

    public void setLineTo() {
        this.commandType = CommandType.LINE_TO;
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

    public void setDestinationPoint(Point destinationPoint) {
        this.destinationPoint = destinationPoint;
    }

    public Point getControlPoint1() {
        return controlPoint1;
    }

    public void setControlPoint1(Point controlPoint1) {
        this.controlPoint1 = controlPoint1;
    }

    public Point getControlPoint2() {
        return controlPoint2;
    }

    public void setControlPoint2(Point controlPoint2) {
        this.controlPoint2 = controlPoint2;
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
            return "M " + destinationPoint.x + "," + destinationPoint.y + " ";
        } else if (isLineTo()) {
            return "L " + destinationPoint.x + "," + destinationPoint.y + " ";
        } else if (isCurveTo()) {
            return "C " + controlPoint1.x + "," + controlPoint1.y + " " + controlPoint2.x + "," + controlPoint2.y + " " + destinationPoint.x + "," + destinationPoint.y + " ";
        } else
            return "";
    }

    public enum CommandType {
        MOVE_TO, LINE_TO, CURVE_TO, SMOOTH_TO, DEFAULT
    }
}
