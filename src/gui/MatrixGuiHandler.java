package gui;

import DSMData.DSMConnection;
import DSMData.DSMItem;
import DSMData.DataHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VerticalDirection;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
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

    private class HighlightScheme {
        private Background defaultBG;
        private Background userHighlightBG;
        private Background crossHighlightBG;
        private Background errorHighlightBG;

        public HighlightScheme(Background defaultBG, Background userHighlightBG, Background crossHighlightBG, Background errorHighlightBG) {
            this.defaultBG = defaultBG;
            this.userHighlightBG = userHighlightBG;
            this.crossHighlightBG = crossHighlightBG;
            this.errorHighlightBG = errorHighlightBG;
        }

        public Background getDefaultBG() {
            return defaultBG;
        }

        public void setDefaultBG(Background defaultBG) {
            this.defaultBG = defaultBG;
        }

        public Background getUserHighlightBG() {
            return userHighlightBG;
        }

        public void setUserHighlightBG(Background userHighlightBG) {
            this.userHighlightBG = userHighlightBG;
        }

        public Background getCrossHighlightBG() {
            return crossHighlightBG;
        }

        public void setCrossHighlightBG(Background crossHighlightBG) {
            this.crossHighlightBG = crossHighlightBG;
        }

        public Background getErrorHighlightBG() {
            return errorHighlightBG;
        }

        public void setErrorHighlightBG(Background errorHighlightBG) {
            this.errorHighlightBG = errorHighlightBG;
        }
    }

    private class Cell {
        private Pair<Integer, Integer> gridLocation;
        private HBox guiCell;
        private HighlightScheme highlightScheme;

        public Cell(Pair<Integer, Integer> gridLocation, HBox guiCell, HighlightScheme highlightScheme) {
            this.gridLocation = gridLocation;
            this.guiCell = guiCell;
            this.highlightScheme = highlightScheme;
        }

        public Pair<Integer, Integer> getGridLocation() {
            return gridLocation;
        }

        public HBox getGuiCell() {
            return guiCell;
        }

        public HighlightScheme getHighlightScheme() {
            return highlightScheme;
        }
    }

    Vector<Cell> cells;  // contains information for highlighting
    HashMap<String, HashMap<Integer, Integer>> gridUidLookup;

    MatrixGuiHandler(DataHandler matrix) {
        this.matrix = matrix;
        cells = new Vector<>();
        gridUidLookup = new HashMap<>();
        gridUidLookup.put("rows", new HashMap<Integer, Integer>());
        gridUidLookup.put("cols", new HashMap<Integer, Integer>());

        this.highlightThread = new Thread(() -> {
            while(true) {
                Platform.runLater(new Runnable() {  // this allows a thread to update the gui
                    @Override
                    public void run() {
                        for (Cell cell : cells) {
                            if (cell.getHighlightScheme().getErrorHighlightBG() != null) {
                                cell.getGuiCell().setBackground(cell.getHighlightScheme().getErrorHighlightBG());
                            } else if (cell.getHighlightScheme().getCrossHighlightBG() != null && crossHighlight) {
                                cell.getGuiCell().setBackground(cell.getHighlightScheme().getCrossHighlightBG());
                            } else if (cell.getHighlightScheme().getUserHighlightBG() != null) {
                                cell.getGuiCell().setBackground(cell.getHighlightScheme().getUserHighlightBG());
                            } else {  // default background determined by groupings
                                Integer rowUid = getUidsFromGridLoc(cell.getGridLocation()).getKey();
                                Integer colUid = getUidsFromGridLoc(cell.getGridLocation()).getValue();
                                Color mergedColor = null;
                                if(rowUid == null && colUid != null) {  // highlight with column color
                                    mergedColor = matrix.getGroupingColors().get(matrix.getItem(colUid).getGroup());
                                    cell.getGuiCell().setBackground(new Background(new BackgroundFill(mergedColor, new CornerRadii(3), new Insets(0))));
                                    continue;
                                } else if(rowUid != null && colUid == null) {  // highlight with row color
                                    mergedColor = matrix.getGroupingColors().get(matrix.getItem(rowUid).getGroup());
                                    cell.getGuiCell().setBackground(new Background(new BackgroundFill(mergedColor, new CornerRadii(3), new Insets(0))));
                                    continue;
                                } else if(rowUid != null && colUid != null) {  // highlight with merged color
                                    Color rowColor = matrix.getGroupingColors().get(matrix.getItem(rowUid).getGroup());
                                    if(rowColor == null) rowColor = Color.color(1.0, 1.0, 1.0);

                                    Color colColor = matrix.getGroupingColors().get(matrix.getItem(colUid).getGroup());
                                    if(colColor == null) colColor = Color.color(1.0, 1.0, 1.0);

                                    double r = (rowColor.getRed() + colColor.getRed()) / 2;
                                    double g = (rowColor.getGreen() + colColor.getGreen()) / 2;
                                    double b = (rowColor.getBlue() + colColor.getBlue()) / 2;
                                    mergedColor = Color.color(r, g, b);

//                                    System.out.println(rowUid + " " + colUid + " " + matrix.getItem(colUid).getAliasUid() + " " + matrix.getItem(rowUid).getGroup() + " " + matrix.getItem(colUid).getGroup());
                                    if(matrix.isSymmetrical() && !rowUid.equals(matrix.getItem(colUid).getAliasUid()) && matrix.getItem(rowUid).getGroup().equals(matrix.getItem(colUid).getGroup())) {  // associated row and column are same group
                                        cell.getGuiCell().setBackground(new Background(new BackgroundFill(mergedColor, new CornerRadii(3), new Insets(0))));
                                        continue;
                                    } else if(!matrix.isSymmetrical()) {
                                        cell.getGuiCell().setBackground(new Background(new BackgroundFill(mergedColor, new CornerRadii(3), new Insets(0))));
                                        continue;
                                    }
                                }

                                cell.getGuiCell().setBackground(cell.getHighlightScheme().getDefaultBG());
                            }
                        }
                    }
                });

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

    private Cell getCellByLoc(Pair<Integer, Integer> cellLoc) {
        for(Cell cell : cells) {  // determine the value to decrease to
            if(cell.getGridLocation().getKey() == cellLoc.getKey() && cell.getGridLocation().getValue() == cellLoc.getValue()) {
                return cell;
            }
        }
        return null;
    }

    private Pair<Integer, Integer> getUidsFromGridLoc(Pair<Integer, Integer> cellLoc) {
        Integer rowUid = gridUidLookup.get("rows").get(cellLoc.getKey());;
        Integer colUid = gridUidLookup.get("cols").get(cellLoc.getValue());;
        return new Pair<>(rowUid, colUid);
    }

    private void toggleUserHighlightCell(Pair<Integer, Integer> cellLoc, Background bg) {
        Cell cell = getCellByLoc(cellLoc);
        if(cell.getHighlightScheme().getUserHighlightBG() == null) {  // is highlighted, so unhighlight it
            cell.getHighlightScheme().setUserHighlightBG(bg);
        } else {
            cell.getHighlightScheme().setUserHighlightBG(null);
        }
    }


    private void setCellHighlight(Pair<Integer, Integer> cellLoc, Background bg, String highlightType) {
        Cell cell = getCellByLoc(cellLoc);
        HashMap<String, Runnable> functions = new HashMap<>();
        functions.put("userHighlight", () -> cell.getHighlightScheme().setUserHighlightBG(bg));
        functions.put("errorHighlight", () -> cell.getHighlightScheme().setErrorHighlightBG(bg));
        functions.get(highlightType).run();
    }


    private void clearCellHighlight(Pair<Integer, Integer> cellLoc, String highlightType) {
        Cell cell = getCellByLoc(cellLoc);
        HashMap<String, Runnable> functions = new HashMap<>();
        functions.put("userHighlight", () -> cell.getHighlightScheme().setUserHighlightBG(null));
        functions.put("errorHighlight", () -> cell.getHighlightScheme().setErrorHighlightBG(null));
        functions.get(highlightType).run();
    }


    private void crossHighlightCell(Pair<Integer, Integer> endLocation, boolean shouldHighlight) {
        int endRow = endLocation.getKey();
        int endCol = endLocation.getValue();

        int minRow = Integer.MAX_VALUE;
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
                        cell.getHighlightScheme().setCrossHighlightBG(CROSS_HIGHLIGHT_BACKGROUND);
                    } else {
                        cell.getHighlightScheme().setCrossHighlightBG(null);
                    }
                    break;
                }
            }
        }

        for(int i=endCol - 1; i>=minCol; i--) {  // highlight horizontally, start at one less because first cell it will find is already highlighted
            for(Cell cell : cells) {  // find the cell to modify
                if(cell.getGridLocation().getValue() == i && cell.getGridLocation().getKey() == endRow) {
                    if(shouldHighlight) {
                        cell.getHighlightScheme().setCrossHighlightBG(CROSS_HIGHLIGHT_BACKGROUND);
                    } else {
                        cell.getHighlightScheme().setCrossHighlightBG(null);
                    }
                    break;
                }
            }
        }

    }

    public void toggleCrossHighlighting() {
        crossHighlight = !crossHighlight;
    }

     VBox getMatrixEditor() {
        VBox rootLayout = new VBox();
        rootLayout.setAlignment(Pos.CENTER);

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
                    VerticalLabel label = new VerticalLabel(VerticalDirection.UP);
                    label.setText((String)item.getValue());
                    cell.setAlignment(Pos.BOTTOM_RIGHT);
                    cell.getChildren().add((Node) label);
                } else if(item.getKey().equals("item_name")) {
                    Label label = new Label(matrix.getItem((Integer) item.getValue()).getName());
                    cell.setAlignment(Pos.BOTTOM_RIGHT);
                    label.setMinWidth(Region.USE_PREF_SIZE);
                    cell.getChildren().add(label);
                } else if(item.getKey().equals("item_name_v")) {
                    VerticalLabel label = new VerticalLabel(VerticalDirection.UP);
                    label.setText(matrix.getItem((Integer)item.getValue()).getName());
                    cell.getChildren().add(label);
                } else if(item.getKey().equals("grouping_item")) {
                    ComboBox<String> groupings = new ComboBox<String>();
                    groupings.setMinWidth(Region.USE_PREF_SIZE);
                    groupings.getItems().addAll(matrix.getGroupings());
                    groupings.getSelectionModel().select(matrix.getItem((Integer)item.getValue()).getGroup());
                    groupings.setOnAction(e -> {
                        if(matrix.isSymmetrical()) {
                            matrix.setGroupSymmetric((Integer)item.getValue(), groupings.getValue());
                        } else {
                            matrix.setGroup((Integer)item.getValue(), groupings.getValue());
                        }
                    });
                    cell.getChildren().add(groupings);
                } else if(item.getKey().equals("grouping_item_v")) {
                    ComboBox<String> groupings = new ComboBox<String>();
                    groupings.getItems().addAll(matrix.getGroupings());
                    groupings.setStyle(  // remove border from button when selecting it because this causes weird resizing bugs in the grouping
                            "-fx-focus-color: transparent;" +
                            "-fx-background-color: -fx-outer-border, -fx-inner-border, -fx-body-color; \n" +
                            "-fx-background-insets: 0, 0, 0;\n" +
                            "-fx-background-radius: 0, 0, 0;"
                    );
                    groupings.setRotate(-90);
                    groupings.getSelectionModel().select(matrix.getItem((Integer)item.getValue()).getGroup());
                    groupings.setOnAction(e -> {
                        matrix.setGroup((Integer)item.getValue(), groupings.getValue());
                    });
                    Group g = new Group();  // box will be added to a group so that it will be formatted correctly if it is vertical
                    g.getChildren().add(groupings);
                    cell.getChildren().add(g);
                } else if(item.getKey().equals("index_item")) {
                    TextField entry = new TextField(((Double)matrix.getItem((Integer)item.getValue()).getSortIndex()).toString());
                    entry.setPrefColumnCount(3);  // set size to 5 characters fitting

                    // force the field to be numeric only TODO: this stopped working on 6/20
                    entry.textProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                            if (!newValue.matches("\\d*+\\.")) {
                                entry.setText(newValue.replaceAll("[^\\d]+[.]", ""));
                            }
                        }
                    });
                    cell.setMaxWidth(Region.USE_COMPUTED_SIZE);

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
                                clearCellHighlight(new Pair<Integer, Integer>(finalR, finalC), "errorHighlight");
                            } catch(NumberFormatException ee) {
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
                    final Label label;
                    if(conn == null) {
                        label = new Label("");
                    } else {
                        label = new Label(conn.getConnectionName());
                    }
                    cell.setAlignment(Pos.CENTER);  // center the text

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
                cell.setPadding(new Insets(1, 1, 1, 1));
                GridPane.setConstraints(cell, c, r);
                grid.getChildren().add(cell);

                cells.add(new Cell(
                        new Pair<>(r, c),
                        cell,
                        new HighlightScheme(defaultBackground, null, null, null)
                ));

            }
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        rootLayout.getChildren().addAll(scrollPane, location);


        return rootLayout;
    }
}
