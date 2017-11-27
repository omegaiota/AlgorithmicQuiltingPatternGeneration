package jackiequiltpatterndeterminaiton;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by JacquelineLi on 6/21/17.
 */
public final class PointDistribution {
    private Distribution strategy = new Grid();
    private SvgFileProcessor regionFileProcessed;
    private ArrayList<SvgPathCommand> distributionVisualizationList = new ArrayList<>();
    private RenderType type;
    private Region boundary;
    private Graph pointGraph;
    private double disLen = 0;
    private GenerationInfo info;
    private TreeNode<Point> spanningTree;

    public PointDistribution(RenderType type, GenerationInfo info) {
        this.type = type;
        this.info = info;
        initialization();
        switch (type) {
            case GRID:
            case RANDOM:
                strategy = new Grid();
                break;
            case TRIANGLE:
                strategy = new Triangle();
                break;
            case THREE_THREE_FOUR_THREE_FOUR:
            default:
                strategy = new TTFTF();
                break;

        }
    }

    public PointDistribution(List<Double> restrictions, GenerationInfo info) {
        this.type = RenderType.ANGLE_RESTRICTED;
        this.info = info;
        initialization();
        strategy = new AngleRestriction(restrictions);
    }

    private void initialization() {
        this.disLen = info.getPointDistributionDist();
        this.pointGraph = new Graph(disLen);
        this.boundary = info.getRegionFile().getBoundary();
        this.regionFileProcessed = info.getRegionFile();

    }

    public void generate() {
        double midX = 0, midY = 0;
        for (Point vertex : boundary.getBoundary()) {
            midX += vertex.x;
            midY += vertex.y;
        }
        midX /= boundary.getBoundary().size();
        midY /= boundary.getBoundary().size();
        Point start = new Point(midX, midY);
        distributionVisualizationList.add(new SvgPathCommand(start, SvgPathCommand.CommandType.MOVE_TO));
        System.out.println("starting point is inside boundary:" + boundary.insideRegion(start));
        strategy.generate(start);
        toTraversal();


    }

    private List<Vertex<Point>> regionFree(Point testPoint, double tolerance) {
        List<Vertex<Point>> closePoints = new ArrayList<>();
        for (Vertex<Point> v : pointGraph.getVertices()) {
            double distance = Point.getDistance(testPoint, v.getData());
            if (Double.compare(Math.abs(distance), tolerance) < 0) {
                closePoints.add(v);
            }
        }
        return closePoints;
    }

    private void angleRestrictedBFS(Vertex<Point> parent, Point current, List<Double> restriction, double dist, double currentAngle, boolean continueRecurse) {
        List<Vertex<Point>> parentList = new ArrayList<>();
        List<Point> currentList = new ArrayList<>();
        List<Double> currentAngleList = new ArrayList<>();
        Vertex<Point> newV = new Vertex<>(current);
        pointGraph.addVertex(newV);
        Point zeroAnglePoint = new Point(current.x + dist, current.y);
        for (double angle : restriction) {
            Point newPoint = zeroAnglePoint.rotateAroundCenter(current, currentAngle + angle);
            double newAngle = (angle + currentAngle + Math.PI);
            while (newAngle > Math.PI * 2)
                newAngle -= Math.PI * 2;
            while (newAngle < 0)
                newAngle += Math.PI * 2;
            parentList.add(newV);
            currentList.add(newPoint);
            currentAngleList.add(newAngle);
        }

        while (!currentList.isEmpty()) {
            assert currentAngleList.size() == currentList.size() && parentList.size() == currentList.size();
            parent = parentList.remove(0);
            current = currentList.remove(0);
            currentAngle = currentAngleList.remove(0);

            List<Vertex<Point>> closePoints = regionFree(current, dist);
            boolean isFree = (closePoints.size() == 0) || (closePoints.size() == 1 && closePoints.get(0) == parent);
            if (pointGraph.getVertices().size() < restriction.size() + 1) {
                if (parent == pointGraph.getVertices().get(0)) ;
                System.out.println("First child:" + Math.toDegrees(currentAngle) + " " + boundary.insideRegion(current) + " regionFreeSize:" + closePoints.size());
            }
            if (boundary.insideRegion(current) && isFree) {
                newV = new Vertex<>(current);
                pointGraph.addVertex(newV);
                if (parent != null)
                    newV.connect(parent);
                zeroAnglePoint = new Point(current.x + dist, current.y);
                for (double angle : restriction) {
                    Point newPoint = zeroAnglePoint.rotateAroundCenter(current, currentAngle + angle);
                    double newAngle = (angle + currentAngle + Math.PI);
                    while (newAngle > Math.PI * 2)
                        newAngle -= Math.PI * 2;
                    while (newAngle < 0)
                        newAngle += Math.PI * 2;
                    parentList.add(newV);
                    currentList.add(newPoint);
                    currentAngleList.add(newAngle);
                }

            }


        }

    }

