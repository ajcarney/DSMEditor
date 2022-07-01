package View.HeaderMenu;

import Data.AsymmetricDSM;
import View.EditorPane;
import View.MatrixViews.AsymmetricView;
import View.PropagationAnalysisWindow;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.FileChooser;

import java.io.File;


/**
 * Class to create the header of the gui for a symmetric matrix.
 *
 * @author Aiden Carney
 */
public class AsymmetricHeaderMenu extends TemplateHeaderMenu {

    /**
     * Creates a new instance of the header menu and instantiate widgets on it
     *
     * @param editor        the EditorPane instance
     */
    public AsymmetricHeaderMenu(EditorPane editor) {
        super(editor);

        // methods to set up buttons are already called
    }


    /**
     * sets up the Menu object for the file menu
     */
    @Override
    protected void setupFileMenu() {
        MenuItem saveFile = new MenuItem("Save...");
        saveFile.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            if(this.editor.getMatrixController().getMatrixIOHandler(editor.getFocusedMatrixUid()).getSavePath().getAbsolutePath().contains("untitled")) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
                File fileName = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
                if(fileName != null) {
                    this.editor.getMatrixController().getMatrixIOHandler(editor.getFocusedMatrixUid()).setSavePath(fileName);
                } else {  // user did not select a file, so do not save it
                    return;
                }
            }
            int matrixUid = editor.getFocusedMatrixUid();
            AsymmetricDSM matrix = (AsymmetricDSM) this.editor.getFocusedMatrix();
            int code = this.editor.getMatrixController().getMatrixIOHandler(matrixUid).saveMatrixToFile(matrix, this.editor.getMatrixController().getMatrixIOHandler(editor.getFocusedMatrixUid()).getSavePath());  // TODO: add checking with the return code
        });

        MenuItem saveFileAs = new MenuItem("Save As...");
        saveFileAs.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
            File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
            if(file != null) {
                int matrixUid = editor.getFocusedMatrixUid();
                AsymmetricDSM matrix = (AsymmetricDSM) this.editor.getFocusedMatrix();
                int code = this.editor.getMatrixController().getMatrixIOHandler(matrixUid).saveMatrixToFile(matrix, file);  // TODO: add checking with the return code

                this.editor.getMatrixController().getMatrixIOHandler(matrixUid).setSavePath(file);
            }
        });

        Menu exportMenu = new Menu("Export");
        MenuItem exportCSV = new MenuItem("CSV File (.csv)");
        exportCSV.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            int matrixUid = editor.getFocusedMatrixUid();
            AsymmetricDSM matrix = (AsymmetricDSM) this.editor.getFocusedMatrix();
            this.editor.getMatrixController().getMatrixIOHandler(matrixUid).promptExportToCSV(matrix, menuBar.getScene().getWindow());
        });
        MenuItem exportXLSX = new MenuItem("Micro$oft Excel File (.xlsx)");
        exportXLSX.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            int matrixUid = editor.getFocusedMatrixUid();
            AsymmetricDSM matrix = (AsymmetricDSM) this.editor.getFocusedMatrix();
            this.editor.getMatrixController().getMatrixIOHandler(matrixUid).promptExportToExcel(matrix, menuBar.getScene().getWindow());
        });
        MenuItem exportImage = new MenuItem("PNG Image File (.png)");
        exportImage.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            int matrixUid = editor.getFocusedMatrixUid();
            AsymmetricDSM matrix = (AsymmetricDSM) this.editor.getFocusedMatrix();
            this.editor.getMatrixController().getMatrixIOHandler(matrixUid).exportToImage(matrix, new AsymmetricView(matrix, 12.0));
        });

        exportMenu.getItems().addAll(exportCSV, exportXLSX, exportImage);

        fileMenu.getItems().add(newFileMenu);
        fileMenu.getItems().add(openMenu);
        fileMenu.getItems().add(saveFile);
        fileMenu.getItems().add(saveFileAs);
//        fileMenu.getItems().add(new SeparatorMenuItem());
//        fileMenu.getItems().add(new MenuItem("Settings..."));
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(importMenu);
        fileMenu.getItems().add(exportMenu);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(exitMenu);
    }


    /**
     * sets up the Menu object for the edit menu
     */
    @Override
    protected void setupEditMenu() {
        MenuItem undo = new MenuItem("Undo");
        undo.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            this.editor.getFocusedMatrix().undoToCheckpoint();
            editor.refreshTab();
        });

        MenuItem redo = new MenuItem("Redo");
        redo.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            this.editor.getFocusedMatrix().redoToCheckpoint();
            editor.refreshTab();
        });


        MenuItem invert = new MenuItem("Invert Matrix");
        invert.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            this.editor.getFocusedMatrix().invertMatrix();
            this.editor.getFocusedMatrix().setCurrentStateAsCheckpoint();
            editor.refreshTab();
        });


        editMenu.setOnShown(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            undo.setDisable(!this.editor.getFocusedMatrix().canUndo());
            redo.setDisable(!this.editor.getFocusedMatrix().canRedo());
        });


        editMenu.getItems().add(undo);
        editMenu.getItems().add(redo);
        editMenu.getItems().add(new SeparatorMenuItem());
        editMenu.getItems().add(invert);
    }


    /**
     * sets up the Menu object for the tools menu
     */
    @Override
    protected void setUpToolsMenu() {
        MenuItem search = new MenuItem("Find Connections");
        search.setOnAction(e -> searchWidget.open());


        MenuItem propagationAnalysis = new MenuItem("Propagation Analysis");
        propagationAnalysis.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }

            PropagationAnalysisWindow p = new PropagationAnalysisWindow(this.editor.getFocusedMatrix());
            p.start();
        });


        toolsMenu.getItems().addAll(search, propagationAnalysis);

    }
}
