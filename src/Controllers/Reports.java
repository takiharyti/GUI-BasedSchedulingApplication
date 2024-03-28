package Controllers;

import Model.Contacts;
import Model.User;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import util.JDBC;
import util.helper;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Reports
 * Controller for the reports view.
 * Lists relevant reports for viewing.
 *
 * @author Tenny Akihary
 */
public class Reports implements Initializable {

    //Fields for input
    @FXML
    private Button typeButton;
    @FXML
    private Button monthButton;
    @FXML
    private Button contactsScheduleButton;
    @FXML
    private TextArea reportTextArea;
    @FXML
    private Button userButton;
    @FXML
    private Button cancelButton;

    /**
     * typeButtonClicked
     * Reports each appointment type in the database and lists the number of appointments.
     * @param event typeButton clicked.
     * @throws SQLException
     */
    public void typeButtonClicked(ActionEvent event) throws SQLException {
        //connect and get data from database
        Connection c = JDBC.getConnection();
        String SQL = "SELECT COUNT(Type) as \"Count_Type\", Type FROM appointments GROUP BY Type;";
        ResultSet rS = c.createStatement().executeQuery(SQL);
        reportTextArea.appendText("\n********TYPE COUNT REPORT********\n");
        //loop and display all the information
        while(rS.next()){

            reportTextArea.appendText(rS.getString("Type") + " has " +
                    rS.getString("Count_Type") + " appointment(s).\n");

        }
        reportTextArea.appendText("\n");
    }

    /**
     * monthButtonClicked
     * Reports each month and their total number of appointments.
     * @param event monthButton clicked
     * @throws SQLException
     */
    public void monthButtonClicked(ActionEvent event) throws SQLException {
        //connect and get data from database
        Connection c = JDBC.getConnection();
        String SQL = "SELECT MONTHNAME(Start) as \"Month_Name\", YEAR(Start) as \"Year\", " +
                "COUNT(MONTH(Start)) as \"Appointment_Count\" FROM appointments GROUP BY Month_Name;";
        ResultSet rS = c.createStatement().executeQuery(SQL);
        reportTextArea.appendText("\n********MONTHLY COUNT REPORT********\n");
        //loop and display all the information
        while(rS.next()){

            reportTextArea.appendText(rS.getString("Year") + " " + rS.getString("Month_Name")
                    +" has " + rS.getString("Appointment_Count") + " appointment(s).\n");

        }
        reportTextArea.appendText("\n");
    }

    /**
     * contactsScheduleButtonClicked
     * Reports each contact and the scheduled appointments and the corresponding information.
     * @param event contactsScheduleButton clicked.
     * @throws SQLException
     */
    public void contactsScheduleButtonClicked(ActionEvent event) throws SQLException {

        //load all contacts from database
        ObservableList<Contacts> allContacts = helper.loadContacts();

        //loop and display all information
        reportTextArea.appendText("\n********CONTACT SCHEDULES********\n");
        //loops every contact
        for(Contacts c : allContacts) {
            reportTextArea.appendText("Contact ID:" + c.getContact_ID() + " Contact Name:" + c.getContact_Name() + "\n\n");
            Connection conn = JDBC.getConnection();
            String SQL = "SELECT Appointment_ID, Title, Type, Description, Start, End, Customer_ID " +
                    "FROM appointments WHERE Contact_ID = " + c.getContact_ID() + ";";
            ResultSet rS = conn.createStatement().executeQuery(SQL);
            //loop for displaying all appointments from the current contact
            while (rS.next()) {
                reportTextArea.appendText("    Appointment ID:" + rS.getString("Appointment_ID") +
                        " Customer ID: " + rS.getString("Customer_ID") + " Title:" +
                        rS.getString("Type") + " Description:" + rS.getString("Description") +
                        "\n    Start:" + rS.getString("Start") + " End:" + rS.getString("End") + "\n\n");
            }
        }
    }

    /**
     * userButtonClicked
     * Reports each user and the last time it was updated.
     * @param event userButton clicked.
     * @throws SQLException
     */
    public void userButtonClicked(ActionEvent event) throws SQLException {

        reportTextArea.appendText("\n********USER DATA********\n");
        //loads users from database
        ObservableList<User> allUsers = helper.loadUsers();
        //loops and displays all information
        for(User u : allUsers){
            reportTextArea.appendText("User ID:"+u.getUser_ID()+" User Name:"+u.getUser_Name()+" Last Update:"+
                    u.getLast_Update()+"\n");
        }
        reportTextArea.appendText("\n");

    }

    /**
     * cancelButtonClicked
     * Loads the main view.
     * @param event cancelButton clicked.
     * @throws IOException
     */
    public void cancelButtonClicked(ActionEvent event) throws IOException {

        helper.strAlert("Heading back to main...");
        helper.loadView(event, "main");

    }

    /**
     * initialize
     * Initializes the Reports view.
     * @param url Stage location.
     * @param resourceBundle Resources.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

}
