package Matrices;

import Matrices.Data.AbstractDSMData;
import Matrices.EditorTabs.IEditorTab;
import Matrices.IOHandlers.AbstractIOHandler;
import Matrices.Views.AbstractMatrixView;


/**
 * Interface that defines how a DSM is to behave. A DSM has four components:
 *     Data
 *     View
 *     IOHandler
 *     SideBar
 * This interface provides the outline for how the UI can interact with each of
 * these components of the matrix
 */
public interface IDSM {

    /**
     * Gets the DSM Data object for the matrix
     *
     * @return     the DSM Data object for the matrix
     */
    AbstractDSMData getMatrixData();


    /**
     * @return  the DSM editor tab object for the matrix
     */
    IEditorTab getMatrixEditorTab();


    /**
     * Gets the DSM View object for the matrix
     *
     * @return     the DSM View object for the matrix
     */
    AbstractMatrixView getMatrixView();


    /**
     * Gets the DSM IOHandler object for the matrix
     *
     * @return     the DSM IOHandler object for the matrix
     */
    AbstractIOHandler getMatrixIOHandler();


    /**
     * @return  true if the latest changes (if any) in the matrix have been saved
     */
    boolean isMatrixSaved();

}
