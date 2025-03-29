package Matrices.ClusterAlgorithms;

import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.SymmetricDSMData;
import Util.RandomColorGenerator;

import java.util.ArrayList;

/**
 * Contains java implementation of the ART1 algorithm.
 *
 * @author: Aiden Carney
 */
public class ART1 {

    ArrayList<DSMItem> sortedItems;

    SymmetricDSMData matrix;
    int numItems;
    ArrayList<ArrayList<Double>> adjacencyMatrix;

    ArrayList<Integer> itemMemberships;
    ArrayList<ArrayList<Double>> prototypes;
    ArrayList<Grouping> prototypeGroups;


    public ART1(SymmetricDSMData inputMatrix) {
        matrix = inputMatrix.createCopy();
        sortedItems = new ArrayList<>(matrix.getRows());

        numItems = matrix.getRows().size();
        adjacencyMatrix = new ArrayList<>();
        for(int row = 0; row < numItems; row++) {
            ArrayList<Double> matrixRow = new ArrayList<>();
            for(int col = 0; col < numItems; col++) {
                // if connection exists between this row and this column add a 1 to the matrix, otherwise, add a 0
                if (matrix.getConnection(sortedItems.get(row).getUid(), sortedItems.get(col).getAliasUid()) != null) {
                    matrixRow.add(1.0);
                } else {
                    matrixRow.add(0.0);
                }
            }
            adjacencyMatrix.add(matrixRow);
        }

        // set all items to not having a group
        matrix.clearGroupings();
        itemMemberships = new ArrayList<>();
        prototypes = new ArrayList<>();  // list of column UIDs where the prototype has a value
        prototypeGroups = new ArrayList<>();
    }


    private void initPrototypes(int maxGroups) {
        // create groupings with distinct colors
        RandomColorGenerator rgc = new RandomColorGenerator(0.2423353);
        for(int i = 0; i < maxGroups; i++) {
            Grouping group = new Grouping("G" + i, null);

            group.setColor(rgc.next());
            prototypeGroups.add(group);  // add the group

            prototypes.add(new ArrayList<>());  // add an empty list for the prototypes
        }

        for(int i = 0; i < numItems; i++) {
            itemMemberships.add(-1);
        }
    }


    /**
     * performs an index-by-index fuzzy and of two vectors. ex <1, 0, 1> and <1, 0, 0> = <1, 0, 0>
     * @param v1 the first vector
     * @param v2 the second vector
     * @return the resulting vector
     */
    private ArrayList<Double> andVectors(ArrayList<Double> v1, ArrayList<Double> v2) {
        ArrayList<Double> v = new ArrayList<>();
        for(int i = 0; i < v1.size(); i++) {
            if(v1.get(i) < v2.get(i)) {  // fuzzy and operator -- choose minimum
                v.add(v1.get(i));
            } else {
                v.add(v2.get(i));
            }
        }

        return v;
    }


    private ArrayList<Double> sumVectors(ArrayList<Double> v1, ArrayList<Double> v2) {
        ArrayList<Double> v = new ArrayList<>();
        for(int i = 0; i < v1.size(); i++) {
            v.add(v1.get(i) + v2.get(i));
        }

        return v;
    }


    /**
     * returns magnitude of vector defined as sum of all elements
     * @param v vector
     * @return magnitude
     */
    private double vectorMagnitude(ArrayList<Double> v) {
        int sum = 0;
        for (Double i : v) {
            sum += i * i;
        }
        return Math.sqrt(sum);
    }


    private void createPrototype(int prototypeIndex, int itemIndex) {
        ArrayList<Double> newPrototype = prototypes.get(prototypeIndex);
        for(int col = 0; col < numItems; col++) {
            // if connection exists between this row and this column add a 1 to the matrix, otherwise, add a 0
            if (matrix.getConnection(sortedItems.get(itemIndex).getUid(), sortedItems.get(col).getAliasUid()) != null) {
                newPrototype.add(1.0);
            } else {
                newPrototype.add(0.0);
            }
        }

    }


