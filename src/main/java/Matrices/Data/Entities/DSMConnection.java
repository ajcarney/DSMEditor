package Matrices.Data.Entities;

import org.jdom2.Element;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Data class to manage DSM connections
 *
 * @author: Aiden Carney
 */
public class DSMConnection {
    private final int colUid;
    private final int rowUid;

    private String connectionName;
    private double weight;
    private ArrayList<DSMInterfaceType> interfaces;


    /**
     * Creates a new connection data object
     *
     * @param connectionName the name given to the connection
     * @param weight         the weight given to the connection
     * @param rowUid         the uid of the row item in the connection
     * @param colUid         the uid of the column item in the connection
     * @param interfaces     the interfaces for the connection
     */
    public DSMConnection(String connectionName, double weight, int rowUid, int colUid, ArrayList<DSMInterfaceType> interfaces) {
        this.connectionName = connectionName;
        this.weight = weight;
        this.colUid = colUid;
        this.rowUid = rowUid;
        this.interfaces = Objects.requireNonNullElseGet(interfaces, ArrayList::new);
    }


    /**
     * Copy constructor for DSMConnection
     *
     * @param copy DSMConnection object to copy
     */
    public DSMConnection(DSMConnection copy) {
        connectionName = copy.getConnectionName();
        weight = copy.getWeight();
        colUid = copy.getColUid();
        rowUid = copy.getRowUid();
        this.interfaces = new ArrayList<>();
        for(DSMInterfaceType interfaceType : copy.getInterfaces()) {
            this.interfaces.add(new DSMInterfaceType(interfaceType));
        }
    }


    /**
     * returns the current name of the connection
     *
     * @return the current connection name
     */
    public String getConnectionName() {
        return connectionName;
    }


    /**
     * returns the current weight of the connection
     *
     * @return the current weight
     */
    public double getWeight() {
        return weight;
    }


    /**
     * returns the uid of the column in the connection
     *
     * @return unique id of the column element
     */
    public int getColUid() {
        return colUid;
    }


    /**
     * returns the uid of the row in the connection
     *
     * @return unique id of the row element
     */
    public int getRowUid() {
        return rowUid;
    }


    /**
     * @return  the list of interface types for the connection
     */
    public ArrayList<DSMInterfaceType> getInterfaces() {
        return interfaces;
    }


    /**
     * Sets the current connection name to a new name
     *
     * @param connectionName the new name to be associated with the connection
     */
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }


    /**
     * Sets the current weight to a new weight
     *
     * @param weight the new weight to be associated with the connection
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }


    /**
     * Adds a new interface type to the connection if it is not already present
     *
     * @param interfaceType  the new interface type
     */
    public void addInterface(DSMInterfaceType interfaceType) {
        if(!interfaces.contains(interfaceType)) {
            interfaces.add(interfaceType);
        }
    }


    /**
     * Removes an interface type from the connection if it is present
     *
     * @param interfaceType  the new interface type
     */
    public void removeInterface(DSMInterfaceType interfaceType) {
        interfaces.remove(interfaceType);
    }


    /**
     * Removes all interfaces from the connection
     */
    public void clearInterfaces() {
        interfaces.clear();
    }


    /**
     * Sets the interfaces for the connection
     */
    public void setInterfaces(ArrayList<DSMInterfaceType> interfaces) {
        this.interfaces = interfaces;
    }


    /**
     * Adds the xml representation of a connection to an XML Element object
     *
     * @param connElement  the root to add the connection data to
     * @return             an xml representation of the connection object so that it can be saved to a file
     */
    public Element getXML(Element connElement) {
        connElement.addContent(new Element("row_uid").setText(Integer.valueOf(getRowUid()).toString()));
        connElement.addContent(new Element("col_uid").setText(Integer.valueOf(getColUid()).toString()));
        connElement.addContent(new Element("name").setText(getConnectionName()));
        connElement.addContent(new Element("weight").setText(Double.valueOf(getWeight()).toString()));

        Element interfacesXML = new Element("interfaces");
        for(DSMInterfaceType interfaceType : interfaces) {
            Element interfaceElement = new Element("interface");
            interfaceElement.setAttribute("uid", interfaceType.getUid().toString());
            interfacesXML.addContent(interfaceElement);
        }
        connElement.addContent(interfacesXML);

        return connElement;
    }


    /**
     * The function for determining if two connections are equal. Compare on weight and name
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

        // Check if o is an instance of DSMConnection or not "null instanceof [type]" also returns false
        if (!(o instanceof DSMConnection c)) {
            return false;
        }

        // cast to this object
        return isSameConnectionType(c) && (c.getRowUid() == this.getRowUid()) && (c.getColUid() == this.getColUid());  // compare based on name, weight, and uids
    }


    /**
     * Compares two DSMConnection types to check if they have the same name, weight, and interfaces
     *
     * @param c  the connection to compare to
     * @return   true or false if connections are the same type
     */
    public boolean isSameConnectionType(DSMConnection c) {
        boolean namesEqual = c.getConnectionName().equals(this.getConnectionName());
        boolean weightsEqual = c.getWeight() == this.getWeight();
        boolean interfacesEqual = false;
        if(c.getInterfaces().size() == interfaces.size()) {
            interfacesEqual = true;
            for (DSMInterfaceType interfaceType : interfaces) {
                if (!c.getInterfaces().stream().map(DSMInterfaceType::getUid).toList().contains(interfaceType.getUid())) {
                    interfacesEqual = false;
                    break;
                }
            }
        }

        return namesEqual && weightsEqual && interfacesEqual;  // compare based on name and weight
    }
}
