package UI;

import Matrices.Data.Entities.Grouping;
import Matrices.Data.SymmetricDSMData;
import Matrices.Data.Entities.DSMItem;
import UI.Widgets.FreezeGrid;
import UI.Widgets.NumericTextField;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.*;


/**
 * Graphically displays the analysis for a given matrix according the Thebeau's algorithm. Useful for
 * debugging of Thebeau's clustering algorithm.
 *
 * @author Aiden Carney
 */
public class ClusterAnalysisWindow {
    SymmetricDSMData matrix;

    Stage window;
    private BorderPane rootLayout;

    // layouts in the border pane
    private MenuBar menuBar;        // top bar
    private VBox configLayout;      // side bar
    private SplitPane mainContent;  // center

    // config parameters
    private DoubleProperty optimalSizeCluster;
    private DoubleProperty powcc;
    private DoubleProperty powdep;
    private DoubleProperty powbid;

    // main content panes
    private final VBox coordinationLayout;
    private final VBox bidsLayout;

    private CheckBox countByWeight;


    /**
     * Initializes all the widgets but does not open the gui window
     *
     * @param matrix The matrix to be analyzed
     */
    public ClusterAnalysisWindow(SymmetricDSMData matrix) {
        this.matrix = matrix;

        window = new Stage();
//        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle(matrix.getTitle() + " - Cluster Analysis");

        // side bar
        updateConfigWidgets();

        // menu
        menuBar = new MenuBar();

        // run menu
        Menu runMenu = new Menu("Run");
        MenuItem run = new MenuItem("Run Cluster Analysis");
        run.setOnAction(e -> {
            runClusterBidsAnalysis();
            runCoordinationScore();
        });
        runMenu.getItems().addAll(run);

        menuBar.getMenus().addAll(runMenu);


    // main content
        mainContent = new SplitPane();
        mainContent.setOrientation(Orientation.VERTICAL);

        bidsLayout = new VBox();
        ScrollPane bidsScrollPane = new ScrollPane(bidsLayout);
        bidsScrollPane.setFitToWidth(true);
        bidsScrollPane.setFitToHeight(true);

        coordinationLayout = new VBox();
        ScrollPane coordinationScrollPane = new ScrollPane(coordinationLayout);
        coordinationScrollPane.setFitToWidth(true);
        coordinationScrollPane.setFitToHeight(true);

        mainContent.getItems().addAll(bidsScrollPane, coordinationScrollPane);


        // set up main layout
        rootLayout = new BorderPane();
        rootLayout.setLeft(configLayout);
        rootLayout.setTop(menuBar);
        rootLayout.setCenter(mainContent);
    }


    /**
     * initializes the widgets on the side panel of the gui. Called from the constructor
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


    // count method layout
        VBox countMethodLayout = new VBox();
        countMethodLayout.setSpacing(10);

        countByWeight = new CheckBox("Count by Weight");
        countByWeight.setSelected(true);
        countByWeight.setMaxWidth(Double.MAX_VALUE);

        countMethodLayout.getChildren().addAll(optimalSizeLayout, countByWeight);
        countMethodLayout.setAlignment(Pos.CENTER);
        countMethodLayout.setPadding(new Insets(10));

    // config layout
        configLayout = new VBox();
        configLayout.getChildren().addAll(optimalSizeLayout, powccArea, powdepArea, powbidArea, countMethodLayout);
        configLayout.setSpacing(15);
        configLayout.setAlignment(Pos.TOP_CENTER);
    }


    /**
     * runs the algorithm that determines the coordination score of a matrix. Updates content on the main window of the gui
     */
    private void runCoordinationScore() {
        HashMap<String, Object> coordinationScore = SymmetricDSMData.getCoordinationScore(matrix, optimalSizeCluster.doubleValue(), powcc.doubleValue(), countByWeight.isSelected());

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

            breakdown.getChildren().addAll(new Label(b.getKey().getName()), value);
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

        coordinationLayout.getChildren().removeAll(coordinationLayout.getChildren());
        coordinationLayout.getChildren().addAll(titleLabel, intraTotal, new Label("Intra Cost Breakdown:"), intraScroll, extraTotal, total);
        coordinationLayout.setAlignment(Pos.TOP_LEFT);
        coordinationLayout.setPadding(new Insets(10));
        coordinationLayout.setSpacing(15);
    }


