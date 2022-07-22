package Matrices.Data;

import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;


/**
 * Test class for the MultiDomainDSM class. Mostly contains tests
 * for stressing the undo and redo functionality.
 */
public class MultiDomainDSMDataTest {

    private static final int NUM_UNDO_REDO_CYCLES = 3;

    /**
     * Stress the undo and redo functionality for a given checkpoint. Ends in the same state but after a
     * set number of undo and redo cycles
     *
     * @param matrix  the matrix data object
     */
    private static void stressUndoRedo(SymmetricDSMData matrix) {
        for(int i=0; i<NUM_UNDO_REDO_CYCLES; i++) {
            matrix.undoToCheckpoint();
            matrix.redoToCheckpoint();
        }
    }


    /**
     *
     */
    @Test
    public void createCopyTest() {

    }


    /**
     *
     */
    @Test
    public void addDomainTest() {

    }


    /**
     *
     */
    @Test
    public void addDomainGroupingTest() {

    }


    /**
     *
     */
    @Test
    public void removeDomainTest() {

    }


    /**
     *
     */
    @Test
    public void removeDomainGroupingTest() {

    }


    /**
     *
     */
    @Test
    public void clearDomainGroupingsTest() {

    }


    /**
     *
     */
    @Test
    public void renameGroupingTest() {

    }


    /**
     *
     */
    @Test
    public void updateGroupingColorTest() {

    }


    /**
     *
     */
    @Test
    public void updateGroupingFontColorTest() {

    }


    /**
     *
     */
    @Test
    public void setItemDomainGroupTest() {

    }


    /**
     *
     */
    @Test
    public void createItemTest() {

    }


    /**
     *
     */
    @Test
    public void deleteItemTest() {

    }


    /**
     *
     */
    @Test
    public void setItemNameTest() {

    }


    /**
     *
     */
    @Test
    public void setItemSortIndexTest() {

    }


    /**
     *
     */
    @Test
    public void createConnectionTest() {

    }


    /**
     *
     */
    @Test
    public void modifyConnectionSymmetricTest() {

    }

}
