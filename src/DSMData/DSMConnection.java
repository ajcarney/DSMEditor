package DSMData;

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


}
