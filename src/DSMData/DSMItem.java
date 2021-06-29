package DSMData;

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
    private String group;


    /**
     * Creates a new DSMItem with the index and name set
     *
     * @param index the starting index of the item
     * @param name  the starting name of the item
     */
    public DSMItem(Double index, String name) {
        this.uid = name.hashCode() + Instant.now().toString().hashCode();
        try {  // wait a millisecond to ensure that the next uid will for sure be unique even with the same name
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.name = name;
        this.sortIndex = index;
        this.group = "(None)";
    }


    /**
     * Creates a new DSMItem with all data fields to be set. Called when creating an item that was saved to a file
     *
     * @param uid      the uid of the item
     * @param aliasUid the uid of the row associated with a column item. Use null if item is not a column item or item is not part of a symmetric matrix
     * @param index    the sort index of the item
     * @param name     the name of the item
     * @param group    the group of the item
     */
    public DSMItem(Integer uid, Integer aliasUid, Double index, String name, String group) {
        this.uid = uid;
        this.aliasUid = aliasUid;
        this.name = name;
        this.sortIndex = index;
        this.group = group;
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
     * Getter function for the group of the item
     *
     * @return the group of the item
     */
    public String getGroup() {
        return group;
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
     * Setter function for the group of the item
     *
     * @param group the new group of the item
     */
    public void setGroup(String group) {
        this.group = group;
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

