package IOHandler;

import DSMData.DSMConnection;
import DSMData.DSMItem;
import DSMData.DataHandler;
import net.minidev.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.xml.crypto.Data;

public class IOHandler {

    private HashMap< Integer, DataHandler > matrices;
    private HashMap< Integer, File> matrixSaveNames;
    private static int currentMatrixUid = 0;

    public IOHandler() {
        matrices = new HashMap<>();
        matrixSaveNames = new HashMap<>();
    }

    public int addMatrix(DataHandler matrix, File fileSaveName) {
        this.currentMatrixUid += 1;

        this.matrices.put(this.currentMatrixUid, matrix);
        this.matrixSaveNames.put(this.currentMatrixUid, fileSaveName);

        return this.currentMatrixUid;
    }


    public int saveMatrixToFile(int matrixUid) {
        try {
            // create xml
            Element rootElement = new Element("dsm");
            Document doc = new Document(rootElement);

            Element infoElement = new Element("info");
            Element rowsElement = new Element("rows");
            Element colsElement = new Element("columns");
            Element connectionsElement = new Element("connections");

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
                colsElement.addContent(colElement);
            }

            // create row elements
            for(DSMItem row : matrices.get(matrixUid).getRows()) {
                Element rowElement = new Element("row");
                rowElement.setAttribute(new Attribute("uid", Integer.valueOf(row.getUid()).toString()));
                rowElement.addContent(new Element("name").setText(row.getName()));
                rowElement.addContent(new Element("sort_index").setText(Double.valueOf(row.getSortIndex()).toString()));
                if(matrices.get(matrixUid).isSymmetrical()) {
                    rowElement.addContent(new Element("associated_col").setText(Integer.valueOf(matrices.get(matrixUid).getColumnLookup().get(row.getUid())).toString()));
                }
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

            doc.getRootElement().addContent(infoElement);
            doc.getRootElement().addContent(colsElement);
            doc.getRootElement().addContent(rowsElement);
            doc.getRootElement().addContent(connectionsElement);

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());  // TODO: change this to getCompactFormat() for release
            xmlOutput.output(doc, new FileOutputStream(getMatrixSaveFile(matrixUid)));

            matrices.get(matrixUid).clearWasModifiedFlag();
            System.out.println("Saving file " + getMatrixSaveFile(matrixUid));

            return 1;  // file was successfully saved
        } catch(Exception e) {  // TODO: add better error handling and bring up an alert box
            System.out.println(e);
            return 0;  // 0 means there was an error somewhere
        }
    }

    public int saveMatrixToNewFile(int matrixUid, File fileName) {
        if(!fileName.equals("")) {  // TODO: add actual validation of path
            matrices.get(matrixUid).clearWasModifiedFlag();
            setMatrixSaveFile(matrixUid, fileName);  // update the location that the file will be saved to
            this.saveMatrixToFile(matrixUid);  // perform save like normal

            return 1;  // file was successfully saved
        }

        return 0;  // 0 means the filename was not present, so the data could not be saved

    }

    public HashMap<Integer, DataHandler> getMatrices() {
        return matrices;
    }

    public HashMap<Integer, File> getMatrixSaveNames() {
        return matrixSaveNames;
    }

    public DataHandler getMatrix(int uid) {
        return matrices.get(uid);
    }

    public File getMatrixSaveFile(int matrixUid) {
        return matrixSaveNames.get(matrixUid);
    }

    public void setMatrixSaveFile(int matrixUid, File newFile) {
        matrixSaveNames.put(matrixUid, newFile);
    }

    public boolean isMatrixSaved(int matrixUid) {
        return !matrices.get(matrixUid).getWasModified();
    }

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
            ArrayList<Integer> uids = new ArrayList<Integer>();

            // parse columns
            List<Element> cols = rootElement.getChild("columns").getChildren();
            for(Element col : cols) {
                int uid = Integer.parseInt(col.getAttribute("uid").getValue());
                uids.add(uid);
                String name = col.getChild("name").getText();
                double sortIndex = Double.parseDouble(col.getChild("sort_index").getText());

                DSMItem item = new DSMItem(uid, sortIndex, name);
                matrix.addItem(item, false);
            }

            // parse rows
            List<Element> rows = rootElement.getChild("rows").getChildren();
            for(Element row : rows) {
                int uid = Integer.parseInt(row.getAttribute("uid").getValue());
                uids.add(uid);
                String name = row.getChild("name").getText();
                double sortIndex = Double.parseDouble(row.getChild("sort_index").getText());

                DSMItem item = new DSMItem(uid, sortIndex, name);
                if(isSymmetrical) {
                    int colUid = Integer.parseInt(row.getChild("associated_col").getValue());
                    matrix.addSymmetricRowItem(item, colUid);  // this will create the link between row and column
                } else {
                    matrix.addItem(item, true);
                }
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

            Set<Integer> set = new HashSet<Integer>(uids);
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

    public void removeMatrix(int matrixUid) {
        matrices.remove(matrixUid);
        matrixSaveNames.remove(matrixUid);
    }

}
