package jackiesvgprocessor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by JacquelineLi on 6/14/17.
 */
public class spinePatternMerger {
    private ArrayList<svgPathCommands> spineCommands, patternCommands, combinedCommands = new ArrayList<>();
    private String spineName, patternName;
    private fileProcessor spineFileProcessed, patternFileProcessed;
    private boolean rotationOn = true;

    public spinePatternMerger( fileProcessor spineFile, fileProcessor patternFile) {
        this.spineCommands = spineFile.getCommandLists().get(0);
        this.patternCommands = patternFile.getCommandLists().get(0);
        this.spineName = spineFile.getfFileName();
        this.patternName = patternFile.getfFileName();
        this.spineFileProcessed = spineFile;
        this.patternFileProcessed = patternFile;
    }

    public void combinePattern() {
        System.out.println("# of spine commands: " + spineCommands.size() +
                "# of patternCommands: " + patternCommands.size());
        svgPathCommands newCommand;
        combinedCommands.add(spineCommands.get(0));
        Point prevInsertPoint = spineCommands.get(0).getDestinationPoint();
        Point insertPoint;
        double gapWidth = patternFileProcessed.getHeight();
        assert (gapWidth > 0);
        System.out.println("gapWidth" + gapWidth);
        double remainingWidth = 0;
        for (int i = 1; i < spineCommands.size(); i++) {
            double totalLength = Point.getDistance(spineCommands.get(i - 1).getDestinationPoint(),
                    spineCommands.get(i).getDestinationPoint());
            System.out.println("Command:" + i + " totalLength:" + totalLength);
            /* first handle remaining width*/

            /*TODO handle remaining width = 0*/

            /*if inserted point is on this line*/
            if (totalLength > remainingWidth ) {
                insertPoint = Point.middlePointWithLen(spineCommands.get(i - 1).getDestinationPoint(),
                        spineCommands.get(i).getDestinationPoint(), remainingWidth);
                System.out.println("Next insert point is: " + insertPoint.toString() );

                System.out.println("Point:" + insertPoint.toString() + "passed test");
                /* insert a lineTo command to this potential point*/
                svgPathCommands handleRemainingCommand = new svgPathCommands(insertPoint, svgPathCommands.typeLineTo);
                combinedCommands.add(handleRemainingCommand);
                /* insert a pattern on this potential point*/
                    double rotationAngle = Point.getAngle(spineCommands.get(i-1).getDestinationPoint(), spineCommands.get(i).getDestinationPoint());
                    insertPatternToList(patternCommands, combinedCommands, insertPoint, rotationAngle);

                /* break this spine command to handle remaining length with each gapWid apart*/
                totalLength -= remainingWidth;
                System.out.println("totalLength after handle remain:" + totalLength);
                prevInsertPoint = insertPoint;

                while (totalLength > gapWidth) {
                    insertPoint = Point.middlePointWithLen(prevInsertPoint, spineCommands.get(i).getDestinationPoint(), gapWidth);
                    System.out.println("Point:" + insertPoint.toString() + "passed test");
                    /* insert a lineTo command to this potential point*/
                    svgPathCommands lineToGap = new svgPathCommands(insertPoint, svgPathCommands.typeLineTo);
                    combinedCommands.add(lineToGap);
                    /* insert a pattern on this potential point*/
                    insertPatternToList(patternCommands, combinedCommands, insertPoint, rotationAngle); //rotation angle should remain on the same line
                    /* break this spine command to handle remaining length with each gapWid apart*/
                    totalLength -= gapWidth;
                    System.out.println("totalLength:" + totalLength);
                    prevInsertPoint = insertPoint;
                }

                /* reset remaining width for next spine point*/
                remainingWidth = totalLength;
            } else {
                /* this is a super short line that can't even handle remaining width*/
                combinedCommands.add(spineCommands.get(i));
                remainingWidth -= totalLength;
                assert (remainingWidth > 0);
            }


                /** without gap adjustion, i.e., position a pattern at every point*/
//                combinedCommands.add(spineCommands.get(i));
//                Point spinePoint = spineCommands.get(i).getDestinationPoint();
//                double rotationAngle = Point.getAngle(spineCommands.get(i-1).getDestinationPoint(), spinePoint);
//                    /** Draw free-motion quilting pattern around spinePoint*/
//                    insertPatternToList(patternCommands, combinedCommands, spinePoint, rotationAngle);
            }
        outputGeneratedPattern();
    }

    public void insertPatternToList(ArrayList<svgPathCommands> patternCommands, ArrayList<svgPathCommands> combinedCommands, Point insertionPoint, double rotationAngle) {
        Point patternPoint = patternCommands.get(0).getDestinationPoint();
        svgPathCommands newCommand;
        for (int j = 0; j < patternCommands.size(); j++) {
            if (rotationOn) /** with rotation*/
                newCommand = new svgPathCommands(patternCommands.get(j), patternPoint, insertionPoint, rotationAngle);
            else /** without rotation*/
                newCommand = new svgPathCommands(patternCommands.get(j), patternPoint, insertionPoint);
            combinedCommands.add(newCommand);
        }

    }

    public void outputGeneratedPattern() {
        try{
            PrintWriter writer = new PrintWriter( "spine-" + spineName + "-pat-" + patternName + ".svg", "UTF-8");
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
            for (svgPathCommands command : combinedCommands) {
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
}
