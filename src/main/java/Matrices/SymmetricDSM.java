package Matrices;

import Matrices.Data.SymmetricDSMData;
import Matrices.IOHandlers.SymmetricIOHandler;
import Matrices.SideBarTools.SymmetricSideBar;
import Matrices.Views.SymmetricView;
import UI.EditorPane;

import java.io.File;


/**
 * Implementation for a symmetric matrix
 */
public class SymmetricDSM implements IDSM {

    private SymmetricDSMData matrixData;
    private SymmetricView matrixView;
    private SymmetricIOHandler matrixIOHandler;
    private SymmetricSideBar matrixSideBar;


    /**
     * Creates a new matrix object by reading in a file. Throws IllegalArgumentException if there was an error reading
     * the file
     *
     * @param file    the file object that contains a symmetric dsm to read
     * @param editor  the editor object
     */
    public SymmetricDSM(File file, EditorPane editor) {
        matrixIOHandler = new SymmetricIOHandler(file);
        matrixData = matrixIOHandler.readFile();
        if(matrixData == null) {
            throw new IllegalArgumentException("There was an error reading the matrix at " + file);  // error because error occurred on file read
        }

        matrixView = new SymmetricView(matrixData, 12.0);
        matrixSideBar = new SymmetricSideBar(matrixData, editor);
    }


    /**
     * Constructor that takes an already created matrix and its ioHandler and creates the other necessary
     * components of a matrix
     *
     * @param matrixData       the matrix data object
     * @param matrixIOHandler  the IOHandler object
     * @param editor           the editor object
     */
    public SymmetricDSM(SymmetricDSMData matrixData, SymmetricIOHandler matrixIOHandler, EditorPane editor) {
        this.matrixData = matrixData;
        this.matrixIOHandler = matrixIOHandler;

        matrixView = new SymmetricView(matrixData, 12.0);
        matrixSideBar = new SymmetricSideBar(matrixData, editor);
    }


    /**
     * @return  the DSM Data object for the matrix
     */
    @Override
    public SymmetricDSMData getMatrixData() {
        return matrixData;
    }


    /**
     * @return  the DSM View object for the matrix
     */
    @Override
    public SymmetricView getMatrixView() {
        return matrixView;
    }


    /**
     * @return  the DSM IOHandler object for the matrix
     */
    @Override
    public SymmetricIOHandler getMatrixIOHandler() {
        return matrixIOHandler;
    }


    /**
     * @return  the DSM Side bar object for the matrix
     */
    @Override
    public SymmetricSideBar getMatrixSideBar() {
        return matrixSideBar;
    }


    /**
     * @return  true if the latest changes (if any) in the matrix have been saved
     */
    @Override
    public boolean isMatrixSaved() {
        return !matrixData.getWasModified();
    }
}
