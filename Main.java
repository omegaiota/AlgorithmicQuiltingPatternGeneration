package jackiesvgprocessor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
    private final Button pebbleButton = new Button("Pebble fill");
    private final Button patternEchoButton = new Button("echo patternGenerate");
    private final Button svgToPatButton = new Button("svg to pat.");
    private final Button generateButton = new Button("Generate");

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
                    Distribution distribute = new Distribution(Distribution.RenderType.GRID, boundary, 20, regionFile);
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
                                renderer.repeatWithRotation(Integer.valueOf(textField.getText()));
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
        pebbleButton.setOnAction(
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
                                Distribution distribute = new Distribution(Distribution.RenderType.RANDOM, boundary, 20, regionFile);
                                distribute.generate();
                                distribute.toRegularGraph();
                                distribute.outputDistribution();
                                distribute.toSpanningTree();
                                TreeTraversal traversal = new TreeTraversal(distribute.getSpanningTree());
                                traversal.traverseTree();
                                svgFileProcessor.outputSvgCommands(traversal.getRenderedCommands(), regionFile.getfFileName() + "-traversal");
                                svgFileProcessor.outputSvgCommands(regionFile.getCommandLists().get(0), regionFile.getfFileName() + "-toAbs");

                                PatternRenderer renderer = new PatternRenderer(distribute.getSpanningTree(), distribute.getPointGraph(), PatternRenderer.RenderType.LANDFILL);
                                renderer.landFill();
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });



        stage.setTitle("Quilt Pattern Generation");
        stage.setScene(new Scene(setLayoutWithGraph(stage), Color.rgb(35, 39, 50)));
        stage.show();
    }

    public BorderPane setLayoutWithGraphN() {
        BorderPane layout = new BorderPane();
        VBox patternSpineCombine = new VBox(3);
        VBox pathGeneration = new VBox(3);
        VBox patternRotate = new VBox( 3);
        VBox pathRender = new VBox( 3);
        HBox menu = new HBox(5);
        patternSpineCombine.getChildren().addAll(svgToPatButton, loadSpineButton, loadFeatherButton, generateRotateButton, generateNoRotateButton);
        pathGeneration.getChildren().addAll(hbcGenerateButton, loadRegionButton, tessellationButton);
        patternRotate.getChildren().addAll(patternRotateButton, patternEchoButton, textField);
        pathRender.getChildren().addAll(pathFillButton, pathFillWithDecoButton, pebbleButton);
        menu.getChildren().addAll(patternSpineCombine, pathGeneration, patternRotate, pathRender);
        layout.setBottom(menu);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");
        return layout;
    }

    public BorderPane setLayoutWithGraph(Stage stage) {
        BorderPane layout = new BorderPane();
        int columnItemSpacing = 15;
        VBox regionColumn = new VBox(columnItemSpacing);
        VBox skeletonColumn = new VBox(columnItemSpacing);
        VBox patternColumn = new VBox(columnItemSpacing);

        int columnItemBundleSpacing = 3;
        VBox regionSelection = new VBox(columnItemBundleSpacing);
        VBox skeletonGeneration = new VBox(columnItemBundleSpacing);
        VBox skeletonRendering = new VBox( columnItemBundleSpacing);
        VBox patternSelection = new VBox(columnItemBundleSpacing);


        VBox pathRender = new VBox( 3);
        Font columnLabelFont = new Font("Luminari", 22);
        Font functionLabelFont = new Font("Avenir Light", 14);
        Font buttonFont = new Font("Avenir", 10);
        Color labelColor = Color.ORANGE;
        Color columnLabelColor = Color.SILVER;
        HBox menu = new HBox(15);

        //Region
        final Label regionLabel = new Label("Region");
        regionLabel.setFont(columnLabelFont);
        regionLabel.setTextFill(columnLabelColor);

        /* Region Selection*/
        loadRegionButton.setFont(buttonFont);
        final Label regionSelectionLabel = new Label("Select Region");
        regionSelectionLabel.setFont(functionLabelFont);
        regionSelectionLabel.setTextFill(labelColor);
        regionSelection.getChildren().addAll(regionSelectionLabel,loadRegionButton);

        regionColumn.getChildren().addAll(regionLabel, regionSelection);

        //Skeleton
        final Label skeletonLabel = new Label("Skeleton Path");
        skeletonLabel.setFont(columnLabelFont);
        skeletonLabel.setTextFill(columnLabelColor);

        /* Skeleton Path Generation */
        final Label skeletonGnerationlabel = new Label("Skeleton Path Generation");
        skeletonGnerationlabel.setFont(functionLabelFont);
        skeletonGnerationlabel.setTextFill(labelColor);
        ComboBox skeletonGenComboBox = new ComboBox();
        skeletonGenComboBox.getItems().addAll("Grid Tessellation", "3.3.4.3.4 Tessellation",
                "Hilbert Curve", "Echo", "Medial Axis");
        skeletonGeneration.getChildren().addAll(skeletonGnerationlabel, skeletonGenComboBox);

        /* Skeleton Path Rendering */
        final Label skeletonRenderinglabel = new Label("Skeleton Path Rendering");
        skeletonRenderinglabel.setFont(functionLabelFont);
        skeletonRenderinglabel.setTextFill(labelColor);
        ComboBox skeletonRenderComboBox = new ComboBox();
        skeletonRenderComboBox.getItems().addAll("No Rendering", "Fixed-width Filling", "Squiggles", "Pebble");
        skeletonRendering.getChildren().addAll(skeletonRenderinglabel, skeletonRenderComboBox);

        skeletonColumn.getChildren().addAll(skeletonLabel ,skeletonGeneration, skeletonRendering);

        //Pattern Column
        final Label patternLabel = new Label("Pattern");
        patternLabel.setFont(columnLabelFont);
        patternLabel.setTextFill(columnLabelColor);

        /* Pattern selection*/
        final Label patternSelectionLabel = new Label("Selecet Pattern");
        patternSelectionLabel.setFont(functionLabelFont);
        patternSelectionLabel.setTextFill(labelColor);

        /*  Toggle group*/
        final ToggleGroup patternSourceGroup = new ToggleGroup();
        HBox patternSourceBox = new HBox(2);
        HBox fileSourceBox = new HBox(2);
        ToggleButton patternFromFile = new ToggleButton("from file");
        patternFromFile.setFont(buttonFont);

        ToggleButton noPattern = new ToggleButton("none");
        noPattern.setFont(buttonFont);
        ToggleButton patternFromLibrary = new ToggleButton("from library");
        patternFromLibrary.setFont(buttonFont);
        
        patternSourceGroup.getToggles().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternFromLibrary.setSelected(true);
        patternSourceBox.getChildren().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSelection.getChildren().addAll(patternSelectionLabel, patternSourceBox);

        ComboBox patternLibraryComboBox = new ComboBox();


        patternColumn.getChildren().addAll(patternLabel ,patternSelection, fileSourceBox);

        menu.getChildren().addAll(regionColumn, skeletonColumn, patternColumn);

        layout.setCenter(menu);
        layout.setBottom(generateButton);
        generateButton.setFont(columnLabelFont);
        generateButton.setTextFill(Color.DARKBLUE);
        layout.setPadding(new Insets(60));
        BorderPane.setAlignment(generateButton, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(generateButton, new Insets(20, 8, 8, 8));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");

        //Selection Listeners
        /* Pattern Source Listener */
        patternLibraryComboBox.getItems().addAll("feather");
        patternSourceGroup.selectedToggleProperty().addListener((ov, toggle, new_toggle) -> {
            if (new_toggle == null) {
                fileSourceBox.getChildren().removeAll();
            } else {
                if (patternSourceGroup.getSelectedToggle() == patternFromFile) {
                    System.out.println("New Pattern Source: Pattern From File ");
                    fileSourceBox.getChildren().setAll(loadFeatherButton);
                } else if (patternSourceGroup.getSelectedToggle() == patternFromLibrary){
                    System.out.println("New Pattern Source: Pattern From Library ");
                    fileSourceBox.getChildren().setAll(patternLibraryComboBox);
                } else if (patternSourceGroup.getSelectedToggle() == noPattern) {
                    System.out.println("New Pattern Source: No Pattern");
                    fileSourceBox.getChildren().removeAll(loadFeatherButton, patternLibraryComboBox);
                }
            }
        });

        /* Skeleton Generation Listener */
        skeletonGenComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            String newSelected = skeletonGenComboBox.getValue().toString();
            System.out.println("Skeleton generation method changed: ");
            if (newSelected.equals("3.3.4.3.4 Tessellation") || newSelected.equals("Grid Tessellation")) {
                System.out.println("case 1: tree structure");
                skeletonRenderComboBox.getItems().setAll("No Rendering", "Fixed-width Filling", "Squiggles", "Pebble");
            } else if (newSelected.equals( "Hilbert Curve") || newSelected.equals( "Echo") || newSelected.equals( "Medial Axis") ) {
                System.out.println("case 2: none tree structure");
                skeletonRenderComboBox.getItems().setAll("No Rendering", "Squiggles");
            }
        });

        //Buttons
        loadRegionButton.setOnAction(
                e -> {
                    final FileChooser fileChooser = new FileChooser();
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

        generateButton.setOnAction(
                e -> {
                    /* Set Boundary */
                    Region boundary = regionFile.getBoundary();

                    /* Skeleton Path Generation */
                    Distribution distribute;
                    switch (skeletonGenComboBox.getButtonCell().getText()) {
                        case "Grid Tessellation":
                            System.out.println("Skeleton Path: Grid Tessellation");
                            distribute = new Distribution(Distribution.RenderType.GRID,
                                    boundary, 20, regionFile);
                            distribute.generate();
                            distribute.outputDistribution();
                            distribute.getTraversalSvg();
                            break;
                        case "3.3.4.3.4 Tessellation":
                            System.out.println("Skeleton Path: 3.3.4.3.4 Tessellation");
                            distribute = new Distribution(Distribution.RenderType.THREE_THREE_FOUR_THREE_FOUR,
                                    boundary, 20, regionFile);
                            distribute.generate();
                            distribute.outputDistribution();
                            distribute.getTraversalSvg();
                            break;
                        case "Hilbert Curve":
                            System.out.println("Skeleton Path: Hilbert Curve...");
                            HilbertCurveGenerator hilbertcurve = new HilbertCurveGenerator(new Point(0, 0), new Point(800, 0), new Point(0, 800), 4);
                            hilbertcurve.patternGeneration();
                            hilbertcurve.outputPath();
                            break;
                        case "Echo":
                            System.out.println("Skeleton Path: Echo...");
                            PatternRenderer renderer = new PatternRenderer(regionFile, PatternRenderer.RenderType.ECHO);
                            renderer.echoPattern(Integer.valueOf(textField.getText()));
                            break;
                        case "Medial Axis":
                            System.out.println("Skeleton Path: Medial Axis...");
                            break;
                    }

                    /* Skeleton Path Rendering */
                    switch (skeletonRenderComboBox.getButtonCell().getText()) {
                        case "Fixed-width Filling":
                            break;
                        case "3.3.4.3.4 Tessellation":
                            break;
                        case "Squiggles":
                            break;
                        case "Pebble":
                            break;
                        case "No Rendering":
                            break;
                    }
                });
        return layout;
    }



}
