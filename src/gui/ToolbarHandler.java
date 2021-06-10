package gui;

import DSMData.DataHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public class ToolbarHandler {
    private VBox layout;

    private Button addMatrixItem;
    private Button deleteMatrixItem;
    private Button renameMatrixItem;
    private Button modifyConnections;
    private Button sort;

    private TabView tabView;

    public ToolbarHandler(TabView tabview) {
        layout = new VBox();

        addMatrixItem = new Button("Add Row/Column");
        addMatrixItem.setOnAction(e -> {
            System.out.println("Adding row or column");
        });
        addMatrixItem.setMaxWidth(Double.MAX_VALUE);

        deleteMatrixItem = new Button("Delete Row/Column");
        deleteMatrixItem.setOnAction(e -> {
            System.out.println("Deleting row or column");
        });
        deleteMatrixItem.setMaxWidth(Double.MAX_VALUE);

        renameMatrixItem = new Button("Rename Row/Column");
        renameMatrixItem.setOnAction(e -> {
            System.out.println("Renaming row or column");
        });
        renameMatrixItem.setMaxWidth(Double.MAX_VALUE);

        modifyConnections = new Button("Modify Connections");
        modifyConnections.setOnAction(e -> {
            System.out.println("Modifying connections");
        });
        modifyConnections.setMaxWidth(Double.MAX_VALUE);

        sort = new Button("Sort");
        sort.setOnAction(e -> {
            System.out.println("Sorting");
        });
        sort.setMaxWidth(Double.MAX_VALUE);

        layout.getChildren().addAll(addMatrixItem, deleteMatrixItem, renameMatrixItem, modifyConnections, sort);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(20);
        layout.setAlignment(Pos.CENTER);
    }

    public VBox getLayout() {
        return layout;
    }
}
