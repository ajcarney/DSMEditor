package gui;

import DSMData.DSMItem;
import DSMData.DataHandler;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;

public class PropagationAnalysis {
    DataHandler matrix;

    Stage window;
    private BorderPane rootLayout;

    // layouts in the border pane
    private VBox configLayout;  // side bar

    // config pane widgets
    private ComboBox startItemEntry;

    private IntegerProperty numLevels;
    private DoubleProperty minWeight;

    private ToggleGroup tg;
    private RadioButton countByWeight;
    private RadioButton countByOccurrence;

    ListView<Integer> itemExclusions;



    public PropagationAnalysis(DataHandler matrix) {
        this.matrix = matrix;

        window = new Stage();
//        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle(matrix.getTitle() + " - Propagation Analysis");

        updateConfigWidgets();

        rootLayout = new BorderPane();
        rootLayout.setLeft(configLayout);

        Scene scene = new Scene(rootLayout, 800, 800);
        window.setScene(scene);
        window.show();
    }


    private void updateConfigWidgets() {
        // function to set text of comboBox items, used for all ComboBoxes
        Callback<ListView<Integer>, ListCell<Integer>> cellFactory = new Callback<ListView<Integer>, ListCell<Integer>>() {
            public ListCell<Integer> call(ListView<Integer> l) {
                return new ListCell<Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(matrix.getItem(item).getName());
                        }
                    }
                };
            }
        };

    // start item and label
        VBox startItemLayout = new VBox();
        Label startItemLabel = new Label("Start Item");
        startItemEntry = new ComboBox<>();

        startItemEntry.setButtonCell(cellFactory.call(null));
        startItemEntry.setCellFactory(cellFactory);
        ArrayList<Integer> items = new ArrayList<>();
        if(matrix.isSymmetrical()) {
            for(DSMItem row : matrix.getRows()) {
                items.add(row.getUid());
            }
        } else {
            for(DSMItem row : matrix.getRows()) {
                items.add(row.getUid());
            }
            for(DSMItem col : matrix.getCols()) {
                items.add(col.getUid());
            }
        }
        startItemEntry.getItems().addAll(items);
        startItemEntry.setMaxWidth(Double.MAX_VALUE);
        startItemEntry.getSelectionModel().selectFirst();
        HBox.setHgrow(startItemEntry, Priority.ALWAYS);
        startItemLayout.getChildren().addAll(startItemLabel, startItemEntry);
        startItemLayout.setSpacing(5);
        startItemLayout.setPadding(new Insets(10));
        startItemLayout.setAlignment(Pos.CENTER);


    // num levels layout
        VBox numLevelsLayout = new VBox();

        Label numLevelsDescriptorLabel = new Label("Number of Levels to Run");

        HBox numLevelsModifierLayout = new HBox();
        numLevels = new SimpleIntegerProperty(1);

        Button decrease = new Button("-");
        decrease.setOnAction(e -> {
            if(numLevels.getValue() > 1) {
                numLevels.setValue(numLevels.getValue() - 1);
            }
        });
        Button increase = new Button("+");
        increase.setOnAction(e -> {
            if(numLevels.getValue() < 10) {
                numLevels.setValue(numLevels.getValue() + 1);
            }
        });

        Label numLevelsLabel = new Label("1");
        numLevelsLabel.textProperty().bind(numLevels.asString());
        HBox.setHgrow(numLevelsLabel, Priority.ALWAYS);

        numLevelsModifierLayout.getChildren().addAll(decrease, numLevelsLabel, increase);
        numLevelsModifierLayout.setSpacing(5);
        numLevelsModifierLayout.setAlignment(Pos.CENTER);

        numLevelsLayout.getChildren().addAll(numLevelsDescriptorLabel, numLevelsModifierLayout);
        numLevelsLayout.setSpacing(5);
        numLevelsLayout.setPadding(new Insets(10));
        numLevelsLayout.setAlignment(Pos.CENTER);


    // min weight layout
        VBox minWeightLayout = new VBox();

        Label minWeightLabel = new Label("Minimum Weight");

        minWeight = new SimpleDoubleProperty(1.0);
        NumericTextField minWeightEntry = new NumericTextField(minWeight.getValue());
        minWeightEntry.textProperty().addListener((obs, oldText, newText) -> {
            minWeight.setValue(minWeightEntry.getNumericValue());
        });

        minWeightLayout.getChildren().addAll(minWeightLabel, minWeightEntry);
        minWeightLayout.setSpacing(5);
        minWeightLayout.setPadding(new Insets(10));
        minWeightLayout.setAlignment(Pos.CENTER);


    // count method layout
        VBox countMethodLayout = new VBox();
        countMethodLayout.setSpacing(10);

        Label countMethodLabel = new Label("Count Method");

        tg = new ToggleGroup();
        countByWeight = new RadioButton("Count by Weight");
        countByWeight.setToggleGroup(tg);
        countByWeight.setSelected(false);
        countByWeight.setMaxWidth(Double.MAX_VALUE);

        countByOccurrence = new RadioButton("Count By Occurrence");
        countByOccurrence.setToggleGroup(tg);
        countByOccurrence.setSelected(true);
        countByOccurrence.setMaxWidth(Double.MAX_VALUE);

        countMethodLayout.getChildren().addAll(countMethodLabel, countByOccurrence, countByWeight);
        countMethodLayout.setAlignment(Pos.CENTER);
        countMethodLayout.setPadding(new Insets(10));

    // exclusions layout
        VBox exclusionsLayout = new VBox();

        Label exclusionsLabel = new Label("Excluded Items");
        itemExclusions = new ListView<>();
        itemExclusions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        itemExclusions.setCellFactory(cellFactory);
        Button deleteSelected = new Button("Delete Selected Item(s)");
        deleteSelected.setOnAction(ee -> {
            itemExclusions.getItems().removeAll(itemExclusions.getSelectionModel().getSelectedItems());
        });

        HBox exceptionSelectorLayout = new HBox();
        ComboBox<Integer> itemExceptionSelector = new ComboBox<>();
        itemExceptionSelector.setButtonCell(cellFactory.call(null));
        itemExceptionSelector.setCellFactory(cellFactory);

        ArrayList<Integer> exceptions = new ArrayList<>();
        if(matrix.isSymmetrical()) {
            for(DSMItem row : matrix.getRows()) {
                exceptions.add(row.getUid());
            }
        } else {
            for(DSMItem row : matrix.getRows()) {
                exceptions.add(row.getUid());
            }
            for(DSMItem col : matrix.getCols()) {
                exceptions.add(col.getUid());
            }
        }
        itemExceptionSelector.getItems().addAll(exceptions);
        itemExceptionSelector.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(itemExceptionSelector, Priority.ALWAYS);
        itemExceptionSelector.setPromptText("Exclusion");

        Button addException = new Button("Add Exclusion");
        addException.setOnAction(e -> {
            if(itemExceptionSelector.getValue() == null || itemExclusions.getItems().contains(itemExceptionSelector.getValue())) return;
            itemExclusions.getItems().add(itemExceptionSelector.getValue());
        });

        exceptionSelectorLayout.getChildren().addAll(itemExceptionSelector, addException);

        exclusionsLayout.getChildren().addAll(exclusionsLabel, itemExclusions, deleteSelected, exceptionSelectorLayout);
        exclusionsLayout.setPadding(new Insets(10));
        exclusionsLayout.setAlignment(Pos.CENTER);
        exclusionsLayout.setSpacing(5);

    // add to config layout
        configLayout = new VBox();
        configLayout.getChildren().addAll(startItemLayout, numLevelsLayout, minWeightLayout, countMethodLayout, exclusionsLayout);
        configLayout.setSpacing(15);
        configLayout.setAlignment(Pos.CENTER);
    }
}
