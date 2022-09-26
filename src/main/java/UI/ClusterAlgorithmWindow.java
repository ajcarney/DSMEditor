package UI;

import Matrices.Data.Entities.Grouping;
import Matrices.Data.SymmetricDSMData;
import Matrices.IOHandlers.SymmetricIOHandler;
import Matrices.Views.AbstractMatrixView;
import Matrices.Views.SymmetricView;
import UI.Widgets.NumericTextField;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


/**
 * A class that graphically runs Thebeau's dsm clustering algorithm. Currently only works for symmetric DSMs
 *
 * @author Aiden Carney
 */
public class ClusterAlgorithmWindow {
    SymmetricDSMData matrix;

    Stage window;
    private BorderPane rootLayout;

    // layouts in the border pane
    private MenuBar menuBar;        // top bar
    private VBox configLayout;      // side bar
    private SplitPane mainContent;  // center

    // config parameters for the algorithm
    private DoubleProperty optimalSizeCluster;
    private DoubleProperty powcc;
    private DoubleProperty powdep;
    private DoubleProperty powbid;
    private DoubleProperty randBid;
    private DoubleProperty randAccept;
    private DoubleProperty numLevels;
    private DoubleProperty randSeed;

    SymmetricDSMData outputMatrix = null;

    // main content panes
    private VBox coordinationLayout;
    private VBox outputMatrixLayout;

    private CheckBox countByWeight;
    private CheckBox debug;


    /**
     * Creates a ClusterAlgorithm object and initializes all the widgets. Does not open
     * the gui.
     *
     * @param matrix the input matrix to perform the algorithm on
     */
    public ClusterAlgorithmWindow(SymmetricDSMData matrix) {
        this.matrix = matrix;

        window = new Stage();
        window.setTitle(matrix.getTitle() + " - Cluster Algorithm");

        // side bar
        updateConfigWidgets();
        outputMatrixLayout = new VBox();
        ScrollPane configScrollPane = new ScrollPane(configLayout);
        configScrollPane.setFitToWidth(true);
        configScrollPane.setFitToHeight(true);

        // menu
        menuBar = new MenuBar();


        // file menu
        Menu fileMenu = new Menu("File");

        Menu exportMenu = new Menu("Export Results");
        MenuItem dsm = new MenuItem("DSM File");
        dsm.setOnAction(e -> {
           if(outputMatrix == null) {
               return;
           }
           SymmetricIOHandler ioHandler = new SymmetricIOHandler(new File(""));
           ioHandler.setMatrix(outputMatrix);
           ioHandler.promptSaveToFile(menuBar.getScene().getWindow());
        });
        MenuItem csv = new MenuItem("CSV File");
        csv.setOnAction(e -> {
            if(outputMatrix == null) {
                return;
            }
            SymmetricIOHandler ioHandler = new SymmetricIOHandler(new File(""));
            ioHandler.setMatrix(outputMatrix);
            ioHandler.promptExportToCSV(menuBar.getScene().getWindow());
        });
        MenuItem excel = new MenuItem("Excel File");
        excel.setOnAction(e -> {
            if(outputMatrix == null) {
                return;
            }
            SymmetricIOHandler ioHandler = new SymmetricIOHandler(new File(""));
            ioHandler.setMatrix(outputMatrix);
            ioHandler.promptExportToExcel(menuBar.getScene().getWindow());
        });
        MenuItem thebeau = new MenuItem("Thebeau Matlab File");
        thebeau.setOnAction(e -> {
            if(outputMatrix == null) {
                return;
            }
            SymmetricIOHandler ioHandler = new SymmetricIOHandler(new File(""));
            ioHandler.setMatrix(outputMatrix);
            ioHandler.promptExportToThebeau(menuBar.getScene().getWindow());
        });

        exportMenu.getItems().addAll(dsm, csv, excel, thebeau);
        exportMenu.setOnShown(e -> {
            if(outputMatrix == null) {
                dsm.setDisable(true);
                csv.setDisable(true);
                excel.setDisable(true);
                thebeau.setDisable(true);
            } else {
                dsm.setDisable(false);
                csv.setDisable(false);
                excel.setDisable(false);
                thebeau.setDisable(false);
            }
        });

        fileMenu.getItems().add(exportMenu);

        // run menu
        Menu runMenu = new Menu("Run");
        MenuItem run = new MenuItem("Run Algorithm");
        run.setOnAction(e -> {
            SymmetricDSMData outputMatrix = runThebeauAlgorithm();
            runCoordinationScore(outputMatrix);
        });
        runMenu.getItems().addAll(run);


        menuBar.getMenus().addAll(fileMenu, runMenu);


        // main content
        mainContent = new SplitPane();
        mainContent.setOrientation(Orientation.VERTICAL);

        outputMatrixLayout = new VBox();
        ScrollPane matrixScrollPane = new ScrollPane(outputMatrixLayout);
        matrixScrollPane.setFitToWidth(true);
        matrixScrollPane.setFitToHeight(true);

        coordinationLayout = new VBox();
        ScrollPane coordinationScrollPane = new ScrollPane(coordinationLayout);
        coordinationScrollPane.setFitToWidth(true);
        coordinationScrollPane.setFitToHeight(true);

        mainContent.getItems().addAll(matrixScrollPane, coordinationScrollPane);


        // set up main layout
        rootLayout = new BorderPane();
        rootLayout.setLeft(configScrollPane);
        rootLayout.setTop(menuBar);
        rootLayout.setCenter(mainContent);
    }


