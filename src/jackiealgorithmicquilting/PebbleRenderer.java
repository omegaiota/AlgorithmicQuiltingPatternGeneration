package src.jackiealgorithmicquilting;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pebble
 */
public final class PebbleRenderer extends PatternRenderer {
    private List<SvgPathCommand> renderedCommands = new ArrayList<>(),
            decoCommands;
    private SVGElement decoElemFile = null;
    private CircleBound decoCommandsBound = null;
    private List<Integer> touchPointIndex = null;
    private TreeNode<Point> treeRoot;
    private GenerationInfo info;
    private boolean leafRenderOnly = false;
    private List<Double> touchPointAngle = new ArrayList<>();
    private int DEBUGSYMBOL = 0;


    public PebbleRenderer(List<SvgPathCommand> myDecoCommands, GenerationInfo info, boolean leafRenderOnly) {
        super(info.spanningTree);
        this.treeRoot = info.spanningTree;
        this.decoCommands = myDecoCommands;
        this.decoElemFile = info.decoElementFile;
        this.info = info;
        this.leafRenderOnly = leafRenderOnly;
        if (decoElemFile != null) {
            Pair<CircleBound, List<Integer>> decoElemBound = decoElemFile.getBoundingCircleAndTouchPointIndex();
            decoCommandsBound = decoElemBound.getKey();
            touchPointIndex = decoElemBound.getValue();
            this.decoCommands = decoElemFile.getCommandList();
            System.out.println(this.decoCommands.size());
        }
    }

    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }

    /**
     * Second strategy of primitive packing. It processs the primitive first to first the number of points that are touching the boundary circle, and change the
     * tree structure based on that
     */
    @Override
    public void pebbleFilling() {
        pebbleFilling2();
    }

    public void pebbleFilling2() {
//        if (leafRenderOnly) {
//            processTree(treeRoot);
//        }
        double dist = Point.getDistance(treeRoot.getData(), treeRoot.getChildren().get(0).getData());

        dist = info.pointDistributionDist * info.initialLength;


        /* Order children tobe counterclockwise*/
        TreeTraversal.treeOrdering(treeRoot, null);

        //landFillTraverse(treeRoot, null, dist);
        renderedCommands.add(new SvgPathCommand(new Point(treeRoot.getData().x + dist, treeRoot.getData().y), SvgPathCommand.CommandType.MOVE_TO));
        Set<CircleBound> determinedBounds = new HashSet<>();
        treeRoot.setBoundingCircle(new CircleBound(dist, treeRoot.getData()));

        // first determination loop, make sure each pebble collidesWith one pebble
        long stage0 = System.nanoTime();

        pebbleRenderDetermineRadii(treeRoot, determinedBounds);
        long stage1 = System.nanoTime();


        pebbleReplaceShortSegment(treeRoot, determinedBounds);
        long stage2 = System.nanoTime();

//            outputCurrent("stage1_after_replace_short");

        // second determination loop, make sure each pebble collidesWith two pebbles
        pebbleAdjustTreenode(treeRoot, dist, determinedBounds);
        long stage3 = System.nanoTime();

//            outputCurrent("stage2_after_first_adjust");
        renderedCommands.add(new SvgPathCommand(new Point(treeRoot.getData().x + dist, treeRoot.getData().y), SvgPathCommand.CommandType.MOVE_TO));
        pebbleSecondAdjustTreenode(treeRoot, determinedBounds);
        long stage4 = System.nanoTime();

        pebbleReplaceShortSegment(treeRoot, determinedBounds);
        long stage5 = System.nanoTime();


        info.drawBound = false;
        pebbleRenderDraw(treeRoot, 0);
        long stage6 = System.nanoTime();
        SVGElement.outputSvgCommands(renderedCommands, "noBound", info);

        long stage7 = System.nanoTime();
        System.out.printf("\nTotal time for stage%d-stage%d:: %.4f miliseconds\n", 0, 1, (stage1 - stage0) / 1000000.0);
        System.out.printf("Total time for stage%d-stage%d:: %.4f miliseconds\n", 1, 2, (stage2 - stage1) / 1000000.0);
        System.out.printf("Total time for stage%d-stage%d:: %.4f miliseconds\n", 2, 3, (stage3 - stage2) / 1000000.0);
        System.out.printf("Total time for stage%d-stage%d:: %.4f miliseconds\n", 3, 4, (stage4 - stage3) / 1000000.0);
        System.out.printf("Total time for stage%d-stage%d:: %.4f miliseconds\n", 4, 5, (stage5 - stage4) / 1000000.0);
        System.out.printf("Total time for stage%d-stage%d:: %.4f miliseconds\n", 5, 6, (stage6 - stage5) / 1000000.0);
        System.out.printf("Total time for stage%d-stage%d:: %.4f miliseconds\n", 6, 7, (stage7 - stage6) / 1000000.0);


    }

    public void outputCurrent(String name) {
        pebbleRenderDraw(treeRoot, 0);
        SVGElement.outputSvgCommands(renderedCommands, name, info);
        renderedCommands.clear();
    }


    /* We push inflate treenodes that touch two pebbles in the direciton of bisectors
*/
    public void pebbleSecondAdjustTreenode(TreeNode<Point> thisNode, final Set<CircleBound> determinedBounds) {
        Point thisCenter = thisNode.getData();
        double r = thisNode.getBoundingCircle().getRadii();
        Point pebble1 = null, pebble2 = null, bestPoint = null;
        double r1 = 0, r2 = 0, best = r;
        int touchCount = 0;

        for (CircleBound bound : determinedBounds)
            if (bound.getCenter() != thisCenter) {
                if (bound.touches(thisNode.getBoundingCircle())) {
                    touchCount++;
                    if (touchCount == 1) {
                        r1 = bound.getRadii();
                        pebble1 = bound.getCenter();
                    } else if (touchCount == 2) {
                        r2 = bound.getRadii();
                        pebble2 = bound.getCenter();
                    } else
                        break;
                }
            }
        int ITERATION = 200;

        if (touchCount == 1) {
            Point pebble1ToThis = thisCenter.minus(pebble1).unit();

            for (int i = 0; i < ITERATION; i++) {
                boolean valid = true;
                double shiftLen = info.pointDistributionDist * 2.0 / ITERATION * i;
                Point newPoint = thisCenter.add(pebble1ToThis.multiply(shiftLen));
                double newRadii = Point.getDistance(newPoint, pebble1) - r1;
                for (CircleBound b : determinedBounds) {
                    Point pCenter = b.getCenter();
                    if ((pCenter != thisCenter) && (pCenter != pebble1) && (pCenter != pebble2)) {
                        if (b.touches(new CircleBound(newRadii, newPoint))) {
                            valid = false;
                            break;
                        }
                    }
                }

                if (valid && inBound(newPoint, newRadii)) {
//                    if (newRadii > best && newRadii < treeRoot.getBoundingCircleAndTouchPointIndex().getRadii()) {
                    if (newRadii > best) {
                        best = Double.min(newRadii, Point.getDistance(thisNode.getData(), pebble1));
                        bestPoint = newPoint;
                    }
                }
            }

            if (bestPoint != null) {
                determinedBounds.remove(thisNode.getBoundingCircle());
                thisNode.setData(bestPoint);
                thisNode.setBoundingCircle(new CircleBound(best, thisNode.getData()));
                determinedBounds.add(thisNode.getBoundingCircle());
            }


        } else if (touchCount == 2) {
            double angle1 = Point.getAngle(thisCenter, pebble1);
            double angle2 = Point.getAngle(thisCenter, pebble2);
            double angleBTW = angle1 - angle2;
            double rotationAngle = -1 * angleBTW / 2;
//            Point pebble1ToThis = thisNode.getData().minus(pebble1.add(pebble2).multiply(0.5)).unit();
            Point bisectorDir = thisNode.getData().minus(pebble1).unit().add(thisNode.getData().minus(pebble2).unit()).unit();
            if (angleBTW < 0)
                rotationAngle += Math.PI;
            double shiftLen;
            for (int dir = 0; dir < 2; dir++) {
                if (dir == 1)
                    rotationAngle = 2 * Math.PI - rotationAngle;//second iteration, opposite direction of bisector
                //first iteration, one direction
                for (int i = 0; i < ITERATION; i++) {
                    shiftLen = info.pointDistributionDist * 2.0 / ((double) ITERATION) * i;
//                    Point newPointAngle = pebble1.rotateAroundCenterWrongVersion(thisCenter, rotationAngle);
//                    Point newPoint = Point.intermediatePointWithLen(thisCenter, newPointAngle, shiftLen);
                    Point newPoint = thisCenter.add(bisectorDir.multiply(shiftLen));
                    double newRadii1 = Point.getDistance(newPoint, pebble1) - r1;
                    double newRadii2 = Point.getDistance(newPoint, pebble2) - r2;
                    double newRadii = Math.min(newRadii1, newRadii2);

//                    boolean valid = (newRadii > 0) && (Math.abs(newRadii1 - newRadii2) < 0.02);
                    boolean valid = (newRadii > 0);
                    boolean isInBound = inBound(newPoint, newRadii);
                    if (valid && isInBound) {
                        for (CircleBound b : determinedBounds) {
                            Point pCenter = b.getCenter();
                            if ((pCenter != thisCenter) && (pCenter != pebble1) && (pCenter != pebble2)) {
                                if (b.touches(new CircleBound(newRadii, newPoint))) {
                                    valid = false;
                                    break;
                                }
                            }
                        }
                    }

                    if (valid && isInBound) {
//                        if (newRadii > best && newRadii < treeRoot.getBoundingCircleAndTouchPointIndex().getRadii()) {
                        if (newRadii > best) {  // this means pebble can exceed "r_max" inorder to pack boundary
                            best = Double.min(newRadii, Point.getDistance(thisNode.getData(), pebble1));
                            bestPoint = newPoint;
                        }
                    } else {
//                        break;
                    }

                }
            }

            if (bestPoint != null) {
                determinedBounds.remove(thisNode.getBoundingCircle());
                thisNode.setData(bestPoint);
                thisNode.setBoundingCircle(new CircleBound(best, thisNode.getData()));
                determinedBounds.add(thisNode.getBoundingCircle());
            }
        }

        for (TreeNode<Point> child : thisNode.getChildren()) {
            pebbleSecondAdjustTreenode(child, determinedBounds);
        }
    }

    private boolean inBound(Point newPoint, double newRadii) {
        List<Point> bound = info.regionFile.getBoundary().getPoints();
        for (int i = 0; i < bound.size(); i++) {
//          Point foot = Point.perpendicularFoot(newPoint, bound.get(i), bound.get(i + 1));
            if (Point.getDistance(newPoint, bound.get(i)) < newRadii)
                return false;
        }
        return true;
    }

    /* Since all internal nodes touch at least its parent and one of its children, we only adjust positions of treenodes. We
    push each treenode towards the direction of parent-self line until it collidesWith a treeenode.
    */
    public void pebbleAdjustTreenode(TreeNode<Point> thisNode, double dist, final Set<CircleBound> determinedBounds) {
        if (thisNode.getChildren().size() == 0 && thisNode.getParent() != null) {
            // leafnode, adjust position
            double shiftLen;
            int ITERATION = 200;
            TreeNode<Point> parent = thisNode.getParent();
            double thisParentDist = Point.getDistance(thisNode.getParent().getData(), thisNode.getData());
            double thisRadii = thisNode.getBoundingCircle().getRadii();
            double tempRadii;
            Point bestCenter = thisNode.getBoundingCircle().getCenter();
            double bestRadii = thisRadii;
            boolean isValid;
            Point parentToThis = thisNode.getData().minus(parent.getData()).unit();
            for (int i = 1; i < ITERATION; i++) {
                isValid = true;
                shiftLen = (dist * 2 / (double) ITERATION) * i;
//                Point newCenter = Point.intermediatePointWithLen(thisNode.getParent().getData(), thisNode.getData(), thisParentDist + shiftLen);
                Point newCenter = thisNode.getData().add(parentToThis.multiply(shiftLen));
                tempRadii = thisRadii + shiftLen;
                for (CircleBound b : determinedBounds) {
                    boolean isSelf = thisNode.getData() == b.getCenter();
                    boolean isParent = thisNode.getParent().getData() == b.getCenter();
//                    assert Point.getDistance(thisNode.getParent().getData(), newCenter) > thisParentDist;
                    if ((!isSelf) && (!isParent) && (Point.getDistance(b.getCenter(), newCenter) - (b.getRadii() + tempRadii) + 0.01 < 0)) {
                        isValid = false;
                        break;
                    }
                }

                if (isValid) {

                    if (tempRadii > bestRadii && tempRadii < dist) {
                        bestRadii = tempRadii;
                        bestCenter = newCenter;
                    }
                } else {
//                    break;
                }
            }
            if (bestCenter != thisNode.getBoundingCircle().getCenter()) {
                determinedBounds.remove(thisNode.getBoundingCircle());
                thisNode.setData(bestCenter);
                thisNode.setBoundingCircle(new CircleBound(bestRadii, thisNode.getData()));
                determinedBounds.add(thisNode.getBoundingCircle());
            }

        }
        for (TreeNode<Point> child : thisNode.getChildren()) {
            pebbleAdjustTreenode(child, dist, determinedBounds);
        }
    }

    public void pebbleRenderDraw2(TreeNode<Point> thisNode, double angle, boolean DRAW_BOUND) {
        boolean DEBUG = false;
        HashMap<Double, TreeNode<Point>> degreeTreeNodeMap = new HashMap<>();
        /* Record the direction of the children*/
        for (TreeNode<Point> child : thisNode.getChildren()) {
            double thisAngle = Math.toDegrees(Point.getAngle(thisNode.getData(), child.getData()));
            degreeTreeNodeMap.put(thisAngle, child);
        }

        Point zeroAnglePoint = new Point(thisNode.getData().x + thisNode.getBoundingCircle().getRadii(), thisNode.getData().y);
        Point startDrawingPoint = zeroAnglePoint.rotateAroundCenterWrongVersion(thisNode.getData(), Math.toRadians(angle));
        renderedCommands.add(new SvgPathCommand(startDrawingPoint, SvgPathCommand.CommandType.LINE_TO));
        if (DEBUG) {
            SVGElement.outputSvgCommands(renderedCommands, "test", info);
        }

        // Insert a kissing primitive instead of a pebble
        List<SvgPathCommand> scaledCommands = new ArrayList<>();

        if (decoCommands.size() != 0) {
            double scaleFactor = thisNode.getBoundingCircle().getRadii() / decoCommandsBound.getRadii();
            PatternRenderer.translateAndRotatePattern(SvgPathCommand.commandsScaling(decoCommands, scaleFactor,
                    decoCommands.get(0).getDestinationPoint()),
                    startDrawingPoint, Math.toRadians(angle), false, false);
            if (DRAW_BOUND)
                renderedCommands.addAll(scaledCommands);
//            printMapping(scaledCommands, primitiveCentroid);
        }

        /** Pebbles **/
        for (int i = 0; i < scaledCommands.size(); i++) {
            renderedCommands.add(scaledCommands.get(i));
            if (touchPointIndex.contains(i)) {
                double thisAngle = Math.toDegrees(Point.getAngle(thisNode.getData(), scaledCommands.get(i).getDestinationPoint()));
                TreeNode<Point> child = degreeTreeNodeMap.get(thisAngle);
                double currentError = 8.0;
                Double key = thisAngle;
                if (child == null) {
                    for (Double k : degreeTreeNodeMap.keySet()) {
                        if (Math.abs(k - thisAngle) < currentError) {
                            child = degreeTreeNodeMap.get(k);
                            key = k;
                            currentError = Math.abs(k - thisAngle);
                        }
                    }
                }


                if (child == null) {
//                    System.out.println("failed");
//                    assert false;
                } else {
                    pebbleRenderDraw2(child, (thisAngle + 180) % 360, DRAW_BOUND);
                    degreeTreeNodeMap.remove(key);

                }
            }
        }

    }


    public void pebbleRenderDraw(TreeNode<Point> thisNode, int angle) {
//        System.out.println("draw called");
        boolean DEBUG = false;
        boolean DRAW_BOUND = info.drawBound;
        HashMap<Integer, TreeNode<Point>> degreeTreeNodeMap = new HashMap<>();
        boolean[] degreeTable = new boolean[360];
        Arrays.fill(degreeTable, false);
        /* Record the direction of the children*/
        for (TreeNode<Point> child : thisNode.getChildren()) {
            int thisAngle = (int) Math.toDegrees(Point.getAngle(thisNode.getData(), child.getData()));
            degreeTable[thisAngle] = true;
            degreeTreeNodeMap.put(thisAngle, child);
        }

        int gap = 10;
        Point zeroAnglePoint = new Point(thisNode.getData().x + thisNode.getBoundingCircle().getRadii(), thisNode.getData().y);
        Point startDrawingPoint = zeroAnglePoint.rotateAroundCenterWrongVersion(thisNode.getData(), Math.toRadians(angle));
        Point primitiveCentroid = thisNode.getData();
        List<SvgPathCommand> scaledCommands = new ArrayList<>();

        if (decoCommands.size() != 0) {
//            Point zeroAnglePoint = new Point(thisNode.getData().x + thisNode.getBoundingCircle().getRadii(), thisNode.getData().y);
//            Point startDrawingPoint = thisNode.getParent().getData().rotateAroundCenter(thisNode.getParent().getData(), Math.toRadians(angle));
//                    Point startDrawingPoint = thisNode.getParent().getData();
//            renderedCommands.add(new SvgPathCommand(startDrawingPoint, SvgPathCommand.CommandType.LINE_TO));
            double scaleFactor = thisNode.getBoundingCircle().getRadii() / decoCommandsBound.getRadii();
            scaledCommands = PatternRenderer.translateAndRotatePattern(SvgPathCommand.commandsScaling(decoCommands, scaleFactor,
                    new Point(0, 0)), startDrawingPoint, Math.toRadians(angle), false, false);
            /** Pebbles : traverse whole pebble first. this is only needed to avoid aliasing when quilting! **/
//            renderedCommands.addAll(scaledCommands);

        }
        int n = scaledCommands.size();
        double[] commandDegreeTable = new double[n];
        for (int i = 0; i < n; i++) {
            commandDegreeTable[i] = Math.toDegrees(Point.getAngle(primitiveCentroid, scaledCommands.get(i).getDestinationPoint()));
        }

        int currentAngle = angle % 360;
        int startCommand = 0;
        while (startCommand < n - 1) {
            if (Point.angleIsBetweenDegree(currentAngle, commandDegreeTable[startCommand], commandDegreeTable[startCommand + 1]))
                break;
            startCommand++;
        }

        System.out.println("Start command is " + startCommand);
        System.out.println(currentAngle);

        System.out.println(commandDegreeTable[startCommand]);
        System.out.println(commandDegreeTable[startCommand + 1]);
        renderedCommands.add(new SvgPathCommand(scaledCommands.get((startCommand - 1 + n) % n).getDestinationPoint(), SvgPathCommand.CommandType.LINE_TO));
        renderedCommands.add(scaledCommands.get(startCommand));
        TreeNode<Point> child;
        for (int i = 0; i < n; i++) {
            int lastCommand = (startCommand + i) % n;
            int thisCommand = (startCommand + i + 1) % n;
            int startAngle = (int) commandDegreeTable[lastCommand];
            int endAngle = (int) commandDegreeTable[thisCommand];

            /**
             * This is assuming commands don't go across 180 degrees. So a command
             * that goes from 0 to 357 assumes to take the arc of 0, 359, 358, 357
             */
            int stepNum = Math.abs(endAngle - startAngle);
            int increment = (endAngle >= startAngle) ? 1 : -1;
            if (Math.abs(endAngle - startAngle) > 180) {
                stepNum = 360 - stepNum;
                increment *= -1;
            }
            int searchAngle = startAngle;
            for (int step = 0; step < stepNum; step++) {
                searchAngle = (searchAngle + 360) % 360;
                if ((child = degreeTreeNodeMap.get(searchAngle)) != null) {
                    degreeTreeNodeMap.remove(searchAngle % 360);
                    pebbleRenderDraw(child, (searchAngle + 180) % 360);
                }
                searchAngle += increment;
            }


            renderedCommands.add(scaledCommands.get(thisCommand));
        }
    }

    public void pebbleReplaceShortSegment(TreeNode<Point> thisNode, final Set<CircleBound> determinedBounds) {
        determinedBounds.add(thisNode.getBoundingCircle());
        //determine bounding circle for each child
        for (TreeNode<Point> child : thisNode.getChildren()) {
            double childParentDist = Point.getDistance(thisNode.getData(), child.getData());
            double segmentDist = childParentDist - child.getBoundingCircle().getRadii() - thisNode.getBoundingCircle().getRadii();
            if (segmentDist > 0.1) {
                // Strategy 1: insert a new pebble at all short line segments
                Point parentP = thisNode.getData(), childP = child.getData();
                double newRadii = segmentDist / 2.0;
                Point childToParent = parentP.minus(childP).unit();
                Point middlePoint = childP.add(childToParent.multiply(child.getBoundingCircle().getRadii() + newRadii));
                for (CircleBound b : determinedBounds) {
                    newRadii = Double.min(newRadii, Point.getDistance(b.getCenter(), middlePoint) - b.getRadii());
                }
                TreeNode<Point> midTreeNode = new TreeNode<>(middlePoint, new ArrayList<>());
                midTreeNode.setParent(thisNode);
                midTreeNode.addChild(child);
                midTreeNode.setBoundingCircle(new CircleBound(newRadii, midTreeNode.getData()));
                thisNode.removeChild(child);
                thisNode.addChild(midTreeNode);
                child.setParent(midTreeNode);
                determinedBounds.add(midTreeNode.getBoundingCircle());
            }

            pebbleReplaceShortSegment(child, determinedBounds);

        }

    }

    public void pebbleRenderDetermineRadii(TreeNode<Point> thisNode, final Set<CircleBound> determinedBounds) {
        determinedBounds.add(thisNode.getBoundingCircle());
        //determine bounding circle for each child
        for (TreeNode<Point> child : thisNode.getChildren()) {
            double childParentDist = Point.getDistance(thisNode.getData(), child.getData());
            double adjustedRadii = childParentDist - thisNode.getBoundingCircle().getRadii();

            //loop through pebbles that have drawn already to adjust radii
            final TreeNode<Point> thisChild = child;
            ArrayList<CircleBound> sortedBoundByDist = new ArrayList<>(determinedBounds);
            sortedBoundByDist.sort((b1, b2) -> (
                    (int) ((Point.getDistance(thisChild.getData(), b1.getCenter()) - b1.getRadii()) -
                            (Point.getDistance(thisChild.getData(), b2.getCenter()) - b2.getRadii()))
            ));

            int adjustmentSize = (sortedBoundByDist.size() > 20) ? 20 : sortedBoundByDist.size();
            for (int i = 0; i < adjustmentSize; i++) {
                double distanceBetween = Point.getDistance(child.getData(), sortedBoundByDist.get(i).getCenter()) - sortedBoundByDist.get(i).getRadii();
                adjustedRadii = Double.min(distanceBetween, adjustedRadii);
                if (inBound(thisChild.getData(), adjustedRadii)) {
                    if ((distanceBetween > 0) && Double.compare(distanceBetween, adjustedRadii) < 0 && (Double.compare(adjustedRadii - distanceBetween, 0.01) > 0)) {
                        adjustedRadii = distanceBetween;
                        assert adjustedRadii > 0;
                    }
                }
            }

            double minDistTobound = 10000;
            adjustedRadii = Double.min(adjustedRadii, minDistTobound);
            child.setBoundingCircle(new CircleBound(adjustedRadii, child.getData()));
            pebbleRenderDetermineRadii(child, determinedBounds);
        }

    }

}
