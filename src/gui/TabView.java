// TODO: maybe think about switching to these tabs: https://berry120.blogspot.com/2014/01/draggable-and-detachable-tabs-in-javafx.html
package gui;

import DSMData.DSMItem;
import DSMData.DataHandler;
import IOHandler.IOHandler;
import com.intellij.util.Matrix;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

public class TabView {
    private static TabPane tabPane;
    private static HashMap<Tab, Integer> tabs;

    private static IOHandler ioHandler;
    private static InfoHandler infoHandler;

    public TabView(IOHandler ioHandler, InfoHandler infoHandler) {
        tabPane = new TabPane();
        tabs = new HashMap<>();
        this.ioHandler = ioHandler;
        this.infoHandler = infoHandler;


        // create current tabs
        Set<Integer> keys = this.ioHandler.getMatrices().keySet();
        for(int uid : keys) {
            String title = ioHandler.getMatrixSaveFile(uid).getName();
            if(!ioHandler.isMatrixSaved(uid)) {
                title += "*";
            }
            MatrixGuiHandler editor = new MatrixGuiHandler(ioHandler.getMatrix(uid));
            Tab tab = new Tab(title, editor.getMatrixEditor());
            // update closing policy to open a window asking for confirmation when closing a file
            tab.setOnCloseRequest(e -> {
                // remove the next line: you only want to consume the event for "No"
                // e.consume();
                tabs.remove(tab);
                System.out.println(uid);
                if(!ioHandler.isMatrixSaved(uid)) {
                    System.out.println("do you really want to close this");
                    // TODO: add alert box that opens asking if you want to save before closing the tab
                }
            });

            tab.setOnSelectionChanged(e -> {
                infoHandler.setMatrix(ioHandler.getMatrix(uid));
            });


            tabs.put(tab, uid);
            this.tabPane.getTabs().add(tab);
        };

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);  // any tab can be closed, but add event to be called on close


    }

    public void addTab(int matrixUid) {
        Vector<DSMItem> rows = this.ioHandler.getMatrix(matrixUid).getRows();
        String label = "";
        for(DSMItem row : rows) {
            label += row.getName() + '\n';
        }

        String title = ioHandler.getMatrixSaveFile(matrixUid).getName();
        if(!ioHandler.isMatrixSaved(matrixUid)) {
            title += "*";
        }
        MatrixGuiHandler editor = new MatrixGuiHandler(ioHandler.getMatrix(matrixUid));
        Tab tab = new Tab(title, editor.getMatrixEditor());

        tab.setOnCloseRequest(e -> {
            // remove the next line: you only want to consume the event for "No"
            // e.consume();
            if(!ioHandler.isMatrixSaved(matrixUid)) {
                // TODO: add alert box that opens asking if you want to save before closing the tab
                int selection = 1;  // 0 = close the tab, 1 = save and close, 2 = don't close
                if(selection == 2) {  // user doesn't want to close the pane so consume the event
                    e.consume();
                    return;
                } else if(selection == 1) {  // user wants to save before closing the pane
                    ioHandler.saveMatrixToFile(matrixUid);  // TODO: if there is an error saving, then display a message and don't close the file
                }
            }
            Tab thisTab = null;
            for (HashMap.Entry<Tab, Integer> m : tabs.entrySet()) {  // remove from HashMap by uid
                if(m.getValue() == matrixUid) {
                    thisTab = m.getKey();
                    break;
                }
            }
            tabs.remove(thisTab);
            ioHandler.removeMatrix(matrixUid);

        });

        tab.setOnSelectionChanged(e -> {
            infoHandler.setMatrix(ioHandler.getMatrix(matrixUid));
        });

        tabs.put(tab, matrixUid);
        this.tabPane.getTabs().add(tab);
    }

    public Integer getFocusedMatrixUid() {
        try {
            return tabs.get(this.tabPane.getSelectionModel().getSelectedItem());
        } catch(Exception e) {
            return null;
        }
    }

    public Tab getFocusedTab() {
        Tab tab = null;
        for (HashMap.Entry<Tab, Integer> m : tabs.entrySet()) {  // remove from HashMap by uid
            if(m.getValue().equals(getFocusedMatrixUid())) {
                tab = m.getKey();
                break;
            }
        }
        return tab;
    }

    public void refreshNames() {
        for(HashMap.Entry<Tab, Integer> entry : tabs.entrySet()) {
            String title = ioHandler.getMatrixSaveFile(entry.getValue()).getName();
            if(!ioHandler.isMatrixSaved(entry.getValue())) {
                title += "*";
            }
            entry.getKey().setText(title);
        }
    }

    public void focusTab(File file) {
        Tab tab = null;
        for (HashMap.Entry<Tab, Integer> e : tabs.entrySet()) {
            if(ioHandler.getMatrixSaveFile(e.getValue()).getAbsolutePath().equals(file.getAbsolutePath())) {
                tab = e.getKey();
                break;
            }
        }
        if(tab != null) {
            tabPane.getSelectionModel().select(tab);
        }
    }

    public static TabPane getTabPane() {
        return tabPane;
    }

    public void refreshTab() {
        MatrixGuiHandler editor = new MatrixGuiHandler(ioHandler.getMatrix(getFocusedMatrixUid()));
        getFocusedTab().setContent(editor.getMatrixEditor());
        refreshNames();
    }
}
