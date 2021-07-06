package DSMData;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.*;

/**
 * A class that contains data about a matrix. All operations to a matrix come through
 * this class. Handles both symmetrical and non-symmetrical matrices.
 *
 * @author: Aiden Carney
 */
public class DataHandler {
    private Vector<DSMItem> rows;
    private Vector<DSMItem> cols;
    private Vector<DSMConnection> connections;

    private HashMap<String, Color> groupingColors;
    private ObservableSet<String> groupings;  // ObservableSet is used so that any gui threads reading it will see changes without needing a callback set up

    private boolean symmetrical;

    private String title = "";
    private String projectName = "";
    private String customer = "";
    private String versionNumber = "";

    private boolean wasModified = true;


    /**
     * Creates a new DataHandler object. Creates no row or column items and metadata are empty strings.
     * There is one grouping, which is the default: "(None)"
     */
    public DataHandler() {
        rows = new Vector<>();
        cols = new Vector<>();
        connections = new Vector<>();
        groupingColors = new HashMap<>();
        groupings = FXCollections.observableSet();

        addGrouping("(None)", Color.color(1.0, 1.0, 1.0));  // add default group

        this.wasModified = true;
    }

    /**
     * Returns the rows in a mutable way  TODO: this should be immutable
     *
     * @return a vector of the items declared as rows
     */
    public Vector<DSMItem> getRows() {
        return rows;
    }


    /**
     * Returns the columns in a mutable way  TODO: this should be immutable
     *
     * @return a vector of the items declared as columns
     */
    public Vector<DSMItem> getCols() {
        return cols;
    }


    /**
     * Finds the maximum sort index of the rows by performing a linear search.
     *
     * @return the maximum sort index of the row items
     */
    public double getRowMaxSortIndex() {
        double index = 0;
        for(DSMItem row : rows) {
            if(row.getSortIndex() > index) {
                index = row.getSortIndex();
            }
        }
        return index;
    }


    /**
     * Finds the maximum sort index of the columns by performing a linear search.
     *
     * @return the maximum sort index of the column items
     */
    public double getColMaxSortIndex() {
        double index = 0;
        for(DSMItem col : cols) {
            if(col.getSortIndex() > index) {
                index = col.getSortIndex();
            }
        }
        return index;
    }


    /**
     * Finds an item by uid and returns it. It can be either a row item or a column item
     * @param uid the uid of the item to return
     * @return    DSMItem of the item with uid
     */
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


    /**
     * Returns connections in a mutable way  TODO: this should be immutable
     *
     * @return a vector of the connections in the matrix
     */
    public Vector<DSMConnection> getConnections() {
        return connections;
    }


    /**
     * Whether or not the matrix is symmetrical
     *
     * @return true is symmetrical, false otherwise
     */
    public Boolean isSymmetrical() {
        return symmetrical;
    }


    /**
     * changes whether or not the matrix is symmetrical
     *
     * @param isSymmetrical boolean of if the matrix should be symmetrical or not
     */
    public void setSymmetrical(boolean isSymmetrical) {
        this.symmetrical = isSymmetrical;
    }


    /**
     * Returns groupings HashMap in a mutable way  TODO: this should be immutable
     *
     * @return a HashMap of the groupings for the matrix
     */
    public HashMap<String, Color> getGroupingColors() {
        return groupingColors;
    }


    /**
     * Adds a new grouping to the matrix
     *
     * @param name  the names of the groupings
     * @param color the color associated with the grouping
     */
    public void addGrouping(String name, Color color) {
        if(color != null) {
            groupingColors.put(name, color);
        } else {
            groupingColors.put(name, Color.color(1.0, 1.0, 1.0));
        }
        groupings.add(name);
        this.wasModified = true;
    }


    /**
     * Removes a grouping from the matrix
     *
     * @param name the name of the grouping to remove
     */
    public void removeGrouping(String name) {
        groupingColors.remove(name);
        groupings.remove(name);
        for(DSMItem item : rows) {
            if(item.getGroup().equals(name)) {
                item.setGroup("(None)");
            }
        }
        for(DSMItem item : cols) {
            if(item.getGroup().equals(name)) {
                item.setGroup("(None)");
            }
        }
        this.wasModified = true;
    }


    /**
     * Removes all groupings from the matrix
     */
    public void clearGroupings() {
        groupingColors.clear();
        groupings.clear();
        this.wasModified = true;
    }