    /**
     * Function to initialize the widgets on the side pane. Called from constructor
     */
    private void updateConfigWidgets() {
        // optimal size layout
        VBox optimalSizeLayout = new VBox();

        Label optimalClusterSizeLabel = new Label("Optimal Cluster Size");

        optimalSizeCluster = new SimpleDoubleProperty(4.5);
        NumericTextField optimalSizeEntry = new NumericTextField(optimalSizeCluster.getValue());
        optimalSizeEntry.textProperty().addListener((obs, oldText, newText) -> {
            optimalSizeCluster.setValue(optimalSizeEntry.getNumericValue());
        });

        optimalSizeLayout.getChildren().addAll(optimalClusterSizeLabel, optimalSizeEntry);
        optimalSizeLayout.setSpacing(5);
        optimalSizeLayout.setPadding(new Insets(10));
        optimalSizeLayout.setAlignment(Pos.CENTER);


        // powcc layout
        VBox powccArea = new VBox();

        Label powccLabel = new Label("powcc constant");
        powccLabel.setTooltip(new Tooltip("Exponential to penalize size of clusters when calculating cluster cost"));

        powcc = new SimpleDoubleProperty(1.0);
        NumericTextField powccEntry = new NumericTextField(powcc.getValue());
        powccEntry.textProperty().addListener((obs, oldText, newText) -> {
            powcc.setValue(powccEntry.getNumericValue());
        });

        powccArea.getChildren().addAll(powccLabel, powccEntry);
        powccArea.setSpacing(5);
        powccArea.setPadding(new Insets(10));
        powccArea.setAlignment(Pos.CENTER);


        // powdep layout
        VBox powdepArea = new VBox();

        Label powdepLabel = new Label("powdep constant");
        powdepLabel.setTooltip(new Tooltip("Exponential to emphasize connections when calculating bids"));

        powdep = new SimpleDoubleProperty(4.0);
        NumericTextField powdepEntry = new NumericTextField(powdep.getValue());
        powdepEntry.textProperty().addListener((obs, oldText, newText) -> {
            powdep.setValue(powdepEntry.getNumericValue());
        });

        powdepArea.getChildren().addAll(powdepLabel, powdepEntry);
        powdepArea.setSpacing(5);
        powdepArea.setPadding(new Insets(10));
        powdepArea.setAlignment(Pos.CENTER);


        // powbid layout
        VBox powbidArea = new VBox();

        Label powbidLabel = new Label("powbid constant");
        powbidLabel.setTooltip(new Tooltip("Exponential to penalize size of clusters when calculating bids"));

        powbid = new SimpleDoubleProperty(1.0);
        NumericTextField powBidEntry = new NumericTextField(powbid.getValue());
        powBidEntry.textProperty().addListener((obs, oldText, newText) -> {
            powbid.setValue(powdepEntry.getNumericValue());
        });

        powbidArea.getChildren().addAll(powbidLabel, powBidEntry);
        powbidArea.setSpacing(5);
        powbidArea.setPadding(new Insets(10));
        powbidArea.setAlignment(Pos.CENTER);
        
        
        // randbid layout
        VBox randBidArea = new VBox();

        Label randBidLabel = new Label("rand_bid constant");
        randBidLabel.setTooltip(new Tooltip("Constant to determine how often to make slightly suboptimal change"));

        randBid = new SimpleDoubleProperty(122);
        NumericTextField randBidEntry = new NumericTextField(randBid.getValue());
        randBidEntry.textProperty().addListener((obs, oldText, newText) -> {
            randBid.setValue(randBidEntry.getNumericValue());
        });

        randBidArea.getChildren().addAll(randBidLabel, randBidEntry);
        randBidArea.setSpacing(5);
        randBidArea.setPadding(new Insets(10));
        randBidArea.setAlignment(Pos.CENTER);


        // randAccept layout
        VBox randAcceptArea = new VBox();

        Label randAcceptLabel = new Label("rand_accept constant");
        randAcceptLabel.setTooltip(new Tooltip("Constant to determine how often to make a suboptimal change"));

        randAccept = new SimpleDoubleProperty(122);
        NumericTextField randAcceptEntry = new NumericTextField(randAccept.getValue());
        randAcceptEntry.textProperty().addListener((obs, oldText, newText) -> {
            randAccept.setValue(randAcceptEntry.getNumericValue());
        });

        randAcceptArea.getChildren().addAll(randAcceptLabel, randAcceptEntry);
        randAcceptArea.setSpacing(5);
        randAcceptArea.setPadding(new Insets(10));
        randAcceptArea.setAlignment(Pos.CENTER);


        // count method layout
        VBox countMethodLayout = new VBox();
        countMethodLayout.setSpacing(10);

        countByWeight = new CheckBox("Count by Weight");
        countByWeight.setSelected(true);
        countByWeight.setMaxWidth(Double.MAX_VALUE);

        countMethodLayout.getChildren().addAll(countByWeight);
        countMethodLayout.setAlignment(Pos.CENTER);
        countMethodLayout.setPadding(new Insets(10));


        // levels layout
        VBox levelsArea = new VBox();

        Label levelsLabel = new Label("Number of Iterations");
        
        numLevels = new SimpleDoubleProperty(1000);
        NumericTextField levelsEntry = new NumericTextField(numLevels.getValue());
        levelsEntry.textProperty().addListener((obs, oldText, newText) -> {
            numLevels.setValue(levelsEntry.getNumericValue());
        });

        levelsArea.getChildren().addAll(levelsLabel, levelsEntry);
        levelsArea.setSpacing(5);
        levelsArea.setPadding(new Insets(10));
        levelsArea.setAlignment(Pos.CENTER);

        
        // randSeed layout
        VBox randSeedArea = new VBox();

        Label randSeedLabel = new Label("Random Seed");

        randSeed = new SimpleDoubleProperty(30);
        NumericTextField randSeedEntry = new NumericTextField(powbid.getValue());
        randSeedEntry.textProperty().addListener((obs, oldText, newText) -> {
            randSeed.setValue(randSeedEntry.getNumericValue());
        });

        randSeedArea.getChildren().addAll(randSeedLabel, randSeedEntry);
        randSeedArea.setSpacing(5);
        randSeedArea.setPadding(new Insets(10));
        randSeedArea.setAlignment(Pos.CENTER);


        // debug checkbox
        VBox debugLayout = new VBox();
        debugLayout.setSpacing(10);

        debug = new CheckBox("Debug to stdout");
        debug.setMaxWidth(Double.MAX_VALUE);

        debugLayout.getChildren().addAll(debug);
        debugLayout.setAlignment(Pos.CENTER);
        debugLayout.setPadding(new Insets(10));

        // config layout
        configLayout = new VBox();
        configLayout.getChildren().addAll(optimalSizeLayout, powccArea, powdepArea, powbidArea, randBidArea, randAcceptArea, countMethodLayout, randSeedArea, levelsArea, debugLayout);
        configLayout.setSpacing(15);
        configLayout.setAlignment(Pos.TOP_CENTER);
    }


