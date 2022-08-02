package Matrices.Data;

import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Test class for the MultiDomainDSM class. Mostly contains tests
 * for stressing the undo and redo functionality.
 */
public class MultiDomainDSMDataTest {

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
     * Unit test for creating a copy of a multi-domain dsm. Test to ensure changes to the copy
     * do not change the original
     */
    @Test
    public void createCopyTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();
        matrix.createItem("item1", true);
        matrix.createItem("item2", true);
        matrix.modifyConnection(matrix.getRows().get(0).getUid(), matrix.getRows().get(1).getAliasUid(), "x", 1.0, new ArrayList<>());
        matrix.setTitle("title");
        matrix.setProjectName("project");
        matrix.setCustomer("customer");
        matrix.setVersionNumber("version");
        Grouping domain = new Grouping("domain", null);
        Grouping domainGroup = new Grouping("group", null);
        matrix.addDomain(domain);
        matrix.addDomainGrouping(domain, domainGroup);

        MultiDomainDSMData copy = matrix.createCopy();

        // perform the changes
        matrix.getRows().get(0).setName("-");
        matrix.getRows().get(1).setName("-");
        matrix.getConnections().get(0).setConnectionName("-");
        matrix.setTitle("-");
        matrix.setProjectName("-");
        matrix.setCustomer("-");
        matrix.setVersionNumber("-");
        domain.setName("-");
        domainGroup.setName("-");

        Grouping domainCopy = copy.getDomains().stream().filter(g -> g.getName().equals("domain")).findFirst().orElse(new Grouping("-", null));

