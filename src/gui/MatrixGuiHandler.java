package gui;

import DSMData.DSMConnection;
import DSMData.DSMItem;
import DSMData.DataHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Vector;

public class MatrixGuiHandler {
    DataHandler matrix;
    private final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.color(1, 1, 1), new CornerRadii(3), new Insets(0)));
    private final Background UNEDITABLE_CONNECTION_BACKGROUND = new Background(new BackgroundFill(Color.color(0, 0, 0), new CornerRadii(3), new Insets(0)));
    private final Background HIGHLIGHT_BACKGROUND = new Background(new BackgroundFill(Color.color(.9, 1, 0), new CornerRadii(3), new Insets(0)));
    private final Background CROSS_HIGHLIGHT_BACKGROUND = new Background(new BackgroundFill(Color.color(.2, 1, 0), new CornerRadii(3), new Insets(0)));

    private Thread highlightThread;
    private static boolean crossHighlight = false;

    private class Triplet<T, U, V> {
        private T first;
        private U second;
        private V third;

        public Triplet(T first, U second, V third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }

        public T getFirst() { return first; }
        public U getSecond() { return second; }
        public V getThird() { return third; }

        public void setFirst(T first) {
            this.first = first;
        }
        public void setSecond(U second) {
            this.second = second;
        }
        public void setThird(V third) {
            this.third = third;
        }
    }

    // pair of locations (row, column) | cell object | triple of highlight colors (default, highlight1, highlight2)
    Vector<Triplet<Pair<Integer, Integer>, HBox, Triplet<Background, Background, Background>>> cells;


    MatrixGuiHandler(DataHandler matrix) {
        this.matrix = matrix;
        cells = new Vector<>();

        this.highlightThread = new Thread(() -> {
            while(true) {
                for(Triplet<Pair<Integer, Integer>, HBox, Triplet<Background, Background, Background>> cell : cells) {
                    if(cell.getThird().getSecond() != null) {
                        cell.getSecond().setBackground(cell.getThird().getSecond());
                    } else if(cell.getThird().getThird() != null && crossHighlight) {
                        cell.getSecond().setBackground(cell.getThird().getThird());
                    } else {
                        cell.getSecond().setBackground(cell.getThird().getFirst());
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        highlightThread.setDaemon(true);
        highlightThread.start();
    }

    private void highlightCell(Pair<Integer, Integer> cellLoc) {
        for(Triplet<Pair<Integer, Integer>, HBox, Triplet<Background, Background, Background>> cell : cells) {  // determine the value to decrease to
            if(cell.getFirst().getKey() == cellLoc.getKey() && cell.getFirst().getValue() == cellLoc.getValue()) {
                if(cell.getThird().getSecond() == null) {  // is highlighted, so unhighlight it
                    cell.getThird().setSecond(HIGHLIGHT_BACKGROUND);
                } else {
                    cell.getThird().setSecond(null);
                }
                break;
            }
        }
    }

    private void crossHighlightCell(Pair<Integer, Integer> endLocation, boolean shouldHighlight) {
        int endRow = endLocation.getKey();
        int endCol = endLocation.getValue();

        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;
        for(Triplet<Pair<Integer, Integer>, HBox, Triplet<Background, Background, Background>> cell : cells) {  // determine the value to decrease to
            if(cell.getFirst().getKey() < minRow) {
                minRow = cell.getFirst().getKey();
            }
            if(cell.getFirst().getValue() < minCol) {
                minCol = cell.getFirst().getValue();
            }
        }

        for(int i=endRow; i>=minRow; i--) {  // highlight vertically
            for(Triplet<Pair<Integer, Integer>, HBox, Triplet<Background, Background, Background>> triplet : cells) {  // find the cell to modify
                if(triplet.getFirst().getKey() == i && triplet.getFirst().getValue() == endCol) {
                    if(shouldHighlight) {
                        triplet.getThird().setThird(CROSS_HIGHLIGHT_BACKGROUND);
                    } else {
                        triplet.getThird().setThird(null);
                    }
                    break;
                }
            }
        }

        for(int i=endCol - 1; i>=minCol; i--) {  // highlight horizontally, start at one less because first cell it will find is already highlighted
            for(Triplet<Pair<Integer, Integer>, HBox, Triplet<Background, Background, Background>> triplet : cells) {  // find the cell to modify
                if(triplet.getFirst().getValue() == i && triplet.getFirst().getKey() == endRow) {
                    if(shouldHighlight) {
                        triplet.getThird().setThird(CROSS_HIGHLIGHT_BACKGROUND);
                    } else {
                        triplet.getThird().setThird(null);
                    }
                    break;
                }
            }
        }

    }

    public void toggleCrossHighlighting() {
        crossHighlight = !crossHighlight;
    }

    GridPane getMatrixEditor() {
        GridPane grid = new GridPane();

        grid.setAlignment(Pos.CENTER);
        ArrayList< ArrayList< Pair<String, Object> > > template = matrix.getGridArray();
        int rows = template.size();
        int columns = template.get(0).size();

        int connectionR = 0;
        int connectionC = 0;

        for(int r=0; r<rows; r++) {
            for(int c=0; c<columns; c++) {
                Pair<String, Object> item = template.get(r).get(c);
                HBox cell = new HBox();  // wrap everything in an HBox so a border can be added easily

                Background defaultBackground = DEFAULT_BACKGROUND;

                if(item.getKey().equals("plain_text")) {
                    Object label = null;
                    if(r == 0) {
                        label = new VerticalLabel(VerticalDirection.UP);
                        ((VerticalLabel) label).setText((String)item.getValue());
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                    } else {
                        label = new Label((String)item.getValue());
                    }
                    cell.getChildren().add((Node) label);
                } else if(item.getKey().equals("item_name")) {
                    Object label = null;
                    if(r == 0) {
                        label = new VerticalLabel(VerticalDirection.UP);
                        ((VerticalLabel) label).setText(matrix.getItem((Integer)item.getValue()).getName());
                        cell.setAlignment(Pos.BOTTOM_RIGHT);
                    } else {
                        label = new Label(matrix.getItem((Integer)item.getValue()).getName());
                    }
                    cell.getChildren().add((Node) label);
                } else if(item.getKey().equals("index_item")) {
                    Label label = new Label(((Double)matrix.getItem((Integer)item.getValue()).getSortIndex()).toString());
                    cell.setAlignment(Pos.CENTER_RIGHT);
                    cell.getChildren().add(label);
                } else if(item.getKey().equals("uneditable_connection")) {
                    HBox label = new HBox();  // use an HBox object because then background color is not tied to the text
                    defaultBackground = UNEDITABLE_CONNECTION_BACKGROUND;
                } else if(item.getKey().equals("editable_connection")) {
                    int rowUid = ((Pair<Integer, Integer>)item.getValue()).getKey();
                    int colUid = ((Pair<Integer, Integer>)item.getValue()).getValue();
                    DSMConnection conn = matrix.getConnection(rowUid, colUid);
                    Label label = null;
                    if(conn == null) {
                        label = new Label("");
                    } else {
                        label = new Label(conn.getConnectionName());
                    }

                    int finalR = r;
                    int finalC = c;
                    cell.setOnMouseClicked(e -> {
                        if(e.getButton().equals(MouseButton.PRIMARY)) {
                            System.out.println("editing connection");
                        } else if(e.getButton().equals(MouseButton.SECONDARY)) {  // toggle highlighting
                            highlightCell(new Pair<Integer, Integer>(finalR, finalC));
                        }
                    });
                    cell.setOnMouseEntered(e -> {
                        crossHighlightCell(new Pair<Integer, Integer>(finalR, finalC), true);
                    });
                    cell.setOnMouseExited(e -> {
                        crossHighlightCell(new Pair<Integer, Integer>(finalR, finalC), false);
                    });

                    cell.getChildren().add(label);
                }
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
                cell.setPadding(new Insets(10, 10, 10, 10));
                GridPane.setConstraints(cell, c, r);
                grid.getChildren().add(cell);

                cells.add(new Triplet<>(
                        new Pair<>(r, c),
                        cell,
                        new Triplet<>(defaultBackground, null, null)
                ));

            }
        }

        return grid;
    }
}
