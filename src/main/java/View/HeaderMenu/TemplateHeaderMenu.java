package View.HeaderMenu;


import Data.SymmetricDSM;
import IOHandler.SymmetricIOHandler;
import IOHandler.TemplateIOHandler;
import View.ConnectionSearchWidget;
import View.EditorPane;
import View.MatrixHandlers.SymmetricMatrixHandler;
import View.SideBarTools.SymmetricSideBar;
import Constants.Constants;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;

/**
 * Class to create the header of the gui. Includes menus like file, edit, and view
 *
 * @author Aiden Carney
 */
public abstract class TemplateHeaderMenu {
    protected static int defaultName = 0;

    protected final Menu fileMenu = new Menu("_File");
    protected final Menu newFileMenu = new Menu("New...");
    protected final MenuItem openMenu = new MenuItem("Open");
    protected final Menu importMenu = new Menu("Import...");
    protected final Menu exitMenu = new Menu("Exit");

    protected final Menu editMenu = new Menu("_Edit");
    protected final Menu viewMenu = new Menu("_View");
    protected final Menu toolsMenu = new Menu("_Tools");
    protected final Menu helpMenu = new Menu("_Help");

    protected final MenuBar menuBar = new MenuBar();
    protected EditorPane editor;
    protected ConnectionSearchWidget searchWidget;

    /**
     * Creates a new instance of the header menu and instantiate widgets on it
     *
     * @param editor        the EditorPane instance
     */
    public TemplateHeaderMenu(EditorPane editor) {
        this.editor = editor;
        this.searchWidget = new ConnectionSearchWidget(editor);

        setupFileMenu();
        setupNewFileMenu();
        setupOpenMenu();
        setupImportMenu();
        setupExitMenu();

        setupEditMenu();
        setUpToolsMenu();
        setupViewMenu();
        setupHelpMenu();

        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, toolsMenu, helpMenu);
    }


    /**
     * sets up the Menu object for the file menu
     */
    protected void setupFileMenu() {
        fileMenu.getItems().add(newFileMenu);
        fileMenu.getItems().add(openMenu);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(importMenu);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(exitMenu);
    }


    /**
     * Sets up the menu item for creating a new dsm
     */
    protected void setupNewFileMenu() {
        MenuItem newSymmetric = new MenuItem("Symmetric Matrix");
        newSymmetric.setOnAction(e -> {
            SymmetricDSM matrix = new SymmetricDSM();
            File file = new File("./untitled" + defaultName);
            while(file.exists()) {  // make sure file does not exist
                defaultName += 1;
                file = new File("./untitled" + defaultName);
            }

            this.editor.addTab(
                    matrix,
                    new SymmetricIOHandler(file),
                    new SymmetricMatrixHandler(matrix, 12.0),
                    this,
                    new SymmetricSideBar(matrix, editor
                    ));

            defaultName += 1;
        });
//        MenuItem newNonSymmetric = new MenuItem("Non-Symmetric Matrix");
//        newNonSymmetric.setOnAction(e -> {
//
//        });

        newFileMenu.getItems().addAll(newSymmetric);
    }

    /**
     * Sets up the menu for opening dsms
     */
    protected void setupOpenMenu() {
        openMenu.setOnAction( e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
            File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
            if(file == null) {
                return;
            } else if (this.editor.getMatrixController().getMatrixFileAbsoluteSavePaths().contains(file.getAbsolutePath())) {
                editor.focusTab(file);  // focus on that tab because it is already open
                return;
            }
            // make sure user did not just close out of the file chooser window
            switch (TemplateIOHandler.getFileDSMType(file)) {
                case "symmetric" -> {
                    SymmetricIOHandler ioHandler = new SymmetricIOHandler(file);
                    SymmetricDSM matrix = ioHandler.readFile();

                    if(matrix != null) {
                        this.editor.addTab(
                                matrix,
                                ioHandler,
                                new SymmetricMatrixHandler(matrix, 12.0),
                                this,
                                new SymmetricSideBar(matrix, editor
                                ));
                    } else {
                        // TODO: open window saying there was an error parsing the document
                        System.out.println("there was an error reading the file " + file);
                    }
                }
                default -> System.out.println("the type of dsm could not be determined from the file " + file.getAbsolutePath());
            }
        });
    }


    /**
     * Sets up the menu for importing dsms from alternative sources (ex. thebeau matlab files)
     */
    protected void setupImportMenu() {
        MenuItem importThebeau = new MenuItem("Thebeau Matlab File");
        importThebeau.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Matlab File", "*.m"));  // matlab is the only file type usable
            File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
            if(file != null) {  // make sure user did not just close out of the file chooser window
                SymmetricIOHandler ioHandler = new SymmetricIOHandler(file);
                SymmetricDSM matrix = ioHandler.importThebeauMatlabFile(file);
                if(matrix == null) {
                    // TODO: open window saying there was an error parsing the document
                    System.out.println("there was an error reading the file " + file);
                } else if(!this.editor.getMatrixController().getMatrixFileAbsoluteSavePaths().contains(file.getAbsolutePath())) {
                    File importedFile = new File(file.getParent(), file.getName().substring(0, file.getName().lastIndexOf('.')) + ".dsm");  // convert .m extension to .dsm
                    this.editor.addTab(
                            matrix,
                            new SymmetricIOHandler(importedFile),
                            new SymmetricMatrixHandler(matrix, 12.0),
                            this,
                            new SymmetricSideBar(matrix, editor
                            ));
                } else {
                    editor.focusTab(file);  // focus on that tab because it is already open
                }
            }
        });

        importMenu.getItems().add(importThebeau);
    }


    /**
     * Sets up the menu item to exit the application
     */
    protected void setupExitMenu() {
        exitMenu.setOnAction(e -> {
            menuBar.getScene().getWindow().fireEvent(
                    new WindowEvent(
                            menuBar.getScene().getWindow(),
                            WindowEvent.WINDOW_CLOSE_REQUEST
                    )
            );
        });
    }


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
        zoomIn.setOnAction(e -> editor.increaseFontScaling());
        MenuItem zoomOut = new MenuItem("Zoom Out");
        zoomOut.setOnAction(e -> editor.decreaseFontScaling());
        MenuItem zoomReset = new MenuItem("Reset Zoom");
        zoomReset.setOnAction(e -> editor.resetFontScaling());

        RadioMenuItem showNames = new RadioMenuItem("Show Connection Names");
        showNames.setSelected(true);
        showNames.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {  // TODO: this will not update to show connection names if no matrix is open
                return;
            }
            editor.getMatrixController().getMatrixHandler(editor.getFocusedMatrixUid()).setShowNames(showNames.isSelected());
            editor.getMatrixController().getMatrixHandler(editor.getFocusedMatrixUid()).refreshMatrixEditor();
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


    /**
     * @return  the menu bar object that has been set up
     */
    public MenuBar getMenuBar() {
        return this.menuBar;
    }


    /**
     * @return  the Hbox container holding the search widget
     */
    public HBox getConnectionSearchLayout() {
        return this.searchWidget.getMainLayout();
    }
}
