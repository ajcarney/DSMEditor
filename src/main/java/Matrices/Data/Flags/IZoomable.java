package Matrices.Data.Flags;

import Matrices.Data.AbstractGroupedDSMData;
import Matrices.Data.Entities.Grouping;


/**
 * Interface that defines how a matrix is to allow zooming to different breakout views
 */
public interface IZoomable {

    /**
     * Takes a matrix from a breakout view and merges its changes
     *
     * @param fromGroup  the "from" grouping item that defines where the breakout view is from
     * @param toGroup    the "to" grouping item that defines where the breakout view is from
     * @param matrix     the breakout view matrix
     */
    void importZoom(Grouping fromGroup, Grouping toGroup, AbstractGroupedDSMData matrix);


    /**
     * Takes a matrix and creates a breakout view from it
     *
     * @param fromGroup  the group that defines the row items
     * @param toGroup    the group that defines the column items
     * @return           the matrix object that is a breakout view
     */
    AbstractGroupedDSMData exportZoom(Grouping fromGroup, Grouping toGroup);

}
