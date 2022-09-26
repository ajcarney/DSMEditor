package Matrices.Views;

import Constants.Constants;
import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.Entities.RenderMode;
import Matrices.Data.MultiDomainDSMData;
import Matrices.Views.Entities.Cell;
import Matrices.Views.Flags.ISymmetricHighlight;
import UI.Widgets.FreezeGrid;
import UI.Widgets.Misc;
import UI.Widgets.NumericTextField;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.util.*;

public class MultiDomainView extends AbstractMatrixView implements ISymmetricHighlight {

    private boolean symmetryValidation = false;
    private Integer domainColumn;

    MultiDomainDSMData matrix;

    /**
     * Returns a MatrixGuiHandler object for a given matrix
     *
     * @param matrix   the SymmetricDSMData object to display
     * @param fontSize the default font size to display the matrix with
     */
    public MultiDomainView(MultiDomainDSMData matrix, double fontSize) {
        super(matrix, fontSize);
        this.matrix = matrix;
    }


    /**
     * Builder pattern method for setting the font size
     *
     * @param fontSize  the new font size for the matrix view
     * @return          this
     */
    public MultiDomainView withFontSize(double fontSize) {
        this.fontSize.set(fontSize);
        return this;
    }


    /**
     * Builder pattern method for setting the matrix view mode
     *
     * @param mode  the new mode for the matrix view
     * @return      this
     */
    public MultiDomainView withMode(MatrixViewMode mode) {
        this.currentMode.set(mode);
        return this;
    }


    /**
     * Copy constructor for MultiDomainView class. Performs a deep copy on the matrix data. Everything else will be
     * generated when the refreshView method is called
     *
     * @return  the copy of the current MultiDomainView
     */
    @Override
    public MultiDomainView createCopy() {
        MultiDomainView copy = new MultiDomainView(matrix.createCopy(), fontSize.doubleValue());

        copy.setCurrentMode(getCurrentMode());

        // no need to copy gridUidLookup HashMap because those values are generated from the matrix on the
        // refreshView call which can be done later

        return copy;
    }


    //region highlight methods
    /**
     * updates the background color of a cell based on the backgrounds set for it. Error highlight
     * is given the highest priority, then cross highlighting, then user highlighting, and lastly the grouping
     * color of the cell or the default color
     */
    @Override
    public void refreshCellHighlight(Cell cell) {
        if (cell.getHighlightBG("error") != null) {
            cell.setCellHighlight(cell.getHighlightBG("error"));
            cell.setCellTextColor(Grouping.DEFAULT_FONT_COLOR);

        } else if(cell.getHighlightBG("search") != null) {
            cell.setCellHighlight(cell.getHighlightBG("search"));
            cell.setCellTextColor(Grouping.DEFAULT_FONT_COLOR);

        } else if(cell.getHighlightBG("symmetryError") != null) {
            cell.setCellHighlight(cell.getHighlightBG("symmetryError"));
            cell.setCellTextColor(Grouping.DEFAULT_FONT_COLOR);

        } else if (cell.getHighlightBG("cross") != null) {
            cell.setCellHighlight(cell.getHighlightBG("cross"));
            cell.setCellTextColor(Grouping.DEFAULT_FONT_COLOR);

        } else if (cell.getHighlightBG("user") != null) {
            cell.setCellHighlight(cell.getHighlightBG("user"));
            cell.setCellTextColor(Grouping.DEFAULT_FONT_COLOR);

        } else {  // default background determined by groupings
            Integer rowUid = getUidsFromGridLoc(cell.getGridLocation()).getKey();  // null is used to check if it is an item or grouping cell
            Integer colUid = getUidsFromGridLoc(cell.getGridLocation()).getValue();
            if (rowUid == null && colUid != null) {  // highlight with column color
                DSMItem col = matrix.getColItem(colUid);
                cell.setCellHighlight(col.getGroup1().getColor());
                cell.setCellTextColor(col.getGroup1().getFontColor());
            } else if (rowUid != null && colUid == null) {  // highlight with row color
                DSMItem row = matrix.getRowItem(rowUid);
                if(cell.getGridLocation().getValue().equals(domainColumn)) {
                    cell.setCellHighlight(row.getGroup2().getColor());
                    cell.setCellTextColor(row.getGroup2().getFontColor());
                } else {
                    cell.setCellHighlight(row.getGroup1().getColor());
                    cell.setCellTextColor(row.getGroup1().getFontColor());
                }
            } else if (
                    rowUid != null && colUid != null
                    && !rowUid.equals(matrix.getColItem(colUid).getAliasUid())
                    && matrix.getRowItem(rowUid).getGroup1().equals(matrix.getColItem(colUid).getGroup1())
                    && matrix.getRowItem(rowUid).getGroup2().equals(matrix.getColItem(colUid).getGroup2())
            ) {
                // row and column color will be the same because row and column
                // have same group in symmetric matrix
                DSMItem row = matrix.getRowItem(rowUid);
                cell.setCellHighlight(row.getGroup1().getColor());
                cell.setCellTextColor(row.getGroup1().getFontColor());
            } else {
                cell.setCellHighlight(cell.getHighlightBG("default"));
                cell.setCellTextColor(Grouping.DEFAULT_FONT_COLOR);
            }

        }
    }



