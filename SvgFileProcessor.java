package jackiequiltpatterndeterminaiton;

import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Created by JacquelineLi on 6/12/17.
 */
public class SvgFileProcessor {
    private Path fFilePath;
    private String fFileName;
    private File fSvgFile;
    private NodeList pathNodeList;
    private Point minPoint = new Point(10000, 10000), maxPoint = new Point(-10000, -10000);
    private double width = -1, height = -1, patternHeight = -1, widthRight = -1;
    private List<SvgPathCommand> commandLists = new ArrayList<>();

    public SvgFileProcessor(File importFile) {
        this.fFilePath = Paths.get(importFile.getPath());
        this.fFileName = importFile.getName();
        this.fSvgFile = importFile;
    }

    public static File outputSvgCommands(List<SvgPathCommand> outputCommandList, String fileName, GenerationInfo info) {
        return outputSvgCommands(outputCommandList, fileName, 750, 750);

    }

    public static File outputSvgCommands(List<SvgPathCommand> outputCommandList, String fileName, int width, int height) {
        try {
            PrintWriter writer = new PrintWriter("./out/" + fileName + ".svg", "UTF-8");
            writer.println("<svg");
            writer.println("   xmlns:dc=\"http://purl.org/dc/elements/1.1/\"");
            writer.println("   xmlns:cc=\"http://creativecommons.org/ns#\"");
            writer.println("   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
            writer.println("   xmlns:svg=\"http://www.w3.org/2000/svg\"");
            writer.println("   xmlns=\"http://www.w3.org/2000/svg\"");
            writer.println("   xmlns:sodipodi=\"http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd\"");
            writer.println("   xmlns:inkscape=\"http://www.inkscape.org/namespaces/inkscape\"");
//            writer.println("   width=\"210mm\"");
//            writer.println("   height=\"210mm\">");
            writer.println(String.format("   width=\"%dpx\"", width));
            writer.println(String.format("   height=\"%dpx\">", height));
//            writer.println("   width=\"750px\"");
//            writer.println("   height=\"750px\">");
            writer.println("   <g");
            writer.println("     inkscape:label=\"Layer 1\"");
            writer.println("     inkscape:groupmode=\"layer\"");
            writer.println("     id=\"layer1\">");
            writer.println("");
            writer.println("");

            writer.println("    <path");
            writer.println("       style=\"fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1\"");
            writer.print("    d=\"");

            // First command needs to be moved to
            for (int i = 0; i < outputCommandList.size(); i++) {
                if (i == 0) {
                    writer.print(new SvgPathCommand(outputCommandList.get(0), SvgPathCommand.CommandType.MOVE_TO).toSvgCode());
                }
                SvgPathCommand command = outputCommandList.get(i);

                // Change moveTo to lineTo so that path will be one stitch
                writer.print(command.toSvgCode());


                //commented out this for generating set of 81 results
//                if ((command.getCommandType() == SvgPathCommand.CommandType.MOVE_TO) && (i != 0)) {
//                    writer.print(new SvgPathCommand(command, SvgPathCommand.CommandType.LINE_TO).toSvgCode());
//                } else
//                    writer.print(command.toSvgCode());
            }
            writer.println("\"");
            writer.println("       id=\"path3342\"\n");
            writer.println("       inkscape:connector-curvature=\"0\" />");
            writer.println("  </g>");
            writer.println("</svg>");
            writer.close();

        } catch (IOException e) {
            // do something
        }

        return new File("/Users/JacquelineLi/IdeaProjects/svgProcessor/out/" + fileName + ".svg");
    }

    public static void outputPat(List<SvgPathCommand> outputCommandList, String fileName) {
        try {
            PrintWriter writer = new PrintWriter("./out/pat" + fileName + ".pat", "UTF-8");
            int count = 0;
            for (SvgPathCommand command : outputCommandList) {

                if (command.isMoveTo() || command.isLineTo()) {
                    count++;
                    switch (command.getCommandType()) {
                        case MOVE_TO:
                            writer.println("N" + count + "G00" + "X" + command.getDestinationPoint().x + "Y" + command.getDestinationPoint().y);
                        case LINE_TO:
                            writer.println("N" + count + "G01" + "X" + command.getDestinationPoint().x + "Y" + command.getDestinationPoint().y);
                    }
                } else if (command.isCurveTo()) {
                    count++;
//                    writer.println("N" + count + "G01" + "X" + command.getDestinationPoint().x + "Y" + command.getDestinationPoint().y);
                    writer.println(String.format("N%dG05I%.4fJ%.4fP%.4fQ%.4fX%.4fY%.4f", count, command.getControlPoint1().x, command.getControlPoint1().y, command.getControlPoint2().x,
                            command.getControlPoint2().y, command.getDestinationPoint().x, command.getDestinationPoint().y));
//                    writer.println("N" + count + "G01" + "X" + command.getDestinationPoint().x + "Y" + command.getDestinationPoint().y);
                }
            }
            count++;
            writer.println("N" + count + "M02");
            writer.close();


        } catch (IOException e) {
            // do something
        }

        try {
            PrintWriter writer = new PrintWriter("./out/pat" + fileName + ".gcode", "UTF-8");
            int count = 0;
            for (SvgPathCommand command : outputCommandList) {

                if (command.isMoveTo() || command.isLineTo()) {
                    count++;
                    if (command.isMoveTo())
                        writer.println("N" + count + " G00 " + "X" + command.getDestinationPoint().x + " Y" + command.getDestinationPoint().y);
                    else
                        writer.println("N" + count + " G01 " + "X" + command.getDestinationPoint().x + " Y" + command.getDestinationPoint().y);
                } else if (command.isCurveTo()) {
                    count++;
                    writer.println("N" + count + " G01 " + "X" + command.getDestinationPoint().x + " Y" + command.getDestinationPoint().y);
                }
            }
            count++;
            writer.println("N" + count + " M02");
            writer.close();


        } catch (IOException e) {
            // do something
        }

    }

    public static Point getCentroidOnList(List<SvgPathCommand> commands) {
        Point maxPoint = commands.get(0).getDestinationPoint(), minPoint = maxPoint;
        for (SvgPathCommand c : commands) {
            Point d = c.getDestinationPoint();
            if (d.x < minPoint.x)
                minPoint = new Point(d.x, minPoint.y);
            if (d.y < minPoint.y)
                minPoint = new Point(minPoint.x, d.y);
            if (d.x > maxPoint.x)
                maxPoint = new Point(d.x, maxPoint.y);
            if (d.y > maxPoint.y)
                maxPoint = new Point(maxPoint.x, d.y);
        }

        return new Point((maxPoint.x + minPoint.x) * 0.5, (maxPoint.y + minPoint.y) * 0.5);

    }

    public Point getMinPoint() {
        return minPoint;
    }

    public Point getMaxPoint() {
        return maxPoint;
    }

    public Region getBoundary() {
        ArrayList<Point> destList = new ArrayList<>();
        for (SvgPathCommand command : commandLists) {
            destList.add(command.getDestinationPoint());
        }
        Region boundaryRegion = new Region(destList);
        return  boundaryRegion;
    }

    public void processSvg() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        System.out.println("\n processing new svg:\n");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(fSvgFile);

        String xpathExpression = "//path/@d";
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile(xpathExpression);
        pathNodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

        for (int i = 0; i < pathNodeList.getLength(); i++) {
            assert pathNodeList.getLength() == 1;
            ArrayList<SvgPathCommand> aCommandList = new ArrayList<>();
            commandLists = aCommandList;
            processPath(pathNodeList.item(i), aCommandList);
        }
        width = maxPoint.x - minPoint.x;
        height = maxPoint.y - minPoint.y;
        if (commandLists.size() > 0) {
            patternHeight = commandLists.get(0).getDestinationPoint().y - minPoint.y;
            widthRight = commandLists.get(commandLists.size() - 1).getDestinationPoint().x - getCommandList().get(0).getDestinationPoint().x;

            System.out.println(String.format("File loaded:\n maxPoint = %s minPoint = %s" +
                            "\n width = %.2f height = %.2f\n " +
                            "effective height = %.2f pattern height = %.2f\n" +
                            "first height = %.2f\n\n",
                    maxPoint, minPoint, width, height, getEffectiveHeight(), patternHeight, commandLists.get(0).getDestinationPoint().y
            ));
        }

    }

