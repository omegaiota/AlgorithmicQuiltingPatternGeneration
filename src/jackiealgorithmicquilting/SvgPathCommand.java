package src.jackiealgorithmicquilting;

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
            SVGElement sguiggleFile = new SVGElement(tileFile);
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
        destinationPoint = oldCommand.getDestinationPoint().rotateAroundCenterWrongVersion(center, angle);
        controlPoint1 = oldCommand.getControlPoint1().rotateAroundCenterWrongVersion(center, angle);
        controlPoint2 = oldCommand.getControlPoint2().rotateAroundCenterWrongVersion(center, angle);
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

    public static List<SvgPathCommand> reverseCommands(List<SvgPathCommand> l) {
        List<SvgPathCommand> output = new ArrayList<>();
        output.add(new SvgPathCommand(l.get(l.size() - 1).getDestinationPoint(), CommandType.MOVE_TO));
        for (int i = l.size() - 1; i >= 1; i--) {
            SvgPathCommand c = l.get(i);
            output.add(new SvgPathCommand(c.getControlPoint2(), c.getControlPoint1(), l.get(i - 1).getDestinationPoint(), c.getCommandType()));
        }

        return output;
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


    public static List<Point> toPoints(List<SvgPathCommand> decoElmentCommands) {
        List<Point> ans = new ArrayList<>();
        for (SvgPathCommand c : decoElmentCommands) {
            ans.add(c.getDestinationPoint());
            if (c.isCurveTo()) {
                ans.add(c.getControlPoint1());
                ans.add(c.getControlPoint2());
            }
        }
        return ans;
    }


    /**
     * Reflect command with respect to y axis
     *
     * @param decoElmentCommands
     * @return
     */
    public static List<SvgPathCommand> reflect(List<SvgPathCommand> decoElmentCommands) {
//        double maxX = decoElmentCommands.stream().map(c -> c.getDestinationPoint().x).reduce(Double.MIN_VALUE, Double::max);
        List<SvgPathCommand> reflected = new ArrayList<>();
        for (SvgPathCommand c : decoElmentCommands) {
//            SvgPathCommand newCommand;
//            if (c.getCommandType() == CommandType.LINE_TO || c.getCommandType() == CommandType.MOVE_TO) {
//                newCommand = new SvgPathCommand(c);
//                newCommand.setDestinationPoint(new Point(maxX - c.getDestinationPoint().x, c.getDestinationPoint().y));
//            } else {
//                newCommand = new SvgPathCommand(new Point(maxX - c.getControlPoint1().x, c.getControlPoint1().y),
//                        new Point(maxX - c.getControlPoint2().x, c.getControlPoint2().y),
//                        new Point(maxX - c.getDestinationPoint().x, c.getDestinationPoint().y), CommandType.CURVE_TO);
//            }
            SvgPathCommand newCommand = c.multiplyXBy(-1);
            reflected.add(newCommand);
        }
//
//        reflected = SvgPathCommand.commandsShift(reflected, decoElmentCommands.get(0).destinationPoint);
        return reflected;
    }

    private SvgPathCommand multiplyXBy(double scale) {
        switch (commandType) {
            case CURVE_TO:
                return new SvgPathCommand(new Point(controlPoint1.x * scale, controlPoint1.y) ,
                        new Point(controlPoint2.x * scale, controlPoint2.y),
                        new Point(destinationPoint.x * scale, destinationPoint.y), CommandType.CURVE_TO);
            default:
                return new SvgPathCommand(new Point(destinationPoint.x * scale, destinationPoint.y), commandType);
        }
    }


    public static SvgPathCommand catmullRomSegment(SvgPathCommand prevprevCommand, SvgPathCommand prevCommand, SvgPathCommand thisCommand, SvgPathCommand nextCommand) {
        Point p2 = thisCommand.getDestinationPoint(),
                p1 = prevCommand.getDestinationPoint();
        Point p3, p0;
        if (prevprevCommand == null)
            p0 = p1.minus(p2.minus(p1));
        else
            p0 = prevprevCommand.getDestinationPoint();
        if (nextCommand == null)
            p3 = p2.add(p2.minus(p1));
        else
            p3 = nextCommand.getDestinationPoint();
        Point c1 = p2.minus(p0).divide(6).add(p1),
                c2 = p2.minus(p3.minus(p1).divide(6));
        return (new SvgPathCommand(c1, c2, p2, SvgPathCommand.CommandType.CURVE_TO));
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
        } else if (isLineTo() || commandType == CommandType.DEFAULT) {
            return "L " + destinationPoint.x + "," + destinationPoint.y + " ";
        } else if (isCurveTo()) {
            return "C " + controlPoint1.x + "," + controlPoint1.y + " " + controlPoint2.x + "," + controlPoint2.y + " " + destinationPoint.x + "," + destinationPoint.y + " ";
        } else
            return "";
    }

    public SvgPathCommand translateBy(Point shift) {
        switch (commandType) {
            case CURVE_TO:
                return new SvgPathCommand(controlPoint1.add(shift), controlPoint2.add(shift), destinationPoint.add(shift), CommandType.CURVE_TO);
            default:
                return new SvgPathCommand(destinationPoint.add(shift), commandType);
        }
    }

    public SvgPathCommand rotateBy(Point center, double degree) {
        switch (commandType) {
            case CURVE_TO:
                return new SvgPathCommand(controlPoint1.rotateAroundCenter(center, degree), controlPoint2.rotateAroundCenter(center, degree), destinationPoint.rotateAroundCenter(center, degree), CommandType.CURVE_TO);
            default:
                return new SvgPathCommand(destinationPoint.rotateAroundCenter(center, degree), commandType);
        }
    }

    public enum CommandType {
        MOVE_TO, LINE_TO, CURVE_TO, SMOOTH_TO, DEFAULT
    }
}
