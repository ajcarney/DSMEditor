package gui;

import DSMData.DSMConnection;
import DSMData.DSMData;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;


/**
 * Creates a new class that manages the gui for a matrix.
 *
 * @author Aiden Carney
 */
public class MatrixGuiHandler {

    /**
     * A data class for a cell displayed in the gui. Contains the HBox object and location in the grid
     */
    private class Cell {
        private Pair<Integer, Integer> gridLocation;
        private HBox guiCell;

        private static Boolean crossHighlightEnabled = false;

        private HashMap<String, Background> highlightBGs = new HashMap<String, Background>() {{
            put("default", null);
            put("user", null);
            put("cross", null);
            put("error", null);
            put("symmetryError", null);
            put("search", null);
        }};

        /**
         * Creates a new cell object
         *
         * @param gridLocation a pair of row, column of the location of the cell in the grid layout
         * @param guiCell      the HBox object of the cell
         */
        public Cell(Pair<Integer, Integer> gridLocation, HBox guiCell) {
            this.gridLocation = gridLocation;
            this.guiCell = guiCell;
        }

        /**
         * Sets the background color of a cell in the grid
         *
         * @param color the color to set the background to
         */
        private void setCellHighlight(Color color) {
            guiCell.setBackground(new Background(new BackgroundFill(color, new CornerRadii(3), new Insets(0))));
        }

        /**
         * Getter function for the crossHighlightEnabled field
         *
         * @return if cross highlighting is enabled
         */
        public static Boolean getCrossHighlightEnabled() {
            return crossHighlightEnabled;
        }

        /**
         * Getter function for the gridLocation field of the class
         *
         * @return gridLocation of the instance
         */
        public Pair<Integer, Integer> getGridLocation() {
            return gridLocation;
        }

        /**
         * Getter function for the guiCell field of the class
         *
         * @return the guiCell of the instance
         */
        public HBox getGuiCell() {
            return guiCell;
        }

        /**
         * Getter function for background of type
         *
         * @param type which background to get the version of
         * @return     the background
         */
        public Background getHighlightBG(String type) {
            if(type.equals("default")) {
                return highlightBGs.get("default");
            } else if(type.equals("user")) {
                return highlightBGs.get("user");
            } else if(type.equals("cross")) {
                return highlightBGs.get("cross");
            } else if(type.equals("error")) {
                return highlightBGs.get("error");
            } else if(type.equals("symmetryError")) {
                return highlightBGs.get("symmetryError");
            } else if(type.equals("search")) {
                return highlightBGs.get("search");
            }

            return null;
        }

        /**
         * Setter function for the crossHighlightEnabled field
         *
         * @param crossHighlightEnabled the new value for the crossHighlightField
         */
        public static void setCrossHighlightEnabled(Boolean crossHighlightEnabled) {
            Cell.crossHighlightEnabled = crossHighlightEnabled;
        }

        /**
         * Setter function for backgrounds
         *
         * @param bg the new background of the cell
         */
        public void setHighlightBG(Background bg, String type) {
            if(type.equals("default")) {
                highlightBGs.put("default", bg);
            } else if(type.equals("user")) {
                highlightBGs.put("user", bg);
            } else if(type.equals("cross")) {
                highlightBGs.put("cross", bg);
            } else if(type.equals("error")) {
                highlightBGs.put("error", bg);
            } else if(type.equals("symmetryError")) {
                highlightBGs.put("symmetryError", bg);
            } else if(type.equals("search")) {
                highlightBGs.put("search", bg);
            }
            updateCellHighlight();
        }