    private void processPath(Node pathNode, ArrayList<SvgPathCommand> pathCommandList) {
        String pathStr = pathNode.getNodeValue();
        String withDelimiter = "(?=[cCmMlLzZ])";
        String[] pathElemArray = pathStr.split(withDelimiter);
        Point current = new Point(0.0, 0.0);
        System.out.println("pathElemArrayLength is" + pathElemArray.length);
        for (int i = 0; i < pathElemArray.length; i++) {

            /** if the last command is close path, add a command that lineTo initial point*/
            if ((i == pathElemArray.length - 1) && (pathElemArray[i].length() > 0)
                    && (pathElemArray[i].substring(0, 1).equalsIgnoreCase("z"))) {
                System.out.println("closing path");
                SvgPathCommand initialComm = pathCommandList.get(0);
                pathCommandList.add(new SvgPathCommand(initialComm.getControlPoint1(), initialComm.getControlPoint2(),
                        initialComm.getDestinationPoint(), SvgPathCommand.CommandType.LINE_TO));
            } else
                /** else parse the next command set*/
                current = parseString(pathElemArray[i], pathCommandList, current);
        }
        //printCommandList(pathCommandList);
    }

    public Point parseString(String commandString, ArrayList<SvgPathCommand> pathCommandList, Point current) {
        String[] arguments = commandString.split(" ");
        if (arguments.length == 0 || arguments[0].length() == 0)
            return new Point();
        char commandChar = arguments[0].toLowerCase().charAt(0);
        boolean useAbsCoordinate = Character.isUpperCase(arguments[0].charAt(0));

        Point destPoint;
        Point controlPoint1;
        Point controlPoint2;

        switch (commandChar) {
            case 'm':
                destPoint = useAbsCoordinate ? new Point(arguments[1]) : new Point(current, arguments[1]);
                updateBoundWithPoint(destPoint);
                current = destPoint;
                pathCommandList.add(new SvgPathCommand(destPoint, SvgPathCommand.CommandType.MOVE_TO));

                for (int i = 2; i < arguments.length; i++) {
                    destPoint = useAbsCoordinate ? new Point(arguments[i]) : new Point(current, arguments[i]);
                    updateBoundWithPoint(destPoint);
                    current = destPoint;
                    pathCommandList.add(new SvgPathCommand(destPoint,  SvgPathCommand.CommandType.LINE_TO));

                }
                break;
            case 'l':
                destPoint = useAbsCoordinate ? new Point(arguments[1]) : new Point(current, arguments[1]);
                updateBoundWithPoint(destPoint);
                current = destPoint;
                pathCommandList.add(new SvgPathCommand(destPoint,  SvgPathCommand.CommandType.LINE_TO));

                for (int i = 2; i < arguments.length; i++) {
                    destPoint = useAbsCoordinate ? new Point(arguments[i]) : new Point(current, arguments[i]);
                    updateBoundWithPoint(destPoint);
                    current = destPoint;
                    pathCommandList.add(new SvgPathCommand(destPoint,  SvgPathCommand.CommandType.LINE_TO));
                }
                break;
            case 'c':
                for (int i = 1; i + 2 < arguments.length; i += 3) {
                    assert (i + 2 < arguments.length);
                    controlPoint1 = useAbsCoordinate ? new Point(arguments[i]) : new Point(current, arguments[i]);
                    controlPoint2 = useAbsCoordinate ? new Point(arguments[i + 1]) : new Point(current, arguments[i + 1]);
                    destPoint = useAbsCoordinate ? new Point(arguments[i + 2]) : new Point(current, arguments[i + 2]);
                    updateBoundWithPoint(destPoint);
                    pathCommandList.add(new SvgPathCommand(controlPoint1, controlPoint2, destPoint,  SvgPathCommand.CommandType.CURVE_TO));
                    current = destPoint;
                }
                break;
        }

        return current;
    }

