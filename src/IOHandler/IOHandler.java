package IOHandler;

import DSMData.DSMItem;
import DSMData.DataHandler;
import net.minidev.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

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
        if(!this.matrixSaveNames.get(matrixUid).equals("")) {  // TODO: add actual file IO
            matrices.get(matrixUid).clearWasModifiedFlag();

            System.out.println("Saving file " + getMatrixSaveFile(matrixUid));

            return 1;  // file was successfully saved
        }

        return 0;  // 0 means the filename was not present, so the data could not be saved

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

            // TODO: add checking to make sure that there are no uid exists multiple times
            // parse rows
            List<Element> rows = rootElement.getChild("rows").getChildren();
            for(Element row : rows) {
                int uid = Integer.parseInt(row.getAttribute("uid").getValue());
                String name = row.getChild("name").getText();
                double sortIndex = Double.parseDouble(row.getChild("sort_index").getText());

                DSMItem item = new DSMItem(uid, sortIndex, name);
                matrix.addItem(item, true);
            }

            // parse columns
            List<Element> cols = rootElement.getChild("columns").getChildren();
            for(Element col : cols) {
                int uid = Integer.parseInt(col.getAttribute("uid").getValue());
                String name = col.getChild("name").getText();
                double sortIndex = Double.parseDouble(col.getChild("sort_index").getText());

                DSMItem item = new DSMItem(uid, sortIndex, name);
                matrix.addItem(item, false);
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

            matrix.clearWasModifiedFlag();  // clear flag because no write operations were performed to the file
            return matrix;

        } catch(Exception e) {
            // TODO: add alert box that says the file was corrupted in some way and could not be read in

            System.out.println(e);
            return null;
        }
    }

}
