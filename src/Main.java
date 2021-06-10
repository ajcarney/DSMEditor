import DSMData.DSMConnection;
import IOHandler.IOHandler;
import gui.HeaderMenu;
import gui.InfoHandler;
import gui.TabView;
import gui.ToolbarHandler;
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
        InfoHandler infoHandler = new InfoHandler();
        TabView editor = new TabView(ioHandler, infoHandler);
        HeaderMenu menu = new HeaderMenu(ioHandler, editor);
        ToolbarHandler toolbarHandler = new ToolbarHandler(editor);

        BorderPane root = new BorderPane();
        root.setTop(menu.getMenuBar());
        root.setCenter(editor.getTabPane());
        root.setRight(infoHandler.getLayout());
        root.setLeft(toolbarHandler.getLayout());

        Scene scene = new Scene(root, 1000, 600);

        primaryStage.setTitle("DSM Editor");
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