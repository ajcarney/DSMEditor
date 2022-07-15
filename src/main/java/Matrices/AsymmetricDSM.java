package Matrices;

import Matrices.Data.AsymmetricDSMData;
import Matrices.IOHandlers.AsymmetricIOHandler;
import Matrices.SideBarTools.AsymmetricSideBar;
import Matrices.Views.AsymmetricView;
import UI.EditorPane;

import java.io.File;


/**
 * Implementation for a Asymmetric matrix
 */
public class AsymmetricDSM implements IDSM {

    private AsymmetricDSMData matrixData;
    private AsymmetricView matrixView;
    private AsymmetricIOHandler matrixIOHandler;
    private AsymmetricSideBar matrixSideBar;


    /**
     * Creates a new matrix object by reading in a file. Throws IllegalArgumentException if there was an error reading
     * the file
     *
     * @param file    the file object that contains a Asymmetric dsm to read
     * @param editor  the editor object
     */
    public AsymmetricDSM(File file, EditorPane editor) {
        matrixIOHandler = new AsymmetricIOHandler(file);
        matrixData = matrixIOHandler.readFile();
        if(matrixData == null) {
            throw new IllegalArgumentException("There was an error reading the matrix at " + file);  // error because error occurred on file read
        }

        matrixIOHandler.setMatrix(matrixData);
        matrixView = new AsymmetricView(matrixData, 12.0);
        matrixSideBar = new AsymmetricSideBar(matrixData, editor);
    }


    /**
     * Constructor that takes an already created matrix and its ioHandler and creates the other necessary
     * components of a matrix
     *
     * @param matrixData       the matrix data object
     * @param matrixIOHandler  the IOHandler object
     * @param editor           the editor object
     */
    public AsymmetricDSM(AsymmetricDSMData matrixData, AsymmetricIOHandler matrixIOHandler, EditorPane editor) {
        this.matrixData = matrixData;
        this.matrixIOHandler = matrixIOHandler;

        matrixView = new AsymmetricView(matrixData, 12.0);
        matrixSideBar = new AsymmetricSideBar(matrixData, editor);
    }


    /**
     * @return  the DSM Data object for the matrix
     */
    @Override
    public AsymmetricDSMData getMatrixData() {
        return matrixData;
    }


    /**
     * @return  the DSM View object for the matrix
     */
    @Override
    public AsymmetricView getMatrixView() {
        return matrixView;
    }


    /**
     * @return  the DSM IOHandler object for the matrix
     */
    @Override
    public AsymmetricIOHandler getMatrixIOHandler() {
        return matrixIOHandler;
    }


    /**
     * @return  the DSM Side bar object for the matrix
     */
    @Override
    public AsymmetricSideBar getMatrixSideBar() {
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
