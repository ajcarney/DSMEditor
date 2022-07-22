package Matrices.Data;

import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


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
    private static void stressUndoRedo(AbstractDSMData matrix) {
        for(int i=0; i<NUM_UNDO_REDO_CYCLES; i++) {
            matrix.undoToCheckpoint();
            matrix.redoToCheckpoint();
        }
    }


    /**
     * Unit test for creating a copy of an asymmetric dsm. Test to ensure changes to the copy
     * do not change the original
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
     * Unit test for creating an item in a dsm. Test to ensure the undo-redo works with creating a new item
     */
    @Test
    public void createItemTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();

        matrix.setCurrentStateAsCheckpoint();
        matrix.createItem("item1", true);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals("item1", matrix.getRows().get(0).getName().getValue());
    }


    /**
     * Unit test for setting an item's group in the dsm to a group that is already in the dsm
     */
    @Test
    public void setItemGroupTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        Grouping startGroup = new Grouping("group1", null);
        Grouping endGroup = new Grouping("group2", null);
        matrix.addGrouping(startGroup);
        matrix.addGrouping(endGroup);
        DSMItem rowItem = new DSMItem(1, null, 1.0, "item1", startGroup, null);
        matrix.addItem(rowItem, true);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemGroup(rowItem, endGroup);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(endGroup, rowItem.getGroup1());
        Assertions.assertEquals(2, matrix.getGroupings().size());
    }


    /**
     * Unit test for setting an item's group in the dsm to a group that is not already in the dsm
     */
    @Test
    public void setItemToNewGroupTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        Grouping startGroup = new Grouping("group1", null);
        Grouping endGroup = new Grouping("group2", null);
        matrix.addGrouping(startGroup);
        DSMItem rowItem = new DSMItem(1, null, 1.0, "item1", startGroup, null);
        matrix.addItem(rowItem, true);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemGroup(rowItem, endGroup);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(endGroup, rowItem.getGroup1());
        Assertions.assertEquals(2, matrix.getGroupings().size());
    }

}