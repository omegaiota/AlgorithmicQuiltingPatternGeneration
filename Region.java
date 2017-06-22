package jackiesvgprocessor;

import java.util.ArrayList;

/**
 * Created by JacquelineLi on 6/19/17.
 */
public class Region {
    private ArrayList<Point> boundary = new ArrayList<>();

    public Region(ArrayList<Point> boundary) {
        this.boundary = boundary;
    }

    public ArrayList<Point> getBoundary() {
        return boundary;
    }

    public boolean insideRegion(Point testPoint) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = boundary.size() - 1; i <  boundary.size(); j = i++) {
            if (( boundary.get(i).getY() > testPoint.getY()) != ( boundary.get(j).getY() > testPoint.getY())
                    && (testPoint.getX() < ( boundary.get(j).getX() - boundary.get(i).getX()) * (testPoint.getY() - boundary.get(i).getY())
                    / ( boundary.get(j).getY() - boundary.get(i).getY()) + boundary.get(i).getX())) {
                result = !result;
            }
        }
        return result;
    }
}
