package UI;

import Matrices.Data.AbstractDSMData;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Flags.IPropagationAnalysis;
import UI.Widgets.NumericTextField;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Class for performing propagation analysis on a given matrix. Only works on DSMs that implement the IPropagationAnalysis
 * interface
 *
 * @author Aiden Carney
 */
public class PropagationAnalysisWindow<T extends AbstractDSMData & IPropagationAnalysis> {
    T matrix;

    Stage window;
    private final BorderPane rootLayout;

    private VBox configLayout;      // side bar

    // config pane widgets
    private ComboBox<Integer> startItemEntry;

    private IntegerProperty numLevels;
    private DoubleProperty minWeight;

    private RadioButton countByOccurrence;

    // main content widgets
    private final VBox graphLayout;
    private final VBox rawOutputLayout;

    ListView<Integer> itemExclusions;


    /**
     * Creates the gui object but does not open it. Initializes all widgets
     *
     * @param matrix the matrix to analyze
     */
    public PropagationAnalysisWindow(T matrix) {
        this.matrix = matrix;

        window = new Stage();
//        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        if(!matrix.getTitle().isEmpty()) {
            window.setTitle(matrix.getTitle() + " - Propagation Analysis");
        } else {
            window.setTitle("Propagation Analysis");
        }

    // side bar
        updateConfigWidgets();

    // menu
        // layouts in the border pane
        // top bar
        MenuBar menuBar = new MenuBar();

        // run menu
        Menu runMenu = new Menu("Run");
        MenuItem run = new MenuItem("Run Propagation Analysis");
        run.setOnAction(e -> runPropagationAnalysis());
        runMenu.getItems().addAll(run);

        menuBar.getMenus().addAll(runMenu);

    // main content
        // center
        SplitPane mainContent = new SplitPane();
        mainContent.setOrientation(Orientation.VERTICAL);

        graphLayout = new VBox();
        ScrollPane graphScrollPane = new ScrollPane(graphLayout);
        graphScrollPane.setFitToWidth(true);
        graphScrollPane.setFitToHeight(true);

        rawOutputLayout = new VBox();
        ScrollPane rawOutputScrollPane = new ScrollPane(rawOutputLayout);
        rawOutputScrollPane.setFitToWidth(true);
        rawOutputScrollPane.setFitToHeight(true);

        mainContent.getItems().addAll(graphScrollPane, rawOutputScrollPane);

