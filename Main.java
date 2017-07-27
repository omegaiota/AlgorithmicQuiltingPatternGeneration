package jackiesvgprocessor;

import javafx.application.Application;
import javafx.event.ActionEvent;
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
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
    /* Constant */
    private static final int columnItemSpacing = 15, columnItemBundleSpacing = 3;

    /* Buttons */
    private final Button loadRegionButton = new Button("Load region ...");
    private final Button loadDecoElementButton = new Button("Load a pattern file ...");
    private final Button loadSvgFileButton = new Button("Load SVG file");
    private final Button generateButton = new Button("Generate");

    /* TextField */
    private TextField textField = new TextField(), patternRenderTextFiled = new TextField();

    /* File processor, renderer */
    private svgFileProcessor skeletonPathFile, decoElementFile, regionFile, svgFile;
    private SpinePatternMerger mergedPattern;
    PatternRenderer patternRenderer;

    /* Layout: VBox, HBox*/
    //Column
    VBox regionColumn = new VBox(columnItemSpacing), skeletonColumn = new VBox(columnItemSpacing),
            patternColumn = new VBox(columnItemSpacing), toolColumn = new VBox(columnItemSpacing);
    //Label + item
    VBox regionSelection = new VBox(columnItemBundleSpacing), skeletonGeneration = new VBox(columnItemBundleSpacing),
            skeletonRendering = new VBox(columnItemBundleSpacing), patternRendering = new VBox(columnItemBundleSpacing),
            patternPropertyInput = new VBox(columnItemBundleSpacing), svgToPat = new VBox(columnItemBundleSpacing),
            pathRender = new VBox(3), patternSelection = new VBox(columnItemBundleSpacing);
    HBox menu = new HBox(15);
    HBox patternSourceBox = new HBox(2);
    HBox fileSourceBox = new HBox(2);
    //ComboBox
    ComboBox skeletonGenComboBox = new ComboBox(), skeletonRenderComboBox = new ComboBox(),
            patternLibraryComboBox = new ComboBox(), patternRenderComboBox = new ComboBox();
    /* Fonts */
    Font columnLabelFont = new Font("Luminari", 22), functionLabelFont = new Font("Avenir Light", 14),
            buttonFont = new Font("Avenir", 10);
    Color labelColor = Color.ORANGE, columnLabelColor = Color.SILVER;

    /* Labels */
    final Label textFieldLabel = new Label(), patternRenderFieldLabel = new Label("Repetitions"), regionLabel = new Label("Region"),
            regionSelectionLabel = new Label("Select Region"), skeletonLabel = new Label("Skeleton Path"),
            skeletonGnerationlabel = new Label("Skeleton Path Generation"), patternLabel = new Label("Pattern"),
            patternSelectionLabel = new Label("Select Pattern"), skeletonRenderinglabel = new Label("Skeleton Path Rendering"),
            patternRenderLabel = new Label("Pattern Rendering"), toolLabel = new Label("Tools"),
            svgToPatLabel = new Label(".SVG to .PAT");


    /* Toggle Group */
    final ToggleGroup patternSourceGroup = new ToggleGroup();
    ToggleButton patternFromFile = new ToggleButton("from file"), noPattern = new ToggleButton("none"),
            patternFromLibrary = new ToggleButton("from library");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final FileChooser fileChooser = new FileChooser();
        stage.setTitle("Quilt Pattern Generation");
        stage.setScene(new Scene(setLayoutWithGraph(stage), Color.rgb(35, 39, 50)));
        stage.show();
    }

    public BorderPane setLayoutWithGraph(Stage stage) {
        BorderPane layout = new BorderPane();
        setUpFont();

        //Region
        /* Region Selection*/
        regionSelection.getChildren().addAll(regionSelectionLabel, loadRegionButton);
        regionColumn.getChildren().addAll(regionLabel, regionSelection);

        //Skeleton
        /* Skeleton Path Generation */
        skeletonGenComboBox.getItems().addAll("Grid Tessellation", "3.3.4.3.4 Tessellation",
                "Hilbert Curve", "Echo", "Medial Axis");
        skeletonGenComboBox.setValue("Grid Tessellation");
        skeletonGeneration.getChildren().addAll(skeletonGnerationlabel, skeletonGenComboBox);

        /* Skeleton Path Rendering */
        skeletonRenderComboBox.getItems().addAll("No Rendering", "Fixed-width Filling", "Squiggles", "Pebble");
        skeletonRendering.getChildren().addAll(skeletonRenderinglabel, skeletonRenderComboBox);
        skeletonRenderComboBox.setValue("No Rendering");
        skeletonColumn.getChildren().addAll(skeletonLabel, skeletonGeneration, skeletonRendering);

        //Pattern Column

        /* Pattern selection*/

        /*  Toggle group*/
        patternSourceGroup.getToggles().addAll(patternFromFile, patternFromLibrary, noPattern);
        noPattern.setSelected(true);
        patternSourceBox.getChildren().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSelection.getChildren().addAll(patternSelectionLabel, patternSourceBox, fileSourceBox);

        /* Pattern rendering */
        patternRenderComboBox.getItems().addAll("Repeat with Rotation", "Echo", "No Rendering");
        patternRenderComboBox.setValue("No Rendering");
        patternRendering.getChildren().setAll(patternRenderLabel, patternRenderComboBox);
        patternColumn.getChildren().addAll(patternLabel, patternSelection, patternRendering, patternPropertyInput);


        //Tool Column
        //svgToPat
        svgToPat.getChildren().addAll(svgToPatLabel, loadSvgFileButton);
        toolColumn.getChildren().addAll(toolLabel, svgToPat);
        menu.getChildren().addAll(regionColumn, skeletonColumn, patternColumn, toolColumn);

        layout.setCenter(menu);
        layout.setBottom(generateButton);

        layout.setPadding(new Insets(60));
        BorderPane.setAlignment(generateButton, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(generateButton, new Insets(20, 8, 8, 8));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");


        setupListeners();
        buttonActions(stage);

        //Buttons, File loader

        return layout;
    }

    private void buttonActions(Stage stage) {
        loadSvgFileButton.setOnAction(
                e -> {
                    final FileChooser fileChooser = new FileChooser();
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a spine ....");
                        svgFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                svgFile.processSvg();
                                svgFileProcessor.outputPat(svgFile.getCommandLists().get(0), svgFile.getfFileName());
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

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
                                regionFile.outputSvgCommands(regionFile.getCommandLists().get(0),
                                        "region-" + regionFile.getfFileName());
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        loadDecoElementButton.setOnAction(
                e -> {
                    final FileChooser fileChooser = new FileChooser();
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        System.out.println("Loading a pattern....");
                        decoElementFile = new svgFileProcessor(file);
                        try {
                            /** Process the svg file */
                            try {
                                decoElementFile.processSvg();
                                svgFileProcessor.outputSvgCommands(decoElementFile.getCommandLists().get(0),
                                        "decoElem-" + decoElementFile.getfFileName());
                            } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                                e1.printStackTrace();
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        generateButton.setOnAction(
                (ActionEvent e) -> {
                    /* Set Boundary */
                    Region boundary = regionFile.getBoundary();
                    /* Pattern Selection */
                    String decoFileName = "noDeco";
                    List<SvgPathCommand> decoCommands = new ArrayList<>();
                    switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                        case "none":
                            break;
                        case "from file":
                            decoCommands = decoElementFile.getCommandLists().get(0);
                            decoFileName = decoElementFile.getfFileName();
                            break;
                        case "from library":
                            break;

                    }
                    /* Pattern rendering */
                    svgFileProcessor renderedDecoElemFileProcessor = null;
                    List<SvgPathCommand> renderedDecoCommands = new ArrayList<>();
                    switch (patternRenderComboBox.getValue().toString()) {
                        case "No Rendering":
                            renderedDecoCommands = decoCommands;
                            renderedDecoElemFileProcessor = decoElementFile;
                            break;
                        case "Repeat with Rotation":
                            patternRenderer = new PatternRenderer(decoFileName, decoCommands, PatternRenderer.RenderType.ROTATION);
                            patternRenderer.repeatWithRotation(Integer.valueOf(patternRenderTextFiled.getText()));
                            renderedDecoCommands = patternRenderer.getRenderedCommands();
                            renderedDecoElemFileProcessor = new svgFileProcessor(patternRenderer.outputRotated(Integer.valueOf(patternRenderTextFiled.getText())));
                            break;
                        case "Echo":
                            patternRenderer = new PatternRenderer(decoFileName, decoCommands, PatternRenderer.RenderType.ECHO);
                            patternRenderer.echoPattern(Integer.valueOf(patternRenderTextFiled.getText()));
                            renderedDecoCommands = patternRenderer.getRenderedCommands();
                            renderedDecoElemFileProcessor = new svgFileProcessor(patternRenderer.outputEchoed(Integer.valueOf(patternRenderTextFiled.getText())));
                            break;
                    }

                    try {
                        renderedDecoElemFileProcessor.processSvg();
                    } catch (ParserConfigurationException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (SAXException e1) {
                        e1.printStackTrace();
                    } catch (XPathExpressionException e1) {
                        e1.printStackTrace();
                    }

                    /* Skeleton Path Generation */
                    Distribution distribute;
                    List<SvgPathCommand> skeletonPathCommands = new ArrayList<>();
                    TreeNode<Point> skeletonSpanningTree = null;
                    File skeletonPathFile = null;

                    switch (skeletonGenComboBox.getValue().toString()) {
                        case "Grid Tessellation":
                            System.out.println("Skeleton Path: Grid Tessellation");
                            distribute = new Distribution(Distribution.RenderType.GRID,
                                    boundary, 20, regionFile);
                            distribute.generate();
                            distribute.outputDistribution();
                            skeletonPathCommands = distribute.toTraversal();
                            skeletonPathFile = svgFileProcessor.outputSvgCommands(skeletonPathCommands,
                                    "traversal-" + regionFile.getfFileName() + "-" + "GRID" + "-dis-" + 20);
                            skeletonSpanningTree = distribute.getSpanningTree();
                            break;
                        case "3.3.4.3.4 Tessellation":
                            System.out.println("Skeleton Path: 3.3.4.3.4 Tessellation");
                            distribute = new Distribution(Distribution.RenderType.THREE_THREE_FOUR_THREE_FOUR,
                                    boundary, 20, regionFile);
                            distribute.generate();
                            distribute.outputDistribution();
                            skeletonPathCommands = distribute.toTraversal();
                            skeletonPathFile = svgFileProcessor.outputSvgCommands(skeletonPathCommands,
                                    "traversal-" + regionFile.getfFileName() + "-" + "TTFTF" + "-dis-" + 20);
                            skeletonSpanningTree = distribute.getSpanningTree();
                            break;

                        case "Hilbert Curve":
                            System.out.println("Skeleton Path: Hilbert Curve...");
                            HilbertCurveGenerator hilbertcurve = new HilbertCurveGenerator(new Point(0, 0),
                                    new Point(800, 0), new Point(0, 800), 4);
                            hilbertcurve.patternGeneration();
                            skeletonPathFile = hilbertcurve.outputPath();
                            skeletonPathCommands = hilbertcurve.getCommandList();
                            break;
                        case "Echo":
                            System.out.println("Skeleton Path: Echo...");
                            PatternRenderer renderer = new PatternRenderer(regionFile.getCommandLists().get(0), PatternRenderer.RenderType.ECHO);
                            skeletonPathFile = renderer.echoPattern(Integer.valueOf(textField.getText()));
                            skeletonPathCommands = renderer.getRenderedCommands();
                            break;
                        case "Medial Axis":
                            System.out.println("Skeleton Path: Medial Axis...");
                            break;
                    }

                    /* Skeleton Path Rendering */
                    PatternRenderer skeletonrenderer;
                    switch (skeletonRenderComboBox.getValue().toString()) {
                        case "Fixed-width Filling":
                            if (skeletonPathCommands.size() != 0) {
                                switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                                    case "none":
                                        skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION);
                                        skeletonrenderer.fixedWidthFilling(5);
                                        break;
                                    case "from file":
                                    case "from library":
                                        skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                                renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION);
                                        skeletonrenderer.fixedWidthFilling(5);
                                        break;
                                }
                            } else {
                                System.out.println("ERROR: skeleton path commands");
                            }

                            break;
                        case "Pebble":
                            if (skeletonSpanningTree == null) {
                                System.out.println("WARNING: spanning tree is NULL");
                            } else {
                                skeletonrenderer = new PatternRenderer(skeletonSpanningTree, PatternRenderer.RenderType.LANDFILL);
                                skeletonrenderer.landFill();
                            }
                            break;
                        case "Squiggles":
                            break;
                        case "Pattern Along Path":
                            String skeletonName = (skeletonPathFile == null) ? (regionFile.getfFileName()) : skeletonPathFile.getName();
                            mergedPattern = new SpinePatternMerger(skeletonName, skeletonPathCommands, renderedDecoElemFileProcessor, true);
                            /** Combine pattern */
                            mergedPattern.combinePattern();
                            break;
                        case "No Rendering":
                            break;

                    }
                });
    }

    private void setUpFont() {
        textFieldLabel.setFont(functionLabelFont);
        textFieldLabel.setTextFill(labelColor);
        regionLabel.setFont(columnLabelFont);
        regionLabel.setTextFill(columnLabelColor);
        loadRegionButton.setFont(buttonFont);
        regionSelectionLabel.setFont(functionLabelFont);
        regionSelectionLabel.setTextFill(labelColor);
        skeletonLabel.setFont(columnLabelFont);
        skeletonLabel.setTextFill(columnLabelColor);
        skeletonGnerationlabel.setFont(functionLabelFont);
        skeletonGnerationlabel.setTextFill(labelColor);
        skeletonRenderinglabel.setFont(functionLabelFont);
        skeletonRenderinglabel.setTextFill(labelColor);
        patternLabel.setFont(columnLabelFont);
        patternLabel.setTextFill(columnLabelColor);
        patternSelectionLabel.setFont(functionLabelFont);
        patternSelectionLabel.setTextFill(labelColor);
        patternFromFile.setFont(buttonFont);
        noPattern.setFont(buttonFont);
        patternFromLibrary.setFont(buttonFont);
        patternRenderFieldLabel.setFont(functionLabelFont);
        patternRenderFieldLabel.setTextFill(labelColor);
        patternRenderLabel.setFont(functionLabelFont);
        patternRenderLabel.setTextFill(labelColor);
        toolLabel.setFont(columnLabelFont);
        toolLabel.setTextFill(columnLabelColor);
        svgToPatLabel.setFont(functionLabelFont);
        svgToPatLabel.setTextFill(labelColor);
        generateButton.setFont(columnLabelFont);
        generateButton.setTextFill(Color.DARKBLUE);
    }

    private void setupListeners() {
        //Selection Listeners
        /* Pattern Source Listener */
        patternLibraryComboBox.getItems().addAll("feather");
        patternSourceGroup.selectedToggleProperty().addListener((ov, toggle, new_toggle) -> {
            if (new_toggle == null) {
                fileSourceBox.getChildren().removeAll();
            } else {
                if (patternSourceGroup.getSelectedToggle() == patternFromFile) {
                    System.out.println("New Pattern Source: Pattern From File ");
                    fileSourceBox.getChildren().setAll(loadDecoElementButton);
                }

                if (patternSourceGroup.getSelectedToggle() == patternFromLibrary) {
                    System.out.println("New Pattern Source: Pattern From Library ");
                    fileSourceBox.getChildren().setAll(patternLibraryComboBox);
                }

                if (patternSourceGroup.getSelectedToggle() == noPattern) {
                    System.out.println("New Pattern Source: No Pattern");
                    fileSourceBox.getChildren().removeAll(loadDecoElementButton, patternSourceBox, patternPropertyInput);
                }
            }
        });

        /* Skeleton Generation Listener */
        skeletonGenComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            String newSelected = skeletonGenComboBox.getValue().toString();
            System.out.println("Skeleton generation method changed: " + newSelected);
            if (newSelected.equals("3.3.4.3.4 Tessellation") || newSelected.equals("Grid Tessellation")) {
                System.out.println("case 1: tree structure");
                skeletonRenderComboBox.getItems().setAll("No Rendering", "Fixed-width Filling", "Squiggles", "Pebble");
            } else if (newSelected.equals("Hilbert Curve") || newSelected.equals("Echo") || newSelected.equals("Medial Axis")) {
                System.out.println("case 2: none tree structure");
                skeletonRenderComboBox.getItems().setAll("No Rendering", "Squiggles", "Pattern Along Path");
            }
            if (newSelected.equals("Echo")) {
                System.out.println("Echo:");
                textFieldLabel.setText("Repetition:");
                skeletonGeneration.getChildren().setAll(skeletonGnerationlabel, skeletonGenComboBox, textFieldLabel, textField);
            } else {
                skeletonGeneration.getChildren().setAll(skeletonGnerationlabel, skeletonGenComboBox);
            }
        });

        /* Skeleton Rendering Listner */
        skeletonRenderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            String newSelected = skeletonRenderComboBox.getValue().toString();
            System.out.println("Skeleton rendering method changed: " + newSelected);
            if (newSelected.equals("Pebble")) {
                System.out.println("Pebble");
                patternSourceBox.getChildren().setAll(noPattern);
            } else {
                patternSourceBox.getChildren().setAll(patternFromFile, patternFromLibrary, noPattern);
            }
        });

        /* Pattern rendering Listener */
        patternRenderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            String newSelected = patternRenderComboBox.getValue().toString();
            if (newSelected.equals("No Rendering")) {
                patternPropertyInput.getChildren().removeAll(patternRenderFieldLabel, patternRenderTextFiled);
            } else {
                patternPropertyInput.getChildren().setAll(patternRenderFieldLabel, patternRenderTextFiled);
            }
        });
    }


}
