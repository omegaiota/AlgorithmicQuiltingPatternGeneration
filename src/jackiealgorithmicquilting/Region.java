package src.jackiealgorithmicquilting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/19/17.
 */
public class Region {
    private List<Point> pointList = new ArrayList<>();

    public Region(List<Point> pointList) {
        this.pointList = pointList;
    }

    public List<Point> getPoints() {
        return pointList;
    }


    public boolean insideRegion(Point testPoint, double minDistToBoundary) {
        int i;
        int j;
        boolean isInside = false;
        if (pointList.size() <= 2)
            return false;
        for (i = 0, j = pointList.size() - 1; i <  pointList.size(); j = i++) {
            Point A = pointList.get(i), B = pointList.get(j);
            if ((A.y > testPoint.y) != (B.y > testPoint.y)
                    && (testPoint.x < (B.x - A.x) * (testPoint.y - A.y)
                    / (B.y - A.y) + A.x)) {
                isInside = !isInside;
            }
        }

        if (isInside) {
            return (minDist(testPoint) > minDistToBoundary);
        }
        return false;
    }

    public double minDist(Point testPoint) {
        double minDirect = pointList.stream().map(p -> Point.getDistance(p, testPoint)).reduce(5000.0, Double::min);
//        for (int i = 0; i < pointList.size(); i++) {
//            Point A  = pointList.get(i);
//            Point B = pointList.get((i + 1) % pointList.size());
//            double angleA = Math.abs(Vector2D.getAngle( new Vector2D(A, testPoint), new Vector2D(A, B)));
//            double angleB = Math.abs(Vector2D.getAngle( new Vector2D(B, testPoint), new Vector2D(B, A)));
////            if (Math.abs(Vector2D.getAngle( new Vector2D(testPoint, A), new Vector2D(testPoint, B))) > Math.PI * 0.5)
//            if (angleA  < Math.PI * 0.5 && angleB  < Math.PI * 0.5)
//                minDirect = Double.min(minDirect, Point.perpendicularDist(testPoint, A, B));
//        }
        return minDirect;
    }

    public List<SvgPathCommand> fitCommandsToRegionDelete(List<SvgPathCommand> commandsOriginal) {
        List<SvgPathCommand> commandsTrimed = new ArrayList<>();
        int start = 0;
        while ((start < commandsOriginal.size()) && (!insideRegion(commandsOriginal.get(start).getDestinationPoint(), 0)) )
            start++;
        if (start >= commandsOriginal.size())
            return commandsTrimed;
        int end = commandsOriginal.size() - 1;
        while ((end >= 0) && (!insideRegion(commandsOriginal.get(end).getDestinationPoint(), 0)))
            end--;
        int index = start;
        int outsideStartIndex = -1;

        while (index <= end) {
            while ((index <= end) && insideRegion(commandsOriginal.get(index).getDestinationPoint(), 0)) {
                commandsTrimed.add(commandsOriginal.get(index));
                index++;
            }
            /* outsideStartIndex is the index of the command that's first outside of region of the following segment */
            while ((index <= end) && (!insideRegion(commandsOriginal.get(index).getDestinationPoint(), 0)))
                index++;
            /* index is the index of the command that's first INSIDe of the region after the outside segment startin gat
            * outside start index*/
        }
        return commandsTrimed;
    }

