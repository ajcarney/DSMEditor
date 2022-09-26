package UI.Widgets;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collection;


/**
 * A class for viewing data in a grid with the ability to freeze rows or columns along the outside of the table
 */
public class FreezeGrid {
    /**
     * A data class used to represent a cell in the freeze grid
     */
    private static class FreezeGridCell {
        private final Pair<Integer, Integer> gridLocation;
        private final HBox cell;
        private int rowSpan;
        private int colSpan;


        /**
         * Constructor
         *
         * @param gridLocation location of (row, column) of the cell in the grid
         * @param cell         the HBox object of the cell
         */
        public FreezeGridCell(Pair<Integer, Integer> gridLocation, HBox cell) {
            this.gridLocation = gridLocation;
            this.cell = cell;
            this.rowSpan = 1;  // default to 1
            this.colSpan = 1;
        }


        /**
         * Constructor
         *
         * @param gridLocation location of (row, column) of the cell in the grid
         * @param cell         the HBox object of the cell
         * @param rowSpan      the number of rows to span
         * @param colSpan      the number of columns to span
         */
        public FreezeGridCell(Pair<Integer, Integer> gridLocation, HBox cell, int rowSpan, int colSpan) {
            this.gridLocation = gridLocation;
            this.cell = cell;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
        }


        /**
         * Returns the HBox object
         *
         * @return HBox of the cell
         */
        public HBox getNode() {
            return cell;
        }


        /**
         * Returns the coordinates of the cell
         *
         * @return pair of (row, column) of the cell
         */
        public Pair<Integer, Integer> getGridLocation() {
            return gridLocation;
        }


        /**
         * @return  the number of rows to span
         */
        public int getRowSpan() {
            return rowSpan;
        }


        /**
         * sets the number of rows to span
         *
         * @param rowSpan  the number of rows to span
         */
        public void setRowSpan(int rowSpan) {
            this.rowSpan = rowSpan;
        }


        /**
         * @return  the number of columns to span
         */
        public int getColSpan() {
            return colSpan;
        }


        /**
         * sets the number of columns to span
         *
         * @param colSpan  the number of columns to span
         */
        public void setColSpan(int colSpan) {
            this.colSpan = colSpan;
        }
    }

    // { {start row, start col}, {end row, end col} }
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> nwGridConstraints;
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> neGridConstraints;
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> swGridConstraints;
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> seGridConstraints;

    private final ArrayList<ArrayList<FreezeGridCell>> cells;
    private final ArrayList<DoubleProperty> colPrefWidths;
    private final ArrayList<DoubleProperty> rowPrefHeights;

    private final ScrollBar xScroll = new ScrollBar();
    private final ScrollBar yScroll = new ScrollBar();

    private final BorderPane grid;


    /**
     * Constructor, does not add any data to the grid
     */
    public FreezeGrid() {
        grid = new BorderPane();

        cells = new ArrayList<>();
        colPrefWidths = new ArrayList<>();
        rowPrefHeights = new ArrayList<>();

        xScroll.setOrientation(Orientation.HORIZONTAL);
        yScroll.setOrientation(Orientation.VERTICAL);
    }


    //region getters
    /**
     * returns the number of rows in the dataset
     *
     * @return int of number of rows
     */
    public int getNumRows() {
        return cells.size();
    }


    /**
     * returns the number of columns in the dataset
     *
     * @return int of number of columns
     */
    public int getNumCols() {
        return cells.get(0).size();
    }


    /**
     * Returns a cell at a given index or null if that index does not exist
     *
     * @param r  the row index of the cell
     * @param c  the column index of the cell
     * @return   the cell if it exists or null
     */
    private FreezeGridCell getCellOrDefault(int r, int c) {
        if(cells.isEmpty()) return null;
        if(r >= cells.size()) return null;
        if(c >= cells.get(r).size()) return null;

        return cells.get(r).get(c);
    }
    //endregion


