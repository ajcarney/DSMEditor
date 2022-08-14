package Matrices.Views;

import Matrices.Data.AbstractDSMData;
import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMInterfaceType;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Views.Entities.Cell;
import UI.ConfigureConnectionInterfaces;
import UI.Widgets.Misc;
import UI.Widgets.NumericTextField;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;


/**
 * Generic class for displaying a matrix
 */
public abstract class AbstractMatrixView {

    public static final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.color(1, 1, 1), new CornerRadii(3), new Insets(0)));
    public static final Background UNEDITABLE_CONNECTION_BACKGROUND = new Background(new BackgroundFill(Color.color(0, 0, 0), new CornerRadii(3), new Insets(0)));
    public static final Background HIGHLIGHT_BACKGROUND = new Background(new BackgroundFill(Color.color(.9, 1, 0), new CornerRadii(3), new Insets(0)));
    public static final Background CROSS_HIGHLIGHT_BACKGROUND = new Background(new BackgroundFill(Color.color(.2, 1, 0), new CornerRadii(3), new Insets(0)));
    public static final Background ERROR_BACKGROUND = new Background(new BackgroundFill(Color.color(1, 0, 0), new CornerRadii(3), new Insets(0)));
    public static final Background SYMMETRY_ERROR_BACKGROUND = new Background(new BackgroundFill(Color.color(1, .5, .2), new CornerRadii(3), new Insets(0)));
    public static final Background SEARCH_BACKGROUND = new Background(new BackgroundFill(Color.color(0, 1, 1), new CornerRadii(3), new Insets(0)));

    protected AbstractDSMData matrix;
    private final ObservableList<DSMInterfaceType> currentInterfaces = FXCollections.observableArrayList();

    protected DoubleProperty fontSize;
    protected ObjectProperty<MatrixViewMode> currentMode;

    protected VBox rootLayout;

    protected Vector<Cell> cells;  // contains information for highlighting
    protected HashMap<String, HashMap<Integer, Integer>> gridUidLookup;

    /**
     * contains the data for how to cross highlight a cell
     *
     * @param hoverGridX             the x grid location of the mouse
     * @param hoverGridY             the y grid location of the mouse
     * @param crossHighlightEnabled  if cross highlighting is enabled
     */
    protected record CrossHighlightData(int hoverGridX, int hoverGridY, boolean crossHighlightEnabled) { }
    protected final ObjectProperty<CrossHighlightData> crossHighlightData = new SimpleObjectProperty<>(new CrossHighlightData(-1, -1, false));

    /**
     * Contains the different types of valid view modes for the matrix. Each of these defines
     * how the matrix is to behave when it is rendered to the user
     */
    public enum MatrixViewMode {
        EDIT_NAMES,
        EDIT_WEIGHTS,
        EDIT_INTERFACES,
        FAST_RENDER,
        STATIC_NAMES,
        STATIC_WEIGHTS,
        STATIC_INTERFACES
    }


    protected static class CellChangedEvent extends Event {
        public CellChangedEvent(EventType<? extends Event> eventType) {
            super(eventType);
        }
    }
    // add the hashcode to it so that the event name string is unique (the name doesn't matter because it isn't used anywhere is just has to be unique)
    protected EventType<CellChangedEvent> CELL_CHANGED_EVENT = new EventType<>("CELL_CHANGED_EVENT" + this.hashCode());


//region Constructors
    /**
     * Constructor for MatrixGuiHandler object for a given matrix. Calls a refresh of the
     * matrix editor gui components
     *
     * @param matrix   the AbstractDSMData object to display
     * @param fontSize the default font size to display the matrix with
     */
    public AbstractMatrixView(AbstractDSMData matrix, double fontSize) {
        this.matrix = matrix;
        cells = new Vector<>();
        gridUidLookup = new HashMap<>();
        gridUidLookup.put("rows", new HashMap<>());
        gridUidLookup.put("cols", new HashMap<>());

        rootLayout = new VBox();

        this.fontSize = new SimpleDoubleProperty(fontSize);
        currentMode = new SimpleObjectProperty<>(MatrixViewMode.EDIT_NAMES);

        // set up the binding for cross highlighting
        crossHighlightData.addListener((o, oldValue, newValue) -> {
            crossHighlightCell(oldValue.hoverGridX, oldValue.hoverGridY, false);  // always unhighlight previous location
            crossHighlightCell(newValue.hoverGridX, newValue.hoverGridY, newValue.crossHighlightEnabled);
        });

    }

