package Matrices;

import Matrices.Data.SymmetricDSMData;
import Matrices.EditorTabs.SymmetricEditorTab;
import Matrices.IOHandlers.SymmetricIOHandler;
import Matrices.SideBarTools.SymmetricSideBar;
import Matrices.Views.SymmetricView;

import java.io.File;


/**
 * Implementation for a symmetric matrix
 */
public class SymmetricDSM implements IDSM {

    private SymmetricDSMData matrixData;
    private SymmetricView matrixView;
    private SymmetricIOHandler matrixIOHandler;
    private SymmetricSideBar matrixSideBar;
    private SymmetricEditorTab editorTab;


    /**
     * Creates a new matrix object by reading in a file. Throws IllegalArgumentException if there was an error reading
     * the file
     *
     * @param file    the file object that contains a symmetric dsm to read
     */
    public SymmetricDSM(File file) {
        matrixIOHandler = new SymmetricIOHandler(file);
        matrixData = matrixIOHandler.readFile();
        if(matrixData == null) {
            throw new IllegalArgumentException("There was an error reading the matrix at " + file);  // error because error occurred on file read
        }

        matrixIOHandler.setMatrix(matrixData);
        editorTab = new SymmetricEditorTab(matrixData);
        matrixView = new SymmetricView(matrixData, 12.0);
        matrixSideBar = new SymmetricSideBar(matrixData, matrixView);
    }


    /**
     * Constructor that takes an already created matrix and its ioHandler and creates the other necessary
     * components of a matrix
     *
     * @param matrixData       the matrix data object
     * @param matrixIOHandler  the IOHandler object
     */
    public SymmetricDSM(SymmetricDSMData matrixData, SymmetricIOHandler matrixIOHandler) {
        this.matrixData = matrixData;
        this.matrixIOHandler = matrixIOHandler;

        editorTab = new SymmetricEditorTab(matrixData);
        matrixView = new SymmetricView(matrixData, 12.0);
        matrixSideBar = new SymmetricSideBar(matrixData, matrixView);
    }


    /**
     * @return  the DSM Data object for the matrix
     */
    @Override
    public SymmetricDSMData getMatrixData() {
        return matrixData;
    }


    /**
     * @return  the DSM editor tab object for the matrix
     */
    @Override
    public SymmetricEditorTab getMatrixEditorTab() {
        return editorTab;
    }


    /**
     * @return  the DSM View object for the matrix
     */
    @Override
    public SymmetricView getMatrixView() {
        return editorTab.getMatrixView();
    }


    /**
     * @return  the DSM IOHandler object for the matrix
     */
    @Override
    public SymmetricIOHandler getMatrixIOHandler() {
        return matrixIOHandler;
    }


    /**
     * @return  true if the latest changes (if any) in the matrix have been saved
     */
    @Override
    public boolean isMatrixSaved() {
        return !matrixData.getWasModified();
    }
}
