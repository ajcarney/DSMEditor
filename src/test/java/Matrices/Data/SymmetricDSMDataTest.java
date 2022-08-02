package Matrices.Data;

import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * Test class for the SymmetricDSMData class. Mostly contains tests
 * for stressing the undo and redo functionality.
 */
public class SymmetricDSMDataTest {

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
     * Unit test for creating a copy of a symmetric dsm. Test to ensure changes to the copy
     * do not change the original
     */
    @Test
    public void createCopyTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.createItem("item1", true);
        matrix.createItem("item2", true);
        matrix.modifyConnection(matrix.getRows().get(0).getUid(), matrix.getRows().get(1).getAliasUid(), "x", 1.0, new ArrayList<>());
        matrix.setTitle("title");
        matrix.setProjectName("project");
        matrix.setCustomer("customer");
        matrix.setVersionNumber("version");
        Grouping group = new Grouping("group", null);
        matrix.addGrouping(group);

        SymmetricDSMData copy = matrix.createCopy();

        // perform the changes
        matrix.getRows().get(0).setName("-");
        matrix.getRows().get(1).setName("-");
        matrix.getConnections().get(0).setConnectionName("-");
        matrix.setTitle("-");
        matrix.setProjectName("-");
        matrix.setCustomer("-");
        matrix.setVersionNumber("-");
        group.setName("-");

