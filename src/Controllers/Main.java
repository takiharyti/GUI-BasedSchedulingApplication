package Controllers;

import com.sun.javafx.runtime.VersionInfo;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleGroup;
import util.JDBC;
import util.helper;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

/**
 * Main
 * Controller for main view
 *
 * Error in while loop in fillWIthData. Attempted to fill observable list with customer objects instead of string values which gave issues with lambda expression on line 52.
 *
 * ERROR: When deleting an appointment, the Contact_Name was displayed, not the Appointment type. Also missing the appointment_ID.
 * INCORRECT COMPETENCY: Now deletes all associated appointments when deleting customer.
 *
 * @author Tenny Akihary
 */
public class Main implements Initializable {

    //Variable declarations
    private ObservableList<String> selected;
    private String custId;
    private static String iD;
    private boolean customerFlag = true;
    private ZonedDateTime nowZDTUTC;
    private ZonedDateTime weekLaterZDTUTC;
    private int month;


    @FXML
    private TableView tV;

    @FXML
    private Button addButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button customersButton;

    @FXML
    private Button appointmentsButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button backButton;

    @FXML
    private RadioButton noneFilterRadio;

    @FXML
    private RadioButton monthlyFilterRadio;

    @FXML
    private RadioButton weeklyFilterRadio;

    @FXML
    private ToggleGroup filter = new ToggleGroup();

    /**
     * addButtonClicked
     * method for code when add is clicked
     * @param event addButton clicked.
     */
    public void addButtonClicked(ActionEvent event) throws IOException {

        //check if adding a customer or appointment
        if(customerFlag) {
            helper.loadView(event, "addCustomer");
        }else{
            helper.loadView(event,"addAppointment");
        }


    }

    /**
     * updateButtonClicked
     * method for code when update is clicked
     * @param event updateButton clicked.
     */
    public void updateButtonClicked(ActionEvent event) throws IOException {
        //assign selection
        selected = (ObservableList) tV.getSelectionModel().getSelectedItem();
        //Check if empty and flag user
        if (selected == null)
            helper.strAlert("You haven't selected anything!");
        //if not empty check if its for customer or appointment and load corresponding view
        else if(customerFlag) {
            iD = selected.get(0);
            helper.loadView(event, "updateCustomer");
        } else {
            iD = selected.get(0);
            helper.loadView(event, "updateAppointment");
        }

    }

    /**
     * deleteButtonClicked
     * delete method that checks if appointment deleted or customer deleted and whether there are any appointments under
     * a customer deletion.
     * @param event delete button clicked
     * @throws IOException
     * @throws SQLException
     */
    public void deleteButtonClicked(ActionEvent event) throws IOException, SQLException {
        //check to see if there is anything selected
        if (tV.getSelectionModel().getSelectedItems().isEmpty())
            helper.strAlert("You haven't selected anything!");
        //checks to see if customer button is currently selected
        else if(customerFlag) {
            selected = (ObservableList) tV.getSelectionModel().getSelectedItem();
            String custId = selected.get(0);

            /*
            NOT COMPETENCY

            Task states: "When deleting a customer record, all of the customer’s appointments must be deleted first, due to foreign key constraints."
            Initial code forced the user to delete all the appointments before the customer was able to delete the customer.
            New code alerts and gets confirmation from the user that deleting the customer will also delete all appointments.

            Connection conn = JDBC.getConnection();
            String SQL = "SELECT Title, Description, Location, Type, Start, End FROM appointments WHERE Customer_ID = "+custId+";";
            ResultSet resultSet = conn.createStatement().executeQuery(SQL);
            if(resultSet.next()){
                helper.strAlert("Must delete all appointments before deleting a customer!");

             */
            ButtonType yes = ButtonType.YES;
            ButtonType no = ButtonType.NO;
            Alert areYouSure = new Alert(Alert.AlertType.WARNING, "Customer ID: "+custId+". Are you sure you want to " +
                    "delete this customer and all associated appointments?", yes, no);
            Optional<ButtonType> answer = areYouSure.showAndWait();

            if(answer.get() == yes){
                //deletes all appointments then customers
                Connection conn = JDBC.getConnection();
                PreparedStatement deleteStatement = conn.prepareStatement("DELETE FROM customers WHERE " +
                        "Customer_ID = ?;\n DELETE FROM appointments WHERE Customer_ID = ?");
                deleteStatement.setString(1, custId);
                deleteStatement.setString(2, custId);
                try {
                    //execute delete
                    deleteStatement.executeUpdate();
                    deleteStatement.close();
                    helper.strAlert("SUCCESSFUL DELETION OF CUSTOMER AND ALL ASSOCIATED APPOINTMENTS!");
                    fillWithData();
                } catch (SQLException exception){
                    exception.printStackTrace();
                    deleteStatement.close();
                    helper.strAlert("CUSTOMER NOT DELETED DUE TO ERROR!");
                }
            }
        } else {
            //appointments is selected and therefore appointment will be deleted
            selected = (ObservableList) tV.getSelectionModel().getSelectedItem();
            String appId = selected.get(0);
            String appType = selected.get(5);//Error, used int 4 when should have been 5 for type
            Connection conn = JDBC.getConnection();
            PreparedStatement deleteStatement = conn.prepareStatement("DELETE FROM appointments WHERE " +
                    "Appointment_ID = " + appId + ";");
            try {
                //execute delete
                deleteStatement.executeUpdate();
                deleteStatement.close();
                helper.strAlert("Appointment ID:"+appId+//Added the missing Appointment ID for the error prompt
                        "Appointment type:" + appType + ". SUCCESSFULLY DELETED!");
                String SQL = "SELECT a.Appointment_ID, a.Title, Description, a.Location, c.Contact_Name, a.Type, a.Start, " +
                        "a.End, a.Customer_ID, a.User_ID FROM appointments as a " +
                        "RIGHT OUTER JOIN contacts as c ON a.Contact_ID = c.Contact_ID WHERE Customer_ID = "+custId+";";
                ResultSet resultSet = conn.createStatement().executeQuery(SQL);
                tV = helper.loadTableView(resultSet, tV);
            } catch (SQLException exception){
                exception.printStackTrace();
                deleteStatement.close();
                helper.strAlert("APPOINTMENT NOT DELETED DUE TO ERROR!");
            }
        }
    }

