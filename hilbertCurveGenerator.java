package jackiesvgprocessor;

import java.util.ArrayList;

/**
 * Created by JacquelineLi on 6/18/17.
 */
public class hilbertCurveGenerator {
    private ArrayList<svgPathCommands> commandList = new ArrayList<>();
    private int level;
    private Point bottomLeft, unitX, unitY;


    public hilbertCurveGenerator(Point bottomLeft, Point unitX, Point unitY, int level) {
        this.level = level;
        this.bottomLeft = bottomLeft;
        this.unitX = unitX;
        this.unitY = unitY;
    }

    public void patternGeneration() {
        generator(bottomLeft, unitX, unitY, level);
    }

    private  void generator(Point bottomLeftCorner, Point unitX, Point unitY, int level) {
        if (level <= 0) {
            Point destination = new Point(bottomLeftCorner.getX() + (unitX.getX() + unitY.getX())/ 2,
                    bottomLeftCorner.getY() + (unitX.getY() + unitY.getY())/ 2);
            commandList.add(new svgPathCommands(destination, svgPathCommands.typeLineTo));
            if (commandList.size() == 1) {
                commandList.get(0).setCommandType(svgPathCommands.typeMoveTo);
            }
        } else {
            Point newUnitX = new Point(unitX.getX() / 2, unitX.getY() / 2);
            Point newUnitY = new Point(unitY.getX() / 2, unitY.getY() / 2);
            generator(bottomLeftCorner, newUnitY,  newUnitX,  level - 1);
            generator(Point.pointAdd(bottomLeftCorner, newUnitX), newUnitX,  newUnitY,  level - 1);
            generator(Point.pointAdd(Point.pointAdd(bottomLeftCorner, newUnitX), newUnitY),
                    newUnitX,  newUnitY,  level - 1);
            generator(Point.pointAdd(Point.pointAdd(bottomLeftCorner, newUnitX), unitY),
                    new Point(-1 * newUnitY.getX(), -1 * newUnitY.getY()),
                    new Point(-1 * newUnitX.getX(), -1 * newUnitX.getY()),  level - 1);

        }
    }

    public void outputPath() {
        fileProcessor.outputSvgCommands(commandList, "hilbertCurve-level" + level + "-w" + unitX.getX() + "-h" + unitY.getY());
    }

    public ArrayList<svgPathCommands> getCommandList() {
        return commandList;
    }
}