    // set up main layout
        rootLayout = new BorderPane();
        rootLayout.setLeft(configLayout);
        rootLayout.setTop(menuBar);
        rootLayout.setCenter(mainContent);

    }


    /**
     * Updates the widgets on the side panel for parameter setting. Called from constructor.
     */
    private void updateConfigWidgets() {
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

    // start item and label
        VBox startItemLayout = new VBox();
        Label startItemLabel = new Label("Start Item");
        startItemEntry = new ComboBox<>();

        startItemEntry.setButtonCell(cellFactory.call(null));
        startItemEntry.setCellFactory(cellFactory);
        ArrayList<Integer> items = new ArrayList<>();
        for(DSMItem row : matrix.getRows()) {
            items.add(row.getUid());
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
        minWeightEntry.textProperty().addListener((obs, oldText, newText) -> minWeight.setValue(minWeightEntry.getNumericValue()));

        minWeightLayout.getChildren().addAll(minWeightLabel, minWeightEntry);
        minWeightLayout.setSpacing(5);
        minWeightLayout.setPadding(new Insets(10));
        minWeightLayout.setAlignment(Pos.CENTER);


    // count method layout
        VBox countMethodLayout = new VBox();
        countMethodLayout.setSpacing(10);

        Label countMethodLabel = new Label("Count Method");

        ToggleGroup tg = new ToggleGroup();
        RadioButton countByWeight = new RadioButton("Count by Weight");
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
        deleteSelected.setOnAction(ee -> itemExclusions.getItems().removeAll(itemExclusions.getSelectionModel().getSelectedItems()));

        HBox exceptionSelectorLayout = new HBox();
        ComboBox<Integer> itemExceptionSelector = new ComboBox<>();
        itemExceptionSelector.setButtonCell(cellFactory.call(null));
        itemExceptionSelector.setCellFactory(cellFactory);

        ArrayList<Integer> exceptions = new ArrayList<>();
        for(DSMItem row : matrix.getRows()) {
            exceptions.add(row.getUid());
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


    /**
     * Runs the propagation analysis algorithm and displays the output in graphical and table format on the main
     * gui screen
     */
    private void runPropagationAnalysis() {
        // set up parameters
        Integer startItem = startItemEntry.getValue();
        Integer numberLevels = numLevels.getValue();

        Double minimumWeight = minWeight.getValue();
        if(minimumWeight == null) {
            minimumWeight = -Double.MAX_VALUE;
        }

        ArrayList<Integer> exclusions = new ArrayList<>(itemExclusions.getItems());

        boolean byWeight = !countByOccurrence.isSelected();

        HashMap<Integer, HashMap<Integer, Double>> results = matrix.propagationAnalysis(startItem, numberLevels, exclusions, minimumWeight, byWeight);

        // combine results by level into one map
        HashMap<Integer, Double> scores = new HashMap<>();
        for(Map.Entry<Integer, HashMap<Integer, Double>> levelEntry : results.entrySet()) {
            for(Map.Entry<Integer, Double> entry : levelEntry.getValue().entrySet()) {
                scores.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }

        // update graph layout
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> graph = new BarChart<>(xAxis,yAxis);
        graph.setTitle("Count Summary");
        xAxis.setLabel("Item Name");
        yAxis.setLabel("Value");

        XYChart.Series series1 = new XYChart.Series();
        if(byWeight) {
            series1.setName("Value by Weight");
        } else {
            series1.setName("Value by Occurrence");
        }

        for(Map.Entry<Integer, Double> entry : scores.entrySet()) {
            series1.getData().add(new XYChart.Data(matrix.getItem(entry.getKey()).getName().getValue(), entry.getValue()));
        }

        graph.getData().addAll(series1);
        graphLayout.getChildren().removeAll(graphLayout.getChildren());
        graphLayout.getChildren().add(graph);


        // update raw data layout
        ObservableList<Pair<String, Double>> tableItems = FXCollections.observableArrayList();
        for(Map.Entry<Integer, Double> entry : scores.entrySet()) {
            tableItems.add(new Pair(matrix.getItem(entry.getKey()).getName().getValue(), entry.getValue()));
        }

        TableView<Pair<String, Double>> table = new TableView<>();

        TableColumn<Pair<String, Double>, String> nameColumn = new TableColumn<>("Item Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<Pair<String, Double>, String> scoreColumn = new TableColumn<>("Value");
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

        table.setItems(tableItems);
        table.getColumns().addAll(nameColumn, scoreColumn);

        Button copyButton = new Button("Copy Table");
        copyButton.setOnAction(e -> {
            StringBuilder copyString = new StringBuilder();
            for (Object row : table.getItems()) {
                for (TableColumn column : table.getColumns()) {
                    if(column.getCellObservableValue(row).getValue().getClass().equals(Integer.class)) {
                        copyString.append(matrix.getItem((Integer) column.getCellObservableValue(row).getValue()).getName().getValue()).append(",");
                    } else {
                        copyString.append(column.getCellObservableValue(row).getValue()).append(",");
                    }
                }
                copyString.append("\n");
            }

            final ClipboardContent content = new ClipboardContent();
            content.putString(copyString.toString());
            Clipboard.getSystemClipboard().setContent(content);
        });

        rawOutputLayout.getChildren().removeAll(rawOutputLayout.getChildren());
        rawOutputLayout.getChildren().addAll(table, copyButton);
        rawOutputLayout.setAlignment(Pos.CENTER);
        rawOutputLayout.setPadding(new Insets(10));
        rawOutputLayout.setSpacing(5);

    }


    /**
     * Opens and starts the gui so users can interact with it.
     */
    public void start() {
        Scene scene = new Scene(rootLayout, 1200, 800);
        window.setScene(scene);
        window.show();
    }

}
