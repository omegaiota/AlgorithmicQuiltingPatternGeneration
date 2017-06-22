package jackiesvgprocessor;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by JacquelineLi on 6/22/17.
 */
public class Vertex<T> {
    T data;
    List<Vertex<T>> neighbors;

    public Vertex(T data) {
        this.data = data;
        this.neighbors = new LinkedList<>();
    }

    public void connect(Vertex<T> otherNode) {
        otherNode.neighbors.add(this);
        this.neighbors.add(otherNode);
    }

    public boolean isConnected(Vertex<T> otherNode) {
        return neighbors.contains(otherNode);
    }
8
    public T getData() {
        return data;
    }

    public List<Vertex<T>> getNeighbors() {
        return neighbors;
    }

    public int neighborSize() {
        return  neighbors.size();
    }

    @Override
    public String toString() {
        String returnStr = "Vertex{" +
                "data=" + data +
                ", neighbors=";

        for (Vertex<T> neighbor : neighbors)
            returnStr += neighbor.toString();
        return returnStr;
    }
}
