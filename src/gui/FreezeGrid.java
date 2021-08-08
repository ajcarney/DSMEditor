package gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * A class for viewing data in a grid with the ability to freeze rows or columns along the outside of the table
 */
public class FreezeGrid {
    /**
     * A data class used to represent a cell in the grid
     */
    private class Cell {
        private final Pair<Integer, Integer> gridLocation;
        private final HBox cell;

        /**
         * Constructor
         *
         * @param gridLocation location of (row, column) of the cell in the grid
         * @param cell         the HBox object of the cell
         */
        public Cell(Pair<Integer, Integer> gridLocation, HBox cell) {
            this.gridLocation = gridLocation;
            this.cell = cell;
        }

        /**
         * returns the prefWidthProperty of the HBox
         *
         * @return prefWidthProperty of the HBox
         */
        public DoubleProperty getPrefWidthProperty() {
            return cell.prefWidthProperty();
        }

        /**
         * returns the prefHeightProperty of the HBox
         *
         * @return prefHeightProperty of the HBox
         */
        public DoubleProperty getPrefHeightProperty() {
            return cell.prefHeightProperty();
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
    }

    // { {start row, start col}, {end row, end col} }
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> nwGridConstraints;
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> neGridConstraints;
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> swGridConstraints;
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> seGridConstraints;

    private final ArrayList<ArrayList<Cell>> cells;
    private final ArrayList<DoubleProperty> colPrefWidths;
    private final ArrayList<DoubleProperty> rowPrefHeights;

    private final BorderPane grid;

    /**
     * Constructor, does not add any data to the grid
     */
    public FreezeGrid() {
        grid = new BorderPane();

        cells = new ArrayList<>();
        colPrefWidths = new ArrayList<>();
        rowPrefHeights = new ArrayList<>();
    }

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
     * creates default constraints for the grid
     */
    public void updateConstraints() {
        nwGridConstraints = new Pair<>(new Pair<>(0, 0), new Pair<>(0, 0));
        neGridConstraints = new Pair<>(new Pair<>(getNumCols(), 0), new Pair<>(getNumCols(), 0));
        swGridConstraints = new Pair<>(new Pair<>(0, getNumRows()), new Pair<>(0, getNumRows()));
        seGridConstraints = new Pair<>(new Pair<>(getNumCols(), getNumRows()), new Pair<>(getNumCols(), getNumRows()));
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

        Pane ghostPane = new Pane();
        Scene ghostScene = new Scene(ghostPane);  // a scene is needed to calculate preferred sizes of nodes

        for(int r=0; r<data.size(); r++) {
            ArrayList<String> row = data.get(r);
            ArrayList<Cell> newRow = new ArrayList<>();

            colPrefWidths.add(new SimpleDoubleProperty(0.0));
            rowPrefHeights.add(new SimpleDoubleProperty(0.0));
            for(int c=0; c<row.size(); c++) {
                HBox area = new HBox();
                area.getChildren().add(new Label(row.get(c)));

                Cell cell = new Cell(new Pair<>(r, c), area);
                newRow.add(cell);

                // update preferred sizes
                // add the node to a test pane with the scene set, but not visible so the preferred size gets calculated
                ghostPane.getChildren().add(area);
                ghostPane.applyCss();
                ghostPane.layout();

                if(area.getBoundsInLocal().getWidth() > colPrefWidths.get(r).doubleValue()) {
                    colPrefWidths.get(r).set(area.getBoundsInLocal().getWidth());
                }
                if(area.getBoundsInLocal().getHeight() > rowPrefHeights.get(r).doubleValue()) {
                    rowPrefHeights.get(r).set(area.getBoundsInLocal().getHeight());
                }

                ghostPane.getChildren().removeAll(ghostPane.getChildren());  // remove it so area has no parent
            }

            cells.add(newRow);

            assert r <= 0 || (newRow.size() == cells.get(0).size());
        }
        updateConstraints();
        updateGrid();
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

        Pane ghostPane = new Pane();
        Scene ghostScene = new Scene(ghostPane);  // a scene is needed to calculate preferred sizes of nodes

        for(int r=0; r<data.size(); r++) {
            ArrayList<HBox> row = data.get(r);
            ArrayList<Cell> newRow = new ArrayList<>();

            if(r >= rowPrefHeights.size()) {
                rowPrefHeights.add(new SimpleDoubleProperty(0.0));
            }
            for(int c=0; c<row.size(); c++) {
                if(c >= colPrefWidths.size()) {
                    colPrefWidths.add(new SimpleDoubleProperty(0.0));
                }
                Cell cell = new Cell(new Pair<>(r, c), row.get(c));
                newRow.add(cell);

                // update preferred sizes
                // add the node to a test pane with the scene set, but not visible so the preferred size gets calculated
                ghostPane.getChildren().add(cell.getNode());
                ghostPane.applyCss();
                ghostPane.layout();

                double width = cell.getNode().getBoundsInLocal().getWidth() + cell.getNode().getPadding().getLeft() + cell.getNode().getPadding().getRight();
                double height = cell.getNode().getBoundsInLocal().getHeight() + cell.getNode().getPadding().getTop() + cell.getNode().getPadding().getBottom();
                if(width > colPrefWidths.get(c).doubleValue()) {
                    colPrefWidths.get(c).set(width);
                }
                if(height > rowPrefHeights.get(r).doubleValue()) {
                    rowPrefHeights.get(r).set(height);
                }

                ghostPane.getChildren().removeAll(ghostPane.getChildren());  // remove it so area has no parent
            }

            cells.add(newRow);

            assert r <= 0 || (newRow.size() == cells.get(0).size());
        }
        updateConstraints();
        updateGrid();
    }


