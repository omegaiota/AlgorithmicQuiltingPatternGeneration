package jackiesvgprocessor;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
    private final Button patternRotateButton = new Button("rotate patternGenerate");
    private final Button pathFillButton = new Button("fill Path");
    private final Button pathFillWithDecoButton = new Button("fill Path with decoration");
    private final Button pathPatternFillButton = new Button("Pebble fill");
    private final Button patternEchoButton = new Button("echo patternGenerate");
    private final Button svgToPatButton = new Button("svg to pat.");

    private SpinePatternMerger mergedPattern;
    private TextField textField = new TextField();
    private svgFileProcessor spineFile, featherFile, regionFile, stitchPathFile;

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
                        spineFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                spineFile.processSvg();
                                svgFileProcessor.outputSvgCommands(spineFile.getCommandLists().get(0), spineFile.getfFileName() + "-toAbsCoor");
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        svgToPatButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a spine ....");
                        spineFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                spineFile.processSvg();
                                svgFileProcessor.outputPat(spineFile.getCommandLists().get(0), spineFile.getfFileName());
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
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
                        featherFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                featherFile.processSvg();
                                svgFileProcessor.outputSvgCommands(featherFile.getCommandLists().get(0), featherFile.getfFileName() + "-toAbsCoor");
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
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
                        mergedPattern = new SpinePatternMerger(spineFile, featherFile, true);
                            /** Combine pattern */
                                mergedPattern.combinePattern();
                });
        generateNoRotateButton.setOnAction(
                e -> {
                    System.out.println("generating svg...");
                    mergedPattern = new SpinePatternMerger(spineFile, featherFile, false);
                    /** Combine pattern */
                    mergedPattern.combinePattern();
                });

        hbcGenerateButton.setOnAction(
                e -> {
                    System.out.println("generating hilbertCurve...");
                    HilbertCurveGenerator hilbertcurve = new HilbertCurveGenerator(new Point(0, 0), new Point(800, 0), new Point(0, 800), 4);
                    hilbertcurve.patternGeneration();
                    hilbertcurve.outputPath();
                });

        loadRegionButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a region ....");
                        regionFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                regionFile.processSvg();
                                regionFile.outputSvgCommands(regionFile.getCommandLists().get(0),"region-" + regionFile.getfFileName());
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
        tessellationButton.setOnAction(
                e -> {
                    System.out.println("generating tessellation...");
                    Region boundary = regionFile.getBoundary();
                    Distribution distribute = new Distribution(Distribution.typeGridTessellation, boundary, 20, regionFile);
                    distribute.generate();
                    distribute.outputDistribution();
                    distribute.getTraversalSvg();
                });

        patternRotateButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a region ....");
                        stitchPathFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                stitchPathFile.processSvg();
                                svgFileProcessor.outputSvgCommands(stitchPathFile.getCommandLists().get(0), stitchPathFile.getfFileName() + "-toAbs");
                                PatternRenderer renderer = new PatternRenderer(stitchPathFile, PatternRenderer.RenderType.ROTATION);
                                renderer.repeatWithRotation(Double.valueOf(textField.getText()));
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
        patternEchoButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a region ....");
                        stitchPathFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                stitchPathFile.processSvg();
                                svgFileProcessor.outputSvgCommands(stitchPathFile.getCommandLists().get(0), stitchPathFile.getfFileName() + "-toAbs");
                                PatternRenderer renderer = new PatternRenderer(stitchPathFile, PatternRenderer.RenderType.ECHO);
                                renderer.echoPattern(Integer.valueOf(textField.getText()));
                            } catch (ParserConfigurationException | SAXException e1) {
                                e1.printStackTrace();
                            } catch (XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        pathFillButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a region ....");
                        stitchPathFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                stitchPathFile.processSvg();
                                svgFileProcessor.outputSvgCommands(stitchPathFile.getCommandLists().get(0), stitchPathFile.getfFileName() + "-toAbs");
                                PatternRenderer renderer = new PatternRenderer(stitchPathFile, PatternRenderer.RenderType.NO_DECORATION);
                                renderer.fixedWidthFilling(5);
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        pathFillWithDecoButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a region ....");
                        stitchPathFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                stitchPathFile.processSvg();
                                svgFileProcessor.outputSvgCommands(stitchPathFile.getCommandLists().get(0), stitchPathFile.getfFileName() + "-toAbs");
                                PatternRenderer renderer = new PatternRenderer(stitchPathFile, featherFile, PatternRenderer.RenderType.WITH_DECORATION);
                                renderer.fixedWidthFilling(5);
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        /* Pebble */
        pathPatternFillButton.setOnAction(
                e -> {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a region ....");
                        regionFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                regionFile.processSvg();
                                svgFileProcessor.outputSvgCommands(regionFile.getCommandLists().get(0), regionFile.getfFileName() + "-toAbs");
                                Region boundary = regionFile.getBoundary();
                                Distribution distribute = new Distribution(Distribution.typeGridTessellation, boundary, 20, regionFile);
                                distribute.generate();
                                distribute.toRegularGraph();
                                distribute.toSpanningTree();
                                distribute.outputDistribution();
                                svgFileProcessor.outputSvgCommands(regionFile.getCommandLists().get(0), regionFile.getfFileName() + "-toAbs");

                                PatternRenderer renderer = new PatternRenderer(distribute.getSpanningTree(), PatternRenderer.RenderType.LANDFILL);
                                renderer.landFill();
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    textField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        stage.setTitle("changing title");
        stage.setScene(new Scene(setLayoutWithGraph(), Color.rgb(35, 39, 50)));
        stage.show();
    }

    public BorderPane setLayoutWithGraph() {
        BorderPane layout = new BorderPane();
        VBox patternSpineCombine = new VBox(3);
        VBox pathGeneration = new VBox(3);
        VBox patternRotate = new VBox( 3);
        VBox pathRender = new VBox( 3);
        HBox menu = new HBox(5);
        patternSpineCombine.getChildren().addAll(svgToPatButton, loadSpineButton, loadFeatherButton, generateRotateButton, generateNoRotateButton);
        pathGeneration.getChildren().addAll(hbcGenerateButton, loadRegionButton, tessellationButton);
        patternRotate.getChildren().addAll(patternRotateButton, patternEchoButton, textField);
        pathRender.getChildren().addAll(pathFillButton, pathFillWithDecoButton, pathPatternFillButton);
        menu.getChildren().addAll(patternSpineCombine, pathGeneration, patternRotate, pathRender);
        layout.setBottom(menu);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");

        return layout;
    }


}
