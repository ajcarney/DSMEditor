package Matrices.Views;

import javafx.scene.layout.Pane;


/**
 * Defines how a matrix view is to behave at a high level
 */
public interface IMatrixView {

    /**
     * Contains the different types of valid view modes for the matrix. Each of these defines
     * how the matrix is to behave when it is rendered to the user
     */
    enum MatrixViewMode {
        EDIT,
        STATIC,
        FAST_RENDER
    }


    /**
     * @return  if the matrix is showing names or values
     */
    boolean getShowNames();


    /**
     * @return  the current view mode of the matrix
     */
    MatrixViewMode getCurrentMode();


    /**
     * Gets the view for the matrix
     *
     * @param <T>  type pane of the node that holds the view
     * @return     a node of type pane of the view
     */
    <T extends Pane> T getView();


    /**
     * Refreshes the content of the view
     */
    void refreshView();


    /**
     * Function to remove several different highlight types of all cells by assigning
     * null to that highlight field
     *
     * @param highlightType the highlight type to assign to (userHighlight, errorHighlight, symmetryErrorHighlight)
     */
    void clearAllCellsHighlight(String highlightType);


    /**
     * Toggles the cross highlighting for the matrix
     */
    void toggleCrossHighlighting();


    /**
     * Sets the font size to a new value. This value is bound to in the gui so it will be auto updated.
     *
     * @param newSize the new font size to use in the gui
     */
    void setFontSize(Double newSize);


    /**
     * Sets the value of showNames. This value is bound to in the gui so that either the connection names or weights will be shown.
     *
     * @param newValue show the names of the connections or the weights if false
     */
    void setShowNames(Boolean newValue);


    /**
     * Sets the new current view mode for the matrix
     *
     * @param mode  the new mode for the matrix
     */
    void setCurrentMode(MatrixViewMode mode);
}