//endregion

    /**
     * Method to clone this object type
     *
     * @return  the deep copy of the object
     */
    public abstract AbstractMatrixView createCopy();


    /**
     * Builder pattern method for setting the font size
     *
     * @param fontSize  the new font size for the matrix view
     * @return          this
     */
    public abstract AbstractMatrixView withFontSize(double fontSize);


    /**
     * Builder pattern method for setting the matrix view mode
     *
     * @param mode  the new mode for the matrix view
     * @return      this
     */
    public abstract AbstractMatrixView withMode(MatrixViewMode mode);


//region Getters

    /**
     * Creates a cell factory for groupings selectors
     *
     * @param groupings  the combobox with the items (this is needed as a hack to set the text color)
     * @return  the cell factory
     */
    public final Callback<ListView<Grouping>, ListCell<Grouping>> getGroupingDropDownFactory(ComboBox<Grouping> groupings) {
        return new Callback<>() {
            @Override
            public ListCell<Grouping> call(ListView<Grouping> l) {
                return new ListCell<>() {

                    @Override
                    protected void updateItem(Grouping group, boolean empty) {
                        super.updateItem(group, empty);
                        if (empty || group == null) {
                            setText("");
                        } else {
                            setFont(new Font(fontSize.doubleValue()));
                            setText(group.getName());
                            // this is a stupid janky hack because javafx styling is stupid and hard to work with when you want it to be dynamic
                            // this sets the text color of the grouping item so that the font color can be updated. The conditional is so that
                            // when the combobox is opened and the font color is white the list doesn't appear empty
                            if(group.equals(groupings.getValue())) {
                                setTextFill(group.getFontColor());
                            } else {
                                setTextFill(Grouping.DEFAULT_FONT_COLOR);
                            }
                        }
                    }
                };
            }
        };
    }


    /**
     * @return  the current view mode of the matrix
     */
    public final MatrixViewMode getCurrentMode() {
        return currentMode.getValue();
    }


    /**
     * Finds a cell by a location by iterating over all cells and determining if the grid location is the
     * specified grid location. Runs the search using a parallel stream
     * to increase performance as array will generally be very large when matrices get large. Overhead is not
     * significant enough to detriment performance of smaller matrices
     *
     * @param cellLoc the grid location to get the cell of (row, column)
     * @return        the cell object with the specified grid location
     */
    protected Cell getCellByLoc(Pair<Integer, Integer> cellLoc) {
        // searching in parallel can add some performance improvements once the size of the cells vector increases
        // to a certain point and the overhead is not such that it will detriment smaller matrices significantly
        assert cellLoc != null : "cellLoc was null";

        Optional<Cell> c = cells.stream().parallel().filter(cell -> cell.getGridLocation().getKey().equals(cellLoc.getKey()) && cell.getGridLocation().getValue().equals(cellLoc.getValue())).findFirst();
        return c.orElse(null);
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
     * Finds a cell given its rowUid and colUid by doing a linear search. Runs the search using a parallel stream
     * to increase performance as array will generally be very large when matrices get large. Overhead is not
     * significant enough to detriment performance of smaller matrices
     *
     * @param uids pair of row uid, column uid of the cell to find
     * @return     pair of row location, column location of the cell
     */
    public Pair<Integer, Integer> getGridLocFromUids(Pair<Integer, Integer> uids) {
        if(uids.getKey() == null || uids.getValue() == null) {  // parameter check to save some time if possible (helpful for larger matrices)
            return new Pair<>(null, null);
        }

        // searching in parallel can add some performance improvements once the size of the cells vector increases
        // to a certain point and the overhead is not such that it will detriment smaller matrices significantly
        Optional<Cell> c = cells.stream().parallel().filter(cell -> {
            Pair<Integer, Integer> testUids = getUidsFromGridLoc(cell.getGridLocation());
            return testUids != null && testUids.equals(uids);
        }).findFirst();

        if(c.isPresent()) {
            return new Pair<>(c.get().getGridLocation().getKey(), c.get().getGridLocation().getValue());
        } else {
            return null;
        }
    }


    /**
     * Returns the current layout for the matrix editor that is either mutable or immutable depending on the last refresh call
     *
     * @return VBox of the root layout
     */
    public final VBox getView() {
        return rootLayout;
    }
//endregion


//region Setters
    /**
     * Sets the font size to a new value. This value is bound to in the gui so it will be auto updated.
     *
     * @param newSize the new font size to use in the gui
     */
    public final void setFontSize(Double newSize) {
        fontSize.setValue(newSize);
    }


    /**
     * Sets the new current view mode for the matrix. If switching outside of the class of view
     * mode (static, edit, fast) then a refresh will be triggered
     *
     * @param mode  the new mode for the matrix
     */
    public final void setCurrentMode(MatrixViewMode mode) {
        currentMode.set(mode);
    }


    /**
     * Sets the list of visible interfaces for if the view mode is set to interface types
     *
     * @param currentInterfaces  the list of interfaces to show in the cells
     */
    public final void setVisibleInterfaces(ArrayList<DSMInterfaceType> currentInterfaces) {
        this.currentInterfaces.clear();
        this.currentInterfaces.addAll(currentInterfaces);
    }
//endregion


//region Highlight Functions
    /**
     * Toggles user highlight for a cell
     *  @param cellLoc the grid location of the cell (row, column)
     *
     */
    private void toggleUserHighlightCell(Pair<Integer, Integer> cellLoc) {
        Cell cell = getCellByLoc(cellLoc);
        if(cell == null) return;

        if(cell.getHighlightBG("user") == null) {  // is not highlighted, so highlight it
            cell.updateHighlightBG(AbstractMatrixView.HIGHLIGHT_BACKGROUND, "user");
        } else {
            cell.updateHighlightBG(null, "user");
        }
        refreshCellHighlight(cell);
    }


    /**
     * Function to set several types of highlight for a cell
     *
     * @param cell          the cell object to set the highlight for
     * @param bg            the color to assign to a cell
     * @param highlightType the highlight type to assign to (see Cell class for types)
     */
    public void setCellHighlight(Cell cell, Background bg, String highlightType) {
        if(cell == null) return;

        cell.updateHighlightBG(bg, highlightType);
        refreshCellHighlight(cell);
    }


    /**
     * Function to set several types of highlight for a cell given its grid location
     *
     * @param cellLoc       the grid location of a cell (row, column)
     * @param bg            the color to assign to a cell
     * @param highlightType the highlight type to assign to (see Cell class for types)
     */
    public void setCellHighlight(Pair<Integer, Integer> cellLoc, Background bg, String highlightType) {
        Cell cell = getCellByLoc(cellLoc);
        setCellHighlight(cell, bg, highlightType);
    }


    /**
     * Function to remove several highlight types of a cell by assigning
     * null to that highlight field
     *
     * @param cell          the cell object
     * @param highlightType the highlight type to assign to (userHighlight, errorHighlight, symmetryErrorHighlight)
     */
    public void clearCellHighlight(Cell cell, String highlightType) {
        if(cell == null) return;

        cell.updateHighlightBG(null, highlightType);
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
        clearCellHighlight(cell, highlightType);
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
     * Sets whether a cell should be cross highlighted. Cross highlights by taking grid location
     * and keeping row constant and decrementing column location to minimum and then keeping column
     * constant and decrementing the row location to minimum. When highlighting horizontally and
     * coming across an empty (null) cell, it will then look upwards until it does find a cell. This
     * is to ensure it deals with multi-span cells in a desirable way
     *
     * @param endRow           the index of the last row to highlight
     * @param endCol           the index of the last column to highlight
     * @param shouldHighlight  whether to cross highlight the cell
     */
    private void crossHighlightCell(int endRow, int endCol, boolean shouldHighlight) {
        if(endRow == -1 || endCol == -1) {
            return;
        }

        int minRow = 0;
        int minCol = 0;

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
                    if(cell.getGuiCell() == null) {  // look upwards until finding a non-null cell and highlight this one
                        for(int row=endRow; row>minRow; row--) {
                            if(getCellByLoc(new Pair<>(row, i)).getGuiCell() != null) {
                                cell = getCellByLoc(new Pair<>(row, i));  // update cell with the non-null one
                                break;
                            }
                        }
                    }

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
        crossHighlightData.set(new CrossHighlightData(crossHighlightData.getValue().hoverGridX, crossHighlightData.getValue().hoverGridY, !crossHighlightData.getValue().crossHighlightEnabled));
    }


    /**
     * enables cross-highlighting for a cell and then updates all cells to either remove or
     * add cross highlighting
     */
    public void enableCrossHighlighting() {
        crossHighlightData.set(new CrossHighlightData(crossHighlightData.getValue().hoverGridX, crossHighlightData.getValue().hoverGridY, true));
    }


    /**
     * disables cross-highlighting for a cell and then updates all cells to either remove or
     * add cross highlighting
     */
    public void disableCrossHighlighting() {
        crossHighlightData.set(new CrossHighlightData(crossHighlightData.getValue().hoverGridX, crossHighlightData.getValue().hoverGridY, false));
    }


    /**
     * updates the background color of a cell based on the backgrounds set for it. Error highlight
     * is given the highest priority, then cross highlighting, then user highlighting, and lastly the grouping
     * color (if matrix supports that) of the cell or the default color
     */
    protected abstract void refreshCellHighlight(Cell cell);
//endregion


//region pop-up edit windows

    /**
     * Gets the text to display for a connection cell based on the current render mode
     *
     * @param conn  the connection to make the text for
     * @return      the text to display
     */
    protected String getConnectionCellText(DSMConnection conn) {
        if(conn == null) {
            return "";
        } else if(currentMode.getValue().equals(MatrixViewMode.EDIT_NAMES) || currentMode.getValue().equals(MatrixViewMode.STATIC_NAMES)) {
            String text = conn.getConnectionName();
            if(text.length() > 3) {  // replace with dots because maintaining grid squareness is very important
                return "...";
            }
            return text;
        } else if(currentMode.getValue().equals(MatrixViewMode.EDIT_WEIGHTS) || currentMode.getValue().equals(MatrixViewMode.STATIC_WEIGHTS)) {
            String text = String.valueOf(conn.getWeight());
            if(text.length() > 3) {  // replace with dots because maintaining grid squareness is very important
                return "...";
            }
            return text;
        } else if(currentMode.getValue().equals(MatrixViewMode.EDIT_INTERFACES) || currentMode.getValue().equals(MatrixViewMode.STATIC_INTERFACES)) {
            StringBuilder text = new StringBuilder();
            for(DSMInterfaceType i : conn.getInterfaces()) {
                if(currentInterfaces.contains(i)) {
                    text.append(i.getAbbreviation());
                }
            }
            if(text.length() > 3) {  // replace with dots because maintaining grid squareness is very important
                return "...";
            }

            return text.toString();
        } else {
            return "";
        }
    }


    /**
     * Modifies an hbox in place for a cell that when clicked will handle the editing of a DSM connection
     *
     * @param locationLabel  the label object stating the user's mouse location with units row:column
     * @param rowUid         the uid of the row item
     * @param colUid         the uid of the column item
     * @param gridRowIndex   the row index the cell will be placed in
     * @param gridColIndex   the column index the cell will be placed in
     * @return               the label that was created inside the hbox so that its text color can be updated later
     */
    protected Label getEditableConnectionCell(HBox cell, Label locationLabel, int rowUid, int colUid, int gridRowIndex, int gridColIndex) {
        DSMConnection conn = matrix.getConnection(rowUid, colUid);
        final Label label = new Label();

        label.textProperty().bind(Bindings.createStringBinding(
            () -> getConnectionCellText(conn),
            currentMode,
            currentInterfaces)
        );

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
        cell.setOnMouseClicked(e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)) {
                // create popup window to edit the connection
                Stage window = new Stage();

                // Create Root window
                window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
                window.setTitle("Modify Connection");

                VBox layout = new VBox();

                // row 0
                Label titleLabel = new Label("Connection From " + matrix.getItem(rowUid).getName().getValue() + " to " + matrix.getItem(colUid).getName().getValue());
                GridPane.setConstraints(titleLabel, 0, 0, 3, 1);  // span 3 columns

                // row 1
                HBox row1 = new HBox();
                row1.setPadding(new Insets(10, 10, 10, 10));
                row1.setSpacing(10);
                Label nameLabel = new Label("Connection Type:  ");
                nameLabel.setStyle("-fx-font-weight: bold");

                String currentName;
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
                row2.setAlignment(Pos.CENTER_LEFT);
                Label weightLabel = new Label("Connection Weight:");
                weightLabel.setStyle("-fx-font-weight: bold");
                row2.setPadding(new Insets(10, 10, 10, 10));
                row2.setSpacing(10);

                double currentWeight;
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
                final ObservableList<DSMInterfaceType> interfaceTypes = FXCollections.observableArrayList();
                if(matrix.getConnection(rowUid, colUid) != null) {
                    interfaceTypes.addAll(matrix.getConnection(rowUid, colUid).getInterfaces());
                }

                HBox row3 = new HBox();
                Label interfacesLabel = new Label("Interfaces:");
                interfacesLabel.setMinWidth(Region.USE_PREF_SIZE);
                interfacesLabel.setStyle("-fx-font-weight: bold");

                Label currentInterfacesLabel = new Label();
                currentInterfacesLabel.textProperty().bind(Bindings.createStringBinding(
                        () ->  interfaceTypes.stream().map(DSMInterfaceType::getName).collect(Collectors.joining(", ")),
                        interfaceTypes)
                );
                currentInterfacesLabel.setWrapText(true);
                currentInterfacesLabel.setPadding(new Insets(0, 10, 0, 10));

                Button configureInterfacesButton = new Button("Configure");
                configureInterfacesButton.setOnAction(ee -> {
                    ArrayList<DSMInterfaceType> newInterfaceTypes = ConfigureConnectionInterfaces.configureConnectionInterfaces(matrix.getInterfaceTypes(), interfaceTypes);
                    interfaceTypes.clear();
                    interfaceTypes.addAll(newInterfaceTypes);  // copy all the interfaces to the array
                });
                configureInterfacesButton.setMinWidth(Region.USE_PREF_SIZE);

                row3.getChildren().addAll(interfacesLabel, currentInterfacesLabel, Misc.getHorizontalSpacer(), configureInterfacesButton);
                row3.setPadding(new Insets(0, 10, 0, 10));


                // row 4
                // create HBox for user to close with our without changes
                HBox closeArea = new HBox();
                Button applyButton = new Button("Apply");
                applyButton.setOnAction(ee -> {
                    if(!nameField.getText().equals("")) {
                        double weight;
                        try {
                            weight = Double.parseDouble(weightField.getText());
                        } catch(NumberFormatException nfe) {
                            weight = 1.0;
                        }
                        matrix.modifyConnection(rowUid, colUid, nameField.getText(), weight, new ArrayList<>(interfaceTypes));
                    } else {
                        matrix.deleteConnection(rowUid, colUid);
                    }
                    matrix.setCurrentStateAsCheckpoint();
                    window.close();

                    label.textProperty().unbind();  // reset binding to update text (Bound values cannot be set)
                    DSMConnection newConn = matrix.getConnection(rowUid, colUid);
                    label.setText(getConnectionCellText(newConn));
                    label.textProperty().bind(Bindings.createStringBinding(
                        () -> getConnectionCellText(newConn),
                        currentMode,
                        currentInterfaces)
                    );
                });

                Button cancelButton = new Button("Cancel");
                cancelButton.setOnAction(ee -> {
                    window.close();
                });
                closeArea.getChildren().addAll(cancelButton, Misc.getHorizontalSpacer(), applyButton);

                //Display window and wait for it to be closed before returning
                layout.getChildren().addAll(titleLabel, row1, row2, row3, Misc.getVerticalSpacer(), closeArea);
                layout.setAlignment(Pos.CENTER);
                layout.setPadding(new Insets(10, 10, 10, 10));
                layout.setSpacing(10);

                Scene scene = new Scene(layout, 450, 250);
                window.setScene(scene);
                window.showAndWait();

                // fire the event because the content could change and the cell might need to do something extra that is unique
                // to a specific kind of matrix handler
                cell.fireEvent(new CellChangedEvent(CELL_CHANGED_EVENT));

            } else if(e.getButton().equals(MouseButton.SECONDARY)) {  // toggle highlighting
                toggleUserHighlightCell(new Pair<>(gridRowIndex, gridColIndex));
            }
        });

        cell.setOnMouseEntered(e -> {
            crossHighlightData.set(new CrossHighlightData(gridRowIndex, gridColIndex, crossHighlightData.getValue().crossHighlightEnabled));
            locationLabel.setText(matrix.getItem(rowUid).getName().getValue() + ":" + matrix.getItem(colUid).getName().getValue());
        });

        cell.setOnMouseExited(e -> {
            crossHighlightData.set(new CrossHighlightData(-1, -1, crossHighlightData.getValue().crossHighlightEnabled));
            locationLabel.setText("");
        });
        //endregion

        cell.getChildren().add(label);
        return label;
    }


    /**
     * Opens a popup window to edit an items name in a matrix
     *
     * @param itemUid  the uid of the item in the matrix whose name is being changed
     */
    protected void editItemName(int itemUid) {
        // create popup window to edit the connection
        Stage window = new Stage();

        // Create Root window
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("Modify Item Name");

        VBox layout = new VBox();

        // row 1
        HBox row1 = new HBox();
        row1.setPadding(new Insets(10, 10, 10, 10));
        row1.setSpacing(10);
        Label nameLabel = new Label("Item Name:  ");

        DSMItem item = matrix.getItem(itemUid);

        TextField nameField = new TextField(item.getName().getValue());
        nameField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameField, Priority.ALWAYS);
        row1.getChildren().addAll(nameLabel, nameField);

        // row 3
        // create HBox for user to close with our without changes
        HBox closeArea = new HBox();
        Button applyButton = new Button("Apply Changes");
        applyButton.setOnAction(ee -> {
            matrix.setItemName(item, nameField.getText());
            matrix.setCurrentStateAsCheckpoint();
            window.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(ee -> window.close());
        closeArea.getChildren().addAll(cancelButton, Misc.getHorizontalSpacer(), applyButton);

        //Display window and wait for it to be closed before returning
        layout.getChildren().addAll(row1, Misc.getVerticalSpacer(), closeArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);

        Scene scene = new Scene(layout, 400, 100);
        window.setScene(scene);
        window.showAndWait();
    }
//endregion


//region refresh functions
    /**
     * Creates the gui that displays a matrix with an editable view. Must add the view to rootLayout in order for
     * it to be displayed
     */
    protected abstract void refreshEditView();


    /**
     * Creates the gui that displays a matrix in a static read only view.
     */
    protected abstract void refreshStaticView();


    /**
     * Creates the gui that displays with minimal detail so it can render large matrices faster
     */
    protected abstract void refreshFastRenderView();


    /**
     * Creates the gui that displays a matrix.
     */
    public final void refreshView() {
        switch(currentMode.getValue()) {
            case EDIT_NAMES, EDIT_WEIGHTS, EDIT_INTERFACES -> refreshEditView();
            case STATIC_NAMES, STATIC_WEIGHTS, STATIC_INTERFACES -> refreshStaticView();
            case FAST_RENDER -> refreshFastRenderView();
        }
    }
//endregion

}
