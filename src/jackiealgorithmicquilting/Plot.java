package src.jackiealgorithmicquilting;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;

/**
 * Created by JacquelineLi on 12/3/18.
 */
public class Plot extends Pane {
    public Plot(Path path, Axes axes, String colorStr) {
        path.setStroke(Color.valueOf(colorStr));
        path.setStrokeWidth(1);

        path.setClip(
                new Rectangle(
                        0, 0,
                        axes.getPrefWidth(),
                        axes.getPrefHeight()
                )
        );

        setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);
        setPrefSize(axes.getPrefWidth(), axes.getPrefHeight());
        setMaxSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE);

        getChildren().setAll(axes, path);
    }


}