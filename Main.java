package jackiesvgprocessor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;

public class Main extends Application {
    private final Button loadSpineButton = new Button("Load a spine file ...");
    private final Button loadRegionButton = new Button("Load region ...");
    private final Button loadFeatherButton = new Button("Load a pattern file ...");
    private final Button generateRotateButton = new Button("Generate - rotate on");
    private final Button generateNoRotateButton = new Button("Generate - rotate off");
    private final Button tessellationButton = new Button("tessellation generate");
    private final Button hbcGenerateButton = new Button("hilbert curve Generate");
    private spinePatternMerger mergedPattern;
    private fileProcessor spineFile, featherFile, regionFile;

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
                                fileProcessor.outputSvgCommands(spineFile.getCommandLists().get(0), spineFile.getfFileName() + "-toAbsCoor");
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
                                fileProcessor.outputSvgCommands(featherFile.getCommandLists().get(0), featherFile.getfFileName() + "-toAbsCoor");
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

        generateRotateButton.setOnAction(
                e -> {
                        System.out.println("generating svg...");
                        mergedPattern = new spinePatternMerger(spineFile, featherFile, true);
                            /** Combine pattern */
                                mergedPattern.combinePattern();
                });
        generateNoRotateButton.setOnAction(
                e -> {
                    System.out.println("generating svg...");
                    mergedPattern = new spinePatternMerger(spineFile, featherFile, false);
                    /** Combine pattern */
                    mergedPattern.combinePattern();
                });

        hbcGenerateButton.setOnAction(
                e -> {
                    System.out.println("generating hilbertCurve...");
                    hilbertCurveGenerator hilbertcurve = new hilbertCurveGenerator(new Point(0, 0), new Point(800, 0), new Point(0, 800), 4);
                    hilbertcurve.patternGeneration();
                    hilbertcurve.outputPath();
                });

        loadRegionButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a region ....");
                        regionFile = new fileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                regionFile.processSvg();
                                regionFile.outputSvgCommands(regionFile.getCommandLists().get(0),"region-" + regionFile.getfFileName());
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
        tessellationButton.setOnAction(
                e -> {
                    System.out.println("generating tesselation...");
                    Region boundary = regionFile.getBoundary();
                    Distribution distribute = new Distribution(Distribution.typeTTFTFTessellation, boundary, 20, regionFile);
                    distribute.generate();
                    distribute.outputDistribution();
                });

        stage.setTitle("changing title");
        stage.setScene(new Scene(setLayoutWithGraph(), Color.rgb(35, 39, 50)));
        stage.show();
    }

    public BorderPane setLayoutWithGraph() {
        BorderPane layout = new BorderPane();
        VBox patternSpineCombine = new VBox(3);
        VBox pathGeneration = new VBox(3);
        HBox menu = new HBox(5);
        patternSpineCombine.getChildren().addAll(loadSpineButton, loadFeatherButton, generateRotateButton, generateNoRotateButton);
        pathGeneration.getChildren().addAll(hbcGenerateButton, loadRegionButton, tessellationButton);
        menu.getChildren().addAll(patternSpineCombine, pathGeneration);
        layout.setBottom(menu);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");

        return layout;
    }


}
