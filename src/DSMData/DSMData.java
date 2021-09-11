package DSMData;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * A class that contains data about a matrix. All operations to a matrix come through
 * this class. Handles both symmetrical and non-symmetrical matrices.
 * TODO: I added double linked aliases, and it could remove some linear searches
 *
 * @author: Aiden Carney
 */
public class DSMData {
    private class MatrixChange {
        private Runnable doFunction;
        private Runnable undoFunction;
        private boolean checkpoint;

        public MatrixChange(Runnable doFunction, Runnable undoFunction, boolean checkpoint) {
            this.doFunction = doFunction;
            this.undoFunction = undoFunction;
            this.checkpoint = checkpoint;
        }

        public void runFunction() {
            doFunction.run();
        }

        public void runUndoFunction() {
            undoFunction.run();
        }

        public void setCheckpoint(boolean isCheckpoint) {
            checkpoint = isCheckpoint;
        }

        public boolean isCheckpoint() {
            return checkpoint;
        }

        public String debug() {
            return undoFunction + " " + doFunction + " " + checkpoint;
        }
    }


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

    private boolean wasModified;

    private Stack<MatrixChange> undoStack;
    private Stack<MatrixChange> redoStack;
    private static final int MAX_UNDO_HISTORY = Integer.MAX_VALUE;  // TODO: undo history should be based on checkpoints


    /**
     * Creates a new DSMData object. Creates no row or column items and metadata are empty strings.
     * There is one grouping, which is the default: "(None)"
     */
    public DSMData() {
        undoStack = new Stack<>();
        redoStack = new Stack<>();

        rows = new Vector<>();
        cols = new Vector<>();
        connections = new Vector<>();
        groupingColors = new HashMap<>();
        groupings = FXCollections.observableSet();

        addGrouping("(None)", Color.color(1.0, 1.0, 1.0));  // add default group

        this.wasModified = true;

        clearStacks();
    }


    /**
     * Copy constructor for DSMData class. Performs a deep copy
     *
     * @param copy DSMData object to copy
     */
    public DSMData(DSMData copy) {
        undoStack = new Stack<>();
        redoStack = new Stack<>();

        rows = new Vector<>();
        for(DSMItem row : copy.getRows()) {
            rows.add(new DSMItem(row));
        }

        cols = new Vector<>();
        for(DSMItem col : copy.getCols()) {
            cols.add(new DSMItem(col));
        }

        connections = new Vector<>();
        for(DSMConnection conn : copy.getConnections()) {
            connections.add(new DSMConnection(conn));
        }

        groupingColors = (HashMap<String, Color>) copy.getGroupingColors().clone();

        groupings = FXCollections.observableSet();
        for(String group : groupingColors.keySet()) {
            groupings.add(group);
        }

        title = copy.getTitle();
        projectName = copy.getProjectName();
        customer = copy.getCustomer();
        versionNumber = copy.getVersionNumber();

        symmetrical = copy.isSymmetrical();
        this.wasModified = true;

        clearStacks();
    }


    /**
     * Adds a change to the undo stack so that it can be handled
     *
     * @param change the change object to handle
     */
    private void addChangeToStack(MatrixChange change) {
        change.runFunction();
        undoStack.push(change);

        if(undoStack.size() > MAX_UNDO_HISTORY) {  // remove bottom item from stack
            undoStack.remove(0);
        }

        this.wasModified = true;
    }


    /**
     * Undoes changes until the last checkpoint (checkpoint is not included). Pops changes from the undo stack and pushes them
     * to the redo stack
     */
    public void undoToCheckpoint() {
        int iter = 0;
        while(true) {  // undo state until the last checkpoint
            if(undoStack.size() > 0) {  // make sure stack is not empty
                MatrixChange change = undoStack.peek();
                if(change.isCheckpoint() && iter > 0) {  // stop before the checkpoint unless it is the first item
                    break;
                }
                undoStack.pop();  // add change to the redo stack

                change.runUndoFunction();
                redoStack.push(change);

                iter += 1;
            } else {
                break;
            }
        }

        this.wasModified = true;
    }


    /**
     * Redoes changes that are on the redo stack to the next checkpoint (checkpoint is included).
     */
    public void redoToCheckpoint() {
        while(true) {
            if(redoStack.size() > 0) {  // make sure stack is not empty
                MatrixChange change = redoStack.peek();
                redoStack.pop();  // add change to the redo stack

                change.runFunction();
                undoStack.push(change);

                if(change.isCheckpoint()) {  // stop after the checkpoint
                    break;
                }
            } else {
                break;
            }
        }

        this.wasModified = true;
    }


    /**
     * Sets the top of the undo stack as a checkpoint and clears the redo stack because redoing after an operation
     * could go horribly wrong
     */
    public void setCurrentStateAsCheckpoint() {
        undoStack.peek().setCheckpoint(true);
        redoStack.clear();
    }


    /**
     * Returns whether there are changes to be undone
     *
     * @return if changes are on the undo stack
     */
    public boolean canUndo() {
        return undoStack.size() > 0;
    }


    /**
     * Returns whether there are changes to be redone
     *
     * @return if changes are on the redo stack
     */
    public boolean canRedo() {
        return redoStack.size() > 0;
    }