    /**
     * Re-calculates the pref widths and heights of the grid
     *
     * @param addToDummy     if all nodes need to be added to a dummy scene so that the size can be accurately calculated
     *                       set to true if the grid has not been added to a scene yet
     * @param importantRows  the rows to factor into the pref sizing (used to cheat if there are a lot of nodes to size
     *                       so that they do not all have to be added to a scene and sized because that is really slow).
     *                       Leave empty to factor in all rows. Ignored if items are already on a scene
     * @param importantCols  the cols to factor into the pref sizing (used to cheat if there are a lot of nodes to size
     *                       so that they do not all have to be added to a scene and sized because that is really slow).
     *                       Leave empty to factor in all cols. Ignored if items are already on a scene
     *
     */
    public void resizeGrid(boolean addToDummy, Collection<Integer> importantRows, Collection<Integer> importantCols) {
        if(cells.isEmpty() || cells.get(0).isEmpty()) {
            return;
        }
        colPrefWidths.clear();
        rowPrefHeights.clear();

        if(importantRows.isEmpty()) {
            for(int r=0; r<cells.size(); r++) {
                importantRows.add(r);
            }
        }
        if(importantCols.isEmpty()) {
            for(int c=0; c<cells.get(0).size(); c++) {
                importantRows.add(c);
            }
        }

        // optionally allow adding to scene because this is expensive and may mess up nodes parents if update grid is
        // not called afterwards
        if(addToDummy) {
            // a dummy scene is needed to calculate preferred sizes of nodes
            StackPane ghostPane = new StackPane();
            Scene ghostScene = new Scene(ghostPane);

            // add all cells to the node for sizing
            ArrayList<HBox> flatCells = new ArrayList<>();
            for(int r=0; r<cells.size(); r++) {
                for(int c=0; c<cells.get(r).size(); c++) {
                    if(cells.get(r).get(c).getNode() != null && (importantRows.contains(r) || importantCols.contains(c))) {
                        flatCells.add(cells.get(r).get(c).getNode());
                    }
                }
            }

            ghostPane.getChildren().addAll(flatCells);
            ghostPane.applyCss();
            ghostPane.layout();
        }

        for(int r=0; r<cells.size(); r++) {
            if(r >= rowPrefHeights.size()) {
                rowPrefHeights.add(new SimpleDoubleProperty(0.0));
            }

            for (int c = 0; c < cells.get(r).size(); c++) {
                if(c >= colPrefWidths.size()) {
                    colPrefWidths.add(new SimpleDoubleProperty(0.0));
                }

                if(cells.get(r).get(c).getNode() == null) {  // skip cases where node doesn't exist
                    continue;
                }

                Bounds bounds = cells.get(r).get(c).getNode().getBoundsInLocal();
                double width = bounds.getWidth();
                double height = bounds.getHeight();
                if(cells.get(r).get(c).getColSpan() == 1 && width > colPrefWidths.get(c).doubleValue()) {  // only update pref width if col span is 1
                    colPrefWidths.get(c).set(width);
                }
                if(cells.get(r).get(c).getRowSpan() == 1 && height > rowPrefHeights.get(r).doubleValue()) {  // only update pref height if row span is 1
                    rowPrefHeights.get(r).set(height);
                }
            }
        }

    }


