package UI;

import Matrices.ClusterAlgorithms.Thebeau;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.SymmetricDSMData;
import Matrices.IOHandlers.SymmetricIOHandler;
import UI.MatrixViews.AbstractMatrixView;
import UI.MatrixViews.SymmetricView;
import UI.ClusterAlgorithmViews.ART1View;
import UI.ClusterAlgorithmViews.IAlgorithmView;
import UI.ClusterAlgorithmViews.ParameterBuilder;
import UI.ClusterAlgorithmViews.ThebeauView;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


/**
 * A class that graphically runs different clustering algorithms. Currently only works for symmetric DSMs
 *
 * @author Aiden Carney
 */
public class ClusterAlgorithmWindow {
    ObservableList<String> algorithms = FXCollections.observableArrayList(
        "Thebeau",
            "ART1"
    );
    IAlgorithmView algorithmView;

    SymmetricDSMData matrix;

    Stage window;
    private final BorderPane rootLayout;

    private DoubleProperty optimalSizeCluster;
    private DoubleProperty powcc;
    private DoubleProperty powdep;
    private DoubleProperty powbid;
    private DoubleProperty randBid;
    private DoubleProperty randAccept;
    private DoubleProperty randSeed;
    private BooleanProperty countByWeight;

    SymmetricDSMData outputMatrix = null;

    // main content panes
    private final VBox coordinationLayout;
    private final VBox outputMatrixLayout;


    /**
     * Creates a ClusterAlgorithm object and initializes all the widgets. Does not open
     * the gui.
     *
     * @param matrix the input matrix to perform the algorithm on
     */
    public ClusterAlgorithmWindow(SymmetricDSMData matrix) {
        this.matrix = matrix;

        window = new Stage();
        if(!matrix.getTitle().isEmpty()) {
            window.setTitle(matrix.getTitle() + " - Cluster Algorithms");
        } else {
            window.setTitle("Cluster Algorithms");
        }

        // left sidebar
        algorithmView = new ThebeauView();  // default to thebeau mode
        VBox leftSideBar = getAlgorithmParametersView();

        ScrollPane leftSidebarScrollPane = new ScrollPane(leftSideBar);
        leftSidebarScrollPane.setFitToWidth(true);
        leftSidebarScrollPane.setFitToHeight(true);


        // right sidebar
        VBox rightSideBar = getCostParametersView();
        ScrollPane rightSidebarScrollPane = new ScrollPane(rightSideBar);
        rightSidebarScrollPane.setFitToWidth(true);
        rightSidebarScrollPane.setFitToHeight(true);


        // menu
        MenuBar menuBar = getMenuBar();


        // main content
        SplitPane dsmOutputLayout = new SplitPane();
        dsmOutputLayout.setOrientation(Orientation.VERTICAL);

        outputMatrixLayout = new VBox();
        ScrollPane matrixScrollPane = new ScrollPane(outputMatrixLayout);
        matrixScrollPane.setFitToWidth(true);
        matrixScrollPane.setFitToHeight(true);

        coordinationLayout = new VBox();
        ScrollPane coordinationScrollPane = new ScrollPane(coordinationLayout);
        coordinationScrollPane.setFitToWidth(true);
        coordinationScrollPane.setFitToHeight(true);

        dsmOutputLayout.getItems().addAll(matrixScrollPane, coordinationScrollPane);

        // put main content in splitpane so it can be moved around
        SplitPane content = new SplitPane();
        content.getItems().addAll(leftSidebarScrollPane, dsmOutputLayout, rightSidebarScrollPane);
        content.setDividerPosition(0, 0.22);
        content.setDividerPosition(1, 0.8);


        // set up main layout
        rootLayout = new BorderPane();
        rootLayout.setTop(menuBar);
        rootLayout.setCenter(content);
    }


