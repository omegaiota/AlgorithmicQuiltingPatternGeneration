package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by JacquelineLi on 6/27/17.
 */
public class TreeTraversal {
    public static AtomicInteger counter = new AtomicInteger(0);
    List<SvgPathCommand> renderedCommands = new ArrayList<>();
    List<SvgPathCommand> squiggleCommands = new ArrayList<>();
    private TreeNode<Point> tree;

    public TreeTraversal(TreeNode<Point> tree) {
        this.tree = tree;
        renderedCommands.add(new SvgPathCommand(tree.getData(), SvgPathCommand.CommandType.MOVE_TO));
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

    public List<SvgPathCommand> traverseTree(List<SvgPathCommand> renderedDecoCommands) {
        renderedCommands.add(new SvgPathCommand(tree.getData(), SvgPathCommand.CommandType.MOVE_TO));
        treeOrdering(tree, null);
        preOrderTraversal(tree, null);
        squiggleCommands = new ArrayList<>();
        sguigglePreorderTraversal(tree, null, renderedDecoCommands);
        return renderedCommands;
    }

    public List<SvgPathCommand> traverseSquiggleTree(List<SvgPathCommand> renderedDecoCommands) {
        renderedCommands.add(new SvgPathCommand(tree.getData(), SvgPathCommand.CommandType.MOVE_TO));
        treeOrdering(tree, null);
        squiggleCommands = new ArrayList<>();
        sguigglePreorderTraversal(tree, null, renderedDecoCommands);
        return squiggleCommands;
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

    private void sguigglePreorderTraversal(TreeNode<Point> treeNode, TreeNode<Point> parentNode, List<SvgPathCommand> renderedDecoCommands) {
        //no filling

        Point parentPoint = (parentNode == null) ? new Point() : parentNode.getData();

        final List<SvgPathCommand> sguiggalized;
        if (parentNode != null) {
            sguiggalized = SvgPathCommand.sguiggalized(parentNode.getData(), treeNode.getData(), SvgPathCommand.CommandType.LINE_TO);
        } else {
            System.out.println("parent node is null");
            (sguiggalized = new ArrayList<>()).add(new SvgPathCommand(treeNode.getData(), SvgPathCommand.CommandType.LINE_TO));
        }
        System.out.println(squiggleCommands.size() + "  0");
        System.out.println("Squiggalized size: " + sguiggalized.size());
        List<SvgPathCommand> sguiggalizedReverse = new ArrayList<>();
        for (int i = sguiggalized.size() - 1; i >= 0; i--)
            sguiggalizedReverse.add(sguiggalized.get(i));
        squiggleCommands.addAll(sguiggalized);
        System.out.println(squiggleCommands.size() + "  1");

        //SvgFileProcessor.outputSvgCommands(squiggleCommands, "hey" + counter + "-1");
        List<TreeNode<Point>> children = treeNode.getChildren();
        TreeNode<Point>[] childArray = children.toArray(new TreeNode[children.size()]);

        if (childArray.length == 0) {
            System.out.println(squiggleCommands.size() + "  2");
            PatternRenderer.insertPatternToList(renderedDecoCommands, squiggleCommands, treeNode.getData(), Point.getAngle(treeNode.getData(), parentPoint));
            //  SvgFileProcessor.outputSvgCommands(squiggleCommands, "hey" + counter + "-3");
            System.out.println(squiggleCommands.size() + "  3");
            squiggleCommands.addAll(sguiggalizedReverse);
            System.out.println(squiggleCommands.size() + "  4");

        } else {
            for (TreeNode<Point> child : childArray) {
                System.out.println(squiggleCommands.size() + "  5");
                sguigglePreorderTraversal(child, treeNode, renderedDecoCommands);
                squiggleCommands.addAll(sguiggalizedReverse);
                System.out.println(squiggleCommands.size() + "  6");
                //   SvgFileProcessor.outputSvgCommands(squiggleCommands, "hey" + counter + "-2-" + (counter2++) );

            }
        }
    }

    public List<SvgPathCommand> getSquiggleCommands() {
        return squiggleCommands;
    }
    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }
}

