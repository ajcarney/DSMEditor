package DSMData;

import java.util.Vector;

public class DSMItem {
    private static int currentUid = 0;

    private int uid;
    private String name;
    private double sortIndex;

    private Vector<DSMConnection> connections;

    public DSMItem(double index, String name) {
        currentUid += 1;

        this.uid = currentUid;
        this.name = name;
        this.sortIndex = index;
    }

    public int getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public double getSortIndex() {
        return sortIndex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSortIndex(int index) {
        this.sortIndex = index;
    }

    public Vector<DSMConnection> getConnections() {
        Vector<DSMConnection> copyConnections = (Vector<DSMConnection>)connections.clone();
        return copyConnections;
    }

    public void modifyConnectionTo(int toUid, String connectionName, double weight) {
        // check to see if the connection is in the list of connections already
        boolean connectionExists = false;
        for(DSMConnection conn : this.connections) {
            if(this.uid == conn.getFromUid() && toUid == conn.getToUid()) {
                connectionExists = true;
                // connection exists, so modify it
                conn.setConnectionName(connectionName);  // TODO: make sure this actually modifies the object and not just a copy of it
                conn.setWeight(weight);
                break;
            }
        }

        // if connection does not exist, add it
        if(!connectionExists) {
            DSMConnection connection = new DSMConnection(connectionName, weight, this.uid, toUid);
            connections.add(connection);
        }
    }


}

