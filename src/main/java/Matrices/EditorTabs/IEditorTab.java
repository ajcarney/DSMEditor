package Matrices.EditorTabs;

import Matrices.Views.IMatrixView;
import javafx.scene.layout.Pane;

public interface IEditorTab {


    Pane getCenterPane();



    Pane getLeftPane();



    Pane getRightPane();



    Pane getBottomPane();



    IMatrixView getMatrixView();

}
