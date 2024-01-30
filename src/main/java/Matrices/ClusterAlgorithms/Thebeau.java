package Matrices.ClusterAlgorithms;


import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.SymmetricDSMData;
import Util.RandomColorGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Contains all methods relating to java implementations of Ronnie Thebeau's research in DSM clustering
 *
 * @author Aiden Carney
 */
public class Thebeau {

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
    public static HashMap<String, Object> getCoordinationScore(SymmetricDSMData matrix, Double optimalSizeCluster, Double powcc, Boolean calculateByWeight) {
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
    public static HashMap<Integer, Double> calculateClusterBids(SymmetricDSMData matrix, Grouping group, Double optimalSizeCluster, Double powdep, Double powbid, Boolean calculateByWeight) {
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
     * @return                   SymmetricDSMData object of the new clustered matrix
     */
    public static SymmetricDSMData thebeauAlgorithm(SymmetricDSMData inputMatrix, Double optimalSizeCluster, Double powdep, Double powbid, Double powcc, Double randBid, Double randAccept, Boolean calculateByWeight, int numLevels, long randSeed, boolean debug) {
        Random generator = new Random(randSeed);

        // place each element in the matrix in its own cluster
        SymmetricDSMData matrix = inputMatrix.createCopy();
        assert !matrix.equals(inputMatrix): "matrices are equal and they should not be";
        matrix.clearGroupings();  // groups will be re-distributed so remove the default as well

        RandomColorGenerator rgc = new RandomColorGenerator(0.2423353);  // use "random" start value for color generation
        for(int i = 0; i < matrix.getRows().size(); i++) {
            Grouping group = new Grouping("G" + i, null);
            matrix.setItemGroup(matrix.getRows().elementAt(i), group);

            matrix.updateGroupingColor(group, rgc.next());
        }

        // save the best solution
        SymmetricDSMData bestSolution = matrix.createCopy();

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
                double bid = calculateClusterBids(matrix, group, optimalSizeCluster, powdep, powbid, calculateByWeight).get(item.getUid());
                bids.put(group, bid);
            }

            // choose a number between 0 and randBid to determine if it should make a suboptimal change
            SymmetricDSMData tempMatrix = matrix.createCopy();
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
                matrix = tempMatrix.createCopy();

                if (coordinationCost < (Double) getCoordinationScore(bestSolution, optimalSizeCluster, powcc, calculateByWeight).get("TotalCost")) {  // save the new solution as the best one
                    bestSolution = matrix.createCopy();
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
