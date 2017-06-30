package jackiesvgprocessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/27/17.
 */
public class treeRenderer {
    private TreeNode<Point> tree;
    ArrayList<svgPathCommand> renderedCommands = new ArrayList<>();
    private int type;
    static int typeFilling = 1, typeNoFill = 0;

    public treeRenderer(TreeNode<Point> tree, int type) {
        this.tree = tree;
        renderedCommands.add(new svgPathCommand(tree.getData(), svgPathCommand.typeMoveTo));
        this.type = type;
    }

    public ArrayList<svgPathCommand> traverseTree() {
        renderedCommands.add(new svgPathCommand(tree.getData(), svgPathCommand.typeMoveTo));
//        if  (type == typeFilling)
//            preOrderTraversal(tree, null);
//        else if (type == typeNoFill)
            preOrderTraversal(tree, null);
        return renderedCommands;
    }


    private void preOrderTraversal(TreeNode<Point> treeNode, TreeNode<Point> parentNode) {
        //no filling
        renderedCommands.add(zeroFilling(treeNode)[0]);
        List<TreeNode<Point>> children = treeNode.getChildren();
        TreeNode<Point>[] arr = children.toArray(new TreeNode[children.size()]);
        TreeNode<Point> tmp;
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                double angleParent = (parentNode == null) ? 0 : Point.getAngle(treeNode.getData(), parentNode.getData());
                double angle1 = Point.getAngle(treeNode.getData(), arr[i].getData()) - angleParent;
                double angle2 = Point.getAngle(treeNode.getData(), arr[j].getData()) - angleParent;
                if (angle1 < 0)
                    angle1 += Math.PI * 2;
                if (angle2 < 0)
                    angle2 += Math.PI * 2;
                if (angle1 < angle2) {
                    tmp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = tmp;
                }
            }
        }

        for (TreeNode<Point> child : arr) {
            preOrderTraversal(child, treeNode);
            renderedCommands.add(zeroFilling(treeNode)[1]);
        }


    }


    private svgPathCommand[] zeroFilling(TreeNode<Point> node) {
        svgPathCommand[] start_and_end = new svgPathCommand[2];
        start_and_end[0] = new svgPathCommand(node.getData(), svgPathCommand.typeLineTo);
        start_and_end[1] = new svgPathCommand(node.getData(), svgPathCommand.typeLineTo);
        return start_and_end;
    }

    private svgPathCommand[] leftRightFilling(TreeNode<Point> node, TreeNode<Point> parentNode) {
        double absOffset = 5;
        svgPathCommand[] start_and_end = new svgPathCommand[2];
        Point point1 = Point.vertOffset(node.getData(), parentNode.getData(), absOffset );
        Point point0 = Point.vertOffset(node.getData(), parentNode.getData(), absOffset * (-1));
        start_and_end[0] = new svgPathCommand(point0, svgPathCommand.typeLineTo);
        start_and_end[1] = new svgPathCommand(point1, svgPathCommand.typeLineTo);
        System.out.println(Point.getDistance(point0, point1));
        return start_and_end;
    }

    public ArrayList<svgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }
}

