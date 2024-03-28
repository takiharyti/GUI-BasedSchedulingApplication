package Model;

import Controllers.Login;
import util.JDBC;
import util.helper;

import java.sql.*;
import java.time.*;

/**
 * Appointment
 * Class for appointment object
 *
 * @author Tenny Akihary
 */
public class Appointment {

    //Variable declaration
    private String title;
    private String description;
    private String location;
    private String type;
    private String userID;
    private String customerID;
    private String contactName;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private String appointmentID;
    private String contactID;
    private String createDate;
    private String createBy;
    private String lastUpdated;
    private String lastUpBy;

    /**
     * Appointment
     * Appointment constructor for appointment creation.
     *
     * @param tit      title
     * @param descrip  Description
     * @param locat    Location
     * @param typ      Type
     * @param uID      User ID
     * @param custID   Customer Id
     * @param conName  COntact name
     * @param zdtStart ZonedDateTime Start in UTC
     * @param zdtEnd   ZonedDateTime End in utc
     * @throws SQLException
     */
    public Appointment(String tit, String descrip, String locat, String typ, String uID, String custID, String conName,
                       ZonedDateTime zdtStart, ZonedDateTime zdtEnd)
            throws SQLException {
        title = tit;
        description = descrip;
        location = locat;
        type = typ;
        userID = uID;
        customerID = custID;
        contactName = conName;
        startDate = zdtStart;
        endDate = zdtEnd;

        getContactId(contactName);

        createDate = ZonedDateTime.now(ZoneOffset.UTC).format(helper.getFormat());
        createBy = Login.getUser();
        lastUpdated = createDate;
        lastUpBy = createBy;

    }

    /**
     * Appointment
     * Appointment constructor for appointment update.
     *
     * @param appId    appointment id
     * @param tit      title
     * @param descrip  Description
     * @param locat    Location
     * @param typ      Type
     * @param uID      User ID
     * @param custID   Customer Id
     * @param conName  COntact name
     * @param zdtStart ZonedDateTime Start in UTC
     * @param zdtEnd   ZonedDateTime End in utc
     * @throws SQLException
     */
    public Appointment(String appId, String tit, String descrip, String locat, String typ, String uID, String custID, String conName,
                       ZonedDateTime zdtStart, ZonedDateTime zdtEnd)
            throws SQLException {
        appointmentID = appId;
        title = tit;
        description = descrip;
        location = locat;
        type = typ;
        userID = uID;
        customerID = custID;
        contactName = conName;
        startDate = zdtStart;
        endDate = zdtEnd;

        getContactId(contactName);

        createDate = ZonedDateTime.now(ZoneOffset.UTC).format(helper.getFormat());
        createBy = Login.getUser();

    }

    /**
     * getContactId
     * Method for getting contact ID from contact name.
     *
     * @param contact Contact name.
     * @throws SQLException
     */
    public void getContactId(String contact) throws SQLException {
        //make connection and execute query with the contact name inserted
        Connection c = JDBC.getConnection();
        PreparedStatement SQL = c.prepareStatement("SELECT Contact_ID FROM contacts WHERE Contact_Name = ?;");
        SQL.setString(1, contact);
        ResultSet rS = SQL.executeQuery();
        if (rS.next()) {
            //return contact ID
            contactID = rS.getString("Contact_ID");
        }

    }

    /**
     * addToDB
     * Method for adding the appointment to the database.
     */
    public void addToDB() throws SQLException {
        //Get connection and prepare and execute update statement with desired variables
        Connection c = JDBC.getConnection();
        PreparedStatement SQL = c.prepareStatement("INSERT INTO appointments (Title, Description, Location, Type, Start, End, \n" +
                "Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID)\n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

        //set the time to UTC for storage in the correct format
        String startDateString = startDate.format(helper.getFormat());
        String endDateString = endDate.format(helper.getFormat());

        SQL.setString(1, title);
        SQL.setString(2, description);
        SQL.setString(3, location);
        SQL.setString(4, type);
        SQL.setString(5, startDateString);
        SQL.setString(6, endDateString);
        SQL.setString(7, createDate);
        SQL.setString(8, createBy);
        SQL.setString(9, lastUpdated);
        SQL.setString(10, lastUpBy);
        SQL.setString(11, customerID);
        SQL.setString(12, userID);
        SQL.setString(13, contactID);

        try {
            SQL.executeUpdate();
            SQL.close();
            helper.strAlert("SUCCESSFULLY ADDED!");
        } catch (SQLException exception) {
            exception.printStackTrace();
            SQL.close();
            helper.strAlert("UNSUCCESSFUL, APPOINTMENT WAS NOT ADDED!");
        }

    }

    /**
     * updateToDB
     * Method for updating the appointment to the database
     */
    public void updateToDB() throws SQLException {

        //Get connection and prepare and execute update statement with desired variables
        Connection c = JDBC.getConnection();
        PreparedStatement SQL = c.prepareStatement("UPDATE appointments SET Title = ?, Description = ?, " +
                "Location = ?, Contact_ID = ?, Type = ?, Start = ?, End = ?, Customer_ID = ?, User_ID = ?, " +
                "Last_Updated_By = ?, Last_Update = ? WHERE Appointment_ID = ?;");
        //get user and set the time to UTC for storage in the correct format
        String uName = Login.getUser();
        String date = ZonedDateTime.now(ZoneOffset.UTC).format(helper.getFormat());
        String startDateString = startDate.format(helper.getFormat());
        String endDateString = endDate.format(helper.getFormat());

        SQL.setString(1, title);
        SQL.setString(2, description);
        SQL.setString(3, location);
        SQL.setString(4, contactID);
        SQL.setString(5, type);
        SQL.setString(6, startDateString);
        SQL.setString(7, endDateString);
        SQL.setString(8, customerID);
        SQL.setString(9, userID);
        SQL.setString(10, uName);
        SQL.setString(11, date);
        SQL.setString(12, appointmentID);

        try {
            SQL.executeUpdate();
            SQL.close();
            helper.strAlert("SUCCESSFULLY UPDATED!");
        } catch (SQLException exception) {
            exception.printStackTrace();
            SQL.close();
            helper.strAlert("UNSUCCESSFUL, APPOINTMENT WAS NOT UPDATED!");
        }

    }

    /**
     * getTitle
     * Getter for title
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * setTitle
     * Setter for title
     *
     * @param title new title
     */
    public void setTitle(String title) {
        this.title = title;
    }
}