    private void angleRestrictedDFS(Vertex<Point> parent, Point current, List<Double> restriction, double dist, double currentAngle, boolean continueRecurse) {
        List<Vertex<Point>> closePoints = regionFree(current, dist);
        boolean isFree = (closePoints.size() == 0) || (closePoints.size() == 1 && closePoints.get(0) == parent);
//        if (pointGraph.getVertices().size() != 0) {
//            if (parent == pointGraph.getVertices().get(0));
//            System.out.println("First child:" + Math.toDegrees(currentAngle) + " " + boundary.insideRegion(current) + " regionFreeSize:" + closePoints.size());
//        }
        if (boundary.insideRegion(current) && isFree) {
            double newDist = dist;
            Vertex<Point> newV = new Vertex<>(current);
            pointGraph.addVertex(newV);
            if (parent != null)
                newV.connect(parent);
            Point zeroAnglePoint = new Point(current.x + dist, current.y);
            if (continueRecurse) {
                for (double angle : restriction) {
                    Point newPoint = zeroAnglePoint.rotateAroundCenter(current, currentAngle + angle);
                    double newAngle = (angle + currentAngle + Math.PI);
                    while (newAngle > Math.PI * 2)
                        newAngle -= Math.PI * 2;
                    while (newAngle < 0)
                        newAngle += Math.PI * 2;
                    angleRestrictedDFS(newV, newPoint, restriction, dist, newAngle, continueRecurse);
                }
            }

        } else {

        }
    }

