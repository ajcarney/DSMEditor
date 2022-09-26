/**
 * Class for starting the javafx application, needed so that maven shade plugin can add all
 * the necessary modules and dependencies
 */
public class Launcher {
    /**
     * starts the application.
     *
     * @param args any command line args used by javafx (probably not used anywhere and will be ignored)
     */
    public static void main(String[] args) {
        DSMApplication.main(args);  // start the application
    }
}