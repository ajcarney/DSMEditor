package Matrices.EditorTabs;

import Matrices.Data.SymmetricDSMData;
import Matrices.SideBarTools.SymmetricSideBar;
import Matrices.Views.SymmetricView;
import UI.MatrixMetaDataPane;
import javafx.scene.layout.Pane;

public class SymmetricEditorTab implements IEditorTab {

    SymmetricDSMData matrixData;
    SymmetricView matrixView;
    SymmetricSideBar sideBar;
    MatrixMetaDataPane metadata;


    public SymmetricEditorTab(SymmetricDSMData matrixData) {
        this.matrixData = matrixData;
        this.matrixView = new SymmetricView(this.matrixData, 12.0);
        this.sideBar = new SymmetricSideBar(this.matrixData, this.matrixView);
        this.metadata = new MatrixMetaDataPane(this.matrixData);
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
    public SymmetricView getMatrixView() {
        return matrixView;
    }
}