        Assertions.assertEquals("item1", copy.getRows().get(0).getName().getValue());
        Assertions.assertEquals("item2", copy.getRows().get(1).getName().getValue());
        Assertions.assertEquals("x", copy.getConnections().get(0).getConnectionName());
        Assertions.assertEquals("title", copy.getTitle());
        Assertions.assertEquals("project", copy.getProjectName());
        Assertions.assertEquals("customer", copy.getCustomer());
        Assertions.assertEquals("version", copy.getVersionNumber());
        Assertions.assertTrue(copy.getDomains().stream().map(Grouping::getName).toList().contains("domain"));
        Assertions.assertTrue(copy.getDomainGroupings(domainCopy).stream().map(Grouping::getName).toList().contains("group"));
    }


    /**
     * Tests adding a new domain. Test to ensure the undo-redo works
     */
    @Test
    public void addDomainTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();
        Grouping domain = new Grouping("domain", null);

        matrix.setCurrentStateAsCheckpoint();
        matrix.addDomain(domain);

        stressUndoRedo(matrix);

        Assertions.assertTrue(matrix.getDomains().contains(domain));
    }


    /**
     * Tests adding a new domain grouping. Test to ensure the undo-redo works
     */
    @Test
    public void addDomainGroupingTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();
        Grouping domain = new Grouping("domain", null);
        Grouping group = new Grouping("group", null);
        matrix.addDomain(domain);

        matrix.setCurrentStateAsCheckpoint();
        matrix.addDomainGrouping(domain, group);

        stressUndoRedo(matrix);

        Assertions.assertTrue(matrix.getDomainGroupings(domain).contains(group));
    }


    /**
     * Test shifting a domain down in order. Stresses the undo/redo cycle
     */
    @Test
    public void shiftDomainDown() {
        Grouping domain1 = new Grouping(111, 1, "domain1", null, null);
        Grouping domain2 = new Grouping(222, 2, "domain2", null, null);
        Grouping domain3 = new Grouping(333, 3, "domain3", null, null);

        HashMap<Grouping, Collection<Grouping>> domains = new HashMap<>();
        domains.put(domain1, new ArrayList<>());
        domains.put(domain2, new ArrayList<>());
        domains.put(domain3, new ArrayList<>());
        MultiDomainDSMData matrix = new MultiDomainDSMData(domains);  // create domains this way to ensure full control

        matrix.setCurrentStateAsCheckpoint();

        // Test 1  -  shifting to the bottom
        matrix.shiftDomainDown(domain1);
        matrix.shiftDomainDown(domain1);  // shift to the bottom

        Assertions.assertEquals(matrix.getDomains().get(0), domain2);  // assert ordering is 2, 3, 1
        Assertions.assertEquals(matrix.getDomains().get(1), domain3);
        Assertions.assertEquals(matrix.getDomains().get(2), domain1);

        // Test 2  -  shift past bottom (should not do anything)
        matrix.shiftDomainDown(domain1);
        Assertions.assertEquals(matrix.getDomains().get(0), domain2);  // assert ordering is still 2, 3, 1
        Assertions.assertEquals(matrix.getDomains().get(1), domain3);
        Assertions.assertEquals(matrix.getDomains().get(2), domain1);

        stressUndoRedo(matrix);

        Assertions.assertEquals(matrix.getDomains().get(0), domain2);  // assert ordering is still 2, 3, 1
        Assertions.assertEquals(matrix.getDomains().get(1), domain3);
        Assertions.assertEquals(matrix.getDomains().get(2), domain1);
    }


    /**
     * Test shifting a domain down in order. Stresses the undo/redo cycle
     */
    @Test
    public void shiftDomainUp() {
        Grouping domain1 = new Grouping(111, 1, "domain1", null, null);
        Grouping domain2 = new Grouping(222, 2, "domain2", null, null);
        Grouping domain3 = new Grouping(333, 3, "domain3", null, null);

        HashMap<Grouping, Collection<Grouping>> domains = new HashMap<>();
        domains.put(domain1, new ArrayList<>());
        domains.put(domain2, new ArrayList<>());
        domains.put(domain3, new ArrayList<>());
        MultiDomainDSMData matrix = new MultiDomainDSMData(domains);  // create domains this way to ensure full control

        matrix.setCurrentStateAsCheckpoint();

        // Test 1  -  shifting to the bottom
        matrix.shiftDomainUp(domain3);
        matrix.shiftDomainUp(domain3);  // shift to the bottom

        Assertions.assertEquals(matrix.getDomains().get(0), domain3);  // assert ordering is 2, 3, 1
        Assertions.assertEquals(matrix.getDomains().get(1), domain1);
        Assertions.assertEquals(matrix.getDomains().get(2), domain2);

        // Test 2  -  shift past bottom (should not do anything)
        matrix.shiftDomainUp(domain3);
        Assertions.assertEquals(matrix.getDomains().get(0), domain3);  // assert ordering is still 2, 3, 1
        Assertions.assertEquals(matrix.getDomains().get(1), domain1);
        Assertions.assertEquals(matrix.getDomains().get(2), domain2);

        stressUndoRedo(matrix);

        Assertions.assertEquals(matrix.getDomains().get(0), domain3);  // assert ordering is still 2, 3, 1
        Assertions.assertEquals(matrix.getDomains().get(1), domain1);
        Assertions.assertEquals(matrix.getDomains().get(2), domain2);
    }


    /**
     * Tests redistributing domain priorities. Stresses the undo/redo cycle
     */
    @Test
    public void redistributeDomainPriorities() {
        Grouping domain1 = new Grouping(111, 7, "domain1", null, null);
        Grouping domain2 = new Grouping(222, 4, "domain2", null, null);
        Grouping domain3 = new Grouping(333, 9, "domain3", null, null);
        Grouping domain4 = new Grouping(444, 2, "domain3", null, null);

        HashMap<Grouping, Collection<Grouping>> domains = new HashMap<>();
        domains.put(domain1, new ArrayList<>());
        domains.put(domain2, new ArrayList<>());
        domains.put(domain3, new ArrayList<>());
        domains.put(domain4, new ArrayList<>());
        MultiDomainDSMData matrix = new MultiDomainDSMData(domains);  // create domains this way to ensure full control

        matrix.setCurrentStateAsCheckpoint();

        matrix.redistributeDomainPriorities();
        Assertions.assertEquals(matrix.getDomains().get(0), domain4);  // assert order is correct
        Assertions.assertEquals(matrix.getDomains().get(1), domain2);
        Assertions.assertEquals(matrix.getDomains().get(2), domain1);
        Assertions.assertEquals(matrix.getDomains().get(3), domain3);

        Assertions.assertEquals(matrix.getDomains().get(0).getPriority(), domain4.getPriority());  // assert priorities are correct
        Assertions.assertEquals(matrix.getDomains().get(1).getPriority(), domain2.getPriority());
        Assertions.assertEquals(matrix.getDomains().get(2).getPriority(), domain1.getPriority());
        Assertions.assertEquals(matrix.getDomains().get(3).getPriority(), domain3.getPriority());
    }


    /**
     * Tests removing a domain. Test to ensure the undo-redo works
     */
    @Test
    public void removeDomainTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();
        Grouping domain = new Grouping("domain", null);
        Grouping group = new Grouping("group1", null);
        matrix.addDomain(domain);
        matrix.addDomainGrouping(domain, group);

        matrix.setCurrentStateAsCheckpoint();
        matrix.removeDomain(domain);

        matrix.undoToCheckpoint();
        Assertions.assertTrue(matrix.getDomains().contains(domain));
        Assertions.assertTrue(matrix.getDomainGroupings(domain).contains(group));
        matrix.redoToCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertFalse(matrix.getDomains().contains(domain));
    }


    /**
     * Tests removing a domain grouping. Test to ensure the undo-redo works
     */
    @Test
    public void removeDomainGroupingTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();
        Grouping domain = new Grouping("domain", null);
        Grouping group = new Grouping("group", null);
        matrix.addDomain(domain);
        matrix.addDomainGrouping(domain, group);

        matrix.setCurrentStateAsCheckpoint();
        matrix.removeDomainGrouping(domain, group);

        stressUndoRedo(matrix);

        Assertions.assertFalse(matrix.getDomainGroupings(domain).contains(group));
    }


    /**
     * Tests removing all domain groupings for a given domain. Checks that items created with that domain grouping
     * do not have that grouping anymore. Test to ensure the undo-redo works
     */
    @Test
    public void clearDomainGroupingsTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();
        Grouping domain = new Grouping("domain", null);
        Grouping group1 = new Grouping("group1", null);
        Grouping group2 = new Grouping("group2", null);
        Grouping group3 = new Grouping("group3", null);
        matrix.addDomain(domain);
        matrix.addDomainGrouping(domain, group1);
        matrix.addDomainGrouping(domain, group2);
        matrix.addDomainGrouping(domain, group3);

        DSMItem rowItem = new DSMItem(1, 11, 1.0, "item1", group1, domain);
        DSMItem colItem = new DSMItem(11, 1, 1.0, "item1", group1, domain);
        matrix.addItem(rowItem, true);
        matrix.addItem(colItem, false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.clearDomainGroupings(domain);

        stressUndoRedo(matrix);

        Assertions.assertFalse(matrix.getDomainGroupings(domain).contains(group1));
        Assertions.assertFalse(matrix.getDomainGroupings(domain).contains(group2));
        Assertions.assertFalse(matrix.getDomainGroupings(domain).contains(group3));
        Assertions.assertNotEquals(rowItem.getGroup1(), group1);
        Assertions.assertNotEquals(colItem.getGroup1(), group1);
    }


    /**
     * Tests renaming a grouping. Uses a domain, but the process is the same for domain groupings.
     * Test to ensure the undo-redo works
     */
    @Test
    public void renameGroupingTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();
        Grouping domain = new Grouping("domain", null);
        matrix.addDomain(domain);

        matrix.setCurrentStateAsCheckpoint();
        matrix.renameGrouping(domain, "new domain");

        stressUndoRedo(matrix);

        Assertions.assertEquals("new domain", domain.getName());
    }


    /**
     * Tests changing a groupings color. Uses a domain, but the process is the same for domain groupings.
     * Test to ensure the undo-redo works
     */
    @Test
    public void updateGroupingColorTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();
        Grouping domain = new Grouping("domain", Color.color(1.0, 1.0, 1.0));
        matrix.addDomain(domain);

        matrix.setCurrentStateAsCheckpoint();
        matrix.updateGroupingColor(domain, Color.color(0.0, 0.0, 0.0));

        stressUndoRedo(matrix);

        Assertions.assertEquals(Color.color(0.0, 0.0, 0.0), domain.getColor());
    }


    /**
     * Tests changing a groupings font color. Uses a domain, but the process is the same for domain groupings.
     * Test to ensure the undo-redo works
     */
    @Test
    public void updateGroupingFontColorTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();
        Grouping domain = new Grouping("domain", Color.color(1.0, 1.0, 1.0));
        matrix.addDomain(domain);

        matrix.setCurrentStateAsCheckpoint();
        matrix.updateGroupingFontColor(domain, Color.color(0.0, 0.0, 0.0));

        stressUndoRedo(matrix);

        Assertions.assertEquals(Color.color(0.0, 0.0, 0.0), domain.getFontColor());
    }


    /**
     * Tests setting an item to a domain group already contained in the matrix. Test to ensure the undo-redo works
     */
    @Test
    public void setItemDomainGroupTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();

        Grouping domain = new Grouping("domain", null);
        Grouping startGroup = new Grouping("start", null);
        Grouping endGroup = new Grouping("end", null);
        matrix.addDomain(domain);

        DSMItem rowItem = new DSMItem(1, 11, 1.0, "item1", startGroup, domain);
        DSMItem colItem = new DSMItem(11, 1, 1.0, "item1", startGroup, domain);
        matrix.addItem(rowItem, true);
        matrix.addItem(colItem, false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemDomainGroup(rowItem, domain, endGroup);

        stressUndoRedo(matrix);

        Assertions.assertEquals(endGroup, rowItem.getGroup1());
        Assertions.assertEquals(endGroup, colItem.getGroup1());
    }


    /**
     * Unit test for creating an item in a dsm. Test to ensure the undo-redo works with creating a new item
     */
    @Test
    public void createItemTest() {
        MultiDomainDSMData matrix = new MultiDomainDSMData();

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
        MultiDomainDSMData matrix = new MultiDomainDSMData();
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
        MultiDomainDSMData matrix = new MultiDomainDSMData();
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
        MultiDomainDSMData matrix = new MultiDomainDSMData();
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
        MultiDomainDSMData matrix = new MultiDomainDSMData();
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

}
