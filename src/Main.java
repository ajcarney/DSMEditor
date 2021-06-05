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
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.time.Instant;
import java.util.Vector;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        IOHandler ioHandler = new IOHandler();
        TabView editor = new TabView(ioHandler);
        HeaderMenu menu = new HeaderMenu(ioHandler, editor);

        ioHandler.readFile(new File("C:/Users/ajcar/Documents/DSMEditor/test2.dsm"));

        BorderPane root = new BorderPane();
        root.setTop(menu.getMenuBar());
        root.setCenter(editor.getTabPane());

        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
//        String name = "Row x";
//        for(int i=0; i < 10; i++) {
//            int hash1 = name.hashCode();
//            int hash2 = Instant.now().toString().hashCode();
//            int uid = hash1 + hash2;
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println(uid);
//        }
        launch(args);  // starts gui application
    }
}