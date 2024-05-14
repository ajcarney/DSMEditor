package Matrices;

import Matrices.Data.AsymmetricDSMData;
import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.MultiDomainDSMData;
import Matrices.Data.SymmetricDSMData;
import org.junit.jupiter.api.Assertions;

import java.util.List;

/**
 * Helper methods for comparing matrices.
 */
public class MatrixHelpers {

    /**
     * Finds a DSM item in a list of items by name.
     * @param items the list of dsm items
     * @param name the name to search for
     * @return the dsm item, null if not found
     */
    private static DSMItem findItemByName(List<DSMItem> items, String name) {
        for (DSMItem item : items) {
            if (item.getName().getValue().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Asserts that two symmetric matrices are equal. This is done by comparing the groupings, items, and connections.
     * Checks based on name because UIDs may be messed up.
     *
     * @param originalMatrix the original matrix
     * @param newMatrix      the new matrix
     */
    public static void assertSymmetricMatricesEqual(SymmetricDSMData originalMatrix, SymmetricDSMData newMatrix) {
        // compare groups
        Assertions.assertEquals(originalMatrix.getGroupings().size(), newMatrix.getGroupings().size());
        for (Grouping origGroup : originalMatrix.getGroupings()) {
            // search by name because UIDs will be messed up
            Assertions.assertTrue(newMatrix.getGroupings().stream().map(Grouping::getName).toList().contains(origGroup.getName()));
        }

        // compare items by name and group
        Assertions.assertEquals(originalMatrix.getRows().size(), newMatrix.getRows().size());
        Assertions.assertEquals(originalMatrix.getCols().size(), newMatrix.getCols().size());

        for (DSMItem origItem : originalMatrix.getRows()) {  // rows
            // search by name because UIDs may be messed up
            DSMItem newItem = findItemByName(newMatrix.getRows(), origItem.getName().getValue());
            Assertions.assertNotNull(newItem);
            Assertions.assertEquals(origItem.getGroup1().getName(), newItem.getGroup1().getName());
        }

        for (DSMItem origItem : originalMatrix.getCols()) {  // columns
            // search by name because UIDs may be messed up
            DSMItem newItem = findItemByName(newMatrix.getCols(), origItem.getName().getValue());
            Assertions.assertNotNull(newItem);
            Assertions.assertEquals(origItem.getGroup1().getName(), newItem.getGroup1().getName());
        }

        // ensure alias's are set up correctly
        for (DSMItem rowItem : newMatrix.getRows()) {
            DSMItem colItem = findItemByName(newMatrix.getCols(), rowItem.getName().getValue());
            Assertions.assertNotNull(colItem);
            Assertions.assertEquals(rowItem.getUid(), colItem.getAliasUid());
            Assertions.assertEquals(colItem.getUid(), rowItem.getAliasUid());
        }

        // compare connections
        Assertions.assertEquals(originalMatrix.getConnections().size(), newMatrix.getConnections().size());

        for (DSMConnection conn : originalMatrix.getConnections()) {
            String rowName = originalMatrix.getItem(conn.getRowUid()).getName().getValue();
            String colName = originalMatrix.getItem(conn.getColUid()).getName().getValue();
            String searchName = rowName + colName + conn.getConnectionName() + conn.getWeight();
            Assertions.assertTrue(newMatrix.getConnections().stream().map( c ->
                    newMatrix.getItem(c.getRowUid()).getName().getValue()
                    + newMatrix.getItem(c.getColUid()).getName().getValue()
                    + c.getConnectionName()
                    + c.getWeight()
            ).toList().contains(searchName));
        }
    }


    /**
     * Asserts that two asymmetric matrices are equal. This is done by comparing the items and connections.
     * Checks based on name because UIDs may be messed up.
     *
     * @param originalMatrix the original matrix
     * @param newMatrix      the new matrix
     */
    public static void assertAsymmetricMatricesEqual(AsymmetricDSMData originalMatrix, AsymmetricDSMData newMatrix) {
        // compare items
        Assertions.assertEquals(originalMatrix.getRows().size(), newMatrix.getRows().size());
        Assertions.assertEquals(originalMatrix.getCols().size(), newMatrix.getCols().size());

        for (DSMItem origItem : originalMatrix.getRows()) {  // rows
            // search by name because UIDs may be messed up
            DSMItem newItem = findItemByName(newMatrix.getRows(), origItem.getName().getValue());
            Assertions.assertNotNull(newItem);
        }

        for (DSMItem origItem : originalMatrix.getCols()) {  // columns
            // search by name because UIDs may be messed up
            DSMItem newItem = findItemByName(newMatrix.getCols(), origItem.getName().getValue());
            Assertions.assertNotNull(newItem);
        }

        // compare connections
        Assertions.assertEquals(originalMatrix.getConnections().size(), newMatrix.getConnections().size());

        for (DSMConnection conn : originalMatrix.getConnections()) {
            String rowName = originalMatrix.getItem(conn.getRowUid()).getName().getValue();
            String colName = originalMatrix.getItem(conn.getColUid()).getName().getValue();
            String searchName = rowName + colName + conn.getConnectionName() + conn.getWeight();
            Assertions.assertTrue(newMatrix.getConnections().stream().map( c ->
                    newMatrix.getItem(c.getRowUid()).getName().getValue()
                    + newMatrix.getItem(c.getColUid()).getName().getValue()
                    + c.getConnectionName()
                    + c.getWeight()
            ).toList().contains(searchName));
        }
    }

    /**
     * Asserts that two multi-domain matrices are equal. This is done by comparing the domains, groupings, items,
     * and connections. Checks based on name because UIDs may be messed up.
     *
     * @param originalMatrix the original matrix
     * @param newMatrix      the new matrix
     */
    public static void assertMultiDomainMatricesEqual(MultiDomainDSMData originalMatrix, MultiDomainDSMData newMatrix) {
        // compare domains
        Assertions.assertEquals(originalMatrix.getDomains().size(), newMatrix.getDomains().size());
        for (Grouping origGroup : originalMatrix.getDomains()) {
            // search by name because UIDs will be messed up
            Assertions.assertTrue(newMatrix.getDomains().stream().map(Grouping::getName).toList().contains(origGroup.getName()));
        }

        // compare domain groupings
        Assertions.assertEquals(originalMatrix.getDomainGroupings().size(), newMatrix.getDomainGroupings().size());
        for (Grouping origGroup : originalMatrix.getDomainGroupings()) {
            // search by name because UIDs will be messed up
            Assertions.assertTrue(newMatrix.getDomainGroupings().stream().map(Grouping::getName).toList().contains(origGroup.getName()));
        }

        // compare items by domain, group, and name
        Assertions.assertEquals(originalMatrix.getRows().size(), newMatrix.getRows().size());
        Assertions.assertEquals(originalMatrix.getCols().size(), newMatrix.getCols().size());

        for (DSMItem origItem : originalMatrix.getRows()) {  // rows
            // search by name because UIDs may be messed up
            DSMItem newItem = findItemByName(newMatrix.getRows(), origItem.getName().getValue());
            Assertions.assertNotNull(newItem);
            Assertions.assertEquals(origItem.getGroup1().getName(), newItem.getGroup1().getName());  // domain grouping
            Assertions.assertEquals(origItem.getGroup2().getName(), newItem.getGroup2().getName());  // domain
        }

        for (DSMItem origItem : originalMatrix.getCols()) {  // columns
            // search by name because UIDs may be messed up
            DSMItem newItem = findItemByName(newMatrix.getCols(), origItem.getName().getValue());
            Assertions.assertNotNull(newItem);
            Assertions.assertEquals(origItem.getGroup1().getName(), newItem.getGroup1().getName());  // domain grouping
            Assertions.assertEquals(origItem.getGroup2().getName(), newItem.getGroup2().getName());  // domain
        }

        // ensure alias's are set up correctly
        for (DSMItem rowItem : newMatrix.getRows()) {
            DSMItem colItem = findItemByName(newMatrix.getCols(), rowItem.getName().getValue());
            Assertions.assertNotNull(colItem);
            Assertions.assertEquals(rowItem.getUid(), colItem.getAliasUid());
            Assertions.assertEquals(colItem.getUid(), rowItem.getAliasUid());
        }

        // compare connections
        Assertions.assertEquals(originalMatrix.getConnections().size(), newMatrix.getConnections().size());

        for (DSMConnection conn : originalMatrix.getConnections()) {
            String rowName = originalMatrix.getItem(conn.getRowUid()).getName().getValue();
            String colName = originalMatrix.getItem(conn.getColUid()).getName().getValue();
            String searchName = rowName + colName + conn.getConnectionName() + conn.getWeight();
            Assertions.assertTrue(newMatrix.getConnections().stream().map( c ->
                    newMatrix.getItem(c.getRowUid()).getName().getValue()
                    + newMatrix.getItem(c.getColUid()).getName().getValue()
                    + c.getConnectionName()
                    + c.getWeight()
            ).toList().contains(searchName));
        }
    }

}
