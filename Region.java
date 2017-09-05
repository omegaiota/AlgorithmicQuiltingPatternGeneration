package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/19/17.
 */
public class Region {
    private ArrayList<Point> boundary = new ArrayList<>();

    public Region(ArrayList<Point> boundary) {
        this.boundary = boundary;
    }

    public ArrayList<Point> getBoundary() {
        return boundary;
    }

    public boolean insideRegion(Point testPoint) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = boundary.size() - 1; i <  boundary.size(); j = i++) {
            if (( boundary.get(i).getY() > testPoint.getY()) != ( boundary.get(j).getY() > testPoint.getY())
                    && (testPoint.getX() < ( boundary.get(j).getX() - boundary.get(i).getX()) * (testPoint.getY() - boundary.get(i).getY())
                    / ( boundary.get(j).getY() - boundary.get(i).getY()) + boundary.get(i).getX())) {
                result = !result;
            }
        }
        return result;
    }

    public List<SvgPathCommand> fitCommandsToRegion(List<SvgPathCommand> commandsOriginal) {
        List<SvgPathCommand> commandsTrimed = new ArrayList<>();
        int start = 0;
        while ((start < commandsOriginal.size()) && (!insideRegion(commandsOriginal.get(start).getDestinationPoint())) )
            start++;
        if (start >= commandsOriginal.size())
            return commandsTrimed;
        int end = commandsOriginal.size() - 1;
        while ((end >= 0) && (!insideRegion(commandsOriginal.get(end).getDestinationPoint())))
            end--;

        int index = start;
        int outsideStartIndex = -1;
        while (index <= end) {
            if (outsideStartIndex != -1) {
                Point lastIn = commandsOriginal.get(outsideStartIndex - 1).getDestinationPoint();
                Point nextIn = commandsOriginal.get(index).getDestinationPoint();
                int indexToLast = nearestBoundaryPointIndex(lastIn),
                        indexToNext = nearestBoundaryPointIndex(nextIn);

                /* First line to the nearest point on boundary*/
                Point intersectPointLast = intersectionPoint(commandsOriginal.get((outsideStartIndex - 1) % commandsOriginal.size()).getDestinationPoint(),
                        commandsOriginal.get((outsideStartIndex) % commandsOriginal.size()).getDestinationPoint());
                commandsTrimed.add(new SvgPathCommand(intersectPointLast, SvgPathCommand.CommandType.LINE_TO));
                //commandsTrimed.add(new SvgPathCommand(nearestBoundaryPoint(lastIn), SvgPathCommand.CommandType.LINE_TO));

                /* Trace the segment of the region boundary*/
                if (indexToLast <= indexToNext) {
                    for (int i = indexToLast; i <= indexToNext; i++)
                        commandsTrimed.add(new SvgPathCommand(boundary.get(i), SvgPathCommand.CommandType.LINE_TO));
                } else {
                    for (int i = indexToLast; i < boundary.size(); i++)
                        commandsTrimed.add(new SvgPathCommand(boundary.get(i), SvgPathCommand.CommandType.LINE_TO));
                    for (int i = 0; i <= indexToNext; i++)
                        commandsTrimed.add(new SvgPathCommand(boundary.get(i), SvgPathCommand.CommandType.LINE_TO));
                }

                /* Move the tracer to the nearest point on boundary*/
                Point intersectPointNext = intersectionPoint(commandsOriginal.get((index - 1) % commandsOriginal.size()).getDestinationPoint(),
                                        commandsOriginal.get((index) % commandsOriginal.size()).getDestinationPoint());
                Point perpendicularPoint = nearestBoundaryPoint(nextIn);
                //commandsTrimed.add(new SvgPathCommand(perpendicularPoint, SvgPathCommand.CommandType.LINE_TO));
                commandsTrimed.add(new SvgPathCommand(intersectPointNext, SvgPathCommand.CommandType.LINE_TO));
            }

            while ((index <= end) && insideRegion(commandsOriginal.get(index).getDestinationPoint())) {
                commandsTrimed.add(commandsOriginal.get(index));
                index++;
            }
            /* outsideStartIndex is the index of the command that's first outside of region of the following segment */
            outsideStartIndex = index;
            while ((index <= end) && (!insideRegion(commandsOriginal.get(index).getDestinationPoint())))
                index++;
            /* index is the index of the command that's first INSIDe of the region after the outside segment startin gat
            * outside start index*/
        }
        return commandsTrimed;
    }

    public int nearestBoundaryPointIndex(Point inputPoint) {
        double distMin = Double.MAX_VALUE;
        int ans = -1;
        for (int i = 0; i < boundary.size(); i++ ) {
            if (Double.compare(Point.getDistance(inputPoint, boundary.get(i)), distMin) <= 0) {
                distMin = Point.getDistance(inputPoint, boundary.get(i));
                ans = i;
            }
        }
        return ans;
    }

    public Point nearestBoundaryPoint(Point inputPoint) {
        double distMin = Double.MAX_VALUE;
        Point ans = boundary.get(0);
        for (int i = 0; i < boundary.size(); i++) {
            Point otherPointOnLine = (i == boundary.size() - 1) ? boundary.get(0) : boundary.get(i + 1);
            Point perpendicularFoot = Point.perpendicularFoot(inputPoint, boundary.get(i), otherPointOnLine);
            double testDist = Point.getDistance(inputPoint, perpendicularFoot);
            if (Double.compare(testDist, distMin) <= 0) {
                ans = perpendicularFoot;
                distMin = testDist;
            }
        }
        return ans;
    }

    public Point intersectionPoint(Point srcPoint, Point destPoint) {
        double distMin = Double.MAX_VALUE;
        Point ans = boundary.get(0);
        for (int i = 0; i < boundary.size(); i++) {
            Point otherPointOnLine = boundary.get((i + 1) % (boundary.size()));
            if (Point.intersect(srcPoint, destPoint, boundary.get(i), otherPointOnLine)) {
                return Point.intersectionPoint(srcPoint, destPoint, boundary.get(i), otherPointOnLine);
            }
        }
        System.out.println("WARNING: couldn't find segment on region");
        return  nearestBoundaryPoint(srcPoint);
    }

    public List<SvgPathCommand> generateMedialAxis() {
        List<SvgPathCommand> medialAxis = new ArrayList<>();
        List<Point> pointList = new ArrayList<>();
        pointList.addAll(boundary);
        pointList.remove(boundary.size() - 1);
        int size = pointList.size();

        for (int i = 0; i <= Math.ceil(size / 2.0); i++ ) {
            int otherIndex = (size - i) % size;
            Point thisPoint = pointList.get(i);
            Point oppositePoint = pointList.get(otherIndex);
            Point midPoint = Point.intermediatePointWithProportion(thisPoint, oppositePoint, 0.5);

            if (insideRegion(midPoint) || (otherIndex == i))
                medialAxis.add(new SvgPathCommand(midPoint, SvgPathCommand.CommandType.LINE_TO));
        }

        medialAxis.get(0).setCommandType(SvgPathCommand.CommandType.MOVE_TO);
        return medialAxis;
    }
}
