package jackiesvgprocessor;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/22/17.
 */
public class TreeNode<T> {
   T data;
   TreeNode<T> parent;
   List<TreeNode<T>> children;

   public TreeNode(T data) {
       this.data = data;
       this.children = new LinkedList<>();
   }

   public TreeNode<T> addChild(T child) {
       TreeNode<T> childNode = new TreeNode<T>(child);
       childNode.parent = this;
       this.children.add(childNode);
       return childNode;
   }
 }
