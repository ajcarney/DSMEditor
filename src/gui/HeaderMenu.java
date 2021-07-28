package gui;

import DSMData.DSMData;
import DSMData.MatrixHandler;
import IOHandler.ExportHandler;
import IOHandler.ImportHandler;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class to create the header of the gui. Includes file menu, edit menu, and view menu
 *
 * @author Aiden Carney
 */
public class HeaderMenu {
    private static int defaultName = 0;

    private static Menu fileMenu;
    private static Menu editMenu;
    private static Menu viewMenu;
    private static Menu toolsMenu;

    private static MenuBar menuBar;
    private static MatrixHandler matrixHandler;
    private static TabView editor;
    private static ConnectionSearchWidget searchWidget;

    private Thread symmetryErrorHandlerThread;
    private Boolean runSymmetryValidationThread;


    /**
     * Creates a new instance of the header menu and instantiate widgets on it
     *
     * @param matrixHandler the MatrixHandler instance
     * @param editor    the TabView instance
     */
    public HeaderMenu(MatrixHandler matrixHandler, TabView editor, ConnectionSearchWidget searchWidget) {
        menuBar = new MenuBar();
        this.matrixHandler = matrixHandler;
        this.editor = editor;
        this.searchWidget = searchWidget;

    //File menu
        fileMenu = new Menu("_File");

        Menu newFileMenu = new Menu("New...");

        MenuItem newSymmetric = new MenuItem("Symmetric Matrix");
        newSymmetric.setOnAction(e -> {
            DSMData matrix = new DSMData();
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
            DSMData matrix = new DSMData();
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
                DSMData matrix = ImportHandler.readFile(file);
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
                DSMData matrix = ImportHandler.importThebeauMatlabFile(file);
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

        exportMenu.getItems().addAll(exportCSV, exportXLSX, exportThebeau);


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

        editMenu.setOnShown(e -> {  // disable validate symmetry for non-symmetrical matrices
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            undo.setDisable(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).canUndo());
            redo.setDisable(!matrixHandler.getMatrix(editor.getFocusedMatrixUid()).canRedo());
        });


        editMenu.getItems().add(undo);
        editMenu.getItems().add(redo);


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
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            matrixHandler.getMatrixGuiHandler(editor.getFocusedMatrixUid()).setShowNames(showNames.isSelected());
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


        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, toolsMenu);
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
