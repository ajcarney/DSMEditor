package UI.Widgets;

import Matrices.Data.AbstractDSMData;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.SearchableComboBox;

import java.util.ArrayList;
import java.util.Comparator;


public class DSMItemSelector {

    AbstractDSMData matrix;
    ListView<Integer> items;


    /**
     * Default constructor
     * @param matrix the matrix to select items from. Needed to map uids to names
     */
    public DSMItemSelector(AbstractDSMData matrix) {
        this.matrix = matrix;
    }


    /**
     * Returns and item selector
     * @param title the title label to display with the widget
     * @param onlyRows if only rows should be able to be selected
     * @param selectedItems the items selected in the listview, will contain same values as listview, should be empty
     * @return the layout as a VBox
     */
    public VBox getItemSelector(String title, boolean onlyRows, ObservableList<Integer> selectedItems) {
        // function to set text of comboBox items, used for all ComboBoxes
        Callback<ListView<Integer>, ListCell<Integer>> cellFactory = new Callback<>() {
            public ListCell<Integer> call(ListView<Integer> l) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(matrix.getItem(item).getName().getValue());
                        }
                    }
                };
            }
        };


        Label titleLabel = new Label(title);

        // set up the list view
        items = new ListView<>();
        items.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        items.setCellFactory(cellFactory);
        items.setItems(selectedItems);
        items.setMinHeight(70);




        // set up delete button
        Button deleteSelected = new Button("Delete Selected Item(s)");
        deleteSelected.setOnAction(
                ee -> items.getItems().removeAll(items.getSelectionModel().getSelectedItems()));


        // make a list of all the items that can be selected
        ArrayList<Integer> canSelect = new ArrayList<>();
        for (DSMItem row : matrix.getRows()) {
            canSelect.add(row.getUid());
        }
        if (!onlyRows) {  // add columns if desired
            for (DSMItem col : matrix.getCols()) {
                canSelect.add(col.getUid());
            }
        }

        // make the combobox selector
        SearchableComboBox<Integer> itemSelector = new SearchableComboBox<>();
        itemSelector.setButtonCell(cellFactory.call(null));
        itemSelector.setCellFactory(cellFactory);

        itemSelector.getItems().addAll(canSelect);
        itemSelector.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(itemSelector, Priority.ALWAYS);
        itemSelector.setPromptText("Item");

        Button addException = new Button("Add Item");
        addException.setOnAction(e -> {
            if (itemSelector.getValue() == null || items.getItems()
                    .contains(itemSelector.getValue()))
                return;
            items.getItems().add(itemSelector.getValue());
            items.getItems().sort((arg0, arg1) ->    // sort the list
                    matrix.getItem(arg0).getName().toString().compareToIgnoreCase(matrix.getItem(arg1).getName().toString())
            );
        });

        HBox itemSelectorLayout = new HBox();
        itemSelectorLayout.getChildren().addAll(itemSelector, addException);

        VBox layout = new VBox();
        layout.getChildren().addAll(titleLabel, items, deleteSelected, itemSelectorLayout);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(5);


        return layout;
    }

}
