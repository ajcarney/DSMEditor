package IOHandler;

import DSMData.DSMConnection;
import DSMData.DSMItem;
import DSMData.DataHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class to manage DataHandler classes and read and write to different file formats
 * TODO: add validation of file paths when they are passed as parameters
 * @author: Aiden Carney
 */
public class IOHandler {

    private HashMap< Integer, DataHandler > matrices;
    private HashMap< Integer, File> matrixSaveNames;
    private static int currentMatrixUid = 0;

    public IOHandler() {
        matrices = new HashMap<>();
        matrixSaveNames = new HashMap<>();
    }


    /**
     * Adds a matrix to be handled and returns the unique id assigned to it
     *
     * @param matrix DataHandler object of the matrix to be added
     * @param file   File object of the location to save the matrix to
     * @return       the unique id given to the matrix so that it can be tracked
     */
    public int addMatrix(DataHandler matrix, File file) {
        currentMatrixUid += 1;

        this.matrices.put(currentMatrixUid, matrix);
        this.matrixSaveNames.put(currentMatrixUid, file);

        return currentMatrixUid;
    }


    /**
     * Saves the matrix to an xml file specified by the caller of the function. Does not clear
     * the matrix's wasModifiedFlag
     *
     * @param matrixUid the uid of the matrix to save
     * @param file      the file to save the matrix to
     * @return          1 on success, 0 on error
     */
    public int saveMatrixToFile(int matrixUid, File file) {
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
            infoElement.addContent(new Element("title").setText(matrices.get(matrixUid).getTitle()));
            infoElement.addContent(new Element("project").setText(matrices.get(matrixUid).getProjectName()));
            infoElement.addContent(new Element("customer").setText(matrices.get(matrixUid).getCustomer()));
            infoElement.addContent(new Element("version").setText(matrices.get(matrixUid).getVersionNumber()));
            if(matrices.get(matrixUid).isSymmetrical()) {
                infoElement.addContent(new Element("symmetric").setText("1"));
            } else {
                infoElement.addContent(new Element("symmetric").setText("0"));
            }

            // create column elements
            for(DSMItem col : matrices.get(matrixUid).getCols()) {
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
            for(DSMItem row : matrices.get(matrixUid).getRows()) {
                Element rowElement = new Element("row");
                rowElement.setAttribute(new Attribute("uid", Integer.valueOf(row.getUid()).toString()));
                rowElement.addContent(new Element("name").setText(row.getName()));
                rowElement.addContent(new Element("sort_index").setText(Double.valueOf(row.getSortIndex()).toString()));
                rowElement.addContent(new Element("group").setText(row.getGroup()));
                rowsElement.addContent(rowElement);
            }

            // create connection elements
            for(DSMConnection connection : matrices.get(matrixUid).getConnections()) {
                Element connElement = new Element("connection");
                connElement.addContent(new Element("row_uid").setText(Integer.valueOf(connection.getRowUid()).toString()));
                connElement.addContent(new Element("col_uid").setText(Integer.valueOf(connection.getColUid()).toString()));
                connElement.addContent(new Element("name").setText(connection.getConnectionName()));
                connElement.addContent(new Element("weight").setText(Double.valueOf(connection.getWeight()).toString()));
                connectionsElement.addContent(connElement);
            }

            // create groupings elements
            for(Map.Entry<String, Color> group: matrices.get(matrixUid).getGroupingColors().entrySet()) {
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

            return 1;  // file was successfully saved
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e);
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }


    /**
     * Saves matrix to the default specified location from the matrixSaveNames HashMap. Clears the
     * matrix's wasModifiedFlag
     *
     * @param matrixUid the uid of the matrix to save
     * @return          1 on success, 0 on error
     */
    public int saveMatrix(int matrixUid) {
        int code = saveMatrixToFile(matrixUid, getMatrixSaveFile(matrixUid));
        matrices.get(matrixUid).clearWasModifiedFlag();
        return code;
    }


    /**
     * Saves a matrix to a csv file that includes the matrix metadata
     *
     * @param matrixUid the unique id of the matrix to export
     * @param file      the file to save the csv file to  TODO: add validation that the file is in fact .csv
     * @return          1 on success, 0 on error
     */
    public int exportMatrixToCSV(int matrixUid, File file) {
        try {
            String contents = "Title," + getMatrix(matrixUid).getTitle() + "\n";
            contents += "Project Name," + getMatrix(matrixUid).getProjectName() + "\n";
            contents += "Customer," + getMatrix(matrixUid).getCustomer() + "\n";
            contents += "Version," + getMatrix(matrixUid).getVersionNumber() + "\n";

            ArrayList<ArrayList<Pair<String, Object>>> template = getMatrix(matrixUid).getGridArray();
            int rows = template.size();
            int columns = template.get(0).size();

            for(int r=0; r<rows; r++) {
                for (int c = 0; c < columns; c++) {
                    Pair<String, Object> item = template.get(r).get(c);

                    if (item.getKey().equals("plain_text") || item.getKey().equals("plain_text_v")) {
                        contents += item.getValue() + ",";
                    } else if (item.getKey().equals("item_name") || item.getKey().equals("item_name_v")) {
                        contents += getMatrix(matrixUid).getItem((Integer) item.getValue()).getName() + ",";
                    } else if (item.getKey().equals("grouping_item") || item.getKey().equals("grouping_item_v")) {
                        contents += getMatrix(matrixUid).getItem((Integer) item.getValue()).getGroup() + ",";
                    } else if (item.getKey().equals("index_item")) {
                        contents += getMatrix(matrixUid).getItem((Integer) item.getValue()).getSortIndex() + ",";
                    } else if (item.getKey().equals("uneditable_connection")) {
                        contents += ",";
                    } else if (item.getKey().equals("editable_connection")) {
                        int rowUid = ((Pair<Integer, Integer>)item.getValue()).getKey();
                        int colUid = ((Pair<Integer, Integer>)item.getValue()).getValue();
                        if(getMatrix(matrixUid).getConnection(rowUid, colUid) != null) {
                            contents += getMatrix(matrixUid).getConnection(rowUid, colUid).getConnectionName();
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
     * @param matrixUid the unique id of the matrix to export
     * @param file      A File object of the location of the .xlsx file  TODO: add validation that it is a .xlsx file
     * @return          1 on success, 0 on error
     */
    public int exportMatrixToXLSX(int matrixUid, File file) {
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
            row0.createCell(1).setCellValue(getMatrix(matrixUid).getProjectName());
            row1.createCell(1).setCellValue(getMatrix(matrixUid).getProjectName());
            row2.createCell(1).setCellValue(getMatrix(matrixUid).getCustomer());
            row3.createCell(1).setCellValue(getMatrix(matrixUid).getVersionNumber());

            // fill with content
            final int ROW_START = 6;  // start row and col so that matrix data is shifted
            final int COL_START = 3;

            ArrayList<ArrayList<Pair<String, Object>>> template = getMatrix(matrixUid).getGridArray();
            int rows = template.size();
            int columns = template.get(0).size();

            for(int r=0; r<rows; r++) {
                Row row = sheet.createRow(r + ROW_START);
                for (int c=0; c<columns; c++) {
                    Pair<String, Object> item = template.get(r).get(c);

                    if (item.getKey().equals("plain_text")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(item.getValue().toString());
                    } else if(item.getKey().equals("plain_text_v")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(item.getValue().toString());
                        CellStyle cellStyle = workbook.createCellStyle();
                        cellStyle.setAlignment(HorizontalAlignment.RIGHT);
                        cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
                        cellStyle.setRotation((short)90);
                        cell.setCellStyle(cellStyle);
                    } else if (item.getKey().equals("item_name")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(getMatrix(matrixUid).getItem((Integer) item.getValue()).getName());

                        Color cellColor = getMatrix(matrixUid).getGroupingColors().get(getMatrix(matrixUid).getItem((Integer)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                    } else if(item.getKey().equals("item_name_v")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(getMatrix(matrixUid).getItem((Integer) item.getValue()).getName());

                        Color cellColor = getMatrix(matrixUid).getGroupingColors().get(getMatrix(matrixUid).getItem((Integer)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setRotation((short)90);
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("grouping_item")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(getMatrix(matrixUid).getItem((Integer) item.getValue()).getGroup());

                        Color cellColor = getMatrix(matrixUid).getGroupingColors().get(getMatrix(matrixUid).getItem((Integer)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("grouping_item_v")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(getMatrix(matrixUid).getItem((Integer) item.getValue()).getGroup());

                        Color cellColor = getMatrix(matrixUid).getGroupingColors().get(getMatrix(matrixUid).getItem((Integer)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setRotation((short)90);
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("index_item")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(getMatrix(matrixUid).getItem((Integer) item.getValue()).getSortIndex());

                        Color cellColor = getMatrix(matrixUid).getGroupingColors().get(getMatrix(matrixUid).getItem((Integer)item.getValue()).getGroup());
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("uneditable_connection")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue("");

                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 0, 0), new DefaultIndexedColorMap()));  // TODO: set this to the color defined in MatrixGuiHandler
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("editable_connection")) {
                        Integer rowUid = ((Pair<Integer, Integer>)item.getValue()).getKey();
                        Integer colUid = ((Pair<Integer, Integer>)item.getValue()).getValue();

                        Cell cell = row.createCell(c + COL_START);
                        if(getMatrix(matrixUid).getConnection(rowUid, colUid) != null) {
                            cell.setCellValue(getMatrix(matrixUid).getConnection(rowUid, colUid).getConnectionName());
                        }

                        // highlight cell
                        Color rowColor = getMatrix(matrixUid).getGroupingColors().get(getMatrix(matrixUid).getItem(rowUid).getGroup());
                        if (rowColor == null) rowColor = Color.color(1.0, 1.0, 1.0);

                        Color colColor = getMatrix(matrixUid).getGroupingColors().get(getMatrix(matrixUid).getItem(colUid).getGroup());
                        if (colColor == null) colColor = Color.color(1.0, 1.0, 1.0);

                        double red = (rowColor.getRed() + colColor.getRed()) / 2;
                        double green = (rowColor.getGreen() + colColor.getGreen()) / 2;
                        double blue = (rowColor.getBlue() + colColor.getBlue()) / 2;

                        XSSFCellStyle style = workbook.createCellStyle();
                        if (getMatrix(matrixUid).isSymmetrical() && !rowUid.equals(getMatrix(matrixUid).getItem(colUid).getAliasUid()) && getMatrix(matrixUid).getItem(rowUid).getGroup().equals(getMatrix(matrixUid).getItem(colUid).getGroup())) {  // associated row and column are same group
                            style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float)(red), (float)(green), (float)(blue)), new DefaultIndexedColorMap()));
                            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        } else if (!getMatrix(matrixUid).isSymmetrical()) {
                            style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float)(red), (float)(green), (float)(blue)), new DefaultIndexedColorMap()));
                            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
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
     * Sets matrix save name to a new File object and then saves it. This is the new
     * default save location stored in the matrixSaveNames HashMap. Path cannot be empty.
     * Will clear the wasModifiedFlag of the matrix
     *
     * @param matrixUid The unique id of the matrix to be saved
     * @param fileName  The new file to save the matrix to
     * @return          1 on success, 0 on error
     */
    public int saveMatrixToNewFile(int matrixUid, File fileName) {
        if(!fileName.getPath().equals("")) {  // TODO: add actual validation of path
            matrices.get(matrixUid).clearWasModifiedFlag();
            setMatrixSaveFile(matrixUid, fileName);  // update the location that the file will be saved to
            this.saveMatrix(matrixUid);  // perform save like normal

            return 1;  // file was successfully saved
        }

        return 0;  // 0 means the filename was not present, so the data could not be saved

    }


    /**
     * Returns the matrices HashMap
     *
     * @return HashMap of matrix uids and DataHandler objects  TODO: This should probably be immutable in the future
     */
    public HashMap<Integer, DataHandler> getMatrices() {
        return matrices;
    }


    /**
     * Returns the matrixSaveNames HashMap
     *
     * @return HashMap of matrix uids and File objects  TODO: This should probably be immutable in the future
     */
    public HashMap<Integer, File> getMatrixSaveNames() {
        return matrixSaveNames;
    }


    /**
     * Returns a DataHandler object
     *
     * @param uid the uid of the matrix to return
     * @return    DataHandler object of the matrix
     */
    public DataHandler getMatrix(int uid) {
        return matrices.get(uid);
    }


    /**
     * Returns the default save path of a matrix
     *
     * @param matrixUid the save path for matrix with uid matrixUid
     * @return          File object of the default save path currently set
     */
    public File getMatrixSaveFile(int matrixUid) {
        return matrixSaveNames.get(matrixUid);
    }


    /**
     * Updates the default save location of a matrix
     *
     * @param matrixUid the matrix to update the save path of
     * @param newFile   File object of the new save path
     */
    public void setMatrixSaveFile(int matrixUid, File newFile) {
        matrixSaveNames.put(matrixUid, newFile);
    }


    /**
     * returns whether or not the wasModifiedFlag of a matrix is set or cleared. If
     * it is set then the matrix is not saved. If the flag is cleared, then the matrix
     * is saved.
     *
     * @param matrixUid the matrix to check whether or not has been saved
     * @return          true if matrix is saved, false otherwise
     */
    public boolean isMatrixSaved(int matrixUid) {
        return !matrices.get(matrixUid).getWasModified();
    }


    /**
     * Brings up a dialogue window that asks whether the user wants to save a file or not.
     * Presents the user with three options: save, don't save, and cancel. This function
     * should be called before removing a matrix
     * TODO: This should be merged with the remove matrix from IOHandler function
     *
     * @param matrixUid The matrix to prompt for
     * @return          0 = don't save, 1 = save, 2 = cancel
     */
    public Integer promptSave(int matrixUid) {
        AtomicReference<Integer> code = new AtomicReference<>(); // 0 = close the tab, 1 = save and close, 2 = don't close
        code.set(2);  // default value
        Stage window = new Stage();

        Label prompt = new Label("Would you like to save your changes to " + getMatrixSaveFile(matrixUid));

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
     * Reads an xml file and parses it as a DataHandler object. Returns the DataHandler object,
     * but does not automatically add it to be handled.
     *
     * @param fileName the file location to read from
     * @return         DataHandler object of the parsed in matrix
     */
    public DataHandler readFile(File fileName) {
        try {
            DataHandler matrix = new DataHandler();

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(fileName);  // read file into memory
            Element rootElement = document.getRootElement();

            Element info = rootElement.getChild("info");
            boolean isSymmetrical = Integer.parseInt(info.getChild("symmetric").getText()) != 0;
            String title = info.getChild("title").getText();
            String project = info.getChild("project").getText();
            String customer = info.getChild("customer").getText();
            String version = info.getChild("version").getText();

            matrix.setSymmetrical(isSymmetrical);
            matrix.setTitle(title);
            matrix.setProjectName(project);
            matrix.setCustomer(customer);
            matrix.setVersionNumber(version);

            // parse rows
            ArrayList<Integer> uids = new ArrayList<>();

            // parse columns
            List<Element> cols = rootElement.getChild("columns").getChildren();
            for(Element col : cols) {
                int uid = Integer.parseInt(col.getAttribute("uid").getValue());
                uids.add(uid);
                String name = col.getChild("name").getText();
                double sortIndex = Double.parseDouble(col.getChild("sort_index").getText());
                Integer aliasUid = null;
                String group = col.getChild("group").getText();
                try {
                    aliasUid = Integer.parseInt(col.getChild("alias").getText());
                } catch(NullPointerException npe) {}

                DSMItem item = new DSMItem(uid, aliasUid, sortIndex, name, group);
                matrix.addItem(item, false);
            }

            // parse rows
            List<Element> rows = rootElement.getChild("rows").getChildren();
            for(Element row : rows) {
                int uid = Integer.parseInt(row.getAttribute("uid").getValue());
                uids.add(uid);
                String name = row.getChild("name").getText();
                double sortIndex = Double.parseDouble(row.getChild("sort_index").getText());
                String group = row.getChild("group").getText();

                DSMItem item = new DSMItem(uid, null, sortIndex, name, group);
                matrix.addItem(item, true);
            }

            // parse connections
            List<Element> connections = rootElement.getChild("connections").getChildren();
            for(Element conn : connections) {
                int rowUid = Integer.parseInt(conn.getChild("row_uid").getText());
                int colUid = Integer.parseInt(conn.getChild("col_uid").getText());
                String name = conn.getChild("name").getText();
                double weight = Double.parseDouble(conn.getChild("weight").getText());

                matrix.modifyConnection(rowUid, colUid, name, weight);
            }

            // parse groupings
            List<Element> groupings = rootElement.getChild("groupings").getChildren();
            for(Element conn : groupings) {
                String name = conn.getChild("name").getText();
                double r = Double.parseDouble(conn.getChild("r").getText());
                double g = Double.parseDouble(conn.getChild("g").getText());
                double b = Double.parseDouble(conn.getChild("b").getText());

                matrix.addGrouping(name, Color.color(r, g, b));
            }

            Set<Integer> set = new HashSet<>(uids);
            if(set.size() != uids.size()) {  // uids were repeated and file is corrupt in some way
                // TODO: add alert box that says the file was corrupted in some way and could not be read in

                System.out.println("There were multiple occurrences of a uid (file is corrupted)");
                return null;
            } else {
                matrix.clearWasModifiedFlag();  // clear flag because no write operations were performed to the file
                return matrix;
            }

        } catch(Exception e) {
            // TODO: add alert box that says the file was corrupted in some way and could not be read in

            System.out.println(e);
            return null;
        }
    }


    /**
     * Removes a matrix to be handled
     *
     * @param matrixUid the uid of the matrix to be removed
     */
    public void removeMatrix(int matrixUid) {
        matrices.remove(matrixUid);
        matrixSaveNames.remove(matrixUid);
    }

}
