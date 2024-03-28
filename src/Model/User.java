package Model;

/**
 * User
 * Class for a user object.
 *
 * @author Tenny Akihary
 */
public class User {

    //Variable declaration
    private String User_ID;
    private String User_Name;
    private String Last_Update;

    /**
     * User
     * Constructor for User objects
     * @param user_ID User ID
     * @param user_Name User Name
     * @param last_Update Last Update
     */
    public User(String user_ID, String user_Name, String last_Update) {
        User_ID = user_ID;
        User_Name = user_Name;
        Last_Update = last_Update;
    }

    /**
     * getUser_ID
     * Getter for user ID
     * @return User ID
     */
    public String getUser_ID() {
        return User_ID;
    }

    /**
     * getUser_Name
     * Getter for username
     * @return User Name
     */
    public String getUser_Name() {
        return User_Name;
    }

    /**
     * getLast_Update
     * Getter for Last Update.
     * @return Last Update
     */
    public String getLast_Update() {
        return Last_Update;
    }

}
