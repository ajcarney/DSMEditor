package View.MatrixHandlers;

import Data.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;


/**
 * Creates a static view of an asymmetric matrix
 */
public class StaticAsymmetricHandler extends TemplateMatrixHandler<AsymmetricDSM> {

    /**
     * Returns a MatrixGuiHandler object for a given matrix
     *
     * @param matrix   the SymmetricDSM object to display
     * @param fontSize the default font size to display the matrix with
     */
    public StaticAsymmetricHandler(AsymmetricDSM matrix, double fontSize) {
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
                cell.setCellHighlight(matrix.getItem(rowUid).getGroup1().getColor());  // row and column color will be the same because row and column
                // have same group in symmetric matrix
                cell.setCellTextColor(matrix.getItem(rowUid).getGroup1().getFontColor());
                cell.setCellBorder(new Border(new BorderStroke(matrix.getItem(colUid).getGroup1().getColor(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
                return;
            }

            cell.setCellHighlight(cell.getHighlightBG("default"));
        }
    }


    /**
     * sets the current rootLayout to an immutable view
     */
    @Override
    public void refreshMatrixEditor() {
        cells = new Vector<>();
        gridUidLookup = new HashMap<>();
        gridUidLookup.put("rows", new HashMap<>());
        gridUidLookup.put("cols", new HashMap<>());

        rootLayout.getChildren().removeAll(rootLayout.getChildren());
        rootLayout.setAlignment(Pos.CENTER);
        rootLayout.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ", fontSize.asString(), "};",
                ".combo-box > .list-cell {-fx-padding: 0 0 0 0; -fx-border-insets: 0 0 0 0;}"
        ));

        GridPane grid = new GridPane();

        grid.setAlignment(Pos.CENTER);
        ArrayList<ArrayList<Pair<String, Object>>> template = matrix.getGridArray();
        int rows = template.size();
        int columns = template.get(0).size();

        for(int r=0; r<rows; r++) {
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
                        label = new Label(((DSMItem) item.getValue()).getName().getValue());
                        label.setPadding(new Insets(0, 5, 0, 5));
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case "item_name_v" -> {
                        label = new Label(((DSMItem) item.getValue()).getName().getValue());
                        label.setPadding(new Insets(0, 5, 0, 5));
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case "grouping_item" -> {
                        label = new Label(((DSMItem) item.getValue()).getGroup1().getName());
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case "grouping_item_v" -> {
                        label = new Label(((DSMItem) item.getValue()).getGroup1().getName());
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical
                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case "index_item" -> {
                        label = new Label(String.valueOf(((DSMItem) item.getValue()).getSortIndex()));
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case "uneditable_connection" -> defaultBackground = UNEDITABLE_CONNECTION_BACKGROUND;
                    case "editable_connection" -> {
                        int rowUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getKey().getUid();
                        int colUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getValue().getUid();
                        DSMConnection conn = matrix.getConnection(rowUid, colUid);
                        label = new Label();
                        if (showNames.getValue() && conn != null) {
                            label.setText(conn.getConnectionName());
                        } else if (!showNames.getValue() && conn != null) {
                            label.setText(String.valueOf(conn.getWeight()));
                        } else {
                            label.setText("");
                        }

                        cell.setAlignment(Pos.CENTER);  // center the text

                        cell.setMinWidth(Region.USE_PREF_SIZE);

                        // this item type will be used to create the lookup table for finding associated uid from grid location
                        if (!gridUidLookup.get("rows").containsKey(r)) {
                            gridUidLookup.get("rows").put(r, rowUid);
                        }

                        if (!gridUidLookup.get("cols").containsKey(c)) {
                            gridUidLookup.get("cols").put(c, colUid);
                        }


                        cell.getChildren().add(label);
                    }
                }
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                cell.setPadding(new Insets(0));
                GridPane.setConstraints(cell, c, r);
                grid.getChildren().add(cell);

                Cell cellData = new Cell(new Pair<>(r, c), cell, label, fontSize);
                cellData.updateHighlightBG(defaultBackground, "default");
                cells.add(cellData);
            }
        }

        for(Cell cell : cells) {
            refreshCellHighlight(cell);
        }

        rootLayout.getChildren().addAll(grid);
    }

}
