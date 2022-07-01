package View;

import Data.AsymmetricDSM;
import Data.MatrixController;
import Data.SymmetricDSM;
import Data.TemplateDSM;
import IOHandler.TemplateIOHandler;
import View.HeaderMenu.AsymmetricHeaderMenu;
import View.HeaderMenu.DefaultHeaderMenu;
import View.HeaderMenu.SymmetricHeaderMenu;
import View.HeaderMenu.TemplateHeaderMenu;
import View.MatrixViews.TemplateMatrixView;
import View.SideBarTools.AsymmetricSideBar;
import View.SideBarTools.SymmetricSideBar;
import View.SideBarTools.TemplateSideBar;
import View.Widgets.DraggableTab;
import javafx.application.Platform;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.util.HashMap;


/**
 * Class to manage the gui for multiple matrices. Contains the file menu, side bar, metadata pane, and the editor
 *
 * @author Aiden Carney
 */
public class EditorPane {
    private final BorderPane rootLayout;

    private final TabPane tabPane = new TabPane();
    private final HashMap<DraggableTab, Integer> tabs = new HashMap<>();  // tab object, matrix uid

    private static int currentMatrixUid = 0;
    private final MatrixController matrices;

    private final MatrixMetaDataPane matrixMetaDataPane;

    private static final double[] fontSizes = {
        5.0, 6.0, 8.0, 9.0, 9.5, 10.0, 10.5, 11.0, 12.0, 12.5, 14.0, 16.0, 18.0, 24.0, 30.0, 36.0, 60.0
    };
    private static final double DEFAULT_FONT_SIZE = 12.0;
    private static int currentFontSizeIndex;


    /**
     * Creates a new EditorPane object where each pane is a different matrix. Creates and starts a daemon thread
     * that manages the saved/unsaved name of the matrices in the tabview.
     *
     */
    public EditorPane(MatrixController matrices, BorderPane rootLayout) {
        this.matrices = matrices;
        this.matrixMetaDataPane = new MatrixMetaDataPane();

        for(int i=0; i<fontSizes.length; i++) {
            if(fontSizes[i] == DEFAULT_FONT_SIZE) {
                currentFontSizeIndex = i;
                break;
            }
        }


        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);  // any tab can be closed, but add event to be called on close

