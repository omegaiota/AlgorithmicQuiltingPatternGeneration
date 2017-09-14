package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by JacquelineLi on 6/21/17.
 */
public class Distribution {


    private SvgFileProcessor regionFileProcessed;
    private ArrayList<Point> pointList = new ArrayList<>();
    private ArrayList<SvgPathCommand> distributionVisualizationList = new ArrayList<>();
    private ArrayList<PointRotation> pairList = new ArrayList<>();
    private RenderType type;
    private Region boundary;
    private Graph<Point> pointGraph;
    private double disLen = 0;
    private TreeNode<Point> spanningTree;
    public Distribution(RenderType type, Region boundary, double disLen, SvgFileProcessor regionFile) {
        this.type = type;
        this.boundary = boundary;
        this.disLen = disLen;
        this.regionFileProcessed = regionFile;
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
        switch (type) {
            case THREE_THREE_FOUR_THREE_FOUR:
                threeFourTessellation(start);
                break;
            case GRID:
                gridTessellation(start);
                break;
            case TRIANGLE:
                triangleTessellation(start);
                break;
            case RANDOM:
                gridTessellation(start);
                break;
        }

        System.out.println("Distribution finished");

    }

    private void triangleTessellation(Point start) {
        triangleToTriangle(start, 0, disLen);
    }

    public void threeFourTessellation(Point start) {
        squareToTriangle(start, 0, disLen);
    }

    public void gridTessellation(Point start) {
        squareToSquare(start, 0, disLen);
    }

    private boolean regionFree(Point testPoint) {
        boolean flag = true;
        for (Point point : pointList) {
            double distance = Point.getDistance(testPoint, point);
            if (Double.compare(Math.abs(distance), disLen * (type == RenderType.RANDOM ? 0.7 : 0.9)) < 0) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    private void squareToTriangle(Point bottomRight, double angle, double dist) {
        if (boundary.insideRegion(bottomRight) && regionFree(bottomRight)) {
            double newDist = dist;
            pointList.add(bottomRight);
            pairList.add(new PointRotation(bottomRight, angle));
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

            triangleToSquare(upperRight, angle + Math.PI / 2, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            triangleToSquare(upperLeft, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            triangleToSquare(bottomLeft, angle - Math.PI / 2, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

        }
    }

    private void triangleToSquare(Point bottomLeft, double angle, double dist) {
        if (boundary.insideRegion(bottomLeft) && regionFree(bottomLeft)) {
            double newDist = dist;

            pointList.add(bottomLeft);
            pairList.add(new PointRotation(bottomLeft, angle));
            Point bottomRight = new Point(bottomLeft.x + dist, bottomLeft.y).rotateAroundCenter(bottomLeft, angle);
            Point top = new Point(bottomLeft.x + (dist / 2), bottomLeft.y - dist / 2 * (Math.sqrt(3))).rotateAroundCenter(bottomLeft, angle);

            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(top, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));

            squareToTriangle(top, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.MOVE_TO));
        }
    }

    private void triangleToTriangle(Point bottomLeft, double angle, double dist) {
        if (boundary.insideRegion(bottomLeft) && regionFree(bottomLeft)) {
            double newDist = dist;

            pointList.add(bottomLeft);
            pairList.add(new PointRotation(bottomLeft, angle));
            Point bottomRight = new Point(bottomLeft.x + dist, bottomLeft.y).rotateAroundCenter(bottomLeft, angle);
            Point top = new Point(bottomLeft.x + (dist / 2), bottomLeft.y - dist / 2 * (Math.sqrt(3))).rotateAroundCenter(bottomLeft, angle);
            Point upperLeft = top.minusPoint(new Point(dist, 0));
            Point lowerLeft = new Point(bottomLeft.x - (dist / 2), bottomLeft.y + dist / 2 * (Math.sqrt(3))).rotateAroundCenter(bottomLeft, angle);

            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(top, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));

            triangleToTriangle(top, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.MOVE_TO));
            triangleToTriangle(bottomRight, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));
            triangleToTriangle(upperLeft, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(upperLeft, SvgPathCommand.CommandType.MOVE_TO));
            triangleToTriangle(lowerLeft, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(lowerLeft, SvgPathCommand.CommandType.MOVE_TO));

        }
    }

    private void squareToSquare(Point bottomRight, double angle, double dist) {
        assert (Double.compare(dist, disLen) <= 0);
        if (boundary.insideRegion(bottomRight) && regionFree(bottomRight)) {
            double newDist = disLen;
            /* Include randomness if type is RANDOM */
            if (type == RenderType.RANDOM) {
                Random rand = new Random();
                int n = rand.nextInt(2);
                double randNum = (n / 10.0) + 0.7;
                newDist *= randNum;
            }
            pointList.add(bottomRight);
            pairList.add(new PointRotation(bottomRight, angle));
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

            squareToSquare(upperRight, angle + Math.PI / 2, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            squareToSquare(upperLeft, angle, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            squareToSquare(bottomLeft, angle - Math.PI / 2, newDist);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));
        }
    }

    public void outputDistribution() {
        distributionVisualizationList.add(new SvgPathCommand(new Point(0, 0), SvgPathCommand.CommandType.MOVE_TO));
        distributionVisualizationList.addAll(regionFileProcessed.getCommandLists().get(0));
        SvgFileProcessor.outputSvgCommands(distributionVisualizationList, "distribution-" + regionFileProcessed.getfFileName() + "-" + type);
    }

    public void toRegularGraph() {
        pointGraph = new Graph<>(disLen);
        for (Point point : pointList) {
            Vertex<Point> vertex = new Vertex<>(point);
            pointGraph.addVertex(vertex);
        }

        ArrayList<Vertex<Point>> vertices = pointGraph.getVertices();
        for (int i = 0; i < vertices.size(); i++)
            for (int j = i + 1; j < vertices.size(); j++) {
                Vertex<Point> vertexI = vertices.get(i), vertexJ = vertices.get(j);
                double distance = Point.getDistance(vertexI.getData(), vertexJ.getData());

                if (type == RenderType.RANDOM) {
                    System.out.println(Math.abs(distance - disLen * 0.7) + " " + disLen * 0.3 + " "
                            + (Double.compare(Math.abs(distance - disLen * 0.7), disLen * 0.31) <= 0));
                    if (Double.compare(distance - disLen * 0.7, disLen * 0.35) <= 0) {
                        vertexI.connect(vertexJ);
                    }
                } else {
                    if (Math.abs(distance - disLen) < disLen * 0.01) {
                        vertexI.connect(vertexJ);
                    }
                }

            }
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
        THREE_THREE_FOUR_THREE_FOUR, GRID, RANDOM, TRIANGLE
    }


}
