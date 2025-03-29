package UI;

import Constants.Constants;
import Logging.Logger;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;


/**
 * Contains methods for popups containing help/other information
 */
public class HelpMenu {

    /**
     * Opens a window with information about the executable
     * @param parentWindow the parent window so that the popup is centered
     */
    public static void openAboutMenu(Window parentWindow) {
        // bring up window asking to delete rows or columns
        Stage window = new Stage();
        window.initOwner(parentWindow);
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
            new ProcessBuilder("x-www-browser", "https://github.com/ajcarney/DSMEditor/issues").start();
        } catch (Exception e) {
            Logger.submitException(e, Logger.LOGGER_PRIORITY.HIGH);
        }
    }


    public static void openContactMenu() {

    }


    public static void openGettingStartedMenu() {

    }


}
