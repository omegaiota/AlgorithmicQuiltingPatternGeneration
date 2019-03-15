package src.jackiealgorithmicquilting;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Created by JacquelineLi on 12/3/18.
 */
public class Visualizer extends Application {
    public Stage myStage;
    Pane myPane = new BorderPane();

    public Visualizer(List<SvgPathCommand> commandList) {
        addCommandsPlot(commandList);
    }

    public static Pane addCommandsPlot(List<SvgPathCommand> commandList) {
        Pane myPane = new BorderPane();
        addCommandsPlot(commandList, myPane);
        return myPane;
    }

    public static void addCommandsPlot(List<SvgPathCommand> commandList, Pane myPane) {
        List<Point> allP = SvgPathCommand.toPoints(commandList);
        RectangleBound bound = RectangleBound.valueOf(allP);
        Axes axes = new Axes(
                (int) (bound.getWidth() + 100), (int) (bound.getHeight() + 100),
                bound.getLeft() - 50, bound.getRight() + 50, bound.getWidth() / 5.0,
                bound.getUp() - 50, bound.getDown() + 50, bound.getHeight() / 5.0
        );
        //path
        Path path = new Path();
        path.setStroke(Color.ORANGE.deriveColor(0, 1, 1, 0.6));
        path.setStrokeWidth(50);
        path.setClip(
                new Rectangle(
                        bound.getCenter().x, bound.getCenter().y,
                        bound.getWidth(),
                        bound.getHeight()
                )
        );

        path.getElements().add(new MoveTo(commandList.get(0).getDestinationPoint().x,
                commandList.get(0).getDestinationPoint().y));
        myPane.setStyle(("-fx-background-color: rgb(35, 39, 50);"));

        for (int i = 1; i < commandList.size(); i++) {
            SvgPathCommand currCommand = commandList.get(i);
            Point currPoint = currCommand.getDestinationPoint();

            switch (currCommand.getCommandType()) {
                case LINE_TO:
                    path.getElements().add(new LineTo(currPoint.x, currPoint.y));
                    break;
                case CURVE_TO:
                    path.getElements().add(new CubicCurveTo(
                            currCommand.getControlPoint1().x, currCommand.getControlPoint1().y, // c1
                            currCommand.getControlPoint2().x, currCommand.getControlPoint2().y, // c2
                            currPoint.x, currPoint.y));
                    break;
                case MOVE_TO:
                    path.getElements().add(new MoveTo(currPoint.x, currPoint.y));
                default:
                case DEFAULT:
                    path.getElements().add(new LineTo(currPoint.x, currPoint.y));
                    System.out.println("Not sure of command type:" + i);

            }
            if (path.getElements().size()> 10) {
                Plot newPlot = new Plot(path, axes, "rgb(200,100,270)");
//                Plot newPlot = new Plot(path, axes, String.format("rgb(%d,%d,70)",(int) (20 + 180 - i * 180.0 / commandList.size()), (int) (20 + i * 180.0 / commandList.size())));
                myPane.getChildren().addAll(newPlot);
                path = new Path();
                path.getElements().add(new MoveTo(currPoint.x, currPoint.y));
                myPane.setPrefHeight(newPlot.getPrefHeight());
                myPane.setPrefWidth(newPlot.getPrefWidth());
            }
        }
//        Plot myPlot = new Plot(path, axes);
//        myPane.getChildren().addAll(myPlot);
        myPane.setStyle(("-fx-background-color: rgb(35, 39, 50);"));

    }

    @Override
    public void start(final Stage primaryStage) {
        myStage = primaryStage;
        myStage.setScene(new Scene(setLayoutWithGraph(), Color.rgb(35, 39, 50)));
        myStage.setTitle("My New Stage Title");
        myStage.show();
    }

    public BorderPane setLayoutWithGraph() {
        BorderPane layout = new BorderPane();
        layout.setCenter(myPane);
        layout.setPrefWidth(1024);
        layout.setPrefHeight(1000);
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");

        return layout;
    }


}
