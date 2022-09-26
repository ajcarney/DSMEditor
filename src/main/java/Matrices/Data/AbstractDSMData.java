package Matrices.Data;

import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMInterfaceType;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.RenderMode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;

import java.util.*;


/**
 * A class that contains generic data and functions about a matrix. All operations to a matrix come through
 * this class. Will be used as a base class for all other types of matrices. All matrices that are to
 * be used by the application must extend from this class
 *
 * @author: Aiden Carney
 */
public abstract class AbstractDSMData {
    protected static class MatrixChange {
        private final Runnable doFunction;
        private final Runnable undoFunction;
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

    }

    protected Vector<DSMItem> rows;
    protected Vector<DSMItem> cols;
    protected Vector<DSMConnection> connections;
    protected HashMap<String, Vector<DSMInterfaceType>> interfaceTypes;

    protected StringProperty title = new SimpleStringProperty("");
    protected StringProperty projectName = new SimpleStringProperty("");
    protected StringProperty customer = new SimpleStringProperty("");
    protected StringProperty versionNumber = new SimpleStringProperty("");

    private final BooleanProperty wasModified = new SimpleBooleanProperty(false);
    protected Stack<MatrixChange> undoStack;
    protected Stack<MatrixChange> redoStack;
    protected static final int MAX_UNDO_HISTORY = Integer.MAX_VALUE;  // TODO: undo history should be based on checkpoints and not this big


//region Constructors
    /**
     * Creates a new SymmetricDSMData object. Creates no row or column items and metadata are empty strings.
     * There is one grouping, which is the default: "(None)"
     */
    public AbstractDSMData() {
        undoStack = new Stack<>();
        redoStack = new Stack<>();

        rows = new Vector<>();
        cols = new Vector<>();
        connections = new Vector<>();
        interfaceTypes = new HashMap<>();

        setWasModified();

        clearStacks();
    }


    /**
     * Copy constructor for AbstractDSMData class. Performs a deep copy
     *
     * @param copy AbstractDSMData object to copy
     */
    public AbstractDSMData(AbstractDSMData copy) {
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

        interfaceTypes = new HashMap<>();
        for(Map.Entry<String, Vector<DSMInterfaceType>> interfaces : copy.getInterfaceTypes().entrySet()) {
            Vector<DSMInterfaceType> newInterfaceTypes = new Vector<>();
            for(DSMInterfaceType i : interfaces.getValue()) {
                newInterfaceTypes.add(new DSMInterfaceType(i));
            }
            interfaceTypes.put(interfaces.getKey(), newInterfaceTypes);
        }

        connections = new Vector<>();
        for(DSMConnection conn : copy.getConnections()) {
            connections.add(new DSMConnection(conn));
        }

        title = copy.getTitleProperty();
        projectName = copy.getProjectNameProperty();
        customer = copy.getCustomerProperty();
        versionNumber = copy.getVersionNumberProperty();

        setWasModified();

        clearStacks();
    }


    /**
     * Method to clone this object type
     *
     * @return  the deep copy of the object
     */
    public abstract AbstractDSMData createCopy();
//endregion


//region Undo Functionality Methods
    /**
     * Adds a change to the undo stack so that it can be handled
     *
     * @param change the change object to handle
     */
    protected final void addChangeToStack(MatrixChange change) {
        change.runFunction();
        undoStack.push(change);

        if(undoStack.size() > MAX_UNDO_HISTORY) {  // remove bottom item from stack
            undoStack.remove(0);
        }

        setWasModified();
    }


    /**
     * Undoes changes until the last checkpoint (checkpoint is not included). Pops changes from the undo stack and pushes them
     * to the redo stack
     */
    public final void undoToCheckpoint() {
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

        setWasModified();
    }


    /**
     * Redoes changes that are on the redo stack to the next checkpoint (checkpoint is included).
     */
    public final void redoToCheckpoint() {
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

        setWasModified();
    }


    /**
     * Sets the top of the undo stack as a checkpoint and clears the redo stack because redoing after an operation
     * could go horribly wrong
     */
    public final void setCurrentStateAsCheckpoint() {
        if (!undoStack.isEmpty()) {
            undoStack.peek().setCheckpoint(true);
        }
        redoStack.clear();
    }


