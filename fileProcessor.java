package jackiesvgprocessor;

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


/**
 * Created by JacquelineLi on 6/12/17.
 */
public class fileProcessor {
    private Path fFilePath;
    private String fFileName;
    private File fSvgFile;
    private NodeList pathNodeList;

    private ArrayList<ArrayList<svgPathCommands>> commandLists = new ArrayList<>();
    int typeMoveTo = 0;
    int typeLineTo = 1;
    int typeCurveTo = 2;
    int typeSmoothTo = 3;


    public fileProcessor(File importFile) {
        this.fFilePath = Paths.get(importFile.getPath());
        this.fFileName = importFile.getName();
        this.fSvgFile = importFile;
    }

    public void processSvg() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(fSvgFile);

        String xpathExpression = "//path/@d";
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xpath = xpf.newXPath();
        XPathExpression expression = xpath.compile(xpathExpression);
        pathNodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

        for (int i = 0; i < pathNodeList.getLength(); i++) {
            ArrayList<svgPathCommands> aCommandList = new ArrayList<>();
            commandLists.add(aCommandList);
            processPath(pathNodeList.item(i), aCommandList);
        }
    }

    private void processPath(Node pathNode, ArrayList<svgPathCommands> pathCommandList) {
        String pathStr = pathNode.getNodeValue();
        String withDelimiter = "(?=[a-zA-Z])";
        String[] pathElemArray = pathStr.split(withDelimiter);
        Point current = new Point(0.0, 0.0);
        for (int i = 0; i < pathElemArray.length; i++) {
            current = parseString(pathElemArray[i], pathCommandList, current);
        }

        printCommandList(pathCommandList);
    }

    private void printCommandList(ArrayList<svgPathCommands> pathCommandList) {
        for (int i = 0; i < pathCommandList.size(); i++) {
            System.out.println(pathCommandList.get(i).toString());
        }
    }

    public Point parseString(String commandString, ArrayList<svgPathCommands> pathCommandList, Point current) {
        String[] arguments = commandString.split(" ");
        char commandChar = arguments[0].charAt(0);
        boolean useAbsCoordinate = Character.isUpperCase(commandChar);

        Point destPoint;
        Point controlPoint1;
        Point controlPoint2;
        for (String arg : arguments) {
//            System.out.println(arg);
        }
        switch (commandChar) {
            case 'm':
                destPoint = useAbsCoordinate ? new Point(arguments[1]) : new Point(current, arguments[1]);
                current = destPoint;
                pathCommandList.add(new svgPathCommands(destPoint, typeMoveTo));
//                System.out.println(pathCommandList.get(pathCommandList.size() - 1).toString());

                for (int i = 2; i < arguments.length; i++) {
                    destPoint = useAbsCoordinate ? new Point(arguments[i]) : new Point(current, arguments[i]);
                    current = destPoint;
                    pathCommandList.add(new svgPathCommands(destPoint, typeLineTo));
//                    System.out.println(pathCommandList.get(pathCommandList.size() - 1).toString());

                }
                break;
            case 'l':
                destPoint = useAbsCoordinate ? new Point(arguments[1]) : new Point(current, arguments[1]);
                current = destPoint;
                pathCommandList.add(new svgPathCommands(destPoint, typeLineTo));
//                System.out.println(pathCommandList.get(pathCommandList.size() - 1).toString());

                for (int i = 2; i < arguments.length; i++) {
                    destPoint = useAbsCoordinate ? new Point(arguments[i]) : new Point(current, arguments[i]);
                    current = destPoint;
                    pathCommandList.add(new svgPathCommands(destPoint, typeLineTo));
//                    System.out.println(pathCommandList.get(pathCommandList.size() - 1).toString());
                }
                break;
            case 'c':
                for (int i = 1; i + 2 < arguments.length; i += 3) {
                    assert (i + 2 < arguments.length);
                    controlPoint1 = useAbsCoordinate ? new Point(arguments[i]) : new Point(current, arguments[i]);
                    controlPoint2 = useAbsCoordinate ? new Point(arguments[i + 1]) : new Point(current, arguments[i + 1]);
                    destPoint = useAbsCoordinate ? new Point(arguments[i + 2]) : new Point(current, arguments[i + 2]);
                    pathCommandList.add(new svgPathCommands(controlPoint1, controlPoint2, destPoint, typeCurveTo));
//                    System.out.println(pathCommandList.get(pathCommandList.size() - 1).toString());
                    current = destPoint;
                }
                break;
        }

        return current;
    }

    public void outputSvg() {
        try{
            PrintWriter writer = new PrintWriter( fFileName + "-absolute" + ".svg", "UTF-8");
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
            for (svgPathCommands command : commandLists.get(0)) {
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
    }

    public ArrayList<ArrayList<svgPathCommands>> getCommandLists() {
        return commandLists;
    }

    public void setCommandLists(ArrayList<ArrayList<svgPathCommands>> commandLists) {
        this.commandLists = commandLists;
    }

    public Path getfFilePath() {
        return fFilePath;
    }

    public void setfFilePath(Path fFilePath) {
        this.fFilePath = fFilePath;
    }

    public String getfFileName() {
        return fFileName;
    }

    public void setfFileName(String fFileName) {
        this.fFileName = fFileName;
    }

    @Override
    public String toString() {
        return "fileProcessor{" +
                "commandLists=" + commandLists.iterator().toString() +
                '}';
    }
}

