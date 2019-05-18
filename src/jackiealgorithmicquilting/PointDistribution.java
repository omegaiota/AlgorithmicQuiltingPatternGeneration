package src.jackiealgorithmicquilting;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by JacquelineLi on 6/21/17.
 */
public final class PointDistribution {
    private Distribution strategy = new Grid();
    private SVGElement regionFileProcessed;
    private ArrayList<SvgPathCommand> distributionVisualizationList = new ArrayList<>();
    private RenderType type;
    private Region boundary;
    private Graph pointGraph;


    private double disLen = 0;
    private GenerationInfo info;
    private TreeNode<Point> spanningTree;
    private List<Point> points = new ArrayList<>();

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
            case HEXAGON:
                strategy = new Hexagon();
                break;
            case ONEROW:
                strategy = new OneRow();
                break;
            case THREE_THREE_FOUR_THREE_FOUR:
            default:
                strategy = new TTFTF();
                break;


        }
    }

    public static List<Point> poissonDiskSamples(GenerationInfo info) {
        Region boundary = info.regionFile.getBoundary();
        List<Point> points = new ArrayList<>();
        int total = 0;
        Point minPoint = info.regionFile.getMinPoint(), maxPoint = info.regionFile.getMaxPoint();

        /* Poisson Disk Sampling*/
        double area = (maxPoint.x - minPoint.x) * (maxPoint.y - minPoint.y);
//        double radius = Math.sqrt(area / NUM / 4.0);
        double radius = info.pointDistributionDist;
        double minDist = info.pointDistributionDist / 2;

        info.setPoissonRadius(radius);
        int NUM = (int) (area / (radius * radius * 3.5));
        int consecutiveFail = 0;
        RectangleBound box = RectangleBound.valueOf(boundary.getPoints());
        double gridSize = minDist * 2;
        int xTotal = (int) (box.getWidth() / gridSize);
        int yTotal = (int) (box.getHeight() / gridSize);
        int[][] counts = new int[yTotal][xTotal];
        minDist = 0;
        while (consecutiveFail < 100) {
            boolean succeeded = false;
            for (int yIndex = 0; yIndex < yTotal; yIndex++) {
                for (int xIndex = 0; xIndex < xTotal; xIndex++) {
//                    int trialTotal = (int) ((1 - counts[yIndex][xIndex] * 1.0 / points.size()) * consecutiveFail) + 5;
                    int trialTotal = 2;
                    for (int trial = 0; trial < trialTotal; trial++) {
                        double x = box.getLeft() + xIndex * gridSize;
                        double y = box.getUp() + yIndex * gridSize;
                        double x0 = Math.random() * gridSize + x,
                                y0 = Math.random() * gridSize + y;
                        Point tempPoint = new Point(x0, y0);
                        if (boundary.insideRegion(tempPoint, minDist)) {
                            boolean isValid = true;
                            for (Point p : points) {
                                if (Point.getDistance(tempPoint, p) < radius) {
                                    isValid = false;
                                    break;
                                }
                            }
                            if (isValid) {
                                counts[yIndex][xIndex]++;
                                points.add(tempPoint);
                                total++;
                                succeeded = true;
                            }
                        }
                    }
                }
            }
            if (!succeeded)
                consecutiveFail++;


        }


        return points;
    }

    public static TreeNode<Point> toMST(List<Point> points) {
        List<Point> sorted = new ArrayList<>(points);
        Collections.sort(sorted, (p1, p2) -> ((Double) (p1.x)).compareTo(p2.x));

        int n = points.size();
        double[][] dist = new double[n][n];
        List<TreeNode<Point>> nodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = Point.getDistance(sorted.get(i), sorted.get(j));
            }
            dist[i][i] = Double.MAX_VALUE;
            nodes.add(new TreeNode<>(sorted.get(i)));
        }

        TreeNode<Point> root = nodes.get(0);
        int counter = 1;
        Set<Integer> pointsIncluded = new HashSet<>();
        pointsIncluded.add(0);

        while (counter != n) {
            int minPointIndex = -1, parentIndex = -1;
            double minDist = Double.MAX_VALUE;
            for (Integer parent : pointsIncluded) {
                for (int i = 0; i < n; i++) {
                    double currDist = dist[parent][i];
                    if (currDist < minDist) {
                        minDist = currDist;
                        minPointIndex = i;
                        parentIndex = parent;
                    }
                }
            }

            nodes.get(parentIndex).addChild(nodes.get(minPointIndex));
            pointsIncluded.add(minPointIndex);
            counter++;
            for (Integer seen : pointsIncluded) {
                dist[seen][minPointIndex] = Double.MAX_VALUE;
                dist[minPointIndex][seen] = Double.MAX_VALUE;
            }


        }

        return root;

    }

    public static void remapPoint(TreeNode<Point> spanningTree, Map<Point, Point> oldNewMap) {
        if (spanningTree == null)
            return;
        if (oldNewMap.get(spanningTree.getData()) == null)
            assert false;
        spanningTree.setData(oldNewMap.get(spanningTree.getData()));
        for (TreeNode<Point> child : spanningTree.getChildren()) {
            remapPoint(child, oldNewMap);
        }
    }

    public static void writeoutToConcorderFormat(List<Point> points, String fileName) {
        PrintWriter writer = null;
        try {

            writer = new PrintWriter("./out/" + fileName + ".tsp", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        writer.println(String.format("NAME : %s\n", fileName) +
                "COMMENT : Jackie \n" +
                "TYPE : TSP\n" +
                String.format("DIMENSION : %d\n", points.size()) +
                "EDGE_WEIGHT_TYPE : EUC_2D\n" +
                "NODE_COORD_SECTION");
        for (int i = 0; i < points.size(); i++) {
            writer.println(String.format("%d %.3f %.2f", i, points.get(i).x, points.get(i).y));
        }
        writer.close();

    }

    public List<Point> getPoints() {
        return points;
    }

    private void initialization() {
        this.disLen = info.pointDistributionDist;
        this.pointGraph = new Graph(disLen);
        this.boundary = info.regionFile.getBoundary();
        this.regionFileProcessed = info.regionFile;

    }

    public void generate() {
        double midX = 0, midY = 0;
        List<Point> allPoints = boundary.getPoints();
        for (Point vertex : allPoints) {
            midX += vertex.x;
            midY += vertex.y;
        }
        midX /= boundary.getPoints().size();
        midY /= boundary.getPoints().size();
        Point start = new Point(midX, midY);
        if (!boundary.insideRegion(start, 0)) {
            boolean found = false;
            while (!found) {
                int i = (int) (Math.random() * allPoints.size()), j = ((int) (Math.random() * allPoints.size()));
                Point mid = boundary.getPoints().get(i).add(boundary.getPoints().get(j)).multiply(0.5);
                if (boundary.insideRegion(mid, 0)) {
                    start = mid;
                    found = true;
                }
            }
        }
        distributionVisualizationList.add(new SvgPathCommand(start, SvgPathCommand.CommandType.MOVE_TO));
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
            Point newPoint = zeroAnglePoint.rotateAroundCenterWrongVersion(current, currentAngle + angle);
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
            }
            if (boundary.insideRegion(current, 0) && isFree) {
                newV = new Vertex<>(current);
                pointGraph.addVertex(newV);
                if (parent != null)
                    newV.connect(parent);
                zeroAnglePoint = new Point(current.x + dist, current.y);
                for (double angle : restriction) {
                    Point newPoint = zeroAnglePoint.rotateAroundCenterWrongVersion(current, currentAngle + angle);
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

    private void squareToTriangle(Vertex<Point> parent, Point bottomRight, double angle, double dist, boolean consecFail) {
        List<Vertex<Point>> closePoints = regionFree(bottomRight, 0.005);
        if (boundary.insideRegion(bottomRight, 0) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = parent;
            newV = new Vertex<>(bottomRight);
            pointGraph.addVertex(newV);
            points.add(bottomRight);
            if (parent != null)
                newV.connect(parent);


            Point upperLeft = new Point(bottomRight.x - dist, bottomRight.y - dist);
            Point upperRight = new Point(bottomRight.x, upperLeft.y);
            Point bottomLeft = new Point(upperLeft.x, bottomRight.y);
            upperRight = upperRight.rotateAroundCenterWrongVersion(bottomRight, angle);
            bottomLeft = bottomLeft.rotateAroundCenterWrongVersion(bottomRight, angle);
            upperLeft = upperLeft.rotateAroundCenterWrongVersion(bottomRight, angle);

            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(upperLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(upperRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));

            triangleToSquare(newV, upperRight, angle + Math.PI / 2, newDist, true, true);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            triangleToSquare(newV, bottomLeft, angle - Math.PI / 2, newDist, true, true);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));

            triangleToSquare(newV, upperLeft, angle, newDist, false, true);
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.MOVE_TO));


        } else {
            for (Vertex<Point> p : closePoints)
                p.connect(parent);
        }


    }

    private void triangleToSquare(Vertex<Point> parent, Point bottomLeft, double angle, double dist, boolean add, boolean fail) {
        List<Vertex<Point>> closePoints = regionFree(bottomLeft, 0.005);
        boolean failure = false;
        if (boundary.insideRegion(bottomLeft, 0) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = parent;
            newV = new Vertex<>(bottomLeft);
            pointGraph.addVertex(newV);
            points.add(bottomLeft);
            if (parent != null && add)
                newV.connect(parent);


            Point bottomRight = new Point(bottomLeft.x + dist, bottomLeft.y).rotateAroundCenterWrongVersion(bottomLeft, angle);

            Point top = new Point(bottomLeft.x + (dist / 2), bottomLeft.y - dist / 2 * (Math.sqrt(3))).rotateAroundCenterWrongVersion(bottomLeft, angle);

            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomRight, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(top, SvgPathCommand.CommandType.LINE_TO));
            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.LINE_TO));

            squareToTriangle(newV, top, angle, newDist, failure);
