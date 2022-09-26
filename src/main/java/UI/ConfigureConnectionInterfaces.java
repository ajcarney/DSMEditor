package UI;

import Matrices.Data.Entities.DSMInterfaceType;
import UI.Widgets.Misc;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;


/**
 * Class for selecting interfaces graphically
 */
public class ConfigureConnectionInterfaces {

    /**
     * Opens a popup window where the user can select different interfaces
     *
     * @param interfaces         the available interfaces grouped by their grouping
     * @param currentInterfaces  the currently selected interfaces
     * @return
     */
    public static ArrayList<DSMInterfaceType> configureConnectionInterfaces(HashMap<String, Vector<DSMInterfaceType>> interfaces, Collection<DSMInterfaceType> currentInterfaces) {
        // create popup window to edit the connection
        Stage window = new Stage();

        // Create Root window
        window.initModality(Modality.APPLICATION_MODAL); //Block events to other windows
        window.setTitle("Interfaces");

        final Accordion accordion = new Accordion ();
        ArrayList<DSMInterfaceType> selectedInterfaces = new ArrayList<>(currentInterfaces);  // copy

        for(Map.Entry<String, Vector<DSMInterfaceType>> interfaceGrouping : interfaces.entrySet()) {
            TitledPane root = new TitledPane();
            root.setText(interfaceGrouping.getKey());

            VBox interfaceCheckBoxView = new VBox();
            interfaceCheckBoxView.setSpacing(5);
            for(DSMInterfaceType i : interfaceGrouping.getValue()) {  // add the checkboxes
                CheckBox checkBoxItem = new CheckBox();
                checkBoxItem.setText(i.getName());
                checkBoxItem.setSelected(currentInterfaces.contains(i));

                checkBoxItem.selectedProperty().addListener((o, oldValue, newValue) -> {
                    if(newValue) {
                        selectedInterfaces.add(i);
                    } else {
                        selectedInterfaces.remove(i);
                    }
                });

                interfaceCheckBoxView.getChildren().add(checkBoxItem);
            }

            root.setContent(new ScrollPane(interfaceCheckBoxView));
            accordion.getPanes().add(root);
        }

        Button okButton = new Button("Ok");
        okButton.setOnAction(ee -> window.close());

        VBox rootLayout = new VBox();
        rootLayout.setSpacing(5);
        rootLayout.setPadding(new Insets(10));
        rootLayout.setAlignment(Pos.CENTER);
        rootLayout.getChildren().addAll(accordion, Misc.getVerticalSpacer(), okButton);

        Scene scene = new Scene(rootLayout, 400, 200);
        window.setScene(scene);
        window.showAndWait();

        return selectedInterfaces;
    }
}
