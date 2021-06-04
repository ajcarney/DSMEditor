import DSMData.DSMConnection;
import IOHandler.IOHandler;
import gui.HeaderMenu;
import gui.TabView;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;import javafx.stage.Stage;

import java.util.Vector;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        IOHandler ioHandler = new IOHandler();
        TabView editor = new TabView(ioHandler);
        HeaderMenu menu = new HeaderMenu(ioHandler, editor);


        BorderPane root = new BorderPane();
        root.setTop(menu.getMenuBar());
        root.setCenter(editor.getTabPane());

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);  // starts gui application
    }
}