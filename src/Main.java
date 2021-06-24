import DSMData.DSMConnection;
import DSMData.DSMItem;
import DSMData.DataHandler;
import IOHandler.IOHandler;
import com.intellij.vcs.log.Hash;
import gui.HeaderMenu;
import gui.InfoHandler;
import gui.TabView;
import gui.ToolbarHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        IOHandler ioHandler = new IOHandler();
        InfoHandler infoHandler = new InfoHandler();
        TabView editor = new TabView(ioHandler, infoHandler);
        HeaderMenu menu = new HeaderMenu(ioHandler, editor);
        ToolbarHandler toolbarHandler = new ToolbarHandler(ioHandler, editor);

        BorderPane root = new BorderPane();
        root.setTop(menu.getMenuBar());
        root.setCenter(editor.getTabPane());
        root.setRight(infoHandler.getLayout());
        root.setLeft(toolbarHandler.getLayout());

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("DSM Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
        Platform.setImplicitExit(true);


        // start with a tab open
        File file = new File("C:\\Users\\ajcar\\Documents\\big matrix.dsm");
        DataHandler matrix = new DataHandler();
        matrix.setSymmetrical(true);
        int uid = ioHandler.addMatrix(matrix, new File("C:\\Users\\ajcar\\Documents\\big matrix.dsm"));
        for(int i=0; i<45; i++) {
            matrix.addNewSymmetricItem("test" + i);
        }
        for(DSMItem row : matrix.getRows()) {
            for(DSMItem col : matrix.getCols()) {
                matrix.modifyConnection(row.getUid(), col.getUid(), "x", 1.0);
            }
        }
        editor.addTab(uid);

        // on close, iterate through each tab and run the close request to save it or not
        scene.getWindow().setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent ev) {
                for (Map.Entry<Tab, Integer> entry : ((HashMap<Tab, Integer>) editor.getTabs().clone()).entrySet()) {  // iterate over clone
                    EventHandler<Event> handler = entry.getKey().getOnCloseRequest();
                    handler.handle(null);

                    if (editor.getTabs().get(entry.getKey()) != null) {
                        ev.consume();
                        return;
                    }
                }
                System.exit(0);  // terminate the program once the window is closed
            }
        });

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