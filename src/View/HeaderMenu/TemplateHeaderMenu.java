package View.HeaderMenu;


import Data.MatricesData;
import View.ConnectionSearchWidget;
import View.EditorPane;
import constants.Constants;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Class to create the header of the gui. Includes menus like file, edit, and view
 *
 * @author Aiden Carney
 */
public abstract class TemplateHeaderMenu {
    protected static int defaultName = 0;

    protected final Menu fileMenu = new Menu("_File");
    protected final Menu editMenu = new Menu("_Edit");
    protected final Menu viewMenu = new Menu("_View");
    protected final Menu toolsMenu = new Menu("_Tools");
    protected final Menu helpMenu = new Menu("_Help");

    protected final MenuBar menuBar = new MenuBar();
    protected MatricesData matricesData;
    protected EditorPane editor;
    protected ConnectionSearchWidget searchWidget;

    /**
     * Creates a new instance of the header menu and instantiate widgets on it
     *
     * @param matricesData  the MatrixHandler instance
     * @param editor        the EditorPane instance
     */
    public TemplateHeaderMenu(MatricesData matricesData, EditorPane editor, ConnectionSearchWidget searchWidget) {
        this.matricesData = matricesData;
        this.editor = editor;
        this.searchWidget = searchWidget;

        setupFileMenu();
        setupEditMenu();
        setUpToolsMenu();
        setupViewMenu();
        setupHelpMenu();
    }


    /**
     * sets up the Menu object for the file menu
     */
    abstract protected void setupFileMenu();


    /**
     * sets up the Menu object for the edit menu
     */
    abstract protected void setupEditMenu();


    /**
     * sets up the Menu object for the tools menu
     */
    abstract protected void setUpToolsMenu();


    /**
     * sets up the Menu object for the view menu
     */
    protected void setupViewMenu() {
        MenuItem zoomIn = new MenuItem("Zoom In");
        zoomIn.setOnAction(e -> {
            editor.increaseFontScaling();
        });
        MenuItem zoomOut = new MenuItem("Zoom Out");
        zoomOut.setOnAction(e -> {
            editor.decreaseFontScaling();
        });
        MenuItem zoomReset = new MenuItem("Reset Zoom");
        zoomReset.setOnAction(e -> {
            editor.resetFontScaling();
        });

        RadioMenuItem showNames = new RadioMenuItem("Show Connection Names");
        showNames.setSelected(true);
        showNames.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {  // TODO: this will not update to show connection names if no matrix is open
                return;
            }
            matricesData.getMatrixHandler(editor.getFocusedMatrixUid()).setShowNames(showNames.isSelected());
            matricesData.getMatrixHandler(editor.getFocusedMatrixUid()).refreshMatrixEditor();
        });

        viewMenu.getItems().addAll(zoomIn, zoomOut, zoomReset);
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().addAll(showNames);
    }


    /**
     * sets up the Menu object for the help menu
     */
    protected void setupHelpMenu() {
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> {
            // bring up window asking to delete rows or columns
            Stage window = new Stage();
            window.setTitle("About");
            window.initModality(Modality.APPLICATION_MODAL);  // Block events to other windows

            VBox rootLayout = new VBox();
            rootLayout.setPadding(new Insets(10, 10, 10, 10));
            rootLayout.setSpacing(10);

            Label versionLabel = new Label("Version: " + Constants.version);

            rootLayout.getChildren().addAll(versionLabel);

            Scene scene = new Scene(rootLayout);
            window.setScene(scene);
            window.showAndWait();
        });

        helpMenu.getItems().addAll(about);
    }
}
