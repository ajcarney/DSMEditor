package Matrices.Data;

import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * Test class for the AsymmetricDSMData class. Mostly contains tests
 * for stressing the undo and redo functionality.
 */
public class AsymmetricDSMDataTest {

    private static final int NUM_UNDO_REDO_CYCLES = 3;

    /**
     * Stress the undo and redo functionality for a given checkpoint. Ends in the same state but after a
     * set number of undo and redo cycles
     *
     * @param matrix  the matrix data object
     */
    private static void stressUndoRedo(SymmetricDSMData matrix) {
        for(int i=0; i<NUM_UNDO_REDO_CYCLES; i++) {
            matrix.undoToCheckpoint();
            matrix.redoToCheckpoint();
        }
    }


    /**
     *
     */
    @Test
    public void createCopyTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        matrix.createItem("row1", true);
        matrix.createItem("col1", false);
        matrix.modifyConnection(matrix.getRows().get(0).getUid(), matrix.getCols().get(0).getUid(), "x", 1.0);
        matrix.setTitle("title");
        matrix.setProjectName("project");
        matrix.setCustomer("customer");
        matrix.setVersionNumber("version");
        Grouping group = new Grouping("group", null);
        matrix.addGrouping(group);

        AsymmetricDSMData copy = matrix.createCopy();

        // perform the changes
        matrix.getRows().get(0).setName("-");
        matrix.getCols().get(0).setName("-");
        matrix.getConnections().get(0).setConnectionName("-");
        matrix.setTitle("-");
        matrix.setProjectName("-");
        matrix.setCustomer("-");
        matrix.setVersionNumber("-");
        group.setName("-");

        Assertions.assertEquals("row1", copy.getRows().get(0).getName().getValue());
        Assertions.assertEquals("col1", copy.getCols().get(0).getName().getValue());
        Assertions.assertEquals("x", copy.getConnections().get(0).getConnectionName());
        Assertions.assertEquals("title", copy.getTitle());
        Assertions.assertEquals("project", copy.getProjectName());
        Assertions.assertEquals("customer", copy.getCustomer());
        Assertions.assertEquals("version", copy.getVersionNumber());
        Assertions.assertEquals("group", copy.getGroupings().stream().findAny().orElse(new Grouping("-", null)).getName());
    }


    /**
     *
     */
    @Test
    public void createItemTest() {

    }


    /**
     *
     */
    @Test
    public void setItemGroupTest() {

    }


    /**
     *
     */
    @Test
    public void setItemToNewGroupTest() {

    }

}