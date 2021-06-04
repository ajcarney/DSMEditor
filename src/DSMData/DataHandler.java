package DSMData;

import java.util.Vector;

public class DataHandler {
    private Vector<DSMItem> rows;
    private Vector<DSMItem> cols;
    private boolean symmetrical;

    private boolean wasModified = true;

    public DataHandler() {
        this.wasModified = true;
    }

    public Vector<DSMItem> getRows() {
        Vector<DSMItem> copy_rows = (Vector<DSMItem>)rows.clone();
        return copy_rows;
    }

    public Vector<DSMItem> getCols() {
        Vector<DSMItem> copy_cols = (Vector<DSMItem>)cols.clone();
        return copy_cols;
    }

    public boolean isSymmetrical() {
        return symmetrical;
    }

    public void setSymmetrical(boolean isSymmetrical) {
        this.symmetrical = isSymmetrical;
    }




    public void addSymmetricItem(String name) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        double index = rows.size();
        DSMItem item = new DSMItem(index, name);
        this.rows.add(item);  // object is the same for row and column because matrix is symmetrical
        this.cols.add(item);

        this.wasModified = true;
    }

    public void addItem(String name, boolean is_row) {
        if(is_row) {
            double index = rows.size();
            DSMItem row = new DSMItem(index, name);
            this.rows.add(row);
        } else {
            double index = cols.size();
            DSMItem col = new DSMItem(index, name);
            this.cols.add(col);
        }

        this.wasModified = true;
    }




    public void deleteSymmetricItem(int uid) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        int r_index = -1;
        int c_index = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            if (rows.elementAt(i).getUid() == uid) r_index = i;
            if (cols.elementAt(i).getUid() == uid) c_index = i;

            if (r_index != -1 && c_index != -1) {
                rows.remove(r_index);
                cols.remove(c_index);
                break;
            }
        }
        assert (!(r_index == -1 || c_index == -1)) : "could not find same uid in row and column in symmetrical matrix when deleting item";
        this.wasModified = true;
    }

    public void deleteItem(int uid) {
        int index = -1;
        for(int i=0; i<this.rows.size(); i++) {     // check to see if uid is in the rows
            if(rows.elementAt(i).getUid() == uid) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            rows.remove(index);
        } else {                                   // uid was not in a row, must be in a column
            for(int i=0; i<this.cols.size(); i++) {
                if(cols.elementAt(i).getUid() == uid) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                cols.remove(index);
            }
        }
        this.wasModified = true;
    }



    public void setItemNameSymmetric(int uid, String new_name) {
        assert isSymmetrical() : "cannot call symmetrical function on non symmetrical dataset";

        int r_index = -1;
        int c_index = -1;
        for (int i = 0; i < this.rows.size(); i++) {
            if (rows.elementAt(i).getUid() == uid) r_index = i;
            if (cols.elementAt(i).getUid() == uid) c_index = i;

            if (r_index != -1 && c_index != -1) {
                rows.elementAt(r_index).setName(new_name);
                cols.elementAt(r_index).setName(new_name);
            }
        }
        assert (r_index != -1 && c_index != -1) : "could not find same uid in row and column in symmetrical matrix when changing item name";
        this.wasModified = true;
    }

    public void setItemName(int uid, String new_name) {
        int index = -1;
        for(int i=0; i<this.rows.size(); i++) {     // check to see if uid is in the rows
            if(rows.elementAt(i).getUid() == uid) {
                index = i;
                break;
            }
        }
        if (index != -1) {  // uid was not in a row, must be in a column
            rows.elementAt(index).setName(new_name);
        } else {
            for(int i=0; i<this.cols.size(); i++) {
                if(cols.elementAt(i).getUid() == uid) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                cols.elementAt(index).setName(new_name);
            }
        }
        this.wasModified = true;
    }

    public void modifyConnection(int row_uid, int col_uid, String connectionName, double weight) {
        int row_index = -1;
        for(int i=0; i<this.rows.size(); i++) {     // find uid in the row
            if(rows.elementAt(i).getUid() == row_uid) {
                row_index = i;
                break;
            }
        }

        int col_index = -1;
        for(int i=0; i<this.rows.size(); i++) {     // find uid in the column
            if(cols.elementAt(i).getUid() == col_uid) {
                col_index = i;
                break;
            }
        }
        assert (!(row_index == -1 || col_index == -1)) : "could not find same uid in row and column in matrix when adding a connection";

        rows.elementAt(row_index).modifyConnectionTo(col_uid, connectionName, weight);
        cols.elementAt(col_index).modifyConnectionTo(row_uid, connectionName, weight);

        this.wasModified = true;
    }

    public void clearWasModifiedFlag() {
        this.wasModified = false;
    }

    public boolean getWasModified() {
        return wasModified;
    }

}
