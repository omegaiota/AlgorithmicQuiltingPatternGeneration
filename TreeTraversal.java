package jackiequiltpatterndeterminaiton;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static jackiequiltpatterndeterminaiton.TreeTraversal.NodeType.IN;
import static jackiequiltpatterndeterminaiton.TreeTraversal.NodeType.LEAF;
import static jackiequiltpatterndeterminaiton.TreeTraversal.NodeType.OUT;

/**
 * Created by JacquelineLi on 6/27/17.
 */
public class TreeTraversal {
    List<SvgPathCommand> renderedCommands = new ArrayList<>();
    List<SvgPathCommand> squiggleCommands = new ArrayList<>();
    List<NodeType> nodeLabel = new ArrayList<>();
    private TreeNode<Point> tree;
    public TreeTraversal(TreeNode<Point> tree) {
        this.tree = tree;
//        renderedCommands.add(new SvgPathCommand(tree.getData(), SvgPathCommand.CommandType.MOVE_TO));
//        nodeLabel.add(IN);
    }

    public static void treeOrdering(TreeNode<Point> treeNode, TreeNode<Point> parentNode) {
//        PrintWriter writer = SVGElement.writeHeader("treeAngles", 750, 750);
        recurseOrder(treeNode, parentNode, null);
//        writer.println("  </g>");
//        writer.println("</svg>");
//        writer.close();
    }

    private static void recurseOrder(TreeNode<Point> treeNode, TreeNode<Point> parentNode, PrintWriter writer) {
        //no filling
        Point parent = treeNode.getData();
        List<TreeNode<Point>> children = treeNode.getChildren();
        TreeNode<Point>[] childArray = children.toArray(new TreeNode[children.size()]);
        double angleParent = (parentNode == null) ? 0 : Point.getAngle(treeNode.getData(), parentNode.getData());
        Collections.sort(children, Comparator.comparingDouble(i -> (Point.getAngle(parent, i.getData()) + Math.PI * 2 - angleParent) % (Math.PI * 2))
        );
        Collections.reverse(children);
        treeNode.setChildren(children);
        for (TreeNode<Point> child : childArray) {
            recurseOrder(child, treeNode, writer);
        }
    }


    public List<NodeType> getNodeLabel() {
        return nodeLabel;
    }

    public List<SvgPathCommand> traverseTree() {
        renderedCommands.add(new SvgPathCommand(tree.getData(), SvgPathCommand.CommandType.MOVE_TO));
        nodeLabel.add(IN);
        treeOrdering(tree, null);
        preOrderTraversal(tree, null);
        return renderedCommands;
    }

    private void preOrderTraversal(TreeNode<Point> treeNode, TreeNode<Point> parentNode) {
        //no filling
        SvgPathCommand goDown = new SvgPathCommand(treeNode.getData(), SvgPathCommand.CommandType.LINE_TO),
                goUp = new SvgPathCommand(treeNode.getData(), SvgPathCommand.CommandType.LINE_TO);
        List<TreeNode<Point>> children = treeNode.getChildren();

        renderedCommands.add(goDown);
        if (children.size() == 0)
            nodeLabel.add(LEAF);
        else
            nodeLabel.add(IN);

        TreeNode<Point>[] childArray = children.toArray(new TreeNode[children.size()]);
        for (TreeNode<Point> child : childArray) {
            preOrderTraversal(child, treeNode);

            renderedCommands.add(goUp);
            nodeLabel.add(OUT);
            assert (goDown.getDestinationPoint().equals(goUp.getDestinationPoint()));

        }
    }


    public List<SvgPathCommand> getSquiggleCommands() {
        return squiggleCommands;
    }

    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }

    public enum NodeType {
        IN, OUT, LEAF
    }
}

