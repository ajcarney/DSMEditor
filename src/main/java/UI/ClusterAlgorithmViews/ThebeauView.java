package UI.ClusterAlgorithmViews;

import Matrices.ClusterAlgorithms.Thebeau;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.SymmetricDSMData;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

/**
 * The Thebeau algorithm view. Generates the pane that contains all the entry areas
 * needed to run the algorithm
 */
public class ThebeauView implements IAlgorithmView {

    private final DoubleProperty optimalSizeCluster = new SimpleDoubleProperty(4.5);
    private final DoubleProperty powcc = new SimpleDoubleProperty(1.0);
    private final DoubleProperty powdep = new SimpleDoubleProperty(4.0);
    private final DoubleProperty powbid = new SimpleDoubleProperty(1.0);
    private final DoubleProperty randBid = new SimpleDoubleProperty(122);
    private final DoubleProperty randAccept = new SimpleDoubleProperty(122);
    private final BooleanProperty countByWeight = new SimpleBooleanProperty(false);
    private final DoubleProperty numLevels = new SimpleDoubleProperty(1000);
    private final DoubleProperty randSeed = new SimpleDoubleProperty(30);
    private final BooleanProperty debug = new SimpleBooleanProperty(false);
    private final ObservableList<Integer> exclusions = FXCollections.observableArrayList();


    /**
     * Creates a pane with widgets for all the necessary parameters
     *
     * @return the pane to display on the gui
     */
    @Override
    public VBox getParametersPane(SymmetricDSMData matrix) {
        return new ParameterBuilder()
                .newNumericEntry(optimalSizeCluster, "Optimal Cluster Size", "", false)
                .newNumericEntry(powcc, "powcc constant", "Exponential to penalize size of clusters when calculating cluster cost", false)
                .newNumericEntry(powdep, "powdep constant", "Exponential to emphasize connections when calculating bids", false)
                .newNumericEntry(powbid, "powbid constant", "Exponential to penalize size of clusters when calculating bids", false)
                .newNumericEntry(randBid, "rand_bid constant", "Constant to determine how often to make slightly suboptimal change", true)
                .newNumericEntry(randAccept, "rand_accept constant", "Constant to determine how often to make a suboptimal change", true)
                .newCheckbox(countByWeight, "Count by Weight")
                .newNumericEntry(numLevels, "Number of Iterations", "", true)
                .newDSMItemSelect("", "", matrix, exclusions)
                .newNumericEntry(randSeed, "Random Seed", "", false)
                .newCheckbox(debug, "Debug to stdout")
                .build();
    }


    /**
     * Runs the Thebeau algorithm on the input matrix
     *
     * @param matrix - the symmetric matrix to run the algorithm on
     * @return the matrix with new groupings
     */
    @Override
    public SymmetricDSMData runSimulation(SymmetricDSMData matrix) {
        for (Integer i : exclusions) {
            System.out.println(matrix.getItem(i).getName());
        }

        SymmetricDSMData outputMatrix = Thebeau.thebeauAlgorithm(
                matrix.createCopy(),  // use copy to not modify this matrix
                optimalSizeCluster.doubleValue(),
                powdep.doubleValue(),
                powbid.doubleValue(),
                powcc.doubleValue(),
                randBid.intValue(),
                randAccept.intValue(),
                new ArrayList<>(exclusions),
                countByWeight.getValue(),
                numLevels.intValue(),
                randSeed.longValue(),
                debug.getValue()
        );
        outputMatrix.reDistributeSortIndicesByGroup();
        return outputMatrix;
    }

}
