package Data;

import View.MatrixViews.RenderMode;
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
 * Note: items in a symmetric dsm will use the property Grouping.group1 to configure groups
 *
 * @author: Aiden Carney
 */
public class SymmetricDSM extends TemplateGroupedMatrix implements IPropagationAnalysis {
    private ObservableSet<Grouping> groupings;  // ObservableSet is used so that any gui threads reading it will see changes without needing a callback set up

//region Constructors
    /**
     * Creates a new SymmetricDSM object. Creates no row or column items and metadata are empty strings.
     * There is one grouping, which is the default: "(None)"
     */
    public SymmetricDSM() {
        super();

        connections = new Vector<>();
        groupings = FXCollections.observableSet();

        this.wasModified = true;

        clearStacks();
    }


    /**
     * Copy constructor for SymmetricDSM class. Performs a deep copy
     *
     * @param copy SymmetricDSM object to copy
     */
    public SymmetricDSM(SymmetricDSM copy) {
        super();

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


        groupings = FXCollections.observableSet();
        groupings.addAll(copy.groupings);

        title = copy.getTitle();
        projectName = copy.getProjectName();
        customer = copy.getCustomer();
        versionNumber = copy.getVersionNumber();

        this.wasModified = true;

        clearStacks();
    }
//endregion


//region Add and Delete Item Overrides
    /**
     * Creates a new item and adds it to the matrix and the stack
     *
     * @param name   the name of the item to create and add
     * @param isRow  is the item a row
     */
    @Override
    public void createItem(String name, boolean isRow) {
        double index = (int)getRowMaxSortIndex() + 1;  // cast to int to remove the decimal place so that the index will be a whole number

        DSMItem rowItem = new DSMItem(index, name);
        DSMItem colItem = new DSMItem(index, name);
        rowItem.setGroup1(defaultGroup);
        colItem.setGroup1(defaultGroup);
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
                addItem(item, isRow);
                addItem(aliasedItem, !isRow);
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


    /**
     * Sets the group of an item in the matrix symmetrically. This method should be called instead of directly modifying the item
     * because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param item     the item to change the name of
     * @param newGroup the new group for the item
     */
    @Override
    public void setItemGroup(DSMItem item, Grouping newGroup) {
        DSMItem aliasedItem = getItemByAlias(item.getUid());
        Grouping oldGroup = item.getGroup1();

        assert oldGroup.getUid().equals(aliasedItem.getGroup1().getUid()) : "Symmetric item groupings were not the same";

        boolean addedNewGroup = !groupings.contains(newGroup) && !newGroup.equals(defaultGroup);

        addChangeToStack(new MatrixChange(
            () -> {  // do function
                if(addedNewGroup) {
                    addGrouping(newGroup);
                }
                item.setGroup1(newGroup);
                aliasedItem.setGroup1(newGroup);
            },
            () -> {  // undo function
                if(addedNewGroup) {
                    removeGrouping(newGroup);
                }
                item.setGroup1(oldGroup);
                aliasedItem.setGroup1(oldGroup);
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
    protected void createConnection(int rowUid, int colUid, String connectionName, double weight) {
        // add assertion in this override
        assert getItem(rowUid).getUid() == getItem(colUid).getAliasUid();  // corresponds to where row and column are same and thus connection cannot be made

        DSMConnection connection = new DSMConnection(connectionName, weight, rowUid, colUid);
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
    public void modifyConnectionSymmetric(int rowUid, int colUid, String connectionName, double weight) {
        Pair<Integer, Integer> uids = getSymmetricConnectionUids(rowUid, colUid);
        modifyConnection(rowUid, colUid, connectionName, weight);
        modifyConnection(uids.getKey(), uids.getValue(), connectionName, weight);
    }
//endregion


    /**
     * Sorts the matrix rows and columns by their group and then their current sort index, then distributes new sort
     * Indices 1 to n. Used to make the sort Indices "clean" numbers and make the groups line up. Puts multiple changes on the
     * stack but does not set any checkpoint.
     */
    public void reDistributeSortIndicesByGroup() {
        rows.sort(Comparator.comparing((DSMItem item) -> item.getGroup1().getName()).thenComparing((DSMItem item) -> item.getName().getValue()));
        Vector<DSMItem> newCols = new Vector<>();

        for(DSMItem row : rows) {  // sort the new columns according to the rows (this does not need to be on the change stack because
                                   // only the index numbers are what matters to the change stack TODO: confirm this
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
            setItemSortIndex(rows.elementAt(i), i + 1);
        }
        for(int i=0; i<cols.size(); i++) {  // reset col sort Indices 1 -> n
            setItemSortIndex(cols.elementAt(i), i + 1);
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

                        Integer resultEntryUid = col.getAliasUid();
//                        if(isSymmetrical()) {
//                            resultEntryUid = col.getAliasUid();
//                        } else {
//                            resultEntryUid = col.getUid();
//                        }

                        results.get(currentLevel).putIfAbsent(resultEntryUid, 0.0);

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
    static public HashMap<String, Object> getCoordinationScore(SymmetricDSM matrix, Double optimalSizeCluster, Double powcc, Boolean calculateByWeight) {
        HashMap<String, Object> results = new HashMap<>();

        HashMap<Grouping, Double> intraCostBreakdown = new HashMap<>();
        double totalIntraCost = 0.0;
        double totalExtraCost = 0.0;
        for(DSMConnection conn : matrix.getConnections()) {
            if(matrix.getItem(conn.getRowUid()).getGroup1().equals(matrix.getItem(conn.getColUid()).getGroup1())) {  // row and col groups are the same so add to intra cluster
                Integer clusterSize = 0;  // calculate cluster size
                for(DSMItem row : matrix.getRows()) {
                    if(row.getGroup1().equals(matrix.getItem(conn.getRowUid()).getGroup1())) {
                        clusterSize += 1;
                    }
                }

                Double intraCost = Math.pow(Math.abs(optimalSizeCluster - clusterSize), powcc);
                if(calculateByWeight) {
                    intraCost = conn.getWeight() * intraCost;
                }

                if(intraCostBreakdown.get(matrix.getItem(conn.getRowUid()).getGroup1()) != null) {
                    intraCostBreakdown.put(matrix.getItem(conn.getRowUid()).getGroup1(), intraCostBreakdown.get(matrix.getItem(conn.getRowUid()).getGroup1()) + intraCost);
                } else {
                    intraCostBreakdown.put(matrix.getItem(conn.getRowUid()).getGroup1(), intraCost);
                }

                totalIntraCost += intraCost;
            } else {
                int dsmSize = matrix.getRows().size();
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
    static public HashMap<Integer, Double> calculateClusterBids(SymmetricDSM matrix, Grouping group, Double optimalSizeCluster, Double powdep, Double powbid, Boolean calculateByWeight) {
        HashMap<Integer, Double> bids = new HashMap<>();

        Integer clusterSize = 0;
        for(DSMItem row : matrix.getRows()) {
            if(row.getGroup1().equals(group)) {
                clusterSize += 1;
            }
        }

        for(DSMItem row : matrix.getRows()) {  // calculate bid of each item in the matrix for the given cluster
            double inout = 0.0;  // sum of DSM interactions of the item with each of the items in the cluster

            for(DSMItem col : matrix.getCols()) {
                if(col.getGroup1().equals(group) && col.getAliasUid() != row.getUid()) {  // make connection a part of inout score
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
     * @return                   SymmetricDSM object of the new clustered matrix
     */
    static public SymmetricDSM thebeauAlgorithm(SymmetricDSM inputMatrix, Double optimalSizeCluster, Double powdep, Double powbid, Double powcc, Double randBid, Double randAccept, Boolean calculateByWeight, int numLevels, long randSeed, boolean debug) {
        Random generator = new Random(randSeed);

        // place each element in the matrix in its own cluster
        SymmetricDSM matrix = new SymmetricDSM(inputMatrix);
        assert matrix != inputMatrix && !matrix.equals(inputMatrix): "matrices are equal and they should not be";
        matrix.clearGroupings();  // groups will be re-distributed

        // this method for generating random colors is from stack overflow, it generates colors based on a start value
        // and the golden ratio conjugate (golden ratio method)
        double h = 0.2423353;  // use random start value for color generation
        for(int i = 0; i < matrix.getRows().size(); i++) {
            Grouping group = new Grouping("G" + i, null);
            matrix.setItemGroup(matrix.getRows().elementAt(i), group);
            h += 0.618033988749895;  // golden_ratio_conjugate, this is a part of the golden ratio method for generating unique colors
            h %= 1;
            java.awt.Color hsvColor = java.awt.Color.getHSBColor((float)h, (float)0.5, (float)0.95);

            double r = hsvColor.getRed() / 255.0;
            double g = hsvColor.getGreen() / 255.0;
            double b = hsvColor.getBlue() / 255.0;
            matrix.updateGroupingColor(group, Color.color(r, g, b));
        }

        // save the best solution
        SymmetricDSM bestSolution = new SymmetricDSM(matrix);

        // calculate initial coordination cost
        double coordinationCost = (Double)getCoordinationScore(matrix, optimalSizeCluster, powcc, calculateByWeight).get("TotalCost");

        StringBuilder debugString = new StringBuilder("iteration,start time, elapsed time,coordination score\n");
        Instant absStart = Instant.now();

        for(int i=0; i < numLevels; i++) {  // iterate numLevels times
            Instant start = Instant.now();

            // choose an element from the matrix
            int n = (int)(generator.nextDouble() * (matrix.getRows().size() - 1));  // double from 0 to 1.0 multiplied by max index cast to integer
            DSMItem item = matrix.getRows().elementAt(n);
            // calculate bids
            HashMap<Grouping, Double> bids = new HashMap<>();
            for (Grouping group : matrix.getGroupings()) {
                double bid = matrix.calculateClusterBids(matrix, group, optimalSizeCluster, powdep, powbid, calculateByWeight).get(item.getUid());
                bids.put(group, bid);
            }

            // choose a number between 0 and randBid to determine if it should make a suboptimal change
            SymmetricDSM tempMatrix = new SymmetricDSM(matrix);
            item = tempMatrix.getRows().elementAt(n);  // update item to the item from the new matrix so that it is not modifying a copy
            int nBid = (int) (generator.nextDouble() * (randBid + 1));  // add one to randBid because with truncation nBid will never be equal to randBid

            // find if the change is optimal
            Grouping highestBidder = bids.entrySet().iterator().next().getKey();  // start with a default value so comparison doesn't throw NullPointerException
            if (nBid == randBid) {  // assign item group to second highest bidder
                Grouping secondHighestBidder = new Grouping("", null);
                for (Map.Entry<Grouping, Double> entry : bids.entrySet()) {
                    if (Double.compare(entry.getValue(), bids.get(highestBidder)) >= 0) {
                        highestBidder = entry.getKey();
                        secondHighestBidder = highestBidder;
                    } else if (Double.compare(entry.getValue(), bids.get(secondHighestBidder)) >= 0) {
                        secondHighestBidder = entry.getKey();
                    }
                }
                tempMatrix.setItemGroup(item, secondHighestBidder);

            } else {  // assign to highest bidder
                for (Map.Entry<Grouping, Double> entry : bids.entrySet()) {
                    if (Double.compare(entry.getValue(), bids.get(highestBidder)) >= 0) {
                        highestBidder = entry.getKey();
                    }
                }
                tempMatrix.setItemGroup(item, highestBidder);
            }

            // choose a number between 0 and randAccept to determine if change is permanent regardless of it being optimal
            int nAccept = (int) (generator.nextDouble() * (randAccept + 1));  // add one to randAccept because with truncation nAccept will never be equal to randAccept
            Double newCoordinationScore = (Double) getCoordinationScore(tempMatrix, optimalSizeCluster, powcc, calculateByWeight).get("TotalCost");

            if (nAccept == randAccept || newCoordinationScore < coordinationCost) {  // make the change permanent
                coordinationCost = newCoordinationScore;
                matrix = new SymmetricDSM(tempMatrix);

                if (coordinationCost < (Double) getCoordinationScore(bestSolution, optimalSizeCluster, powcc, calculateByWeight).get("TotalCost")) {  // save the new solution as the best one
                    bestSolution = new SymmetricDSM(matrix);
                }
            }


            String startTime = String.valueOf(Duration.between(absStart, start).toMillis());
            String elapsedTime = String.valueOf(Duration.between(start, Instant.now()).toMillis());
            debugString.append(i).append(",").append(startTime).append(",").append(elapsedTime).append(",").append(newCoordinationScore).append("\n");
        }

        if(debug) {
            System.out.println(debugString);
        }

        return bestSolution;
    }
//endregion
}