    /**
     * Function to run and display the coordination score and breakdown of the output matrix from the algorithm
     * and the total coordination score of the input matrix with the given parameters
     *
     * @param matrix the matrix output from the algorithm
     */
    private void runCoordinationScore(SymmetricDSMData matrix) {
        HashMap<String, Object> coordinationScore = SymmetricDSMData.getCoordinationScore(matrix, optimalSizeCluster.doubleValue(), powcc.doubleValue(), countByWeight.isSelected());
        HashMap<String, Object> currentScores = SymmetricDSMData.getCoordinationScore(this.matrix, optimalSizeCluster.doubleValue(), powcc.doubleValue(), countByWeight.isSelected());

        Label titleLabel = new Label("Cluster Cost Analysis");
        titleLabel.setStyle(titleLabel.getStyle() + "-fx-font-weight: bold;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        VBox intraBreakDown = new VBox();
        ScrollPane intraScroll = new ScrollPane(intraBreakDown);
        for(Map.Entry<Grouping, Double> b : ((HashMap<Grouping, Double>)coordinationScore.get("IntraBreakdown")).entrySet()) {
            HBox breakdown = new HBox();
            Label value = new Label(b.getValue().toString());
            value.setStyle(value.getStyle() + "-fx-font-weight: bold;");

            breakdown.getChildren().addAll(new Label((b.getKey().getName())), value);
            breakdown.setPadding(new Insets(10));
            breakdown.setSpacing(10);

            intraBreakDown.getChildren().add(breakdown);
        }

        HBox intraTotal = new HBox();
        Label v1 = new Label(coordinationScore.get("TotalIntraCost").toString());
        v1.setStyle(v1.getStyle() + "-fx-font-weight: bold;");
        intraTotal.getChildren().addAll(new Label("Total Intra Cluster Cost:"), v1);
        intraTotal.setSpacing(10);

        HBox extraTotal = new HBox();
        Label v2 = new Label(coordinationScore.get("TotalExtraCost").toString());
        v2.setStyle(v1.getStyle() + "-fx-font-weight: bold;");
        extraTotal.getChildren().addAll(new Label("Total Extra Cluster Cost:"), v2);
        extraTotal.setSpacing(10);

        HBox total = new HBox();
        Label v3 = new Label(coordinationScore.get("TotalCost").toString());
        v3.setStyle(v1.getStyle() + "-fx-font-weight: bold;");
        total.getChildren().addAll(new Label("Total Cost:"), v3);
        total.setSpacing(10);

        HBox comparison = new HBox();
        Label v4 = new Label(currentScores.get("TotalCost").toString());
        v4.setStyle(v1.getStyle() + "-fx-font-weight: bold;");
        comparison.getChildren().addAll(new Label("Total Cost of User-Defined Groupings:"), v4);
        comparison.setSpacing(10);

        coordinationLayout.getChildren().removeAll(coordinationLayout.getChildren());
        coordinationLayout.getChildren().addAll(titleLabel, intraTotal, new Label("Intra Cost Breakdown:"), intraScroll, extraTotal, total, comparison);
        coordinationLayout.setAlignment(Pos.TOP_LEFT);
        coordinationLayout.setPadding(new Insets(10));
        coordinationLayout.setSpacing(15);
    }


    /**
     * Runs the algorithm with the parameters from the gui and returns the output matrix object. Displays the
     * matrix in the main window of the gui
     *
     * @return the new clustered matrix
     */
    private SymmetricDSMData runThebeauAlgorithm() {
        BooleanProperty completedProperty = new SimpleBooleanProperty();  // used to know when to close popup
        completedProperty.set(false);

        Thread t = new Thread(() -> {  // thread to perform the function
            outputMatrix = SymmetricDSMData.thebeauAlgorithm(
                    matrix,
                    optimalSizeCluster.doubleValue(),
                    powdep.doubleValue(),
                    powbid.doubleValue(),
                    powcc.doubleValue(),
                    randBid.doubleValue(),
                    randAccept.doubleValue(),
                    countByWeight.isSelected(),
                    numLevels.intValue(),
                    randSeed.longValue(),
                    debug.isSelected()
            );
            outputMatrix.reDistributeSortIndicesByGroup();
            completedProperty.set(true);
        });
        t.setDaemon(true);
        t.start();

        // create popup window saying how long it has been running for
        Stage popup = new Stage();
        popup.initStyle(StageStyle.UTILITY);
        popup.setTitle(matrix.getTitle() + " - Cluster Algorithm");

        VBox uptimeArea = new VBox();
        uptimeArea.setAlignment(Pos.CENTER);
        Instant start = Instant.now();
        StringProperty text = new SimpleStringProperty("Uptime: 00:00:00");
        Label l = new Label();
        l.textProperty().bind(text);
        uptimeArea.getChildren().add(l);

        Scene popupScene = new Scene(uptimeArea, 200, 100);
        popup.setScene(popupScene);

        Thread t2 = new Thread(() -> {  // thread to update the text to the thread
            while(!completedProperty.get()) {
                Platform.runLater(() -> {
                    long seconds = Duration.between(start, Instant.now()).getSeconds();
                    String dt = String.format(
                            "%02d:%02d:%02d",
                            seconds / 3600,
                            (seconds % 3600) / 60,
                            seconds % 60
                    );
                    text.set("Uptime: " + dt);
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(() -> {
                text.set("Finished...");
            });
        });
        t2.setDaemon(true);
        t2.start();

        completedProperty.addListener((observable, oldValue, newValue) -> {  // close window when completed
            if(newValue) {
                Platform.runLater(() -> {
                    popup.close();
                });
            }
        });

        popup.showAndWait();  // wait for it to finish

        SymmetricView gui = new SymmetricView(outputMatrix, 10);
        gui.setCurrentMode(AbstractMatrixView.MatrixViewMode.STATIC_NAMES);
        gui.refreshView();

        outputMatrixLayout.getChildren().removeAll(outputMatrixLayout.getChildren());
        outputMatrixLayout.getChildren().addAll(gui.getView());

        return outputMatrix;
    }


    /**
     * opens the gui window
     */
    public void start() {
        Scene scene = new Scene(rootLayout, 800, 600);
        window.setScene(scene);
        window.show();
    }

}
