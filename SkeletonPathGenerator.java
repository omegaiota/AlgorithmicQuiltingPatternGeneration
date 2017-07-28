package jackiesvgprocessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 7/27/17.
 */
public class SkeletonPathGenerator {
    Region region;
    List<SvgPathCommand> skeletonPath = new ArrayList<>();
    public enum GenerationMethodType {
        SNAKE
    }

    public SkeletonPathGenerator(Region region) {
        this.region = region;
    }

    public void snakePathGenerator(int rows) {
        Double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE,
                height = 0.0;
        for (Point point : region.getBoundary()) {
            if (Double.compare(point.getX(), minX) <= 0)
                minX = point.getX();
            if (Double.compare(point.getX(), maxX) >= 0)
                maxX = point.getX();
            if (Double.compare(point.getY(), minY) <= 0)
                minY = point.getY();
            if (Double.compare(point.getY(), maxY) >= 0)
                maxY = point.getY();
        }

        height = maxY - minY;


        for (int i = 0; i <= rows; i++) {
            if (i % 2 == 0) {
                // Line from left to right
                skeletonPath.add(new SvgPathCommand(new Point(minX, maxY - height * ((double)i) / rows), SvgPathCommand.CommandType.LINE_TO));
                skeletonPath.add(new SvgPathCommand(new Point(maxX, maxY - height * ((double)i) / rows), SvgPathCommand.CommandType.LINE_TO));

            } else {
                // Line from right to left
                skeletonPath.add(new SvgPathCommand(new Point(maxX, maxY - height * ((double)i) / rows), SvgPathCommand.CommandType.LINE_TO));
                skeletonPath.add(new SvgPathCommand(new Point(minX, maxY - height * ((double)i)/ rows), SvgPathCommand.CommandType.LINE_TO));
            }
        }
        skeletonPath.get(0).setCommandType(SvgPathCommand.CommandType.MOVE_TO);

    }

    public List<SvgPathCommand> getSkeletonPath() {
        return skeletonPath;
    }

    public File outputSnake(int rows) {
        return svgFileProcessor.outputSvgCommands(skeletonPath, "snake-" + rows);
    }
}