        Assertions.assertEquals("item1", copy.getRows().get(0).getName().getValue());
        Assertions.assertEquals("item2", copy.getRows().get(1).getName().getValue());
        Assertions.assertEquals("x", copy.getConnections().get(0).getConnectionName());
        Assertions.assertEquals("title", copy.getTitle());
        Assertions.assertEquals("project", copy.getProjectName());
        Assertions.assertEquals("customer", copy.getCustomer());
        Assertions.assertEquals("version", copy.getVersionNumber());
        Assertions.assertTrue(copy.getGroupings().stream().map(Grouping::getName).collect(Collectors.toCollection(ArrayList::new)).contains("group"));

    }


    /**
     * Tests adding a new grouping. Test to ensure the undo-redo works
     */
    @Test
    public void addGroupingTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        Grouping group = new Grouping("group", null);

        matrix.setCurrentStateAsCheckpoint();
        matrix.addGrouping(group);

        stressUndoRedo(matrix);

        Assertions.assertTrue(matrix.getGroupings().contains(group));
    }


    /**
     * Tests removing a grouping. Test to ensure the undo-redo works
     */
    @Test
    public void removeGroupingTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        Grouping group = new Grouping("group", null);
        matrix.addGrouping(group);

        matrix.setCurrentStateAsCheckpoint();
        matrix.removeGrouping(group);

        stressUndoRedo(matrix);

        Assertions.assertFalse(matrix.getGroupings().contains(group));
    }


    /**
     * Tests removing all groupings for a given matrix. Checks that items created with that grouping
     * do not have that grouping anymore. Test to ensure the undo-redo works
     */
    @Test
    public void clearGroupingsTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        Grouping group1 = new Grouping("group1", null);
        Grouping group2 = new Grouping("group2", null);
        Grouping group3 = new Grouping("group3", null);
        matrix.addGrouping(group1);
        matrix.addGrouping(group2);
        matrix.addGrouping(group3);

        DSMItem rowItem = new DSMItem(1, 11, 1.0, "item1", group1, null);
        DSMItem colItem = new DSMItem(11, 1, 1.0, "item1", group1, null);
        matrix.addItem(rowItem, true);
        matrix.addItem(colItem, false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.clearGroupings();

        stressUndoRedo(matrix);

        Assertions.assertFalse(matrix.getGroupings().contains(group1));
        Assertions.assertFalse(matrix.getGroupings().contains(group2));
        Assertions.assertFalse(matrix.getGroupings().contains(group3));
        Assertions.assertNotEquals(rowItem.getGroup1(), group1);
    }


    /**
     * Tests changing a groupings color. Test to ensure the undo-redo works
     */
    @Test
    public void renameGroupingTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        Grouping group = new Grouping("group", null);
        matrix.addGrouping(group);

        matrix.setCurrentStateAsCheckpoint();
        matrix.renameGrouping(group, "new group");

        stressUndoRedo(matrix);

        Assertions.assertEquals("new group", group.getName());
    }


    /**
     * Tests changing a groupings font color. Test to ensure the undo-redo works
     */
    @Test
    public void updateGroupingColorTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        Grouping group = new Grouping("group", Color.color(1.0, 1.0, 1.0));
        matrix.addGrouping(group);

        matrix.setCurrentStateAsCheckpoint();
        matrix.updateGroupingColor(group, Color.color(0.0, 0.0, 0.0));

        stressUndoRedo(matrix);

        Assertions.assertEquals(Color.color(0.0, 0.0, 0.0), group.getColor());
    }


    /**
     * Unit test for setting an item's group in the dsm to a group that is already in the dsm
     */
    @Test
    public void updateGroupingFontColorTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        Grouping group = new Grouping("group", Color.color(1.0, 1.0, 1.0));
        matrix.addGrouping(group);

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
        SymmetricDSMData matrix = new SymmetricDSMData();
        Grouping startGroup = new Grouping("group1", null);
        Grouping endGroup = new Grouping("group2", null);
        matrix.addGrouping(startGroup);
        matrix.addGrouping(endGroup);
        DSMItem rowItem = new DSMItem(1, 11, 1.0, "item1", startGroup, null);
        DSMItem colItem = new DSMItem(11, 1, 1.0, "item1", startGroup, null);
        matrix.addItem(rowItem, true);
        matrix.addItem(colItem, false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemGroup(rowItem, endGroup);

        stressUndoRedo(matrix);

        Assertions.assertEquals(endGroup, rowItem.getGroup1());
        Assertions.assertEquals(endGroup, colItem.getGroup1());
        Assertions.assertEquals(3, matrix.getGroupings().size());  // use 3 to account for default
    }


    /**
     * Unit test for setting an item's group in the dsm to a group that is not already in the dsm
     */
    @Test
    public void setItemToNewGroupTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        Grouping startGroup = new Grouping("group1", null);
        Grouping endGroup = new Grouping("group2", null);
        matrix.addGrouping(startGroup);
        DSMItem rowItem = new DSMItem(1, 11, 1.0, "item1", startGroup, null);
        DSMItem colItem = new DSMItem(11, 1, 1.0, "item1", startGroup, null);
        matrix.addItem(rowItem, true);
        matrix.addItem(colItem, false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemGroup(rowItem, endGroup);

        stressUndoRedo(matrix);

        Assertions.assertEquals(endGroup, rowItem.getGroup1());
        Assertions.assertEquals(endGroup, colItem.getGroup1());
        Assertions.assertEquals(3, matrix.getGroupings().size());  // use 3 to account for default
    }


    /**
     * Unit test for creating an item in a dsm. Test to ensure the undo-redo works with creating a new item
     */
    @Test
    public void createItemTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();

        matrix.setCurrentStateAsCheckpoint();
        matrix.createItem("item1", true);

        stressUndoRedo(matrix);

        Assertions.assertEquals("item1", matrix.getRows().get(0).getName().getValue());
        Assertions.assertEquals("item1", matrix.getCols().get(0).getName().getValue());
    }


    /**
     * Unit test for deleting an item in a dsm. Test to ensure the undo-redo works with deleting a new item
     */
    @Test
    public void deleteItemTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        DSMItem rowItem = new DSMItem(1, 11, 1.0, "item1", null, null);
        DSMItem colItem = new DSMItem(11, 1, 1.0, "item1", null, null);
        matrix.addItem(rowItem, true);
        matrix.addItem(colItem, false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.deleteItem(colItem);

        stressUndoRedo(matrix);

        Assertions.assertEquals(0, matrix.getRows().size());
        Assertions.assertEquals(0, matrix.getCols().size());
    }


    /**
     * Unit test for setting an item's name in the dsm
     */
    @Test
    public void setItemNameTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        DSMItem rowItem = new DSMItem(1, 11, 1.0, "item1", null, null);
        DSMItem colItem = new DSMItem(11, 1, 1.0, "item1", null, null);
        matrix.addItem(rowItem, true);
        matrix.addItem(colItem, false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemName(rowItem, "newItem1");

        stressUndoRedo(matrix);

        Assertions.assertEquals("newItem1", rowItem.getName().getValue());
        Assertions.assertEquals("newItem1", colItem.getName().getValue());
    }


    /**
     * Unit test for setting an item's sort index in the dsm
     */
    @Test
    public void setItemSortIndexTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        DSMItem rowItem = new DSMItem(1, 11, 1.0, "item1", null, null);
        DSMItem colItem = new DSMItem(11, 1, 1.0, "item1", null, null);
        matrix.addItem(rowItem, true);
        matrix.addItem(colItem, false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemSortIndex(rowItem, 42.0);

        stressUndoRedo(matrix);

        Assertions.assertEquals(42.0, rowItem.getSortIndex());
        Assertions.assertEquals(42.0, colItem.getSortIndex());
    }


    /**
     * tests creating a connection for a matrix
     */
    @Test
    public void createConnectionTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.createItem("item1", true);
        matrix.createItem("item2", true);

        matrix.setCurrentStateAsCheckpoint();
        matrix.modifyConnection(matrix.getRows().get(0).getUid(), matrix.getCols().get(1).getUid(), "x", 1.0, new ArrayList<>());

        stressUndoRedo(matrix);

        Assertions.assertEquals(1, matrix.getConnections().size());
        Assertions.assertEquals("x", matrix.getConnections().get(0).getConnectionName());
        Assertions.assertEquals(1.0, matrix.getConnections().get(0).getWeight());
    }


    /**
     * Unit test for creating a symmetric connection in the dsm. Assert it creates the connection and its symmetric
     * pair correctly, and that the undo-redo functionality works for it
     */
    @Test
    public void modifyConnectionSymmetricTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.addItem(new DSMItem(1, 11, 1.0, "item1", null, null), true);
        matrix.addItem(new DSMItem(2, 22, 1.0, "item2", null, null), true);
        matrix.addItem(new DSMItem(3, 33, 1.0, "item3", null, null), true);
        matrix.addItem(new DSMItem(11, 1, 1.0, "item1", null, null), false);
        matrix.addItem(new DSMItem(22, 2, 1.0, "item2", null, null), false);
        matrix.addItem(new DSMItem(33, 3, 1.0, "item3", null, null), false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.modifyConnectionSymmetric(1, 33, "x", 1.0, new ArrayList<>());

        stressUndoRedo(matrix);

        ArrayList<Integer> expectedRowUids = new ArrayList<>(Arrays.asList(1, 3));
        ArrayList<Integer> expectedColUids = new ArrayList<>(Arrays.asList(11, 33));
        ArrayList<Integer> actualRowUids = matrix.getConnections().stream().map(DSMConnection::getRowUid).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Integer> actualColUids = matrix.getConnections().stream().map(DSMConnection::getColUid).collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(expectedRowUids);
        Collections.sort(expectedColUids);
        Collections.sort(actualRowUids);
        Collections.sort(actualColUids);

        Assertions.assertEquals(2, matrix.getConnections().size());  // make sure connections stayed the same
        Assertions.assertIterableEquals(expectedRowUids, actualRowUids);
        Assertions.assertIterableEquals(expectedColUids, actualColUids);
    }


    /**
     * Unit test for redistributing sort indices by group
     */
    @Test
    public void reDistributeSortIndicesByGroupTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        Grouping group1 = new Grouping("group1", null);
        Grouping group2 = new Grouping("group2", null);

        // add a bunch of items
        matrix.addItem(new DSMItem(1, 11, 1.0, "item1", group1, null), true);
        matrix.addItem(new DSMItem(2, 22, 1.0, "item2", group1, null), true);
        matrix.addItem(new DSMItem(3, 33, 1.0, "item3", group1, null), true);
        matrix.addItem(new DSMItem(4, 44, 1.0, "item4", group1, null), true);
        matrix.addItem(new DSMItem(5, 55, 1.0, "item5", group2, null), true);
        matrix.addItem(new DSMItem(6, 66, 1.0, "item6", group2, null), true);
        matrix.addItem(new DSMItem(7, 77, 1.0, "item7", group2, null), true);
        matrix.addItem(new DSMItem(8, 88, 1.0, "item8", group2, null), true);
        matrix.addItem(new DSMItem(9, 99, 1.0, "item9", group2, null), true);
        matrix.addItem(new DSMItem(11, 1, 1.0, "item1", group1, null), false);
        matrix.addItem(new DSMItem(22, 2, 1.0, "item2", group1, null), false);
        matrix.addItem(new DSMItem(33, 3, 1.0, "item3", group1, null), false);
        matrix.addItem(new DSMItem(44, 4, 1.0, "item4", group1, null), false);
        matrix.addItem(new DSMItem(55, 5, 1.0, "item5", group2, null), false);
        matrix.addItem(new DSMItem(66, 6, 1.0, "item6", group2, null), false);
        matrix.addItem(new DSMItem(77, 7, 1.0, "item7", group2, null), false);
        matrix.addItem(new DSMItem(88, 8, 1.0, "item8", group2, null), false);
        matrix.addItem(new DSMItem(99, 9, 1.0, "item9", group2, null), false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.reDistributeSortIndicesByGroup();

        ArrayList<Double> expectedRowIndices = new ArrayList<>(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));
        ArrayList<Double> expectedColIndices = new ArrayList<>(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0));
        ArrayList<Double> actualRowIndices = matrix.getRows().stream().map(DSMItem::getSortIndex).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Double> actualColIndices = matrix.getCols().stream().map(DSMItem::getSortIndex).collect(Collectors.toCollection(ArrayList::new));
        Collections.sort(expectedRowIndices);
        Collections.sort(expectedColIndices);
        Collections.sort(actualRowIndices);
        Collections.sort(actualColIndices);

        Assertions.assertIterableEquals(expectedRowIndices, actualRowIndices);
        Assertions.assertIterableEquals(expectedColIndices, actualColIndices);
    }


    /**
     * Unit test for getting a connection's symmetric pair's row and column uids
     */
    @Test
    public void getSymmetricConnectionUidsTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.addItem(new DSMItem(1, 11, 1.0, "item1", null, null), true);
        matrix.addItem(new DSMItem(2, 22, 1.0, "item2", null, null), true);
        matrix.addItem(new DSMItem(3, 33, 1.0, "item3", null, null), true);
        matrix.addItem(new DSMItem(11, 1, 1.0, "item1", null, null), false);
        matrix.addItem(new DSMItem(22, 2, 1.0, "item2", null, null), false);
        matrix.addItem(new DSMItem(33, 3, 1.0, "item3", null, null), false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.modifyConnectionSymmetric(1, 33, "x", 1.0, new ArrayList<>());

        stressUndoRedo(matrix);

        int expectedSymmetricRowUid = 3;
        int expectedSymmetricColUid = 11;
        Assertions.assertEquals(expectedSymmetricRowUid, matrix.getSymmetricConnectionUids(1, 33).getKey());
        Assertions.assertEquals(expectedSymmetricColUid, matrix.getSymmetricConnectionUids(1, 33).getValue());
    }


    /**
     * Unit test for getting a connection's symmetric pair
     */
    @Test
    public void getSymmetricConnectionTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.addItem(new DSMItem(1, 11, 1.0, "item1", null, null), true);
        matrix.addItem(new DSMItem(2, 22, 1.0, "item2", null, null), true);
        matrix.addItem(new DSMItem(3, 33, 1.0, "item3", null, null), true);
        matrix.addItem(new DSMItem(11, 1, 1.0, "item1", null, null), false);
        matrix.addItem(new DSMItem(22, 2, 1.0, "item2", null, null), false);
        matrix.addItem(new DSMItem(33, 3, 1.0, "item3", null, null), false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.modifyConnectionSymmetric(1, 33, "x", 1.0, new ArrayList<>());

        stressUndoRedo(matrix);

        Assertions.assertEquals("x", matrix.getSymmetricConnection(1, 33).getConnectionName());
    }

}