    /**
     * Updates the size of a column based on its index. Resizes the whole grid if any cell nodes are null
     *
     * @param index    the index of the column starting from 0
     */
    public void resizeColumn(int index) {
        if(cells.isEmpty()) {
            return;
        }

        // perform search to ensure no null nodes because if there are this means a multi-span cell
        // needs resizing and therefore the whole grid should be resized out of an abundance of caution
        boolean nulls = false;
        for (ArrayList<FreezeGridCell> cellRowList : cells) {
            if (cellRowList.get(index).getNode() == null) {
                nulls = true;
                break;
            }
        }

        if(nulls) {
            resizeGrid(false, new ArrayList<>(), new ArrayList<>());  // node should already be laid out. Why else would anyone resize an individual column?
        } else {
            colPrefWidths.get(index).set(0);  // set to 0 so that real maximum can be found
            // update the pref widths array so that sizes can be set accurately
            for (ArrayList<FreezeGridCell> cell : cells) {
                cell.get(index).getNode().resize(0, 0);  // resizing to nothing ensures that it will find the min width
                cell.get(index).getNode().layout();  // layout so that bounds are accurate
                double width = cell.get(index).getNode().getBoundsInLocal().getWidth();  // calculate the width
                if (width > colPrefWidths.get(index).doubleValue()) {
                    colPrefWidths.get(index).set(width);
                }
            }

            // update all the sizes
            for(int r=0; r<cells.size(); r++) {
                double width = getCellPrefWidth(r, index);
                double height = getCellPrefHeight(r, index);
                HBox node = cells.get(r).get(index).getNode();
                if(node != null) {
                    node.setMinSize(width, height);
                    node.setMaxSize(width, height);
                }
            }
        }
    }


    /**
     * Updates the size of a row based on its index. Resizes the whole grid if any cell nodes are null
     *
     * @param index    the index of the row
     */
    public void resizeRow(int index) {
        if(cells.isEmpty()) {
            return;
        }

        // perform search to ensure no null nodes because if there are this means a multi-span cell
        // needs resizing and therefore the whole grid should be resized out of an abundance of caution
        boolean nulls = false;
        for (FreezeGridCell cell : cells.get(index)) {
            if (cell.getNode() == null) {
                nulls = true;
                break;
            }
        }
        if(nulls) {
            resizeGrid(false, new ArrayList<>(), new ArrayList<>());  // node should already be laid out. Why else would anyone resize an individual column?
        } else {
            rowPrefHeights.get(index).set(0);  // set to 0 so that real maximum can be found
            // update the pref widths array so that sizes can be set accurately
            for(int c=0; c<cells.get(index).size(); c++) {
                cells.get(index).get(c).getNode().resize(0, 0);  // resizing to nothing ensures that it will find the actual min height
                cells.get(index).get(c).getNode().layout();  // layout again so that bounds are accurate
                double height = cells.get(index).get(c).getNode().getBoundsInLocal().getHeight();  // calculate the height
                if (height > rowPrefHeights.get(index).doubleValue()) {
                    rowPrefHeights.get(index).set(height);
                }
            }

            // update all the sizes
            for(int c=0; c<cells.get(index).size(); c++) {  // update the sizes
                double width = getCellPrefWidth(index, c);
                double height = getCellPrefHeight(index, c);
                HBox node = cells.get(index).get(c).getNode();
                if(node != null) {
                    node.setMinSize(width, height);
                    node.setMaxSize(width, height);
                }
            }
        }

    }


    /**
     * Sets the span of a cell in the cells data array
     *
     * @param r        the cell row index
     * @param c        the cell column index
     * @param rowSpan  the new row span
     * @param colSpan  the new col span
     */
    public void setCellSpan(int r, int c, int rowSpan, int colSpan) {
        if(cells.isEmpty()) {
            return;
        }
        cells.get(r).get(c).setRowSpan(rowSpan);
        cells.get(r).get(c).setColSpan(colSpan);
    }


