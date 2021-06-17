package gui;

import DSMData.DSMConnection;
import DSMData.DSMItem;
import DSMData.DataHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Vector;

public class MatrixGuiHandler {
    DataHandler matrix;
    private final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.color(1, 1, 1), new CornerRadii(3), new Insets(0)));
    private final Background UNEDITABLE_CONNECTION_BACKGROUND = new Background(new BackgroundFill(Color.color(0, 0, 0), new CornerRadii(3), new Insets(0)));
    private final Background HIGHLIGHT_BACKGROUND = new Background(new BackgroundFill(Color.color(.9, 1, 0), new CornerRadii(3), new Insets(0)));
    private final Background CROSS_HIGHLIGHT_BACKGROUND = new Background(new BackgroundFill(Color.color(.2, 1, 0), new CornerRadii(3), new Insets(0)));
    private final Background ERROR_BACKGROUND = new Background(new BackgroundFill(Color.color(1, 0, 0), new CornerRadii(3), new Insets(0)));

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

    private void toggleHighlightCell(Pair<Integer, Integer> cellLoc, Background bg) {
        for(Triplet<Pair<Integer, Integer>, HBox, Triplet<Background, Background, Background>> cell : cells) {  // determine the value to decrease to
            if(cell.getFirst().getKey() == cellLoc.getKey() && cell.getFirst().getValue() == cellLoc.getValue()) {
                if(cell.getThird().getSecond() == null) {  // is highlighted, so unhighlight it
                    cell.getThird().setSecond(bg);
                } else {
                    cell.getThird().setSecond(null);
                }
                break;
            }
        }
    }

    private void clearCellHighlight(Pair<Integer, Integer> cellLoc) {
        for(Triplet<Pair<Integer, Integer>, HBox, Triplet<Background, Background, Background>> cell : cells) {  // determine the value to decrease to
            if(cell.getFirst().getKey() == cellLoc.getKey() && cell.getFirst().getValue() == cellLoc.getValue()) {
                cell.getThird().setSecond(null);
                break;
            }
        }
    }

    private void enableCellHighlight(Pair<Integer, Integer> cellLoc, Background bg) {
        for(Triplet<Pair<Integer, Integer>, HBox, Triplet<Background, Background, Background>> cell : cells) {  // determine the value to decrease to
            if(cell.getFirst().getKey() == cellLoc.getKey() && cell.getFirst().getValue() == cellLoc.getValue()) {
                cell.getThird().setSecond(bg);
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
                    TextField entry = new TextField(((Double)matrix.getItem((Integer)item.getValue()).getSortIndex()).toString());
                    // force the field to be numeric only
                    entry.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            if (!newValue.matches("\\d*+\\.")) {
                                entry.setText(newValue.replaceAll("[^\\d]+[.]", ""));
                            }
                        }
                    });
                    cell.setAlignment(Pos.CENTER_RIGHT);

                    int finalR = r;
                    int finalC = c;
                    entry.setOnAction(e -> {
                        cell.getParent().requestFocus();
                    });
                    entry.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if(!newVal) {  // if changing to not focused
                            try {
                                Double newSortIndex = Double.parseDouble(entry.getText());
                                if(matrix.isSymmetrical()) {
                                    matrix.setSortIndexSymmetric((Integer)item.getValue(), newSortIndex);
                                } else {
                                    matrix.setSortIndex((Integer)item.getValue(), newSortIndex);
                                }
                                clearCellHighlight(new Pair<Integer, Integer>(finalR, finalC));
                            } catch(NumberFormatException ee) {
                                enableCellHighlight(new Pair<Integer, Integer>(finalR, finalC), ERROR_BACKGROUND);
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
                    final Label label;
                    if(conn == null) {
                        label = new Label("");
                    } else {
                        label = new Label(conn.getConnectionName());
                    }

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

                            String currentWeight = null;
                            if(matrix.getConnection(rowUid, colUid) != null) {
                                currentWeight = ((Double)matrix.getConnection(rowUid, colUid).getWeight()).toString();
                            } else {
                                currentWeight = "1.0";
                            }
                            TextField weightField = new TextField(currentWeight);
                            weightField.setMaxWidth(Double.MAX_VALUE);
                            HBox.setHgrow(weightField, Priority.ALWAYS);
                            // force the field to be numeric only
                            weightField.textProperty().addListener(new ChangeListener<String>() {
                                @Override
                                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                                    if (!newValue.matches("\\d*+\\.")) {
                                        weightField.setText(newValue.replaceAll("[^\\d]+[.]", ""));
                                    }
                                }
                            });
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
                                label.setText(nameField.getText());
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
                            toggleHighlightCell(new Pair<Integer, Integer>(finalR, finalC), HIGHLIGHT_BACKGROUND);
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