    private void updatePrototype(int prototypeIndex) {
        ArrayList<Integer> memberIndices = new ArrayList<>();  // contains list of the items in this prototype
        for(int i = 0; i < sortedItems.size(); i++) {
            if(itemMemberships.get(i) == prototypeIndex) {
                memberIndices.add(i);
            }
        }

        if(memberIndices.size() > 0) {
            ArrayList<Double> newPrototype = adjacencyMatrix.get(memberIndices.get(0));
            for (int i = 1; i < memberIndices.size(); i++) {
                newPrototype = andVectors(newPrototype, adjacencyMatrix.get(i));
            }

            prototypes.get(prototypeIndex).clear();
            prototypes.get(prototypeIndex).addAll(newPrototype);
        }
    }



    /**
     * Performs art1 algorithm on a symmetric DSM
     *
     * @param maxGroups - the max number of groups to allow
     * @param vigilance - algo parameter
     * @param beta - algo parameter
     * @return the new clustered matrix
     */
    public SymmetricDSMData art1Algorithm(int maxGroups, double vigilance, double beta) {
        initPrototypes(maxGroups);

        // create initial prototype to be the first element in the dsm item rows
        int numPrototypes = 1;
        createPrototype(0, 0);
        matrix.addGrouping(prototypeGroups.get(0));  // add this grouping because it is now in use
        matrix.setItemGroup(sortedItems.get(0), prototypeGroups.get(0));


        boolean done = false;
        int iters = 0;
        while(!done) {
            done = true;  // done when no changes have been made
            for(int itemIndex = 0; itemIndex < sortedItems.size(); itemIndex++) {
                boolean addedToGroup = false;  // used to determine when to end early if example was added to prototype
                int prototypeIndex = 0;
                while(prototypeIndex < numPrototypes && !addedToGroup) {  // check if this item matches
                    double similarityMagnitude = vectorMagnitude(andVectors(prototypes.get(prototypeIndex), adjacencyMatrix.get(itemIndex)));

                    double proximityScore = similarityMagnitude / (beta + vectorMagnitude(prototypes.get(prototypeIndex)));
                    double minProximity = vectorMagnitude(adjacencyMatrix.get(itemIndex)) / (beta + numItems);

                    // proximity check -- passes means check vigilance
                    if (proximityScore > minProximity) {
                        // check vigilance
                        if(similarityMagnitude / vectorMagnitude(adjacencyMatrix.get(itemIndex)) > vigilance) {
                            // add item to this group
                            itemMemberships.set(itemIndex, prototypeIndex);
                            matrix.setItemGroup(sortedItems.get(itemIndex), prototypeGroups.get(prototypeIndex));
                            addedToGroup = true;

                            // update the prototype as the bitwise and of the current prototype and the example
                            updatePrototype(prototypeIndex);
                            done = false;
                        }
                    }

                    prototypeIndex++;  // move to next prototype
                }

                if(itemMemberships.get(itemIndex) == -1) {  // if not in a prototype already
                    // if a new prototype can be created, then create one
                    if(numPrototypes < maxGroups) {
                        createPrototype(numPrototypes, itemIndex);

                        matrix.addGrouping(prototypeGroups.get(numPrototypes));  // add this grouping because it is now in use
                        matrix.setItemGroup(sortedItems.get(itemIndex), prototypeGroups.get(numPrototypes));
                        itemMemberships.set(itemIndex, numPrototypes);

                        numPrototypes++;
                    }
                    // if no new prototypes can be created then add it to this last prototype
                    else {
                        // add item to this group
                        matrix.setItemGroup(sortedItems.get(itemIndex), prototypeGroups.get(maxGroups - 1));
                        itemMemberships.set(itemIndex, prototypeIndex);

                        // update the prototype as the bitwise and of the current prototype and the example
                        updatePrototype(maxGroups - 1);
                    }

                    done = false;  // change was just made so not done yet
                }

            }

            iters++;
            if(iters > 10000) {
                done = true;
            }
        }

        return matrix;
    }
}
