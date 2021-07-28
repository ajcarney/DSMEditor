package gui;

import DSMData.DSMItem;
import DSMData.MatrixHandler;
import IOHandler.ExportHandler;
import javafx.application.Platform;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;


/**
 * Class to manage the tabs in the gui
 *
 * @author Aiden Carney
 */
public class TabView {
    private static TabPane tabPane;
    private static HashMap<DraggableTab, Integer> tabs;  // tab object, matrix uid

    private static MatrixHandler matrixHandler;
    private static InfoHandler infoHandler;

    private static final Double[] fontSizes = {
        5.0, 6.0, 8.0, 9.0, 9.5, 10.0, 10.5, 11.0, 12.0, 12.5, 14.0, 16.0, 18.0, 24.0, 30.0, 36.0, 60.0
    };
    private static final double DEFAULT_FONT_SIZE = 12.0;
    private static int currentFontSizeIndex;

    private Thread nameHandlerThread;


    /**
     * Creates a new TabView object where each pane is a different matrix. Needs an MatrixHandler instance to determine
     * which matrices to display and an InfoHandler instance to set the matrix in use. Creates and starts a daemon thread
     * that manages the saved/unsaved name of the matrices in the tabview. Matrices already in the MatrixHandler instance
     * will be added to the tab.
     *
     * @param matrixHandler   the MatrixHandler instance
     * @param infoHandler the InfoHandler instance
     */
    public TabView(MatrixHandler matrixHandler, InfoHandler infoHandler) {
        tabPane = new TabPane();
        tabs = new HashMap<>();
        this.matrixHandler = matrixHandler;
        this.infoHandler = infoHandler;

        for(int i=0; i<fontSizes.length; i++) {
            if(fontSizes[i] == DEFAULT_FONT_SIZE) {
                currentFontSizeIndex = i;
                break;
            }
        }


        // create current tabs
        Set<Integer> keys = this.matrixHandler.getMatrices().keySet();
        for(int uid : keys) {
            addTab(uid);
        };

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);  // any tab can be closed, but add event to be called on close

