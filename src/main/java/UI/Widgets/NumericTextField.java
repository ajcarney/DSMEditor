package UI.Widgets;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TextField;


/**
 * Gui widget that acts like a text field, but only allows floating point numbers or integers
 *
 * @author Aiden Carney
 */
public class NumericTextField extends TextField {
    private final DoubleProperty numericValue;
    private boolean integerMode;


    /**
     * Creates the NumericTextField object and sets up callbacks to ensure only
     * numbers are entered in the area
     *
     * @param initialValue default text to display
     * @param intMode true to specify only integers are allowed
     */
    public NumericTextField(Double initialValue, boolean intMode) {
        integerMode = intMode;
        if(initialValue != null) {
            numericValue = new SimpleDoubleProperty(initialValue);
            if (integerMode) {
                setText(String.valueOf(initialValue.intValue()));
            } else {
                setText(initialValue.toString());
            }

        } else {
            numericValue = new SimpleDoubleProperty();
            setText("");
        }

        final NumericTextField numericTextField = this;
        this.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null || "".equals(newValue)) {
                setNumericValue(null);
                return;
            }

            int numPeriods = 0;
            StringBuilder modifiedValue = new StringBuilder();

            // allow first character to be a negative sign
            if (newValue.charAt(0) == '.') {
                if(!integerMode) {
                    numPeriods += 1;
                    modifiedValue.append(".");
                }
            } else if ("-0123456789".contains(String.valueOf(newValue.charAt(0)))) { // char is not numeric and should be removed
                modifiedValue.append(newValue.charAt(0));
            }

            for (int i = 1; i < newValue.length(); i++) {  // rebuild the string with no characters that are not numeric or have multiple decimal places
                if (newValue.charAt(i) == '.') {
                    if(!integerMode) {
                        numPeriods += 1;
                        if (numPeriods <= 1) {
                            modifiedValue.append(".");
                        }
                    } else {
                        break;  // don't add anything after the decimal
                    }
                } else if ("0123456789".contains(String.valueOf(newValue.charAt(i)))) { // char is not numeric and should be removed
                    modifiedValue.append(newValue.charAt(i));
                }
            }

            // only update if a valid number
            if(!"".contentEquals(modifiedValue) && !".".contentEquals(modifiedValue) && !"-".contentEquals(modifiedValue)) {
                numericValue.set(Double.parseDouble(modifiedValue.toString()));
            }
            numericTextField.setText(modifiedValue.toString());
        });
    }


    /**
     * Creates the NumericTextField object and sets up callbacks to ensure only
     * numbers are entered in the area. Allow doubles
     *
     * @param initialValue default text to display
     */
    public NumericTextField(Double initialValue) {
        this(initialValue, false);
    }


    /**
     * Sets the new value for integer mode
     *
     * @param integerMode if the input box should only allow integers or also doubles
     */
    public void setIntegerMode(boolean integerMode) {
        this.integerMode = integerMode;
    }


    /**
     * Getter function for the current numeric number in the TextField
     *
     * @return the number in the TextField
     */
    public Double getNumericValue() {
        return numericValue.getValue();
    }


    /**
     * Setter value for the current number in the TextField
     *
     * @param newValue the number to display in the TextField
     */
    public void setNumericValue(Double newValue) {
        numericValue.setValue(newValue);
    }

}
