package Matrices.Data;

import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
        matrix.modifyConnection(matrix.getRows().get(0).getUid(), matrix.getCols().get(0).getUid(), "x", 1.0, new ArrayList<>());
        matrix.setTitle("title");
        matrix.setProjectName("project");
        matrix.setCustomer("customer");
        matrix.setVersionNumber("version");
        Grouping group = new Grouping("group", null);
        matrix.addGrouping(true, group);

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
        Assertions.assertTrue(copy.getGroupings(true).stream().map(Grouping::getName).collect(Collectors.toCollection(ArrayList::new)).contains("group"));
    }


    /**
     * Tests adding a new grouping. Test to ensure the undo-redo works
     */
    @Test
    public void addGroupingTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        Grouping group = new Grouping("group", null);

        matrix.setCurrentStateAsCheckpoint();
        matrix.addGrouping(true, group);

        stressUndoRedo(matrix);

        Assertions.assertTrue(matrix.getGroupings(true).contains(group));
    }


    /**
     * Tests removing a grouping. Test to ensure the undo-redo works
     */
    @Test
    public void removeGroupingTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        Grouping group = new Grouping("group", null);
        matrix.addGrouping(true, group);

        matrix.setCurrentStateAsCheckpoint();
        matrix.removeGrouping(true, group);

        stressUndoRedo(matrix);

        Assertions.assertFalse(matrix.getGroupings(true).contains(group));
    }


    /**
     * Tests removing all groupings for a given matrix. Checks that items created with that grouping
     * do not have that grouping anymore. Test to ensure the undo-redo works
     */
    @Test
    public void clearGroupingsTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        Grouping group1 = new Grouping("group1", null);
        Grouping group2 = new Grouping("group2", null);
        Grouping group3 = new Grouping("group3", null);
        matrix.addGrouping(true, group1);
        matrix.addGrouping(true, group2);
        matrix.addGrouping(true, group3);

        DSMItem rowItem = new DSMItem(1, null, 1.0, "item1", group1, null);
        matrix.addItem(rowItem, true);

        matrix.setCurrentStateAsCheckpoint();
        matrix.clearGroupings(true);

        stressUndoRedo(matrix);

        Assertions.assertFalse(matrix.getGroupings(true).contains(group1));
        Assertions.assertFalse(matrix.getGroupings(true).contains(group2));
        Assertions.assertFalse(matrix.getGroupings(true).contains(group3));
        Assertions.assertNotEquals(rowItem.getGroup1(), group1);
    }


    /**
     * Tests renaming a grouping. Test to ensure the undo-redo works
     */
    @Test
    public void renameGroupingTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        Grouping group = new Grouping("group", null);
        matrix.addGrouping(true, group);

        matrix.setCurrentStateAsCheckpoint();
        matrix.renameGrouping(group, "new group");

        stressUndoRedo(matrix);

        Assertions.assertEquals("new group", group.getName());
    }


    /**
     * Tests changing a groupings color. Test to ensure the undo-redo works
     */
    @Test
    public void updateGroupingColorTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        Grouping group = new Grouping("group", Color.color(1.0, 1.0, 1.0));
        matrix.addGrouping(true, group);

        matrix.setCurrentStateAsCheckpoint();
        matrix.updateGroupingColor(group, Color.color(0.0, 0.0, 0.0));

        stressUndoRedo(matrix);

        Assertions.assertEquals(Color.color(0.0, 0.0, 0.0), group.getColor());
    }


    /**
     * Tests changing a groupings font color. Test to ensure the undo-redo works
     */
    @Test
    public void updateGroupingFontColorTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        Grouping group = new Grouping("group", Color.color(1.0, 1.0, 1.0));
        matrix.addGrouping(true, group);

        matrix.setCurrentStateAsCheckpoint();
        matrix.updateGroupingFontColor(group, Color.color(0.0, 0.0, 0.0));

        stressUndoRedo(matrix);

        Assertions.assertEquals(Color.color(0.0, 0.0, 0.0), group.getFontColor());
    }


    /**
     * Unit test for setting an item's group in the dsm to a group that is already in the dsm
     */
    @Test
    public void setItemGroupTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        Grouping startGroup = new Grouping("group1", null);
        Grouping endGroup = new Grouping("group2", null);
        matrix.addGrouping(true, startGroup);
        matrix.addGrouping(true, endGroup);
        DSMItem rowItem = new DSMItem(1, null, 1.0, "item1", startGroup, null);
        matrix.addItem(rowItem, true);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemGroup(rowItem, endGroup);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(endGroup, rowItem.getGroup1());
        Assertions.assertEquals(3, matrix.getGroupings(true).size());  // use 3 to account for default
    }


    /**
     * Unit test for setting an item's group in the dsm to a group that is not already in the dsm
     */
    @Test
    public void setItemToNewGroupTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        Grouping startGroup = new Grouping("group1", null);
        Grouping endGroup = new Grouping("group2", null);
        matrix.addGrouping(true, startGroup);
        DSMItem rowItem = new DSMItem(1, null, 1.0, "item1", startGroup, null);
        matrix.addItem(rowItem, true);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemGroup(rowItem, endGroup);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(endGroup, rowItem.getGroup1());
        Assertions.assertEquals(3, matrix.getGroupings(true).size());  // use 3 to account for default
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

}