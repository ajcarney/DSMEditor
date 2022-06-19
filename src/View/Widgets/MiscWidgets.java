package View.Widgets;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


/**
 * A class that contains miscellaneous widgets that do not have many functions
 */
public class MiscWidgets {
    /**
     * Creates a JavaFx Pane that grows vertically to fill up remaining space in a window
     *
     * @return the Pane object that grows vertically
     */
    public static Pane getVerticalSpacer() {
        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        spacer.setMaxHeight(Double.MAX_VALUE);

        return spacer;
    }


    /**
     * Creates a JavaFx Pane that grows horizontally to fill up remaining space in a window
     *
     * @return the Pane object that grows vertically
     */
    public static Pane getHorizontalSpacer() {
        Pane spacer = new Pane();  // used as a spacer between buttons
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMaxWidth(Double.MAX_VALUE);

        return spacer;
    }

}
