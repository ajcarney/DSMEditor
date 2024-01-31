package UI.ClusterAlgorithmViews;

import Matrices.ClusterAlgorithms.ART1;
import Matrices.Data.SymmetricDSMData;
import UI.Widgets.NumericTextField;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public class ART1View implements IAlgorithmView {

    private final DoubleProperty maxGroups = new SimpleDoubleProperty(50);;
    private final DoubleProperty vigilance = new SimpleDoubleProperty(0.1);;
    private final DoubleProperty beta = new SimpleDoubleProperty(4.0);;

    private final BooleanProperty debug = new SimpleBooleanProperty(false);


    @Override
    public VBox getParametersPane() {
//        VBox parametersPane = new VBox();
//
//
//        // optimal size layout
//        VBox maxGroupsLayout = new VBox();
//
//        Label maxGroupsLabel = new Label("Max Clusters");
//
//        NumericTextField maxGroupsEntry = new NumericTextField(Double.valueOf(maxGroups.getValue()));
//        // maxGroupsEntry.setIntegerMode(true);
//        maxGroupsEntry.textProperty().addListener((obs, oldText, newText) -> {
//            maxGroups.setValue(maxGroupsEntry.getNumericValue());
//        });
//
//        maxGroupsLayout.getChildren().addAll(maxGroupsLabel, maxGroupsEntry);
//        maxGroupsLayout.setSpacing(5);
//        maxGroupsLayout.setPadding(new Insets(10));
//        maxGroupsLayout.setAlignment(Pos.CENTER);
//
//
//        // vigilance layout
//        VBox vigilanceArea = new VBox();
//
//        Label vigilanceLabel = new Label("vigilance constant");
//        vigilanceLabel.setTooltip(new Tooltip("Value between 0 and 1 to determine how closely features must match prototypes"));
//
//        NumericTextField vigilanceEntry = new NumericTextField(vigilance.getValue());
//        vigilanceEntry.textProperty().addListener((obs, oldText, newText) -> {
//            vigilance.setValue(vigilanceEntry.getNumericValue());
//        });
//
//        vigilanceArea.getChildren().addAll(vigilanceLabel, vigilanceEntry);
//        vigilanceArea.setSpacing(5);
//        vigilanceArea.setPadding(new Insets(10));
//        vigilanceArea.setAlignment(Pos.CENTER);
//
//
//        // beta layout
//        VBox betaArea = new VBox();
//
//        Label betaLabel = new Label("beta constant");
//        betaLabel.setTooltip(new Tooltip("Exponential to emphasize connections when calculating bids"));
//
//        NumericTextField betaEntry = new NumericTextField(beta.getValue());
//        betaEntry.textProperty().addListener((obs, oldText, newText) -> {
//            beta.setValue(betaEntry.getNumericValue());
//        });
//
//        betaArea.getChildren().addAll(betaLabel, betaEntry);
//        betaArea.setSpacing(5);
//        betaArea.setPadding(new Insets(10));
//        betaArea.setAlignment(Pos.CENTER);
//
//
//        // debug checkbox
//        VBox debugLayout = new VBox();
//        debugLayout.setSpacing(10);
//
//        debug = new CheckBox("Debug to stdout");
//        debug.setMaxWidth(Double.MAX_VALUE);
//
//        debugLayout.getChildren().addAll(debug);
//        debugLayout.setAlignment(Pos.CENTER);
//        debugLayout.setPadding(new Insets(10));
//
//
//        // config layout
//        parametersPane.getChildren().addAll(maxGroupsLayout, vigilanceArea, betaArea, debugLayout);
//        parametersPane.setSpacing(15);
//        parametersPane.setAlignment(Pos.TOP_CENTER);

        return new ParameterBuilder()
                .newNumericEntry(maxGroups, "Max Clusters", "", true)
                .newNumericEntry(vigilance, "vigilance constant", "Value between 0 and 1 to determine how closely features must match prototypes", false)
                .newNumericEntry(beta, "beta constant", "Exponential to emphasize connections when calculating bids", false)
                .newCheckbox(debug, "Debug to stdout")
                .build();
    }

    @Override
    public SymmetricDSMData runSimulation(SymmetricDSMData matrix) {
        ART1 algo = new ART1(matrix);
        SymmetricDSMData outputMatrix = algo.art1Algorithm(maxGroups.intValue(), vigilance.doubleValue(), beta.doubleValue());

        outputMatrix.reDistributeSortIndicesByGroup();
        return outputMatrix;
    }
}
