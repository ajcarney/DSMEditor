package gui;

import DSMData.DSMData;
import DSMData.DSMItem;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableStringValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;


/**
 * Graphically displays the analysis for a given matrix according the Thebeau's algorithm. Useful for
 * debugging of Thebeau's clustering algorithm.
 *
 * @author Aiden Carney
 */
public class ClusterAnalysis {
    DSMData matrix;

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
    private VBox coordinationLayout;
    private VBox bidsLayout;

    private CheckBox countByWeight;


    /**
     * Initializes all the widgets but does not open the gui window
     *
     * @param matrix The matrix to be analyzed
     */
    public ClusterAnalysis(DSMData matrix) {
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
        HashMap<String, Object> coordinationScore = DSMData.getCoordinationScore(matrix, optimalSizeCluster.doubleValue(), powcc.doubleValue(), countByWeight.isSelected());

        Label titleLabel = new Label("Cluster Cost Analysis");
        titleLabel.setStyle(titleLabel.getStyle() + "-fx-font-weight: bold;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

        VBox intraBreakDown = new VBox();
        ScrollPane intraScroll = new ScrollPane(intraBreakDown);
        for(Map.Entry<String, Double> b : ((HashMap<String, Double>)coordinationScore.get("IntraBreakdown")).entrySet()) {
            HBox breakdown = new HBox();
            Label value = new Label(b.getValue().toString());
            value.setStyle(value.getStyle() + "-fx-font-weight: bold;");

            breakdown.getChildren().addAll(new Label(b.getKey()), value);
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
        class CellData {
            private ObservableStringValue name;
            private ObservableIntegerValue highlight;

            public CellData(String name, int highlight) {
                this.highlight = new SimpleIntegerProperty(highlight);
                this.name = new SimpleStringProperty(name);
            }

            public ObservableStringValue getName() {
                return name;
            }

            public ObservableIntegerValue getHighlight() {
                return highlight;
            }
        }

        Vector<String> groupOrder = new Vector<>(matrix.getGroupings());
        Vector<DSMItem> items = matrix.getRows();
        Collections.sort(items, Comparator.comparing(r -> r.getSortIndex()));

        // create data structure for the table
        ArrayList<ArrayList<CellData>> data = new ArrayList<>();
        ArrayList<Integer> highlightColors = new ArrayList<>();  // 0 for none, 1 for red, 2 for green
        for(int r = 0; r < items.size(); r++) {
            ArrayList<String> rowBids = new ArrayList<>();
            double maxBid = 0;
            double minBid = 0;
            String maxBidGroup = "";
            String minBidGroup = "";
            for(int c = 0; c < groupOrder.size() + 2; c++) {  // add two to include header columns
                if(c == 0) {
                    rowBids.add(items.get(r).getGroup());
                } else if(c == 1) {
                    rowBids.add(items.get(r).getName());
                } else {
                    HashMap<Integer, Double> groupBids = DSMData.calculateClusterBids(matrix, groupOrder.get(c - 2), optimalSizeCluster.doubleValue(), powdep.doubleValue(), powbid.doubleValue(), countByWeight.isSelected());

                    double bid = groupBids.get(items.get(r).getUid());
                    rowBids.add(String.valueOf(bid));

                    if(bid > maxBid) {
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
            int highlightColor = 0;
            if(maxBidGroup.equals(items.get(r).getGroup())) {  // decide row highlight color
                highlightColor = 2;
            } else if(minBidGroup.equals(items.get(r).getGroup())) {
                highlightColor = 1;
            }

            // add row to data structure
            ArrayList<CellData> row = new ArrayList<>();
            for(String bid : rowBids) {
                row.add(new CellData(bid, highlightColor));
            }
            data.add(row);
        }

        // create table and columns
        TableView<ArrayList<CellData>> table = new TableView<>();  // column name, cell data
        table.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(table, Priority.ALWAYS);

        for(int c = 0; c < data.get(0).size(); c++) {
            TableColumn<ArrayList<CellData>, String> column = new TableColumn<>();
            if(c == 0) {
                column.setText("Current Group");
            } else if(c == 1) {
                column.setText("Item Name");
            } else {
                column.setText(groupOrder.get(c - 2) + " Bids");
            }
            int finalC = c;
            column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().get(finalC).getName().getValue() + String.valueOf(cellData.getValue().get(finalC).getHighlight().getValue())));

            column.setCellFactory(col -> {
                return new TableCell<ArrayList<CellData>, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                            setStyle("");
                        } else {
                            // Format date.
                            String text = item.substring(0, item.length() - 1);
                            Integer highlight = Integer.parseInt(item.substring(item.length() - 1, item.length()));
                            setText(text);
                            if(highlight == 2) {
                                setStyle("-fx-background-color:palegreen");
                            } else if(highlight == 1) {
                                setStyle("-fx-background-color:indianred");
                            } else {
                                setStyle("-fx-background-color:white");
                            }
                        }
                    }
                };
            });

            table.getColumns().add(column);
        }

        table.getItems().addAll(data);


        bidsLayout.getChildren().removeAll(bidsLayout.getChildren());
        bidsLayout.getChildren().addAll(table);
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
