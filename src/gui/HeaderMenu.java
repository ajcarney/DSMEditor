package gui;

import DSMData.DataHandler;
import IOHandler.IOHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;

public class HeaderMenu {
    private static int defaultName = 0;

    private static Menu fileMenu;
    private static Menu editMenu;
    private static Menu viewMenu;

    private static MenuBar menuBar;
    private static IOHandler ioHandler;
    private static TabView editor;

    public HeaderMenu(IOHandler ioHandler, TabView editor) {
        menuBar = new MenuBar();
        this.ioHandler = ioHandler;
        this.editor = editor;

        //File menu
        fileMenu = new Menu("_File");

        Menu newFileMenu = new Menu("New...");

        MenuItem newSymmetric = new MenuItem("Symmetric Matrix");
        newSymmetric.setOnAction(e -> {
            DataHandler matrix = new DataHandler();
            matrix.setSymmetrical(true);
            File file = new File("./untitled" + Integer.toString(defaultName));
            while(file.exists()) {  // make sure file does not exist
                defaultName += 1;
                file = new File("./untitled" + Integer.toString(defaultName));
            }

            int uid = this.ioHandler.addMatrix(matrix, file);
            this.editor.addTab(uid);

            defaultName += 1;
        });
        MenuItem newNonSymmetric = new MenuItem("Non-Symmetric Matrix");
        newNonSymmetric.setOnAction(e -> {
            DataHandler matrix = new DataHandler();
            matrix.setSymmetrical(false);
            File file = new File("./untitled" + Integer.toString(defaultName));
            while(file.exists()) {  // make sure file does not exist
                defaultName += 1;
                file = new File("./untitled" + Integer.toString(defaultName));
            }

            int uid = this.ioHandler.addMatrix(matrix, file);
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
                DataHandler matrix = this.ioHandler.readFile(file);
                if(matrix == null) {
                    // TODO: open window saying there was an error parsing the document
                    System.out.println("there was an error reading the file " + file.toString());
                } else if(!ioHandler.getMatrixSaveNames().containsValue(file)) {
                    int uid = this.ioHandler.addMatrix(matrix, file);
                    this.editor.addTab(uid);
                } else {
                    editor.focusTab(file);  // focus on that tab because it is already open
                }
            }
        });

        MenuItem saveFile = new MenuItem("Save...");
        saveFile.setOnAction( e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            if(this.ioHandler.getMatrixSaveFile(editor.getFocusedMatrixUid()).getName().contains("untitled")) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("DSM File", "*.dsm"));  // dsm is the only file type usable
                File fileName = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
                if(fileName != null) {
                    this.ioHandler.setMatrixSaveFile(editor.getFocusedMatrixUid(), fileName);
                } else {  // user did not select a file, so do not save it
                    return;
                }
            }
            int code = this.ioHandler.saveMatrix(editor.getFocusedMatrixUid());  // TODO: add checking with the return code

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
                int code = this.ioHandler.saveMatrixToNewFile(editor.getFocusedMatrixUid(), fileName);
            }
        });

        Menu exportMenu = new Menu("Export");
        MenuItem exportCSV = new MenuItem("CSV File (.csv)");
        exportCSV.setOnAction(e -> {
            if(editor.getFocusedMatrixUid() == null) {
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV File", "*.csv"));  // dsm is the only file type usable
            File fileName = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
            if(fileName != null) {
                int code = this.ioHandler.exportMatrixToCSV(editor.getFocusedMatrixUid(), fileName);
            }
        });
        exportMenu.getItems().add(exportCSV);


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
        fileMenu.getItems().add(exportMenu);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(exit);


        //Edit menu
        editMenu = new Menu("_Edit");
        editMenu.setDisable(true);
        editMenu.getItems().add(new MenuItem("Cut"));
        editMenu.getItems().add(new MenuItem("Copy"));
        MenuItem paste = new MenuItem("Paste");
        editMenu.getItems().add(paste);


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

        viewMenu.getItems().addAll(zoomIn, zoomOut, zoomReset);
        


        menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu);
    }


    public MenuBar getMenuBar() {
        return menuBar;
    }

}
