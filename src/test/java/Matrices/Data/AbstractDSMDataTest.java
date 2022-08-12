package Matrices.Data;

import Matrices.Data.Entities.DSMInterfaceType;
import Matrices.Data.Entities.DSMItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * Unit test class for testing the final methods in the Abstract DSM class. Mostly contains tests
 * for stressing the undo and redo functionality. Only tests the methods marked final and tests it
 * using the SymmetricDSM concrete implementation class
 */
public class AbstractDSMDataTest {

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
     * Tests adding an interface grouping. Stresses the undo and redo functionality for it
     */
    @Test
    public void addInterfaceTypeGrouping() {
        SymmetricDSMData matrix = new SymmetricDSMData();

        matrix.setCurrentStateAsCheckpoint();
        matrix.addInterfaceTypeGrouping("mechanical");

        stressUndoRedo(matrix);

        Assertions.assertTrue(matrix.getInterfaceGroupings().contains("mechanical"));
    }


    /**
     * Tests adding an interface. Stresses the undo and redo functionality for it
     */
    @Test
    public void addInterface() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.addInterfaceTypeGrouping("mechanical");

        matrix.setCurrentStateAsCheckpoint();
        DSMInterfaceType interfaceType = new DSMInterfaceType("m1", "a");
        matrix.addInterface("mechanical", interfaceType);

        stressUndoRedo(matrix);

