package Matrices.Data;

import Matrices.Data.Entities.*;
import Matrices.Data.Flags.IPropagationAnalysis;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.*;


/**
 * A class that contains data about a matrix. All operations to a matrix come through
 * this class. Handles both symmetrical and non-symmetrical matrices.
 * Note: items in a symmetric dsm will use the property Grouping.group1 to configure groups
 *
 * @author: Aiden Carney
 */
public class SymmetricDSMData extends AbstractDSMData implements IPropagationAnalysis {

    private ObservableList<Grouping> groupings;

    public static final Integer DEFAULT_GROUP_UID = Integer.MAX_VALUE;


//region Constructors
    /**
     * Creates a new SymmetricDSMData object. Creates no row or column items and metadata are empty strings.
     * There is one grouping, which is the default: "(None)"
     */
    public SymmetricDSMData() {
        super();

        groupings = FXCollections.observableArrayList();
        addGrouping(new Grouping(DEFAULT_GROUP_UID, Grouping.DEFAULT_PRIORITY, "(none)", Color.WHITE, Grouping.DEFAULT_FONT_COLOR));

        setWasModified();

        clearStacks();
    }


    /**
     * Creates a new SymmetricDSMData object. Creates no row or column items and metadata are empty strings.
     * Adds all the groupings from the parameters
     *
     * @param groupings  a list of the groupings
     */
    public SymmetricDSMData(Collection<Grouping> groupings) {
        super();

        this.groupings = FXCollections.observableArrayList(groupings);

        setWasModified();

        clearStacks();
    }



    /**
     * Copy constructor for SymmetricDSMData class. Performs a deep copy
     *
     * @return  the copy of the current symmetric DSM
     */
    @Override
    public SymmetricDSMData createCopy() {
        SymmetricDSMData copy = new SymmetricDSMData();

        for(DSMItem row : getRows()) {
            copy.rows.add(new DSMItem(row));
        }

        for(DSMItem col : getCols()) {
            copy.cols.add(new DSMItem(col));
        }

        for(DSMConnection conn : getConnections()) {
            copy.connections.add(new DSMConnection(conn));
        }

        for(Grouping group : getGroupings()) {
            copy.groupings.add(new Grouping(group));
        }

        for(Map.Entry<String, List<DSMInterfaceType>> interfaceGroup : getInterfaceTypes().entrySet()) {
            List<DSMInterfaceType> interfaces = new ArrayList<>();
            for(DSMInterfaceType i : interfaceGroup.getValue()) {
                interfaces.add(new DSMInterfaceType(i));
            }
            copy.interfaceTypes.put(interfaceGroup.getKey(), interfaces);
        }

        copy.setTitle(getTitle());
        copy.setProjectName(getProjectName());
        copy.setCustomer(getCustomer());
        copy.setVersionNumber(getVersionNumber());

        copy.setWasModified();
        copy.clearStacks();

        return copy;
    }
//endregion


//region Grouping functions
    /**
     * @return  the default grouping object for the matrix
     */
    public Grouping getDefaultGroup() {
        for (Grouping grouping : groupings) {
            if (grouping.getUid().equals(DEFAULT_GROUP_UID)) {
                return grouping;
            }
        }
        return null;
    }


