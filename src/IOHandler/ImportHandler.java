package IOHandler;

import DSMData.DSMItem;
import DSMData.DataHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ImportHandler {
    public static DataHandler importThebeauMatlabFile(File file) {
        DataHandler matrix = new DataHandler();
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
}
