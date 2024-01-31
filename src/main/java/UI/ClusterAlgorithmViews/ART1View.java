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
