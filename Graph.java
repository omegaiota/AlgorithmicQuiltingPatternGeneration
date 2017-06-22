package jackiesvgprocessor;

import java.util.ArrayList;

/**
 * Created by JacquelineLi on 6/22/17.
 */
public class Graph<T> {
    private ArrayList<Vertex<T>> mVertices;
    private int V = 0,E = 0;

    public Graph() {
        mVertices = new ArrayList<>();
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
