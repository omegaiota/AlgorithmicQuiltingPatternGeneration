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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static jackiequiltpatterndeterminaiton.Main.SkeletonPathType.*;
import static jackiequiltpatterndeterminaiton.Main.SkeletonRenderType.*;

public class Main extends Application {
    /* Constant */
    private static final int columnItemSpacing = 15, columnItemBundleSpacing = 3;
    /* Labels */
    private final Label textFieldLabel = new Label(), patternRenderFieldLabel = new Label("Repetitions"),
            skeletonGenFieldLabel = new Label("Rows"), regionLabel = new Label("Region"),
            regionSelectionLabel = new Label("Select Region"), skeletonLabel = new Label("Skeleton Path"),
            skeletonGnerationlabel = new Label("Skeleton Path Generation"), patternLabel = new Label("Pattern"),
            patternSelectionLabel = new Label("Select Pattern"), skeletonRenderinglabel = new Label("Skeleton Path Rendering"),
            patternRenderLabel = new Label("Pattern Rendering"), toolLabel = new Label("Tools"),
            svgToPatLabel = new Label(".SVG to .PAT"), quiltingPatternGeneration = new Label("Quilting Pattern Generation"),
            skeletonRenderFieldLabel1 = new Label("Decoration Density"),
            skeletonRenderFieldLabel2 = new Label("Decoration Density"),
            skeletonRenderFieldLabel4 = new Label("Decoration Density"),
            skeletonRenderFieldLabel3 = new Label("Decoration Density");
    /* Toggle Group */
    private final ToggleGroup patternSourceGroup = new ToggleGroup();
    /* Buttons */
    private final Button loadRegionButton = new Button("Load region ..."), loadCollisionGeoButton = new Button("Load collision file ...");
    private final Button loadDecoElementButton = new Button("Load a pattern file ...");
    private final Button loadSvgFileButton = new Button("Load SVG file");
    private final Button generateButton = new Button("Generate");
    List<String> tileList = new ArrayList<>(), alongPathList = new ArrayList<>(), endpointList = new ArrayList<>();
    private PatternRenderer patternRenderer;
    /* Layout: VBox, HBox*/
    //Column
    private VBox regionColumn = new VBox(columnItemSpacing), skeletonColumn = new VBox(columnItemSpacing),
            patternColumn = new VBox(columnItemSpacing), toolColumn = new VBox(columnItemSpacing);
    //Label + item
    private VBox regionSelection = new VBox(columnItemBundleSpacing), skeletonGeneration = new VBox(columnItemBundleSpacing),
            skeletonRendering = new VBox(columnItemBundleSpacing), patternRendering = new VBox(columnItemBundleSpacing),
            patternPropertyInput = new VBox(columnItemBundleSpacing), svgToPat = new VBox(columnItemBundleSpacing),
            skeletonGenPropertyInput = new VBox(columnItemBundleSpacing), skeletonRenderPropertyInput = new VBox(columnItemBundleSpacing),
            patternSelection = new VBox(columnItemBundleSpacing);
    private HBox menu = new HBox(15), patternSourceBox = new HBox(2), fileSourceBox = new HBox(2);
    //ComboBox
    private ComboBox skeletonGenComboBox = new ComboBox(), skeletonRenderComboBox = new ComboBox(),
            patternLibraryComboBox = new ComboBox(), patternRenderComboBox = new ComboBox();
    /* Fonts */
    private Font columnLabelFont = new Font("Luminari", 22), functionLabelFont = new Font("Avenir Light", 14),
            buttonFont = new Font("Avenir", 10), titleFont = new Font("Luminari", 40);
    private Color labelColor = Color.ORANGE, columnLabelColor = Color.SILVER;
    private ToggleButton patternFromFile = new ToggleButton("from file"), noPattern = new ToggleButton("none"),
            patternFromLibrary = new ToggleButton("from library");
    /* Folder */
    private File tileLibrary = new File("./src/resources/patterns/tiles/"),
            alongPathLibrary = new File("./src/resources/patterns/alongPath/"),
            endpointLibrary = new File("./src/resources/patterns/endpoints/");
    /* TextField */
    private TextField patternRenderTextFiled = new TextField(),
            skeletonGenTextField = new TextField(),
            skeletonRenderTextField1 = new TextField(),
            skeletonRenderTextField2 = new TextField(),
            skeletonRenderTextField3 = new TextField();
    /* File processor, renderer */
    private SvgFileProcessor decoElementFile = null, regionFile, svgFile;
    private GenerationInfo info = new GenerationInfo();
    private SpinePatternMerger mergedPattern;
    private PatternRenderer skeletonrenderer = null;
    private SkeletonPathType skeletonPathType;
    private SkeletonRenderType skeletonRenderType;
    private String skeletonName, decoFileName;
    private Region boundary;
    private int rows = -1;
    private List<SvgPathCommand> collisionCommands = new ArrayList<>();


