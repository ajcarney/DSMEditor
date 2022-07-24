package UI;

import Constants.Constants;
import Matrices.Data.AbstractDSMData;
import Matrices.IDSM;
import Matrices.MatricesCollection;
import UI.Widgets.DraggableTab;
import UI.Widgets.Misc;
import javafx.beans.binding.Bindings;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

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
    private final MatricesCollection matrices;
    private final HeaderMenu headerMenu;
    private final ConnectionSearchWidget searchWidget;
    private final VBox bottomEditorTabLayout = new VBox();

    private static final double DEFAULT_FONT_SIZE = 12.0;

    private int currentMatrixUid = 0;
    private int currentFontSizeIndex;


    /**
     * Creates a new EditorPane object where each pane is a different matrix. Creates and starts a daemon thread
     * that manages the saved/unsaved name of the matrices in the tabview.
     *
     */
    public EditorPane(MatricesCollection matrices, BorderPane rootLayout) {
        this.matrices = matrices;
        this.searchWidget = new ConnectionSearchWidget(this);
        this.headerMenu = new HeaderMenu(this);

        for(int i=0; i<Constants.fontSizes.length; i++) {
            if(Constants.fontSizes[i] == DEFAULT_FONT_SIZE) {
                currentFontSizeIndex = i;
                break;
            }
        }


        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);  // any tab can be closed, but add event to be called on close

        this.rootLayout = rootLayout;
        this.rootLayout.setTop(headerMenu.getMenuBar());

        VBox bottomLayout = new VBox();
        bottomLayout.getChildren().addAll(bottomEditorTabLayout, searchWidget.getMainLayout());
        this.rootLayout.setBottom(bottomLayout);

        this.rootLayout.setCenter(getTabPane());
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
    public AbstractDSMData getFocusedMatrixData() {
        try {
            return matrices.getMatrix(getFocusedMatrixUid()).getMatrixData();
        } catch(Exception e) {
            return null;
        }
    }


    /**
     * Finds the matrix the user is focused on by using a lookup table
     *
     * @return  the uid of the matrix that is focused
     */
    public IDSM getFocusedMatrix() {
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
    public MatricesCollection getMatricesCollection() {
        return matrices;
    }


    /**
     * @return  the header menu object for the matrix
     */
    public HeaderMenu getHeaderMenu() {
        return headerMenu;
    }
    

    /**
     * @return  the search widget for the editor
     */
    public ConnectionSearchWidget getSearchWidget() {
        return searchWidget;
    }


    /**
     * Creates and adds a matrix tab to the TabPane from a matrix object.
     *
     * @param  matrix  the matrix to add a tab for
     */
    public void addTab(IDSM matrix)
    {
        int matrixUid = currentMatrixUid;
        currentMatrixUid += 1;

        this.matrices.addMatrix(matrixUid, matrix);

        DraggableTab tab = new DraggableTab("");

        // bind a "*" to the name if the matrix has not been saved
        tab.getLabelTextProperty().bind(Bindings.createStringBinding(() -> {
            String title = this.matrices.getMatrix(matrixUid).getMatrixIOHandler().getSavePath().getName();
            if (matrix.getMatrixData().getWasModifiedProperty().get()) {
                title += "*";
            }
            return title;
        }, matrix.getMatrixData().getWasModifiedProperty()));


        this.matrices.getMatrix(matrixUid).getMatrixView().refreshView();
        tab.setContent(this.matrices.getMatrix(matrixUid).getMatrixEditorTab().getCenterPane());

        tab.setDetachable(false);
        tabPane.getScene().setOnKeyPressed(e -> {  // add keybinding to toggle cross-highlighting on the editor
            if (e.getCode() == KeyCode.F) {
                this.matrices.getMatrix(matrixUid).getMatrixView().toggleCrossHighlighting();
            }
        });

        tab.setOnCloseRequest(e -> {
            if(!matrices.isMatrixSaved(matrixUid)) {
                focusTab(matrixUid);
                int selection = this.matrices.getMatrix(matrixUid).getMatrixIOHandler().promptSave();

                // 0 = close the tab, 1 = save and close, 2 = don't close
                if(selection == 2) {  // user doesn't want to close the pane so consume the event
                    if(e != null) {
                        e.consume();
                    }
                    return;
                } else if(selection == 1) {  // user wants to save before closing the pane
                    // TODO: if there is an error saving, then display a message and don't close the file
                    this.matrices.getMatrix(matrixUid).getMatrixIOHandler().saveMatrixToFile(this.matrices.getMatrix(matrixUid).getMatrixIOHandler().getSavePath());
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
                headerMenu.refresh(null, null, null);
                this.rootLayout.setLeft(null);
                this.rootLayout.setRight(null);
            }

        });

        tab.setOnSelectionChanged(e -> {
            headerMenu.refresh(this.matrices.getMatrix(matrixUid).getMatrixData(), this.matrices.getMatrix(matrixUid).getMatrixIOHandler(), this.matrices.getMatrix(matrixUid).getMatrixView());
            headerMenu.setEditDisabled(false);

            this.rootLayout.setLeft(this.matrices.getMatrix(matrixUid).getMatrixEditorTab().getLeftPane());
            this.rootLayout.setRight(this.matrices.getMatrix(matrixUid).getMatrixEditorTab().getRightPane());
            bottomEditorTabLayout.getChildren().clear();
            bottomEditorTabLayout.getChildren().add(this.matrices.getMatrix(matrixUid).getMatrixEditorTab().getBottomPane());
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
            if(this.matrices.getMatrix(e.getValue()).getMatrixIOHandler().getSavePath().getAbsolutePath().equals(file.getAbsolutePath())) {
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
    public void refreshSelectedTab() {
        if(getFocusedMatrixUid() != null) {
            this.matrices.getMatrix(getFocusedMatrixUid()).getMatrixView().refreshView();
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
        if(currentFontSizeIndex > Constants.fontSizes.length - 1) currentFontSizeIndex = Constants.fontSizes.length - 1;

        this.matrices.getMatrix(getFocusedMatrixUid()).getMatrixView().setFontSize(Constants.fontSizes[currentFontSizeIndex]);
        this.matrices.getMatrix(getFocusedMatrixUid()).getMatrixView().refreshView();
        refreshSelectedTab();
    }


    /**
     * Decreases the font size of the current tab's matrix content. Updates the matrix content by refreshing the tab.
     */
    public void decreaseFontScaling() {
        if(getFocusedMatrixUid() == null) return;
        currentFontSizeIndex -= 1;
        if(currentFontSizeIndex < 0) currentFontSizeIndex = 0;

        this.matrices.getMatrix(getFocusedMatrixUid()).getMatrixView().setFontSize(Constants.fontSizes[currentFontSizeIndex]);
        this.matrices.getMatrix(getFocusedMatrixUid()).getMatrixView().refreshView();
        refreshSelectedTab();
    }


    /**
     * Sets the font size of the current tab's matrix content to the default. Updates the matrix content by refreshing the tab
     */
    public void resetFontScaling() {
        if(getFocusedMatrixUid() == null) return;
        for(int i=0; i<Constants.fontSizes.length; i++) {
            if(Constants.fontSizes[i] == DEFAULT_FONT_SIZE) {
                currentFontSizeIndex = i;
                break;
            }
        }

        this.matrices.getMatrix(getFocusedMatrixUid()).getMatrixView().setFontSize(DEFAULT_FONT_SIZE);
        this.matrices.getMatrix(getFocusedMatrixUid()).getMatrixView().refreshView();
        refreshSelectedTab();
    }

}
