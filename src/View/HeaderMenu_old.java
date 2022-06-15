package View;

import Data.SymmetricDSM;
import Data.MatrixHandler;
import IOHandler.ExportHandler;
import IOHandler.ImportHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import constants.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class to create the header of the gui. Includes file menu, edit menu, and view menu
 *
 * @author Aiden Carney
 */
public class HeaderMenu_old {
    private static int defaultName = 0;

    private static Menu fileMenu;
    private static Menu editMenu;
    private static Menu viewMenu;
    private static Menu toolsMenu;
    private static Menu helpMenu;

    private static MenuBar menuBar;
    private static MatrixHandler matrixHandler;
    private static EditorPane editor;
    private static ConnectionSearchWidget searchWidget;

    private Thread symmetryErrorHandlerThread;
    private Boolean runSymmetryValidationThread;


    /**
     * Creates a new instance of the header menu and instantiate widgets on it
     *
     * @param matrixHandler the MatrixHandler instance
     * @param editor    the EditorPane instance
     */
    public HeaderMenu_old(MatrixHandler matrixHandler, EditorPane editor, ConnectionSearchWidget searchWidget) {
        menuBar = new MenuBar();
        this.matrixHandler = matrixHandler;
        this.editor = editor;
        this.searchWidget = searchWidget;

    //File menu
        fileMenu = new Menu("_File");

        Menu newFileMenu = new Menu("New...");

        MenuItem newSymmetric = new MenuItem("Symmetric Matrix");
        newSymmetric.setOnAction(e -> {
            SymmetricDSM matrix = new SymmetricDSM();
            matrix.setSymmetrical(true);
            File file = new File("./untitled" + Integer.toString(defaultName));
            while(file.exists()) {  // make sure file does not exist
                defaultName += 1;
                file = new File("./untitled" + Integer.toString(defaultName));
            }

            int uid = this.matrixHandler.addMatrix(matrix, file);
            this.editor.addTab(uid);

            defaultName += 1;
        });
        MenuItem newNonSymmetric = new MenuItem("Non-Symmetric Matrix");
        newNonSymmetric.setOnAction(e -> {
            SymmetricDSM matrix = new SymmetricDSM();
            matrix.setSymmetrical(false);
            File file = new File("./untitled" + Integer.toString(defaultName));
            while(file.exists()) {  // make sure file does not exist
                defaultName += 1;
                file = new File("./untitled" + Integer.toString(defaultName));
            }

            int uid = this.matrixHandler.addMatrix(matrix, file);
            this.editor.addTab(uid);

            defaultName += 1;
        });

        newFileMenu.getItems().addAll(newSymmetric, newNonSymmetric);



        MenuItem openFile = new MenuItem("Open...");
        openFile.setOnAction( e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
            File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
            if(file != null) {  // make sure user did not just close out of the file chooser window
                SymmetricDSM matrix = ImportHandler.readFile(file);
                if(matrix == null) {
                    // TODO: open window saying there was an error parsing the document
                    System.out.println("there was an error reading the file " + file.toString());
                } else if(!matrixHandler.getMatrixSaveNames().containsValue(file)) {
                    int uid = this.matrixHandler.addMatrix(matrix, file);
                    this.editor.addTab(uid);
                } else {
                    editor.focusTab(file);  // focus on that tab because it is already open
                }
            }
        });