    /**
     * customersButtonClicked
     * Fills tableview with data and disables/enables specific fields.
     * @param event customersButton clicked.
     */
    public void customersButtonClicked(ActionEvent event) throws SQLException {

        //fill table view and disable/enable fields
        fillWithData();
        noneFilterRadio.setDisable(true);
        monthlyFilterRadio.setDisable(true);
        weeklyFilterRadio.setDisable(true);
        backButton.setDisable(true);
        nextButton.setDisable(true);
        customersButton.setDisable(true);
        appointmentsButton.setDisable(false);
        //toggle flag
        customerFlag = true;

    }

    /**
     * appointmentsButtonClicked
     * method for code when appointments is clicked
     * @param event appointmentsButton clicked.
     */
    public void appointmentsButtonClicked(ActionEvent event) throws SQLException {
        selected = (ObservableList) tV.getSelectionModel().getSelectedItem();
        //check to see if anything is selected, else if check to see if customer is already selected
        if (selected == null)
            helper.strAlert("You haven't selected anything!");
        else if(customerFlag) {
            //get selected record
            custId = selected.get(0);

            //connect and pull data based on the selected customer
            Connection conn = JDBC.getConnection();
            String SQL = "SELECT a.Appointment_ID, a.Title, Description, a.Location, c.Contact_Name, a.Type, a.Start, a.End, " +
                    "a.Customer_ID, a.User_ID FROM appointments as a RIGHT OUTER JOIN contacts as c " +
                    "ON a.Contact_ID = c.Contact_ID WHERE Customer_ID = "+custId+";";
            ResultSet resultSet = conn.createStatement().executeQuery(SQL);
            helper.loadTableView(resultSet, tV);

            //Ready fields for this selection
            noneFilterRadio.setDisable(false);
            monthlyFilterRadio.setDisable(false);
            weeklyFilterRadio.setDisable(false);
            customersButton.setDisable(false);
            appointmentsButton.setDisable(true);
            customerFlag = false;
            noneFilterRadio.setSelected(true);

            //Set variables for filters.
            month = ZonedDateTime.now().getMonthValue();
            nowZDTUTC = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
            weekLaterZDTUTC = nowZDTUTC.plus(7, ChronoUnit.DAYS);
        }

    }

    /**
     * fillWithData
     * method to pull customer data from database
     * @throws SQLException
     */
    public void fillWithData() throws SQLException {

        //Initializing connection and getting the result set
        Connection conn = JDBC.getConnection();
        String SQL = "SELECT cu.Customer_ID, cu.Customer_Name, cu.Address, cu.Postal_Code, cu.Phone, di.Division_ID, di.Division, co.Country_ID, co.Country FROM customers AS cu, first_level_divisions as di, countries as co\n" +
                "WHERE cu.Division_ID = di.Division_ID AND di.Country_ID = co.Country_ID;";
        ResultSet resultSet = conn.createStatement().executeQuery(SQL);


        //add data to the table view
        helper.loadCustomerTableView(resultSet, tV);
    }