    /**
     * Returns the ObservableSet of the groupings that is used for gui widgets to auto update and
     * not need a callback to update
     *
     * @return ObservableSet of the current groupings
     */
    public ObservableSet<String> getGroupings() {
        return groupings;
    }


    /**
     * Renames a grouping and updates all DSMItem objects with the new grouping name
     *
     * @param oldName the old name to be changed
     * @param newName the new name of the grouping
     */
    public void renameGrouping(String oldName, String newName) {
        Color oldColor = groupingColors.get(oldName);
        for(DSMItem item : rows) {
            if(item.getGroup().equals(oldName)) {
                item.setGroup(newName);
            }
        }
        for(DSMItem item : cols) {
            if(item.getGroup().equals(oldName)) {
                item.setGroup(newName);
            }
        }
        removeGrouping(oldName);
        addGrouping(newName, oldColor);
        this.wasModified = true;
    }


    /**
     * Changes a color of a grouping
     *
     * @param name     the name of the grouping to change the color of
     * @param newColor the new color of the grouping
     */
    public void updateGroupingColor(String name, Color newColor) {
        groupingColors.remove(name);
        groupingColors.put(name, newColor);
        this.wasModified = true;
    }


    /**
     * Adds an item to the matrix symmetrically. The symmetrical flag must be set in order to use
     * this function. This function adds both the row and column and creates the alias for the column
     *
     * @param name the name of the item to be added
     */
    public void addNewSymmetricItem(String name) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        double index = (int)getRowMaxSortIndex() + 1;  // cast to int to remove the decimal place so that the index will be a whole number
        DSMItem rowItem = new DSMItem(index, name);
        DSMItem colItem = new DSMItem(index, name);
        colItem.setAliasUid(rowItem.getUid());
        this.rows.add(rowItem);  // object is the same for row and column because matrix is symmetrical
        this.cols.add(colItem);