    public List<SvgPathCommand> fitCommandsToRegionTrimToBoundary(List<SvgPathCommand> commandsOriginal, GenerationInfo info) {
        List<SvgPathCommand> commandsTrimed = new ArrayList<>();
        int start = 0;
        while ((start < commandsOriginal.size()) && (!insideRegion(commandsOriginal.get(start).getDestinationPoint(), 0)))
            start++;
        if (start >= commandsOriginal.size())
            return commandsTrimed;
        int end = commandsOriginal.size() - 1;
        while ((end >= 0) && (!insideRegion(commandsOriginal.get(end).getDestinationPoint(), 0)))
            end--;

        int index = start;
        int outsideStartIndex = -1;
        while (index <= end) {
            if (outsideStartIndex != -1) {
                Point lastIn = commandsOriginal.get(outsideStartIndex - 1).getDestinationPoint();
                Point nextIn = commandsOriginal.get(index).getDestinationPoint();
                int indexToLast = nearestBoundaryPointIndex(lastIn),
                        indexToNext = nearestBoundaryPointIndex(nextIn);

                /* First line to the nearest point on pointList*/
                Point intersectPointLast = intersectionPoint(commandsOriginal.get((outsideStartIndex - 1) % commandsOriginal.size()).getDestinationPoint(),
                        commandsOriginal.get((outsideStartIndex) % commandsOriginal.size()).getDestinationPoint());
                commandsTrimed.add(new SvgPathCommand(intersectPointLast, SvgPathCommand.CommandType.LINE_TO));
                //commandsTrimed.add(new SvgPathCommand(nearestBoundaryPoint(lastIn), SvgPathCommand.CommandType.LINE_TO));

                /* Trace the segment of the region pointList*/
//                if (indexToLast <= indexToNext) {

                boolean inOrder = indexToLast <= indexToNext;
                boolean smaller = Math.abs(indexToLast - indexToNext) <= 0.5 * pointList.size();
                if (inOrder) {
                    if (smaller) {
                        for (int i = indexToLast; i <= indexToNext; i++) {
                            commandsTrimed.add(new SvgPathCommand(pointList.get(i), SvgPathCommand.CommandType.LINE_TO));
//                        commandsTrimed.add(new SvgPathCommand(info.getRegionFile().getCommandList().get(i)));
                        }
                    } else {
                        for (int i = indexToLast; i >= 0; i--) {
                            commandsTrimed.add(new SvgPathCommand(pointList.get(i), SvgPathCommand.CommandType.LINE_TO));
//                        commandsTrimed.add(new SvgPathCommand(info.getRegionFile().getCommandList().get(i)));

                        }
                        for (int i = pointList.size() - 1; i >= indexToNext; i--) {
                            commandsTrimed.add(new SvgPathCommand(pointList.get(i), SvgPathCommand.CommandType.LINE_TO));
                        }
                    }

                } else {
                    if (smaller) {
                        for (int i = indexToLast; i >= indexToNext; i--) {
                            commandsTrimed.add(new SvgPathCommand(pointList.get(i), SvgPathCommand.CommandType.LINE_TO));
//                        commandsTrimed.add(new SvgPathCommand(info.getRegionFile().getCommandList().get(i)));
                        }
                    } else {
                        for (int i = indexToLast; i < pointList.size(); i++) {
                            commandsTrimed.add(new SvgPathCommand(pointList.get(i), SvgPathCommand.CommandType.LINE_TO));
//                        commandsTrimed.add(new SvgPathCommand(info.getRegionFile().getCommandList().get(i)));

                        }
                        for (int i = 0; i <= indexToNext; i++) {
                            commandsTrimed.add(new SvgPathCommand(pointList.get(i), SvgPathCommand.CommandType.LINE_TO));
//                        commandsTrimed.add(new SvgPathCommand(info.getRegionFile().getCommandList().get(i)));

                        }
                    }


                }

                /* Move the tracer to the nearest point on pointList*/
                Point intersectPointNext = intersectionPoint(commandsOriginal.get((index - 1) % commandsOriginal.size()).getDestinationPoint(),
                        commandsOriginal.get((index) % commandsOriginal.size()).getDestinationPoint());
                commandsTrimed.add(new SvgPathCommand(intersectPointNext, SvgPathCommand.CommandType.LINE_TO));
            }

            while ((index <= end) && insideRegion(commandsOriginal.get(index).getDestinationPoint(), 0)) {
                commandsTrimed.add(commandsOriginal.get(index));
                index++;
            }
            /* outsideStartIndex is the index of the command that's first outside of region of the following segment */
            outsideStartIndex = index;
            while ((index <= end) && (!insideRegion(commandsOriginal.get(index).getDestinationPoint(), 0)))
                index++;
            /* index is the index of the command that's first INSIDe of the region after the outside segment startin gat
            * outside start index*/
        }
        return commandsTrimed;
    }

