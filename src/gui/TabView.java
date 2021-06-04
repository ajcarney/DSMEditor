package gui;

import DSMData.DataHandler;
import IOHandler.IOHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.HashMap;
import java.util.Set;

public class TabView {
//    private class fxTab {
//        private int uid;
//        private Tab tab;
//
//        fxTab(int uid, Tab tab) {
//            this.tab = tab;
//            this.uid = uid;
//        }
//    }

    private static TabPane tabPane;
    private static HashMap<Integer, Tab> tabs;

    private static IOHandler ioHandler;

    TabView(IOHandler ioHandler) {
        this.ioHandler = ioHandler;

        // create current tabs
        Set<Integer> keys = this.ioHandler.getMatrices().keySet();
        for(int uid : keys) {
            Tab tab = new Tab(this.ioHandler.getMatrixSaveFile(uid), new Label("grid layout of matrix"));
            tabs.put(uid, tab);
            this.tabPane.getTabs().add(tab);
        };

    }

    public void addTab(int matrixUid) {
        Tab tab = new Tab(this.ioHandler.getMatrixSaveFile(matrixUid), new Label("grid layout of matrix"));
        tabs.put(matrixUid, tab);
        this.tabPane.getTabs().add(tab);
    }

    public int getFocusedMatrixUid() {
        return 0;  // TODO:
    }

}