        Assertions.assertTrue(matrix.getInterfaceGroupings().contains("mechanical"));
        Assertions.assertTrue(matrix.getInterfaceTypes().get("mechanical").contains(interfaceType));
    }


    /**
     * Tests removing an interface grouping. Ensures that any interfaces under it are present on undo.
     * Stresses the undo/redo cycle
     */
    @Test
    public void removeInterfaceTypeGrouping() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.addInterfaceTypeGrouping("mechanical");
        DSMInterfaceType interfaceType = new DSMInterfaceType("m1", "a");
        matrix.addInterface("mechanical", interfaceType);

        matrix.setCurrentStateAsCheckpoint();
        matrix.removeInterfaceTypeGrouping("mechanical");

        matrix.undoToCheckpoint();
        Assertions.assertTrue(matrix.getInterfaceGroupings().contains("mechanical"));
        Assertions.assertTrue(matrix.getInterfaceTypes().get("mechanical").contains(interfaceType));
        matrix.redoToCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertFalse(matrix.getInterfaceGroupings().contains("mechanical"));
    }


    /**
     * Tests removing an interface. Ensures that the interface is present on undo.
     * Stresses the undo/redo cycle
     */
    @Test
    public void removeInterface() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.addInterfaceTypeGrouping("mechanical");
        DSMInterfaceType interfaceType = new DSMInterfaceType("m1", "a");
        matrix.addInterface("mechanical", interfaceType);

        matrix.setCurrentStateAsCheckpoint();
        matrix.removeInterface("mechanical", interfaceType);

        matrix.undoToCheckpoint();
        Assertions.assertTrue(matrix.getInterfaceTypes().get("mechanical").contains(interfaceType));
        matrix.redoToCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertFalse(matrix.getInterfaceTypes().get("mechanical").contains(interfaceType));
    }


    /**
     * Tests renaming an interface Grouping. Checks to ensure that any interfaces under it are still accessible.
     * Stresses the undo/redo cycle
     */
    @Test
    public void renameInterfaceTypeGrouping() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        DSMInterfaceType interfaceType = new DSMInterfaceType("m1", "a");
        matrix.addInterfaceTypeGrouping("mechanical");
        matrix.addInterface("mechanical", interfaceType);

        matrix.setCurrentStateAsCheckpoint();
        matrix.renameInterfaceTypeGrouping("mechanical", "electrical");

        matrix.undoToCheckpoint();
        Assertions.assertTrue(matrix.getInterfaceGroupings().contains("mechanical"));
        Assertions.assertTrue(matrix.getInterfaceTypes().get("mechanical").contains(interfaceType));
        matrix.redoToCheckpoint();

        stressUndoRedo(matrix);


        Assertions.assertTrue(matrix.getInterfaceGroupings().contains("electrical"));
        Assertions.assertTrue(matrix.getInterfaceTypes().get("electrical").contains(interfaceType));
    }


    /**
     * Tests renaming an interface. Stresses the undo/redo cycle
     */
    @Test
    public void renameInterfaceType() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        DSMInterfaceType interfaceType = new DSMInterfaceType("m1", "a");
        matrix.addInterfaceTypeGrouping("mechanical");
        matrix.addInterface("mechanical", interfaceType);

        matrix.setCurrentStateAsCheckpoint();
        matrix.renameInterfaceType(interfaceType, "m2");

        stressUndoRedo(matrix);

        Assertions.assertTrue(matrix.getInterfaceTypes().get("mechanical").contains(interfaceType));
        Assertions.assertEquals("m2", interfaceType.getName());
    }


    /**
     * Tests changing an interface's abbreviation. Stresses the undo/redo cycle
     */
    @Test
    public void updateInterfaceTypeAbbreviation() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        DSMInterfaceType interfaceType = new DSMInterfaceType("m1", "a");
        matrix.addInterfaceTypeGrouping("mechanical");
        matrix.addInterface("mechanical", interfaceType);

        matrix.setCurrentStateAsCheckpoint();
        matrix.updateInterfaceTypeAbbreviation(interfaceType, "a2");

        stressUndoRedo(matrix);

        Assertions.assertTrue(matrix.getInterfaceTypes().get("mechanical").contains(interfaceType));
        Assertions.assertEquals("a2", interfaceType.getAbbreviation());
    }


    /**
     * tests creating a connection for a matrix
     */
    @Test
    public void createConnectionTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        matrix.createItem("item1", true);
        matrix.createItem("item2", false);

        matrix.setCurrentStateAsCheckpoint();
        matrix.modifyConnection(matrix.getRows().get(0).getUid(), matrix.getCols().get(0).getUid(), "x", 1.0, new ArrayList<>());
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(1, matrix.getConnections().size());
        Assertions.assertEquals("x", matrix.getConnections().get(0).getConnectionName());
        Assertions.assertEquals(1.0, matrix.getConnections().get(0).getWeight());
    }


    /**
     * tests creating a connection for a matrix
     */
    @Test
    public void modifyConnectionTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.createItem("item1", true);
        matrix.createItem("item2", true);
        matrix.createConnection(matrix.getRows().get(0).getUid(), matrix.getRows().get(1).getAliasUid(), "x", 1.0, new ArrayList<>());

        matrix.setCurrentStateAsCheckpoint();
        matrix.modifyConnection(matrix.getRows().get(0).getUid(), matrix.getRows().get(1).getAliasUid(), "a", 42.0, new ArrayList<>());
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(1, matrix.getConnections().size());
        Assertions.assertEquals("a", matrix.getConnections().get(0).getConnectionName());
        Assertions.assertEquals(42.0, matrix.getConnections().get(0).getWeight());
    }



    /**
     * tests deleting a connection for a matrix
     */
    @Test
    public void deleteConnectionTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.createItem("item1", true);
        matrix.createItem("item2", true);
        matrix.createConnection(matrix.getRows().get(0).getUid(), matrix.getRows().get(1).getAliasUid(), "x", 1.0, new ArrayList<>());

        matrix.setCurrentStateAsCheckpoint();
        matrix.deleteConnection(matrix.getRows().get(0).getUid(), matrix.getRows().get(1).getAliasUid());
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(0, matrix.getConnections().size());
    }


    /**
     * tests removing all connections in a matrix
     */
    @Test
    public void deleteAllConnections() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.addItem(new DSMItem(1, 11, 1.0, "item1", null, null), true);
        matrix.addItem(new DSMItem(2, 22, 1.0, "item2", null, null), true);
        matrix.addItem(new DSMItem(3, 33, 1.0, "item3", null, null), true);
        matrix.addItem(new DSMItem(11, 1, 1.0, "item1", null, null), false);
        matrix.addItem(new DSMItem(22, 2, 1.0, "item2", null, null), false);
        matrix.addItem(new DSMItem(33, 3, 1.0, "item3", null, null), false);
        matrix.createConnection(1, 22, "x", 1.0, new ArrayList<>());
        matrix.createConnection(3, 22, "x", 1.0, new ArrayList<>());
        matrix.createConnection(2, 11, "x", 1.0, new ArrayList<>());
        matrix.createConnection(3, 11, "x", 1.0, new ArrayList<>());

        matrix.setCurrentStateAsCheckpoint();
        matrix.deleteAllConnections();
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(0, matrix.getConnections().size());
    }


    /**
     * tests transposing a matrix
     */
    @Test
    public void transposeMatrixTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        matrix.addItem(new DSMItem(1, 11, 1.0, "item1", null, null), true);
        matrix.addItem(new DSMItem(2, 22, 1.0, "item2", null, null), true);
        matrix.addItem(new DSMItem(3, 33, 1.0, "item3", null, null), true);
        matrix.addItem(new DSMItem(11, 1, 1.0, "item1", null, null), false);
        matrix.addItem(new DSMItem(22, 2, 1.0, "item2", null, null), false);
        matrix.addItem(new DSMItem(33, 3, 1.0, "item3", null, null), false);
        matrix.createConnection(1, 22, "x", 1.0, new ArrayList<>());
        matrix.createConnection(3, 22, "x", 1.0, new ArrayList<>());
        matrix.createConnection(2, 11, "x", 1.0, new ArrayList<>());
        matrix.createConnection(3, 11, "x", 1.0, new ArrayList<>());

        matrix.setCurrentStateAsCheckpoint();
        matrix.transposeMatrix();
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        ArrayList<String> expectedRowNames = new ArrayList<>(Arrays.asList("item1", "item2", "item3"));
        ArrayList<String> expectedColNames = new ArrayList<>(Arrays.asList("item1", "item2", "item3"));
        ArrayList<String> actualRowNames = new ArrayList<>(matrix.getRows().stream().map(item -> item.getName().getValue()).collect(Collectors.toList()));
        ArrayList<String> actualColNames = new ArrayList<>(matrix.getCols().stream().map(item -> item.getName().getValue()).collect(Collectors.toList()));
        Collections.sort(expectedRowNames);
        Collections.sort(expectedColNames);
        Collections.sort(actualRowNames);
        Collections.sort(actualColNames);

        Assertions.assertEquals(4, matrix.getConnections().size());  // make sure connections stayed the same
        Assertions.assertIterableEquals(expectedRowNames, actualRowNames);
        Assertions.assertIterableEquals(expectedColNames, actualColNames);
    }


    /**
     * Tests adding a single row item. Uses an Asymmetric matrix as the implementation class because it does not
     * override this method
     */
    @Test
    public void addRowItemTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        DSMItem item = new DSMItem(1.0, "item");

        matrix.setCurrentStateAsCheckpoint();
        matrix.addItem(item, true);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(item, matrix.getRows().get(0));
        Assertions.assertEquals(1, matrix.getRows().size());
        Assertions.assertEquals(0, matrix.getCols().size());
    }


    /**
     * Tests adding a single column item. Uses an Asymmetric matrix as the implementation class because it does not
     * override this method
     */
    @Test
    public void addColItemTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        DSMItem item = new DSMItem(1.0, "item");

        matrix.setCurrentStateAsCheckpoint();
        matrix.addItem(item, false);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(item, matrix.getCols().get(0));
        Assertions.assertEquals(0, matrix.getRows().size());
        Assertions.assertEquals(1, matrix.getCols().size());
    }


    /**
     * Tests creating a single row item. Uses an Asymmetric matrix as the implementation class because it does not
     * override this method
     */
    @Test
    public void createRowItemTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();

        matrix.setCurrentStateAsCheckpoint();
        matrix.createItem("item", true);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(1, matrix.getRows().size());
        Assertions.assertEquals(0, matrix.getCols().size());
    }


    /**
     * Tests creating a single column item. Uses an Asymmetric matrix as the implementation class because it does not
     * override this method
     */
    @Test
    public void createColItemTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();

        matrix.setCurrentStateAsCheckpoint();
        matrix.createItem("item", false);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(0, matrix.getRows().size());
        Assertions.assertEquals(1, matrix.getCols().size());
    }



    /**
     * Tests deleting a single row item. Uses an Asymmetric matrix as the implementation class because it does not
     * override this method
     */
    @Test
    public void deleteItemTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        DSMItem item = new DSMItem(1.0, "item");
        matrix.addItem(item, true);

        matrix.setCurrentStateAsCheckpoint();
        matrix.deleteItem(item);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(0, matrix.getRows().size());
        Assertions.assertEquals(0, matrix.getCols().size());
    }


    /**
     * Tests setting the name of a single row item. Uses an Asymmetric matrix as the implementation class
     * because it does not override this method
     */
    @Test
    public void setItemNameTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        DSMItem rowItem = new DSMItem(1, null, 1.0, "item1", null, null);
        matrix.addItem(rowItem, true);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemName(rowItem, "newItem1");
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals("newItem1", rowItem.getName().getValue());
    }


    /**
     * Tests setting the sort index of a single row item. Uses an Asymmetric matrix as the implementation class
     * because it does not override this method
     */
    @Test
    public void setItemSortIndexTest() {
        AsymmetricDSMData matrix = new AsymmetricDSMData();
        DSMItem rowItem = new DSMItem(1, null, 1.0, "item1", null, null);
        matrix.addItem(rowItem, true);

        matrix.setCurrentStateAsCheckpoint();
        matrix.setItemSortIndex(rowItem, 42.0);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(42.0, rowItem.getSortIndex());
    }


    /**
     * Tests the undo and redo functionality for setting the title metadata property
     */
    @Test
    public void setTitleTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        String newName = "new name";
        matrix.setTitle(newName);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(newName, matrix.getTitle());
    }


    /**
     * Tests the undo and redo functionality for setting the project name metadata property
     */
    @Test
    public void setProjectNameTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        String newName = "new name";
        matrix.setProjectName(newName);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(newName, matrix.getProjectName());
    }


    /**
     * Tests the undo and redo functionality for setting the customer metadata property
     */
    @Test
    public void setCustomerTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        String newName = "new name";
        matrix.setCustomer(newName);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(newName, matrix.getCustomer());
    }


    /**
     * Tests the undo and redo functionality for setting the version number metadata property
     */
    @Test
    public void setVersionNumberTest() {
        SymmetricDSMData matrix = new SymmetricDSMData();
        String newName = "new name";
        matrix.setVersionNumber(newName);
        matrix.setCurrentStateAsCheckpoint();

        stressUndoRedo(matrix);

        Assertions.assertEquals(newName, matrix.getVersionNumber());
    }

}
