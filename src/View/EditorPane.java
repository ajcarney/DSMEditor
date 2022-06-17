package View;

import Data.TemplateDSM;
import IOHandler.TemplateIOHandler;
import View.HeaderMenu.DefaultHeaderMenu;
import View.HeaderMenu.TemplateHeaderMenu;
import View.MatrixHandlers.TemplateMatrixHandler;
import View.SideBarTools.TemplateSideBar;
import View.Widgets.DraggableTab;
import javafx.application.Platform;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Class to manage the gui for multiple matrices. Contains the file menu, side bar, metadata pane, and the editor
 *
 * @author Aiden Carney
 */
public class EditorPane {
    private BorderPane rootLayout;

    private TabPane tabPane;
    private HashMap<DraggableTab, Integer> tabs;  // tab object, matrix uid

    private static int currentMatrixUid = 0;
    private HashMap<Integer, TemplateDSM> matrices;
    private HashMap<Integer, TemplateIOHandler> matrixIOHandlers;
    private HashMap<Integer, TemplateMatrixHandler> matrixHandlers;
    private HashMap<Integer, TemplateHeaderMenu> headerMenus;
    private HashMap<Integer, TemplateSideBar> sideBars;

    private MatrixMetaDataPane matrixMetaDataPane;
    private ConnectionSearchWidget searchWidget;

    private static final double[] fontSizes = {
        5.0, 6.0, 8.0, 9.0, 9.5, 10.0, 10.5, 11.0, 12.0, 12.5, 14.0, 16.0, 18.0, 24.0, 30.0, 36.0, 60.0
    };
    private static final double DEFAULT_FONT_SIZE = 12.0;
    private static int currentFontSizeIndex;

    private Thread nameHandlerThread;


    /**
     * Creates a new EditorPane object where each pane is a different matrix. Creates and starts a daemon thread
     * that manages the saved/unsaved name of the matrices in the tabview.
     *
     */
    public EditorPane(BorderPane rootLayout) {
        this.tabPane = new TabPane();
        this.tabs = new HashMap<>();
        this.matrices = new HashMap<>();
        this.matrixIOHandlers = new HashMap<>();
        this.matrixHandlers = new HashMap<>();
        this.headerMenus = new HashMap<>();
        this.sideBars = new HashMap<>();
        this.matrixMetaDataPane = new MatrixMetaDataPane();
        this.searchWidget = new ConnectionSearchWidget(this);

        for(int i=0; i<fontSizes.length; i++) {
            if(fontSizes[i] == DEFAULT_FONT_SIZE) {
                currentFontSizeIndex = i;
                break;
            }
        }


        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);  // any tab can be closed, but add event to be called on close

