package Model;

import Controllers.Login;
import util.JDBC;
import util.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Customer
 * Object class for customer.
 *
 * @author Tenny Akihary
 */
public class Customer {

    //Variable declaration
    private String Customer_ID;
    private String Division_ID;
    private String Customer_Name;
    private String Address;
    private String Postal_Code;
    private String Phone;
    private String Division;
    private String Country_ID;
    private String Country;

    /**
     * Customer
     * Constructor for customer object for adding to database.
     * Doesn't include Customer ID
     *
     * @param customer_Name Customer Name
     * @param address Address
     * @param postal_Code Postal Code
     * @param phone Phone
     * @param division Division
     * @param country Country
     */
    public Customer(String customer_Name, String address, String postal_Code, String phone, String division, String country) throws SQLException {
        Customer_Name = customer_Name;
        Address = address;
        Postal_Code = postal_Code;
        Phone = phone;
        Division = division;
        Country = country;

        getIDs(Division);

    }

    /**
     * Customer
     * Constructor for customer object for updating to database.
     * Includes Customer ID
     * @param id customer ID
     * @param customer_Name Customer Name
     * @param address Address
     * @param postal_Code Postal Code
     * @param phone Phone
     * @param division Division
     * @param country Country
     * @throws SQLException
     */
    public Customer(String id, String customer_Name, String address, String postal_Code, String phone, String division, String country) throws SQLException {
        Customer_ID = id;
        Customer_Name = customer_Name;
        Address = address;
        Postal_Code = postal_Code;
        Phone = phone;
        Division = division;
        Country = country;

        getIDs(Division);

    }

    /**
     * getIDs
     * Method for getting the specific Division_ID and Country_ID based on the division name
     * @param division Division name.
     * @throws SQLException
     */
    public void getIDs(String division) throws SQLException {
        //Connect and prepare statement with division name
        Connection c = JDBC.getConnection();
        PreparedStatement SQL = c.prepareStatement("SELECT Division_ID, Country_ID FROM first_level_divisions WHERE Division = ?;");
        SQL.setString(1, division);
        ResultSet rS = SQL.executeQuery();
        //pull the IDs and assign them to variables
        if(rS.next()) {
            Division_ID = rS.getString("Division_ID");
            Country_ID = rS.getString("Country_ID");
        }

    }

    /**
     * addToDB
     * class to add the customer to the DB
     * @throws SQLException
     */
    public void addToDB() throws SQLException {
        //Get connection and prepare and execute update statement with desired variables
        Connection c = JDBC.getConnection();
        PreparedStatement SQL = c.prepareStatement("INSERT INTO customers (Division_ID, Customer_Name, Address, " +
                "Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By) \n" +
                "VaLUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
        //get user and set the time to UTC for storage in the correct format
        String uName = Login.getUser();
        String date = ZonedDateTime.now(ZoneOffset.UTC).format(helper.getFormat());
        SQL.setString(1, Division_ID);
        SQL.setString(2, Customer_Name);
        SQL.setString (3, Address);
        SQL.setString(4, Postal_Code);
        SQL.setString(5, Phone);
        SQL.setString(6, date);
        SQL.setString(7, uName);
        SQL.setString(8, date);
        SQL.setString(9, uName);

        try {
            SQL.executeUpdate();
            SQL.close();
            helper.strAlert("SUCCESSFULLY ADDED!");
        } catch (SQLException exception){
            exception.printStackTrace();
            SQL.close();
            helper.strAlert("UNSUCCESSFUL, CUSTOMER WAS NOT ADDED!");
        }
    }

    /**
     * updateToDB
     * class to update customers to the database
     * @throws SQLException
     */
    public void updateToDB() throws SQLException {
        //Get connection and prepare and execute update statement with desired variables
        Connection c = JDBC.getConnection();
        PreparedStatement SQL = c.prepareStatement("UPDATE customers SET Division_ID = ?, Customer_Name = ?, " +
                "Address = ?, Postal_Code = ?, Phone = ?, Last_Update = ?, Last_Updated_By = ? " +
                "WHERE Customer_ID = ?;");
        //get user and set the time to UTC for storage in the correct format
        String uName = Login.getUser();
        String date = ZonedDateTime.now(ZoneOffset.UTC).format(helper.getFormat());
        SQL.setString(1, Division_ID);
        SQL.setString(2, Customer_Name);
        SQL.setString(3, Address);
        SQL.setString(4, Postal_Code);
        SQL.setString(5, Phone);
        SQL.setString(6, date);
        SQL.setString(7, uName);
        SQL.setString(8, Customer_ID);

        try {
            SQL.executeUpdate();
            SQL.close();
            helper.strAlert("SUCCESSFULLY UPDATED!");
        } catch (SQLException exception){
            exception.printStackTrace();
            SQL.close();
            helper.strAlert("UNSUCCESSFUL, CUSTOMER WAS NOT UPDATED!");
        }
    }

}
