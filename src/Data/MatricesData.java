package Data;

import IOHandler.TemplateIOHandler;
import View.MatrixHandlers.TemplateMatrixHandler;

import java.util.HashMap;

public class MatricesData {

    private HashMap<Integer, TemplateDSM> matrices;
    private HashMap<Integer, TemplateIOHandler> matrixIOHandlers;
    private HashMap<Integer, TemplateMatrixHandler> matrixHandlers;
    private static int currentMatrixUid = 0;


    /**
     * creates a new MatricesData object
     */
    public MatricesData() {
        matrices = new HashMap<>();
        matrixIOHandlers = new HashMap<>();
        matrixHandlers = new HashMap<>();
    }


    /**
     * Adds a matrix to be handled and returns the unique id assigned to it
     *
     * @param matrix         SymmetricDSM object of the matrix to be added
     * @param ioHandler      File object of the location to save the matrix to
     * @param matrixHandler  the matrix handler object for the matrix
     * @return               the unique id given to the matrix so that it can be tracked
     */
    public int addMatrix(TemplateDSM matrix, TemplateIOHandler ioHandler, TemplateMatrixHandler matrixHandler) {
        currentMatrixUid += 1;

        this.matrices.put(currentMatrixUid, matrix);
        this.matrixIOHandlers.put(currentMatrixUid, ioHandler);
        this.matrixHandlers.put(currentMatrixUid, matrixHandler);

        return currentMatrixUid;
    }


    /**
     * Returns the matrices HashMap
     *
     * @return  HashMap of matrix uids and SymmetricDSM objects  TODO: This should probably be immutable in the future
     */
    public HashMap<Integer, TemplateDSM> getMatrices() {
        return matrices;
    }


    /**
     * Returns a TemplateDSM object with a given uid
     *
     * @param uid  the uid of the matrix to return
     * @return     SymmetricDSM object of the matrix
     */
    public TemplateDSM getMatrix(int uid) {
        return matrices.get(uid);
    }


    /**
     * Returns the matrixIOHandlers HashMap
     *
     * @return  HashMap of matrix uids and File objects  TODO: This should probably be immutable in the future
     */
    public HashMap<Integer, TemplateIOHandler> getMatrixIOHandlers() {
        return matrixIOHandlers;
    }


    /**
     * Returns the default save path of a matrix
     *
     * @param   matrixUid the file handler object for matrix with uid matrixUid
     * @return  File object of the default save path currently set
     */
    public TemplateIOHandler getMatrixIOHandler(int matrixUid) {
        return matrixIOHandlers.get(matrixUid);
    }


    /**
     * Returns all gui handler objects
     *
     * @return  MatrixGuiHandler HashMap
     */
    public HashMap<Integer, TemplateMatrixHandler> getMatrixHandlers() {
        return matrixHandlers;
    }


    /**
     * Returns the gui handler object associated with the matrix
     *
     * @param   matrixUid the save path for matrix with uid matrixUid
     * @return  matrix handler object for the matrix
     */
    public TemplateMatrixHandler getMatrixHandler(int matrixUid) {
        return matrixHandlers.get(matrixUid);
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
        matrixIOHandlers.remove(matrixUid);
        matrixHandlers.remove(matrixUid);
    }

}
