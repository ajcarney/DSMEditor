package Matrices.EditorTabs;

import Matrices.Data.AsymmetricDSMData;
import Matrices.Data.SymmetricDSMData;
import Matrices.SideBarTools.SymmetricSideBar;
import Matrices.Views.AbstractMatrixView;
import Matrices.Views.SymmetricView;
import UI.MatrixMetaDataPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;


/**
 * The editor tab for symmetric matrices. Only shows the matrix view as the main content
 */
public class SymmetricEditorTab implements IEditorTab {

    SymmetricDSMData matrixData;
    SymmetricView matrixView;
    SymmetricSideBar sideBar;
    MatrixMetaDataPane metadata;


    /**
     * Generic constructor. Takes the data for a symmetric DSM
     *
     * @param matrixData  the data for the symmetric matrix
     */
    public SymmetricEditorTab(SymmetricDSMData matrixData) {
        this.matrixData = matrixData;
        this.matrixView = new SymmetricView(this.matrixData, 12.0);
        this.sideBar = new SymmetricSideBar(this.matrixData, this.matrixView);
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
    public SymmetricView getMatrixView() {
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
    public SymmetricDSMData getMatrixData() {
        return matrixData;
    }
}
