package Matrices.IOHandlers;

import Matrices.Data.AsymmetricDSMData;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.MatrixHelpers;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * Tests the AsymmetricIOHandler class.
 */
public class AsymmetricIOHandlerTest {

    /**
     * Creates an AsymmetricDSMData matrix with some data.
     * @return An AsymmetricDSMData matrix with some data.
     */
    public AsymmetricDSMData createMatrix() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();

        Grouping rowDefault = matrix.getDefaultGroup(true);
        Grouping g1 = new Grouping("g1", Color.BLUE);
        Grouping g2 = new Grouping("g2", Color.BLUE);
        Grouping colDefault = matrix.getDefaultGroup(false);
        Grouping g3 = new Grouping("g1", Color.RED);
        Grouping g4 = new Grouping("g2", Color.RED);


        // add row items - default group
        matrix.addItem(new DSMItem(1, null, 1.0, "item1", rowDefault, null), true);
        matrix.addItem(new DSMItem(2, null, 1.0, "item2", rowDefault, null), true);

        // add row items - g1
        matrix.addItem(new DSMItem(3, null, 1.0, "item3", g1, null), true);
        matrix.addItem(new DSMItem(4, null, 1.0, "item4", g1, null), true);

        // add row items - g2
        matrix.addItem(new DSMItem(5, null, 1.0, "item5", g2, null), true);
        matrix.addItem(new DSMItem(6, null, 1.0, "item6", g2, null), true);

        // add col items - default group
        matrix.addItem(new DSMItem(7, null, 1.0, "item7", colDefault, null), false);
        matrix.addItem(new DSMItem(8, null, 1.0, "item8", colDefault, null), false);

        // add col items - g3
        matrix.addItem(new DSMItem(9, null, 1.0, "item9", g3, null), false);
        matrix.addItem(new DSMItem(10, null, 1.0, "item10", g2, null), false);

        // add col items - g4
        matrix.addItem(new DSMItem(11, null, 1.0, "item11", g4, null), false);
        matrix.addItem(new DSMItem(12, null, 1.0, "item12", g4, null), false);

        // add connections
        matrix.modifyConnection(1, 7, "x", 1, null);
        matrix.modifyConnection(1, 8, "x", 2, null);
        matrix.modifyConnection(3, 12, "x", 3, null);
        matrix.modifyConnection(4, 10, "x", 4, null);
        matrix.modifyConnection(5, 9, "x", 5, null);

        return matrix;
    }


    /**
     * Tests the ability to write an AsymmetricDSMData matrix to an adjacency matrix and read it back.
     */
    @Test
    public void testWriteAndReadAsymmetricAdjacencyMatrix() {
        // Step 1: Create a SymmetricDSMData matrix and populate it with some data
        AsymmetricDSMData originalMatrix = createMatrix();

        // Step 2: Create an instance of SymmetricIOHandler and use it to write the matrix to a file
        AsymmetricIOHandler ioHandler = new AsymmetricIOHandler(null);
        ioHandler.setMatrix(originalMatrix);
        File file = new File("testMatrix.csv");
        ioHandler.exportMatrixToAdjacencyMatrix(file);

        // Step 3: Use the same SymmetricIOHandler instance to read the matrix back from the file
        AsymmetricDSMData readMatrix = ioHandler.importAdjacencyMatrix(file);

        // Step 4: Compare the original matrix and the read matrix to ensure they are the same
        MatrixHelpers.assertAsymmetricMatricesEqual(originalMatrix, readMatrix);

        // Step 5: Clean up
        file.delete();
    }

    @Test
    public void testWriteAndReadAsymmetricMatrix() {
        // Step 1: Create a AsymmetricDSMData matrix and populate it with some data
        AsymmetricDSMData originalMatrix = createMatrix();

        // Step 2: Create an instance of SymmetricIOHandler and use it to write the matrix to a file
        File file = new File("testMatrix.dsm");
        AsymmetricIOHandler ioHandler = new AsymmetricIOHandler(file);
        ioHandler.setMatrix(originalMatrix);
        ioHandler.saveMatrixToFile(file);

        // Step 3: Use the same SymmetricIOHandler instance to read the matrix back from the file
        AsymmetricDSMData readMatrix = ioHandler.readFile();

        // Step 4: Compare the original matrix and the read matrix to ensure they are the same
        MatrixHelpers.assertAsymmetricMatricesEqual(originalMatrix, readMatrix);

        // Step 5: Clean up
        file.delete();
    }

}
