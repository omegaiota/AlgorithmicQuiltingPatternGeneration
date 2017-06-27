package jackiesvgprocessor;

import java.util.ArrayList;

/**
 * Created by JacquelineLi on 6/25/17.
 */
public class patternRenderer {
    private ArrayList<svgPathCommands> patternCommands, renderedCommands = new ArrayList<>();
    private fileProcessor patternFileProcessed;

    public patternRenderer(fileProcessor patternFileProcessed) {
        this.patternFileProcessed = patternFileProcessed;
        this.patternCommands = patternFileProcessed.getCommandLists().get(0);
    }

    public void rotateWithDegrees(Double inputAngle) {
        Double angle = Math.toRadians(inputAngle);
        Double time = Math.PI * 2 / angle;
        System.out.println(time.intValue());
        int repeat =  time.intValue();
        svgPathCommands originCommand = new svgPathCommands(patternCommands.get(0).getDestinationPoint(), svgPathCommands.typeLineTo);
        renderedCommands.addAll(patternCommands);
        System.out.println(renderedCommands.size());
        System.out.println(renderedCommands.get(0).getCommandType());
        for (int i = 0; i < repeat; i++) {
            renderedCommands.add(originCommand);
            for (int j = 1; j < patternCommands.size(); j++) {
                renderedCommands.add(new svgPathCommands(patternCommands.get(j), originCommand.getDestinationPoint(), i* angle ));
            }
        }

        outputRotated(inputAngle);
    }

    public void echoPattern(int numbers) {

        svgPathCommands originCommand = new svgPathCommands(patternCommands.get(0).getDestinationPoint(), svgPathCommands.typeLineTo);
        renderedCommands.addAll(patternCommands);
        System.out.println(renderedCommands.size());
        System.out.println(renderedCommands.get(0).getCommandType());
        for (int i = 0; i < repeat; i++) {
            renderedCommands.add(originCommand);
            for (int j = 1; j < patternCommands.size(); j++) {
                renderedCommands.add(new svgPathCommands(patternCommands.get(j), originCommand.getDestinationPoint(), i* angle ));
            }
        }

        outputRotated(inputAngle);
    }

    public void outputRotated(Double angle) {
        fileProcessor.outputSvgCommands(renderedCommands, patternFileProcessed.getfFileName() + "-rotation-" + angle.intValue());
    }
}
