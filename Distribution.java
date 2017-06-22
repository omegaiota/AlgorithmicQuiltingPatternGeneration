package jackiesvgprocessor;

import java.util.ArrayList;

/**
 * Created by JacquelineLi on 6/21/17.
 */
public class Distribution {
    private fileProcessor regionFileProcessed;
    private ArrayList<Point> pointList = new ArrayList<>();
    private ArrayList<svgPathCommands> commandList = new ArrayList<>();
    private ArrayList<PointRotation> pairList = new ArrayList<>();
    private int type;
    private Region boundary;
    private double disLen = 0;
    public static final int typeTTFTFTessellation = 1;

    public Distribution(int type, Region boundary, double disLen, fileProcessor regionFile) {
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
        commandList.add(new svgPathCommands(start, svgPathCommands.typeMoveTo));
        System.out.println("starting point is inside boundary:" + boundary.insideRegion(start));
        switch (type) {
            case typeTTFTFTessellation: threeFourTesselation(start);
                                        break;
        }

        System.out.println("Distribution finished");

    }
    public void threeFourTesselation(Point start) {
        square(start, 0);
    }

    private boolean regionFree(Point testPoint) {
        boolean flag = true;
        for (Point point : pointList) {
            double distance = Point.getDistance(testPoint, point);
            System.out.println(distance);
            if (Double.compare(Math.abs(distance), disLen * 0.9) < 0) {
                System.out.println(testPoint.toString() + "point failed");
                flag = false;
                break;
            }
        }
        return  flag;
    }
    private void square(Point bottomRight, double angle) {
        System.out.println("Square:" + Point.truncateDouble(Math.toDegrees(angle), 1)  + " /" + bottomRight.toString());
        if (boundary.insideRegion(bottomRight) && regionFree(bottomRight) ) {
            pointList.add(bottomRight);
            pairList.add(new PointRotation(bottomRight, angle));
            Point upperLeft = new Point(bottomRight.getX() - disLen, bottomRight.getY() - disLen);
            Point upperRight = new Point(bottomRight.getX(), upperLeft.getY());
            Point bottomLeft = new Point(upperLeft.getX(), bottomRight.getY());
            Point.rotateAroundCenter(upperRight, bottomRight, angle);
            Point.rotateAroundCenter(bottomLeft, bottomRight, angle);
            Point.rotateAroundCenter(upperLeft, bottomRight, angle);

            commandList.add(new svgPathCommands(bottomRight, svgPathCommands.typeLineTo));
            commandList.add(new svgPathCommands(bottomLeft, svgPathCommands.typeLineTo));
            commandList.add(new svgPathCommands(upperLeft, svgPathCommands.typeLineTo));
            commandList.add(new svgPathCommands(upperRight, svgPathCommands.typeLineTo));
            commandList.add(new svgPathCommands(bottomRight, svgPathCommands.typeLineTo));

            triangle(upperRight,   angle + Math.PI / 2);
            commandList.add(new svgPathCommands(bottomRight, svgPathCommands.typeMoveTo));

            triangle(upperLeft, angle);
            commandList.add(new svgPathCommands(bottomRight, svgPathCommands.typeMoveTo));

            triangle(bottomLeft, angle - Math.PI / 2);
            commandList.add(new svgPathCommands(bottomRight, svgPathCommands.typeMoveTo));

            triangle(bottomRight,  angle - Math.PI);
            commandList.add(new svgPathCommands(bottomRight, svgPathCommands.typeMoveTo));


        }
    }

    private void triangle(Point bottomLeft, double angle) {
        System.out.println("Triangle:" + Point.truncateDouble(Math.toDegrees(angle), 1) + " /" + bottomLeft.toString());
        if (boundary.insideRegion(bottomLeft) && regionFree(bottomLeft)) {
            pointList.add(bottomLeft);
            pairList.add(new PointRotation(bottomLeft, angle));
            Point bottomRight = new Point(bottomLeft.getX() + disLen, bottomLeft.getY());
            Point top = new Point(bottomLeft.getX() + (disLen / 2), bottomLeft.getY() -  disLen / 2 * (Math.sqrt(3)));
            Point.rotateAroundCenter(bottomRight, bottomLeft, angle);
            Point.rotateAroundCenter(top, bottomLeft, angle);

            commandList.add(new svgPathCommands(bottomLeft, svgPathCommands.typeLineTo));
            commandList.add(new svgPathCommands(bottomRight, svgPathCommands.typeLineTo));
            commandList.add(new svgPathCommands(top, svgPathCommands.typeLineTo));
            commandList.add(new svgPathCommands(bottomLeft, svgPathCommands.typeLineTo));

            square(top,  angle);
            commandList.add(new svgPathCommands(bottomLeft, svgPathCommands.typeMoveTo));


        }
    }

    public void outputDistribution() {
        commandList.add(new svgPathCommands(new Point(0,0), svgPathCommands.typeMoveTo));
        commandList.addAll(regionFileProcessed.getCommandLists().get(0));

        fileProcessor.outputSvgCommands(commandList, "distribution-" + regionFileProcessed.getfFileName() + "-" + type);
    }


}