    /**
     * Adds a new grouping to the matrix. Puts the change on the stack but does not set
     * a checkpoint
     *
     * @param group  the object of type Grouping to add
     */
    public void addGrouping(Grouping group) {
        if(groupings.contains(group)) return;

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    groupings.add(group);
                },
                () -> {  // undo function
                    groupings.remove(group);
                },
                false
        ));
    }


    /**
     * Removes a grouping from the matrix from either the rows or the columns. Puts the change on the stack but does
     * not set a checkpoint
     *
     * @param group  the object of type Grouping to remove
     * @return       0 on success, -1 on error
     */
    public int removeGrouping(Grouping group) {
        if(group.getUid().equals(DEFAULT_GROUP_UID)) return -1;

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    groupings.remove(group);
                    for(DSMItem item : rows) {  // these changes already get put on the stack so no need to add them a second time
                        if(item.getGroup1().equals(group)) {
                            setItemGroup(item, getDefaultGroup());
                        }
                    }
                },
                () -> {  // undo function
                    groupings.add(group);
                },
                false
        ));

        return 0;
    }


    /**
     * Removes all groupings from the matrix. Puts the change on the stack but does not set a checkpoint.
     * The default grouping will remain
     */
    public void resetGroupings() {
        ObservableList<Grouping> oldGroupings = FXCollections.observableArrayList();
        oldGroupings.addAll(groupings);

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    groupings.clear();
                    groupings.add(new Grouping(DEFAULT_GROUP_UID, Grouping.DEFAULT_PRIORITY,  "(none)", Color.WHITE, Grouping.DEFAULT_FONT_COLOR));
                    for(DSMItem r : rows) {
                        setItemGroup(r, getDefaultGroup());  // only need to set the rows because the operation is symmetric
                    }
                },
                () -> {  // undo function
                    groupings = oldGroupings;
                },
                false
        ));
    }

    /**
     * Removes all groupings including the default. Does not set a checkpoint
     * Should be used with a clustering algorithm to grant it more control
     */
    public void clearGroupings() {
        groupings.clear();
    }


    /**
     * @return  ObservableList of the matrix groupings. Sorts the groupings by alphabetical order with default at the start
     */
    public ObservableList<Grouping> getGroupings() {
        Comparator<Grouping> groupingComparator = (o1, o2) -> {
            if(o1.getUid().equals(DEFAULT_GROUP_UID)) return -1;
            if(o2.getUid().equals(DEFAULT_GROUP_UID)) return 1;

            return o1.getName().compareTo(o2.getName());
        };

        FXCollections.sort(groupings, groupingComparator);
        return groupings;
    }


    /**
     * Renames a grouping. Puts the change on the stack but does not set a checkpoint.
     *
     * @param grouping  the group whose name should be changed
     * @param newName   the new name for the group
     */
    public void renameGrouping(Grouping grouping, String newName) {
        String oldName = grouping.getName();

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    grouping.setName(newName);
                },
                () -> {  // undo function
                    grouping.setName(oldName);
                },
                false
        ));
    }


    /**
     * Changes a color of a grouping. Puts the change on the stack but does not set a checkpoint.
     *
     * @param grouping  the group whose name should be changed
     * @param newColor  the new color of the grouping
     */
    public void updateGroupingColor(Grouping grouping, Color newColor) {
        Color oldColor = grouping.getColor();
        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    grouping.setColor(newColor);
                },
                () -> {  // undo function
                    grouping.setColor(oldColor);
                },
                false
        ));
    }


    /**
     * Changes a color of a grouping. Puts the change on the stack but does not set a checkpoint.
     *
     * @param grouping  the grouping whose font color should be changed
     * @param newColor  the new color of the grouping
     */
    public void updateGroupingFontColor(Grouping grouping, Color newColor) {
        Color oldColor = grouping.getFontColor();
        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    grouping.setFontColor(newColor);
                },
                () -> {  // undo function
                    grouping.setFontColor(oldColor);
                },
                false
        ));
    }


    /**
     * Sets the group of an item in the matrix symmetrically. This method should be called instead of directly modifying the item
     * because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param item     the item to change the name of
     * @param newGroup the new group for the item
     */
    public void setItemGroup(DSMItem item, Grouping newGroup) {
        DSMItem aliasedItem = getItemByAlias(item.getUid());
        Grouping oldGroup = item.getGroup1();
        assert oldGroup.getUid().equals(aliasedItem.getGroup1().getUid()) : "Symmetric item groupings were not the same";

        boolean addNewGroup = !groupings.contains(newGroup);

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    item.setGroup1(newGroup);
                    if (addNewGroup) {  // no need to undo because this puts another change on the stack
                        addGrouping(newGroup);
                    }
                    item.setGroup1(newGroup);
                    aliasedItem.setGroup1(newGroup);
                },
                () -> {  // undo function
                    item.setGroup1(oldGroup);
                    aliasedItem.setGroup1(oldGroup);
                },
                false
        ));
    }



//endregion


//region Add and Delete Item Overrides
    /**
     * Creates a new item and adds it to the matrix and the stack. Creates both the row and the column item
     *
     * @param name   the name of the item to create and add
     * @param isRow  is the item a row (ignored)
     */
    @Override
    public void createItem(String name, boolean isRow) {
        double index = (int)getRowMaxSortIndex() + 1;  // cast to int to remove the decimal place so that the index will be a whole number

        DSMItem rowItem = new DSMItem(index, name);
        DSMItem colItem = new DSMItem(index, name);
        rowItem.setGroup1(getDefaultGroup());
        colItem.setGroup1(getDefaultGroup());
        colItem.setAliasUid(rowItem.getUid());
        rowItem.setAliasUid(colItem.getUid());

        addItem(rowItem, true);
        addItem(colItem, false);
    }


    /**
     * Deletes an item from the matrix symmetrically. Puts the change on the stack but does not set a checkpoint
     *
     * @param item the item to delete
     */
    @Override
    public void deleteItem(DSMItem item) {
        boolean isRow = rows.contains(item);  // check if the item was a row in case it needs to be added again
        DSMItem aliasedItem = getItemByAlias(item.getUid());

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    removeItem(item);
                    removeItem(aliasedItem);
                },
                () -> {  // undo function
                    if (isRow) {
                        this.rows.add(item);
                        this.cols.add(aliasedItem);
                    } else {
                        this.rows.add(aliasedItem);
                        this.cols.add(item);
                    }
                },
                false
        ));
    }
