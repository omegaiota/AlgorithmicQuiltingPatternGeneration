package jackiealgorithmicquilting;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by JacquelineLi on 6/22/17.
 */
public class Vertex<T> {
    private T data;
    private List<Vertex<T>> neighbors;
    private List<Integer> weight;
    public Vertex(T data) {
        this.data = data;
        this.neighbors = new ArrayList<>();
        this.weight = new ArrayList<>();
    }

    public void connect(Vertex<T> otherNode) {
        if (otherNode == null)
            return;
        Random rand = new Random();
        int weight = rand.nextInt(100) + 1;
        otherNode.neighbors.add(this);
        otherNode.weight.add(weight);
        this.neighbors.add(otherNode);
        this.weight.add(weight);
    }

    public T getData() {
        return data;
    }

    public List<Integer> getWeight() {
        return weight;
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
                "data=" + data.toString() +
                ", neighbors=";

        for (Vertex<T> neighbor : neighbors)
            returnStr += neighbor.data.toString();
        return returnStr;
    }



}
