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
import Matrices.SideBarTools.AbstractSideBar;
import Matrices.SideBarTools.AsymmetricSideBar;
import Matrices.SideBarTools.MultiDomainSideBar;
import Matrices.SideBarTools.SymmetricSideBar;
import Matrices.Views.AsymmetricView;
import Matrices.Views.IMatrixView;
import Matrices.Views.MultiDomainView;
import Matrices.Views.SymmetricView;
import UI.HeaderMenu;
import UI.MatrixMetaDataPane;
import UI.Widgets.DraggableTab;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.io.File;
import java.util.HashMap;

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
    protected final HashMap<DraggableTab, Pair<AbstractDSMData, IMatrixView>> tabsData = new HashMap<>();  // tab object, matrix uid


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

            leftLayout.getChildren().clear();
            leftLayout.getChildren().add(new MultiDomainSideBar(this.matrixData, this.matrixView).getLayout());

            MatrixMetaDataPane metadata = new MatrixMetaDataPane(this.matrixData);
            rightLayout.getChildren().clear();
            rightLayout.getChildren().add(metadata.getLayout());

            bottomLayout.getChildren().clear();
            bottomLayout.getChildren().add(new Pane());
        });

        tabPane.getTabs().add(tab);
        tabsData.put(tab, new Pair<>(this.matrixData, this.matrixView));
        centerLayout.getChildren().add(tabPane);
    }


    public void addBreakOutView(Grouping fromGroup, Grouping toGroup) {
        AbstractDSMData data = this.matrixData.exportZoom(fromGroup, toGroup);
        IMatrixView view;
        AbstractSideBar sideBar;
        AbstractIOHandler ioHandler;
        if(data instanceof SymmetricDSMData symmetricData) {
            view = new SymmetricView(symmetricData, 12.0);
            sideBar = new SymmetricSideBar(symmetricData, (SymmetricView) view);
            ioHandler = new SymmetricIOHandler(new File(""), symmetricData);
        } else if(data instanceof AsymmetricDSMData asymmetricData) {
            view = new AsymmetricView(asymmetricData, 12.0);
            sideBar = new AsymmetricSideBar(asymmetricData, (AsymmetricView) view);
            ioHandler = new AsymmetricIOHandler(new File(""), asymmetricData);
        } else {
            throw new AssertionError("Breakout view created was of an invalid type");
        }

        DraggableTab tab = new DraggableTab("Breakout");
        tab.setOnCloseRequest(e -> {
           // TODO: ask if user wants to apply changes
            this.tabsData.remove(tab);
            this.tabPane.getTabs().remove(tab);
        });


        tab.setOnSelectionChanged(e -> {
            headerMenu.refresh(data, ioHandler, view);

            leftLayout.getChildren().clear();
            leftLayout.getChildren().add(sideBar.getLayout());

            MatrixMetaDataPane metadata = new MatrixMetaDataPane(data);
            rightLayout.getChildren().clear();
            rightLayout.getChildren().add(metadata.getLayout());

            bottomLayout.getChildren().clear();
            Button applyButton = new Button("Apply Changes");
            applyButton.setOnAction(ee -> {
                this.matrixData.importZoom(fromGroup, toGroup, data);
                this.matrixView.refreshView();  // refresh the main view
            });
            HBox applyButtonLayout = new HBox();
            applyButtonLayout.setAlignment(Pos.CENTER);
            applyButtonLayout.getChildren().add(applyButton);
            bottomLayout.getChildren().add(applyButtonLayout);
        });


        tabPane.getTabs().add(tab);
        tabsData.put(tab, new Pair<>(data, view));
    }


    @Override
    public final Pane getCenterPane() {
        return centerLayout;
    }

    @Override
    public final Pane getLeftPane() {
        return leftLayout;
    }

    @Override
    public final Pane getRightPane() {
        return rightLayout;
    }

    @Override
    public final Pane getBottomPane() {
        return bottomLayout;
    }

    @Override
    public IMatrixView getMatrixView() {
        return tabsData.get((DraggableTab)tabPane.getSelectionModel().getSelectedItem()).getValue();
    }

}
