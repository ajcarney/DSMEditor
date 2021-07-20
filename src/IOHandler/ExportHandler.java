package IOHandler;

import DSMData.DSMConnection;
import DSMData.DSMData;
import DSMData.DSMItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A class with methods that handle exporting or saving a DSMData matrix object
 * Currently supports DSM file (.dsm), CSV file (.csv), Excel spreadsheet (.xlsx), and Thebeau
 * Matlab files (.m)
 */
public class ExportHandler {
    /**
     * Saves a matrix to a csv file that includes the matrix metadata
     *
     * @param matrix    the matrix to export
     * @param file      the file to save the csv file to  TODO: add validation that the file is in fact .csv
     * @return          1 on success, 0 on error
     */
    static public int exportMatrixToCSV(DSMData matrix, File file) {
        try {
            String contents = "Title," + matrix.getTitle() + "\n";
            contents += "Project Name," + matrix.getProjectName() + "\n";
            contents += "Customer," + matrix.getCustomer() + "\n";
            contents += "Version," + matrix.getVersionNumber() + "\n";

            ArrayList<ArrayList<Pair<String, Object>>> template = matrix.getGridArray();
            int rows = template.size();
            int columns = template.get(0).size();

            for(int r=0; r<rows; r++) {
                for (int c = 0; c < columns; c++) {
                    Pair<String, Object> item = template.get(r).get(c);

                    if (item.getKey().equals("plain_text") || item.getKey().equals("plain_text_v")) {
                        contents += item.getValue() + ",";
                    } else if (item.getKey().equals("item_name") || item.getKey().equals("item_name_v")) {
                        contents += matrix.getItem((Integer) item.getValue()).getName() + ",";
                    } else if (item.getKey().equals("grouping_item") || item.getKey().equals("grouping_item_v")) {
                        contents += matrix.getItem((Integer) item.getValue()).getGroup() + ",";
                    } else if (item.getKey().equals("index_item")) {
                        contents += matrix.getItem((Integer) item.getValue()).getSortIndex() + ",";
                    } else if (item.getKey().equals("uneditable_connection")) {
                        contents += ",";
                    } else if (item.getKey().equals("editable_connection")) {
                        int rowUid = ((Pair<Integer, Integer>)item.getValue()).getKey();
                        int colUid = ((Pair<Integer, Integer>)item.getValue()).getValue();
                        if(matrix.getConnection(rowUid, colUid) != null) {
                            contents += matrix.getConnection(rowUid, colUid).getConnectionName();
                        }
                        contents += ",";
                    }
                }
                contents += "\n";
            }

            FileWriter writer = new FileWriter(file);
            writer.write(contents);
            writer.close();

            return 1;
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e);
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }


