package src.jackiealgorithmicquilting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/18/17.
 */
public class HilbertCurveGenerator {
    private ArrayList<SvgPathCommand> commandList = new ArrayList<>();
    private int level;
    private Point bottomLeft, unitX, unitY;


    public HilbertCurveGenerator(Point bottomLeft, Point unitX, Point unitY, int level) {
        this.level = level;
        this.bottomLeft = bottomLeft;
        this.unitX = unitX;
        this.unitY = unitY;
    }

    public List<SvgPathCommand> patternGeneration() {
        generator(bottomLeft, unitX, unitY, level);
        return commandList;
    }

    private  void generator(Point bottomLeftCorner, Point unitX, Point unitY, int level) {
        if (level <= 0) {
            Point destination = new Point(bottomLeftCorner.x + (unitX.x + unitY.x) / 2,
                    bottomLeftCorner.y + (unitX.y + unitY.y) / 2);
            commandList.add(new SvgPathCommand(destination, SvgPathCommand.CommandType.LINE_TO));
            if (commandList.size() == 1) {
                commandList.get(0).setCommandType(SvgPathCommand.CommandType.MOVE_TO);
            }
        } else {
            Point newUnitX = new Point(unitX.x / 2, unitX.y / 2);
            Point newUnitY = new Point(unitY.x / 2, unitY.y / 2);
            generator(bottomLeftCorner, newUnitY,  newUnitX,  level - 1);
            generator(Point.sumOfPoint(bottomLeftCorner, newUnitX), newUnitX, newUnitY, level - 1);
            generator(Point.sumOfPoint(Point.sumOfPoint(bottomLeftCorner, newUnitX), newUnitY),
                    newUnitX,  newUnitY,  level - 1);
            generator(Point.sumOfPoint(Point.sumOfPoint(bottomLeftCorner, newUnitX), unitY),
                    new Point(-1 * newUnitY.x, -1 * newUnitY.y),
                    new Point(-1 * newUnitX.x, -1 * newUnitX.y), level - 1);

        }
    }
}
