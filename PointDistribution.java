package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by JacquelineLi on 6/21/17.
 */
public final class PointDistribution {
    private Distribution strategy = new Grid();
    private SvgFileProcessor regionFileProcessed;
    private ArrayList<Point> pointList = new ArrayList<>();
    private ArrayList<SvgPathCommand> distributionVisualizationList = new ArrayList<>();
    private RenderType type;
    private Region boundary;
    private Graph pointGraph;
    private double disLen = 0;
    private TreeNode<Point> spanningTree;

    public PointDistribution(RenderType type, Region boundary, double disLen, SvgFileProcessor regionFile) {
        this.type = type;
        this.boundary = boundary;
        this.disLen = disLen;
        this.regionFileProcessed = regionFile;
        this.pointGraph = new Graph(disLen);
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

    public PointDistribution(List<Double> restrictions, Region boundary, double disLen, SvgFileProcessor regionFile) {
        this.type = RenderType.ANGLE_RESTRICTED;
        this.boundary = boundary;
        this.disLen = disLen;
        this.regionFileProcessed = regionFile;
        strategy = new AngleRestriction(restrictions);
    }

    public TreeNode<Point> getSpanningTree() {
        return spanningTree;
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


    }

    private List<Vertex<Point>> regionFree(Point testPoint) {
        List<Vertex<Point>> closePoints = new ArrayList<>();
        for (Vertex<Point> v : pointGraph.getVertices()) {
            double distance = Point.getDistance(testPoint, v.getData());
            if (Double.compare(Math.abs(distance), 0.005) < 0) {
                closePoints.add(v);
            }
        }
        return closePoints;
    }

    private void angleRestricted(Vertex<Point> parrent, Point current, List<Double> restriction, double dist, double currentAngle) {
        if (boundary.insideRegion(current) && regionFree(current).size() == 0) {
            pointList.add(current);

        }
    }

    private void squareToTriangle(Vertex<Point> parent, Point bottomRight, double angle, double dist) {
        List<Vertex<Point>> closePoints = regionFree(bottomRight);
        if (boundary.insideRegion(bottomRight) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = new Vertex<>(bottomRight);
            pointGraph.addVertex(newV);
            if (parent != null)
                newV.connect(parent);

            pointList.add(bottomRight);
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

            triangleToSquare(newV, upperRight, angle + Math.PI / 2, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

//            triangleToSquare(newV,upperLeft, angle, newDist);
//            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            triangleToSquare(newV, bottomLeft, angle - Math.PI / 2, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));
        } else {
            for (Vertex<Point> p : closePoints)
                p.connect(parent);
        }
    }

    private void triangleToSquare(Vertex<Point> parent, Point bottomLeft, double angle, double dist) {
        List<Vertex<Point>> closePoints = regionFree(bottomLeft);
        if (boundary.insideRegion(bottomLeft) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = new Vertex<>(bottomLeft);
            pointGraph.addVertex(newV);
            if (parent != null)
                newV.connect(parent);
            pointList.add(bottomLeft);
            Point bottomRight = new Point(bottomLeft.x + dist, bottomLeft.y).rotateAroundCenter(bottomLeft, angle);
            Point top = new Point(bottomLeft.x + (dist / 2), bottomLeft.y - dist / 2 * (Math.sqrt(3))).rotateAroundCenter(bottomLeft, angle);

            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(top, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));

            squareToTriangle(newV, top, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.MOVE_TO));
        } else {
//            for (Vertex<Point> p : closePoints)
//                p.connect(parent);
        }
    }

    private void triangleToTriangle(Vertex<Point> parent, Point bottomLeft, double angle, double dist) {
        List<Vertex<Point>> closePoints = regionFree(bottomLeft);
        if (boundary.insideRegion(bottomLeft) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = new Vertex<>(bottomLeft);
            pointGraph.addVertex(newV);
            if (parent != null)
                newV.connect(parent);
            pointList.add(bottomLeft);
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
        List<Vertex<Point>> closePoints = regionFree(bottomRight);
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
            pointList.add(bottomRight);
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

    public void toRegularGraph() {
//        pointGraph = new Graph(disLen);
//        for (Point point : pointList) {
//            Vertex<Point> vertex = new Vertex<>(point);
//            pointGraph.addVertex(vertex);
//        }
//
//        ArrayList<Vertex<Point>> vertices = pointGraph.getVertices();
//        for (int i = 0; i < vertices.size(); i++)
//            for (int j = i + 1; j < vertices.size(); j++) {
//                Vertex<Point> vertexI = vertices.get(i), vertexJ = vertices.get(j);
//                double distance = Point.getDistance(vertexI.getData(), vertexJ.getData());
//
//                if (type == RenderType.RANDOM) {
//                    System.out.println(Math.abs(distance - disLen * 0.7) + " " + disLen * 0.3 + " "
//                            + (Double.compare(Math.abs(distance - disLen * 0.7), disLen * 0.31) <= 0));
//                    if (Double.compare(distance - disLen * 0.7, disLen * 0.35) <= 0) {
//                        vertexI.connect(vertexJ);
//                    }
//                } else {
//                    if (Math.abs(distance - disLen) < disLen * 0.01) {
//                        vertexI.connect(vertexJ);
//                    }
//                }
//
//            }
    }

    public List<SvgPathCommand> toTraversal(List<SvgPathCommand> renderedDecoCommands) {
        toRegularGraph();
        toSpanningTree();
        List<SvgPathCommand> commands = this.traverseTree(renderedDecoCommands);
        return commands;
    }

    public List<SvgPathCommand> toSguiggleTraversal(List<SvgPathCommand> renderedDecoCommands) {
        toRegularGraph();
        toSpanningTree();
        List<SvgPathCommand> commands = this.squiggleTraverseTree(renderedDecoCommands);
        return commands;
    }

    private List<SvgPathCommand> squiggleTraverseTree(List<SvgPathCommand> renderedDecoCommands) {
        TreeTraversal renderer = new TreeTraversal(spanningTree);
        return renderer.traverseSquiggleTree(renderedDecoCommands);
    }

    public void toSpanningTree() {

        spanningTree = pointGraph.generateSpanningTree();
    }

    public List<SvgPathCommand> traverseTree(List<SvgPathCommand> renderedDecoCommands) {
        TreeTraversal renderer = new TreeTraversal(spanningTree);
        renderer.traverseTree(renderedDecoCommands);
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
            angleRestricted(null, start, angles, disLen, 0);
        }
    }


}