//endregion


//region Setters for Items (name, index, group)
    /**
     * Sets the name of an item in the matrix symmetrically. This method should be called instead of directly modifying the
     * item name because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param item    the item to change the name of
     * @param newName the new name for the item
     */
    @Override
    public void setItemName(DSMItem item, String newName) {
        DSMItem aliasedItem = getItemByAlias(item.getUid());
        String oldName = item.getName().getValue();

        assert oldName.equals(aliasedItem.getName().getValue()) : "Symmetric item names were not the same";

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    item.setName(newName);
                    aliasedItem.setName(newName);
                },
                () -> {  // undo function
                    item.setName(oldName);
                    aliasedItem.setName(oldName);
                },
                false
        ));
    }


    /**
     * Sets the sort index of an item in the matrix symmetrically. This method should be called instead of directly
     * modifying the item because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param item     the item to change the name of
     * @param newIndex the new index for the item
     */
    @Override
    public void setItemSortIndex(DSMItem item, double newIndex) {
        DSMItem aliasedItem = getItemByAlias(item.getUid());
        double oldIndex = item.getSortIndex();

        assert oldIndex == aliasedItem.getSortIndex() : "Symmetric item sort indices were not the same";

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    item.setSortIndex(newIndex);
                    aliasedItem.setSortIndex(newIndex);
                },
                () -> {  // undo function
                    item.setSortIndex(oldIndex);
                    aliasedItem.setSortIndex(oldIndex);
                },
                false
        ));
    }
//endregion