        Menu importMenu = new Menu("Import...");
        MenuItem importThebeau = new MenuItem("Thebeau Matlab File");
        importThebeau.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Matlab File", "*.m"));  // matlab is the only file type usable
            File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
            if(file != null) {  // make sure user did not just close out of the file chooser window
                SymmetricDSM matrix = ImportHandler.importThebeauMatlabFile(file);
                if(matrix == null) {
                    // TODO: open window saying there was an error parsing the document
                    System.out.println("there was an error reading the file " + file.toString());
                } else if(!matrixHandler.getMatrixSaveNames().containsValue(file)) {
                    File importedFile = new File(file.getParent(), file.getName().substring(0, file.getName().lastIndexOf('.')) + ".dsm");
                    int uid = this.matrixHandler.addMatrix(matrix, importedFile);
                    this.editor.addTab(uid);
                } else {
                    editor.focusTab(file);  // focus on that tab because it is already open
                }
            }
        });

        importMenu.getItems().add(importThebeau);

        MenuItem saveFile = new MenuItem("Save...");
        saveFile.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            if(this.matrixHandler.getMatrixSaveFile(editor.getFocusedMatrixUid()).getName().contains("untitled")) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
                File fileName = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
                if(fileName != null) {
                    this.matrixHandler.setMatrixSaveFile(editor.getFocusedMatrixUid(), fileName);
                } else {  // user did not select a file, so do not save it
                    return;
                }
            }
            int code = ExportHandler.saveMatrixToFile(matrixHandler.getMatrix(editor.getFocusedMatrixUid()), matrixHandler.getMatrixSaveFile(editor.getFocusedMatrixUid()));  // TODO: add checking with the return code

        });

        MenuItem saveFileAs = new MenuItem("Save As...");
        saveFileAs.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
            File fileName = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
            if(fileName != null) {
                int code = ExportHandler.saveMatrixToFile(matrixHandler.getMatrix(editor.getFocusedMatrixUid()), fileName);  // TODO: add checking with the return code
                matrixHandler.setMatrixSaveFile(editor.getFocusedMatrixUid(), fileName);
            }
        });

        Menu exportMenu = new Menu("Export");
        MenuItem exportCSV = new MenuItem("CSV File (.csv)");
        exportCSV.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            ExportHandler.promptExportToCSV(matrixHandler.getMatrix(editor.getFocusedMatrixUid()), menuBar.getScene().getWindow());
        });
        MenuItem exportXLSX = new MenuItem("Micro$oft Excel File (.xlsx)");
        exportXLSX.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            ExportHandler.promptExportToExcel(matrixHandler.getMatrix(editor.getFocusedMatrixUid()), menuBar.getScene().getWindow());
        });
        MenuItem exportThebeau = new MenuItem("Thebeau Matlab File (.m)");
        exportThebeau.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            ExportHandler.promptExportToThebeau(matrixHandler.getMatrix(editor.getFocusedMatrixUid()), menuBar.getScene().getWindow());
        });
        MenuItem exportImage = new MenuItem("PNG Image File (.png)");
        exportImage.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            ExportHandler.exportToImage(matrixHandler.getMatrix(editor.getFocusedMatrixUid()));
        });

        exportMenu.getItems().addAll(exportCSV, exportXLSX, exportThebeau, exportImage);


        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            menuBar.getScene().getWindow().fireEvent(
                    new WindowEvent(
                        menuBar.getScene().getWindow(),
                        WindowEvent.WINDOW_CLOSE_REQUEST
                    )
            );
        });

        fileMenu.getItems().add(newFileMenu);
        fileMenu.getItems().add(openFile);
        fileMenu.getItems().add(saveFile);
        fileMenu.getItems().add(saveFileAs);
