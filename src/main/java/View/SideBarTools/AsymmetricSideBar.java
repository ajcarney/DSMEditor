package View.SideBarTools;

import Data.*;
import View.EditorPane;
import View.Widgets.Misc;
import View.Widgets.NumericTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * Creates a sidebar with methods to interact with an asymmetric matrix
 */
public class AsymmetricSideBar extends TemplateSideBar {

    protected final Button configureGroupings = new Button("Configure Groupings");
    private AsymmetricDSM matrix;

    public AsymmetricSideBar(AsymmetricDSM matrix, EditorPane editor) {
        super(matrix, editor);
        this.matrix = matrix;

        addMatrixItems.setText("Add Rows/Columns");
        deleteMatrixItems.setText("Delete Rows/Columns");

        configureGroupings.setOnAction(e -> configureGroupingsCallback());
        configureGroupings.setMaxWidth(Double.MAX_VALUE);

        layout.getChildren().addAll(addMatrixItems, deleteMatrixItems, appendConnections, setConnections, deleteConnections, configureGroupings, sort, reDistributeIndices);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(20);
        layout.setAlignment(Pos.CENTER);
    }


    /**
     * Sets up the button for adding items to the matrix
     */
    @Override
    protected void addMatrixItemsCallback() {
        Stage window = new Stage();

        // Create Root window
        window.initModality(Modality.APPLICATION_MODAL); // Block events to other windows
        window.setTitle("Add Row/Column");

        // Create changes view and button for it
        Label label = new Label("Changes to be made");
        ListView<Pair<String, String>> changesToMakeView = new ListView<>();  // item name, row/column
        changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        changesToMakeView.setCellFactory(param -> new ListCell<>() {  // item name
            @Override
            protected void updateItem(Pair<String, String> item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    if(item.getValue().equals("row")) {
                        setText(item + " Row");
                    } else {
                        setText(item + " Column");
                    }
                }
            }
        });

        Button deleteSelected = new Button("Delete Selected Item(s)");
        deleteSelected.setOnAction(e -> {
            changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
        });

        // Create user input area
        HBox entryArea = new HBox();

        TextField textField = new TextField();
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setPromptText("Row/Column Name");
        HBox.setHgrow(textField, Priority.ALWAYS);

        Button addRow = new Button("Add as Row");
        addRow.setOnAction(e -> {
            changesToMakeView.getItems().add(new Pair<>(textField.getText(), "row"));
        });
        Button addCol = new Button("Add as Column");
        addCol.setOnAction(e -> {
            changesToMakeView.getItems().add(new Pair<>(textField.getText(), "col"));
        });
        entryArea.getChildren().addAll(textField, addRow, addCol);
        entryArea.setPadding(new Insets(10, 10, 10, 10));
        entryArea.setSpacing(20);

