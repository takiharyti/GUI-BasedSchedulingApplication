package util;

import Model.Contacts;
import Model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Helper class in util to reduce code with simple tools and methods used often throughout this project.
 *
 * Index error in loadView, all code in loadCustTableView was initially in MainController. When using
 * observable list to try and create a customer object the callback function wouldn't work
 * since the index was searching for string but found the customer object.
 *
 * @author Tenny Akihary
 */
public class helper {



    /**
     * getFormat
     * Class to get the format for storage into the DB.
     * @return Correct date time format
     */
    public static DateTimeFormatter getFormat(){
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return f;
    }

    /**
     * loadTableView
     * Gets the result set of appointments and tableview to populate and return a dynamic tableview.
     * LAMBDA USE: To create a dynamic table for the main display, the tablecolumn
     * will loop through the column count for the desired resultSet. Setting the column cell
     * from a specific row then returning it as the simple string property in the temporary column.
     * @param resultSet Result Set
     * @param tV Table View
     * @return Dynamic Table View
     * @throws SQLException
     */
    public static TableView loadTableView(ResultSet resultSet, TableView tV) throws SQLException {

        //Declare variables and clear tableview
        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        TableView tableView = tV;
        tV.getColumns().clear();
        //int variables to select specific columns
        int start = resultSet.findColumn("Start");
        int end = resultSet.findColumn("End");

        //Dynamic TableView
        for (int i=0; i<resultSet.getMetaData().getColumnCount();i++){
            //using a temp variable to hold for get statement on SimpleStringProperty for each column
            final int temp = i;
            TableColumn c = new TableColumn(resultSet.getMetaData().getColumnName(i+1));
            //set column cell from a specific row then returning it as a string in the specific column 'temp'
            c.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>,
                     ObservableValue<String>>) param -> new SimpleStringProperty(param.getValue().get(temp).toString()));
            tV.getColumns().addAll(c);

        }

        while (resultSet.next()){
            //add record to list of arrays using a for loop
            ObservableList<String> record = FXCollections.observableArrayList();
            for (int i=1; i<=resultSet.getMetaData().getColumnCount(); i++) {
                //Checks for start or end columns and converts from UTC to Local time and populates the list
                if(i == start){
                    LocalDateTime startLDT = LocalDateTime.parse(resultSet.getString("Start"), getFormat());
                    ZonedDateTime startUTCZDT = ZonedDateTime.of(startLDT, ZoneOffset.UTC);
                    ZonedDateTime startLocalZDT = timeConvert(startUTCZDT, 0);
                    record.add(startLocalZDT.format(getFormat()));
                } else if (i == end){
                    LocalDateTime endLDT = LocalDateTime.parse(resultSet.getString("End"), helper.getFormat());
                    ZonedDateTime endUTCZDT = ZonedDateTime.of(endLDT, ZoneOffset.UTC);
                    ZonedDateTime endLocalZDT = helper.timeConvert(endUTCZDT, 0);
                    record.add(endLocalZDT.format(getFormat()));
                } else {
                    record.add(resultSet.getString(i));
                }
            }
            data.add(record);

        }

        //populate tV then return
        tV.setItems(data);
        return tV;
    }


    /**
     * loadCustTableView
     * loadView is used for multiple sql statements that will populate the customer tableview without
     * needing to add multiple views for each query.
     * ERROR when trying to create list of customer objects instead of string.
     *
     * LAMBDA USE: To create a dynamic table for the main display, the tablecolumn
     * will loop through the column count for the desired resultSet. Setting the column cell
     * from a specific row then returning it as the simple string property in the temporary column.
     * @param resultSet getting the result set from the sql query.
     * @param tV table view to keep out null errors.
     * @return returns the table view.
     * @throws SQLException
     */
    public static TableView loadCustomerTableView(ResultSet resultSet, TableView tV) throws SQLException {

        ObservableList<ObservableList> data;
        data = FXCollections.observableArrayList();
        TableView tableView = tV;
        tV.getColumns().clear();

        //Dynamic TableView
        for (int i=0; i<resultSet.getMetaData().getColumnCount();i++){
            //using a temp variable to hold for get statement on SimpleStringProperty for each column
            final int temp = i;
            TableColumn c = new TableColumn(resultSet.getMetaData().getColumnName(i+1));
            //set column cell from a specific row then returning it as a string in the specific column 'temp'
            c.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>)
                    (t) -> new SimpleStringProperty(t.getValue().get(temp).toString()) );
            tV.getColumns().addAll(c);

        }

        while (resultSet.next()){
/*
            ******ERROR******
            ObservableList<Customer> record = FXCollections.observableArrayList();
            //Customer object creation and record input
            int Customer_ID = resultSet.getInt("Customer_ID");
            String Customer_Name = resultSet.getString("Customer_Name");
            String Address = resultSet.getString("Address");
            String Postal_Code = resultSet.getString("Postal_Code");
            String Phone = resultSet.getString("Phone");
            int Division_ID = resultSet.getInt("Division_ID");
            String Division = resultSet.getString("Division");
            int Country_ID = resultSet.getInt("Country_ID");
            String Country = resultSet.getString("Country");
            Customer addCustomer = new Customer(Customer_ID, Division_ID, Customer_Name, Address, Postal_Code, Phone, Division, Country_ID, Country);
            record.add(addCustomer);
*/

            //add record to list of arrays using a for loop
            ObservableList<String> record = FXCollections.observableArrayList();
            for (int i=1; i<=resultSet.getMetaData().getColumnCount(); i++) {
                record.add(resultSet.getString(i));
            }
            data.add(record);

        }

        //populate tV then return
        tV.setItems(data);
        return tV;
    }

    /**
     * loadView
     * Helper class to load a different view given the event and the string for the view file (NOT including the .fxml extension)
     * @param event Events.
     * @param view Simple filename as string.
     * @throws IOException
     */
    public static void loadView(ActionEvent event, String view) throws IOException {

        Parent parent = FXMLLoader.load(helper.class.getResource("/Views/" + view + ".fxml"));
        Scene scene = new Scene(parent);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();

    }


    /**
     * strAlert
     * Helper Method to display an alert with given text.
     * @param alertText String for the alert text.
     */
    public static void strAlert(String alertText){
        Alert error = new Alert(Alert.AlertType.INFORMATION);
        error.setTitle("ALERT!");
        error.setHeaderText(alertText);
        error.showAndWait();
    }

    /**
     * loadCountries
     * helper method to load all the countries
     * @return list of countries
     * @throws SQLException
     */
    public static ObservableList<String> loadCountries() throws SQLException {

        //connect and pull distinct country data from DB
        Connection c = JDBC.getConnection();
        String SQL = "SELECT DISTINCT Country FROM countries;";
        ResultSet rS = c.createStatement().executeQuery(SQL);
        ObservableList<String> countries = FXCollections.observableArrayList();

        //populate and return
        while(rS.next()) {
            countries.add(rS.getString("Country"));
        }
        return countries;
    }

    /**
     * loadDivs
     * Helper method for gathering all the specific divisions from a countrys name.
     * @param country Country Name
     * @return List of first level divisions from a country.
     * @throws SQLException
     */
    public static ObservableList<String> loadDivs(String country) throws SQLException {

        //connect insert country name for SQL query
        Connection c = JDBC.getConnection();
        PreparedStatement SQL = c.prepareStatement("SELECT d.Division FROM countries as c RIGHT OUTER JOIN " +
                "first_level_divisions AS d ON c.Country_ID = d.Country_ID WHERE c.Country = ?;");
        SQL.setString(1, country);
        ResultSet rS = SQL.executeQuery();
        ObservableList<String> countries = FXCollections.observableArrayList();

        //populate list and return
        while(rS.next()) {
            countries.add(rS.getString("Division"));
        }
        return countries;
    }

    /**
     * loadContact
     * Loads all contacts and returns the names in a string
     * @return ObservableList of String Contact Names.
     * @throws SQLException
     */
    public static ObservableList<String> loadContact() throws SQLException{

        //connect and execute sql query
        Connection c = JDBC.getConnection();
        String SQL = "SELECT DISTINCT Contact_Name FROM contacts;";
        ResultSet rS = c.createStatement().executeQuery(SQL);
        ObservableList<String> contact = FXCollections.observableArrayList();

        //populate list and return
        while(rS.next()) {
            contact.add(rS.getString("Contact_Name"));
        }
        return contact;

    }

    /**
     * loadCustomerID
     * Loads all customer ID's into a list.
     * @return ObservableList of Customer IDs.
     * @throws SQLException
     */
    public static ObservableList<String> loadCustomerID() throws SQLException {
        //Connect and execute sql query
        Connection c = JDBC.getConnection();
        String SQL = "SELECT DISTINCT Customer_ID FROM Customers;";
        ResultSet rS = c.createStatement().executeQuery(SQL);
        ObservableList<String> custID = FXCollections.observableArrayList();

        //populate list and return
        while(rS.next()) {
            custID.add(rS.getString("Customer_ID"));
        }
        return custID;
    }

    /**
     * loadUsId
     * Loads User IDs into a list.
     * @return ObservableList of User IDs.
     * @throws SQLException
     */
    public static ObservableList<String> loadUserId() throws SQLException {
        //Connect and execute sql query
        Connection c = JDBC.getConnection();
        String SQL = "SELECT DISTINCT User_ID FROM Users;";
        ResultSet rS = c.createStatement().executeQuery(SQL);
        ObservableList<String> usID = FXCollections.observableArrayList();

        //populate list and return
        while(rS.next()) {
            usID.add(rS.getString("User_ID"));
        }
        return usID;
    }

    /**
     * getUserId
     * Getter for User IDs from a User Name.
     * @param uName User Name.
     * @return User ID.
     * @throws SQLException
     */
    public static String getUserId(String uName) throws SQLException {
        //Connect and execute sql query
        Connection c = JDBC.getConnection();
        PreparedStatement SQL = c.prepareStatement("SELECT User_ID FROM Users WHERE User_Name = ?;");
        SQL.setString(1, uName);

        ResultSet rS = SQL.executeQuery();
        String usID = "";

        //store and return
        if(rS.next()) {
            usID = (rS.getString("User_ID"));
        }
        return usID;
    }

    /**
     * loadContacts
     * loads all contacts into a list.
     * @return ObservableList of contact objects.
     * @throws SQLException
     */
    public static ObservableList<Contacts> loadContacts() throws SQLException {
        //connect and execute sql query
        ObservableList<Contacts> contacts = FXCollections.observableArrayList();
        Connection c = JDBC.getConnection();
        String SQL = "SELECT Contact_ID, Contact_Name FROM contacts;";
        ResultSet rS = c.createStatement().executeQuery(SQL);
        //populate list with contact objects and return
        while (rS.next()){
            contacts.add(new Contacts(rS.getString("Contact_ID"), rS.getString("Contact_Name")));
        }
        return contacts;
    }

    /**
     * loadUsers
     * Loads all users into a list.
     * @return ObservableList of user objects
     * @throws SQLException
     */
    public static ObservableList<User> loadUsers() throws SQLException {
        //connect and execute sql query
        ObservableList<User> users = FXCollections.observableArrayList();
        Connection c = JDBC.getConnection();
        String SQL = "SELECT User_ID, User_Name, Last_Update FROM users;";
        ResultSet rS = c.createStatement().executeQuery(SQL);
        //populate list with user objects and return
        while (rS.next()){
            users.add(new User(rS.getString("User_ID"), rS.getString("User_Name"),
                    rS.getString("Last_Update")));
        }
        return users;
    }

    /**
     * timeConvert
     * Converts ZonedDateTime into
     *      0 - Local Time
     *      1 - UTC
     *      2 - EST
     * and returns the converted time.
     * @param oldTime Initial time to convert.
     * @param toTimeZone Switch for type of conversion.
     * @return Converted ZonedDateTime.
     */
    public static ZonedDateTime timeConvert(ZonedDateTime oldTime, int toTimeZone){

        //Get local zone
        ZoneId localID = ZoneId.systemDefault();

        //Switch for desired conversion
        switch(toTimeZone){
            case 0:
                //returns conversion from oldTime to local time
                return oldTime.withZoneSameInstant(localID);
            case 1:
                //returns conversion from oldTime to UTC
                return oldTime.withZoneSameInstant(ZoneOffset.UTC);
            case 2:
                //returns conversion from oldTime to US/Eastern
                return oldTime.withZoneSameInstant(ZoneId.of("US/Eastern"));
            default:
                return oldTime;
        }
    }



}
