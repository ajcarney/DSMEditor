package View.MatrixHandlers;

import Data.DSMItem;
import Data.Grouping;
import Data.SymmetricDSM;
import View.Widgets.FreezeGrid;
import View.Widgets.NumericTextField;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class SymmetricMatrixHandler extends TemplateMatrixHandler<SymmetricDSM> {

    /**
     * Returns a MatrixGuiHandler object for a given matrix
     *
     * @param matrix   the SymmetricDSM object to display
     * @param fontSize the default font size to display the matrix with
     */
    public SymmetricMatrixHandler(SymmetricDSM matrix, double fontSize) {
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

        } else if(cell.getHighlightBG("symmetryError") != null) {
            cell.setCellHighlight(cell.getHighlightBG("symmetryError"));

        } else if(cell.getHighlightBG("search") != null) {
            cell.setCellHighlight(cell.getHighlightBG("search"));

        } else if (cell.getHighlightBG("cross") != null && cell.getCrossHighlightEnabled()) {
            cell.setCellHighlight(cell.getHighlightBG("cross"));

        } else if (cell.getHighlightBG("user") != null) {
            cell.setCellHighlight(cell.getHighlightBG("user"));

        } else {  // default background determined by groupings
            Integer rowUid = getUidsFromGridLoc(cell.getGridLocation()).getKey();
            Integer colUid = getUidsFromGridLoc(cell.getGridLocation()).getValue();
            Color mergedColor;
            if (rowUid == null && colUid != null) {  // highlight with column color
                mergedColor = matrix.getItem(colUid).getGroup1().getColor();
                cell.setCellHighlight(mergedColor);
                return;
            } else if (rowUid != null && colUid == null) {  // highlight with row color
                mergedColor = matrix.getItem(rowUid).getGroup1().getColor();
                cell.setCellHighlight(mergedColor);
                return;
            } else if (
                    rowUid != null && colUid != null
                    && !rowUid.equals(matrix.getItem(colUid).getAliasUid())
                    && matrix.getItem(rowUid).getGroup1().equals(matrix.getItem(colUid).getGroup1())
            ) {  // highlight with merged color
                cell.setCellHighlight(matrix.getItem(rowUid).getGroup1().getColor());  // row and column color will be the same because row and column
                                                                                       // have same group in symmetric matrix
                return;
            }

            cell.setCellHighlight(cell.getHighlightBG("default"));
        }
    }


    /**
     * Creates the gui that displays a matrix. Uses the SymmetricDSM's getGridArray() method to create the grid.
     * Puts grid in a scroll pane and adds a location label (displays connection row, column) at the bottom of the VBox.
     * Returns the VBox so that it can be added to a layout
     */
    @Override
    public void refreshMatrixEditor() {
        cells = new Vector<>();
        gridUidLookup = new HashMap<>();
        gridUidLookup.put("rows", new HashMap<Integer, Integer>());
        gridUidLookup.put("cols", new HashMap<Integer, Integer>());

        rootLayout.getChildren().removeAll(rootLayout.getChildren());
        rootLayout.setAlignment(Pos.CENTER);

        Label locationLabel = new Label("");
        grid = new FreezeGrid();

        ArrayList<ArrayList<Pair<String, Object>>> template = matrix.getGridArray();
        ArrayList<ArrayList<HBox>> gridData = new ArrayList<>();

        int rows = template.size();
        int columns = template.get(0).size();

        for(int r=0; r<rows; r++) {
            ArrayList<HBox> rowData = new ArrayList<>();
            for(int c=0; c<columns; c++) {
                Pair<String, Object> item = template.get(r).get(c);
                HBox cell = new HBox();  // wrap everything in an HBox so a border can be added easily

                Background defaultBackground = DEFAULT_BACKGROUND;

                switch (item.getKey()) {
                    case "plain_text" -> {
                        Label label = new Label((String) item.getValue());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add((Node) label);

                        break;
                    }
                    case "plain_text_v" -> {
                        Label label = new Label((String) item.getValue());
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);

                        break;
                    }
                    case "item_name" -> {
                        Label label = new Label(((DSMItem) item.getValue()).getName());
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);

                        break;
                    }
                    case "item_name_v" -> {
                        Label label = new Label(((DSMItem) item.getValue()).getName());
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);

                        break;
                    }
                    case "grouping_item" -> {  // dropdown box for choosing group
                        ComboBox<Grouping> groupings = new ComboBox<>();
                        groupings.setMinWidth(Region.USE_PREF_SIZE);
                        groupings.setPadding(new Insets(0));
                        groupings.setPadding(new Insets(-5));
                        groupings.setStyle(
                                "-fx-background-color: transparent;" +
                                        "-fx-padding: 0, 0, 0, 0;" +
                                        "-fx-font-size: " + (fontSize.doubleValue() - 2) + " };"
                        );

                        groupings.setCellFactory(param -> new ListCell<Grouping>() {
                            @Override
                            protected void updateItem(Grouping group, boolean empty) {
                                super.updateItem(group, empty);

                                if (empty || group == null) {
                                    setText(null);
                                } else {
                                    setText(group.getName());
                                }
                            }
                        });

                        Pane ghostPane = new Pane();
                        Scene ghostScene = new Scene(ghostPane);  // a scene is needed to calculate preferred sizes of nodes


                        ghostPane.getChildren().add(groupings);
                        ghostPane.applyCss();
                        ghostPane.layout();
                        double maxHeight = groupings.getBoundsInLocal().getHeight() + groupings.getPadding().getTop() + groupings.getPadding().getBottom();

                        groupings.getItems().addAll(matrix.getGroupings());
                        groupings.getSelectionModel().select(((DSMItem) item.getValue()).getGroup1());
                        groupings.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                            matrix.setItemGroup((DSMItem) item.getValue(), groupings.getValue());
                            matrix.setCurrentStateAsCheckpoint();

                            for (Cell c_ : cells) {
                                refreshCellHighlight(c_);
                            }
                        });

                        cell.getChildren().add(groupings);
                        break;
                    }
                    case "grouping_item_v" -> {
                        ComboBox<Grouping> groupings = new ComboBox<Grouping>();
                        groupings.getItems().addAll(matrix.getGroupings());
                        groupings.setStyle(  // remove border from button when selecting it because this causes weird resizing bugs in the grouping
                                """
                                        -fx-focus-color: transparent;
                                        -fx-background-insets: 0, 0, 0;
                                        -fx-background-radius: 0, 0, 0;"""
                        );
                        groupings.setRotate(-90);
                        groupings.getSelectionModel().select(((DSMItem) item.getValue()).getGroup1());

                        groupings.setCellFactory(param -> new ListCell<>() {
                            @Override
                            protected void updateItem(Grouping group, boolean empty) {
                                super.updateItem(group, empty);

                                if (empty || group == null) {
                                    setText(null);
                                } else {
                                    setText(group.getName());
                                }
                            }
                        });

                        groupings.setOnAction(e -> {
                            matrix.setItemGroup((DSMItem) item.getValue(), groupings.getValue());
                            matrix.setCurrentStateAsCheckpoint();
                            for (Cell c_ : cells) {
                                refreshCellHighlight(c_);
                            }
                        });
                        Group g = new Group();  // box will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(groupings);
                        cell.getChildren().add(g);
                        break;
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
                                    clearCellHighlight(new Pair<Integer, Integer>(finalR, finalC), "errorHighlight");
                                } else {
                                    setCellHighlight(new Pair<Integer, Integer>(finalR, finalC), ERROR_BACKGROUND, "errorHighlight");
                                }
                            }
                        });
                        cell.getChildren().add(entry);
                    }
                    case "uneditable_connection" -> defaultBackground = UNEDITABLE_CONNECTION_BACKGROUND;
                    case "editable_connection" -> {
                        int rowUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getKey().getUid();
                        int colUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getValue().getUid();
                        getEditableConnectionCell(cell, locationLabel, rowUid, colUid, r, c);
                    }
                }
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                cell.setPadding(new Insets(0));
                cell.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), "};"));
                rowData.add(cell);

                Cell cellObject = new Cell(new Pair<>(r, c), cell);
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
