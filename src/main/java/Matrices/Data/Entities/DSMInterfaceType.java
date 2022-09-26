package Matrices.Data.Entities;

import org.jdom2.Element;


/**
 * Data class to handle interface types used in a DSM. Each interface has a uid and a name associated with it.
 *
 * @author Aiden Carney
 */
public class DSMInterfaceType {

    private final Integer uid;
    private String name;
    private String abbreviation;

    /**
     * Creates a new DSMInterfaceType with a given name
     *
     * @param name  the starting name of the interface type
     */
    public DSMInterfaceType(String name, String abbreviation) {
        this.uid = java.util.UUID.randomUUID().hashCode();
        this.name = name;
        this.abbreviation = abbreviation;
    }


    /**
     * Creates a new DSMInterfaceType with all data fields to be set. Called when creating a DSMInterfaceType
     * from data that was saved to a file
     *
     * @param uid      the uid of the interface type
     * @param name     the name of the interface type
     */
    public DSMInterfaceType(Integer uid, String name, String abbreviation) {
        this.uid = uid;
        this.name = name;
        this.abbreviation = abbreviation;
    }


    /**
     * Creates a new interface type based on an xml representation
     *
     * @param xml  the xml element associated with the interface type
     */
    public DSMInterfaceType(Element xml) {
        this.uid = Integer.parseInt(xml.getChild("uid").getText());
        this.name = xml.getChild("name").getText();
        try {
            this.abbreviation = xml.getChild("abbrev").getText();
        } catch(Exception e) {
            this.abbreviation = String.valueOf(this.name.charAt(0));
        }
    }


    /**
     * Copy constructor for DSMInterfaceType
     *
     * @param copy DSMInterfaceType object that will be copied
     */
    public DSMInterfaceType(DSMInterfaceType copy) {
        uid = copy.getUid();
        name = copy.getName();
        abbreviation = copy.getAbbreviation();
    }


    /**
     * @return  the uid of the interface type
     */
    public Integer getUid() {
        return uid;
    }


    /**
     * @return  the name of the interface type
     */
    public String getName() {
        return name;
    }


    /**
     * @return  the abbreviation of the interface type
     */
    public String getAbbreviation() {
        return abbreviation;
    }


    /**
     * Adds the xml representation of an interface type to an XML Element object
     *
     * @param interfaceElement  the root to add the interface type data to
     * @return                  an xml representation of the interface type so that it can be saved to a file
     */
    public Element getXML(Element interfaceElement) {
        interfaceElement.addContent(new Element("uid").setText(getUid().toString()));
        interfaceElement.addContent(new Element("name").setText(getName()));
        interfaceElement.addContent(new Element("abbrev").setText(getAbbreviation()));

        return interfaceElement;
    }


    /**
     * Setter function for the name of the interface type
     *
     * @param name the new name of the interface type
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Setter function for the abbreviation of the interface type
     *
     * @param abbreviation the new abbreviation of the interface type
     */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }


    /**
     * The function for determining if two interface types are equal. Compare on uid
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
        if (!(o instanceof DSMInterfaceType i)) {
            return false;
        }

        return i.getUid().equals(this.getUid());  // compare based on uid
    }

}
