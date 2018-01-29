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

    public List<SvgPathCommand> traverseTree() {
        renderedCommands.add(new SvgPathCommand(tree.getData(), SvgPathCommand.CommandType.MOVE_TO));
        treeOrdering(tree, null);
        preOrderTraversal(tree, null);
        return renderedCommands;
    }

    public List<SvgPathCommand> traverseSquiggleTree() {
//        renderedCommands.add(new SvgPathCommand(tree.getData(), SvgPathCommand.CommandType.MOVE_TO));
//        treeOrdering(tree, null);
//        squiggleCommands = new ArrayList<>();
//        sguigglePreorderTraversal(tree, null, renderedDecoCommands);

        return squiggleCommands;
    }

    private void preOrderTraversal(TreeNode<Point> treeNode, TreeNode<Point> parentNode) {
        //no filling
        int downIndex = renderedCommands.size();
        SvgPathCommand goDown = new SvgPathCommand(treeNode.getData(), SvgPathCommand.CommandType.LINE_TO),
                goUp = new SvgPathCommand(treeNode.getData(), SvgPathCommand.CommandType.LINE_TO);
        renderedCommands.add(goDown);
        List<TreeNode<Point>> children = treeNode.getChildren();
        TreeNode<Point>[] childArray = children.toArray(new TreeNode[children.size()]);
        for (TreeNode<Point> child : childArray) {
            preOrderTraversal(child, treeNode);
            int upIndex = renderedCommands.size();
            renderedCommands.add(goUp);

            // Spline Interpolation
            if ((downIndex != 0) && ((upIndex - downIndex) != 1)) {
//                Point p2 = renderedCommands.get(downIndex + 1).getDestinationPoint(),
//                        p1 = renderedCommands.get(downIndex - 1).getDestinationPoint();
//                Point p0, p3;
//                if (downIndex - 2 < 0)
//                    p0 = p1.minus(p2.minus(p1));
//                else
//                    p0 = renderedCommands.get(downIndex - 2).getDestinationPoint();
//                if (downIndex + 2 > upIndex)
//                    p3 = p2.add(p2.minus(p1));
//                else
//                    p3 = renderedCommands.get(downIndex + 2).getDestinationPoint();
//                Point c1 = p2.minus(p0).divide(2).add(p1), c2 = p3.minus(p1).divide(2).add(p1);
//                goDown.setControlPoint1(c1);
//                goDown.setControlPoint2(c2);
//
//                goUp.setControlPoint1(c2);
//                goUp.setControlPoint2(c1);
//                goDown.setCommandType(SvgPathCommand.CommandType.CURVE_TO);
//                goUp.setCommandType(SvgPathCommand.CommandType.CURVE_TO);
            }
        }
    }

//    private void sguigglePreorderTraversal(TreeNode<Point> treeNode, TreeNode<Point> parentNode, List<SvgPathCommand> renderedDecoCommands) {
//        //no filling
//
//        Point parentPoint = (parentNode == null) ? new Point() : parentNode.getData();
//
//        final List<SvgPathCommand> sguiggalized;
//        if (parentNode != null) {
//            sguiggalized = SvgPathCommand.sguiggalized(parentNode.getData(), treeNode.getData(), SvgPathCommand.CommandType.LINE_TO);
//        } else {
//            System.out.println("parent node is null");
//            (sguiggalized = new ArrayList<>()).add(new SvgPathCommand(treeNode.getData(), SvgPathCommand.CommandType.LINE_TO));
//        }
//        System.out.println(squiggleCommands.size() + "  0");
//        System.out.println("Squiggalized size: " + sguiggalized.size());
//        List<SvgPathCommand> sguiggalizedReverse = new ArrayList<>();
//        for (int i = sguiggalized.size() - 1; i >= 0; i--)
//            sguiggalizedReverse.add(sguiggalized.get(i));
//        squiggleCommands.addAll(sguiggalized);
//        System.out.println(squiggleCommands.size() + "  1");
//
//        //SvgFileProcessor.outputSvgCommands(squiggleCommands, "hey" + counter + "-1");
//        List<TreeNode<Point>> children = treeNode.getChildren();
//        TreeNode<Point>[] childArray = children.toArray(new TreeNode[children.size()]);
//
//        if (childArray.length == 0) {
//            System.out.println(squiggleCommands.size() + "  2");
//            PatternRenderer.insertPatternToList(renderedDecoCommands, squiggleCommands, treeNode.getData(), Point.getAngle(treeNode.getData(), parentPoint));
//            //  SvgFileProcessor.outputSvgCommands(squiggleCommands, "hey" + counter + "-3");
//            System.out.println(squiggleCommands.size() + "  3");
//            squiggleCommands.addAll(sguiggalizedReverse);
//            System.out.println(squiggleCommands.size() + "  4");
//
//        } else {
//            for (TreeNode<Point> child : childArray) {
//                System.out.println(squiggleCommands.size() + "  5");
////                sguigglePreorderTraversal(child, treeNode, renderedDecoCommands);
//                squiggleCommands.addAll(sguiggalizedReverse);
//                System.out.println(squiggleCommands.size() + "  6");
//                //   SvgFileProcessor.outputSvgCommands(squiggleCommands, "hey" + counter + "-2-" + (counter2++) );
//
//            }
//        }
//    }

    public List<SvgPathCommand> getSquiggleCommands() {
        return squiggleCommands;
    }
    public List<SvgPathCommand> getRenderedCommands() {
        return renderedCommands;
    }
}