    /**
     * Sets or clears a cells symmetry highlight based on the symmetryValidation flag
     *
     * @param cell  the cell to check the highlighting for
     */
    private void symmetryHighlightCell(Cell cell) {
        Pair<Integer, Integer> gridLocation = cell.getGridLocation();
        Pair<Integer, Integer> uids = getUidsFromGridLoc(gridLocation);
        if(uids.getKey() == null || uids.getValue() == null) {
            return;
        }

        int rowUid = uids.getKey();
        int colUid = uids.getValue();
        DSMConnection conn = matrix.getConnection(rowUid, colUid);
        Pair<Integer, Integer> symmetricConnUids = matrix.getSymmetricConnectionUids(rowUid, colUid);
        DSMConnection symmetricConn = matrix.getConnection(symmetricConnUids.getKey(), symmetricConnUids.getValue());

        if(symmetryValidation && ((conn == null && symmetricConn != null) || (conn != null && symmetricConn == null) || (conn != null && symmetricConn != null && !conn.isSameConnectionType(symmetricConn)))) {
            this.setCellHighlight(cell, AbstractMatrixView.SYMMETRY_ERROR_BACKGROUND, "symmetryError");
            this.setCellHighlight(this.getGridLocFromUids(symmetricConnUids), AbstractMatrixView.SYMMETRY_ERROR_BACKGROUND, "symmetryError");
        } else {
            this.clearCellHighlight(cell, "symmetryError");
            this.clearCellHighlight(this.getGridLocFromUids(symmetricConnUids), "symmetryError");
        }
    }


    /**
     * Highlights all cells symmetrically, faster than using symmetryHighlightCell in a loop because it will not double
     * check the highlight of all cells
     */
    private void symmetryHighlightAllCells() {
        for(Cell cell : cells) {
            Pair<Integer, Integer> gridLocation = cell.getGridLocation();
            Pair<Integer, Integer> uids = getUidsFromGridLoc(gridLocation);
            if(uids.getKey() == null || uids.getValue() == null) {
                continue;
            }

            int rowUid = uids.getKey();
            int colUid = uids.getValue();
            DSMConnection conn = matrix.getConnection(rowUid, colUid);
            Pair<Integer, Integer> symmetricConnUids = matrix.getSymmetricConnectionUids(rowUid, colUid);
            DSMConnection symmetricConn = matrix.getConnection(symmetricConnUids.getKey(), symmetricConnUids.getValue());

            // ignore the symmetric connection because it will be hit later in the loop
            if(symmetryValidation && ((conn == null && symmetricConn != null) || (conn != null && symmetricConn == null) || (conn != null && symmetricConn != null && !conn.isSameConnectionType(symmetricConn)))) {
                this.setCellHighlight(cell, AbstractMatrixView.SYMMETRY_ERROR_BACKGROUND, "symmetryError");
            } else {
                this.clearCellHighlight(cell, "symmetryError");
            }
        }
    }


    /**
     * Sets symmetryValidation to true in order to highlight symmetry errors
     */
    public void setValidateSymmetry() {
        symmetryValidation = true;
        symmetryHighlightAllCells();
        for(Cell cell : cells) {
            refreshCellHighlight(cell);
        }
    }


    /**
     * Sets symmetryValidation to false in order to stop highlighting symmetry errors
     */
    public void clearValidateSymmetry() {
        symmetryValidation = false;
        symmetryHighlightAllCells();
        for(Cell cell : cells) {
            refreshCellHighlight(cell);
        }
    }