        this.nameHandlerThread = new Thread(() -> {
            while(true) {  // go through and update names
                for(HashMap.Entry<DraggableTab, Integer> entry : tabs.entrySet()) {
                    String title = matrixIOHandlers.get(entry.getValue()).getSavePath().getName();
                    if(!isMatrixSaved(entry.getValue())) {
                        title += "*";
                    }
                    if(!entry.getKey().getLabelText().equals(title)) {
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

        this.rootLayout = rootLayout;
        this.rootLayout.setTop(new DefaultHeaderMenu(this, searchWidget).getMenuBar());
        this.rootLayout.setCenter(getTabPane());
        this.rootLayout.setBottom(searchWidget.getMainLayout());
    }


    /**
     * Creates and adds a matrix tab to the TabPane from a matrix object.
     *
     * @param  matrix  the matrix to add a tab for
     */
    public void addTab(TemplateDSM matrix, TemplateIOHandler ioHandler, TemplateMatrixHandler matrixHandler, TemplateHeaderMenu headerMenu, TemplateSideBar sideBar) {
        int matrixUid = currentMatrixUid;
        currentMatrixUid += 1;

        this.matrices.put(matrixUid, matrix);
        this.matrixIOHandlers.put(matrixUid, ioHandler);
        this.matrixHandlers.put(matrixUid, matrixHandler);
        this.headerMenus.put(matrixUid, headerMenu);
        this.sideBars.put(matrixUid, sideBar);

        // update the root layout
        this.rootLayout.setTop(headerMenu.getMenuBar());
        this.rootLayout.setLeft(sideBar.getLayout());

        String title = this.matrixIOHandlers.get(matrixUid).getSavePath().getName();
        if(!isMatrixSaved(matrixUid)) {
            title += "*";
        }
        DraggableTab tab = new DraggableTab(title);
        this.matrixHandlers.get(matrixUid).refreshMatrixEditor();
        tab.setContent(this.matrixHandlers.get(matrixUid).getMatrixEditor());
        tab.setDetachable(false);
        tabPane.getScene().setOnKeyPressed(e -> {  // add keybinding to toggle cross-highlighting on the editor
            if (e.getCode() == KeyCode.F) {
                this.matrixHandlers.get(matrixUid).toggleCrossHighlighting();
            }
        });

        tab.setOnCloseRequest(e -> {
            if(!isMatrixSaved(matrixUid)) {
                focusTab(this.matrixIOHandlers.get(matrixUid).getSavePath());
                int selection = this.matrixIOHandlers.get(matrixUid).promptSave();

                // 0 = close the tab, 1 = save and close, 2 = don't close
                if(selection == 2) {  // user doesn't want to close the pane so consume the event
                    if(e != null) {
                        e.consume();
                    }
                    return;
                } else if(selection == 1) {  // user wants to save before closing the pane
                    // TODO: if there is an error saving, then display a message and don't close the file
                    this.matrixIOHandlers.get(matrixUid).saveMatrixToFile(this.matrices.get(matrixUid), this.matrixIOHandlers.get(matrixUid).getSavePath());
                }
            }
            DraggableTab thisTab = null;
            for (HashMap.Entry<DraggableTab, Integer> m : tabs.entrySet()) {  // remove from HashMap by uid
                if(m.getValue() == matrixUid) {
                    thisTab = m.getKey();
                    break;
                }
            }
            this.tabs.remove(thisTab);
            this.tabPane.getTabs().remove(thisTab);
            this.matrices.remove(matrixUid);
            this.matrixIOHandlers.remove(matrixUid);
            this.matrixHandlers.remove(matrixUid);
            this.headerMenus.remove(matrixUid);
            this.sideBars.remove(matrixUid);
            this.matrixMetaDataPane.setMatrix(null);

            if(this.tabs.isEmpty()) {
                this.rootLayout.setTop(new DefaultHeaderMenu(this, searchWidget).getMenuBar());
                this.rootLayout.setLeft(null);
            }
        });

        tab.setOnSelectionChanged(e -> {
            matrixMetaDataPane.setMatrix(this.matrices.get(matrixUid));
        });

        tabs.put(tab, matrixUid);
        tabPane.getTabs().add(tab);
    }


    /**
     * Finds the uid of the matrix the user is focused on by using a lookup table
     * TODO: this function and getFocusedTab() are implemented really stupidly and inefficiently
     *
     * @return  the uid of the matrix that is focused
     */
    public Integer getFocusedMatrixUid() {
        try {
            return tabs.get(tabPane.getSelectionModel().getSelectedItem());
        } catch(Exception e) {
            return null;
        }
    }


    /**
     * Finds the matrix the user is focused on by using a lookup table
     *
     * @return  the uid of the matrix that is focused
     */
    public TemplateDSM getFocusedMatrix() {
        try {
            return matrices.get(getFocusedMatrixUid());
        } catch(Exception e) {
            return null;
        }
    }


    /**
     * @return  the hashmap for all the matrix io handlers
     */
    public HashMap<Integer, TemplateDSM> getMatrices() {
        return this.matrices;
    }


    /**
     * returns a matrix based on the matrix uid
     *
     * @param matrixUid  the uid of the matrix
     * @return           the matrix object associated with the uid
     */
    public TemplateDSM getMatrix(int matrixUid) {
        return this.matrices.get(matrixUid);
    }


    /**
     * returns a matrix handler based on the matrix uid
     *
     * @param matrixUid  the uid of the matrix
     * @return           the matrix handler object associated with the uid
     */
    public TemplateMatrixHandler getMatrixHandler(int matrixUid) {
        return this.matrixHandlers.get(matrixUid);
    }


    /**
     * @return  the hashmap for all the matrix handlers
     */
    public HashMap<Integer, TemplateMatrixHandler> getMatrixHandlers() {
        return this.matrixHandlers;
    }


    /**
     * @return  the hashmap for all the matrix io handlers
     */
    public HashMap<Integer, TemplateIOHandler> getMatrixIOHandlers() {
        return this.matrixIOHandlers;
    }



    /**
     * returns a matrix io handler based on the matrix uid
     *
     * @param matrixUid  the uid of the matrix
     * @return           the matrix io handler object associated with the uid
     */
    public TemplateIOHandler getMatrixIOHandler(int matrixUid) {
        return this.matrixIOHandlers.get(matrixUid);
    }


    /**
     * @return  a list of all the absolute save paths for the matrices stored by the editor
     */
    public ArrayList<String> getMatrixFileAbsoluteSavePaths() {
        ArrayList<String> saveNames = new ArrayList<>();
        for(TemplateIOHandler ioHandler : this.matrixIOHandlers.values()) {
            saveNames.add(ioHandler.getSavePath().getAbsolutePath());
        }
        return saveNames;
    }


    /**
     * Finds the tab that is currently focused on by the user
     *
     * @return  the DraggableTab object that is selected
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
     * @param  file the matrix with this file path will be focused
     */
    public void focusTab(File file) {
        DraggableTab tab = null;
        for (HashMap.Entry<DraggableTab, Integer> e : tabs.entrySet()) {
            if(this.matrixIOHandlers.get(e.getValue()).getSavePath().getAbsolutePath().equals(file.getAbsolutePath())) {
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
    public TabPane getTabPane() {
        return tabPane;
    }


    /**
     * Refreshes a tabs content by redrawing the content
     */
    public void refreshTab() {
        if(getFocusedMatrixUid() != null) {
            this.matrixHandlers.get(getFocusedMatrixUid()).refreshMatrixEditor();
            getFocusedTab().setContent(this.matrixHandlers.get(getFocusedMatrixUid()).getMatrixEditor());
            matrixMetaDataPane.setMatrix(this.matrices.get(getFocusedMatrixUid()));
        }
    }


    /**
     * Returns that HashMap that contains the tab objects and matrix uids
     *
     * @return the tabs HashMap
     */
    public HashMap<DraggableTab, Integer> getTabs() {
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

        this.matrixHandlers.get(getFocusedMatrixUid()).setFontSize(fontSizes[currentFontSizeIndex]);
        this.matrixHandlers.get(getFocusedMatrixUid()).refreshMatrixEditor();
        refreshTab();
    }


    /**
     * Decreases the font size of the current tab's matrix content. Updates the matrix content by refreshing the tab.
     */
    public void decreaseFontScaling() {
        if(getFocusedMatrixUid() == null) return;
        currentFontSizeIndex -= 1;
        if(currentFontSizeIndex < 0) currentFontSizeIndex = 0;

        this.matrixHandlers.get(getFocusedMatrixUid()).setFontSize(fontSizes[currentFontSizeIndex]);
        this.matrixHandlers.get(getFocusedMatrixUid()).refreshMatrixEditor();
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

        this.matrixHandlers.get(getFocusedMatrixUid()).setFontSize(DEFAULT_FONT_SIZE);
        this.matrixHandlers.get(getFocusedMatrixUid()).refreshMatrixEditor();
        refreshTab();
    }


    /**
     * Finds all the save paths of the matrices being handled
     *
     * @return  ArrayList of type string of the absolute paths of the save locations
     */
    public ArrayList<String> getMatrixFileSavePaths() {
        ArrayList<String> savePaths = new ArrayList<>();
        for(TemplateIOHandler ioHandler : this.matrixIOHandlers.values()) {
            savePaths.add(ioHandler.getSavePath().getAbsolutePath());
        }

        return savePaths;
    }


    /**
     * returns whether or not the wasModifiedFlag of a matrix is set or cleared. If
     * it is set then the matrix is not saved. If the flag is cleared, then the matrix
     * is saved.
     *
     * @param matrixUid the matrix to check whether or not has been saved
     * @return true if matrix is saved, false otherwise
     */
    public boolean isMatrixSaved(int matrixUid) {
        return !matrices.get(matrixUid).getWasModified();
    }


    /**
     * @return  the connection search widget of the matrix
     */
    public ConnectionSearchWidget getSearchWidget() {
        return this.searchWidget;
    }


}
