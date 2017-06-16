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
        System.out.println("# of spine commands: " + spineCommands.size() + "# of patternCommands: " + patternCommands.size());
        combinedCommands.add(spineCommands.get(0));
        Point prevInsertPoint = spineCommands.get(0).getDestinationPoint();
        Point insertPoint;
        double gapWidth = patternFileProcessed.getWidth();
        double remainingWidth = 0;
        for (int i = 1; i < spineCommands.size(); i++) {
            double totalLength = Point.getDistance(spineCommands.get(i - 1).getDestinationPoint(),
                    spineCommands.get(i).getDestinationPoint());
            System.out.println("\nCommand:" + i + " totalLength:" + totalLength);
            /* first handle remaining width*/

            /*if current line is long enough to put the insert point*/
            if (totalLength > remainingWidth ) {
                //offset to handle corners
                if (i >=2) {
                    double angleThisLine = Point.getAngle(spineCommands.get(i - 1).getDestinationPoint(), spineCommands.get(i).getDestinationPoint());
                    double anglePrevLine = Point.getAngle(i <= 1 ? new Point(0, 0) : spineCommands.get(i - 2).getDestinationPoint(), spineCommands.get(i - 1).getDestinationPoint());
                    double betweenAngles = (angleThisLine - anglePrevLine);
                    betweenAngles += betweenAngles > 0 ? 0 : (Math.PI * 2);
                    System.out.println("prevLine angle:" + Math.toDegrees(anglePrevLine));
                    System.out.println("thisLine angle:" + Math.toDegrees(angleThisLine));
                    System.out.println("angle between lines:" + (Math.toDegrees(betweenAngles)));
                    double adjustmentThreshhold = Math.PI / 6;
                    if ((betweenAngles > adjustmentThreshhold) && (betweenAngles < (Math.PI * 2 - adjustmentThreshhold))) {
                        System.out.println("line angle meets adjustment threshhold. Adjusting corners...");
                        if (betweenAngles < Math.PI) {
                            // concave outward, need to add patterns
                            insertPatternToList(patternCommands, combinedCommands, spineCommands.get(i - 1).getDestinationPoint(), anglePrevLine + (Math.PI - betweenAngles + ((remainingWidth - gapWidth / 2) / gapWidth) * 2) / 2);

                        } else if  (Double.compare(remainingWidth / gapWidth, 0.1) > 0 ) {
                            betweenAngles = 2 * Math.PI - betweenAngles;
                            remainingWidth += (Math.toDegrees(betweenAngles)) / 80 * patternFileProcessed.getHeight();
                        }
                    }
                }
                System.out.println("remaining width is:" + remainingWidth);
                insertPoint = Point.middlePointWithLen(spineCommands.get(i - 1).getDestinationPoint(),
                        spineCommands.get(i).getDestinationPoint(), remainingWidth);
                System.out.println("prev command dest:" + spineCommands.get(i - 1).getDestinationPoint());
                System.out.println("this command dest:" + spineCommands.get(i).getDestinationPoint());
                System.out.println("Next insert point is: " + insertPoint.toString() );
                /* insert a lineTo command to this potential point*/
                svgPathCommands handleRemainingCommand = new svgPathCommands(insertPoint, svgPathCommands.typeLineTo);
                combinedCommands.add(handleRemainingCommand);
                /* insert a pattern on this potential point*/
                    double rotationAngle = Point.getAngle(spineCommands.get(i-1).getDestinationPoint(), spineCommands.get(i).getDestinationPoint());
                    insertPatternToList(patternCommands, combinedCommands, insertPoint, rotationAngle);

                /* break this spine command to handle remaining length with each gapWid apart*/
                totalLength -= remainingWidth;
//                System.out.println("totalLength after handle remain:" + totalLength);
                prevInsertPoint = insertPoint;

                while (totalLength > gapWidth) {
                    insertPoint = Point.middlePointWithLen(prevInsertPoint, spineCommands.get(i).getDestinationPoint(), gapWidth);
                    /* insert a lineTo command to this potential point*/
                    svgPathCommands lineToGap = new svgPathCommands(insertPoint, svgPathCommands.typeLineTo);
                    combinedCommands.add(lineToGap);
                    /* insert a pattern on this potential point*/
                    insertPatternToList(patternCommands, combinedCommands, insertPoint, rotationAngle); //rotation angle should remain on the same line
                    /* break this spine command to handle remaining length with each gapWid apart*/
                    totalLength -= gapWidth;
//                    System.out.println("totalLength:" + totalLength);
                    prevInsertPoint = insertPoint;
                }

                /* reset remaining width for next spine point*/
                remainingWidth = gapWidth -  totalLength;
                combinedCommands.add(spineCommands.get(i));
            } else {
                /* this is a super short line that can't even handle remaining width, skip this line */
                combinedCommands.add(spineCommands.get(i));
                remainingWidth -= totalLength;
                assert (remainingWidth > 0);
            }

            }
        outputGeneratedPattern();
    }

    public void insertPatternToList(ArrayList<svgPathCommands> patternCommands,
                                    ArrayList<svgPathCommands> combinedCommands,
                                    Point insertionPoint, double rotationAngle) {
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
