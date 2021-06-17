package gui;

import DSMData.DataHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class InfoHandler {
    private Label titleLabel;
    private Label projectNameLabel;
    private Label customerLabel;
    private Label versionNumberLabel;

    private Button modifyButton;

    private VBox layout;
    private GridPane detailsLayout;

    private DataHandler matrix;

    public InfoHandler() {
        layout = new VBox();
        detailsLayout = new GridPane();

        Label titleHeader = new Label("Title: ");
        Label projectHeader = new Label("Project Name: ");
        Label customerHeader = new Label("Customer: ");
        Label versionHeader = new Label("Version: ");

        titleLabel = new Label("");
        projectNameLabel = new Label("");
        customerLabel = new Label("");
        versionNumberLabel = new Label("");

        GridPane.setConstraints(titleHeader, 0, 0);
        GridPane.setConstraints(projectHeader, 0, 1);
        GridPane.setConstraints(customerHeader, 0, 2);
        GridPane.setConstraints(versionHeader, 0, 3);
        GridPane.setConstraints(titleLabel, 1, 0);
        GridPane.setConstraints(projectNameLabel, 1, 1);
        GridPane.setConstraints(customerLabel, 1, 2);
        GridPane.setConstraints(versionNumberLabel, 1, 3);
        detailsLayout.getChildren().addAll(titleHeader, projectHeader, customerHeader, versionHeader, titleLabel, projectNameLabel, customerLabel, versionNumberLabel);
        detailsLayout.setHgap(10);
        detailsLayout.setVgap(10);

        modifyButton = new Button("Edit");
        modifyButton.setOnAction(e -> {
            System.out.println("Modifying matrix");
        });
        modifyButton.setMaxWidth(Double.MAX_VALUE);

        layout.getChildren().addAll(detailsLayout, modifyButton);
        layout.setSpacing(20);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setAlignment(Pos.CENTER);
    }

    public void setMatrix(DataHandler newMatrix) {
        matrix = newMatrix;
        if(matrix != null) {
            titleLabel.setText(matrix.getTitle());
            projectNameLabel.setText(matrix.getProjectName());
            customerLabel.setText(matrix.getCustomer());
            versionNumberLabel.setText(matrix.getVersionNumber());
        } else {
            titleLabel.setText("");
            projectNameLabel.setText("");
            customerLabel.setText("");
            versionNumberLabel.setText("");
        }
    }

    public VBox getLayout() {
        return layout;
    }
}
