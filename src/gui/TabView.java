// TODO: maybe think about switching to these tabs so that the application is more "IDE-like": https://berry120.blogspot.com/2014/01/draggable-and-detachable-tabs-in-javafx.html
package gui;

import DSMData.DSMItem;
import DSMData.DataHandler;
import IOHandler.IOHandler;
import com.intellij.util.Matrix;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.util.Pair;

import java.io.File;
import java.util.*;

public class TabView {
    private static TabPane tabPane;
    private static HashMap<Tab, Integer> tabs;  // tab object, matrix uid
    private static HashMap<Tab, MatrixGuiHandler> editors;

    private static IOHandler ioHandler;
    private static InfoHandler infoHandler;

    private static final Double fontSizes[] = {
        5.0, 6.0, 8.0, 9.0, 9.5, 10.0, 10.5, 11.0, 12.0, 12.5, 14.0, 16.0, 18.0, 24.0, 30.0, 36.0, 60.0
    };
    private static final double DEFAULT_FONT_SIZE = 12;
    private static int currentFontSizeIndex;

    private Thread nameHandlerThread;

    public TabView(IOHandler ioHandler, InfoHandler infoHandler) {
        tabPane = new TabPane();
        tabs = new HashMap<>();
        editors = new HashMap<>();
        this.ioHandler = ioHandler;
        this.infoHandler = infoHandler;

        for(int i=0; i<fontSizes.length; i++) {
            if(fontSizes[i] == DEFAULT_FONT_SIZE) {
                currentFontSizeIndex = i;
                break;
            }
        }


        // create current tabs
        Set<Integer> keys = this.ioHandler.getMatrices().keySet();
        for(int uid : keys) {
            addTab(uid);
        };

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);  // any tab can be closed, but add event to be called on close

        this.nameHandlerThread = new Thread(() -> {
            while(true) {  // go through and update names
                for(HashMap.Entry<Tab, Integer> entry : tabs.entrySet()) {
                    String title = ioHandler.getMatrixSaveFile(entry.getValue()).getName();
                    if(!ioHandler.isMatrixSaved(entry.getValue())) {
                        title += "*";
                    }
                    if(entry.getKey().getText() != title) {
                        String finalTitle = title;
                        Platform.runLater(new Runnable(){  // this allows a thread to update the gui
                            @Override
                            public void run() {
                                entry.getKey().setText(finalTitle);
                            }
                        });
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        nameHandlerThread.setDaemon(true);
        nameHandlerThread.start();


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
        MatrixGuiHandler editor = new MatrixGuiHandler(ioHandler.getMatrix(matrixUid), DEFAULT_FONT_SIZE);
        Tab tab = new Tab(title, editor.getMatrixEditor());
        tabPane.getScene().setOnKeyPressed(e -> {  // add keybinding to toggle cross-highlighting on the editor
            if (e.getCode() == KeyCode.F) {
                editor.toggleCrossHighlighting();
            }
        });

        tab.setOnCloseRequest(e -> {
            if(!ioHandler.isMatrixSaved(matrixUid)) {
                focusTab(ioHandler.getMatrixSaveFile(matrixUid));
                int selection = ioHandler.promptSave(matrixUid);
                // TODO: add alert box that opens asking if you want to save before closing the tab

                // 0 = close the tab, 1 = save and close, 2 = don't close
                if(selection == 2) {  // user doesn't want to close the pane so consume the event
                    if(e != null) {
                        e.consume();
                    }
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
            editors.remove(thisTab);
            tabPane.getTabs().remove(thisTab);
            ioHandler.removeMatrix(matrixUid);
            infoHandler.setMatrix(null);

        });

        tab.setOnSelectionChanged(e -> {
            infoHandler.setMatrix(ioHandler.getMatrix(matrixUid));
        });

        tabs.put(tab, matrixUid);
        editors.put(tab, editor);
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
        if(getFocusedMatrixUid() != null) {
            getFocusedTab().setContent(editors.get(getFocusedTab()).getMatrixEditor());
        }
    }

    public static HashMap<Tab, Integer> getTabs() {
        return tabs;
    }

    public void closeTab(Tab tab) {
        tabPane.getTabs().remove(tab);
    }

    public void increaseFontScaling() {
        currentFontSizeIndex += 1;
        if(currentFontSizeIndex > fontSizes.length - 1) currentFontSizeIndex = fontSizes.length - 1;

        editors.get(getFocusedTab()).setFontSize(fontSizes[currentFontSizeIndex]);
        refreshTab();
    }

    public void decreaseFontScaling() {
        currentFontSizeIndex -= 1;
        if(currentFontSizeIndex < 0) currentFontSizeIndex = 0;

        editors.get(getFocusedTab()).setFontSize(fontSizes[currentFontSizeIndex]);
        refreshTab();
    }

    public void resetFontScaling() {
        for(int i=0; i<fontSizes.length; i++) {
            if(fontSizes[i] == DEFAULT_FONT_SIZE) {
                currentFontSizeIndex = i;
                break;
            }
        }

        editors.get(getFocusedTab()).setFontSize(DEFAULT_FONT_SIZE);
        refreshTab();
    }

}
