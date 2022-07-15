package Matrices;


import Matrices.Data.AbstractDSMData;
import Matrices.IOHandlers.AbstractIOHandler;
import Matrices.SideBarTools.AbstractSideBar;
import Matrices.Views.IMatrixView;


/**
 * Interface that defines how a DSM is to behave. A DSM has five components:
 *     Data
 *     View
 *     IOHandler
 *     SideBar
 *     HeaderMenu
 * This interface provides the outline for how the UI can interact with each of
 * these components of the matrix
 */
public interface IDSM {

    /**
     * Gets the DSM Data object for the matrix
     *
     * @param <T>  the type of matrix data
     * @return     the DSM Data object for the matrix
     */
    <T extends AbstractDSMData> T getMatrixData();


    /**
     * Gets the DSM View object for the matrix
     *
     * @param <T>  the type of matrix view
     * @return     the DSM View object for the matrix
     */
    <T extends IMatrixView> T getMatrixView();


    /**
     * Gets the DSM IOHandler object for the matrix
     *
     * @param <T>  the type of matrix IOHandler
     * @return     the DSM IOHandler object for the matrix
     */
    <T extends AbstractIOHandler> T getMatrixIOHandler();


    /**
     * Gets the DSM SideBar object for the matrix
     *
     * @param <T>  the type of matrix SideBar
     * @return     the DSM SideBar object for the matrix
     */
    <T extends AbstractSideBar> T getMatrixSideBar();


    /**
     * @return  true if the latest changes (if any) in the matrix have been saved
     */
    boolean isMatrixSaved();

}
