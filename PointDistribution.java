package jackiealgorithmicquilting;

import java.util.*;

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
            case VINE:
                strategy = new TTFTF();
                break;
            case HEXAGON:
                strategy = new Hexagon();
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

    public static List<Point> poissonDiskSamples(GenerationInfo info) {
        Region boundary = info.regionFile.getBoundary();
        List<Point> points = new ArrayList<>();
        int total = 0;
        Point minPoint = info.regionFile.getMinPoint(), maxPoint = info.regionFile.getMaxPoint();

        /* Poisson Disk Sampling*/
        double area = (maxPoint.x - minPoint.x) * (maxPoint.y - minPoint.y);
//        double radius = Math.sqrt(area / NUM / 4.0);
        double radius = info.pointDistributionDist;
        System.out.println("radius:" + radius);
        System.out.println("area:" + area);
        info.setPoissonRadius(radius);
        int NUM = (int) (area / (radius * radius * 3.5));
        System.out.println("num:" + area);
        int consecutiveFail = 0;
        while (consecutiveFail < 1000) {
//        while (total < NUM) {
            double x = Math.random() * (maxPoint.x - minPoint.x) + minPoint.x,
                    y = Math.random() * (maxPoint.y - minPoint.y) + minPoint.y;
            Point tempPoint = new Point(x, y);
            if (boundary.insideRegion(tempPoint)) {
                boolean isValid = true;
                for (Point p : points) {
                    if (Point.getDistance(tempPoint, p) < radius) {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    consecutiveFail = 0;
                    points.add(tempPoint);
                    total++;
                } else
                    consecutiveFail++;
            }

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
            System.out.println(minDist + " " + minPointIndex + " " + parentIndex);
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
        if (!boundary.insideRegion(start)) {
            boolean found = false;
            while (!found) {
                int i = (int) (Math.random() * allPoints.size()), j = ((int) (Math.random() * allPoints.size()));
                Point mid = boundary.getPoints().get(i).add(boundary.getPoints().get(j)).multiply(0.5);
                if (boundary.insideRegion(mid)) {
                    start = mid;
                    found = true;
                }
            }


        }
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

    private boolean tightgrowInside(Point testP, double d) {
        for (Point p : boundary.getPoints()) {
            if (Point.getDistance(testP, p) < 0.7 * d) // hardcode for d =0.7. This func makes sure pebbles never grow outside of boundary
                return false;
        }
        return true;
    }

    private void squareToTriangle(Vertex<Point> parent, Point bottomRight, double angle, double dist, boolean consecFail) {
        List<Vertex<Point>> closePoints = regionFree(bottomRight, 0.005);
        boolean fail = false;
        if (boundary.insideRegion(bottomRight) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = parent;
            if (tightgrowInside(bottomRight, dist)) {
                newV = new Vertex<>(bottomRight);
                pointGraph.addVertex(newV);
                points.add(bottomRight);
                if (parent != null)
                    newV.connect(parent);
            } else {
                if (consecFail)
                    return;
                fail = true;
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
        if (boundary.insideRegion(bottomLeft) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = parent;
            if (tightgrowInside(bottomLeft, dist)) {
                newV = new Vertex<>(bottomLeft);
                pointGraph.addVertex(newV);
                points.add(bottomLeft);
                if (parent != null && add)
                    newV.connect(parent);
            } else {
                if (fail)
                    return;
                failure = true;
            }

            Point bottomRight = new Point(bottomLeft.x + dist, bottomLeft.y).rotateAroundCenter(bottomLeft, angle);

            Point top = new Point(bottomLeft.x + (dist / 2), bottomLeft.y - dist / 2 * (Math.sqrt(3))).rotateAroundCenter(bottomLeft, angle);

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
        if (boundary.insideRegion(bottomLeft) && ((closePoints).size() == 0)) {
            double newDist = dist;
            Vertex<Point> newV = new Vertex<>(bottomLeft);
            pointGraph.addVertex(newV);
            points.add(bottomLeft);

            if (parent != null)
                newV.connect(parent);
            Point bottomRight = new Point(bottomLeft.x + dist, bottomLeft.y).rotateAroundCenter(bottomLeft, angle);
            Point top = new Point(bottomLeft.x + (dist / 2), bottomLeft.y - dist / 2 * (Math.sqrt(3))).rotateAroundCenter(bottomLeft, angle);
            Point upperLeft = top.minus(new Point(dist, 0));
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
        SVGElement.outputSvgCommands(distributionVisualizationList, "distribution-" + regionFileProcessed.getfFileName() + "-" + type, info);
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
        THREE_THREE_FOUR_THREE_FOUR, GRID, RANDOM, TRIANGLE, ANGLE_RESTRICTED, VINE, HEXAGON
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
            if (boundary.insideRegion(center) && ((closePoints).size() == 0)) {
                double newDist = dist;
                Vertex<Point> newV = new Vertex<>(center);
                pointGraph.addVertex(newV);
                points.add(center);

                if (parent != null)
                    newV.connect(parent);

                Point rotatingPoint = (parent == null) ? new Point(center.x + disLen, center.y) : parent.getData();
                Point p1 = rotatingPoint.rotateAroundCenter(center, Math.toRadians(120)),
                        p2 = rotatingPoint.rotateAroundCenter(center, Math.toRadians(240));

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
            angleRestrictedBFS(null, start, angles, disLen, 0, true);
        }
    }


}
