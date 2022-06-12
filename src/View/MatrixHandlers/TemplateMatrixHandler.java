package View.MatrixHandlers;

import Data.DSMConnection;
import Data.TemplateDSM;
import View.Widgets.NumericTextField;
import View.Widgets.FreezeGrid;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Vector;

public abstract class TemplateMatrixHandler<T extends TemplateDSM> {

    public static final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.color(1, 1, 1), new CornerRadii(3), new Insets(0)));
    public static final Background UNEDITABLE_CONNECTION_BACKGROUND = new Background(new BackgroundFill(Color.color(0, 0, 0), new CornerRadii(3), new Insets(0)));
    public static final Background HIGHLIGHT_BACKGROUND = new Background(new BackgroundFill(Color.color(.9, 1, 0), new CornerRadii(3), new Insets(0)));
    public static final Background CROSS_HIGHLIGHT_BACKGROUND = new Background(new BackgroundFill(Color.color(.2, 1, 0), new CornerRadii(3), new Insets(0)));
    public static final Background ERROR_BACKGROUND = new Background(new BackgroundFill(Color.color(1, 0, 0), new CornerRadii(3), new Insets(0)));
    public static final Background SYMMETRY_ERROR_BACKGROUND = new Background(new BackgroundFill(Color.color(1, .5, .2), new CornerRadii(3), new Insets(0)));
    public static final Background SEARCH_BACKGROUND = new Background(new BackgroundFill(Color.color(0, 1, 1), new CornerRadii(3), new Insets(0)));

    protected T matrix;

    protected DoubleProperty fontSize;
    protected BooleanProperty showNames;

    protected VBox rootLayout;
    protected FreezeGrid grid;

    protected Vector<Cell> cells;  // contains information for highlighting
    protected HashMap<String, HashMap<Integer, Integer>> gridUidLookup;


    /**
     * Constructor for MatrixGuiHandler object for a given matrix. Calls a refresh of the
     * matrix editor gui components
     *
     * @param matrix   the TemplateDSM object to display
     * @param fontSize the default font size to display the matrix with
     */
    public TemplateMatrixHandler(T matrix, double fontSize) {
        this.matrix = matrix;
        cells = new Vector<>();
        gridUidLookup = new HashMap<>();
        gridUidLookup.put("rows", new HashMap<Integer, Integer>());
        gridUidLookup.put("cols", new HashMap<Integer, Integer>());

        rootLayout = new VBox();
        grid = new FreezeGrid();

        this.fontSize = new SimpleDoubleProperty(fontSize);
        showNames = new SimpleBooleanProperty(true);  // default to showing names instead of weights
        refreshMatrixEditor();
    }


//region Getters
    /**
     * Finds a cell by a location by iterating over all cells and determining if the grid location is the
     * specified grid location
     *
     * @param cellLoc the grid location to get the cell of (row, column)
     * @return        the cell object with the specified grid location
     */
    protected Cell getCellByLoc(Pair<Integer, Integer> cellLoc) {
        for(Cell cell : (Vector<Cell>)cells.clone()) {  // use a clone so we don't run into concurrent modification exceptions
            if(cell.getGridLocation().getKey() == cellLoc.getKey() && cell.getGridLocation().getValue() == cellLoc.getValue()) {
                return cell;
            }
        }
        return null;
    }


    /**
     * Finds the row and column uid associated with a grid item. Connections will have both a row and column uid.
     * Names and other fields associated with a row or column will only have a row or column uid.
     * If a field does not exist, then null is returned for that uid
     *
     * @param cellLoc the grid location of the cell (row, column)
     * @return        the uids associated with the cell (row uid, column uid)
     */
    protected Pair<Integer, Integer> getUidsFromGridLoc(Pair<Integer, Integer> cellLoc) {
        try {
            Integer rowUid = gridUidLookup.get("rows").get(cellLoc.getKey());
            Integer colUid = gridUidLookup.get("cols").get(cellLoc.getValue());
            return new Pair<>(rowUid, colUid);
        } catch(NullPointerException e) {
            return null;
        }
    }


    /**
     * Finds a cell given its rowUid and colUid by doing a linear search
     *
     * @param uids pair of row uid, column uid of the cell to find
     * @return     pair of row location, column location of the cell
     */
    public Pair<Integer, Integer> getGridLocFromUids(Pair<Integer, Integer> uids) {
        Integer rowLoc = null;
        Integer colLoc = null;

        for(Cell cell : (Vector<Cell>)cells.clone()) {  // use a clone so we don't run into concurrent modification exceptions
            Pair<Integer, Integer> testUids = getUidsFromGridLoc(cell.getGridLocation());
            if(testUids != null && testUids.equals(uids)) {
                rowLoc = cell.getGridLocation().getKey();
                colLoc = cell.getGridLocation().getValue();
            }
        }

        return new Pair<>(rowLoc, colLoc);
    }


    /**
     * Returns the current layout for the matrix editor that is either mutable or immutable depending on the last refresh call
     *
     * @return VBox of the root layout
     */
    public VBox getMatrixEditor() {
        return rootLayout;
    }
