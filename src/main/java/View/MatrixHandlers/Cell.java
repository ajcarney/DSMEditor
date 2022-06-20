package View.MatrixHandlers;

import javafx.geometry.Insets;
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

    protected Boolean crossHighlightEnabled = false;

    // dictionary of all the different highlight types that are supported
    protected HashMap<String, Background> highlightBGs = new HashMap<String, Background>() {{
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
     * @param gridLocation a pair of row, column of the location of the cell in the grid layout
     * @param guiCell      the HBox object of the cell
     */
    public Cell(Pair<Integer, Integer> gridLocation, HBox guiCell) {
        this.gridLocation = gridLocation;
        this.guiCell = guiCell;
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
        if(type.equals("default")) {
            return highlightBGs.get("default");
        } else if(type.equals("user")) {
            return highlightBGs.get("user");
        } else if(type.equals("cross")) {
            return highlightBGs.get("cross");
        } else if(type.equals("error")) {
            return highlightBGs.get("error");
        } else if(type.equals("symmetryError")) {
            return highlightBGs.get("symmetryError");
        } else if(type.equals("search")) {
            return highlightBGs.get("search");
        }

        return null;
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
        if(type.equals("default")) {
            highlightBGs.put("default", bg);
        } else if(type.equals("user")) {
            highlightBGs.put("user", bg);
        } else if(type.equals("cross")) {
            highlightBGs.put("cross", bg);
        } else if(type.equals("error")) {
            highlightBGs.put("error", bg);
        } else if(type.equals("symmetryError")) {
            highlightBGs.put("symmetryError", bg);
        } else if(type.equals("search")) {
            highlightBGs.put("search", bg);
        }
    }

}
