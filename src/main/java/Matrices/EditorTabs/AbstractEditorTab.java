package Matrices.EditorTabs;

import Matrices.Data.AbstractDSMData;
import Matrices.IOHandlers.AbstractIOHandler;
import UI.MatrixMetaDataPane;
import UI.MatrixViews.AbstractMatrixView;
import UI.SideBarViews.AbstractSideBar;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface to define what an editor tab is able to have. It is able to
 * have different panes where the child nodes can be set by the implementation
 * of this interface
 */
public abstract class AbstractEditorTab {

    public BooleanProperty isChanged = new SimpleBooleanProperty(false);  // true when matrix has been changed internally and needs a refresh
    protected boolean isMutable = true;  // true when ui buttons should be able to mutate the visible matrix

    protected BooleanProperty isSaved = new SimpleBooleanProperty(false);  // true when ui buttons should be able to mutate the visible matrix
    protected ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();  // the title of the tab

    protected AbstractDSMData matrixData;         // the data for the visible matrix
    protected AbstractIOHandler matrixIOHandler;  // the io handler for the visible matrix
    protected AbstractMatrixView matrixView;      // the view for the visible matrix
    protected AbstractSideBar matrixSideBar;      // the sidebar for the visible matrix

    protected MatrixMetaDataPane metadata;

    /**
     * Default constructor
     */
    protected AbstractEditorTab() {}

    /**
     * Constructor that takes the data for a matrix and the io handler for the matrix
     * Sets metadata pane and observables
     *
     * @param matrixData the data for the matrix
     * @param ioHandler  the io handler for the matrix
     */
    protected AbstractEditorTab(AbstractDSMData matrixData, AbstractIOHandler ioHandler) {
        this.matrixData = matrixData;
        this.matrixIOHandler = ioHandler;
        this.metadata = new MatrixMetaDataPane(this.matrixData);

        this.isSaved.bind(this.matrixData.getWasModifiedProperty().not());  // saved when not modified

        this.title.bind(Bindings.createStringBinding(() -> {
            String title = matrixIOHandler.getSavePath().getName();
            if (matrixData.getWasModifiedProperty().get()) {
                title += "*";
            }
            return title;
        }, matrixData.getWasModifiedProperty()));
    }


    /**
     * @return  The title of the tab as a read only property
     */
    public ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }


    /**
     * @return  true if the matrix is mutable, false otherwise
     */
    public boolean getIsMutable() {
        return isMutable;
    }


    /**
     * @return  The node to be displayed as the center content
     */
    public Pane getCenterPane() {
        return matrixView.getView();
    }


    /**
     * @return  The node to be displayed as the left content
     */
    public Pane getLeftPane() {
        return matrixSideBar.getLayout();
    }


    /**
     * @return  The node to be displayed as the right content
     */
    public Pane getRightPane() {
        return metadata.getLayout();
    }


    /**
     * @return  The node to be displayed as the bottom content
     */
    public Pane getBottomPane() {
        return new Pane();
    }


    /**
     * @return  The matrix view used by the tab
     */
    public AbstractMatrixView getMatrixView() {
        return matrixView;
    }


    /**
     * @return  all the matrix views currently in the editor tab
     */
    public List<AbstractMatrixView> getAllMatrixViews() {
        List<AbstractMatrixView> views = new ArrayList<>();
        views.add(matrixView);
        return views;
    }


    /**
     * @return  the matrix data of the open matrix
     */
    public AbstractDSMData getMatrixData() {
        return matrixData;
    }


    /**
     * @return  the matrix io handler of the open matrix
     */
    public AbstractIOHandler getMatrixIOHandler() {
        return matrixIOHandler;
    }

}
