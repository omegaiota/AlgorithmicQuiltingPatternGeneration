package jackiequiltpatterndeterminaiton;

import javafx.util.Pair;

import java.util.*;

/**
 * Pebble
 */
public final class PebbleRenderer extends PatternRenderer {
    private List<SvgPathCommand> renderedCommands = new ArrayList<>(),
            decoCommands;
    private SvgFileProcessor decoElemFile = null;
    private CircleBound decoCommandsBound = null;
    private List<Integer> touchPointIndex = null;
    private TreeNode<Point> treeRoot;
    private GenerationInfo info;
    private boolean leafRenderOnly = false;
    private List<Double> touchPointAngle = new ArrayList<>();


    public PebbleRenderer(List<SvgPathCommand> decoCommands, GenerationInfo info, boolean leafRenderOnly) {
        super(info.getSpanningTree());
        this.treeRoot = info.getSpanningTree();
        this.decoCommands = decoCommands;
        this.decoElemFile = info.getDecoElementFile();
        this.info = info;
        this.leafRenderOnly = leafRenderOnly;
        if (decoElemFile != null) {
            Pair<CircleBound, List<Integer>> decoElemBound = decoElemFile.getBoundingCircle();
            decoCommandsBound = decoElemBound.getKey();
            touchPointIndex = decoElemBound.getValue();
            this.decoCommands = decoElemFile.getCommandList();
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
        pebbleFilling2(true);
//
//         using optimized point touching algorithm strategy
//        if (decoElemFile == null || touchPointIndex.size() < 3) {
//            // render a pebble, which would be handled in normal pebbleFilling
//            pebbleFilling2(true);
//        } else {
//            assert decoCommandsBound != null && touchPointIndex != null;
//            for (Integer index : touchPointIndex) {
//                Point p = decoCommands.get(index).getDestinationPoint();
//                touchPointAngle.add(Point.getAngle(decoCommandsBound.getCenter(), p));
//            }
//            Collections.sort(touchPointAngle);
//
//
//            // Create a new tree where each
//            for (double a : touchPointAngle)
//                System.out.println("touch angle: " + Math.toDegrees(a));
//            PointDistribution distribution = new PointDistribution(touchPointAngle, info);
//            distribution.generate();
//            List<SvgPathCommand> traversalCommands = distribution.toTraversal();
//            treeRoot = info.getSpanningTree();
//            pebbleFilling2(false);
//            traversalCommands.addAll(renderedCommands);
//            SvgFileProcessor.outputSvgCommands(traversalCommands, "withTree", info);
//        }

    }

    public void pebbleFilling2(boolean optimizePebble) {
//        if (leafRenderOnly) {
//            processTree(treeRoot);
//        }
        double dist = Point.getDistance(treeRoot.getData(), treeRoot.getChildren().get(0).getData());
//        for (TreeNode<Point> firstChildren : treeRoot.getChildren()) {
//            if (Double.compare((Point.getDistance(treeRoot.getData(), firstChildren.getData())), dist) < 0)
//                dist = Point.getDistance(treeRoot.getData(), firstChildren.getData());
//        }
        dist = info.getPointDistributionDist();
        dist = dist * info.getInitialLength();

        System.out.println("Command distance is" + dist);

        /* Order children tobe counterclockwise*/
        TreeTraversal.treeOrdering(treeRoot, null);

        //landFillTraverse(treeRoot, null, dist);
        renderedCommands.add(new SvgPathCommand(new Point(treeRoot.getData().x + dist, treeRoot.getData().y), SvgPathCommand.CommandType.MOVE_TO));
        Set<CircleBound> determinedBounds = new HashSet<>();
        treeRoot.setBoundingCircle(new CircleBound(dist, treeRoot.getData()));

        // first determination loop, make sure each pebble collidesWith one pebble
        pebbleRenderDetermineRadii(treeRoot, determinedBounds);
        double RANDFAC = info.getRandomFactor();
//        RANDFAC = 0.99;
        pebbleRandomize(treeRoot, RANDFAC);
        if (optimizePebble) {
            outputCurrent("stage0");

            pebbleReplaceShortSegment(treeRoot, determinedBounds);
            outputCurrent("stage1_after_replace_short");

            // second determination loop, make sure each pebble collidesWith two pebbles
            pebbleAdjustTreenode(treeRoot, dist, determinedBounds);
            outputCurrent("stage2_after_first_adjust");
            renderedCommands.add(new SvgPathCommand(new Point(treeRoot.getData().x + dist, treeRoot.getData().y), SvgPathCommand.CommandType.MOVE_TO));
            pebbleSecondAdjustTreenode(treeRoot, determinedBounds);
            pebbleReplaceShortSegment(treeRoot, determinedBounds);

//            if (leafRenderOnly)
//                pebbleRenderDrawLeaf(treeRoot, 0);
//            else

            info.setDrawBound(true);
            pebbleRenderDraw(treeRoot, 0);
            SvgFileProcessor.outputSvgCommands(renderedCommands, "withBound", info);
            renderedCommands.clear();


            List<SvgPathCommand> decoCommandsCopy = new ArrayList<>(decoCommands);
            decoCommands.clear();

            renderedCommands.clear();
            pebbleRenderDraw(treeRoot, 0);
            SvgFileProcessor.outputSvgCommands(renderedCommands, "pebble", info);

            decoCommands = decoCommandsCopy;
            renderedCommands.clear();
            info.setDrawBound(false);
            pebbleRenderDraw(treeRoot, 0);
            SvgFileProcessor.outputSvgCommands(renderedCommands, "noBound", info);
        } else {
            outputCurrent("stage0");
            renderedCommands.add(new SvgPathCommand(new Point(treeRoot.getData().x + dist, treeRoot.getData().y), SvgPathCommand.CommandType.MOVE_TO));
            if (leafRenderOnly)
                pebbleRenderDrawLeaf(treeRoot, 0);
            else
                pebbleRenderDraw2(treeRoot, 0, info.getDrawBound());

        }


    }

    private void pebbleRandomize(TreeNode<Point> treeRoot, double randfac) {
        double fac = Math.random();
        double r = treeRoot.getBoundingCircle().getRadii();

        if (r > info.getPointDistributionDist() * 0.5 * 0.3 && fac > randfac) {
            fac = Math.random() * 0.4 + 0.3;
            treeRoot.getBoundingCircle().setRadii(fac * r);
        }

        for (TreeNode<Point> child : treeRoot.getChildren()) {
            pebbleRandomize(child, randfac);
        }
    }
//
//    /* add leaf nodes*/
//    private void processTree(TreeNode<Point> currTreeNode) {
//        TreeNode<Point> parent = currTreeNode.getParent();
//        double len = 0;
//        if (parent != null) {
//            Point parentPoint = parent.getData(), thisPoint = currTreeNode.getData();
//
//            TreeNode<Point> lastAddedNode = parent;
//            for (int i = 0; i < Point.getDistance(parentPoint, thisPoint) / info.getPointDistributionDist(); i++) {
//                Point branchPoint = Point.intermediatePointWithLen(parentPoint, thisPoint, info.getPointDistributionDist() * i);
//                TreeNode<Point> branchNode = new TreeNode<>(branchPoint, new ArrayList<>());
//                branchNode.setParent(lastAddedNode);
//                branchNode.addChild(currTreeNode);
//                lastAddedNode.removeChild(currTreeNode);
//                lastAddedNode.addChild(branchNode);
//                lastAddedNode = branchNode;
//
//            }
//        }
//        for (TreeNode<Point> child : currTreeNode.getChildren())
//            processTree(child);
//    }

    public void outputCurrent(String name) {
        pebbleRenderDraw(treeRoot, 0);
        SvgFileProcessor.outputSvgCommands(renderedCommands, name, info);
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
                double shiftLen = info.getPointDistributionDist() * 2.0 / ITERATION * i;
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
//                    if (newRadii > best && newRadii < treeRoot.getBoundingCircle().getRadii()) {
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
                    shiftLen = info.getPointDistributionDist() * 2.0 / ((double) ITERATION) * i;
//                    Point newPointAngle = pebble1.rotateAroundCenter(thisCenter, rotationAngle);
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
//                        if (newRadii > best && newRadii < treeRoot.getBoundingCircle().getRadii()) {
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
        List<Point> bound = info.getRegionFile().getBoundary().getPoints();
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
        Point startDrawingPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(angle));
        renderedCommands.add(new SvgPathCommand(startDrawingPoint, SvgPathCommand.CommandType.LINE_TO));
        if (DEBUG) {
            SvgFileProcessor.outputSvgCommands(renderedCommands, "test", info);
        }

        // Insert a kissing primitive instead of a pebble
        List<SvgPathCommand> scaledCommands = new ArrayList<>();

        if (decoCommands.size() != 0) {
            double scaleFactor = thisNode.getBoundingCircle().getRadii() / decoCommandsBound.getRadii();
            PatternRenderer.insertPatternToList(SvgPathCommand.commandsScaling(decoCommands, scaleFactor,
                    decoCommands.get(0).getDestinationPoint()),
                    scaledCommands, startDrawingPoint, Math.toRadians(angle));
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

    public void pebbleRenderDrawLeaf(TreeNode<Point> thisNode, double angle) {
        List<SvgPathCommand> scaledCommands = new ArrayList<>();
        if (thisNode.getChildren().size() == 0) {
            if (decoCommands.size() != 0) {
                Point zeroAnglePoint = new Point(thisNode.getData().x + thisNode.getBoundingCircle().getRadii(), thisNode.getData().y);
                Point startDrawingPoint = thisNode.getParent().getData().rotateAroundCenter(thisNode.getParent().getData(), Math.toRadians(angle));
//                    Point startDrawingPoint = thisNode.getParent().getData();
                renderedCommands.add(new SvgPathCommand(startDrawingPoint, SvgPathCommand.CommandType.LINE_TO));
                double scaleFactor = thisNode.getBoundingCircle().getRadii() / decoCommandsBound.getRadii();
                PatternRenderer.insertPatternToList(SvgPathCommand.commandsScaling(decoCommands, scaleFactor,
                        decoCommands.get(0).getDestinationPoint()),
                        scaledCommands, startDrawingPoint, Math.toRadians(angle));
                renderedCommands.addAll(scaledCommands);
            }

        } else {
            for (TreeNode<Point> child : thisNode.getChildren()) {
                double newAngle = (Math.toDegrees(Point.getAngle(thisNode.getData(), child.getData())) + 180) % 360;
                renderedCommands.add(new SvgPathCommand(thisNode.getData(), SvgPathCommand.CommandType.LINE_TO));
                pebbleRenderDrawLeaf(child, newAngle);
                renderedCommands.add(new SvgPathCommand(thisNode.getData(), SvgPathCommand.CommandType.LINE_TO));
            }
        }
    }

    public void pebbleRenderDraw(TreeNode<Point> thisNode, int angle) {
//        System.out.println("draw called");
        boolean DEBUG = false;
        boolean DRAW_BOUND = info.getDrawBound();
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
        Point startDrawingPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(angle));
        Point primitiveCentroid = thisNode.getData();
        renderedCommands.add(new SvgPathCommand(startDrawingPoint, SvgPathCommand.CommandType.LINE_TO));
        if (DEBUG) {
            SvgFileProcessor.outputSvgCommands(renderedCommands, "test", info);
        }
        // Insert a kissing primitive instead of a pebble
        List<SvgPathCommand> scaledCommands = new ArrayList<>();

        if (decoCommands.size() != 0) {
            double scaleFactor = thisNode.getBoundingCircle().getRadii() / decoCommandsBound.getRadii();
            PatternRenderer.insertPatternToList(SvgPathCommand.commandsScaling(decoCommands, scaleFactor,
                    decoCommands.get(0).getDestinationPoint()),
                    scaledCommands, startDrawingPoint, Math.toRadians(angle));
            if (DRAW_BOUND)
                renderedCommands.addAll(scaledCommands);
            printMapping(scaledCommands, primitiveCentroid);
        }


        /** Pebbles : traverse whole pebble first. this is only needed to avoid aliasing when quilting! **/
//        if (decoCommands.size() == 0)
//            for (int offset = 0; offset < 360; offset += gap) {
//                int currentAngle = (angle + offset) % 360;
//                TreeNode<Point> child;
//                Point cutPoint;
//                if (scaledCommands.size() != 0) {
//                    if (!DRAW_BOUND)
//                        cutPoint = pointOnPrimitiveWithDegreeToCenter(scaledCommands, primitiveCentroid, currentAngle); // render primitive
//                    else
//                        cutPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(currentAngle));
//                } else {
//                    cutPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(currentAngle));
//                }
//                renderedCommands.add(new SvgPathCommand(cutPoint, SvgPathCommand.CommandType.LINE_TO)); // render bubble
//            }
//        else
//            renderedCommands.addAll(scaledCommands);

        for (int offset = 0; offset < 360; offset += gap) {
            int currentAngle = (angle + offset) % 360;
            TreeNode<Point> child;
            Point cutPoint;
            if (scaledCommands.size() != 0) {
                if (!DRAW_BOUND)
                    cutPoint = pointOnPrimitiveWithDegreeToCenter(scaledCommands, primitiveCentroid, currentAngle); // render primitive
                else
                    cutPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(currentAngle));
            } else {
                cutPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(currentAngle));
            }
            renderedCommands.add(new SvgPathCommand(cutPoint, SvgPathCommand.CommandType.LINE_TO)); // render bubble
            if (DEBUG) {
                SvgFileProcessor.outputSvgCommands(renderedCommands, "test", info);
            }
            for (int j = currentAngle; j < currentAngle + gap; j++) {
                int searchAngle = j % 360;
                int newAngle = (searchAngle + 180) % 360;
                if (scaledCommands.size() != 0) {
                    if (!DRAW_BOUND)
                        cutPoint = pointOnPrimitiveWithDegreeToCenter(scaledCommands, primitiveCentroid, searchAngle); // render primitive
                    else
                        cutPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(searchAngle));
                } else {
                    cutPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(searchAngle));
                }
                if (DEBUG) {
                    SvgFileProcessor.outputSvgCommands(renderedCommands, "test", info);
                }
                if ((child = degreeTreeNodeMap.get(searchAngle)) != null) {
                    renderedCommands.add(new SvgPathCommand(cutPoint, SvgPathCommand.CommandType.LINE_TO)); // add a command to the branching point
                    pebbleRenderDraw(child, newAngle);
                    renderedCommands.add(new SvgPathCommand(cutPoint, SvgPathCommand.CommandType.LINE_TO)); // add a command to the branching point
                }
            }
        }
        Point thisPoint;
        if (scaledCommands.size() != 0) {
            if (!DRAW_BOUND)
                thisPoint = pointOnPrimitiveWithDegreeToCenter(scaledCommands, primitiveCentroid, angle); // render primitive
            else
                thisPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(angle));
        } else {
            thisPoint = zeroAnglePoint.rotateAroundCenter(thisNode.getData(), Math.toRadians(angle));
        }
        renderedCommands.add(new SvgPathCommand(thisPoint, SvgPathCommand.CommandType.LINE_TO));
        if (DEBUG) {
            SvgFileProcessor.outputSvgCommands(renderedCommands, "test", info);
        }

    }


    private void printMapping(List<SvgPathCommand> decoElment, Point center) {
        for (int i = 0; i < decoElment.size(); i++) {
            Point lastCommandPoint = decoElment.get(((i - 1) + decoElment.size()) % decoElment.size()).getDestinationPoint(),
                    thisCommandPoint = decoElment.get(i).getDestinationPoint();
            double commandToThisPointAngle = (Math.toDegrees(Point.getAngle(center, thisCommandPoint))) % 360;
            double commandToLastPointAngle = (Math.toDegrees(Point.getAngle(center, lastCommandPoint))) % 360;
//            System.out.printf("command %d has angle: %s\n", i - 1, commandToLastPointAngle);
//            System.out.printf("command %d has angle: %s\n\n", i, commandToThisPointAngle);
        }
    }

    private Point pointOnPrimitiveWithDegreeToCenter(List<SvgPathCommand> decoElment, Point center, int degree) {
        if (!(degree >= 0 && degree < 360)) {
            assert (false);
        }

        int decreasingCounter = 0;
        // sample to see which direction do commands go
        for (int i = 0; i < decoElment.size(); i++) {
            Point firstCommandPoint = decoElment.get(i % decoElment.size()).getDestinationPoint(),
                    secondCommandPoint = decoElment.get((i + 1) % decoElment.size()).getDestinationPoint();
            double firstAngle = (Math.toDegrees(Point.getAngle(center, firstCommandPoint))) % 360;
            double secondAngle = (Math.toDegrees(Point.getAngle(center, secondCommandPoint))) % 360;
            if (firstAngle > secondAngle)
                decreasingCounter++;
            else if (firstAngle < 5 && secondAngle > 355)
                decreasingCounter++;
        }
        boolean decreasingOrder = decreasingCounter > (decoElment.size() / 2);

        for (int i = 1; i < decoElment.size(); i++) {
            Point lastCommandPoint = decoElment.get(((i - 1) + decoElment.size()) % decoElment.size()).getDestinationPoint(),
                    thisCommandPoint = decoElment.get(i).getDestinationPoint();
            double commandToThisPointAngle = (Math.toDegrees(Point.getAngle(center, thisCommandPoint))) % 360;
            double commandToLastPointAngle = (Math.toDegrees(Point.getAngle(center, lastCommandPoint))) % 360;

            boolean isBetween = false;
            if (decreasingOrder) {
                if (degree <= commandToLastPointAngle && degree >= commandToThisPointAngle)
                    isBetween = true;
                else if ((Math.abs(commandToLastPointAngle - commandToThisPointAngle) > 200) && commandToThisPointAngle > commandToLastPointAngle) {
                    if ((degree <= commandToLastPointAngle && degree >= 0)
                            || (degree >= commandToThisPointAngle && degree <= 360))
                        isBetween = true;
                }

            } else {
                if (degree <= commandToThisPointAngle && degree >= commandToLastPointAngle)
                    isBetween = true;
                else if ((Math.abs(commandToLastPointAngle - commandToThisPointAngle) > 200) && commandToThisPointAngle < commandToLastPointAngle) {
                    if ((degree <= commandToThisPointAngle && degree >= 0)
                            || (degree >= commandToLastPointAngle && degree <= 360))
                        isBetween = true;
                }
            }
            if (isBetween) {
                double alphaInRadian = Math.toRadians(degree - commandToLastPointAngle),
                        thetaInRadian = Point.getAngle(lastCommandPoint, thisCommandPoint)
                                - Point.getAngle(lastCommandPoint, center),
                        betaInRadian = Math.PI - alphaInRadian - thetaInRadian;
                double movedLength = alphaInRadian / betaInRadian * Point.getDistance(lastCommandPoint, center);
                Point cutPoint = Point.intermediatePointWithLen(lastCommandPoint, thisCommandPoint, movedLength);
//                System.out.printf("degree: %d commandSelected:%d\n", degree, i - 1);
                if (Math.abs(commandToLastPointAngle - degree) < Math.abs(commandToThisPointAngle - degree))
                    return lastCommandPoint;
                else
                    return thisCommandPoint;
            }

        }
        return decoElment.get(0).getDestinationPoint();

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
//            List<Point> bound = info.getRegionFile().getBoundary().getPoints();
//            for (int i = 0; i < bound.size()-1; i++) {
//                if (Point.getDistance(bound.get(i), bound.get( (i+1) % bound.size())) > 0.1) {
//                    Point foot = Point.perpendicularFoot(child.getData(), bound.get(i), bound.get( (i+1) % bound.size() ));
//                    double dist = Point.getDistance(foot, child.getData());
//                    if (dist < minDistTobound)
//                        minDistTobound = dist;
//                }
//
//            }
            adjustedRadii = Double.min(adjustedRadii, minDistTobound);
//            double randomFactor = 0.7;
//            if (Math.random() > randomFactor ) {
//                adjustedRadii *= randomFactor;
//            }
//            if (adjustedRadii > info.getPointDistributionDist() * 0.3)
//                adjustedRadii *= Math.random() > 0.7? 0.6: 1;
            child.setBoundingCircle(new CircleBound(adjustedRadii, child.getData()));
            pebbleRenderDetermineRadii(child, determinedBounds);
        }

    }

}
