package Matrices.EditorTabs;

import Matrices.Data.SymmetricDSMData;
import Matrices.IOHandlers.SymmetricIOHandler;
import UI.MatrixMetaDataPane;
import UI.MatrixViews.SymmetricView;
import UI.SideBarViews.SymmetricSideBar;
import javafx.beans.binding.Bindings;

import java.io.File;


/**
 * The editor tab for symmetric matrices. Only shows the matrix view as the main content
 */
public class SymmetricEditorTab extends AbstractEditorTab {

    /**
     * Takes the data for a symmetric DSM
     *
     * @param matrixData  the data for the symmetric matrix
     */
    public SymmetricEditorTab(SymmetricDSMData matrixData, SymmetricIOHandler ioHandler) {
        super(matrixData, ioHandler);
        this.matrixView = new SymmetricView((SymmetricDSMData) this.matrixData, 12.0);
        this.matrixSideBar = new SymmetricSideBar((SymmetricDSMData) this.matrixData, (SymmetricView) this.matrixView);
    }

    /**
     * Creates a new matrix object by reading in a file. Throws IllegalArgumentException if there was an error reading
     * the file
     *
     * @param file    the file object that contains a symmetric dsm to read
     */
    public SymmetricEditorTab(File file) {
        matrixIOHandler = new SymmetricIOHandler(file);
        matrixData = matrixIOHandler.readFile();
        if(matrixData == null) {
            throw new IllegalArgumentException("There was an error reading the matrix at " + file);  // error because error occurred on file read
        }
        matrixIOHandler.setMatrix(matrixData);

        matrixView = new SymmetricView((SymmetricDSMData) matrixData, 12.0);
        matrixSideBar = new SymmetricSideBar((SymmetricDSMData) matrixData, (SymmetricView) matrixView);

        this.metadata = new MatrixMetaDataPane(this.matrixData);
        this.isSaved.bind(this.matrixData.getWasModifiedProperty().not());  // saved when not modified

        this.titleProperty.bind(Bindings.createStringBinding(() -> {
            String title = matrixIOHandler.getSavePath().getName();
            if (matrixData.getWasModifiedProperty().get()) {
                title += "*";
            }
            return title;
        }, matrixData.getWasModifiedProperty()));
    }

}