    /**
     * @return  if validation symmetry is set or not
     */
    public boolean getSymmetryValidation() {
        return symmetryValidation;
    }
    //endregion


    /**
     * Creates the gui that displays a matrix with an editable view. Uses the SymmetricDSMData's getGridArray() method
     * to create the grid. Adds a location label (displays connection row, column)
     * at the bottom of the VBox.
     */
    @Override
    protected void refreshEditView() {
        cells = new Vector<>();
        gridUidLookup = new HashMap<>();
        gridUidLookup.put("rows", new HashMap<>());
        gridUidLookup.put("cols", new HashMap<>());

        rootLayout.getChildren().clear();
        rootLayout.setAlignment(Pos.CENTER);

        Label locationLabel = new Label("");
        FreezeGrid grid = new FreezeGrid();

        ArrayList<ArrayList<Pair<RenderMode, Object>>> template = matrix.getGridArray();
        ArrayList<ArrayList<HBox>> gridData = new ArrayList<>();
        int rows = template.size();
        int columns = template.get(0).size();


        // create a test item to determine layout width for a vertical item cell to square up the matrix when viewed
        // as well as to determine the width of grouping ComboBoxes because they all contain different items
        ComboBox<Grouping> _groupings = new ComboBox<>();
        Grouping longestGroup = matrix.getDomainGroupings().stream().max(Comparator.comparing(g -> g.getName().length())).orElse(new Grouping("", Color.WHITE));
        ObservableList<Grouping> _g = FXCollections.observableArrayList();
        _g.add(longestGroup);
        _groupings.setItems(_g);
        _groupings.setMinWidth(Region.USE_COMPUTED_SIZE);
        _groupings.setStyle("-fx-font-size: " + (fontSize.doubleValue()) + " };");
        double maxHeight = Misc.calculateNodeSize(_groupings).getHeight();

        // Use a label to calculate the width of a grouping because a combobox is grossly inaccurate
        // Add a constant to the width to add extra padding that javafx gives between text and the arrow so that
        // the ComboBox's text is not cut off. A possible reason for the poor size calculation is given at
        // from this StackOverflow issue: https://stackoverflow.com/questions/24852429/making-a-smaller-javafx-combobox
        Label _groupingsWidthLabel = new Label(longestGroup.getName());
        double groupingWidth = Misc.calculateNodeSize(_groupingsWidthLabel).getWidth() + 40;


        // create a list to update the cell spans once the matrix has been created. Order is:
        // row location, column location, row span, column span
        ArrayList<Quartet<Integer, Integer, Integer, Integer>> cellSpans = new ArrayList<>();

        for(int r=0; r<rows; r++) {
            ArrayList<HBox> rowData = new ArrayList<>();
            for(int c=0; c<columns; c++) {
                Pair<RenderMode, Object> item = template.get(r).get(c);
                HBox cell = new HBox();  // wrap everything in an HBox so a border can be added easily
                Label label = null;

                Background defaultBackground = DEFAULT_BACKGROUND;

                switch (item.getKey()) {
                    case PLAIN_TEXT -> {
                        label = new Label((String) item.getValue());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        label.setPadding(new Insets(1));
                        cell.getChildren().add(label);
                    }
                    case PLAIN_TEXT_V -> {
                        label = new Label((String) item.getValue());
                        label.setRotate(-90);
                        label.setPadding(new Insets(1));
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case MULTI_SPAN_DOMAIN_TEXT -> {
                        Triplet<Grouping, Integer, Integer> data = (Triplet<Grouping, Integer, Integer>) item.getValue();
                        label = new Label(data.getValue0().getName());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        label.setPadding(new Insets(1, 5, 1, 5));
                        cell.getChildren().add(label);
                        cell.setAlignment(Pos.CENTER);

                        cellSpans.add(new Quartet<>(r, c, data.getValue1(), data.getValue2()));
                        domainColumn = c;
                    }
                    case MULTI_SPAN_NULL -> cell = null;  // set cell to null so it can be expanded into
                    case ITEM_NAME -> {
                        label = new Label();
                        label.setPadding(new Insets(0, 5, 0, 5));
                        label.textProperty().bind(((DSMItem) item.getValue()).getName());
                        cell.setAlignment(Pos.CENTER_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                        int finalC = c;
                        cell.setOnMouseClicked(e -> {
                            if (e.getButton().equals(MouseButton.PRIMARY)) {
                                editItemName(((DSMItem) item.getValue()).getUid());
                                grid.resizeColumn(finalC);
                                grid.resizeRow(0);  // 0 is first row which contains the vertical names (this is cheating)
                            }
                        });
                    }
                    case ITEM_NAME_V -> {
                        label = new Label();
                        label.textProperty().bind(((DSMItem) item.getValue()).getName());
                        label.setPadding(new Insets(0, 5, 0, 5));
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_CENTER);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                        int finalR = r;
                        cell.setOnMouseClicked(e -> {
                            if (e.getButton().equals(MouseButton.PRIMARY)) {
                                editItemName(((DSMItem) item.getValue()).getUid());
                                grid.resizeRow(finalR);
                                grid.resizeColumn(1);  // 1 is first column which contains the horizontal names (this is cheating)
                            }
                        });
                        cell.setMinWidth(maxHeight);  // set a min width so that the matrix is less boxy (all connection items will follow this even if not
                                                      // explicitly set due to how the freeze grid is set up)
                    }
                    case GROUPING_ITEM -> {  // dropdown box for choosing group
                        DSMItem matrixItem = ((DSMItem) item.getValue());
                        ComboBox<Grouping> groupings = new ComboBox<>();
                        groupings.setMinWidth(groupingWidth);
                        groupings.setPadding(new Insets(0));
                        groupings.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(3), new Insets(0))));

                        Callback<ListView<Grouping>, ListCell<Grouping>> groupingItemCellFactory = getGroupingDropDownFactory(groupings);
                        groupings.setCellFactory(groupingItemCellFactory);
                        groupings.setButtonCell(groupingItemCellFactory.call(null));

                        groupings.getItems().addAll(matrix.getDomainGroupings(matrixItem.getGroup2()));
                        groupings.getSelectionModel().select(matrixItem.getGroup1());
                        groupings.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                            matrix.setItemDomainGroup(matrixItem, matrixItem.getGroup2(), groupings.getValue());
                            matrix.setCurrentStateAsCheckpoint();
                            for (Cell c_ : cells) {
                                refreshCellHighlight(c_);
                            }
                        });

                        cell.getChildren().add(groupings);
                    }
                    case INDEX_ITEM -> {
                        NumericTextField entry = new NumericTextField(((DSMItem) item.getValue()).getSortIndex());
                        entry.setPrefColumnCount(3);  // set size to 3 characters fitting
                        entry.setPadding(new Insets(0));
                        cell.setMaxWidth(Region.USE_COMPUTED_SIZE);
                        cell.setAlignment(Pos.CENTER);
                        int finalR = r;
                        int finalC = c;
                        HBox finalCell = cell;
                        entry.setOnAction(e -> {  // remove focus when enter key is pressed
                            finalCell.getParent().requestFocus();
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
                    case UNEDITABLE_CONNECTION -> defaultBackground = UNEDITABLE_CONNECTION_BACKGROUND;
                    case EDITABLE_CONNECTION -> {
                        int rowUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getKey().getUid();
                        int colUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getValue().getUid();
                        label = getEditableConnectionCell(cell, locationLabel, rowUid, colUid, r, c);
                        int finalR = r;
                        int finalC = c;
                        cell.addEventHandler(CELL_CHANGED_EVENT, e -> {
                            symmetryHighlightCell(getCellByLoc(new Pair<>(finalR, finalC)));
                        });
                    }

                }

                if(cell != null) {
                    cell.setPadding(new Insets(0));
                }
                rowData.add(cell);

                Cell cellObject = new Cell(new Pair<>(r, c), cell, label, fontSize);
                cellObject.setCellBorder(Color.BLACK);
                cellObject.updateHighlightBG(defaultBackground, "default");
                cells.add(cellObject);
            }
            gridData.add(rowData);
        }

