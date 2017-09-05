package jackiequiltpatterndeterminaiton;

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

   public TreeNode(T data, List<Vertex<T>> neighbors) {
       this.data = data;
       this.children = new ArrayList<>();
       this.neighbors = neighbors;
   }

    public void addChild(TreeNode<T> child) {
        this.children.add(child);
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
//       naiveSorting();
       return children;
    }

    public void setChildren(List<TreeNode<T>> children) {
        this.children = children;
    }
}
