import Data.MatrixController;
import Data.SymmetricDSM;
import Data.TemplateDSM;
import IOHandler.SymmetricIOHandler;
import View.EditorPane;
import View.HeaderMenu.SymmetricHeaderMenu;
import View.MatrixHandlers.SymmetricMatrixHandler;
import View.SideBarTools.SymmetricSideBar;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Main application class for the DSMEditor. Starts gui and manages exceptions
 *
 * @author Aiden Carney
 */
public class DSMApplication extends Application {
    private static final BorderPane root = new BorderPane();

    private static final EditorPane editor = new EditorPane(new MatrixController(), root);

    private static ArrayList<String> cliArgs = new ArrayList<>();

    /**
     * Starts the gui application
     *
     * @param primaryStage the Stage object which acts as the main window
     */
    @Override
    public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler(DSMApplication::handleError);


        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setTitle("DSM Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
        Platform.setImplicitExit(true);


        // start with a tab open (used for debugging, remove or comment out for release)
        if(cliArgs.contains("debug=true")) {
            SymmetricIOHandler ioHandler = new SymmetricIOHandler(new File("/home/aiden/Documents/DSMEditor/test2.dsm"));
            SymmetricDSM matrix = ioHandler.readFile();
            this.editor.addTab(
                    matrix,
                    ioHandler,
                    new SymmetricMatrixHandler(matrix, 12.0),
                    new SymmetricHeaderMenu(editor),
                    new SymmetricSideBar(matrix, editor)
            );
        }

        for(int i=0; i<cliArgs.size(); i++) {
            System.out.println(cliArgs.get(i));
        }


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

//        FreezeGrid.debug();

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
        System.err.println("Check the log file for information");
        System.err.println("Saving files to .recovery\n\n");
        System.err.println(e.getStackTrace());
        e.printStackTrace();

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
        for(Map.Entry<Integer, TemplateDSM> matrix : editor.getMatrixController().getMatrices().entrySet()) {
            File f = new File("./.recovery/" + editor.getMatrixController().getMatrixIOHandler(matrix.getKey()).getSavePath().getName());
            editor.getMatrixController().getMatrixIOHandler(matrix.getKey()).saveMatrixToFile(matrix.getValue(), f);
            matrix.getValue().setWasModified();  // matrix is not saved to known location, so don't display it as saved to the user
        }

    }


    /**
     * starts the application
     *
     * @param args any command line args used by javafx (probably not used anywhere and will be ignored)
     */
    public static void main(String[] args) {
        for(int i=0; i<args.length; i++) {
            cliArgs.add(args[i]);
        }

        launch(args);  // starts gui application
    }
}