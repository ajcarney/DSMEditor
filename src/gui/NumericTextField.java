package gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;


/**
 * Gui widget that acts like a text field, but only allows floating point numbers or integers
 *
 * @author Aiden Carney
 */
public class NumericTextField extends TextField {
    private DoubleProperty numericValue;

    public Double getNumericValue() {
        return numericValue.getValue();
    }

    public void setNumericValue(Double newValue) {
        numericValue.setValue(newValue);
    }

    NumericTextField(Double initialValue) {
        if(initialValue != null) {
            numericValue = new SimpleDoubleProperty(initialValue);
            setText(initialValue.toString());
        } else {
            numericValue = new SimpleDoubleProperty();
            setText("");
        }

        // ensure any entered values lie inside the required range.
        final NumericTextField numericTextField = this;
        this.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
                if (newValue == null || "".equals(newValue)) {
                    setNumericValue(null);
                    return;
                }

                int numPeriods = 0;
                StringBuilder modifiedValue = new StringBuilder();
                for (int i = 0; i < newValue.length(); i++) {  // rebuild the string with no characters that are not numeric or have multiple decimal places
                    if (newValue.charAt(i) == '.') {
                        numPeriods += 1;
                        if (numPeriods > 1) {
                            modifiedValue.append("");
                        } else {
                            modifiedValue.append(".");
                        }
                    } else if (!"0123456789".contains(String.valueOf(newValue.charAt(i)))) { // char is not numeric and should be removed
                        modifiedValue.append("");
                    } else {
                        modifiedValue.append(newValue.charAt(i));
                    }
                }

                if(!"".equals(modifiedValue.toString()) && !".".equals(modifiedValue.toString())) {
                    numericValue.set(Double.parseDouble(modifiedValue.toString()));
                }
                numericTextField.setText(modifiedValue.toString());
            }
        });
    }

}