    /**
     * Returns whether there are changes to be undone
     *
     * @return if changes are on the undo stack
     */
    public final boolean canUndo() {
        return undoStack.size() > 0;
    }


    /**
     * Returns whether there are changes to be redone
     *
     * @return if changes are on the redo stack
     */
    public final boolean canRedo() {
        return redoStack.size() > 0;
    }


    /**
     * clears both undo and redo stacks (useful for instantiation of the class)
     */
    public final void clearStacks() {
        undoStack.clear();
        redoStack.clear();
    }


    /**
     * Clears the wasModified flag. Used for when matrix has been saved to a file. Does not add changes to the stack
     */
    public final void clearWasModifiedFlag() {
        this.wasModified.set(false);
    }


    /**
     * Sets the was modified flag. Does not add changes to the stack
     */
    public final void setWasModified() {
        wasModified.set(true);
    }


    /**
     * Returns whether the wasModified flag is set or not. Used to check if matrix has been
     * saved
     *
     * @return if the matrix has been modified, true if it has, false if not
     */
    public final boolean getWasModified() {
        return wasModified.getValue();
    }


    /**
     * @return  the BooleanProperty of the wasModified flag so that things can be bound to it
     */
    public final BooleanProperty getWasModifiedProperty() {
        return wasModified;
    }
//endregion


//region Protected Helper functions (makes changes but do not add them to the change stack)
    /**
     * Removes an item from the matrix and clears its connections. This change is not added to the stack, however the call
     * to clear connections does make a change to the stack
     *
     * @param item the item to delete
     */
    protected final void removeItem(DSMItem item) {
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
     * @param interfaces     the interfaces for the connection
     */
    protected void createConnection(int rowUid, int colUid, String connectionName, double weight, ArrayList<DSMInterfaceType> interfaces) {
        if(isRow(rowUid) && isCol(colUid)) {
            DSMConnection connection = new DSMConnection(connectionName, weight, rowUid, colUid, interfaces);
            connections.add(connection);
        }
    }


    /**
     * Removes a connection element from the connections list, but does not add the change to the stack
     *
     * @param rowUid        the row item uid
     * @param colUid        the col item uid
     */
    protected final void removeConnection(int rowUid, int colUid) {
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getRowUid() == rowUid && connection.getColUid() == colUid) {
                connections.remove(connection);
                break;
            }
        }
    }
//endregion


//region Getters for matrix data (rows, cols, items)
    /**
     * Returns the rows in a mutable way
     *
     * @return a vector of the items declared as rows
     */
    public final Vector<DSMItem> getRows() {
        return rows;
    }


    /**
     * Returns the columns in a mutable way
     *
     * @return a vector of the items declared as columns
     */
    public final Vector<DSMItem> getCols() {
        return cols;
    }


    /**
     * Finds an item by uid and returns it. It can be either a row item or a column item
     *
     * @param uid the uid of the item to return
     * @return    DSMItem of the item with uid
     */
    public final DSMItem getItem(int uid) {
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
        throw new NoSuchElementException();
    }


    /**
     * Searches the rows for an uid
     *
     * @param rowUid  the uid of the row item to look for
     * @return        the row item
     */
    public final DSMItem getRowItem(int rowUid) {
        for(DSMItem row : rows) {
            if(row.getUid() == rowUid) {
                return row;
            }
        }
        throw new NoSuchElementException();
    }


    /**
     * Searches the columns for an uid
     *
     * @param colUid  the uid of the column item to look for
     * @return        the col item
     */
    public final DSMItem getColItem(int colUid) {
        for(DSMItem col : cols) {
            if(col.getUid() == colUid) {
                return col;
            }
        }
        throw new NoSuchElementException();
    }


    /**
     * Finds an item by an alias uid and returns it. It can be either a row item or a column item
     *
     * @param uid the alias uid of the item to return
     * @return    DSMItem of the item with uid
     */
    public final DSMItem getItemByAlias(int uid) {
        for(DSMItem row : rows) {
            if(row.getAliasUid() == uid) {
                return row;
            }
        }
        for(DSMItem col : cols) {
            if(col.getAliasUid() == uid) {
                return col;
            }
        }
        throw new NoSuchElementException();
    }


    /**
     * Returns connections in a mutable way
     *
     * @return a vector of the connections in the matrix
     */
    public final Vector<DSMConnection> getConnections() {
        return connections;
    }


    /**
     * @return  List of all the interface groupings
     */
    public final ArrayList<String> getInterfaceGroupings() {
        return new ArrayList<>(interfaceTypes.keySet());
    }


    /**
     * @return  HashMap of the interface groupings and the associated interface types
     */
    public final HashMap<String, Vector<DSMInterfaceType>> getInterfaceTypes() {
        return interfaceTypes;
    }


    /**
     * Returns a connection from row item with rowUid to column item with colUid
     *
     * @param rowUid the uid of the row item in the connection
     * @param colUid the uid of the column item in the connection
     * @return       DSMConnection object of the connection
     */
    public final DSMConnection getConnection(int rowUid, int colUid) {
        DSMConnection connection = null;
        for(DSMConnection conn : connections) {
            if(conn.getRowUid() == rowUid && conn.getColUid() == colUid) {
                connection = conn;
                break;
            }
        }

        return connection;
    }
