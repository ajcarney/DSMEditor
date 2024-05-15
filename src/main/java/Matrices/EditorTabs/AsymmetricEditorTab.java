package Matrices.EditorTabs;

import Matrices.Data.AsymmetricDSMData;
import Matrices.IOHandlers.AsymmetricIOHandler;
import UI.MatrixMetaDataPane;
import UI.MatrixViews.AsymmetricView;
import UI.SideBarViews.AsymmetricSideBar;
import javafx.beans.binding.Bindings;

import java.io.File;


/**
 * The editor tab for Asymmetric matrices. Only shows the matrix view as the main content
 */
public class AsymmetricEditorTab extends AbstractEditorTab {

    /**
     * Generic constructor. Takes the data for an Asymmetric DSM
     *
     * @param matrixData  the data for the Asymmetric matrix
     */
    public AsymmetricEditorTab(AsymmetricDSMData matrixData, AsymmetricIOHandler ioHandler) {
        super(matrixData, ioHandler);
        this.matrixView = new AsymmetricView((AsymmetricDSMData) this.matrixData, 12.0);
        this.matrixSideBar = new AsymmetricSideBar((AsymmetricDSMData) this.matrixData, (AsymmetricView) this.matrixView);
    }

    /**
     * Creates a new matrix object by reading in a file. Throws IllegalArgumentException if there was an error reading
     * the file
     *
     * @param file    the file object that contains an Asymmetric dsm to read
     */
    public AsymmetricEditorTab(File file) {
        matrixIOHandler = new AsymmetricIOHandler(file);
        matrixData = matrixIOHandler.readFile();
        if(matrixData == null) {
            throw new IllegalArgumentException("There was an error reading the matrix at " + file);  // error because error occurred on file read
        }
        matrixIOHandler.setMatrix(matrixData);

        matrixView = new AsymmetricView((AsymmetricDSMData) matrixData, 12.0);
        matrixSideBar = new AsymmetricSideBar((AsymmetricDSMData) matrixData, (AsymmetricView) matrixView);

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
