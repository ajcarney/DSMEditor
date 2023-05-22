package UI.ClusterAlgorithmViews;

import Matrices.Data.SymmetricDSMData;
import javafx.scene.layout.VBox;

public interface IAlgorithmView {
    VBox getParametersPane();
    SymmetricDSMData runSimulation(SymmetricDSMData matrix);
}
