package Data;

import View.MatrixGuiHandler;

import java.io.File;
import java.util.HashMap;

/**
 * Class to manage SymmetricDSM classes and read and write to different file formats
 * TODO: add validation of file paths when they are passed as parameters
 *
 * @author: Aiden Carney
 */
public class MatrixHandler {
    private HashMap<Integer, SymmetricDSM> matrices;
    private HashMap<Integer, File> matrixSaveNames;
    private HashMap<Integer, MatrixGuiHandler> matrixGuiHandlers;
    private static int currentMatrixUid = 0;


    /**
     * creates a new IOHandler object
     */
    public MatrixHandler() {
        matrices = new HashMap<>();
        matrixSaveNames = new HashMap<>();
        matrixGuiHandlers = new HashMap<>();
    }


    /**
     * Adds a matrix to be handled and returns the unique id assigned to it
     *
     * @param matrix SymmetricDSM object of the matrix to be added
     * @param file   File object of the location to save the matrix to
     * @return the unique id given to the matrix so that it can be tracked
     */
    public int addMatrix(SymmetricDSM matrix, File file) {
        currentMatrixUid += 1;

        this.matrices.put(currentMatrixUid, matrix);
        this.matrixSaveNames.put(currentMatrixUid, file);
        this.matrixGuiHandlers.put(currentMatrixUid, new MatrixGuiHandler(matrix, 12));

        return currentMatrixUid;
    }


    /**
     * Returns the matrices HashMap
     *
     * @return HashMap of matrix uids and SymmetricDSM objects  TODO: This should probably be immutable in the future
     */
    public HashMap<Integer, SymmetricDSM> getMatrices() {
        return matrices;
    }


    /**
     * Returns the matrixSaveNames HashMap
     *
     * @return HashMap of matrix uids and File objects  TODO: This should probably be immutable in the future
     */
    public HashMap<Integer, File> getMatrixSaveNames() {
        return matrixSaveNames;
    }


    /**
     * Returns a SymmetricDSM object
     *
     * @param uid the uid of the matrix to return
     * @return SymmetricDSM object of the matrix
     */
    public SymmetricDSM getMatrix(int uid) {
        return matrices.get(uid);
    }


    /**
     * Returns the default save path of a matrix
     *
     * @param matrixUid the save path for matrix with uid matrixUid
     * @return File object of the default save path currently set
     */
    public File getMatrixSaveFile(int matrixUid) {
        return matrixSaveNames.get(matrixUid);
    }


    /**
     * Returns the gui handler object associated with the matrix
     *
     * @param matrixUid the save path for matrix with uid matrixUid
     * @return MatrixGuiHandler object of the matrix
     */
    public MatrixGuiHandler getMatrixGuiHandler(int matrixUid) {
        return matrixGuiHandlers.get(matrixUid);
    }


    /**
     * Returns all gui handler objects
     *
     * @return MatrixGuiHandler HashMap
     */
    public HashMap<Integer, MatrixGuiHandler> getMatrixGuiHandlers() {
        return matrixGuiHandlers;
    }


    /**
     * Updates the default save location of a matrix
     *
     * @param matrixUid the matrix to update the save path of
     * @param newFile   File object of the new save path
     */
    public void setMatrixSaveFile(int matrixUid, File newFile) {
        matrixSaveNames.put(matrixUid, newFile);
    }


    /**
     * returns whether or not the wasModifiedFlag of a matrix is set or cleared. If
     * it is set then the matrix is not saved. If the flag is cleared, then the matrix
     * is saved.
     *
     * @param matrixUid the matrix to check whether or not has been saved
     * @return true if matrix is saved, false otherwise
     */
    public boolean isMatrixSaved(int matrixUid) {
        return !matrices.get(matrixUid).getWasModified();
    }


    /**
     * Removes a matrix to be handled
     *
     * @param matrixUid the uid of the matrix to be removed
     */
    public void removeMatrix(int matrixUid) {
        matrices.remove(matrixUid);
        matrixSaveNames.remove(matrixUid);
        matrixGuiHandlers.remove(matrixUid);
    }

}