    /**
     * Creates a section of the grid based on the start and end x, y coordinates.
     *
     * @param startX start x index
     * @param startY start y index
     * @param endX   end x index
     * @param endY   end y index
     * @return       VBox of the box
     */
    private VBox createCornerBox(int startX, int startY, int endX, int endY) {
        VBox box = new VBox();
        for(int r=startX; r<endX; r++) {
            ArrayList<Cell> rowData = cells.get(r);
            if(rowData == null) {
                continue;
            }

            HBox row = new HBox();
            for(int c=startY; c<endY; c++) {
                Cell cell = rowData.get(c);
                if(cell == null) {
                    continue;
                }

                // add the cell to the row
                row.getChildren().add(cell.getNode());
            }

            // add the row to the vbox
            if(!row.getChildren().isEmpty()) {
                box.getChildren().add(row);
            }
        }

        return box;
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
        updateGrid();
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
        updateGrid();
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
        updateGrid();
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
        updateGrid();
    }


    /**
     * recreates the grid object. Does not create a new object, but updates the children
     */
    public void updateGrid() {
        grid.getChildren().removeAll(grid.getChildren());
        if(cells.isEmpty()) {
            return;
        }

        HBox nBox = new HBox();
        HBox sBox = new HBox();


        // create nw box
        VBox nwBox = createCornerBox(nwGridConstraints.getKey().getKey(), nwGridConstraints.getKey().getValue(), nwGridConstraints.getValue().getValue(), nwGridConstraints.getValue().getKey());
        if(!nwBox.getChildren().isEmpty()) {
            nBox.getChildren().add(nwBox);
        }

        // create nn box
        VBox nnBox = createCornerBox(
            nwGridConstraints.getKey().getValue(),    // nw start y
            nwGridConstraints.getValue().getKey(),    // nw end x
            neGridConstraints.getValue().getValue(),  // ne end y
            neGridConstraints.getKey().getKey()       // ne start x
        );

        ScrollPane scrollNBox = new ScrollPane(nnBox);  // configure scroll pane later
        if(!nnBox.getChildren().isEmpty()) {
            nBox.getChildren().add(scrollNBox);
        }

        // create ne box
        VBox neBox = createCornerBox(neGridConstraints.getKey().getValue(), neGridConstraints.getKey().getKey(), neGridConstraints.getValue().getValue(), neGridConstraints.getValue().getKey());
        if(!neBox.getChildren().isEmpty()) {
            nBox.getChildren().add(neBox);
        }

        // create ww box
        VBox wBox = createCornerBox(
            nwGridConstraints.getValue().getValue(),  // nw end y
            nwGridConstraints.getKey().getKey(),      // nw start x
            swGridConstraints.getKey().getValue(),    // sw start y
            swGridConstraints.getValue().getKey()     // sw end x
        );
        ScrollPane scrollWBox = new ScrollPane(wBox);  // configure scroll pane later

        // create center box
        VBox cBox = createCornerBox(
                nwGridConstraints.getValue().getValue(),  // nw end y
                nwGridConstraints.getValue().getKey(),    // nw end x
                seGridConstraints.getKey().getValue(),    // se start y
                seGridConstraints.getKey().getKey()       // se start x
        );
        ScrollPane scrollCBox = new ScrollPane(cBox);  // configure scroll pane later

        // create sw box
        VBox swBox = createCornerBox(swGridConstraints.getKey().getValue(), swGridConstraints.getKey().getKey(), swGridConstraints.getValue().getValue(), swGridConstraints.getValue().getKey());

        if(!swBox.getChildren().isEmpty()) {
            sBox.getChildren().add(swBox);
        }

        // create ss box
        VBox ssBox = createCornerBox(
            swGridConstraints.getKey().getValue(),    // sw start y
            swGridConstraints.getValue().getKey(),    // sw end x
            seGridConstraints.getValue().getValue(),  // se end y
            seGridConstraints.getKey().getKey()       // se start x
        );

        ScrollPane scrollSBox = new ScrollPane(ssBox);  // configure scroll pane later
        if(!ssBox.getChildren().isEmpty()) {
            sBox.getChildren().add(scrollSBox);
        }

        // create se box
        VBox seBox = createCornerBox(seGridConstraints.getKey().getValue(), seGridConstraints.getKey().getKey(), seGridConstraints.getValue().getValue(), seGridConstraints.getValue().getKey());
        if(!seBox.getChildren().isEmpty()) {
            sBox.getChildren().add(seBox);
        }

        // create ee box
        VBox eBox = createCornerBox(
            neGridConstraints.getValue().getValue(),  // ne end y
            neGridConstraints.getKey().getKey(),      // ne start x
            seGridConstraints.getKey().getValue(),    // se start y
            seGridConstraints.getValue().getKey()     // se end x
        );
        ScrollPane scrollEBox = new ScrollPane(eBox);  // configure scroll pane later


        // bind cell pref sizes to row/col sizes
        assert colPrefWidths.size() == cells.size();
        assert rowPrefHeights.size() == cells.get(0).size();  // no need to perform null check on get() (0 size checked for earlier)
        for (int r=0; r<cells.size(); r++) {
            ArrayList<Cell> row = cells.get(r);
            for (int c = 0; c < row.size(); c++) {
                Cell cell = row.get(c);
                cell.getNode().setMinWidth(colPrefWidths.get(c).getValue());
                cell.getNode().setMinHeight(rowPrefHeights.get(r).getValue());
            }
        }

        // configure scroll panes
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
    }


    /**
     * Returns the grid object
     *
     * @return BorderPane of the grid
     */
    public BorderPane getGrid() {
        return grid;
    }


    /**
     * A function to help debug the grid
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

        Stage window;
        window = new Stage();
        VBox rootLayout = new VBox();

        rootLayout.getChildren().add(grid.getGrid());

        Scene scene = new Scene(rootLayout, 1200, 800);
        window.setScene(scene);
        window.show();

    }

}