//region Connection Modification Methods
    /**
     * Creates a connection and adds it to the matrix, but does not add the change to the stack. Overrides to add assertion
     * that row and column do not alias to each other because this should never happen with a symmetric matrix
     *
     * @param rowUid         the row item uid
     * @param colUid         the column item uid
     * @param connectionName the name of the connection
     * @param weight         the weight of the connection
     */
    @Override
    protected void createConnection(int rowUid, int colUid, String connectionName, double weight, ArrayList<DSMInterfaceType> interfaces) {
        // add assertion in this override
        assert getItem(rowUid).getUid() != getItem(colUid).getAliasUid();  // corresponds to where row and column are same and thus connection cannot be made

        DSMConnection connection = new DSMConnection(connectionName, weight, rowUid, colUid, interfaces);
        connections.add(connection);
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
    public void modifyConnectionSymmetric(int rowUid, int colUid, String connectionName, double weight, ArrayList<DSMInterfaceType> interfaces) {
        Pair<Integer, Integer> uids = getSymmetricConnectionUids(rowUid, colUid);
        modifyConnection(rowUid, colUid, connectionName, weight, interfaces);
        modifyConnection(uids.getKey(), uids.getValue(), connectionName, weight, interfaces);
    }
//endregion


    /**
     * Sorts the matrix rows and columns by their group and then their current sort index, then distributes new sort
     * Indices 1 to n. Used to make the sort Indices "clean" numbers and make the groups line up. Puts multiple changes on the
     * stack but does not set any checkpoint.
     */
    public void reDistributeSortIndicesByGroup() {
        rows.sort(Comparator.comparing((DSMItem item) -> item.getGroup1().getName()).thenComparing((DSMItem item) -> item.getName().getValue()));
        List<DSMItem> newCols = new ArrayList<>();

        for(DSMItem row : rows) {  // sort the new columns according to the rows (this does not need to be on the change stack because
                                   // only the index numbers are what matters to the change stack
            for(DSMItem col : cols) {
                if(col.getAliasUid() == row.getUid()) {
                    assert col.getGroup1().getUid().equals(row.getGroup1().getUid()) : "Groups were not the same when redistributing sort indices";

                    newCols.add(col);
                    break;
                }
            }
        }
        cols = newCols;

        for(int i=0; i<rows.size(); i++) {  // reset row sort Indices 1 -> n
            setItemSortIndex(rows.get(i), i + 1);
        }
        for(int i=0; i<cols.size(); i++) {  // reset col sort Indices 1 -> n
            setItemSortIndex(cols.get(i), i + 1);
        }
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
        Integer newRowUid = getItem(colUid).getAliasUid();
        Integer newColUid = null;
        for(DSMItem item : getCols()) {
            if(item.getAliasUid() != null && item.getAliasUid() == rowUid) {
                newColUid = item.getUid();
                break;
            }
        }

        if(newColUid != null && newRowUid != null) {
            return new Pair<>(newRowUid, newColUid);
        }
        return null;
    }


    /**
     * Returns the symmetric connection of a given pair of uids
     *
     * @param   rowUid the uid of the row item of the connection
     * @param   colUid the uid of the column item of the connection
     * @return  the DSMConnection object of the symmetric connection
     */
    public DSMConnection getSymmetricConnection(int rowUid, int colUid) {
        Pair<Integer, Integer> symmetricUids = getSymmetricConnectionUids(rowUid, colUid);
        return getConnection(symmetricUids.getKey(), symmetricUids.getValue());
    }


    /**
     * Creates a 2d ArrayList of the matrix so that it can be displayed. Each cell in the grid is made
     * up of a RenderMode, which is the key, and an Object that is different based on the key.
     *
     * @return 2d ArrayList of matrix
     */
    public ArrayList<ArrayList<Pair<RenderMode, Object>>> getGridArray() {
        ArrayList<ArrayList<Pair<RenderMode, Object>>> grid = new ArrayList<>();

        // sort row and columns by sortIndex
        rows.sort(Comparator.comparing(DSMItem::getSortIndex));
        cols.sort(Comparator.comparing(DSMItem::getSortIndex));

        // create header row
        ArrayList<Pair<RenderMode, Object>> row0 = new ArrayList<>();
        row0.add(new Pair<>(RenderMode.PLAIN_TEXT_V, ""));
        row0.add(new Pair<>(RenderMode.PLAIN_TEXT_V, ""));
        row0.add(new Pair<>(RenderMode.PLAIN_TEXT_V, "Column Items"));
        for(DSMItem c : cols) {
            row0.add(new Pair<>(RenderMode.ITEM_NAME_V, c));
        }
        grid.add(row0);

        // create third header row
        ArrayList<Pair<RenderMode, Object>> row1 = new ArrayList<>();
        row1.add(new Pair<>(RenderMode.PLAIN_TEXT, "Grouping"));
        row1.add(new Pair<>(RenderMode.PLAIN_TEXT, "Row Items"));
        row1.add(new Pair<>(RenderMode.PLAIN_TEXT, "Re-Sort Index"));
        for(DSMItem c : cols) {
            row1.add(new Pair<>(RenderMode.PLAIN_TEXT, ""));
        }
        grid.add(row1);

        // create rows
        for(DSMItem r : rows) {
            ArrayList<Pair<RenderMode, Object>> row = new ArrayList<>();
            row.add(new Pair<>(RenderMode.GROUPING_ITEM, r));
            row.add(new Pair<>(RenderMode.ITEM_NAME, r));
            row.add(new Pair<>(RenderMode.INDEX_ITEM, r));
            for(DSMItem c : cols) {  // create connection items for all columns
                if(c.getAliasUid() == r.getUid()) {  // can't have connection to itself in a symmetrical matrix
                    row.add(new Pair<>(RenderMode.UNEDITABLE_CONNECTION, null));
                } else {
                    row.add(new Pair<>(RenderMode.EDITABLE_CONNECTION, new Pair<>(r, c)));
                }
            }
            grid.add(row);
        }

        return grid;
    }


//region Analysis Functions
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
    @Override
    public HashMap<Integer, HashMap<Integer, Double>> propagationAnalysis(Integer startItem, int numLevels, ArrayList<Integer> exclusions, double minWeight, boolean countByWeight) {
        int currentLevel = 1;
        HashMap<Integer, HashMap<Integer, Double>> results = new HashMap<>();
        ArrayList<Integer> dependentConnections = new ArrayList<>();
        dependentConnections.add(startItem);
        exclusions.add(startItem);

        while(currentLevel <= numLevels) {
            ArrayList<Integer> newDependentConnections = new ArrayList<>();
            results.put(currentLevel, new HashMap<>());  // add default item

            for(Integer uid : dependentConnections) {  // find dependent connections of each item from the previous level

                // find connections with uid as the row item
                for(DSMItem col : cols) {  // iterate over column items finding the ones that match the row
                    DSMConnection conn = getConnection(uid, col.getUid());

                    // define exit conditions
                    if(conn == null) continue;
                    if(conn.getWeight() < minWeight) continue;

                    Integer resultEntryUid = col.getAliasUid();

                    results.get(currentLevel).putIfAbsent(resultEntryUid, 0.0);

                    if(countByWeight) {
                        results.get(currentLevel).put(resultEntryUid, results.get(currentLevel).get(resultEntryUid) + conn.getWeight());
                    } else {
                        results.get(currentLevel).put(resultEntryUid, results.get(currentLevel).get(resultEntryUid) + 1.0);
                    }

                    if(!exclusions.contains(resultEntryUid) && !newDependentConnections.contains(resultEntryUid)) {  // add to next level if not present and not excluded
                        newDependentConnections.add(resultEntryUid);  // add the actual item uid
                    }
                }
            }


            dependentConnections.clear();
            dependentConnections = newDependentConnections;
            currentLevel += 1;
        }

        return results;
    }


    //endregion
}
