package View.MatrixHandlers;

import Data.*;
import View.Widgets.NumericTextField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.util.Callback;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;


/**
 * class for viewing and editing asymmetric matrices
 */
public class AsymmetricMatrixHandler extends TemplateMatrixHandler<AsymmetricDSM> {

    /**
     * Returns a MatrixGuiHandler object for a given matrix
     *
     * @param matrix   the SymmetricDSM object to display
     * @param fontSize the default font size to display the matrix with
     */
    public AsymmetricMatrixHandler(AsymmetricDSM matrix, double fontSize) {
        super(matrix, fontSize);
    }


    /**
     * updates the background color of a cell based on the backgrounds set for it. Error highlight
     * is given the highest priority, then cross highlighting, then user highlighting, and lastly the grouping
     * color of the cell or the default color
     */
    @Override
    public void refreshCellHighlight(Cell cell) {
        if (cell.getHighlightBG("error") != null) {
            cell.setCellHighlight(cell.getHighlightBG("error"));
            cell.setCellTextColor(Grouping.defaultFontColor);

        } else if(cell.getHighlightBG("symmetryError") != null) {
            cell.setCellHighlight(cell.getHighlightBG("symmetryError"));
            cell.setCellTextColor(Grouping.defaultFontColor);

        } else if(cell.getHighlightBG("search") != null) {
            cell.setCellHighlight(cell.getHighlightBG("search"));
            cell.setCellTextColor(Grouping.defaultFontColor);

        } else if (cell.getHighlightBG("cross") != null && cell.getCrossHighlightEnabled()) {
            cell.setCellHighlight(cell.getHighlightBG("cross"));
            cell.setCellTextColor(Grouping.defaultFontColor);

        } else if (cell.getHighlightBG("user") != null) {
            cell.setCellHighlight(cell.getHighlightBG("user"));
            cell.setCellTextColor(Grouping.defaultFontColor);

        } else {  // default background determined by groupings
            Integer rowUid = getUidsFromGridLoc(cell.getGridLocation()).getKey();
            Integer colUid = getUidsFromGridLoc(cell.getGridLocation()).getValue();
            if (rowUid == null && colUid != null) {  // highlight with column color
                cell.setCellHighlight(matrix.getItem(colUid).getGroup1().getColor());
                cell.setCellTextColor(matrix.getItem(colUid).getGroup1().getFontColor());
                return;
            } else if (rowUid != null && colUid == null) {  // highlight with row color
                cell.setCellHighlight(matrix.getItem(rowUid).getGroup1().getColor());
                cell.setCellTextColor(matrix.getItem(rowUid).getGroup1().getFontColor());
                return;
            } else if (rowUid != null && colUid != null) {  // highlight with row group color and border it with the col group color
                Stop[] stops = new Stop[] { new Stop(0, matrix.getItem(rowUid).getGroup1().getColor()), new Stop(1, matrix.getItem(colUid).getGroup1().getColor())};
                LinearGradient lg1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

                cell.setCellHighlight(new Background(new BackgroundFill(lg1, new CornerRadii(3), new Insets(0))));  // row and column color will be the same because row and column
                cell.setCellTextColor(matrix.getItem(rowUid).getGroup1().getFontColor());
                return;
            }

            cell.setCellHighlight(cell.getHighlightBG("default"));
        }
    }


