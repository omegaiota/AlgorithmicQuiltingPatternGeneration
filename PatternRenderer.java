package jackiequiltpatterndeterminaiton;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 */
public class PatternRenderer {
    private List<SvgPathCommand> skeletonPathCommands;
    private List<SvgPathCommand> decorativeElementCommands;


    private List<SvgPathCommand> renderedCommands = new ArrayList<>();
    private TreeNode<Point> spanningTree;
    private RenderType renderType;
    private String patternName = "", skeletonPathName = "";


    public PatternRenderer(String skeletonPathName, List<SvgPathCommand> skeletonPathCommands, RenderType type) {
        this.skeletonPathName = skeletonPathName;
        this.skeletonPathCommands = skeletonPathCommands;
        this.renderType = type;
    }
    public PatternRenderer(List<SvgPathCommand> commands, RenderType type) {
        this.skeletonPathCommands = commands;
        this.renderType = type;
    }

    public PatternRenderer(String skeletonPathName, List<SvgPathCommand> skeletonPathCommands, String patternName, List<SvgPathCommand> decorativeElementCommands, RenderType type) {
        this.skeletonPathName = skeletonPathName;
        this.skeletonPathCommands = skeletonPathCommands;
        this.patternName = patternName;
        this.decorativeElementCommands =  decorativeElementCommands;
        this.renderType = type;
    }

    public PatternRenderer(TreeNode<Point> spanningTree) {
        this.spanningTree = spanningTree;
    }

    /**
     * Translate some commands to start at a insertion point and rotate them for some angles around the insertion point
     * and return the commands.
     * If supplied with a destination list, it will insert the transformed commands at the end of the list
     *
     * @param patternCommands       commands that will be transformed
     * @param destination           list to which transformed commands will be attached to. null if doesn't want attach.
     * @param insertionPoint        point to which commands need to be translated to
     * @param rotationAngleInRadian angle(specified in radian) that the transformed commands will be rotated
     * @return
     */
    public static List<SvgPathCommand> insertPatternToList(List<SvgPathCommand> patternCommands,
                                                           List<SvgPathCommand> destination,
                                           Point insertionPoint, double rotationAngleInRadian) {
        if (patternCommands.size() == 0)
            return new ArrayList<>();
        List<SvgPathCommand> transformedDecoElmentCommands = new ArrayList<>();
        Point patternPoint = patternCommands.get(0).getDestinationPoint();
        SvgPathCommand newCommand;
        for (int j = 0; j < patternCommands.size(); j++) {
            newCommand = new SvgPathCommand(patternCommands.get(j), patternPoint, insertionPoint, rotationAngleInRadian);
            if (j == 0)
                newCommand.setCommandType(SvgPathCommand.CommandType.LINE_TO);
            transformedDecoElmentCommands.add(newCommand);
        }

        if (destination != null)
            destination.addAll(transformedDecoElmentCommands);
        return transformedDecoElmentCommands;
    }

    public void toCatmullRom() {
        Map<Point, SvgPathCommand> destinationCommandMap = new HashMap<>();
        for (int i = 0; i < skeletonPathCommands.size(); i++) {
            Point p = skeletonPathCommands.get(i).getDestinationPoint();
            SvgPathCommand pastRecord = destinationCommandMap.get(p);
            if (pastRecord != null) {
                renderedCommands.add(new SvgPathCommand(pastRecord.getControlPoint2(),
                        pastRecord.getControlPoint1(), p, SvgPathCommand.CommandType.CURVE_TO));
                continue;
            }
            if (i == 0 || i == skeletonPathCommands.size() - 1) {
                renderedCommands.add(skeletonPathCommands.get(i));
                continue;
            } else {
                Point p2 = skeletonPathCommands.get(i).getDestinationPoint(),
                        p1 = skeletonPathCommands.get(i - 1).getDestinationPoint();
                Point p3, p0;
                if (i - 2 < 0)
                    p0 = p1.minus(p2.minus(p1));
                else
                    p0 = skeletonPathCommands.get(i - 2).getDestinationPoint();
                if (i + 2 > skeletonPathCommands.size() - 1)
                    p3 = p2.add(p2.minus(p1));
                else
                    p3 = skeletonPathCommands.get(i + 1).getDestinationPoint();
                Point c1 = p2.minus(p0).divide(6).add(p1),
                        c2 = p2.minus(p3.minus(p1).divide(6));
                SvgPathCommand newCommand = new SvgPathCommand(c1, c2, skeletonPathCommands.get(i).getDestinationPoint(),
                        SvgPathCommand.CommandType.CURVE_TO);
                renderedCommands.add(newCommand);
                destinationCommandMap.put(p1, newCommand);
            }

        }
    }


