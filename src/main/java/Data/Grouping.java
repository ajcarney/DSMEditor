package Data;

import javafx.scene.paint.Color;

/**
 * Data class to handle grouping used in a DSM. Each grouping has a uid, a name, and a color associated with it. This
 * is generic so it can be used for different levels of groupings in a hierarchy design (ie. domains vs subsystems)
 *
 * @author Aiden Carney
 */
public class Grouping {

    private final Integer uid;
    private String name;
    private Color color;
    private Color fontColor;

    public static final Color defaultFontColor = Color.color(0, 0, 0);


    /**
     * Creates a new Grouping with a given color
     *
     * @param name  the starting name of the item
     * @param color the starting index of the item
     */
    public Grouping(String name, Color color) {
        this.uid = java.util.UUID.randomUUID().hashCode();
        this.name = name;
        this.color = color;
        this.fontColor = defaultFontColor;
    }


    /**
     * Creates a new Grouping with a given color
     *
     * @param name  the starting name of the item
     * @param color the starting index of the item
     */
    public Grouping(String name, Color color, Color fontColor) {
        this.uid = java.util.UUID.randomUUID().hashCode();
        this.name = name;
        this.color = color;
        this.fontColor = fontColor;
    }


    /**
     * Creates a new Group with all data fields to be set. Called when creating an item from data that was saved to a file
     *
     * @param uid      the uid of the group
     * @param name     the name of the group
     * @param color    the color of the group
     */
    public Grouping(Integer uid, String name, Color color, Color fontColor) {
        this.uid = uid;
        this.name = name;
        this.color = color;
        this.fontColor = fontColor;
    }


    /**
     * Copy constructor for DSMItem
     *
     * @param copy DSMItem object that will be copied
     */
    public Grouping(Grouping copy) {
        uid = copy.getUid();
        name = copy.getName();
        color = copy.getColor();
        fontColor = copy.getFontColor();
    }


    /**
     * Getter function for the uid of the grouping
     *
     * @return the uid of the item
     */
    public Integer getUid() {
        return uid;
    }


    /**
     * Getter function for the name of the grouping
     *
     * @return the name of the grouping
     */
    public String getName() {
        return name;
    }


    /**
     * Getter function for the color of the grouping
     *
     * @return the color of the grouping
     */
    public Color getColor() {
        return color;
    }


    /**
     * Getter function for the font color of the grouping
     *
     * @return the font color of the grouping
     */
    public Color getFontColor() {
        return fontColor;
    }


    /**
     * Setter function for the name of the grouping
     *
     * @param name the new name of the grouping
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Setter function for the color of the grouping
     *
     * @param color the new color of the grouping
     */
    public void setColor(Color color) {
        this.color = color;
    }


    /**
     * Setter function for the font color of the grouping
     *
     * @param fontColor  the new fontColor of the grouping
     */
    public void setFontColor(Color fontColor) {
        this.fontColor = fontColor;
    }



    /**
     * The function for determining if two groupings are equal. Compare on uid
     *
     * @param o  the object to compare
     * @return   true if the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        // Check if o is an instance of Grouping or not "null instanceof [type]" also returns false
        if (!(o instanceof Grouping)) {
            return false;
        }

        // cast to this object
        Grouping g = (Grouping) o;
        return g.getUid() == this.getUid();  // compare based on uid
    }
}
