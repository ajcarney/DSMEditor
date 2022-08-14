package UI;

import Matrices.Data.AbstractDSMData;
import Matrices.Data.Entities.DSMConnection;
import Matrices.IDSM;
import Matrices.Views.AbstractMatrixView;
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
import java.util.HashSet;
import java.util.Set;


/**
 * Widget that allows users of the application to search for and highlight different connections in the DSM
 *
 * @author Aiden Carney
 */
public class ConnectionSearchWidget {
    private final HBox mainLayout = new HBox();

    private final TextField searchInput = new TextField();

    private final ToggleGroup tg = new ToggleGroup();
    private final RadioButton exactRadio = new RadioButton("Name - Exact Match");
    private final RadioButton containsRadio = new RadioButton("Name - Contains");
    private final RadioButton weightRadio = new RadioButton("Weight");

    private final IntegerProperty numResults = new SimpleIntegerProperty(0);
    private final Label numResultsLabel = new Label("0 Results");

    private final Button closeButton = new Button("Close");

    private Boolean isOpen = false;
    private final EditorPane editor;
    private final Thread searchHighlightThread;


    /**
     * Creates the object and formats all the widgets on the HBox pane
     *
     * @param editor    the EditorPane object to determine which matrix is open
     */
    public ConnectionSearchWidget(EditorPane editor) {
        this.editor = editor;
        close();  // default to hidden

        searchInput.setPromptText("Connection Name/Weight");
        searchInput.setMinWidth(Region.USE_PREF_SIZE);

        exactRadio.setSelected(true);

        exactRadio.setMinWidth(Region.USE_PREF_SIZE);
        containsRadio.setMinWidth(Region.USE_PREF_SIZE);
        weightRadio.setMinWidth(Region.USE_PREF_SIZE);

        exactRadio.setToggleGroup(tg);
        containsRadio.setToggleGroup(tg);
        weightRadio.setToggleGroup(tg);

        VBox searchTypeLayout = new VBox();
        searchTypeLayout.getChildren().addAll(exactRadio, containsRadio, weightRadio);

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

            AbstractMatrixView prevView = null;
            AbstractDSMData prevMatrix = null;

            while(true) {  // go through and update highlighting
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (editor.getFocusedMatrixUid() == null || !isOpen) {
                    continue;
                }

                final AbstractMatrixView view = this.editor.getFocusedMatrixView();
                final AbstractDSMData matrix = this.editor.getFocusedMatrixData();
                if(!matrix.equals(prevMatrix) || !view.equals(prevView)) {  // if matrix switched, reset the previous matches
                    prevMatches.clear();
                    prevMatrix = matrix;
                    prevView = view;
                }

                synchronized (view) {
                    matches = getMatches(searchInput.getText(), matrix);
                    Set<Pair<Integer, Integer>> prevAndCurrentErrors = new HashSet<>(prevMatches);
                    prevAndCurrentErrors.addAll(matches);

                    int numMatches = matches.size();  // update label text
                    ArrayList<Pair<Integer, Integer>> finalMatches = matches;
                    ArrayList<Pair<Integer, Integer>> finalPrevMatches = prevMatches;
                    Platform.runLater(() -> {
                        numResults.setValue(numMatches);

                        for (Pair<Integer, Integer> pair : prevAndCurrentErrors) {
                            if (!finalMatches.contains(pair) && finalPrevMatches.contains(pair)) {  // old match that no longer matches, unhighlight it
                                view.clearCellHighlight(view.getGridLocFromUids(pair), "search");
                            } else {
                                view.setCellHighlight(view.getGridLocFromUids(pair), AbstractMatrixView.SEARCH_BACKGROUND, "search");
                            }
                        }
                    });  // needs to be run on javafx thread or errors will occur

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
    private ArrayList<Pair<Integer, Integer>> getMatches(String text, AbstractDSMData matrix) {
        ArrayList<Pair<Integer, Integer>> matches = new ArrayList<>();  // find the connection cells to highlight
        if(tg.getSelectedToggle().equals(exactRadio)) {
            for(DSMConnection connection : matrix.getConnections()) {
                if(connection.getConnectionName().equals(text)) {
                    matches.add(new Pair<>(connection.getRowUid(), connection.getColUid()));
                }
            }
        } else if(tg.getSelectedToggle().equals(containsRadio)){
            for(DSMConnection connection : matrix.getConnections()) {
                if(connection.getConnectionName().contains(text)) {
                    matches.add(new Pair<>(connection.getRowUid(), connection.getColUid()));
                }
            }
        } else {
            double searchWeight;
            try {
                searchWeight = Double.parseDouble(text);
            } catch(NumberFormatException e) {
                return new ArrayList<>();
            }

            for(DSMConnection connection : matrix.getConnections()) {
                if(connection.getWeight() == searchWeight) {
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
        searchInput.setText("");

        for(IDSM matrix : this.editor.getMatricesCollection().getMatrices().values()) {  // clear all search highlight for all matrices for better flow when switching tabs
            for(AbstractMatrixView view : matrix.getMatrixEditorTab().getAllMatrixViews()) {
                view.clearAllCellsHighlight("search");
            }
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