    /**
     * Runs the bidding analysis algorithm for the input matrix. Updates content on the main window of the gui
     */
    private void runClusterBidsAnalysis() {
        Vector<Grouping> groupOrder = new Vector<>(matrix.getGroupings());
        Vector<DSMItem> items = matrix.getRows();
        items.sort(Comparator.comparing(DSMItem::getSortIndex));

        // create data structure for the table
        ArrayList<ArrayList<HBox>> data = new ArrayList<>();

        // fill in the main data structure
        for(int r = 0; r < items.size(); r++) {
            ArrayList<String> rowBids = new ArrayList<>();
            double maxBid = 0;
            double minBid = Double.MAX_VALUE;
            Grouping maxBidGroup = new Grouping("", Color.color(1.0, 1.0, 1.0));
            Grouping minBidGroup = new Grouping("", Color.color(1.0, 1.0, 1.0));
            for(int c = 0; c < groupOrder.size() + 2; c++) {  // add two to include header columns
                if(c == 0) {
                    rowBids.add(items.get(r).getGroup1().getName());
                } else if(c == 1) {
                    rowBids.add(items.get(r).getName().getValue());
                } else {
                    HashMap<Integer, Double> groupBids = SymmetricDSMData.calculateClusterBids(matrix, groupOrder.get(c - 2), optimalSizeCluster.doubleValue(), powdep.doubleValue(), powbid.doubleValue(), countByWeight.isSelected());

                    double bid = groupBids.get(items.get(r).getUid());
                    rowBids.add(String.valueOf(bid));

                    if(bid > maxBid) {  // check for max or min bids
                        if(maxBid < minBid) {
                            minBid = maxBid;
                            minBidGroup = maxBidGroup;
                        }
                        maxBid = bid;
                        maxBidGroup = groupOrder.get(c - 2);
                    } else if(bid < minBid) {
                        if(minBid > maxBid) {
                            maxBid = minBid;
                            maxBidGroup = minBidGroup;
                        }
                        minBid = bid;
                        minBidGroup = groupOrder.get(c - 2);
                    }
                }
            }

            // add row to data structure
            ArrayList<HBox> row = new ArrayList<>();
            for(String text : rowBids) {
                HBox cell = new HBox();
                Label label = new Label(text);
                if(maxBidGroup.getUid().equals(items.get(r).getGroup1().getUid())) {  // highlight green because group has highest bid
                    cell.setStyle("-fx-background-color:palegreen");
                } else if(minBidGroup.getUid().equals(items.get(r).getGroup1().getUid())) {  // highlight red because group has lowest bid
                    cell.setStyle("-fx-background-color:indianred");
                } else {
                    cell.setStyle("-fx-background-color:white");
                }
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                cell.setPadding(new Insets(5));
                Group g = new Group();  // add to group so that size of label can be managed properly
                g.getChildren().add(label);
                cell.getChildren().add(g);

                row.add(cell);
            }
            data.add(row);
        }

        // create header row
        ArrayList<HBox> headerRow = new ArrayList<>();
        for(int c = 0; c < data.get(0).size(); c++) {
            HBox cell = new HBox();
            Label label = new Label();

            if (c == 0) {
                label.setText("Current Group");
            } else if (c == 1) {
                label.setText("Item Name");
            } else {
                label.setText(groupOrder.get(c - 2).getName() + " Bids");
            }
            label.setStyle(label.getStyle() + "-fx-font-weight: bold;" + "-fx-font-size: 18;");
            label.setAlignment(Pos.CENTER);

            cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            cell.setPadding(new Insets(5));
            Group g = new Group();  // add to group so that size of label can be managed properly
            g.getChildren().add(label);
            cell.getChildren().add(g);
            cell.setAlignment(Pos.CENTER);

            headerRow.add(cell);
        }
        data.add(0, headerRow);

        FreezeGrid table = new FreezeGrid();
        table.setGridDataHBox(data);
        table.setFreezeHeader(1);
        table.setFreezeLeft(2);
        table.resizeGrid(true, new ArrayList<>(), new ArrayList<>());
        table.updateGrid();

        bidsLayout.getChildren().clear();
        bidsLayout.getChildren().addAll(table.getGrid());
        bidsLayout.setAlignment(Pos.CENTER);
        bidsLayout.setPadding(new Insets(10));
        bidsLayout.setSpacing(15);
    }


    /**
     * Opens the gui window for user interaction
     */
    public void start() {
        Scene scene = new Scene(rootLayout, 800, 600);
        window.setScene(scene);
        window.show();
    }

}