    /**
     * Adding decorative element to treenodes on a spline tree
     *
     * @param decoElmentCommands deco element
     */
    public void addDecoElmentToSplineTree(List<SvgPathCommand> decoElmentCommands, GenerationInfo info) {
        /**
         * I'm assuming that rendered commands is holding the tree skeleton after combined to splines
         */
        Map<Point, SvgPathCommand> destinationCommandMap = new HashMap<>();
        List<RectangleBound> decoBounds = new ArrayList<>();
        int n = renderedCommands.size();

        for (int i = n - 1; i >= 0; i--) {
            Point p = renderedCommands.get(i).getDestinationPoint();
            SvgPathCommand pastRecord = destinationCommandMap.get(p);
            if (pastRecord != null) {
                continue;
            }
            Point prevDest = renderedCommands.get(i == 0 ? 0 : i - 1).getDestinationPoint();
//            double anglePrev = Point.getAngle(renderedCommands.get(i == n-1 ? i : i + 1).getDestinationPoint(), p);
            double anglePrev = getTangentAngleBezier(prevDest,
                    renderedCommands.get(i).getControlPoint1(),
                    renderedCommands.get(i).getControlPoint2(),
                    renderedCommands.get(i).getDestinationPoint());
            List<SvgPathCommand> renderedcommands = PatternRenderer.insertPatternToList(decoElmentCommands,
                    null, p, anglePrev);
            RectangleBound thisBound = getBoundingBox(renderedcommands);
            boolean collides = false;

            for (RectangleBound b : decoBounds) {
                if (b.touches(thisBound)) {
                    collides = true;
                    break;
                }
            }
            if (!collides) {
                decoBounds.add(thisBound);
                renderedCommands.addAll(i + 1, renderedcommands);
            } else {

                System.out.println("COLLIDES!");
            }

            destinationCommandMap.put(p, renderedCommands.get(i));

        }
    }

    private RectangleBound getBoundingBox(List<SvgPathCommand> commands) {
        int n = commands.size();
        List<Point> points = commands.stream().map(p -> p.getDestinationPoint()).collect(Collectors.toList());
        points.sort(Comparator.comparingDouble(p -> p.x));

        double minx = points.get(0).x, maxx = points.get(n - 1).x;
        System.out.println("minx:" + minx + " maxx:" + maxx);
        points.sort(Comparator.comparingDouble(p -> p.y));
        double miny = points.get(0).y, maxy = points.get(n - 1).y;
        RectangleBound bound = new RectangleBound(minx, miny, maxx, maxy);
        System.out.println(bound);
        return bound;
    }

