package src.jackiealgorithmicquilting;

import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.Pane;

/**
 * Created by JacquelineLi on 12/3/18.
 */
public class Axes extends Pane {
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    public Axes(int width, int height,
                double xLow, double xHi, double xTickUnit,
                double yLow, double yHi, double yTickUnit) {
        System.out.println("new axes created with xLow = " + xLow + " xHigh = " + xHi);
        setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        setPrefSize(width, height);
        setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        xAxis = new NumberAxis(xLow, xHi, xTickUnit);
        xAxis.setSide(Side.BOTTOM);
        xAxis.setMinorTickVisible(false);
        xAxis.setPrefWidth(width);
        xAxis.setLayoutY(height * (1 - ((0 - yLow) / (yHi - yLow))));


        yAxis = new NumberAxis(yLow, yHi, yTickUnit);
        yAxis.setSide(Side.LEFT);
        yAxis.setMinorTickVisible(false);
        yAxis.setPrefHeight(height);
        yAxis.setLayoutX(width * ((0 - xLow / (xHi - xLow))));
        getChildren().setAll(xAxis, yAxis);
    }

}
