package gui;

import DSMData.DSMData;
import DSMData.DSMItem;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.util.*;

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


    private void updateConfigWidgets() {
    // optimal size layout
        VBox optimalSizeLayout = new VBox();

        Label optimalClusterSizeLabel = new Label("Optimal Cluster Size");

        optimalSizeCluster = new SimpleDoubleProperty(7.0);
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

        powdep = new SimpleDoubleProperty(1.0);
        NumericTextField powdepEntry = new NumericTextField(powdep.getValue());
        powdepEntry.textProperty().addListener((obs, oldText, newText) -> {
            powdep.setValue(powdepEntry.getNumericValue());
        });

        powdepArea.getChildren().addAll(powdepLabel, powdepEntry);
        powdepArea.setSpacing(5);
        powdepArea.setPadding(new Insets(10));
        powdepArea.setAlignment(Pos.CENTER);


    // powdep layout
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


    private void runClusterBidsAnalysis() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        // create header row
        int currentRow = 0;
        int currentCol = 0;
        Vector<String> groupOrder = new Vector<>(matrix.getGroupings());

        HBox groupHeader = new HBox();  // group
        groupHeader.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        groupHeader.setPadding(new Insets(2));
        Label groupHeaderLabel = new Label("Current Group");
        groupHeaderLabel.setMinWidth(Region.USE_PREF_SIZE);
        groupHeader.getChildren().add(groupHeaderLabel);
        GridPane.setConstraints(groupHeader, currentCol, currentRow);
        grid.getChildren().add(groupHeader);
        currentCol += 1;

        HBox nameHeader = new HBox();  // item name
        nameHeader.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        nameHeader.setPadding(new Insets(2));
        Label nameHeaderLabel = new Label("Item Group");
        nameHeaderLabel.setMinWidth(Region.USE_PREF_SIZE);
        nameHeader.getChildren().add(nameHeaderLabel);
        GridPane.setConstraints(nameHeader, currentCol, currentRow);
        grid.getChildren().add(nameHeader);
        currentCol += 1;

        for(String groupName : groupOrder) {
            HBox cell = new HBox();
            Label text = new Label(groupName + " Bids");
            text.setMinWidth(Region.USE_PREF_SIZE);
            cell.getChildren().add(text);
            cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            cell.setPadding(new Insets(2));

            GridPane.setConstraints(cell, currentCol, currentRow);
            grid.getChildren().add(cell);
            currentCol += 1;
        }


        // add the rest of the content
        Vector<DSMItem> items = matrix.getRows();
        Collections.sort(items, Comparator.comparing(r -> r.getSortIndex()));

        currentRow = 1;  // group names
        currentCol = 0;
        for(DSMItem item : items) {
            HBox cell = new HBox();
            Label text = new Label(item.getGroup());
            text.setMinWidth(Region.USE_PREF_SIZE);
            cell.getChildren().add(text);
            cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            cell.setPadding(new Insets(2));

            GridPane.setConstraints(cell, currentCol, currentRow);
            grid.getChildren().add(cell);
            currentRow += 1;
        }

        currentRow = 1;  // item names
        currentCol = 1;
        for(DSMItem item : items) {
            HBox cell = new HBox();
            Label text = new Label(item.getName());
            text.setMinWidth(Region.USE_PREF_SIZE);
            cell.getChildren().add(text);
            cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            cell.setPadding(new Insets(2));

            GridPane.setConstraints(cell, currentCol, currentRow);
            grid.getChildren().add(cell);
            currentRow += 1;
        }

        currentCol = 2;  // bids
        for(String group : groupOrder) {
            currentRow = 1;

            HashMap<Integer, Double> groupBids = DSMData.calculateClusterBids(matrix, group, optimalSizeCluster.doubleValue(), powdep.doubleValue(), powbid.doubleValue(), countByWeight.isSelected());
            for (DSMItem item : items) {
                HBox cell = new HBox();
                DecimalFormat df = new DecimalFormat("#.##");
                Double bid = Double.valueOf(df.format(groupBids.get(item.getUid())));
                Label text = new Label(bid.toString());
                text.setMinWidth(Region.USE_PREF_SIZE);
                cell.getChildren().add(text);
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                cell.setPadding(new Insets(2));

                GridPane.setConstraints(cell, currentCol, currentRow);
                grid.getChildren().add(cell);
                currentRow += 1;
            }

            currentCol += 1;
        }


        // perform highlighting
        for(int r=1; r < items.size(); r++) {  // 1 is start row for data
            String maxBidGroup = "";
            Double maxBid = 0.0;
            String minBidGroup = "";
            Double minBid = 0.0;
            for(int c=2; c < groupOrder.size() + 2; c++) {  // 2 is start column for data, so offset it by two
                // find the cell and extract the value from it
                Double bid = 0.0;
                for (Node cell : grid.getChildren()) {
                    if (GridPane.getColumnIndex(cell) == c && GridPane.getRowIndex(cell) == r) {
                        for(Node node : ((HBox) cell).getChildren()) {
                            bid = Double.parseDouble(((Label)node).getText());
                            break;
                        }
                        break;
                    }
                }
                if(bid >= maxBid) {
                    maxBid = bid;
                    maxBidGroup = groupOrder.get(c - 2);  // subtract 2 because that is the offset
                } else if(bid <= minBid) {
                    minBid = bid;
                    minBidGroup = groupOrder.get(c - 2);  // subtract 2 because that is the offset
                }
            }

            String itemGroup = "";
            for (Node cell : grid.getChildren()) {
                if (GridPane.getColumnIndex(cell) == 0 && GridPane.getRowIndex(cell) == r) {
                    for (Node node : ((HBox) cell).getChildren()) {
                        itemGroup = ((Label) node).getText();
                        break;
                    }
                    break;
                }
            }
            if(itemGroup.equals(maxBidGroup)) {  // highlight green
                for (Node cell : grid.getChildren()) {
                    if (GridPane.getColumnIndex(cell) == 1 && GridPane.getRowIndex(cell) == r) {
                        ((HBox)cell).setBackground(new Background(new BackgroundFill(Color.color(.6, 1, .6), new CornerRadii(3), new Insets(0))));
                    }
                }
            } else if(itemGroup.equals(minBidGroup)) {  // highlight red
                for (Node cell : grid.getChildren()) {
                    if (GridPane.getColumnIndex(cell) == 1 && GridPane.getRowIndex(cell) == r) {
                        ((HBox)cell).setBackground(new Background(new BackgroundFill(Color.color(1, .6, .6), new CornerRadii(3), new Insets(0))));
                    }
                }
            }

        }

        bidsLayout.getChildren().removeAll(bidsLayout.getChildren());
        bidsLayout.getChildren().addAll(grid);
        bidsLayout.setAlignment(Pos.CENTER);
        bidsLayout.setPadding(new Insets(10));
        bidsLayout.setSpacing(15);
    }


    public void start() {
        Scene scene = new Scene(rootLayout, 800, 600);
        window.setScene(scene);
        window.show();
    }

}
