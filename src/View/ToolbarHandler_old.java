//package View;
//
//import Data.SymmetricDSM;
//import Data.DSMItem;
//import Data.MatrixHandler;
//import View.Widgets.NumericTextField;
//import javafx.application.Platform;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.Node;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//import javafx.scene.paint.Color;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//import javafx.util.Callback;
//import javafx.util.Pair;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Vector;
//
//
///**
// * Class that creates and manages a VBox that contains gui functions to modify a matrix
// *
// * @author Aiden Carney
// */
//public class ToolbarHandler {
//    private VBox layout;
//
//    private final Button addMatrixItem;
//    private final Button deleteMatrixItem;
//    private final Button renameMatrixItem;
//    private final Button appendConnections;
//    private final Button setConnections;
//    private final Button configureGroupings;
//    private final Button sort;
//    private final Button reDistributeIndices;
//    private final Button deleteConnections;
//
//    private EditorPane editor;
//    private MatrixHandler matrixHandler;
//
//
//    /**
//     * Creates a new ToolBar Handler object. Sets up the gui and all its widgets and puts them in the layout field.
//     * Requires an MatrixHandler object to get the matrix and a EditorPane object to get the current focused tab
//     * and call updates to it.
//     *
//     * @param matrixHandler the MatrixHandler instance
//     * @param editor    the EditorPane instance
//     */
//    public ToolbarHandler(MatrixHandler matrixHandler, EditorPane editor) {
//        layout = new VBox();
//        this.editor = editor;
//        this.matrixHandler = matrixHandler;
//
//        addMatrixItem = new Button("Add Rows/Columns");
//        addMatrixItem.setOnAction(e -> {
//            if(editor.getFocusedMatrixUid() == null) {
//                return;
//            }
//            Stage window = new Stage();
//
//            // Create Root window
//            window.initModality(Modality.APPLICATION_MODAL); // Block events to other windows
//            window.setTitle("Add Row/Column");
//
//            // Create changes view and button for it
//            Label label = new Label("Changes to be made");
//            ListView< Pair<String, String> > changesToMakeView = new ListView<>();
//            changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//            changesToMakeView.setCellFactory(param -> new ListCell< Pair<String, String> >() {  // row/column/symmetric | item name
//                @Override
//                protected void updateItem(Pair<String, String> item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null || item.getKey() == null) {
//                        setText(null);
//                    } else {
//                        if(item.getKey().equals("symmetric")) {
//                            setText(item.getValue() + " (Row/Column)");
//                        } else if(item.getKey().equals("row")) {
//                            setText(item.getValue() + " (Row)");
//                        } else {
//                            setText(item.getValue() + "(Col)");
//                        }
//                    }
//                }
//            });
//
//            Button deleteSelected = new Button("Delete Selected Item(s)");
//            deleteSelected.setOnAction(ee -> {
//                changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
//            });
//
//            // Create user input area
//            HBox entryArea = new HBox();
//
//            TextField textField = new TextField();
//            textField.setMaxWidth(Double.MAX_VALUE);
//            textField.setPromptText("Row/Column Name");
//            HBox.setHgrow(textField, Priority.ALWAYS);
//
//            if(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical()) {
//                Button addItem = new Button("Add Item");
//                addItem.setOnAction(ee -> {
//                    String itemName = textField.getText();
//                    changesToMakeView.getItems().add(new Pair<String, String>("symmetric", itemName));
//                });
//                entryArea.getChildren().addAll(textField, addItem);
//                entryArea.setPadding(new Insets(10, 10, 10, 10));
//                entryArea.setSpacing(20);
//            } else {
//                Button addRow = new Button("Add as Row");
//                addRow.setOnAction(ee -> {
//                    String itemName = textField.getText();
//                    changesToMakeView.getItems().add(new Pair<String, String>("row", itemName));
//                });
//
//                Button addColumn = new Button("Add as Column");
//                addColumn.setOnAction(ee -> {
//                    String itemName = textField.getText();
//                    changesToMakeView.getItems().add(new Pair<String, String>("col", itemName));
//                });
//
//                entryArea.getChildren().addAll(textField, addRow, addColumn);
//                entryArea.setPadding(new Insets(10, 10, 10, 10));
//                entryArea.setSpacing(20);
//            }
//
//            // create HBox for user to close with our without changes
//            Pane vSpacer = new Pane();  // used as a spacer to move HBox to the bottom
//            VBox.setVgrow(vSpacer, Priority.ALWAYS);
//            vSpacer.setMaxHeight(Double.MAX_VALUE);
//
//            HBox closeArea = new HBox();
//            Button applyButton = new Button("Apply Changes");
//            applyButton.setOnAction(ee -> {
//                for(Pair<String, String> item : changesToMakeView.getItems()) {
//                    if(item.getKey().equals("row")) {
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).addItem(item.getValue(), true);
//                    } else if(item.getKey().equals("col")) {
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).addItem(item.getValue(), false);
//                    } else {
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).addSymmetricItem(item.getValue());
//                    }
//                }
//                matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setCurrentStateAsCheckpoint();
//                window.close();
//                editor.refreshTab();
//            });
//
//            Pane spacer = new Pane();  // used as a spacer between buttons
//            HBox.setHgrow(spacer, Priority.ALWAYS);
//            spacer.setMaxWidth(Double.MAX_VALUE);
//
//            Button cancelButton = new Button("Cancel");
//            cancelButton.setOnAction(ee -> {
//                window.close();
//            });
//            closeArea.getChildren().addAll(cancelButton, spacer, applyButton);
//
//            VBox layout = new VBox(10);
//            layout.getChildren().addAll(label, changesToMakeView, deleteSelected, entryArea, vSpacer, closeArea);
//            layout.setAlignment(Pos.CENTER);
//            layout.setPadding(new Insets(10, 10, 10, 10));
//            layout.setSpacing(10);
//
//            //Display window and wait for it to be closed before returning
//            Scene scene = new Scene(layout, 700, 350);
//            window.setScene(scene);
//            window.showAndWait();
//        });
//        addMatrixItem.setMaxWidth(Double.MAX_VALUE);
//
//
//        deleteMatrixItem = new Button("Delete Row/Column");
//        deleteMatrixItem.setOnAction(e -> {
//            if(editor.getFocusedMatrixUid() == null) {
//                return;
//            }
//            Stage window = new Stage();  // Create Root window
//            window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
//            window.setTitle("Delete Rows/Columns");
//
//            // Create changes view and button for it
//            Label label = new Label("Changes to be made");
//            ListView< Pair<String, Integer> > changesToMakeView = new ListView<>();
//            changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//            changesToMakeView.setCellFactory(param -> new ListCell< Pair<String, Integer> >() {  // row/column/symmetric | item uid
//                @Override
//                protected void updateItem(Pair<String, Integer> item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null || item.getKey() == null) {
//                        setText(null);
//                    } else {
//                        if(item.getKey().equals("symmetric")) {
//                            setText(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(item.getValue()).getName() + " (Row/Column)");
//                        } else {
//                            setText(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(item.getValue()).getName());
//                        }
//                    }
//                }
//            });
//
//            Button deleteSelected = new Button("Delete Selected Item(s)");
//            deleteSelected.setOnAction(ee -> {
//                changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
//            });
//
//            // Create user input area
//            HBox entryArea = new HBox();
//
//            // ComboBox to choose which row or column to modify connections of
//            ComboBox< DSMItem > itemSelector = new ComboBox<>();  // rowUid | colUid | name | weight
//            itemSelector.setCellFactory(param -> new ListCell< DSMItem >() {
//                @Override
//                protected void updateItem(DSMItem item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null) {
//                        setText(null);
//                    } else {
//                        setText(item.getName());
//                    }
//                }
//            });
//            itemSelector.setMaxWidth(Double.MAX_VALUE);
//            HBox.setHgrow(itemSelector, Priority.ALWAYS);
//
//            Button deleteItem = new Button("Delete Item");
//
//            entryArea.getChildren().addAll(itemSelector, deleteItem);
//            entryArea.setPadding(new Insets(10, 10, 10, 10));
//            entryArea.setSpacing(20);
//
//            if(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical()) {
//                itemSelector.setPromptText("Item Name");
//                itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getRows());
//
//                deleteItem.setOnAction(ee -> {
//                    Integer itemUid = itemSelector.getValue().getUid();
//                    changesToMakeView.getItems().add(new Pair<String, Integer>("symmetric", itemUid));
//                });
//            } else {
//                itemSelector.setPromptText("Row/Column Name");
//                itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getRows());
//                itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols());
//
//                deleteItem.setOnAction(ee -> {
//                    Integer itemUid = itemSelector.getValue().getUid();
//                    changesToMakeView.getItems().add(new Pair<String, Integer>("row/col", itemUid));
//                });
//
//            }
//
//            // create HBox for user to close with our without changes
//            Pane vSpacer = new Pane();  // used as a spacer to move HBox to the bottom
//            VBox.setVgrow(vSpacer, Priority.ALWAYS);
//            vSpacer.setMaxHeight(Double.MAX_VALUE);
//
//            HBox closeArea = new HBox();
//            Button applyButton = new Button("Apply Changes");
//            applyButton.setOnAction(ee -> {
//                for(Pair<String, Integer> item : changesToMakeView.getItems()) {
//                    if(item.getKey().equals("symmetric")) {
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).deleteSymmetricItem(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(item.getValue()));
//                    } else {
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).deleteItem(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(item.getValue()));
//                    }
//                }
//                window.close();
//                editor.refreshTab();
//                matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setCurrentStateAsCheckpoint();
//            });
//
//            Pane spacer = new Pane();  // used as a spacer between buttons
//            HBox.setHgrow(spacer, Priority.ALWAYS);
//            spacer.setMaxWidth(Double.MAX_VALUE);
//
//            Button cancelButton = new Button("Cancel");
//            cancelButton.setOnAction(ee -> {
//                window.close();
//            });
//            closeArea.getChildren().addAll(cancelButton, spacer, applyButton);
//
//            VBox layout = new VBox(10);
//            layout.getChildren().addAll(label, changesToMakeView, deleteSelected, entryArea, vSpacer, closeArea);
//            layout.setAlignment(Pos.CENTER);
//            layout.setPadding(new Insets(10, 10, 10, 10));
//            layout.setSpacing(10);
//
//            //Display window and wait for it to be closed before returning
//            Scene scene = new Scene(layout, 700, 350);
//            window.setScene(scene);
//            window.showAndWait();
//        });
//        deleteMatrixItem.setMaxWidth(Double.MAX_VALUE);
//
//
//        renameMatrixItem = new Button("Rename Row/Column");
//        renameMatrixItem.setOnAction(e -> {
//            if(editor.getFocusedMatrixUid() == null) {
//                return;
//            }
//            Stage window = new Stage();  // Create Root window
//            window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
//            window.setTitle("Delete Rows/Columns");
//
//            // Create changes view and button for it
//            Label label = new Label("Changes to be made");
//            ListView< Pair<Integer, String> > changesToMakeView = new ListView<>();
//            changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//            changesToMakeView.setCellFactory(param -> new ListCell< Pair<Integer, String> >() {  // row/column/symmetric | item uid
//                @Override
//                protected void updateItem(Pair<Integer, String> item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null || item.getKey() == null) {
//                        setText(null);
//                    } else {
//                        setText(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(item.getKey()).getName() + "->" + item.getValue());
//                    }
//                }
//            });
//
//            Button deleteSelected = new Button("Delete Selected Item(s)");
//            deleteSelected.setOnAction(ee -> {
//                changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
//            });
//
//            // Create user input area
//            HBox entryArea = new HBox();
//
//            // ComboBox to choose which row or column to modify connections of
//            ComboBox< DSMItem > itemSelector = new ComboBox<>();  // rowUid | colUid | name | weight
//            itemSelector.setPromptText("Item Name");
//            itemSelector.setCellFactory(param -> new ListCell< DSMItem >() {
//                @Override
//                protected void updateItem(DSMItem item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null) {
//                        setText(null);
//                    } else {
//                        setText(item.getName());
//                    }
//                }
//            });
//            itemSelector.setMaxWidth(Double.MAX_VALUE);
//            HBox.setHgrow(itemSelector, Priority.ALWAYS);
//
//            TextField textField = new TextField();
//            textField.setMaxWidth(Double.MAX_VALUE);
//            textField.setPromptText("New Item Name");
//            HBox.setHgrow(textField, Priority.ALWAYS);
//
//
//            Button renameItem = new Button("Rename Item");
//
//            entryArea.getChildren().addAll(itemSelector, textField, renameItem);
//            entryArea.setPadding(new Insets(10, 10, 10, 10));
//            entryArea.setSpacing(20);
//
//            renameItem.setOnAction(ee -> {
//                Integer itemUid = itemSelector.getValue().getUid();
//                changesToMakeView.getItems().add(new Pair<Integer, String>(itemUid, textField.getText()));
//            });
//
//            if(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical()) {  // add either just rows or rows and columns if it is symmetric or not
//                itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getRows());
//            } else {
//                itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getRows());
//                itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols());
//            }
//
//            // create HBox for user to close with our without changes
//            Pane vSpacer = new Pane();  // used as a spacer to move HBox to the bottom
//            VBox.setVgrow(vSpacer, Priority.ALWAYS);
//            vSpacer.setMaxHeight(Double.MAX_VALUE);
//
//            HBox closeArea = new HBox();
//            Button applyButton = new Button("Apply Changes");
//            applyButton.setOnAction(ee -> {
//                for(Pair<Integer, String> item : changesToMakeView.getItems()) {
//                    if(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical()) {
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setItemNameSymmetric(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(item.getKey()), item.getValue());
//                    } else {
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setItemName(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(item.getKey()), item.getValue());
//                    }
//                }
//                window.close();
//                editor.refreshTab();
//                matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setCurrentStateAsCheckpoint();
//            });
//
//            Pane spacer = new Pane();  // used as a spacer between buttons
//            HBox.setHgrow(spacer, Priority.ALWAYS);
//            spacer.setMaxWidth(Double.MAX_VALUE);
//
//            Button cancelButton = new Button("Cancel");
//            cancelButton.setOnAction(ee -> {
//                window.close();
//            });
//            closeArea.getChildren().addAll(cancelButton, spacer, applyButton);
//
//            VBox layout = new VBox(10);
//            layout.getChildren().addAll(label, changesToMakeView, deleteSelected, entryArea, vSpacer, closeArea);
//            layout.setAlignment(Pos.CENTER);
//            layout.setPadding(new Insets(10, 10, 10, 10));
//            layout.setSpacing(10);
//
//            //Display window and wait for it to be closed before returning
//            Scene scene = new Scene(layout, 700, 350);
//            window.setScene(scene);
//            window.showAndWait();
//        });
//        renameMatrixItem.setMaxWidth(Double.MAX_VALUE);
//
//
//        appendConnections = new Button("Append Connections");
//        appendConnections.setOnAction(e -> {
//            if(editor.getFocusedMatrixUid() == null) {
//                return;
//            }
//            Stage window = new Stage();
//
//            // Create Root window
//            window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
//            window.setTitle("Append Connections");
//
//
//            // Create changes view (does not have button to remove items from it
//            Label label = new Label("Changes to be made");
//            ListView< Vector<String> > changesToMakeView = new ListView<>();  // rowUid | colUid | name | weight
//            changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//            changesToMakeView.setCellFactory(param -> new ListCell< Vector<String> >() {
//                @Override
//                protected void updateItem(Vector<String> item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null) {
//                        setText(null);
//                    } else {
//                        setText(
//                                matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(Integer.parseInt(item.get(0))).getName() + " (Row):" +
//                                matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(Integer.parseInt(item.get(1))).getName() + " (Col)" +
//                                "  {" + item.get(2) + ", " + item.get(3) + "}"
//                        );
//                    }
//                }
//            });
//            Button deleteSelected = new Button("Delete Selected Item(s)");
//            deleteSelected.setOnAction(ee -> {
//                changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
//            });
//
//
//            // area to interact with the connections
//            HBox connectionsArea = new HBox();
//            connectionsArea.setSpacing(10);
//            connectionsArea.setPadding(new Insets(10, 10, 10, 10));
//
//            // HBox area full of checklists to modify the connections, default to columns
//            HBox connectionsModifier = new HBox();
//            connectionsModifier.setSpacing(10);
//            connectionsModifier.setPadding(new Insets(10, 10, 10, 10));
//            HashMap<CheckBox, DSMItem> connections = new HashMap<>();
//
//            for(DSMItem conn : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                VBox connectionVBox = new VBox();
//                connectionVBox.setAlignment(Pos.CENTER);
//
//                Label name = new Label(conn.getName());
//                CheckBox box = new CheckBox();
//                connections.put(box, conn);
//                connectionVBox.getChildren().addAll(name, box);
//                connectionsModifier.getChildren().add(connectionVBox);
//            }
//            ScrollPane scrollPane = new ScrollPane(connectionsModifier);
//            scrollPane.setOnScroll(event -> {  // allow vertical scrolling to scroll horizontally
//                if(event.getDeltaX() == 0 && event.getDeltaY() != 0) {
//                    scrollPane.setHvalue(scrollPane.getHvalue() - event.getDeltaY() / connectionsModifier.getWidth());
//                }
//            });
//            scrollPane.setFitToHeight(true);
//
//            // vbox to choose row or column
//            VBox itemSelectorView = new VBox();
//            itemSelectorView.setMinWidth(Region.USE_PREF_SIZE);
//
//            // ComboBox to choose which row or column to modify connections of
//            ComboBox< DSMItem > itemSelector = new ComboBox<>();  // rowUid | colUid | name | weight
//            itemSelector.setCellFactory(param -> new ListCell< DSMItem >() {
//                @Override
//                protected void updateItem(DSMItem item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null) {
//                        setText(null);
//                    } else {
//                        setText(item.getName());
//                    }
//                }
//            });
//
//            itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getRows());  // default to choosing a row item
//
//            Label l = new Label("Create connections by row or column?");
//            l.setWrapText(true);
//            l.prefWidthProperty().bind(itemSelector.widthProperty());  // this will make sure the label will not be bigger than the biggest object
//            VBox.setVgrow(l, Priority.ALWAYS);
//            HBox.setHgrow(l, Priority.ALWAYS);
//            l.setMinHeight(Region.USE_PREF_SIZE);  // make sure all text will be displayed
//
//            // radio buttons
//            HBox rowColRadioButtons = new HBox();
//            HBox.setHgrow(rowColRadioButtons, Priority.ALWAYS);
//            rowColRadioButtons.setSpacing(10);
//            rowColRadioButtons.setPadding(new Insets(10, 10, 10, 10));
//            rowColRadioButtons.setMinHeight(Region.USE_PREF_SIZE);
//
//            ToggleGroup tg = new ToggleGroup();
//            RadioButton selectByRow = new RadioButton("Row");
//            RadioButton selectByCol = new RadioButton("Column");
//            HBox.setHgrow(selectByRow, Priority.ALWAYS);
//            HBox.setHgrow(selectByCol, Priority.ALWAYS);
//            selectByRow.setMinHeight(Region.USE_PREF_SIZE);
//            selectByCol.setMinHeight(Region.USE_PREF_SIZE);
//
//            selectByRow.setToggleGroup(tg);  // add RadioButtons to toggle group
//            selectByCol.setToggleGroup(tg);
//            selectByRow.setSelected(true);  // default to selectByRow
//
//            // add a change listener
//            tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
//                public void changed(ObservableValue<? extends Toggle> ob, Toggle o, Toggle n) {
//                    RadioButton rb = (RadioButton)tg.getSelectedToggle();
//                    if(rb.equals(selectByRow)) {  // clear all items and add rows to it
//                        itemSelector.getItems().removeAll(itemSelector.getItems());
//                        itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getRows());
//
//                        connectionsModifier.getChildren().removeAll(connectionsModifier.getChildren());
//                        connections.clear();
//
//                        for(DSMItem conn : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                            VBox connectionVBox = new VBox();
//                            connectionVBox.setAlignment(Pos.CENTER);
//
//                            Label name = new Label(conn.getName());
//                            CheckBox box = new CheckBox();
//                            connections.put(box, conn);
//                            connectionVBox.getChildren().addAll(name, box);
//                            connectionsModifier.getChildren().add(connectionVBox);
//                        }
//                    } else if(rb.equals(selectByCol)) {  // clear all items and add cols to it
//                        itemSelector.getItems().removeAll(itemSelector.getItems());
//                        itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols());
//
//                        connectionsModifier.getChildren().removeAll(connectionsModifier.getChildren());
//                        connections.clear();
//
//                        for(DSMItem conn : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getRows()) {
//                            VBox connectionVBox = new VBox();
//                            connectionVBox.setAlignment(Pos.CENTER);
//
//                            Label name = new Label(conn.getName());
//                            CheckBox box = new CheckBox();
//                            connections.put(box, conn);
//                            connectionVBox.getChildren().addAll(name, box);
//                            connectionsModifier.getChildren().add(connectionVBox);
//                        }
//                    } else {  // clear all items
//                        System.out.println("here");
//                        itemSelector.getItems().removeAll(itemSelector.getItems());
//
//                        connectionsModifier.getChildren().removeAll(connectionsModifier.getChildren());
//                        connections.clear();
//                    }
//                }
//            });
//            rowColRadioButtons.getChildren().addAll(selectByRow, selectByCol);
//            itemSelectorView.getChildren().addAll(l, rowColRadioButtons, itemSelector);
//
//            // add a spacer to ensure that the connections details are to the far side of the window
//            Pane connectionsSpacer = new Pane();  // used as a spacer between buttons
//            HBox.setHgrow(connectionsSpacer, Priority.ALWAYS);
//            connectionsSpacer.setMaxWidth(Double.MAX_VALUE);
//
//            // area to set details for the connection
//            VBox connectionDetailsLayout = new VBox();
//            connectionDetailsLayout.setSpacing(10);
//            connectionDetailsLayout.setPadding(new Insets(10, 10, 10, 10));
//            VBox.setVgrow(connectionDetailsLayout, Priority.ALWAYS);
//            connectionDetailsLayout.setMinWidth(Region.USE_PREF_SIZE);
//
//            TextField connectionName = new TextField();
//            NumericTextField weight = new NumericTextField(null);
//            connectionName.setPromptText("Connection Name");
//            weight.setPromptText("Connection Weight");
//            connectionName.setMinWidth(connectionName.getPrefWidth());
//            weight.setMinWidth(weight.getPrefWidth());
//
//            connectionDetailsLayout.getChildren().addAll(connectionName, weight);
//
//
//            connectionsArea.getChildren().addAll(itemSelectorView, scrollPane, connectionsSpacer, connectionDetailsLayout);
//
//            // Pane to modify the connections
//            HBox modifyPane = new HBox();
//            modifyPane.setAlignment(Pos.CENTER);
//
//            Button applyButton = new Button("Modify Connections");
//            applyButton.setOnAction(ee -> {
//                if(itemSelector.getValue() == null || connectionName.getText().isEmpty() || weight.getText().isEmpty()) {  // ensure connection can be added
//                    // TODO: add popup window saying why it cannot make the changes
//                    return;
//                }
//                for (Map.Entry<CheckBox, DSMItem> entry : connections.entrySet()) {
//                    if(entry.getKey().isSelected()) {
//                        // rowUid | colUid | name | weight
//                        Vector<String> data = new Vector<String>();
//
//                        if(((RadioButton)tg.getSelectedToggle()).equals(selectByRow)) {  // selecting by row
//                            data.add(Integer.toString(itemSelector.getValue().getUid()));  // row uid
//                            data.add(Integer.toString(entry.getValue().getUid()));  // col uid
//                        } else if(((RadioButton)tg.getSelectedToggle()).equals(selectByCol)) {  // selecting by column
//                            data.add(Integer.toString(entry.getValue().getUid()));  // row uid
//                            data.add(Integer.toString(itemSelector.getValue().getUid()));  // col uid
//                        }
//                        data.add(connectionName.getText());
//                        data.add(weight.getText());
//
//                        if(!changesToMakeView.getItems().contains(data)) {  // ensure no duplicates
//                            changesToMakeView.getItems().add(data);
//                        }
//                    }
//                }
//            });
//
//            Button applySymmetricButton = new Button("Modify Connections Symmetrically");
//            applySymmetricButton.setOnAction(ee -> {
//                if(itemSelector.getValue() == null || connectionName.getText().isEmpty() || weight.getText().isEmpty()) {  // ensure connection can be added
//                    return;
//                }
//                for (Map.Entry<CheckBox, DSMItem> entry : connections.entrySet()) {
//                    if(entry.getKey().isSelected()) {
//                        // rowUid | colUid | name | weight
//                        Vector<String> data1 = new Vector<String>();  // original connection
//                        Vector<String> data2 = new Vector<String>();  // symmetric connection
//
//                        if(((RadioButton)tg.getSelectedToggle()).equals(selectByRow)) {  // selecting by row
//                            data1.add(Integer.toString(itemSelector.getValue().getUid()));  // row uid
//                            data1.add(Integer.toString(entry.getValue().getUid()));  // col uid
//
//                            data2.add(Integer.toString(entry.getValue().getAliasUid()));  // row uid for symmetric connection
//                            // iterate over columns and find the one that corresponds to the selected row
//                            for(DSMItem item : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                                if(item.getAliasUid() == itemSelector.getValue().getUid()) {
//                                    data2.add(Integer.toString(item.getUid()));
//                                }
//                            }
//                        } else if(((RadioButton)tg.getSelectedToggle()).equals(selectByCol)) {  // selecting by column
//                            data1.add(Integer.toString(entry.getValue().getUid()));  // row uid
//                            data1.add(Integer.toString(itemSelector.getValue().getUid()));  // col uid
//
//                            data2.add(Integer.toString(itemSelector.getValue().getAliasUid()));  // row uid for symmetric connection
//                            for(DSMItem item : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                                if(item.getAliasUid() == entry.getValue().getUid()) {
//                                    data2.add(Integer.toString(item.getUid()));
//                                }
//                            }
//
//                            // iterate over columns to find the column that corresponds to the row
//                            for(DSMItem item : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                                if(item.getAliasUid() == itemSelector.getValue().getUid()) {
//                                    data2.add(Integer.toString(item.getUid()));
//                                }
//                            }
//                        }
//                        data1.add(connectionName.getText());
//                        data2.add(connectionName.getText());
//                        if(weight.getNumericValue() != null) {
//                            data1.add(weight.getNumericValue().toString());
//                            data2.add(weight.getNumericValue().toString());
//                        } else {
//                            data1.add("1.0");
//                            data2.add("1.0");
//                        }
//
//                        if(!changesToMakeView.getItems().contains(data1)) {  // ensure no duplicates
//                            changesToMakeView.getItems().add(data1);
//                        }
//                        if(!changesToMakeView.getItems().contains(data2)) {  // ensure no duplicates
//                            changesToMakeView.getItems().add(data2);
//                        }
//                    }
//                }
//            });
//            if(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical()) {  // hide button if not a symmetric matrix
//                applySymmetricButton.setManaged(false);
//                applySymmetricButton.setVisible(false);
//            }
//            modifyPane.getChildren().addAll(applyButton, applySymmetricButton);
//
//
//            // create HBox for user to close with our without changes
//            Pane vSpacer = new Pane();  // used as a spacer between buttons
//            VBox.setVgrow(vSpacer, Priority.ALWAYS);
//            vSpacer.setMaxHeight(Double.MAX_VALUE);
//
//            HBox closeArea = new HBox();
//            Button applyAllButton = new Button("Apply All Changes");
//            applyAllButton.setOnAction(ee -> {
//                for(Vector<String> item : changesToMakeView.getItems()) {  // rowUid | colUid | name | weight
//                    matrixHandler.getMatrix(editor.getFocusedMatrixUid()).modifyConnection(Integer.parseInt(item.get(0)), Integer.parseInt(item.get(1)), item.get(2), Double.parseDouble(item.get(3)));
//                }
//                window.close();
//                editor.refreshTab();
//                matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setCurrentStateAsCheckpoint();
//            });
//
//            Pane spacer = new Pane();  // used as a spacer between buttons
//            HBox.setHgrow(spacer, Priority.ALWAYS);
//            spacer.setMaxWidth(Double.MAX_VALUE);
//
//            Button cancelButton = new Button("Cancel");
//            cancelButton.setOnAction(ee -> {
//                window.close();
//            });
//            closeArea.getChildren().addAll(cancelButton, spacer, applyAllButton);
//
//
//            VBox layout = new VBox(10);
//            layout.getChildren().addAll(label, changesToMakeView, deleteSelected, connectionsArea, modifyPane, vSpacer, closeArea);
//            layout.setAlignment(Pos.CENTER);
//            layout.setPadding(new Insets(10, 10, 10, 10));
//            layout.setSpacing(10);
//
//
//            //Display window and wait for it to be closed before returning
//            Scene scene = new Scene(layout, 900, 500);
//            window.setScene(scene);
//            window.showAndWait();
//        });
//        appendConnections.setMaxWidth(Double.MAX_VALUE);
//
//
//        setConnections = new Button("Set Connections");
//        setConnections.setOnAction(e -> {
//            if(editor.getFocusedMatrixUid() == null) {
//                return;
//            }
//            Stage window = new Stage();
//
//            // Create Root window
//            window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
//            window.setTitle("Set Connections");
//
//
//            // Create changes view (does not have button to remove items from it
//            Label label = new Label("Changes to be made");
//            ListView< Vector<String> > changesToMakeView = new ListView<>();  // rowUid | colUid | name | weight
//            changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//            changesToMakeView.setCellFactory(param -> new ListCell< Vector<String> >() {
//                @Override
//                protected void updateItem(Vector<String> item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null) {
//                        setText(null);
//                    } else {
//                        setText(
//                                matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(Integer.parseInt(item.get(0))).getName() + " (Row):" +
//                                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(Integer.parseInt(item.get(1))).getName() + " (Col)" +
//                                        "  {" + item.get(2) + ", " + item.get(3) + "}"
//                        );
//                    }
//                }
//            });
//            Button deleteSelected = new Button("Delete Selected Item(s)");
//            deleteSelected.setOnAction(ee -> {
//                changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
//            });
//
//
//            // area to interact with the connections
//            HBox connectionsArea = new HBox();
//            connectionsArea.setSpacing(10);
//            connectionsArea.setPadding(new Insets(10, 10, 10, 10));
//
//            // HBox area full of checklists to modify the connections, default to columns
//            HBox connectionsModifier = new HBox();
//            connectionsModifier.setSpacing(10);
//            connectionsModifier.setPadding(new Insets(10, 10, 10, 10));
//            HashMap<CheckBox, DSMItem> connections = new HashMap<>();
//
//            for(DSMItem item : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                VBox connectionVBox = new VBox();
//                connectionVBox.setAlignment(Pos.CENTER);
//
//                Label name = new Label(item.getName());
//                CheckBox box = new CheckBox();
//                connections.put(box, item);
//                connectionVBox.getChildren().addAll(name, box);
//                connectionsModifier.getChildren().add(connectionVBox);
//            }
//            ScrollPane scrollPane = new ScrollPane(connectionsModifier);
//            scrollPane.setOnScroll(event -> {  // allow vertical scrolling to scroll horizontally
//                if(event.getDeltaX() == 0 && event.getDeltaY() != 0) {
//                    scrollPane.setHvalue(scrollPane.getHvalue() - event.getDeltaY() / connectionsModifier.getWidth());
//                }
//            });
//            scrollPane.setFitToHeight(true);
//
//            // vbox to choose row or column
//            VBox itemSelectorView = new VBox();
//            itemSelectorView.setMinWidth(Region.USE_PREF_SIZE);
//
//            // ComboBox to choose which row or column to modify connections of
//            ComboBox< DSMItem > itemSelector = new ComboBox<>();  // rowUid | colUid | name | weight
//            itemSelector.setCellFactory(param -> new ListCell< DSMItem >() {
//                @Override
//                protected void updateItem(DSMItem item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null) {
//                        setText(null);
//                    } else {
//                        setText(item.getName());
//                    }
//                }
//            });
//            itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getRows());  // default to choosing a row item
//
//            Label l = new Label("Create connections by row or column?");
//            l.setWrapText(true);
//            l.prefWidthProperty().bind(itemSelector.widthProperty());  // this will make sure the label will not be bigger than the biggest object
//            VBox.setVgrow(l, Priority.ALWAYS);
//            HBox.setHgrow(l, Priority.ALWAYS);
//            l.setMinHeight(Region.USE_PREF_SIZE);  // make sure all text will be displayed
//
//            // radio buttons
//            HBox rowColRadioButtons = new HBox();
//            HBox.setHgrow(rowColRadioButtons, Priority.ALWAYS);
//            rowColRadioButtons.setSpacing(10);
//            rowColRadioButtons.setPadding(new Insets(10, 10, 10, 10));
//            rowColRadioButtons.setMinHeight(Region.USE_PREF_SIZE);
//
//            ToggleGroup tg = new ToggleGroup();
//            RadioButton selectByRow = new RadioButton("Row");
//            RadioButton selectByCol = new RadioButton("Column");
//            HBox.setHgrow(selectByRow, Priority.ALWAYS);
//            HBox.setHgrow(selectByCol, Priority.ALWAYS);
//            selectByRow.setMinHeight(Region.USE_PREF_SIZE);
//            selectByCol.setMinHeight(Region.USE_PREF_SIZE);
//
//            selectByRow.setToggleGroup(tg);  // add RadioButtons to toggle group
//            selectByCol.setToggleGroup(tg);
//            selectByRow.setSelected(true);  // default to selectByRow
//
//            // add a change listener
//            tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {  // re-draw connections when toggled
//                public void changed(ObservableValue<? extends Toggle> ob, Toggle o, Toggle n) {
//                    RadioButton rb = (RadioButton)tg.getSelectedToggle();
//                    if(rb.equals(selectByRow)) {  // clear all items and add rows to it
//                        itemSelector.getItems().removeAll(itemSelector.getItems());
//                        itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getRows());
//
//                        connectionsModifier.getChildren().removeAll(connectionsModifier.getChildren());
//                        connections.clear();
//
//                        for(DSMItem row : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                            VBox connectionVBox = new VBox();
//                            connectionVBox.setAlignment(Pos.CENTER);
//
//                            Label name = new Label(row.getName());
//                            CheckBox box = new CheckBox();
//                            DSMItem col = itemSelector.getValue();
//
//                            connections.put(box, row);
//                            connectionVBox.getChildren().addAll(name, box);
//                            connectionsModifier.getChildren().add(connectionVBox);
//                        }
//                    } else if(rb.equals(selectByCol)) {  // clear all items and add cols to it
//                        itemSelector.getItems().removeAll(itemSelector.getItems());
//                        itemSelector.getItems().addAll(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols());
//
//                        connectionsModifier.getChildren().removeAll(connectionsModifier.getChildren());
//                        connections.clear();
//
//                        for(DSMItem col : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getRows()) {
//                            VBox connectionVBox = new VBox();
//                            connectionVBox.setAlignment(Pos.CENTER);
//
//                            Label name = new Label(col.getName());
//                            CheckBox box = new CheckBox();
//                            DSMItem row = itemSelector.getValue();
//
//                            connections.put(box, col);
//                            connectionVBox.getChildren().addAll(name, box);
//                            connectionsModifier.getChildren().add(connectionVBox);
//                        }
//                    } else {  // clear all items
//                        System.out.println("here");
//                        itemSelector.getItems().removeAll(itemSelector.getItems());
//
//                        connectionsModifier.getChildren().removeAll(connectionsModifier.getChildren());
//                        connections.clear();
//                    }
//                }
//            });
//            rowColRadioButtons.getChildren().addAll(selectByRow, selectByCol);
//            itemSelectorView.getChildren().addAll(l, rowColRadioButtons, itemSelector);
//
//            itemSelector.setOnAction(ee -> {  // when item changes, change the connections that are selected
//                if(itemSelector.getValue() == null) {  // ensure connection can be added
//                    return;
//                }
//                for (Map.Entry<CheckBox, DSMItem> entry : connections.entrySet()) {
//                    RadioButton rb = (RadioButton)tg.getSelectedToggle();
//                    if(rb.equals(selectByRow) && matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getConnection(itemSelector.getValue().getUid(), entry.getValue().getUid()) != null) {
//                        entry.getKey().setSelected(true);
//                    } else if(rb.equals(selectByCol) && matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getConnection(entry.getValue().getUid(), itemSelector.getValue().getUid()) != null) {
//                        entry.getKey().setSelected(true);
//                    } else {
//                        entry.getKey().setSelected(false);
//                    }
//                }
//            });
//
//            // add a spacer to ensure that the connections details are to the far side of the window
//            Pane connectionsSpacer = new Pane();  // used as a spacer between buttons
//            HBox.setHgrow(connectionsSpacer, Priority.ALWAYS);
//            connectionsSpacer.setMaxWidth(Double.MAX_VALUE);
//
//            // area to set details for the connection
//            VBox connectionDetailsLayout = new VBox();
//            connectionDetailsLayout.setSpacing(10);
//            connectionDetailsLayout.setPadding(new Insets(10, 10, 10, 10));
//            VBox.setVgrow(connectionDetailsLayout, Priority.ALWAYS);
//            connectionDetailsLayout.setMinWidth(Region.USE_PREF_SIZE);
//
//            TextField connectionName = new TextField();
//            NumericTextField weight = new NumericTextField(null);
//            connectionName.setPromptText("Connection Name");
//            weight.setPromptText("Connection Weight");
//            connectionName.setMinWidth(connectionName.getPrefWidth());
//            weight.setMinWidth(weight.getPrefWidth());
//
//            connectionDetailsLayout.getChildren().addAll(connectionName, weight);
//
//
//            connectionsArea.getChildren().addAll(itemSelectorView, scrollPane, connectionsSpacer, connectionDetailsLayout);
//
//            // Pane to modify the connections
//            HBox modifyPane = new HBox();
//            modifyPane.setAlignment(Pos.CENTER);
//
//            Button applyButton = new Button("Modify Connections");
//            applyButton.setOnAction(ee -> {
//                if(itemSelector.getValue() == null) {  // ensure connection can be added and allow empty connections because that means to delete them
//                    // TODO: add popup window saying why it cannot make the changes
//                    return;
//                }
//                for (Map.Entry<CheckBox, DSMItem> entry : connections.entrySet()) {
//                    if(entry.getKey().isSelected()) {
//                        // rowUid | colUid | name | weight | add/del
//                        Vector<String> data = new Vector<>();
//
//                        if((tg.getSelectedToggle()).equals(selectByRow)) {  // selecting by row
//                            data.add(Integer.toString(itemSelector.getValue().getUid()));  // row uid
//                            data.add(Integer.toString(entry.getValue().getUid()));  // col uid
//                        } else if((tg.getSelectedToggle()).equals(selectByCol)) {  // selecting by column
//                            data.add(Integer.toString(entry.getValue().getUid()));  // row uid
//                            data.add(Integer.toString(itemSelector.getValue().getUid()));  // col uid
//                        }
//                        data.add(connectionName.getText());
//                        if(weight.getNumericValue() != null) {
//                            data.add(weight.getNumericValue().toString());
//                        } else {
//                            data.add("1.0");
//                        }
//
//                        data.add("add");
//
//                        if(!changesToMakeView.getItems().contains(data)) {  // ensure no duplicates
//                            changesToMakeView.getItems().add(data);
//                        }
//                    } else {
//                        // rowUid | colUid | name | weight | add/del
//                        Vector<String> data = new Vector<String>();
//
//                        if(((RadioButton)tg.getSelectedToggle()).equals(selectByRow)) {  // selecting by row
//                            data.add(Integer.toString(itemSelector.getValue().getUid()));  // row uid
//                            data.add(Integer.toString(entry.getValue().getUid()));  // col uid
//                        } else if(((RadioButton)tg.getSelectedToggle()).equals(selectByCol)) {  // selecting by column
//                            data.add(Integer.toString(entry.getValue().getUid()));  // row uid
//                            data.add(Integer.toString(itemSelector.getValue().getUid()));  // col uid
//                        }
//
//                        data.add(null);
//                        data.add(null);
//
//                        data.add("del");
//
//                        if(!changesToMakeView.getItems().contains(data)) {  // ensure no duplicates
//                            changesToMakeView.getItems().add(data);
//                        }
//                    }
//                }
//            });
//
//            Button applySymmetricButton = new Button("Modify Connections Symmetrically");
//            applySymmetricButton.setOnAction(ee -> {
//                if(itemSelector.getValue() == null) {  // ensure connection can be added
//                    return;
//                }
//                for (Map.Entry<CheckBox, DSMItem> entry : connections.entrySet()) {
//                    if(entry.getKey().isSelected()) {
//                        // rowUid | colUid | name | weight | add/del
//                        Vector<String> data1 = new Vector<String>();  // original connection
//                        Vector<String> data2 = new Vector<String>();  // symmetric connection
//
//                        if(((RadioButton)tg.getSelectedToggle()).equals(selectByRow)) {  // selecting by row
//                            data1.add(Integer.toString(itemSelector.getValue().getUid()));  // row uid
//                            data1.add(Integer.toString(entry.getValue().getUid()));  // col uid
//
//                            data2.add(Integer.toString(entry.getValue().getAliasUid()));  // row uid for symmetric connection
//                            // iterate over columns and find the one that corresponds to the selected row
//                            for(DSMItem item : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                                if(item.getAliasUid() == itemSelector.getValue().getUid()) {
//                                    data2.add(Integer.toString(item.getUid()));
//                                }
//                            }
//                        } else if(((RadioButton)tg.getSelectedToggle()).equals(selectByCol)) {  // selecting by column
//                            data1.add(Integer.toString(entry.getValue().getUid()));  // row uid
//                            data1.add(Integer.toString(itemSelector.getValue().getUid()));  // col uid
//
//                            data2.add(Integer.toString(itemSelector.getValue().getAliasUid()));  // row uid for symmetric connection
//                            for(DSMItem item : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                                if(item.getAliasUid() == entry.getValue().getUid()) {
//                                    data2.add(Integer.toString(item.getUid()));
//                                }
//                            }
//
//                            // iterate over columns to find the column that corresponds to the row
//                            for(DSMItem item : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                                if(item.getAliasUid() == itemSelector.getValue().getUid()) {
//                                    data2.add(Integer.toString(item.getUid()));
//                                }
//                            }
//                        }
//                        data1.add(connectionName.getText());
//                        data1.add(weight.getText());
//                        data1.add("add");
//
//                        data2.add(connectionName.getText());
//                        data2.add(weight.getText());
//                        data2.add("add");
//
//                        if(!changesToMakeView.getItems().contains(data1)) {  // ensure no duplicates
//                            changesToMakeView.getItems().add(data1);
//                        }
//                        if(!changesToMakeView.getItems().contains(data2)) {  // ensure no duplicates
//                            changesToMakeView.getItems().add(data2);
//                        }
//                    } else {
//                        // rowUid | colUid | name | weight | add/del
//                        Vector<String> data1 = new Vector<String>();  // original connection
//                        Vector<String> data2 = new Vector<String>();  // symmetric connection
//
//                        if(((RadioButton)tg.getSelectedToggle()).equals(selectByRow)) {  // selecting by row
//                            data1.add(Integer.toString(itemSelector.getValue().getUid()));  // row uid
//                            data1.add(Integer.toString(entry.getValue().getUid()));  // col uid
//
//                            data2.add(Integer.toString(entry.getValue().getAliasUid()));  // row uid for symmetric connection
//                            // iterate over columns and find the one that corresponds to the selected row
//                            for(DSMItem item : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                                if(item.getAliasUid() == itemSelector.getValue().getUid()) {
//                                    data2.add(Integer.toString(item.getUid()));
//                                }
//                            }
//                        } else if(((RadioButton)tg.getSelectedToggle()).equals(selectByCol)) {  // selecting by column
//                            data1.add(Integer.toString(entry.getValue().getUid()));  // row uid
//                            data1.add(Integer.toString(itemSelector.getValue().getUid()));  // col uid
//
//                            data2.add(Integer.toString(itemSelector.getValue().getAliasUid()));  // row uid for symmetric connection
//                            for(DSMItem item : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                                if(item.getAliasUid() == entry.getValue().getUid()) {
//                                    data2.add(Integer.toString(item.getUid()));
//                                }
//                            }
//
//                            // iterate over columns to find the column that corresponds to the row
//                            for(DSMItem item : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getCols()) {
//                                if(item.getAliasUid() == itemSelector.getValue().getUid()) {
//                                    data2.add(Integer.toString(item.getUid()));
//                                }
//                            }
//                        }
//                        data1.add(null);
//                        data1.add(null);
//                        data1.add("del");
//
//                        data2.add(null);
//                        data2.add(null);
//                        data2.add("del");
//
//                        if(!changesToMakeView.getItems().contains(data1)) {  // ensure no duplicates
//                            changesToMakeView.getItems().add(data1);
//                        }
//                        if(!changesToMakeView.getItems().contains(data2)) {  // ensure no duplicates
//                            changesToMakeView.getItems().add(data2);
//                        }
//                    }
//                }
//            });
//            if(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical()) {  // hide button if not a symmetric matrix
//                applySymmetricButton.setManaged(false);
//                applySymmetricButton.setVisible(false);
//            }
//            modifyPane.getChildren().addAll(applyButton, applySymmetricButton);
//
//            // create HBox for user to close with our without changes
//            Pane vSpacer = new Pane();  // used as a spacer to move HBox to the bottom
//            VBox.setVgrow(vSpacer, Priority.ALWAYS);
//            vSpacer.setMaxHeight(Double.MAX_VALUE);
//
//            HBox closeArea = new HBox();
//            Button applyAllButton = new Button("Apply All Changes");
//            applyAllButton.setOnAction(ee -> {
//                for(Vector<String> item : changesToMakeView.getItems()) {  // rowUid | colUid | name | weight | add/del
//                    if(item.get(4).equals("add")) {
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).modifyConnection(Integer.parseInt(item.get(0)), Integer.parseInt(item.get(1)), item.get(2), Double.parseDouble(item.get(3)));
//                    } else {
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).deleteConnection(Integer.parseInt(item.get(0)), Integer.parseInt(item.get(1)));
//                    }
//                }
//                window.close();
//                editor.refreshTab();
//                matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setCurrentStateAsCheckpoint();
//            });
//
//            Pane spacer = new Pane();  // used as a spacer between buttons
//            HBox.setHgrow(spacer, Priority.ALWAYS);
//            spacer.setMaxWidth(Double.MAX_VALUE);
//
//            Button cancelButton = new Button("Cancel");
//            cancelButton.setOnAction(ee -> {
//                window.close();
//            });
//            closeArea.getChildren().addAll(cancelButton, spacer, applyAllButton);
//
//
//            VBox layout = new VBox(10);
//            layout.getChildren().addAll(label, changesToMakeView, deleteSelected, connectionsArea, modifyPane, vSpacer, closeArea);
//            layout.setAlignment(Pos.CENTER);
//            layout.setPadding(new Insets(10, 10, 10, 10));
//            layout.setSpacing(10);
//
//
//            //Display window and wait for it to be closed before returning
//            Scene scene = new Scene(layout, 900, 500);
//            window.setScene(scene);
//            window.showAndWait();
//        });
//        setConnections.setMaxWidth(Double.MAX_VALUE);
//
//
//        deleteConnections = new Button("Delete Connections");
//        deleteConnections.setOnAction(e -> {
//            if(editor.getFocusedMatrixUid() == null) {
//                return;
//            }
//            SymmetricDSM matrix = matrixHandler.getMatrix(editor.getFocusedMatrixUid());
//            Stage window = new Stage();
//
//            // Create Root window
//            window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
//            window.setTitle("Delete Connections");
//
//
//            // Create changes view (does not have button to remove items from it
//            Label label = new Label("Changes to be made");
//            ListView< Pair<Integer, Integer> > changesToMakeView = new ListView<>();  // rowUid | colUid
//            changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//            changesToMakeView.setCellFactory(param -> new ListCell< Pair<Integer, Integer> >() {
//                @Override
//                protected void updateItem(Pair<Integer, Integer> item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null) {
//                        setText(null);
//                    } else {
//                        setText(
//                            "DELETE " +
//                            matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(item.getKey()).getName() + ":" +
//                            matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getItem(item.getValue()).getName()
//                        );
//                    }
//                }
//            });
//
//            Button deleteSelected = new Button("Delete Selected Item(s)");
//            deleteSelected.setOnAction(ee -> {
//                changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
//            });
//
//
//            // Create area for the user to choose the connection they want to remove
//            HBox entryArea = new HBox();
//
//            // ComboBox to choose which row or column to modify connections of
//            ComboBox<Integer> firstItemSelector = new ComboBox<>();
//            // function to set text of comboBox items
//            Callback<ListView<Integer>, ListCell<Integer>> cellFactory = new Callback<>() {
//                @Override
//                public ListCell<Integer> call(ListView<Integer> l) {
//                    return new ListCell<Integer>() {
//
//                        @Override
//                        protected void updateItem(Integer item, boolean empty) {
//                            super.updateItem(item, empty);
//                            if (item == null || empty) {
//                                setGraphic(null);
//                            } else {
//                                if(item == Integer.MAX_VALUE) {
//                                    setText("All");
//                                } else if(matrix.isRow(matrix.getItem(item).getUid())) {
//                                    setText(matrix.getItem(item).getName() + " (Row)");
//                                } else {
//                                    setText(matrix.getItem(item).getName() + " (Column)");
//                                }
//                            }
//                        }
//                    };
//                }
//            };
//            firstItemSelector.setButtonCell(cellFactory.call(null));
//            firstItemSelector.setCellFactory(cellFactory);
//            Vector<Integer> items = new Vector<>();
//            items.add(Integer.MAX_VALUE);  // this will be used for selecting all items
//            for(DSMItem row : matrix.getRows()) {
//                items.add(row.getUid());
//            }
//            for(DSMItem col : matrix.getCols()) {
//                items.add(col.getUid());
//            }
//            firstItemSelector.getItems().addAll(items);
//
//
//            firstItemSelector.setMaxWidth(Double.MAX_VALUE);
//            HBox.setHgrow(firstItemSelector, Priority.ALWAYS);
//            firstItemSelector.setPromptText("From");
//
//
//            // ComboBox to choose the second item of the connection (row or column)
//            ObservableList<Integer> connectionItems = FXCollections.observableArrayList();
//
//            ComboBox<Integer> secondItemSelector = new ComboBox<>();
//            secondItemSelector.setButtonCell(cellFactory.call(null));
//            secondItemSelector.setCellFactory(cellFactory);
//            secondItemSelector.setItems(connectionItems);
//
//            secondItemSelector.setMaxWidth(Double.MAX_VALUE);
//            HBox.setHgrow(secondItemSelector, Priority.ALWAYS);
//            secondItemSelector.setPromptText("To");
//
//            // add listener to first item to update options in second column
//            firstItemSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
//                if(matrix.isRow(newValue)) {
//                    connectionItems.clear();
//                    connectionItems.add(Integer.MAX_VALUE);  // add the all option
//                    for(DSMItem col : matrix.getCols()) {
//                        if(matrix.getConnection(newValue, col.getUid()) != null) {
//                            connectionItems.add(col.getUid());
//                        }
//                    }
//                } else {
//                    connectionItems.clear();
//                    connectionItems.add(Integer.MAX_VALUE);  // add the all option
//                    for(DSMItem row : matrix.getRows()) {
//                        if(matrix.getConnection(row.getUid(), newValue) != null) {
//                            connectionItems.add(row.getUid());
//                        }
//                    }
//                }
//
//                secondItemSelector.getSelectionModel().clearSelection();  // clear selection of the second item
//                secondItemSelector.setValue(null);
//            });
//
//            Button deleteConnection = new Button("Delete Connection");
//            deleteConnection.setMaxWidth(Double.MAX_VALUE);
//            HBox.setHgrow(deleteConnection, Priority.ALWAYS);
//
//            Button deleteConnectionSymmetrically = new Button("Delete Connection Symmetrically");
//            deleteConnectionSymmetrically.setMaxWidth(Double.MAX_VALUE);
//            HBox.setHgrow(deleteConnectionSymmetrically, Priority.ALWAYS);
//
//            entryArea.getChildren().addAll(firstItemSelector, secondItemSelector, deleteConnection);
//            if(matrix.isSymmetrical()) {
//                entryArea.getChildren().add(deleteConnectionSymmetrically);
//            }
//            entryArea.setPadding(new Insets(10, 10, 10, 10));
//            entryArea.setSpacing(20);
//
//            deleteConnection.setOnAction(ee -> {
//                if(firstItemSelector.getValue() == null || secondItemSelector.getValue() == null) return;
//
//                if(firstItemSelector.getValue() == Integer.MAX_VALUE) {
//                    ArrayList<Integer> toDelete = new ArrayList<>();
//                    if(secondItemSelector.getValue() == Integer.MAX_VALUE) {
//                        for(DSMItem col : matrix.getCols()) {
//                            toDelete.add(col.getUid());
//                        }
//                    } else {
//                        toDelete.add(secondItemSelector.getValue());
//                    }
//
//                    for(DSMItem row : matrix.getRows()) {
//                        for(Integer i : toDelete) {
//                            if(matrix.getConnection(row.getUid(), i) != null) {
//                                safeAddConnectionChange(changesToMakeView, row.getUid(), i);
//                            }
//                        }
//                    }
//                } else if(secondItemSelector.getValue() == Integer.MAX_VALUE) {
//                    for(DSMItem col : matrix.getCols()) {
//                        if(matrix.getConnection(firstItemSelector.getValue(), col.getUid()) != null) {
//                            safeAddConnectionChange(changesToMakeView, firstItemSelector.getValue(), col.getUid());
//                        }
//                    }
//                } else if(matrix.isRow(firstItemSelector.getValue())) {
//                    safeAddConnectionChange(changesToMakeView, firstItemSelector.getValue(), secondItemSelector.getValue());
//                } else {
//                    safeAddConnectionChange(changesToMakeView, secondItemSelector.getValue(), firstItemSelector.getValue());
//                }
//            });
//            deleteConnectionSymmetrically.setOnAction(ee -> {
//                if(firstItemSelector.getValue() == null || secondItemSelector.getValue() == null) return;
//
//                if(firstItemSelector.getValue() == Integer.MAX_VALUE) {
//                    ArrayList<Integer> toDelete = new ArrayList<>();
//                    if(secondItemSelector.getValue() == Integer.MAX_VALUE) {  // if all then add all the columns
//                        for(DSMItem col : matrix.getCols()) {
//                            toDelete.add(col.getUid());
//                        }
//                    } else {                                                  // else then only add the second value
//                        toDelete.add(secondItemSelector.getValue());
//                    }
//
//                    for(DSMItem row : matrix.getRows()) {
//                        for(Integer colUid : toDelete) {
//                            if(matrix.getConnection(row.getUid(), colUid) != null) {
//                                safeAddConnectionChange(changesToMakeView, row.getUid(), colUid);
//                                Pair<Integer, Integer> uids = matrix.getSymmetricConnectionUids(row.getUid(), colUid);
//                                if(matrix.getConnection(uids.getKey(), uids.getValue()) != null) {
//                                    safeAddConnectionChange(changesToMakeView, uids.getKey(), uids.getValue());
//                                }
//                            }
//                        }
//                    }
//                } else if(secondItemSelector.getValue() == Integer.MAX_VALUE) {
//                    for(DSMItem col : matrix.getCols()) {
//                        if(matrix.getConnection(firstItemSelector.getValue(), col.getUid()) != null) {
//                            safeAddConnectionChange(changesToMakeView, firstItemSelector.getValue(), col.getUid());
//                            Pair<Integer, Integer> uids = matrix.getSymmetricConnectionUids(firstItemSelector.getValue(), col.getUid());
//                            if(matrix.getConnection(uids.getKey(), uids.getValue()) != null) {
//                                safeAddConnectionChange(changesToMakeView, uids.getKey(), uids.getValue());
//                            }
//                        }
//                    }
//                } else if(matrix.isRow(firstItemSelector.getValue())) {
//                    safeAddConnectionChange(changesToMakeView, firstItemSelector.getValue(), secondItemSelector.getValue());
//                    Pair<Integer, Integer> uids = matrix.getSymmetricConnectionUids(firstItemSelector.getValue(), secondItemSelector.getValue());
//                    if(matrix.getConnection(uids.getKey(), uids.getValue()) != null) {
//                        safeAddConnectionChange(changesToMakeView, uids.getKey(), uids.getValue());
//                    }
//                } else {
//                    safeAddConnectionChange(changesToMakeView, secondItemSelector.getValue(), firstItemSelector.getValue());
//                    Pair<Integer, Integer> uids = matrix.getSymmetricConnectionUids(secondItemSelector.getValue(), firstItemSelector.getValue());
//                    if(matrix.getConnection(uids.getKey(), uids.getValue()) != null) {
//                        safeAddConnectionChange(changesToMakeView, uids.getKey(), uids.getValue());
//                    }
//                }
//
//            });
//
//
//            // create HBox for user to close with our without changes
//            Pane vSpacer = new Pane();  // used as a spacer to move HBox to the bottom
//            VBox.setVgrow(vSpacer, Priority.ALWAYS);
//            vSpacer.setMaxHeight(Double.MAX_VALUE);
//
//            HBox closeArea = new HBox();
//            Button applyAllButton = new Button("Apply All Changes");
//            applyAllButton.setOnAction(ee -> {
//                for(Pair<Integer, Integer> item : changesToMakeView.getItems()) {  // rowUid | colUid
//                    matrixHandler.getMatrix(editor.getFocusedMatrixUid()).deleteConnection(item.getKey(), item.getValue());
//                }
//                window.close();
//                editor.refreshTab();
//                matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setCurrentStateAsCheckpoint();
//            });
//
//            Pane spacer = new Pane();  // used as a spacer between buttons
//            HBox.setHgrow(spacer, Priority.ALWAYS);
//            spacer.setMaxWidth(Double.MAX_VALUE);
//
//            Button cancelButton = new Button("Cancel");
//            cancelButton.setOnAction(ee -> {
//                window.close();
//            });
//            closeArea.getChildren().addAll(cancelButton, spacer, applyAllButton);
//
//
//            VBox layout = new VBox(10);
//            layout.getChildren().addAll(label, changesToMakeView, deleteSelected, entryArea, vSpacer, closeArea);
//            layout.setAlignment(Pos.CENTER);
//            layout.setPadding(new Insets(10, 10, 10, 10));
//            layout.setSpacing(10);
//
//
//            //Display window and wait for it to be closed before returning
//            Scene scene = new Scene(layout, 1100, 500);
//            window.setScene(scene);
//            window.showAndWait();
//        });
//        deleteConnections.setMaxWidth(Double.MAX_VALUE);
//
//
//        configureGroupings = new Button("Configure Groupings");
//        configureGroupings.setOnAction(e -> {
//            if(editor.getFocusedMatrixUid() == null) {
//                return;
//            }
//            Stage window = new Stage();
//
//            // Create Root window
//            window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
//            window.setTitle("Configure Groupings");
//
//            VBox currentGroupings = new VBox();
//            ScrollPane currentGroupingsPane = new ScrollPane(currentGroupings);
//            currentGroupingsPane.setFitToWidth(true);
//
//            for(Map.Entry<String, Color> entry : matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getGroupingColors().entrySet()) {
//                HBox display = new HBox();
//
//                Label groupingName = new Label(entry.getKey());
//                Pane groupingSpacer = new Pane();
//                groupingSpacer.setMaxWidth(Double.MAX_VALUE);
//                HBox.setHgrow(groupingSpacer, Priority.ALWAYS);
//                ColorPicker groupingColor = new ColorPicker(entry.getValue());
//                display.getChildren().addAll(groupingName, groupingSpacer, groupingColor);
//                currentGroupings.getChildren().add(display);
//            }
//
//            // area to add, delete, rename
//            HBox modifyArea = new HBox();
//            modifyArea.setAlignment(Pos.CENTER);
//
//            Button addButton = new Button("Add New Grouping");
//            addButton.setOnAction(ee -> {
//                if(editor.getFocusedMatrixUid() == null) {
//                    return;
//                }
//                Stage addWindow = new Stage();
//
//                // Create Root window
//                addWindow.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
//                addWindow.setTitle("Add Grouping");
//
//                TextField newName = new TextField();
//                newName.setPromptText("Grouping Name");
//                newName.setMaxWidth(Double.MAX_VALUE);
//                HBox.setHgrow(newName, Priority.ALWAYS);
//
//                Pane vSpacer = new Pane();  // used as a spacer between buttons
//                VBox.setVgrow(vSpacer, Priority.ALWAYS);
//                vSpacer.setMaxHeight(Double.MAX_VALUE);
//
//                // create HBox for user to close with or without changes
//                HBox closeArea = new HBox();
//                Button applyAllButton = new Button("Ok");
//                applyAllButton.setOnAction(eee -> {
//                    if(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getGroupingColors().containsKey(newName.getText())) {
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).addGrouping(newName.getText(), null);
//
//                        HBox display = new HBox();
//                        Label groupingName = new Label(newName.getText());
//                        Pane groupingSpacer = new Pane();
//                        groupingSpacer.setMaxWidth(Double.MAX_VALUE);
//                        HBox.setHgrow(groupingSpacer, Priority.ALWAYS);
//                        ColorPicker groupingColor = new ColorPicker(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getGroupingColors().get(newName.getText()));
//                        display.getChildren().addAll(groupingName, groupingSpacer, groupingColor);
//                        currentGroupings.getChildren().add(display);
//                    }
//                    addWindow.close();
//                });
//
//                Pane spacer = new Pane();  // used as a spacer between buttons
//                HBox.setHgrow(spacer, Priority.ALWAYS);
//                spacer.setMaxWidth(Double.MAX_VALUE);
//
//                Button cancelButton = new Button("Cancel");
//                cancelButton.setOnAction(eee -> {
//                    addWindow.close();
//                });
//                closeArea.getChildren().addAll(cancelButton, spacer, applyAllButton);
//
//                VBox addLayout = new VBox(10);
//                addLayout.getChildren().addAll(newName, vSpacer, closeArea);
//                addLayout.setAlignment(Pos.CENTER);
//                addLayout.setPadding(new Insets(10, 10, 10, 10));
//                addLayout.setSpacing(10);
//
//
//                //Display window and wait for it to be closed before returning
//                Scene scene = new Scene(addLayout, 300, 100);
//                addWindow.setScene(scene);
//                addWindow.showAndWait();
//            });
//
//            Button renameButton = new Button("Rename Grouping");
//            renameButton.setOnAction(ee -> {
//                if(editor.getFocusedMatrixUid() == null) {
//                    return;
//                }
//                Stage renameWindow = new Stage();
//
//                // Create Root window
//                renameWindow.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
//                renameWindow.setTitle("Rename Grouping");
//
//                HBox renameLayout = new HBox();
//                ComboBox<String> currentItems = new ComboBox<>();
//                Vector<String> groupings = new Vector<>(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getGroupingColors().keySet());
//                groupings.remove("(None)");
//                currentItems.getItems().addAll(groupings);
//                currentItems.setMaxWidth(Double.MAX_VALUE);
//                HBox.setHgrow(currentItems, Priority.ALWAYS);
//
//                TextField newName = new TextField();
//                newName.setPromptText("Grouping Name");
//                newName.setMaxWidth(Double.MAX_VALUE);
//
//                renameLayout.getChildren().addAll(currentItems, newName);
//
//                Pane vSpacer = new Pane();  // used as a spacer between buttons
//                VBox.setVgrow(vSpacer, Priority.ALWAYS);
//                vSpacer.setMaxHeight(Double.MAX_VALUE);
//
//                // create HBox for user to close with our without changes
//                HBox closeArea = new HBox();
//                Button applyAllButton = new Button("Ok");
//                applyAllButton.setOnAction(eee -> {
//                    // key must not be empty and must not already exist
//                    if(!currentItems.getValue().equals("") && !matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getGroupingColors().containsKey(newName.getText())) {
//                        for(Node grouping : currentGroupings.getChildren()) {  // delete the old object from the pane
//                            HBox area = (HBox)grouping;
//                            for(Node item : area.getChildren()) {
//                                if(item.getClass().equals(Label.class)) {
//                                    Label l = (Label)item;
//                                    if(l.getText().equals(currentItems.getValue())) {  // update the text of the label that was displayed in the drop down box
//                                        Platform.runLater(new Runnable() {  // this allows a thread to update the gui
//                                            @Override
//                                            public void run() {
//                                                l.setText(newName.getText());
//                                        }});
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).renameGrouping(currentItems.getValue(), newName.getText());
//
//                    }  // TODO: add some kind of notification saying grouping cannot be empty or exist already
//                    renameWindow.close();
//                    editor.refreshTab();
//                });
//
//                Pane spacer = new Pane();  // used as a spacer between buttons
//                HBox.setHgrow(spacer, Priority.ALWAYS);
//                spacer.setMaxWidth(Double.MAX_VALUE);
//
//                Button cancelButton = new Button("Cancel");
//                cancelButton.setOnAction(eee -> {
//                    renameWindow.close();
//                });
//                closeArea.getChildren().addAll(cancelButton, spacer, applyAllButton);
//
//                VBox addLayout = new VBox(10);
//                addLayout.getChildren().addAll(renameLayout, vSpacer, closeArea);
//                addLayout.setAlignment(Pos.CENTER);
//                addLayout.setPadding(new Insets(10, 10, 10, 10));
//                addLayout.setSpacing(10);
//
//
//                //Display window and wait for it to be closed before returning
//                Scene scene = new Scene(addLayout, 300, 100);
//                renameWindow.setScene(scene);
//                renameWindow.showAndWait();
//            });
//
//            Button deleteButton = new Button("Delete Grouping");
//            deleteButton.setOnAction(ee -> {
//                if(editor.getFocusedMatrixUid() == null) {
//                    return;
//                }
//                Stage deleteWindow = new Stage();
//
//                // Create Root window
//                deleteWindow.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
//                deleteWindow.setTitle("Delete Grouping");
//
//                ComboBox<String> currentItems = new ComboBox<>();
//                Vector<String> groupings = new Vector<>(matrixHandler.getMatrix(editor.getFocusedMatrixUid()).getGroupingColors().keySet());
//                groupings.remove("(None)");
//                currentItems.getItems().addAll(groupings);
//                currentItems.setMaxWidth(Double.MAX_VALUE);
//                HBox.setHgrow(currentItems, Priority.ALWAYS);
//
//                Pane vSpacer = new Pane();  // used as a spacer between buttons
//                VBox.setVgrow(vSpacer, Priority.ALWAYS);
//                vSpacer.setMaxHeight(Double.MAX_VALUE);
//
//                // create HBox for user to close with our without changes
//                HBox closeArea = new HBox();
//                Button applyAllButton = new Button("Ok");
//                applyAllButton.setOnAction(eee -> {
//                    // key must not be empty and must not already exist
//                    if(currentItems.getValue() != "") {
//                        for(Node grouping : currentGroupings.getChildren()) {  // delete the old object from the window
//                            HBox area = (HBox)grouping;
//                            for(Node item : area.getChildren()) {
//                                if(item.getClass().equals(Label.class)) {
//                                    Label l = (Label)item;
//                                    if(l.getText().equals(currentItems.getValue())) {
//                                        Platform.runLater(new Runnable() {  // this allows a thread to update the gui
//                                            @Override
//                                            public void run() {
//                                                currentGroupings.getChildren().remove(area);
//                                            }});
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                        matrixHandler.getMatrix(editor.getFocusedMatrixUid()).removeGrouping(currentItems.getValue());
//                    }  // TODO: add some kind of notification saying grouping cannot be empty or exist already
//                    deleteWindow.close();
//                });
//
//                Pane spacer = new Pane();  // used as a spacer between buttons
//                HBox.setHgrow(spacer, Priority.ALWAYS);
//                spacer.setMaxWidth(Double.MAX_VALUE);
//
//                Button cancelButton = new Button("Cancel");
//                cancelButton.setOnAction(eee -> {
//                    deleteWindow.close();
//                });
//                closeArea.getChildren().addAll(cancelButton, spacer, applyAllButton);
//
//                VBox addLayout = new VBox(10);
//                addLayout.getChildren().addAll(currentItems, vSpacer, closeArea);
//                addLayout.setAlignment(Pos.CENTER);
//                addLayout.setPadding(new Insets(10, 10, 10, 10));
//                addLayout.setSpacing(10);
//
//
//                //Display window and wait for it to be closed before returning
//                Scene scene = new Scene(addLayout, 300, 100);
//                deleteWindow.setScene(scene);
//                deleteWindow.showAndWait();
//            });
//
//            modifyArea.getChildren().addAll(addButton, renameButton, deleteButton);
//
//            // create HBox for user to close with our without changes
//            Pane vSpacer = new Pane();  // used as a spacer to move HBox to the bottom
//            VBox.setVgrow(vSpacer, Priority.ALWAYS);
//            vSpacer.setMaxHeight(Double.MAX_VALUE);
//
//            HBox closeArea = new HBox();
//            Button applyAllButton = new Button("Ok");
//
//            applyAllButton.setOnAction(ee -> {
//                for(Node grouping : currentGroupings.getChildren()) {  // update the colors
//                    HBox area = (HBox) grouping;
//                    String groupingName = null;
//                    Color groupingColor = null;
//                    for (Node item : area.getChildren()) {
//                        if (item.getClass().equals(Label.class)) {
//                            groupingName = ((Label)item).getText();
//                        } else if (item.getClass().equals(ColorPicker.class)) {
//                            ColorPicker c = (ColorPicker)item;
//                            if(c.getValue() != null) {  // if color is selected
//                                groupingColor = Color.color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue());
//                            }
//                        }
//                    }
//                    assert(groupingName != null) : "could not find name on screen when configuring groupings";
//                    matrixHandler.getMatrix(editor.getFocusedMatrixUid()).updateGroupingColor(groupingName, groupingColor);
//                }
//                window.close();
//                editor.refreshTab();
//            });
//
//            Pane spacer = new Pane();  // used as a spacer between buttons
//            HBox.setHgrow(spacer, Priority.ALWAYS);
//            spacer.setMaxWidth(Double.MAX_VALUE);
//
//            closeArea.getChildren().addAll(spacer, applyAllButton);
//
//
//            VBox layout = new VBox(10);
//            layout.getChildren().addAll(currentGroupingsPane, modifyArea, vSpacer, closeArea);
//            layout.setAlignment(Pos.CENTER);
//            layout.setPadding(new Insets(10, 10, 10, 10));
//            layout.setSpacing(10);
//
//
//            //Display window and wait for it to be closed before returning
//            Scene scene = new Scene(layout, 500, 300);
//            window.setScene(scene);
//            scene.getWindow().setOnCloseRequest(ee -> {
//                for (Node grouping : currentGroupings.getChildren()) {  // update the colors
//                    HBox area = (HBox) grouping;
//                    String groupingName = null;
//                    Color groupingColor = null;
//                    for (Node item : area.getChildren()) {
//                        if (item.getClass().equals(Label.class)) {
//                            groupingName = ((Label) item).getText();
//                        } else if (item.getClass().equals(ColorPicker.class)) {
//                            ColorPicker c = (ColorPicker) item;
//                            if (c.getValue() != null) {  // if color is selected
//                                groupingColor = Color.color(c.getValue().getRed(), c.getValue().getGreen(), c.getValue().getBlue());
//                            }
//                        }
//                    }
//                    assert (groupingName != null) : "could not find name on screen when configuring groupings";
//                    matrixHandler.getMatrix(editor.getFocusedMatrixUid()).updateGroupingColor(groupingName, groupingColor);
//                }
//                editor.refreshTab();
//                matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setCurrentStateAsCheckpoint();
//            });
//            window.showAndWait();
//
//        });
//        configureGroupings.setMaxWidth(Double.MAX_VALUE);
//
//
//
//
//        sort = new Button("Sort");
//        sort.setOnAction(e -> {
//            editor.refreshTab();
//        });
//        sort.setMaxWidth(Double.MAX_VALUE);
//
//
//
//
//        reDistributeIndices = new Button("Re-Distribute Sort Indices");
//        reDistributeIndices.setOnAction(e -> {
//            if(editor.getFocusedMatrixUid() == null) {
//                return;
//            }
//            matrixHandler.getMatrix(editor.getFocusedMatrixUid()).reDistributeSortIndices();
//            editor.refreshTab();
//            matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setCurrentStateAsCheckpoint();
//        });
//        reDistributeIndices.setMaxWidth(Double.MAX_VALUE);
//
//
//
//
//        layout.getChildren().addAll(addMatrixItem, deleteMatrixItem, renameMatrixItem, appendConnections, setConnections, deleteConnections, configureGroupings, sort, reDistributeIndices);
//        layout.setPadding(new Insets(10, 10, 10, 10));
//        layout.setSpacing(20);
//        layout.setAlignment(Pos.CENTER);
//    }
//
//
//    /**
//     * Safely adds a change to a javafx listview by ensuring the item is not present
//     *
//     * @param currentChanges the javafx listview to add the change to
//     * @param rowUid         the row uid of the connection to attempt to add
//     * @param colUid         the col uid of the connection to attempt to add
//     */
//    public void safeAddConnectionChange(ListView<Pair<Integer, Integer>> currentChanges, int rowUid, int colUid) {
//        for(Pair<Integer, Integer> item : currentChanges.getItems()) {
//            if(item.getKey() == rowUid && item.getValue() == colUid) {
//                return;
//            }
//        }
//        currentChanges.getItems().add(new Pair<>(rowUid, colUid));
//    }
//
//    /**
//     * Returns the VBox of the layout so that it can be added to a scene
//     *
//     * @return the VBox layout of the toolbar
//     */
//    public VBox getLayout() {
//        return layout;
//    }
//}
