package UI.Widgets;

import Matrices.Data.Entities.DSMItem;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;


/**
 * Wrapper class for a DSMItem combobox that allows for searching.
 * Stores items by uid and not the actual object
 */
public class DSMItemComboBox extends SearchableComboBox<DSMItem> {

    /**
     * Creates a new instance and sets an item converter so everything looks correct
     */
    public DSMItemComboBox() {
        this.setConverter(new StringConverter<>() {  // use converter to allow to search for things
            @Override
            public String toString(DSMItem i) {
                if (i != null) {
                    return i.getName().getValue();
                }
                return "";
            }

            @Override
            public DSMItem fromString(String string) {
                return null;
            }
        });

    }



}
