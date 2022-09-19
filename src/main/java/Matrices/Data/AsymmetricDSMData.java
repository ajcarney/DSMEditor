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
public class AsymmetricDSMData extends AbstractDSMData implements IPropagationAnalysis {

    public static final Integer DEFAULT_GROUP_UID = Integer.MAX_VALUE;

    private ObservableList<Grouping> rowGroupings;
    private ObservableList<Grouping> colGroupings;


//region Constructors
    /**
     * Creates a new AsymmetricDSMData object. Creates no row or column items and metadata are empty strings.
     * There is one grouping, which is the default: "(none)" for both the rows and columns
     */
    public AsymmetricDSMData() {
        super();

        connections = new Vector<>();
        rowGroupings = FXCollections.observableArrayList();
        colGroupings = FXCollections.observableArrayList();
        addGrouping(true, new Grouping(DEFAULT_GROUP_UID, Grouping.DEFAULT_PRIORITY,  "(none)", Color.WHITE, Grouping.DEFAULT_FONT_COLOR));
        addGrouping(false, new Grouping(DEFAULT_GROUP_UID, Grouping.DEFAULT_PRIORITY,  "(none)", Color.WHITE, Grouping.DEFAULT_FONT_COLOR));

        setWasModified();

        clearStacks();
    }


    /**
     * Creates a new AsymmetricDSMData object. Creates no row or column items and metadata are empty strings.
     * Adds all the groupings from the parameters
     *
     * @param rowGroupings  a list of the row groupings
     * @param colGroupings  a list of the column groupings
     */
    public AsymmetricDSMData(Collection<Grouping> rowGroupings, Collection<Grouping> colGroupings) {
        super();

        connections = new Vector<>();
        this.rowGroupings = FXCollections.observableArrayList(rowGroupings);
        this.colGroupings = FXCollections.observableArrayList(colGroupings);

        setWasModified();

        clearStacks();
    }


