package Matrices;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.util.ArrayList;


/**
 * Wrapper class for handling matrix objects. Maps matrix objects to a uid
 */
public class MatricesCollection {

    private final ObservableMap<Integer, IDSM> matrices;


    /**
     * Constructor. Initializes the ObservableMap for the matrices
     */
    public MatricesCollection() {
        matrices = FXCollections.observableHashMap();
    }


    /**
     * Adds a matrix to the collection based on its uid
     *
     * @param uid     the uid to identify the matrix
     * @param matrix  the matrix object
     */
    public void addMatrix(int uid, IDSM matrix) {
        matrices.put(uid, matrix);
    }


    /**
     * @return  a list of all the matrix objects
     */
    public ObservableMap<Integer, IDSM> getMatrices() {
        return matrices;
    }


    /**
     * Returns a matrix based on its uid
     *
     * @param uid  the uid of the matrix
     * @return     the DSM
     */
    public IDSM getMatrix(int uid) {
        return matrices.get(uid);
    }


    /**
     * Removes a matrix from the collection by its uid
     *
     * @param uid  the uid of the matrix
     */
    public void removeMatrix(int uid) {
        matrices.remove(uid);
    }


    /**
     * @return  a list of all the absolute save paths for the matrices stored by the controller
     */
    public ArrayList<String> getMatrixFileAbsoluteSavePaths() {
        ArrayList<String> saveNames = new ArrayList<>();
        for(IDSM dsm : this.matrices.values()) {
            saveNames.add(dsm.getMatrixIOHandler().getSavePath().getAbsolutePath());
        }
        return saveNames;
    }


    /**
     * returns whether or not the wasModifiedFlag of a matrix is set or cleared. If
     * it is set then the matrix is not saved. If the flag is cleared, then the matrix
     * is saved.
     *
     * @param matrixUid the matrix to check whether or not has been saved
     * @return          true if matrix is saved, false otherwise
     */
    public boolean isMatrixSaved(int matrixUid) {
        return matrices.get(matrixUid).isMatrixSaved();
    }
}
