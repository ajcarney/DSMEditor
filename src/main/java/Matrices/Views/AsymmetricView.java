package Matrices.Views;

import Constants.Constants;
import Matrices.Data.AsymmetricDSMData;
import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.Entities.RenderMode;
import Matrices.Views.Entities.Cell;
import UI.Widgets.FreezeGrid;
import UI.Widgets.Misc;
import UI.Widgets.NumericTextField;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.util.Callback;
import javafx.util.Pair;
import org.javatuples.Triplet;

import java.util.*;


/**
 * class for viewing and editing asymmetric matrices
 */
public class AsymmetricView extends AbstractMatrixView {

    AsymmetricDSMData matrix;

    /**
     * Returns a MatrixGuiHandler object for a given matrix
     *
     * @param matrix   the SymmetricDSMData object to display
     * @param fontSize the default font size to display the matrix with
     */
    public AsymmetricView(AsymmetricDSMData matrix, double fontSize) {
        super(matrix, fontSize);
        this.matrix = matrix;
    }


    /**
     * Builder pattern method for setting the font size
     *
     * @param fontSize  the new font size for the matrix view
     * @return          this
     */
    public AsymmetricView withFontSize(double fontSize) {
        this.fontSize.set(fontSize);
        return this;
    }


    /**
     * Builder pattern method for setting the matrix view mode
     *
     * @param mode  the new mode for the matrix view
     * @return      this
     */
    public AsymmetricView withMode(MatrixViewMode mode) {
        this.currentMode.set(mode);
        return this;
    }


