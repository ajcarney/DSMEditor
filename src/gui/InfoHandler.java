package gui;

import Data.SymmetricDSM;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;


/**
 * Class for creating the part of the gui that allows users to view and edit
 * the metadata for a matrix
 *
 * @author Aiden Carney
 */
public class InfoHandler {
    private Label titleLabel;
    private Label projectNameLabel;
    private Label customerLabel;
    private Label versionNumberLabel;

    private Button modifyButton;

    private VBox layout;
    private GridPane detailsLayout;

    private SymmetricDSM matrix;


    /**
     * Creates a new object and instantiates widgets on the gui.
     */
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
            if(matrix == null) {
                return;
            }
            Stage window = new Stage();

            // Create Root window
            window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
            window.setTitle("Configure Matrix Info");

            GridPane editLayout = new GridPane();

            Label titlePrompt = new Label("Title: ");
            Label projectPrompt = new Label("Project Name: ");
            Label customerPrompt = new Label("Customer: ");
            Label versionPrompt = new Label("Version: ");

            TextField title = new TextField();
            title.setText(titleLabel.getText());
            title.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(title, Priority.ALWAYS);

            TextField project = new TextField();
            project.setText(projectNameLabel.getText());
            project.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(project, Priority.ALWAYS);

            TextField customer = new TextField();
            customer.setText(customerLabel.getText());
            customer.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(customer, Priority.ALWAYS);

            TextField version = new TextField();
            version.setText(versionNumberLabel.getText());
            version.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(version, Priority.ALWAYS);

            GridPane.setConstraints(titlePrompt, 0, 0);
            GridPane.setConstraints(projectPrompt, 0, 1);
            GridPane.setConstraints(customerPrompt, 0, 2);
            GridPane.setConstraints(versionPrompt, 0, 3);
            GridPane.setConstraints(title, 1, 0);
            GridPane.setConstraints(project, 1, 1);
            GridPane.setConstraints(customer, 1, 2);
            GridPane.setConstraints(version, 1, 3);
            editLayout.getChildren().addAll(titlePrompt, projectPrompt, customerPrompt, versionPrompt, title, project, customer, version);
            editLayout.setHgap(10);
            editLayout.setVgap(10);
            editLayout.setAlignment(Pos.CENTER);

            // create HBox for user to close with our without changes
            Pane vSpacer = new Pane();  // used as a spacer between buttons
            VBox.setVgrow(vSpacer, Priority.ALWAYS);
            vSpacer.setMaxHeight(Double.MAX_VALUE);

            HBox closeArea = new HBox();
            Button applyAllButton = new Button("Apply");
            applyAllButton.setOnAction(ee -> {
                titleLabel.setText(title.getText());
                projectNameLabel.setText(project.getText());
                customerLabel.setText(customer.getText());
                versionNumberLabel.setText(version.getText());

                matrix.setTitle(title.getText());
                matrix.setProjectName(project.getText());
                matrix.setCustomer(customer.getText());
                matrix.setVersionNumber(version.getText());

                matrix.setCurrentStateAsCheckpoint();

                window.close();
            });

            Pane spacer = new Pane();  // used as a spacer between buttons
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer.setMaxWidth(Double.MAX_VALUE);

            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(ee -> {
                window.close();
            });
            closeArea.getChildren().addAll(cancelButton, spacer, applyAllButton);

            VBox rootLayout = new VBox(10);
            rootLayout.getChildren().addAll(editLayout, vSpacer, closeArea);
            rootLayout.setAlignment(Pos.CENTER);
            rootLayout.setPadding(new Insets(10, 10, 10, 10));
            rootLayout.setSpacing(10);


            //Display window and wait for it to be closed before returning
            Scene scene = new Scene(rootLayout, 400, 250);
            window.setScene(scene);
            window.showAndWait();

        });
        modifyButton.setMaxWidth(Double.MAX_VALUE);

        layout.getChildren().addAll(detailsLayout, modifyButton);
        layout.setSpacing(20);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setAlignment(Pos.CENTER);
    }


    /**
     * Changes which matrix the gui will display the metadata of. Updates the gui as well
     *
     * @param newMatrix the new SymmetricDSM object to use
     */
    public void setMatrix(SymmetricDSM newMatrix) {
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


    /**
     * Returns the VBox layout that contains all the widgets so that it can be added to a main layout
     *
     * @return VBox of the gui that displays the metadata
     */
    public VBox getLayout() {
        return layout;
    }
}
