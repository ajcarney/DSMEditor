package Matrices.Views.Entities;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Pair;

import java.util.HashMap;


/**
 * Generic class to hold a data type that matrix views use to display the matrix elements along with storing
 * metadata for them
 */
public class Cell {
    protected Pair<Integer, Integer> gridLocation;
    protected HBox guiCell;
    protected Label label;

    protected DoubleProperty fontSize;
    protected ObjectProperty<Color> fontColor = new SimpleObjectProperty<>(Color.color(0.0, 0.0, 0.0));

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

        if(this.guiCell != null) {
            this.guiCell.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize));
        }
        if(this.label != null) {
            this.label.textFillProperty().bind(fontColor);
        }
    }


    /**
     * Sets the background color of a cell in the grid
     *
     * @param color the color to set the background to
     */
    public void setCellHighlight(Color color) {
        if(guiCell == null) return;
        guiCell.setBackground(new Background(new BackgroundFill(color, new CornerRadii(3), new Insets(0))));
    }


    /**
     * Sets the background color of a cell in the grid
     *
     * @param bg  the background object to set the background to
     */
    public void setCellHighlight(Background bg) {
        if(guiCell == null) return;
        guiCell.setBackground(bg);
    }


    /**
     * Sets the border for a cell
     * @param newBorder  type Border of the new border
     */
    public void setCellBorder(Border newBorder) {
        if(guiCell == null) return;
        guiCell.setBorder(newBorder);
    }


    /**
     * Sets the color of the border for a cell. Uses solid stroke style, no corner radii, and default width
     * @param color  the new color for the border
     */
    public void setCellBorder(Color color) {
        if(guiCell == null) return;
        guiCell.setBorder(new Border(new BorderStroke(color, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
    }


    /**
     * Sets the text color of a cell
     *
     * @param color  the new text color
     */
    public void setCellTextColor(Color color) {
        fontColor.set(color);
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
