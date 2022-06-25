package View.MatrixHandlers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.HashMap;


/**
 * Generic class to hold a data type that the matrix gui handler uses to display the matrix elements along with storing
 * metadata for them
 */
public class Cell {
    protected Pair<Integer, Integer> gridLocation;
    protected HBox guiCell;
    protected Label label;

    protected DoubleProperty fontSize;
    protected StringProperty fontColorCss = new SimpleStringProperty("-fx-text-fill: rgb(0, 0, 0);");

    protected Boolean crossHighlightEnabled = false;

    // dictionary of all the different highlight types that are supported
    protected HashMap<String, Background> highlightBGs = new HashMap<>() {{
        put("default", null);
        put("user", null);
        put("cross", null);
        put("error", null);
        put("symmetryError", null);
        put("search", null);
    }};


    /**
     * Creates a new cell object
     *
     * @param gridLocation  a pair of row, column of the location of the cell in the grid layout
     * @param guiCell       the HBox object of the cell
     * @param label         the label object so that the font color of it can be updated if desired
     * @param fontSize      an observable double for the font size
     */
    public Cell(Pair<Integer, Integer> gridLocation, HBox guiCell, Label label, DoubleProperty fontSize) {
        this.gridLocation = gridLocation;
        this.guiCell = guiCell;
        this.fontSize = fontSize;
        this.label = label;

        this.guiCell.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize));
        if(this.label != null) {
            this.label.styleProperty().bind(fontColorCss);
        }
    }


    /**
     * Sets the background color of a cell in the grid
     *
     * @param color the color to set the background to
     */
    public void setCellHighlight(Color color) {
        guiCell.setBackground(new Background(new BackgroundFill(color, new CornerRadii(3), new Insets(0))));
    }


    /**
     * Sets the background color of a cell in the grid
     *
     * @param bg  the background object to set the background to
     */
    public void setCellHighlight(Background bg) {
        guiCell.setBackground(bg);
    }


    /**
     * Sets the text color of a cell
     *
     * @param color  the new text color
     */
    public void setCellTextColor(Color color) {
        fontColorCss.set("-fx-text-fill: rgb(" + 255 * color.getRed() + ", " + 255 * color.getGreen() + ", " + 255 * color.getBlue() + ");");
    }


    /**
     * Getter function for the crossHighlightEnabled field
     *
     * @return if cross highlighting is enabled
     */
    public Boolean getCrossHighlightEnabled() {
        return crossHighlightEnabled;
    }


    /**
     * Getter function for the gridLocation field of the class
     *
     * @return gridLocation of the instance
     */
    public Pair<Integer, Integer> getGridLocation() {
        return gridLocation;
    }


    /**
     * Getter function for the guiCell field of the class
     *
     * @return the guiCell of the instance
     */
    public HBox getGuiCell() {
        return guiCell;
    }


    /**
     * Getter function for background of type
     *
     * @param type which background to get the version of
     * @return     the background
     */
    public Background getHighlightBG(String type) {
        return switch (type) {
            case "default" -> highlightBGs.get("default");
            case "user" -> highlightBGs.get("user");
            case "cross" -> highlightBGs.get("cross");
            case "error" -> highlightBGs.get("error");
            case "symmetryError" -> highlightBGs.get("symmetryError");
            case "search" -> highlightBGs.get("search");
            default -> null;
        };

    }


    /**
     * Setter function for the crossHighlightEnabled field
     *
     * @param crossHighlightEnabled the new value for the crossHighlightField
     */
    public void setCrossHighlightEnabled(Boolean crossHighlightEnabled) {
        this.crossHighlightEnabled = crossHighlightEnabled;
    }


    /**
     * Setter function for backgrounds
     *
     * @param bg the new background of the cell
     */
    public void updateHighlightBG(Background bg, String type) {
        switch (type) {
            case "default" -> highlightBGs.put("default", bg);
            case "user" -> highlightBGs.put("user", bg);
            case "cross" -> highlightBGs.put("cross", bg);
            case "error" -> highlightBGs.put("error", bg);
            case "symmetryError" -> highlightBGs.put("symmetryError", bg);
            case "search" -> highlightBGs.put("search", bg);
        }
    }

}
