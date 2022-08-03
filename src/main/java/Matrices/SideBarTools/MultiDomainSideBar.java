package Matrices.SideBarTools;

import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMInterfaceType;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.MultiDomainDSMData;
import Matrices.Views.MultiDomainView;
import UI.Widgets.Misc;
import UI.Widgets.NumericTextField;
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
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


/**
 * Creates a sidebar with methods to interact with a symmetric matrix
 */
public class MultiDomainSideBar extends AbstractSideBar {

    private final Button configureGroupings = new Button("Configure Groupings");
    private final MultiDomainDSMData matrix;

    /**
     * Constructor for a new side bar for a multi-domain matrix
     *
     * @param matrix      the matrix the side bar will make changes to
     * @param matrixView  the matrix view instance for the matrix
     */
    public MultiDomainSideBar(MultiDomainDSMData matrix, MultiDomainView matrixView) {
        super(matrix, matrixView);
        this.matrix = matrix;

        addMatrixItems.setText("Add Rows/Columns");
        deleteMatrixItems.setText("Delete Rows/Columns");

        configureGroupings.setOnAction(e -> configureGroupingsCallback());
        configureGroupings.setMaxWidth(Double.MAX_VALUE);

        layout.getChildren().addAll(addMatrixItems, deleteMatrixItems, appendConnections, setConnections, deleteConnections, configureInterfaces, configureGroupings, sort, reDistributeIndices);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(20);
        layout.setAlignment(Pos.CENTER);
    }


    /**
     * Disables the sidebar buttons
     */
    public void setDisabled() {
        addMatrixItems.setDisable(true);
        deleteMatrixItems.setDisable(true);
        appendConnections.setDisable(true);
        setConnections.setDisable(true);
        deleteConnections.setDisable(true);
        configureGroupings.setDisable(true);
        sort.setDisable(true);
        reDistributeIndices.setDisable(true);
        configureInterfaces.setDisable(true);
    }


