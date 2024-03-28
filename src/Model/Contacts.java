package Model;

/**
 * Contacts
 * Class for the contacts object
 *
 * @author Tenny Akihary
 */
public class Contacts {

    //variable declaration
    private String Contact_ID;
    private String Contact_Name;

    /**
     * Contacts
     * constructor for contact objects
     * @param contact_ID Contact ID
     * @param contact_Name Contact Name
     */
    public Contacts(String contact_ID, String contact_Name) {
        Contact_ID = contact_ID;
        Contact_Name = contact_Name;
    }

    public String getContact_ID() {
        return Contact_ID;
    }


    public String getContact_Name() {
        return Contact_Name;
    }

}