        // create HBox for user to close with our without changes
        HBox closeArea = new HBox();
        Button applyButton = new Button("Apply Changes");
        applyButton.setOnAction(e -> {
            for(Pair<String, String> item : changesToMakeView.getItems()) {
                boolean isRow = item.getValue().equals("row");
                matrix.createItem(item.getKey(), isRow);
            }
            matrix.setCurrentStateAsCheckpoint();
            window.close();
            editor.refreshTab();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            window.close();
        });
        closeArea.getChildren().addAll(cancelButton, Misc.getHorizontalSpacer(), applyButton);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, changesToMakeView, deleteSelected, entryArea, Misc.getVerticalSpacer(), closeArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);

        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout, 700, 350);
        window.setScene(scene);
        window.showAndWait();
    }


    /**
     * Sets up the button for deleting items from the matrix
     */
    @Override
    protected void deleteMatrixItemsCallback() {
        Stage window = new Stage();  // Create Root window
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("Delete Rows/Columns");

        // Create changes view and button for it
        Label label = new Label("Changes to be made");
        ListView<Integer> changesToMakeView = new ListView<>();
        changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        changesToMakeView.setCellFactory(param -> new ListCell<>() {  // item uid
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(matrix.getItem(item).getName().getValue());
                }
            }
        });

        Button deleteSelected = new Button("Delete Selected Item(s)");
        deleteSelected.setOnAction(e -> {
            changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
        });

        // Create user input area
        HBox entryArea = new HBox();

        // ComboBox to choose which row or column to modify connections of
        ComboBox<DSMItem> itemSelector = new ComboBox<>();  // rowUid | colUid | name | weight
        itemSelector.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DSMItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName().getValue());
                }
            }
        });
        itemSelector.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(itemSelector, Priority.ALWAYS);
        itemSelector.setPromptText("Item Name");
        itemSelector.getItems().addAll(matrix.getRows());
        itemSelector.getItems().addAll(matrix.getCols());

        Button deleteItem = new Button("Delete Item");
        deleteItem.setOnAction(e -> {
            changesToMakeView.getItems().add(itemSelector.getValue().getUid());
        });

        entryArea.getChildren().addAll(itemSelector, deleteItem);
        entryArea.setPadding(new Insets(10, 10, 10, 10));
        entryArea.setSpacing(20);

        // create HBox for user to close with our without changes
        HBox closeArea = new HBox();
        Button applyButton = new Button("Apply Changes");
        applyButton.setOnAction(e -> {
            for(Integer uid : changesToMakeView.getItems()) {
                DSMItem item = matrix.getItem(uid);  // null check in case user tries to delete the same item twice
                if(item != null) {
                    matrix.deleteItem(item);
                }
            }
            window.close();
            editor.refreshTab();
            matrix.setCurrentStateAsCheckpoint();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            window.close();
        });
        closeArea.getChildren().addAll(cancelButton, Misc.getHorizontalSpacer(), applyButton);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, changesToMakeView, deleteSelected, entryArea, Misc.getVerticalSpacer(), closeArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);

        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout, 700, 350);
        window.setScene(scene);
        window.showAndWait();
    }


    /**
     * Sets up the button for appending connections to the matrix
     */
    @Override
    protected void appendConnectionsCallback() {
        Stage window = new Stage();

        // Create Root window
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("Append Connections");


        // Create changes view (does not have button to remove items from it
        Label label = new Label("Changes to be made");
        ListView<DSMConnection> changesToMakeView = new ListView<>();
        changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        changesToMakeView.setCellFactory(CONNECTION_CELL_FACTORY);

        Button deleteSelected = new Button("Delete Selected Item(s)");
        deleteSelected.setOnAction(ee -> {
            changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
        });


        // area to interact with the connections
        HBox connectionsArea = new HBox();
        connectionsArea.setSpacing(10);
        connectionsArea.setPadding(new Insets(10, 10, 10, 10));

        // HBox area full of checklists to modify the connections, default to columns
        HBox connectionsModifier = new HBox();
        connectionsModifier.setSpacing(10);
        connectionsModifier.setPadding(new Insets(10, 10, 10, 10));
        HashMap<CheckBox, DSMItem> connections = new HashMap<>();

        for(DSMItem conn : matrix.getCols()) {  // will default to choosing a row item so populate the scroll window with the columns
            VBox connectionVBox = new VBox();
            connectionVBox.setAlignment(Pos.CENTER);

            Label name = new Label(conn.getName().getValue());
            CheckBox box = new CheckBox();
            connections.put(box, conn);
            connectionVBox.getChildren().addAll(name, box);
            connectionsModifier.getChildren().add(connectionVBox);
        }
        ScrollPane scrollPane = new ScrollPane(connectionsModifier);
        scrollPane.setOnScroll(event -> {  // allow vertical scrolling to scroll horizontally
            if(event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                scrollPane.setHvalue(scrollPane.getHvalue() - event.getDeltaY() / connectionsModifier.getWidth());
            }
        });
        scrollPane.setFitToHeight(true);

        // vbox to choose row or column
        VBox itemSelectorView = new VBox();
        itemSelectorView.setMinWidth(Region.USE_PREF_SIZE);

        // ComboBox to choose which row or column to modify connections of
        ComboBox< DSMItem > itemSelector = new ComboBox<>();  // rowUid | colUid | name | weight
        itemSelector.setCellFactory(MATRIX_ITEM_COMBOBOX_CELL_FACTORY);

        itemSelector.getItems().addAll(matrix.getRows());  // default to choosing a row item

        Label l = new Label("Create connections by row or column?");
        l.setWrapText(true);
        l.prefWidthProperty().bind(itemSelector.widthProperty());  // this will make sure the label will not be bigger than the biggest object
        VBox.setVgrow(l, Priority.ALWAYS);
        HBox.setHgrow(l, Priority.ALWAYS);
        l.setMinHeight(Region.USE_PREF_SIZE);  // make sure all text will be displayed

        // radio buttons
        HBox rowColRadioButtons = new HBox();
        HBox.setHgrow(rowColRadioButtons, Priority.ALWAYS);
        rowColRadioButtons.setSpacing(10);
        rowColRadioButtons.setPadding(new Insets(10, 10, 10, 10));
        rowColRadioButtons.setMinHeight(Region.USE_PREF_SIZE);

        ToggleGroup tg = new ToggleGroup();
        RadioButton selectByRow = new RadioButton("Row");
        RadioButton selectByCol = new RadioButton("Column");
        HBox.setHgrow(selectByRow, Priority.ALWAYS);
        HBox.setHgrow(selectByCol, Priority.ALWAYS);
        selectByRow.setMinHeight(Region.USE_PREF_SIZE);
        selectByCol.setMinHeight(Region.USE_PREF_SIZE);

        selectByRow.setToggleGroup(tg);  // add RadioButtons to toggle group
        selectByCol.setToggleGroup(tg);
        selectByRow.setSelected(true);  // default to selectByRow

        // add a change listener
        tg.selectedToggleProperty().addListener((ob, o, n) -> {  // o is old value, n is new value
            RadioButton rb = (RadioButton)tg.getSelectedToggle();
            if(rb.equals(selectByRow)) {  // clear all items and add rows to it
                itemSelector.getItems().removeAll(itemSelector.getItems());
                itemSelector.getItems().addAll(matrix.getRows());  // populate combobox with the rows
            } else if(rb.equals(selectByCol)) {  // clear all items and add cols to it
                itemSelector.getItems().removeAll(itemSelector.getItems());
                itemSelector.getItems().addAll(matrix.getCols());  // populate combobox with the columns
            } else {  // clear all items
                itemSelector.getItems().removeAll(itemSelector.getItems());
            }
        });

        itemSelector.valueProperty().addListener((options, oldValue, newValue) -> {
            RadioButton rb = (RadioButton)tg.getSelectedToggle();
            connectionsModifier.getChildren().removeAll(connectionsModifier.getChildren());
            connections.clear();
            if (rb.equals(selectByRow)) {  // clear all items and add rows to it
                for(DSMItem col : matrix.getCols()) {  // create the checkboxes
                    VBox connectionVBox = new VBox();
                    connectionVBox.setAlignment(Pos.CENTER);

                    Label name = new Label(col.getName().getValue());
                    CheckBox box = new CheckBox();
                    connections.put(box, col);
                    connectionVBox.getChildren().addAll(name, box);
                    connectionsModifier.getChildren().add(connectionVBox);
                }
            } else if (rb.equals(selectByCol)) {
                for(DSMItem row : matrix.getRows()) {  // create the checkboxes
                    VBox connectionVBox = new VBox();
                    connectionVBox.setAlignment(Pos.CENTER);

                    Label name = new Label(row.getName().getValue());
                    CheckBox box = new CheckBox();
                    connections.put(box, row);
                    connectionVBox.getChildren().addAll(name, box);
                    connectionsModifier.getChildren().add(connectionVBox);
                }
            }
        });

        rowColRadioButtons.getChildren().addAll(selectByRow, selectByCol);
        itemSelectorView.getChildren().addAll(l, rowColRadioButtons, itemSelector);

        // area to set details for the connection
        VBox connectionDetailsLayout = new VBox();
        connectionDetailsLayout.setSpacing(10);
        connectionDetailsLayout.setPadding(new Insets(10, 10, 10, 10));
        VBox.setVgrow(connectionDetailsLayout, Priority.ALWAYS);
        connectionDetailsLayout.setMinWidth(Region.USE_PREF_SIZE);

        TextField connectionName = new TextField();
        NumericTextField weight = new NumericTextField(null);
        connectionName.setPromptText("Connection Name");
        weight.setPromptText("Connection Weight");
        connectionName.setMinWidth(connectionName.getPrefWidth());
        weight.setMinWidth(weight.getPrefWidth());

        connectionDetailsLayout.getChildren().addAll(connectionName, weight);

        connectionsArea.getChildren().addAll(itemSelectorView, scrollPane, Misc.getHorizontalSpacer(), connectionDetailsLayout);

        // Pane to modify the connections
        HBox modifyPane = new HBox();
        modifyPane.setAlignment(Pos.CENTER);

        Button applyButton = new Button("Modify Connections");
        applyButton.setOnAction(ee -> {
            if(itemSelector.getValue() == null || connectionName.getText().isEmpty() || weight.getText().isEmpty()) {  // ensure connection can be added
                // TODO: add popup window saying why it cannot make the changes
                return;
            }
            for (Map.Entry<CheckBox, DSMItem> entry : connections.entrySet()) {
                if(entry.getKey().isSelected()) {
                    if(tg.getSelectedToggle().equals(selectByRow)) {  // selecting by row
                        DSMConnection conn = new DSMConnection(connectionName.getText(), weight.getNumericValue(), itemSelector.getValue().getUid(), entry.getValue().getUid());
                        if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn);
                        }

                    } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                        DSMConnection conn = new DSMConnection(connectionName.getText(), weight.getNumericValue(), entry.getValue().getUid(), itemSelector.getValue().getUid());
                        if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn);
                        }
                    }
                }
            }
        });

        modifyPane.getChildren().addAll(applyButton);


        // create HBox for user to close with our without changes
        HBox closeArea = new HBox();
        Button applyAllButton = new Button("Apply All Changes");
        applyAllButton.setOnAction(ee -> {
            for(DSMConnection conn : changesToMakeView.getItems()) {  // rowUid | colUid | name | weight
                matrix.modifyConnection(conn.getRowUid(), conn.getColUid(), conn.getConnectionName(), conn.getWeight());
            }
            window.close();
            editor.refreshTab();
            matrix.setCurrentStateAsCheckpoint();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(ee -> {
            window.close();
        });
        closeArea.getChildren().addAll(cancelButton, Misc.getHorizontalSpacer(), applyAllButton);


        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, changesToMakeView, deleteSelected, connectionsArea, modifyPane, Misc.getVerticalSpacer(), closeArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);


        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout, 900, 500);
        window.setScene(scene);
        window.showAndWait();
    }


    /**
     * Sets up the button for setting connections in the matrix
     */
    @Override
    protected void setConnectionsCallback() {
        Stage window = new Stage();

        // Create Root window
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("Set Connections");


        // Create changes view (does not have button to remove items from it
        Label label = new Label("Changes to be made");
        ListView<DSMConnection> changesToMakeView = new ListView<>();
        changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        changesToMakeView.setCellFactory(CONNECTION_CELL_FACTORY);

        Button deleteSelected = new Button("Delete Selected Item(s)");
        deleteSelected.setOnAction(ee -> {
            changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
        });


        // area to interact with the connections
        HBox connectionsArea = new HBox();
        connectionsArea.setSpacing(10);
        connectionsArea.setPadding(new Insets(10, 10, 10, 10));

        // HBox area full of checklists to modify the connections, default to columns
        HBox connectionsModifier = new HBox();
        connectionsModifier.setSpacing(10);
        connectionsModifier.setPadding(new Insets(10, 10, 10, 10));
        HashMap<CheckBox, DSMItem> connections = new HashMap<>();

        for(DSMItem conn : matrix.getCols()) {  // will default to choosing a row item so populate the scroll window with the columns
            VBox connectionVBox = new VBox();
            connectionVBox.setAlignment(Pos.CENTER);

            Label name = new Label(conn.getName().getValue());
            CheckBox box = new CheckBox();
            connections.put(box, conn);
            connectionVBox.getChildren().addAll(name, box);
            connectionsModifier.getChildren().add(connectionVBox);
        }
        ScrollPane scrollPane = new ScrollPane(connectionsModifier);
        scrollPane.setOnScroll(event -> {  // allow vertical scrolling to scroll horizontally
            if(event.getDeltaX() == 0 && event.getDeltaY() != 0) {
                scrollPane.setHvalue(scrollPane.getHvalue() - event.getDeltaY() / connectionsModifier.getWidth());
            }
        });
        scrollPane.setFitToHeight(true);

        // vbox to choose row or column
        VBox itemSelectorView = new VBox();
        itemSelectorView.setMinWidth(Region.USE_PREF_SIZE);

        // ComboBox to choose which row or column to modify connections of
        ComboBox< DSMItem > itemSelector = new ComboBox<>();  // rowUid | colUid | name | weight
        itemSelector.setCellFactory(MATRIX_ITEM_COMBOBOX_CELL_FACTORY);

        itemSelector.getItems().addAll(matrix.getRows());  // default to choosing a row item

        Label l = new Label("Create connections by row or column?");
        l.setWrapText(true);
        l.prefWidthProperty().bind(itemSelector.widthProperty());  // this will make sure the label will not be bigger than the biggest object
        VBox.setVgrow(l, Priority.ALWAYS);
        HBox.setHgrow(l, Priority.ALWAYS);
        l.setMinHeight(Region.USE_PREF_SIZE);  // make sure all text will be displayed

        // radio buttons
        HBox rowColRadioButtons = new HBox();
        HBox.setHgrow(rowColRadioButtons, Priority.ALWAYS);
        rowColRadioButtons.setSpacing(10);
        rowColRadioButtons.setPadding(new Insets(10, 10, 10, 10));
        rowColRadioButtons.setMinHeight(Region.USE_PREF_SIZE);

        ToggleGroup tg = new ToggleGroup();
        RadioButton selectByRow = new RadioButton("Row");
        RadioButton selectByCol = new RadioButton("Column");
        HBox.setHgrow(selectByRow, Priority.ALWAYS);
        HBox.setHgrow(selectByCol, Priority.ALWAYS);
        selectByRow.setMinHeight(Region.USE_PREF_SIZE);
        selectByCol.setMinHeight(Region.USE_PREF_SIZE);

        selectByRow.setToggleGroup(tg);  // add RadioButtons to toggle group
        selectByCol.setToggleGroup(tg);
        selectByRow.setSelected(true);  // default to selectByRow

        // add a change listener
        tg.selectedToggleProperty().addListener((ob, o, n) -> {
            RadioButton rb = (RadioButton) tg.getSelectedToggle();
            if (rb.equals(selectByRow)) {  // clear all items and add rows to it
                itemSelector.getItems().removeAll(itemSelector.getItems());
                itemSelector.getItems().addAll(matrix.getRows());  // populate combobox with the rows
            } else if (rb.equals(selectByCol)) {  // clear all items and add cols to it
                itemSelector.getItems().removeAll(itemSelector.getItems());
                itemSelector.getItems().addAll(matrix.getCols());  // populate combobox with the columns
            } else {  // clear all items
                itemSelector.getItems().removeAll(itemSelector.getItems());
            }
        });

        itemSelector.valueProperty().addListener((options, oldValue, newValue) -> {
            RadioButton rb = (RadioButton)tg.getSelectedToggle();
            connectionsModifier.getChildren().removeAll(connectionsModifier.getChildren());
            connections.clear();
            if (rb.equals(selectByRow)) {  // clear all items and add rows to it
                for(DSMItem col : matrix.getCols()) {  // create the checkboxes
                    VBox connectionVBox = new VBox();
                    connectionVBox.setAlignment(Pos.CENTER);

                    Label name = new Label(col.getName().getValue());
                    CheckBox box = new CheckBox();
                    connections.put(box, col);
                    connectionVBox.getChildren().addAll(name, box);
                    connectionsModifier.getChildren().add(connectionVBox);
                }
            } else if (rb.equals(selectByCol)) {
                for(DSMItem row : matrix.getRows()) {  // create the checkboxes
                    VBox connectionVBox = new VBox();
                    connectionVBox.setAlignment(Pos.CENTER);

                    Label name = new Label(row.getName().getValue());
                    CheckBox box = new CheckBox();
                    connections.put(box, row);
                    connectionVBox.getChildren().addAll(name, box);
                    connectionsModifier.getChildren().add(connectionVBox);
                }
            }
        });

        rowColRadioButtons.getChildren().addAll(selectByRow, selectByCol);
        itemSelectorView.getChildren().addAll(l, rowColRadioButtons, itemSelector);

        itemSelector.setOnAction(ee -> {  // when item changes, change the connections that are selected
            if(itemSelector.getValue() == null) {  // ensure connection can be added
                return;
            }
            for (Map.Entry<CheckBox, DSMItem> entry : connections.entrySet()) {
                RadioButton rb = (RadioButton)tg.getSelectedToggle();
                if(rb.equals(selectByRow) && matrix.getConnection(itemSelector.getValue().getUid(), entry.getValue().getUid()) != null) {
                    entry.getKey().setSelected(true);
                } else if(rb.equals(selectByCol) && matrix.getConnection(entry.getValue().getUid(), itemSelector.getValue().getUid()) != null) {
                    entry.getKey().setSelected(true);
                } else {
                    entry.getKey().setSelected(false);
                }
            }
        });

        // area to set details for the connection
        VBox connectionDetailsLayout = new VBox();
        connectionDetailsLayout.setSpacing(10);
        connectionDetailsLayout.setPadding(new Insets(10, 10, 10, 10));
        VBox.setVgrow(connectionDetailsLayout, Priority.ALWAYS);
        connectionDetailsLayout.setMinWidth(Region.USE_PREF_SIZE);

        TextField connectionName = new TextField();
        NumericTextField weight = new NumericTextField(null);
        connectionName.setPromptText("Connection Name");
        weight.setPromptText("Connection Weight");
        connectionName.setMinWidth(connectionName.getPrefWidth());
        weight.setMinWidth(weight.getPrefWidth());

        connectionDetailsLayout.getChildren().addAll(connectionName, weight);

        connectionsArea.getChildren().addAll(itemSelectorView, scrollPane, Misc.getHorizontalSpacer(), connectionDetailsLayout);

        // Pane to modify the connections
        HBox modifyPane = new HBox();
        modifyPane.setAlignment(Pos.CENTER);

        Button applyButton = new Button("Modify Connections");
        applyButton.setOnAction(ee -> {
            if(itemSelector.getValue() == null) {  // ensure connection can be added
                // TODO: add popup window saying why it cannot make the changes
                return;
            }
            for (Map.Entry<CheckBox, DSMItem> entry : connections.entrySet()) {
                if (entry.getKey().isSelected() && !connectionName.getText().isEmpty()) {  // create the connection
                    if(tg.getSelectedToggle().equals(selectByRow)) {  // selecting by row
                        DSMConnection conn = new DSMConnection(connectionName.getText(), weight.getNumericValue(), itemSelector.getValue().getUid(), entry.getValue().getUid());
                        if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn);
                        }

                    } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                        DSMConnection conn = new DSMConnection(connectionName.getText(), weight.getNumericValue(), entry.getValue().getUid(), itemSelector.getValue().getUid());
                        if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn);
                        }
                    }
                } else {  // delete the connection
                    if(tg.getSelectedToggle().equals(selectByRow)) {  // selecting by row
                        DSMConnection conn = new DSMConnection("", Double.MAX_VALUE, itemSelector.getValue().getUid(), entry.getValue().getUid());
                        if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn);
                        }

                    } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                        DSMConnection conn = new DSMConnection("", Double.MAX_VALUE, entry.getValue().getUid(), itemSelector.getValue().getUid());
                        if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn);
                        }
                    }
                }
            }
        });

        modifyPane.getChildren().addAll(applyButton);


        // create HBox for user to close with our without changes
        HBox closeArea = new HBox();
        Button applyAllButton = new Button("Apply All Changes");
        applyAllButton.setOnAction(ee -> {
            for(DSMConnection conn : changesToMakeView.getItems()) {
                if(conn.getConnectionName() != null && conn.getWeight() != Double.MAX_VALUE) {
                    matrix.modifyConnection(conn.getRowUid(), conn.getColUid(), conn.getConnectionName(), conn.getWeight());
                } else {
                    matrix.deleteConnection(conn.getRowUid(), conn.getColUid());
                }
            }
            window.close();
            editor.refreshTab();
            matrix.setCurrentStateAsCheckpoint();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(ee -> {
            window.close();
        });
        closeArea.getChildren().addAll(cancelButton, Misc.getHorizontalSpacer(), applyAllButton);


        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, changesToMakeView, deleteSelected, connectionsArea, modifyPane, Misc.getVerticalSpacer(), closeArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);


        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout, 900, 500);
        window.setScene(scene);
        window.showAndWait();
    }


    /**
     * Sets up the button for deleting connections in the matrix
     */
    @Override
    protected void deleteConnectionsCallback() {
        Stage window = new Stage();

        // Create Root window
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("Delete Connections");


        // Create changes view (does not have button to remove items from it
        Label label = new Label("Changes to be made");
        ListView<DSMConnection> changesToMakeView = new ListView<>();  // rowUid | colUid
        changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        changesToMakeView.setCellFactory(DELETE_CONNECTION_CELL_FACTORY);

        Button deleteSelected = new Button("Delete Selected Item(s)");
        deleteSelected.setOnAction(ee -> {
            changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
        });


        // Create area for the user to choose the connection they want to remove
        HBox entryArea = new HBox();

        // ComboBox to choose which row or column to modify connections of
        ComboBox<Integer> firstItemSelector = new ComboBox<>();
        firstItemSelector.setButtonCell(MATRIX_ITEM_INTEGER_COMBOBOX_CELL_FACTORY.call(null));
        firstItemSelector.setCellFactory(MATRIX_ITEM_INTEGER_COMBOBOX_CELL_FACTORY);
        Vector<Integer> items = new Vector<>();
        items.add(Integer.MAX_VALUE);  // this will be used for selecting all items
        for(DSMItem row : matrix.getRows()) {
            items.add(row.getUid());
        }
        for(DSMItem col : matrix.getCols()) {
            items.add(col.getUid());
        }
        firstItemSelector.getItems().addAll(items);


        firstItemSelector.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(firstItemSelector, Priority.ALWAYS);
        firstItemSelector.setPromptText("From");


        // ComboBox to choose the second item of the connection (row or column)
        ObservableList<Integer> connectionItems = FXCollections.observableArrayList();

        ComboBox<Integer> secondItemSelector = new ComboBox<>();
        secondItemSelector.setButtonCell(MATRIX_ITEM_INTEGER_COMBOBOX_CELL_FACTORY.call(null));
        secondItemSelector.setCellFactory(MATRIX_ITEM_INTEGER_COMBOBOX_CELL_FACTORY);
        secondItemSelector.setItems(connectionItems);

        secondItemSelector.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(secondItemSelector, Priority.ALWAYS);
        secondItemSelector.setPromptText("To");

        // add listener to first item to update options in second column
        firstItemSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(matrix.isRow(newValue)) {
                connectionItems.clear();
                connectionItems.add(Integer.MAX_VALUE);  // add the all option
                for(DSMItem col : matrix.getCols()) {
                    if(matrix.getConnection(newValue, col.getUid()) != null) {
                        connectionItems.add(col.getUid());
                    }
                }
            } else {
                connectionItems.clear();
                connectionItems.add(Integer.MAX_VALUE);  // add the all option
                for(DSMItem row : matrix.getRows()) {
                    if(matrix.getConnection(row.getUid(), newValue) != null) {
                        connectionItems.add(row.getUid());
                    }
                }
            }

            secondItemSelector.getSelectionModel().clearSelection();  // clear selection of the second item
            secondItemSelector.setValue(null);
        });

        Button deleteConnection = new Button("Delete Connection");
        deleteConnection.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteConnection, Priority.ALWAYS);


        entryArea.getChildren().addAll(firstItemSelector, secondItemSelector, deleteConnection);
        entryArea.setPadding(new Insets(10, 10, 10, 10));
        entryArea.setSpacing(20);

        deleteConnection.setOnAction(ee -> {
            if(firstItemSelector.getValue() == null || secondItemSelector.getValue() == null) return;  // have to have a value selected

            ArrayList<DSMItem> rowSelected = new ArrayList<>();
            ArrayList<DSMItem> colSelected = new ArrayList<>();

            if(firstItemSelector.getValue() == Integer.MAX_VALUE && secondItemSelector.getValue() == Integer.MAX_VALUE) {  // delete all connections
                rowSelected.addAll(matrix.getRows());
                colSelected.addAll(matrix.getCols());

            } else if(firstItemSelector.getValue() == Integer.MAX_VALUE && secondItemSelector.getValue() != Integer.MAX_VALUE) {
                boolean secondIsRow = matrix.isRow(secondItemSelector.getValue());
                if(secondIsRow) {  // delete all columns going to a row
                    DSMItem row = matrix.getItem(secondItemSelector.getValue());
                    rowSelected.add(row);
                    colSelected.addAll(matrix.getCols());
                } else {  // delete all rows going to a column
                    DSMItem col = matrix.getItem(secondItemSelector.getValue());
                    rowSelected.addAll(matrix.getRows());
                    colSelected.add(col);
                }

            } else if(firstItemSelector.getValue() != Integer.MAX_VALUE && secondItemSelector.getValue() == Integer.MAX_VALUE) {
                boolean firstIsRow = matrix.isRow(firstItemSelector.getValue());
                if(firstIsRow) {  // delete all columns going to a row
                    DSMItem row = matrix.getItem(firstItemSelector.getValue());
                    rowSelected.add(row);
                    colSelected.addAll(matrix.getCols());

                } else {  // delete all rows going to a column
                    DSMItem col = matrix.getItem(firstItemSelector.getValue());
                    rowSelected.addAll(matrix.getRows());
                    colSelected.add(col);
                }

            } else if(matrix.isRow(firstItemSelector.getValue())) {
                rowSelected.add(matrix.getItem(firstItemSelector.getValue()));
                colSelected.add(matrix.getItem(secondItemSelector.getValue()));

            } else {
                rowSelected.add(matrix.getItem(secondItemSelector.getValue()));
                colSelected.add(matrix.getItem(firstItemSelector.getValue()));

            }
            for(DSMItem rowItem : rowSelected) {
                for(DSMItem colItem : colSelected) {
                    DSMConnection conn = matrix.getConnection(rowItem.getUid(), colItem.getUid());
                    if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                        changesToMakeView.getItems().add(conn);
                    }
                }
            }
        });


        // create HBox for user to close with our without changes
        Pane vSpacer = new Pane();  // used as a spacer to move HBox to the bottom
        VBox.setVgrow(vSpacer, Priority.ALWAYS);
        vSpacer.setMaxHeight(Double.MAX_VALUE);

        HBox closeArea = new HBox();
        Button applyAllButton = new Button("Apply All Changes");
        applyAllButton.setOnAction(ee -> {
            for(DSMConnection conn : changesToMakeView.getItems()) {  // rowUid | colUid
                matrix.deleteConnection(conn.getRowUid(), conn.getColUid());
            }
            window.close();
            editor.refreshTab();
            matrix.setCurrentStateAsCheckpoint();
        });

        Pane spacer = new Pane();  // used as a spacer between buttons
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMaxWidth(Double.MAX_VALUE);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(ee -> {
            window.close();
        });
        closeArea.getChildren().addAll(cancelButton, spacer, applyAllButton);


        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, changesToMakeView, deleteSelected, entryArea, vSpacer, closeArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);


        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout, 1100, 500);
        window.setScene(scene);
        window.showAndWait();
    }


    /**
     * Sets up the button for modifying groupings in the matrix
     */
    private void configureGroupingsCallback() {
        // Create Root window
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("Configure Groupings");

        VBox allGroupings = new VBox();
        VBox mainGroupingsView = new VBox();
        ScrollPane currentGroupingsPane = new ScrollPane(allGroupings);
        currentGroupingsPane.setFitToWidth(true);

        for(Grouping grouping : matrix.getGroupings()) {
            HBox groupRow = configureGroupingEditorRow(matrix, grouping, mainGroupingsView, true);
            mainGroupingsView.getChildren().add(groupRow);
        }
        HBox defaultGroupLabel = new HBox();
        defaultGroupLabel.setPadding(new Insets(10));
        defaultGroupLabel.getChildren().add(new Label("Default Grouping:"));

        HBox defaultGroupRow = configureGroupingEditorRow(matrix, matrix.getDefaultGrouping(), mainGroupingsView, false);
        allGroupings.getChildren().addAll(mainGroupingsView, defaultGroupLabel, defaultGroupRow);

        // area to add, delete, rename
        HBox modifyArea = new HBox();
        modifyArea.setAlignment(Pos.CENTER);

        Button addButton = new Button("Add New Grouping");
        addButton.setOnAction(e -> {
            Grouping newGrouping = new Grouping("New Grouping", Color.color(1, 1, 1));
            HBox groupRow = configureGroupingEditorRow(matrix, newGrouping, mainGroupingsView, true);
            matrix.addGrouping(newGrouping);
            mainGroupingsView.getChildren().add(groupRow);
        });

        modifyArea.getChildren().add(addButton);

        // create HBox for user to close with changes
        HBox closeArea = new HBox();
        Button applyAllButton = new Button("Ok");

        applyAllButton.setOnAction(e -> {
            window.close();        // changes have already been made so just close the window
        });

        closeArea.getChildren().addAll(Misc.getHorizontalSpacer(), applyAllButton);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(currentGroupingsPane, modifyArea, Misc.getVerticalSpacer(), closeArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);


        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout, 900, 300);
        window.setScene(scene);
        scene.getWindow().setOnHidden(e -> {  // TODO: 6/17/2020 changed from setOnCloseRequest when it was working before and idk why this fixed it
            window.close();                        // changes have already been made so just close and refresh the screen
            matrix.setCurrentStateAsCheckpoint();
            editor.refreshTab();
        });
        window.showAndWait();
    }

}
