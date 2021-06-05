package DSMData;

import java.time.Instant;
import java.util.Vector;

public class DSMItem {
    private int uid;
    private String name;
    private double sortIndex;

    public DSMItem(double index, String name) {
        this.uid = name.hashCode() + Instant.now().toString().hashCode();
        try {  // wait a millisecond to ensure that the next uid will for sure be unique even with the same name
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.name = name;
        this.sortIndex = index;
    }

    public DSMItem(int uid, double index, String name) {
        this.uid = uid;
        this.name = name;
        this.sortIndex = index;
    }

    public int getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public double getSortIndex() {
        return sortIndex;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSortIndex(int index) {
        this.sortIndex = index;
    }


}