//endregion


//region Setters
    /**
     * Sets the font size to a new value. This value is bound to in the gui so it will be auto updated.
     *
     * @param newSize the new font size to use in the gui
     */
    public void setFontSize(Double newSize) {
        fontSize.setValue(newSize);
    }


    /**
     * Sets the value of showNames. This value is bound to in the gui so that either the connection names or weights will be shown.
     *
     * @param newValue show the names of the connections or the weights if false
     */
    public void setShowNames(Boolean newValue) {
        showNames.set(newValue);
    }
//endregion


//region Highlight Functions
    /**
     * Toggles user highlight for a cell
     *
     * @param cellLoc the grid location of the cell (row, column)
     * @param bg      the background to set the highlight to
     */
    protected void toggleUserHighlightCell(Pair<Integer, Integer> cellLoc, Background bg) {
        Cell cell = getCellByLoc(cellLoc);
        if(cell == null) return;

        if(cell.getHighlightBG("user") == null) {  // is not highlighted, so highlight it
            cell.updateHighlightBG(bg, "user");
        } else {
            cell.updateHighlightBG(null, "user");
        }
        refreshCellHighlight(cell);
    }


    /**
     * Function to set several different types of highlight for a cell given its grid location
     *
     * @param cellLoc       the grid location of a cell (row, column)
     * @param bg            the color to assign to a cell
     * @param highlightType the highlight type to assign to (see Cell class for types)
     */
    public void setCellHighlight(Pair<Integer, Integer> cellLoc, Background bg, String highlightType) {
        Cell cell = getCellByLoc(cellLoc);
        if(cell == null) return;

        cell.updateHighlightBG(bg, highlightType);
        refreshCellHighlight(cell);
    }


    /**
     * Function to remove several different highlight types of a cell given its grid location by assigning
     * null to that highlight field
     *
     * @param cellLoc       the grid location of a cell (row, column)
     * @param highlightType the highlight type to assign to (userHighlight, errorHighlight, symmetryErrorHighlight)
     */
    public void clearCellHighlight(Pair<Integer, Integer> cellLoc, String highlightType) {
        Cell cell = getCellByLoc(cellLoc);
        if(cell == null) return;

        cell.updateHighlightBG(null, highlightType);
        refreshCellHighlight(cell);
    }


    /**
     * Function to remove several different highlight types of all cells by assigning
     * null to that highlight field
     *
     * @param highlightType the highlight type to assign to (userHighlight, errorHighlight, symmetryErrorHighlight)
     */
    public void clearAllCellsHighlight(String highlightType) {
        for(Cell cell : (Vector<Cell>)cells.clone()) {  // use a clone so we don't run into concurrent modification exceptions
            cell.updateHighlightBG(null, highlightType);
            refreshCellHighlight(cell);
        }
    }


    /**
     * Sets whether or not a cell should be cross highlighted. Cross highlights by taking grid location
     * and keeping row constant and decrementing column location to minimum and then keeping column
     * constant and decrementing the row location to minimum.
     *
     * @param endLocation     the cell passed by location (row, column) to cross highlight to, no cells will be highlighted past this cell
     * @param shouldHighlight whether or not to cross highlight the cell
     */
    protected void crossHighlightCell(Pair<Integer, Integer> endLocation, boolean shouldHighlight) {
        int endRow = endLocation.getKey();
        int endCol = endLocation.getValue();

        int minRow = Integer.MAX_VALUE;  // TODO: shouldn't this just be 0? This would save a linear search
        int minCol = Integer.MAX_VALUE;
        for(Cell cell : cells) {  // determine the value to decrease to
            if(cell.getGridLocation().getKey() < minRow) {
                minRow = cell.getGridLocation().getKey();
            }
            if(cell.getGridLocation().getValue() < minCol) {
                minCol = cell.getGridLocation().getValue();
            }
        }

        for(int i=endRow; i>=minRow; i--) {  // highlight vertically
            for(Cell cell : cells) {  // find the cell to modify
                if(cell.getGridLocation().getKey() == i && cell.getGridLocation().getValue() == endCol) {
                    if(shouldHighlight) {
                        cell.updateHighlightBG(CROSS_HIGHLIGHT_BACKGROUND, "cross");
                    } else {
                        cell.updateHighlightBG(null, "cross");
                    }
                    refreshCellHighlight(cell);
                    break;
                }
            }
        }

        for(int i=endCol - 1; i>=minCol; i--) {  // highlight horizontally, start at one less because first cell it will find is already highlighted
            for(Cell cell : cells) {  // find the cell to modify
                if(cell.getGridLocation().getValue() == i && cell.getGridLocation().getKey() == endRow) {
                    if(shouldHighlight) {
                        cell.updateHighlightBG(CROSS_HIGHLIGHT_BACKGROUND, "cross");
                    } else {
                        cell.updateHighlightBG(null, "cross");
                    }
                    refreshCellHighlight(cell);
                    break;
                }
            }
        }

    }


    /**
     * enables or disables cross-highlighting for a cell and then updates all cells to either remove or
     * add cross highlighting
     */
    public void toggleCrossHighlighting() {
        for(Cell cell : cells) {
            cell.setCrossHighlightEnabled(!cell.getCrossHighlightEnabled());
            refreshCellHighlight(cell);
        }
    }
