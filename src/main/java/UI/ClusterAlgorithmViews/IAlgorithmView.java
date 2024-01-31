package UI.ClusterAlgorithmViews;

import Matrices.Data.SymmetricDSMData;
import javafx.scene.layout.VBox;

/**
 * Interface to define how a new cluster algorithm should be integrated into the system
 */
public interface IAlgorithmView {
    /**
     * Creates a pane where users can enter parameters for the algorithm. Any classes that implement this
     * are in charge of handling their own data
     *
     * @return VBox - a pane containing the widgets
     */
    VBox getParametersPane();


    /**
     * Runs the simulation using a given algorithm
     *
     * @param matrix - the symmetric matrix to run the algorithm on
     * @return the matrix with new groupings
     */
    SymmetricDSMData runSimulation(SymmetricDSMData matrix);
}
