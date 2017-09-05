package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by JacquelineLi on 6/27/17.
 */
public class TreeTraversal {
    private TreeNode<Point> tree;
    List<SvgPathCommand> renderedCommands = new ArrayList<>();

    public TreeTraversal(TreeNode<Point> tree) {
        this.tree = tree;
        renderedCommands.add(new SvgPathCommand(tree.getData(), SvgPathCommand.CommandType.MOVE_TO));
    }

    public List<SvgPathCommand> traverseTree() {
        renderedCommands.add(new SvgPathCommand(tree.getData(), SvgPathCommand.CommandType.MOVE_TO));
        treeOrdering(tree, null);
        preOrderTraversal(tree, null);
        return renderedCommands;
    }

    public static void treeOrdering(TreeNode<Point> treeNode, TreeNode<Point> parentNode) {
        //no filling
        List<TreeNode<Point>> children = treeNode.getChildren();
        TreeNode<Point>[] childArray = children.toArray(new TreeNode[children.size()]);
        for (int i = 0; i < childArray.length; i++) {
            for (int j = i + 1; j < childArray.length; j++) {
                double angleParent = (parentNode == null) ? 0 : Point.getAngle(treeNode.getData(), parentNode.getData());
                double angle1 = Point.getAngle(treeNode.getData(), childArray[i].getData()) - angleParent;
                double angle2 = Point.getAngle(treeNode.getData(), childArray[j].getData()) - angleParent;
                if (angle1 < 0)
                    angle1 += Math.PI * 2;
                if (angle2 < 0)
                    angle2 += Math.PI * 2;
                if (angle1 < angle2) {
                    Collections.swap(children, i, j);
                }
            }
        }
        treeNode.setChildren(children);
        for (TreeNode<Point> child : childArray) {
            treeOrdering(child, treeNode);
        }
    }

    private void preOrderTraversal(TreeNode<Point> treeNode, TreeNode<Point> parentNode) {
        //no filling
        renderedCommands.add(new SvgPathCommand(treeNode.getData(), SvgPathCommand.CommandType.LINE_TO));
        List<TreeNode<Point>> children = treeNode.getChildren();
        TreeNode<Point>[] childArray = children.toArray(new TreeNode[children.size()]);
        for (TreeNode<Point> child : childArray) {
            preOrderTraversal(child, treeNode);
            renderedCommands.add(new SvgPathCommand(treeNode.getData(), SvgPathCommand.CommandType.LINE_TO));
        }
    }

    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }
}