    /**
     * Copy constructor for AsymmetricView class. Performs a deep copy on the matrix data. Everything else will be
     * generated when the refreshView method is called
     *
     * @return  the copy of the current AsymmetricView
     */
    @Override
    public AsymmetricView createCopy() {
        AsymmetricView copy = new AsymmetricView(matrix.createCopy(), fontSize.doubleValue());

        copy.setCurrentMode(getCurrentMode());

        // no need to copy gridUidLookup HashMap because those values are generated from the matrix on the
        // refreshView call which can be done later

        return copy;
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
            Integer rowUid = getUidsFromGridLoc(cell.getGridLocation()).getKey();
            Integer colUid = getUidsFromGridLoc(cell.getGridLocation()).getValue();
            if (rowUid == null && colUid != null) {  // highlight with column color
                cell.setCellHighlight(matrix.getColItem(colUid).getGroup1().getColor());
                cell.setCellTextColor(matrix.getColItem(colUid).getGroup1().getFontColor());
            } else if (rowUid != null && colUid == null) {  // highlight with row color
                cell.setCellHighlight(matrix.getRowItem(rowUid).getGroup1().getColor());
                cell.setCellTextColor(matrix.getRowItem(rowUid).getGroup1().getFontColor());
            } else if (rowUid != null && colUid != null) {  // highlight with row group color col group color
                Stop[] stops = new Stop[] { new Stop(0, matrix.getRowItem(rowUid).getGroup1().getColor()), new Stop(1, matrix.getColItem(colUid).getGroup1().getColor())};
                LinearGradient lg1 = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

                cell.setCellHighlight(new Background(new BackgroundFill(lg1, new CornerRadii(3), new Insets(0))));  // row and column color will be the same because row and column
                cell.setCellTextColor(matrix.getRowItem(rowUid).getGroup1().getFontColor());
            } else {
                cell.setCellHighlight(cell.getHighlightBG("default"));
                cell.setCellTextColor(Grouping.DEFAULT_FONT_COLOR);
            }
        }
    }


    /**
     * Creates the gui that displays a matrix. Uses the AsymmetricDSMData's getGridArray() method to create the grid.
     * Puts grid in a scroll pane and adds a location label (displays connection row, column) at the bottom of the VBox.
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
        ComboBox<Grouping> _groupings = new ComboBox<>();
        _groupings.setMinWidth(Region.USE_PREF_SIZE);
        _groupings.setPadding(new Insets(0));
        _groupings.setStyle("-fx-background-color: transparent; -fx-padding: 0, 0, 0, 0; -fx-font-size: " + (fontSize.doubleValue()) + " };");
        double maxHeight = Misc.calculateNodeSize(_groupings).getHeight();


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
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setPadding(new Insets(1));
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case ITEM_NAME -> {
                        label = new Label();
                        label.textProperty().bind(((DSMItem) item.getValue()).getName());
                        label.setPadding(new Insets(0, 5, 0, 5));
                        cell.setAlignment(Pos.CENTER_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                        int finalC = c;
                        cell.setOnMouseClicked(e -> {
                            if (e.getButton().equals(MouseButton.PRIMARY)) {
                                editItemName(((DSMItem) item.getValue()).getUid());
                                grid.resizeColumn(finalC);
                                grid.updateGrid();
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
                                grid.updateGrid();
                            }
                        });
                        cell.setMinWidth(maxHeight);  // set a min width so that the matrix is less boxy (all connection items will follow this even if not
                        // explicitly set due to how the freeze grid is set up)
                    }
                    case GROUPING_ITEM -> {  // dropdown box for choosing group
                        ComboBox<Grouping> groupings = new ComboBox<>();
                        groupings.setMinWidth(Region.USE_PREF_SIZE);
                        groupings.setPadding(new Insets(0));
                        groupings.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(3), new Insets(0))));

                        Callback<ListView<Grouping>, ListCell<Grouping>> groupingItemCellFactory = getGroupingDropDownFactory(groupings);
                        groupings.setCellFactory(groupingItemCellFactory);
                        groupings.setButtonCell(groupingItemCellFactory.call(null));

                        groupings.getItems().addAll(matrix.getGroupings(true));
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
                    case GROUPING_ITEM_V -> {  // dropdown box for choosing group
                        ComboBox<Grouping> groupings = new ComboBox<>();
                        groupings.setMinWidth(Region.USE_PREF_SIZE);
                        groupings.setPadding(new Insets(0));
                        groupings.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(3), new Insets(0))));
                        groupings.setRotate(-90);

                        Callback<ListView<Grouping>, ListCell<Grouping>> groupingItemCellFactory = getGroupingDropDownFactory(groupings);
                        groupings.setCellFactory(groupingItemCellFactory);
                        groupings.setButtonCell(groupingItemCellFactory.call(null));

                        groupings.getItems().addAll(matrix.getGroupings(false));
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
                    case INDEX_ITEM -> {
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
                    case EDITABLE_CONNECTION -> {
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

        ArrayList<Integer> importantRows = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        ArrayList<Integer> importantCols = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        grid.resizeGrid(true, importantRows, importantCols);
        grid.updateGrid();

        rootLayout.getChildren().addAll(grid.getGrid(), locationLabel);
    }


    /**
     * sets the current rootLayout to an immutable view
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
                HBox cell = new HBox();  // wrap everything in an HBox so a border can be added easily
                Label label = null;

                Background defaultBackground = DEFAULT_BACKGROUND;

                switch (item.getKey()) {
                    case PLAIN_TEXT -> {
                        label = new Label((String) item.getValue());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case PLAIN_TEXT_V -> {
                        label = new Label((String) item.getValue());
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case ITEM_NAME -> {
                        label = new Label(((DSMItem) item.getValue()).getName().getValue());
                        label.setPadding(new Insets(0, 5, 0, 5));
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case ITEM_NAME_V -> {
                        label = new Label(((DSMItem) item.getValue()).getName().getValue());
                        label.setPadding(new Insets(0, 5, 0, 5));
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical

                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case GROUPING_ITEM -> {
                        label = new Label(((DSMItem) item.getValue()).getGroup1().getName());
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case GROUPING_ITEM_V -> {
                        label = new Label(((DSMItem) item.getValue()).getGroup1().getName());
                        label.setRotate(-90);
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        Group g = new Group();  // label will be added to a group so that it will be formatted correctly if it is vertical
                        g.getChildren().add(label);
                        cell.getChildren().add(g);
                    }
                    case INDEX_ITEM -> {
                        label = new Label(String.valueOf(((DSMItem) item.getValue()).getSortIndex()));
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.getChildren().add(label);
                    }
                    case EDITABLE_CONNECTION -> {
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

        // Start by calculating the width of column 1 and height of row 1 (groupings)
        ArrayList<Grouping> allGroupings = new ArrayList<>(matrix.getGroupings(true));
        allGroupings.addAll(matrix.getGroupings(false));
        String longestGroupingName = allGroupings.stream().max(Comparator.comparing(g -> g.getName().length())).orElse(new Grouping("", Color.BLACK)).getName();
        longestGroupingName = longestGroupingName.length() > "Groupings".length() ? longestGroupingName: "Groupings";
        testLabel.setText(longestGroupingName);
        double col1Width = Misc.calculateNodeSize(testCell).getWidth();
        double row1Height = col1Width;

        // calculate width of column 2 and height of row 2 (item names)
        ArrayList<DSMItem> items = new ArrayList<>();
        items.addAll(matrix.getRows());
        items.addAll(matrix.getCols());
        items.add(new DSMItem(-1.0, "Column Items"));
        String longestItemName = items.stream().max(Comparator.comparing((DSMItem item) -> item.getName().toString().length())).orElse(new DSMItem(-1.0, "")).getName().getValue();
        testLabel.setText(longestItemName);
        double col2Width = Misc.calculateNodeSize(testCell).getWidth();
        double row2Height = col2Width;

        // calculate width of column 3
        testLabel.setText("Re-Sort Index");
        double col3Width = Misc.calculateNodeSize(testCell).getWidth();

        // calculate height of row 3 and connection cell size
        testLabel.setText("x");
        double row3Height = Misc.calculateNodeSize(testCell).getHeight();
        double cellSize = row3Height;


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

        // update the cells so they can be displayed by the freeze grid
        for(int r=0; r<numRows; r++) {
            ArrayList<HBox> rowData = new ArrayList<>();
            for(int c=0; c<numCols; c++) {
                Pair<RenderMode, Object> item = template.get(r).get(c);
                HBox cell = new HBox();  // wrap everything in an HBox so a border can be added easily
                Label label = null;

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
                    case GROUPING_ITEM_V -> {  // dropdown box for choosing group
                        Grouping group = ((DSMItem) item.getValue()).getGroup1();
                        label = new Label(group.getName());
                        label.setRotate(-90);
                        label.setPadding(new Insets(0, 5, 0, 5));
                        label.setTextFill(group.getFontColor());
                        label.setMinWidth(Region.USE_PREF_SIZE);
                        cell.setAlignment(Pos.CENTER);
                        cell.getChildren().add(label);

                        // set a width so that the matrix is less boxy (all connection items will follow this even if not
                        // explicitly set due to how the freeze grid is set up)
                        cell.setMinWidth(cellSize);
                        cell.setMaxWidth(cellSize);
                        cell.setPrefWidth(cellSize);
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
                    case EDITABLE_CONNECTION -> {
                        if(connectionsRow == -1) {  // if not set, this will be the first connection cell so set it here
                            connectionsRow = r;
                            connectionsCol = c;
                        }

                        int rowUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getKey().getUid();
                        int colUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getValue().getUid();
                        DSMConnection conn = matrix.getConnection(rowUid, colUid);
                        if(conn != null) {  // only add connections that exist
                            Color color = Color.BLACK;  // default to black
                            if(matrix.getRowItem(rowUid).getGroup1().equals(matrix.getColItem(colUid).getGroup1())) {
                                color = matrix.getRowItem(rowUid).getGroup1().getFontColor();
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
                    cellObject.updateHighlightBG(DEFAULT_BACKGROUND, "default");
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
                // upper left triangle will be row color
                graphics_context.setFill(rowItem.getGroup1().getColor());
                double[] leftXCoordinates = {x, x + cellSize, x};
                double[] leftYCoordinates = {y, y, y + cellSize};
                graphics_context.fillPolygon(leftXCoordinates, leftYCoordinates, 3);

                // lower right triangle will be column color
                graphics_context.setFill(colItem.getGroup1().getColor());
                double[] rightXCoordinates = {x, x + cellSize, x + cellSize};
                double[] rightYCoordinates = {y + cellSize, y + cellSize, y};
                graphics_context.fillPolygon(rightXCoordinates, rightYCoordinates, 3);

                x += cellSize;
            }
            x = 0;  // reset to first column at start of canvas
            y += cellSize;  // move to next row
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

        // set the span of the canvas item
        grid.setCellSpan(connectionsRow, connectionsCol, matrix.getRows().size(), matrix.getCols().size());

        // set up the freezing
        grid.setFreezeLeft(3);
        grid.setFreezeHeader(3);

        // manually size all the items to save time and be more accurate
        grid.setRowPrefHeight(0, row1Height);
        grid.setRowPrefHeight(1, row2Height);
        grid.setRowPrefHeight(2, row3Height);
        grid.setColPrefWidth(0, col1Width);
        grid.setColPrefWidth(1, col2Width);
        grid.setColPrefWidth(2, col3Width);

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
