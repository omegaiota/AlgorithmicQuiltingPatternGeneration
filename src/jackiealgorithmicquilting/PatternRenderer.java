package src.jackiealgorithmicquilting;

import java.io.File;
import java.util.*;

/**
 */
public class PatternRenderer {
    private List<SvgPathCommand> skeletonPathCommands;
    private List<SvgPathCommand> decorativeElementCommands;
    private List<SvgPathCommand> renderedCommands = new ArrayList<>();
    private TreeNode<Point> spanningTree;
    private RenderType renderType;
    private String patternName = "", skeletonPathName = "";
    private GenerationInfo info;

    public PatternRenderer(String skeletonPathName, List<SvgPathCommand> skeletonPathCommands, RenderType type, GenerationInfo info) {
        this.skeletonPathName = skeletonPathName;
        this.skeletonPathCommands = skeletonPathCommands;
        this.renderType = type;
        this.info = info;
    }


    public PatternRenderer(List<SvgPathCommand> commands, RenderType type, GenerationInfo info) {
        this.skeletonPathCommands = commands;
        this.renderType = type;
        this.info = info;
    }

    public PatternRenderer(String skeletonPathName, List<SvgPathCommand> skeletonPathCommands, String patternName, List<SvgPathCommand> decorativeElementCommands, RenderType type, GenerationInfo info) {
        this.skeletonPathName = skeletonPathName;
        this.skeletonPathCommands = skeletonPathCommands;
        this.patternName = patternName;
        this.decorativeElementCommands = decorativeElementCommands;
        this.renderType = type;
        this.info = info;
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
     * @param insertionPoint        point to which commands need to be translated to
     * @param rotationAngleInRadian angle(specified in radian) that the transformed commands will be rotated
     * @param skipFirst
     * @param skipLast
     * @return
     */
    public static List<SvgPathCommand> translateAndRotatePattern(List<SvgPathCommand> patternCommands,
                                                                 Point insertionPoint, double rotationAngleInRadian, boolean skipFirst, boolean skipLast) {

        List<SvgPathCommand> transformedDecoElmentCommands = new ArrayList<>();
        if (patternCommands.size() == 0)
            return transformedDecoElmentCommands;
        Point firstPoint = patternCommands.get(0).getDestinationPoint();
        SvgPathCommand newCommand;
        int first = skipFirst ? 1 : 0;
        for (int j = first; j < patternCommands.size(); j++) {
            if (skipLast && j == patternCommands.size() - 1)
                continue;
            newCommand = new SvgPathCommand(patternCommands.get(j), firstPoint, insertionPoint, rotationAngleInRadian);
            if (j == 0)
                newCommand.setCommandType(SvgPathCommand.CommandType.LINE_TO);
            transformedDecoElmentCommands.add(newCommand);
        }

//        if (destination != null)
//            destination.addAll(transformedDecoElmentCommands);
        return transformedDecoElmentCommands;
    }

    public static List<SvgPathCommand> translateCommands(List<SvgPathCommand> patternCommands,
                                                                 Point newStartPoint) {
        Point shift = newStartPoint.minus(patternCommands.get(0).getDestinationPoint());
        List<SvgPathCommand> transformedDecoElmentCommands = new ArrayList<>();
        if (patternCommands.size() == 0)
            return transformedDecoElmentCommands;
        for (int j = 0; j < patternCommands.size(); j++) {
            transformedDecoElmentCommands.add(patternCommands.get(j).translateBy(shift));
        }
        return transformedDecoElmentCommands;
    }

    public static List<SvgPathCommand> rotateCommandsAround(List<SvgPathCommand> patternCommands,
                                                         Point center, double degree) {
        List<SvgPathCommand> transformedDecoElmentCommands = new ArrayList<>();
        if (patternCommands.size() == 0)
            return transformedDecoElmentCommands;
        for (int j = 0; j < patternCommands.size(); j++) {
            transformedDecoElmentCommands.add(patternCommands.get(j).rotateBy(center, degree));
        }
        return transformedDecoElmentCommands;
    }


    public static List<SvgPathCommand> interpolate(List<SvgPathCommand> original) {
        Map<Point, SvgPathCommand> destinationCommandMap = new HashMap<>();
        List<SvgPathCommand> interpolated = new ArrayList<>();
        interpolated.add(original.get(0));


        for (int i = 1; i < original.size() - 1; i++) {
            Point p = original.get(i).getDestinationPoint();
            SvgPathCommand pastRecord = destinationCommandMap.get(p);

            if (pastRecord != null) {
                interpolated.add(new SvgPathCommand(pastRecord.getControlPoint2(),
                        pastRecord.getControlPoint1(), p, SvgPathCommand.CommandType.CURVE_TO));
                continue;
            }

            Point p2 = original.get(i).getDestinationPoint(),
                    p1 = original.get(i - 1).getDestinationPoint();
            Point p3, p0;
            if (i - 2 < 0)
                p0 = p1.minus(p2.minus(p1));
            else
                p0 = original.get(i - 2).getDestinationPoint();
            if (i + 2 > original.size() - 1)
                p3 = p2.add(p2.minus(p1));
            else
                p3 = original.get(i + 1).getDestinationPoint();
            Point c1 = p2.minus(p0).divide(6).add(p1),
                    c2 = p2.minus(p3.minus(p1).divide(6));
            SvgPathCommand newCommand = new SvgPathCommand(c1, c2, p,
                    SvgPathCommand.CommandType.CURVE_TO);
            interpolated.add(newCommand);
            destinationCommandMap.put(p1, newCommand);


        }
        interpolated.add(original.get(original.size() - 1));
        return interpolated;
    }

    public void toCatmullRom() {
        renderedCommands = interpolate(skeletonPathCommands);
    }

    /**
     * Adding decorative element to treenodes on a spline tree (Alternating)
     * TODO: refactor this to combine it with non-alternating algorithm since they are largely the same
     *
     * @param decoElmentCommands deco element
     */
    public void addAlternatingDecoElmentToSplineTree(List<SvgPathCommand> decoElmentCommands, GenerationInfo info) {
        /**
         * I'm assuming that rendered commands is holding the tree skeleton after combined to splines
         */
        boolean isBranching = info.skeletonRenderType == Main.SkeletonRenderType.CATMULL_ROM;
        List<SvgPathCommand> skeletonPath = new ArrayList<>(), preprocessingVisualizaiton = new ArrayList<>(),
                collisionGeoCommands = info.collisionCommands;
        List<SvgPathCommand> reflectedDecoelmentCommands = SvgPathCommand.reflect(decoElmentCommands);
        List<SvgPathCommand> reflectedCollisionGeoCommands = SvgPathCommand.reflect(collisionGeoCommands);


        /* Spline pre-processing : break splines that are too long */
//        double adjustionComparator = info.getDecoElementFile().getHeight() * info.getDecorationSize();
        List<SvgPathCommand> splitted = new ArrayList<>();
        List<SvgPathCommand> originalPath = new ArrayList<>();
        List<TreeTraversal.NodeType> nodeType = new ArrayList<>(info.nodeType), splittedNodeType = new ArrayList<>(), beforeSplittedNodeType = new ArrayList<>(info.nodeType);
        for (TreeTraversal.NodeType t : nodeType) {
            System.out.println(t);
        }
        originalPath.addAll(renderedCommands);
        boolean adjusted = true;
        boolean isLeft = true;
        double MAX_LEN = info.decorationGap;

        while (adjusted && renderedCommands.size() > 0) {
            adjusted = false;
            splitted.add(renderedCommands.get(0));
            if (isBranching)
                splittedNodeType.add(beforeSplittedNodeType.get(0));
            for (int i = 1; i < renderedCommands.size(); i++) {
                double len;
                if (renderedCommands.get(i).getCommandType() == SvgPathCommand.CommandType.CURVE_TO) {
                    Point start = renderedCommands.get(i - 1).getDestinationPoint(),
                            c1 = renderedCommands.get(i).getControlPoint1(),
                            c2 = renderedCommands.get(i).getControlPoint2(),
                            end = renderedCommands.get(i).getDestinationPoint();
                    len = Spline.getLen(start, c1, c2, end);
                    // repeated split curve until smaller than adjustion
                    if (len > MAX_LEN) {
                        adjusted = true;
                        /**
                         * Spline is defined by destination point of i-1,  dest point of i, control points of i
                         */
                        splitted.add(Spline.splineSplittingAtHalf(start, c1, c2, end));
                        if (isBranching)
                            splittedNodeType.add(beforeSplittedNodeType.get(i) == TreeTraversal.NodeType.OUT ? TreeTraversal.NodeType.OUT : TreeTraversal.NodeType.IN);
                        // these points are in reverse order
                        SvgPathCommand secondHalf = Spline.splineSplitting(end, c2, c1, start, 0.5);
                        splitted.add(new SvgPathCommand(secondHalf.getControlPoint2(),
                                secondHalf.getControlPoint1(), end, SvgPathCommand.CommandType.CURVE_TO));
                        if (isBranching)
                            splittedNodeType.add(beforeSplittedNodeType.get(i));
                    } else {
                        splitted.add(renderedCommands.get(i));
                        if (isBranching)
                            splittedNodeType.add(beforeSplittedNodeType.get(i));
                    }
                } else {
                    splitted.add(renderedCommands.get(i));
                    if (isBranching)
                        splittedNodeType.add(beforeSplittedNodeType.get(i));
                }


            }
            renderedCommands = new ArrayList<>(splitted);
            beforeSplittedNodeType = new ArrayList<>(splittedNodeType);
            System.out.println("size" + renderedCommands.size() + " adjusted:" + adjusted);
            splitted.clear();
            splittedNodeType.clear();
        }


        skeletonPath.addAll(renderedCommands);
        preprocessingVisualizaiton.addAll(renderedCommands);

        SVGElement.outputSvgCommands(skeletonPath, "splittedSpline", info);
        nodeType = beforeSplittedNodeType;
        Map<Point, SvgPathCommand> destinationCommandMap = new HashMap<>();
        List<ConvexHullBound> decoBounds = new ArrayList<>();
        int n = skeletonPath.size();
        Map<Integer, List<SvgPathCommand>> leafNodeIndexCommandsMap = new HashMap<>();

        int lastFailed = -1;
        for (int preprocessing = 0; preprocessing < 2; preprocessing++) {
            assert (skeletonPath.size() == n);
            System.out.println("map size:" + leafNodeIndexCommandsMap.size());
            int prevInsert = -1;
            for (int i = n - 1; i >= 0; i--) {

                Point p = skeletonPath.get(i).getDestinationPoint();
                /**
                 * Check leaf node
                 */

                boolean isLeafNode = false;
                if (isBranching) {
                    if (preprocessing == 1)
                        isLeafNode = leafNodeIndexCommandsMap.containsKey(i);
                    else {
                        isLeafNode = nodeType.get(i) == TreeTraversal.NodeType.LEAF;

                    }
                }


                SvgPathCommand pastRecord = destinationCommandMap.get(p);
                if (!isLeafNode && (preprocessing != 0) && pastRecord != null) {
                    continue;
                }
                Point prevDest = skeletonPath.get(i == 0 ? 0 : i - 1).getDestinationPoint();


                if ((preprocessing == 0) && (!isLeafNode))
                    continue;

                if ((preprocessing == 1) && (isLeafNode)) {
                    System.out.println("hello from" + i + "size:" + leafNodeIndexCommandsMap.get(i).size());
                    skeletonPath.addAll(i + 1, leafNodeIndexCommandsMap.get(i));
                    preprocessingVisualizaiton.addAll(i + 1, leafNodeIndexCommandsMap.get(i));
                    continue;

                }

                if (isBranching && (preprocessing == 1) && (nodeType.get(i) == TreeTraversal.NodeType.OUT))
                    continue;

                /* Alternatve leave direction for even branches*/

                double anglePrev;
                if (isLeafNode || isLeft)
                    anglePrev = Spline.getTangentAngle(prevDest, // anglePrev is in radian
                            skeletonPath.get(i).getControlPoint1(),
                            skeletonPath.get(i).getControlPoint2(),
                            skeletonPath.get(i).getDestinationPoint());
                else
                    anglePrev = Spline.getTangentAngle(skeletonPath.get(i).getDestinationPoint(),
                            skeletonPath.get(i).getControlPoint1(),
                            skeletonPath.get(i).getControlPoint2(),
                            prevDest);


                final double INITIAL_ANGLE = Math.toRadians(info.initialAngle);
                double SIGN = 1;

                if (isLeft)
                    SIGN *= 1.0;
                else
                    SIGN *= -1.0;

                /**
                 * If alternating direction
                 */
                List<SvgPathCommand> originalCommandToUse = (isLeft) ? decoElmentCommands : reflectedDecoelmentCommands;
                List<SvgPathCommand> originalCollisionCommandToUse = (isLeft) ? collisionGeoCommands : reflectedCollisionGeoCommands;
                if (!isLeafNode)
                    anglePrev += INITIAL_ANGLE * SIGN;
                else
                    anglePrev += Math.PI * 0.5;
                Point decoToCollision = originalCollisionCommandToUse.get(0).getDestinationPoint().minus(originalCommandToUse.get(0).getDestinationPoint());

                List<SvgPathCommand> translatedRotatedCommands = rotateCommandsAround(translateCommands(originalCommandToUse, p), p, anglePrev);
                List<SvgPathCommand> translatedRotatedCollision =rotateCommandsAround(translateCommands(originalCollisionCommandToUse, p.add(decoToCollision) ), p, anglePrev);
                List<SvgPathCommand> removedFirst = new ArrayList<>(translatedRotatedCollision);
                removedFirst.remove(0);
                ConvexHullBound thisBound = ConvexHullBound.fromCommands(removedFirst);

//                List<SvgPathCommand> withCollision = new ArrayList<>(translatedRotatedCommands);
//                withCollision.addAll(translatedRotatedCollision);
//

            /* Collison Detection / Solving */
                boolean collides = collides(thisBound, decoBounds, skeletonPath, isBranching, i);
                if (!collides) {
                    boolean neighborhoodClear = true;
                    if (!isBranching) {
                        // wanderer
                        double dist = shortestDist(thisBound, decoBounds);
                        if (dist < info.decorationDensity * 0.9)
                            neighborhoodClear = false;
                    }
                    if (neighborhoodClear) {
                        decoBounds.add(thisBound);
                        isLeft = !isLeft;
                        if (preprocessing == 0)
                            leafNodeIndexCommandsMap.put(i, translatedRotatedCommands);
                        else {
                            skeletonPath.addAll(i + 1, translatedRotatedCommands);
                        }
                    }

                } else {
                    double proportion = 1.0;
                    boolean resolved = false;
                    List<SvgPathCommand> adjustedScaledCommands = new ArrayList<>(translatedRotatedCommands);
                    List<SvgPathCommand> adjustedScaledCollision = new ArrayList<>(translatedRotatedCollision);
                    final double MINIMUM_SIZE = isLeafNode ? 0.5 : 0.7;
                    if (isBranching) {
                        while (proportion > MINIMUM_SIZE && !resolved) {
                            proportion *= 0.9;
                    /* Collision Solving Strategy 1, shrink to 50% and test again */
                            for (double testAngle = info.initialAngle; testAngle < 60.0; testAngle += 10.0) {
                                // don't want to rotate leaf node
                                double radianToRotate = isLeafNode ? 0 : (Math.toRadians(testAngle) - INITIAL_ANGLE) * SIGN;
                                List<SvgPathCommand> rotated = rotateCommandsAround(adjustedScaledCommands, p, radianToRotate),
                                        rotatedCollision = rotateCommandsAround(adjustedScaledCollision, p, radianToRotate);
                                List<SvgPathCommand> removedFirstC = new ArrayList<>(rotatedCollision.subList(1, rotatedCollision.size()));
                                List<SvgPathCommand> rotatedWithCollision = new ArrayList<>(rotated);
                                rotatedWithCollision.addAll(rotatedCollision);
                                thisBound = ConvexHullBound.fromCommands(removedFirstC);
                                if (!collides(thisBound, decoBounds, originalPath, true, i)) {
                                    decoBounds.add(thisBound);
                                    if (preprocessing == 0)
                                        leafNodeIndexCommandsMap.put(i, rotated);
                                    else
                                        skeletonPath.addAll(i + 1, rotated);
                                    resolved = true;
                                    isLeft = !isLeft;
                                    break;
                                }

                                if (isLeafNode) // early out leaf nodes
                                    break;
                            }
                            adjustedScaledCommands = SvgPathCommand.commandsScaling(translatedRotatedCommands,
                                    proportion, p );
                            adjustedScaledCollision = SvgPathCommand.commandsScaling(translatedRotatedCollision,
                                    proportion, p );
                        }
                        if (preprocessing == 1) {
                            if (lastFailed != i) {
                                lastFailed = i;
                                i++;
//                                isLeft = !isLeft;
                            } else {
                                isLeft = !isLeft;
                            }
//
                        }
                    }

                }
                if (isBranching)
                    destinationCommandMap.put(p, skeletonPath.get(i));
            }

        }
        SVGElement.outputSvgCommands(preprocessingVisualizaiton, "afterPreprocessing", info);

        renderedCommands = skeletonPath;
    }

    private double shortestDist(ConvexHullBound thisBound, List<ConvexHullBound> decoBounds) {
        OptionalDouble boundsMin = decoBounds.parallelStream().mapToDouble(b -> Point.getDistance(b.getBox().getCenter(), thisBound.getBox().getCenter())).min();
        return (boundsMin.isPresent() ? boundsMin.getAsDouble() : Double.MAX_VALUE);

    }

    private boolean collides(ConvexHullBound testBound, List<ConvexHullBound> bounds, List<SvgPathCommand> skeleton,
                             boolean testLocalNeighborhood, int currentSplineIndex) {
        boolean DEBUG_OUTPUT = true;
        if (DEBUG_OUTPUT) {
            System.out.println("\nSkeleton size:" + skeleton.size());
            System.out.println("Bounds size:" + bounds.size());
            System.out.println("Region size:" + bounds.size());
        }

        // collision detection with convex hulls
        if (testBound.collidesWith(bounds)) {
            if (DEBUG_OUTPUT)
                System.out.println("collides bounds");
            return true;

        }

        if (!(info.regionFile.getBoundary().insideRegion(testBound.getBox().getCenter(), 0) &&
                info.regionFile.getBoundary().insideRegion(testBound.getBox().getUpperLeft(), 0) &&
                info.regionFile.getBoundary().insideRegion(testBound.getBox().getLowerRight(), 0) &&
                info.regionFile.getBoundary().insideRegion(testBound.getBox().getLowerLeft(), 0) &&
                info.regionFile.getBoundary().insideRegion(testBound.getBox().getUpperRight(), 0))
                ) {
            if (DEBUG_OUTPUT)
                System.out.println("Not inside region");
//            return true;
        }


        // collision detection with skeleton
        for (int i = 1; i < skeleton.size(); i++) {
            Point start = skeleton.get(i - 1).getDestinationPoint(),
                    c1 = skeleton.get(i).getControlPoint1(),
                    c2 = skeleton.get(i).getControlPoint2(),
                    end = skeleton.get(i).getDestinationPoint();
            if (Spline.collidesWith(start, c1, c2, end, testBound)) {
                if (!testLocalNeighborhood) {
                    if (Math.abs(currentSplineIndex - i) > GenerationInfo.WANDERER_NEIGHBORHOOD_COLLISION) { //collision outside neighborhood
                        if (DEBUG_OUTPUT)
                            System.out.println("Collision at skeleton:" + i + " when trying to insert at" + currentSplineIndex);
                        return true;
                    } else { //nearby spline, count collision percentage
                        if (i == currentSplineIndex && Spline.collideAt(start, c1, c2, end, testBound, 0.2)) {
                            return true;
                        } else if (i + 1 == currentSplineIndex && Spline.collideAt(start, c1, c2, end, testBound, 0.8)) {
                            return true;
                        }

                    }

                } else
                    return true;
            }
        }
        if (DEBUG_OUTPUT)
            System.out.println("Insertion success");
        return false;
    }

    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }

    public void setRenderedCommands(List<SvgPathCommand> renderedCommands) {
        this.renderedCommands = renderedCommands;
    }

    public void pebbleFilling() {

    }

    public List<SvgPathCommand> fixedWidthFilling(double width, double density) {
        System.out.println("Rendering with fixed width");
        renderedCommands.add(skeletonPathCommands.get(0));
        for (int i = 1; i < skeletonPathCommands.size() - 1; i++) {
            SvgPathCommand commandThis = skeletonPathCommands.get(i),
                    commandPrev = skeletonPathCommands.get(i - 1),
                    commandNext = skeletonPathCommands.get(i + 1);

            double angleNext = Point.getAngle(commandThis.getDestinationPoint(), commandNext.getDestinationPoint());
            double anglePrev = Point.getAngle(commandThis.getDestinationPoint(), commandPrev.getDestinationPoint());
            double betweenAngle = angleNext - anglePrev;
            System.out.println(i + ":" + angleNext + " " + anglePrev + " " + betweenAngle);
            double rotationAngle = -1.0 * betweenAngle / 2.0;
            if (betweenAngle > 0)
                rotationAngle += Math.PI;

            if (Double.compare(Math.abs(betweenAngle), 0.0001) < 0) {
                /* leafnode*/
                Point pointLeft = Point.vertOffset(commandThis.getDestinationPoint(), commandPrev.getDestinationPoint(), width);
                Point pointRight = Point.vertOffset(commandThis.getDestinationPoint(), commandPrev.getDestinationPoint(), width * (-1));
                renderedCommands.add(new SvgPathCommand(pointRight, SvgPathCommand.CommandType.LINE_TO));
                if (renderType == RenderType.WITH_DECORATION) {
                    double random = Math.random();
                    /* random factor */
                    if (Double.compare(random, density) < 1.0) {
                        List<SvgPathCommand> decoCommands = new ArrayList<>();
                        decoCommands.addAll(translateAndRotatePattern(decorativeElementCommands, commandThis.getDestinationPoint(), anglePrev, false, false));
                        boolean notCollide = decoCommands.stream().map(a -> info.regionFile.getBoundary().insideRegion(a.getDestinationPoint(), 0)).reduce((a, b) -> a && b).get();
                        if (notCollide)
                            renderedCommands.addAll(decoCommands);
                    }
                }
                renderedCommands.add(new SvgPathCommand(pointLeft, SvgPathCommand.CommandType.LINE_TO));
            } else {
            /* endpoint of the angle divider */
                Point divEnd = new Point(commandNext.getDestinationPoint()).rotateAroundCenterWrongVersion(commandThis.getDestinationPoint(), rotationAngle);
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
        int repeat = time.intValue();
        SvgPathCommand originCommand = new SvgPathCommand(skeletonPathCommands.get(0).getDestinationPoint(), SvgPathCommand.CommandType.LINE_TO);
        renderedCommands.addAll(skeletonPathCommands);
        for (int i = 1; i < repeat; i++) {
            renderedCommands.add(originCommand);
            for (int j = 1; j < skeletonPathCommands.size(); j++) {
                renderedCommands.add(new SvgPathCommand(skeletonPathCommands.get(j), originCommand.getDestinationPoint(), i * angle));
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
                renderedCommands.add(new SvgPathCommand(baseDestination, SvgPathCommand.CommandType.LINE_TO));
            else
                renderedCommands.add(new SvgPathCommand(complementBaseDestination, SvgPathCommand.CommandType.LINE_TO));
            System.out.println("Calling command with proportion:" + (1 - proportion * k));
            List<SvgPathCommand> scaled = SvgPathCommand.commandsScaling(skeletonPathCommands, 1 - proportion * k, new Point(midX, midY));
            List<SvgPathCommand> newSet = new ArrayList<>();
            if (k % 2 == 1) {
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
                renderedCommands.add(new SvgPathCommand(complementBaseDestination, SvgPathCommand.CommandType.LINE_TO));
            else
                renderedCommands.add(new SvgPathCommand(baseDestination, SvgPathCommand.CommandType.LINE_TO));
        }
        renderedCommands.add(new SvgPathCommand(center, SvgPathCommand.CommandType.LINE_TO));


        return renderedCommands;

    }


    public File outputEchoed(int number) {
        return SVGElement.outputSvgCommands(renderedCommands, skeletonPathName + "-echo-" + number, null);
    }

    public File outputRotated(Integer angle) {
        return SVGElement.outputSvgCommands(renderedCommands, skeletonPathName + "-rotation-" + angle.intValue(), null);
    }

    public enum RenderType {
        NO_DECORATION, WITH_DECORATION, ROTATION, ECHO, CATMULL_ROM
    }


}