        // only run this section if symmetryValidation is enabled because if cells default to not being highlighted
        // then it is a waste of precious time to clear something that is already cleared
        if(symmetryValidation) {
            symmetryHighlightAllCells();
        }

        for (Cell c_ : cells) {  // this is needed outside the render loop so that the groupings and item names are highlighted correctly
            refreshCellHighlight(c_);
        }

        grid.setGridDataHBox(gridData);
        for(Quartet<Integer, Integer, Integer, Integer> cell : cellSpans) {  // update the cell spans
            grid.setCellSpan(cell.getValue0(), cell.getValue1(), cell.getValue2(), cell.getValue3());
        }

        grid.setFreezeLeft(4);
        grid.setFreezeHeader(2);  // freeze top two rows for symmetric matrix

        ArrayList<Integer> importantRows = new ArrayList<>(Arrays.asList(0, 1));
        ArrayList<Integer> importantCols = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        grid.resizeGrid(true, importantRows, importantCols);
        grid.updateGrid();

        rootLayout.getChildren().addAll(grid.getGrid(), locationLabel);
    }


    /**
     * Creates the guid that displays a matrix in a static read only view. Uses the SymmetricDSMData's getGridArray() method
     * to create the grid.
     */
    @Override
    protected void refreshStaticView() {
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
        ArrayList<ArrayList<Pair<RenderMode, Object>>> template = matrix.getGridArray();
        int rows = template.size();
        int columns = template.get(0).size();

        for(int r=0; r<rows; r++) {
            for(int c=0; c<columns; c++) {
                Pair<RenderMode, Object> item = template.get(r).get(c);
                HBox cell = null;  // wrap everything in an HBox so a border can be added easily
                Label label = null;

                Background defaultBackground = DEFAULT_BACKGROUND;

                switch (item.getKey()) {
                    case PLAIN_TEXT -> {
                        cell = new HBox();
                        label = new Label((String) item.getValue());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case PLAIN_TEXT_V -> {
                        cell = new HBox();
                        label = new Label((String) item.getValue());
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case MULTI_SPAN_DOMAIN_TEXT -> {
                        cell = new HBox();
                        Triplet<Grouping, Integer, Integer> data = (Triplet<Grouping, Integer, Integer>) item.getValue();
                        label = new Label(data.getValue0().getName());
                        cell.setAlignment(Pos.CENTER);
                        cell.getChildren().add(label);

                        GridPane.setRowSpan(cell, data.getValue1());
                        GridPane.setColumnSpan(cell, data.getValue2());
                        domainColumn = c;
                    }
                    case ITEM_NAME -> {
                        cell = new HBox();
                        label = new Label(((DSMItem) item.getValue()).getName().getValue());
                        label.setPadding(new Insets(0, 5, 0, 5));
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case ITEM_NAME_V -> {
                        cell = new HBox();
                        label = new Label(((DSMItem) item.getValue()).getName().getValue());
                        label.setPadding(new Insets(0, 5, 0, 5));
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case GROUPING_ITEM -> {
                        cell = new HBox();
                        label = new Label(((DSMItem) item.getValue()).getGroup1().getName());
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case INDEX_ITEM -> {
                        cell = new HBox();
                        label = new Label(String.valueOf(((DSMItem) item.getValue()).getSortIndex()));
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case UNEDITABLE_CONNECTION -> {
                        cell = new HBox();
                        defaultBackground = UNEDITABLE_CONNECTION_BACKGROUND;
                    }
                    case EDITABLE_CONNECTION -> {
                        cell = new HBox();
                        int rowUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getKey().getUid();
                        int colUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getValue().getUid();
                        DSMConnection conn = matrix.getConnection(rowUid, colUid);
                        label = new Label(getConnectionCellText(conn));

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
                if(cell != null) {
                    cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                    cell.setPadding(new Insets(0));
                    GridPane.setConstraints(cell, c, r);
                    grid.getChildren().add(cell);
                }

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


    /**
     * Creates the gui that displays with minimal detail so it can render large matrices faster
     */
    @Override
    protected void refreshFastRenderView() {
        cells = new Vector<>();
        gridUidLookup = new HashMap<>();
        gridUidLookup.put("rows", new HashMap<>());
        gridUidLookup.put("cols", new HashMap<>());

        rootLayout.getChildren().clear();
        rootLayout.setAlignment(Pos.CENTER);

        FreezeGrid grid = new FreezeGrid();

        ArrayList<ArrayList<Pair<RenderMode, Object>>> template = matrix.getGridArray();
        ArrayList<ArrayList<HBox>> gridData = new ArrayList<>();

        int numRows = template.size();
        int numCols = template.get(0).size();

        // set up the scaled font size. Don't use the normal range because if fast render is on matrices will likely
        // be very large and a smaller font size is needed to view them
        double minFontSize = Constants.fastRenderMinFontSize;
        double maxFontSize = Constants.fastRenderMaxFontSize;
        double scaledFont = ((maxFontSize - minFontSize) * ((fontSize.doubleValue() - Constants.fontSizes[0]) / (Constants.fontSizes[Constants.fontSizes.length - 1] - Constants.fontSizes[0]))) + minFontSize;
        DoubleProperty scaledFontSize = new SimpleDoubleProperty(scaledFont);
        double connectionDotSize = 2;  // this is the number of pixels a circle representing a dot will be

        // create test items to statically determine widths and heights for different items in the cell
        // the sizes are really only needed to calculate where to draw items in the canvas, the freeze grid
        // can render the rest automatically. get a label and determine its height because this will be the
        // size used for the cells.
        Label testLabel = new Label();
        testLabel.setStyle("-fx-font-size: " + scaledFontSize.doubleValue());
        testLabel.setPadding(new Insets(1, 5, 1, 5));
        testLabel.setMinWidth(Region.USE_PREF_SIZE);
        HBox testCell = new HBox();  // wrap everything in an HBox so a border can be added easily
        testCell.getChildren().add(testLabel);

        // Start by calculating the width of column 1 (domains)
        String longestDomainName = matrix.getDomains().stream().max(Comparator.comparing(g -> g.getName().length())).orElse(new Grouping("", Color.BLACK)).getName();
        longestDomainName = longestDomainName.length() > "Groupings".length() ? longestDomainName: "Groupings";
        testLabel.setText(longestDomainName);
        double col1Width = Misc.calculateNodeSize(testCell).getWidth();

        // calculate width of column 2
        ArrayList<Grouping> domainGroupings = new ArrayList<>();
        for(Grouping domain : matrix.getDomains()) {
            domainGroupings.addAll(matrix.getDomainGroupings(domain));
        }
        String longestGroupingName = domainGroupings.stream().max(Comparator.comparing(g -> g.getName().length())).orElse(new Grouping("", Color.BLACK)).getName();
        longestGroupingName = longestGroupingName.length() > "Groupings".length() ? longestGroupingName: "Groupings";
        testLabel.setText(longestGroupingName);
        double col2Width = Misc.calculateNodeSize(testCell).getWidth();

        // calculate width of column 3 and height of row 1 (item names)
        String longestItemName = matrix.getRows().stream().max(Comparator.comparing((DSMItem item) -> item.getName().toString().length())).orElse(new DSMItem(-1.0, "")).getName().getValue();
        longestItemName = longestItemName.length() > "Column Items".length() ? longestItemName: "Column Items";
        testLabel.setText(longestItemName);
        double col3Width = Misc.calculateNodeSize(testCell).getWidth();
        double row1Height = col3Width;

        // calculate width of column 3
        testLabel.setText("Re-Sort Index");
        double col4Width = Misc.calculateNodeSize(testCell).getWidth();

        // calculate height of row 2 and connection cell size
        testLabel.setText("x");
        double row2Height = Misc.calculateNodeSize(testCell).getHeight();
        double cellSize = row2Height;


        // with the way freeze grid handles spanning cells there needs to be a bunch of nulls and a single
        // node for the canvas with the row and col span set. To avoid weird indexing and looping issues
        // all connection items will initially be null and the one that needs to span will be updated later
        int connectionsRow = -1;
        int connectionsCol = -1;

        // keep track of the connections so that they can be plotted on a canvas
        // object later. Only the grid location of its row and column items needs
        // to be tracked as it is immutable and only displayed as a single dot.
        // Also keep track of the uneditable connection cells so they can be
        // plotted as a box
        ArrayList<Triplet<Integer, Integer, Color>> connectionLocations = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> uneditableConnectionLocations = new ArrayList<>();

        // create a list to update the cell spans once the matrix has been created. Order is:
        // row location, column location, row span, column span
        ArrayList<Quartet<Integer, Integer, Integer, Integer>> cellSpans = new ArrayList<>();

        // update the cells so they can be displayed by the freeze grid
        for(int r=0; r<numRows; r++) {
            ArrayList<HBox> rowData = new ArrayList<>();
            for(int c=0; c<numCols; c++) {
                Pair<RenderMode, Object> item = template.get(r).get(c);
                HBox cell = new HBox();  // wrap everything in an HBox so a border can be added easily
                Label label = null;

                Background defaultBackground = DEFAULT_BACKGROUND;

                switch (item.getKey()) {
                    case PLAIN_TEXT -> {
                        label = new Label((String) item.getValue());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        label.setPadding(new Insets(1));
                        cell.getChildren().add(label);
                    }
                    case PLAIN_TEXT_V -> {
                        label = new Label((String) item.getValue());
                        label.setRotate(-90);
                        label.setPadding(new Insets(1));
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case MULTI_SPAN_DOMAIN_TEXT -> {
                        Triplet<Grouping, Integer, Integer> data = (Triplet<Grouping, Integer, Integer>) item.getValue();
                        label = new Label(data.getValue0().getName());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        label.setPadding(new Insets(1, 5, 1, 5));
                        cell.getChildren().add(label);
                        cell.setAlignment(Pos.CENTER);

                        cellSpans.add(new Quartet<>(r, c, data.getValue1(), data.getValue2()));
                        domainColumn = c;
                    }
                    case MULTI_SPAN_NULL -> cell = null;  // set cell to null so it can be expanded into
                    case ITEM_NAME -> {
                        label = new Label();
                        label.setPadding(new Insets(0, 5, 0, 5));
                        label.textProperty().bind(((DSMItem) item.getValue()).getName());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.setAlignment(Pos.CENTER_RIGHT);
                        cell.getChildren().add(label);
                    }
                    case ITEM_NAME_V -> {
                        label = new Label();
                        label.textProperty().bind(((DSMItem) item.getValue()).getName());
                        label.setPadding(new Insets(0, 5, 0, 5));
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_CENTER);

                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical
                        g.getChildren().add(label);
                        cell.getChildren().add(g);

                        // set a min width so that the matrix is less boxy (all connection items will follow this even if not
                        // explicitly set due to how the freeze grid is set up)
                        cell.setMinWidth(cellSize);
                        cell.setMaxWidth(cellSize);
                        cell.setPrefWidth(cellSize);
                    }
                    case GROUPING_ITEM -> {  // dropdown box for choosing group
                        Grouping group = ((DSMItem) item.getValue()).getGroup1();
                        label = new Label(group.getName());
                        label.setPadding(new Insets(0, 5, 0, 5));
                        label.setTextFill(group.getFontColor());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.setAlignment(Pos.CENTER);
                        cell.getChildren().add(label);
                    }
                    case INDEX_ITEM -> {
                        Grouping group = ((DSMItem) item.getValue()).getGroup1();
                        label = new Label(String.valueOf(((DSMItem) item.getValue()).getSortIndex()));
                        label.setPadding(new Insets(0, 5, 0, 5));
                        label.setTextFill(group.getFontColor());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.setAlignment(Pos.CENTER);
                        cell.getChildren().add(label);
                    }
                    case UNEDITABLE_CONNECTION -> {
                        uneditableConnectionLocations.add(new Pair<>(r, c));
                        cell = null;
                        if(connectionsRow == -1) {  // if not set, this will be the first connection cell so set it here
                            connectionsRow = r;
                            connectionsCol = c;
                        }
                    }
                    case EDITABLE_CONNECTION -> {
                        int rowUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getKey().getUid();
                        int colUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getValue().getUid();
                        DSMConnection conn = matrix.getConnection(rowUid, colUid);
                        if(conn != null) {  // only add connections that exist
                            Color color = Color.BLACK;  // default to black
                            DSMItem rowItem = matrix.getRowItem(rowUid);
                            DSMItem colItem = matrix.getColItem(colUid);
                            if(rowItem.getGroup1().equals(colItem.getGroup1())) {
                                color = rowItem.getGroup1().getFontColor();
                            }
                            connectionLocations.add(new Triplet<>(r, c, color));
                        }
                        cell = null;

                        // this item type will be used to create the lookup table for finding associated uid from grid location
                        // this is needed to determine the cell highlights for name cells
                        if(!gridUidLookup.get("rows").containsKey(r)) {
                            gridUidLookup.get("rows").put(r, rowUid);
                        }

                        if(!gridUidLookup.get("cols").containsKey(c)) {
                            gridUidLookup.get("cols").put(c, colUid);
                        }
                    }
                }

                // add to data to be tracked
                if(cell == null) {
                    rowData.add(null);
                    cells.add(new Cell(new Pair<>(r, c), new HBox(), new Label(), scaledFontSize));  // add a generic cell because it won't be used
                } else {
                    Cell cellObject = new Cell(new Pair<>(r, c), cell, label, scaledFontSize);
                    rowData.add(cell);
                    cellObject.setCellBorder(Color.BLACK);
                    cellObject.updateHighlightBG(defaultBackground, "default");
                    cells.add(cellObject);
                }
            }
            gridData.add(rowData);
        }

        //region canvas setup
        // draw the canvas for the connections
        HBox canvasPane = new HBox();

        double canvasWidth = matrix.getCols().size() * cellSize;
        double canvasHeight = matrix.getRows().size() * cellSize;
        Canvas canvas = new Canvas(canvasWidth, canvasHeight);

        GraphicsContext graphics_context = canvas.getGraphicsContext2D();

        // draw the background
        Color defaultColor = (Color) DEFAULT_BACKGROUND.getFills().get(0).getFill();
        graphics_context.setFill(defaultColor);
        graphics_context.fillRect(0, 0, canvasWidth, canvasHeight);

        // draw the grouping rectangles
        double x = 0;
        double y = 0;
        for(DSMItem rowItem : matrix.getRows()) {
            for(DSMItem colItem : matrix.getCols()) {
                if(rowItem.getGroup1().equals(colItem.getGroup1()) && rowItem.getGroup2().equals(colItem.getGroup2())) {
                    graphics_context.setFill(rowItem.getGroup1().getColor());
                } else {
                    graphics_context.setFill(defaultColor);
                }
                graphics_context.fillRect(x, y, cellSize, cellSize);
                x += cellSize;
            }
            x = 0;  // reset to first column at start of canvas
            y += cellSize;  // move to next row
        }

        // draw the uneditable connection boxes
        graphics_context.setFill(UNEDITABLE_CONNECTION_BACKGROUND.getFills().get(0).getFill());
        for(Pair<Integer, Integer> coordinates : uneditableConnectionLocations) {
            x = (coordinates.getKey() - connectionsRow) * cellSize;  // coordinates offset by connections start index so subtract it
            y = (coordinates.getValue() - connectionsCol) * cellSize;
            graphics_context.fillRect(x, y, cellSize, cellSize);
        }

        // draw the connections
        for(Triplet<Integer, Integer, Color> connection : connectionLocations) {
            graphics_context.setFill(connection.getValue2());
            // coordinates offset by connections start index so subtract it. Coordinates give top right of cell so
            // center it by averaging cell size. Subtract half the size of the circle so it stays centered
            x = ((connection.getValue1() - connectionsCol) * cellSize) + (cellSize / 2) - (connectionDotSize / 2);  // x corresponds to the column number, y corresponds to the row number
            y = ((connection.getValue0() - connectionsRow) * cellSize) + (cellSize / 2) - (connectionDotSize / 2);
            graphics_context.fillOval(x, y, connectionDotSize, connectionDotSize);
        }

        canvasPane.getChildren().add(canvas);
        gridData.get(connectionsRow).set(connectionsCol, canvasPane);  // update the item to be the canvas
        //endregion

        for (Cell c_ : cells) {  // this is needed outsize the render loop so that the groupings and item names are highlighted correctly
            refreshCellHighlight(c_);
        }

        grid.setGridDataHBox(gridData);

        // update the cell spans
        for(Quartet<Integer, Integer, Integer, Integer> cell : cellSpans) {
            grid.setCellSpan(cell.getValue0(), cell.getValue1(), cell.getValue2(), cell.getValue3());
        }

        // set the span of the canvas item
        grid.setCellSpan(connectionsRow, connectionsCol, matrix.getRows().size(), matrix.getCols().size());

        // set up the freezing
        grid.setFreezeLeft(4);
        grid.setFreezeHeader(2);

        // manually size all the items to save time and be more accurate
        grid.setRowPrefHeight(0, row1Height);
        grid.setRowPrefHeight(1, row2Height);
        grid.setColPrefWidth(0, col1Width);
        grid.setColPrefWidth(1, col2Width);
        grid.setColPrefWidth(2, col3Width);
        grid.setColPrefWidth(3, col4Width);

        for(int r=connectionsRow; r<(connectionsRow + matrix.getRows().size()); r++) {  // manually force sizes for rows and columns
            grid.setRowPrefHeight(r, cellSize);
        }
        for(int c=connectionsCol; c<(connectionsCol + matrix.getCols().size()); c++) {
            grid.setColPrefWidth(c, cellSize);
        }

        grid.updateGrid();

        rootLayout.getChildren().addAll(grid.getGrid());
    }

}
