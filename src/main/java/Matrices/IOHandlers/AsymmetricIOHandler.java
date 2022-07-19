package Matrices.IOHandlers;

import Matrices.Data.AsymmetricDSMData;
import Matrices.Data.Entities.DSMConnection;
import Matrices.Data.Entities.DSMItem;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.Entities.RenderMode;
import Matrices.Views.AbstractMatrixView;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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


/**
 * A class with methods that handle exporting, saving, or importing a AsymmetricDSMData object
 * Extended to support Thebeau matlab files (.m). Implements saving to CSV and Excel files
 *
 * @author Aiden Carney
 */
public class AsymmetricIOHandler extends AbstractIOHandler {

    private AsymmetricDSMData matrix;

    /**
     * Constructor
     *
     * @param file  the path to default to reading from and saving to
     */
    public AsymmetricIOHandler(File file) {
        super(file);
    }


    /**
     * Constructor
     *
     * @param file    the path to default to reading from and saving to
     * @param matrix  the matrix object to use with the IOHandler
     */
    public AsymmetricIOHandler(File file, AsymmetricDSMData matrix) {
        super(file);
        this.matrix = matrix;
    }


    /**
     * Sets the current matrix used by the IOHandler
     *
     * @param matrix  the new matrix object for the io handler
     */
    public void setMatrix(AsymmetricDSMData matrix) {
        this.matrix = matrix;
    }