//endregion


//region Advanced Getters
    /**
     * Checks whether a uid is a row or not
     *
     * @param uid the uid of the item to check
     * @return    true or false if it is a row or not
     */
    public final boolean isRow(int uid) {
        for(DSMItem row : rows) {
            if(row.getUid() == uid) {
                return true;
            }
        }

        return false;
    }


    /**
     * Checks whether a uid is a column or not
     *
     * @param uid the uid of the item to check
     * @return    true or false if it is a column or not
     */
    public final boolean isCol(int uid) {
        for(DSMItem col : cols) {
            if(col.getUid() == uid) {
                return true;
            }
        }

        return false;
    }


    /**
     * Finds the maximum sort index of the rows by performing a linear search.
     *
     * @return the maximum sort index of the row items
     */
    public final double getRowMaxSortIndex() {
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
    public final double getColMaxSortIndex() {
        double index = 0;
        for(DSMItem col : cols) {
            if(col.getSortIndex() > index) {
                index = col.getSortIndex();
            }
        }
        return index;
    }
//endregion


//region Public Modifier functions (These functions add changes to the stack)
    /**
     * Adds an existing item to the matrix as either a row or a column. Puts the change on the stack but does not
     * set a checkpoint. This method can be overridden
     *
     * @param item  DSMItem object of the item to add
     * @param isRow boolean flag of whether the item is a row or not
     */
    public void addItem(DSMItem item, boolean isRow) {
        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    if (isRow) {
                        this.rows.add(item);
                    } else {
                        this.cols.add(item);
                    }
                },
                () -> {  // undo function
                    removeItem(item);
                },
                false
        ));
    }


    /**
     * creates and returns a new item based on the name passed to it. Does not add the change to the stack
     * or add the item to the matrix
     *
     * @param name the name of the item
     */
    public void createItem(String name, boolean isRow) {
        double index;
        if(isRow) {
            index = (int) getRowMaxSortIndex() + 1;  // cast to int to remove the decimal place so that the index will be a whole number
        } else {
            index = (int) getColMaxSortIndex() + 1;  // cast to int to remove the decimal place so that the index will be a whole number
        }
        DSMItem item = new DSMItem(index, name);

        addItem(item, isRow);
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
                    if (isRow) {
                        this.rows.add(item);
                    } else {
                        this.cols.add(item);
                    }
                },
                false
        ));
    }


    /**
     * Sets the name of an item in the matrix. This method should be called instead of directly modifying the item name
     * because this method puts the change on the stack but does not set a checkpoint. This method can be overridden
     *
     * @param item    the item to change the name of
     * @param newName the new name for the item
     */
    public void setItemName(DSMItem item, String newName) {
        String oldName = item.getName().getValue();

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
     * Sets the sort index of an item in the matrix. This method should be called instead of directly modifying the item
     * because this method puts the change on the stack but does not set a checkpoint. This method can be overridden
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
     * Adds a new grouping of interface types. Puts the change on the stack but does not set a checkpoint.
     *
     * @param name  the name of the grouping
     */
    public final void addInterfaceTypeGrouping(String name) {
        if(!interfaceTypes.containsKey(name)) {
            addChangeToStack(new MatrixChange(
                    () -> {  // do function
                        interfaceTypes.put(name, new Vector<>());
                    },
                    () -> {  // undo function
                        interfaceTypes.remove(name);
                    },
                    false
            ));
        }
    }


    /**
     * Adds a new interface type of a given grouping to the matrix. Puts the change on the stack but does not
     * set a checkpoint
     *
     * @param interfaceTypeGrouping  the name of the grouping for this interface type. Adds the grouping if not present
     * @param interfaceType          the interface type to add
     */
    public final void addInterface(String interfaceTypeGrouping, DSMInterfaceType interfaceType) {
        if(!interfaceTypes.containsKey(interfaceTypeGrouping)) {
            addInterfaceTypeGrouping(interfaceTypeGrouping);
        }

        if(!interfaceTypes.get(interfaceTypeGrouping).contains(interfaceType)) {
            addChangeToStack(new MatrixChange(
                    () -> {  // do function
                        interfaceTypes.get(interfaceTypeGrouping).add(interfaceType);
                    },
                    () -> {  // undo function
                        interfaceTypes.get(interfaceTypeGrouping).remove(interfaceType);
                    },
                    false
            ));
        }
    }


    /**
     * Removes an existing grouping of interface types. Puts the change on the stack but does not set a checkpoint.
     *
     * @param name  the name of the grouping
     */
    public final void removeInterfaceTypeGrouping(String name) {
        if(interfaceTypes.containsKey(name)) {
            Vector<DSMInterfaceType> oldInterfaces = interfaceTypes.get(name);

            addChangeToStack(new MatrixChange(
                    () -> {  // do function
                        interfaceTypes.remove(name);
                    },
                    () -> {  // undo function
                        interfaceTypes.put(name, oldInterfaces);
                    },
                    false
            ));
        }
    }


    /**
     * Removes an existing interface type of a given grouping from the matrix. Puts the change on the stack but does not
     * set a checkpoint
     *
     * @param interfaceTypeGrouping  the name of the grouping for this interface type. Adds the grouping if not present
     * @param interfaceType          the interface type to add
     */
    public final void removeInterface(String interfaceTypeGrouping, DSMInterfaceType interfaceType) {
        if(!interfaceTypes.containsKey(interfaceTypeGrouping)) {  // don't do anything if not a valid grouping
            return;
        }

        if(interfaceTypes.get(interfaceTypeGrouping).contains(interfaceType)) {
            addChangeToStack(new MatrixChange(
                    () -> {  // do function
                        interfaceTypes.get(interfaceTypeGrouping).remove(interfaceType);
                    },
                    () -> {  // undo function
                        interfaceTypes.get(interfaceTypeGrouping).add(interfaceType);
                    },
                    false
            ));
        }
    }


    /**
     * Renames an interface grouping. Puts the change on the stack but does not set a checkpoint.
     *
     * @param oldName  the interface grouping whose name should be changed
     * @param newName  the new name for the interface grouping
     */
    public void renameInterfaceTypeGrouping(String oldName, String newName) {
        assert(interfaceTypes.get(oldName) != null);

        Vector<DSMInterfaceType> interfaces = interfaceTypes.get(oldName);
        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    interfaceTypes.remove(oldName);
                    interfaceTypes.put(newName, interfaces);
                },
                () -> {  // undo function
                    interfaceTypes.remove(newName);
                    interfaceTypes.put(oldName, interfaces);
                },
                false
        ));
    }


    /**
     * Renames an interface type. Puts the change on the stack but does not set a checkpoint.
     *
     * @param interfaceType          the interface who's name should be changed
     * @param newName                the new name for the interface grouping
     */
    public void renameInterfaceType(DSMInterfaceType interfaceType, String newName) {
        String oldName = interfaceType.getName();
        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    interfaceType.setName(newName);
                },
                () -> {  // undo function
                    interfaceType.setName(oldName);
                },
                false
        ));
    }


    /**
     * Renames an interface type's abbreviation. Puts the change on the stack but does not set a checkpoint.
     *
     * @param interfaceType    the interface who's abbreviation should be changed
     * @param newAbbreviation  the new abbreviation for the interface grouping
     */
    public void updateInterfaceTypeAbbreviation(DSMInterfaceType interfaceType, String newAbbreviation) {
        String oldName = interfaceType.getName();
        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    interfaceType.setAbbreviation(newAbbreviation);
                },
                () -> {  // undo function
                    interfaceType.setName(oldName);
                },
                false
        ));
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
     * @param interfaces     the interfaces for the connection
     */
    public final void modifyConnection(int rowUid, int colUid, String connectionName, double weight, ArrayList<DSMInterfaceType> interfaces) {
        DSMConnection connection = null;
        String oldName = "";
        double oldWeight = 0.0;
        ArrayList<DSMInterfaceType> oldInterfaces = new ArrayList<>();
        for(DSMConnection conn : this.connections) {
            if(rowUid == conn.getRowUid() && colUid == conn.getColUid()) {
                connection = conn;
                oldName = conn.getConnectionName();
                oldWeight = conn.getWeight();
                oldInterfaces = conn.getInterfaces();

                break;
            }
        }

        DSMConnection finalConnection = connection;
        String finalOldName = oldName;
        double finalOldWeight = oldWeight;
        ArrayList<DSMInterfaceType> finalOldInterfaces = oldInterfaces;
        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    if (finalConnection == null) {
                        createConnection(rowUid, colUid, connectionName, weight, interfaces);
                    } else {
                        finalConnection.setConnectionName(connectionName);
                        finalConnection.setWeight(weight);
                        finalConnection.setInterfaces(interfaces);
                    }
                },
                () -> {  // undo function
                    if (finalConnection == null) {
                        removeConnection(rowUid, colUid);
                    } else {
                        finalConnection.setConnectionName(finalOldName);
                        finalConnection.setWeight(finalOldWeight);
                        finalConnection.setInterfaces(finalOldInterfaces);
                    }
                },
                false
        ));
    }


    /**
     * Removes all connections associated with an item with a given uid. Used for when an item is deleted
     * from the matrix. Adds changes to the stack.
     *
     * @param uid the uid of the item that will be looked for when removing connections
     */
    public final void clearItemConnections(int uid) {
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
     * Removes a connection from the matrix connections by rowUid, colUid. Puts the change on the stack but does
     * not set a checkpoint
     *
     * @param rowUid the connection row item uid
     * @param colUid the connection column item uid
     */
    public final void deleteConnection(int rowUid, int colUid) {
        for(DSMConnection connection : connections) {     // check to see if uid is in the rows
            if(connection.getRowUid() == rowUid && connection.getColUid() == colUid) {
                addChangeToStack(new MatrixChange(
                        () -> {  // do function
                            removeConnection(rowUid, colUid);
                        },
                        () -> {  // undo function
                            createConnection(rowUid, colUid, connection.getConnectionName(), connection.getWeight(), connection.getInterfaces());
                        },
                        false
                ));
                break;
            }
        }
    }


    /**
     * deletes all connections in the matrix. Adds changes to the stack, but does not set a checkpoint
     */
    public final void deleteAllConnections() {
        Vector<DSMConnection> connectionsClone = new Vector<>(connections);
        for(DSMConnection connection : connectionsClone) {     // check to see if uid is in the rows
            deleteConnection(connection.getRowUid(), connection.getColUid());
        }
    }


    /**
     * Inverts a matrix by flipping its rows and columns and switching the connection rows and columns. Adds changes to
     * the stack to be handled, but does not set a checkpoint
     */
    public void transposeMatrix() {
        Vector<DSMItem> oldRows = new Vector<>(rows);
        Vector<DSMItem> oldCols = new Vector<>(cols);
        Vector<DSMConnection> oldConnections = new Vector<>(connections);

        for(DSMConnection conn : oldConnections) {  // these function calls already put a change on the stack so they don't need to be wrapped
            deleteConnection(conn.getRowUid(), conn.getColUid());
            modifyConnection(conn.getColUid(), conn.getRowUid(), conn.getConnectionName(), conn.getWeight(), conn.getInterfaces());
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
        rows.sort(Comparator.comparing(DSMItem::getSortIndex));
        cols.sort(Comparator.comparing(DSMItem::getSortIndex));
        for(int i=0; i<rows.size(); i++) {  // reset row sort Indices 1 -> n
            setItemSortIndex(rows.elementAt(i), i + 1);
        }
        for(int i=0; i<cols.size(); i++) {  // reset col sort Indices 1 -> n
            setItemSortIndex(cols.elementAt(i), i + 1);
        }
    }
//endregion


//region Metadata getters and setters
    /**
     * Returns the title metadata information about the matrix
     *
     * @return title data assigned to the matrix
     */
    public final String getTitle() {
        return title.getValue();
    }


    /**
     * Returns the title metadata information about the matrix
     *
     * @return title data property assigned to the matrix
     */
    public final StringProperty getTitleProperty() {
        return title;
    }


    /**
     * Sets the title metadata information about the matrix. Puts changes on the stack, but does not set a checkpoint.
     *
     * @param title the new title data for the matrix
     */
    public final void setTitle(String title) {
        String currentTitle = this.title.getValue();
        addChangeToStack(new MatrixChange(
                () -> this.title.set(title),
                () -> this.title.set(currentTitle),
                false
        ));
    }


    /**
     * Returns the project name metadata information about the matrix
     *
     * @return project name data assigned to the matrix
     */
    public final String getProjectName() {
        return projectName.getValue();
    }


    /**
     * Returns the project name metadata information about the matrix
     *
     * @return project name property assigned to the matrix
     */
    public final StringProperty getProjectNameProperty() {
        return projectName;
    }


    /**
     * Sets the project name metadata information about the matrix. Puts changes on the stack, but does not set a checkpoint.
     *
     * @param projectName the new project name data for the matrix
     */
    public final void setProjectName(String projectName) {
        String currentName = this.projectName.getValue();
        addChangeToStack(new MatrixChange(
                () -> this.projectName.set(projectName),
                () -> this.projectName.set(currentName),
                false
        ));
    }


    /**
     * Returns the customer metadata information about the matrix
     *
     * @return customer data assigned to the matrix
     */
    public final String getCustomer() {
        return customer.getValue();
    }


    /**
     * Returns the customer metadata information about the matrix
     *
     * @return customer property assigned to the matrix
     */
    public final StringProperty getCustomerProperty() {
        return customer;
    }


    /**
     * Sets the customer metadata information about the matrix. Puts changes on the stack, but does not set a checkpoint.
     *
     * @param customer the new customer data for the matrix
     */
    public final void setCustomer(String customer) {
        String currentCustomer = this.customer.getValue();
        addChangeToStack(new MatrixChange(
                () -> this.customer.set(customer),
                () -> this.customer.set(currentCustomer),
                false
        ));
    }


    /**
     * Returns the version number metadata information about the matrix
     *
     * @return version number property assigned to the matrix
     */
    public final String getVersionNumber() {
        return versionNumber.getValue();
    }


    /**
     * Returns the version number metadata information about the matrix
     *
     * @return version number property assigned to the matrix
     */
    public final StringProperty getVersionNumberProperty() {
        return versionNumber;
    }



    /**
     * Sets the version number metadata information about the matrix. Puts changes on the stack, but does not set a checkpoint.
     *
     * @param versionNumber the new version number data for the matrix
     */
    public final void setVersionNumber(String versionNumber) {
        String currentVersionNumber = this.versionNumber.getValue();
        addChangeToStack(new MatrixChange(
                () -> this.versionNumber.set(versionNumber),
                () -> this.versionNumber.set(currentVersionNumber),
                false
        ));
    }
//endregion


    /**
     * Creates a 2d ArrayList of the matrix so that it can be displayed. Each cell in the grid is made
     * up of a RenderMode, which is the key, and an Object that is different based on the key.
     *
     * @return 2d ArrayList of matrix
     */
    public abstract ArrayList<ArrayList<Pair<RenderMode, Object>>> getGridArray();
}
