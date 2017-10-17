package jackiequiltpatterndeterminaiton;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by JacquelineLi on 6/25/17.
 */
public class PatternRenderer {
    private List<SvgPathCommand> skeletonPathCommands, decorativeElementCommands, renderedCommands = new ArrayList<>();
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

    public PatternRenderer(TreeNode<Point> spanningTree, RenderType type) {
        this.spanningTree = spanningTree;
        this.renderType = type;
    }

    public PatternRenderer(ArrayList<SvgPathCommand> commands, RenderType type) {
        this.skeletonPathCommands = commands;
        this.renderType = type;
    }

    public static void insertPatternToList(List<SvgPathCommand> patternCommands,
                                           List<SvgPathCommand> combinedCommands,
                                           Point insertionPoint, double rotationAngle) {
        if (patternCommands.size() == 0)
            return;
        Point patternPoint = patternCommands.get(0).getDestinationPoint();
        SvgPathCommand newCommand;
        for (int j = 0; j < patternCommands.size(); j++) {
            newCommand = new SvgPathCommand(patternCommands.get(j), patternPoint, insertionPoint, rotationAngle);
            if (j == 0)
                newCommand.setCommandType(SvgPathCommand.CommandType.LINE_TO);
            combinedCommands.add(newCommand);
        }

    }

    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }

    public void pebbleFilling() {
        Double dist = Point.getDistance(spanningTree.getData(), spanningTree.getChildren().get(0).getData());
        for (TreeNode<Point> firstChildren : spanningTree.getChildren()) {
            if (Double.compare((Point.getDistance(spanningTree.getData(), firstChildren.getData())), dist) < 0)
                dist = Point.getDistance(spanningTree.getData(), firstChildren.getData());
        }
        dist = dist * 0.63;
        System.out.println("Command distance is" + dist);

        /* Order children tobe counterclockwise*/
        TreeTraversal.treeOrdering(spanningTree, null);
        //landFillTraverse(spanningTree, null, dist);
        renderedCommands.add(new SvgPathCommand(new Point(spanningTree.getData().x + dist, spanningTree.getData().y), SvgPathCommand.CommandType.MOVE_TO));
        HashMap<Point, Double> radiusList = new HashMap<>();
        spanningTree.setRadii(dist);
        pebbleRenderDetermineRadii(spanningTree, 0, dist, radiusList);
        pebbleRenderDraw(spanningTree, 0);
    }

    public void pebbleRenderDraw(TreeNode<Point> thisNode, int angle) {
        HashMap<Integer, TreeNode<Point>> degreeTreeNodeMap = new HashMap<>();
        boolean[] degreeTable = new boolean[360];
        Arrays.fill(degreeTable, false);
        /* Record the direction of the children*/
        for (TreeNode<Point> child : thisNode.getChildren()) {
            int thisAngle = (int) Math.toDegrees(Point.getAngle(thisNode.getData(), child.getData()));
            degreeTable[thisAngle] = true;
            degreeTreeNodeMap.put(thisAngle, child);
        }

        int gap = 15;
        Point zeroAnglePoint = new Point(thisNode.getData().x + thisNode.getRadii(), thisNode.getData().y);
        for (Integer offset = 0; offset < 360; offset += gap) {
            int currentAngle = (angle + offset) % 360;
            TreeNode<Point> child;
            Point thisPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(currentAngle));
            renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));

            for (Integer j = currentAngle; j < currentAngle + gap; j++) {
                int searchAngle = j % 360;
                if ((child = degreeTreeNodeMap.get(searchAngle)) != null) {
                    int newAngle = (searchAngle + 180) % 360;
                    // Find the minimum radii that won't cause conflict issue
                    thisPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(j));
                    renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
                    pebbleRenderDraw(child, newAngle);
                    renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
                }
            }
            renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));


        }

        Point thisPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(angle));
        renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));

    }

    public void pebbleRenderDetermineRadii(TreeNode<Point> thisNode, int angle, double dist, final HashMap<Point, Double> radiusList) {
        HashMap<Integer, TreeNode<Point>> degreeTreeNodeMap = new HashMap<>();
        boolean[] degreeTable = new boolean[360];
        Arrays.fill(degreeTable, false);
        /* Record the direction of the children*/
        for (TreeNode<Point> child : thisNode.getChildren()) {
            int thisAngle = (int) Math.toDegrees(Point.getAngle(thisNode.getData(), child.getData()));
            degreeTable[thisAngle] = true;
            degreeTreeNodeMap.put(thisAngle, child);
        }

        int gap = 15;
        Point zeroAnglePoint = new Point(thisNode.getData().x + dist, thisNode.getData().y);
        for (Integer offset = 0; offset < 360; offset += gap) {
            int currentAngle = (angle + offset) % 360;
            TreeNode<Point> child;
            Point thisPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(currentAngle));

            for (Integer j = currentAngle; j < currentAngle + gap; j++) {
                int searchAngle = j % 360;
                if ((child = degreeTreeNodeMap.get(searchAngle)) != null) {
                    int newAngle = (searchAngle + 180) % 360;
                    double newDist = Point.getDistance(thisNode.getData(), child.getData()) - dist;
                    // Find the minimum radii that won't cause conflict issue
                    List<Point> childrenPoint = new ArrayList<>();
                    for (TreeNode<Point> firstChildren : child.getChildren()) {
                        double distanceBetween = Point.getDistance(child.getData(), firstChildren.getData());
                        if (Double.compare(distanceBetween, 0.002) > 0 && Double.compare(distanceBetween, newDist) < 0)
                            newDist = distanceBetween ;
                        childrenPoint.add(firstChildren.getData());
                    }

                    double radiiBeforeAdjust = newDist;
                    boolean shortLineSegment = false;

                    //loop through pebbles that have drawn already to adjust radii
                    childrenPoint.add(child.getData());
                    final TreeNode<Point> thisChild = child;
                    ArrayList<Point> pointsDetermined = new ArrayList<>(radiusList.keySet());
                    pointsDetermined.sort((p1, p2) -> (
                            new Double(Point.getDistance(thisChild.getData(), p1) - radiusList.get(p1)).compareTo((Point.getDistance(thisChild.getData(), p2) - radiusList.get(p2))
                            )));
                    if (pointsDetermined.size() != 0) {
                        double distanceBetween = Point.getDistance(child.getData(), pointsDetermined.get(0)) - radiusList.get(pointsDetermined.get(0));
                        if ((distanceBetween > 0) && Double.compare(distanceBetween, newDist) < 0) {
                            newDist = distanceBetween ;
                            assert newDist > 0;
                            shortLineSegment = true;

                        }
                    }

                    radiusList.put(child.getData(), newDist);
                    thisPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(j));


                    if (shortLineSegment) {
                        // Strategy 1: insert a new pebble at allshort line segments
                        double distBtwChildParent = Point.getDistance(thisNode.getData(), child.getData());
                        assert (distBtwChildParent - dist - newDist) > 0;
                        double newRadii = (distBtwChildParent - dist - newDist) / 2.0;
                        Point middlePoint = Point.intermediatePointWithLen(thisNode.getData(), child.getData(), newRadii + dist);
                        if (!Point.onLine(thisNode.getData(), child.getData(), middlePoint)) {
                            System.out.println("");
                            assert newRadii + dist < distBtwChildParent;
                        }
                        TreeNode<Point> midTreeNode = new TreeNode<>(middlePoint, new ArrayList<>());
                        midTreeNode.addChild(child);
                        thisNode.removeChild(child);
                        thisNode.addChild(midTreeNode);
                        midTreeNode.setRadii(newRadii);
                        pebbleRenderDetermineRadii(midTreeNode, newAngle, newRadii, radiusList);
                    } else {
                        child.setRadii(newDist);
                        pebbleRenderDetermineRadii(child, newAngle, newDist, radiusList);
                    }

                }
            }
        }

    }


    public void fixedWidthFilling(double width, double density) {
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
            double rotationAngle = -1 * betweenAngle / 2;
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
                    if (Double.compare(random, density) < 1)
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
        System.out.println("Outputing.....");
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

    public File echoPattern(int number) {
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


        return outputEchoed(number);

    }

    private void outputLandFill() {
        SvgFileProcessor.outputSvgCommands(renderedCommands, "pebbleFilling");
    }

    public File outputEchoed(int number) {
        return SvgFileProcessor.outputSvgCommands(renderedCommands, skeletonPathName + "-echo-" + number);
    }

    public File  outputRendered(double width) {
        return SvgFileProcessor.outputSvgCommands(renderedCommands, skeletonPathName + "-render-" + width);
    }

    public File  outputRotated(Integer angle) {
        return SvgFileProcessor.outputSvgCommands(renderedCommands, skeletonPathName + "-rotation-" + angle.intValue());
    }

    public enum RenderType {
        NO_DECORATION, WITH_DECORATION, ROTATION, ECHO, LANDFILL
    }


}
