package DSMData;

public class DSMConnection {
    private String connectionName;
    private double weight;
    private int toUid;
    private int fromUid;

    public String getConnectionName() {
        return connectionName;
    }

    public double getWeight() {
        return weight;
    }

    public int getToUid() {
        return toUid;
    }

    public int getFromUid() {
        return fromUid;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public DSMConnection(String connectionName, double weight, int toUid, int fromUid) {
        this.connectionName = connectionName;
        this.weight = weight;
        this.toUid = toUid;
        this.fromUid = fromUid;
    }

}