    private SvgFileProcessor skeletonPathFileProcessor = null, renderedDecoElemFileProcessor = null, collisionFile = null;

    private List<SvgPathCommand> renderedDecoCommands = new ArrayList<>(), skeletonPathCommands = new ArrayList<>(), skeletonCopy = new ArrayList<>();
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Quilt Pattern Generation");
        stage.setScene(new Scene(setLayoutWithGraph(stage), Color.rgb(35, 39, 50)));
        stage.show();
        setDefaultValue();
    }

    private void setDefaultValue() {
        skeletonGenComboBox.setValue(POISSON_DISK);
        skeletonRenderTextField1.setText("0");
        skeletonGenTextField.setText("20");
        skeletonRenderComboBox.setValue(CATMULL_ROM);
        patternRenderComboBox.setValue("No Rendering");
        patternFromLibrary.setSelected(true);
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
        skeletonGenComboBox.getItems().addAll(SkeletonPathType.values());
        skeletonGeneration.getChildren().addAll(skeletonGnerationlabel, skeletonGenComboBox);

        /* Skeleton Path Rendering */
        skeletonRenderComboBox.getItems().addAll(SkeletonRenderType.values());
        skeletonRendering.getChildren().addAll(skeletonRenderinglabel, skeletonRenderComboBox);

        skeletonColumn.getChildren().addAll(skeletonLabel, skeletonGeneration, skeletonGenPropertyInput, skeletonRendering, skeletonRenderPropertyInput);

        //Pattern Column

        /* Pattern selection*/

        /*  Toggle group*/
        patternSourceGroup.getToggles().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSourceBox.getChildren().addAll(patternFromFile, patternFromLibrary, noPattern);
        patternSelection.getChildren().addAll(patternSelectionLabel, patternSourceBox, fileSourceBox);

        /* initialize pattern library */
        addLibraryFilesToList(tileLibrary, tileList);
        addLibraryFilesToList(alongPathLibrary, alongPathList);
        addLibraryFilesToList(endpointLibrary, endpointList);


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

    private void addLibraryFilesToList(File folderFile, List<String> fileList) {
        for (File tileFile : folderFile.listFiles()) {
            String fileName = tileFile.getName();
            fileName = fileName.substring(0, fileName.length() - ".svg".length()); // get rid of .svg
            fileList.add(fileName);
        }
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
                        SvgFileProcessor.outputPat(svgFile.getCommandList(), svgFile.getfFileName());
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
                        regionFile.outputSvgCommands(regionFile.getCommandList(),
                                "region-" + regionFile.getfFileName(), info);
                        info.setRegionFile(regionFile);
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
                        SvgFileProcessor.outputSvgCommands(decoElementFile.getCommandList(),
                                "decoElem-" + decoElementFile.getfFileName(), info);
                    } catch (ParserConfigurationException | SAXException | XPathExpressionException e1) {
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

        loadCollisionGeoButton.setOnAction(e -> {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                System.out.println("Loading a pattern....");
                collisionFile = new SvgFileProcessor(file);
                try {
                    /** Process the svg file */
                    try {
                        collisionFile.processSvg();
                        SvgFileProcessor.outputSvgCommands(collisionFile.getCommandList(),
                                "collision-" + collisionFile.getfFileName(), info);
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
            boundary = regionFile.getBoundary();
            info.setRegionFile(regionFile);

            /* Pattern Selection */
            decoFileName = "noDeco";
            List<SvgPathCommand> decoCommands = new ArrayList<>();
            switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                case "none":
                    break;
                case "from file":
                    decoCommands = decoElementFile.getCommandList();
                    decoFileName = decoElementFile.getfFileName();
                    break;
                case "from library":
                    decoFileName = "lib" + patternLibraryComboBox.getValue().toString();
                    decoCommands = decoElementFile.getCommandList();
                    break;
            }


            /* Pattern rendering */


            int repetitions;
            String patternRenderMethod = patternRenderComboBox.getValue().toString();
            switch (patternRenderMethod) {
                case "No Rendering":
                    renderedDecoCommands = decoCommands;
                    renderedDecoElemFileProcessor = decoElementFile;
                    decoFileName += "_noRender";
                    break;
                case "Repeat with Rotation":
                case "Echo":
                    patternRenderer = new PatternRenderer(decoFileName, decoCommands, PatternRenderer.RenderType.ROTATION, info);
                    repetitions = Integer.valueOf(patternRenderTextFiled.getText());
                    if (patternRenderMethod.equals("Echo")) {
                        patternRenderer.echoPattern(repetitions);
                    } else {
                        patternRenderer.repeatWithRotation(repetitions);
                    }
                    renderedDecoCommands = patternRenderer.getRenderedCommands();
                    renderedDecoElemFileProcessor = new SvgFileProcessor(patternRenderer.outputRotated(Integer.valueOf(patternRenderTextFiled.getText())));
                    decoFileName += String.format("_%s_", patternRenderMethod) + repetitions;
                    break;
            }
            if (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText() != "none")
                try {
                    renderedDecoElemFileProcessor.processSvg();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            info.setDecoElementFile(renderedDecoElemFileProcessor);

            /* Skeleton Path Generation */
            PointDistribution distribute = null;
            File skeletonPathFile = null;
            skeletonName = regionFile.getfFileName();

            skeletonName += skeletonGenComboBox.getValue().toString();
            skeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
            if (skeletonPathType.isTessellation()) {
                info.setPointDistributionDist(Double.valueOf(skeletonGenTextField.getText()));
                System.out.println("Distribution dist set to" + info.getPointDistributionDist());
                System.out.println("Skeleton Path: Grid Tessellation");

                /* Using tessellation method*/
                distribute = new PointDistribution(skeletonPathType.getPointDistributionType(), info);
                distribute.generate();
                distribute.outputDistribution();
                skeletonPathCommands = distribute.toTraversal();
                skeletonCopy.addAll(skeletonPathCommands);

//                /**
//                 * Using random tree
//                 */
                distribute = new PointDistribution(skeletonPathType.getPointDistributionType(), info);
                distribute.generate();
                List<Point> points = distribute.getPoints();
                SvgFileProcessor.outputPoints(points, info);
                List<Point> perterbed = points.stream().map(p -> new Point(p.x + Math.random(), p.y + Math.random())).collect(Collectors.toList());
                Map<Point, Point> oldNewMap = new HashMap<>();
                for (int i = 0; i < points.size(); i++) {
                    oldNewMap.put(perterbed.get(i), points.get(i));
                }

                info.setSpanningTree(PointDistribution.toMST(perterbed));
                PointDistribution.remapPoint(info.getSpanningTree(), oldNewMap);
                TreeTraversal renderer = new TreeTraversal(info.getSpanningTree());
                renderer.traverseTree();
                skeletonPathCommands = renderer.getRenderedCommands();
                skeletonCopy.addAll(skeletonPathCommands);
                info.setNodeType(renderer.getNodeLabel());
                SvgFileProcessor.outputSvgCommands(skeletonPathCommands, "Tessellation Skeleton", info);
            } else switch (skeletonPathType) {
                case POISSON_DISK:
                    info.setPointDistributionDist(Double.valueOf(skeletonGenTextField.getText()));
                    List<Point> randomPoints = PointDistribution.poissonDiskSamples(info);
                    info.setSpanningTree(PointDistribution.toMST(randomPoints));
                    SvgFileProcessor.outputPoints(randomPoints, info);
                    TreeTraversal renderer = new TreeTraversal(info.getSpanningTree());
                    renderer.traverseTree();
                    skeletonPathCommands = renderer.getRenderedCommands();
                    info.setNodeType(renderer.getNodeLabel());
                    SvgFileProcessor.outputSvgCommands(skeletonPathCommands, "Poisson Disk Skeleton", info);
                    skeletonCopy.addAll(skeletonPathCommands);

                    break;

                case HILBERT_CURVE:
                    System.out.println("Skeleton Path: Hilbert Curve...");

                    HilbertCurveGenerator hilbertcurve = new HilbertCurveGenerator(regionFile.getMinPoint(),
                            new Point(regionFile.getMaxPoint().x, 0),
                            new Point(0, regionFile.getMaxPoint().y), Integer.valueOf(skeletonGenTextField.getText()));
                    skeletonPathCommands = hilbertcurve.patternGeneration();
                    List<SvgPathCommand> fittedPath = boundary.fitCommandsToRegionTrimToBoundary(skeletonPathCommands, info);
                    skeletonPathCommands = fittedPath;
                    break;

                case ECHO:
                    System.out.println("Skeleton Path: Echo...");
                    PatternRenderer traversaler = new PatternRenderer(regionFile.getCommandList(), PatternRenderer.RenderType.ECHO, info);
                    skeletonPathCommands = traversaler.echoPattern(Integer.valueOf(skeletonGenTextField.getText()));
                    break;

                case MEDIAL_AXIS:
                    System.out.println("Skeleton Path: Medial Axis...");
                    skeletonPathCommands = boundary.generateMedialAxis();
                    break;

                case SNAKE:
                    System.out.println("Skeleton Path: Snake...");
                    SkeletonPathGenerator generator = new SkeletonPathGenerator(boundary);
                    rows = Integer.valueOf(skeletonGenTextField.getText());
                    generator.snakePathGenerator(rows);
                    skeletonPathCommands = generator.getSkeletonPath();
                    break;
            }
            skeletonPathFile = SvgFileProcessor.outputSvgCommands(skeletonPathCommands, skeletonName, info);

            if (skeletonPathFile != null) {
                skeletonPathFileProcessor = new SvgFileProcessor(skeletonPathFile);
                try {
                    skeletonPathFileProcessor.processSvg();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            /* Skeleton Path Rendering */


            /* Tree Structure based rendering */
            skeletonName += skeletonRenderComboBox.getValue().toString();
            skeletonRenderType = (SkeletonRenderType) skeletonRenderComboBox.getValue();
//            outputAllTreeRendering();
            determineSkeleton();
            SvgFileProcessor.outputSvgCommands(skeletonPathCommands, "generated skeleton paths", info);

        });
    }

    private void outputAllTreeRendering() {
        PatternRenderer interpolationRenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                renderedDecoCommands, PatternRenderer.RenderType.CATMULL_ROM, info);
        interpolationRenderer.toCatmullRom();
        SvgFileProcessor.outputSvgCommands(interpolationRenderer.getRenderedCommands(), skeletonName + "_catmull_rom_skeleton_" + decoFileName, info);

        PatternRenderer stippleRenderer, fixedWidthRenderer;
        stippleRenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
        fixedWidthRenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
        stippleRenderer.fixedWidthFilling(info.getPointDistributionDist() / 3, Double.valueOf(skeletonRenderTextField1.getText()));
        fixedWidthRenderer.fixedWidthFilling(info.getPointDistributionDist() / 5.0, Double.valueOf(skeletonRenderTextField1.getText()));


        stippleRenderer.setRenderedCommands(PatternRenderer.interpolate(stippleRenderer.getRenderedCommands()));
        SvgFileProcessor.outputSvgCommands(stippleRenderer.getRenderedCommands(), skeletonName + "_stipple_" + decoFileName, info);
        SvgFileProcessor.outputSvgCommands(fixedWidthRenderer.getRenderedCommands(), skeletonName + "_fixedwidth_" + decoFileName, info);
    }

    private void determineSkeleton() {
        if (skeletonPathType.isTreeStructure()) {
            switch (skeletonRenderType) {
                case CATMULL_ROM:
                    // Normalize deco element to 100 * 100
                    double maxDimension = decoElementFile.getWidth();
                    if (collisionFile != null) {
                        collisionCommands.add(renderedDecoCommands.get(0));
                        collisionCommands.addAll(collisionFile.getCommandList());
                    }

                    renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands, 10.0 / maxDimension, renderedDecoCommands.get(0).getDestinationPoint());
                    collisionCommands = SvgPathCommand.commandsScaling(collisionCommands, 10.0 / maxDimension, collisionCommands.get(0).getDestinationPoint());

                    skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                            renderedDecoCommands, PatternRenderer.RenderType.CATMULL_ROM, info);
                    skeletonrenderer.toCatmullRom();
                        /* A curved tree is not really "rendered"/ we still want to be able to put patterns on it. needs to add additional
                         * parameterization here for selecting how we want the deco elements put on the curved tree */
                    SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_catmull_rom_skeleton_" + decoFileName, info);
                        /*TODO: rewrite code! below code is exactly the same as FIXED WIDTH FILL*/
                    if (!((ToggleButton) patternSourceGroup.getSelectedToggle()).getText().equals("none")) {
                                    /* scale deco to full*/
//                            info.setDecoElmentScalingFactor(info.getRegionFile().getPoints().getArea() / (2.5 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())));

                        info.setCollisionFile(collisionFile);


                        final List<SvgPathCommand> renderedCommandsCopy = new ArrayList<>(renderedDecoCommands),
                                collisionCopy = new ArrayList<>(collisionCommands),
                                skeletonCopy = new ArrayList<>(skeletonrenderer.getRenderedCommands());

                        boolean batchOutput = false; // for producing set of 3 x 3 patterns with different parameters
                        if (batchOutput) {
                            for (int i = 1; i < 2; i++) {
//                            info.setPointDistributionDist();
                                for (double decorationSize = 0.8; decorationSize < 1.65; decorationSize += 0.4) {
                                    List<List<SvgPathCommand>> setsOfNine = new ArrayList<>();
                                    List<String> setsOfNineName = new ArrayList<>();
                                    info.setDecorationSize(Double.valueOf(decorationSize));
                                    for (int gapMultiplier = 0; gapMultiplier < 3; gapMultiplier++) {
                                        double gap = gapMultiplier * 0.3 + 0.7;
                                        info.setDecorationGap(gap * 10.0);
                                        for (int angleMultiplier = 1; angleMultiplier < 4; angleMultiplier++) {
                                            double initialAngle = angleMultiplier * 15;
                                            collisionCommands = new ArrayList<>(collisionCopy);
                                            renderedDecoCommands = new ArrayList<>(renderedCommandsCopy);
                                            skeletonrenderer.setRenderedCommands(new ArrayList<>(skeletonCopy));
                                            info.setInitialAngle(initialAngle);
                                            collisionCommands = SvgPathCommand.commandsScaling(collisionCommands,
                                                    info.getDecorationSize(),
                                                    renderedDecoCommands.get(0).getDestinationPoint());
                                            renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                                    info.getDecorationSize(),
                                                    renderedDecoCommands.get(0).getDestinationPoint());

                                        /* Adding alternating leaves*/
                                            info.setCollisionCommands(collisionCommands);
                                            String paramterStr = "_size_" + decorationSize + "_gap_" + gap + "_angle_" + initialAngle + "_poisson_" + info.getPointDistributionDist();
                                            System.out.println(paramterStr);
                                            skeletonrenderer.addAlternatingDecoElmentToSplineTree(renderedDecoCommands, info);

                                            Point firstPoint = skeletonrenderer.getRenderedCommands().get(0).getDestinationPoint();
                                            setsOfNine.add(SvgPathCommand.commandsShift(skeletonrenderer.getRenderedCommands(), new Point(firstPoint.x + 750.0 * (angleMultiplier - 1), firstPoint.y + 750 * gapMultiplier)));
                                            setsOfNineName.add(paramterStr);

//                                            SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), decoFileName + paramterStr, info);

                                        }

                                    }

                                    List<SvgPathCommand> flattened = new ArrayList<>();
                                    for (List<SvgPathCommand> li : setsOfNine)
                                        flattened.addAll(li);
                                    SvgFileProcessor.outputSvgCommands(flattened, info.getPointDistributionDist() + "_" + decoFileName + "_decoSize_" + decorationSize, 2200, 2200);
                                }
                            }
                        } else {
                            info.setDecorationSize(Double.valueOf(skeletonRenderTextField1.getText()));
                            info.setDecorationGap(Double.valueOf(skeletonRenderTextField2.getText()) * 10);
                            info.setInitialAngle(Double.valueOf(skeletonRenderTextField3.getText()));


                            collisionCommands = SvgPathCommand.commandsScaling(collisionCommands,
                                    info.getDecorationSize(),
                                    renderedDecoCommands.get(0).getDestinationPoint());
                            renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                    info.getDecorationSize(),
                                    renderedDecoCommands.get(0).getDestinationPoint());
                                /* Adding a "pair" of leaves */
//                            skeletonrenderer.addDecoElmentToSplineTree(renderedDecoCommands, info);
                                /* Adding alternating leaves*/
                            info.setCollisionCommands(collisionCommands);

                            skeletonrenderer.addAlternatingDecoElmentToSplineTree(renderedDecoCommands, info);

                        }


                    }

                    SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_catmull_rom_" + decoFileName, info);
                    SvgFileProcessor.outputSvgCommandsWithBoundary(skeletonrenderer.getRenderedCommands(), skeletonName + "_catmull_rom_visualize_region" + decoFileName, info);
                    break;
                case FIXED_WIDTH_FILL:
                case STIPPLING:
                    boolean isStippling = skeletonRenderType == STIPPLING;
                    double width = isStippling ? info.getPointDistributionDist() / 3 : info.getPointDistributionDist() / 5.0;

                    switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                        case "none":
                            skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
                            skeletonrenderer.fixedWidthFilling(width, Double.valueOf(skeletonRenderTextField1.getText()));
                            break;
                        default:
                                    /* scale deco to full*/
                            renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                    (info.getPointDistributionDist() - width) / (1.5 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())),
                                    renderedDecoCommands.get(0).getDestinationPoint());
                            skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                    renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION, info);
                            skeletonrenderer.fixedWidthFilling(width, Double.valueOf(skeletonRenderTextField1.getText()));
                            break;
                    }
                    if (isStippling) {
                        skeletonrenderer.setRenderedCommands(PatternRenderer.interpolate(skeletonrenderer.getRenderedCommands()));
                    }
                    SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName, info);
                    SvgFileProcessor.outputSvgCommandsWithBoundary(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName, info);
                    break;
                case RECTANGLE:
                    skeletonrenderer = new RectanglePacking(renderedDecoCommands, info,
                            skeletonRenderType != RECTANGLE && skeletonGenComboBox.getValue().equals(VINE));
                    ((RectanglePacking) skeletonrenderer).rectanglePacking();
                    SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName, info);
                    List<SvgPathCommand> skeletonAndRender = new ArrayList<>(skeletonCopy),
                            regionAndRender = new ArrayList<>(regionFile.getCommandList());
                    skeletonAndRender.addAll(skeletonrenderer.getRenderedCommands());
                    regionAndRender.addAll(skeletonrenderer.getRenderedCommands());
                    SvgFileProcessor.outputSvgCommands(skeletonAndRender, "visualize_skeleton_" + skeletonName + decoFileName, info);
                    SvgFileProcessor.outputSvgCommands(regionAndRender, "visualize_region_" + skeletonName + decoFileName, info);
                    break;
                case PEBBLE:

                    List<List<SvgPathCommand>> setsOfNine = new ArrayList<>();
                    info.setDrawBound(false);
                        if (info.getSpanningTree() == null) {
                        } else {
                            skeletonrenderer = new PebbleRenderer(renderedDecoCommands, info,
                                    skeletonRenderType != RECTANGLE && skeletonGenComboBox.getValue().equals(VINE));
                            info.setRandomFactor(Double.valueOf(skeletonRenderTextField1.getText()));
                            info.setInitialLength(Double.valueOf(skeletonRenderTextField2.getText()));
                            skeletonrenderer.pebbleFilling();

                            SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName, info);
                            List<SvgPathCommand> skeletonAddRender = new ArrayList<>(skeletonCopy),
                                    regionAddRender = new ArrayList<>(regionFile.getCommandList());
                            skeletonAddRender.addAll(skeletonrenderer.getRenderedCommands());
                            regionAddRender.addAll(skeletonrenderer.getRenderedCommands());
                            SvgFileProcessor.outputSvgCommands(skeletonAddRender, "visualize_skeleton_" + skeletonName + decoFileName, info);
                            SvgFileProcessor.outputSvgCommands(regionAddRender, "visualize_region_" + skeletonName + decoFileName, info);

                        }

                    break;
                case NONE:
                    if (skeletonPathCommands.size() != 0) {
                        System.out.println("Skeleton Size" + skeletonPathCommands.size());
                        switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                            case "none":
                                skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
                                skeletonrenderer.fixedWidthFilling(0, Double.valueOf(skeletonRenderTextField1.getText()));
                                break;
                            default:
                                    /* scale deco to full*/
                                renderedDecoCommands = SvgPathCommand.commandsScaling(renderedDecoCommands,
                                        info.getPointDistributionDist() / (1.4 * Double.max(decoElementFile.getHeight(), decoElementFile.getWidth())),
                                        renderedDecoCommands.get(0).getDestinationPoint());
                                skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                        renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION, info);
                                skeletonrenderer.fixedWidthFilling(0, Double.valueOf(skeletonRenderTextField1.getText()));
                                break;
                        }
                        SvgFileProcessor.outputSvgCommands(skeletonrenderer.getRenderedCommands(), skeletonName + "_" + decoFileName, info);
                    } else {
                        System.out.println("ERROR: skeleton path commands");
                    }
                    break;
            }
        } else {
            List<SvgPathCommand> renderedCommands = skeletonPathCommands;
            switch ((SkeletonRenderType) skeletonRenderComboBox.getValue()) {
                case FIXED_WIDTH_FILL:
                    switch (((ToggleButton) patternSourceGroup.getSelectedToggle()).getText()) {
                        case "none":
                            skeletonrenderer = new PatternRenderer(skeletonPathCommands, PatternRenderer.RenderType.NO_DECORATION, info);
                            break;
                        case "from file":
                        case "from library":
                            skeletonrenderer = new PatternRenderer(regionFile.getfFileName(), skeletonPathCommands, decoFileName,
                                    renderedDecoCommands, PatternRenderer.RenderType.WITH_DECORATION, info);
                            break;
                    }
                    renderedCommands = skeletonrenderer.fixedWidthFilling(5, Double.valueOf(skeletonRenderTextField1.getText()));
                    break;

                case ALONG_PATH:
                    mergedPattern = new SpinePatternMerger(skeletonName, skeletonPathCommands, renderedDecoElemFileProcessor, true);
                    /** Combine pattern */
                    mergedPattern.combinePattern();
                    renderedCommands = boundary.fitCommandsToRegionTrimToBoundary(mergedPattern.getCombinedCommands(), info);
                    break;
                case TILING:
                    double patternHeight = skeletonPathFileProcessor.getHeight() / rows;
                    mergedPattern = new SpinePatternMerger(skeletonName, skeletonPathCommands, renderedDecoElemFileProcessor, true);
                    /** Combine pattern */
                    mergedPattern.tilePattern(patternHeight);
                    renderedCommands = boundary.fitCommandsToRegionTrimToBoundary(mergedPattern.getCombinedCommands(), info);
                    break;
            }
            SvgFileProcessor.outputSvgCommands(renderedCommands, skeletonName + "_" + decoFileName, info);
        }
    }


    private void setUpFont() {
        Label[] functionLabels = {textFieldLabel, regionSelectionLabel, skeletonGnerationlabel, skeletonRenderinglabel,
                patternLabel, svgToPatLabel, skeletonGenFieldLabel, skeletonRenderFieldLabel1, skeletonRenderFieldLabel2, skeletonRenderFieldLabel3,
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
                    SkeletonPathType skeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
                    if (skeletonPathType.isTreeStructure()) {
                        patternLibraryComboBox.getItems().setAll(endpointList);
                    } else {
                        if (skeletonRenderComboBox.getValue().equals(TILING))
                            patternLibraryComboBox.getItems().setAll(tileList);
                        else
                            patternLibraryComboBox.getItems().setAll(alongPathList);
                    }

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
            SkeletonPathType newSkeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
            System.out.println("Skeleton generation method changed: " + newSkeletonPathType);
            skeletonRenderComboBox.setValue(NONE);
            if (newSkeletonPathType.isTreeStructure() || newSkeletonPathType == POISSON_DISK) {
                patternLibraryComboBox.getItems().setAll(endpointList);
                skeletonRenderComboBox.getItems().setAll(NONE, FIXED_WIDTH_FILL, PEBBLE, RECTANGLE, CATMULL_ROM, STIPPLING);
            } else if (newSkeletonPathType.equals(SNAKE)) {
                System.out.println("Snake");
                skeletonRenderComboBox.getItems().setAll(NONE, ALONG_PATH, TILING);
            } else {
                System.out.println("case 2: none tree structure");
                skeletonRenderComboBox.getItems().setAll(NONE, ALONG_PATH);
            }

            switch (newSkeletonPathType) {
                case ECHO:
                case HILBERT_CURVE:
                case SNAKE:
                case HEXAGON:
                case TRIANGLE_TESSELLATION:
                case THREE_3_4_3_4_TESSELLATION:
                case GRID_TESSELLATION:
                case POISSON_DISK:
                case VINE:
                    if (newSkeletonPathType.isTreeStructure()) {
                        skeletonGenFieldLabel.setText("Point Distribution Distance:");

                    } else switch (newSkeletonPathType) {
                        case ECHO:
                            skeletonGenFieldLabel.setText("Repetitions:");
                            break;
                        case HILBERT_CURVE:
                            skeletonGenFieldLabel.setText("Level:");
                            break;
                        case SNAKE:
                            skeletonGenFieldLabel.setText("Rows:");
                            break;
                        default:
                            skeletonGenFieldLabel.setText("Unable to identify skeleton generation method");
                    }
                    skeletonGenPropertyInput.getChildren().setAll(skeletonGenFieldLabel, skeletonGenTextField);
                    break;
                default:
                    skeletonGenPropertyInput.getChildren().removeAll(skeletonGenFieldLabel, skeletonGenTextField);
                    break;
            }

            /* Tree structured */
            if (newSkeletonPathType.isTreeStructure()) {
                skeletonRenderPropertyInput.getChildren().setAll(skeletonRenderFieldLabel1, skeletonRenderTextField1);
            } else {
                skeletonRenderPropertyInput.getChildren().removeAll(skeletonRenderFieldLabel1, skeletonRenderTextField1);
            }
        });

        /* Skeleton Rendering Listener */
        skeletonRenderComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            SkeletonRenderType newSelected = (SkeletonRenderType) skeletonRenderComboBox.getValue();
            System.out.println("Skeleton rendering method changed: " + newSelected);
            if (newSelected.equals(PEBBLE) || newSelected.equals(RECTANGLE)) {
                patternSourceGroup.selectToggle(noPattern);
            } else {
                patternSourceGroup.selectToggle(patternFromFile);
            }

            if (newSelected.equals(TILING)) {
                patternLibraryComboBox.getItems().setAll(tileList);
            }

            if (newSelected.equals(CATMULL_ROM)) {
                skeletonRenderFieldLabel1.setText("Decoration Size");
                skeletonRenderTextField1.setText("1.6");
                skeletonRenderFieldLabel2.setText("Decoration Gap");
                skeletonRenderTextField2.setText("1.3");
                skeletonRenderFieldLabel3.setText("Initial Angle");
                skeletonRenderTextField3.setText("45");
                skeletonRenderFieldLabel4.setText("load collision");
                skeletonRenderPropertyInput.getChildren().setAll(skeletonRenderFieldLabel1, skeletonRenderTextField1,
                        skeletonRenderFieldLabel2, skeletonRenderTextField2,
                        skeletonRenderFieldLabel3, skeletonRenderTextField3, skeletonRenderFieldLabel4, loadCollisionGeoButton);
            } else {
                skeletonRenderPropertyInput.getChildren().setAll(skeletonRenderFieldLabel1, skeletonRenderTextField1);
            }
            if (newSelected.equals(PEBBLE)) {
                skeletonRenderFieldLabel1.setText("Randomness");
                skeletonRenderFieldLabel2.setText("Initial Length");
                skeletonRenderPropertyInput.getChildren().setAll(skeletonRenderFieldLabel1, skeletonRenderTextField1,
                        skeletonRenderFieldLabel2, skeletonRenderTextField2);
            } else
                skeletonRenderFieldLabel1.setText("Decoration Density");
        });


        /* Library ComboBox Listener/ pattern library combobox Listener */
        patternLibraryComboBox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            System.out.println(patternLibraryComboBox.getItems().toString());
            String newPatternFile = patternLibraryComboBox.getValue().toString();
            File library = alongPathLibrary;
            if (skeletonRenderComboBox.getValue().equals(TILING))
                library = tileLibrary;
            /* Tree Structured */
            SkeletonPathType skeletonPathType = (SkeletonPathType) skeletonGenComboBox.getValue();
            if (skeletonPathType.isTreeStructure())
                library = endpointLibrary;
            File file = new File(library.getPath() + "/" + newPatternFile + ".svg");
            System.out.println("Loading a pattern....");
            decoElementFile = new SvgFileProcessor(file);
            try {
                decoElementFile.processSvg();
                SvgFileProcessor.outputSvgCommands(decoElementFile.getCommandList(),
                        "decoElem-" + decoElementFile.getfFileName(), info);
            } catch (Exception e) {
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

    public enum SkeletonPathType {
        GRID_TESSELLATION, TRIANGLE_TESSELLATION, THREE_3_4_3_4_TESSELLATION, POISSON_DISK, HILBERT_CURVE, ECHO, MEDIAL_AXIS, SNAKE, VINE, HEXAGON;

        public boolean isTreeStructure() {
            switch (this) {
                case THREE_3_4_3_4_TESSELLATION:
                case GRID_TESSELLATION:
                case POISSON_DISK:
                case VINE:
                case HEXAGON:
                case TRIANGLE_TESSELLATION:
                    return true;
                default:
                    return false;
            }
        }

        public boolean isTessellation() {
            switch (this) {
                case THREE_3_4_3_4_TESSELLATION:
                case GRID_TESSELLATION:
                case HEXAGON:
                case TRIANGLE_TESSELLATION:
                    return true;
                default:
                    return false;
            }
        }

        public PointDistribution.RenderType getPointDistributionType() {
            switch (this) {
                case GRID_TESSELLATION:
                    return PointDistribution.RenderType.GRID;
                case VINE:
                    return PointDistribution.RenderType.VINE;
                case TRIANGLE_TESSELLATION:
                    return PointDistribution.RenderType.TRIANGLE;
                case HEXAGON:
                    return PointDistribution.RenderType.HEXAGON;
                default:
                    return PointDistribution.RenderType.THREE_THREE_FOUR_THREE_FOUR;
            }
        }
    }

    public enum SkeletonRenderType {
        NONE, FIXED_WIDTH_FILL, PEBBLE, TILING, ALONG_PATH, RECTANGLE, CATMULL_ROM, STIPPLING
    }

    public enum FileSourceType {
        FILE, LIBRARY, NONE;
    }

}
