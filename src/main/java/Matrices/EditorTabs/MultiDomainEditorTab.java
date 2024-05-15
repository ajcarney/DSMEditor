package Matrices.EditorTabs;

import Matrices.Data.AbstractDSMData;
import Matrices.Data.AsymmetricDSMData;
import Matrices.Data.Entities.Grouping;
import Matrices.Data.MultiDomainDSMData;
import Matrices.Data.SymmetricDSMData;
import Matrices.IOHandlers.AbstractIOHandler;
import Matrices.IOHandlers.AsymmetricIOHandler;
import Matrices.IOHandlers.MultiDomainIOHandler;
import Matrices.IOHandlers.SymmetricIOHandler;
import UI.MatrixMetaDataPane;
import UI.MatrixViews.AbstractMatrixView;
import UI.MatrixViews.AsymmetricView;
import UI.MatrixViews.MultiDomainView;
import UI.MatrixViews.SymmetricView;
import UI.SideBarViews.AbstractSideBar;
import UI.SideBarViews.AsymmetricSideBar;
import UI.SideBarViews.MultiDomainSideBar;
import UI.SideBarViews.SymmetricSideBar;
import UI.Widgets.DraggableTab;
import UI.Widgets.Misc;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Used to handle a tab for a multi-domain matrix. Creates a TabPane. Opens a tab for the full view and supports
 * adding breakout views by selecting the domains to zoom to. When breakout views are open the main view is set
 * to the static render mode
 */
public class MultiDomainEditorTab extends AbstractEditorTab {
    int breakoutNum = 1;

    protected final VBox centerLayout = new VBox();
    protected final VBox leftLayout = new VBox();
    protected final VBox rightLayout = new VBox();
    protected final HBox bottomLayout = new HBox();

    protected final MultiDomainDSMData mainMatrixData;
    protected final MultiDomainView mainMatrixView;
    protected final MultiDomainIOHandler mainIOHandler;
    protected final MultiDomainSideBar mainSidebar;


    protected final TabPane tabPane = new TabPane();
    protected final HashMap<DraggableTab, Pair<AbstractDSMData, AbstractMatrixView>> tabsData = new HashMap<>();  // tab object, matrix uid


    /**
     * Generic constructor. Creates the TabPane and sets an immutable tab for the main view
     *
     * @param data        the data for the multi-domain matrix
     * @param ioHandler   the ioHandler object for the matrix
     */
    public MultiDomainEditorTab(MultiDomainDSMData data, MultiDomainIOHandler ioHandler) {
        this.mainMatrixData = data;
        this.mainIOHandler = ioHandler;
        this.mainMatrixView = new MultiDomainView(this.mainMatrixData, 12.0);
        this.mainSidebar = new MultiDomainSideBar(this.mainMatrixData, this.mainMatrixView);

        init();  // set up the tab pane
    }


    /**
     * Creates a new matrix object by reading in a file. Throws IllegalArgumentException if there was an error reading
     * the file
     *
     * @param file    the file object that contains a MultiDomain dsm to read
     */
    public MultiDomainEditorTab(File file) {
        this.mainIOHandler = new MultiDomainIOHandler(file);
        this.mainMatrixData = mainIOHandler.readFile();
        if(this.mainMatrixData == null) {
            throw new IllegalArgumentException("There was an error reading the matrix at " + file);  // error because error occurred on file read
        }
        mainIOHandler.setMatrix(this.mainMatrixData);

        this.mainMatrixView = new MultiDomainView(mainMatrixData, 12.0);
        this.mainSidebar = new MultiDomainSideBar(mainMatrixData, mainMatrixView);


        init();  // set up the tab pane
    }