        // TODO: this should be set up as a binding
        // go through and update names
        // this allows a thread to update the gui
        Thread nameHandlerThread = new Thread(() -> {  // TODO: this should be set up as a binding
            while (true) {  // go through and update names
                for (HashMap.Entry<DraggableTab, Integer> entry : tabs.entrySet()) {
                    String title = matrices.getMatrixIOHandler(entry.getValue()).getSavePath().getName();
                    if (!matrices.isMatrixSaved(entry.getValue())) {
                        title += "*";
                    }
                    if (!entry.getKey().getLabelText().equals(title)) {
                        String finalTitle = title;
                        // this allows a thread to update the gui
                        Platform.runLater(() -> entry.getKey().setLabelText(finalTitle));
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
        DefaultHeaderMenu menu = new DefaultHeaderMenu(this);
        this.rootLayout.setTop(menu.getMenuBar());
        this.rootLayout.setBottom(menu.getConnectionSearchLayout());
        this.rootLayout.setCenter(getTabPane());
        this.rootLayout.setRight(matrixMetaDataPane.getLayout());
    }


    /**
     * Finds the uid of the matrix the user is focused on by using a lookup table
     * TODO: this function and getFocusedTab() are implemented really stupidly and inefficiently
     *
     * @return  the uid of the matrix that is focused
     */
    public Integer getFocusedMatrixUid() {
        try {
            return tabs.get((DraggableTab)tabPane.getSelectionModel().getSelectedItem());  // explicit cast to fail sooner if type issue
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
            return matrices.getMatrix(getFocusedMatrixUid());
        } catch(Exception e) {
            return null;
        }
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
     * Returns that HashMap that contains the tab objects and matrix uids
     *
     * @return the tabs HashMap
     */
    public HashMap<DraggableTab, Integer> getTabs() {
        return tabs;
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
     * @return  The matrix controller object that all the tabs are based on
     */
    public MatrixController getMatrixController() {
        return matrices;
    }


    /**
     * Creates and adds a matrix tab to the TabPane from a matrix object.
     *
     * @param  matrix  the matrix to add a tab for
     */
    public void addTab(
            TemplateDSM matrix,
            TemplateIOHandler<? extends TemplateDSM, ? extends TemplateMatrixView> ioHandler,
            TemplateMatrixView matrixView,
            TemplateHeaderMenu headerMenu,
            TemplateSideBar sideBar)
    {
        int matrixUid = currentMatrixUid;
        currentMatrixUid += 1;

        this.matrices.addMatrix(matrixUid, matrix, ioHandler, matrixView);

        // update the root layout
        this.rootLayout.setTop(headerMenu.getMenuBar());
        this.rootLayout.setLeft(sideBar.getLayout());

        String title = this.matrices.getMatrixIOHandler(matrixUid).getSavePath().getName();
        if(!matrices.isMatrixSaved(matrixUid)) {
            title += "*";
        }
        DraggableTab tab = new DraggableTab(title);
        this.matrices.getMatrixView(matrixUid).refreshMatrixEditor();
        tab.setContent(this.matrices.getMatrixView(matrixUid).getMatrixEditor());
        tab.setDetachable(false);
        tabPane.getScene().setOnKeyPressed(e -> {  // add keybinding to toggle cross-highlighting on the editor
            if (e.getCode() == KeyCode.F) {
                this.matrices.getMatrixView(matrixUid).toggleCrossHighlighting();
            }
        });

        tab.setOnCloseRequest(e -> {
            if(!matrices.isMatrixSaved(matrixUid)) {
                focusTab(matrixUid);
                int selection = this.matrices.getMatrixIOHandler(matrixUid).promptSave();

                // 0 = close the tab, 1 = save and close, 2 = don't close
                if(selection == 2) {  // user doesn't want to close the pane so consume the event
                    if(e != null) {
                        e.consume();
                    }
                    return;
                } else if(selection == 1) {  // user wants to save before closing the pane
                    // TODO: if there is an error saving, then display a message and don't close the file
                    this.matrices.getMatrixIOHandler(matrixUid).saveMatrixToFile(this.matrices.getMatrix(matrixUid), this.matrices.getMatrixIOHandler(matrixUid).getSavePath());
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
            this.matrices.removeMatrix(matrixUid);

            if(this.tabs.isEmpty()) {
                DefaultHeaderMenu menu = new DefaultHeaderMenu(this);
                this.rootLayout.setTop(menu.getMenuBar());
                this.rootLayout.setBottom(menu.getConnectionSearchLayout());
                this.rootLayout.setLeft(null);
                this.matrixMetaDataPane.setMatrix(null);
            }

        });

        tab.setOnSelectionChanged(e -> {
            matrixMetaDataPane.setMatrix(this.matrices.getMatrix(matrixUid));
            if(this.matrices.getMatrix(matrixUid).getClass().equals(SymmetricDSM.class)) {
                SymmetricHeaderMenu menu = new SymmetricHeaderMenu(this);
                this.rootLayout.setTop(menu.getMenuBar());
                this.rootLayout.setBottom(menu.getConnectionSearchLayout());
                this.rootLayout.setLeft(new SymmetricSideBar((SymmetricDSM)this.matrices.getMatrix(matrixUid), this).getLayout());
            } else if(this.matrices.getMatrix(matrixUid).getClass().equals(AsymmetricDSM.class)) {
                AsymmetricHeaderMenu menu = new AsymmetricHeaderMenu(this);
                this.rootLayout.setTop(menu.getMenuBar());
                this.rootLayout.setBottom(menu.getConnectionSearchLayout());
                this.rootLayout.setLeft(new AsymmetricSideBar((AsymmetricDSM)this.matrices.getMatrix(matrixUid), this).getLayout());
            } else {
                throw new IllegalStateException("Matrix being handled was not of a valid type");
            }
        });

        tabs.put(tab, matrixUid);
        tabPane.getTabs().add(tab);
    }


    /**
     * Focuses a tab by the matrix uid
     *
     * @param  matrixUid  the matrix uid that will be focused
     */
    public void focusTab(int matrixUid) {
        DraggableTab tab = null;
        for (HashMap.Entry<DraggableTab, Integer> e : tabs.entrySet()) {
            if(e.getValue().equals(matrixUid)) {
                tab = e.getKey();
                break;
            }
        }
        if(tab != null) {
            tabPane.getSelectionModel().select(tab);
        }
    }


    /**
     * Focuses a tab by a matrices save file
     *
     * @param file the matrix with this file path will be focused
     */
    public void focusTab(File file) {
        DraggableTab tab = null;
        for (HashMap.Entry<DraggableTab, Integer> e : tabs.entrySet()) {
            if(this.matrices.getMatrixIOHandler(e.getValue()).getSavePath().getAbsolutePath().equals(file.getAbsolutePath())) {
                tab = e.getKey();
                break;
            }
        }
        if(tab != null) {
            tabPane.getSelectionModel().select(tab);
        }
    }


    /**
     * Refreshes a tabs content by redrawing the content
     */
    public void refreshTab() {
        if(getFocusedMatrixUid() != null) {
            this.matrices.getMatrixView(getFocusedMatrixUid()).refreshMatrixEditor();
            getFocusedTab().setContent(this.matrices.getMatrixView(getFocusedMatrixUid()).getMatrixEditor());
            matrixMetaDataPane.setMatrix(this.matrices.getMatrix(getFocusedMatrixUid()));
        }
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

        this.matrices.getMatrixView(getFocusedMatrixUid()).setFontSize(fontSizes[currentFontSizeIndex]);
        this.matrices.getMatrixView(getFocusedMatrixUid()).refreshMatrixEditor();
        refreshTab();
    }


    /**
     * Decreases the font size of the current tab's matrix content. Updates the matrix content by refreshing the tab.
     */
    public void decreaseFontScaling() {
        if(getFocusedMatrixUid() == null) return;
        currentFontSizeIndex -= 1;
        if(currentFontSizeIndex < 0) currentFontSizeIndex = 0;

        this.matrices.getMatrixView(getFocusedMatrixUid()).setFontSize(fontSizes[currentFontSizeIndex]);
        this.matrices.getMatrixView(getFocusedMatrixUid()).refreshMatrixEditor();
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

        this.matrices.getMatrixView(getFocusedMatrixUid()).setFontSize(DEFAULT_FONT_SIZE);
        this.matrices.getMatrixView(getFocusedMatrixUid()).refreshMatrixEditor();
        refreshTab();
    }

}
