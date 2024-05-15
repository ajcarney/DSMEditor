package Matrices.IOHandlers;

import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.SymmetricDSMData;
import Matrices.MatrixHelpers;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;


/**
 * Tests the SymmetricIOHandler class.
 */
public class SymmetricIOHandlerTest {

    /**
     * Creates a SymmetricDSMData matrix with some data.
     * @return A SymmetricDSMData matrix with some data.
     */
    public SymmetricDSMData createMatrix() {
        SymmetricDSMData matrix = new SymmetricDSMData();

        // create groupings
        Grouping defaultGroup = matrix.getDefaultGroup();
        Grouping g1 = new Grouping("g1", Color.BLUE);
        Grouping g2 = new Grouping("g2", Color.BLUE);
        matrix.addGrouping(g1);
        matrix.addGrouping(g2);

        // create items - default group
        matrix.addItem(new DSMItem(1, 2, 1.0, "item1", defaultGroup, null), true);
        matrix.addItem(new DSMItem(2, 1, 1.0, "item1", defaultGroup, null), false);
        matrix.addItem(new DSMItem(3, 4, 1.0, "item2", defaultGroup, null), true);
        matrix.addItem(new DSMItem(4, 3, 1.0, "item2", defaultGroup, null), false);

        // create items - g1
        matrix.addItem(new DSMItem(5, 6, 1.0, "item3", g1, null), true);
        matrix.addItem(new DSMItem(6, 5, 1.0, "item3", g1, null), false);
        matrix.addItem(new DSMItem(7, 8, 1.0, "item4", g1, null), true);
        matrix.addItem(new DSMItem(8, 7, 1.0, "item4", g1, null), false);

        // create items - g3
        matrix.addItem(new DSMItem(9, 10, 1.0, "item5", g2, null), true);
        matrix.addItem(new DSMItem(10, 9, 1.0, "item5", g2, null), false);
        matrix.addItem(new DSMItem(11, 12, 1.0, "item6", g2, null), true);
        matrix.addItem(new DSMItem(12, 11, 1.0, "item6", g2, null), false);

        // add connections
        matrix.modifyConnection(1, 6, "x", 1, new ArrayList<>());
        matrix.modifyConnection(1, 8, "x", 2, new ArrayList<>());
        matrix.modifyConnection(3, 12, "x", 3, new ArrayList<>());
        matrix.modifyConnection(5, 10, "x", 4, new ArrayList<>());
        matrix.modifyConnection(7, 2, "x", 5, new ArrayList<>());
        matrix.modifyConnection(9, 4, "x", 5, new ArrayList<>());
        matrix.modifyConnection(11, 10, "x", 5, new ArrayList<>());

        return matrix;

    }

    /**
     * Tests the ability to write a SymmetricDSMData matrix to an adjacency matrix and read it back.
     */
    @Test
    public void testWriteAndReadSymmetricAdjacencyMatrix() {
        // Step 1: Create a SymmetricDSMData matrix and populate it with some data
        SymmetricDSMData originalMatrix = createMatrix();

        // Step 2: Create an instance of SymmetricIOHandler and use it to write the matrix to a file
        SymmetricIOHandler ioHandler = new SymmetricIOHandler(null);
        ioHandler.setMatrix(originalMatrix);
        File file = new File("testMatrix.csv");
        ioHandler.exportMatrixToAdjacencyMatrix(file);

        // Step 3: Use the same SymmetricIOHandler instance to read the matrix back from the file
        SymmetricDSMData readMatrix = ioHandler.importAdjacencyMatrix(file);

        // Step 4: Compare the original matrix and the read matrix to ensure they are the same
        MatrixHelpers.assertSymmetricMatricesEqual(originalMatrix, readMatrix, false);

        // Step 5: Clean up
        file.delete();
    }


    /**
     * Tests the ability to write a SymmetricDSMData matrix to a Thebeau Matlab file and read it back.
     */
    @Test
    public void testWriteAndReadMatlab() {
        // Step 1: Create a SymmetricDSMData matrix and populate it with some data
        SymmetricDSMData originalMatrix = createMatrix();

        // Step 2: Create an instance of SymmetricIOHandler and use it to write the matrix to a file
        SymmetricIOHandler ioHandler = new SymmetricIOHandler(null);
        ioHandler.setMatrix(originalMatrix);
        File file = new File("testMatrix.m");
        ioHandler.exportMatrixToThebeauMatlabFile(file);

        // Step 3: Use the same SymmetricIOHandler instance to read the matrix back from the file
        SymmetricDSMData readMatrix = ioHandler.importThebeauMatlabFile(file);

        // Step 4: Compare the original matrix and the read matrix to ensure they are the same
        MatrixHelpers.assertSymmetricMatricesEqual(originalMatrix, readMatrix, true);

        // Step 5: Clean up
        file.delete();
    }


    /**
     * Tests the ability to write a SymmetricDSMData matrix to a file and read it back.
     */
    @Test
    public void testWriteAndRead() {
        // Step 1: Create a SymmetricDSMData matrix and populate it with some data
        SymmetricDSMData originalMatrix = createMatrix();

        // Step 2: Create an instance of SymmetricIOHandler and use it to write the matrix to a file
        File file = new File("testMatrix.dsm");
        SymmetricIOHandler ioHandler = new SymmetricIOHandler(file);
        ioHandler.setMatrix(originalMatrix);
        ioHandler.saveMatrixToFile(file);

        // Step 3: Use the same SymmetricIOHandler instance to read the matrix back from the file
        SymmetricDSMData readMatrix = ioHandler.readFile();

        // Step 4: Compare the original matrix and the read matrix to ensure they are the same
        MatrixHelpers.assertSymmetricMatricesEqual(originalMatrix, readMatrix, false);

        // Step 5: Clean up
        file.delete();
    }
}
