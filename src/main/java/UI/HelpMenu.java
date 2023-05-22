package UI;

import Constants.Constants;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import Logging.Logger;

import java.awt.*;
import java.net.URI;

public class HelpMenu {

    public static void openAboutMenu() {
        // bring up window asking to delete rows or columns
        Stage window = new Stage();
        window.setTitle("About DSMEditor");
        window.initModality(Modality.APPLICATION_MODAL);  // Block events to other windows

        VBox rootLayout = new VBox();
        rootLayout.setPadding(new Insets(10, 10, 10, 10));
        rootLayout.setSpacing(10);

        Label programVersionLabel = new Label("Executable Version: " + Constants.version);

        String javaVersion = "???";
        try {
            javaVersion = System.getProperty("java.version");
        } catch (Exception e) {
            Logger.submitException(e, Logger.LOGGER_PRIORITY.LOW);
        }
        Label javaVersionLabel = new Label("JVM Version: " + javaVersion);

        rootLayout.getChildren().addAll(programVersionLabel, javaVersionLabel);

        Scene scene = new Scene(rootLayout);
        window.setScene(scene);
        window.showAndWait();
    }


    public static void openBugReportMenu() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/ajcarney/DSMEditor/issues"));
        } catch (Exception e) {
            Logger.submitException(e, Logger.LOGGER_PRIORITY.HIGH);
        }
    }


    public static void openContactMenu() {

    }


    public static void openGettingStartedMenu() {

    }


}
