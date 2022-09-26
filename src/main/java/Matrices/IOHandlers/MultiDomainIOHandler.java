package Matrices.IOHandlers;

import Constants.Constants;
import Matrices.Data.Entities.*;
import Matrices.Data.MultiDomainDSMData;
import Matrices.Views.AbstractMatrixView;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javatuples.Triplet;
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
 * A class with methods that handle exporting, saving, or importing a MultiDomainDSMData matrix object
 * Extended to support Thebeau matlab files (.m). Implements saving to CSV and Excel files
 *
 * @author Aiden Carney
 */
public class MultiDomainIOHandler extends AbstractIOHandler {

    private MultiDomainDSMData matrix;

    /**
     * Constructor
     *
     * @param file  the path to default to reading from and saving to
     */
    public MultiDomainIOHandler(File file) {
        super(file);
    }


    /**
     * Constructor
     *
     * @param file    the path to default to reading from and saving to
     * @param matrix  the matrix object to use with the IOHandler
     */
    public MultiDomainIOHandler(File file, MultiDomainDSMData matrix) {
        super(file);
        this.matrix = matrix;
    }


    /**
     * Sets the current matrix used by the IOHandler
     *
     * @param matrix  the new matrix object for the io handler
     */
    public void setMatrix(MultiDomainDSMData matrix) {
        this.matrix = matrix;
    }
    

    /**
     * Reads an xml file and parses it as an object that extends the template DSM. Returns the object,
     * but does not automatically add it to be handled.
     *
     * @return  the parsed in matrix
     */
    @Override
    public MultiDomainDSMData readFile() {
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
            if(!type.equals("multi-domain")) {
                System.out.println("File was not of correct DSM type when trying to read it");
                return null;
            }


            // parse domains and groupings

            HashMap<Integer, Grouping> matrixDomains = new HashMap<>();
            HashMap<Grouping, Collection<Grouping>> groupingConfiguration = new HashMap<>();

            // parse user defined groupings
            List<Element> domains = rootElement.getChild("domains").getChildren();
            for(Element domainXML : domains) {
                Grouping matrixDomain = new Grouping(domainXML);
                matrixDomains.put(matrixDomain.getUid(), matrixDomain);
                groupingConfiguration.put(matrixDomain, new ArrayList<>());

                List<Element> domainGroupings = domainXML.getChild("domainGroupings").getChildren();
                for(Element groupXML : domainGroupings) {
                    Grouping matrixGroup = new Grouping(groupXML);
                    groupingConfiguration.get(matrixDomain).add(matrixGroup);
                }
            }
            MultiDomainDSMData matrix = new MultiDomainDSMData(groupingConfiguration);  // create the matrix with the given domains
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
                Integer aliasUid = Integer.parseInt(col.getChild("alias").getText());

                Integer groupUid = Integer.parseInt(col.getChild("group1").getText());
                Integer domainUid = Integer.parseInt(col.getChild("group2").getText());
                Grouping domain = matrixDomains.get(domainUid);
                Grouping group = groupingConfiguration.get(domain).stream().filter(g -> g.getUid().equals(groupUid)).findFirst().orElse(null);

                DSMItem item = new DSMItem(uid, aliasUid, sortIndex, name, group, domain);
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

                Integer groupUid = Integer.parseInt(row.getChild("group1").getText());
                Integer domainUid = Integer.parseInt(row.getChild("group2").getText());
                Grouping domain = matrixDomains.get(domainUid);
                Grouping group = groupingConfiguration.get(domain).stream().filter(g -> g.getUid().equals(groupUid)).findFirst().orElse(null);

                DSMItem item = new DSMItem(uid, aliasUid, sortIndex, name, group, domain);
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
            Element groupingsElement = new Element("domains");
            Element interfacesElement = new Element("interfaces");

            // update metadata
            infoElement.addContent(new Element("title").setText(matrix.getTitle()));
            infoElement.addContent(new Element("project").setText(matrix.getProjectName()));
            infoElement.addContent(new Element("customer").setText(matrix.getCustomer()));
            infoElement.addContent(new Element("version").setText(matrix.getVersionNumber()));
            infoElement.addContent(new Element("type").setText("multi-domain"));
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

            // create domain and domain-grouping elements
            for(Grouping domain: matrix.getDomains()) {
                Element domainElement = domain.getXML(new Element("domain"));

                Element domainGroupingsElement = new Element("domainGroupings");
                for(Grouping domainGroup : matrix.getDomainGroupings(domain)) {
                    domainGroupingsElement.addContent(domainGroup.getXML(new Element("group")));
                }
                domainElement.addContent(domainGroupingsElement);

                groupingsElement.addContent(domainElement);
            }

            // create interface type elements
            for(Map.Entry<String, Vector<DSMInterfaceType>> interfaces : matrix.getInterfaceTypes().entrySet()) {
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
            doc.getRootElement().addContent(groupingsElement);
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
            int columns = template.get(0).size();

            for (ArrayList<Pair<RenderMode, Object>> pairs : template) {
                for (int c = 0; c < columns; c++) {
                    Pair<RenderMode, Object> item = pairs.get(c);

                    switch (item.getKey()) {
                        case PLAIN_TEXT, PLAIN_TEXT_V -> contents.append(item.getValue()).append(",");
                        case MULTI_SPAN_DOMAIN_TEXT -> contents.append(((Triplet<Grouping, Integer, Integer>) item.getValue()).getValue0().getName()).append(",");
                        case ITEM_NAME, ITEM_NAME_V -> contents.append(((DSMItem) item.getValue()).getName().getValue()).append(",");
                        case GROUPING_ITEM, GROUPING_ITEM_V -> contents.append(((DSMItem) item.getValue()).getGroup1().getName()).append(",");
                        case INDEX_ITEM -> contents.append(((DSMItem) item.getValue()).getSortIndex()).append(",");
                        case UNEDITABLE_CONNECTION, MULTI_SPAN_NULL -> contents.append(",");
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
                        case MULTI_SPAN_DOMAIN_TEXT -> {
                            Triplet<Grouping, Integer, Integer> data = (Triplet<Grouping, Integer, Integer>) item.getValue();
                            Cell cell = row.createCell(c + COL_START);
                            cell.setCellValue(data.getValue0().getName());

                            CellStyle cellStyle = workbook.createCellStyle();
                            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                            cell.setCellStyle(cellStyle);
                            Color bgColor = data.getValue0().getColor();
                            Color fontColor = data.getValue0().getFontColor();
                            styleExcelCell(workbook, cell, bgColor, fontColor, HORIZONTAL_ROTATION);

                            // subtract 1 to account for row span vs last row
                            sheet.addMergedRegion(new CellRangeAddress(r + ROW_START, r + ROW_START + data.getValue1() - 1, c + COL_START, c + COL_START + data.getValue2() - 1));
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
