package Matrices.SideBarTools;

import Matrices.Data.AbstractDSMData;
import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMInterfaceType;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.SymmetricDSMData;
import Matrices.Views.AbstractMatrixView;
import UI.Widgets.Misc;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.Map;
import java.util.Vector;


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
     * Cell factory for a matrix item in a listview. Takes an integer that represents a uid in the matrix. Appends
     * row or column to it based on if the item is in the matrix as a row or a column. If the uid is set to
     * Integer.MAX_VALUE then the string 'All' is displayed
     */
    protected final Callback<ListView<Integer>, ListCell<Integer>> MATRIX_ITEM_INTEGER_COMBOBOX_CELL_FACTORY = new Callback<>() {
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
                            setText(matrix.getItem(item).getName().getValue() + " (Row)");
                        } else {
                            setText(matrix.getItem(item).getName().getValue() + " (Column)");
                        }
                    }
                }
            };
        }
    };


    /**
     * Cell factory for a matrix item in a combobox. Takes a DSMItem and displays the name of it
     */
    protected final Callback<ListView<DSMItem>, ListCell<DSMItem>> MATRIX_ITEM_COMBOBOX_CELL_FACTORY = new Callback<>() {
        @Override
        public ListCell<DSMItem> call(ListView<DSMItem> l) {
            return new ListCell<>() {
                @Override
                protected void updateItem(DSMItem item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName().getValue());
                    }
                }
            };
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

        deleteMatrixItems.setOnAction(e -> deleteMatrixItemsCallback());
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
    protected abstract void deleteMatrixItemsCallback();


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
