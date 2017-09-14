package jackiequiltpatterndeterminaiton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by JacquelineLi on 6/22/17.
 */
public class Graph<T> {
    private ArrayList<Vertex<T>> mVertices;
    private int V = 0,E = 0;
    private double disLen = 0;

    public Graph(double disLen) {
        mVertices = new ArrayList<>();
    }

    public TreeNode<T> generateSpanningTree() {
        boolean flag = true;
        HashSet<Vertex<T>> toInclude = new HashSet<>();
        HashMap<Vertex<T>, TreeNode<T>> vertexTreeNodeHashMap = new HashMap<>();
        TreeNode<T> root = new TreeNode<>(mVertices.get(0).getData(), mVertices.get(0).getNeighbors());
        vertexTreeNodeHashMap.put(mVertices.get(0), root);
        toInclude.addAll(mVertices);
        toInclude.remove(mVertices.get(0));

        while (!toInclude.isEmpty() && flag) {
            //System.out.println("Remaining:" + toInclude.size());
         int minCost = 10000;
         Vertex<T> minVertex = null, parentVertex = null;
         for (Vertex<T> key : vertexTreeNodeHashMap.keySet()) {
             assert(toInclude.contains(key) == false);
             for (int i = 0; i < key.neighborSize(); i++) {
                 if ((key.getWeight().get(i) < minCost) && toInclude.contains(key.getNeighbors().get(i))) {
                     minCost = key.getWeight().get(i);
                     minVertex = key.getNeighbors().get(i);
                     parentVertex = key;
                 }
             }
         }
         assert(toInclude.size() + vertexTreeNodeHashMap.size() == mVertices.size());
         /* Some vertices might not be connected  */
         if (minVertex != null) {
             TreeNode<T> newNode = new TreeNode<>(minVertex.getData(), minVertex.getNeighbors());
             newNode.setParent(vertexTreeNodeHashMap.get(parentVertex));
             vertexTreeNodeHashMap.get(parentVertex).addChild(newNode);
             vertexTreeNodeHashMap.put(minVertex, newNode);
             toInclude.remove(minVertex);
         } else {
             flag = false;
         }
        }
        return root;
    }

    public void addVertex(Vertex<T> vertex) {
        mVertices.add(vertex);
        V++;
    }


    public ArrayList<Vertex<T>> getVertices() {
        return mVertices;
    }

    public int getV() {
        return V;
    }

    public int getE() {
        return E;
    }
}