        this.wasModified = true;
    }


    /**
     * Creates a new item and adds it to the matrix as either a row or a column. This function should
     * be used only for non-symmetrical datasets.
     *
     * @param name  the name of the item to create
     * @param isRow boolean flag of whether the item is a row or not
     */
    public void addNewItem(String name, boolean isRow) {
        if(isRow) {
            DSMItem row = new DSMItem((int)getRowMaxSortIndex() + 1.0, name);
            this.rows.add(row);
        } else {
            double index = cols.size();
            DSMItem col = new DSMItem((int)getColMaxSortIndex() + 1.0, name);
            this.cols.add(col);
        }

        this.wasModified = true;
    }


    /**
     * Adds an existing item to the matrix as either a row or a column. This function should
     * be used only for non-symmetrical datasets.
     *
     * @param item  DSMItem object of the item to add
     * @param isRow boolean flag of whether the item is a row or not
     */
    public void addItem(DSMItem item, boolean isRow) {
        if(isRow) {
            this.rows.add(item);
        } else {
            this.cols.add(item);
        }
        if(!groupingColors.containsKey(item.getGroup())) {
            addGrouping(item.getGroup(), null);
        }

        this.wasModified = true;
    }


    /**
     * Removes all connections associated with an item with a given uid. Used for when an item is deleted
     * from the matrix
     *
     * @param uid the uid of the item that will be looked for when removing connections
     */
    public void clearItemConnections(int uid) {
        Set<DSMConnection> toRemove = new HashSet<>();  // this will not allow duplicates, although there should never be duplicates
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getRowUid() == uid || connection.getColUid() == uid) {
                toRemove.add(connection);
            }
        }
        connections.removeAll(toRemove);
        this.wasModified = true;
    }


    /**
     * Removes an item from the matrix symmetrically. Both row and column with alias equal to the row
     * uid are removed. Removes connections for the items as well
     *
     * @param rowUid the uid of the row item to be removed
     */
    public void deleteSymmetricItem(int rowUid) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        int rIndex = -1;
        int cIndex = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            if (rows.elementAt(i).getUid() == rowUid) rIndex = i;
            if (cols.elementAt(i).getAliasUid() == rowUid) cIndex = i;

            if (rIndex != -1 && cIndex != -1) {
                clearItemConnections(rowUid);
                clearItemConnections(cols.elementAt(cIndex).getUid());

                rows.remove(rIndex);
                cols.remove(cIndex);
                break;
            }
        }

        assert (!(rIndex == -1 || cIndex == -1)) : "could not find same uid in row and column in symmetrical matrix when deleting item";
        this.wasModified = true;
    }


    /**
     * Removes an item from the matrix by its uid and deletes associated connections. This should be called only
     * for non-symmetric datasets
     *
     * @param uid the uid of the item to remove
     */
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


    /**
     * Changes the name of an item of the matrix symmetrically. Both row and column with alias equal to the row
     * uid are changed. Must be called on a symmetric matrix
     *
     * @param rowUid the uid of the row item to be modified
     */
    public void setItemNameSymmetric(int rowUid, String newName) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        int r_index = -1;
        int c_index = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            if (rows.elementAt(i).getUid() == rowUid) r_index = i;
            if (cols.elementAt(i).getAliasUid() == rowUid) c_index = i;

            if (r_index != -1 && c_index != -1) {
                rows.elementAt(r_index).setName(newName);
                cols.elementAt(r_index).setName(newName);
            }
        }
        assert (r_index != -1 && c_index != -1) : "could not find same uid in row and column in symmetrical matrix when changing item name";
        this.wasModified = true;
    }


    /**
     * Changes the name of an item from the matrix by its uid. This should be called only
     * for non-symmetric datasets
     *
     * @param uid the uid of the item to be modified
     */
    public void setItemName(int uid, String newName) {
        for(int i=0; i<this.rows.size(); i++) {     // check to see if uid is in the rows
            if(rows.elementAt(i).getUid() == uid) {
                rows.elementAt(i).setName(newName);
                this.wasModified = true;
                return;
            }
        }
        for(int i=0; i<this.cols.size(); i++) {
            if(cols.elementAt(i).getUid() == uid) {
                cols.elementAt(i).setName(newName);
                this.wasModified = true;
                return;
            }
        }
    }


    /**
     * Sets the sort index of an item of the matrix symmetrically. Both row and column with alias equal to the row
     * uid are changed. Must be called on a symmetric matrix
     *
     * @param rowUid the uid of the row item to be modified
     */
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


    /**
     * Changes the sort index of an item from the matrix by its uid. This should be called only
     * for non-symmetric datasets
     *
     * @param uid the uid of the item to be modified
     */
    public void setSortIndex(int uid, double newIndex) {
        for(int i=0; i<this.rows.size(); i++) {     // check to see if uid is in the rows
            if(rows.elementAt(i).getUid() == uid) {
                rows.elementAt(i).setSortIndex(newIndex);
                this.wasModified = true;
                return;
            }
        }
        for(int i=0; i<this.cols.size(); i++) {
            if(cols.elementAt(i).getUid() == uid) {
                cols.elementAt(i).setSortIndex(newIndex);
                this.wasModified = true;
                return;
            }
        }
    }


    /**
     * Changes the group of an item of the matrix symmetrically. Both row and column with alias equal to the row
     * uid are changed. Must be called on a symmetric matrix
     *
     * @param rowUid the uid of the row item to be modified
     */
    public void setGroupSymmetric(int rowUid, String newGroup) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        int r_index = -1;
        int c_index = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            if (rows.elementAt(i).getUid() == rowUid) r_index = i;
            if (cols.elementAt(i).getAliasUid() == rowUid) c_index = i;

            if (r_index != -1 && c_index != -1) {
                rows.elementAt(r_index).setGroup(newGroup);
                cols.elementAt(r_index).setGroup(newGroup);
            }
        }
        assert (r_index != -1 && c_index != -1) : "could not find same uid in row and column in symmetrical matrix when changing item name";
        this.wasModified = true;
    }


    /**
     * Sets the group of an item from the matrix by its uid. This should be called only
     * for non-symmetric datasets
     *
     * @param uid the uid of the item to be modified
     */
    public void setGroup(int uid, String newGroup) {
        for(int i=0; i<this.rows.size(); i++) {     // check to see if uid is in the rows
            if(rows.elementAt(i).getUid() == uid) {
                rows.elementAt(i).setGroup(newGroup);
                this.wasModified = true;
                return;
            }
        }
        for(int i=0; i<this.cols.size(); i++) {
            if(cols.elementAt(i).getUid() == uid) {
                cols.elementAt(i).setGroup(newGroup);
                this.wasModified = true;
                return;
            }
        }
    }


    /**
     * Returns a connection from row item with rowUid to column item with colUid
     *
     * @param rowUid the uid of the row item in the connection
     * @param colUid the uid of the column item in the connection
     * @return       DSMConnection object of the connection
     */
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


    /**
     * Finds the uids of the connection symmetric to the connection with uids rowUid and colUid. For example,
     * x, y returns y, column uid with alias x
     *
     * @param rowUid the uid of the row item of the connection
     * @param colUid the uid of the column item of the connection
     * @return       Pair of type Integer of symmetric row uid, symmetric col uid
     */
    public Pair<Integer, Integer> getSymmetricConnectionUids(int rowUid, int colUid) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        Integer newRowUid = getItem(colUid).getAliasUid();
        Integer newColUid = null;
        for(DSMItem item : getCols()) {
            if(item.getAliasUid() == rowUid) {
                newColUid = item.getUid();
                break;
            }
        }

        if(newColUid != null && newRowUid != null) {
            return new Pair<Integer, Integer>(newRowUid, newColUid);
        }
        return null;
    }


    /**
     * Modifies a connection with given row item uid and column item uid. First checks to see if the connection
     * exists, if not it creates it. If it does exist, it will update the connection name and weight
     *
     * @param rowUid         the row item uid of the connection
     * @param colUid         the column item uid of the connection
     * @param connectionName the new name of the connection
     * @param weight         the weight of the connection
     */
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


    /**
     * Removes a connection from the matrix connections by rowUid, colUid
     *
     * @param rowUid the connection row item uid
     * @param colUid the connection column item uid
     */
    public void clearConnection(int rowUid, int colUid) {
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getRowUid() == rowUid && connection.getColUid() == colUid) {
                connections.remove(connection);
                break;
            }
        }
        this.wasModified = true;
    }


    /**
     * Modifies a connection symmetrically. Can only be used with symmetric matrices.
     *
     * @param rowUid         the row item uid of one of the connections
     * @param colUid         the column item uid of one of the connections
     * @param connectionName the new name of the connections
     * @param weight         the new weight of the connections
     */
    public void modifyConnectionSymmetrically(int rowUid, int colUid, String connectionName, double weight) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        Pair<Integer, Integer> uids = getSymmetricConnectionUids(rowUid, colUid);
        modifyConnection(rowUid, colUid, connectionName, weight);
        modifyConnection(uids.getKey(), uids.getValue(), connectionName, weight);
        this.wasModified = true;
    }


    /**
     * Sorts the current matrix rows and columns by sort index and modifies all the sort indexes
     * such that they are now 1 to n. Used to make the sort indexes "clean" numbers.
     */
    public void reDistributeSortIndexes() {
        // sort row and columns by sortIndex
        Collections.sort(rows, Comparator.comparing(r -> r.getSortIndex()));
        Collections.sort(cols, Comparator.comparing(c -> c.getSortIndex()));
        for(int i=0; i<rows.size(); i++) {  // reset row sort indexes 1 -> n
            rows.elementAt(i).setSortIndex(i + 1);
        }
        for(int i=0; i<cols.size(); i++) {  // reset col sort indexes 1 -> n
            cols.elementAt(i).setSortIndex(i + 1);
        }
    }


    /**
     * Checks to make sure that connections have a symmetrical counterpart that has an equal name and an equal weight
     *
     * @return list of row uid, column uid of the connections that have issues. Will always be an even length.
     */
    public ArrayList<Pair<Integer, Integer>> findSymmetryErrors() {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        ArrayList<Pair<Integer, Integer>> errors = new ArrayList<>();
        for(DSMConnection connection : connections) {
            Pair<Integer, Integer> symmetricUids = getSymmetricConnectionUids(connection.getRowUid(), connection.getColUid());
            DSMConnection symmetricConnection = getConnection(symmetricUids.getKey(), symmetricUids.getValue());
            if(symmetricConnection == null) {
                errors.add(new Pair<Integer, Integer>(connection.getRowUid(), connection.getColUid()));
                errors.add(new Pair<Integer, Integer>(symmetricUids.getKey(), symmetricUids.getValue()));
            } else if(!symmetricConnection.getConnectionName().equals(connection.getConnectionName()) || symmetricConnection.getWeight() != connection.getWeight()) {
                errors.add(new Pair<Integer, Integer>(connection.getRowUid(), connection.getColUid()));
                errors.add(new Pair<Integer, Integer>(symmetricUids.getKey(), symmetricUids.getValue()));
            }
        }

        return errors;
    }


    /**
     * Creates a 2d ArrayList of the matrix so that it can be displayed. Each cell in the grid is made
     * up of a String, which is the key, and an Object that is different based on the key.
     * Possible keys:
     *   plain_text            : String -> text
     *   plain_text_v          : String -> text
     *   item_name             : Integer -> item uid
     *   item_name_v           : Integer -> item uid
     *   grouping_item         : Integer -> item uid
     *   grouping_item_v       : Integer -> item uid
     *   index_item            : Integer -> item uid
     *   uneditable_connection : null
     *   editable_connection   : Pair<Integer, Integer> -> rowUid, colUid
     *
     * @return 2d ArrayList of matrix
     */
    public ArrayList< ArrayList<Pair< String, Object> > > getGridArray() {
        ArrayList< ArrayList<Pair< String, Object> > > grid = new ArrayList<>();

        // sort row and columns by sortIndex
        Collections.sort(rows, Comparator.comparing(r -> r.getSortIndex()));
        Collections.sort(cols, Comparator.comparing(c -> c.getSortIndex()));

        // create header row
        ArrayList<Pair< String, Object> > row0 = new ArrayList<>();
        row0.add(new Pair<>("plain_text_v", ""));
        row0.add(new Pair<>("plain_text_v", ""));
        row0.add(new Pair<>("plain_text_v", "Column Items"));
        for(DSMItem c : cols) {
            row0.add(new Pair<>("item_name_v", c.getUid()));
        }
        grid.add(row0);

        // create second header row for groupings if it is a non-symmetrical matrix
        if(!isSymmetrical()) {
            ArrayList<Pair<String, Object>> row1 = new ArrayList<Pair<String, Object>>();
            row1.add(new Pair<>("plain_text_v", ""));
            row1.add(new Pair<>("plain_text_v", ""));
            row1.add(new Pair<>("plain_text_v", "Grouping"));
            for (DSMItem c : cols) {
                row1.add(new Pair<>("grouping_item_v", c.getUid()));
            }
            grid.add(row1);
        }

        // create third header row
        ArrayList<Pair< String, Object> > row2 = new ArrayList<Pair< String, Object> >();
        row2.add(new Pair<>("plain_text", "Grouping"));
        row2.add(new Pair<>("plain_text", "Row Items"));
        row2.add(new Pair<>("plain_text", "Re-Sort Index"));
        if(isSymmetrical()) {  // add nothing to this row because it does not need to be displayed to the user
            for(DSMItem c : cols) {
                row2.add(new Pair<>("plain_text", ""));
            }
        } else {
            for(DSMItem c : cols) {
                row2.add(new Pair<>("index_item", c.getUid()));
            }
        }
        grid.add(row2);

        // create rows
        for(DSMItem r : rows) {
            ArrayList<Pair< String, Object> > row = new ArrayList<Pair< String, Object> >();
            row.add(new Pair<>("grouping_item", r.getUid()));
            row.add(new Pair<>("item_name", r.getUid()));
            row.add(new Pair<>("index_item", r.getUid()));
            for(DSMItem c : cols) {  // create connection items for all columns
                if(isSymmetrical() && c.getAliasUid() == r.getUid()) {  // can't have connection to itself in a symmetrical matrix
                    row.add(new Pair<>("uneditable_connection", null));
                } else {
                    row.add(new Pair<>("editable_connection", new Pair<>(r.getUid(), c.getUid())));
                }
            }
            grid.add(row);
        }

        return grid;
    }


    public HashMap<Integer, HashMap<Integer, Double>> propagationAnalysis(Integer startItem, int numLevels, ArrayList<Integer> exclusions, double minWeight, boolean countByWeight) {
        int currentLevel = 1;
        HashMap<Integer, HashMap<Integer, Double>> results = new HashMap<>();
        ArrayList<Integer> dependentConnections = new ArrayList<>();
        dependentConnections.add(startItem);

        // check if start item is a row or column item
        boolean startIsRow;
        if(rows.contains(getItem(startItem))) {
            startIsRow = true;
        } else {
            startIsRow = false;
        }

        while(currentLevel <= numLevels) {
            ArrayList<Integer> newDependentConnections = new ArrayList<>();
            results.put(currentLevel, new HashMap<>());  // add default item

            if((currentLevel % 2 == 1 && startIsRow) || (currentLevel % 2 == 0 && !startIsRow)) {  // currentLevel is odd so choose row
                for(Integer uid : dependentConnections) {  // find dependent connections of each item from the previous level

                    // find connections with uid as the row item
                    for(DSMItem col : cols) {  // iterate over column items finding the ones that match the row
                        DSMConnection conn = getConnection(uid, col.getUid());

                        // define exit conditions
                        if(conn == null) continue;
                        if(conn.getWeight() < minWeight) continue;

                        Integer itemUid = col.getUid();

                        if(results.get(currentLevel).get(itemUid) == null) {
                            results.get(currentLevel).put(itemUid, 0.0);
                        }

                        if(countByWeight) {
                            results.get(currentLevel).put(itemUid, results.get(currentLevel).get(itemUid) + conn.getWeight());
                        } else {
                            results.get(currentLevel).put(itemUid, results.get(currentLevel).get(itemUid) + 1.0);
                        }

                        if(!exclusions.contains(itemUid) && !newDependentConnections.contains(itemUid)) {  // add to next level if not present and not excluded
                            newDependentConnections.add(itemUid);
                        }
                    }
                }
            } else {  // currentLevel is even so choose column
                for(Integer uid : dependentConnections) {  // find dependent connections of each item from the previous level

                    // find connections with uid as the row item
                    for(DSMItem row : rows) {  // iterate over row items finding the ones that match the column
                        DSMConnection conn = getConnection(row.getUid(), uid);

                        // define exit conditions
                        if(conn == null) continue;
                        if(conn.getWeight() < minWeight) continue;

                        Integer itemUid = row.getUid();


                        if(results.get(currentLevel).get(itemUid) == null) {
                            results.get(currentLevel).put(itemUid, 0.0);
                        }

                        if(countByWeight) {
                            results.get(currentLevel).put(itemUid, results.get(currentLevel).get(itemUid) + conn.getWeight());
                        } else {
                            results.get(currentLevel).put(itemUid, results.get(currentLevel).get(itemUid) + 1.0);
                        }

                        if(!exclusions.contains(itemUid) && !newDependentConnections.contains(itemUid)) {  // add to next level if not present and not excluded
                            newDependentConnections.add(itemUid);
                        }
                    }
                }
            }

            dependentConnections.clear();
            dependentConnections = newDependentConnections;
            currentLevel += 1;
        }

        return results;
    }


    /**
     * Clears the wasModified flag. Used for when matrix has been saved to a file
     */
    public void clearWasModifiedFlag() {
        this.wasModified = false;
    }


    /**
     * Returns whether the wasModified flag is set or not. Used to check if matrix has been
     * saved
     *
     * @return if the matrix has been modified, true if it has, false if not
     */
    public boolean getWasModified() {
        return wasModified;
    }


    /**
     * Returns the title metadata information about the matrix
     *
     * @return title data assigned to the matrix
     */
    public String getTitle() {
        return title;
    }


    /**
     * Sets the title metadata information about the matrix
     *
     * @param title the new title data for the matrix
     */
    public void setTitle(String title) {
        this.title = title;
        this.wasModified = true;
    }


    /**
     * Returns the project name metadata information about the matrix
     *
     * @return project name data assigned to the matrix
     */
    public String getProjectName() {
        return projectName;
    }


    /**
     * Sets the project name metadata information about the matrix
     *
     * @param projectName the new project name data for the matrix
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
        this.wasModified = true;
    }


    /**
     * Returns the customer metadata information about the matrix
     *
     * @return customer data assigned to the matrix
     */
    public String getCustomer() {
        return customer;
    }


    /**
     * Sets the customer metadata information about the matrix
     *
     * @param customer the new customer data for the matrix
     */
    public void setCustomer(String customer) {
        this.customer = customer;
        this.wasModified = true;
    }


    /**
     * Returns the version number metadata information about the matrix
     *
     * @return version number data assigned to the matrix
     */
    public String getVersionNumber() {
        return versionNumber;
    }


    /**
     * Sets the version number metadata information about the matrix
     *
     * @param versionNumber the new version number data for the matrix
     */
    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
        this.wasModified = true;
    }
}