    /**
     * Saves a matrix to an Excel Spreadsheet file. The spreadsheet includes the matrix metadata.
     * Cells are highlighted and auto sized. The matrix itself is shifted by ROW_START and COL_START
     * so that the sizing for it is not impacted by the matrix metadata
     *
     * @param matrix    the matrix to export
     * @param file      A File object of the location of the .xlsx file  TODO: add validation that it is a .xlsx file
     * @return          1 on success, 0 on error
     */
    static public int exportMatrixToXLSX(DSMData matrix, File file) {
        try {
            // set up document
            XSSFWorkbook workbook = new XSSFWorkbook();
            String safeName = WorkbookUtil.createSafeSheetName(file.getName().replaceFirst("[.][^.]+$", "")); // TODO: validate this regex
            XSSFSheet sheet = workbook.createSheet(safeName);

            // create metadata rows
            Row row0 = sheet.createRow(0);
            Row row1 = sheet.createRow(1);
            Row row2 = sheet.createRow(2);
            Row row3 = sheet.createRow(3);
            row0.createCell(0).setCellValue("Title");
            row1.createCell(0).setCellValue("Project Name");
            row2.createCell(0).setCellValue("Customer");
            row3.createCell(0).setCellValue("Version");
            row0.createCell(1).setCellValue(matrix.getProjectName());
            row1.createCell(1).setCellValue(matrix.getProjectName());
            row2.createCell(1).setCellValue(matrix.getCustomer());
            row3.createCell(1).setCellValue(matrix.getVersionNumber());

            // fill with content
            final int ROW_START = 6;  // start row and col so that matrix data is shifted
            final int COL_START = 3;

            short HORIZONTAL_ROTATION = 0;
            short VERTICAL_ROTATION = 90;

            ArrayList<ArrayList<Pair<String, Object>>> template = matrix.getGridArray();
            int rows = template.size();
            int columns = template.get(0).size();

            for(int r=0; r<rows; r++) {
                Row row = sheet.createRow(r + ROW_START);
                for (int c=0; c<columns; c++) {
                    Pair<String, Object> item = template.get(r).get(c);

                    if (item.getKey().equals("plain_text")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(item.getValue().toString());

                        CellStyle cellStyle = workbook.createCellStyle();
                        cellStyle.setRotation(HORIZONTAL_ROTATION);
                        cell.setCellStyle(cellStyle);
                    } else if(item.getKey().equals("plain_text_v")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(item.getValue().toString());

                        CellStyle cellStyle = workbook.createCellStyle();
                        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
                        cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        cellStyle.setRotation(VERTICAL_ROTATION);
                        cell.setCellStyle(cellStyle);
                    } else if (item.getKey().equals("item_name")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(matrix.getItem((Integer) item.getValue()).getName());

                        javafx.scene.paint.Color cellColor = matrix.getGroupingColors().get(matrix.getItem((Integer)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(HORIZONTAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if(item.getKey().equals("item_name_v")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(matrix.getItem((Integer) item.getValue()).getName());

                        javafx.scene.paint.Color cellColor = matrix.getGroupingColors().get(matrix.getItem((Integer)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setRotation(VERTICAL_ROTATION);
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("grouping_item")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(matrix.getItem((Integer) item.getValue()).getGroup());

                        javafx.scene.paint.Color cellColor = matrix.getGroupingColors().get(matrix.getItem((Integer)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(HORIZONTAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("grouping_item_v")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(matrix.getItem((Integer) item.getValue()).getGroup());

                        javafx.scene.paint.Color cellColor = matrix.getGroupingColors().get(matrix.getItem((Integer)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(VERTICAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("index_item")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(matrix.getItem((Integer) item.getValue()).getSortIndex());

                        javafx.scene.paint.Color cellColor = matrix.getGroupingColors().get(matrix.getItem((Integer)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(HORIZONTAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("uneditable_connection")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue("");

                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 0, 0), new DefaultIndexedColorMap()));  // TODO: set this to the color defined in MatrixGuiHandler
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(HORIZONTAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("editable_connection")) {
                        Integer rowUid = ((Pair<Integer, Integer>)item.getValue()).getKey();
                        Integer colUid = ((Pair<Integer, Integer>)item.getValue()).getValue();

                        Cell cell = row.createCell(c + COL_START);
                        if(matrix.getConnection(rowUid, colUid) != null) {
                            cell.setCellValue(matrix.getConnection(rowUid, colUid).getConnectionName());
                        }

                        // highlight cell
                        javafx.scene.paint.Color rowColor = matrix.getGroupingColors().get(matrix.getItem(rowUid).getGroup());
                        if (rowColor == null) rowColor = javafx.scene.paint.Color.color(1.0, 1.0, 1.0);

                        javafx.scene.paint.Color colColor = matrix.getGroupingColors().get(matrix.getItem(colUid).getGroup());
                        if (colColor == null) colColor = Color.color(1.0, 1.0, 1.0);

                        double red = (rowColor.getRed() + colColor.getRed()) / 2;
                        double green = (rowColor.getGreen() + colColor.getGreen()) / 2;
                        double blue = (rowColor.getBlue() + colColor.getBlue()) / 2;

                        XSSFCellStyle style = workbook.createCellStyle();
                        if (matrix.isSymmetrical() && !rowUid.equals(matrix.getItem(colUid).getAliasUid()) && matrix.getItem(rowUid).getGroup().equals(matrix.getItem(colUid).getGroup())) {  // associated row and column are same group
                            style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float)(red), (float)(green), (float)(blue)), new DefaultIndexedColorMap()));
                            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            style.setRotation(HORIZONTAL_ROTATION);
                        } else if (!matrix.isSymmetrical()) {
                            style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float)(red), (float)(green), (float)(blue)), new DefaultIndexedColorMap()));
                            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                            style.setRotation(HORIZONTAL_ROTATION);
                        } else {
                            style.setRotation(HORIZONTAL_ROTATION);
                        }
                        cell.setCellStyle(style);
                    }
                    sheet.autoSizeColumn(c + COL_START);
                }
            }

            // write file
            System.out.println(file.getAbsolutePath());
            OutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();

            return 1;
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e);
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }


    /**
     * Exports a matrix to a matlab file that can be used with Thebeau's source code
     *
     * @param matrix    the matrix to export
     * @param file      the file to export the matrix to
     * @return          1 on success, 0 on error
     */
    static public int exportMatrixToThebeauMatlabFile(DSMData matrix, File file) {
        try {
            matrix.reDistributeSortIndexes();  // re-number 0 -> n

            String connectionsString = "";
            for(DSMConnection conn: matrix.getConnections()) {
                String c = "DSM("
                        + (int)matrix.getItem(conn.getRowUid()).getSortIndex()  // add one because matlab is 1 indexed
                        + ","
                        + (int)matrix.getItem(conn.getRowUid()).getSortIndex()  // add one because matlab is 1 indexed
                        + ") = "
                        + conn.getWeight()
                        + ";\n";
                connectionsString += c;
            }

            String labelsString = "";
            for(DSMItem row : matrix.getRows()) {
                String l = "DSMLABEL{"
                        + (int)row.getSortIndex()
                        + ",1} = '"
                        + row.getName()
                        + "';\n";
                labelsString += l;
            }

            String matlabString = "DSM_size = "
                    + matrix.getRows().size()  // add one because of how the matlab script works
                    + ";\nDSM = zeros(DSM_size);\n\n\n"
                    + connectionsString
                    + "\n\nDSMLABEL = cell(DSM_size,1);\n"
                    + labelsString;

            PrintWriter out = new PrintWriter(file);
            out.println(matlabString);
            out.close();

            return 1;
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e);
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }



    /**
     * Saves the matrix to an xml file specified by the caller of the function. Clears
     * the matrix's wasModifiedFlag
     *
     * @param matrix    the matrix to save
     * @param file      the file to save the matrix to
     * @return          1 on success, 0 on error
     */
    static public int saveMatrixToFile(DSMData matrix, File file) {
        try {
            // create xml
            Element rootElement = new Element("dsm");
            Document doc = new Document(rootElement);

            Element infoElement = new Element("info");
            Element rowsElement = new Element("rows");
            Element colsElement = new Element("columns");
            Element connectionsElement = new Element("connections");
            Element groupingsElement = new Element("groupings");

            // update information
            infoElement.addContent(new Element("title").setText(matrix.getTitle()));
            infoElement.addContent(new Element("project").setText(matrix.getProjectName()));
            infoElement.addContent(new Element("customer").setText(matrix.getCustomer()));
            infoElement.addContent(new Element("version").setText(matrix.getVersionNumber()));
            if(matrix.isSymmetrical()) {
                infoElement.addContent(new Element("symmetric").setText("1"));
            } else {
                infoElement.addContent(new Element("symmetric").setText("0"));
            }

            // create column elements
            for(DSMItem col : matrix.getCols()) {
                Element colElement = new Element("col");
                colElement.setAttribute(new Attribute("uid", Integer.valueOf(col.getUid()).toString()));
                colElement.addContent(new Element("name").setText(col.getName()));
                colElement.addContent(new Element("sort_index").setText(Double.valueOf(col.getSortIndex()).toString()));
                if(col.getAliasUid() != null) {
                    colElement.addContent(new Element("alias").setText(col.getAliasUid().toString()));
                }
                colElement.addContent(new Element("group").setText(col.getGroup()));

                colsElement.addContent(colElement);
            }

            // create row elements
            for(DSMItem row : matrix.getRows()) {
                Element rowElement = new Element("row");
                rowElement.setAttribute(new Attribute("uid", Integer.valueOf(row.getUid()).toString()));
                rowElement.addContent(new Element("name").setText(row.getName()));
                rowElement.addContent(new Element("sort_index").setText(Double.valueOf(row.getSortIndex()).toString()));
                rowElement.addContent(new Element("group").setText(row.getGroup()));
                rowsElement.addContent(rowElement);
            }

            // create connection elements
            for(DSMConnection connection : matrix.getConnections()) {
                Element connElement = new Element("connection");
                connElement.addContent(new Element("row_uid").setText(Integer.valueOf(connection.getRowUid()).toString()));
                connElement.addContent(new Element("col_uid").setText(Integer.valueOf(connection.getColUid()).toString()));
                connElement.addContent(new Element("name").setText(connection.getConnectionName()));
                connElement.addContent(new Element("weight").setText(Double.valueOf(connection.getWeight()).toString()));
                connectionsElement.addContent(connElement);
            }

            // create groupings elements
            for(Map.Entry<String, Color> group: matrix.getGroupingColors().entrySet()) {
                Element groupElement = new Element("group");
                groupElement.addContent(new Element("name").setText(group.getKey()));
                groupElement.addContent(new Element("r").setText(Double.valueOf(group.getValue().getRed()).toString()));
                groupElement.addContent(new Element("g").setText(Double.valueOf(group.getValue().getGreen()).toString()));
                groupElement.addContent(new Element("b").setText(Double.valueOf(group.getValue().getBlue()).toString()));

                groupingsElement.addContent(groupElement);
            }

            doc.getRootElement().addContent(infoElement);
            doc.getRootElement().addContent(colsElement);
            doc.getRootElement().addContent(rowsElement);
            doc.getRootElement().addContent(connectionsElement);
            doc.getRootElement().addContent(groupingsElement);

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());  // TODO: change this to getCompactFormat() for release
            xmlOutput.output(doc, new FileOutputStream(file));

            System.out.println("Saving file " + file);
            matrix.clearWasModifiedFlag();

            return 1;  // file was successfully saved
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e);
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }



    /**
     * Brings up a dialogue window that asks whether the user wants to save a file or not.
     * Presents the user with three options: save, don't save, and cancel. This function
     * should be called before removing a matrix
     *
     * @param file The matrix save path
     * @return     0 = don't save, 1 = save, 2 = cancel
     */
    static public Integer promptSave(String file) {
        AtomicReference<Integer> code = new AtomicReference<>(); // 0 = close the tab, 1 = save and close, 2 = don't close
        code.set(2);  // default value
        Stage window = new Stage();

        Label prompt = new Label("Would you like to save your changes to " + file);

        // Create Root window
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("DSMEditor");

        // create HBox for user to close with our without changes
        HBox optionsArea = new HBox();
        optionsArea.setAlignment(Pos.CENTER);
        optionsArea.setSpacing(15);
        optionsArea.setPadding(new Insets(10, 10, 10, 10));

        Button saveAndCloseButton = new Button("Save");
        saveAndCloseButton.setOnAction(ee -> {
            code.set(1);
            window.close();
        });

        Button closeButton = new Button("Don't Save");
        closeButton.setOnAction(ee -> {
            code.set(0);
            window.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(ee -> {
            code.set(2);
            window.close();
        });

        optionsArea.getChildren().addAll(saveAndCloseButton, closeButton, cancelButton);


        VBox layout = new VBox(10);
        layout.getChildren().addAll(prompt, optionsArea);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(10);


        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(layout, 500, 125);
        window.setScene(scene);
        window.showAndWait();

        return code.get();
    }


    /**
     * Opens a file chooser window to choose a location to save a matrix to
     *
     * @param matrix the matrix to save
     * @param window the window associated with the file chooser
     */
    static public void promptSaveToFile(DSMData matrix, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = ExportHandler.saveMatrixToFile(matrix, fileName);
        }
    }


    /**
     * Opens a file chooser window to choose a location to export a matrix to csv
     *
     * @param matrix the matrix to save
     * @param window the window associated with the file chooser
     */
    static public void promptExportToCSV(DSMData matrix, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = ExportHandler.exportMatrixToCSV(matrix, fileName);
        }
    }


    /**
     * Opens a file chooser window to choose a location to export a matrix to excel
     *
     * @param matrix the matrix to save
     * @param window the window associated with the file chooser
     */
    static public void promptExportToExcel(DSMData matrix, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Microsoft Excel File", "*.xlsx"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = exportMatrixToXLSX(matrix, fileName);
        }
    }


    /**
     * Opens a file chooser window to choose a location to export a matrix to the thebeau matlab format
     *
     * @param matrix the matrix to save
     * @param window the window associated with the file chooser
     */
    static public void promptExportToThebeau(DSMData matrix, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Matlab File", "*.m"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = ExportHandler.exportMatrixToThebeauMatlabFile(matrix, fileName);
        }
    }

}
