package jackiesvgprocessor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {
    private final Button loadSpineButton = new Button("Load a spine file ...");
    private final Button loadFeatherButton = new Button("Load a pattern file ...");
    private final Button generateButton = new Button("Generate");
    private spinePatternMerger mergedPattern;
    private fileProcessor spineFile, featherFile;
    private ArrayList<svgPathCommands> spineCommands, featherCommands;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception{
        final FileChooser fileChooser = new FileChooser();
        loadSpineButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a spine ....");
                        spineFile = new fileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                spineFile.processSvg();
                                spineCommands = spineFile.getCommandLists().get(0);
                                spineFile.outputSvg();
                            } catch (ParserConfigurationException e1) {
                                e1.printStackTrace();
                            } catch (SAXException e1) {
                                e1.printStackTrace();
                            } catch (XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        loadFeatherButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a pattern....");
                        featherFile = new fileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                featherFile.processSvg();
                                featherCommands = featherFile.getCommandLists().get(0);
                                featherFile.outputSvg();
                            } catch (ParserConfigurationException e1) {
                                e1.printStackTrace();
                            } catch (SAXException e1) {
                                e1.printStackTrace();
                            } catch (XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        generateButton.setOnAction(
                e -> {
                        System.out.println("generating svg...");
                        mergedPattern = new spinePatternMerger(spineFile, featherFile, spineCommands, featherCommands,
                                spineFile.getfFileName(), featherFile.getfFileName());
                            /** Combine pattern */
                                mergedPattern.combinePattern();
                });

        stage.setTitle("changing title");
        stage.setScene(new Scene(setLayoutWithGraph(), Color.rgb(35, 39, 50)));
        stage.show();
    }

    public BorderPane setLayoutWithGraph() {
        BorderPane layout = new BorderPane();
        HBox buttons = new HBox(3);
        buttons.getChildren().addAll(loadSpineButton, loadFeatherButton, generateButton);
        layout.setBottom(buttons);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");

        return layout;
    }


}