    /**
     * Creates the gui that displays a matrix. Uses the SymmetricDSM's getGridArray() method to create the grid.
     * Puts grid in a scroll pane and adds a location label (displays connection row, column) at the bottom of the VBox.
     */
    @Override
    public void refreshMatrixEditor() {
        cells = new Vector<>();
        gridUidLookup = new HashMap<>();
        gridUidLookup.put("rows", new HashMap<>());
        gridUidLookup.put("cols", new HashMap<>());

        rootLayout.getChildren().removeAll(rootLayout.getChildren());
        rootLayout.setAlignment(Pos.CENTER);

        Label locationLabel = new Label("");
        grid.clear();

        ArrayList<ArrayList<Pair<String, Object>>> template = matrix.getGridArray();
        ArrayList<ArrayList<HBox>> gridData = new ArrayList<>();

        int rows = template.size();
        int columns = template.get(0).size();

        // create a test item to determine layout width for a vertical item cell to square up the matrix when viewed
        ComboBox<Grouping> _groupings = new ComboBox<>();
        _groupings.setMinWidth(Region.USE_PREF_SIZE);
        _groupings.setPadding(new Insets(0));
        _groupings.setStyle("-fx-background-color: transparent; -fx-padding: 0, 0, 0, 0; -fx-font-size: " + (fontSize.doubleValue()) + " };");
        Pane _ghostPane = new Pane();
        Scene _ghostScene = new Scene(_ghostPane);  // a scene is needed to calculate preferred sizes of nodes

        _ghostPane.getChildren().add(_groupings);
        _ghostPane.applyCss();
        _ghostPane.layout();
        double maxHeight = _groupings.getBoundsInLocal().getHeight() + _groupings.getPadding().getTop() + _groupings.getPadding().getBottom();


        for(int r=0; r<rows; r++) {
            ArrayList<HBox> rowData = new ArrayList<>();
            for(int c=0; c<columns; c++) {
                Pair<String, Object> item = template.get(r).get(c);
                HBox cell = new HBox();  // wrap everything in an HBox so a border can be added easily
                Label label = null;

                Background defaultBackground = DEFAULT_BACKGROUND;

                switch (item.getKey()) {
                    case "plain_text" -> {
                        label = new Label((String) item.getValue());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case "plain_text_v" -> {
                        label = new Label((String) item.getValue());
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case "item_name" -> {
                        label = new Label();
                        label.textProperty().bind(((DSMItem) item.getValue()).getName());
                        cell.setAlignment(Pos.CENTER_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                        cell.setOnMouseClicked(e -> {
                            if (e.getButton().equals(MouseButton.PRIMARY)) {
                                editItemName(((DSMItem) item.getValue()).getUid());
                                grid.resizeGrid();
                                grid.updateGrid();
                            }
                        });
                    }
                    case "item_name_v" -> {
                        label = new Label();
                        label.textProperty().bind(((DSMItem) item.getValue()).getName());
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_CENTER);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                        cell.setOnMouseClicked(e -> {
                            if (e.getButton().equals(MouseButton.PRIMARY)) {
                                editItemName(((DSMItem) item.getValue()).getUid());
                                grid.resizeGrid();
                                grid.updateGrid();
                            }
                        });
                        cell.setMinWidth(maxHeight);  // set a min width so that the matrix is less boxy (all connection items will follow this even if not
                        // explicitly set due to how the freeze grid is set up)
                    }
                    case "grouping_item" -> {  // dropdown box for choosing group
                        ComboBox<Grouping> groupings = new ComboBox<>();
                        groupings.setMinWidth(Region.USE_PREF_SIZE);
                        groupings.setPadding(new Insets(0));
                        groupings.setStyle(
                                "-fx-background-color: transparent;" +
                                "-fx-padding: 0, 0, 0, 0;" +
                                "-fx-font-size: " + (fontSize.doubleValue()) + " };"
                        );

                        Callback<ListView<Grouping>, ListCell<Grouping>> cellFactory = new Callback<>() {
                            @Override
                            public ListCell<Grouping> call(ListView<Grouping> l) {
                                return new ListCell<>() {

                                    @Override
                                    protected void updateItem(Grouping group, boolean empty) {
                                        super.updateItem(group, empty);

                                        if (empty || group == null) {
                                            setText(null);
                                        } else {
                                            setText(group.getName());
                                            // this is a stupid janky hack because javafx styling is stupid and hard to work with when you want it to be dynamic
                                            // this sets the text color of the grouping item so that the font color can be updated
                                            if(group.equals(groupings.getValue())) {
                                                setTextFill(group.getFontColor());
                                            } else {
                                                setTextFill(Grouping.defaultFontColor);
                                            }
                                        }
                                    }
                                };
                            }
                        };
                        groupings.setCellFactory(cellFactory);
                        groupings.setButtonCell(cellFactory.call(null));

                        groupings.getItems().addAll(matrix.getGroupings());
                        groupings.getItems().add(matrix.getDefaultGrouping());
                        groupings.getSelectionModel().select(((DSMItem) item.getValue()).getGroup1());
                        groupings.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                            matrix.setItemGroup((DSMItem) item.getValue(), groupings.getValue());
                            matrix.setCurrentStateAsCheckpoint();
                            for (Cell c_ : cells) {
                                refreshCellHighlight(c_);
                            }
                        });

                        cell.getChildren().add(groupings);
                    }
                    case "grouping_item_v" -> {  // dropdown box for choosing group
                        ComboBox<Grouping> groupings = new ComboBox<>();
                        groupings.setMinWidth(Region.USE_PREF_SIZE);
                        groupings.setPadding(new Insets(0));
                        groupings.setStyle(
                                "-fx-background-color: transparent;" +
                                "-fx-padding: 0, 0, 0, 0;" +
                                "-fx-font-size: " + (fontSize.doubleValue()) + " };"
                        );
                        groupings.setRotate(-90);

                        Callback<ListView<Grouping>, ListCell<Grouping>> cellFactory = new Callback<>() {
                            @Override
                            public ListCell<Grouping> call(ListView<Grouping> l) {
                                return new ListCell<>() {

                                    @Override
                                    protected void updateItem(Grouping group, boolean empty) {
                                        super.updateItem(group, empty);

                                        if (empty || group == null) {
                                            setText(null);
                                        } else {
                                            setText(group.getName());
                                            // this is a stupid janky hack because javafx styling is stupid and hard to work with when you want it to be dynamic
                                            // this sets the text color of the grouping item so that the font color can be updated
                                            if(group.equals(groupings.getValue())) {
                                                setTextFill(group.getFontColor());
                                            } else {
                                                setTextFill(Grouping.defaultFontColor);
                                            }
                                        }
                                    }
                                };
                            }
                        };
                        groupings.setCellFactory(cellFactory);
                        groupings.setButtonCell(cellFactory.call(null));

                        groupings.getItems().addAll(matrix.getGroupings());
                        groupings.getItems().add(matrix.getDefaultGrouping());
                        groupings.getSelectionModel().select(((DSMItem) item.getValue()).getGroup1());
                        groupings.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                            matrix.setItemGroup((DSMItem) item.getValue(), groupings.getValue());
                            matrix.setCurrentStateAsCheckpoint();
                            for (Cell c_ : cells) {
                                refreshCellHighlight(c_);
                            }
                        });

                        Group g = new Group();  // box will be added to a group so that it will be formatted correctly if it is vertical
                        g.getChildren().add(groupings);
                        cell.getChildren().add(g);
                    }
                    case "index_item" -> {
                        NumericTextField entry = new NumericTextField(((DSMItem) item.getValue()).getSortIndex());
                        entry.setPrefColumnCount(3);  // set size to 3 characters fitting
                        entry.setPadding(new Insets(0));
                        cell.setMaxWidth(Region.USE_COMPUTED_SIZE);
                        cell.setAlignment(Pos.CENTER);
                        int finalR = r;
                        int finalC = c;
                        entry.setOnAction(e -> {
                            cell.getParent().requestFocus();
                        });
                        entry.focusedProperty().addListener((obs, oldVal, newVal) -> {
                            if (!newVal) {  // if changing to not focused
                                if (entry.getNumericValue() != null) {
                                    Double newSortIndex = entry.getNumericValue();
                                    matrix.setItemSortIndex((DSMItem) item.getValue(), newSortIndex);
                                    matrix.setCurrentStateAsCheckpoint();
                                    clearCellHighlight(new Pair<>(finalR, finalC), "errorHighlight");
                                } else {
                                    setCellHighlight(new Pair<>(finalR, finalC), ERROR_BACKGROUND, "errorHighlight");
                                }
                            }
                        });
                        cell.getChildren().add(entry);
                    }
                    case "editable_connection" -> {
                        int rowUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getKey().getUid();
                        int colUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getValue().getUid();
                        label = getEditableConnectionCell(cell, locationLabel, rowUid, colUid, r, c);
                    }
                }
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                cell.setPadding(new Insets(0));
                //cell.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), "};"));
                rowData.add(cell);

                Cell cellObject = new Cell(new Pair<>(r, c), cell, label, fontSize);
                cellObject.updateHighlightBG(defaultBackground, "default");
                cells.add(cellObject);

            }
            gridData.add(rowData);
        }
        for(Cell cell : cells) {
            refreshCellHighlight(cell);
        }

        grid.setGridDataHBox(gridData);
        grid.setFreezeLeft(3);
        grid.setFreezeHeader(2);  // freeze top two rows for symmetric matrix

        rootLayout.getChildren().addAll(grid.getGrid(), locationLabel);
    }

}
