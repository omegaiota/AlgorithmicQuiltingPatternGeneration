package src.jackiealgorithmicquilting;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/22/17.
 */
public class TreeNode<T> {
   private T data;
   private TreeNode<T> parent;
   private List<Vertex<T>> neighbors;
   private List<TreeNode<T>> children;
    private CircleBound boundingCircle;
    private RectangleBound boundingRectangle;

   public TreeNode(T data, List<Vertex<T>> neighbors) {
       this.data = data;
       this.children = new ArrayList<>();
       this.neighbors = neighbors;
   }

    public TreeNode(T data) {
        this.data = data;
        this.children = new ArrayList<>();
        this.neighbors = new ArrayList<>();
    }

    public CircleBound getBoundingCircle() {
        return boundingCircle;
    }

    public void setBoundingCircle(CircleBound boundingCircle) {
        this.boundingCircle = boundingCircle;
    }



    public void addChild(TreeNode<T> child) {
        this.children.add(child);
        child.setParent(this);
    }

    public void removeChild(TreeNode<T> child) {
        for (TreeNode<T> myChild : children) {
            if (myChild.equals(child)) {
                children.remove(child);
                break;
            }

        }
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public TreeNode<T> getParent() {
        return parent;
    }

    public void setParent(TreeNode<T> parent) {
        this.parent = parent;
    }

    public List<TreeNode<T>> getChildren() {
        return new ArrayList<>(children);
    }


    public void setChildren(List<TreeNode<T>> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TreeNode)) return false;

        TreeNode<?> treeNode = (TreeNode<?>) o;

        if (!data.equals(treeNode.data)) return false;
        if (parent != null ? !parent.equals(treeNode.parent) : treeNode.parent != null) return false;
        if (!neighbors.equals(treeNode.neighbors)) return false;
        return children.equals(treeNode.children);
    }

    @Override
    public int hashCode() {
        int result = data.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }
}
