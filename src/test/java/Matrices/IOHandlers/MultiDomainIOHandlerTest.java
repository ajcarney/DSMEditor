package Matrices.IOHandlers;

import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.MultiDomainDSMData;
import Matrices.MatrixHelpers;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;

/**
 * Tests the MultiDomainIOHandler class.
 */
public class MultiDomainIOHandlerTest {

    /**
     * Creates a MultiDomainDSMData matrix with two domains and some data.
     * @return the matrix
     */
    private MultiDomainDSMData createMatrix() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();

        // add domains
        Grouping domain1 = matrix.getDefaultDomain();
        Grouping domain2 = new Grouping("domain2", Color.BLUE);
        matrix.addDomain(domain2);

        // create domain groupings
        Grouping default1 = matrix.getDefaultDomainGroup(domain1);
        Grouping g1 = new Grouping("g1", Color.BLUE);
        Grouping g2 = new Grouping("g2", Color.BLUE);
        Grouping default2 = matrix.getDefaultDomainGroup(domain2);
        Grouping g3 = new Grouping("g3", Color.BLUE);
        Grouping g4 = new Grouping("g4", Color.BLUE);
        matrix.addDomainGrouping(domain1, g1);
        matrix.addDomainGrouping(domain1, g2);
        matrix.addDomainGrouping(domain2, g3);
        matrix.addDomainGrouping(domain2, g4);

        // create items
        // domain 1 - default group
        matrix.addItem(new DSMItem(1, 2, 1.0, "item1", default1, domain1), true);
        matrix.addItem(new DSMItem(2, 1, 1.0, "item1", default1, domain1), false);
        matrix.addItem(new DSMItem(3, 4, 1.0, "item2", default1, domain1), true);
        matrix.addItem(new DSMItem(4, 3, 1.0, "item2", default1, domain1), false);

        // domain 1 - g1
        matrix.addItem(new DSMItem(5, 6, 1.0, "item3", g1, domain1), true);
        matrix.addItem(new DSMItem(6, 5, 1.0, "item3", g1, domain1), false);
        matrix.addItem(new DSMItem(7, 8, 1.0, "item4", g1, domain1), true);
        matrix.addItem(new DSMItem(8, 7, 1.0, "item4", g1, domain1), false);

        // domain 1 - g2
        matrix.addItem(new DSMItem(9, 10, 1.0, "item5", g2, domain1), true);
        matrix.addItem(new DSMItem(10, 9, 1.0, "item5", g2, domain1), false);
        matrix.addItem(new DSMItem(11, 12, 1.0, "item6", g2, domain1), true);
        matrix.addItem(new DSMItem(12, 11, 1.0, "item6", g2, domain1), false);

        // domain 2 - default group
        matrix.addItem(new DSMItem(13, 14, 1.0, "item7", default2, domain2), true);
        matrix.addItem(new DSMItem(14, 13, 1.0, "item7", default2, domain2), false);
        matrix.addItem(new DSMItem(15, 16, 1.0, "item8", default2, domain2), true);
        matrix.addItem(new DSMItem(16, 15, 1.0, "item8", default2, domain2), false);

        // domain 2 - g3
        matrix.addItem(new DSMItem(17, 18, 1.0, "item9", g3, domain2), true);
        matrix.addItem(new DSMItem(18, 17, 1.0, "item9", g3, domain2), false);
        matrix.addItem(new DSMItem(19, 20, 1.0, "item10", g3, domain2), true);
        matrix.addItem(new DSMItem(20, 19, 1.0, "item10", g3, domain2), false);

        // domain 2 - g4
        matrix.addItem(new DSMItem(21, 22, 1.0, "item11", g4, domain2), true);
        matrix.addItem(new DSMItem(22, 21, 1.0, "item11", g4, domain2), false);
        matrix.addItem(new DSMItem(23, 24, 1.0, "item12", g4, domain2), true);
        matrix.addItem(new DSMItem(24, 23, 1.0, "item12", g4, domain2), false);


        // add connections. Only use x for connection name because this is default import value
        matrix.modifyConnection(1, 6, "x", 1, new ArrayList<>());
        matrix.modifyConnection(1, 8, "x", 2, new ArrayList<>());
        matrix.modifyConnection(3, 12, "x", 3, new ArrayList<>());
        matrix.modifyConnection(5, 10, "x", 4, new ArrayList<>());
        matrix.modifyConnection(9, 16, "x", 5, new ArrayList<>());
        matrix.modifyConnection(13, 20, "x", 4, new ArrayList<>());
        matrix.modifyConnection(13, 2, "x", 5, new ArrayList<>());
        matrix.modifyConnection(15, 24, "x", 4, new ArrayList<>());
        matrix.modifyConnection(17, 4, "x", 5, new ArrayList<>());
        
        return matrix;
    }

    /**
     * Tests the ability to write a SymmetricDSMData matrix to an adjacency matrix and read it back.
     */
    @Test
    public void testWriteAndReadMultiDomainAdjacencyMatrix() {
        // Step 1: Create a SymmetricDSMData matrix and populate it with some data
        MultiDomainDSMData originalMatrix = createMatrix();

        // Step 2: Create an instance of SymmetricIOHandler and use it to write the matrix to a file
        MultiDomainIOHandler ioHandler = new MultiDomainIOHandler(null);
        ioHandler.setMatrix(originalMatrix);
        File file = new File("testMatrix.csv");
        ioHandler.exportMatrixToAdjacencyMatrix(file);

        // Step 3: Use the same SymmetricIOHandler instance to read the matrix back from the file
        MultiDomainDSMData readMatrix = ioHandler.importAdjacencyMatrix(file);

        // Step 4: Compare the original matrix and the read matrix to ensure they are the same
        MatrixHelpers.assertMultiDomainMatricesEqual(originalMatrix, readMatrix);

        // Step 5: Clean up
        file.delete();
    }
}
