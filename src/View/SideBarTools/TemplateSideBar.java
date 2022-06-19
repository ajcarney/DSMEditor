package View.SideBarTools;

import Data.TemplateDSM;
import View.EditorPane;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;


/**
 * Generic class for creating a sidebar to interact with matrices
 *
 * @param <T>  the type of matrix the sidebar is for
 */
public abstract class TemplateSideBar<T extends TemplateDSM> {
    protected VBox layout;

    protected final Button addMatrixItems = new Button();
    protected final Button deleteMatrixItems = new Button();
    protected final Button appendConnections = new Button("Append Connections");
    protected final Button setConnections = new Button("Set Connections");
    protected final Button deleteConnections = new Button("Delete Connections");
    protected final Button sort = new Button("Sort");
    protected final Button reDistributeIndices = new Button("Re-Distribute Indices");

    protected EditorPane editor;
    protected T matrix;


    /**
     * Creates a new Sidebar object. Sets up the gui and all its widgets and puts them in the layout field.
     * Requires an MatrixHandler object to get the matrix and a EditorPane object to get the current focused tab
     * and call updates to it.
     *
     * @param matrix  the matrix data object instance
     * @param editor  the EditorPane instance
     */
    TemplateSideBar(T matrix, EditorPane editor) {
        layout = new VBox();
        this.editor = editor;
        this.matrix = matrix;

        addMatrixItems.setOnAction(e -> addMatrixItemsCallback());
        addMatrixItems.setMaxWidth(Double.MAX_VALUE);

        deleteMatrixItems.setOnAction(e -> deleteMatrixItemsCallback());
        deleteMatrixItems.setMaxWidth(Double.MAX_VALUE);

        appendConnections.setOnAction(e -> appendConnectionsCallback());
        appendConnections.setMaxWidth(Double.MAX_VALUE);

        setConnections.setOnAction(e -> setConnectionsCallback());
        setConnections.setMaxWidth(Double.MAX_VALUE);

        deleteConnections.setOnAction(e -> deleteConnectionsCallback());
        deleteConnections.setMaxWidth(Double.MAX_VALUE);

        sort.setOnAction(e -> sortCallback());
        sort.setMaxWidth(Double.MAX_VALUE);

        reDistributeIndices.setOnAction(e -> reDistributeIndicesCallback());
        reDistributeIndices.setMaxWidth(Double.MAX_VALUE);
    }


    /**
     * Sets up the button callback for adding items to the matrix
     */
    protected abstract void addMatrixItemsCallback();


    /**
     * Sets up the button callback for deleting items from the matrix
     */
    protected abstract void deleteMatrixItemsCallback();


    /**
     * Sets up the button callback for appending connections to the matrix
     */
    protected abstract void appendConnectionsCallback();


    /**
     * Sets up the button callback for setting connections in the matrix
     */
    protected abstract void setConnectionsCallback();


    /**
     * Sets up the button callback for deleting connections in the matrix
     */
    protected abstract void deleteConnectionsCallback();


    /**
     * Sets up the button callback for sorting the matrix
     */
    protected void sortCallback() {
        sort.setOnAction(e -> {
            editor.refreshTab();
        });
        sort.setMaxWidth(Double.MAX_VALUE);
    }


    /**
     * Sets up the button callback for re-distributing sort indices
     */
    protected void reDistributeIndicesCallback() {
        reDistributeIndices.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            matrix.reDistributeSortIndices();
            editor.refreshTab();
            matrix.setCurrentStateAsCheckpoint();
        });
        reDistributeIndices.setMaxWidth(Double.MAX_VALUE);
    }


    /**
     * Returns the VBox of the layout so that it can be added to a scene
     *
     * @return the VBox layout of the toolbar
     */
    public VBox getLayout() {
        return layout;
    }
}
