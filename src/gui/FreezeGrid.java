package gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

public class FreezeGrid {
    private class Cell {
        private final Pair<Integer, Integer> gridLocation;
        private final HBox cell;

        public Cell(Pair<Integer, Integer> gridLocation, HBox cell) {
            this.gridLocation = gridLocation;
            this.cell = cell;
        }

        public DoubleProperty getPrefWidthProperty() {
            return cell.prefWidthProperty();
        }

        public DoubleProperty getPrefHeightProperty() {
            return cell.prefHeightProperty();
        }

        public HBox getNode() {
            return cell;
        }

        public Pair<Integer, Integer> getGridLocation() {
            return gridLocation;
        }
    }

    // { {start x, start y}, {end x, end y} }
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> nwGridConstraints;
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> neGridConstraints;
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> swGridConstraints;
    private Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> seGridConstraints;

    private final ArrayList<ArrayList<Cell>> cells;
    private final ArrayList<DoubleProperty> colPrefWidths;
    private final ArrayList<DoubleProperty> rowPrefHeights;

    private final BorderPane grid;

    public FreezeGrid() {
        grid = new BorderPane();

        cells = new ArrayList<>();
        colPrefWidths = new ArrayList<>();
        rowPrefHeights = new ArrayList<>();
    }

    public int getNumRows() {
        return cells.size();
    }


    public int getNumCols() {
        return cells.get(0).size();
    }

    public void updateConstraints() {
        nwGridConstraints = new Pair<>(new Pair<>(0, 0), new Pair<>(0, 0));
        neGridConstraints = new Pair<>(new Pair<>(getNumCols() - 1, 0), new Pair<>(getNumCols() - 1, 0));
        swGridConstraints = new Pair<>(new Pair<>(0, getNumRows() - 1), new Pair<>(0, getNumRows() - 1));
        seGridConstraints = new Pair<>(new Pair<>(getNumRows() - 1, getNumCols() - 1), new Pair<>(getNumRows() - 1, getNumCols() - 1));
    }


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

    public void setGridDataHBox(ArrayList<ArrayList<HBox>> data) {
        cells.clear();
        colPrefWidths.clear();
        rowPrefHeights.clear();

        Pane ghostPane = new Pane();
        Scene ghostScene = new Scene(ghostPane);  // a scene is needed to calculate preferred sizes of nodes

        for(int r=0; r<data.size(); r++) {
            ArrayList<HBox> row = data.get(r);
            ArrayList<Cell> newRow = new ArrayList<>();

            colPrefWidths.add(new SimpleDoubleProperty(0.0));
            rowPrefHeights.add(new SimpleDoubleProperty(0.0));
            for(int c=0; c<row.size(); c++) {
                Cell cell = new Cell(new Pair<>(r, c), row.get(c));
                newRow.add(cell);

                // update preferred sizes
                // add the node to a test pane with the scene set, but not visible so the preferred size gets calculated
                ghostPane.getChildren().add(cell.getNode());
                ghostPane.applyCss();
                ghostPane.layout();

                if(cell.getNode().getBoundsInLocal().getWidth() > colPrefWidths.get(r).doubleValue()) {
                    colPrefWidths.get(r).set(cell.getNode().getBoundsInLocal().getWidth());
                }
                if(cell.getNode().getBoundsInLocal().getHeight() > rowPrefHeights.get(r).doubleValue()) {
                    rowPrefHeights.get(r).set(cell.getNode().getBoundsInLocal().getHeight());
                }

                ghostPane.getChildren().removeAll(ghostPane.getChildren());  // remove it so area has no parent
            }

            cells.add(newRow);

            assert r <= 0 || (newRow.size() == cells.get(0).size());
        }
        updateConstraints();
        updateGrid();
    }