    /**
     * Initializes the tab pane and sets the main view to the tab
     */
    private void init() {
        VBox.setVgrow(centerLayout, Priority.ALWAYS);
        HBox.setHgrow(centerLayout, Priority.ALWAYS);
        centerLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        HBox.setHgrow(tabPane, Priority.ALWAYS);
        tabPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        leftLayout.setAlignment(Pos.CENTER);

        DraggableTab tab = new DraggableTab("Main View");
        tab.setContent(this.mainMatrixView.getView());
        tab.setDetachable(false);
        tab.setClosable(false);

        tab.setOnSelectionChanged(e -> {
//            this.headerMenu.refresh(this.matrixData, this.ioHandler, this.matrixView);
            // update the data members for the main view
            this.matrixData = this.mainMatrixData;
            this.matrixIOHandler = this.mainIOHandler;
            this.matrixView = this.mainMatrixView;
            this.matrixSideBar = this.mainSidebar;

            if(tabsData.keySet().size() > 1) {  // don't allow editing if breakout views are open
                mainSidebar.setDisabled();
                this.mainMatrixView.setCurrentMode(AbstractMatrixView.MatrixViewMode.STATIC_NAMES);  // TODO: make this change between names weights, fast
                isMutable.set(false);
            } else {
                mainSidebar.setEnabled();
                isMutable.set(true);
            }
            this.mainMatrixView.refreshView();
            this.isChanged.set(true);

            leftLayout.getChildren().clear();
            leftLayout.getChildren().add(mainSidebar.getLayout());

            MatrixMetaDataPane metadata = new MatrixMetaDataPane(this.mainMatrixData);
            rightLayout.getChildren().clear();
            rightLayout.getChildren().add(metadata.getLayout());

            bottomLayout.getChildren().clear();
            bottomLayout.setAlignment(Pos.CENTER);
            bottomLayout.setPadding(new Insets(15));
            Button zoomButton = new Button("Zoom");
            zoomButton.setOnAction(ee -> {
                // Create Root window
                Stage window = new Stage();
                window.initOwner(centerLayout.getScene().getWindow());
                window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
                window.setTitle("Configure Groupings");

                Callback<ListView<Grouping>, ListCell<Grouping>> groupingItemCellFactory = new Callback<>() {
                    @Override
                    public ListCell<Grouping> call(ListView<Grouping> l) {
                        return new ListCell<>() {

                            @Override
                            protected void updateItem(Grouping group, boolean empty) {
                                super.updateItem(group, empty);

                                if (empty || group == null) {
                                    setText(null);
                                } else {
                                    setText(group.getName());
                                }
                            }
                        };
                    }
                };
                ComboBox<Grouping> fromDomain = new ComboBox<>();
                fromDomain.setPromptText("From Domain");
                fromDomain.setMinWidth(Region.USE_PREF_SIZE);
                fromDomain.setPadding(new Insets(0));
                fromDomain.setCellFactory(groupingItemCellFactory);
                fromDomain.setButtonCell(groupingItemCellFactory.call(null));
                fromDomain.getItems().addAll(this.mainMatrixData.getDomains());

                ComboBox<Grouping> toDomain = new ComboBox<>();
                toDomain.setPromptText("To Domain");
                toDomain.setMinWidth(Region.USE_PREF_SIZE);
                toDomain.setPadding(new Insets(0));
                toDomain.setCellFactory(groupingItemCellFactory);
                toDomain.setButtonCell(groupingItemCellFactory.call(null));
                toDomain.getItems().addAll(this.mainMatrixData.getDomains());

                HBox mainView = new HBox();
                mainView.setSpacing(15);
                mainView.setPadding(new Insets(15));
                mainView.getChildren().addAll(fromDomain, Misc.getHorizontalSpacer(), toDomain);

                // create HBox for user to do the zoom function or to cancel the zoom
                HBox closeArea = new HBox();
                Button cancelButton = new Button("Cancel");
                Button okButton = new Button("Ok");

                cancelButton.setOnAction(eee -> {
                    window.close();  // do nothing
                });

                okButton.setOnAction(eee -> {
                    addBreakOutView(fromDomain.getValue(), toDomain.getValue());
                    window.close();
                });

                closeArea.getChildren().addAll(cancelButton, Misc.getHorizontalSpacer(), okButton);
                closeArea.setPadding(new Insets(10));

                // set up layout
                VBox layout = new VBox(10);
                layout.getChildren().addAll(mainView, Misc.getVerticalSpacer(), closeArea);
                layout.setAlignment(Pos.CENTER);
                layout.setPadding(new Insets(10, 10, 10, 10));
                layout.setSpacing(10);

                //Display window and wait for it to be closed before returning
                Scene scene = new Scene(layout, 350, 125);
                window.setScene(scene);
                window.showAndWait();
            });
            bottomLayout.getChildren().add(zoomButton);
        });

        tabPane.getTabs().add(tab);
        tabsData.put(tab, new Pair<>(this.mainMatrixData, this.mainMatrixView));
        centerLayout.getChildren().add(tabPane);

        // set up bindings
        this.isSaved.bind(this.mainMatrixData.getWasModifiedProperty().not());  // saved when not modified
        this.titleProperty.bind(Bindings.createStringBinding(() -> {
            String title = mainIOHandler.getSavePath().getName();
            if (mainMatrixData.getWasModifiedProperty().get()) {
                title += "*";
            }
            return title;
        }, mainMatrixData.getWasModifiedProperty()));
    }


