package Matrices.Data;

import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.paint.Color;

import java.lang.reflect.InvocationTargetException;


/**
 * Extends the functionality of a AbstractDSMData to add groupings. Contains methods for handling groupings
 */
public abstract class AbstractGroupedDSMData extends AbstractDSMData implements Cloneable {
    protected ObservableSet<Grouping> groupings;  // ObservableSet is used so that any gui threads reading it will see changes without needing a callback set up
    protected final Grouping defaultGroup = new Grouping(Integer.MAX_VALUE, "(none)", Color.color(1.0, 1.0, 1.0), Grouping.defaultFontColor);  // create a default group when none is assigned (this will always have the same uid)

    /**
     * Generic constructor. Creates empty list for groupings
     */
    public AbstractGroupedDSMData() {
        super();
        groupings = FXCollections.observableSet();
    }


    /**
     * Copy constructor for AbstractGroupedDSMData class. Performs a deep copy
     *
     * @param copy  AbstractGroupedDSMData object to copy
     */
    public AbstractGroupedDSMData(AbstractGroupedDSMData copy) {
        super();
        this.groupings = copy.getGroupings();
    }


    /**
     * Gets the default grouping object
     *
     * @return the default group for the matrix
     */
    public Grouping getDefaultGrouping() {
        return defaultGroup;
    }


    /**
     * Adds a new grouping to the matrix. Puts the change on the stack but does not set a checkpoint
     *
     * @param group  the object of type Grouping to add
     */
    public void addGrouping(Grouping group) {
        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    groupings.add(group);
                },
                () -> {  // undo function
                    groupings.remove(group);
                    for(DSMItem item : rows) {
                        if(item.getGroup1().equals(group)) {
                            item.setGroup1(defaultGroup);
                        }
                    }
                    for(DSMItem item : cols) {
                        if(item.getGroup1().equals(group)) {
                            item.setGroup1(defaultGroup);
                        }
                    }
                },
                false
        ));
    }


    /**
     * Removes a grouping from the matrix. Puts the change on the stack but does not set a checkpoint
     *
     * @param group  the object of type Grouping to remove
     */
    public void removeGrouping(Grouping group) {
        for(DSMItem item : rows) {  // these changes already get put on the stack so no need to add them a second time
            if(item.getGroup1().equals(group)) {
                setItemGroup(item, defaultGroup);
            }
        }
        for(DSMItem item : cols) {
            if(item.getGroup1().equals(group)) {
                setItemGroup(item, defaultGroup);
            }
        }

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    groupings.remove(group);
                },
                () -> {  // undo function
                    groupings.add(group);
                },
                false
        ));
    }


    /**
     * Removes all groupings from the matrix. Puts the change on the stack but does not set a checkpoint
     */
    public void clearGroupings() {
        ObservableSet<Grouping> oldGroupings = FXCollections.observableSet();
        oldGroupings.addAll(groupings);

        for(DSMItem r : rows) {
            setItemGroup(r, defaultGroup);
        }
        for(DSMItem c : cols) {
            setItemGroup(c, defaultGroup);
        }

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    groupings.clear();
                },
                () -> {  // undo function
                    groupings = oldGroupings;
                },
                false
        ));
    }


    /**
     * Returns the ObservableSet of the groupings that is used for gui widgets to auto update and
     * not need a callback to update
     *
     * @return ObservableSet of the current groupings
     */
    public ObservableSet<Grouping> getGroupings() {
        return groupings;
    }


    /**
     * Renames a grouping and updates all DSMItem objects with the new grouping name. Puts the change on the stack
     * but does not set a checkpoint
     *
     * @param group    the group who's name should be changed
     * @param newName  the new name for the group
     */
    public void renameGrouping(Grouping group, String newName) {
        String oldName = group.getName();

        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    group.setName(newName);
                },
                () -> {  // undo function
                    group.setName(oldName);
                },
                false
        ));
    }


    /**
     * Changes a color of a grouping. Puts the change on the stack but does not set a checkpoint
     *
     * @param group    the group who's name should be changed
     * @param newColor the new color of the grouping
     */
    public void updateGroupingColor(Grouping group, Color newColor) {
        Color oldColor = group.getColor();
        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    group.setColor(newColor);
                },
                () -> {  // undo function
                    group.setColor(oldColor);
                },
                false
        ));
    }


    /**
     * Changes a color of a grouping. Puts the change on the stack but does not set a checkpoint
     *
     * @param group    the group who's font color should be changed
     * @param newColor the new color of the grouping
     */
    public void updateGroupingFontColor(Grouping group, Color newColor) {
        Color oldColor = group.getFontColor();
        addChangeToStack(new MatrixChange(
                () -> {  // do function
                    group.setFontColor(newColor);
                },
                () -> {  // undo function
                    group.setFontColor(oldColor);
                },
                false
        ));
    }


    /**
     * Sets the group of an item in the matrix. This method should be called instead of directly modifying the item
     * because this method puts the change on the stack but does not set a checkpoint.
     *
     * @param item     the item to change the name of
     * @param newGroup the new group for the item
     */
    public abstract void setItemGroup(DSMItem item, Grouping newGroup);

}