    /**
     * Enables the sidebar buttons
     */
    public void setEnabled() {
        addMatrixItems.setDisable(false);
        deleteMatrixItems.setDisable(false);
        appendConnections.setDisable(false);
        setConnections.setDisable(false);
        deleteConnections.setDisable(false);
        configureGroupings.setDisable(false);
        sort.setDisable(false);
        reDistributeIndices.setDisable(false);
        configureInterfaces.setDisable(false);
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
        ListView<Pair<String, Grouping>> changesToMakeView = new ListView<>();
        changesToMakeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        changesToMakeView.setCellFactory(param -> new ListCell<>() {  // item name
            @Override
            protected void updateItem(Pair<String, Grouping> item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getKey() + " (Row/Column)  -  " + item.getValue().getName());
                }
            }
        });

        Button deleteSelected = new Button("Delete Selected from Change Stack");
        deleteSelected.setOnAction(e -> {
            changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
        });

        // Create user input area
        TextField textField = new TextField();
        textField.setMinWidth(500);
        textField.setPromptText("Row/Column Name");
        HBox.setHgrow(textField, Priority.ALWAYS);

        ComboBox<Grouping> domainSelector = new ComboBox<>();
        domainSelector.setPromptText("Domain");
        domainSelector.setMinWidth(Region.USE_PREF_SIZE);
        Callback<ListView<Grouping>, ListCell<Grouping>> groupingItemCellFactory = new Callback<>() {
            @Override
            public ListCell<Grouping> call(ListView<Grouping> l) {
                return new ListCell<>() {

                    @Override
                    protected void updateItem(Grouping group, boolean empty) {
                        super.updateItem(group, empty);
                        if (empty || group == null) {
                            setText("");
                        } else {
                            setText(group.getName());
                        }
                    }
                };
            }
        };
        domainSelector.setCellFactory(groupingItemCellFactory);
        domainSelector.setButtonCell(groupingItemCellFactory.call(null));
        domainSelector.getItems().addAll(matrix.getDomains());

        VBox itemData = new VBox();
        itemData.setSpacing(10);
        itemData.getChildren().addAll(textField, domainSelector);

        Button addItem = new Button("Add Item");
        addItem.setOnAction(e -> {
            if(domainSelector.getValue() != null) {
                changesToMakeView.getItems().add(new Pair<>(textField.getText(), domainSelector.getValue()));
            }
        });
        HBox entryArea = new HBox();
        entryArea.getChildren().addAll(itemData, Misc.getHorizontalSpacer(), addItem);
        entryArea.setPadding(new Insets(10, 10, 10, 10));
        entryArea.setSpacing(20);

        // create HBox for user to close with our without changes
        HBox closeArea = new HBox();
        Button applyButton = new Button("Apply Changes");
        applyButton.setOnAction(e -> {
            for(Pair<String, Grouping> item : changesToMakeView.getItems()) {
                matrix.createItem(item.getKey(), item.getValue());
            }
            matrix.setCurrentStateAsCheckpoint();
            window.close();
            matrixView.refreshView();
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
                    setText(matrix.getItem(item).getName().getValue() + " (Row/Column)");
                }
            }
        });

        Button deleteSelected = new Button("Delete Selected from Change Stack");
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
            matrixView.refreshView();
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

        Button deleteSelected = new Button("Delete Selected from Change Stack");
        deleteSelected.setOnAction(ee -> {
            changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
        });


        // set up the items needed by the builder
        HBox connectionsArea = new HBox();
        ComboBox<DSMItem> itemSelector = new ComboBox<>();
        HashMap<CheckBox, DSMItem> connections = new HashMap<>();
        ToggleGroup tg = new ToggleGroup();
        RadioButton selectByRow = new RadioButton("Row");
        RadioButton selectByCol = new RadioButton("Column");
        TextField connectionName = new TextField();
        NumericTextField weight = new NumericTextField(null);
        ArrayList<DSMInterfaceType> selectedInterfaces = new ArrayList<>();

        // create the viewer for connections
        WidgetBuilders.createConnectionsViewerScrollPane(
            matrix,
            connectionsArea,
            itemSelector,
            connections,
            tg,
            selectByRow,
            selectByCol,
            connectionName,
            weight,
            selectedInterfaces,
            false
        );


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
                if(!entry.getKey().isSelected()) {  // skip because checkbox was not selected
                    continue;
                }

                if(tg.getSelectedToggle().equals(selectByRow)) {  // selecting by row
                    DSMConnection conn = new DSMConnection(connectionName.getText(), weight.getNumericValue(), itemSelector.getValue().getUid(), entry.getValue().getUid(), selectedInterfaces);
                    if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                        changesToMakeView.getItems().add(conn);
                    }

                } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                    DSMConnection conn = new DSMConnection(connectionName.getText(), weight.getNumericValue(), entry.getValue().getUid(), itemSelector.getValue().getUid(), selectedInterfaces);
                    if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                        changesToMakeView.getItems().add(conn);
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
                if(!entry.getKey().isSelected()) {
                    continue;
                }

                if(tg.getSelectedToggle().equals(selectByRow)) {  // selecting by row
                    int rowUid = itemSelector.getValue().getUid();
                    int colUid = entry.getValue().getUid();
                    int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                    int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                    DSMConnection conn1 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), rowUid, colUid, selectedInterfaces);
                    DSMConnection conn2 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), symmetricRowUid, symmetricColUid, selectedInterfaces);

                    if(!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                        changesToMakeView.getItems().add(conn1);
                    }
                    if(!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                        changesToMakeView.getItems().add(conn2);
                    }
                } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                    int rowUid = entry.getValue().getUid();
                    int colUid = itemSelector.getValue().getUid();
                    int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                    int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                    DSMConnection conn1 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), rowUid, colUid, selectedInterfaces);
                    DSMConnection conn2 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), symmetricRowUid, symmetricColUid, selectedInterfaces);

                    if(!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                        changesToMakeView.getItems().add(conn1);
                    }
                    if(!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                        changesToMakeView.getItems().add(conn2);
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
                matrix.modifyConnection(conn.getRowUid(), conn.getColUid(), conn.getConnectionName(), conn.getWeight(), selectedInterfaces);
            }
            window.close();
            matrixView.refreshView();
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

        Button deleteSelected = new Button("Delete Selected from Change Stack");
        deleteSelected.setOnAction(ee -> {
            changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
        });


        // set up the items needed by the builder
        HBox connectionsArea = new HBox();
        ComboBox<DSMItem> itemSelector = new ComboBox<>();
        HashMap<CheckBox, DSMItem> connections = new HashMap<>();
        ToggleGroup tg = new ToggleGroup();
        RadioButton selectByRow = new RadioButton("Row");
        RadioButton selectByCol = new RadioButton("Column");
        TextField connectionName = new TextField();
        NumericTextField weight = new NumericTextField(null);
        ArrayList<DSMInterfaceType> selectedInterfaces = new ArrayList<>();

        // create the viewer for connections
        WidgetBuilders.createConnectionsViewerScrollPane(
            matrix,
            connectionsArea,
            itemSelector,
            connections,
            tg,
            selectByRow,
            selectByCol,
            connectionName,
            weight,
            selectedInterfaces,
            true
        );


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
                        DSMConnection conn = new DSMConnection(connectionName.getText(), weight.getNumericValue(), itemSelector.getValue().getUid(), entry.getValue().getUid(), selectedInterfaces);
                        if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn);
                        }

                    } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                        DSMConnection conn = new DSMConnection(connectionName.getText(), weight.getNumericValue(), entry.getValue().getUid(), itemSelector.getValue().getUid(), selectedInterfaces);
                        if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn);
                        }
                    }
                } else {  // delete the connection
                    if(tg.getSelectedToggle().equals(selectByRow)) {  // selecting by row
                        DSMConnection conn = new DSMConnection("", Double.MAX_VALUE, itemSelector.getValue().getUid(), entry.getValue().getUid(), new ArrayList<>());
                        if(!changesToMakeView.getItems().contains(conn)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn);
                        }

                    } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                        DSMConnection conn = new DSMConnection("", Double.MAX_VALUE, entry.getValue().getUid(), itemSelector.getValue().getUid(), new ArrayList<>());
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

                        DSMConnection conn1 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), rowUid, colUid, selectedInterfaces);
                        DSMConnection conn2 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), symmetricRowUid, symmetricColUid, selectedInterfaces);

                        if(!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn1);
                        }
                        if(!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn2);
                        }
                    } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                        int rowUid = entry.getValue().getUid();
                        int colUid = itemSelector.getValue().getUid();
                        int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                        int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                        DSMConnection conn1 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), rowUid, colUid, selectedInterfaces);
                        DSMConnection conn2 = new DSMConnection(connectionName.getText(), weight.getNumericValue(), symmetricRowUid, symmetricColUid, selectedInterfaces);

                        if(!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn1);
                        }
                        if(!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn2);
                        }
                    }
                } else {  // deleting connection
                    if(tg.getSelectedToggle().equals(selectByRow)) {  // selecting by row
                        int rowUid = itemSelector.getValue().getUid();
                        int colUid = entry.getValue().getUid();
                        int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                        int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                        DSMConnection conn1 = new DSMConnection("", Double.MAX_VALUE, rowUid, colUid, new ArrayList<>());
                        DSMConnection conn2 = new DSMConnection("", Double.MAX_VALUE, symmetricRowUid, symmetricColUid, new ArrayList<>());

                        if(!changesToMakeView.getItems().contains(conn1)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn1);
                        }
                        if(!changesToMakeView.getItems().contains(conn2)) {  // ensure no duplicates
                            changesToMakeView.getItems().add(conn2);
                        }
                    } else if(tg.getSelectedToggle().equals(selectByCol)) {  // selecting by column
                        int rowUid = entry.getValue().getUid();
                        int colUid = itemSelector.getValue().getUid();
                        int symmetricRowUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getKey();
                        int symmetricColUid = matrix.getSymmetricConnectionUids(rowUid, colUid).getValue();

                        DSMConnection conn1 = new DSMConnection("", Double.MAX_VALUE, rowUid, colUid, new ArrayList<>());
                        DSMConnection conn2 = new DSMConnection("", Double.MAX_VALUE, symmetricRowUid, symmetricColUid, new ArrayList<>());

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
                    matrix.modifyConnection(conn.getRowUid(), conn.getColUid(), conn.getConnectionName(), conn.getWeight(), selectedInterfaces);
                } else {
                    matrix.deleteConnection(conn.getRowUid(), conn.getColUid());
                }
            }
            window.close();
            matrixView.refreshView();
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

        Button deleteSelected = new Button("Delete Selected from Change Stack");
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

        Button deleteConnectionSymmetrically = new Button("Delete Connection Symmetrically");
        deleteConnectionSymmetrically.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteConnectionSymmetrically, Priority.ALWAYS);

        entryArea.getChildren().addAll(firstItemSelector, secondItemSelector, deleteConnection, deleteConnectionSymmetrically);
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
            matrixView.refreshView();
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
     * Creates a row for modifying groupings (either domain or domain-grouping) for a MultiDomain matrix
     *
     * @param matrix          the matrix that contains the grouping
     * @param grouping        the grouping to create the row for
     * @param deleteFunction  the function to run when the delete button is called
     * @param deletable       if a delete button should be present for the domain
     * @return                HBox of the row
     */
    private static HBox configureGroupingEditorRow(MultiDomainDSMData matrix, Grouping grouping, Runnable deleteFunction, boolean deletable) {
        HBox display = new HBox();
        display.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(display, Priority.ALWAYS);

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

        Label groupingColorPickerLabel = new Label("Color: ");
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
        Button deleteButton = new Button("Delete");  // wrap in HBox to add padding (doesn't work right otherwise)
        deleteButton.setOnAction(e -> {
            deleteFunction.run();
        });
        if(deletable) {
            deleteButtonSpace.getChildren().add(deleteButton);
        }

        display.getChildren().addAll(groupingName, Misc.getHorizontalSpacer(), groupingColorPickerLabel, groupingColorPicker, fontColorPickerLabel, groupingFontColorPicker);
        display.getChildren().add(deleteButtonSpace);

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

        VBox mainView = new VBox();
        mainView.setSpacing(15);


        ListView<Grouping> domainsListView = new ListView<>();
        domainsListView.setItems(matrix.getDomains());
        domainsListView.setSelectionModel(new Misc.NoSelectionModel<>());


        domainsListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Grouping> call(ListView<Grouping> param) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(Grouping domain, boolean empty) {
                        super.updateItem(domain, empty);

                        if(empty || domain == null) {
                            setGraphic(null);
                            return;
                        }

                        VBox domainEditCell = new VBox();    // the layout for the whole cell
                        HBox groupingsLayout = new HBox();   // the layout for the grouping edit rows and the up/down buttons
                        VBox groupingsVLayout = new VBox();  // the layout for just the grouping edit rows

                        groupingsLayout.setSpacing(10);

                        // configure the row for the domain
                        HBox domainRow = configureGroupingEditorRow(matrix, domain, () -> {
                            if(matrix.getDomains().size() > 1) {
                                // TODO: Prompt delete confirmation
                                matrix.removeDomain(domain);  // delete the grouping from the matrix
                            }  // TODO: prompt that it can't be deleted
                        }, true);
                        groupingsVLayout.getChildren().add(domainRow);


                        // configure the rows for the domain groupings
                        for(Grouping domainGrouping : matrix.getDomainGroupings(domain)) {
                            HBox groupingRow = new HBox();
                            groupingRow.setPadding(new Insets(0, 0, 0, 50));
                            groupingRow.setMaxWidth(Double.MAX_VALUE);
                            HBox.setHgrow(groupingRow, Priority.ALWAYS);
                            boolean deletable = !domainGrouping.getUid().equals(Integer.MAX_VALUE);

                            HBox groupingRowContent = configureGroupingEditorRow(matrix, domainGrouping, () -> {
                                if(matrix.getDomainGroupings(domain).size() > 1) {
                                    matrix.removeDomainGrouping(domain, domainGrouping);  // delete the grouping from the matrix
                                    groupingsVLayout.getChildren().remove(groupingRow);  // remove the domain from the view
                                }  // TODO: prompt that it can't be deleted
                            }, deletable);

                            groupingRow.getChildren().add(groupingRowContent);
                            groupingsVLayout.getChildren().add(groupingRow);
                        }


                        // configure the buttons for moving a domain up or down
                        VBox shiftButtonsLayout = new VBox();
                        shiftButtonsLayout.setAlignment(Pos.CENTER);
                        shiftButtonsLayout.setSpacing(10);
                        Button upButton = new Button(Character.toString(0x25b2));  // up arrow utf-16 hex code
                        upButton.setOnAction(ee -> matrix.shiftDomainUp(domain));

                        Button downButton = new Button(Character.toString(0x25bc));  // down arrow utf-16 hex code
                        downButton.setOnAction(ee -> matrix.shiftDomainDown(domain));
                        shiftButtonsLayout.getChildren().addAll(upButton, downButton);
                        groupingsLayout.getChildren().addAll(shiftButtonsLayout, groupingsVLayout);


                        // configure the button for adding a new grouping
                        Button addDomainGroupingButton = new Button("Add New Grouping");
                        addDomainGroupingButton.setOnAction(e -> {
                            Grouping newDomainGrouping = new Grouping("New Grouping", Color.color(1, 1, 1));
                            HBox groupingRow = new HBox();  // wrap content in another hbox to add padding for an indent
                            groupingRow.setPadding(new Insets(0, 0, 0, 50));
                            HBox groupingRowContent = configureGroupingEditorRow(matrix, newDomainGrouping, () -> {
                                if (matrix.getDomainGroupings(domain).size() > 1) {
                                    matrix.removeDomainGrouping(domain, newDomainGrouping);  // delete the grouping from the matrix
                                    groupingsVLayout.getChildren().remove(groupingRow);  // remove the domain from the view
                                }  // TODO: prompt that it can't be deleted
                            }, true);

                            groupingRow.getChildren().add(groupingRowContent);
                            groupingsVLayout.getChildren().add(groupingRow);
                            matrix.addDomainGrouping(domain, newDomainGrouping);
                        });

                        domainEditCell.setAlignment(Pos.CENTER);
                        domainEditCell.setPadding(new Insets(5));
                        domainEditCell.getChildren().addAll(groupingsLayout, addDomainGroupingButton);
                        domainEditCell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

                        setGraphic(domainEditCell);
                    }
                };
            }
        });



        HBox addDomainButtonPane = new HBox();
        Button addDomainButton = new Button("Add New Domain");
        addDomainButton.setOnAction(e -> {
            Grouping newDomain = new Grouping("New Domain", Color.color(1, 1, 1));
            matrix.addDomain(newDomain);
        });
        addDomainButtonPane.getChildren().addAll(Misc.getHorizontalSpacer(), addDomainButton);


        mainView.getChildren().add(domainsListView);
        mainView.getChildren().add(addDomainButtonPane);


        // create HBox for user to close with changes
        HBox closeArea = new HBox();
        Button applyAllButton = new Button("Ok");

        applyAllButton.setOnAction(e -> {
            window.close();        // changes have already been made so just close the window
        });

        closeArea.getChildren().addAll(Misc.getHorizontalSpacer(), applyAllButton);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(mainView, Misc.getVerticalSpacer(), closeArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);


        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout, 1000, 550);
        window.setScene(scene);
        scene.getWindow().setOnHidden(e -> {  // TODO: 6/17/2020 changed from setOnCloseRequest when it was working before and idk why this fixed it
            window.close();                        // changes have already been made so just close and refresh the screen
            matrix.setCurrentStateAsCheckpoint();
            matrixView.refreshView();
        });
        window.showAndWait();
    }

}
