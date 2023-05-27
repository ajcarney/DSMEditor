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
    private boolean integerMode = false;


    /**
     * Creates the NumericTextField object and sets up callbacks to ensure only
     * numbers are entered in the area
     *
     * @param initialValue default text to display
     */
    public NumericTextField(Double initialValue) {
        if(initialValue != null) {
            numericValue = new SimpleDoubleProperty(initialValue);
            setText(initialValue.toString());
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
            for (int i = 0; i < newValue.length(); i++) {  // rebuild the string with no characters that are not numeric or have multiple decimal places
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

            if(!"".contentEquals(modifiedValue) && !".".contentEquals(modifiedValue)) {
                numericValue.set(Double.parseDouble(modifiedValue.toString()));
            }
            numericTextField.setText(modifiedValue.toString());
        });
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
