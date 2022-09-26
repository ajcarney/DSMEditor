package Matrices.IOHandlers.Flags;

import Matrices.Data.AbstractDSMData;
import Matrices.Views.AbstractMatrixView;
import javafx.stage.Window;

import java.io.File;


/**
 * Interface to define the standard exports a matrix is to have:
 *     .csv
 *     .xlsx
 *     .png
 * Also defines functions that open a window to ask where to save the export to
 */
public interface IStandardExports {

    /**
     * Saves a matrix to a csv file that includes the matrix metadata
     *
     * @param file      the file to save the csv file to
     * @return          0 on success, 1 on error
     */
    int exportMatrixToCSV(File file);


    /**
     * Opens a file chooser window to choose a location to export a matrix to csv
     *
     * @param window the window associated with the file chooser
     */
    void promptExportToCSV(Window window);


    /**
     * Saves a matrix to an Excel Spreadsheet file.
     *
     * @param file      A File object of the location of the .xlsx file
     * @return          0 on success, 1 on error
     */
    int exportMatrixToXLSX(File file);


    /**
     * Opens a file chooser window to choose a location to export a matrix to excel
     *
     * @param window  the window associated with the file chooser
     */
    void promptExportToExcel(Window window);


    /**
     * Opens a window to export a matrix to a png file with different configuration options
     *
     * @param matrix      the matrix object to save to an image
     * @param matrixView  the matrix gui handler for the matrix object
     */
    void exportToImage(AbstractDSMData matrix, AbstractMatrixView matrixView);

}
