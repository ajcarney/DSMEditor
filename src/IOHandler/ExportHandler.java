package IOHandler;

import Data.DSMConnection;
import Data.SymmetricDSM;
import Data.DSMItem;
import gui.MatrixGuiHandler;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


/**
 * A class with methods that handle exporting or saving a SymmetricDSM matrix object
 * Currently supports DSM file (.dsm), CSV file (.csv), Excel spreadsheet (.xlsx), and Thebeau
 * Matlab files (.m)
 *
 * @author Aiden Carney
 */
public class ExportHandler {
    /**
     * Forces a file to have a specific extension. Returns a new file object with the specified extension.
     * Checks if the file absolute path ends with ".extension" and if not adds it
     *
     * @param file      the file to check
     * @param extension the extension to force
     * @return          a file object with the extension at the end
     */
    static private File forceExtension(File file, String extension) {
        String path = file.getAbsolutePath();
        if(!path.endsWith(extension)) {
            path += extension;
            return new File(path);
        }

        return file;
    }


    /**
     * Saves a matrix to a csv file that includes the matrix metadata
     *
     * @param matrix    the matrix to export
     * @param file      the file to save the csv file to
     * @return          1 on success, 0 on error
     */
    static public int exportMatrixToCSV(SymmetricDSM matrix, File file) {
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
                        contents += ((DSMItem)item.getValue()).getName() + ",";
                    } else if (item.getKey().equals("grouping_item") || item.getKey().equals("grouping_item_v")) {
                        contents += ((DSMItem)item.getValue()).getGroup() + ",";
                    } else if (item.getKey().equals("index_item")) {
                        contents += ((DSMItem)item.getValue()).getSortIndex() + ",";
                    } else if (item.getKey().equals("uneditable_connection")) {
                        contents += ",";
                    } else if (item.getKey().equals("editable_connection")) {
                        int rowUid = ((Pair<DSMItem, DSMItem>)item.getValue()).getKey().getUid();
                        int colUid = ((Pair<DSMItem, DSMItem>)item.getValue()).getValue().getUid();
                        if(matrix.getConnection(rowUid, colUid) != null) {
                            contents += matrix.getConnection(rowUid, colUid).getConnectionName();
                        }
                        contents += ",";
                    }
                }
                contents += "\n";
            }

            file = forceExtension(file, ".csv");
            System.out.println("Exporting to " + file.getAbsolutePath());
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
     * @param file      A File object of the location of the .xlsx file
     * @return          1 on success, 0 on error
     */
    static public int exportMatrixToXLSX(SymmetricDSM matrix, File file) {
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
                        cell.setCellValue(((DSMItem)item.getValue()).getName());

                        javafx.scene.paint.Color cellColor = matrix.getGroupingColors().get(((DSMItem)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(HORIZONTAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if(item.getKey().equals("item_name_v")) {
                        Cell cell = row.createCell(c + COL_START);
                        System.out.println(item);
                        cell.setCellValue(((DSMItem)item.getValue()).getName());

                        javafx.scene.paint.Color cellColor = matrix.getGroupingColors().get(((DSMItem)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setRotation(VERTICAL_ROTATION);
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("grouping_item")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(((DSMItem)item.getValue()).getGroup());

                        javafx.scene.paint.Color cellColor = matrix.getGroupingColors().get(((DSMItem)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(HORIZONTAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("grouping_item_v")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(((DSMItem)item.getValue()).getGroup());

                        javafx.scene.paint.Color cellColor = matrix.getGroupingColors().get(((DSMItem)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(VERTICAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("index_item")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(((DSMItem)item.getValue()).getSortIndex());

                        javafx.scene.paint.Color cellColor = matrix.getGroupingColors().get(((DSMItem)item.getValue()).getGroup());
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
                        Integer rowUid = ((Pair<DSMItem, DSMItem>)item.getValue()).getKey().getUid();
                        Integer colUid = ((Pair<DSMItem, DSMItem>)item.getValue()).getValue().getUid();

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
            file = forceExtension(file, ".xlsx");
            System.out.println("Exporting to " + file.getAbsolutePath());
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
    static public int exportMatrixToThebeauMatlabFile(SymmetricDSM matrix, File file) {
        try {
            matrix.reDistributeSortIndices();  // re-number 0 -> n

            String connectionsString = "";
            for(DSMConnection conn: matrix.getConnections()) {
                DSMItem row = matrix.getItem(conn.getRowUid());
                DSMItem col = matrix.getItem(conn.getColUid());

                String c = "DSM("
                        + (int)matrix.getItem(conn.getRowUid()).getSortIndex()  // add one because matlab is 1 indexed
                        + ","
                        + (int)matrix.getItem(conn.getColUid()).getSortIndex()  // add one because matlab is 1 indexed
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


            file = forceExtension(file, ".m");
            System.out.println("Exporting to " + file.getAbsolutePath());
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
     * Converts a symmetrical matrix to a non-symmetrical matrix in place.
     * Method can be called on non-symmetrical matrix without issues.
     *
     * @param matrix    the matrix to convert
     * @param clearRows whether or not to clear rows or columns when converting
     */
    static public void convertToNonSymmetrical(SymmetricDSM matrix, boolean clearRows) {
        if(clearRows) {
            matrix.deleteRows();
        } else {
            matrix.deleteCols();
        }
        matrix.setSymmetrical(false);
    }


    /**
     * Exports a symmetrical matrix as non-symmetrical in a new file
     *
     * @param matrix    the matrix to convert
     * @param file      the file to export to
     * @param clearRows clear matrix rows if true, matrix columns if false
     * @return          error code: 1 on success
     */
    static public int exportSymmetricalToNonSymmetrical(SymmetricDSM matrix, File file, boolean clearRows) {
        convertToNonSymmetrical(matrix, clearRows);
        SymmetricDSM nonSymmetricMatrix = new SymmetricDSM(matrix);  // make a copy of the matrix

        int code = saveMatrixToFile(nonSymmetricMatrix, file);
        return code;
    }


    /**
     * Saves the matrix to an xml file specified by the caller of the function. Clears
     * the matrix's wasModifiedFlag
     *
     * @param matrix    the matrix to save
     * @param file      the file to save the matrix to
     * @return          1 on success, 0 on error
     */
    static public int saveMatrixToFile(SymmetricDSM matrix, File file) {
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
                if(row.getAliasUid() != null) {
                    rowElement.addContent(new Element("alias").setText(row.getAliasUid().toString()));
                } else if(row.getAliasUid() == null && matrix.isSymmetrical()) {  // leave for compatibility with old files
                    for(DSMItem col : matrix.getCols()) {
                        if(col.getAliasUid() == row.getUid()) {
                            rowElement.addContent(new Element("alias").setText(String.valueOf(col.getUid())));
                            break;
                        }
                    }
                }
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


    static public void exportToImage(SymmetricDSM matrix) {
        // Create Root window
        Stage window = new Stage();
//        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("DSMEditor");

        SplitPane splitPane = new SplitPane();

        // preview area
        VBox preview = new VBox();
        preview.setAlignment(Pos.CENTER_LEFT);
        preview.setSpacing(10);
        preview.setPadding(new Insets(5));


        // parameters area
        VBox parametersLayout = new VBox();
        parametersLayout.setAlignment(Pos.CENTER);
        parametersLayout.setSpacing(10);
        parametersLayout.setPadding(new Insets(5));

        // add info about the matrix (customer, title, version, etc.)
        CheckBox addInfo = new CheckBox("Add Matrix Info");
        addInfo.setSelected(true);
        addInfo.setMaxWidth(Double.MAX_VALUE);

        // make title big and centered
        CheckBox bigTitle = new CheckBox("Big Title");
        bigTitle.setSelected(true);
        bigTitle.setMaxWidth(Double.MAX_VALUE);

        // show connection names
        CheckBox showConnectionNames = new CheckBox("Show Connection Names");
        showConnectionNames.setSelected(true);
        showConnectionNames.setMaxWidth(Double.MAX_VALUE);

        // annotation
        CheckBox addAnnotation = new CheckBox("Add Annotation");
        addAnnotation.setMaxWidth(Double.MAX_VALUE);

        VBox annotationLayout = new VBox();
        TextArea annotation = new TextArea();
        annotation.setMinHeight(100);
        annotation.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(annotation, Priority.ALWAYS);
        annotationLayout.getChildren().addAll(annotation);

        // listener to make annotation area invisible
        addAnnotation.selectedProperty().addListener((observable, oldValue, newValue) -> {
           if(newValue) {
               annotationLayout.setVisible(true);
               annotationLayout.setManaged(true);
           } else {
               annotationLayout.setVisible(false);
               annotationLayout.setManaged(false);
           }
        });

        // file save area
        HBox saveArea = new HBox();
        Label saveLocation = new Label("");
        saveLocation.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(saveLocation, Priority.ALWAYS);

        Button chooseFile = new Button("Choose Save Location");
        chooseFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image File", "*.png"));  // dsm is the only file type usable
            File fileName = fileChooser.showSaveDialog(window);
            if(fileName != null) {
                saveLocation.setText(fileName.getAbsolutePath());
            }
        });

        saveArea.getChildren().addAll(saveLocation, chooseFile);
        saveArea.setSpacing(10);
        saveArea.setPadding(new Insets(5));

        // save button
        VBox vSpacer = new VBox();
        vSpacer.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(vSpacer, Priority.ALWAYS);

        HBox saveButtonArea = new HBox();
        HBox spacer = new HBox();
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            if(saveLocation.getText() != ""){
                File file = new File(saveLocation.getText());
                BufferedImage img = SwingFXUtils.fromFXImage(preview.snapshot(new SnapshotParameters(), null), null);
                try {
                    ImageIO.write(img, "png", file);
                } catch (IOException ioException) {
                    System.out.println("Did not save image - caught IOException");
                    ioException.printStackTrace();
                }
            }
            window.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            window.close();
        });

        saveButtonArea.getChildren().addAll(spacer, cancelButton, saveButton);


        // parameter layout configuring
        parametersLayout.getChildren().addAll(addInfo, bigTitle, showConnectionNames, addAnnotation, annotationLayout, saveArea, vSpacer, saveButtonArea);
        addAnnotation.setSelected(false);  // set it here so annotation area is not shown
        annotationLayout.setVisible(false);
        annotationLayout.setManaged(false);


        Runnable updatePreview = () -> {
            preview.getChildren().removeAll(preview.getChildren());  // remove items
            if(bigTitle.isSelected()) {
                HBox centeredLabel = new HBox();
                Label l = new Label(matrix.getTitle());
                l.setStyle(l.getStyle() + "-fx-font-size: 24}; .combo-box > .list-cell {-fx-padding: 0 0 0 0; -fx-border-insets: 0 0 0 0;}");
                l.setAlignment(Pos.CENTER);

                centeredLabel.getChildren().add(l);
                centeredLabel.setAlignment(Pos.CENTER);

                preview.getChildren().add(centeredLabel);
            }

            if(addInfo.isSelected() && bigTitle.isSelected()) {
                Label l = new Label(
                    "Project Name: " + matrix.getProjectName() + "\n"
                    + "Customer: " + matrix.getCustomer() + "\n"
                    + "Version: " + matrix.getVersionNumber()
                );
                l.setAlignment(Pos.CENTER_LEFT);

                preview.getChildren().add(l);
            } else if(addInfo.isSelected() && !bigTitle.isSelected()) {
                Label l = new Label(
                    "Title: " + matrix.getTitle() + "\n"
                    + "Project Name: " + matrix.getProjectName() + "\n"
                    + "Customer: " + matrix.getCustomer() + "\n"
                    + "Version: " + matrix.getVersionNumber()
                );
                l.setAlignment(Pos.CENTER_LEFT);

                preview.getChildren().add(l);
            }

            MatrixGuiHandler m = new MatrixGuiHandler(matrix, 12);
            HBox centeredMatrix = new HBox();
            m.refreshMatrixEditorImmutable(!showConnectionNames.isSelected());
            centeredMatrix.getChildren().add(m.getMatrixEditor());
            centeredMatrix.setAlignment(Pos.CENTER);
            preview.getChildren().add(centeredMatrix);

            if(addAnnotation.isSelected()) {
                preview.getChildren().add(new Label(annotation.getText()));
            }
        };

        // set up listeners to update the preview
        addInfo.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updatePreview.run();
        });
        bigTitle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updatePreview.run();
        });
        showConnectionNames.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updatePreview.run();
        });
        addAnnotation.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updatePreview.run();
        });
        annotation.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePreview.run();
        });
        updatePreview.run();  // initial update to show the matrix


        ScrollPane previewArea = new ScrollPane(preview);
        previewArea.setFitToWidth(true);
        splitPane.getItems().addAll(previewArea, parametersLayout);
        splitPane.setDividerPositions(0.8);

        //Display window and wait for it to be closed before returning
        Scene scene = new Scene(splitPane, 1400, 1000);
        window.setScene(scene);
        window.showAndWait();
    }


    /**
     * Opens a file chooser window to choose a location to save a matrix to
     *
     * @param matrix the matrix to save
     * @param window the window associated with the file chooser
     */
    static public void promptSaveToFile(SymmetricDSM matrix, Window window) {
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
    static public void promptExportToCSV(SymmetricDSM matrix, Window window) {
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
    static public void promptExportToExcel(SymmetricDSM matrix, Window window) {
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
    static public void promptExportToThebeau(SymmetricDSM matrix, Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Matlab File", "*.m"));  // dsm is the only file type usable
        File fileName = fileChooser.showSaveDialog(window);
        if(fileName != null) {
            int code = ExportHandler.exportMatrixToThebeauMatlabFile(matrix, fileName);
        }
    }

}