//            squareToTriangle(newV, bottomRight, angle, newDist);

            distributionVisualizationList.add(new SvgPathCommand(bottomLeft, SvgPathCommand.CommandType.MOVE_TO));
        } else {
            if (add)
                for (Vertex<Point> p : closePoints)
                    p.connect(parent);
        }
    }

    private void triangleToTriangle(Vertex<Point> parent, Point bottomLeft, double angle, double dist) {
        List<Vertex<Point>> closePoints = regionFree(bottomLeft, 0.005);
        if (boundary.insideRegion(bottomLeft, 0) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = new Vertex<>(bottomLeft);
            pointGraph.addVertex(newV);
            points.add(bottomLeft);

            if (parent != null)
                newV.connect(parent);
            Point bottomRight = new Point(bottomLeft.x + dist, bottomLeft.y).rotateAroundCenterWrongVersion(bottomLeft, angle);
            Point top = new Point(bottomLeft.x + (dist / 2), bottomLeft.y - dist / 2 * (Math.sqrt(3))).rotateAroundCenterWrongVersion(bottomLeft, angle);
            Point upperLeft = top.minus(new Point(dist, 0));
            Point lowerLeft = new Point(bottomLeft.x - (dist / 2), bottomLeft.y + dist / 2 * (Math.sqrt(3))).rotateAroundCenterWrongVersion(bottomLeft, angle);

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
        if (boundary.insideRegion(bottomRight, 0) && ((closePoints).size() == 0)) {
            double newDist = disLen;
            Vertex<Point> newV = new Vertex<>(bottomRight);
            pointGraph.addVertex(newV);
            points.add(bottomRight);
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
            upperRight = upperRight.rotateAroundCenterWrongVersion(bottomRight, angle);
            bottomLeft = bottomLeft.rotateAroundCenterWrongVersion(bottomRight, angle);
            upperLeft = upperLeft.rotateAroundCenterWrongVersion(bottomRight, angle);

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
        SVGElement.outputPoints(points, info);
    }

    public List<SvgPathCommand> toTraversal() {
        toSpanningTree();
        info.spanningTree = spanningTree;
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
        return stitchPath;
    }


    public enum RenderType {
        THREE_THREE_FOUR_THREE_FOUR, GRID, RANDOM, TRIANGLE, HEXAGON, ONEROW;

        public static RenderType getDefault() {
            return THREE_THREE_FOUR_THREE_FOUR;
        }
    }

    interface Distribution {
        void generate(Point start);

    }

    final class Hexagon implements Distribution {

        @Override
        public void generate(Point start) {
            hexagon(null, start, 0, disLen);
        }

        private void hexagon(Vertex<Point> parent, Point center, double angle, double dist) {
            List<Vertex<Point>> closePoints = regionFree(center, 0.005);
            if (boundary.insideRegion(center, 0) && ((closePoints).size() == 0)) {
                double newDist = dist;
                Vertex<Point> newV = new Vertex<>(center);
                pointGraph.addVertex(newV);
                points.add(center);

                if (parent != null)
                    newV.connect(parent);

                Point rotatingPoint = (parent == null) ? new Point(center.x + disLen, center.y) : parent.getData();
                Point p1 = rotatingPoint.rotateAroundCenterWrongVersion(center, Math.toRadians(120)),
                        p2 = rotatingPoint.rotateAroundCenterWrongVersion(center, Math.toRadians(240));

                distributionVisualizationList.add(new SvgPathCommand(p1, SvgPathCommand.CommandType.LINE_TO));
                distributionVisualizationList.add(new SvgPathCommand(p2, SvgPathCommand.CommandType.LINE_TO));

                hexagon(newV, p1, angle, newDist);
                distributionVisualizationList.add(new SvgPathCommand(p1, SvgPathCommand.CommandType.MOVE_TO));
                hexagon(newV, p2, angle, newDist);
                distributionVisualizationList.add(new SvgPathCommand(p1, SvgPathCommand.CommandType.MOVE_TO));
            } else {
                for (Vertex<Point> p : closePoints)
                    p.connect(parent);
            }
        }
    }

    final class TTFTF implements Distribution {
        @Override
        public void generate(Point start) {
            squareToTriangle(null, start, 0, disLen, false);
        }
    }

    final class Grid implements Distribution {
        @Override
        public void generate(Point start) {
            squareToSquare(null, start, 0, disLen);
        }
    }

    final class OneRow implements Distribution {
        @Override
        public void generate(Point start) {
            onerow(null, start, 0, disLen);
        }

        private void onerow(Vertex<Point> parent, Point bottomRight, double angle, double dist) {
            double x = bottomRight.x;
            Point newP = new Point(x - dist, bottomRight.y);
            while (boundary.insideRegion(newP, 0)) {
                Vertex<Point> newV = new Vertex<>(newP);
                pointGraph.addVertex(newV);
                points.add(newP);
                x = x - dist;
                newP = new Point(x, bottomRight.y);
            }

            x = bottomRight.x;
            newP = new Point(x + dist, bottomRight.y);
            while (boundary.insideRegion(newP, 0)) {
                Vertex<Point> newV = new Vertex<>(newP);
                pointGraph.addVertex(newV);
                points.add(newP);
                x = x + dist;
                newP = new Point(x, bottomRight.y);
            }
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
            angleRestrictedBFS(null, start, angles, disLen, 0, true);
        }
    }


}
