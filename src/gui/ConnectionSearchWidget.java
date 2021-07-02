package gui;

import DSMData.DSMConnection;
import IOHandler.IOHandler;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Widget that allows users of the application to search for and highlight different connections in the DSM
 * TODO: this should probably be a singleton class
 *
 * @author Aiden Carney
 */
public class ConnectionSearchWidget {
    private HBox mainLayout = new HBox();

    private TextField searchInput = new TextField();

    private ToggleGroup tg = new ToggleGroup();
    private RadioButton exactRadio = new RadioButton("Exact Match");
    private RadioButton containsRadio = new RadioButton("Contains");

    private IntegerProperty numResults = new SimpleIntegerProperty(0);
    private Label numResultsLabel = new Label("0 Results");

    private Button closeButton = new Button("Close");

    private Boolean isOpen = false;
    private static IOHandler ioHandler;
    private static TabView editor;
    private Thread searchHighlightThread;


    /**
     * Creates the object and formats all the widgets on the HBox pane
     *
     * @param ioHandler the IOHandler object to access the gui handler to highlight cells
     * @param editor    the TabView object to determine which matrix is open
     */
    public ConnectionSearchWidget(IOHandler ioHandler, TabView editor) {
        this.ioHandler = ioHandler;
        this.editor = editor;
        close();  // default to hidden

        searchInput.setPromptText("Connection Name");
        searchInput.setMinWidth(Region.USE_PREF_SIZE);

        exactRadio.setSelected(true);
        exactRadio.setMinWidth(Region.USE_PREF_SIZE);
        containsRadio.setMinWidth(Region.USE_PREF_SIZE);
        exactRadio.setToggleGroup(tg);
        containsRadio.setToggleGroup(tg);
        VBox searchTypeLayout = new VBox();
        searchTypeLayout.getChildren().addAll(exactRadio, containsRadio);

        numResultsLabel.textProperty().bind(
            Bindings.createStringBinding(() -> {
                if (numResults.getValue() == null) {
                    return "0 Results";
                } else {
                    return String.format("%d Results", numResults.getValue());
                }
            }, numResults)
        );

        closeButton.setOnAction(e -> {
            close();
        });

        mainLayout.getChildren().addAll(searchInput, searchTypeLayout, numResultsLabel, closeButton);
        mainLayout.setSpacing(15);
        mainLayout.setPadding(new Insets(10));
        mainLayout.setAlignment(Pos.CENTER_LEFT);

        searchHighlightThread = new Thread(() -> {
            ArrayList<Pair<Integer, Integer>> matches = new ArrayList<>();
            ArrayList<Pair<Integer, Integer>> prevMatches = new ArrayList<>();

            while(true) {  // go through and update highlighting
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (editor.getFocusedMatrixUid() == null || !isOpen) {
                    continue;
                }

                synchronized (ioHandler.getMatrix(editor.getFocusedMatrixUid())) {  // TODO: maybe this synchronization call can be removed. Idk, i was too scared to check
                    MatrixGuiHandler m = ioHandler.getMatrixGuiHandler(editor.getFocusedMatrixUid());

                    matches = getMatches(searchInput.getText());
                    Set<Pair<Integer, Integer>> prevAndCurrentErrors = prevMatches.stream().collect(Collectors.toSet());
                    prevAndCurrentErrors.addAll(matches);

                    int numMatches = matches.size();  // update label text
                    Platform.runLater(() -> numResults.setValue(numMatches));  // needs to be run on javafx thread or errors will occur

                    for (Pair<Integer, Integer> pair : prevAndCurrentErrors) {
                        if (!matches.contains(pair) && prevMatches.contains(pair)) {  // old match that no longer matches, unhighlight it
                            m.clearCellHighlight(m.getGridLocFromUids(pair), "search");
                        } else {
                            m.setCellHighlight(m.getGridLocFromUids(pair), MatrixGuiHandler.SEARCH_BACKGROUND, "search");
                        }
                    }


                    prevMatches = matches;

                }
            }
        });
        searchHighlightThread.setDaemon(true);
        searchHighlightThread.start();
    }


    /**
     * Finds the connection cells with text that matches or contains the search text
     *
     * @param text the text to search for
     * @return     ArrayList of Pair row uid, column uid of all the matches
     */
    private ArrayList<Pair<Integer, Integer>> getMatches(String text) {
        ArrayList<Pair<Integer, Integer>> matches = new ArrayList<>();  // find the connection cells to highlight
        if(tg.getSelectedToggle().equals(exactRadio)) {
            for(DSMConnection connection : ioHandler.getMatrix(editor.getFocusedMatrixUid()).getConnections()) {
                if(connection.getConnectionName().equals(text)) {
                    matches.add(new Pair<>(connection.getRowUid(), connection.getColUid()));
                }
            }
        } else {
            for(DSMConnection connection : ioHandler.getMatrix(editor.getFocusedMatrixUid()).getConnections()) {
                if(connection.getConnectionName().contains(text)) {
                    matches.add(new Pair<>(connection.getRowUid(), connection.getColUid()));
                }
            }
        }

        return matches;
    }


    /**
     * sets the pane to be invisible, clears highlighting, and stops the highlighting thread
     */
    public void close() {
        isOpen = false;
        mainLayout.setVisible(false);
        mainLayout.setManaged(false);  // so that the layout will not take up space on the application

        for(MatrixGuiHandler m : ioHandler.getMatrixGuiHandlers().values()) {  // clear all search highlight for all matrices for better flow when switching tabs
            m.clearAllCellsHighlight("search");
        }
    }


    /**
     * sets the pane to be visible and restarts the highlighting thread
     */
    public void open() {
        isOpen = true;
        mainLayout.setVisible(true);
        mainLayout.setManaged(true);
    }


    /**
     * returns the main layout that contains all the widgets
     *
     * @return HBox of the main layout
     */
    public HBox getMainLayout() {
        return mainLayout;
    }

}

