package jackiequiltpatterndeterminaiton;

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
import java.util.List;


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
    private ArrayList<ArrayList<SvgPathCommand>> commandLists = new ArrayList<>();

    public SvgFileProcessor(File importFile) {
        this.fFilePath = Paths.get(importFile.getPath());
        this.fFileName = importFile.getName();
        this.fSvgFile = importFile;
    }


    public static File outputSvgCommands(List<SvgPathCommand> outputCommandList, String fileName) {
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
            writer.println("   width=\"210mm\"");
            writer.println("   height=\"297mm\">");
            writer.println("   <g");
            writer.println("     inkscape:label=\"Layer 1\"");
            writer.println("     inkscape:groupmode=\"layer\"");
            writer.println("     id=\"layer1\">");
            writer.println("");
            writer.println("");

            writer.println("    <path");
            writer.println("       style=\"fill:none;fill-rule:evenodd;stroke:#000000;stroke-width:1px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1\"");
            writer.print("    d=\"");
            outputCommandList.get(0).setCommandType(SvgPathCommand.CommandType.MOVE_TO);
            for (int i = 0; i < outputCommandList.size(); i++) {
                SvgPathCommand command = outputCommandList.get(i);
                if ((command.getCommandType() == SvgPathCommand.CommandType.MOVE_TO) && (i != 0)) {
                    System.out.println("WARNING: MOVE_TO inside commandlist");
                    command.setCommandType(SvgPathCommand.CommandType.LINE_TO);
                }
                writer.print(command.toSvgCode());
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
                    if (command.isMoveTo())
                        writer.println("N" + count + "G00" + "X" + command.getDestinationPoint().x + "Y" + command.getDestinationPoint().y);
                    else
                        writer.println("N" + count + "G01" + "X" + command.getDestinationPoint().x + "Y" + command.getDestinationPoint().y);
                } else if (command.isCurveTo()) {
                    count++;
                    writer.println("N" + count + "G01" + "X" + command.getDestinationPoint().x + "Y" + command.getDestinationPoint().y);
                }
            }
            count++;
            writer.println("N" + count + "M02");
            writer.close();


        } catch (IOException e) {
            // do something
        }


    }

    public Point getMinPoint() {
        return minPoint;
    }

    public Point getMaxPoint() {
        return maxPoint;
    }

    public Region getBoundary() {
        ArrayList<Point> destList = new ArrayList<>();
        for ( SvgPathCommand command : commandLists.get(0)) {
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
            ArrayList<SvgPathCommand> aCommandList = new ArrayList<>();
            commandLists.add(aCommandList);
            processPath(pathNodeList.item(i), aCommandList);
        }
        width = maxPoint.x - minPoint.x;
        height = maxPoint.y - minPoint.y;
        patternHeight = commandLists.get(0).get(0).getDestinationPoint().y - minPoint.y;
        //widthRight = maxPoint.getX() - getCommandLists().get(0).get(0).getDestinationPoint().getX();
        widthRight = getCommandLists().get(0).get(getCommandLists().get(0).size() - 1).getDestinationPoint().x - getCommandLists().get(0).get(0).getDestinationPoint().x;

        System.out.println("File loaded:" + "maxPoint=" + maxPoint.toString() + "minPoint=" + minPoint.toString()
                +  "width=" + width + ";\n height=" + height + "effective height" + getEffectiveHeight() +
                "\npattern height:" + patternHeight + " first height:"
                + commandLists.get(0).get(0).getDestinationPoint().y);
        System.out.println();
    }

    private void processPath(Node pathNode, ArrayList<SvgPathCommand> pathCommandList) {
        String pathStr = pathNode.getNodeValue();
        String withDelimiter = "(?=[cCmMlLzZ])";
        String[] pathElemArray = pathStr.split(withDelimiter);
        Point current = new Point(0.0, 0.0);
        System.out.println("pathElemArrayLength is" + pathElemArray.length);
        for (int i = 0; i < pathElemArray.length; i++) {

            /** if the last command is close path, add a command that lineTo initial point*/
            if ((i == pathElemArray.length - 1) && (pathElemArray[i].substring(0,1).equalsIgnoreCase("z"))) {
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

    public ArrayList<ArrayList<SvgPathCommand>> getCommandLists() {
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

    public double getPatternHeight() {
        return patternHeight;
    }

    public double getWidthRight() {
        return widthRight;
    }

    public double getEffectiveHeight() {
        List<SvgPathCommand> commands = getCommandLists().get(0);
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


}