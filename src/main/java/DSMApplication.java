import Matrices.*;
import UI.EditorPane;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Main application class for the DSMEditor. Starts gui and manages exceptions
 *
 * @author Aiden Carney
 */
public class DSMApplication extends Application {
    private static EditorPane editor;
    private static ArrayList<String> cliArgs;

    /**
     * Starts the gui application
     *
     * @param primaryStage the Stage object which acts as the main window
     */
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setTitle("DSM Editor");
        primaryStage.setScene(scene);
        primaryStage.show();
        Platform.setImplicitExit(true);

        editor = new EditorPane(new MatricesCollection(), root);

        Thread.setDefaultUncaughtExceptionHandler(DSMApplication::handleError);

        // start with a tab open (used for debugging, remove or comment out for release)
        if(cliArgs.contains("debug=true")) {
//            File f = new File("/home/aiden/Documents/DSMEditor/dsms/defect_group_text_color.dsm");
//            if(f.exists()) {
//                editor.addTab(new SymmetricDSM(f));
//            }
            Constants.Constants.isDebug = true;
            File f = new File("/home/aiden/Documents/DSMEditor/input_nonsymmetric_Test_Propagation_analysis.dsm");
            if(f.exists()) {
                editor.addTab(new AsymmetricDSM(f));
            }
        }

        for (String cliArg : cliArgs) {
            System.out.println(cliArg);
        }


        // on close, iterate through each tab and run the close request to save it or not
        scene.getWindow().setOnCloseRequest(ev -> {
            for (Map.Entry<Tab, Integer> entry : ((HashMap<Tab, Integer>) editor.getTabs().clone()).entrySet()) {  // iterate over clone
                EventHandler<Event> handler = entry.getKey().getOnCloseRequest();
                handler.handle(null);

                if (editor.getTabs().get(entry.getKey()) != null) {  // this is certainly suspicious but don't touch unless you know what you are doing
                    ev.consume();
                    return;
                }
            }
            System.exit(0);  // terminate the program once the window is closed
        });

        //FreezeGrid.debug2();
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
        System.err.println(Arrays.toString(e.getStackTrace()));
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
            w.write(sw + "\n");

            w.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        File recoveryDir = new File("./.recovery");
        boolean exists = recoveryDir.exists();
        if(!exists)  {
            exists = recoveryDir.mkdir();
        }
        if(exists) {  // make sure recovery directory exists before saving there
            for (Map.Entry<Integer, IDSM> matrix : editor.getMatricesCollection().getMatrices().entrySet()) {
                File f = new File("./.recovery/" + matrix.getValue().getMatrixIOHandler().getSavePath().getName());
                matrix.getValue().getMatrixIOHandler().saveMatrixToFile(f);
                matrix.getValue().getMatrixData().setWasModified();  // matrix is not saved to known location, so don't display it as saved to the user
            }
        }

    }


    /**
     * starts the application
     *
     * @param args any command line args used by javafx (probably not used anywhere and will be ignored)
     */
    public static void main(String[] args) {
        cliArgs = new ArrayList<>();
        cliArgs.addAll(Arrays.asList(args));

        launch(args);  // starts gui application
    }
}