package DSMData;

import javafx.util.Pair;

import java.util.*;

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
        return rows;
    }

    public Vector<DSMItem> getCols() {
        return cols;
    }

    public double getRowMaxSortIndex() {
        double index = 0;
        for(DSMItem row : rows) {
            if(row.getSortIndex() > index) {
                index = row.getSortIndex();
            }
        }
        return index;
    }

    public double getColMaxSortIndex() {
        double index = 0;
        for(DSMItem col : cols) {
            if(col.getSortIndex() > index) {
                index = col.getSortIndex();
            }
        }
        return index;
    }

    public DSMItem getItem(int uid) {
        for(DSMItem row : rows) {
            if(row.getUid() == uid) {
                return row;
            }
        }
        for(DSMItem col : cols) {
            if(col.getUid() == uid) {
                return col;
            }
        }
        return null;
    }
    
    public Vector<DSMConnection> getConnections() {
        return connections;
    }

    public boolean isSymmetrical() {
        return symmetrical;
    }

    public void setSymmetrical(boolean isSymmetrical) {
        this.symmetrical = isSymmetrical;
    }




    public void addNewSymmetricItem(String name) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        double index = getRowMaxSortIndex() + 1;
        DSMItem rowItem = new DSMItem(index, name);
        DSMItem colItem = new DSMItem(index, name);
        colItem.setAliasUid(rowItem.getUid());
        this.rows.add(rowItem);  // object is the same for row and column because matrix is symmetrical
        this.cols.add(colItem);

        this.wasModified = true;
    }


    public void addNewItem(String name, boolean is_row) {
        if(is_row) {
            DSMItem row = new DSMItem(getRowMaxSortIndex() + 1, name);
            this.rows.add(row);
        } else {
            double index = cols.size();
            DSMItem col = new DSMItem(getColMaxSortIndex() + 1, name);
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


    public void clearItemConnections(int uid) {
        Vector<DSMConnection> toRemove = new Vector<>();
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getRowUid() == uid || connection.getColUid() == uid) {
                toRemove.add(connection);
            }
        }
        connections.removeAll((Set)toRemove);
    }

    public void clearConnection(int rowUid, int colUid) {
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getRowUid() == rowUid && connection.getColUid() == colUid) {
                connections.remove(connection);
                break;
            }
        }
    }


    public void deleteSymmetricItem(int rowUid) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        int r_index = -1;
        int c_index = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            if (rows.elementAt(i).getUid() == rowUid) r_index = i;
            if (cols.elementAt(i).getAliasUid() == rowUid) c_index = i;

            if (r_index != -1 && c_index != -1) {
                rows.remove(r_index);
                cols.remove(c_index);
                break;
            }
        }
        clearItemConnections(rowUid);

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



    public void setItemNameSymmetric(int rowUid, String new_name) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        int r_index = -1;
        int c_index = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            if (rows.elementAt(i).getUid() == rowUid) r_index = i;
            if (cols.elementAt(i).getAliasUid() == rowUid) c_index = i;

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


    public void setSortIndexSymmetric(int rowUid, double newIndex) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        int r_index = -1;
        int c_index = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            if (rows.elementAt(i).getUid() == rowUid) r_index = i;
            if (cols.elementAt(i).getAliasUid() == rowUid) c_index = i;

            if (r_index != -1 && c_index != -1) {
                rows.elementAt(r_index).setSortIndex(newIndex);
                cols.elementAt(c_index).setSortIndex(newIndex);
            }
        }
        assert (r_index != -1 && c_index != -1) : "could not find same uid matching row and column in symmetrical matrix when changing item name";
        this.wasModified = true;
    }


    public void setSortIndex(int uid, double newIndex) {
        int index = -1;
        for(int i=0; i<this.rows.size(); i++) {     // check to see if uid is in the rows
            if(rows.elementAt(i).getUid() == uid) {
                index = i;
                break;
            }
        }
        if (index != -1) {  // uid was not in a row, must be in a column
            rows.elementAt(index).setSortIndex(newIndex);
        } else {
            for(int i=0; i<this.cols.size(); i++) {
                if(cols.elementAt(i).getUid() == uid) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                cols.elementAt(index).setSortIndex(newIndex);
            }
        }
        this.wasModified = true;
    }



    public DSMConnection getConnection(int rowUid, int colUid) {
        DSMConnection connection = null;
        for(DSMConnection conn : connections) {
            if(conn.getRowUid() == rowUid && conn.getColUid() == colUid) {
                connection = conn;
                break;
            }
        }

        return connection;
    }

    public void modifyConnection(int rowUid, int colUid, String connectionName, double weight) {
        // check to see if the connection is in the list of connections already
        boolean connectionExists = false;
        for(DSMConnection conn : this.connections) {
            if(rowUid == conn.getRowUid() && colUid == conn.getColUid()) {
                connectionExists = true;
                // connection exists, so modify it
                conn.setConnectionName(connectionName);  // TODO: make sure this actually modifies the object and not just a copy of it
                conn.setWeight(weight);
                break;
            }
        }

        if(!connectionExists) {  // if connection does not exist, add it
            DSMConnection connection = new DSMConnection(connectionName, weight, rowUid, colUid);
            connections.add(connection);
        }

        this.wasModified = true;
    }

    public void modifyConnectionSymmetrically(int rowUid, int colUid, String connectionName, double weight) {
        modifyConnection(rowUid, colUid, connectionName, weight);
        modifyConnection(getItem(colUid).getAliasUid(), colUid, connectionName, weight);
    }

    public ArrayList< ArrayList<Pair< String, Object> > > getGridArray() {
        ArrayList< ArrayList<Pair< String, Object> > > grid = new ArrayList<>();

        // sort row and columns by sortIndex
        Collections.sort(rows, Comparator.comparing(r -> r.getSortIndex()));
        Collections.sort(cols, Comparator.comparing(c -> c.getSortIndex()));

        // create header row
        ArrayList<Pair< String, Object> > row0 = new ArrayList<Pair< String, Object> >();
        row0.add(new Pair<String, Object>(new String("plain_text"), new String("")));
        row0.add(new Pair<String, Object>(new String("plain_text"), new String("Column Items")));
        for(DSMItem c : cols) {
            row0.add(new Pair<String, Object>(new String("item_name"), c.getUid()));
        }

        // create second header row
        ArrayList<Pair< String, Object> > row1 = new ArrayList<Pair< String, Object> >();
        row1.add(new Pair<String, Object>(new String("plain_text"), new String("Row Items")));
        row1.add(new Pair<String, Object>(new String("plain_text"), new String("Re-Sort Index")));
        if(isSymmetrical()) {  // add nothing to this row because it does not need to be displayed to the user
            for(DSMItem c : cols) {
                row1.add(new Pair<String, Object>(new String("plain_text"), new String("")));
            }
        } else {
            for(DSMItem c : cols) {
                row1.add(new Pair<String, Object>(new String("index_item"), c.getUid()));
            }
        }

        grid.add(row0);
        grid.add(row1);

        // create rows
        for(DSMItem r : rows) {
            ArrayList<Pair< String, Object> > row = new ArrayList<Pair< String, Object> >();
            row.add(new Pair<String, Object>(new String("item_name"), r.getUid()));
            row.add(new Pair<String, Object>(new String("index_item"), r.getUid()));
            for(DSMItem c : cols) {  // create connection items for all columns
                if(isSymmetrical() && c.getAliasUid() == r.getUid()) {  // can't have connection to itself in a symmetrical matrix
                    row.add(new Pair<String, Object>(new String("uneditable_connection"), null));
                } else {
                    row.add(new Pair<String, Object>(new String("editable_connection"), new Pair<Integer, Integer>(r.getUid(), c.getUid())));
                }
            }
            grid.add(row);
        }

        return grid;
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
