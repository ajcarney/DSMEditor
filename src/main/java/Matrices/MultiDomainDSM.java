package Matrices;

import Matrices.Data.MultiDomainDSMData;
import Matrices.EditorTabs.MultiDomainEditorTab;
import Matrices.IOHandlers.MultiDomainIOHandler;
import Matrices.SideBarTools.MultiDomainSideBar;
import Matrices.Views.AbstractMatrixView;
import Matrices.Views.MultiDomainView;
import UI.HeaderMenu;

import java.io.File;


/**
 * Implementation for a MultiDomain matrix
 */
public class MultiDomainDSM implements IDSM {

    private MultiDomainDSMData matrixData;
    private MultiDomainView matrixView;
    private MultiDomainIOHandler matrixIOHandler;
    private MultiDomainSideBar matrixSideBar;
    private MultiDomainEditorTab editorTab;


    /**
     * Creates a new matrix object by reading in a file. Throws IllegalArgumentException if there was an error reading
     * the file
     *
     * @param file    the file object that contains a MultiDomain dsm to read
     */
    public MultiDomainDSM(File file, HeaderMenu headerMenu) {
        matrixIOHandler = new MultiDomainIOHandler(file);
        matrixData = matrixIOHandler.readFile();
        if(matrixData == null) {
            throw new IllegalArgumentException("There was an error reading the matrix at " + file);  // error because error occurred on file read
        }

        matrixIOHandler.setMatrix(matrixData);
        editorTab = new MultiDomainEditorTab(matrixData, matrixIOHandler, headerMenu);
        matrixView = new MultiDomainView(matrixData, 12.0);
        matrixSideBar = new MultiDomainSideBar(matrixData, matrixView);
    }


    /**
     * Constructor that takes an already created matrix and its ioHandler and creates the other necessary
     * components of a matrix
     *
     * @param matrixData       the matrix data object
     * @param matrixIOHandler  the IOHandler object
     */
    public MultiDomainDSM(MultiDomainDSMData matrixData, MultiDomainIOHandler matrixIOHandler, HeaderMenu headerMenu) {
        this.matrixData = matrixData;
        this.matrixIOHandler = matrixIOHandler;

        editorTab = new MultiDomainEditorTab(this.matrixData, this.matrixIOHandler, headerMenu);
        matrixView = new MultiDomainView(matrixData, 12.0);
        matrixSideBar = new MultiDomainSideBar(matrixData, matrixView);
    }


    /**
     * @return  the DSM Data object for the matrix
     */
    @Override
    public MultiDomainDSMData getMatrixData() {
        return matrixData;
    }


    /**
     * @return  the DSM editor tab object for the matrix
     */
    @Override
    public MultiDomainEditorTab getMatrixEditorTab() {
        return editorTab;
    }


    /**
     * @return  the DSM View object for the matrix
     */
    @Override
    public AbstractMatrixView getMatrixView() {
        return editorTab.getMatrixView();
    }


    /**
     * @return  the DSM IOHandler object for the matrix
     */
    @Override
    public MultiDomainIOHandler getMatrixIOHandler() {
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
