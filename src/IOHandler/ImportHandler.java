package IOHandler;

import DSMData.DSMData;
import DSMData.DSMItem;
import javafx.scene.paint.Color;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Class with methods for reading and importing files to the DSMData type.
 * Currently supports DSM (.dsm) and Thebeau matlab file (.m)
 *
 * @author Aiden Carney
 */
public class ImportHandler {
    /**
     * Reads a matlab file in Thebeau's format and parses it as a DSMData object. Returns the DSMData object,
     * but does not automatically add it to be handled.
     *
     * @param file the file location to read from
     * @return     DSMData object of the parsed in matrix
     */
    public static DSMData importThebeauMatlabFile(File file) {
        DSMData matrix = new DSMData();
        matrix.setSymmetrical(true);  // all of thebeau's matrices are symmetrical

        ArrayList<String> lines = new ArrayList<>();
        Scanner s = null;
        try {
            s = new Scanner(file);
            while (s.hasNextLine()){
                lines.add(s.nextLine());
            }
            s.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        ArrayList<ArrayList<Double>> connections = new ArrayList<>();
        HashMap<Integer, DSMItem> rowItems = new HashMap<>();
        HashMap<Integer, DSMItem> colItems = new HashMap<>();
        int uid = 0;
        for(String line : lines) {  // parse the relevant data
            if(line.contains("DSM(")) {
                double xLoc = Integer.parseInt(line.split(Pattern.quote("DSM("))[1].split(Pattern.quote(","))[0]);
                double yLoc = Integer.parseInt(line.split(Pattern.quote(","))[1].split(Pattern.quote(")"))[0]);
                double weight = Double.parseDouble(line.split(Pattern.quote("= "))[1].split(Pattern.quote(";"))[0]);
                ArrayList<Double> data = new ArrayList<>();
                data.add(xLoc);
                data.add(yLoc);
                data.add(weight);
                connections.add(data);
            } else if(line.contains("DSMLABEL{")) {
                int loc = Integer.parseInt(line.split(Pattern.quote("DSMLABEL{"))[1].split(Pattern.quote(","))[0]);
                String name = line.split(Pattern.quote("'"))[1];
                DSMItem rowItem = new DSMItem(uid, null, (double)uid, name, "(none)");
                DSMItem colItem = new DSMItem(uid + 1, uid, (double)uid + 1, name, "(none)");
                uid += 2;  // add two because of column item

                matrix.addItem(rowItem, true);
                matrix.addItem(colItem, false);
                rowItems.put(loc, rowItem);
                colItems.put(loc, colItem);
            }
        }

        // create the connections
        for(ArrayList<Double> conn : connections) {
            int rowUid = rowItems.get(conn.get(0).intValue()).getUid();
            int colUid = colItems.get(conn.get(1).intValue()).getUid();
            matrix.modifyConnection(rowUid, colUid, "x", conn.get(2));
        }

        return matrix;
    }


    /**
     * Reads an xml file and parses it as a DSMData object. Returns the DSMData object,
     * but does not automatically add it to be handled.
     *
     * @param fileName the file location to read from
     * @return         DSMData object of the parsed in matrix
     */
    static public DSMData readFile(File fileName) {
        try {
            DSMData matrix = new DSMData();

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

}
