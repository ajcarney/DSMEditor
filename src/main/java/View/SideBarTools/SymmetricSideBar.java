package View.SideBarTools;

import Data.DSMConnection;
import Data.DSMItem;
import Data.Grouping;
import Data.SymmetricDSM;
import View.EditorPane;
import View.Widgets.Misc;
import View.Widgets.NumericTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.util.Callback;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * Creates a sidebar with methods to interact with a symmetric matrix
 */
public class SymmetricSideBar extends TemplateSideBar<SymmetricDSM> {

    protected final Button configureGroupings = new Button("Configure Groupings");

    public SymmetricSideBar(SymmetricDSM matrix, EditorPane editor) {
        super(matrix, editor);

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
        ListView<String> changesToMakeView = new ListView<>();
        changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        changesToMakeView.setCellFactory(param -> new ListCell<>() {  // item name
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item + " (Row/Column)");
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

        Button addItem = new Button("Add Item");
        addItem.setOnAction(e -> {
            changesToMakeView.getItems().add(textField.getText());
        });
        entryArea.getChildren().addAll(textField, addItem);
        entryArea.setPadding(new Insets(10, 10, 10, 10));
        entryArea.setSpacing(20);

        // create HBox for user to close with our without changes
        HBox closeArea = new HBox();
        Button applyButton = new Button("Apply Changes");
        applyButton.setOnAction(e -> {
            for(String item : changesToMakeView.getItems()) {
                matrix.createItem(item, true);
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
                    setText(matrix.getItem(item).getName() + " (Row/Column)");
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
        changesToMakeView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DSMConnection connection, boolean empty) {
                super.updateItem(connection, empty);

                if (empty || connection == null) {
                    setText(null);
                } else {
                    setText(
                        matrix.getItem(connection.getRowUid()).getName().getValue() + " (Row):" +
                        matrix.getItem(connection.getColUid()).getName().getValue() + " (Col)" +
                        "  {" + connection.getConnectionName() + ", " + connection.getWeight() + "}"
                    );
                }
            }
        });
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
                    if(itemSelector.getValue() != null && col.getAliasUid().equals(itemSelector.getValue().getUid())) {  // don't allow creating connections between same row and column pair
                        continue;
                    }
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
                    if(itemSelector.getValue() != null && row.getAliasUid().equals(itemSelector.getValue().getUid())) {  // don't allow creating connections between same row and column pair
                        continue;
                    }
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

        Button applySymmetricButton = new Button("Modify Connections Symmetrically");
        applySymmetricButton.setOnAction(ee -> {
            if(itemSelector.getValue() == null || connectionName.getText().isEmpty() || weight.getText().isEmpty()) {  // ensure connection can be added
                return;
            }
            for (Map.Entry<CheckBox, DSMItem> entry : connections.entrySet()) {
                if(entry.getKey().isSelected()) {
                    if(tg.getSelectedToggle().equals(selectByRow)) {  // selecting by row
                        int rowUid = itemSelector.getValue().getUid();
                        int colUid = entry.getValue().getUid();
                        int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                        int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                        DSMConnection conn1 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), rowUid, colUid);
                        DSMConnection conn2 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), symmetricRowUid, symmetricColUid);

                        if(!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn1);
                        }
                        if(!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn2);
                        }
                    } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                        int rowUid = itemSelector.getValue().getUid();
                        int colUid = entry.getValue().getUid();
                        int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                        int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                        DSMConnection conn1 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), rowUid, colUid);
                        DSMConnection conn2 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), symmetricRowUid, symmetricColUid);

                        if(!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn1);
                        }
                        if(!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn2);
                        }
                    }
                }
            }
        });
        modifyPane.getChildren().addAll(applyButton, applySymmetricButton);


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
        changesToMakeView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DSMConnection connection, boolean empty) {
                super.updateItem(connection, empty);

                if (empty || connection == null) {
                    setText(null);
                } else if (!connection.getConnectionName().isEmpty() && connection.getWeight() != Double.MAX_VALUE) {
                    setText(
                        matrix.getItem(connection.getRowUid()).getName().getValue() + " (Row):" +
                        matrix.getItem(connection.getColUid()).getName().getValue() + " (Col)" +
                        "  {" + connection.getConnectionName() + ", " + connection.getWeight() + "}"
                    );
                } else {
                    setText(
                        "Delete " +
                        matrix.getItem(connection.getRowUid()).getName().getValue() + " (Row):" +
                        matrix.getItem(connection.getColUid()).getName().getValue() + " (Col)"
                    );
                }
            }
        });
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
        tg.selectedToggleProperty().addListener(new ChangeListener<>() {
            public void changed(ObservableValue<? extends Toggle> ob, Toggle o, Toggle n) {
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
            }
        });

        itemSelector.valueProperty().addListener((options, oldValue, newValue) -> {
            RadioButton rb = (RadioButton)tg.getSelectedToggle();
            connectionsModifier.getChildren().removeAll(connectionsModifier.getChildren());
            connections.clear();
            if (rb.equals(selectByRow)) {  // clear all items and add rows to it
                for(DSMItem col : matrix.getCols()) {  // create the checkboxes
                    if(itemSelector.getValue() != null && col.getAliasUid().equals(itemSelector.getValue().getUid())) {  // don't allow creating connections between same row and column pair
                        continue;
                    }
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
                    if(itemSelector.getValue() != null && row.getAliasUid().equals(itemSelector.getValue().getUid())) {  // don't allow creating connections between same row and column pair
                        continue;
                    }
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

        Button applySymmetricButton = new Button("Modify Connections Symmetrically");
        applySymmetricButton.setOnAction(ee -> {
            if(itemSelector.getValue() == null) {  // ensure connection can be added
                return;
            }
            for (Map.Entry<CheckBox, DSMItem> entry : connections.entrySet()) {
                if(entry.getKey().isSelected()) {
                    if(tg.getSelectedToggle().equals(selectByRow)) {  // selecting by row
                        int rowUid = itemSelector.getValue().getUid();
                        int colUid = entry.getValue().getUid();
                        int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                        int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                        DSMConnection conn1 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), rowUid, colUid);
                        DSMConnection conn2 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), symmetricRowUid, symmetricColUid);

                        if(!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn1);
                        }
                        if(!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn2);
                        }
                    } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                        int rowUid = itemSelector.getValue().getUid();
                        int colUid = entry.getValue().getUid();
                        int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                        int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                        DSMConnection conn1 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), rowUid, colUid);
                        DSMConnection conn2 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), symmetricRowUid, symmetricColUid);

                        if(!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn1);
                        }
                        if(!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn2);
                        }
                    }
                } else {
                    if(tg.getSelectedToggle().equals(selectByRow)) {  // selecting by row
                        int rowUid = itemSelector.getValue().getUid();
                        int colUid = entry.getValue().getUid();
                        int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                        int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                        DSMConnection conn1 = new DSMConnection("", Double.MAX_VALUE, rowUid, colUid);
                        DSMConnection conn2 = new DSMConnection("", Double.MAX_VALUE, symmetricRowUid, symmetricColUid);

                        if(!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn1);
                        }
                        if(!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn2);
                        }
                    } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                        int rowUid = itemSelector.getValue().getUid();
                        int colUid = entry.getValue().getUid();
                        int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                        int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                        DSMConnection conn1 = new DSMConnection("", Double.MAX_VALUE, rowUid, colUid);
                        DSMConnection conn2 = new DSMConnection("", Double.MAX_VALUE, symmetricRowUid, symmetricColUid);

                        if (!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn1);
                        }
                        if (!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn2);
                        }
                    }
                }
            }
        });
        modifyPane.getChildren().addAll(applyButton, applySymmetricButton);


        // create HBox for user to close with our without changes
        HBox closeArea = new HBox();
        Button applyAllButton = new Button("Apply All Changes");
        applyAllButton.setOnAction(ee -> {
            for(DSMConnection conn : changesToMakeView.getItems()) {
                if(!conn.getConnectionName().isEmpty() && conn.getWeight() != Double.MAX_VALUE) {
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
        changesToMakeView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(DSMConnection conn, boolean empty) {
                super.updateItem(conn, empty);

                if (empty || conn == null) {
                    setText(null);
                } else {
                    setText(
                        "DELETE " +
                        matrix.getItem(conn.getRowUid()).getName() + ":" +
                        matrix.getItem(conn.getColUid()).getName()
                    );
                }
            }
        });

        Button deleteSelected = new Button("Delete Selected Item(s)");
        deleteSelected.setOnAction(ee -> {
            changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
        });


        // Create area for the user to choose the connection they want to remove
        HBox entryArea = new HBox();

        // ComboBox to choose which row or column to modify connections of
        ComboBox<Integer> firstItemSelector = new ComboBox<>();
        // function to set text of comboBox items
        Callback<ListView<Integer>, ListCell<Integer>> cellFactory = new Callback<>() {
            @Override
            public ListCell<Integer> call(ListView<Integer> l) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            if(item == Integer.MAX_VALUE) {
                                setText("All");
                            } else if(matrix.isRow(matrix.getItem(item).getUid())) {
                                setText(matrix.getItem(item).getName() + " (Row)");
                            } else {
                                setText(matrix.getItem(item).getName() + " (Column)");
                            }
                        }
                    }
                };
            }
        };
        firstItemSelector.setButtonCell(cellFactory.call(null));
        firstItemSelector.setCellFactory(cellFactory);
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
        secondItemSelector.setButtonCell(cellFactory.call(null));
        secondItemSelector.setCellFactory(cellFactory);
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

        Button deleteConnectionSymmetrically = new Button("Delete Connection Symmetrically");
        deleteConnectionSymmetrically.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteConnectionSymmetrically, Priority.ALWAYS);

        entryArea.getChildren().addAll(firstItemSelector, secondItemSelector, deleteConnection, deleteConnectionSymmetrically);
        entryArea.setPadding(new Insets(10, 10, 10, 10));
        entryArea.setSpacing(20);

        deleteConnection.setOnAction(ee -> {
            if(firstItemSelector.getValue() == null || secondItemSelector.getValue() == null) return;  // have to have a value selected


            if(firstItemSelector.getValue() == Integer.MAX_VALUE && secondItemSelector.getValue() == Integer.MAX_VALUE) {  // delete all connections
                for(DSMConnection conn : matrix.getConnections()) {
                    if(!changesToMakeView.getItems().contains(conn)) {
                        changesToMakeView.getItems().add(conn);
                    }
                }
            } else if(firstItemSelector.getValue() == Integer.MAX_VALUE && secondItemSelector.getValue() != Integer.MAX_VALUE) {
                boolean secondIsRow = matrix.isRow(secondItemSelector.getValue());
                if(secondIsRow) {  // delete all columns going to a row
                    DSMItem row = matrix.getItem(secondItemSelector.getValue());
                    for(DSMItem col : matrix.getCols()) {
                        DSMConnection conn = matrix.getConnection(row.getUid(), col.getUid());
                        if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                            changesToMakeView.getItems().add(conn);
                        }
                    }

                } else {  // delete all rows going to a column
                    DSMItem col = matrix.getItem(secondItemSelector.getValue());
                    for(DSMItem row : matrix.getRows()) {
                        DSMConnection conn = matrix.getConnection(row.getUid(), col.getUid());
                        if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                            changesToMakeView.getItems().add(conn);
                        }
                    }
                }

            } else if(firstItemSelector.getValue() != Integer.MAX_VALUE && secondItemSelector.getValue() == Integer.MAX_VALUE) {
                boolean firstIsRow = matrix.isRow(firstItemSelector.getValue());
                if(firstIsRow) {  // delete all columns going to a row
                    DSMItem row = matrix.getItem(firstItemSelector.getValue());
                    for(DSMItem col : matrix.getCols()) {
                        DSMConnection conn = matrix.getConnection(row.getUid(), col.getUid());
                        if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                            changesToMakeView.getItems().add(conn);
                        }
                    }

                } else {  // delete all rows going to a column
                    DSMItem col = matrix.getItem(firstItemSelector.getValue());
                    for(DSMItem row : matrix.getRows()) {
                        DSMConnection conn = matrix.getConnection(row.getUid(), col.getUid());
                        if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                            changesToMakeView.getItems().add(conn);
                        }
                    }
                }

            } else if(matrix.isRow(firstItemSelector.getValue())) {
                DSMConnection conn = matrix.getConnection(firstItemSelector.getValue(), secondItemSelector.getValue());
                if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                    changesToMakeView.getItems().add(conn);
                }
            } else {
                DSMConnection conn = matrix.getConnection(secondItemSelector.getValue(), firstItemSelector.getValue());
                if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                    changesToMakeView.getItems().add(conn);
                }
            }
        });
        deleteConnectionSymmetrically.setOnAction(ee -> {
            if(firstItemSelector.getValue() == null || secondItemSelector.getValue() == null) return;  // have to have a value selected


            if(firstItemSelector.getValue() == Integer.MAX_VALUE && secondItemSelector.getValue() == Integer.MAX_VALUE) {  // delete all connections
                for(DSMConnection conn : matrix.getConnections()) {
                    if(!changesToMakeView.getItems().contains(conn)) {
                        changesToMakeView.getItems().add(conn);
                    }
                }
            } else if(firstItemSelector.getValue() == Integer.MAX_VALUE && secondItemSelector.getValue() != Integer.MAX_VALUE) {
                boolean secondIsRow = matrix.isRow(secondItemSelector.getValue());
                if(secondIsRow) {  // delete all columns going to a row
                    DSMItem row = matrix.getItem(secondItemSelector.getValue());
                    for(DSMItem col : matrix.getCols()) {
                        DSMConnection conn = matrix.getConnection(row.getUid(), col.getUid());
                        DSMConnection symmetricConn = matrix.getSymmetricConnection(row.getUid(), col.getUid());
                        if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                            changesToMakeView.getItems().add(conn);
                        }
                        if(symmetricConn != null && !changesToMakeView.getItems().contains(symmetricConn)) {
                            changesToMakeView.getItems().add(symmetricConn);
                        }
                    }

                } else {  // delete all rows going to a column
                    DSMItem col = matrix.getItem(secondItemSelector.getValue());
                    for(DSMItem row : matrix.getRows()) {
                        DSMConnection conn = matrix.getConnection(row.getUid(), col.getUid());
                        DSMConnection symmetricConn = matrix.getSymmetricConnection(row.getUid(), col.getUid());
                        if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                            changesToMakeView.getItems().add(conn);
                        }
                        if(symmetricConn != null && !changesToMakeView.getItems().contains(symmetricConn)) {
                            changesToMakeView.getItems().add(symmetricConn);
                        }
                    }
                }

            } else if(firstItemSelector.getValue() != Integer.MAX_VALUE && secondItemSelector.getValue() == Integer.MAX_VALUE) {
                boolean firstIsRow = matrix.isRow(firstItemSelector.getValue());
                if(firstIsRow) {  // delete all columns going to a row
                    DSMItem row = matrix.getItem(firstItemSelector.getValue());
                    for(DSMItem col : matrix.getCols()) {
                        DSMConnection conn = matrix.getConnection(row.getUid(), col.getUid());
                        DSMConnection symmetricConn = matrix.getSymmetricConnection(row.getUid(), col.getUid());
                        if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                            changesToMakeView.getItems().add(conn);
                        }
                        if(symmetricConn != null && !changesToMakeView.getItems().contains(symmetricConn)) {
                            changesToMakeView.getItems().add(symmetricConn);
                        }
                    }

                } else {  // delete all rows going to a column
                    DSMItem col = matrix.getItem(firstItemSelector.getValue());
                    for(DSMItem row : matrix.getRows()) {
                        DSMConnection conn = matrix.getConnection(row.getUid(), col.getUid());
                        DSMConnection symmetricConn = matrix.getSymmetricConnection(row.getUid(), col.getUid());
                        if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                            changesToMakeView.getItems().add(conn);
                        }
                        if(symmetricConn != null && !changesToMakeView.getItems().contains(symmetricConn)) {
                            changesToMakeView.getItems().add(symmetricConn);
                        }
                    }
                }

            } else if(matrix.isRow(firstItemSelector.getValue())) {
                DSMConnection conn = matrix.getConnection(firstItemSelector.getValue(), secondItemSelector.getValue());
                DSMConnection symmetricConn = matrix.getSymmetricConnection(firstItemSelector.getValue(), secondItemSelector.getValue());
                if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                    changesToMakeView.getItems().add(conn);
                }
                if(symmetricConn != null && !changesToMakeView.getItems().contains(symmetricConn)) {
                    changesToMakeView.getItems().add(symmetricConn);
                }
            } else {
                DSMConnection conn = matrix.getConnection(secondItemSelector.getValue(), firstItemSelector.getValue());
                DSMConnection symmetricConn = matrix.getSymmetricConnection(secondItemSelector.getValue(), firstItemSelector.getValue());
                if(conn != null && !changesToMakeView.getItems().contains(conn)) {
                    changesToMakeView.getItems().add(conn);
                }
                if(symmetricConn != null && !changesToMakeView.getItems().contains(symmetricConn)) {
                    changesToMakeView.getItems().add(symmetricConn);
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
     * Configures a row in the edit grouping window. Does not add the row to its parent, but does not return it
     *
     * @param matrix     the matrix that will be updated when data about the grouping changes
     * @param grouping   the grouping object from the matrix that this row is representing
     * @param parent     the parent display object that will hold the row (used so that if deleted it is removed from parent)
     * @param deletable  if the grouping is allowed to be deleted (if yes there will be a button to delete it)
     *
     * @return           the HBox that contains the row with all the widgets configured
     */
    private static HBox configureGroupingEditorRow(SymmetricDSM matrix, Grouping grouping, VBox parent, boolean deletable) {
        HBox display = new HBox();

        TextField groupingName = new TextField();     // use a text field to display the name so that it can be renamed easily
        groupingName.setText(grouping.getName());
        groupingName.setMaxWidth(Double.MAX_VALUE);
        groupingName.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (!newPropertyValue) {  // TextField changed to be not focused so update the new name in the matrix
                if(!groupingName.getText().equals(grouping.getName())) {  // name changed
                    matrix.renameGrouping(grouping, groupingName.getText());
                }
            }
        });

        Label groupingColorPickerLabel = new Label("Grouping Color: ");
        groupingColorPickerLabel.setPadding(new Insets(10, 10, 10, 10));
        groupingColorPickerLabel.setAlignment(Pos.TOP_RIGHT);
        ColorPicker groupingColorPicker = new ColorPicker(grouping.getColor());
        groupingColorPicker.setOnAction(e -> {
            Color newColor = Color.color(groupingColorPicker.getValue().getRed(), groupingColorPicker.getValue().getGreen(), groupingColorPicker.getValue().getBlue());
            if(!newColor.equals(grouping.getColor())) {
                matrix.updateGroupingColor(grouping, newColor);
            }
        });

        Label fontColorPickerLabel = new Label("Font Color: ");
        fontColorPickerLabel.setPadding(new Insets(10, 10, 10, 30));
        fontColorPickerLabel.setAlignment(Pos.TOP_RIGHT);
        ColorPicker groupingFontColorPicker = new ColorPicker(grouping.getFontColor());
        groupingFontColorPicker.setOnAction(e -> {
            Color newColor = Color.color(groupingFontColorPicker.getValue().getRed(), groupingFontColorPicker.getValue().getGreen(), groupingFontColorPicker.getValue().getBlue());
            if(!newColor.equals(grouping.getFontColor())) {
                matrix.updateGroupingFontColor(grouping, newColor);
            }
        });

        HBox deleteButtonSpace = new HBox();
        deleteButtonSpace.setPadding(new Insets(0, 0, 0, 50));
        Button deleteButton = new Button("Delete Grouping");  // wrap in HBox to add padding (doesn't work right otherwise)
        deleteButton.setOnAction(e -> {
            parent.getChildren().remove(display);  // delete the display item
            matrix.removeGrouping(grouping);       // delete the grouping from the matrix
        });
        deleteButtonSpace.getChildren().add(deleteButton);

        display.getChildren().addAll(groupingName, Misc.getHorizontalSpacer(), groupingColorPickerLabel, groupingColorPicker, fontColorPickerLabel, groupingFontColorPicker);
        if(deletable) {
            display.getChildren().add(deleteButtonSpace);
        }

        return display;
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