    /**
     * Reads an xml file and parses it as an object that extends the template DSM. Returns the object,
     * but does not automatically add it to be handled.
     *
     * @return  the parsed in matrix
     */
    @Override
    public AsymmetricDSMData readFile() {
        try {
            AsymmetricDSMData matrix = new AsymmetricDSMData();

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(savePath);  // read file into memory
            Element rootElement = document.getRootElement();

            Element info = rootElement.getChild("info");
            String title = info.getChild("title").getText();
            String project = info.getChild("project").getText();
            String customer = info.getChild("customer").getText();
            String version = info.getChild("version").getText();
            String type = info.getChild("type").getText();
            if(!type.equals("asymmetric")) {
                System.out.println("File was not of correct DSM type when trying to read it");
                return null;
            }

            matrix.setTitle(title);
            matrix.setProjectName(project);
            matrix.setCustomer(customer);
            matrix.setVersionNumber(version);

            // parse groupings
            HashMap<Integer, Grouping> matrixGroupings = new HashMap<>();

            // parse default grouping
            Element defaultGroup = rootElement.getChild("default_grouping");
            String defaultGroupName = defaultGroup.getChild("name").getText();
            double defaultGroupGroupColorR = Double.parseDouble(defaultGroup.getChild("gr").getText());
            double defaultGroupGroupColorG = Double.parseDouble(defaultGroup.getChild("gg").getText());
            double defaultGroupGroupColorB = Double.parseDouble(defaultGroup.getChild("gb").getText());
            double defaultGroupFontColorR = Double.parseDouble(defaultGroup.getChild("fr").getText());
            double defaultGroupFontColorG = Double.parseDouble(defaultGroup.getChild("fg").getText());
            double defaultGroupFontColorB = Double.parseDouble(defaultGroup.getChild("fb").getText());
            matrix.getDefaultGrouping().setName(defaultGroupName);
            matrix.getDefaultGrouping().setColor(Color.color(defaultGroupGroupColorR, defaultGroupGroupColorG, defaultGroupGroupColorB));
            matrix.getDefaultGrouping().setFontColor(Color.color(defaultGroupFontColorR, defaultGroupFontColorG, defaultGroupFontColorB));
            matrixGroupings.put(Integer.MAX_VALUE, matrix.getDefaultGrouping());

            // parse user defined groupings
            List<Element> groupings = rootElement.getChild("groupings").getChildren();
            for(Element conn : groupings) {
                Integer uid = Integer.parseInt(conn.getChild("uid").getText());
                String name = conn.getChild("name").getText();
                double group_r = Double.parseDouble(conn.getChild("gr").getText());
                double group_g = Double.parseDouble(conn.getChild("gg").getText());
                double group_b = Double.parseDouble(conn.getChild("gb").getText());
                double font_r = Double.parseDouble(conn.getChild("fr").getText());
                double font_g = Double.parseDouble(conn.getChild("fg").getText());
                double font_b = Double.parseDouble(conn.getChild("fb").getText());

                Grouping group = new Grouping(uid, name, Color.color(group_r, group_g, group_b), Color.color(font_r, font_g, font_b));
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

                Integer groupUid = Integer.parseInt(col.getChild("group").getText());
                Grouping group = matrixGroupings.get(groupUid);

                DSMItem item = new DSMItem(uid, null, sortIndex, name, group, null);
                matrix.addItem(item, false);
            }


            // parse rows
            List<Element> rows = rootElement.getChild("rows").getChildren();
            for(Element row : rows) {
                int uid = Integer.parseInt(row.getAttribute("uid").getValue());
                uids.add(uid);

                String name = row.getChild("name").getText();
                double sortIndex = Double.parseDouble(row.getChild("sort_index").getText());

                Integer groupUid = Integer.parseInt(row.getChild("group").getText());
                Grouping group = matrixGroupings.get(groupUid);

                DSMItem item = new DSMItem(uid, null, sortIndex, name, group, null);
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
     * @param file      the file to save the matrix to
     * @return          1 on success, 0 on error
     */
    @Override
    public int saveMatrixToFile(File file) {
        try {
            file = forceExtension(file, ".dsm");

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
            infoElement.addContent(new Element("type").setText("asymmetric"));

            // create column elements
            for(DSMItem col : matrix.getCols()) {
                Element colElement = new Element("col");
                colElement.setAttribute(new Attribute("uid", Integer.valueOf(col.getUid()).toString()));
                colElement.addContent(new Element("name").setText(col.getName().getValue()));
                colElement.addContent(new Element("sort_index").setText(Double.valueOf(col.getSortIndex()).toString()));
                colElement.addContent(new Element("group").setText(col.getGroup1().getUid().toString()));

                colsElement.addContent(colElement);
            }

            // create row elements
            for(DSMItem row : matrix.getRows()) {
                Element rowElement = new Element("row");
                rowElement.setAttribute(new Attribute("uid", Integer.valueOf(row.getUid()).toString()));
                rowElement.addContent(new Element("name").setText(row.getName().getValue()));
                rowElement.addContent(new Element("sort_index").setText(Double.valueOf(row.getSortIndex()).toString()));
                rowElement.addContent(new Element("group").setText(row.getGroup1().getUid().toString()));
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
                groupElement.addContent(new Element("gr").setText(Double.valueOf(group.getColor().getRed()).toString()));
                groupElement.addContent(new Element("gg").setText(Double.valueOf(group.getColor().getGreen()).toString()));
                groupElement.addContent(new Element("gb").setText(Double.valueOf(group.getColor().getBlue()).toString()));
                groupElement.addContent(new Element("fr").setText(Double.valueOf(group.getFontColor().getRed()).toString()));
                groupElement.addContent(new Element("fg").setText(Double.valueOf(group.getFontColor().getGreen()).toString()));
                groupElement.addContent(new Element("fb").setText(Double.valueOf(group.getFontColor().getBlue()).toString()));

                groupingsElement.addContent(groupElement);
            }
            // don't write uid for default grouping element because it is always the same
            defaultGroupingElement.addContent(new Element("name").setText(matrix.getDefaultGrouping().getName()));
            defaultGroupingElement.addContent(new Element("gr").setText(Double.valueOf(matrix.getDefaultGrouping().getColor().getRed()).toString()));
            defaultGroupingElement.addContent(new Element("gg").setText(Double.valueOf(matrix.getDefaultGrouping().getColor().getGreen()).toString()));
            defaultGroupingElement.addContent(new Element("gb").setText(Double.valueOf(matrix.getDefaultGrouping().getColor().getBlue()).toString()));
            defaultGroupingElement.addContent(new Element("fr").setText(Double.valueOf(matrix.getDefaultGrouping().getFontColor().getRed()).toString()));
            defaultGroupingElement.addContent(new Element("fg").setText(Double.valueOf(matrix.getDefaultGrouping().getFontColor().getGreen()).toString()));
            defaultGroupingElement.addContent(new Element("fb").setText(Double.valueOf(matrix.getDefaultGrouping().getFontColor().getBlue()).toString()));

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
     * @param file      the file to save the csv file to
     * @return          0 on success, 1 on error
     */
    @Override
    public int exportMatrixToCSV(File file) {
        try {
            StringBuilder contents = new StringBuilder("Title," + matrix.getTitle() + "\n");
            contents.append("Project Name,").append(matrix.getProjectName()).append("\n");
            contents.append("Customer,").append(matrix.getCustomer()).append("\n");
            contents.append("Version,").append(matrix.getVersionNumber()).append("\n");

            ArrayList<ArrayList<Pair<RenderMode, Object>>> template = matrix.getGridArray();
            int rows = template.size();
            int columns = template.get(0).size();

            for(int r=0; r<rows; r++) {
                for (int c = 0; c < columns; c++) {
                    Pair<RenderMode, Object> item = template.get(r).get(c);

                    switch(item.getKey()) {
                        case PLAIN_TEXT, PLAIN_TEXT_V -> contents.append(item.getValue()).append(",");
                        case ITEM_NAME, ITEM_NAME_V -> contents.append(((DSMItem) item.getValue()).getName()).append(",");
                        case GROUPING_ITEM, GROUPING_ITEM_V -> contents.append(((DSMItem) item.getValue()).getGroup1().getName()).append(",");
                        case INDEX_ITEM -> contents.append(((DSMItem) item.getValue()).getSortIndex()).append(",");
                        case UNEDITABLE_CONNECTION -> contents.append(",");
                        case EDITABLE_CONNECTION -> {
                            int rowUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getKey().getUid();
                            int colUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getValue().getUid();
                            if (matrix.getConnection(rowUid, colUid) != null) {
                                contents.append(matrix.getConnection(rowUid, colUid).getConnectionName());
                            }
                            contents.append(",");
                        }
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
     * @param file      A File object of the location of the .xlsx file
     * @return          0 on success, 1 on error
     */
    @Override
    public int exportMatrixToXLSX(File file) {
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

            ArrayList<ArrayList<Pair<RenderMode, Object>>> template = matrix.getGridArray();
            int rows = template.size();
            int columns = template.get(0).size();

            for(int r=0; r<rows; r++) {
                Row row = sheet.createRow(r + ROW_START);
                for (int c=0; c<columns; c++) {
                    Pair<RenderMode, Object> item = template.get(r).get(c);

                    switch (item.getKey()) {
                        case PLAIN_TEXT -> {
                            Cell cell = row.createCell(c + COL_START);
                            cell.setCellValue(item.getValue().toString());

                            styleExcelCell(workbook, cell, null, null, HORIZONTAL_ROTATION);
                        }
                        case PLAIN_TEXT_V -> {
                            Cell cell = row.createCell(c + COL_START);
                            cell.setCellValue(item.getValue().toString());

                            CellStyle cellStyle = workbook.createCellStyle();
                            cellStyle.setAlignment(HorizontalAlignment.RIGHT);
                            cellStyle.setVerticalAlignment(VerticalAlignment.BOTTOM);
                            cellStyle.setRotation(VERTICAL_ROTATION);
                            cell.setCellStyle(cellStyle);
                        }
                        case ITEM_NAME -> {
                            Cell cell = row.createCell(c + COL_START);
                            cell.setCellValue(((DSMItem) item.getValue()).getName().getValue());

                            Color bgColor = ((DSMItem) item.getValue()).getGroup1().getColor();
                            Color fontColor = ((DSMItem) item.getValue()).getGroup1().getFontColor();
                            styleExcelCell(workbook, cell, bgColor, fontColor, HORIZONTAL_ROTATION);
                        }
                        case ITEM_NAME_V -> {
                            Cell cell = row.createCell(c + COL_START);
                            cell.setCellValue(((DSMItem) item.getValue()).getName().getValue());

                            Color bgColor = ((DSMItem) item.getValue()).getGroup1().getColor();
                            Color fontColor = ((DSMItem) item.getValue()).getGroup1().getFontColor();
                            styleExcelCell(workbook, cell, bgColor, fontColor, VERTICAL_ROTATION);
                        }
                        case GROUPING_ITEM -> {
                            Cell cell = row.createCell(c + COL_START);
                            cell.setCellValue(((DSMItem) item.getValue()).getGroup1().getName());

                            Color bgColor = ((DSMItem) item.getValue()).getGroup1().getColor();
                            Color fontColor = ((DSMItem) item.getValue()).getGroup1().getFontColor();
                            styleExcelCell(workbook, cell, bgColor, fontColor, HORIZONTAL_ROTATION);
                        }
                        case GROUPING_ITEM_V -> {
                            Cell cell = row.createCell(c + COL_START);
                            cell.setCellValue(((DSMItem) item.getValue()).getGroup1().getName());

                            Color bgColor = ((DSMItem) item.getValue()).getGroup1().getColor();
                            Color fontColor = ((DSMItem) item.getValue()).getGroup1().getFontColor();
                            styleExcelCell(workbook, cell, bgColor, fontColor, VERTICAL_ROTATION);
                        }
                        case INDEX_ITEM -> {
                            Cell cell = row.createCell(c + COL_START);
                            cell.setCellValue(((DSMItem) item.getValue()).getSortIndex());

                            Color bgColor = ((DSMItem) item.getValue()).getGroup1().getColor();
                            Color fontColor = ((DSMItem) item.getValue()).getGroup1().getFontColor();
                            styleExcelCell(workbook, cell, bgColor, fontColor, HORIZONTAL_ROTATION);
                        }
                        case UNEDITABLE_CONNECTION -> {
                            Cell cell = row.createCell(c + COL_START);
                            cell.setCellValue("");

                            Color bgColor = (Color) AbstractMatrixView.UNEDITABLE_CONNECTION_BACKGROUND.getFills().get(0).getFill();
                            styleExcelCell(workbook, cell, bgColor, null, HORIZONTAL_ROTATION);
                        }
                        case EDITABLE_CONNECTION -> {
                            Integer rowUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getKey().getUid();
                            Integer colUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getValue().getUid();

                            Cell cell = row.createCell(c + COL_START);
                            if (matrix.getConnection(rowUid, colUid) != null) {
                                cell.setCellValue(matrix.getConnection(rowUid, colUid).getConnectionName());
                            }

                            if (matrix.getItem(rowUid).getGroup1().equals(matrix.getItem(colUid).getGroup1())) {  // groups are the same
                                Color bgColor = matrix.getItem(rowUid).getGroup1().getColor();
                                Color fontColor = matrix.getItem(rowUid).getGroup1().getFontColor();
                                styleExcelCell(workbook, cell, bgColor, fontColor, HORIZONTAL_ROTATION);
                            } else {
                                Color bgColor = (Color) AbstractMatrixView.DEFAULT_BACKGROUND.getFills().get(0).getFill();
                                Color fontColor = Grouping.defaultFontColor;
                                styleExcelCell(workbook, cell, bgColor, fontColor, HORIZONTAL_ROTATION);
                            }
                        }
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

}
