package Data;

import java.time.Instant;

/**
 * Data class to handle an item in a DSM. Each item has a uid, an alias uid to be used to associate rows
 * and columns in a symmetric matrix, a name, a sort index, and a group
 *
 * @author Aiden Carney
 */
public class DSMItem {
    private final Integer uid;
    private Integer aliasUid;
    private String name;
    private Double sortIndex;
    private Grouping group1;  // how these 2 groupings are used is up to implementation of the DSM type
    private Grouping group2;


    /**
     * Creates a new DSMItem with the index and name set
     *
     * @param index the starting index of the item
     * @param name  the starting name of the item
     */
    public DSMItem(Double index, String name) {
        this.uid = java.util.UUID.randomUUID().hashCode();

        this.name = name;
        this.sortIndex = index;
        this.group1 = null;
        this.group2 = null;
    }


    /**
     * Creates a new DSMItem with all data fields to be set. Called when creating an item that was saved to a file
     *
     * @param uid      the uid of the item
     * @param aliasUid the uid of the row associated with a column item. Use null if item is not a column item or item is not part of a symmetric matrix
     * @param index    the sort index of the item
     * @param name     the name of the item
     * @param group1   the first group of the item
     * @param group2   the second group of the item
     */
    public DSMItem(Integer uid, Integer aliasUid, Double index, String name, Grouping group1, Grouping group2) {
        this.uid = uid;
        this.aliasUid = aliasUid;
        this.name = name;
        this.sortIndex = index;
        this.group1 = group1;
        this.group2 = group2;
    }


    /**
     * Copy constructor for DSMItem
     *
     * @param copy DSMItem object that will be copied
     */
    public DSMItem(DSMItem copy) {
        uid = copy.getUid();
        aliasUid = copy.getAliasUid();
        name = copy.getName();
        sortIndex = copy.getSortIndex();
        this.group1 = copy.getGroup1();
        this.group2 = copy.getGroup2();
    }


    /**
     * Getter function for the uid of the item
     *
     * @return the uid of the item
     */
    public int getUid() {
        return uid;
    }


    /**
     * Getter function for the name of the item
     *
     * @return the name of the item
     */
    public String getName() {
        return name;
    }


    /**
     * Getter function for the sort index of the item
     *
     * @return the sort index of the item
     */
    public double getSortIndex() {
        return sortIndex;
    }

    /**
     * Getter function for the alias uid of the item
     *
     * @return the alias uid of the item
     */
    public Integer getAliasUid() {
        return aliasUid;
    }


    /**
     * Getter function for the first group of the item
     *
     * @return the second group of the item
     */
    public Grouping getGroup1() {
        return group1;
    }


    /**
     * Getter function for the second group of the item
     *
     * @return the first group of the item
     */
    public Grouping getGroup2() {
        return group2;
    }


    /**
     * Setter function for the name of the item
     *
     * @param name the new name of the item
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Setter function for the sort index of the item
     *
     * @param index the new sort index of the item
     */
    public void setSortIndex(double index) {
        this.sortIndex = index;
    }


    /**
     * Setter function for the alias uid of the item
     *
     * @param aliasUid the new alias uid of the item
     */
    public void setAliasUid(Integer aliasUid) {
        this.aliasUid = aliasUid;
    }


    /**
     * Setter function for the first group of the item
     *
     * @param group1 the new group of the item
     */
    public void setGroup1(Grouping group1) {
        this.group1 = group1;
    }


    /**
     * Setter function for the second group of the item
     *
     * @param group2 the new group of the item
     */
    public void setGroup2(Grouping group2) {
        this.group2 = group2;
    }


    /**
     * Overrides the toString() function of the class so that when object is printed or used in the gui
     * the text is shown as the name assigned to the item
     *
     * @return the name field of the item
     */
    @Override
    public String toString() {
        return name;
    }
}

