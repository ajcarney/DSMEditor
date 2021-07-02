import DSMData.DataHandler;
import IOHandler.IOHandler;
import gui.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Main application class for the DSMEditor. Starts gui and manages exceptions
 *
 * @author Aiden Carney
 */
public class Main extends Application {

    private static final IOHandler ioHandler = new IOHandler();
    private static final InfoHandler infoHandler = new InfoHandler();
    private static final TabView editor = new TabView(ioHandler, infoHandler);
    private static final ConnectionSearchWidget searchWidget = new ConnectionSearchWidget(ioHandler, editor);
    private static final HeaderMenu menu = new HeaderMenu(ioHandler, editor, searchWidget);
    private static final ToolbarHandler toolbarHandler = new ToolbarHandler(ioHandler, editor);


    /**
     * Starts the gui application
     *
     * @param primaryStage the Stage object which acts as the main window
     */
    @Override
    public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler(Main::handleError);

        BorderPane root = new BorderPane();
        root.setTop(menu.getMenuBar());
        root.setCenter(editor.getTabPane());
        root.setRight(infoHandler.getLayout());
        root.setLeft(toolbarHandler.getLayout());
        root.setBottom(searchWidget.getMainLayout());

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("DSM Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
        Platform.setImplicitExit(true);


        // start with a tab open (used for debugging, remove or comment out for release)
//        File file = new File("C:\\Users\\ajcar\\Documents\\big matrix.dsm");
//        DataHandler matrix = new DataHandler();
//        matrix.setSymmetrical(true);
//        int uid = ioHandler.addMatrix(matrix, new File("C:\\Users\\ajcar\\Documents\\big matrix.dsm"));
//        for(int i=0; i<45; i++) {
//            matrix.addNewSymmetricItem("test" + i);
//        }
//        for(DSMItem row : matrix.getRows()) {
//            for(DSMItem col : matrix.getCols()) {
//                matrix.modifyConnection(row.getUid(), col.getUid(), "x", 1.0);
//            }
//        }
//        editor.addTab(uid);

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


    /**
     * Default error handler. Called by java whenever there is an error that is not handled. Prints a message
     * to the terminal, logs the info about the error including the stacktrace,
     * and saves all open files to a recovery spot in case it is a bad error
     *
     * @param t the current thread
     * @param e the unhandled error
     */
    private static void handleError(Thread t, Throwable e) {
        System.err.println("***An unhandled exception was thrown***");
        System.err.println("An unexpected error occurred in " + t);
        System.err.println(e.getMessage());
        System.err.println("Check the log file for more information");
        System.err.println("Saving files to .recovery");

        File logDir = new File("./.log");
        File logFile = new File("./.log/log");
        if(!logDir.exists()) logDir.mkdir();

       try {
           Writer w = new FileWriter(logFile, true);  // open file in append mode

           Date date = new Date();  // write time stamp
           SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
           String formattedDate = sdf.format(date);
           w.write("[" + formattedDate + "]\n");
           w.write(e.getMessage() + "\n");

           StringWriter sw = new StringWriter();
           e.printStackTrace(new PrintWriter(sw));
           w.write(sw.toString() + "\n");

           w.close();
       } catch (IOException ioException) {
           ioException.printStackTrace();
       }

       File recoveryDir = new File("./.recovery");
       if(!recoveryDir.exists()) recoveryDir.mkdir();
       for(Map.Entry<Integer, DataHandler> matrix : ioHandler.getMatrices().entrySet()) {
           File f = new File("./.recovery/" + ioHandler.getMatrixSaveFile(matrix.getKey()).getName());
           ioHandler.saveMatrixToFile(matrix.getKey(), f);
       }

    }


    /**
     * starts the application
     *
     * @param args any command line args used by javafx (probably not used anywhere and will be ignored)
     */
    public static void main(String[] args) {
        launch(args);  // starts gui application
    }
}