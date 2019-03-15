package src.jackiealgorithmicquilting;

import javafx.util.Pair;

import java.util.*;

/**
 * Created by JacquelineLi on 4/5/18.
 */
public class RectanglePacking extends PatternRenderer {
    private List<SvgPathCommand> renderedCommands = new ArrayList<>(),
            decoCommands;
    private SVGElement decoElemFile = null;
    private CircleBound decoCommandsBound = null;
    private List<Integer> touchPointIndex = null;
    private TreeNode<Point> treeRoot;
    private GenerationInfo info;
    private boolean leafRenderOnly = false;

    public RectanglePacking(List<SvgPathCommand> decoCommands, GenerationInfo info, boolean leafRenderOnly) {
        super(info.spanningTree);
        this.treeRoot = info.spanningTree;
        this.decoCommands = decoCommands;
        this.decoElemFile = info.decoElementFile;
        this.info = info;
        this.leafRenderOnly = leafRenderOnly;
        if (decoElemFile != null) {
            Pair<CircleBound, List<Integer>> decoElemBound = decoElemFile.getBoundingCircleAndTouchPointIndex();
            decoCommandsBound = decoElemBound.getKey();
            touchPointIndex = decoElemBound.getValue();
            this.decoCommands = decoElemFile.getCommandList();
        }
    }

    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }

    public void rectanglePacking() {
        double dist = Point.getDistance(treeRoot.getData(), treeRoot.getChildren().get(0).getData());
        for (TreeNode<Point> firstChildren : treeRoot.getChildren()) {
            if (Double.compare((Point.getDistance(treeRoot.getData(), firstChildren.getData())), dist) < 0)
                dist = Point.getDistance(treeRoot.getData(), firstChildren.getData());
        }
//        System.out.println("Command distance is" + dist);

        /* Order children tobe counterclockwise*/
        TreeTraversal.treeOrdering(treeRoot, null);

        //landFillTraverse(treeRoot, null, dist);
//        renderedCommands.add(new SvgPathCommand(new Point(treeRoot.getData().x + dist, treeRoot.getData().y), SvgPathCommand.CommandType.MOVE_TO));
        Set<RectangleBound> determinedBounds = new HashSet<>();
        treeRoot.setBoundingRectangle(new RectangleBound(treeRoot.getData(), dist * 0.66, dist * 0.66));
        // first determination loop, make sure each pebble collidesWith one pebble
        rectangleRandomDistributeSquare(treeRoot, dist, determinedBounds);
        rectangleRenderDetermineBound(treeRoot, dist, determinedBounds, true);
        rectangleRenderDetermineBound(treeRoot, dist, determinedBounds, false);
//        rectangleAdjustRectangle(treeRoot, dist, determinedBounds);
        determinedBounds.clear();
//        rectanglePerturbSquare(treeRoot, dist, determinedBounds);
        rectangleRenderDraw(treeRoot, 0);
    }

    public void rectangleRenderDraw(TreeNode<Point> thisNode, double angle) {
        renderedCommands.add(new SvgPathCommand(pointOnRectangleWithDegreeToCenter(thisNode.getBoundingRectangle(), angle), SvgPathCommand.CommandType.LINE_TO));
        HashMap<Integer, TreeNode<Point>> degreeTreeNodeMap = new HashMap<>();
        boolean[] degreeTable = new boolean[360];
        Arrays.fill(degreeTable, false);
        boolean isValidRectangle = (thisNode.getBoundingRectangle().getWidth() > 1.0) && (thisNode.getBoundingRectangle().getHeight() > 1.0);
        isValidRectangle = true;
        /* Record the direction of the children*/
        for (TreeNode<Point> child : thisNode.getChildren()) {
            int thisAngle = (int) Math.toDegrees(Point.getAngle(thisNode.getData(), child.getData()));
            degreeTable[thisAngle] = true;
            degreeTreeNodeMap.put(thisAngle, child);
        }

        // first octdran
        Point zeroAnglePoint = new Point(thisNode.getData().x + thisNode.getBoundingRectangle().getHalfWidth(), thisNode.getData().y);
        double firstQuadrantAngle = Math.atan2(thisNode.getBoundingRectangle().getHalfHeight(), thisNode.getBoundingRectangle().getHalfWidth());
        TreeNode<Point> child;
        Point cutPoint;
        int gap = 15;
        for (int offset = 0; offset < 360; offset += gap) {
            int currentAngle = ((int) angle + offset) % 360;
            cutPoint = pointOnRectangleWithDegreeToCenter(thisNode.getBoundingRectangle(), currentAngle);
            if (isValidRectangle)
                renderedCommands.add(new SvgPathCommand(cutPoint, SvgPathCommand.CommandType.LINE_TO));
            for (int j = currentAngle; j < currentAngle + gap; j++) {
                int searchAngle = j % 360;
                int newAngle = (searchAngle + 180) % 360;
                cutPoint = pointOnRectangleWithDegreeToCenter(thisNode.getBoundingRectangle(), searchAngle);
                if ((child = degreeTreeNodeMap.get(searchAngle)) != null || (searchAngle % 90 < 3)) {
                    if (isValidRectangle)
                        renderedCommands.add(new SvgPathCommand(cutPoint, SvgPathCommand.CommandType.LINE_TO));
                    if (child != null) {
                        rectangleRenderDraw(child, newAngle);
                        renderedCommands.add(new SvgPathCommand(cutPoint, SvgPathCommand.CommandType.LINE_TO));
                    }
                }

            }
            if (isValidRectangle)
                renderedCommands.add(new SvgPathCommand(cutPoint, SvgPathCommand.CommandType.LINE_TO));

        }
        if (isValidRectangle)
            renderedCommands.add(new SvgPathCommand(pointOnRectangleWithDegreeToCenter(thisNode.getBoundingRectangle(), angle), SvgPathCommand.CommandType.LINE_TO));

    }

    private Point pointOnRectangleWithDegreeToCenter(RectangleBound bound, double degree) {
        if (bound.getWidth() < 1.0 || bound.getHeight() < 1.0) {
            System.out.printf("width: %.2f height:%.2f\n", bound.getWidth(), bound.getHeight());
        }
        if (!(degree >= 0 && degree < 360)) {
            assert (false);

        }
        double radian = Math.toRadians(degree);
        double firstQuadrantAngleInRadian = Math.atan2(bound.getHalfHeight(), bound.getHalfWidth());
        assert (Math.toDegrees(firstQuadrantAngleInRadian) <= 90);
        if (degree < Math.toDegrees(firstQuadrantAngleInRadian))
            return new Point(bound.getCenter().x + bound.getHalfWidth(), bound.getCenter().y + Math.tan(radian) * bound.getHalfWidth());
        if (degree <= 90) {
            return new Point(bound.getCenter().x + Math.tan(Math.PI * 0.5 - firstQuadrantAngleInRadian) * bound.getHalfHeight(), bound.getCenter().y + bound.getHalfHeight());
        }

        if (degree > 90 && degree <= 180) {
            Point flipX = pointOnRectangleWithDegreeToCenter(bound, 180 - degree);
            return new Point(2 * bound.getCenter().x - flipX.x, flipX.y);
        }


        double supplementary = 360 - degree;
        assert (supplementary < 180);
        Point opposite = pointOnRectangleWithDegreeToCenter(bound, supplementary);
        return new Point(opposite.x, 2 * bound.getCenter().y - opposite.y);
    }


    public void rectangleRenderDetermineBound(TreeNode<Point> thisNode, double dist, final Set<RectangleBound> determinedBounds, boolean ignoreDetermined) {
        determinedBounds.add(thisNode.getBoundingRectangle());
        RectangleBound parentBound = thisNode.getBoundingRectangle();
        for (TreeNode<Point> child : thisNode.getChildren()) {

            RectangleBound childBound = child.getBoundingRectangle();
            if (childBound == null) {
                childBound = new RectangleBound(child.getData(), dist * 0.66, dist * 0.66);
                child.setBoundingRectangle(childBound);
            }
            if (ignoreDetermined && childBound != null && determinedBounds.contains(childBound)) {
                rectangleRenderDetermineBound(child, dist, determinedBounds, ignoreDetermined);
            } else {
                thisNode.getBoundingRectangle().modifyToTightestBound(childBound);
                boolean shortLineSegment = false;

                //loop through rectangles that have drawn already to adjust radii
                for (RectangleBound b : determinedBounds) {
                    if (b != childBound)
                        b.modifyToTightestBound(childBound);
                }

                double childParentXDist = Math.abs(parentBound.getCenter().x - childBound.getCenter().x),
                        childParentYDist = Math.abs(parentBound.getCenter().y - childBound.getCenter().y),
                        halfWidthSum = Math.abs(parentBound.getHalfWidth() + childBound.getHalfWidth()),
                        halfHeightSum = Math.abs(parentBound.getHalfHeight() + childBound.getHalfHeight());
                double gapX = childParentXDist - halfWidthSum, gapY = childParentYDist - halfHeightSum;
                gapX = (gapX < 0.0) ? 0 : gapX;
                gapY = (gapY < 0.0) ? 0 : gapY;

                boolean xDetached = Double.compare(gapX, 0.001) > 0,
                        yDetached = Double.compare(gapY, 0.01) > 0;

                childParentXDist = Math.abs(parentBound.getCenter().x - childBound.getCenter().x);
                childParentYDist = Math.abs(parentBound.getCenter().y - childBound.getCenter().y);
                halfWidthSum = Math.abs(parentBound.getHalfWidth() + childBound.getHalfWidth());
                halfHeightSum = Math.abs(parentBound.getHalfHeight() + childBound.getHalfHeight());
                gapX = childParentXDist - halfWidthSum;
                gapY = childParentYDist - halfHeightSum;
                gapX = (gapX < 0) ? 0 : gapX;
                gapY = (gapY < 0) ? 0 : gapY;

                xDetached = Double.compare(gapX, 0.01) > 0;
                yDetached = Double.compare(gapY, 0.01) > 0;
                if (xDetached || yDetached) {
                    shortLineSegment = true;
                }

                if (shortLineSegment) {
                    //insert a new rectangle
                    double newWidth = childParentXDist - halfWidthSum - 0.0001,
                            newHeight = childParentYDist - halfHeightSum - 0.0001;


                    double newX = Double.min(parentBound.getCenter().x + parentBound.getHalfWidth(),
                            childBound.getCenter().x + childBound.getHalfWidth()) + newWidth * 0.5;
                    double newY = Double.min(parentBound.getCenter().y + parentBound.getHalfHeight(),
                            childBound.getCenter().y + childBound.getHalfHeight()) + newHeight * 0.5;
                    if (yDetached) {
                        newX = (childBound.getCenter().x + parentBound.getCenter().x) * 0.5;
                        newWidth = Double.min(parentBound.getWidth(), childBound.getWidth()) * 0.5;
                    } else {
//                    System.out.println("x detached");
                        newY = (childBound.getCenter().y + parentBound.getCenter().y) * 0.5;
                        newHeight = Double.min(parentBound.getHeight(), childBound.getHeight()) * 0.5;
//                    System.out.printf("newWidth:%.2f newHeight:%.2f\n newX:%.2f newY:%.2f\n\n",newWidth, newHeight, newX, newY);

                    }

                    Point newCenter = new Point(newX, newY);
                    TreeNode<Point> midTreeNode = new TreeNode<>(newCenter, new ArrayList<>());
                    midTreeNode.setBoundingRectangle(new RectangleBound(newCenter, newWidth, newHeight));
                    determinedBounds.add(midTreeNode.getBoundingRectangle());

                    midTreeNode.setParent(thisNode);
                    midTreeNode.addChild(child);
                    thisNode.removeChild(child);
                    thisNode.addChild(midTreeNode);

                    child.setBoundingRectangle(childBound);
                    rectangleRenderDetermineBound(child, dist, determinedBounds, ignoreDetermined);
                } else {
                    child.setBoundingRectangle(childBound);
                    rectangleRenderDetermineBound(child, dist, determinedBounds, ignoreDetermined);
                }
            }


        }

    }

    public void rectangleRandomDistributeSquare(TreeNode<Point> thisNode, double dist, final Set<RectangleBound> determinedBounds) {
        double rand1 = Math.random();
        RectangleBound newBound = new RectangleBound(thisNode.getData(), dist, dist);
        if (rand1 < 0.5) {
            if (Math.random() < 0.5) {
                newBound.setWidth(dist * Math.max(0.3, Math.random() * 2));
            } else {
                newBound.setHeight(dist * Math.max(0.3, Math.random() * 2));
            }
            for (RectangleBound b : determinedBounds) {
                b.modifyToTightestBound(newBound);
            }
            determinedBounds.add(newBound);
            thisNode.setBoundingRectangle(newBound);

        }
        for (TreeNode<Point> child : thisNode.getChildren()) {
            rectangleRandomDistributeSquare(child, dist, determinedBounds);
        }
    }


}