    /**
     * clears both undo and redo stacks (useful for instantiation of the class)
     */
    public void clearStacks() {
        undoStack.clear();
        redoStack.clear();
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
     * Deletes the columns of the matrix. Adds changes to the stack. Clears alias uids also
     */
    public void deleteCols() {
        Vector<DSMItem> oldCols = new Vector<>();  // deep clone old column items
        for(DSMItem col : cols) {
            oldCols.add(new DSMItem(col));
        }

        Vector<DSMItem> oldRows = new Vector<>();  // deep clone old column items
        for(DSMItem row : rows) {
            oldRows.add(new DSMItem(row));
        }

        addChangeToStack(new MatrixChange(
            () -> {
                cols.clear();
                for(DSMItem row : rows) {
                    row.setAliasUid(null);
                }
            },
            () -> {
                cols = oldCols;
                rows = oldRows;
            },
            false
        ));
    }


    /**
     * Deletes the rows of the matrix. Adds changes to the stack. Clears alias uids also
     */
    public void deleteRows() {
        Vector<DSMItem> oldCols = new Vector<>();  // deep clone old column items
        for(DSMItem col : cols) {
            oldCols.add(new DSMItem(col));
        }

        Vector<DSMItem> oldRows = new Vector<>();  // deep clone old column items
        for(DSMItem row : rows) {
            oldRows.add(new DSMItem(row));
        }

        addChangeToStack(new MatrixChange(
            () -> {
                rows.clear();
                for(DSMItem col : cols) {
                    col.setAliasUid(null);
                }
            },
            () -> {
                cols = oldCols;
                rows = oldRows;
            },
            false
        ));
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
     * Checks whether an item is a row or not
     *
     * @param uid the uid of the item to check
     * @return    true or false if it is a row or not
     */
    public boolean isRow(int uid) {
        for(DSMItem row : rows) {
            if(row.getUid() == uid) {
                return true;
            }
        }

        return false;
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
     * changes whether or not the matrix is symmetrical. Puts the change on the stack but does not set a checkpoint
     *
     * @param isSymmetrical boolean of if the matrix should be symmetrical or not
     */
    public void setSymmetrical(boolean isSymmetrical) {
        boolean currentState = this.symmetrical;
        addChangeToStack(new MatrixChange(
            () -> symmetrical = isSymmetrical,
            () -> symmetrical = currentState,
            false
        ));
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
     * Adds a new grouping to the matrix. Puts the change on the stack but does not set a checkpoint
     *
     * @param name  the names of the groupings
     * @param color the color associated with the grouping
     */
    public void addGrouping(String name, Color color) {
        addChangeToStack(new MatrixChange(
            () -> {  // do function
                if(color != null) {
                    groupingColors.put(name, color);
                } else {
                    groupingColors.put(name, Color.color(1.0, 1.0, 1.0));
                }
                groupings.add(name);
            },
            () -> {  // undo function
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
            },
            false
        ));
    }


    /**
     * Removes a grouping from the matrix. Puts the change on the stack but does not set a checkpoint
     *
     * @param name the name of the grouping to remove
     */
    public void removeGrouping(String name) {
        Color color = groupingColors.get(name);
        for(DSMItem item : rows) {
            if(item.getGroup().equals(name)) {
                setItemGroup(item, "(None)");
            }
        }
        for(DSMItem item : cols) {
            if(item.getGroup().equals(name)) {
                setItemGroup(item, "(None)");
            }
        }

        addChangeToStack(new MatrixChange(
            () -> {  // do function
                groupingColors.remove(name);
                groupings.remove(name);
            },
            () -> {  // undo function
                if(color != null) {
                    groupingColors.put(name, color);
                } else {
                    groupingColors.put(name, Color.color(1.0, 1.0, 1.0));
                }
                groupings.add(name);
            },
            false
        ));
    }


    /**
     * Removes all groupings from the matrix. Puts the change on the stack but does not set a checkpoint
     */
    public void clearGroupings() {
        HashMap<String, Color> oldGroupingColors = (HashMap<String, Color>) getGroupingColors().clone();

        ObservableSet<String> oldGroupings = FXCollections.observableSet();
        for(String group : groupingColors.keySet()) {
            oldGroupings.add(group);
        }

        for(DSMItem r : rows) {
            setItemGroup(r, "");
        }
        for(DSMItem c : cols) {
            setItemGroup(c, "");
        }

        addChangeToStack(new MatrixChange(
            () -> {  // do function
                groupingColors.clear();
                groupings.clear();
            },
            () -> {  // undo function
                groupingColors = oldGroupingColors;
                groupings = oldGroupings;
            },
            false
        ));
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
     * Renames a grouping and updates all DSMItem objects with the new grouping name. Puts the change on the stack
     * but does not set a checkpoint
     *
     * @param oldName the old name to be changed
     * @param newName the new name of the grouping
     */
    public void renameGrouping(String oldName, String newName) {
        Color color = groupingColors.get(oldName);
        for(DSMItem item : rows) {
            if(item.getGroup().equals(oldName)) {
                setItemGroup(item, newName);
            }
        }
        for(DSMItem item : cols) {
            if(item.getGroup().equals(oldName)) {
                setItemGroup(item, newName);
            }
        }
        removeGrouping(oldName);
        addGrouping(newName, color);
    }


    /**
     * Changes a color of a grouping. Puts the change on the stack but does not set a checkpoint
     *
     * @param name     the name of the grouping to change the color of
     * @param newColor the new color of the grouping
     */
    public void updateGroupingColor(String name, Color newColor) {
        Color oldColor = groupingColors.get(name);
        addChangeToStack(new MatrixChange(
            () -> {  // do function
                groupingColors.remove(name);
                groupingColors.put(name, newColor);
            },
            () -> {  // undo function
                groupingColors.remove(name);
                groupingColors.put(name, oldColor);
            },
            false
        ));
    }


    /**
     * creates and returns a new symmetric item based on the name passed to it. Does not add the change to the stack
     * or add the items to the matrix
     *
     * @param name the name of the item
     * @return     pair of row, column of the created items
     */
    private Pair<DSMItem, DSMItem> createSymmetricItem(String name) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        double index = (int)getRowMaxSortIndex() + 1;  // cast to int to remove the decimal place so that the index will be a whole number
        DSMItem rowItem = new DSMItem(index, name);
        DSMItem colItem = new DSMItem(index, name);
        colItem.setAliasUid(rowItem.getUid());

        return new Pair<>(rowItem, colItem);
    }


    /**
     * creates and returns a new item based on the name passed to it. Does not add the change to the stack
     * or add the item to the matrix
     *
     * @param name the name of the item
     * @return     the created item
     */
    private DSMItem createItem(String name, boolean isRow) {
        double index;
        if(isRow) {
            index = (int) getRowMaxSortIndex() + 1;  // cast to int to remove the decimal place so that the index will be a whole number
        } else {
            index = (int) getColMaxSortIndex() + 1;  // cast to int to remove the decimal place so that the index will be a whole number
        }
        DSMItem item = new DSMItem(index, name);

        return item;
    }


    /**
     * Removes an item from the matrix and clears its connections. This change is not added to the stack, however the call
     * to clear connections does make a change to the stack
     *
     * @param item the item to delete
     */
    private void removeItem(DSMItem item) {
        int index = -1;
        for(int i=0; i<this.rows.size(); i++) {     // check to see if uid is in the rows
            if(rows.elementAt(i).getUid() == item.getUid()) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            rows.remove(index);
        } else {                                   // uid was not in a row, must be in a column
            for(int i=0; i<this.cols.size(); i++) {
                if(cols.elementAt(i).getUid() == item.getUid()) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                cols.remove(index);
            }
        }
        clearItemConnections(item.getUid());
    }


    /**
     * Creates a connection and adds it to the matrix, but does not add the change to the stack
     *
     * @param rowUid         the row item uid
     * @param colUid         the column item uid
     * @param connectionName the name of the connection
     * @param weight         the weight of the connection
     */
    private void createConnection(int rowUid, int colUid, String connectionName, double weight) {
        if(isSymmetrical() && getItem(rowUid).getUid() == getItem(colUid).getAliasUid()) {
            return;
        }
        DSMConnection connection = new DSMConnection(connectionName, weight, rowUid, colUid);
        connections.add(connection);
    }

    private void removeConnection(int rowUid, int colUid) {
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getRowUid() == rowUid && connection.getColUid() == colUid) {
                connections.remove(connection);
                break;
            }
        }
    }

    
    /**
     * Adds an existing item to the matrix as either a row or a column. Puts the change on the stack but does not
     * set a checkpoint
     *
     * @param item  DSMItem object of the item to add
     * @param isRow boolean flag of whether the item is a row or not
     */
    public void addItem(DSMItem item, boolean isRow) {
        addChangeToStack(new MatrixChange(
            () -> {  // do function
                if(isRow) {
                    this.rows.add(item);
                } else {
                    this.cols.add(item);
                }
                if(!groupingColors.containsKey(item.getGroup())) {
                    addGrouping(item.getGroup(), null);
                }
            },
            () -> {  // undo function
                removeItem(item);
            },
            false
        ));
    }


    /**
     * Creates a new item and adds it to the matrix and the stack
     *
     * @param name  the name of the item to create and add
     * @param isRow is the item a row
     */
    public void addItem(String name, boolean isRow) {
        DSMItem item = createItem(name, isRow);
        addItem(item, isRow);
    }

    /**
     * Adds an item to the matrix symmetrically. The symmetrical flag must be set in order to use
     * this function. This function adds both the row and column and creates the alias for the column. Changes
     * to the stack will be made because of this call.
     *
     * @param name the name of the item to be added
     */
    public void addSymmetricItem(String name) {
        Pair<DSMItem, DSMItem> items = createSymmetricItem(name);
        addItem(items.getKey(), true);
        addItem(items.getValue(), false);
    }


    /**
     * Deletes an item from the matrix. Puts the change on the stack but does not set a checkpoint
     *
     * @param item the item to delete
     */
    public void deleteItem(DSMItem item) {
        boolean isRow = rows.contains(item);  // check if the item was a row in case it needs to be added again

        addChangeToStack(new MatrixChange(
            () -> {  // do function
                removeItem(item);
            },
            () -> {  // undo function
                addItem(item, isRow);
            },
            false
        ));
    }


    /**
     * Deletes an item from the matrix. Puts the change on the stack but does not set a checkpoint
     *
     * @param rowItem the row item of the symmetrical items to delete
     */
    public void deleteSymmetricItem(DSMItem rowItem) {
        deleteItem(rowItem);
        for(DSMItem col : cols) {
            if(col.getAliasUid() == rowItem.getUid()) {
                deleteItem(col);
                break;
            }
        }
    }


    /**
     * Removes all connections associated with an item with a given uid. Used for when an item is deleted
     * from the matrix. Adds changes to the stack.
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

        addChangeToStack(new MatrixChange(
            () -> {  // do function
                connections.removeAll(toRemove);
            },
            () -> {  // undo function
                connections.addAll(toRemove);
            },
            false
        ));
    }


    /**
     * Sets the name of an item in the matrix. This method should be called instead of directly modifying the item name
     * because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param item    the item to change the name of
     * @param newName the new name for the item
     */
    public void setItemName(DSMItem item, String newName) {
        String oldName = item.getName();

        addChangeToStack(new MatrixChange(
            () -> {  // do function
                item.setName(newName);
            },
            () -> {  // undo function
                item.setName(oldName);
            },
            false
        ));
    }


    /**
     * Sets the names of a symmetric pair in the matrix. This method should be called instead of directly modifying the
     * item because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param rowItem the item to change the name of
     * @param newName the new name for the item
     */
    public void setItemNameSymmetric(DSMItem rowItem, String newName) {
        setItemName(rowItem, newName);
        for(DSMItem col : cols) {
            if(col.getAliasUid() == rowItem.getUid()) {
                setItemName(col, newName);
                break;
            }
        }
    }


    /**
     * Sets the sort index of an item in the matrix. This method should be called instead of directly modifying the item
     * because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param item     the item to change the name of
     * @param newIndex the new index for the item
     */
    public void setItemSortIndex(DSMItem item, double newIndex) {
        double oldIndex = item.getSortIndex();

        addChangeToStack(new MatrixChange(
            () -> {  // do function
                item.setSortIndex(newIndex);
            },
            () -> {  // undo function
                item.setSortIndex(oldIndex);
            },
            false
        ));
    }


    /**
     * Sets the sort Indices of a symmetric pair in the matrix. This method should be called instead of directly modifying the
     * item because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param rowItem  the row item in the pair to change the name of
     * @param newIndex the new sort index for the item
     */
    public void setItemSortIndexSymmetric(DSMItem rowItem, double newIndex) {
        setItemSortIndex(rowItem, newIndex);
        for(DSMItem col : cols) {
            if(col.getAliasUid() == rowItem.getUid()) {
                setItemSortIndex(col, newIndex);
                break;
            }
        }
    }


    /**
     * Sets the group of an item in the matrix. This method should be called instead of directly modifying the item
     * because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param item     the item to change the name of
     * @param newGroup the new group for the item
     */
    public void setItemGroup(DSMItem item, String newGroup) {
        String oldGroup = item.getGroup();

        addChangeToStack(new MatrixChange(
            () -> {  // do function
                if(!groupingColors.containsKey(newGroup)) {
                    addGrouping(newGroup, null);
                }
                item.setGroup(newGroup);
            },
            () -> {  // undo function
                item.setGroup(oldGroup);
            },
            false
        ));
    }


    /**
     * Sets the group of a symmetric pair in the matrix. This method should be called instead of directly modifying the
     * item groups because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param rowItem  the row item in the pair to change the name of
     * @param newGroup the new group for the item
     */
    public void setItemGroupSymmetric(DSMItem rowItem, String newGroup) {
        setItemGroup(rowItem, newGroup);
        for(DSMItem col : cols) {
            if(col.getAliasUid() == rowItem.getUid()) {
                setItemGroup(col, newGroup);
                break;
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
            if(item.getAliasUid() != null && item.getAliasUid() == rowUid) {
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
     * exists, if not it creates it. If it does exist, it will update the connection name and weight. Cannot be used to
     * delete connections. Puts the change on the stack but does not set a checkpoint.
     *
     * @param rowUid         the row item uid of the connection
     * @param colUid         the column item uid of the connection
     * @param connectionName the new name of the connection
     * @param weight         the weight of the connection
     */
    public void modifyConnection(int rowUid, int colUid, String connectionName, double weight) {
        DSMConnection connection = null;
        String oldName = "";
        double oldWeight = 0.0;
        for(DSMConnection conn : this.connections) {
            if(rowUid == conn.getRowUid() && colUid == conn.getColUid()) {
                connection = conn;
                oldName = conn.getConnectionName();
                oldWeight = conn.getWeight();
                break;
            }
        }
        DSMConnection finalConnection = connection;
        String finalOldName = oldName;
        double finalOldWeight = oldWeight;

        addChangeToStack(new MatrixChange(
            () -> {  // do function
                if(finalConnection == null) {
                    createConnection(rowUid, colUid, connectionName, weight);
                } else {
                    finalConnection.setConnectionName(connectionName);
                    finalConnection.setWeight(weight);
                }
            },
            () -> {  // undo function
                if(finalConnection == null) {
                    removeConnection(rowUid, colUid);
                } else {
                    finalConnection.setConnectionName(finalOldName);
                    finalConnection.setWeight(finalOldWeight);
                }
            },
            false
        ));
    }

    /**
     * Modifies a connection symmetrically. Can only be used with symmetric matrices. Puts the change on the
     * stack but does not set a checkpoint
     *
     * @param rowUid         the row item uid of one of the connections
     * @param colUid         the column item uid of one of the connections
     * @param connectionName the new name of the connections
     * @param weight         the new weight of the connections
     */
    public void modifyConnectionSymmetric(int rowUid, int colUid, String connectionName, double weight) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        Pair<Integer, Integer> uids = getSymmetricConnectionUids(rowUid, colUid);
        modifyConnection(rowUid, colUid, connectionName, weight);
        modifyConnection(uids.getKey(), uids.getValue(), connectionName, weight);
    }


    /**
     * Removes a connection from the matrix connections by rowUid, colUid. Puts the change on the stack but does
     * not set a checkpoint
     *
     * @param rowUid the connection row item uid
     * @param colUid the connection column item uid
     */
    public void deleteConnection(int rowUid, int colUid) {
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getRowUid() == rowUid && connection.getColUid() == colUid) {
                addChangeToStack(new MatrixChange(
                    () -> {  // do function
                        removeConnection(rowUid, colUid);
                    },
                    () -> {  // undo function
                        createConnection(rowUid, colUid, connection.getConnectionName(), connection.getWeight());
                    },
                    false
                ));
                break;
            }
        }
    }


    /**
     * Deletes all connections for a given row. Adds changes to the stack, but does not set a checkpoint
     *
     * @param rowUid the uid of the row to clear the connections of
     */
    public void deleteRowConnections(int rowUid) {
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getRowUid() == rowUid) {
                deleteConnection(connection.getRowUid(), connection.getColUid());
            }
        }
    }


    /**
     * Deletes all connections for a given column. Adds changes to the stack, but does not set a checkpoint
     *
     * @param colUid the uid of the row to clear the connections of
     */
    public void deleteColConnections(int colUid) {
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getColUid() == colUid) {
                deleteConnection(connection.getRowUid(), connection.getColUid());
            }
        }
    }


    /**
     * deletes all connections in the matrix. Adds changes to the stack, but does not set a checkpoint
     */
    public void deleteAllConnections() {
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            deleteConnection(connection.getRowUid(), connection.getColUid());
        }
    }


    /**
     * Inverts a matrix by flipping its rows and columns and switching the connection rows and columns. Adds changes to
     * the stack to be handled, but does not set a checkpoint
     */
    public void invertMatrix() {
        Vector<DSMItem> oldRows = (Vector<DSMItem>)rows.clone();
        Vector<DSMItem> oldCols = (Vector<DSMItem>)cols.clone();
        Vector<DSMConnection> oldConnections = (Vector<DSMConnection>)connections.clone();

        for(DSMConnection conn : oldConnections) {  // these function calls already put a change on the stack so they don't need to be wrapped
            deleteConnection(conn.getRowUid(), conn.getColUid());
            modifyConnection(conn.getColUid(), conn.getRowUid(), conn.getConnectionName(), conn.getWeight());
        }

        addChangeToStack(new MatrixChange(
            () -> {  // do function
                cols = oldRows;
                rows = oldCols;
            },
            () -> {  // undo function
                cols = oldCols;
                rows = oldRows;
            },
            false
        ));
    }


    /**
     * Sorts the current matrix rows and columns by sort index and modifies all the sort Indices
     * such that they are now 1 to n. Used to make the sort Indices "clean" numbers. Puts multiple changes on the
     * stack but does not set any checkpoint.
     */
    public void reDistributeSortIndices() {
        // sort row and columns by sortIndex
        Collections.sort(rows, Comparator.comparing(r -> r.getSortIndex()));
        Collections.sort(cols, Comparator.comparing(c -> c.getSortIndex()));
        for(int i=0; i<rows.size(); i++) {  // reset row sort Indices 1 -> n
            setItemSortIndex(rows.elementAt(i), i + 1);
        }
        for(int i=0; i<cols.size(); i++) {  // reset col sort Indices 1 -> n
            setItemSortIndex(cols.elementAt(i), i + 1);
        }
    }


    /**
     * Sorts the matrix rows and columns by their group and then their current sort index, then distributes new sort
     * Indices 1 to n. Used to make the sort Indices "clean" numbers and make the groups line up. Puts multiple changes on the
     * stack but does not set any checkpoint.
     */
    public void reDistributeSortIndexByGroup() {
        Collections.sort(rows, Comparator.comparing(DSMItem::getGroup).thenComparing(DSMItem::getName));
//        Collections.sort(cols, Comparator.comparing(DSMItem::getGroup).thenComparing(DSMItem::getName));
        Vector<DSMItem> newCols = new Vector<>();

        for(DSMItem row : rows) {
            for(DSMItem col : cols) {
                if(col.getAliasUid() == row.getUid()) {
                    if(!col.getGroup().equals(row.getGroup())) {
                        System.out.println("groups do not match");
                    }
                    newCols.add(col);
                    break;
                }
            }
        }
        cols = newCols;

        for(int i=0; i<rows.size(); i++) {  // reset row sort Indices 1 -> n
            setItemSortIndex(rows.elementAt(i), i + 1);
        }
        for(int i=0; i<cols.size(); i++) {  // reset col sort Indices 1 -> n
            setItemSortIndex(cols.elementAt(i), i + 1);
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
        for(DSMConnection connection : (Vector<DSMConnection>)connections.clone()) {  // use a clone so that there are (hopefully) not concurrency issues
            Pair<Integer, Integer> symmetricUids = getSymmetricConnectionUids(connection.getRowUid(), connection.getColUid());
            if(symmetricUids == null) {
                return errors;
            }

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
    public ArrayList<ArrayList<Pair<String, Object>>> getGridArray() {
        ArrayList<ArrayList<Pair<String, Object>>> grid = new ArrayList<>();

        // sort row and columns by sortIndex
        Collections.sort(rows, Comparator.comparing(r -> r.getSortIndex()));
        Collections.sort(cols, Comparator.comparing(c -> c.getSortIndex()));

        // create header row
        ArrayList<Pair< String, Object> > row0 = new ArrayList<>();
        row0.add(new Pair<>("plain_text_v", ""));
        row0.add(new Pair<>("plain_text_v", ""));
        row0.add(new Pair<>("plain_text_v", "Column Items"));
        for(DSMItem c : cols) {
            row0.add(new Pair<>("item_name_v", c));
        }
        grid.add(row0);

        // create second header row for groupings if it is a non-symmetrical matrix
        if(!isSymmetrical()) {
            ArrayList<Pair<String, Object>> row1 = new ArrayList<Pair<String, Object>>();
            row1.add(new Pair<>("plain_text_v", ""));
            row1.add(new Pair<>("plain_text_v", ""));
            row1.add(new Pair<>("plain_text_v", "Grouping"));
            for (DSMItem c : cols) {
                row1.add(new Pair<>("grouping_item_v", c));
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
                row2.add(new Pair<>("index_item", c));
            }
        }
        grid.add(row2);

        // create rows
        for(DSMItem r : rows) {
            ArrayList<Pair< String, Object> > row = new ArrayList<Pair< String, Object> >();
            row.add(new Pair<>("grouping_item", r));
            row.add(new Pair<>("item_name", r));
            row.add(new Pair<>("index_item", r));
            for(DSMItem c : cols) {  // create connection items for all columns
                if(isSymmetrical() && c.getAliasUid() == r.getUid()) {  // can't have connection to itself in a symmetrical matrix
                    row.add(new Pair<>("uneditable_connection", null));
                } else {
                    row.add(new Pair<>("editable_connection", new Pair<>(r, c)));
                }
            }
            grid.add(row);
        }

        return grid;
    }


    /**
     * Runs propagation analysis for a matrix. Pick a start item and each level find the connections of the items in the
     * previous level. Items that are excluded are added to the count, but not propagated through.
     *
     * @param startItem     the item to start at
     * @param numLevels     number of levels to run
     * @param exclusions    array of item uids to be excluded
     * @param minWeight     minimum weight for item to be included
     * @param countByWeight count by weight or by occurrence
     * @return              HashMap(level : Hashmap(uid, occurrences/weights))
     */
    public HashMap<Integer, HashMap<Integer, Double>> propagationAnalysis(Integer startItem, int numLevels, ArrayList<Integer> exclusions, double minWeight, boolean countByWeight) {
        int currentLevel = 1;
        HashMap<Integer, HashMap<Integer, Double>> results = new HashMap<>();
        ArrayList<Integer> dependentConnections = new ArrayList<>();
        dependentConnections.add(startItem);
        exclusions.add(startItem);

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

                        Integer resultEntryUid;
                        if(isSymmetrical()) {
                            resultEntryUid = col.getAliasUid();
                        } else {
                            resultEntryUid = col.getUid();
                        }

                        if(results.get(currentLevel).get(resultEntryUid) == null) {
                            results.get(currentLevel).put(resultEntryUid, 0.0);
                        }

                        if(countByWeight) {
                            results.get(currentLevel).put(resultEntryUid, results.get(currentLevel).get(resultEntryUid) + conn.getWeight());
                        } else {
                            results.get(currentLevel).put(resultEntryUid, results.get(currentLevel).get(resultEntryUid) + 1.0);
                        }

                        if(!exclusions.contains(resultEntryUid) && !newDependentConnections.contains(resultEntryUid)) {  // add to next level if not present and not excluded
                            newDependentConnections.add(col.getUid());  // add the actual item uid
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
     * Function to calculate the coordination score of a DSM using Fernandez's thesis (https://dsmweborg.files.wordpress.com/2019/05/msc_thebeau.pdf p28-29)
     *
     * @param matrix             The matrix object to calculate the coordination score of
     * @param optimalSizeCluster The optimal size of a cluster, will penalize the IntraClusterCost score if it is not this value
     * @param powcc              A constant to penalize the size of clusters
     * @param calculateByWeight  Calculate the score using the weight of a connection or a default value of 1
     *
     * @return HashMap of the results with keys:
     *     IntraBreakdown
     *     TotalIntraCost
     *     TotalExtraCost
     *     TotalCost
     */
    static public HashMap<String, Object> getCoordinationScore(DSMData matrix, Double optimalSizeCluster, Double powcc, Boolean calculateByWeight) {
        assert matrix.isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        HashMap<String, Object> results = new HashMap<>();

        HashMap<String, Double> intraCostBreakdown = new HashMap<>();
        Double totalIntraCost = 0.0;
        Double totalExtraCost = 0.0;
        for(DSMConnection conn : matrix.getConnections()) {
            if(matrix.getItem(conn.getRowUid()).getGroup().equals(matrix.getItem(conn.getColUid()).getGroup())) {  // row and col groups are the same so add to intra cluster
                Integer clusterSize = 0;  // calculate cluster size
                for(DSMItem row : matrix.getRows()) {
                    if(row.getGroup().equals(matrix.getItem(conn.getRowUid()).getGroup())) {
                        clusterSize += 1;
                    }
                }

                Double intraCost;
                if(calculateByWeight) {
                    intraCost = conn.getWeight() * Math.pow(Math.abs(optimalSizeCluster - clusterSize), powcc);
                } else {
                    intraCost = Math.pow(Math.abs(optimalSizeCluster - clusterSize), powcc);
                }

                if(intraCostBreakdown.get(matrix.getItem(conn.getRowUid()).getGroup()) != null) {
                    intraCostBreakdown.put(matrix.getItem(conn.getRowUid()).getGroup(), intraCostBreakdown.get(matrix.getItem(conn.getRowUid()).getGroup()) + intraCost);
                } else {
                    intraCostBreakdown.put(matrix.getItem(conn.getRowUid()).getGroup(), intraCost);
                }

                totalIntraCost += intraCost;
            } else {
                Integer dsmSize = matrix.getRows().size();
                if(calculateByWeight) {
                    totalExtraCost += conn.getWeight() * Math.pow(dsmSize, powcc);
                } else {
                    totalExtraCost += Math.pow(dsmSize, powcc);
                }
            }
        }

        results.put("IntraBreakdown", intraCostBreakdown);
        results.put("TotalIntraCost", totalIntraCost);
        results.put("TotalExtraCost", totalExtraCost);
        results.put("TotalCost", totalIntraCost + totalExtraCost);

        return results;
    }


    /**
     * Calculates the bids of each item in a given group based on the Thebeau algorithm
     *
     * @param matrix             the matrix to use
     * @param group              the group in the matrix to use
     * @param optimalSizeCluster optimal cluster size that will receive no penalty
     * @param powdep             exponential to emphasize connections
     * @param powbid             exponential to penalize non-optimal cluster size
     * @param calculateByWeight  calculate bid by weight or occurrence
     *
     * @return HashMap of rowUid and bid for the given group
     */
    static public HashMap<Integer, Double> calculateClusterBids(DSMData matrix, String group, Double optimalSizeCluster, Double powdep, Double powbid, Boolean calculateByWeight) {
        assert matrix.isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        HashMap<Integer, Double> bids = new HashMap<>();

        Integer clusterSize = 0;
        for(DSMItem row : matrix.getRows()) {
            if(row.getGroup().equals(group)) {
                clusterSize += 1;
            }
        }

        for(DSMItem row : matrix.getRows()) {  // calculate bid of each item in the matrix for the given cluster
            Double inout = 0.0;  // sum of DSM interactions of the item with each of the items in the cluster

            for(DSMItem col : matrix.getCols()) {
                if(col.getGroup().equals(group) && col.getAliasUid() != row.getUid()) {  // make connection a part of inout score
                    DSMConnection conn = matrix.getConnection(row.getUid(), col.getUid());
                    if(calculateByWeight && conn != null) {
                        inout += conn.getWeight();
                    } else if(conn != null) {
                        inout += 1;
                    }
                }
            }

            Double clusterBid = Math.pow(inout, powdep) / Math.pow(Math.abs(optimalSizeCluster - clusterSize), powbid);
            bids.put(row.getUid(), clusterBid);
        }

        return bids;
    }


    /**
     * Runs Thebeau's matrix clustering algorithm based on his 2001 research paper (https://dsmweborg.files.wordpress.com/2019/05/msc_thebeau.pdf)
     *
     * Original Algorithm Steps (Directly from the paper):
     * 1. Each element is initially placed in its own cluster
     * 2. Calculate the Coordination Cost of the Cluster Matrix
     * 3. Randomly choose an element
     * 4. Calculate bid from all clusters for the selected element
     * 5. Randomly choose a number between 1 and rand_bid (algorithm parameter)
     * 6. Calculate the total Coordination Cost if the selected element becomes a member of the cluster with highest bid (use second highest bid if step 5 is equal to rand_bid)
     * 7. Randomly choose a number between I and rand_accept (algorithm parameter)
     * 8. If new Coordination Cost is lower than the old coordination cost or the number chosen in step 7 is equal to rand_accept, make the change permanent otherwise make no changes
     * 9. Go back to Step 3 until repeated a set number of times
     *
     * @param inputMatrix        matrix to run the algorithm on
     * @param optimalSizeCluster a constant to penalize clusters not of this size
     * @param powdep             constant to emphasize interactions
     * @param powbid             constant to penalize cluster size when bidding
     * @param powcc              constant to penalize size of cluster in cost calculation
     * @param randBid            constant to determine how often to perform an action based on the second highest bid
     * @param randAccept         constant to determine how often to perform a not necessarily optimal action
     * @param calculateByWeight  calculate scores and bidding by weight or by number of occurrences
     * @param numLevels          number of iterations
     * @param randSeed           seed for random number generator
     * @param debug              debug to stdout
     * @return                   DSMData object of the new clustered matrix
     */
    static public DSMData thebeauAlgorithm(DSMData inputMatrix, Double optimalSizeCluster, Double powdep, Double powbid, Double powcc, Double randBid, Double randAccept, Boolean calculateByWeight, int numLevels, long randSeed, boolean debug) {
        assert inputMatrix.isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";
        Random generator = new Random(randSeed);

        // place each element in the matrix in its own cluster
        DSMData matrix = new DSMData(inputMatrix);
        assert matrix != inputMatrix && !matrix.equals(inputMatrix): "matrices are equal and they should not be";
        matrix.clearGroupings();  // groups will be re-distributed

        double h = 0.2423353;  // use random start value for color generation
        for(int i = 0; i < matrix.getRows().size(); i++) {
            matrix.setItemGroupSymmetric(matrix.getRows().elementAt(i), "G" + i);
            h += 0.618033988749895;  // golden_ratio_conjugate, this is a part of the golden ratio method for generating unique colors
            h %= 1;
            java.awt.Color hsvColor = java.awt.Color.getHSBColor((float)h, (float)0.5, (float)0.95);

            double r = hsvColor.getRed() / 255.0;
            double g = hsvColor.getGreen() / 255.0;
            double b = hsvColor.getBlue() / 255.0;
            matrix.updateGroupingColor("G" + i, Color.color(r, g, b));
        }

        // save the best solution
        DSMData bestSolution = new DSMData(matrix);

        // calculate initial coordination cost
        double coordinationCost = (Double)getCoordinationScore(matrix, optimalSizeCluster, powcc, calculateByWeight).get("TotalCost");

        String debugString = "iteration,start time, elapsed time,coordination score\n";
        Instant absStart = Instant.now();

        for(int i=0; i < numLevels; i++) {  // iterate numLevels times
            Instant start = Instant.now();

            // choose an element from the matrix
            int n = (int)(generator.nextDouble() * (matrix.getRows().size() - 1));  // double from 0 to 1.0 multiplied by max index cast to integer
            DSMItem item = matrix.getRows().elementAt(n);
            // calculate bids
            HashMap<String, Double> bids = new HashMap<>();
            for (String group : matrix.getGroupings()) {
                double bid = matrix.calculateClusterBids(matrix, group, optimalSizeCluster, powdep, powbid, calculateByWeight).get(item.getUid());
                bids.put(group, bid);
            }

            // choose a number between 0 and randBid to determine if it should make a suboptimal change
            DSMData tempMatrix = new DSMData(matrix);
            item = tempMatrix.getRows().elementAt(n);  // update item to the item from the new matrix so that it is not modifying a copy
            int nBid = (int) (generator.nextDouble() * (randBid + 1));  // add one to randBid because with truncation nBid will never be equal to randBid

            // find if the change is optimal
            String highestBidder = bids.entrySet().iterator().next().getKey();  // start with a default value so comparison doesn't throw NullPointerException
            if (nBid == randBid) {  // assign item group to second highest bidder
                String secondHighestBidder = "";
                for (Map.Entry<String, Double> entry : bids.entrySet()) {
                    if (Double.compare(entry.getValue(), bids.get(highestBidder)) >= 0) {
                        highestBidder = entry.getKey();
                        secondHighestBidder = highestBidder;
                    } else if (Double.compare(entry.getValue(), bids.get(secondHighestBidder)) >= 0) {
                        secondHighestBidder = entry.getKey();
                    }
                }
                tempMatrix.setItemGroupSymmetric(item, secondHighestBidder);

            } else {  // assign to highest bidder
                for (Map.Entry<String, Double> entry : bids.entrySet()) {
                    if (Double.compare(entry.getValue(), bids.get(highestBidder)) >= 0) {
                        highestBidder = entry.getKey();
                    }
                }
                tempMatrix.setItemGroupSymmetric(item, highestBidder);
            }

            // choose a number between 0 and randAccept to determine if change is permanent regardless of it being optimal
            int nAccept = (int) (generator.nextDouble() * (randAccept + 1));  // add one to randAccept because with truncation nAccept will never be equal to randAccept
            Double newCoordinationScore = (Double) getCoordinationScore(tempMatrix, optimalSizeCluster, powcc, calculateByWeight).get("TotalCost");

            if (nAccept == randAccept || newCoordinationScore < coordinationCost) {  // make the change permanent
                coordinationCost = newCoordinationScore;
                matrix = new DSMData(tempMatrix);

                if (coordinationCost < (Double) getCoordinationScore(bestSolution, optimalSizeCluster, powcc, calculateByWeight).get("TotalCost")) {  // save the new solution as the best one
                    bestSolution = new DSMData(matrix);
                }
            }


            String startTime = String.valueOf(Duration.between(absStart, start).toMillis());
            String elapsedTime = String.valueOf(Duration.between(start, Instant.now()).toMillis());
            debugString += i + "," + startTime + "," + elapsedTime + "," + newCoordinationScore + "\n";
        }

        if(debug) {
            System.out.println(debugString);
        }

        return bestSolution;
    }


    /**
     * Clears the wasModified flag. Used for when matrix has been saved to a file. Does not add changes to the stack
     */
    public void clearWasModifiedFlag() {
        this.wasModified = false;
    }


    /**
     * Sets the was modified flag. Does not add changes to the stack
     */
    public void setWasModified() {
        wasModified = true;
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
     * Sets the title metadata information about the matrix. Puts changes on the stack, but does not set a checkpoint.
     *
     * @param title the new title data for the matrix
     */
    public void setTitle(String title) {
        String currentState = this.title;
        addChangeToStack(new MatrixChange(
            () -> this.title = title,
            () -> this.title = currentState,
            false
        ));
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
     * Sets the project name metadata information about the matrix. Puts changes on the stack, but does not set a checkpoint.
     *
     * @param projectName the new project name data for the matrix
     */
    public void setProjectName(String projectName) {
        String currentState = this.projectName;
        addChangeToStack(new MatrixChange(
            () -> this.projectName = projectName,
            () -> this.projectName = currentState,
            false
        ));
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
     * Sets the customer metadata information about the matrix. Puts changes on the stack, but does not set a checkpoint.
     *
     * @param customer the new customer data for the matrix
     */
    public void setCustomer(String customer) {
        String currentState = this.customer;
        addChangeToStack(new MatrixChange(
            () -> this.customer = customer,
            () -> this.customer = currentState,
            false
        ));
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
     * Sets the version number metadata information about the matrix. Puts changes on the stack, but does not set a checkpoint.
     *
     * @param versionNumber the new version number data for the matrix
     */
    public void setVersionNumber(String versionNumber) {
        String currentState = this.versionNumber;
        addChangeToStack(new MatrixChange(
            () -> this.versionNumber = versionNumber,
            () -> this.versionNumber = currentState,
            false
        ));
    }
}
