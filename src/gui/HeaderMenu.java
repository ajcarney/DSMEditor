package gui;

import DSMData.DataHandler;
import IOHandler.IOHandler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HeaderMenu {
    private static int defaultName = 0;

    private static Menu fileMenu;
    private static Menu editMenu;

    private static MenuBar menuBar;
    private static IOHandler ioHandler;
    private static TabView tabView;

    public HeaderMenu(IOHandler ioHandler, TabView tabView) {
        menuBar = new MenuBar();
        this.ioHandler = ioHandler;
        this.tabView = tabView;

        //File menu
        fileMenu = new Menu("File");
        MenuItem newFile = new MenuItem("New...");
        newFile.setOnAction(e -> {
            DataHandler matrix = new DataHandler();
            int uid = this.ioHandler.addMatrix(matrix, ".\\untitled" + Integer.toString(defaultName));  // TODO: add checking to make sure this file does not exist
            this.tabView.addTab(uid);
            defaultName += 1;
        });

        MenuItem openFile = new MenuItem("Open...");
        openFile.setOnAction( e -> {
            String fileName = "";
            // TODO: open new window asking for what file to open
            DataHandler matrix = this.ioHandler.readFile(fileName);
            int uid = this.ioHandler.addMatrix(matrix, fileName);
            this.tabView.addTab(uid);
        });

        MenuItem saveFile = new MenuItem("Save...");
        saveFile.setOnAction( e -> {
            if(this.ioHandler.getMatrixSaveFile(tabView.getFocusedMatrixUid()).contains("untitled")) {
                // TODO: open new window asking for a file to save to when it is only a default value
                String fileName = "untitled";
            }
            int code = this.ioHandler.saveMatrixToFile(tabView.getFocusedMatrixUid());  // TODO: add checking with the return code
            // tell tabView to refresh names
        });

        MenuItem saveFileAs = new MenuItem("Save As...");
        saveFileAs.setOnAction( e -> {
            String fileName = "";
            // TODO: open new window asking for what file to save to
            int code = this.ioHandler.saveMatrixToNewFile(tabView.getFocusedMatrixUid(), fileName);
            // tell tabView to refresh names

        });

        fileMenu.getItems().add(newFile);
        fileMenu.getItems().add(openFile);
        fileMenu.getItems().add(saveFile);
        fileMenu.getItems().add(saveFileAs);
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(new MenuItem("Settings..."));
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(new MenuItem("Exit"));


        //Edit menu
        editMenu = new Menu("_Edit");
        editMenu.getItems().add(new MenuItem("Cut"));
        editMenu.getItems().add(new MenuItem("Copy"));
        MenuItem paste = new MenuItem("Paste");
        paste.setOnAction(e -> System.out.println("Pasting some crap"));
        paste.setDisable(true);
        editMenu.getItems().add(paste);

        menuBar.getMenus().addAll(fileMenu, editMenu);
    }


    public static MenuBar getMenuBar() {
        return menuBar;
    }

}
