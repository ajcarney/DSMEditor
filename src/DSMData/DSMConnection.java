package DSMData;

public class DSMConnection {
    private String connectionName;
    private double weight;
    private int colUid;
    private int rowUid;

    public DSMConnection(String connectionName, double weight, int rowUid, int colUid) {
        this.connectionName = connectionName;
        this.weight = weight;
        this.colUid = colUid;
        this.rowUid = rowUid;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public double getWeight() {
        return weight;
    }

    public int getColUid() {
        return colUid;
    }

    public int getRowUid() {
        return rowUid;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }


}