    public void updateBoundWithPoint(Point current) {
        if (current.x < minPoint.x)
            minPoint = new Point(current.x, minPoint.y);
        if (current.y < minPoint.y)
            minPoint = new Point(minPoint.x, current.y);
        if (current.x > maxPoint.x)
            maxPoint = new Point(current.x, maxPoint.y);
        if (current.y > maxPoint.y)
            maxPoint = new Point(maxPoint.x, current.y);
    }

    public List<SvgPathCommand> getCommandList() {
        return commandLists;
    }

    public String getfFileName() {
        return fFileName;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getMaximumExtentFromStartPoint() {
        double maxDist = 0;
        for (int i = 0; i < commandLists.size(); i++)
            for (int j = i + 1; j < commandLists.size(); j++) {
                maxDist = Double.max(maxDist, Point.getDistance(commandLists.get(j).getDestinationPoint(), commandLists.get(i).getDestinationPoint()));
            }
        return maxDist * 1.1;
    }

    public Point getCentroid() {
        return new Point((getMaxPoint().x + getMinPoint().x) * 0.5, (getMaxPoint().y + getMinPoint().y) * 0.5);
    }

    public double getPatternHeight() {
        return patternHeight;
    }

    public double getWidthRight() {
        return widthRight;
    }

    public double getEffectiveHeight() {
        List<SvgPathCommand> commands = getCommandList();
        double effectiveHeight = 0;
        for (int i = 1; i < commands.size(); i++)
            for (int j = i + 1; j < commands.size(); j++) {
                Point A = commands.get(i - 1).getDestinationPoint();
                Point B = commands.get(i).getDestinationPoint();
                Point C = commands.get(j - 1).getDestinationPoint();
                Point D = commands.get(j).getDestinationPoint();
                Point temp;
                /* Standardize vector direction from leftToRight*/
                if (A.x > B.x) {
                    temp = A;
                    A = B;
                    B = temp;
                }
                if (C.x > D.x) {
                    temp = C;
                    C = D;
                    D = temp;
                }
                double lengthAB = Point.getDistance(A, B),
                        lengthCD = Point.getDistance(C, D);
                double xLeft = 0, xRight = 0;
                if (xIsBetween(A, C, D) || xIsBetween(B, C, D) || xIsBetween(C, A, B) || xIsBetween(D, A, B)) {
                    Point leftAB, rightAB, leftCD, rightCD;
                    if (A.x > C.x) {
                        xLeft = A.x;
                        leftAB = A;
                        leftCD = Point.interMediatePointWithX(C, D, xLeft);
                    } else {
                        xLeft = C.x;
                        leftCD = C;
                        leftAB = Point.interMediatePointWithX(A, B, xLeft);
                    }

                    if (B.x < D.x) {
                        xRight = B.x;
                        rightAB = B;
                        rightCD = Point.interMediatePointWithX(C, D, xRight);
                    } else {
                        xRight = D.x;
                        rightAB = Point.interMediatePointWithX(A, B, xRight);
                        rightCD = D;
                    }

                    effectiveHeight = Double.max(effectiveHeight,
                            Double.max(Point.getDistance(leftAB, leftCD),
                                    Point.getDistance(rightAB, rightCD)));
                }

            }
        return effectiveHeight;
    }

    /** return true if the xPosition of A is between that of C and D **/
    private boolean xIsBetween(Point A, Point C, Point D) {
        return ((A.x >= C.x) && A.x <= D.x) ||
                ((A.x <= C.x) && A.x >= D.x);

    }
    @Override
    public String toString() {
        return "SvgFileProcessor{" +
                "commandLists=" + commandLists.iterator().toString() +
                '}';
    }

    //    public Pair<CircleBound, List<Point>> getBoundingCircle() {
    public Pair<CircleBound, List<Integer>> getBoundingCircle() {
        List<Point> pointList = getCommandList().stream().map(SvgPathCommand::getDestinationPoint).collect(Collectors.toList());
        CircleBound bound = SmallestEnclosingCircle.makeCircle(pointList);
        int[] touchIndex = IntStream.range(0,
                pointList.size()).filter(
                i -> Math.abs(Point.getDistance(bound.getCenter(), pointList.get(i)) - bound.getRadii()) < 0.5)
                .toArray();
        System.out.println("Original size:" + touchIndex.length);
        if (touchIndex.length != 0) {
            int first = touchIndex[0];
            List<SvgPathCommand> newCommandList = new ArrayList<>();
            for (int j = 0; j < commandLists.size(); j++) {
                newCommandList.add(commandLists.get((j + first) % pointList.size()));
                newCommandList.get(j).setCommandType(SvgPathCommand.CommandType.LINE_TO);
            }
            newCommandList.add(new SvgPathCommand(newCommandList.get(0).getDestinationPoint(), SvgPathCommand.CommandType.LINE_TO));
            newCommandList.get(0).setCommandType(SvgPathCommand.CommandType.MOVE_TO);
            commandLists.clear();
            PatternRenderer.insertPatternToList(newCommandList, commandLists, newCommandList.get(0).getDestinationPoint(),
                    -1 * Point.getAngle(bound.getCenter(), pointList.get(first)));
            CircleBound newBound = SmallestEnclosingCircle.makeCircle(
                    getCommandList().stream().map(SvgPathCommand::getDestinationPoint).collect(Collectors.toList()));
            touchIndex = Arrays.stream(touchIndex).map(i -> (i - first + pointList.size()) % pointList.size()).toArray();
            ArrayList<Integer> touchIndexList = new ArrayList<>();
            for (int i : touchIndex) {
                touchIndexList.add(i);
            }
            return new Pair<>(newBound, touchIndexList);
        }


        return new Pair<>(bound, new ArrayList<>());
    }


}