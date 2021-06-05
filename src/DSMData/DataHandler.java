package DSMData;

import java.util.Set;
import java.util.Vector;

public class DataHandler {
    private Vector<DSMItem> rows;
    private Vector<DSMItem> cols;
    private Vector<DSMConnection> connections;

    private boolean symmetrical;

    private String title = "";
    private String projectName = "";
    private String customer = "";
    private String versionNumber = "";

    private boolean wasModified = true;

    public DataHandler() {
        rows = new Vector<DSMItem>();
        cols = new Vector<DSMItem>();
        connections = new Vector<DSMConnection>();

        this.wasModified = true;
    }

    public Vector<DSMItem> getRows() {
        Vector<DSMItem> copy_rows = (Vector<DSMItem>)rows.clone();
        return copy_rows;
    }

    public Vector<DSMItem> getCols() {
        Vector<DSMItem> copy_cols = (Vector<DSMItem>)cols.clone();
        return copy_cols;
    }

    public boolean isSymmetrical() {
        return symmetrical;
    }

    public void setSymmetrical(boolean isSymmetrical) {
        this.symmetrical = isSymmetrical;
    }




    public void addSymmetricItem(String name) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        double index = rows.size();
        DSMItem rowItem = new DSMItem(index, name);
        DSMItem colItem = new DSMItem(index, name);
        this.rows.add(rowItem);  // object is the same for row and column because matrix is symmetrical
        this.cols.add(colItem);

        this.wasModified = true;
    }

    public void addItem(String name, boolean is_row) {
        if(is_row) {
            double index = rows.size();
            DSMItem row = new DSMItem(index, name);
            this.rows.add(row);
        } else {
            double index = cols.size();
            DSMItem col = new DSMItem(index, name);
            this.cols.add(col);
        }

        this.wasModified = true;
    }

    public void addItem(DSMItem item, boolean is_row) {
        if(is_row) {
            this.rows.add(item);
        } else {
            this.cols.add(item);
        }

        this.wasModified = true;
    }


    private void clearItemConnections(int uid) {
        Vector<DSMConnection> toRemove = new Vector<>();
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getFromUid() == uid || connection.getToUid() == uid) {
                toRemove.add(connection);
            }
        }
        connections.removeAll((Set)toRemove);
    }


    public void deleteSymmetricItem(int uid) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        int r_index = -1;
        int c_index = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            if (rows.elementAt(i).getUid() == uid) r_index = i;
            if (cols.elementAt(i).getUid() == uid) c_index = i;

            if (r_index != -1 && c_index != -1) {
                rows.remove(r_index);
                cols.remove(c_index);
                break;
            }
        }

        clearItemConnections(uid);
        assert (!(r_index == -1 || c_index == -1)) : "could not find same uid in row and column in symmetrical matrix when deleting item";
        this.wasModified = true;
    }

    public void deleteItem(int uid) {
        int index = -1;
        for(int i=0; i<this.rows.size(); i++) {     // check to see if uid is in the rows
            if(rows.elementAt(i).getUid() == uid) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            rows.remove(index);
        } else {                                   // uid was not in a row, must be in a column
            for(int i=0; i<this.cols.size(); i++) {
                if(cols.elementAt(i).getUid() == uid) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                cols.remove(index);
            }
        }
        clearItemConnections(uid);
        this.wasModified = true;
    }



    public void setItemNameSymmetric(int uid, String new_name) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        int r_index = -1;
        int c_index = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            if (rows.elementAt(i).getUid() == uid) r_index = i;
            if (cols.elementAt(i).getUid() == uid) c_index = i;

            if (r_index != -1 && c_index != -1) {
                rows.elementAt(r_index).setName(new_name);
                cols.elementAt(r_index).setName(new_name);
            }
        }
        assert (r_index != -1 && c_index != -1) : "could not find same uid in row and column in symmetrical matrix when changing item name";
        this.wasModified = true;
    }

    public void setItemName(int uid, String new_name) {
        int index = -1;
        for(int i=0; i<this.rows.size(); i++) {     // check to see if uid is in the rows
            if(rows.elementAt(i).getUid() == uid) {
                index = i;
                break;
            }
        }
        if (index != -1) {  // uid was not in a row, must be in a column
            rows.elementAt(index).setName(new_name);
        } else {
            for(int i=0; i<this.cols.size(); i++) {
                if(cols.elementAt(i).getUid() == uid) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                cols.elementAt(index).setName(new_name);
            }
        }
        this.wasModified = true;
    }


    public void modifyConnection(int row_uid, int col_uid, String connectionName, double weight) {
        // check to see if the connection is in the list of connections already
        boolean connectionExists = false;
        for(DSMConnection conn : this.connections) {
            if(row_uid == conn.getFromUid() && col_uid == conn.getToUid()) {
                connectionExists = true;
                // connection exists, so modify it
                conn.setConnectionName(connectionName);  // TODO: make sure this actually modifies the object and not just a copy of it
                conn.setWeight(weight);
                break;
            }
        }

        if(!connectionExists) {  // if connection does not exist, add it
            DSMConnection connection = new DSMConnection(connectionName, weight, row_uid, col_uid);
            connections.add(connection);
        }

        if(isSymmetrical()) {  // if symmetrical, then it needs to create the connection that is the same
            // check to see if the connection is in the list of connections already
            connectionExists = false;
            for(DSMConnection conn : this.connections) {
                if(col_uid == conn.getFromUid() && row_uid == conn.getToUid()) {
                    connectionExists = true;
                    // connection exists, so modify it
                    conn.setConnectionName(connectionName);  // TODO: make sure this actually modifies the object and not just a copy of it
                    conn.setWeight(weight);
                    break;
                }
            }

            if(!connectionExists) {  // if connection does not exist, add it
                DSMConnection connection = new DSMConnection(connectionName, weight, row_uid, col_uid);
                connections.add(connection);
            }
        }

        this.wasModified = true;
    }

    public void clearWasModifiedFlag() {
        this.wasModified = false;
    }

    public boolean getWasModified() {
        return wasModified;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }
}
