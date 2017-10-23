package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Pebble
 */
public class PebbleRenderer extends PatternRenderer {
    private List<SvgPathCommand> skeletonPathCommands, decorativeElementCommands, renderedCommands = new ArrayList<>();
    private TreeNode<Point> spanningTree;
    private PatternRenderer.RenderType renderType;
    private String patternName = "", skeletonPathName = "";

    public PebbleRenderer(TreeNode<Point> spanningTree) {
        super(spanningTree);
        this.spanningTree = spanningTree;
    }

    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }

    @Override
    public void pebbleFilling() {
        Double dist = Point.getDistance(spanningTree.getData(), spanningTree.getChildren().get(0).getData());
        for (TreeNode<Point> firstChildren : spanningTree.getChildren()) {
            if (Double.compare((Point.getDistance(spanningTree.getData(), firstChildren.getData())), dist) < 0)
                dist = Point.getDistance(spanningTree.getData(), firstChildren.getData());
        }
        dist = dist * 0.66;
        System.out.println("Command distance is" + dist);

        /* Order children tobe counterclockwise*/
        TreeTraversal.treeOrdering(spanningTree, null);

        //landFillTraverse(spanningTree, null, dist);
        renderedCommands.add(new SvgPathCommand(new Point(spanningTree.getData().x + dist, spanningTree.getData().y), SvgPathCommand.CommandType.MOVE_TO));
        HashMap<Point, Double> radiusList = new HashMap<>();
        spanningTree.setRadii(dist);

        // first determination loop, make sure each pebble touches one pebble
        pebbleRenderDetermineRadii(spanningTree, dist, radiusList);
        pebbleRenderDraw(spanningTree, 0);
        SvgFileProcessor.outputSvgCommands(renderedCommands, "beforeAdjustion");
        renderedCommands.clear();
        renderedCommands.add(new SvgPathCommand(new Point(spanningTree.getData().x + dist, spanningTree.getData().y), SvgPathCommand.CommandType.MOVE_TO));

        // second determination loop, make sure each pebble touches two pebbles
        pebbleAdjustTreenode(spanningTree, dist, radiusList);
        pebbleRenderDraw(spanningTree, 0);
        SvgFileProcessor.outputSvgCommands(renderedCommands, "afterfirstadjust");
        renderedCommands.clear();
        renderedCommands.add(new SvgPathCommand(new Point(spanningTree.getData().x + dist, spanningTree.getData().y), SvgPathCommand.CommandType.MOVE_TO));

        //
        System.out.println("First iteration of 3 touch");
        pebbleSecondAdjustTreenode(spanningTree, dist, radiusList);
        System.out.println("Second iteration of 3 touch");

        pebbleSecondAdjustTreenode(spanningTree, dist, radiusList);

        pebbleRenderDraw(spanningTree, 0);
    }

    public void pebbleSecondAdjustTreenode(TreeNode<Point> thisNode, double dist, final HashMap<Point, Double> radiusList) {
        Point c = thisNode.getData();
        double r = thisNode.getRadii();
        Point pebble1 = null, pebble2 = null, bestPoint = null;
        double r1 = 0, r2 = 0, best = r;
        int touchCount = 0;

        for (Point pCenter : radiusList.keySet())
            if (pCenter != c) {
                if (touches(c, r, pCenter, radiusList.get(pCenter))) {
                    touchCount++;
                    if (touchCount == 1) {
                        r1 = radiusList.get(pCenter);
                        pebble1 = pCenter;
                    } else if (touchCount == 2) {
                        r2 = radiusList.get(pCenter);
                        pebble2 = pCenter;
                    } else
                        break;
                }
            }
        if (touchCount == 2) {
            double angle1 = Point.getAngle(c, pebble1);
            double angle2 = Point.getAngle(c, pebble2);
            double angleBTW = angle1 - angle2;
            double rotationAngle = -1 * angleBTW / 2;
            if (angleBTW < 0)
                rotationAngle += Math.PI;
            int ITERATION = 100;
            double shiftLen;

            //first iteration, one direction
            for (int i = 0; i < ITERATION; i++) {
                shiftLen = dist / ITERATION * i;
                Point newPointAngle = new Point(pebble1.x, pebble1.y).rotateAroundCenter(c, rotationAngle);
                Point newPoint = Point.intermediatePointWithLen(c, newPointAngle, shiftLen);
                double newRadii1 = Point.getDistance(newPoint, pebble1) - r1;
                double newRadii2 = Point.getDistance(newPoint, pebble2) - r2;
                double newRadii = Math.min(newRadii1, newRadii2);

                boolean valid = (newRadii > 0) && (Math.abs(newRadii1 - newRadii2) < 0.05);
                if (valid) {
                    for (Point pCenter : radiusList.keySet()) {
                        if ((pCenter != c) && (pCenter != pebble1) && (pCenter != pebble2)) {
                            if (touches(newPoint, newRadii, pCenter, radiusList.get(pCenter))) {
                                valid = false;
                                break;
                            }
                        }
                    }
                }

                if (valid) {
                    if (newRadii > best) {
                        best = newRadii;
                        bestPoint = newPoint;
                    }
                } else {
                    break;
                }

            }
            rotationAngle = 2 * Math.PI - rotationAngle;
            //second iteration, other direction
            for (int i = 0; i < ITERATION; i++) {
                shiftLen = dist / ITERATION * i;
                Point newPointAngle = new Point(pebble1.x, pebble1.y).rotateAroundCenter(c, rotationAngle);
                Point newPoint = Point.intermediatePointWithLen(c, newPointAngle, shiftLen);
                double newRadii1 = Point.getDistance(newPoint, pebble1) - r1;
                double newRadii2 = Point.getDistance(newPoint, pebble2) - r2;
                double newRadii = Math.min(newRadii1, newRadii2);

                boolean valid = (newRadii > 0) && (Math.abs(newRadii1 - newRadii2) < 0.05);
                if (valid) {
                    for (Point pCenter : radiusList.keySet()) {
                        if ((pCenter != c) && (pCenter != pebble1) && (pCenter != pebble2)) {
                            if (touches(newPoint, newRadii, pCenter, radiusList.get(pCenter))) {
                                valid = false;
                                break;
                            }
                        }
                    }
                }

                if (valid) {
                    if (newRadii > best) {
                        best = newRadii;
                        bestPoint = newPoint;
                    }
                } else {
                    break;
                }

            }


            if (bestPoint != null) {
                System.out.println("adjusted");
                radiusList.remove(c);
                thisNode.setData(bestPoint);
                thisNode.setRadii(best);
                radiusList.put(bestPoint, best);
            }

        }

        for (TreeNode<Point> child : thisNode.getChildren()) {
            pebbleSecondAdjustTreenode(child, dist, radiusList);
        }
    }

    private boolean touches(Point center1, double radii1, Point center2, double radii2) {
        return Double.compare(Point.getDistance(center1, center2), radii1 + radii2 + 0.05) < 0;
//        return Double.compare(Math.abs(Point.getDistance(center1, center2) - (radii1 + radii2)), 0.05   ) < 0;
    }

    public void pebbleAdjustTreenode(TreeNode<Point> thisNode, double dist, final HashMap<Point, Double> radiusList) {
        if (thisNode.getChildren().size() == 0) {
            // leafnode, adjust position
            double shiftLen;
            int ITERATION = 100;
            double thisParentDist = Point.getDistance(thisNode.getParent().getData(), thisNode.getData());
            double thisRadii = thisNode.getRadii();
            double parentRadii = thisParentDist - thisRadii;
            double tempRadii;
            Point bestCenter = thisNode.getData();
            double bestRadii = thisRadii;
            boolean isValid;
            for (int i = 0; i < ITERATION; i++) {
                isValid = true;
                shiftLen = dist / 100 * i;
                Point newCenter = Point.intermediatePointWithLen(thisNode.getParent().getData(), thisNode.getData(), thisParentDist + shiftLen);
                tempRadii = thisRadii + shiftLen;
                for (Point p : radiusList.keySet()) {
                    double pRadii = radiusList.get(p);
                    boolean isSelfTest = Double.compare(Point.getDistance(p, thisNode.getData()), 0.03) < 0;
                    boolean isParentTest = Double.compare(Point.getDistance(p, thisNode.getParent().getData()), 0.03) < 0;
                    if ((!isSelfTest) && (!isParentTest) && Double.compare(Point.getDistance(p, newCenter) - (pRadii + tempRadii), -0.05) < 0) {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    if (tempRadii > bestRadii) {
                        bestRadii = tempRadii;
                        bestCenter = newCenter;
                    }

                }
            }
            radiusList.remove(thisNode.getData());
            thisNode.setRadii(bestRadii);
            thisNode.setData(bestCenter);
            radiusList.put(thisNode.getData(), thisNode.getRadii());
        }
        for (TreeNode<Point> child : thisNode.getChildren()) {
            pebbleAdjustTreenode(child, dist, radiusList);
        }
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

    public void pebbleRenderDetermineRadii(TreeNode<Point> thisNode, double dist, final HashMap<Point, Double> radiusList) {
        radiusList.put(thisNode.getData(), thisNode.getRadii());

        for (TreeNode<Point> child : thisNode.getChildren()) {

            double adjustedRadii = Point.getDistance(thisNode.getData(), child.getData()) - dist;
            // Find the minimum radii that won't cause conflict issue
            List<Point> childrenPoint = new ArrayList<>();
            for (TreeNode<Point> firstChildren : child.getChildren()) {
                double distanceBetween = Point.getDistance(child.getData(), firstChildren.getData());
                if (Double.compare(distanceBetween, 0.002) > 0 && Double.compare(distanceBetween, adjustedRadii) < 0)
                    adjustedRadii = distanceBetween;
                childrenPoint.add(firstChildren.getData());
            }

            boolean shortLineSegment = false;

            //loop through pebbles that have drawn already to adjust radii
            childrenPoint.add(child.getData());
            final TreeNode<Point> thisChild = child;
            ArrayList<Point> pointsDetermined = new ArrayList<>(radiusList.keySet());
            pointsDetermined.sort((p1, p2) -> (
                    new Double(Point.getDistance(thisChild.getData(), p1) - radiusList.get(p1)).compareTo(
                            Point.getDistance(thisChild.getData(), p2) - radiusList.get(p2))
            ));

            int adjustmentSize = (pointsDetermined.size() > 5) ? 5 : pointsDetermined.size();
            for (int i = 0; i < adjustmentSize; i++) {
                double distanceBetween = Point.getDistance(child.getData(), pointsDetermined.get(i)) - radiusList.get(pointsDetermined.get(i));
                if ((distanceBetween > 0) && Double.compare(distanceBetween, adjustedRadii) < 0 && (Double.compare(adjustedRadii - distanceBetween, 0.01) > 0)) {
                    adjustedRadii = distanceBetween;
                    assert adjustedRadii > 0;
                    shortLineSegment = true;

                }
            }
//            radiusList.put(child.getData(), adjustedRadii);

            if (shortLineSegment) {
                // Strategy 1: insert a new pebble at allshort line segments
                double distBtwChildParent = Point.getDistance(thisNode.getData(), child.getData());
                assert (distBtwChildParent - dist - adjustedRadii) > 0;
                double newRadii = (distBtwChildParent - dist - adjustedRadii) / 2.0;
                Point middlePoint = Point.intermediatePointWithLen(thisNode.getData(), child.getData(), newRadii + dist);
                if (!Point.onLine(thisNode.getData(), child.getData(), middlePoint)) {
                    System.out.println("");
                    assert newRadii + dist < distBtwChildParent;
                }
                TreeNode<Point> midTreeNode = new TreeNode<>(middlePoint, new ArrayList<>());
                midTreeNode.setParent(thisNode);
                midTreeNode.addChild(child);
                thisNode.removeChild(child);
                thisNode.addChild(midTreeNode);
                midTreeNode.setRadii(newRadii);
                pebbleRenderDetermineRadii(midTreeNode, newRadii, radiusList);
            } else {
                child.setRadii(adjustedRadii);
                pebbleRenderDetermineRadii(child, adjustedRadii, radiusList);
            }
        }

    }

}
