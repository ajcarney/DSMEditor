package Matrices.Data;

import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class ART1ClusterAlgorithm {

    ArrayList<DSMItem> sortedItems;

    SymmetricDSMData matrix;
    int numItems;
    ArrayList<ArrayList<Integer>> adjacencyMatrix;

    ArrayList<Integer> itemMemberships;
    ArrayList<ArrayList<Integer>> prototypes;
    ArrayList<Grouping> prototypeGroups;


    public ART1ClusterAlgorithm(SymmetricDSMData inputMatrix) {
        matrix = inputMatrix.createCopy();

        numItems = matrix.getRows().size();
        adjacencyMatrix = new ArrayList<>();
        for(int row = 0; row < numItems; row++) {
            ArrayList<Integer> matrixRow = new ArrayList<>();
            for(int col = 0; col < numItems; col++) {
                // if connection exists between this row and this column add a 1 to the matrix, otherwise, add a 0
                if (matrix.getConnection(sortedItems.get(row).getUid(), sortedItems.get(col).getAliasUid()) != null) {
                    matrixRow.add(1);
                } else {
                    matrixRow.add(0);
                }
            }
            adjacencyMatrix.add(matrixRow);
        }

        // set all items to not having a group
        matrix.clearGroupings();
        itemMemberships = new ArrayList<>();
        prototypes = new ArrayList<>();  // list of column uids where the prototype has a value
        prototypeGroups = new ArrayList<>();
    }


    private void initGroupings(int maxGroups) {
        // create groupings with distinct colors
        // this method for generating random colors is from stack overflow, it generates colors based on a start value
        // and the golden ratio conjugate (golden ratio method)
        double h = 0.2423353;  // use random start value for color generation
        for(int i = 0; i < maxGroups; i++) {
            Grouping group = new Grouping("G" + i, null);
            h += 0.618033988749895;  // golden_ratio_conjugate, this is a part of the golden ratio method for generating unique colors
            h %= 1;
            java.awt.Color hsvColor = java.awt.Color.getHSBColor((float)h, (float)0.5, (float)0.95);

            double r = hsvColor.getRed() / 255.0;
            double g = hsvColor.getGreen() / 255.0;
            double b = hsvColor.getBlue() / 255.0;
            group.setColor(Color.color(r, g, b));
            prototypeGroups.set(i, group);
        }
    }


    private ArrayList<Integer> andVectors(ArrayList<Integer> v1, ArrayList<Integer> v2) {
        ArrayList<Integer> v = new ArrayList<>();
        for(int i = 0; i < v1.size(); i++) {
            if(v1.get(i) > 0 && v2.get(i) > 0) {
                v.add(1);
            } else {
                v.add(0);
            }
        }

        return v;
    }


    private int vectorMagnitude(ArrayList<Integer> v) {
        int sum = 0;
        for (Integer i : v) {
            sum += i;
        }
        return sum;
    }


    private void createPrototype(int prototypeIndex, int itemIndex) {
        ArrayList<Integer> newPrototype = prototypes.get(prototypeIndex);
        for(DSMConnection conn : matrix.getConnections()) {
            if(sortedItems.get(itemIndex).getUid() == conn.getRowUid()) {
                newPrototype.add(1);
            } else {
                newPrototype.add(0);
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

        ArrayList<Integer> newPrototype = adjacencyMatrix.get(0);
        for(int i = 1; i < memberIndices.size(); i++) {
            newPrototype = andVectors(newPrototype, adjacencyMatrix.get(i));
        }

        prototypes.get(prototypeIndex).clear();
        prototypes.get(prototypeIndex).addAll(newPrototype);
    }


    public SymmetricDSMData art1Algorithm(int maxGroups, double vigilance, double beta) {
        initGroupings(maxGroups);

        // create initial prototype to be the first element in the dsm item rows
        int numPrototypes = 1;
        createPrototype(0, 0);
        matrix.addGrouping(prototypeGroups.get(0));  // add this grouping because it is now in use
        matrix.setItemGroup(sortedItems.get(0), prototypeGroups.get(0));


        boolean done = false;
        while(!done) {
            done = true;  // done when no changes have been made
            for(int itemIndex = 0; itemIndex < sortedItems.size(); itemIndex++) {
                boolean addedToGroup = false;  // used to determine when to end early if example was added to prototype
                int prototypeIndex = 0;
                while(prototypeIndex < numPrototypes && !addedToGroup) {  // check if this item matches
                    int similarityMagnitude = vectorMagnitude(andVectors(prototypes.get(prototypeIndex), adjacencyMatrix.get(itemIndex)));

                    double proximityScore = similarityMagnitude / (beta + vectorMagnitude(prototypes.get(prototypeIndex)));
                    double minProximity = vectorMagnitude(adjacencyMatrix.get(itemIndex)) / (beta + numItems);

                    // proximity check -- passes means check vigilance
                    if (proximityScore > minProximity) {
                        // check vigilance
                        if(((double) similarityMagnitude / vectorMagnitude(adjacencyMatrix.get(itemIndex))) > vigilance) {
                            // add item to this group
                            itemMemberships.set(itemIndex, prototypeIndex);
                            sortedItems.get(itemIndex).setGroup1(prototypeGroups.get(prototypeIndex));
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
                        ArrayList<Integer> newPrototype = new ArrayList<>(matrix.getCols().stream().map(DSMItem::getUid).toList());
                        createPrototype(numPrototypes, itemIndex);

                        prototypes.set(numPrototypes, newPrototype);
                        matrix.addGrouping(prototypeGroups.get(numPrototypes));  // add this grouping because it is now in use
                        matrix.setItemGroup(sortedItems.get(numPrototypes), prototypeGroups.get(numPrototypes));
                        itemMemberships.set(itemIndex, numPrototypes);

                        numPrototypes++;
                    }
                    // if no new prototypes can be created then add it to this last prototype
                    else {
                        // add item to this group
                        sortedItems.get(itemIndex).setGroup1(prototypeGroups.get(maxGroups - 1));
                        itemMemberships.set(itemIndex, prototypeIndex);

                        // update the prototype as the bitwise and of the current prototype and the example
                        updatePrototype(maxGroups - 1);
                    }

                    done = false;  // change was just made so not done yet
                }

            }

        }




        return matrix;
    }
}
