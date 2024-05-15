package Matrices.ClusterAlgorithms;


import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.SymmetricDSMData;
import Util.RandomColorGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Contains all methods relating to java implementations of Ronnie Thebeau's research in DSM clustering
 *
 * @author Aiden Carney
 */
public class Thebeau {

    public static class CoordinationScore {
        public HashMap<Grouping, Double> intraBreakdown;
        public double totalIntraCost;
        public double totalExtraCost;
        public double totalCost;

        public CoordinationScore(HashMap<Grouping, Double> intraBreakdown, double totalIntraCost, double totalExtraCost, double totalCost) {
            this.intraBreakdown = intraBreakdown;
            this.totalIntraCost = totalIntraCost;
            this.totalExtraCost = totalExtraCost;
            this.totalCost = totalCost;
        }
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
    public static CoordinationScore getCoordinationScore(SymmetricDSMData matrix, Double optimalSizeCluster, Double powcc, Boolean calculateByWeight) {
        HashMap<Grouping, Double> intraCostBreakdown = new HashMap<>();
        double totalIntraCost = 0.0;
        double totalExtraCost = 0.0;

        int dsmSize = matrix.getRows().size();

        // determine cluster sizes
        HashMap<Grouping, Integer> clusterSizes = new HashMap<>();
        for (DSMItem row : matrix.getRows()) {
            clusterSizes.merge(row.getGroup1(), 1, Integer::sum);  // increment or add new key to cluster size
        }

        for(DSMConnection conn : matrix.getConnections()) {
            DSMItem rowItem = matrix.getRowItem(conn.getRowUid());
            DSMItem colItem = matrix.getColItem(conn.getColUid());

            if(!SymmetricDSMData.DEFAULT_GROUP_UID.equals(rowItem.getGroup1().getUid()) && rowItem.getGroup1().equals(colItem.getGroup1())) {
                // row and col groups are the same so add to intra cluster
                double intraCost = Math.pow(Math.abs(optimalSizeCluster - clusterSizes.get(rowItem.getGroup1())), powcc);
                if(calculateByWeight) {
                    intraCost = conn.getWeight() * intraCost;
                }

                intraCostBreakdown.merge(rowItem.getGroup1(), intraCost, Double::sum);  // increment or add new key to breakdown
                totalIntraCost += intraCost;
            } else {
                if(calculateByWeight) {
                    totalExtraCost += conn.getWeight() * Math.pow(dsmSize, powcc);
                } else {
                    totalExtraCost += Math.pow(dsmSize, powcc);
                }
            }
        }

        return new CoordinationScore(intraCostBreakdown, totalIntraCost, totalExtraCost, totalIntraCost + totalExtraCost);
    }


    /**
     * Calculates the bids of each item in a given group based on the Thebeau algorithm
     *
     * @param matrix             the matrix to use
     * @param group              the group in the matrix to use
     * @param rowItem            the row item to calculate bid for
     * @param optimalSizeCluster optimal cluster size that will receive no penalty
     * @param powdep             exponential to emphasize connections
     * @param powbid             exponential to penalize non-optimal cluster size
     * @param calculateByWeight  calculate bid by weight or occurrence
     * @return the cluster bid for a given row item
     */
    public static double calculateClusterBid(SymmetricDSMData matrix, Grouping group, DSMItem rowItem, Double optimalSizeCluster, Double powdep, Double powbid, Boolean calculateByWeight) {
        Integer clusterSize = 0;
        for(DSMItem row : matrix.getRows()) {
            if(row.getGroup1().equals(group)) {
                clusterSize += 1;
            }
        }

        double inout = 0.0;  // sum of DSM interactions of the item with each of the items in the cluster

        for(DSMItem col : matrix.getCols()) {
            if(col.getGroup1().equals(group) && col.getAliasUid() != rowItem.getUid()) {  // make connection a part of inout score
                DSMConnection conn = matrix.getConnection(rowItem.getUid(), col.getUid());
                if(calculateByWeight && conn != null) {
                    inout += conn.getWeight();
                } else if(conn != null) {
                    inout += 1;
                }
            }
        }

        return Math.pow(inout, powdep) / Math.pow(Math.abs(optimalSizeCluster - clusterSize), powbid);
    }


