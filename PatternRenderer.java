package jackiesvgprocessor;

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
    public enum RenderType {
        NO_DECORATION, WITH_DECORATION, ROTATION, ECHO, LANDFILL
    }
    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }



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

    public PatternRenderer(String patternName, List<SvgPathCommand> decorativeElementCommands, TreeNode<Point> spanningTree, RenderType type) {
        this.patternName = patternName;
        this.decorativeElementCommands =  decorativeElementCommands;
        this.spanningTree = spanningTree;
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
    public void landFill() {
        Double dist = Point.getDistance(spanningTree.getData(), spanningTree.getChildren().get(0).getData());
        for (TreeNode<Point> firstChildren : spanningTree.getChildren()) {
            if (Double.compare((Point.getDistance(spanningTree.getData(), firstChildren.getData())), dist) < 0)
                dist = Point.getDistance(spanningTree.getData(), firstChildren.getData());
        }
        dist = dist * 0.6;
        System.out.println("Command distance is" + dist);

        /* Order children tobe counterclockwise*/
        TreeTraversal.treeOrdering(spanningTree, null);
        //landFillTraverse(spanningTree, null, dist);
        renderedCommands.add(new SvgPathCommand(new Point(spanningTree.getData().getX() + dist, spanningTree.getData().getY()), SvgPathCommand.CommandType.MOVE_TO));
        HashMap<Point, Double> radiusList = new HashMap<>();
        landFillPebble(spanningTree, 0, dist, radiusList);


        outputLandFill();
    }

    public void landFillPebble(TreeNode<Point> thisNode, int angle, double dist, HashMap<Point, Double> radiusList) {
        HashMap<Integer, TreeNode<Point>> degreeTreeNodeMap = new HashMap<>();
//        System.out.println("Dist:" + dist);
        boolean[] degreeTable = new boolean[360];
        Arrays.fill(degreeTable, false);
        /* Record the direction of the children*/
        for (TreeNode<Point> child : thisNode.getChildren()) {
            int thisAngle = (int) Math.toDegrees(Point.getAngle(thisNode.getData(), child.getData()));
            degreeTable[thisAngle] = true;
            degreeTreeNodeMap.put(thisAngle, child);
        }

        int gap = 30;
        Point zeroAnglePoint = new Point(thisNode.getData().getX() + dist, thisNode.getData().getY());
        for (Integer i = angle; i < 360; i += gap) {
            TreeNode<Point> child;
            Point thisPoint = new Point(zeroAnglePoint);
            Point.rotateAroundCenter(thisPoint, thisNode.getData(), Math.toRadians(i));
            renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
            for (Integer j = i; j < i + gap; j++) {
                if ((child = degreeTreeNodeMap.get(j)) != null) {
                    int newAngle = i - 180;
                    if (newAngle < 0)
                        newAngle += 360;
                    double newDist = Point.getDistance(thisNode.getData(), child.getData()) - dist;
                    // Find the minimum radii that won't cause conflict issue
                    for (TreeNode<Point> firstChildren : child.getChildren()) {
                        double distanceBetween = Point.getDistance(child.getData(), firstChildren.getData());
                        if (Double.compare(distanceBetween, 0.02) > 0 && Double.compare(distanceBetween, newDist) < 0)
                            newDist = distanceBetween ;
                    }

                    for (Point prevCenter: radiusList.keySet()) {
                        double distanceBetween = Point.getDistance(child.getData(), prevCenter) - radiusList.get(prevCenter);
                        System.out.println("ListDist:" + radiusList.get(prevCenter));
                        if (Double.compare(distanceBetween, newDist) < 0)
                            newDist = distanceBetween ;
                    }
                    radiusList.put(child.getData(), newDist);
                    thisPoint = new Point(zeroAnglePoint);
                    Point.rotateAroundCenter(thisPoint, thisNode.getData(), Math.toRadians(j));
                    renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
                   landFillPebble(child, newAngle, newDist, radiusList);
                    renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
                }
            }
            renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));

        }

        for (int i = 0 + (angle % gap); i < angle; i+= gap) {
            TreeNode<Point> child;
            Point thisPoint = new Point(zeroAnglePoint);
            Point.rotateAroundCenter(thisPoint, thisNode.getData(), Math.toRadians(i));
            renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
            for (Integer j = i; j < i + gap; j++) {
                if ((child = degreeTreeNodeMap.get(j)) != null) {
                    int newAngle = i - 180;
                    if (newAngle < 0)
                        newAngle += 360;
                    double newDist = Point.getDistance(thisNode.getData(), child.getData()) - dist;
                    for (TreeNode<Point> firstChildren : child.getChildren()) {
                        double distanceBetween = Point.getDistance(child.getData(), firstChildren.getData());
                        if (Double.compare(distanceBetween, 0.02) > 0 &&  Double.compare(distanceBetween, newDist) < 0)
                            newDist = distanceBetween * 0.8;
                    }
                    for (Point prevCenter: radiusList.keySet()) {
                        double distanceBetween = Point.getDistance(child.getData(), prevCenter) - radiusList.get(prevCenter);
                        if (Double.compare(distanceBetween, newDist) < 0)
                            newDist = distanceBetween * 0.8 ;
                    }
                    radiusList.put(child.getData(), newDist);
                    thisPoint = new Point(zeroAnglePoint);
                    Point.rotateAroundCenter(thisPoint, thisNode.getData(), Math.toRadians(j));
                    renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
                    landFillPebble(child, newAngle, newDist, radiusList);
                    renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
                }
            }
            renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
        }
        Point thisPoint = new Point(zeroAnglePoint);
        Point.rotateAroundCenter(thisPoint, thisNode.getData(), Math.toRadians(angle));
        renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
    }

    public void fixedWidthFilling(double width) {
        System.out.println("Rendering with fixed width");
        renderedCommands.add(skeletonPathCommands.get(0));
        for (int i = 1; i < skeletonPathCommands.size() - 1; i++) {
            SvgPathCommand commandThis = skeletonPathCommands.get(i),
                    commandPrev = skeletonPathCommands.get(i - 1),
                    commandNext = skeletonPathCommands.get(i+1);

            double angleNext = Point.getAngle( commandThis.getDestinationPoint(), commandNext.getDestinationPoint());
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
                    insertPatternToList(decorativeElementCommands, renderedCommands, commandThis.getDestinationPoint(), anglePrev);
                }
                renderedCommands.add(new SvgPathCommand(pointLeft, SvgPathCommand.CommandType.LINE_TO));
            } else {
            /* endpoint of the angle divider */
                Point divEnd = new Point(commandNext.getDestinationPoint());
                Point.rotateAroundCenter(divEnd, commandThis.getDestinationPoint(), rotationAngle);
                Point divPoint = Point.intermediatePointWithLen(commandThis.getDestinationPoint(), divEnd, width);
                //renderedCommands.add(skeletonPathCommands.get(i));
                renderedCommands.add(new SvgPathCommand(divPoint, SvgPathCommand.CommandType.LINE_TO));
                //renderedCommands.add(skeletonPathCommands.get(i));
            }

        }
        renderedCommands.add(skeletonPathCommands.get(skeletonPathCommands.size() - 1));
        System.out.println("Outputing.....");
        outputRendered(width);
    }

    public void insertPatternToList(List<SvgPathCommand> patternCommands,
                                    List<SvgPathCommand> combinedCommands,
                                    Point insertionPoint, double rotationAngle) {
        Point patternPoint = patternCommands.get(0).getDestinationPoint();
        SvgPathCommand newCommand;
        for (int j = 0; j < patternCommands.size(); j++) {
                newCommand = new SvgPathCommand(patternCommands.get(j), patternPoint, insertionPoint, rotationAngle);
                if (j == 0)
                    newCommand.setCommandType(SvgPathCommand.CommandType.LINE_TO);
            combinedCommands.add(newCommand);
        }

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
            midX += command.getDestinationPoint().getX();
            midY += command.getDestinationPoint().getY();
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
        svgFileProcessor.outputSvgCommands(renderedCommands, "landFill");
    }

    public File outputEchoed(int number) {
        return svgFileProcessor.outputSvgCommands(renderedCommands, skeletonPathName + "-echo-" + number);
    }

    public File  outputRendered(double width) {
        return svgFileProcessor.outputSvgCommands(renderedCommands, skeletonPathName + "-render-" + width);
    }
    public File  outputRotated(Integer angle) {
        return svgFileProcessor.outputSvgCommands(renderedCommands, skeletonPathName + "-rotation-" + angle.intValue());
    }


}