    public List<SvgPathCommand> fitCommandsToRegionIntelligent(List<SvgPathCommand> commandsOriginal) {
        List<SvgPathCommand> commandsTrimed = new ArrayList<>();
        int start = 0;
        while ((start < commandsOriginal.size()) && (!insideRegion(commandsOriginal.get(start).getDestinationPoint(), 0)))
            start++;
        if (start >= commandsOriginal.size())
            return commandsTrimed;
        int end = commandsOriginal.size() - 1;
        while ((end >= 0) && (!insideRegion(commandsOriginal.get(end).getDestinationPoint(), 0)))
            end--;

        int index = start;
        int outsideStartIndex = -1;
        while (index <= end) {
            if (outsideStartIndex != -1) {
                Point lastIn = commandsOriginal.get(outsideStartIndex - 1).getDestinationPoint();
                Point nextIn = commandsOriginal.get(index).getDestinationPoint();
                List<SvgPathCommand> outsideCommandsPortion = new ArrayList<>();
                for (int i = outsideStartIndex - 1; i < index; i++)
                    outsideCommandsPortion.add(commandsOriginal.get(i));

                /* try to search for the object's starting point */
                int searchRange = 50;
                int searchBeginIndex = outsideStartIndex - searchRange;
                searchBeginIndex = (searchBeginIndex > 0) ? searchBeginIndex : 0;
                int searchEndIndex = index + searchRange;

                /*
                // trim to ensure segment is continuously inside the region
                for (int i = index; i < searchEndIndex; i++)
                    if (!insideRegion(commandsOriginal.get(i).getDestinationPoint())) {
                        searchEndIndex = i - 1;
                        break;
                    }

                for (int i = searchBeginIndex; i > outsideStartIndex; i--)
                    if (!insideRegion(commandsOriginal.get(i).getDestinationPoint())) {
                        searchBeginIndex = i + 1;
                        break;
                    }

                    */

                searchEndIndex = (searchEndIndex < commandsOriginal.size()) ? searchEndIndex : commandsOriginal.size() - 1;
                boolean foundObject = false;

                /* searching prev 50 and after 50 commands to see if the shape can be found*/
                for (int i = searchBeginIndex; i < outsideStartIndex; i++)
                    for (int j = index; j < searchEndIndex; j++)
                        if (!foundObject) {
                            if ((Point.getDistance(commandsOriginal.get(i).getDestinationPoint(),
                                    commandsOriginal.get(j).getDestinationPoint()) < 3.00)) {
                                foundObject = true;
                                if (insideRegion(commandsOriginal.get(i).getDestinationPoint(), 0)) {
                                    // object start point is inside the region, shrink
                                    List<SvgPathCommand> shrinkingPortion = new ArrayList<>();
                                    for (int k = i; k <= j; k++)
                                        shrinkingPortion.add(commandsOriginal.get(k));
                                    boolean shrinkingDone = false;

                                /* binary shrink*/
                                    int shrinkIteration = 0;
                                    while (!shrinkingDone && (shrinkIteration < 8)) {
                                        shrinkIteration++;
                                        shrinkingPortion = SvgPathCommand.commandsScaling(shrinkingPortion, 0.5, commandsOriginal.get(i).getDestinationPoint());
                                        shrinkingDone = true;
                                        for (int k = 0; k < shrinkingPortion.size(); k++) {
                                            if (!insideRegion(shrinkingPortion.get(k).getDestinationPoint(), 0)) {
                                                shrinkingDone = false;
                                                break;
                                            }
                                        }

                                    }
                                    if (shrinkIteration < 8) {
                                        for (int k = i; k < outsideStartIndex - 1; k++)
                                            commandsTrimed.remove(commandsTrimed.size() - 1);
                                        commandsTrimed.addAll(shrinkingPortion);

                                    }
                                    System.out.println("shrinking iteration:" + shrinkIteration);
                                    index = j;
                                } else {

                                    System.out.println("remove object: " + (j - i));
                                    // object start point is outside the region, remove
                                    for (int k = i; k < outsideStartIndex - 1; k++)
                                        commandsTrimed.remove(commandsTrimed.size() - 1);
                                    index = j;
                                }

                            }

                        }

            }

            while ((index <= end) && insideRegion(commandsOriginal.get(index).getDestinationPoint(), 0)) {
                commandsTrimed.add(commandsOriginal.get(index));
                index++;
            }

            /* outsideStartIndex is the index of the command that's first outside of region of the following segment */
            outsideStartIndex = index;
            while ((index <= end) && (!insideRegion(commandsOriginal.get(index).getDestinationPoint(), 0)))
                index++;
            /* index is the index of the command that's first INSIDe of the region after the outside segment startin gat
            * outside start index*/
        }
        return commandsTrimed;
    }