        this.nameHandlerThread = new Thread(() -> {
            while(true) {  // go through and update names
                for(HashMap.Entry<DraggableTab, Integer> entry : tabs.entrySet()) {
                    String title = matrixHandler.getMatrixSaveFile(entry.getValue()).getName();
                    if(!matrixHandler.isMatrixSaved(entry.getValue())) {
                        title += "*";
                    }
                    if(entry.getKey().getText() != title) {
                        String finalTitle = title;
                        Platform.runLater(new Runnable(){  // this allows a thread to update the gui
                            @Override
                            public void run() {
                                entry.getKey().setLabelText(finalTitle);
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


    /**
     * Creates and adds a matrix tab to the TabPane from a matrix in the MatrixHandler. This function
     * must be called when creating or adding a matrix to the MatrixHandler instance or else the matrix
     * will not be displayed in the TabPane
     *
     * @param matrixUid the uid of the matrix in the MatrixHandler instance
     */
    public void addTab(int matrixUid) {
        Vector<DSMItem> rows = this.matrixHandler.getMatrix(matrixUid).getRows();
        String label = "";
        for(DSMItem row : rows) {
            label += row.getName() + '\n';
        }

        String title = matrixHandler.getMatrixSaveFile(matrixUid).getName();
        if(!matrixHandler.isMatrixSaved(matrixUid)) {
            title += "*";
        }
        DraggableTab tab = new DraggableTab(title);
        tab.setContent(matrixHandler.getMatrixGuiHandler(matrixUid).getMatrixEditor());
        tab.setDetachable(false);
        tabPane.getScene().setOnKeyPressed(e -> {  // add keybinding to toggle cross-highlighting on the editor
            if (e.getCode() == KeyCode.F) {
                matrixHandler.getMatrixGuiHandler(matrixUid).toggleCrossHighlighting();
            }
        });

        tab.setOnCloseRequest(e -> {
            if(!matrixHandler.isMatrixSaved(matrixUid)) {
                focusTab(matrixHandler.getMatrixSaveFile(matrixUid));
                int selection = ExportHandler.promptSave(matrixHandler.getMatrixSaveFile(matrixUid).getAbsolutePath());
                // TODO: add alert box that opens asking if you want to save before closing the tab

                // 0 = close the tab, 1 = save and close, 2 = don't close
                if(selection == 2) {  // user doesn't want to close the pane so consume the event
                    if(e != null) {
                        e.consume();
                    }
                    return;
                } else if(selection == 1) {  // user wants to save before closing the pane
                    ExportHandler.saveMatrixToFile(matrixHandler.getMatrix(matrixUid), matrixHandler.getMatrixSaveFile(matrixUid));  // TODO: if there is an error saving, then display a message and don't close the file
                }
            }
            DraggableTab thisTab = null;
            for (HashMap.Entry<DraggableTab, Integer> m : tabs.entrySet()) {  // remove from HashMap by uid
                if(m.getValue() == matrixUid) {
                    thisTab = m.getKey();
                    break;
                }
            }
            tabs.remove(thisTab);
            tabPane.getTabs().remove(thisTab);
            matrixHandler.removeMatrix(matrixUid);
            infoHandler.setMatrix(null);

        });

        tab.setOnSelectionChanged(e -> {
            infoHandler.setMatrix(matrixHandler.getMatrix(matrixUid));
        });

        tabs.put(tab, matrixUid);
        this.tabPane.getTabs().add(tab);
    }


    /**
     * Finds the matrix the user is focused on by using a lookup table
     * TODO: this function and getFocusedTab() are implemented really stupidly and inefficiently
     *
     * @return the uid of the matrix that is focused
     */
    public Integer getFocusedMatrixUid() {
        try {
            return tabs.get(this.tabPane.getSelectionModel().getSelectedItem());
        } catch(Exception e) {
            return null;
        }
    }


    /**
     * Finds the tab that is currently focused on by the user
     *
     * @return the DraggableTab object that is selected
     */
    public DraggableTab getFocusedTab() {
        DraggableTab tab = null;
        for (HashMap.Entry<DraggableTab, Integer> m : tabs.entrySet()) {  // remove from HashMap by uid
            if(m.getValue().equals(getFocusedMatrixUid())) {
                tab = m.getKey();
                break;
            }
        }
        return tab;
    }


    /**
     * Focuses a tab by a matrices save file
     *
     * @param file the matrix with this file path will be focused
     */
    public void focusTab(File file) {
        DraggableTab tab = null;
        for (HashMap.Entry<DraggableTab, Integer> e : tabs.entrySet()) {
            if(matrixHandler.getMatrixSaveFile(e.getValue()).getAbsolutePath().equals(file.getAbsolutePath())) {
                tab = e.getKey();
                break;
            }
        }
        if(tab != null) {
            tabPane.getSelectionModel().select(tab);
        }
    }


    /**
     * Returns the TabPane object so it can be added to a scene
     *
     * @return the TabPane object with all its widgets
     */
    public static TabPane getTabPane() {
        return tabPane;
    }


    /**
     * Refreshes a tabs content by redrawing the content
     */
    public void refreshTab() {
        if(getFocusedMatrixUid() != null) {
            getFocusedTab().setContent(matrixHandler.getMatrixGuiHandler(getFocusedMatrixUid()).getMatrixEditor());
            infoHandler.setMatrix(matrixHandler.getMatrix(getFocusedMatrixUid()));
        }
    }


    /**
     * Returns that HashMap that contains the tab objects and matrix uids
     *
     * @return the tabs HashMap
     */
    public static HashMap<DraggableTab, Integer> getTabs() {
        return tabs;
    }


    /**
     * Closes a tab. It will be removed from the HashMaps as well because each tab has a closing policy that
     * does this
     *
     * @param tab the DraggableTab object
     */
    public void closeTab(DraggableTab tab) {
        tabPane.getTabs().remove(tab);  // TODO: this probably needs error handling
    }


    /**
     * Increases the font size of the current tab's matrix content. Updates the matrix content by refreshing the tab.
     */
    public void increaseFontScaling() {
        if(getFocusedMatrixUid() == null) return;
        currentFontSizeIndex += 1;
        if(currentFontSizeIndex > fontSizes.length - 1) currentFontSizeIndex = fontSizes.length - 1;

        matrixHandler.getMatrixGuiHandler(getFocusedMatrixUid()).setFontSize(fontSizes[currentFontSizeIndex]);
        refreshTab();
    }


    /**
     * Decreases the font size of the current tab's matrix content. Updates the matrix content by refreshing the tab.
     */
    public void decreaseFontScaling() {
        if(getFocusedMatrixUid() == null) return;
        currentFontSizeIndex -= 1;
        if(currentFontSizeIndex < 0) currentFontSizeIndex = 0;

        matrixHandler.getMatrixGuiHandler(getFocusedMatrixUid()).setFontSize(fontSizes[currentFontSizeIndex]);
        refreshTab();
    }


    /**
     * Sets the font size of the current tab's matrix content to the default. Updates the matrix content by refreshing the tab
     */
    public void resetFontScaling() {
        if(getFocusedMatrixUid() == null) return;
        for(int i=0; i<fontSizes.length; i++) {
            if(fontSizes[i] == DEFAULT_FONT_SIZE) {
                currentFontSizeIndex = i;
                break;
            }
        }

        matrixHandler.getMatrixGuiHandler(getFocusedMatrixUid()).setFontSize(DEFAULT_FONT_SIZE);
        refreshTab();
    }

}
