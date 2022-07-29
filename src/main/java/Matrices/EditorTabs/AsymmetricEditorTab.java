package Matrices.EditorTabs;

import Matrices.Data.AsymmetricDSMData;
import Matrices.SideBarTools.AsymmetricSideBar;
import Matrices.Views.AbstractMatrixView;
import Matrices.Views.AsymmetricView;
import UI.MatrixMetaDataPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;


/**
 * The editor tab for Asymmetric matrices. Only shows the matrix view as the main content
 */
public class AsymmetricEditorTab implements IEditorTab {

    AsymmetricDSMData matrixData;
    AsymmetricView matrixView;
    AsymmetricSideBar sideBar;
    MatrixMetaDataPane metadata;


    /**
     * Generic constructor. Takes the data for an Asymmetric DSM
     *
     * @param matrixData  the data for the Asymmetric matrix
     */
    public AsymmetricEditorTab(AsymmetricDSMData matrixData) {
        this.matrixData = matrixData;
        this.matrixView = new AsymmetricView(this.matrixData, 12.0);
        this.sideBar = new AsymmetricSideBar(this.matrixData, this.matrixView);
        this.metadata = new MatrixMetaDataPane(this.matrixData);
    }


    /**
     * @return  The node to be displayed as the center content
     */
    @Override
    public Pane getCenterPane() {
        return matrixView.getView();
    }


    /**
     * @return  The node to be displayed as the left content
     */
    @Override
    public Pane getLeftPane() {
        return sideBar.getLayout();
    }


    /**
     * @return  The node to be displayed as the right content
     */
    @Override
    public Pane getRightPane() {
        return metadata.getLayout();
    }


    /**
     * @return  The node to be displayed as the bottom content
     */
    @Override
    public Pane getBottomPane() {
        return new Pane();
    }


    /**
     * @return  The matrix view used by the tab
     */
    @Override
    public AsymmetricView getMatrixView() {
        return matrixView;
    }


    /**
     * @return  all the matrix views currently in the editor tab
     */
    @Override
    public ArrayList<AbstractMatrixView> getAllMatrixViews() {
        ArrayList<AbstractMatrixView> views = new ArrayList<>();
        views.add(matrixView);
        return views;
    }


    /**
     * @return  the matrix data of the open matrix
     */
    @Override
    public AsymmetricDSMData getMatrixData() {
        return matrixData;
    }
}
