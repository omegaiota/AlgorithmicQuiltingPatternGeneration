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
        svgPathCommands newCommand;
        combinedCommands.add(spineCommands.get(0));
        double gapWidth = patternFileProcessed.getHeight();
        for (int i = 1; i < spineCommands.size(); i++) {
            combinedCommands.add(spineCommands.get(i));
            Point spinePoint = spineCommands.get(i).getDestinationPoint();
            Point patternPoint = patternCommands.get(0).getDestinationPoint();
            double rotationAngle = Point.getAngle(spineCommands.get(i-1).getDestinationPoint(), spinePoint);

            /** Draw free-motion quilting pattern around spinePoint*/
            for (int j = 0; j < patternCommands.size(); j++) {
                if (rotationOn) /** with rotation*/
                    newCommand = new svgPathCommands(patternCommands.get(j), patternPoint, spinePoint, rotationAngle);
                else /** without rotation*/
                    newCommand = new svgPathCommands(patternCommands.get(j), patternPoint, spinePoint);
                combinedCommands.add(newCommand);
            }
        }

        outputGeneratedPattern();
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
