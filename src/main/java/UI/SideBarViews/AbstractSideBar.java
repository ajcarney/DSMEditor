package UI.SideBarViews;

import Matrices.Data.AbstractDSMData;
import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMInterfaceType;
import Matrices.Data.Entities.DSMItem;
import UI.ConfigureConnectionInterfaces;
import UI.MatrixViews.AbstractMatrixView;
import UI.Widgets.DSMItemComboBox;
import UI.Widgets.Misc;
import UI.Widgets.NumericTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Generic class for creating a sidebar to interact with matrices
 */
public abstract class AbstractSideBar {
    protected VBox layout;

    protected final Button addMatrixItems = new Button();
    protected final Button deleteMatrixItems = new Button();
    protected final Button appendConnections = new Button("Append Connections");
    protected final Button setConnections = new Button("Set Connections");
    protected final Button deleteConnections = new Button("Delete Connections");
    protected final Button sort = new Button("Sort");
    protected final Button reDistributeIndices = new Button("Re-Distribute Indices");
    protected final Button configureInterfaces = new Button("Configure Interface Types");

    protected AbstractMatrixView matrixView;
    protected AbstractDSMData matrix;


//region Cell Factories
    /**
     * Cell factory for displaying DSM connections in a listview. Takes a DSMConnection and Prepends DELETE
     * if the connection name is empty and the weight is set to Double.MAX_VALUE
     */
    protected final Callback<ListView<DSMConnection>, ListCell<DSMConnection>> CONNECTION_CELL_FACTORY = new Callback<>() {
        @Override
        public ListCell<DSMConnection> call(ListView<DSMConnection> l) {
            return new ListCell<>() {
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
                            "DELETE " +
                            matrix.getItem(connection.getRowUid()).getName().getValue() + " (Row):" +
                            matrix.getItem(connection.getColUid()).getName().getValue() + " (Col)"
                        );
                    }
                }
            };
        }
    };


    /**
     * Cell factory for a deleting connections listview. Takes a DSMConnection and always prepends DELETE to it
     */
    protected final Callback<ListView<DSMConnection>, ListCell<DSMConnection>> DELETE_CONNECTION_CELL_FACTORY = new Callback<>() {
        @Override
        public ListCell<DSMConnection> call(ListView<DSMConnection> l) {
            return new ListCell<>() {
                @Override
                protected void updateItem(DSMConnection conn, boolean empty) {
                    super.updateItem(conn, empty);

                    if (empty || conn == null) {
                        setText(null);
                    } else {
                        setText(
                            "DELETE " +
                            matrix.getItem(conn.getRowUid()).getName().getValue() + ":" +
                            matrix.getItem(conn.getColUid()).getName().getValue()
                        );
                    }
                }
            };
        }
    };


    /**
     * String converter for a searchable combobox of DSMItems. Holds an uid and converts it to human-readable
     * format. Integer.MAX_VALUE is treated as a special case of all items being selected
     */
    protected final StringConverter<Integer> DSM_ITEM_COMBOBOX_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(Integer object) {
            if (object == null) {
                return "";
            } else {
                if(object == Integer.MAX_VALUE) {
                    return "All";
                } else if(matrix.isRow(matrix.getItem(object).getUid())) {
                    return matrix.getItem(object).getName().getValue() + " (Row)";
                } else {
                    return matrix.getItem(object).getName().getValue() + " (Column)";
                }
            }
        }

        @Override
        public Integer fromString(String string) {
            return null;
        }
    };