    /**
     * Sets up a tab to add to the TabPane as a breakout view
     *
     * @param fromGroup  the domain for the row items
     * @param toGroup    the domain for the column items
     */
    public void addBreakOutView(Grouping fromGroup, Grouping toGroup) {
        // TODO: may want to rethink how ioHandler (in header menu) is used in breakout views
        AbstractDSMData data = this.mainMatrixData.exportZoom(fromGroup, toGroup);
        AbstractMatrixView view;
        AbstractSideBar sideBar;
        AbstractIOHandler ioHandler;
        if(data instanceof SymmetricDSMData symmetricData) {
            view = new SymmetricView(symmetricData, 12.0);
            view.refreshView();
            sideBar = new SymmetricSideBar(symmetricData, (SymmetricView) view);
            ioHandler = new SymmetricIOHandler(new File(""), symmetricData);
        } else if(data instanceof AsymmetricDSMData asymmetricData) {
            view = new AsymmetricView(asymmetricData, 12.0);
            view.refreshView();
            sideBar = new AsymmetricSideBar(asymmetricData, (AsymmetricView) view);
            ioHandler = new AsymmetricIOHandler(new File(""), asymmetricData);
        } else {
            throw new AssertionError("Breakout view created was of an invalid type");
        }

        DraggableTab tab = new DraggableTab("Breakout" + breakoutNum);
        breakoutNum++;
        tab.setContent(view.getView());
        tab.setDetachable(false);
        tab.setClosable(true);

        tab.setOnCloseRequest(e -> {
           // TODO: ask if user wants to apply changes
            this.tabsData.remove(tab);
            this.tabPane.getTabs().remove(tab);
            isMutable.set(true);
        });


        tab.setOnSelectionChanged(e -> {
            matrixData = data;
            matrixIOHandler = ioHandler;
            matrixView = view;
            matrixSideBar = sideBar;

            isMutable.set(true);
            isChanged.set(true);

            leftLayout.getChildren().clear();
            leftLayout.getChildren().add(sideBar.getLayout());

            MatrixMetaDataPane metadata = new MatrixMetaDataPane(data);
            rightLayout.getChildren().clear();
            rightLayout.getChildren().add(metadata.getLayout());

            bottomLayout.getChildren().clear();
            Button applyButton = new Button("Apply Changes");
            applyButton.setOnAction(ee -> {
                this.mainMatrixData.importZoom(fromGroup, toGroup, data);
                this.mainMatrixData.setCurrentStateAsCheckpoint();
                this.mainMatrixView.refreshView();  // refresh the main view
            });
            HBox applyButtonLayout = new HBox();
            applyButtonLayout.setAlignment(Pos.CENTER);
            applyButtonLayout.getChildren().add(applyButton);
            bottomLayout.getChildren().add(applyButtonLayout);
        });


        tabPane.getTabs().add(tab);
        tabsData.put(tab, new Pair<>(data, view));

        tabPane.getSelectionModel().select(tab);  // focus the tab right away
    }


    /**
     * @return  The node to be displayed as the center content
     */
    @Override
    public final Pane getCenterPane() {
        return centerLayout;
    }


    /**
     * @return  The node to be displayed as the left content
     */
    @Override
    public final Pane getLeftPane() {
        return leftLayout;
    }


    /**
     * @return  The node to be displayed as the right content
     */
    @Override
    public final Pane getRightPane() {
        return rightLayout;
    }


    /**
     * @return  The node to be displayed as the bottom content
     */
    @Override
    public final Pane getBottomPane() {
        return bottomLayout;
    }


    /**
     * @return  The matrix view used by the tab
     */
    @Override
    public AbstractMatrixView getMatrixView() {
//        return tabsData.get((DraggableTab)tabPane.getSelectionModel().getSelectedItem()).getValue();
        return matrixView;
    }


    /**
     * @return  all the matrix views currently in the editor tab
     */
    @Override
    public ArrayList<AbstractMatrixView> getAllMatrixViews() {
        ArrayList<AbstractMatrixView> views = new ArrayList<>();
        for(Pair<AbstractDSMData, AbstractMatrixView> data : tabsData.values()) {
            views.add(data.getValue());
        }
        return views;
    }


    /**
     * @return  the matrix data of the open matrix
     */
    @Override
    public AbstractDSMData getMatrixData() {
        return matrixData;
//        return tabsData.get((DraggableTab)tabPane.getSelectionModel().getSelectedItem()).getKey();
    }

}