    /**
     * noneFilterRadioClicked
     * Loads all appointments.
     * @param event noneFilterRadio clicked.
     * @throws SQLException
     */
    public void noneFilterRadioClicked(ActionEvent event) throws SQLException {
        //Reloads all appointments, non-filtered.
        Connection conn = JDBC.getConnection();
        String SQL = "SELECT a.Appointment_ID, a.Title, Description, a.Location, c.Contact_Name, a.Type, a.Start, a.End, " +
                "a.Customer_ID, a.User_ID FROM appointments as a RIGHT OUTER JOIN contacts as c " +
                "ON a.Contact_ID = c.Contact_ID WHERE Customer_ID = "+custId+";";
        ResultSet resultSet = conn.createStatement().executeQuery(SQL);
        helper.loadTableView(resultSet, tV);
        backButton.setDisable(true);
        nextButton.setDisable(true);
    }

    /**
     * monthlyFilterRadioClicked
     * Filter appointments by month.
     * @param event monthlyFilterRadio clicked.
     * @throws SQLException
     */
    public void monthlyFilterRadioClicked(ActionEvent event) throws SQLException {
        //Connect to DB and pull appointments by month.
        Connection conn = JDBC.getConnection();
        String SQL = "SELECT a.Appointment_ID, a.Title, Description, a.Location, c.Contact_Name, a.Type, a.Start, a.End, " +
                "a.Customer_ID, a.User_ID FROM appointments as a RIGHT OUTER JOIN contacts as c " +
                "ON a.Contact_ID = c.Contact_ID WHERE Customer_ID = "+custId+" AND Month(a.Start) = "+month+";";
        ResultSet resultSet = conn.createStatement().executeQuery(SQL);
        helper.loadTableView(resultSet, tV);
        backButton.setDisable(false);
        nextButton.setDisable(false);
    }

    /**
     * weeklyFilterRadioClicked
     * Toggles appointment filter by week.
     * @param event weeklyFilterRadio clicked.
     * @throws SQLException
     */
    public void weeklyFilterRadioClicked(ActionEvent event) throws SQLException {
        //Get appointments based on the next week
        Connection conn = JDBC.getConnection();
        PreparedStatement SQL = conn.prepareStatement("SELECT a.Appointment_ID, a.Title, Description, a.Location, c.Contact_Name, a.Type, a.Start, a.End, " +
                "a.Customer_ID, a.User_ID FROM appointments as a RIGHT OUTER JOIN contacts as c " +
                "ON a.Contact_ID = c.Contact_ID WHERE Customer_ID = ? AND a.Start BETWEEN ? AND ?;");
        SQL.setString(1, custId);
        SQL.setString(2, nowZDTUTC.format(helper.getFormat()));
        SQL.setString(3, weekLaterZDTUTC.format(helper.getFormat()));
        ResultSet resultSet = SQL.executeQuery();

        //load into table view
        helper.loadTableView(resultSet, tV);
        backButton.setDisable(false);
        nextButton.setDisable(false);
    }

    /**
     * nextButtonClicked
     * Adjusts the filtered appointments forward one step.
     * Step adjustment is monthly, or weekly.
     * @param event nextButton clicked.
     * @throws SQLException
     */
    public void nextButtonClicked(ActionEvent event) throws SQLException {
        //Check for monthly filter and adjust
        if(monthlyFilterRadio.isSelected()){
            if(month == 12)
                month = 1;
            else
                month++;
            monthlyFilterRadioClicked(event);
        } else {
            //Else adjust one week forward
            nowZDTUTC = nowZDTUTC.plusDays(7);
            weekLaterZDTUTC = weekLaterZDTUTC.plusDays(7);
            weeklyFilterRadioClicked(event);
        }
    }

    /**
     * backButtonClicked
     * Adjusts the filtered appointments back one step.
     * Step adjustment is monthly, or weekly.
     * @param event backButton clicked.
     * @throws SQLException
     */
    public void backButtonClicked(ActionEvent event) throws SQLException {
        //Check for monthly filter and adjust
        if(monthlyFilterRadio.isSelected()){
            if(month == 1)
                month = 12;
            else
                month--;
            monthlyFilterRadioClicked(event);
        } else {
            //Else adjust a previous week
            nowZDTUTC = nowZDTUTC.minusDays(7);
            weekLaterZDTUTC = weekLaterZDTUTC.minusDays(7);
            weeklyFilterRadioClicked(event);
        }
    }

    /**
     * reportsButtonClicked
     * Load reports view.
     * @param event reportsButton clicked.
     * @throws IOException
     */
    public void reportsButtonClicked(ActionEvent event) throws IOException {
        helper.loadView(event, "reports");
    }

    /**
     * getter for the iD associated with the selected Customer/Appointment for updating.
     * @return
     */
    public static String getiD(){
        return iD;
    }

    /**
     * initialize
     * Initializes the Main stage.
     * @param url Filepath.
     * @param resourceBundle Resource.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            fillWithData();
            noneFilterRadio.setToggleGroup(filter);
            monthlyFilterRadio.setToggleGroup(filter);
            weeklyFilterRadio.setToggleGroup(filter);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
