package Matrices.Data.Entities;

/**
 * Data class to manage DSM connections
 *
 * @author: Aiden Carney
 */
public class DSMConnection {
    private String connectionName;
    private double weight;
    private int colUid;
    private int rowUid;


    /**
     * Creates a new connection data object
     *
     * @param connectionName the name given to the connection
     * @param weight         the weight given to the connection
     * @param rowUid         the uid of the row item in the connection
     * @param colUid         the uid of the column item in the connection
     */
    public DSMConnection(String connectionName, double weight, int rowUid, int colUid) {
        this.connectionName = connectionName;
        this.weight = weight;
        this.colUid = colUid;
        this.rowUid = rowUid;
    }


    /**
     * Copy constructor for DSMConnection
     *
     * @param copy DSMConnection object to copy
     */
    public DSMConnection(DSMConnection copy) {
        connectionName = copy.getConnectionName();
        weight = copy.getWeight();
        colUid = copy.getColUid();
        rowUid = copy.getRowUid();
    }

    /**
     * returns the current name of the connection
     *
     * @return the current connection name
     */
    public String getConnectionName() {
        return connectionName;
    }


    /**
     * returns the current weight of the connection
     *
     * @return the current weight
     */
    public double getWeight() {
        return weight;
    }


    /**
     * returns the uid of the column in the connection
     *
     * @return unique id of the column element
     */
    public int getColUid() {
        return colUid;
    }


    /**
     * returns the uid of the row in the connection
     *
     * @return unique id of the row element
     */
    public int getRowUid() {
        return rowUid;
    }


    /**
     * Sets the current connection name to a new name
     *
     * @param connectionName the new name to be associated with the connection
     */
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }


    /**
     * Sets the current weight to a new weight
     *
     * @param weight the new weight to be associated with the connection
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }


    /**
     * The function for determining if two connections are equal. Compare on weight and name
     *
     * @param o  the object to compare
     * @return   true if the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        // Check if o is an instance of DSMConnection or not "null instanceof [type]" also returns false
        if (!(o instanceof DSMConnection)) {
            return false;
        }

        // cast to this object
        DSMConnection c = (DSMConnection) o;
        return ((c.getConnectionName().equals(this.getConnectionName())) && (c.getWeight() == this.getWeight()) && (c.getRowUid() == this.getRowUid()) && (c.getColUid() == this.getColUid()));  // compare based on name, weight, and uids
    }


    /**
     * Compares two DSMConnection types to check if they have the same name and weight
     *
     * @param c  the connection to compare to
     * @return   true or false if connections are the same type
     */
    public boolean isSameConnectionType(DSMConnection c) {
        return ((c.getConnectionName().equals(this.getConnectionName())) && (c.getWeight() == this.getWeight()));  // compare based on name and weight
    }
}