    /**
     * Deletes a grouping from a symmetric dsm if no items are contained in it
     * @param matrix  the matrix the group is from
     * @param group   the group to check if empty
     */
    private static void deleteClusterIfEmpty(SymmetricDSMData matrix, Grouping group) {
        int clusterSize = 0;
        for (DSMItem row : matrix.getRows()) {
            if (row.getGroup1().equals(group)) clusterSize++;
        }

        if (clusterSize == 0) {
            matrix.removeGrouping(group);
        }
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
     * 6. Calculate the total Coordination Cost if the selected element becomes a member of the cluster with the highest bid (use second-highest bid if step 5 is equal to rand_bid)
     * 7. Randomly choose a number between 1 and rand_accept (algorithm parameter)
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
     * @param exclusions         a list of UIDs to exclude from clustering
     * @param calculateByWeight  calculate scores and bidding by weight or by number of occurrences
     * @param numLevels          number of iterations
     * @param randSeed           seed for random number generator
     * @param debug              debug to stdout
     * @return                   SymmetricDSMData object of the new clustered matrix
     */
    public static SymmetricDSMData thebeauAlgorithm(SymmetricDSMData inputMatrix, Double optimalSizeCluster, Double powdep,
                Double powbid, Double powcc, Integer randBid, Integer randAccept, ArrayList<Integer> exclusions,
                Boolean calculateByWeight, int numLevels,long randSeed, boolean debug) {
        Random generator = new Random(randSeed);

        // place each element in the matrix in its own cluster
        SymmetricDSMData matrix = inputMatrix.createCopy();
        assert !matrix.equals(inputMatrix): "matrices are equal and they should not be";
        matrix.clearGroupings();  // groups will be re-distributed so remove the default as well

        RandomColorGenerator rgc = new RandomColorGenerator(0.2423353);  // use "random" start value for color generation
        for(int i = 0; i < matrix.getRows().size(); i++) {
            Grouping group = new Grouping("G" + i, null);
            matrix.setItemGroup(matrix.getRows().get(i), group);

            matrix.updateGroupingColor(group, rgc.next());
        }

        // calculate initial coordination cost
        double coordinationCost = getCoordinationScore(matrix, optimalSizeCluster, powcc, calculateByWeight).totalCost;

        // save the best solution
        SymmetricDSMData bestSolution = matrix.createCopy();
        double bestSolutionCost = coordinationCost;  // best solution is just a copy of matrix so this number is the same and
                                                     // does not need to be calculated twice

        StringBuilder debugString = new StringBuilder("iteration,start time, elapsed time,coordination score\n");
        Instant absStart = Instant.now();

        for(int i=0; i < numLevels; i++) {  // iterate numLevels times
            Instant start = Instant.now();

            // Choose an element from the matrix. Keep choosing randomly until chosen item is not excluded
            int n = generator.nextInt(matrix.getRows().size());
            DSMItem item = matrix.getRows().get(n);
            while (exclusions.contains(item.getUid())) {
                n = generator.nextInt(matrix.getRows().size());
                item = matrix.getRows().get(n);
            }

            // calculate bids
            HashMap<Grouping, Double> bids = new HashMap<>();
            for (Grouping group : matrix.getGroupings()) {
                double bid = calculateClusterBid(matrix, group, item, optimalSizeCluster, powdep, powbid, calculateByWeight);
                bids.put(group, bid);
            }


            // find first and second-highest bidders
            Grouping highestBidder = null;
            Grouping secondHighestBidder = null;
            double highestBid = -1;
            double secondHighestBid = -1;
            for (Map.Entry<Grouping, Double> entry : bids.entrySet()) {
                if (Double.compare(entry.getValue(), highestBid) > 0) {
                    secondHighestBidder = highestBidder;
                    secondHighestBid = highestBid;

                    highestBidder = entry.getKey();
                    highestBid = entry.getValue();
                } else if (Double.compare(entry.getValue(), secondHighestBid) > 0) {
                    secondHighestBidder = entry.getKey();
                    secondHighestBid = entry.getValue();
                }
            }

            // choose a number between 0 and randBid to determine if it should make a suboptimal change
            SymmetricDSMData tempMatrix = matrix.createCopy();
            item = tempMatrix.getRows().get(n);  // update item to the item from the new matrix so that it is not modifying a copy
            int nBid = generator.nextInt(randBid) + 1;  // add one to randBid because with truncation nBid will never be equal to randBid

            Grouping oldGroup = item.getGroup1();
            if (nBid == randBid) {  // assign item group to second-highest bidder
                tempMatrix.setItemGroup(item, secondHighestBidder);

            } else {  // assign to highest bidder
                tempMatrix.setItemGroup(item, highestBidder);
            }
            deleteClusterIfEmpty(tempMatrix, oldGroup);

            // choose a number between 0 and randAccept to determine if change is permanent regardless of it being optimal
            int nAccept = generator.nextInt(randAccept) + 1;  // add one to randAccept because with truncation nAccept will never be equal to randAccept
            Double newCoordinationScore = getCoordinationScore(tempMatrix, optimalSizeCluster, powcc, calculateByWeight).totalCost;

            if (nAccept == randAccept || newCoordinationScore < coordinationCost) {  // make the change permanent
                coordinationCost = newCoordinationScore;
                matrix = tempMatrix;

                if (coordinationCost < bestSolutionCost) {  // save the new solution as the best one
                    bestSolution = matrix.createCopy();  // use copy so this is permanent
                    bestSolutionCost = coordinationCost;
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


}
