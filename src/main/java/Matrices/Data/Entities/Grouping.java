package Matrices.Data.Entities;

import javafx.scene.paint.Color;
import org.jdom2.Element;

/**
 * Data class to handle groupings used in a DSM. Each grouping has a uid, a name, and a color associated with it. This
 * is generic so it can be used for different levels of groupings in a hierarchy design (ie. domains vs subsystems)
 *
 * @author Aiden Carney
 */
public class Grouping {

    private final Integer uid;
    private Integer priority;
    private String name;
    private Color color;
    private Color fontColor;

    public static final Color DEFAULT_FONT_COLOR = Color.color(0, 0, 0);
    public static final Integer DEFAULT_PRIORITY = -1;


    /**
     * Creates a new Grouping with a given name and color
     *
     * @param name  the starting name of the grouping
     * @param color the starting index of the grouping
     */
    public Grouping(String name, Color color) {
        this.uid = java.util.UUID.randomUUID().hashCode();
        this.priority = DEFAULT_PRIORITY;
        this.name = name;
        this.color = color;
        this.fontColor = DEFAULT_FONT_COLOR;
    }


    /**
     * Creates a new Grouping with given colors and name
     *
     * @param name  the starting name of the grouping
     * @param color the starting index of the grouping
     */
    public Grouping(String name, Color color, Color fontColor) {
        this.uid = java.util.UUID.randomUUID().hashCode();
        this.priority = DEFAULT_PRIORITY;
        this.name = name;
        this.color = color;
        this.fontColor = fontColor;
    }


    /**
     * Creates a new Group with all data fields to be set. Called when creating a Grouping from data that was
     * saved to a file
     *
     * @param uid      the uid of the group
     * @param name     the name of the group
     * @param color    the color of the group
     */
    public Grouping(Integer uid, Integer priority, String name, Color color, Color fontColor) {
        this.uid = uid;
        this.priority = priority;
        this.name = name;
        this.color = color;
        this.fontColor = fontColor;
    }


    /**
     * Creates a new group based on an xml representation
     *
     * @param xml  the xml element associated with the grouping
     */
    public Grouping(Element xml) {
        this.uid = Integer.parseInt(xml.getChild("uid").getText());
        this.priority = Integer.parseInt(xml.getChild("priority").getText());
        this.name = xml.getChild("name").getText();

        double groupR = Double.parseDouble(xml.getChild("gr").getText());
        double groupG = Double.parseDouble(xml.getChild("gg").getText());
        double groupB = Double.parseDouble(xml.getChild("gb").getText());
        this.color = Color.color(groupR, groupG, groupB);

        double fontR = Double.parseDouble(xml.getChild("fr").getText());
        double fontG = Double.parseDouble(xml.getChild("fg").getText());
        double fontB = Double.parseDouble(xml.getChild("fb").getText());
        this.fontColor = Color.color(fontR, fontG, fontB);
    }


    /**
     * Copy constructor for a Grouping
     *
     * @param copy Grouping object that will be copied
     */
    public Grouping(Grouping copy) {
        uid = copy.getUid();
        priority = copy.getPriority();
        name = copy.getName();
        color = copy.getColor();
        fontColor = copy.getFontColor();
    }


    /**
     * Getter function for the uid of the grouping
     *
     * @return the uid of the grouping
     */
    public Integer getUid() {
        return uid;
    }


    /**
     * Getter function for the priority of the grouping
     *
     * @return the priority of the grouping
     */
    public Integer getPriority() {
        return priority;
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
     * Setter function for the priority of the grouping
     *
     * @param priority the new priority of the grouping
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
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
     * Adds the xml representation of a Grouping to an XML Element object
     *
     * @param groupElement  the root to add the grouping data to
     * @return              an xml representation of the Grouping object so that it can be saved to a file
     */
    public Element getXML(Element groupElement) {
        groupElement.addContent(new Element("uid").setText(getUid().toString()));
        groupElement.addContent(new Element("priority").setText(getPriority().toString()));
        groupElement.addContent(new Element("name").setText(getName()));
        groupElement.addContent(new Element("gr").setText(Double.valueOf(getColor().getRed()).toString()));
        groupElement.addContent(new Element("gg").setText(Double.valueOf(getColor().getGreen()).toString()));
        groupElement.addContent(new Element("gb").setText(Double.valueOf(getColor().getBlue()).toString()));
        groupElement.addContent(new Element("fr").setText(Double.valueOf(getFontColor().getRed()).toString()));
        groupElement.addContent(new Element("fg").setText(Double.valueOf(getFontColor().getGreen()).toString()));
        groupElement.addContent(new Element("fb").setText(Double.valueOf(getFontColor().getBlue()).toString()));

        return groupElement;
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
        if (!(o instanceof Grouping g)) {
            return false;
        }

        return g.getUid().equals(this.getUid());  // compare based on uid
    }
}
