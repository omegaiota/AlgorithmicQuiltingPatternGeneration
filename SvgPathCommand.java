package jackiequiltpatterndeterminaiton;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/13/17.
 */
public class SvgPathCommand {
    private static final File squiggleLibrary = new File("./src/resources/squiggles/");
    private static List<List<SvgPathCommand>> squiggleList;

    static {
        squiggleList = new ArrayList<>(new ArrayList<>());
        for (File tileFile : squiggleLibrary.listFiles()) {
            SvgFileProcessor sguiggleFile = new SvgFileProcessor(tileFile);
            try {
                sguiggleFile.processSvg();
                squiggleList.add(sguiggleFile.getCommandList());
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("QguiggleList initialized:" + squiggleList.size());
    }

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

    public SvgPathCommand(SvgPathCommand oldCommand, CommandType newType) {
        this.destinationPoint = new Point(oldCommand.getDestinationPoint());
        this.controlPoint1 = new Point(oldCommand.getControlPoint1());
        this.controlPoint2 = new Point(oldCommand.getControlPoint2());
        this.commandType = newType;
    }
    public SvgPathCommand(Point destinationPoint, CommandType commandType) {
        this.destinationPoint = new Point(destinationPoint);
        this.controlPoint1 = new Point();
        this.controlPoint2 = new Point();
        this.commandType = commandType;
    }
    public SvgPathCommand(SvgPathCommand oldCommand, Point originalStart, Point finalStart) {
        this.commandType = oldCommand.getCommandType();
        destinationPoint = oldCommand.getDestinationPoint().add(finalStart).minus(originalStart);
        controlPoint1 = oldCommand.getControlPoint1().add(finalStart).minus(originalStart);
        controlPoint2 = oldCommand.getControlPoint2().add(finalStart).minus(originalStart);
    }

    /** generate a path command from an old command whose center point is shifted and rotated*/
    public SvgPathCommand(SvgPathCommand oldCommand, Point originalStart, Point finalStart, double radian) {
        this.commandType = oldCommand.getCommandType();
        destinationPoint = oldCommand.getDestinationPoint().minus(originalStart).rotateAroundOrigin(radian).add(finalStart);
        controlPoint1 = oldCommand.getControlPoint1().minus(originalStart).rotateAroundOrigin(radian).add(finalStart);
        controlPoint2 = oldCommand.getControlPoint2().minus(originalStart).rotateAroundOrigin(radian).add(finalStart);
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

    public static SvgPathCommand commandFromShiftAndRotate(SvgPathCommand oldCommand, Point originalStart, Point finalStart, double angle) {
        SvgPathCommand copy = new SvgPathCommand(oldCommand);
        copy.setDestinationPoint(copy.getDestinationPoint().minus(originalStart).rotateAroundOrigin(angle).add(finalStart));
        copy.setControlPoint1(copy.getControlPoint1().minus(originalStart).rotateAroundOrigin(angle).add(finalStart));
        copy.setControlPoint2(copy.getControlPoint2().minus(originalStart).rotateAroundOrigin(angle).add(finalStart));
        return copy;
    }

    /**
     * generate a path command from an old command whose center point is shifted
     */
    public static SvgPathCommand commandFromShift(SvgPathCommand oldCommand, Point originalStart, Point finalStart) {
        SvgPathCommand copy = new SvgPathCommand(oldCommand);
        copy.setDestinationPoint(copy.getDestinationPoint().add(finalStart).minus(originalStart));
        copy.setControlPoint1(copy.getControlPoint1().add(finalStart).minus(originalStart));
        copy.setControlPoint2(copy.getControlPoint2().add(finalStart).minus(originalStart));
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

    public static List<SvgPathCommand> sguiggalized(Point parentNodeData, Point data, CommandType type) {
        double newDist = Point.getDistance(parentNodeData, data);
        double angle = Point.getAngle(parentNodeData, data);
        List<SvgPathCommand> squiggleCommands = squiggleList.get((int) (Math.random() * squiggleList.size()));
        System.out.println("read squiggle size:" + squiggleCommands.size());
        Point squiggleStart = squiggleCommands.get(0).getDestinationPoint(),
                squiggleEnd = squiggleCommands.get(squiggleCommands.size() - 1).getDestinationPoint();
        double squiggleDist = Point.getDistance(squiggleStart, squiggleEnd);
        List<SvgPathCommand> returnList = new ArrayList<>();
        for (SvgPathCommand command : squiggleCommands) {
            returnList.add(SvgPathCommand.commandFromShiftAndRotate(command, squiggleStart, parentNodeData, angle));

        }

        returnList.get(0).setCommandType(CommandType.LINE_TO);


        return SvgPathCommand.commandsScaling(returnList, newDist / squiggleDist, parentNodeData);
    }

    /**
     * Break a Bezier Spline at a point using DeCasteljau's algorithm
     *
     * @param start start point of the bezier curve
     * @param t     a value between 0,1 specifying the position to break the curve
     * @param c1
     * @param c2
     * @param end
     * @return
     */
    public static SvgPathCommand splineSplitting(Point start, Point c1, Point c2, Point end, double t) {
        double x1 = start.x, y1 = start.y,
                x2 = c1.x, y2 = c1.y,
                x3 = c2.x, y3 = c2.y,
                x4 = end.x, y4 = end.y;
        double x12 = (x2 - x1) * t + x1,
                y12 = (y2 - y1) * t + y1,

                x23 = (x3 - x2) * t + x2,
                y23 = (y3 - y2) * t + y2,

                x34 = (x4 - x3) * t + x3,
                y34 = (y4 - y3) * t + y3,

                x123 = (x23 - x12) * t + x12,
                y123 = (y23 - y12) * t + y12,

                x234 = (x34 - x23) * t + x23,
                y234 = (y34 - y23) * t + y23,

                x1234 = (x234 - x123) * t + x123,
                y1234 = (y234 - y123) * t + y123;
        return new SvgPathCommand(new Point(x12, y12),
                new Point(x123, y123),
                new Point(x1234, y1234),
                CommandType.CURVE_TO);

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
