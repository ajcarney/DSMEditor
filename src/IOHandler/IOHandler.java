package IOHandler;

import DSMData.DataHandler;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Vector;

public class IOHandler {

    private HashMap< Integer, DataHandler > matrices;
    private HashMap< Integer, String > matrixSaveNames;
    private static int currentMatrixUid = 0;

    public IOHandler() {
        matrices = new HashMap<>();
        matrixSaveNames = new HashMap<>();
    }

    public int addMatrix(DataHandler matrix, String fileSaveName) {
        this.currentMatrixUid += 1;

        this.matrices.put(this.currentMatrixUid, matrix);
        this.matrixSaveNames.put(this.currentMatrixUid, fileSaveName);

        return this.currentMatrixUid;
    }


    public int saveMatrixToFile(int matrixUid) {
        if(!this.matrixSaveNames.get(matrixUid).equals("")) {  // TODO: add actual file IO
            matrices.get(matrixUid).clearWasModifiedFlag();

            System.out.println("Saving file " + getMatrixSaveFile(matrixUid));

            return 1;  // file was successfully saved
        }

        return 0;  // 0 means the filename was not present, so the data could not be saved

    }

    public int saveMatrixToNewFile(int matrixUid, String fileName) {
        if(!fileName.equals("")) {  // TODO: add actual validation of path
            matrices.get(matrixUid).clearWasModifiedFlag();
            setMatrixSaveFile(matrixUid, fileName);  // update the location that the file will be saved to
            this.saveMatrixToFile(matrixUid);  // perform save like normal

            return 1;  // file was successfully saved
        }

        return 0;  // 0 means the filename was not present, so the data could not be saved

    }

    public HashMap<Integer, DataHandler> getMatrices() {
        return matrices;
    }

    public HashMap<Integer, String> getMatrixSaveNames() {
        return matrixSaveNames;
    }

    public String getMatrixSaveFile(int matrixUid) {
        return matrixSaveNames.get(matrixUid);
    }

    public void setMatrixSaveFile(int matrixUid, String newFile) {
        matrixSaveNames.put(matrixUid, newFile);
    }

    public boolean isMatrixSaved(int matrixUid) {
        return !matrices.get(matrixUid).getWasModified();
    }

    public DataHandler readFile(String fileName) {
        DataHandler matrix = new DataHandler();
        // TODO: add actual reading of file
        return matrix;
    }

}