//endregion


    /**
     * Creates a new Sidebar object. Sets up the gui and all its widgets and puts them in the layout field.
     * Requires a MatrixView object to get the matrix and a EditorPane object to get the current focused tab
     * and call updates to it.
     *
     * @param matrix      the matrix data object instance
     * @param matrixView  the matrix view instance for the matrix
     */
    AbstractSideBar(AbstractDSMData matrix, AbstractMatrixView matrixView) {
        layout = new VBox();
        this.matrixView = matrixView;
        this.matrix = matrix;

        addMatrixItems.setOnAction(e -> addMatrixItemsCallback());
        addMatrixItems.setMaxWidth(Double.MAX_VALUE);

        // default to rows only for deleting items
        deleteMatrixItems.setOnAction(e -> deleteMatrixItemsCallback(matrix.getRows()));
        deleteMatrixItems.setMaxWidth(Double.MAX_VALUE);

        appendConnections.setOnAction(e -> appendConnectionsCallback());
        appendConnections.setMaxWidth(Double.MAX_VALUE);

        setConnections.setOnAction(e -> setConnectionsCallback());
        setConnections.setMaxWidth(Double.MAX_VALUE);

        deleteConnections.setOnAction(e -> deleteConnectionsCallback());
        deleteConnections.setMaxWidth(Double.MAX_VALUE);

        sort.setOnAction(e -> sortCallback());
        sort.setMaxWidth(Double.MAX_VALUE);

        reDistributeIndices.setOnAction(e -> reDistributeIndicesCallback());
        reDistributeIndices.setMaxWidth(Double.MAX_VALUE);

        configureInterfaces.setOnAction(e -> configureInterfacesCallback());
        configureInterfaces.setMaxWidth(Double.MAX_VALUE);
    }


    /**
     * Disables the sidebar buttons
     */
    public abstract void setDisabled();


    /**
     * Enables the sidebar buttons
     */
    public abstract void setEnabled();


    /**
     * Sets up the button callback for adding items to the matrix
     */
    protected abstract void addMatrixItemsCallback();


    /**
     * Sets up the button callback for deleting items from the matrix
     */
    protected void deleteMatrixItemsCallback(List<DSMItem> items) {
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

        Button deleteSelected = new Button("Delete Selected from Change Stack");
        deleteSelected.setOnAction(e -> {
            changesToMakeView.getItems().removeAll(changesToMakeView.getSelectionModel().getSelectedItems());
        });

        // Create user input area
        HBox entryArea = new HBox();

        // ComboBox to choose which row or column to modify connections of
        DSMItemComboBox itemSelector = new DSMItemComboBox();  // rowUid | colUid | name | weight

        itemSelector.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(itemSelector, Priority.ALWAYS);
        itemSelector.setPromptText("Item Name");
        itemSelector.getItems().addAll(items);

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
                matrix.deleteItem(item);
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
     * Sets up the button callback for appending connections to the matrix
     */
    protected abstract void appendConnectionsCallback();


    /**
     * Sets up the button callback for setting connections in the matrix
     */
    protected abstract void setConnectionsCallback();


    /**
     * Sets up the button callback for deleting connections in the matrix
     */
    protected abstract void deleteConnectionsCallback();




    /**
     * Sets up the button callback for sorting the matrix
     */
    protected void sortCallback() {
        matrixView.refreshView();
    }



    /**
     * Sets up widget that contains a view for choosing connections and typing a name and weight. Creates the widget by
     * modifying other widgets in place. This is so that the function that calls this can get data from this widget.
     * Sets the styling and listeners/callbacks
     *
     * @param matrix                the AbstractDSMData matrix the widget is for
     * @param connectionsArea       the HBox that will contain all the widgets
     * @param itemSelector          the combobox that holds the items
     * @param connectionCheckBoxes  a hashmap of the connection checkboxes and the corresponding dsm item
     * @param tg                    the toggle group for choosing between rows and columns
     * @param selectByRow           the radio button for selecting connections by row
     * @param selectByCol           the radio button for selecting connections by column
     * @param connectionNameEntry   the entry text field for the connection name
     * @param weightEntry           the entry numeric text field for the connection weight
     */
    public static void createConnectionsViewerScrollPane(
            AbstractDSMData matrix,
            HBox connectionsArea,
            DSMItemComboBox itemSelector,
            HashMap<CheckBox, DSMItem> connectionCheckBoxes,
            ToggleGroup tg,
            RadioButton selectByRow,
            RadioButton selectByCol,
            TextField connectionNameEntry,
            NumericTextField weightEntry,
            ArrayList<DSMInterfaceType> selectedInterfaces,
            Boolean checkConnectionsIfExist
    ) {
        // area to interact with the connections
        connectionsArea.setSpacing(10);
        connectionsArea.setPadding(new Insets(10, 10, 10, 10));

        // HBox area full of checklists to modify the connections, default to columns
        HBox connectionsModifier = new HBox();
        connectionsModifier.setSpacing(10);
        connectionsModifier.setPadding(new Insets(10, 10, 10, 10));

        for(DSMItem conn : matrix.getCols()) {  // will default to choosing a row item so populate the scroll window with the columns
            VBox connectionVBox = new VBox();
            connectionVBox.setAlignment(Pos.CENTER);

            Label name = new Label(conn.getName().getValue());
            CheckBox box = new CheckBox();
            connectionCheckBoxes.put(box, conn);
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
            connectionCheckBoxes.clear();
            if (rb.equals(selectByRow)) {  // clear all items and add rows to it
                for(DSMItem col : matrix.getCols()) {  // create the checkboxes
                    if(itemSelector.getValue() != null && col.getAliasUid() != null && col.getAliasUid().equals(itemSelector.getValue().getUid())) {  // don't allow creating connections between same row and column pair
                        continue;
                    }
                    VBox connectionVBox = new VBox();
                    connectionVBox.setAlignment(Pos.CENTER);

                    Label name = new Label(col.getName().getValue());
                    CheckBox box = new CheckBox();
                    connectionCheckBoxes.put(box, col);
                    connectionVBox.getChildren().addAll(name, box);
                    connectionsModifier.getChildren().add(connectionVBox);
                }
            } else if (rb.equals(selectByCol)) {
                for(DSMItem row : matrix.getRows()) {  // create the checkboxes
                    if(itemSelector.getValue() != null && row.getAliasUid() != null && row.getAliasUid().equals(itemSelector.getValue().getUid())) {  // don't allow creating connections between same row and column pair
                        continue;
                    }
                    VBox connectionVBox = new VBox();
                    connectionVBox.setAlignment(Pos.CENTER);

                    Label name = new Label(row.getName().getValue());
                    CheckBox box = new CheckBox();
                    connectionCheckBoxes.put(box, row);
                    connectionVBox.getChildren().addAll(name, box);
                    connectionsModifier.getChildren().add(connectionVBox);
                }
            }
        });

        rowColRadioButtons.getChildren().addAll(selectByRow, selectByCol);
        itemSelectorView.getChildren().addAll(l, rowColRadioButtons, itemSelector);

        if(checkConnectionsIfExist) {
            itemSelector.setOnAction(ee -> {  // when item changes, change the connections that are selected
                if (itemSelector.getValue() == null) {  // ensure connection can be added
                    return;
                }
                for (Map.Entry<CheckBox, DSMItem> entry : connectionCheckBoxes.entrySet()) {
                    RadioButton rb = (RadioButton) tg.getSelectedToggle();
                    if (rb.equals(selectByRow) && matrix.getConnection(itemSelector.getValue().getUid(), entry.getValue().getUid()) != null) {
                        entry.getKey().setSelected(true);
                    } else if (rb.equals(selectByCol) && matrix.getConnection(entry.getValue().getUid(), itemSelector.getValue().getUid()) != null) {
                        entry.getKey().setSelected(true);
                    } else {
                        entry.getKey().setSelected(false);
                    }
                }
            });
        }

        // area to set details for the connection
        VBox connectionDetailsLayout = new VBox();
        connectionDetailsLayout.setSpacing(10);
        connectionDetailsLayout.setPadding(new Insets(10, 10, 10, 10));
        VBox.setVgrow(connectionDetailsLayout, Priority.ALWAYS);
        connectionDetailsLayout.setMinWidth(Region.USE_PREF_SIZE);

        connectionNameEntry.setPromptText("Connection Name");
        connectionNameEntry.setMinWidth(connectionNameEntry.getPrefWidth());

        weightEntry.setPromptText("Connection Weight");
        weightEntry.setMinWidth(weightEntry.getPrefWidth());

        Button configureInterfacesButton = new Button("Configure Interfaces");
        configureInterfacesButton.setOnAction(e -> {
            ArrayList<DSMInterfaceType> newInterfaceTypes = ConfigureConnectionInterfaces.configureConnectionInterfaces(matrix.getInterfaceTypes(), selectedInterfaces);
            selectedInterfaces.clear();
            selectedInterfaces.addAll(newInterfaceTypes);  // copy all the interfaces to the array
        });
        HBox configureInterfacesPane = new HBox();  // wrap in a pane to center it
        configureInterfacesPane.getChildren().addAll(Misc.getHorizontalSpacer(), configureInterfacesButton, Misc.getHorizontalSpacer());

        connectionDetailsLayout.getChildren().addAll(connectionNameEntry, weightEntry, configureInterfacesPane);

        connectionsArea.getChildren().addAll(itemSelectorView, scrollPane, Misc.getHorizontalSpacer(), connectionDetailsLayout);
    }


    /**
     * Configures a row in the edit grouping window. Does not add the row to its parent, but does not return it
     *
     * @param matrix              the matrix that will be updated when data about the grouping changes
     * @param interfaceTypeGroup  the interface type's grouping
     * @param interfaceType       the interface type to modify
     * @param parent              the parent display object that will hold the row (used so that if deleted it is removed from parent)
     *
     * @return           the HBox that contains the row with all the widgets configured
     */
    private static HBox configureInterfaceEditorRow(AbstractDSMData matrix, StringProperty interfaceTypeGroup, DSMInterfaceType interfaceType, VBox parent) {
        HBox display = new HBox();

        Label nameLabel = new Label("Name:");
        nameLabel.setPadding(new Insets(0, 10, 0, 10));

        TextField interfaceName = new TextField();     // use a text field to display the name so that it can be renamed easily
        interfaceName.setText(interfaceType.getName());
        interfaceName.setMaxWidth(Double.MAX_VALUE);
        interfaceName.setPrefColumnCount(30);
        interfaceName.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (!newPropertyValue) {  // TextField changed to be not focused so update the new name in the matrix
                if(!interfaceName.getText().equals(interfaceType.getName())) {  // name changed
                    matrix.renameInterfaceType(interfaceType, interfaceName.getText());
                }
            }
        });


        Label abbreviationLabel = new Label("Abbreviation:");
        abbreviationLabel.setPadding(new Insets(0, 10, 0, 10));

        TextField interfaceAbbreviation = new TextField();     // use a text field to display the name so that it can be renamed easily
        interfaceAbbreviation.setText(interfaceType.getAbbreviation());
        interfaceAbbreviation.setPrefColumnCount(3);
        interfaceAbbreviation.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (!newPropertyValue) {  // TextField changed to be not focused so update the new name in the matrix
                if(!interfaceAbbreviation.getText().equals(interfaceType.getName())) {  // name changed
                    matrix.updateInterfaceTypeAbbreviation(interfaceType, interfaceAbbreviation.getText());
                }
            }
        });


        HBox deleteButtonSpace = new HBox();
        deleteButtonSpace.setPadding(new Insets(0, 0, 0, 50));
        Button deleteButton = new Button("Delete Interface");  // wrap in HBox to add padding (doesn't work right otherwise)
        deleteButton.setOnAction(e -> {
            parent.getChildren().remove(display);  // delete the display item
            matrix.removeInterface(interfaceTypeGroup.getValue(), interfaceType);       // delete the grouping from the matrix
        });
        deleteButtonSpace.getChildren().add(deleteButton);

        display.getChildren().addAll(nameLabel, interfaceName, Misc.getHorizontalSpacer(), abbreviationLabel, interfaceAbbreviation, deleteButtonSpace);

        return display;
    }


    /**
     * Creates a row to be able to edit an interface type grouping. Does not add the created widget to its parent
     *
     * @param matrix              the matrix data
     * @param interfaceTypeGroup  the group for the interface
     * @param parent              the parent object
     * @return  the node to be displayed
     */
    private static VBox configureInterfaceGroupingEditorRow(AbstractDSMData matrix, String interfaceTypeGroup, VBox parent) {
        VBox interfacesLayout = new VBox();
        HBox groupingEditRow = new HBox();

        StringProperty interfaceGroupName = new SimpleStringProperty(interfaceTypeGroup);

        // text entry for setting the interface grouping name
        TextField interfaceGroupTextField = new TextField();     // use a text field to display the name so that it can be renamed easily
        interfaceGroupTextField.setText(interfaceGroupName.getValue());
        interfaceGroupTextField.setMaxWidth(Double.MAX_VALUE);
        interfaceGroupTextField.setPrefColumnCount(30);
        interfaceGroupTextField.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (!newPropertyValue) {  // TextField changed to be not focused so update the new name in the matrix
                if(!matrix.getInterfaceGroupings().contains(interfaceGroupTextField.getText())) {  // name changed
                    matrix.renameInterfaceTypeGrouping(interfaceGroupName.getValue(), interfaceGroupTextField.getText());
                    interfaceGroupName.set(interfaceGroupTextField.getText());
                } else {
                    interfaceGroupTextField.setText(interfaceGroupName.getValue());
                }
            }
        });

        // button for deleting this interface grouping
        HBox deleteButtonSpace = new HBox();
        deleteButtonSpace.setPadding(new Insets(0, 0, 0, 50));
        Button deleteButton = new Button("Delete Grouping");  // wrap in HBox to add padding (doesn't work right otherwise)
        deleteButton.setOnAction(e -> {
            parent.getChildren().remove(interfacesLayout);
            matrix.removeInterfaceTypeGrouping(interfaceGroupName.getValue());       // delete the grouping from the matrix
        });
        deleteButtonSpace.getChildren().add(deleteButton);

        groupingEditRow.getChildren().addAll(interfaceGroupTextField, Misc.getHorizontalSpacer(), deleteButtonSpace);


        // pane for all the interfaces of this grouping
        VBox interfacesPane = new VBox();
        interfacesPane.setPadding(new Insets(0, 0, 0, 50));
        interfacesPane.setSpacing(7);
        for(DSMInterfaceType i : matrix.getInterfaceTypes().get(interfaceTypeGroup)) {
            HBox interfaceTypePane = configureInterfaceEditorRow(matrix, interfaceGroupName, i, interfacesPane);
            interfacesPane.getChildren().add(interfaceTypePane);
        }

        // pane for adding a new interface
        HBox addInterfaceButtonPane = new HBox();
        addInterfaceButtonPane.setAlignment(Pos.CENTER);
        Button addInterfaceButton = new Button("Add New Interface");
        addInterfaceButton.setOnAction(e -> {
            DSMInterfaceType newInterface = new DSMInterfaceType("New Interface", "I");
            matrix.addInterface(interfaceGroupName.getValue(), newInterface);
            HBox interfaceTypePane = configureInterfaceEditorRow(matrix, interfaceGroupName, newInterface, interfacesPane);
            interfacesPane.getChildren().add(interfaceTypePane);
        });
        addInterfaceButtonPane.getChildren().add(addInterfaceButton);

        // add all the nodes to the root
        interfacesLayout.getChildren().addAll(groupingEditRow, interfacesPane, addInterfaceButtonPane);
        interfacesLayout.setSpacing(12);
        interfacesLayout.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        interfacesLayout.setPadding(new Insets(5));

        return interfacesLayout;
    }


    /**
     * Opens a window to configure interface types for a matrix
     */
    protected void configureInterfacesCallback() {
        // Create Root window
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("Configure Groupings");

        VBox mainView = new VBox();
        mainView.setSpacing(15);
        VBox groupingsView = new VBox();
        groupingsView.setSpacing(25);

        for(String grouping : matrix.getInterfaceGroupings()) {
            VBox interfaceEditLayout = configureInterfaceGroupingEditorRow(matrix, grouping, groupingsView);
            groupingsView.getChildren().addAll(interfaceEditLayout);
        }


        HBox addInterfaceGroupingPane = new HBox();
        Button addInterfaceGroupingButton = new Button("Add New Interface Grouping");
        addInterfaceGroupingButton.setOnAction(e -> {
            int i = matrix.getInterfaceGroupings().size() + 1;
            String groupingName = "New Interface Grouping" + i;
            while(matrix.getInterfaceGroupings().contains(groupingName)) {  // keep increasing number until reaching a unique name
                i += 1;
                groupingName = "New Interface Grouping" + i;
            }

            matrix.addInterfaceTypeGrouping(groupingName);
            VBox interfaceEditLayout = configureInterfaceGroupingEditorRow(matrix, groupingName, groupingsView);
            groupingsView.getChildren().add(interfaceEditLayout);
        });
        addInterfaceGroupingPane.getChildren().addAll(Misc.getHorizontalSpacer(), addInterfaceGroupingButton);


        ScrollPane interfacesScrollView = new ScrollPane(groupingsView);
        interfacesScrollView.setFitToWidth(true);
        mainView.getChildren().add(interfacesScrollView);
        mainView.getChildren().add(addInterfaceGroupingPane);


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
        Scene scene = new Scene(layout, 1000, 400);
        window.setScene(scene);
        scene.getWindow().setOnHidden(e -> {  // TODO: 6/17/2020 changed from setOnCloseRequest when it was working before and idk why this fixed it
            window.close();                        // changes have already been made so just close and refresh the screen
            matrix.setCurrentStateAsCheckpoint();
            matrixView.refreshView();
        });
        window.showAndWait();
    }


    /**
     * Sets up the button callback for re-distributing sort indices
     */
    protected void reDistributeIndicesCallback() {
        matrix.reDistributeSortIndices();
        matrixView.refreshView();
        matrix.setCurrentStateAsCheckpoint();
    }


    /**
     * Returns the VBox of the layout so that it can be added to a scene
     *
     * @return the VBox layout of the toolbar
     */
    public VBox getLayout() {
        return layout;
    }
}
