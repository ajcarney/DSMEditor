package UI.ClusterAlgorithmViews;

import Matrices.Data.SymmetricDSMData;
import javafx.scene.layout.VBox;

public class ART1View implements IAlgorithmView {


    @Override
    public VBox getParametersPane() {
        VBox parametersPane = new VBox();

        return parametersPane;
    }

    @Override
    public SymmetricDSMData runSimulation(SymmetricDSMData matrix) {
        return null;
    }
}
