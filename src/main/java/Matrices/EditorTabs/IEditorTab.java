package Matrices.EditorTabs;

import Matrices.Data.AbstractDSMData;
import Matrices.Views.AbstractMatrixView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Interface to define what an editor tab is able to have. It is able to
 * have different panes where the child nodes can be set by the implementation
 * of this interface
 */
public interface IEditorTab {

    /**
     * @return  The node to be displayed as the center content
     */
    Pane getCenterPane();


    /**
     * @return  The node to be displayed as the left content
     */
    Pane getLeftPane();


    /**
     * @return  The node to be displayed as the right content
     */
    Pane getRightPane();


    /**
     * @return  The node to be displayed as the bottom content
     */
    Pane getBottomPane();


    /**
     * @return  The matrix view currently selected in case it needs to be used directly somewhere
     */
    AbstractMatrixView getMatrixView();


    /**
     * @return  all the matrix views currently in the editor tab
     */
    Collection<AbstractMatrixView> getAllMatrixViews();


    /**
     * @return  the matrix data of the open matrix
     */
    AbstractDSMData getMatrixData();

}