    public int nearestBoundaryPointIndex(Point inputPoint) {
        double distMin = Double.MAX_VALUE;
        int ans = -1;
        for (int i = 0; i < pointList.size(); i++ ) {
            if (Double.compare(Point.getDistance(inputPoint, pointList.get(i)), distMin) <= 0) {
                distMin = Point.getDistance(inputPoint, pointList.get(i));
                ans = i;
            }
        }
        return ans;
    }

    public Point nearestBoundaryPoint(Point inputPoint) {
        double distMin = Double.MAX_VALUE;
        Point ans = pointList.get(0);
        for (int i = 0; i < pointList.size(); i++) {
            Point otherPointOnLine = (i == pointList.size() - 1) ? pointList.get(0) : pointList.get(i + 1);
            Point perpendicularFoot = Point.perpendicularFoot(inputPoint, pointList.get(i), otherPointOnLine);
            double testDist = Point.getDistance(inputPoint, perpendicularFoot);
            if (Double.compare(testDist, distMin) <= 0) {
                ans = perpendicularFoot;
                distMin = testDist;
            }
        }
        return ans;
    }

    public Point intersectionPoint(Point srcPoint, Point destPoint) {
        for (int i = 0; i < pointList.size(); i++) {
            Point otherPointOnLine = pointList.get((i + 1) % (pointList.size()));
            if (Point.intersect(srcPoint, destPoint, pointList.get(i), otherPointOnLine)) {
                return Point.intersectionPoint(srcPoint, destPoint, pointList.get(i), otherPointOnLine);
            }
        }
        System.out.println("WARNING: couldn't find segment on region");
        return  nearestBoundaryPoint(srcPoint);
    }

    public List<SvgPathCommand> generateMedialAxis() {
        List<SvgPathCommand> medialAxis = new ArrayList<>();
        List<Point> pointList = new ArrayList<>();
        pointList.addAll(this.pointList);
        pointList.remove(this.pointList.size() - 1);
        int size = pointList.size();

        for (int i = 0; i <= Math.ceil(size / 2.0); i++ ) {
            int otherIndex = (size - i) % size;
            Point thisPoint = pointList.get(i);
            Point oppositePoint = pointList.get(otherIndex);
            Point midPoint = Point.intermediatePointWithProportion(thisPoint, oppositePoint, 0.5);

            if (insideRegion(midPoint, 0) || (otherIndex == i))
                medialAxis.add(new SvgPathCommand(midPoint, SvgPathCommand.CommandType.LINE_TO));
        }

        medialAxis.get(0).setCommandType(SvgPathCommand.CommandType.MOVE_TO);
        return medialAxis;
    }
}
