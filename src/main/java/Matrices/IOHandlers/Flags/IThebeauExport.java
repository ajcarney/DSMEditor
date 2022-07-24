package Matrices.IOHandlers.Flags;


import javafx.stage.Window;

import java.io.File;


/**
 * Interface to define how a matrix is to be able to export to thebeau matlab files (.m)
 */
public interface IThebeauExport {

    /**
     * Exports a matrix to a matlab file that can be used with Thebeau's source code
     *
     * @param file      the file to export the matrix to
     * @return          0 on success, 1 on error
     */
    int exportMatrixToThebeauMatlabFile(File file);


    /**
     * Opens a file chooser window to choose a location to export a matrix to the thebeau matlab format
     *
     * @param window the window associated with the file chooser
     */
    void promptExportToThebeau(Window window);
}
