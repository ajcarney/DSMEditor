package gui;

import DSMData.DataHandler;
import IOHandler.IOHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.HashMap;
import java.util.Set;

public class TabView {
    private static TabPane tabPane;
    private static HashMap<Tab, Integer> tabs;

    private static IOHandler ioHandler;

    public TabView(IOHandler ioHandler) {
        tabPane = new TabPane();
        tabs = new HashMap<>();
        this.ioHandler = ioHandler;


        // create current tabs
        Set<Integer> keys = this.ioHandler.getMatrices().keySet();
        for(int uid : keys) {
            Tab tab = new Tab(this.ioHandler.getMatrixSaveFile(uid), new Label("grid layout of matrix"));
            // update closing policy to open a window asking for confirmation when closing a file
            tab.setOnCloseRequest(e -> {
                // remove the next line: you only want to consume the event for "No"
                // e.consume();
                if(!ioHandler.isMatrixSaved(uid)) {
                    // TODO: add alert box that opens asking if you want to save before closing the tab
                }
            });
            tabs.put(tab, uid);
            this.tabPane.getTabs().add(tab);
        };

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);  // any tab can be closed, but add event to be called on close


    }

    public void addTab(int matrixUid) {
        Tab tab = new Tab(this.ioHandler.getMatrixSaveFile(matrixUid), new Label("grid layout of matrix"));
        tabs.put(tab, matrixUid);
        this.tabPane.getTabs().add(tab);
    }

    public int getFocusedMatrixUid() {
        return tabs.get(this.tabPane.getSelectionModel().getSelectedItem());
    }

    public static TabPane getTabPane() {
        return tabPane;
    }
}
