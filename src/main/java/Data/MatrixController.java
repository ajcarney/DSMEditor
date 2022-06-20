package Data;

import IOHandler.TemplateIOHandler;
import View.MatrixHandlers.TemplateMatrixHandler;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Class for managing data corresponding to a matrix
 */
public class MatrixController {

    private HashMap<Integer, TemplateDSM> matrices;
    private HashMap<Integer, TemplateIOHandler> matrixIOHandlers;
    private HashMap<Integer, TemplateMatrixHandler> matrixHandlers;


    /**
     * Initializes hashmaps for storing the matrix data
     */
    public MatrixController() {
        matrices = new HashMap<>();
        matrixIOHandlers = new HashMap<>();
        matrixHandlers = new HashMap<>();
    }


    /**
     * Adds a matrix to be handled and keeps track of it based on its uid
     *
     * @param uid            the uid to use for the matrix objects
     * @param matrix         the matrix
     * @param ioHandler      the io handler object for the matrix
     * @param matrixHandler  the matrix handler object for the matrix
     */
    public void addMatrix(int uid, TemplateDSM matrix, TemplateIOHandler ioHandler, TemplateMatrixHandler matrixHandler) {
        matrices.put(uid, matrix);
        matrixIOHandlers.put(uid, ioHandler);
        matrixHandlers.put(uid, matrixHandler);
    }


    /**
     * Removes a matrix and its data from being stored
     *
     * @param uid  the uid that the matrix data was stored under
     */
    public void removeMatrix(int uid) {
        matrices.remove(uid);
        matrixIOHandlers.remove(uid);
        matrixHandlers.remove(uid);
    }


    /**
     * @return  the hashmap for all the matrix io handlers
     */
    public HashMap<Integer, TemplateDSM> getMatrices() {
        return this.matrices;
    }


    /**
     * returns a matrix based on the matrix uid
     *
     * @param matrixUid  the uid of the matrix
     * @return           the matrix object associated with the uid
     */
    public TemplateDSM getMatrix(int matrixUid) {
        return this.matrices.get(matrixUid);
    }


    /**
     * @return  the hashmap for all the matrix handlers
     */
    public HashMap<Integer, TemplateMatrixHandler> getMatrixHandlers() {
        return this.matrixHandlers;
    }


    /**
     * returns a matrix handler based on the matrix uid
     *
     * @param matrixUid  the uid of the matrix
     * @return           the matrix handler object associated with the uid
     */
    public TemplateMatrixHandler getMatrixHandler(int matrixUid) {
        return this.matrixHandlers.get(matrixUid);
    }


    /**
     * @return  the hashmap for all the matrix io handlers
     */
    public HashMap<Integer, TemplateIOHandler> getMatrixIOHandlers() {
        return this.matrixIOHandlers;
    }


    /**
     * returns a matrix io handler based on the matrix uid
     *
     * @param matrixUid  the uid of the matrix
     * @return           the matrix io handler object associated with the uid
     */
    public TemplateIOHandler getMatrixIOHandler(int matrixUid) {
        return this.matrixIOHandlers.get(matrixUid);
    }


    /**
     * @return  a list of all the absolute save paths for the matrices stored by the editor
     */
    public ArrayList<String> getMatrixFileAbsoluteSavePaths() {
        ArrayList<String> saveNames = new ArrayList<>();
        for(TemplateIOHandler ioHandler : this.matrixIOHandlers.values()) {
            saveNames.add(ioHandler.getSavePath().getAbsolutePath());
        }
        return saveNames;
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

}
