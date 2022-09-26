package Matrices.Data.Entities;

import Matrices.Data.Entities.DSMInterfaceType;
import Matrices.Data.Entities.DSMItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;


/**
 * Class for testing methods relating to a DSMConnection
 */
public class DSMConnectionTest {

    /**
     * Tests the equality method of a DSMConnection. Connections should be equal
     */
    @Test
    public void equalsTrueTest() {
        ArrayList<DSMInterfaceType> interfaces = new ArrayList<>();
        interfaces.add(new DSMInterfaceType("interface1", "1"));
        interfaces.add(new DSMInterfaceType("interface2", "2"));
        interfaces.add(new DSMInterfaceType("interface3", "3"));

        DSMConnection conn1 = new DSMConnection("conn", 1.0, 1, 2, interfaces);
        DSMConnection conn2 = new DSMConnection("conn", 1.0, 1, 2, interfaces);

        Assertions.assertEquals(conn1, conn2);
    }


    /**
     * Tests the equality method of a DSMConnection. Connections should not be equal
     */
    @Test
    public void equalsFalseTest() {
        ArrayList<DSMInterfaceType> interfaces = new ArrayList<>();
        interfaces.add(new DSMInterfaceType("interface1", "1"));
        interfaces.add(new DSMInterfaceType("interface2", "2"));
        interfaces.add(new DSMInterfaceType("interface3", "3"));

        DSMConnection conn1 = new DSMConnection("conn", 1.0, 1, 2, interfaces);
        DSMConnection conn2 = new DSMConnection("conn", 1.0, 1, 2, new ArrayList<>());

        Assertions.assertNotEquals(conn1, conn2);
    }


    /**
     * Tests how the connection determines if two connections are of the same type based on name, weight,
     * and interfaces. Connections should be the same
     */
    @Test
    public void isSameConnectionTypeTrueTest() {
        ArrayList<DSMInterfaceType> interfaces = new ArrayList<>();
        interfaces.add(new DSMInterfaceType("interface1", "1"));
        interfaces.add(new DSMInterfaceType("interface2", "2"));
        interfaces.add(new DSMInterfaceType("interface3", "3"));

        DSMConnection conn1 = new DSMConnection("conn", 1.0, 1, 2, interfaces);
        DSMConnection conn2 = new DSMConnection("conn", 1.0, 3, 4, interfaces);

        Assertions.assertTrue(conn1.isSameConnectionType(conn2));
    }


    /**
     * Tests how the connection determines if two connections are of the same type based on name, weight,
     * and interfaces. Connections should not be the same
     */
    @Test
    public void isSameConnectionTypeFalseTest() {
        ArrayList<DSMInterfaceType> interfaces = new ArrayList<>();
        interfaces.add(new DSMInterfaceType("interface1", "1"));
        interfaces.add(new DSMInterfaceType("interface2", "2"));
        interfaces.add(new DSMInterfaceType("interface3", "3"));

        DSMConnection conn1 = new DSMConnection("conn", 1.0, 1, 2, interfaces);
        DSMConnection conn2 = new DSMConnection("conn", 1.0, 3, 4, new ArrayList<>());

        Assertions.assertFalse(conn1.isSameConnectionType(conn2));
    }

}