//endregion


    /**
     * Modifies an hbox in place for a cell that when clicked will handle the editing of a DSM connection
     *
     * @param locationLabel  the label object stating the user's mouse location with units row:column
     * @param rowUid         the uid of the row item
     * @param colUid         the uid of the column item
     * @param gridRowIndex   the row index the cell will be placed in
     * @param gridColIndex   the column index the cell will be placed in
     * @return               the HBox object that contains all the callbacks and data
     */
    public void getEditableConnectionCell(HBox cell, Label locationLabel, int rowUid, int colUid, int gridRowIndex, int gridColIndex) {
        DSMConnection conn = matrix.getConnection(rowUid, colUid);
        final Label label = new Label();
        label.textProperty().bind(Bindings.createStringBinding(() -> {  // bind so that either weights or name can be shown
            if(conn == null) {
                return "";
            } else if(showNames.getValue()) {
                return conn.getConnectionName();
            } else{
                return String.valueOf(conn.getWeight());
            }
        }, showNames));

        cell.setAlignment(Pos.CENTER);  // center the text
        cell.setMinWidth(Region.USE_PREF_SIZE);

        // this item type will be used to create the lookup table for finding associated uid from grid location
        if(!gridUidLookup.get("rows").containsKey(gridRowIndex)) {
            gridUidLookup.get("rows").put(gridRowIndex, rowUid);
        }

        if(!gridUidLookup.get("cols").containsKey(gridColIndex)) {
            gridUidLookup.get("cols").put(gridColIndex, colUid);
        }

        //region cell callbacks
        // set up callback functions
        int finalR = gridRowIndex;
        int finalC = gridColIndex;
        cell.setOnMouseClicked(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                // create popup window to edit the connection
                Stage window = new Stage();

                // Create Root window
                window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
                window.setTitle("Modify Connection");

                VBox layout = new VBox();

                // row 0
                Label titleLabel = new Label("Connection From " + matrix.getItem(rowUid).getName() + " to " + matrix.getItem(colUid).getName());
                GridPane.setConstraints(titleLabel, 0, 0, 3, 1);  // span 3 columns

                // row 1
                HBox row1 = new HBox();
                row1.setPadding(new Insets(10, 10, 10, 10));
                row1.setSpacing(10);
                Label nameLabel = new Label("Connection Type:  ");

                String currentName = null;
                if(matrix.getConnection(rowUid, colUid) != null) {
                    currentName = matrix.getConnection(rowUid, colUid).getConnectionName();
                } else {
                    currentName = "";
                }
                TextField nameField = new TextField(currentName);
                nameField.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(nameField, Priority.ALWAYS);
                row1.getChildren().addAll(nameLabel, nameField);

                // row 2
                HBox row2 = new HBox();
                Label weightLabel = new Label("Connection Weight:");
                row2.setPadding(new Insets(10, 10, 10, 10));
                row2.setSpacing(10);

                Double currentWeight = null;
                if(matrix.getConnection(rowUid, colUid) != null) {
                    currentWeight = matrix.getConnection(rowUid, colUid).getWeight();
                } else {
                    currentWeight = 1.0;
                }
                NumericTextField weightField = new NumericTextField(currentWeight);
                weightField.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(weightField, Priority.ALWAYS);
                row2.getChildren().addAll(weightLabel, weightField);

                // row 3
                // create HBox for user to close with our without changes
                HBox closeArea = new HBox();
                Button applyButton = new Button("Apply Changes");
                applyButton.setOnAction(ee -> {
                    if(!nameField.getText().equals("")) {
                        Double weight = null;
                        try {
                            weight = Double.parseDouble(weightField.getText());
                        } catch(NumberFormatException nfe) {
                            weight = 1.0;
                        }
                        matrix.modifyConnection(rowUid, colUid, nameField.getText(), weight);
                    } else {
                        matrix.deleteConnection(rowUid, colUid);
                    }
                    matrix.setCurrentStateAsCheckpoint();
                    window.close();

                    label.textProperty().unbind();  // reset binding to update text (Bound values cannot be set)
                    label.setText(nameField.getText());
                    label.textProperty().bind(Bindings.createStringBinding(() -> {
                        if(matrix.getConnection(rowUid, colUid) == null) {
                            return "";
                        } else if(showNames.getValue()) {
                            return matrix.getConnection(rowUid, colUid).getConnectionName();
                        } else{
                            return String.valueOf(matrix.getConnection(rowUid, colUid).getWeight());
                        }
                    }, showNames));
                });

                Pane spacer = new Pane();  // used as a spacer between buttons
                HBox.setHgrow(spacer, Priority.ALWAYS);
                spacer.setMaxWidth(Double.MAX_VALUE);

                Button cancelButton = new Button("Cancel");
                cancelButton.setOnAction(ee -> {
                    window.close();
                });
                closeArea.getChildren().addAll(cancelButton, spacer, applyButton);

                //Display window and wait for it to be closed before returning
                layout.getChildren().addAll(titleLabel, row1, row2, closeArea);
                layout.setAlignment(Pos.CENTER);
                layout.setPadding(new Insets(10, 10, 10, 10));
                layout.setSpacing(10);

                Scene scene = new Scene(layout, 400, 200);
                window.setScene(scene);
                window.showAndWait();

            } else if(e.getButton().equals(MouseButton.SECONDARY)) {  // toggle highlighting
                toggleUserHighlightCell(new Pair<Integer, Integer>(finalR, finalC), HIGHLIGHT_BACKGROUND);
            }
        });

        cell.setOnMouseEntered(e -> {
            crossHighlightCell(new Pair<Integer, Integer>(finalR, finalC), true);
            locationLabel.setText(matrix.getItem(rowUid).getName() + ":" + matrix.getItem(colUid).getName());
        });

        cell.setOnMouseExited(e -> {
            crossHighlightCell(new Pair<Integer, Integer>(finalR, finalC), false);
            locationLabel.setText("");
        });
        //endregion

        cell.getChildren().add(label);
    }


    /**
     * updates the background color of a cell based on the backgrounds set for it. Error highlight
     * is given the highest priority, then cross highlighting, then user highlighting, and lastly the grouping
     * color of the cell or the default color
     */
    public abstract void refreshCellHighlight(Cell cell);


    /**
     * Creates the gui that displays a matrix.
     */
    public abstract void refreshMatrixEditor();

}
