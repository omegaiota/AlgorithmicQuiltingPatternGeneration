package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 7/27/17.
 */
public class SkeletonPathGenerator {
    Region region;
    List<SvgPathCommand> skeletonPath = new ArrayList<>();
    public SkeletonPathGenerator(Region region) {
        this.region = region;
    }

    public void snakePathGenerator(int rows) {
        Double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE,
                height = 0.0;
        for (Point point : region.getPoints()) {
            if (Double.compare(point.x, minX) <= 0)
                minX = point.x;
            if (Double.compare(point.x, maxX) >= 0)
                maxX = point.x;
            if (Double.compare(point.y, minY) <= 0)
                minY = point.y;
            if (Double.compare(point.y, maxY) >= 0)
                maxY = point.y;
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
}
