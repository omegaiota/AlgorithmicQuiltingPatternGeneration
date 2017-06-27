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

    public void echoPattern(int number) {
        double midX = 0, midY = 0;
        for (svgPathCommands command : patternCommands) {
            midX += command.getDestinationPoint().getX();
            midY += command.getDestinationPoint().getY();
        }
        midX /= patternCommands.size();
        midY /= patternCommands.size();

        double proportion = 1.0 / number;
        System.out.println(proportion);
        renderedCommands.addAll(patternCommands);
        Point start = patternCommands.get(0).getDestinationPoint();
        Point end = patternCommands.get(patternCommands.size() - 1).getDestinationPoint();
        Point center = Point.intermediatePointWithProportion(start, end, 0.5);
        for (int k = 1; k < number; k++) {
            Point baseDestination = Point.intermediatePointWithProportion(start, end, 1 - proportion * k / 2);
            Point complementBaseDestination = Point.intermediatePointWithProportion(start, end, proportion * k / 2);
            if (k % 2 == 1)
                renderedCommands.add( new svgPathCommands(baseDestination, svgPathCommands.typeLineTo));
            else
                renderedCommands.add( new svgPathCommands(complementBaseDestination, svgPathCommands.typeLineTo));
            System.out.println("Calling command with proportion:" + (1 - proportion * k));
            ArrayList<svgPathCommands> scaled = svgPathCommands.commandsScaling(patternCommands, 1 - proportion * k, new Point(midX, midY));
            ArrayList<svgPathCommands> newSet = new ArrayList<>();
            if (k % 2 == 1){
                System.out.println("this loop");
            for (int i = scaled.size() - 2; i >= 1; i--) {
                System.out.println(i);
                newSet.add(scaled.get(i));
            }
            } else {
                System.out.println("that loop");
                for (int i = 1; i < scaled.size() - 1; i++) {
                    newSet.add(scaled.get(i));
                }
            }

            //newSet = svgPathCommands.commandsShift(scaled, baseDestination);
            //newSet = scaled;
            renderedCommands.addAll(newSet);
            if (k % 2 == 1)
                renderedCommands.add( new svgPathCommands(complementBaseDestination, svgPathCommands.typeLineTo));
            else
                renderedCommands.add( new svgPathCommands(baseDestination, svgPathCommands.typeLineTo));
        }
        renderedCommands.add( new svgPathCommands(center, svgPathCommands.typeLineTo));


        outputEchoed(number);

    }

    private void outputEchoed(int number) {
        fileProcessor.outputSvgCommands(renderedCommands, patternFileProcessed.getfFileName() + "-echo-" + number);

    }

    public void outputRotated(Double angle) {
        fileProcessor.outputSvgCommands(renderedCommands, patternFileProcessed.getfFileName() + "-rotation-" + angle.intValue());
    }
}