    /**
     * Creates the layout that manages the parameters for the algorithm
     *
     * @return the created layout with all widgets configured properly
     */
    private VBox getAlgorithmParametersView() {
        VBox leftSideBar = new VBox();

        Label parametersLabel = new Label("Algorithm Parameters");
        parametersLabel.setFont(new Font(18));
        parametersLabel.setPadding(new Insets(5));

        VBox algorithmParametersPane = new VBox();
        algorithmParametersPane.getChildren().add(algorithmView.getParametersPane(matrix));

        // label and combobox for choosing the clustering algorithm
        HBox algorithmTypePane = new HBox();
        algorithmTypePane.setSpacing(5);

        Label algorithmTypeLabel = new Label("Algorithm\nType:");
        algorithmTypeLabel.setMinHeight(Region.USE_PREF_SIZE);

        ComboBox<String> algorithmType = new ComboBox<>();
        algorithmType.setItems(algorithms);
        algorithmType.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if("Thebeau".equals(newValue)) {
                algorithmView = new ThebeauView();
            } else if("ART1".equals(newValue)) {
                algorithmView = new ART1View();
            }
            algorithmParametersPane.getChildren().clear();
            algorithmParametersPane.getChildren().add(algorithmView.getParametersPane(matrix));
        });
        algorithmType.setValue("Thebeau");  // default to thebeau algorithm
        algorithmTypePane.getChildren().addAll(algorithmTypeLabel, algorithmType);


        leftSideBar.getChildren().addAll(parametersLabel, algorithmTypePane, new Separator(), algorithmParametersPane);
        leftSideBar.setSpacing(10);
        leftSideBar.setMinWidth(Region.USE_PREF_SIZE);

        return leftSideBar;
    }


    /**
     * Creates a layout containing all the parameters for analyzing clusters
     *
     * @return VBox containing all the widgets configured properly
     */
    private VBox getCostParametersView() {
        optimalSizeCluster = new SimpleDoubleProperty(4.5);
        powcc = new SimpleDoubleProperty(1.0);
        powdep = new SimpleDoubleProperty(4.0);
        powbid = new SimpleDoubleProperty(1.0);
        randBid = new SimpleDoubleProperty(122);
        randAccept = new SimpleDoubleProperty(122);
        countByWeight = new SimpleBooleanProperty(false);
        randSeed = new SimpleDoubleProperty(30);

        // button the re-run analysis
        Button rerun = new Button("Update Analysis");
        rerun.setOnAction(e -> runCoordinationScore(outputMatrix));

        // config layout
        VBox costParametersLayout = new ParameterBuilder()
                .newLabel("Cluster Analysis Parameters", 18.0)
                .newNumericEntry(optimalSizeCluster, "Optimal Cluster Size", "", false)
                .newNumericEntry(powcc, "powcc constant", "Exponential to penalize size of clusters when calculating cluster cost", false)
                .newNumericEntry(powdep, "powdep constant", "Exponential to emphasize connections when calculating bids", false)
                .newNumericEntry(powbid, "powbid constant", "Exponential to penalize size of clusters when calculating bids", false)
                .newNumericEntry(randBid, "rand_bid constant", "Constant to determine how often to make slightly suboptimal change", false)
                .newNumericEntry(randAccept, "rand_accept constant", "Constant to determine how often to make a suboptimal change", false)
                .newCheckbox(countByWeight, "Count by Weight")
                .newNumericEntry(randSeed, "Random Seed", "", false)
                .build();

        costParametersLayout.getChildren().addAll(new Separator(), rerun);

        return costParametersLayout;
    }


    /**
     * Creates a menu-bar object for the window
     *
     * @return the created menu-bar with all the widgets added and configured properly
     */
    private MenuBar getMenuBar() {
        MenuBar menuBar = new MenuBar();

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
            runAlgorithm();
            runCoordinationScore(outputMatrix);
        });
        runMenu.getItems().addAll(run);


        menuBar.getMenus().addAll(fileMenu, runMenu);

        return menuBar;
    }


    /**
     * Function to run and display the coordination score and breakdown of the output matrix from the algorithm
     * and the total coordination score of the input matrix with the given parameters
     *
     * @param matrix the matrix output from the algorithm
     */
    private void runCoordinationScore(SymmetricDSMData matrix) {
        if(matrix == null) return;

        Thebeau.CoordinationScore coordinationScore = Thebeau.getCoordinationScore(matrix, optimalSizeCluster.doubleValue(), powcc.doubleValue(), countByWeight.getValue());
        Thebeau.CoordinationScore currentScores = Thebeau.getCoordinationScore(this.matrix, optimalSizeCluster.doubleValue(), powcc.doubleValue(), countByWeight.getValue());

        Label titleLabel = new Label("Cluster Cost Analysis");
        titleLabel.setStyle(titleLabel.getStyle() + "-fx-font-weight: bold;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        VBox intraBreakDown = new VBox();
        ScrollPane intraScroll = new ScrollPane(intraBreakDown);
        for(Map.Entry<Grouping, Double> b : coordinationScore.intraBreakdown.entrySet()) {
            HBox breakdown = new HBox();
            Label value = new Label(b.getValue().toString());
            value.setStyle(value.getStyle() + "-fx-font-weight: bold;");

            breakdown.getChildren().addAll(new Label((b.getKey().getName())), value);
            breakdown.setPadding(new Insets(10));
            breakdown.setSpacing(10);

            intraBreakDown.getChildren().add(breakdown);
        }

        HBox intraTotal = new HBox();
        Label v1 = new Label(String.valueOf(coordinationScore.totalIntraCost));
        v1.setStyle(v1.getStyle() + "-fx-font-weight: bold;");
        intraTotal.getChildren().addAll(new Label("Total Intra Cluster Cost:"), v1);
        intraTotal.setSpacing(10);

        HBox extraTotal = new HBox();
        Label v2 = new Label(String.valueOf(coordinationScore.totalExtraCost));
        v2.setStyle(v1.getStyle() + "-fx-font-weight: bold;");
        extraTotal.getChildren().addAll(new Label("Total Extra Cluster Cost:"), v2);
        extraTotal.setSpacing(10);

        HBox total = new HBox();
        Label v3 = new Label(String.valueOf(coordinationScore.totalCost));
        v3.setStyle(v1.getStyle() + "-fx-font-weight: bold;");
        total.getChildren().addAll(new Label("Total Cost:"), v3);
        total.setSpacing(10);

        HBox comparison = new HBox();
        Label v4 = new Label(String.valueOf(currentScores.totalCost));
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
     */
    private void runAlgorithm() {
        BooleanProperty completedProperty = new SimpleBooleanProperty();  // used to know when to close popup
        completedProperty.set(false);

        Thread t = new Thread(() -> {  // thread to perform the function
            outputMatrix = algorithmView.runSimulation(matrix);
            completedProperty.set(true);
        });

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

        completedProperty.addListener((observable, oldValue, newValue) -> {  // close window when completed
            if(newValue) {
                Platform.runLater(popup::close);
            }
        });


        t.setDaemon(true);
        t2.setDaemon(true);
        t.start();
        t2.start();
        popup.showAndWait();  // wait for it to finish

        if(outputMatrix != null) {
            SymmetricView gui = new SymmetricView(outputMatrix, 10);
            gui.setCurrentMode(AbstractMatrixView.MatrixViewMode.STATIC_NAMES);
            gui.refreshView();

            outputMatrixLayout.getChildren().removeAll(outputMatrixLayout.getChildren());
            outputMatrixLayout.getChildren().addAll(gui.getView());
        }
    }


    /**
     * opens the gui window
     * @param parentWindow the parents window so that the scene can open centered
     */
    public void start(Window parentWindow) {
        Scene scene = new Scene(rootLayout, 1400, 700);
        window.setScene(scene);
        window.initOwner(parentWindow);
        window.show();
    }

}