    private VBox createCornerBox(int startX, int startY, int endX, int endY) {
        VBox box = new VBox();
        System.out.println(startX + " " + startY + " " + endX + " " + endY);
        for(int r=startY; r<endY; r++) {
            ArrayList<Cell> rowData = cells.get(r);
            if(rowData == null) {
                continue;
            }

            HBox row = new HBox();
            for(int c=startX; c<endX; c++) {
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

    public void setFreezeHeader(int endRow) {
        // set nw end location
        nwGridConstraints = new Pair<>(new Pair<>(nwGridConstraints.getKey().getKey(), nwGridConstraints.getKey().getValue()), new Pair<>(nwGridConstraints.getValue().getKey(), endRow));

        // set ne end location
        neGridConstraints = new Pair<>(new Pair<>(neGridConstraints.getKey().getKey(), neGridConstraints.getKey().getValue()), new Pair<>(neGridConstraints.getValue().getKey(), endRow));
        updateGrid();
    }


    public void setFreezeFooter(int endRow) {
        // set sw start location
        swGridConstraints = new Pair<>(new Pair<>(swGridConstraints.getKey().getKey(), getNumRows() - 1 - endRow), new Pair<>(swGridConstraints.getValue().getKey(), swGridConstraints.getValue().getValue()));

        // set se start location
        seGridConstraints = new Pair<>(new Pair<>(seGridConstraints.getKey().getKey(), getNumRows() - 1 - endRow), new Pair<>(seGridConstraints.getValue().getKey(), seGridConstraints.getValue().getValue()));
        updateGrid();
    }


    public void setFreezeLeft(int endCol) {
        // set nw end location
        nwGridConstraints = new Pair<>(new Pair<>(nwGridConstraints.getKey().getKey(), nwGridConstraints.getKey().getValue()), new Pair<>(endCol, nwGridConstraints.getValue().getValue()));

        // set sw end location
        swGridConstraints = new Pair<>(new Pair<>(swGridConstraints.getKey().getKey(), swGridConstraints.getKey().getValue()), new Pair<>(endCol, swGridConstraints.getValue().getValue()));
        updateGrid();
    }


    public void setFreezeRight(int endCol) {
        // set ne start location
        neGridConstraints = new Pair<>(new Pair<>(getNumCols() - 1 - endCol, neGridConstraints.getKey().getValue()), new Pair<>(neGridConstraints.getValue().getKey(), neGridConstraints.getValue().getValue()));

        // set se start location
        seGridConstraints = new Pair<>(new Pair<>(getNumCols() - 1 - endCol, seGridConstraints.getKey().getValue()), new Pair<>(seGridConstraints.getValue().getKey(), seGridConstraints.getValue().getValue()));
        updateGrid();
    }



    public void updateGrid() {
        grid.getChildren().removeAll(grid.getChildren());
        if(cells.isEmpty()) {
            return;
        }

        HBox nBox = new HBox();
        HBox sBox = new HBox();

        // create nw box
        VBox nwBox = createCornerBox(nwGridConstraints.getKey().getKey(), nwGridConstraints.getKey().getValue(), nwGridConstraints.getValue().getKey(), nwGridConstraints.getValue().getValue());
        if(!nwBox.getChildren().isEmpty()) {
            nBox.getChildren().add(nwBox);
        }

        // create nn box
        VBox nnBox = createCornerBox(
            nwGridConstraints.getValue().getKey(),    // nw end x
            nwGridConstraints.getKey().getValue(),    // nw start y
            neGridConstraints.getKey().getKey(),      // ne start x
            neGridConstraints.getValue().getValue()   // ne end y
        );

        ScrollPane scrollNBox = new ScrollPane(nnBox);  // configure scroll pane later
        if(!nnBox.getChildren().isEmpty()) {
            nBox.getChildren().add(scrollNBox);
        }

        // create ne box
        VBox neBox = createCornerBox(neGridConstraints.getKey().getKey(), neGridConstraints.getKey().getValue(), neGridConstraints.getValue().getKey(), neGridConstraints.getValue().getValue());
        if(!neBox.getChildren().isEmpty()) {
            nBox.getChildren().add(neBox);
        }

        // create ww box
        VBox wBox = createCornerBox(
                nwGridConstraints.getKey().getKey(),      // nw start x
                nwGridConstraints.getValue().getValue(),  // nw end y
                swGridConstraints.getValue().getKey(),    // sw end x
                swGridConstraints.getKey().getValue()     // sw start y
        );
        ScrollPane scrollWBox = new ScrollPane(wBox);  // configure scroll pane later

        // create center box
        VBox cBox = createCornerBox(
                nwGridConstraints.getValue().getKey(),    // nw end x
                nwGridConstraints.getValue().getValue(),  // nw end y
                seGridConstraints.getKey().getKey(),      // se start x
                seGridConstraints.getKey().getValue()     // se start y
        );
        ScrollPane scrollCBox = new ScrollPane(cBox);  // configure scroll pane later

        // create sw box
        VBox swBox = createCornerBox(swGridConstraints.getKey().getKey(), swGridConstraints.getKey().getValue(), swGridConstraints.getValue().getKey(), swGridConstraints.getValue().getValue());
        if(!swBox.getChildren().isEmpty()) {
            sBox.getChildren().add(swBox);
        }

        // create ss box
        VBox ssBox = createCornerBox(
                swGridConstraints.getValue().getKey(),    // sw end x
                swGridConstraints.getKey().getValue(),    // sw start y
                seGridConstraints.getKey().getKey(),      // se start x
                seGridConstraints.getValue().getValue()   // se end y
        );

        ScrollPane scrollSBox = new ScrollPane(ssBox);  // configure scroll pane later
        if(!ssBox.getChildren().isEmpty()) {
            sBox.getChildren().add(scrollSBox);
        }

        // create se box
        VBox seBox = createCornerBox(seGridConstraints.getKey().getKey(), seGridConstraints.getKey().getValue(), seGridConstraints.getValue().getKey(), seGridConstraints.getValue().getValue());
        if(!seBox.getChildren().isEmpty()) {
            sBox.getChildren().add(seBox);
        }

        // create ee box
        VBox eBox = createCornerBox(
                neGridConstraints.getKey().getKey(),      // ne start x
                neGridConstraints.getValue().getValue(),  // ne end y
                seGridConstraints.getValue().getKey(),    // se end x
                seGridConstraints.getKey().getValue()     // se start y
        );
        ScrollPane scrollEBox = new ScrollPane(eBox);  // configure scroll pane later


        // bind cell pref sizes to row/col sizes
        assert colPrefWidths.size() == cells.size();
        assert rowPrefHeights.size() == cells.get(0).size();  // no need to perform null check on get() (0 size checked for earlier)
        for (ArrayList<Cell> row : cells) {
            for (int c = 0; c < row.size(); c++) {
                Cell cell = row.get(c);
                cell.getNode().setMinWidth(colPrefWidths.get(c).getValue());
                cell.getNode().setMinHeight(rowPrefHeights.get(c).getValue());
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
        }
        if(!wBox.getChildren().isEmpty()) {
            grid.setLeft(scrollWBox);
        }
        if(!eBox.getChildren().isEmpty()) {
            grid.setRight(scrollEBox);
        }
        if(!sBox.getChildren().isEmpty()) {
            grid.setBottom(sBox);
        }
        grid.setCenter(scrollCBox);

    }

    public BorderPane getGrid() {
        return grid;
    }


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
