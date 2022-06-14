package IOHandler;

import Data.DSMConnection;
import Data.DSMItem;
import Data.Grouping;
import Data.SymmetricDSM;
import View.MatrixHandlers.SymmetricMatrixHandler;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
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

import java.io.*;
import java.util.*;


/**
 * A class with methods that handle exporting, saving, or importing a SymmetricDSM matrix object
 * Extended to support Thebeau matlab files (.m). Implements saving to CSV and Excel files
 *
 * @author Aiden Carney
 */
public class SymmetricIOHandler extends TemplateIOHandler<SymmetricDSM, SymmetricMatrixHandler> {

    /**
     * Constructor
     *
     * @param file  the path to default to reading from and saving to
     */
    SymmetricIOHandler(File file) {
        super(file);
    }


    /**
     * Reads an xml file and parses it as an object that extends the template DSM. Returns the object,
     * but does not automatically add it to be handled.
     *
     * @return  the parsed in matrix
     */
    @Override
    public SymmetricDSM readFile() {
        try {
            SymmetricDSM matrix = new SymmetricDSM();

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(savePath);  // read file into memory
            Element rootElement = document.getRootElement();

            Element info = rootElement.getChild("info");
            String title = info.getChild("title").getText();
            String project = info.getChild("project").getText();
            String customer = info.getChild("customer").getText();
            String version = info.getChild("version").getText();
            String type = info.getChild("type").getText();
            if(!type.equals("symmetric")) {
                System.out.println("File was not of correct DSM type when trying to read it");
                return null;
            }

            matrix.setTitle(title);
            matrix.setProjectName(project);
            matrix.setCustomer(customer);
            matrix.setVersionNumber(version);

            // parse default grouping
            Element defaultGroup = rootElement.getChild("default_grouping");
            String defaultGroupName = defaultGroup.getChild("name").getText();
            double defaultGroupR = Double.parseDouble(defaultGroup.getChild("r").getText());
            double defaultGroupG = Double.parseDouble(defaultGroup.getChild("g").getText());
            double defaultGroupB = Double.parseDouble(defaultGroup.getChild("b").getText());
            matrix.getDefaultGrouping().setName(defaultGroupName);
            matrix.getDefaultGrouping().setColor(Color.color(defaultGroupR, defaultGroupG, defaultGroupB));

            // parse groupings
            HashMap<Integer, Grouping> matrixGroupings = new HashMap<>();
            List<Element> groupings = rootElement.getChild("groupings").getChildren();
            for(Element conn : groupings) {
                Integer uid = Integer.parseInt(conn.getChild("uid").getText());
                String name = conn.getChild("name").getText();
                double r = Double.parseDouble(conn.getChild("r").getText());
                double g = Double.parseDouble(conn.getChild("g").getText());
                double b = Double.parseDouble(conn.getChild("b").getText());

                Grouping group = new Grouping(uid, name, Color.color(r, g, b));
                matrix.addGrouping(group);
                matrixGroupings.put(uid, group);
            }


            ArrayList<Integer> uids = new ArrayList<>();  // keep track of the uids when reading rows and columns to ensure no duplicates

            // parse columns
            List<Element> cols = rootElement.getChild("columns").getChildren();
            for(Element col : cols) {
                int uid = Integer.parseInt(col.getAttribute("uid").getValue());
                uids.add(uid);

                String name = col.getChild("name").getText();
                double sortIndex = Double.parseDouble(col.getChild("sort_index").getText());
                Integer aliasUid = Integer.parseInt(col.getChild("alias").getText());

                Integer groupUid = Integer.parseInt(col.getChild("group").getText());
                Grouping group = matrixGroupings.get(groupUid);

                DSMItem item = new DSMItem(uid, aliasUid, sortIndex, name, group, null);
                matrix.addItem(item, false);
            }


            // parse rows
            List<Element> rows = rootElement.getChild("rows").getChildren();
            for(Element row : rows) {
                int uid = Integer.parseInt(row.getAttribute("uid").getValue());
                uids.add(uid);

                String name = row.getChild("name").getText();
                double sortIndex = Double.parseDouble(row.getChild("sort_index").getText());
                Integer aliasUid = Integer.parseInt(row.getChild("alias").getText());

                Integer groupUid = Integer.parseInt(row.getChild("group").getText());
                Grouping group = matrixGroupings.get(groupUid);

                DSMItem item = new DSMItem(uid, aliasUid, sortIndex, name, group, null);
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


            Set<Integer> set = new HashSet<>(uids);
            if(set.size() != uids.size()) {  // uids were repeated and file is corrupt in some way
                // TODO: add alert box that says the file was corrupted in some way and could not be read in
                System.out.println("There were multiple occurrences of a uid (file is corrupted)");
                return null;
            } else {
                matrix.clearWasModifiedFlag();  // clear flag because no write operations were performed to the file
                matrix.clearStacks();  // make sure there are no changes when it is opened

                return matrix;
            }

        } catch(Exception e) {
            // TODO: add alert box that says the file was corrupted in some way and could not be read in
            System.out.println("Error reading file");
            System.out.println(e);
            e.printStackTrace();
            return null;
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
    @Override
    public int saveMatrixToFile(SymmetricDSM matrix, File file) {
        try {
            // create xml
            Element rootElement = new Element("dsm");
            Document doc = new Document(rootElement);

            Element infoElement = new Element("info");
            Element rowsElement = new Element("rows");
            Element colsElement = new Element("columns");
            Element connectionsElement = new Element("connections");
            Element defaultGroupingElement = new Element("default_grouping");
            Element groupingsElement = new Element("groupings");

            // update metadata
            infoElement.addContent(new Element("title").setText(matrix.getTitle()));
            infoElement.addContent(new Element("project").setText(matrix.getProjectName()));
            infoElement.addContent(new Element("customer").setText(matrix.getCustomer()));
            infoElement.addContent(new Element("version").setText(matrix.getVersionNumber()));
            infoElement.addContent(new Element("type").setText("symmetric"));

            // create column elements
            for(DSMItem col : matrix.getCols()) {
                Element colElement = new Element("col");
                colElement.setAttribute(new Attribute("uid", Integer.valueOf(col.getUid()).toString()));
                colElement.addContent(new Element("name").setText(col.getName()));
                colElement.addContent(new Element("sort_index").setText(Double.valueOf(col.getSortIndex()).toString()));
                if(col.getAliasUid() != null) {
                    colElement.addContent(new Element("alias").setText(col.getAliasUid().toString()));
                }
                colElement.addContent(new Element("group").setText(col.getGroup1().getUid().toString()));

                colsElement.addContent(colElement);
            }

            // create row elements
            for(DSMItem row : matrix.getRows()) {
                Element rowElement = new Element("row");
                rowElement.setAttribute(new Attribute("uid", Integer.valueOf(row.getUid()).toString()));
                rowElement.addContent(new Element("name").setText(row.getName()));
                rowElement.addContent(new Element("sort_index").setText(Double.valueOf(row.getSortIndex()).toString()));
                rowElement.addContent(new Element("group").setText(row.getGroup1().getUid().toString()));
                rowElement.addContent(new Element("alias").setText(row.getAliasUid().toString()));
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
            for(Grouping group: matrix.getGroupings()) {
                Element groupElement = new Element("group");
                groupElement.addContent(new Element("uid").setText(group.getUid().toString()));
                groupElement.addContent(new Element("name").setText(group.getName()));
                groupElement.addContent(new Element("r").setText(Double.valueOf(group.getColor().getRed()).toString()));
                groupElement.addContent(new Element("g").setText(Double.valueOf(group.getColor().getGreen()).toString()));
                groupElement.addContent(new Element("b").setText(Double.valueOf(group.getColor().getBlue()).toString()));

                groupingsElement.addContent(groupElement);
            }
            // don't write uid for default grouping element because it is always the same
            defaultGroupingElement.addContent(new Element("name").setText(matrix.getDefaultGrouping().getName()));
            defaultGroupingElement.addContent(new Element("r").setText(Double.valueOf(matrix.getDefaultGrouping().getColor().getRed()).toString()));
            defaultGroupingElement.addContent(new Element("g").setText(Double.valueOf(matrix.getDefaultGrouping().getColor().getRed()).toString()));
            defaultGroupingElement.addContent(new Element("b").setText(Double.valueOf(matrix.getDefaultGrouping().getColor().getRed()).toString()));

            doc.getRootElement().addContent(infoElement);
            doc.getRootElement().addContent(colsElement);
            doc.getRootElement().addContent(rowsElement);
            doc.getRootElement().addContent(connectionsElement);
            doc.getRootElement().addContent(defaultGroupingElement);
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
     * Saves a matrix to a csv file that includes the matrix metadata
     *
     * @param matrix    the matrix to export
     * @param file      the file to save the csv file to
     * @return          1 on success, 0 on error
     */
    @Override
    public int exportMatrixToCSV(SymmetricDSM matrix, File file) {
        try {
            StringBuilder contents = new StringBuilder("Title," + matrix.getTitle() + "\n");
            contents.append("Project Name,").append(matrix.getProjectName()).append("\n");
            contents.append("Customer,").append(matrix.getCustomer()).append("\n");
            contents.append("Version,").append(matrix.getVersionNumber()).append("\n");

            ArrayList<ArrayList<Pair<String, Object>>> template = matrix.getGridArray();
            int rows = template.size();
            int columns = template.get(0).size();

            for(int r=0; r<rows; r++) {
                for (int c = 0; c < columns; c++) {
                    Pair<String, Object> item = template.get(r).get(c);

                    if (item.getKey().equals("plain_text") || item.getKey().equals("plain_text_v")) {
                        contents.append(item.getValue()).append(",");
                    } else if (item.getKey().equals("item_name") || item.getKey().equals("item_name_v")) {
                        contents.append(((DSMItem) item.getValue()).getName()).append(",");
                    } else if (item.getKey().equals("grouping_item") || item.getKey().equals("grouping_item_v")) {
                        contents.append(((DSMItem) item.getValue()).getGroup1().getName()).append(",");
                    } else if (item.getKey().equals("index_item")) {
                        contents.append(((DSMItem) item.getValue()).getSortIndex()).append(",");
                    } else if (item.getKey().equals("uneditable_connection")) {
                        contents.append(",");
                    } else if (item.getKey().equals("editable_connection")) {
                        int rowUid = ((Pair<DSMItem, DSMItem>)item.getValue()).getKey().getUid();
                        int colUid = ((Pair<DSMItem, DSMItem>)item.getValue()).getValue().getUid();
                        if(matrix.getConnection(rowUid, colUid) != null) {
                            contents.append(matrix.getConnection(rowUid, colUid).getConnectionName());
                        }
                        contents.append(",");
                    }
                }
                contents.append("\n");
            }

            file = forceExtension(file, ".csv");
            System.out.println("Exporting to " + file.getAbsolutePath());
            FileWriter writer = new FileWriter(file);
            writer.write(contents.toString());
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
     * Cells are highlighted and auto sized. The matrix itself is shifted by ROW_START and COL_START (in function body)
     * so that the sizing for it is not impacted by the matrix metadata
     *
     * @param matrix    the matrix to export
     * @param file      A File object of the location of the .xlsx file
     * @return          1 on success, 0 on error
     */
    @Override
    public int exportMatrixToXLSX(SymmetricDSM matrix, File file) {
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

                        javafx.scene.paint.Color cellColor = ((DSMItem)item.getValue()).getGroup1().getColor();
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(HORIZONTAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if(item.getKey().equals("item_name_v")) {
                        Cell cell = row.createCell(c + COL_START);
                        System.out.println(item);
                        cell.setCellValue(((DSMItem)item.getValue()).getName());

                        javafx.scene.paint.Color cellColor = ((DSMItem)item.getValue()).getGroup1().getColor();
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setRotation(VERTICAL_ROTATION);
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("grouping_item")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(((DSMItem)item.getValue()).getGroup1().getName());

                        javafx.scene.paint.Color cellColor = ((DSMItem)item.getValue()).getGroup1().getColor();
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(HORIZONTAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("grouping_item_v")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(((DSMItem)item.getValue()).getGroup1().getName());

                        javafx.scene.paint.Color cellColor = ((DSMItem)item.getValue()).getGroup1().getColor();
                        XSSFCellStyle style = workbook.createCellStyle();
                        style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float) (cellColor.getRed()), (float) (cellColor.getGreen()), (float) (cellColor.getBlue())), new DefaultIndexedColorMap()));
                        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        style.setRotation(VERTICAL_ROTATION);
                        cell.setCellStyle(style);
                    } else if (item.getKey().equals("index_item")) {
                        Cell cell = row.createCell(c + COL_START);
                        cell.setCellValue(((DSMItem)item.getValue()).getSortIndex());

                        javafx.scene.paint.Color cellColor = ((DSMItem)item.getValue()).getGroup1().getColor();
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
                        javafx.scene.paint.Color rowColor = matrix.getItem(rowUid).getGroup1().getColor();
                        if (rowColor == null) rowColor = javafx.scene.paint.Color.color(1.0, 1.0, 1.0);

                        javafx.scene.paint.Color colColor = matrix.getItem(colUid).getGroup1().getColor();
                        if (colColor == null) colColor = Color.color(1.0, 1.0, 1.0);

                        double red = (rowColor.getRed() + colColor.getRed()) / 2;
                        double green = (rowColor.getGreen() + colColor.getGreen()) / 2;
                        double blue = (rowColor.getBlue() + colColor.getBlue()) / 2;

                        XSSFCellStyle style = workbook.createCellStyle();
                        if (!rowUid.equals(matrix.getItem(colUid).getAliasUid()) && matrix.getItem(rowUid).getGroup1().equals(matrix.getItem(colUid).getGroup1())) {  // associated row and column are same group
                            style.setFillForegroundColor(new XSSFColor(new java.awt.Color((float)(red), (float)(green), (float)(blue)), new DefaultIndexedColorMap()));
                            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        }
                        style.setRotation(HORIZONTAL_ROTATION);
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
                        + (int)row.getSortIndex()  // add one because matlab is 1 indexed
                        + ","
                        + (int)col.getSortIndex()  // add one because matlab is 1 indexed
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
            int code = exportMatrixToThebeauMatlabFile(matrix, fileName);
        }
    }

}