    private double getTangentAngleBezier(Point p0, Point p1, Point p2, Point p3) {
        /* P(t) = (1 - t)^3 * P0 + 3t(1-t)^2 * P1 + 3t^2 (1-t) * P2 + t^3 * P3 */
        double t = 0.99;
        double c0 = Math.pow((1 - t), 3), c1 = 3 * t * (1 - t) * (1 - t), c2 = 3 * t * t * (1 - t), c3 = t * t * t;
        Point stepped = (p0.multiply(c0)).add((p1.multiply(c1))).add(p2.multiply(c2)).add(p3.multiply(c3));
        return (Point.getAngle(stepped, p3) + Math.PI);
    }

    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }

    public void pebbleFilling() {

    }

    public List<SvgPathCommand> fixedWidthFilling(double width, double density) {
        System.out.println("Rendering with fixed width");
        renderedCommands.add(skeletonPathCommands.get(0));
        for (int i = 1; i < skeletonPathCommands.size() - 1; i++) {
            SvgPathCommand commandThis = skeletonPathCommands.get(i),
                    commandPrev = skeletonPathCommands.get(i - 1),
                    commandNext = skeletonPathCommands.get(i+1);

            double angleNext = Point.getAngle(commandThis.getDestinationPoint(), commandNext.getDestinationPoint());
            double anglePrev = Point.getAngle(commandThis.getDestinationPoint(), commandPrev.getDestinationPoint());
            double betweenAngle = angleNext - anglePrev;
            System.out.println( i + ":" + angleNext + " " + anglePrev + " " + betweenAngle);
            double rotationAngle = -1.0 * betweenAngle / 2.0;
            if (betweenAngle > 0)
                rotationAngle += Math.PI;

            if (Double.compare(Math.abs(betweenAngle), 0.0001) < 0)
            {
                /* leafnode*/
                Point pointLeft = Point.vertOffset(commandThis.getDestinationPoint(), commandPrev.getDestinationPoint(), width );
                Point pointRight = Point.vertOffset(commandThis.getDestinationPoint(), commandPrev.getDestinationPoint(), width * (-1));
                renderedCommands.add(new SvgPathCommand(pointRight, SvgPathCommand.CommandType.LINE_TO));
                if (renderType == RenderType.WITH_DECORATION) {
                    double random = Math.random();
                    /* random factor */
                    if (Double.compare(random, density) < 1.0)
                        insertPatternToList(decorativeElementCommands, renderedCommands, commandThis.getDestinationPoint(), anglePrev);
                }
                renderedCommands.add(new SvgPathCommand(pointLeft, SvgPathCommand.CommandType.LINE_TO));
            } else {
            /* endpoint of the angle divider */
                Point divEnd = new Point(commandNext.getDestinationPoint()).rotateAroundCenter(commandThis.getDestinationPoint(), rotationAngle);
                Point divPoint = Point.intermediatePointWithLen(commandThis.getDestinationPoint(), divEnd, width);
                //renderedCommands.add(skeletonPathCommands.get(i));
                renderedCommands.add(new SvgPathCommand(divPoint, SvgPathCommand.CommandType.LINE_TO));
                //renderedCommands.add(skeletonPathCommands.get(i));
            }

        }
        renderedCommands.add(skeletonPathCommands.get(skeletonPathCommands.size() - 1));
        return renderedCommands;
    }

    public void repeatWithRotation(Integer repetition) {
        Double angle = Math.toRadians(360.0 / repetition);
        Double time = Math.PI * 2 / angle;
        System.out.println(time.intValue());
        int repeat =  time.intValue();
        SvgPathCommand originCommand = new SvgPathCommand(skeletonPathCommands.get(0).getDestinationPoint(), SvgPathCommand.CommandType.LINE_TO);
        renderedCommands.addAll(skeletonPathCommands);
        for (int i = 1; i < repeat; i++) {
            renderedCommands.add(originCommand);
            for (int j = 1; j < skeletonPathCommands.size(); j++) {
                renderedCommands.add(new SvgPathCommand(skeletonPathCommands.get(j), originCommand.getDestinationPoint(), i* angle ));
            }
        }

        outputRotated(repetition);
    }

    public List<SvgPathCommand> echoPattern(int number) {
        double midX = 0, midY = 0;
        for (SvgPathCommand command : skeletonPathCommands) {
            midX += command.getDestinationPoint().x;
            midY += command.getDestinationPoint().y;
        }
        midX /= skeletonPathCommands.size();
        midY /= skeletonPathCommands.size();

        double proportion = 1.0 / number;
        System.out.println(proportion);
        renderedCommands.addAll(skeletonPathCommands);
        Point start = skeletonPathCommands.get(0).getDestinationPoint();
        Point end = skeletonPathCommands.get(skeletonPathCommands.size() - 1).getDestinationPoint();
        Point center = Point.intermediatePointWithProportion(start, end, 0.5);
        for (int k = 1; k < number; k++) {
            Point baseDestination = Point.intermediatePointWithProportion(start, end, 1 - proportion * k / 2);
            Point complementBaseDestination = Point.intermediatePointWithProportion(start, end, proportion * k / 2);
            if (k % 2 == 1)
                renderedCommands.add( new SvgPathCommand(baseDestination, SvgPathCommand.CommandType.LINE_TO));
            else
                renderedCommands.add( new SvgPathCommand(complementBaseDestination, SvgPathCommand.CommandType.LINE_TO));
            System.out.println("Calling command with proportion:" + (1 - proportion * k));
            List<SvgPathCommand> scaled = SvgPathCommand.commandsScaling(skeletonPathCommands, 1 - proportion * k, new Point(midX, midY));
            List<SvgPathCommand> newSet = new ArrayList<>();
            if (k % 2 == 1){
                System.out.println("this loop");
            for (int i = scaled.size() - 2; i >= 1; i--) {
                System.out.println(i);
                newSet.add(scaled.get(i));
            }
            } else {
                System.out.println("that loop");
                for (int i = 1; i < scaled.size() - 1; i++) {
                    newSet.add(scaled.get(i));
                }
            }
            //newSet = SvgPathCommand.commandsShift(scaled, baseDestination);
            renderedCommands.addAll(newSet);
            if (k % 2 == 1)
                renderedCommands.add( new SvgPathCommand(complementBaseDestination, SvgPathCommand.CommandType.LINE_TO));
            else
                renderedCommands.add( new SvgPathCommand(baseDestination, SvgPathCommand.CommandType.LINE_TO));
        }
        renderedCommands.add( new SvgPathCommand(center, SvgPathCommand.CommandType.LINE_TO));


        return renderedCommands;

    }


    public File outputEchoed(int number) {
        return SvgFileProcessor.outputSvgCommands(renderedCommands, skeletonPathName + "-echo-" + number, null);
    }

    public File  outputRotated(Integer angle) {
        return SvgFileProcessor.outputSvgCommands(renderedCommands, skeletonPathName + "-rotation-" + angle.intValue(), null);
    }

    public enum RenderType {
        NO_DECORATION, WITH_DECORATION, ROTATION, ECHO, CATMULL_ROM
    }


}