    /**
     * Gets a cells pref width based on the calculated pref widths. Factors in the cell width by adding the width
     * of the next column if the span is greater than one
     *
     * @param r  the row index of the cell
     * @param c  the col index of the cell
     * @return  the preferred width of the cell
     */
    public double getCellPrefWidth(int r, int c) {
        if(getCellOrDefault(r, c) == null || cells.get(r).get(c).getNode() == null) {
            return 0;
        }

        double width = 0;
        for(int i=c; i<(c + cells.get(r).get(c).getColSpan()); i++) {  // iterate from start column to column of the column span
            try {
                width += colPrefWidths.get(i).doubleValue();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return width;
    }


    /**
     * Overrides a given column preferred width at a given index
     *
     * @param index         the index of the column to update the preferred width of
     * @param newPrefWidth  the new preferred width for items in that column
     */
    public void setColPrefWidth(int index, double newPrefWidth) {
        if(index < colPrefWidths.size()) {
            colPrefWidths.get(index).set(newPrefWidth);
        }
    }


    /**
     * Overrides a given row preferred height at a given index
     *
     * @param index          the index of the row to update the preferred height of
     * @param newPrefHeight  the new preferred height for items in that row
     */
    public void setRowPrefHeight(int index, double newPrefHeight) {
        if(index < rowPrefHeights.size()) {
            rowPrefHeights.get(index).set(newPrefHeight);
        }
    }


    /**
     * Gets a cells pref height based on the calculated pref heights. Factors in the cell height by adding the height
     * of the next row if the span is greater than one
     *
     * @param r  the row index of the cell
     * @param c  the col index of the cell
     * @return  the preferred height of the cell
     */
    public double getCellPrefHeight(int r, int c) {
        if(getCellOrDefault(r, c) == null || cells.get(r).get(c).getNode() == null) {
            return 0;
        }

        double height = 0;
        for(int i=r; i<(r + cells.get(r).get(c).getRowSpan()); i++) {  // iterate from start column to column of the column span
            height += rowPrefHeights.get(i).doubleValue();
        }

        return height;
    }


    /**
     * sets the grid data based on strings by creating labels in an HBox
     *
     * @param data 2d array list type string of the data
     */
    public void setGridDataString(ArrayList<ArrayList<String>> data) {
        cells.clear();
        colPrefWidths.clear();
        rowPrefHeights.clear();

        for(int r=0; r<data.size(); r++) {
            ArrayList<String> row = data.get(r);
            ArrayList<FreezeGridCell> newRow = new ArrayList<>();
            for(int c=0; c<row.size(); c++) {
                HBox box = new HBox();
                Label l = new Label(row.get(c));
                l.setMinSize(50, 30);
                box.getChildren().add(l);
                box.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                HBox.setHgrow(box, Priority.ALWAYS);
                if(row.get(c).equals("")) {
                    box = null;
                }
                FreezeGridCell cell = new FreezeGridCell(new Pair<>(r, c), box);
                newRow.add(cell);
            }

            cells.add(newRow);

            assert r == 0 || (newRow.size() == cells.get(0).size());
        }

        // initialize pref width arrays
        for(int r=0; r<cells.size(); r++) {
            rowPrefHeights.add(new SimpleDoubleProperty(0.0));
        }

        for (int c = 0; c < cells.get(0).size(); c++) {
            colPrefWidths.add(new SimpleDoubleProperty(0.0));
        }

        updateConstraints();
    }


    /**
     * sets the grid data based on array list of type HBox
     *
     * @param data 2d array list type HBox of the data
     */
    public void setGridDataHBox(ArrayList<ArrayList<HBox>> data) {
        cells.clear();
        colPrefWidths.clear();
        rowPrefHeights.clear();

        for(int r=0; r<data.size(); r++) {
            ArrayList<HBox> row = data.get(r);
            ArrayList<FreezeGridCell> newRow = new ArrayList<>();
            for(int c=0; c<row.size(); c++) {
                FreezeGridCell cell = new FreezeGridCell(new Pair<>(r, c), row.get(c));
                newRow.add(cell);
            }

            cells.add(newRow);

            assert r == 0 || (newRow.size() == cells.get(0).size());
        }


        // initialize pref width arrays
        for(int r=0; r<cells.size(); r++) {
            rowPrefHeights.add(new SimpleDoubleProperty(0.0));
        }

        for (int c = 0; c < cells.get(0).size(); c++) {
            colPrefWidths.add(new SimpleDoubleProperty(0.0));
        }

        updateConstraints();
    }


    //region set freeze rows/columns
    /**
     * creates default constraints for the grid
     */
    private void updateConstraints() {
        nwGridConstraints = new Pair<>(new Pair<>(0, 0), new Pair<>(0, 0));
        neGridConstraints = new Pair<>(new Pair<>(getNumCols(), 0), new Pair<>(getNumCols(), 0));
        swGridConstraints = new Pair<>(new Pair<>(0, getNumRows()), new Pair<>(0, getNumRows()));
        seGridConstraints = new Pair<>(new Pair<>(getNumCols(), getNumRows()), new Pair<>(getNumCols(), getNumRows()));
    }


    /**
     * Freezes a header until endRow (1 indexed, endpoint inclusive)
     *
     * @param endRow the row to freeze to
     */
    public void setFreezeHeader(int endRow) {
        // set nw end location
        nwGridConstraints = new Pair<>(new Pair<>(nwGridConstraints.getKey().getKey(), nwGridConstraints.getKey().getValue()), new Pair<>(nwGridConstraints.getValue().getKey(), endRow));

        // set ne end location
        neGridConstraints = new Pair<>(new Pair<>(neGridConstraints.getKey().getKey(), neGridConstraints.getKey().getValue()), new Pair<>(neGridConstraints.getValue().getKey(), endRow));
    }


    /**
     * Freezes a footer until endRow (1 indexed, endpoint inclusive)
     *
     * @param endRow the row to freeze to
     */
    public void setFreezeFooter(int endRow) {
        // set sw start location
        swGridConstraints = new Pair<>(new Pair<>(swGridConstraints.getKey().getKey(), getNumRows() - endRow), new Pair<>(swGridConstraints.getValue().getKey(), swGridConstraints.getValue().getValue()));

        // set se start location
        seGridConstraints = new Pair<>(new Pair<>(seGridConstraints.getKey().getKey(), getNumRows() - endRow), new Pair<>(seGridConstraints.getValue().getKey(), seGridConstraints.getValue().getValue()));
    }


    /**
     * Freezes a column until endCol (1 indexed, endpoint inclusive)
     *
     * @param endCol the column to freeze to
     */
    public void setFreezeLeft(int endCol) {
        // set nw end location
        nwGridConstraints = new Pair<>(new Pair<>(nwGridConstraints.getKey().getKey(), nwGridConstraints.getKey().getValue()), new Pair<>(endCol, nwGridConstraints.getValue().getValue()));

        // set sw end location
        swGridConstraints = new Pair<>(new Pair<>(swGridConstraints.getKey().getKey(), swGridConstraints.getKey().getValue()), new Pair<>(endCol, swGridConstraints.getValue().getValue()));
    }


    /**
     * Freezes a column until endCol (1 indexed, endpoint inclusive)
     *
     * @param endCol the column to freeze to
     */
    public void setFreezeRight(int endCol) {
        // set ne start location
        neGridConstraints = new Pair<>(new Pair<>(getNumCols() - endCol, neGridConstraints.getKey().getValue()), new Pair<>(neGridConstraints.getValue().getKey(), neGridConstraints.getValue().getValue()));

        // set se start location
        seGridConstraints = new Pair<>(new Pair<>(getNumCols() - endCol, seGridConstraints.getKey().getValue()), new Pair<>(seGridConstraints.getValue().getKey(), seGridConstraints.getValue().getValue()));
    }
    //endregion


    /**
     * Creates a section of the grid based on the start and end x, y coordinates.
     *
     * @param startX start x index
     * @param startY start y index
     * @param endX   end x index
     * @param endY   end y index
     * @return       VBox of the box
     */
    private GridPane createCornerBox(int startX, int startY, int endX, int endY) {
        GridPane box = new GridPane();
        for(int r=startX; r<endX; r++) {
            ArrayList<FreezeGridCell> rowData = cells.get(r);
            if(rowData == null) {
                continue;
            }

            //HBox row = new HBox();
            for(int c=startY; c<endY; c++) {
                FreezeGridCell cell = rowData.get(c);
                if(cell.getNode() == null) {
                    continue;
                }

                box.add(cell.getNode(), c, r);
                GridPane.setColumnSpan(cell.getNode(), cell.getColSpan());
                GridPane.setRowSpan(cell.getNode(), cell.getRowSpan());
            }

        }

        return box;
    }


    /**
     * recreates the grid object. Does not create a new object, but updates the children
     */
    public void updateGrid() {
        if(cells.isEmpty()) {
            return;
        }
        grid.getChildren().clear();

        HBox nBox = new HBox();
        HBox sBox = new HBox();


        // create NW box
        GridPane nwBox = createCornerBox(nwGridConstraints.getKey().getKey(), nwGridConstraints.getKey().getValue(), nwGridConstraints.getValue().getValue(), nwGridConstraints.getValue().getKey());
        if(!nwBox.getChildren().isEmpty()) {
            nBox.getChildren().add(nwBox);
        }

        // create NN box
        GridPane nnBox = createCornerBox(
            nwGridConstraints.getKey().getValue(),    // nw start y
            nwGridConstraints.getValue().getKey(),    // nw end x
            neGridConstraints.getValue().getValue(),  // ne end y
            neGridConstraints.getKey().getKey()       // ne start x
        );

        ScrollPane scrollNBox = new ScrollPane(nnBox);  // configure scroll pane later
        if(!nnBox.getChildren().isEmpty()) {
            nBox.getChildren().add(scrollNBox);
        }

        // create NE box
        GridPane neBox = createCornerBox(neGridConstraints.getKey().getValue(), neGridConstraints.getKey().getKey(), neGridConstraints.getValue().getValue(), neGridConstraints.getValue().getKey());
        if(!neBox.getChildren().isEmpty()) {
            nBox.getChildren().add(neBox);
        }

        // create WW box
        GridPane wBox = createCornerBox(
            nwGridConstraints.getValue().getValue(),  // nw end y
            nwGridConstraints.getKey().getKey(),      // nw start x
            swGridConstraints.getKey().getValue(),    // sw start y
            swGridConstraints.getValue().getKey()     // sw end x
        );
        ScrollPane scrollWBox = new ScrollPane(wBox);  // configure scroll pane later

        // create center box
        GridPane cBox = createCornerBox(
                nwGridConstraints.getValue().getValue(),  // nw end y
                nwGridConstraints.getValue().getKey(),    // nw end x
                seGridConstraints.getKey().getValue(),    // se start y
                seGridConstraints.getKey().getKey()       // se start x
        );
        ScrollPane scrollCBox = new ScrollPane(cBox);  // configure scroll pane later


        // create SW box
        GridPane swBox = createCornerBox(swGridConstraints.getKey().getValue(), swGridConstraints.getKey().getKey(), swGridConstraints.getValue().getValue(), swGridConstraints.getValue().getKey());

        if(!swBox.getChildren().isEmpty()) {
            sBox.getChildren().add(swBox);
        }

        // create SS box
        GridPane ssBox = createCornerBox(
            swGridConstraints.getKey().getValue(),    // sw start y
            swGridConstraints.getValue().getKey(),    // sw end x
            seGridConstraints.getValue().getValue(),  // se end y
            seGridConstraints.getKey().getKey()       // se start x
        );

        ScrollPane scrollSBox = new ScrollPane(ssBox);  // configure scroll pane later
        if(!ssBox.getChildren().isEmpty()) {
            sBox.getChildren().add(scrollSBox);
        }

        // create SE box
        GridPane seBox = createCornerBox(seGridConstraints.getKey().getValue(), seGridConstraints.getKey().getKey(), seGridConstraints.getValue().getValue(), seGridConstraints.getValue().getKey());
        if(!seBox.getChildren().isEmpty()) {
            sBox.getChildren().add(seBox);
        }

        // create EE box
        GridPane eBox = createCornerBox(
            neGridConstraints.getValue().getValue(),  // ne end y
            neGridConstraints.getKey().getKey(),      // ne start x
            seGridConstraints.getKey().getValue(),    // se start y
            seGridConstraints.getValue().getKey()     // se end x
        );
        ScrollPane scrollEBox = new ScrollPane(eBox);  // configure scroll pane later


        // set cell pref sizes to row/col sizes
        assert rowPrefHeights.size() == cells.size();
        assert colPrefWidths.size() == cells.get(0).size();  // no need to perform null check on get() (0 size checked for earlier)
        for (int r=0; r<cells.size(); r++) {
            ArrayList<FreezeGridCell> row = cells.get(r);
            for (int c = 0; c < row.size(); c++) {
                FreezeGridCell cell = row.get(c);
                if(cell.getNode() == null) {
                    continue;
                }
                cell.getNode().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                cell.getNode().setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                cell.getNode().setPrefSize(getCellPrefWidth(r, c), getCellPrefHeight(r, c));
            }
        }

        // configure scroll panes
        // disable individual scroll bars because there is a global one for all the
        // scroll panes.
        scrollNBox.hvalueProperty().bindBidirectional(scrollCBox.hvalueProperty());
        scrollNBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollNBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollNBox.setPannable(true);

        scrollSBox.hvalueProperty().bindBidirectional(scrollCBox.hvalueProperty());
        scrollSBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollSBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollSBox.setPannable(true);

        scrollWBox.vvalueProperty().bindBidirectional(scrollCBox.vvalueProperty());
        scrollWBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollWBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollWBox.setPannable(true);

        scrollEBox.vvalueProperty().bindBidirectional(scrollCBox.vvalueProperty());
        scrollEBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollEBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollEBox.setPannable(true);

        scrollCBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollCBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollCBox.setPannable(true);


        // add boxes to border pane
        if(!nBox.getChildren().isEmpty()) {
            grid.setTop(nBox);
            BorderPane.setMargin(nBox, new Insets(-1));
        }
        if(!wBox.getChildren().isEmpty()) {
            grid.setLeft(scrollWBox);
            BorderPane.setMargin(scrollWBox, new Insets(-1));
        }
        if(!eBox.getChildren().isEmpty()) {
            grid.setRight(scrollEBox);
            BorderPane.setMargin(scrollEBox, new Insets(-1));
        }
        if(!sBox.getChildren().isEmpty()) {
            grid.setBottom(sBox);
            BorderPane.setMargin(sBox, new Insets(-1));
        }
        grid.setCenter(scrollCBox);
        BorderPane.setMargin(scrollCBox, new Insets(-1));

        grid.setStyle(grid.getStyle() + "-fx-border-width: -1; -fx-box-border: transparent; -fx-border-style: none;");

        // set up scroll bindings
        xScroll.minProperty().bindBidirectional(scrollCBox.hminProperty());
        xScroll.maxProperty().bindBidirectional(scrollCBox.hmaxProperty());
        scrollCBox.hvalueProperty().bindBidirectional(xScroll.valueProperty());
        scrollSBox.hvalueProperty().bindBidirectional(xScroll.valueProperty());
        scrollNBox.hvalueProperty().bindBidirectional(xScroll.valueProperty());

        yScroll.minProperty().bindBidirectional(scrollCBox.vminProperty());
        yScroll.maxProperty().bindBidirectional(scrollCBox.vmaxProperty());
        scrollCBox.vvalueProperty().bindBidirectional(yScroll.valueProperty());
        scrollEBox.vvalueProperty().bindBidirectional(yScroll.valueProperty());
        scrollWBox.vvalueProperty().bindBidirectional(yScroll.valueProperty());

    }


    /**
     * Returns the grid object
     *
     * @return BorderPane of the grid
     */
    public HBox getGrid() {
        // create hbox for adding the vertical scroll bar and a vbox for adding the horizontal scroll bar
        HBox yScrollPane = new HBox();
        VBox xScrollPane = new VBox();
        xScrollPane.getChildren().addAll(grid, xScroll);
        yScrollPane.getChildren().addAll(xScrollPane, yScroll);
        yScrollPane.setAlignment(Pos.CENTER);
        return yScrollPane;
    }


    /**
     * Sets a FreezeGrid back to its initial state
     */
    public void clear() {
        cells.clear();
        colPrefWidths.clear();
        rowPrefHeights.clear();
        nwGridConstraints = null;
        neGridConstraints = null;
        swGridConstraints = null;
        seGridConstraints = null;
        grid.getChildren().clear();
    }


    /**
     * A function to help debug the grid. Opens its own window so can be called as is
     */
    public static void debug() {
        FreezeGrid grid = new FreezeGrid();
        ArrayList<ArrayList<String>> data = new ArrayList<>();
        for(int r=0; r < 50; r++) {
            ArrayList<String> row = new ArrayList<>();
            for(int c=0; c < 50; c++) {
                row.add("(" + r + ", " + c + ")");
            }
            data.add(row);
        }
        grid.setGridDataString(data);
        grid.setFreezeHeader(2);
        grid.setFreezeLeft(2);
        grid.setFreezeRight(2);
        grid.setFreezeFooter(2);
        grid.updateGrid();

        Stage window;
        window = new Stage();
        VBox rootLayout = new VBox();

        rootLayout.getChildren().add(grid.getGrid());

        Scene scene = new Scene(rootLayout, 1200, 800);
        window.setScene(scene);
        window.show();

    }


    /**
     * A function to help debug the grid. Opens its own window so can be called as is. Useful for debugging spans
     */
    public static void debug2() {
        FreezeGrid grid = new FreezeGrid();
        ArrayList<ArrayList<String>> data = new ArrayList<>();

        int width = 50;

        ArrayList<String> row0 = new ArrayList<>();
        for(int c=0; c < width; c++) {
            row0.add("(" + 1 + ", " + c + ")");
        }

        ArrayList<String> row1 = new ArrayList<>();
        for(int c=0; c < width; c++) {  // row span of cell (1, 2) is col span 3 and row span 2
            if(c >= 3 && c < 5) {
                row1.add("");
            } else {
                row1.add("(" + 2 + ", " + c + ")");
            }
        }

        ArrayList<String> row2 = new ArrayList<>();
        for(int c=0; c < width; c++) {
            if(c >= 2 && c < 5) {
                row2.add("");
            } else {
                row2.add("(" + 3 + ", " + c + ")");
            }
        }

        ArrayList<String> row3 = new ArrayList<>();
        for(int c=0; c < width; c++) {
            row3.add("(" + 4 + ", " + c + ")");
        }

        data.add(row0);
        data.add(row1);
        data.add(row2);
        data.add(row3);
        for(int i=5; i<50; i++) {
            ArrayList<String> rowN = new ArrayList<>();
            for(int c=0; c < width; c++) {
                rowN.add("(" + i + ", " + c + ")");
            }
            data.add(rowN);
        }

        Stage window;
        window = new Stage();
        VBox rootLayout = new VBox();

        rootLayout.getChildren().add(grid.getGrid());

        Scene scene = new Scene(rootLayout, 1200, 800);
        window.setScene(scene);

        grid.setGridDataString(data);
        grid.setFreezeHeader(4);
        grid.setFreezeLeft(1);
        grid.setFreezeRight(1);
        grid.setFreezeFooter(2);
        grid.setCellSpan(1, 2, 2, 3);
        grid.updateGrid();

        window.show();

    }


}
