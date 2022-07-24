import javafx.embed.swing.JFXPanel;

public class Launcher {
    /**
     * starts the application
     *
     * @param args any command line args used by javafx (probably not used anywhere and will be ignored)
     */
    public static void main(String[] args) {
        // the following line is a hack to create a main class that wraps the application so that a single jar
        // file can be created that requires no runtime dependencies. Javafx requires initialization before creating any
        // components and this would occur in the class that extends application however, this happens out of order
        // with the creation of this wrapper class so a dummy variable is used which will call the javafx initialization.
        // Without the line of code a runtime error will occur when packaging application as a single jar
        final JFXPanel fxPanel = new JFXPanel();

        // start the application
        DSMApplication.main(args);
    }
}