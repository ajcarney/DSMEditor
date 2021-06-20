package DSMData;

import java.time.Instant;
import java.util.Vector;

public class DSMItem {
    private Integer uid;
    private Integer aliasUid;
    private String name;
    private Double sortIndex;
    private String group;

    public DSMItem(Double index, String name) {
        this.uid = name.hashCode() + Instant.now().toString().hashCode();
        try {  // wait a millisecond to ensure that the next uid will for sure be unique even with the same name
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.name = name;
        this.sortIndex = index;
        this.group = "(None)";
    }

    public DSMItem(Integer uid, Integer aliasUid, Double index, String name, String group) {
        this.uid = uid;
        this.aliasUid = aliasUid;
        this.name = name;
        this.sortIndex = index;
        this.group = group;
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

    public void setSortIndex(double index) {
        this.sortIndex = index;
    }

    public Integer getAliasUid() {
        return aliasUid;
    }

    public void setAliasUid(Integer aliasUid) {
        this.aliasUid = aliasUid;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return name;
    }
}

