package Matrices;

import Matrices.Data.MultiDomainDSMData;
import Matrices.IOHandlers.MultiDomainIOHandler;
import Matrices.SideBarTools.MultiDomainSideBar;
import Matrices.Views.MultiDomainView;
import UI.EditorPane;

import java.io.File;


/**
 * Implementation for a MultiDomain matrix
 */
public class MultiDomainDSM implements IDSM {

    private MultiDomainDSMData matrixData;
    private MultiDomainView matrixView;
    private MultiDomainIOHandler matrixIOHandler;
    private MultiDomainSideBar matrixSideBar;


    /**
     * Creates a new matrix object by reading in a file. Throws IllegalArgumentException if there was an error reading
     * the file
     *
     * @param file    the file object that contains a MultiDomain dsm to read
     * @param editor  the editor object
     */
    public MultiDomainDSM(File file, EditorPane editor) {
        matrixIOHandler = new MultiDomainIOHandler(file);
        matrixData = matrixIOHandler.readFile();
        if(matrixData == null) {
            throw new IllegalArgumentException("There was an error reading the matrix at " + file);  // error because error occurred on file read
        }

        matrixView = new MultiDomainView(matrixData, 12.0);
        matrixSideBar = new MultiDomainSideBar(matrixData, editor);
    }


    /**
     * Constructor that takes an already created matrix and its ioHandler and creates the other necessary
     * components of a matrix
     *
     * @param matrixData       the matrix data object
     * @param matrixIOHandler  the IOHandler object
     * @param editor           the editor object
     */
    public MultiDomainDSM(MultiDomainDSMData matrixData, MultiDomainIOHandler matrixIOHandler, EditorPane editor) {
        this.matrixData = matrixData;
        this.matrixIOHandler = matrixIOHandler;

        matrixView = new MultiDomainView(matrixData, 12.0);
        matrixSideBar = new MultiDomainSideBar(matrixData, editor);
    }


    /**
     * @return  the DSM Data object for the matrix
     */
    @Override
    public MultiDomainDSMData getMatrixData() {
        return matrixData;
    }


    /**
     * @return  the DSM View object for the matrix
     */
    @Override
    public MultiDomainView getMatrixView() {
        return matrixView;
    }


    /**
     * @return  the DSM IOHandler object for the matrix
     */
    @Override
    public MultiDomainIOHandler getMatrixIOHandler() {
        return matrixIOHandler;
    }


    /**
     * @return  the DSM Side bar object for the matrix
     */
    @Override
    public MultiDomainSideBar getMatrixSideBar() {
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
