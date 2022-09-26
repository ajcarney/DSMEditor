package Matrices.EditorTabs;

import Matrices.Data.*;
import Matrices.Data.Entities.Grouping;
import Matrices.IOHandlers.AbstractIOHandler;
import Matrices.IOHandlers.AsymmetricIOHandler;
import Matrices.IOHandlers.MultiDomainIOHandler;
import Matrices.IOHandlers.SymmetricIOHandler;
import Matrices.SideBarTools.AbstractSideBar;
import Matrices.SideBarTools.AsymmetricSideBar;
import Matrices.SideBarTools.MultiDomainSideBar;
import Matrices.SideBarTools.SymmetricSideBar;
import Matrices.Views.*;
import UI.HeaderMenu;
import UI.MatrixMetaDataPane;
import UI.Widgets.DraggableTab;
import UI.Widgets.Misc;
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
public class MultiDomainEditorTab implements IEditorTab {

    protected final VBox centerLayout = new VBox();
    protected final VBox leftLayout = new VBox();
    protected final VBox rightLayout = new VBox();
    protected final HBox bottomLayout = new HBox();

    protected final MultiDomainDSMData matrixData;
    protected final MultiDomainView matrixView;
    protected final MultiDomainIOHandler ioHandler;
    protected final HeaderMenu headerMenu;


    protected final TabPane tabPane = new TabPane();
    protected final HashMap<DraggableTab, Pair<AbstractDSMData, AbstractMatrixView>> tabsData = new HashMap<>();  // tab object, matrix uid


    /**
     * Generic constructor. Creates the TabPane and sets an immutable tab for the main view
     *
     * @param data        the data for the multi-domain matrix
     * @param ioHandler   the ioHandler object for the matrix
     * @param headerMenu  the header menu instance so that it can update it for the matrix on the currently selected tab
     */
    public MultiDomainEditorTab(MultiDomainDSMData data, MultiDomainIOHandler ioHandler, HeaderMenu headerMenu) {
        VBox.setVgrow(centerLayout, Priority.ALWAYS);
        HBox.setHgrow(centerLayout, Priority.ALWAYS);
        centerLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        HBox.setHgrow(tabPane, Priority.ALWAYS);
        tabPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        leftLayout.setAlignment(Pos.CENTER);

        this.matrixData = data;
        this.ioHandler = ioHandler;
        this.headerMenu = headerMenu;

        this.matrixView = new MultiDomainView(this.matrixData, 12.0);

        DraggableTab tab = new DraggableTab("Main View");
        tab.setContent(this.matrixView.getView());
        tab.setDetachable(false);
        tab.setClosable(false);

        tab.setOnSelectionChanged(e -> {
            this.headerMenu.refresh(this.matrixData, this.ioHandler, this.matrixView);

            // don't allow editing if breakout views are open
            MultiDomainSideBar sideBar = new MultiDomainSideBar(this.matrixData, this.matrixView);
            if(tabsData.keySet().size() > 1) {
                sideBar.setDisabled();
                headerMenu.setDisabled(true);
            } else {
                sideBar.setEnabled();
                headerMenu.setDisabled(false);
            }
            this.matrixView.refreshView();

            leftLayout.getChildren().clear();
            leftLayout.getChildren().add(sideBar.getLayout());

            MatrixMetaDataPane metadata = new MatrixMetaDataPane(this.matrixData);
            rightLayout.getChildren().clear();
            rightLayout.getChildren().add(metadata.getLayout());

            bottomLayout.getChildren().clear();
            bottomLayout.setAlignment(Pos.CENTER);
            bottomLayout.setPadding(new Insets(15));
            Button zoomButton = new Button("Zoom");
            zoomButton.setOnAction(ee -> {
                // Create Root window
                Stage window = new Stage();
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
                fromDomain.getItems().addAll(this.matrixData.getDomains());

                ComboBox<Grouping> toDomain = new ComboBox<>();
                toDomain.setPromptText("To Domain");
                toDomain.setMinWidth(Region.USE_PREF_SIZE);
                toDomain.setPadding(new Insets(0));
                toDomain.setCellFactory(groupingItemCellFactory);
                toDomain.setButtonCell(groupingItemCellFactory.call(null));
                toDomain.getItems().addAll(this.matrixData.getDomains());

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
        tabsData.put(tab, new Pair<>(this.matrixData, this.matrixView));
        centerLayout.getChildren().add(tabPane);
    }


    /**
     * Sets up a tab to add to the TabPane as a breakout view
     *
     * @param fromGroup  the domain for the row items
     * @param toGroup    the domain for the column items
     */
    public void addBreakOutView(Grouping fromGroup, Grouping toGroup) {
        AbstractDSMData data = this.matrixData.exportZoom(fromGroup, toGroup);
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

        DraggableTab tab = new DraggableTab("Breakout");
        tab.setContent(view.getView());
        tab.setDetachable(false);
        tab.setClosable(true);

        tab.setOnCloseRequest(e -> {
           // TODO: ask if user wants to apply changes
            this.tabsData.remove(tab);
            this.tabPane.getTabs().remove(tab);
            headerMenu.setDisabled(false);
        });


        tab.setOnSelectionChanged(e -> {
            headerMenu.refresh(data, ioHandler, view);
            headerMenu.setDisabled(false);

            leftLayout.getChildren().clear();
            leftLayout.getChildren().add(sideBar.getLayout());

            MatrixMetaDataPane metadata = new MatrixMetaDataPane(data);
            rightLayout.getChildren().clear();
            rightLayout.getChildren().add(metadata.getLayout());

            bottomLayout.getChildren().clear();
            Button applyButton = new Button("Apply Changes");
            applyButton.setOnAction(ee -> {
                this.matrixData.importZoom(fromGroup, toGroup, data);
                this.matrixData.setCurrentStateAsCheckpoint();
                this.matrixView.refreshView();  // refresh the main view
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
        return tabsData.get((DraggableTab)tabPane.getSelectionModel().getSelectedItem()).getValue();
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
        return tabsData.get((DraggableTab)tabPane.getSelectionModel().getSelectedItem()).getKey();
    }

}
