package jackiesvgprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by JacquelineLi on 6/21/17.
 */
public class Distribution {
    private svgFileProcessor regionFileProcessed;
    private ArrayList<Point> pointList = new ArrayList<>();
    private ArrayList<SvgPathCommand> commandList = new ArrayList<>();
    private ArrayList<PointRotation> pairList = new ArrayList<>();
    private RenderType type;
    private Region boundary;


    private Graph<Point> pointGraph;
    private double disLen = 0;
    public enum RenderType {
        THREE_THREE_FOUR_THREE_FOUR, GRID, RANDOM
    }

    public TreeNode<Point> getSpanningTree() {
        return spanningTree;
    }

    private TreeNode<Point> spanningTree;

    public Distribution(RenderType type, Region boundary, double disLen, svgFileProcessor regionFile) {
        this.type = type;
        this.boundary = boundary;
        this.disLen = disLen;
        this.regionFileProcessed = regionFile;
    }

    public void generate() {
        double midX = 0, midY = 0;
        for (Point vertex : boundary.getBoundary()) {
            midX += vertex.getX();
            midY += vertex.getY();
        }
        midX /= boundary.getBoundary().size();
        midY /= boundary.getBoundary().size();
        Point start = new Point(midX, midY);
        commandList.add(new SvgPathCommand(start, SvgPathCommand.CommandType.MOVE_TO));
        System.out.println("starting point is inside boundary:" + boundary.insideRegion(start));
        switch (type) {
            case THREE_THREE_FOUR_THREE_FOUR:
                threeFourTessellation(start);
                break;
            case GRID:
                gridTessellation(start);
                break;
            case RANDOM:
                gridTessellation(start);
                break;
        }

        System.out.println("Distribution finished");

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
        return  flag;
    }
    private void squareToTriangle(Point bottomRight, double angle, double dist) {
        if (boundary.insideRegion(bottomRight) && regionFree(bottomRight) ) {
            double newDist = dist;
            pointList.add(bottomRight);
            pairList.add(new PointRotation(bottomRight, angle));
            Point upperLeft = new Point(bottomRight.getX() - dist, bottomRight.getY() - dist);
            Point upperRight = new Point(bottomRight.getX(), upperLeft.getY());
            Point bottomLeft = new Point(upperLeft.getX(), bottomRight.getY());
            Point.rotateAroundCenter(upperRight, bottomRight, angle);
            Point.rotateAroundCenter(bottomLeft, bottomRight, angle);
            Point.rotateAroundCenter(upperLeft, bottomRight, angle);

            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            commandList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            commandList.add(new SvgPathCommand(upperLeft, SvgPathCommand.CommandType.LINE_TO));
            commandList.add(new SvgPathCommand(upperRight, SvgPathCommand.CommandType.LINE_TO));
            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));

            triangleToSquare(upperRight,   angle + Math.PI / 2, newDist);
            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            triangleToSquare(upperLeft, angle, newDist );
            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            triangleToSquare(bottomLeft, angle - Math.PI / 2, newDist);
            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

        }
    }

    private void triangleToSquare(Point bottomLeft, double angle, double dist) {
        if (boundary.insideRegion(bottomLeft) && regionFree(bottomLeft)) {
            double newDist = dist;

            pointList.add(bottomLeft);
            pairList.add(new PointRotation(bottomLeft, angle));
            Point bottomRight = new Point(bottomLeft.getX() + dist, bottomLeft.getY());
            Point top = new Point(bottomLeft.getX() + (dist / 2), bottomLeft.getY() -  dist / 2 * (Math.sqrt(3)));
            Point.rotateAroundCenter(bottomRight, bottomLeft, angle);
            Point.rotateAroundCenter(top, bottomLeft, angle);

            commandList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
//            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.typeLineTo));
//            commandList.add(new SvgPathCommand(top, SvgPathCommand.typeLineTo));
//            commandList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.typeLineTo));

            squareToTriangle(top,  angle, newDist);
            commandList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.MOVE_TO));
        }
    }

    private void squareToSquare(Point bottomRight, double angle, double dist) {
        assert(Double.compare(dist, disLen) <= 0);
        if (boundary.insideRegion(bottomRight) && regionFree(bottomRight) ) {
            double newDist = disLen;
            /* Include randomness if type is RANDOM */
            if (type == RenderType.RANDOM) {
                Random rand = new Random();
                int  n = rand.nextInt(2);
                double randNum = (n / 10.0) + 0.7;
                newDist *= randNum;
            }
            pointList.add(bottomRight);
            pairList.add(new PointRotation(bottomRight, angle));
            Point upperLeft = new Point(bottomRight.getX() - dist, bottomRight.getY() - dist);
            Point upperRight = new Point(bottomRight.getX(), upperLeft.getY());
            Point bottomLeft = new Point(upperLeft.getX(), bottomRight.getY());
            Point.rotateAroundCenter(upperRight, bottomRight, angle);
            Point.rotateAroundCenter(bottomLeft, bottomRight, angle);
            Point.rotateAroundCenter(upperLeft, bottomRight, angle);

            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            commandList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            commandList.add(new SvgPathCommand(upperLeft, SvgPathCommand.CommandType.LINE_TO));
            commandList.add(new SvgPathCommand(upperRight, SvgPathCommand.CommandType.LINE_TO));
            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));

            squareToSquare(upperRight,   angle + Math.PI / 2, newDist);
            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            squareToSquare(upperLeft, angle, newDist);
            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            squareToSquare(bottomLeft, angle - Math.PI / 2, newDist);
            commandList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));
        }
    }

    public void outputDistribution() {
        commandList.add(new SvgPathCommand(new Point(0,0), SvgPathCommand.CommandType.MOVE_TO));
        commandList.addAll(regionFileProcessed.getCommandLists().get(0));
        svgFileProcessor.outputSvgCommands(commandList, "distribution-" + regionFileProcessed.getfFileName() + "-" + type);
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

    public List<SvgPathCommand> toTraversal() {
        toRegularGraph();
        toSpanningTree();
        List<SvgPathCommand> commands = this.traverseTree();
        return commands;
    }

    public void toSpanningTree() {
        spanningTree = pointGraph.generateSpanningTree();
    }

    public List<SvgPathCommand> traverseTree() {
        TreeTraversal renderer = new TreeTraversal(spanningTree);
        renderer.traverseTree();
        List<SvgPathCommand> stitchPath = renderer.getRenderedCommands();
      return  stitchPath;
    }

    public Graph<Point> getPointGraph() {
        return pointGraph;
    }





}
