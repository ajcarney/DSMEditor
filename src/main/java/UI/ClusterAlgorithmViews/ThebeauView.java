package UI.ClusterAlgorithmViews;

import Matrices.Data.SymmetricDSMData;
import UI.Widgets.NumericTextField;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import Matrices.ClusterAlgorithms.Thebeau;

/**
 * The Thebeau algorithm
 */
public class ThebeauView implements IAlgorithmView {

    private DoubleProperty optimalSizeCluster;
    private DoubleProperty powcc;
    private DoubleProperty powdep;
    private DoubleProperty powbid;
    private DoubleProperty randBid;
    private DoubleProperty randAccept;
    private DoubleProperty numLevels;
    private DoubleProperty randSeed;

    private CheckBox countByWeight;
    private CheckBox debug;

    /**
     * Creates a pane with widgets for all the necessary parameters
     *
     * @return the pane to display on the gui
     */
    @Override
    public VBox getParametersPane() {
        VBox parametersPane = new VBox();

        // optimal size layout
        VBox optimalSizeLayout = new VBox();

        Label optimalClusterSizeLabel = new Label("Optimal Cluster Size");

        optimalSizeCluster = new SimpleDoubleProperty(4.5);
        NumericTextField optimalSizeEntry = new NumericTextField(optimalSizeCluster.getValue());
        optimalSizeEntry.textProperty().addListener((obs, oldText, newText) -> {
            optimalSizeCluster.setValue(optimalSizeEntry.getNumericValue());
        });

        optimalSizeLayout.getChildren().addAll(optimalClusterSizeLabel, optimalSizeEntry);
        optimalSizeLayout.setSpacing(5);
        optimalSizeLayout.setPadding(new Insets(10));
        optimalSizeLayout.setAlignment(Pos.CENTER);


        // powcc layout
        VBox powccArea = new VBox();

        Label powccLabel = new Label("powcc constant");
        powccLabel.setTooltip(new Tooltip("Exponential to penalize size of clusters when calculating cluster cost"));

        powcc = new SimpleDoubleProperty(1.0);
        NumericTextField powccEntry = new NumericTextField(powcc.getValue());
        powccEntry.textProperty().addListener((obs, oldText, newText) -> {
            powcc.setValue(powccEntry.getNumericValue());
        });

        powccArea.getChildren().addAll(powccLabel, powccEntry);
        powccArea.setSpacing(5);
        powccArea.setPadding(new Insets(10));
        powccArea.setAlignment(Pos.CENTER);


        // powdep layout
        VBox powdepArea = new VBox();

        Label powdepLabel = new Label("powdep constant");
        powdepLabel.setTooltip(new Tooltip("Exponential to emphasize connections when calculating bids"));

        powdep = new SimpleDoubleProperty(4.0);
        NumericTextField powdepEntry = new NumericTextField(powdep.getValue());
        powdepEntry.textProperty().addListener((obs, oldText, newText) -> {
            powdep.setValue(powdepEntry.getNumericValue());
        });

        powdepArea.getChildren().addAll(powdepLabel, powdepEntry);
        powdepArea.setSpacing(5);
        powdepArea.setPadding(new Insets(10));
        powdepArea.setAlignment(Pos.CENTER);


        // powbid layout
        VBox powbidArea = new VBox();

        Label powbidLabel = new Label("powbid constant");
        powbidLabel.setTooltip(new Tooltip("Exponential to penalize size of clusters when calculating bids"));

        powbid = new SimpleDoubleProperty(1.0);
        NumericTextField powBidEntry = new NumericTextField(powbid.getValue());
        powBidEntry.textProperty().addListener((obs, oldText, newText) -> {
            powbid.setValue(powdepEntry.getNumericValue());
        });

        powbidArea.getChildren().addAll(powbidLabel, powBidEntry);
        powbidArea.setSpacing(5);
        powbidArea.setPadding(new Insets(10));
        powbidArea.setAlignment(Pos.CENTER);


        // randbid layout
        VBox randBidArea = new VBox();

        Label randBidLabel = new Label("rand_bid constant");
        randBidLabel.setTooltip(new Tooltip("Constant to determine how often to make slightly suboptimal change"));

        randBid = new SimpleDoubleProperty(122);
        NumericTextField randBidEntry = new NumericTextField(randBid.getValue());
        randBidEntry.textProperty().addListener((obs, oldText, newText) -> {
            randBid.setValue(randBidEntry.getNumericValue());
        });

        randBidArea.getChildren().addAll(randBidLabel, randBidEntry);
        randBidArea.setSpacing(5);
        randBidArea.setPadding(new Insets(10));
        randBidArea.setAlignment(Pos.CENTER);


        // randAccept layout
        VBox randAcceptArea = new VBox();

        Label randAcceptLabel = new Label("rand_accept constant");
        randAcceptLabel.setTooltip(new Tooltip("Constant to determine how often to make a suboptimal change"));

        randAccept = new SimpleDoubleProperty(122);
        NumericTextField randAcceptEntry = new NumericTextField(randAccept.getValue());
        randAcceptEntry.textProperty().addListener((obs, oldText, newText) -> {
            randAccept.setValue(randAcceptEntry.getNumericValue());
        });

        randAcceptArea.getChildren().addAll(randAcceptLabel, randAcceptEntry);
        randAcceptArea.setSpacing(5);
        randAcceptArea.setPadding(new Insets(10));
        randAcceptArea.setAlignment(Pos.CENTER);


        // count method layout
        VBox countMethodLayout = new VBox();
        countMethodLayout.setSpacing(10);

        countByWeight = new CheckBox("Count by Weight");
        countByWeight.setSelected(true);
        countByWeight.setMaxWidth(Double.MAX_VALUE);

        countMethodLayout.getChildren().addAll(countByWeight);
        countMethodLayout.setAlignment(Pos.CENTER);
        countMethodLayout.setPadding(new Insets(10));


        // levels layout
        VBox levelsArea = new VBox();

        Label levelsLabel = new Label("Number of Iterations");

        numLevels = new SimpleDoubleProperty(1000);
        NumericTextField levelsEntry = new NumericTextField(numLevels.getValue());
        levelsEntry.textProperty().addListener((obs, oldText, newText) -> {
            numLevels.setValue(levelsEntry.getNumericValue());
        });

        levelsArea.getChildren().addAll(levelsLabel, levelsEntry);
        levelsArea.setSpacing(5);
        levelsArea.setPadding(new Insets(10));
        levelsArea.setAlignment(Pos.CENTER);


        // randSeed layout
        VBox randSeedArea = new VBox();

        Label randSeedLabel = new Label("Random Seed");

        randSeed = new SimpleDoubleProperty(30);
        NumericTextField randSeedEntry = new NumericTextField(powbid.getValue());
        randSeedEntry.textProperty().addListener((obs, oldText, newText) -> {
            randSeed.setValue(randSeedEntry.getNumericValue());
        });

        randSeedArea.getChildren().addAll(randSeedLabel, randSeedEntry);
        randSeedArea.setSpacing(5);
        randSeedArea.setPadding(new Insets(10));
        randSeedArea.setAlignment(Pos.CENTER);


        // debug checkbox
        VBox debugLayout = new VBox();
        debugLayout.setSpacing(10);

        debug = new CheckBox("Debug to stdout");
        debug.setMaxWidth(Double.MAX_VALUE);

        debugLayout.getChildren().addAll(debug);
        debugLayout.setAlignment(Pos.CENTER);
        debugLayout.setPadding(new Insets(10));

        // config layout
        parametersPane.getChildren().addAll(optimalSizeLayout, powccArea, powdepArea, powbidArea, randBidArea, randAcceptArea, countMethodLayout, randSeedArea, levelsArea, debugLayout);
        parametersPane.setSpacing(15);
        parametersPane.setAlignment(Pos.TOP_CENTER);

        return parametersPane;
    }


    /**
     * Runs the Thebeau algorithm on the input matrix
     *
     * @param matrix - the symmetric matrix to run the algorithm on
     * @return the matrix with new groupings
     */
    @Override
    public SymmetricDSMData runSimulation(SymmetricDSMData matrix) {
        SymmetricDSMData outputMatrix = Thebeau.thebeauAlgorithm(
                matrix.createCopy(),  // use copy to not modify this matrix
                optimalSizeCluster.doubleValue(),
                powdep.doubleValue(),
                powbid.doubleValue(),
                powcc.doubleValue(),
                randBid.doubleValue(),
                randAccept.doubleValue(),
                countByWeight.isSelected(),
                numLevels.intValue(),
                randSeed.longValue(),
                debug.isSelected()
        );
        outputMatrix.reDistributeSortIndicesByGroup();
        return outputMatrix;
    }


}