    private void squareToTriangle(Vertex<Point> parent, Point bottomRight, double angle, double dist) {
        List<Vertex<Point>> closePoints = regionFree(bottomRight, 0.005);
        if (boundary.insideRegion(bottomRight) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = new Vertex<>(bottomRight);
            pointGraph.addVertex(newV);
            if (parent != null)
                newV.connect(parent);

            Point upperLeft = new Point(bottomRight.x - dist, bottomRight.y - dist);
            Point upperRight = new Point(bottomRight.x, upperLeft.y);
            Point bottomLeft = new Point(upperLeft.x, bottomRight.y);
            upperRight = upperRight.rotateAroundCenter(bottomRight, angle);
            bottomLeft = bottomLeft.rotateAroundCenter(bottomRight, angle);
            upperLeft = upperLeft.rotateAroundCenter(bottomRight, angle);

            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(upperLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(upperRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));

            triangleToSquare(newV, upperRight, angle + Math.PI / 2, newDist, true);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            triangleToSquare(newV, bottomLeft, angle - Math.PI / 2, newDist, true);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            triangleToSquare(newV, upperLeft, angle, newDist, false);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));


        } else {
            for (Vertex<Point> p : closePoints)
                p.connect(parent);
        }


    }

    private void triangleToSquare(Vertex<Point> parent, Point bottomLeft, double angle, double dist, boolean add) {
        List<Vertex<Point>> closePoints = regionFree(bottomLeft, 0.005);

        if (boundary.insideRegion(bottomLeft) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = new Vertex<>(bottomLeft);
            pointGraph.addVertex(newV);
            if (parent != null && add)
                newV.connect(parent);
            Point bottomRight = new Point(bottomLeft.x + dist, bottomLeft.y).rotateAroundCenter(bottomLeft, angle);

            Point top = new Point(bottomLeft.x + (dist / 2), bottomLeft.y - dist / 2 * (Math.sqrt(3))).rotateAroundCenter(bottomLeft, angle);

            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(top, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));

            squareToTriangle(newV, top, angle, newDist);

            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.MOVE_TO));
        } else {
            if (add)
                for (Vertex<Point> p : closePoints)
                    p.connect(parent);
        }
    }

    private void triangleToTriangle(Vertex<Point> parent, Point bottomLeft, double angle, double dist) {
        List<Vertex<Point>> closePoints = regionFree(bottomLeft, 0.005);
        if (boundary.insideRegion(bottomLeft) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = new Vertex<>(bottomLeft);
            pointGraph.addVertex(newV);
            if (parent != null)
                newV.connect(parent);
            Point bottomRight = new Point(bottomLeft.x + dist, bottomLeft.y).rotateAroundCenter(bottomLeft, angle);
            Point top = new Point(bottomLeft.x + (dist / 2), bottomLeft.y - dist / 2 * (Math.sqrt(3))).rotateAroundCenter(bottomLeft, angle);
            Point upperLeft = top.minusPoint(new Point(dist, 0));
            Point lowerLeft = new Point(bottomLeft.x - (dist / 2), bottomLeft.y + dist / 2 * (Math.sqrt(3))).rotateAroundCenter(bottomLeft, angle);

            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(top, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));

            triangleToTriangle(newV, top, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.MOVE_TO));
            triangleToTriangle(newV, bottomRight, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));
            triangleToTriangle(newV, upperLeft, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(upperLeft, SvgPathCommand.CommandType.MOVE_TO));
            triangleToTriangle(newV, lowerLeft, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(lowerLeft, SvgPathCommand.CommandType.MOVE_TO));
        } else {
            for (Vertex<Point> p : closePoints)
                p.connect(parent);
        }
    }

    private void squareToSquare(Vertex<Point> parent, Point bottomRight, double angle, double dist) {
        assert (Double.compare(dist, disLen) <= 0);
        List<Vertex<Point>> closePoints = regionFree(bottomRight, 0.005);
        if (boundary.insideRegion(bottomRight) && ((closePoints).size() == 0)) {
            double newDist = disLen;
            Vertex<Point> newV = new Vertex<>(bottomRight);
            pointGraph.addVertex(newV);
            if (parent != null)
                newV.connect(parent);
            /* Include randomness if type is RANDOM */
            if (type == RenderType.RANDOM) {
                Random rand = new Random();
                int n = rand.nextInt(2);
                double randNum = (n / 10.0) + 0.7;
                newDist *= randNum;
            }
            Point upperLeft = new Point(bottomRight.x - dist, bottomRight.y - dist);
            Point upperRight = new Point(bottomRight.x, upperLeft.y);
            Point bottomLeft = new Point(upperLeft.x, bottomRight.y);
            upperRight = upperRight.rotateAroundCenter(bottomRight, angle);
            bottomLeft = bottomLeft.rotateAroundCenter(bottomRight, angle);
            upperLeft = upperLeft.rotateAroundCenter(bottomRight, angle);

            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(upperLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(upperRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));

            squareToSquare(newV, upperRight, angle + Math.PI / 2, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            squareToSquare(newV, upperLeft, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            squareToSquare(newV, bottomLeft, angle - Math.PI / 2, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));
        } else {
            for (Vertex<Point> p : closePoints)
                p.connect(parent);
        }
    }

    public void outputDistribution() {
        distributionVisualizationList.add(new SvgPathCommand(new Point(0, 0), SvgPathCommand.CommandType.MOVE_TO));
        distributionVisualizationList.addAll(regionFileProcessed.getCommandList());
        SvgFileProcessor.outputSvgCommands(distributionVisualizationList, "distribution-" + regionFileProcessed.getfFileName() + "-" + type);
    }


    public List<SvgPathCommand> toTraversal() {
        toSpanningTree();
        info.setSpanningTree(spanningTree);
        List<SvgPathCommand> commands = this.traverseTree();
        return commands;
    }

    public List<SvgPathCommand> toSguiggleTraversal() {
        toSpanningTree();
        info.setSpanningTree(spanningTree);
        List<SvgPathCommand> commands = this.squiggleTraverseTree();
        return commands;
    }

    private List<SvgPathCommand> squiggleTraverseTree() {
        TreeTraversal renderer = new TreeTraversal(spanningTree);
        return renderer.traverseSquiggleTree();
    }

    public void toSpanningTree() {

        spanningTree = pointGraph.generateSpanningTree();
    }

    public List<SvgPathCommand> traverseTree() {
        TreeTraversal renderer = new TreeTraversal(spanningTree);
        renderer.traverseTree();
        List<SvgPathCommand> stitchPath = renderer.getRenderedCommands();
        return stitchPath;
    }

    public enum RenderType {
        THREE_THREE_FOUR_THREE_FOUR, GRID, RANDOM, TRIANGLE, ANGLE_RESTRICTED
    }

    interface Distribution {
        void generate(Point start);

    }

    final class TTFTF implements Distribution {
        @Override
        public void generate(Point start) {
            squareToTriangle(null, start, 0, disLen);
        }
    }

    final class Grid implements Distribution {
        @Override
        public void generate(Point start) {
            squareToSquare(null, start, 0, disLen);
        }
    }

    public final class Triangle implements Distribution {
        @Override
        public void generate(Point start) {
            triangleToTriangle(null, start, 0, disLen);
        }
    }

    public final class AngleRestriction implements Distribution {
        List<Double> angles;

        public AngleRestriction(List<Double> angles) {
            this.angles = angles;
        }

        @Override
        public void generate(Point start) {
            System.out.println("Restriction size:" + angles.size());
            angleRestrictedDFS(null, start, angles, disLen, 0, true);
        }
    }


}
