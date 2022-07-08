package View.MatrixViews;

/**
 * Contains possible values to determine how to display a matrix. Used in a dictionary as keys
 * with the value being the data held. Comments are the data type associated with the key
 */
public enum RenderMode {
    PLAIN_TEXT,
    PLAIN_TEXT_V,
    ITEM_NAME,
    ITEM_NAME_V,
    GROUPING_ITEM,
    GROUPING_ITEM_V,
    INDEX_ITEM,
    UNEDITABLE_CONNECTION,
    EDITABLE_CONNECTION,
    MULTI_SPAN_TEXT,        // Triplet<String, Integer, Integer>  -  text, row span, col span
    MULTI_SPAN_NULL         // null
}