        /**
         * updates the background color of a cell based on the backgrounds set for it. Error highlight
         * is given the highest priority, then cross highlighting, then user highlighting, and lastly the grouping
         * color of the cell or the default color
         */
        public void updateCellHighlight() {
            if (getHighlightBG("error") != null) {
                guiCell.setBackground(getHighlightBG("error"));
            } else if(getHighlightBG("symmetryError") != null) {
                guiCell.setBackground(getHighlightBG("symmetryError"));
            } else if(getHighlightBG("search") != null) {
                guiCell.setBackground(getHighlightBG("search"));
            }else if (getHighlightBG("cross") != null && crossHighlightEnabled) {
                guiCell.setBackground(getHighlightBG("cross"));
            } else if (getHighlightBG("user") != null) {
                guiCell.setBackground(getHighlightBG("user"));
            } else {  // default background determined by groupings
                Integer rowUid = getUidsFromGridLoc(gridLocation).getKey();
                Integer colUid = getUidsFromGridLoc(gridLocation).getValue();
                Color mergedColor = null;
                if (rowUid == null && colUid != null) {  // highlight with column color
                    mergedColor = matrix.getGroupingColors().get(matrix.getItem(colUid).getGroup());
                    setCellHighlight(mergedColor);
                    return;
                } else if (rowUid != null && colUid == null) {  // highlight with row color
                    mergedColor = matrix.getGroupingColors().get(matrix.getItem(rowUid).getGroup());
                    setCellHighlight(mergedColor);
                    return;
                } else if (rowUid != null && colUid != null) {  // highlight with merged color
                    Color rowColor = matrix.getGroupingColors().get(matrix.getItem(rowUid).getGroup());
                    if (rowColor == null) rowColor = Color.color(1.0, 1.0, 1.0);

                    Color colColor = matrix.getGroupingColors().get(matrix.getItem(colUid).getGroup());
                    if (colColor == null) colColor = Color.color(1.0, 1.0, 1.0);

                    double r = (rowColor.getRed() + colColor.getRed()) / 2;
                    double g = (rowColor.getGreen() + colColor.getGreen()) / 2;
                    double b = (rowColor.getBlue() + colColor.getBlue()) / 2;
                    mergedColor = Color.color(r, g, b);

                    if (matrix.isSymmetrical() && !rowUid.equals(matrix.getItem(colUid).getAliasUid()) && matrix.getItem(rowUid).getGroup().equals(matrix.getItem(colUid).getGroup())) {  // associated row and column are same group
                        setCellHighlight(mergedColor);
                        return;
                    } else if (!matrix.isSymmetrical()) {
                        setCellHighlight(mergedColor);
                        return;
                    }
                }

                setCellHighlight((Color)getHighlightBG("default").getFills().get(0).getFill());
            }
        }

    }

