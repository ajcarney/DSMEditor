package Matrices.EditorTabs;

import Matrices.Data.AsymmetricDSMData;
import Matrices.SideBarTools.AsymmetricSideBar;
import Matrices.Views.AsymmetricView;
import UI.MatrixMetaDataPane;
import javafx.scene.layout.Pane;

public class AsymmetricEditorTab implements IEditorTab {

    AsymmetricDSMData matrixData;
    AsymmetricView matrixView;
    AsymmetricSideBar sideBar;
    MatrixMetaDataPane metadata;


    public AsymmetricEditorTab(AsymmetricDSMData matrixData) {
        this.matrixData = matrixData;
        this.matrixView = new AsymmetricView(this.matrixData, 12.0);
        this.sideBar = new AsymmetricSideBar(this.matrixData, this.matrixView);
        this.metadata = new MatrixMetaDataPane();
        metadata.setMatrix(matrixData);
    }


    @Override
    public Pane getCenterPane() {
        return matrixView.getView();
    }

    @Override
    public Pane getLeftPane() {
        return sideBar.getLayout();
    }

    @Override
    public Pane getRightPane() {
        return metadata.getLayout();
    }

    @Override
    public Pane getBottomPane() {
        return new Pane();
    }

    @Override
    public AsymmetricView getMatrixView() {
        return matrixView;
    }
}