    /**
     * Copy constructor for AsymmetricDSMData class. Performs a deep copy
     *
     * @return  the copy of the current Asymmetric DSM
     */
    @Override
    public AsymmetricDSMData createCopy() {
        AsymmetricDSMData copy = new AsymmetricDSMData();

        for(DSMItem row : getRows()) {
            copy.rows.add(new DSMItem(row));
        }

        for(DSMItem col : getCols()) {
            copy.cols.add(new DSMItem(col));
        }

        for(DSMConnection conn : getConnections()) {
            copy.connections.add(new DSMConnection(conn));
        }

        for(Grouping group : getGroupings(true)) {
            copy.rowGroupings.add(new Grouping(group));
        }
        for(Grouping group : getGroupings(false)) {
            copy.colGroupings.add(new Grouping(group));
        }

        for(Map.Entry<String, Vector<DSMInterfaceType>> interfaceGroup : getInterfaceTypes().entrySet()) {
            Vector<DSMInterfaceType> interfaces = new Vector<>();
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
     * @param isRow  if getting the default group for the rows or columns
     * @return       the default grouping object for either a row or a column
     */
    private Grouping getDefaultGroup(boolean isRow) {
        if(isRow) {
            for (Grouping grouping : rowGroupings) {
                if (grouping.getUid().equals(DEFAULT_GROUP_UID)) {
                    return grouping;
                }
            }
        } else {
            for (Grouping grouping : colGroupings) {
                if (grouping.getUid().equals(DEFAULT_GROUP_UID)) {
                    return grouping;
                }
            }
        }
        return null;
    }


    /**
     * Adds a new grouping to the matrix for either the rows or columns. Puts the change on the stack but does not set
     * a checkpoint
     *
     * @param isRow  if group should be added to the row or column groups
     * @param group  the object of type Grouping to add
     */
    public void addGrouping(Boolean isRow, Grouping group) {
        if(isRow && rowGroupings.contains(group)) return;
        if(!isRow && colGroupings.contains(group)) return;

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    if(isRow) {
                        rowGroupings.add(group);
                    } else {
                        colGroupings.add(group);
                    }
                },
                () -> {  // undo function
                    if(isRow) {
                        rowGroupings.remove(group);
                    } {
                        colGroupings.remove(group);
                    }
                },
                false
        ));
    }


    /**
     * Removes a grouping from the matrix from either the rows or the columns. Puts the change on the stack but does
     * not set a checkpoint
     *
     * @param isRow  if the grouping is a part of the rows or columns
     * @param group  the object of type Grouping to remove
     * @return       0 on success, -1 on error
     */
    public int removeGrouping(Boolean isRow, Grouping group) {
        if(group.getUid().equals(DEFAULT_GROUP_UID)) return -1;

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    if(isRow) {
                        rowGroupings.remove(group);
                        for(DSMItem item : rows) {  // these changes already get put on the stack so no need to add them a second time
                            if(item.getGroup1().equals(group)) {
                                setItemGroup(item, getDefaultGroup(true));
                            }
                        }
                    } else {
                        colGroupings.remove(group);
                        for(DSMItem item : cols) {  // these changes already get put on the stack so no need to add them a second time
                            if(item.getGroup1().equals(group)) {
                                setItemGroup(item, getDefaultGroup(false));
                            }
                        }
                    }
                },
                () -> {  // undo function
                    if(isRow) {
                        rowGroupings.add(group);
                    } else {
                        colGroupings.add(group);
                    }
                },
                false
        ));

        return 0;
    }


    /**
     * Removes all groupings from the matrix. Puts the change on the stack but does not set a checkpoint
     */
    public void clearGroupings(Boolean isRow) {
        ObservableList<Grouping> oldGroupings = FXCollections.observableArrayList();
        if(isRow) {
            oldGroupings.addAll(rowGroupings);
        } else {
            oldGroupings.addAll(colGroupings);
        }


        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    if(isRow) {
                        rowGroupings.clear();
                        rowGroupings.add(new Grouping(DEFAULT_GROUP_UID, Grouping.DEFAULT_PRIORITY,  "(none)", Color.WHITE, Grouping.DEFAULT_FONT_COLOR));
                        for(DSMItem r : rows) {
                            setItemGroup(r, getDefaultGroup(true));
                        }
                    } else {
                        colGroupings.clear();
                        colGroupings.add(new Grouping(DEFAULT_GROUP_UID, Grouping.DEFAULT_PRIORITY,  "(none)", Color.WHITE, Grouping.DEFAULT_FONT_COLOR));
                        for(DSMItem c : cols) {
                            setItemGroup(c, getDefaultGroup(false));
                        }
                    }
                },
                () -> {  // undo function
                    if(isRow) {
                        rowGroupings = oldGroupings;
                    } else {
                        colGroupings = oldGroupings;
                    }
                },
                false
        ));
    }


    /**
     * @return  ObservableList of the matrix groupings. Sorts the groupings by alphabetical order with default at the start
     */
    public ObservableList<Grouping> getGroupings(boolean isRow) {
        Comparator<Grouping> groupingComparator = (o1, o2) -> {
            if(o1.getUid().equals(DEFAULT_GROUP_UID)) return -1;
            if(o2.getUid().equals(DEFAULT_GROUP_UID)) return 1;

            return o1.getName().compareTo(o2.getName());
        };

        if(isRow) {
            FXCollections.sort(rowGroupings, groupingComparator);
            return rowGroupings;
        } else {
            FXCollections.sort(colGroupings, groupingComparator);
            return colGroupings;
        }
    }


    /**
     * Renames a grouping. Puts the change on the stack but does not set a checkpoint.
     *
     * @param grouping  the group who's name should be changed
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
     * @param grouping  the group who's name should be changed
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
     * @param grouping  the grouping who's font color should be changed
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
        Grouping oldGroup = item.getGroup1();

        boolean addNewGroup = (isRow(item.getUid()) && !rowGroupings.contains(newGroup)) || (!isRow(item.getUid()) && !colGroupings.contains(newGroup));

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    if (addNewGroup) {
                        addGrouping(isRow(item.getUid()), newGroup);
                    }
                    item.setGroup1(newGroup);
                },
                () -> {  // undo function
                    item.setGroup1(oldGroup);
                },
                false
        ));
    }



//endregion

    /**
     * Creates a new item and adds it to the matrix and the stack. Overrides to set a default group
     *
     * @param name   the name of the item to create and add
     * @param isRow  is the item a row
     */
    @Override
    public void createItem(String name, boolean isRow) {
        double index;
        if(isRow) {
            index = (int) getRowMaxSortIndex() + 1;  // cast to int to remove the decimal place so that the index will be a whole number
        } else {
            index = (int) getColMaxSortIndex() + 1;  // cast to int to remove the decimal place so that the index will be a whole number
        }
        DSMItem item = new DSMItem(index, name);
        item.setGroup1(getDefaultGroup(isRow));
        item.setAliasUid(null);

        addItem(item, isRow);
    }


    /**
     * Inverts a matrix by flipping its rows and columns and switching the connection rows and columns. Adds changes to
     * the stack to be handled, but does not set a checkpoint
     */
    public final void transposeMatrix() {
        Vector<DSMItem> oldRows = new Vector<>(rows);
        Vector<DSMItem> oldCols = new Vector<>(cols);
        Vector<DSMConnection> oldConnections = new Vector<>(connections);
        ObservableList<Grouping> oldRowGroupings = FXCollections.observableArrayList(rowGroupings);
        ObservableList<Grouping> oldColGroupings = FXCollections.observableArrayList(colGroupings);

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    cols = oldRows;
                    rows = oldCols;
                    colGroupings = oldRowGroupings;
                    rowGroupings = oldColGroupings;

                    connections.clear();
                    for(DSMConnection conn : oldConnections) {
                        createConnection(conn.getColUid(), conn.getRowUid(), conn.getConnectionName(), conn.getWeight(), conn.getInterfaces());
                    }
                },
                () -> {  // undo function
                    cols = new Vector<>(oldCols);
                    rows = new Vector<>(oldRows);
                    colGroupings = FXCollections.observableArrayList(oldColGroupings);
                    rowGroupings = FXCollections.observableArrayList(oldRowGroupings);
                    connections = new Vector<>(oldConnections);
                },
                false
        ));

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
        row0.add(new Pair<>(RenderMode.PLAIN_TEXT_V, "Grouping"));
        for (DSMItem c : cols) {
            row0.add(new Pair<>(RenderMode.GROUPING_ITEM_V, c));
        }
        grid.add(row0);


        ArrayList<Pair<RenderMode, Object>> row1 = new ArrayList<>();
        row1.add(new Pair<>(RenderMode.PLAIN_TEXT_V, ""));
        row1.add(new Pair<>(RenderMode.PLAIN_TEXT_V, ""));
        row1.add(new Pair<>(RenderMode.PLAIN_TEXT_V, "Column Items"));
        for(DSMItem c : cols) {
            row1.add(new Pair<>(RenderMode.ITEM_NAME_V, c));
        }
        grid.add(row1);

        // create third header row
        ArrayList<Pair<RenderMode, Object>> row2 = new ArrayList<>();
        row2.add(new Pair<>(RenderMode.PLAIN_TEXT, "Grouping"));
        row2.add(new Pair<>(RenderMode.PLAIN_TEXT, "Row Items"));
        row2.add(new Pair<>(RenderMode.PLAIN_TEXT, "Re-Sort Index"));
        for(DSMItem c : cols) {
            row2.add(new Pair<>(RenderMode.INDEX_ITEM, c));
        }

        grid.add(row2);

        // create rows
        for(DSMItem r : rows) {
            ArrayList<Pair<RenderMode, Object>> row = new ArrayList<>();
            row.add(new Pair<>(RenderMode.GROUPING_ITEM, r));
            row.add(new Pair<>(RenderMode.ITEM_NAME, r));
            row.add(new Pair<>(RenderMode.INDEX_ITEM, r));
            for(DSMItem c : cols) {  // create connection items for all columns
                row.add(new Pair<>(RenderMode.EDITABLE_CONNECTION, new Pair<>(r, c)));
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
    @Override
    public HashMap<Integer, HashMap<Integer, Double>> propagationAnalysis(Integer startItem, int numLevels, ArrayList<Integer> exclusions, double minWeight, boolean countByWeight) {
        int currentLevel = 1;
        HashMap<Integer, HashMap<Integer, Double>> results = new HashMap<>();
        ArrayList<Integer> dependentConnections = new ArrayList<>();
        dependentConnections.add(startItem);
        exclusions.add(startItem);

        // check if start item is a row or column item
        boolean startIsRow = rows.contains(getItem(startItem));

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

                        Integer resultEntryUid = col.getUid();
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
            } else {  // currentLevel is even so choose column
                for(Integer uid : dependentConnections) {  // find dependent connections of each item from the previous level

                    // find connections with uid as the row item
                    for(DSMItem row : rows) {  // iterate over row items finding the ones that match the column
                        DSMConnection conn = getConnection(row.getUid(), uid);

                        // define exit conditions
                        if(conn == null) continue;
                        if(conn.getWeight() < minWeight) continue;

                        Integer itemUid = row.getUid();
                        results.get(currentLevel).putIfAbsent(itemUid, 0.0);

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
}