    DSMData matrix;
    public static final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.color(1, 1, 1), new CornerRadii(3), new Insets(0)));
    public static final Background UNEDITABLE_CONNECTION_BACKGROUND = new Background(new BackgroundFill(Color.color(0, 0, 0), new CornerRadii(3), new Insets(0)));
    public static final Background HIGHLIGHT_BACKGROUND = new Background(new BackgroundFill(Color.color(.9, 1, 0), new CornerRadii(3), new Insets(0)));
    public static final Background CROSS_HIGHLIGHT_BACKGROUND = new Background(new BackgroundFill(Color.color(.2, 1, 0), new CornerRadii(3), new Insets(0)));
    public static final Background ERROR_BACKGROUND = new Background(new BackgroundFill(Color.color(1, 0, 0), new CornerRadii(3), new Insets(0)));
    public static final Background SYMMETRY_ERROR_BACKGROUND = new Background(new BackgroundFill(Color.color(1, .5, .2), new CornerRadii(3), new Insets(0)));
    public static final Background SEARCH_BACKGROUND = new Background(new BackgroundFill(Color.color(0, 1, 1), new CornerRadii(3), new Insets(0)));


    private DoubleProperty fontSize;
    private BooleanProperty showNames;

    private VBox rootLayout = new VBox();

    Vector<Cell> cells;  // contains information for highlighting
    HashMap<String, HashMap<Integer, Integer>> gridUidLookup;


    /**
     * Returns a MatrixGuiHandler object for a given matrix
     *
     * @param matrix   the DSMData object to display
     * @param fontSize the default font size to display the matrix with
     */
    public MatrixGuiHandler(DSMData matrix, double fontSize) {
        this.matrix = matrix;
        cells = new Vector<>();
        gridUidLookup = new HashMap<>();
        gridUidLookup.put("rows", new HashMap<Integer, Integer>());
        gridUidLookup.put("cols", new HashMap<Integer, Integer>());

        this.fontSize = new SimpleDoubleProperty(fontSize);
        showNames = new SimpleBooleanProperty(true);  // default to showing names instead of weights
    }


    /**
     * Finds a cell by a location by iterating over all cells and determining if the grid location is the
     * specified grid location
     *
     * @param cellLoc the grid location to get the cell of (row, column)
     * @return        the cell object with the specified grid location
     */
    private Cell getCellByLoc(Pair<Integer, Integer> cellLoc) {
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
    private Pair<Integer, Integer> getUidsFromGridLoc(Pair<Integer, Integer> cellLoc) {
        try {
            Integer rowUid = gridUidLookup.get("rows").get(cellLoc.getKey());
            Integer colUid = gridUidLookup.get("cols").get(cellLoc.getValue());
            return new Pair<>(rowUid, colUid);
        } catch(NullPointerException e) {
            e.printStackTrace();
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
            if(getUidsFromGridLoc(cell.getGridLocation()).equals(uids)) {
                rowLoc = cell.getGridLocation().getKey();
                colLoc = cell.getGridLocation().getValue();
            }
        }

        return new Pair<>(rowLoc, colLoc);
    }


    /**
     * Toggles user highlight for a cell
     *
     * @param cellLoc the grid location of the cell (row, column)
     * @param bg      the background to set the highlight to
     */
    private void toggleUserHighlightCell(Pair<Integer, Integer> cellLoc, Background bg) {
        Cell cell = getCellByLoc(cellLoc);
        if(cell == null) return;

        if(cell.getHighlightBG("user") == null) {  // is not highlighted, so highlight it
            cell.setHighlightBG(bg, "user");
        } else {
            cell.setHighlightBG(null, "user");
        }
    }


    /**
     * Function to set several different types of highlight for a cell given its grid location
     *
     * @param cellLoc       the grid location of a cell (row, column)
     * @param bg            the color to assign to a cell
     * @param highlightType the highlight type to assign to
     */
    public void setCellHighlight(Pair<Integer, Integer> cellLoc, Background bg, String highlightType) {
        Cell cell = getCellByLoc(cellLoc);
        if(cell == null) return;

        cell.setHighlightBG(bg, highlightType);
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

        cell.setHighlightBG(null, highlightType);
    }


    /**
     * Function to remove several different highlight types of all cells by assigning
     * null to that highlight field
     *
     * @param highlightType the highlight type to assign to (userHighlight, errorHighlight, symmetryErrorHighlight)
     */
    public void clearAllCellsHighlight(String highlightType) {
        for(Cell cell : (Vector<Cell>)cells.clone()) {  // use a clone so we don't run into concurrent modification exceptions
            cell.setHighlightBG(null, highlightType);
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
    private void crossHighlightCell(Pair<Integer, Integer> endLocation, boolean shouldHighlight) {
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
                        cell.setHighlightBG(CROSS_HIGHLIGHT_BACKGROUND, "cross");
                    } else {
                        cell.setHighlightBG(null, "cross");
                    }
                    break;
                }
            }
        }

        for(int i=endCol - 1; i>=minCol; i--) {  // highlight horizontally, start at one less because first cell it will find is already highlighted
            for(Cell cell : cells) {  // find the cell to modify
                if(cell.getGridLocation().getValue() == i && cell.getGridLocation().getKey() == endRow) {
                    if(shouldHighlight) {
                        cell.setHighlightBG(CROSS_HIGHLIGHT_BACKGROUND, "cross");
                    } else {
                        cell.setHighlightBG(null, "cross");
                    }
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
        Cell.setCrossHighlightEnabled(!Cell.getCrossHighlightEnabled());
        for(Cell cell : cells) {
            cell.updateCellHighlight();
        }
    }


    /**
     * Creates the gui that displays a matrix. Uses the DSMData's getGridArray() method to create the grid.
     * Puts grid in a scroll pane and adds a location label (displays connection row, column) at the bottom of the VBox.
     * Returns the VBox so that it can be added to a layout
     *
     * @return VBox that contains all the gui widgets
     */
    public VBox getMatrixEditor() {
        cells = new Vector<>();
        gridUidLookup = new HashMap<>();
        gridUidLookup.put("rows", new HashMap<Integer, Integer>());
        gridUidLookup.put("cols", new HashMap<Integer, Integer>());

        rootLayout = new VBox();
        rootLayout.setAlignment(Pos.CENTER);
        rootLayout.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ", fontSize.asString(), "};",
                ".combo-box > .list-cell {-fx-padding: 0 0 0 0; -fx-border-insets: 0 0 0 0;}"
        ));

        Label location = new Label("");
        GridPane grid = new GridPane();

        grid.setAlignment(Pos.CENTER);
        ArrayList<ArrayList<Pair<String, Object>>> template = matrix.getGridArray();
        int rows = template.size();
        int columns = template.get(0).size();

        for(int r=0; r<rows; r++) {
            for(int c=0; c<columns; c++) {
                Pair<String, Object> item = template.get(r).get(c);
                HBox cell = new HBox();  // wrap everything in an HBox so a border can be added easily

                Background defaultBackground = DEFAULT_BACKGROUND;

                if(item.getKey().equals("plain_text")) {
                    Label label = new Label((String)item.getValue());
                    label.setMinWidth(Region.USE_PREF_SIZE);
                    cell.getChildren().add((Node) label);
                } else if(item.getKey().equals("plain_text_v")) {
                    Label label = new Label((String)item.getValue());
                    label.setRotate(-90);
                    cell.setAlignment(Pos.BOTTOM_RIGHT);
                    Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical
                    g.getChildren().add(label);
                    cell.getChildren().add(g);
                } else if(item.getKey().equals("item_name")) {
                    Label label = new Label(matrix.getItem((Integer) item.getValue()).getName());
                    cell.setAlignment(Pos.BOTTOM_RIGHT);
                    label.setMinWidth(Region.USE_PREF_SIZE);
                    cell.getChildren().add(label);
                } else if(item.getKey().equals("item_name_v")) {
                    Label label = new Label(matrix.getItem((Integer)item.getValue()).getName());
                    label.setRotate(-90);
                    cell.setAlignment(Pos.BOTTOM_RIGHT);
                    Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical
                    g.getChildren().add(label);
                    cell.getChildren().add(g);
                } else if(item.getKey().equals("grouping_item")) {
                    ComboBox<String> groupings = new ComboBox<String>();
                    groupings.setMinWidth(Region.USE_PREF_SIZE);

                    groupings.setStyle("""
                            -fx-border-insets: -2, -2, -2, -2;
                            -fx-padding: -5, -5, -5, -5;"""
                    );

                    groupings.getItems().addAll(matrix.getGroupings());
                    groupings.getSelectionModel().select(matrix.getItem((Integer)item.getValue()).getGroup());
                    groupings.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal)->{
                        if(matrix.isSymmetrical()) {
                            matrix.setGroupSymmetric((Integer)item.getValue(), groupings.getValue());
                        } else {
                            matrix.setGroup((Integer)item.getValue(), groupings.getValue());
                        }

                        for(Cell c_ : cells) {
                            c_.updateCellHighlight();
                        }
                    });
                    cell.getChildren().add(groupings);
                } else if(item.getKey().equals("grouping_item_v")) {
                    ComboBox<String> groupings = new ComboBox<String>();
                    groupings.getItems().addAll(matrix.getGroupings());
                    groupings.setStyle(  // remove border from button when selecting it because this causes weird resizing bugs in the grouping
                            """
                            -fx-focus-color: transparent;
                            -fx-background-insets: 0, 0, 0;
                            -fx-background-radius: 0, 0, 0;"""
                    );
                    groupings.setRotate(-90);
                    groupings.getSelectionModel().select(matrix.getItem((Integer)item.getValue()).getGroup());
                    groupings.setOnAction(e -> {
                        matrix.setGroup((Integer)item.getValue(), groupings.getValue());
                        for(Cell c_ : cells) {
                            c_.updateCellHighlight();
                        }
                    });
                    Group g = new Group();  // box will be added to a group so that it will be formatted correctly if it is vertical
                    g.getChildren().add(groupings);
                    cell.getChildren().add(g);
                } else if(item.getKey().equals("index_item")) {
                    NumericTextField entry = new NumericTextField(matrix.getItem((Integer)item.getValue()).getSortIndex());
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
                        if(!newVal) {  // if changing to not focused
                            if(entry.getNumericValue() != null) {
                                Double newSortIndex = entry.getNumericValue();
                                if(matrix.isSymmetrical()) {
                                    matrix.setSortIndexSymmetric((Integer)item.getValue(), newSortIndex);
                                } else {
                                    matrix.setSortIndex((Integer)item.getValue(), newSortIndex);
                                }
                                clearCellHighlight(new Pair<Integer, Integer>(finalR, finalC), "errorHighlight");
                            } else {
                                setCellHighlight(new Pair<Integer, Integer>(finalR, finalC), ERROR_BACKGROUND, "errorHighlight");
                            }
                        }
                    });

                    cell.getChildren().add(entry);
                } else if(item.getKey().equals("uneditable_connection")) {
                    HBox label = new HBox();  // use an HBox object because then background color is not tied to the text
                    defaultBackground = UNEDITABLE_CONNECTION_BACKGROUND;
                } else if(item.getKey().equals("editable_connection")) {
                    int rowUid = ((Pair<Integer, Integer>)item.getValue()).getKey();
                    int colUid = ((Pair<Integer, Integer>)item.getValue()).getValue();
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
                    if(!gridUidLookup.get("rows").containsKey(r)) {
                        gridUidLookup.get("rows").put(r, rowUid);
                    }

                    if(!gridUidLookup.get("cols").containsKey(c)) {
                        gridUidLookup.get("cols").put(c, colUid);
                    }

                    // set up callback functions
                    int finalR = r;
                    int finalC = c;

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
                                    matrix.clearConnection(rowUid, colUid);
                                }
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
                        location.setText(matrix.getItem(rowUid).getName() + ":" + matrix.getItem(colUid).getName());
                    });
                    cell.setOnMouseExited(e -> {
                        crossHighlightCell(new Pair<Integer, Integer>(finalR, finalC), false);
                        location.setText("");
                    });

                    cell.getChildren().add(label);
                }
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                cell.setPadding(new Insets(0));
                GridPane.setConstraints(cell, c, r);
                grid.getChildren().add(cell);

                Cell cellData = new Cell(new Pair<>(r, c), cell);
                cellData.setHighlightBG(defaultBackground, "default");
                cells.add(cellData);

            }
        }
        for(Cell cell : cells) {
            cell.updateCellHighlight();
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        rootLayout.getChildren().addAll(scrollPane, location);


        return rootLayout;
    }


    /**
     * Sets the font size to a new value. This value is bound to in the gui so it will be auto updated
     *
     * @param newSize the new font size to use in the gui
     */
    public void setFontSize(Double newSize) {
        fontSize.setValue(newSize);
    }


    /**
     * Sets the value of showNames. This value is bound to in the gui so that either the connection names or weights will be shown
     *
     * @param newValue show the names of the connections or the weights if false
     */
    public void setShowNames(Boolean newValue) {
        showNames.set(newValue);
    }
}
