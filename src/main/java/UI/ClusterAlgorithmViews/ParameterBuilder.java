package UI.ClusterAlgorithmViews;

import UI.Widgets.NumericTextField;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * Class with builder pattern to be able to create views for cluster algorithms with less code
 */
public class ParameterBuilder {

    private final VBox parametersPane;

    /**
     * Constructor. Initializes the main pane that holds all other parameters
     */
    public ParameterBuilder() {
        parametersPane = new VBox();
        parametersPane.setSpacing(15);
        parametersPane.setAlignment(Pos.TOP_CENTER);
        parametersPane.setMinWidth(Region.USE_PREF_SIZE);
    }


    /**
     * Adds a new numeric window to the pane
     * @param nProperty the double property to write the value to
     * @param label the text to display next to the entry
     * @param tooltip a help menu to display on hover
     * @param intOnly if true only allow integers to be entered
     * @return current instance to allow for builder pattern
     */
    public ParameterBuilder newNumericEntry(DoubleProperty nProperty, String label, String tooltip, boolean intOnly) {
        VBox layout = new VBox();

        Label l = new Label(label);
        l.setTooltip(new Tooltip(tooltip));


        NumericTextField entry = new NumericTextField(nProperty.getValue(), intOnly);
        entry.textProperty().addListener((obs, oldText, newText) -> {
            nProperty.setValue(entry.getNumericValue());
        });

        layout.getChildren().addAll(l, entry);
        layout.setSpacing(5);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        parametersPane.getChildren().add(layout);

        return this;
    }


    /**
     * Adds a new checkbox to the pane
     * @param bProperty the boolean property to write the input to
     * @param label the text to display next to the checkbox
     * @return current instance to allow for builder pattern
     */
    public ParameterBuilder newCheckbox(BooleanProperty bProperty, String label) {
        VBox layout = new VBox();
        layout.setSpacing(10);

        CheckBox checkbox = new CheckBox(label);
        checkbox.setSelected(bProperty.get());
        checkbox.setMaxWidth(Double.MAX_VALUE);
        checkbox.selectedProperty().addListener((obs, oldText, newText) -> {
            bProperty.setValue(checkbox.isSelected());
        });

        layout.getChildren().addAll(checkbox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(10));

        parametersPane.getChildren().add(layout);

        return this;
    }


    /**
     * Adds a new label to the pane
     * @param text the text to display
     * @param fontSize the font size
     * @return current instance to allow for builder pattern
     */
    public ParameterBuilder newLabel(String text, double fontSize) {
        Label l = new Label(text);
        l.setFont(new Font(fontSize));
        l.setPadding(new Insets(5));
        l.setWrapText(true);
        l.setMinHeight(l.getMinHeight());


        parametersPane.getChildren().add(l);

        return this;
    }


    /**
     * Final function call of the builder pattern
     * @return the pane that holds all the elements
     */
    public VBox build() {
        return parametersPane;
    }

}