//        fileMenu.getItems().add(new SeparatorMenuItem());
//        fileMenu.getItems().add(new MenuItem("Settings..."));
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(importMenu);
        fileMenu.getItems().add(exportMenu);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(exit);


    //Edit menu
        editMenu = new Menu("_Edit");

        MenuItem undo = new MenuItem("Undo");
        undo.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            matrixHandler.getMatrix(editor.getFocusedMatrixUid()).undoToCheckpoint();
            editor.refreshTab();
        });

        MenuItem redo = new MenuItem("Redo");
        redo.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            matrixHandler.getMatrix(editor.getFocusedMatrixUid()).redoToCheckpoint();
            editor.refreshTab();
        });

        MenuItem convertToNonSymmetrical = new MenuItem("Convert to Non-Symmetrical Matrix");
        convertToNonSymmetrical.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            // bring up window asking to delete rows or columns
            Stage window = new Stage();
            window.setTitle("Clear Rows or Columns?");
            window.initModality(Modality.APPLICATION_MODAL);  // Block events to other windows

            VBox rootLayout = new VBox();
            rootLayout.setPadding(new Insets(10, 10, 10, 10));
            rootLayout.setSpacing(10);

            Label title = new Label("Delete Matrix Rows or Columns?");

            // create radio buttons
            RadioButton deleteRows = new RadioButton("Rows");
            Pane hSpacer = new Pane();
            hSpacer.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(hSpacer, Priority.ALWAYS);
            RadioButton deleteColumns = new RadioButton("Columns");
            // create a toggle group
            ToggleGroup tg = new ToggleGroup();
            deleteRows.setToggleGroup(tg);
            deleteColumns.setToggleGroup(tg);
            deleteRows.setSelected(true);
            // add radio buttons to HBox
            HBox radioButtonLayout = new HBox();
            radioButtonLayout.getChildren().addAll(deleteRows, hSpacer, deleteColumns);

            Pane vSpacer = new Pane();
            vSpacer.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(vSpacer, Priority.ALWAYS);

            Button okButton = new Button("Ok");
            okButton.setOnAction(ee -> {
                boolean clearRows = false;
                if(deleteRows.isSelected()) {
                    clearRows = true;
                }
                ExportHandler.convertToNonSymmetrical(matrixHandler.getMatrix(editor.getFocusedMatrixUid()), clearRows);
                window.close();
                editor.refreshTab();
            });
            Pane spacer = new Pane();
            spacer.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button cancelButton = new Button("Cancel");
            cancelButton.setOnAction(ee -> {
                window.close();
            });
            HBox closeView = new HBox();
            closeView.getChildren().addAll(cancelButton, spacer, okButton);

            rootLayout.getChildren().addAll(title, radioButtonLayout, vSpacer, closeView);

            Scene scene = new Scene(rootLayout);
            window.setScene(scene);
            window.showAndWait();
        });

        MenuItem invert = new MenuItem("Invert Matrix");
        invert.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            matrixHandler.getMatrix(editor.getFocusedMatrixUid()).invertMatrix();
            matrixHandler.getMatrix(editor.getFocusedMatrixUid()).setCurrentStateAsCheckpoint();
            editor.refreshTab();
        });


        editMenu.setOnShown(e -> {  // disable validate symmetry for non-symmetrical matrices
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            undo.setDisable(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).canUndo());
            redo.setDisable(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).canRedo());
            convertToNonSymmetrical.setDisable(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical());
        });


        editMenu.getItems().add(undo);
        editMenu.getItems().add(redo);
        editMenu.getItems().add(new SeparatorMenuItem());
        editMenu.getItems().add(convertToNonSymmetrical);
        editMenu.getItems().add(invert);


    // View menu
        viewMenu = new Menu("_View");

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
            matrixHandler.getMatrixGuiHandler(editor.getFocusedMatrixUid()).setShowNames(showNames.isSelected());
            matrixHandler.getMatrixGuiHandler(editor.getFocusedMatrixUid()).refreshMatrixEditorMutable();
        });

        viewMenu.getItems().addAll(zoomIn, zoomOut, zoomReset);
        viewMenu.getItems().add(new SeparatorMenuItem());
        viewMenu.getItems().addAll(showNames);

        

    // tools menu
        runSymmetryValidationThread = false;
        toolsMenu = new Menu("_Tools");

        RadioMenuItem validateSymmetry = new RadioMenuItem("Validate Symmetry");
        validateSymmetry.setOnAction(e -> {
            runSymmetryValidationThread = validateSymmetry.isSelected();
        });

        symmetryErrorHandlerThread = new Thread(() -> {
            ArrayList<Pair<Integer, Integer>> errors = new ArrayList<>();
            ArrayList<Pair<Integer, Integer>> prevErrors = new ArrayList<>();

            while(true) {  // go through and update names
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (editor.getFocusedMatrixUid() == null || !matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical()) {
                    continue;
                }

                synchronized (matrixHandler.getMatrix(editor.getFocusedMatrixUid())) {  // TODO: maybe this synchronization call can be removed. Idk, i was too scared to check
                    if (editor.getFocusedMatrixUid() == null || !matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical()) {
                        continue;
                    }

                    MatrixGuiHandler m = matrixHandler.getMatrixGuiHandler(editor.getFocusedMatrixUid());

                    errors = matrixHandler.getMatrix(editor.getFocusedMatrixUid()).findSymmetryErrors();
                    Set<Pair<Integer, Integer>> prevAndCurrentErrors = prevErrors.stream().collect(Collectors.toSet());
                    prevAndCurrentErrors.addAll(errors);

                    if (!runSymmetryValidationThread) {
                        for (Pair<Integer, Integer> pair : prevAndCurrentErrors) {
                            m.clearCellHighlight(m.getGridLocFromUids(pair), "symmetryError");
                        }
                        continue;
                    } else {
                        for (Pair<Integer, Integer> pair : prevAndCurrentErrors) {
                            if (!errors.contains(pair) && prevErrors.contains(pair)) {  // old error that has been fixed, unhighlight it
                                m.clearCellHighlight(m.getGridLocFromUids(pair), "symmetryError");
                            } else {
                                m.setCellHighlight(m.getGridLocFromUids(pair), MatrixGuiHandler.SYMMETRY_ERROR_BACKGROUND, "symmetryError");
                            }
                        }
                    }

                    prevErrors = errors;

                }
            }
        });
        symmetryErrorHandlerThread.setDaemon(true);
        symmetryErrorHandlerThread.start();

        MenuItem search = new MenuItem("Find Connections");
        search.setOnAction(e -> {
            searchWidget.open();
        });


        MenuItem propagationAnalysis = new MenuItem("Propagation Analysis");
        propagationAnalysis.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }

            PropagationAnalysis p = new PropagationAnalysis(matrixHandler.getMatrix(editor.getFocusedMatrixUid()));
            p.start();
        });

        MenuItem coordinationScore = new MenuItem("Thebeau Cluster Analysis");
        coordinationScore.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }

            ClusterAnalysis c = new ClusterAnalysis(matrixHandler.getMatrix(editor.getFocusedMatrixUid()));
            c.start();
        });

        MenuItem thebeau = new MenuItem("Thebeau Algorithm");
        thebeau.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }

            ClusterAlgorithm c = new ClusterAlgorithm(matrixHandler.getMatrix(editor.getFocusedMatrixUid()));
            c.start();
        });

        toolsMenu.setOnShown(e -> {  // disable validate symmetry for non-symmetrical matrices
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            validateSymmetry.setDisable(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical());
            coordinationScore.setDisable(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical());
            thebeau.setDisable(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).isSymmetrical());
        });

        toolsMenu.getItems().addAll(validateSymmetry, search, propagationAnalysis, coordinationScore, thebeau);

    // help menu
        helpMenu = new Menu("_Help");

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


        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, toolsMenu, helpMenu);
    }


    /**
     * Returns the MenuBar so that it can be added to a layout
     *
     * @return the MenuBar object created by the constructor
     */
    public MenuBar getMenuBar() {
        return menuBar;
    }

}
