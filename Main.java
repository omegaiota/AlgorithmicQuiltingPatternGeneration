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
    private final Button loadRegionButton = new Button("Load region ...");
    private final Button loadDecoElementButton = new Button("Load a pattern file ...");
    private final Button loadSvgFileButton = new Button("Load SVG file");
    private final Button generateButton = new Button("Generate");

    private SpinePatternMerger mergedPattern;
    private TextField textField = new TextField();
    private svgFileProcessor skeletonPathFile, decoElementFile, regionFile, svgFile;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception{
        final FileChooser fileChooser = new FileChooser();
        stage.setTitle("Quilt Pattern Generation");
        stage.setScene(new Scene(setLayoutWithGraph(stage), Color.rgb(35, 39, 50)));
        stage.show();
    }


    public BorderPane setLayoutWithGraph(Stage stage) {
        BorderPane layout = new BorderPane();
        int columnItemSpacing = 15;
        VBox regionColumn = new VBox(columnItemSpacing);
        VBox skeletonColumn = new VBox(columnItemSpacing);
        VBox patternColumn = new VBox(columnItemSpacing);
        VBox toolColumn = new VBox(columnItemSpacing);

        int columnItemBundleSpacing = 3;
        VBox regionSelection = new VBox(columnItemBundleSpacing);
        VBox skeletonGeneration = new VBox(columnItemBundleSpacing);
        VBox skeletonRendering = new VBox(columnItemBundleSpacing);
        VBox patternSelection = new VBox(columnItemBundleSpacing);
        VBox patternRendering = new VBox(columnItemBundleSpacing);
        VBox patternPropertyInput = new VBox(columnItemBundleSpacing);
        VBox svgToPat = new VBox(columnItemBundleSpacing);


        VBox pathRender = new VBox( 3);
        Font columnLabelFont = new Font("Luminari", 22);
        Font functionLabelFont = new Font("Avenir Light", 14);
        Font buttonFont = new Font("Avenir", 10);
        Label textFieldLabel = new Label();
        Color labelColor = Color.ORANGE;
        Color columnLabelColor = Color.SILVER;
        HBox menu = new HBox(15);

        textFieldLabel.setFont(functionLabelFont);
        textFieldLabel.setTextFill(labelColor);

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
        skeletonGenComboBox.setValue("Grid Tessellation");
        skeletonGeneration.getChildren().addAll(skeletonGnerationlabel, skeletonGenComboBox);

        /* Skeleton Path Rendering */
        final Label skeletonRenderinglabel = new Label("Skeleton Path Rendering");
        skeletonRenderinglabel.setFont(functionLabelFont);
        skeletonRenderinglabel.setTextFill(labelColor);
        ComboBox skeletonRenderComboBox = new ComboBox();
        skeletonRenderComboBox.getItems().addAll("No Rendering", "Fixed-width Filling", "Squiggles", "Pebble");
        skeletonRendering.getChildren().addAll(skeletonRenderinglabel, skeletonRenderComboBox);
        skeletonRenderComboBox.setValue("No Rendering");

        skeletonColumn.getChildren().addAll(skeletonLabel ,skeletonGeneration, skeletonRendering);

        //Pattern Column
        final Label patternLabel = new Label("Pattern");
        patternLabel.setFont(columnLabelFont);
        patternLabel.setTextFill(columnLabelColor);

        /* Pattern selection*/
        final Label patternSelectionLabel = new Label("Select Pattern");
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
        noPattern.setSelected(true);
        patternSourceBox.getChildren().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSelection.getChildren().addAll(patternSelectionLabel, patternSourceBox, fileSourceBox);

        ComboBox patternLibraryComboBox = new ComboBox();

        /* Pattern rendering */
        final Label patternRenderLabel = new Label("Pattern Rendering");
        TextField patternRenderTextFiled = new TextField();
        Label patternRenderFieldLabel = new Label("Repetitions");
        patternRenderFieldLabel.setFont(functionLabelFont);
        patternRenderFieldLabel.setTextFill(labelColor);

        patternRenderLabel.setFont(functionLabelFont);
        patternRenderLabel.setTextFill(labelColor);
        ComboBox patternRenderComboBox = new ComboBox();
        patternRenderComboBox.getItems().addAll("Repeat with Rotation", "Echo", "No Rendering");
        patternRenderComboBox.setValue("No Rendering");
        patternRendering.getChildren().setAll(patternRenderLabel, patternRenderComboBox);
        patternColumn.getChildren().addAll(patternLabel ,patternSelection, patternRendering, patternPropertyInput);



        //Tool Column
        final Label toolLabel = new Label("Tools");
        toolLabel.setFont(columnLabelFont);
        toolLabel.setTextFill(columnLabelColor);

        //svgToPat
        final Label svgToPatLabel = new Label(".SVG to .PAT");
        svgToPatLabel.setFont(functionLabelFont);
        svgToPatLabel.setTextFill(labelColor);
        svgToPat.getChildren().addAll(svgToPatLabel, loadSvgFileButton);

        toolColumn.getChildren().addAll(toolLabel, svgToPat);

        menu.getChildren().addAll(regionColumn, skeletonColumn, patternColumn, toolColumn);

        layout.setCenter(menu);
        layout.setBottom(generateButton);
        generateButton.setFont(columnLabelFont);
        generateButton.setTextFill(Color.DARKBLUE);
        layout.setPadding(new Insets(60));
        BorderPane.setAlignment(generateButton, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(generateButton, new Insets(20, 8, 8, 8));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");


        setupListeners();
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

                if (patternSourceGroup.getSelectedToggle() == patternFromLibrary){
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
            } else if (newSelected.equals( "Hilbert Curve") || newSelected.equals( "Echo") || newSelected.equals( "Medial Axis") ) {
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

        //Buttons, File loader
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
                                regionFile.outputSvgCommands(regionFile.getCommandLists().get(0),"region-" + regionFile.getfFileName());
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
                                svgFileProcessor.outputSvgCommands(decoElementFile.getCommandLists().get(0), "decoElem-" + decoElementFile.getfFileName());
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
                    PatternRenderer patternRenderer;
                    /* Pattern Selection */
                    String decoFileName = "noDeco";
                    List<SvgPathCommand> decoCommands = new ArrayList<>();
                    switch ( ((ToggleButton)patternSourceGroup.getSelectedToggle()).getText()) {
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
                            skeletonPathFile =  svgFileProcessor.outputSvgCommands(skeletonPathCommands, "traversal-" + regionFile.getfFileName() + "-" + "GRID" + "-dis-" + 20);
                            skeletonSpanningTree = distribute.getSpanningTree();
                            break;
                        case "3.3.4.3.4 Tessellation":
                            System.out.println("Skeleton Path: 3.3.4.3.4 Tessellation");
                            distribute = new Distribution(Distribution.RenderType.THREE_THREE_FOUR_THREE_FOUR,
                                    boundary, 20, regionFile);
                            distribute.generate();
                            distribute.outputDistribution();
                            skeletonPathCommands = distribute.toTraversal();
                            skeletonPathFile = svgFileProcessor.outputSvgCommands(skeletonPathCommands, "traversal-" + regionFile.getfFileName() + "-" + "TTFTF" + "-dis-" + 20);
                            skeletonSpanningTree = distribute.getSpanningTree();
                            break;

                        case "Hilbert Curve":
                            System.out.println("Skeleton Path: Hilbert Curve...");
                            HilbertCurveGenerator hilbertcurve = new HilbertCurveGenerator(new Point(0, 0), new Point(800, 0), new Point(0, 800), 4);
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
                                switch ( ((ToggleButton)patternSourceGroup.getSelectedToggle()).getText()) {
                                    case "none":
                                        skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION);
                                        skeletonrenderer.fixedWidthFilling(5);
                                        break;
                                    case "from file":
                                    case "from library":
                                        skeletonrenderer = new PatternRenderer(regionFile.getfFileName(),skeletonPathCommands, decoFileName,
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
        return layout;
    }

    private void setupListeners() {
    }


}
