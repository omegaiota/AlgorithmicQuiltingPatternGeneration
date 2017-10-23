package jackiequiltpatterndeterminaiton;

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
    /* Labels */
    final Label textFieldLabel = new Label(), patternRenderFieldLabel = new Label("Repetitions"),
            skeletonGenFieldLabel = new Label("Rows"), regionLabel = new Label("Region"),
            regionSelectionLabel = new Label("Select Region"), skeletonLabel = new Label("Skeleton Path"),
            skeletonGnerationlabel = new Label("Skeleton Path Generation"), patternLabel = new Label("Pattern"),
            patternSelectionLabel = new Label("Select Pattern"), skeletonRenderinglabel = new Label("Skeleton Path Rendering"),
            patternRenderLabel = new Label("Pattern Rendering"), toolLabel = new Label("Tools"),
            svgToPatLabel = new Label(".SVG to .PAT"), quiltingPatternGeneration = new Label("Quilting Pattern Generation"),
            skeletonRenderFieldLabel = new Label("Decoration Density");
    /* Toggle Group */
    final ToggleGroup patternSourceGroup = new ToggleGroup();
    /* Buttons */
    private final Button loadRegionButton = new Button("Load region ...");
    private final Button loadDecoElementButton = new Button("Load a pattern file ...");
    private final Button loadSvgFileButton = new Button("Load SVG file");
    private final Button generateButton = new Button("Generate");
    PatternRenderer patternRenderer;
    /* Layout: VBox, HBox*/
    //Column
    VBox regionColumn = new VBox(columnItemSpacing), skeletonColumn = new VBox(columnItemSpacing),
            patternColumn = new VBox(columnItemSpacing), toolColumn = new VBox(columnItemSpacing);
    //Label + item
    VBox regionSelection = new VBox(columnItemBundleSpacing), skeletonGeneration = new VBox(columnItemBundleSpacing),
            skeletonRendering = new VBox(columnItemBundleSpacing), patternRendering = new VBox(columnItemBundleSpacing),
            patternPropertyInput = new VBox(columnItemBundleSpacing), svgToPat = new VBox(columnItemBundleSpacing),
            skeletonGenPropertyInput = new VBox(columnItemBundleSpacing), skeletonRenderPropertyInput = new VBox(columnItemBundleSpacing),
            pathRender = new VBox(3), patternSelection = new VBox(columnItemBundleSpacing);
    HBox menu = new HBox(15);
    HBox patternSourceBox = new HBox(2);
    HBox fileSourceBox = new HBox(2);
    //ComboBox
    ComboBox skeletonGenComboBox = new ComboBox(), skeletonRenderComboBox = new ComboBox(),
            patternLibraryComboBox = new ComboBox(), patternRenderComboBox = new ComboBox();
    /* Fonts */
    Font columnLabelFont = new Font("Luminari", 22), functionLabelFont = new Font("Avenir Light", 14),
            buttonFont = new Font("Avenir", 10), titleFont = new Font("Luminari", 40);
    Color labelColor = Color.ORANGE, columnLabelColor = Color.SILVER;
    ToggleButton patternFromFile = new ToggleButton("from file"), noPattern = new ToggleButton("none"),
            patternFromLibrary = new ToggleButton("from library");
    /* Folder */
    File tileLibrary = new File("./src/resources/patterns/tiles/"),
            alongPathLibrary = new File("./src/resources/patterns/alongPath/"),
            endpointLibrary = new File("./src/resources/patterns/endpoints/");
    List<String> tileList = new ArrayList<>(), alongPathList = new ArrayList<>(), endpointList = new ArrayList<>(), squiggleList = new ArrayList<>();
    /* TextField */
    private TextField textField = new TextField(), patternRenderTextFiled = new TextField(), skeletonGenTextField = new TextField(),
            skeletonRenderTextField = new TextField();
    /* File processor, renderer */
    private SvgFileProcessor skeletonPathFile, decoElementFile = null, regionFile, svgFile;
    private SpinePatternMerger mergedPattern;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        final FileChooser fileChooser = new FileChooser();
        stage.setTitle("Quilt Pattern Generation");
        stage.setScene(new Scene(setLayoutWithGraph(stage), Color.rgb(35, 39, 50)));
        stage.show();
        setDefaultValue();
    }

    private void setDefaultValue() {
        skeletonGenComboBox.setValue("Grid Tessellation");
        skeletonRenderComboBox.setValue("No Rendering");
        patternRenderComboBox.setValue("No Rendering");
        noPattern.setSelected(true);
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
                "Hilbert Curve", "Echo", "Medial Axis", "Snake");

        skeletonGeneration.getChildren().addAll(skeletonGnerationlabel, skeletonGenComboBox);

        /* Skeleton Path Rendering */
        skeletonRenderComboBox.getItems().addAll("No Rendering", "Fixed-width Filling", "Squiggles", "Pebble", "Tiling");
        skeletonRendering.getChildren().addAll(skeletonRenderinglabel, skeletonRenderComboBox);

        skeletonColumn.getChildren().addAll(skeletonLabel, skeletonGeneration, skeletonGenPropertyInput, skeletonRendering, skeletonRenderPropertyInput);

        //Pattern Column

        /* Pattern selection*/

        /*  Toggle group*/
        patternSourceGroup.getToggles().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSourceBox.getChildren().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSelection.getChildren().addAll(patternSelectionLabel, patternSourceBox, fileSourceBox);

        /* initialize pattern library */
        for (File tileFile : tileLibrary.listFiles()) {
            String fileName = tileFile.getName();
            fileName = fileName.substring(0, fileName.length() - ".svg".length()); // get rid of .svg
            tileList.add(fileName);
        }

        for (File tileFile : alongPathLibrary.listFiles()) {
            String fileName = tileFile.getName();
            fileName = fileName.substring(0, fileName.length() - ".svg".length()); // get rid of .svg
            alongPathList.add(fileName);
        }

        for (File tileFile : endpointLibrary.listFiles()) {
            String fileName = tileFile.getName();
            fileName = fileName.substring(0, fileName.length() - ".svg".length()); // get rid of .svg
            endpointList.add(fileName);
        }


        System.out.println(endpointList.size() + " " + alongPathList.size() + " " + tileList.size());
        /* Pattern rendering */
        patternRenderComboBox.getItems().addAll("Repeat with Rotation", "Echo", "No Rendering");


        patternRendering.getChildren().setAll(patternRenderLabel, patternRenderComboBox);
        patternColumn.getChildren().addAll(patternLabel, patternSelection, patternRendering, patternPropertyInput);


        //Tool Column
        //svgToPat
        svgToPat.getChildren().addAll(svgToPatLabel, loadSvgFileButton);
        toolColumn.getChildren().addAll(toolLabel, svgToPat);
        menu.getChildren().addAll(regionColumn, skeletonColumn, patternColumn, toolColumn);

        layout.setCenter(menu);
        layout.setBottom(generateButton);
        layout.setTop(quiltingPatternGeneration);

        layout.setPadding(new Insets(60));
        BorderPane.setAlignment(generateButton, Pos.BOTTOM_CENTER);
        BorderPane.setAlignment(quiltingPatternGeneration, Pos.TOP_CENTER);
        BorderPane.setMargin(generateButton, new Insets(10, 8, 8, 8));
        layout.setStyle("-fx-background-color: rgb(35, 39, 50);");

        //Listeners
        setupListeners();

        //Buttons, File loader
        buttonActions(stage);
        return layout;
    }

    private void buttonActions(Stage stage) {
        loadSvgFileButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a skeleton path ....");
                svgFile = new SvgFileProcessor(file);
                try {
                    /** Process the svg file */
                    try {
                        svgFile.processSvg();
                        SvgFileProcessor.outputPat(svgFile.getCommandLists().get(0), svgFile.getfFileName());
                    } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        loadRegionButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a region ....");
                regionFile = new SvgFileProcessor(file);
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

        loadDecoElementButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a pattern....");
                decoElementFile = new SvgFileProcessor(file);
                try {
                    /** Process the svg file */
                    try {
                        decoElementFile.processSvg();
                        SvgFileProcessor.outputSvgCommands(decoElementFile.getCommandLists().get(0),
                                "decoElem-" + decoElementFile.getfFileName());
                    } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        generateButton.setOnAction((ActionEvent e) -> {
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
                    decoFileName = "lib" + patternLibraryComboBox.getValue().toString();
                    decoCommands = decoElementFile.getCommandLists().get(0);
                    break;
            }
            /* Pattern rendering */
            SvgFileProcessor renderedDecoElemFileProcessor = null;
            List<SvgPathCommand> renderedDecoCommands = new ArrayList<>();
            int repetitions = 0;
            switch (patternRenderComboBox.getValue().toString()) {
                case "No Rendering":
                    renderedDecoCommands = decoCommands;
                    renderedDecoElemFileProcessor = decoElementFile;
                    decoFileName += "_noRender";
                    break;
                case "Repeat with Rotation":
                    patternRenderer = new PatternRenderer(decoFileName, decoCommands, PatternRenderer.RenderType.ROTATION);
                    repetitions = Integer.valueOf(patternRenderTextFiled.getText());
                    patternRenderer.repeatWithRotation(repetitions);
                    renderedDecoCommands = patternRenderer.getRenderedCommands();
                    renderedDecoElemFileProcessor = new SvgFileProcessor(patternRenderer.outputRotated(Integer.valueOf(patternRenderTextFiled.getText())));
                    decoFileName += "_Rotation_" + repetitions;
                    break;
                case "Echo":
                    patternRenderer = new PatternRenderer(decoFileName, decoCommands, PatternRenderer.RenderType.ECHO);
                    repetitions = Integer.valueOf(patternRenderTextFiled.getText());
                    patternRenderer.echoPattern(repetitions);
                    renderedDecoCommands = patternRenderer.getRenderedCommands();
                    decoFileName += "_Echo_" + repetitions;
                    renderedDecoElemFileProcessor = new SvgFileProcessor(patternRenderer.outputEchoed(Integer.valueOf(patternRenderTextFiled.getText())));
                    break;
            }
            if (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText() != "none")
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
            Distribution distribute = null;
            List<SvgPathCommand> skeletonPathCommands = new ArrayList<>();
            TreeNode<Point> skeletonSpanningTree = null;
            File skeletonPathFile = null;
            SvgFileProcessor skeletonPathFileProcessor = null;
            String skeletonName = regionFile.getfFileName();
            int rows = -1;
            int distributionDist = 0;
            ArrayList<SvgPathCommand> temp = new ArrayList<>();

            switch (skeletonGenComboBox.getValue().toString()) {
                case "Grid Tessellation":
                    distributionDist = Integer.valueOf(skeletonGenTextField.getText());
                    System.out.println("Skeleton Path: Grid Tessellation");
                    distribute = new Distribution(Distribution.RenderType.TRIANGLE,
                            boundary, distributionDist, regionFile);

                    distribute.generate();
                    distribute.outputDistribution();
                    skeletonPathCommands = distribute.toTraversal(renderedDecoCommands);
                    skeletonName += "_tessellation_grid_" + 20;
                    skeletonPathFile = SvgFileProcessor.outputSvgCommands(skeletonPathCommands, skeletonName);
                    skeletonSpanningTree = distribute.getSpanningTree();
                    break;
                case "3.3.4.3.4 Tessellation":
                    distributionDist = Integer.valueOf(skeletonGenTextField.getText());
                    System.out.println("Skeleton Path: 3.3.4.3.4 Tessellation");
                    distribute = new Distribution(Distribution.RenderType.THREE_THREE_FOUR_THREE_FOUR,
                            boundary, distributionDist, regionFile);
                    distribute.generate();
                    distribute.outputDistribution();
                    skeletonPathCommands = distribute.toTraversal(renderedDecoCommands);
                    skeletonName += "_tessellation_33434_" + 20;
                    skeletonPathFile = SvgFileProcessor.outputSvgCommands(skeletonPathCommands, skeletonName);
                    temp.addAll(skeletonPathCommands);
                    skeletonSpanningTree = distribute.getSpanningTree();
                    break;

                case "Hilbert Curve":
                    System.out.println("Skeleton Path: Hilbert Curve...");

                    HilbertCurveGenerator hilbertcurve = new HilbertCurveGenerator(regionFile.getMinPoint(),
                            new Point(regionFile.getMaxPoint().x, 0),
                            new Point(0, regionFile.getMaxPoint().y), Integer.valueOf(skeletonGenTextField.getText()));
                    skeletonName += "_hilbertCurve_" + Integer.valueOf(skeletonGenTextField.getText());
                    hilbertcurve.patternGeneration();
                    skeletonPathFile = hilbertcurve.outputPath();
                    List<SvgPathCommand> fittedPath = boundary.fitCommandsToRegionTrimToBoundary(hilbertcurve.getCommandList());
                    skeletonPathCommands = fittedPath;
                    break;

                case "Echo":
                    System.out.println("Skeleton Path: Echo...");
                    PatternRenderer renderer = new PatternRenderer(regionFile.getCommandLists().get(0), PatternRenderer.RenderType.ECHO);
                    skeletonPathFile = renderer.echoPattern(Integer.valueOf(skeletonGenTextField.getText()));
                    skeletonName += "_echo_" + Integer.valueOf(skeletonGenTextField.getText());
                    skeletonPathCommands = renderer.getRenderedCommands();
                    break;

                case "Medial Axis":
                    System.out.println("Skeleton Path: Medial Axis...");
                    skeletonName += "_medialAxis";
                    skeletonPathCommands = boundary.generateMedialAxis();
                    skeletonPathFile = SvgFileProcessor.outputSvgCommands(skeletonPathCommands, skeletonName);
                    break;

                case "Snake":
                    System.out.println("Skeleton Path: Snake...");
                    SkeletonPathGenerator generator = new SkeletonPathGenerator(boundary);
                    rows = Integer.valueOf(skeletonGenTextField.getText());
                    generator.snakePathGenerator(rows);
                    skeletonPathCommands = generator.getSkeletonPath();
                    skeletonPathFile = generator.outputSnake(rows);
                    skeletonName += "_snake_" + rows;
                    break;
            }

            if (skeletonPathFile != null) {
                skeletonPathFileProcessor = new SvgFileProcessor(skeletonPathFile);
                try {
                    skeletonPathFileProcessor.processSvg();
                } catch (ParserConfigurationException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (SAXException e1) {
                    e1.printStackTrace();
                } catch (XPathExpressionException e1) {
                    e1.printStackTrace();
                }
            }

            /* Skeleton Path Rendering */
            PatternRenderer skeletonrenderer = null;
            List<SvgPathCommand> fittedPath;

            /* Tree Structure based rendering */
            if (skeletonGenComboBox.getValue().toString().equals("3.3.4.3.4 Tessellation") ||
                    skeletonGenComboBox.getValue().toString().equals("Grid Tessellation")) {
                switch (skeletonRenderComboBox.getValue().toString()) {
                    case "Fixed-width Filling":
                        double width = distributionDist / 5;
                        if (skeletonPathCommands.size() != 0) {
                            skeletonName += "_fixedWidth" + width;
                            switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                                case "none":
                                    skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION);
                                    skeletonrenderer.fixedWidthFilling(width, Double.valueOf(skeletonRenderTextField.getText()));
                                    break;
                                default:
                                    /* scale deco to full*/
                                    renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                            (distributionDist - width) / (1.4 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())),
                                            renderedDecoCommands.get(0).getDestinationPoint());
                                    skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                            renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION);
                                    skeletonrenderer.fixedWidthFilling(width, Double.valueOf(skeletonRenderTextField.getText()));
                                    break;
                            }
                            if (skeletonrenderer != null)
                                SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName);
                        } else {
                            System.out.println("ERROR: skeleton path commands");
                        }

                        break;
                    case "Pebble":
                        if (skeletonSpanningTree == null) {
                            System.out.println("WARNING: spanning tree is NULL");
                        } else {
                            skeletonName += "_Pebble";
                            skeletonrenderer = new PebbleRenderer(skeletonSpanningTree);
                            skeletonrenderer.pebbleFilling();

                            SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName);

                            temp.addAll(skeletonrenderer.getRenderedCommands());
                            SvgFileProcessor.outputSvgCommands(temp, skeletonName + "_temp_" + decoFileName);
                        }
                        break;
                    case "Squiggles":
                        System.out.println("Skeleton Path: Squiggle Tree");
                        skeletonName += "_squiggles";
                        renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                (distributionDist) / (1.4 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())),
                                renderedDecoCommands.get(0).getDestinationPoint());
                        if (distribute != null)
                            skeletonPathCommands = distribute.toSguiggleTraversal(renderedDecoCommands);
                        SvgFileProcessor.outputSvgCommands(skeletonPathCommands, skeletonName + "_" + decoFileName);
                        break;
                    case "No Rendering":
                        System.out.println("Skeleton Path: No Rendering ");
                        if (skeletonPathCommands.size() != 0) {
                            System.out.println("Skeleton Size" + skeletonPathCommands.size());
                            skeletonName += "_no_render";
                            switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                                case "none":
                                    skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION);
                                    skeletonrenderer.fixedWidthFilling(0, Double.valueOf(skeletonRenderTextField.getText()));
                                    break;
                                default:
                                    /* scale deco to full*/
                                    renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                            distributionDist / (1.4 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())),
                                            renderedDecoCommands.get(0).getDestinationPoint());
                                    skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                            renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION);
                                    skeletonrenderer.fixedWidthFilling(0, Double.valueOf(skeletonRenderTextField.getText()));
                                    break;
                            }
                            if (skeletonrenderer != null)
                                SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName);
                        } else {
                            System.out.println("ERROR: skeleton path commands");
                        }
                        break;
                }
            } else
                switch (skeletonRenderComboBox.getValue().toString()) {
                    case "Fixed-width Filling":
                        if (skeletonPathCommands.size() != 0) {
                            skeletonName += "_fixedWidth" + 5;
                            switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                                case "none":
                                    skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION);
                                    skeletonrenderer.fixedWidthFilling(5, Double.valueOf(skeletonRenderTextField.getText()));
                                    break;
                                case "from file":
                                    skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                            renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION);
                                    skeletonrenderer.fixedWidthFilling(5, Double.valueOf(skeletonRenderTextField.getText()));
                                case "from library":
                                    skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                            renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION);
                                    skeletonrenderer.fixedWidthFilling(5, Double.valueOf(skeletonRenderTextField.getText()));
                                    break;
                            }
                            if (skeletonrenderer != null)
                                SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName);
                        } else {
                            System.out.println("ERROR: skeleton path commands");
                        }

                        break;
                    case "Squiggles":
                        skeletonName += "_squiggles";
                        break;
                    case "Pattern Along Path":
                        skeletonName += "pattern_along_path";
                        mergedPattern = new SpinePatternMerger(skeletonName, skeletonPathCommands, renderedDecoElemFileProcessor, true);
                        /** Combine pattern */
                        mergedPattern.combinePattern();
                        fittedPath = boundary.fitCommandsToRegionTrimToBoundary(mergedPattern.getCombinedCommands());
                        SvgFileProcessor.outputSvgCommands(fittedPath, skeletonName + "_" + decoFileName);
                        //SvgFileProcessor.outputSvgCommands(mergedPattern.getCombinedCommands(), );
                        break;
                    case "No Rendering":
                        skeletonName += "_no_render";
                        SvgFileProcessor.outputSvgCommands(skeletonPathCommands, skeletonName + "_" + decoFileName);
                        break;
                    case "Tiling":
                        skeletonName += "_tiling";
                        double patternHeight = skeletonPathFileProcessor.getHeight() / rows;
                        mergedPattern = new SpinePatternMerger(skeletonName, skeletonPathCommands, renderedDecoElemFileProcessor, true);
                        /** Combine pattern */
                        mergedPattern.tilePattern(patternHeight);
                        fittedPath = boundary.fitCommandsToRegionIntelligent(mergedPattern.getCombinedCommands());
                        // fittedPath = boundary.fitCommandsToRegionDelete(mergedPattern.getCombinedCommands());
                        //fittedPath.addAll(regionFile.getCommandLists().get(0));
                        SvgFileProcessor.outputSvgCommands(fittedPath, skeletonName + "_" + decoFileName);
                        break;

                }
        });
    }

    private void setUpFont() {
        Label[] functionLabels = {textFieldLabel, regionSelectionLabel, skeletonGnerationlabel, skeletonRenderinglabel,
                patternLabel, svgToPatLabel, skeletonGenFieldLabel, skeletonRenderFieldLabel,
                patternSelectionLabel, patternRenderFieldLabel, patternRenderFieldLabel, patternRenderLabel};
        Label[] columnLabels = {regionLabel, skeletonLabel, patternLabel, toolLabel};
        Button[] functionButtons = {loadRegionButton};
        ToggleButton[] toggleButtons = {patternFromFile, noPattern, patternFromLibrary};
        for (Label label : functionLabels) {
            label.setFont(functionLabelFont);
            label.setTextFill(labelColor);
        }

        for (Label label : columnLabels) {
            label.setFont(columnLabelFont);
            label.setTextFill(columnLabelColor);
        }

        for (Button button : functionButtons)
            button.setFont(buttonFont);

        for (ToggleButton button : toggleButtons)
            button.setFont(buttonFont);

        generateButton.setFont(columnLabelFont);
        quiltingPatternGeneration.setFont(titleFont);
        quiltingPatternGeneration.setTextFill(Color.ALICEBLUE);
        generateButton.setTextFill(Color.DARKBLUE);

    }

    private void setupListeners() {
        //Selection Listeners
        /* Pattern Source Listener */
        patternLibraryComboBox.getItems().addAll("feather");
        patternSourceGroup.selectedToggleProperty().addListener((ov, toggle, new_toggle) -> {
            if (new_toggle == null) {
                fileSourceBox.getChildren().setAll();
            } else {
                if (patternSourceGroup.getSelectedToggle() == patternFromFile) {
                    System.out.println("New Pattern Source: Pattern From File ");
                    fileSourceBox.getChildren().setAll(loadDecoElementButton);
                }

                if (patternSourceGroup.getSelectedToggle() == patternFromLibrary) {
                    System.out.println("New Pattern Source: Pattern From Library ");
                    fileSourceBox.getChildren().setAll(patternLibraryComboBox);
                    if (skeletonGenComboBox.getValue().toString().equals("3.3.4.3.4 Tessellation") ||
                            skeletonGenComboBox.getValue().toString().equals("Grid Tessellation")) {
                        patternLibraryComboBox.getItems().setAll(endpointList);
                    } else if (skeletonRenderComboBox.getValue().toString().equals("Tiling"))
                        patternLibraryComboBox.getItems().setAll(tileList);
                    else
                        patternLibraryComboBox.getItems().setAll(alongPathList);
                    System.out.println(patternLibraryComboBox.getItems().toString());
                }

                if (patternSourceGroup.getSelectedToggle() == noPattern) {
                    System.out.println("New Pattern Source: No Pattern");
                    fileSourceBox.getChildren().setAll();
                }
            }
        });

        /* Skeleton Generation Listener */
        skeletonGenComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(patternLibraryComboBox.getItems().toString());
            String newSelected = skeletonGenComboBox.getValue().toString();
            System.out.println("Skeleton generation method changed: " + newSelected);
            skeletonRenderComboBox.setValue("No Rendering");
            if (newSelected.equals("3.3.4.3.4 Tessellation") || newSelected.equals("Grid Tessellation")) {
                System.out.println("case 1: tree structure");
                patternLibraryComboBox.getItems().setAll(endpointList);
                skeletonRenderComboBox.getItems().setAll("No Rendering", "Fixed-width Filling", "Squiggles", "Pebble");
            } else if (newSelected.equals("Echo") || newSelected.equals("Medial Axis") || newSelected.equals("Hilbert Curve")) {
                System.out.println("case 2: none tree structure");
                skeletonRenderComboBox.getItems().setAll("No Rendering", "Squiggles", "Pattern Along Path");
            } else if (newSelected.equals("Snake")) {
                System.out.println("Snake");
                skeletonRenderComboBox.getItems().setAll("No Rendering", "Pattern Along Path", "Squiggles", "Pebble", "Tiling");
            }

            if (newSelected.equals("Echo") || newSelected.equals("Hilbert Curve") || newSelected.equals("Snake")
                    || newSelected.equals("3.3.4.3.4 Tessellation") || newSelected.equals("Grid Tessellation")) {
                if (newSelected.equals("Echo"))
                    skeletonGenFieldLabel.setText("Repetitions:");
                if (newSelected.equals("Hilbert Curve"))
                    skeletonGenFieldLabel.setText("Level:");
                if (newSelected.equals("Snake"))
                    skeletonGenFieldLabel.setText("Rows:");
                if (newSelected.equals("3.3.4.3.4 Tessellation") || newSelected.equals("Grid Tessellation"))
                    skeletonGenFieldLabel.setText("Distribution Distance:");
                skeletonGenPropertyInput.getChildren().setAll(skeletonGenFieldLabel, skeletonGenTextField);
            }  else {
                skeletonGenPropertyInput.getChildren().removeAll(skeletonGenFieldLabel, skeletonGenTextField);
            }

            if (newSelected.equals("3.3.4.3.4 Tessellation") || newSelected.equals("Grid Tessellation")) {
                skeletonRenderPropertyInput.getChildren().setAll(skeletonRenderFieldLabel, skeletonRenderTextField);
            } else {
                skeletonRenderPropertyInput.getChildren().removeAll(skeletonRenderFieldLabel, skeletonRenderTextField);
            }
        });

        /* Skeleton Rendering Listener */
        skeletonRenderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            String newSelected = skeletonRenderComboBox.getValue().toString();
            System.out.println("Skeleton rendering method changed: " + newSelected);
            if (newSelected.equals("Pebble")) {
                System.out.println("Pebble");
                patternSourceGroup.selectToggle(noPattern);
            } else {
                patternSourceGroup.selectToggle(patternFromFile);
            }

            if (newSelected.equals("Tiling")) {
                    patternLibraryComboBox.getItems().setAll(tileList);
            }
        });

        /* Library ComboBox Listener/ pattern library combobox Listener */
        patternLibraryComboBox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            System.out.println(patternLibraryComboBox.getItems().toString());
            String newPatternFile = patternLibraryComboBox.getValue().toString();
            File library = alongPathLibrary;
            if (skeletonRenderComboBox.getValue().toString().equals("Tiling"))
                library = tileLibrary;
            if (skeletonGenComboBox.getValue().toString().equals("3.3.4.3.4 Tessellation") ||
                    skeletonGenComboBox.getValue().toString().equals("Grid Tessellation"))
                library = endpointLibrary;
            File file = new File(library.getPath() + "/" + newPatternFile + ".svg");
            System.out.println("Loading a pattern....");
            decoElementFile = new SvgFileProcessor(file);
            try {
                decoElementFile.processSvg();
                SvgFileProcessor.outputSvgCommands(decoElementFile.getCommandLists().get(0),
                        "decoElem-" + decoElementFile.getfFileName());
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }

        }));

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
