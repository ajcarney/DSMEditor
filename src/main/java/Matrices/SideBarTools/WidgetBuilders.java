package Matrices.SideBarTools;

import Matrices.Data.AbstractDSMData;
import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMInterfaceType;
import Matrices.Data.Entities.DSMItem;
import UI.ConfigureConnectionInterfaces;
import UI.Widgets.Misc;
import UI.Widgets.NumericTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Class with static methods for helping to create widgets used by the sidebars
 */
public class WidgetBuilders {

    /**
     * Cell factory for displaying DSM connections in a listview. Takes a DSMConnection and Prepends DELETE
     * if the connection name is empty and the weight is set to Double.MAX_VALUE
     *
     * @return  Cell factory for displaying DSM connections in a listview.
     */
    public static Callback<ListView<DSMConnection>, ListCell<DSMConnection>> getConnectionCellFactory(AbstractDSMData matrix) {
        return new Callback<>() {
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

    }


    /**
     * @return  Cell factory for a deleting connections listview. Takes a DSMConnection and always prepends DELETE to it
     */
    public static Callback<ListView<DSMConnection>, ListCell<DSMConnection>> getDeleteConnectionCellFactory(AbstractDSMData matrix) {
        return new Callback<>() {
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
    }


    /**
     * Cell factory for a matrix item in a listview. Takes an integer that represents a uid in the matrix. Appends
     * row or column to it based on if the item is in the matrix as a row or a column. If the uid is set to
     * Integer.MAX_VALUE then the string 'All' is displayed
     *
     * @return  the cell factory
     */
    public static Callback<ListView<Integer>, ListCell<Integer>> getMatrixItemIntegerComboBoxCellFactory(AbstractDSMData matrix) {
        return new Callback<>() {
            @Override
            public ListCell<Integer> call(ListView<Integer> l) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            if (item == Integer.MAX_VALUE) {
                                setText("All");
                            } else if (matrix.isRow(matrix.getItem(item).getUid())) {
                                setText(matrix.getItem(item).getName().getValue() + " (Row)");
                            } else {
                                setText(matrix.getItem(item).getName().getValue() + " (Column)");
                            }
                        }
                    }
                };
            }
        };
    }


    /**
     * @return  Cell factory for a matrix item in a combobox. Takes a DSMItem and displays the name of it
     */
    public static Callback<ListView<DSMItem>, ListCell<DSMItem>> getMatrixItemComboBoxCellFactory(AbstractDSMData matrix) {
        return new Callback<>() {
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
        ComboBox<DSMItem> itemSelector,
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

        // Set up ComboBox to choose which row or column to modify connections of
        itemSelector.setCellFactory(getMatrixItemComboBoxCellFactory(matrix));

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

}
