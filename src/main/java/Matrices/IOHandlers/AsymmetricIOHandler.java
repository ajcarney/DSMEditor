package Matrices.IOHandlers;

import Constants.Constants;
import Matrices.Data.AsymmetricDSMData;
import Matrices.Data.Entities.*;
import UI.MatrixViews.AbstractMatrixView;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
     * Reads a xml file and parses it as an object that extends the template DSM. Returns the object,
     * but does not automatically add it to be handled.
     *
     * @return  the parsed in matrix
     */
    @Override
    public AsymmetricDSMData readFile() {
        try {
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


            // parse groupings
            List<Element> rowGroupingsXML = rootElement.getChild("row_groupings").getChildren();
            ArrayList<Grouping> rowGroupings = new ArrayList<>();
            for(Element groupXML : rowGroupingsXML) {
                Grouping group = new Grouping(groupXML);
                rowGroupings.add(group);
            }

            List<Element> colGroupingsXML = rootElement.getChild("col_groupings").getChildren();
            ArrayList<Grouping> colGroupings = new ArrayList<>();
            for(Element groupXML : colGroupingsXML) {
                Grouping group = new Grouping(groupXML);
                colGroupings.add(group);
            }

            AsymmetricDSMData matrix = new AsymmetricDSMData(rowGroupings, colGroupings);
            matrix.setTitle(title);
            matrix.setProjectName(project);
            matrix.setCustomer(customer);
            matrix.setVersionNumber(version);

            // parse interfaces
            HashMap<Integer, DSMInterfaceType> interfaces = new HashMap<>();
            for(Element interfaceGroupingXML : rootElement.getChild("interfaces").getChildren()) {
                String interfaceGrouping = interfaceGroupingXML.getAttribute("name").getValue();
                matrix.addInterfaceTypeGrouping(interfaceGrouping);

                for(Element interfaceXML : interfaceGroupingXML.getChildren()) {
                    DSMInterfaceType interfaceType = new DSMInterfaceType(interfaceXML);
                    interfaces.put(interfaceType.getUid(), interfaceType);
                    matrix.addInterface(interfaceGrouping, interfaceType);
                }
            }


            ArrayList<Integer> uids = new ArrayList<>();  // keep track of the uids when reading rows and columns to ensure no duplicates

            // parse columns
            List<Element> cols = rootElement.getChild("columns").getChildren();
            for(Element col : cols) {
                int uid = Integer.parseInt(col.getAttribute("uid").getValue());
                uids.add(uid);

                String name = col.getChild("name").getText();
                double sortIndex = Double.parseDouble(col.getChild("sort_index").getText());

                Integer groupUid = Integer.parseInt(col.getChild("group1").getText());
                Grouping group = colGroupings.stream().filter(g -> g.getUid().equals(groupUid)).findFirst().orElse(null);

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

                Integer groupUid = Integer.parseInt(row.getChild("group1").getText());
                Grouping group = rowGroupings.stream().filter(g -> g.getUid().equals(groupUid)).findFirst().orElse(null);

                DSMItem item = new DSMItem(uid, null, sortIndex, name, group, null);
                matrix.addItem(item, true);
            }

            // parse connections
            List<Element> connections = rootElement.getChild("connections").getChildren();
            for(Element connXML : connections) {
                int rowUid = Integer.parseInt(connXML.getChild("row_uid").getText());
                int colUid = Integer.parseInt(connXML.getChild("col_uid").getText());
                String name = connXML.getChild("name").getText();
                double weight = Double.parseDouble(connXML.getChild("weight").getText());

                ArrayList<DSMInterfaceType> connectionInterfaces = new ArrayList<>();
                for(Element interfaceXML : connXML.getChild("interfaces").getChildren()) {
                    int interfaceUid = interfaceXML.getAttribute("uid").getIntValue();
                    connectionInterfaces.add(interfaces.get(interfaceUid));
                }

                matrix.modifyConnection(rowUid, colUid, name, weight, connectionInterfaces);
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
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Saves the matrix to an XML file specified by the caller of the function. Clears
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
            Element rowGroupingsElement = new Element("row_groupings");
            Element colGroupingsElement = new Element("col_groupings");
            Element interfacesElement = new Element("interfaces");

            // update metadata
            infoElement.addContent(new Element("title").setText(matrix.getTitle()));
            infoElement.addContent(new Element("project").setText(matrix.getProjectName()));
            infoElement.addContent(new Element("customer").setText(matrix.getCustomer()));
            infoElement.addContent(new Element("version").setText(matrix.getVersionNumber()));
            infoElement.addContent(new Element("type").setText("asymmetric"));
            infoElement.addContent(new Element("file_structure").setText(Constants.version));

            // create column elements
            for(DSMItem col : matrix.getCols()) {
                colsElement.addContent(col.getXML(new Element("col")));
            }

            // create row elements
            for(DSMItem row : matrix.getRows()) {
                rowsElement.addContent(row.getXML(new Element("row")));
            }

            // create connection elements
            for(DSMConnection connection : matrix.getConnections()) {
                connectionsElement.addContent(connection.getXML(new Element("connection")));
            }

            // create groupings elements
            for(Grouping group: ((AsymmetricDSMData) matrix).getGroupings(true)) {
                rowGroupingsElement.addContent(group.getXML(new Element("group")));
            }
            for(Grouping group: ((AsymmetricDSMData) matrix).getGroupings(false)) {
                colGroupingsElement.addContent(group.getXML(new Element("group")));
            }

            // create interface type elements
            for(Map.Entry<String, List<DSMInterfaceType>> interfaces : matrix.getInterfaceTypes().entrySet()) {
                Element interfacesGroupingElement = new Element("grouping");
                interfacesGroupingElement.setAttribute("name", interfaces.getKey());
                for(DSMInterfaceType i : interfaces.getValue()) {
                    interfacesGroupingElement.addContent(i.getXML(new Element("interface")));
                }
                interfacesElement.addContent(interfacesGroupingElement);
            }

            doc.getRootElement().addContent(infoElement);
            doc.getRootElement().addContent(colsElement);
            doc.getRootElement().addContent(rowsElement);
            doc.getRootElement().addContent(connectionsElement);
            doc.getRootElement().addContent(rowGroupingsElement);
            doc.getRootElement().addContent(colGroupingsElement);
            doc.getRootElement().addContent(interfacesElement);

            XMLOutputter xmlOutput = new XMLOutputter();
            if(Constants.isDebug) {
                xmlOutput.setFormat(Format.getPrettyFormat());
            } else {
                xmlOutput.setFormat(Format.getCompactFormat());
            }
            xmlOutput.output(doc, new FileOutputStream(file));

            System.out.println("Saving file " + file);
            matrix.clearWasModifiedFlag();

            return 1;  // file was successfully saved
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e.getMessage());
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }


    /**
     * Reads a csv file as an adjacency matrix and parses it into a SymmetricDSMData object and returns it. Does not
     * add it to be handled. Must be in following format
     * <row>, col1, col2, col3, ...
     * r1,      w1,   w2,   w3, ...
     * r2,      w4,   w5,   w6, ...
     * ...
     *
     * @param file  the file location to read from
     * @return      SymmetricDSMData object of the parsed in matrix
     */
    public AsymmetricDSMData importAdjacencyMatrix(File file) {
        AsymmetricDSMData matrix = new AsymmetricDSMData();

        // read the lines of the file
        List<List<String>> lines = readAdjacencyMatrix(file);

        ArrayList<String> itemsOrder = new ArrayList<>();
        HashMap<String, Integer> rowItems = new HashMap<>();
        HashMap<String, Integer> colItems = new HashMap<>();

        // parse the first line to create groups
        List<String> line = lines.get(1);
        HashMap<String, Grouping> colGroups = new HashMap<>();  // skip the first two columns
        List<String> inOrderColGroups = new ArrayList<>();
        int uid = 1;
        for (String groupName : line.subList(2, line.size())) {
            if (!colGroups.containsKey(groupName)) {  // only add the unique group names
                Grouping group;
                if (line.get(0).equals("(none)")) {
                    group = matrix.getDefaultGroup(false);
                } else {
                    group = new Grouping(uid, -1, line.get(0), Color.WHITE, Color.BLACK);
                    matrix.addGrouping(false, group);
                    uid += 1;
                }

                colGroups.put(groupName, group);
            }
            inOrderColGroups.add(groupName);

        }

        // parse the third line to create col items
        line = lines.get(2);
        for(int i = 2; i < line.size(); i++) {
            DSMItem col = new DSMItem(uid, null, i, line.get(i), colGroups.get(inOrderColGroups.get(i - 2)), null);
            colItems.put(line.get(i), uid);
            itemsOrder.add(line.get(i));

            uid += 1;
            matrix.addItem(col, false);
        }

        // parse all the rest of the rows to determine groups and connections
        HashMap<String, Grouping> rowGroups = new HashMap<>();  // skip the first two columns
        for(int i = 3; i < lines.size(); i++) {
            line = lines.get(i);

            String groupName = line.get(0);
            Grouping rowGroup;
            if (!rowGroups.containsKey(groupName)) {  // only add the unique group names
                Grouping group;
                if (groupName.equals("(none)")) {
                    group = matrix.getDefaultGroup(false);
                } else {
                    group = new Grouping(uid, -1, line.get(0), Color.WHITE, Color.BLACK);
                    matrix.addGrouping(true, group);
                    uid += 1;
                }
                rowGroups.put(groupName, group);
            }
            rowGroup = rowGroups.get(groupName);

            DSMItem row = new DSMItem(uid, null, i, line.get(1), rowGroup, null);
            matrix.addItem(row, true);

            for (int j = 2; j < line.size(); j++) {  // start at 2 to skip the first two columns
                double weight = Double.parseDouble(line.get(j));
                if (weight > 0.0) {
                    Integer colUid = colItems.get(itemsOrder.get(j - 2));  // subtract two for group and name columns
                    matrix.modifyConnection(uid, colUid, "x", weight, new ArrayList<>());
                }
            }

            uid += 1;
        }

        matrix.reDistributeSortIndices();
        matrix.clearWasModifiedFlag();  // clear flag because no write operations were performed to the file
        matrix.clearStacks();  // make sure there are no changes when it is opened

        return matrix;
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
                        case ITEM_NAME, ITEM_NAME_V -> contents.append(((DSMItem) item.getValue()).getName().getValue()).append(",");
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
            System.out.println(e.getMessage());
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }


    /**
     * Saves a matrix as an adjacency matrix in csv format:
     * asymmetric
     *        ,      , g1,   g2,   g3,   ...
     * <group>, <row>, col1, col2, col3, ...
     * g1,      r1,      w1,   w2,   w3, ...
     * g2,      r2,      w4,   w5,   w6, ...
     * ...
     *
     * @param file      the file to save the csv file to
     * @return          0 on success, 1 on error
     */
    @Override
    public int exportMatrixToAdjacencyMatrix(File file) {
        try {
            StringBuilder contents = new StringBuilder("asymmetric," + matrix.getTitle() + "\n");

            matrix.reDistributeSortIndices();

            // write the column groups
            contents.append(",");
            for (DSMItem col : matrix.getCols()) {
                contents.append(",").append(col.getGroup1().getName());
            }

            contents.append("\n<group>,<row>");
            for (DSMItem col : matrix.getCols()) {
                contents.append(",").append(col.getName().getValue());
            }

            for (DSMItem row : matrix.getRows()) {
                contents.append("\n").append(row.getGroup1().getName()).append(",").append(row.getName().getValue());
                for (DSMItem col : matrix.getCols()) {
                    DSMConnection connection = matrix.getConnection(row.getUid(), col.getUid());
                    if (connection != null) {
                        contents.append(",").append(connection.getWeight());
                    } else {
                        contents.append(",0");
                    }
                }
            }

            file = forceExtension(file, ".csv");
            System.out.println("Exporting to " + file.getAbsolutePath());
            FileWriter writer = new FileWriter(file);
            writer.write(contents.toString());
            writer.close();

            return 1;
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e.getMessage());
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
                            int rowUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getKey().getUid();
                            int colUid = ((Pair<DSMItem, DSMItem>) item.getValue()).getValue().getUid();

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
                                Color fontColor = Grouping.DEFAULT_FONT_COLOR;
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
            System.out.println(e.getMessage());
            e.printStackTrace();
            return 0;  // 0 means there was an error somewhere
        }
    }

}
