package Controllers;

import Model.Appointment;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import util.JDBC;
import util.helper;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.*;
import java.util.ResourceBundle;

/**
 * UpdateAppointment
 * Controller for the UpdateAppointment.fxml view
 *
 * @author Tenny Akihary
 */
public class UpdateAppointment  implements Initializable {
    //Fields for user input
    @FXML
    private Button updateButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField titleText;
    @FXML
    private TextField appointmentId;
    @FXML
    private TextField descriptionText;
    @FXML
    private TextField locationText;
    @FXML
    private TextField typeText;
    @FXML
    private ComboBox<String> userIdCombo;
    @FXML
    private ComboBox<String> customerIdCombo;
    @FXML
    private DatePicker dateDatePicker;
    @FXML
    private Spinner startMinuteSpinner;
    @FXML
    private Spinner startHourSpinner;
    @FXML
    private Spinner endMinuteSpinner;
    @FXML
    private Spinner endHourSpinner;
    @FXML
    private ComboBox<String> contactCombo;

    //Temporary variable to hold appointment ID
    String tempId;

    /**
     * updateButtonClicked
     * Check input fields for errors and update appointment to database.
     * @param event updateButton clicked.
     * @throws SQLException
     * @throws IOException
     */
    public void updateButtonClicked(ActionEvent event) throws SQLException, IOException {

        //pull all data from fields
        String title = titleText.getText();
        String desc = descriptionText.getText();
        String loc = locationText.getText();
        String type = typeText.getText();
        String userId = userIdCombo.getValue();
        String custId = customerIdCombo.getValue();
        LocalDate date = dateDatePicker.getValue();
        Integer startMin = (Integer)startMinuteSpinner.getValue();
        Integer startHour = (Integer)startHourSpinner.getValue();
        Integer endMin = (Integer)endMinuteSpinner.getValue();
        Integer endHour = (Integer)endHourSpinner.getValue();
        String contact = contactCombo.getValue();

        //Code for converting the appointment time to EST and then checking the hours for business time
        LocalDateTime ldtStart;
        ZonedDateTime zdtStartSysDefault;
        ZonedDateTime startEst;
        LocalDateTime ldtEnd;
        ZonedDateTime zdtEndSystemDefault;
        ZonedDateTime endEst;
        Boolean isValid = true;
        Boolean isOverlappingTime = true;

        //if statement to check if any fields are blank, if the time is incorrect, or if the time is out of business hours
        if (!title.isBlank() && !desc.isBlank() && !loc.isBlank() && !type.isBlank() && (userId != null) &&
                (custId != null) && (date != null) && (contact != null)){
            ldtStart = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), startHour, startMin);
            zdtStartSysDefault = ldtStart.atZone(ZoneId.systemDefault());
            startEst = helper.timeConvert(zdtStartSysDefault, 2);
            ldtEnd = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), endHour, endMin);
            zdtEndSystemDefault = ldtEnd.atZone(ZoneId.systemDefault());
            endEst = helper.timeConvert(zdtEndSystemDefault, 2);

            //boolean to hold whether hours are within business hours
            isValid = validateHours(startEst, endEst);

            //boolean to isOverlappingTime
            isOverlappingTime = checkOverlappingHours(custId, tempId, zdtStartSysDefault, zdtEndSystemDefault);

            //If statement to check hours and do nothing if either methods are true
            if (isValid || isOverlappingTime) {
                helper.strAlert("Review Hours and Try again.");
            } else {

                //create appointment object and add to the database
                Appointment appointment = new Appointment(tempId, title, desc, loc, type, userId, custId, contact,
                        helper.timeConvert(zdtStartSysDefault, 1), helper.timeConvert(zdtEndSystemDefault, 1));
                appointment.updateToDB();
                helper.strAlert("SUCCESS!! Updating appointment to the database.");
                helper.loadView(event, "main");

            }
        } else
            helper.strAlert("Error, review input and try again.");

    }

    /**
     * cancelButtonClicked
     * Loads the main view.
     * @param event cancelButton clicked.
     * @throws IOException
     */
    public void cancelButtonClicked(ActionEvent event) throws IOException {
        helper.strAlert("Nothing was updated... Heading back...");
        helper.loadView(event, "main");
    }

    /**
     * validateHours
     * Method to validate whether hours are valid.
     * Start time has to be before the End time.
     * Start time can't be before 8am EST.
     * End time can't be before 10PM EST.
     * @return true if hours are invalid. False otherwise.
     */
    private boolean validateHours(ZonedDateTime start, ZonedDateTime end) {

        //ZoneDateTime variables for open and close times, DayOfWeek to get current day of week.
        ZonedDateTime openBusin = ZonedDateTime.of(start.toLocalDate(),
                LocalTime.of(8, 0), ZoneId.of("US/Eastern"));
        ZonedDateTime closeBusin = ZonedDateTime.of(end.toLocalDate(),
                LocalTime.of(22, 0), ZoneId.of("US/Eastern"));
        DayOfWeek dayOfWeek = start.getDayOfWeek();

        //Checks valid hours: No negative hours and inside business hours/days
        if(start.isAfter(end) || start.isAfter(closeBusin) || end.isAfter(closeBusin) || start.isEqual(end) ||
                start.isBefore(openBusin) || dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY)){
            helper.strAlert("Invalid input, make sure hours are valid (Start is before end, inside business hours, " +
                    "and not a past date!");
            return true;
        } else
            return false;

    }

    /**
     * checkOverlappingHours
     * Method for checking for overlapping appointment hours in database.
     * @param start Start time in local time
     * @param end End time in local time
     * @return true if updated start/end is between an already scheduled start/end
     * @throws SQLException
     */
    private boolean checkOverlappingHours(String custId, String appId, ZonedDateTime start, ZonedDateTime end) throws SQLException {
        //converts start and end times to UTC
        ZonedDateTime zdtUTCStart = helper.timeConvert(start, 1);
        ZonedDateTime zdtUTCEnd = helper.timeConvert(end, 1);

        //connect and get overlapping values in database
        Connection c = JDBC.getConnection();
        PreparedStatement SQL = c.prepareStatement("SELECT Start, End FROM appointments" +
                " WHERE Customer_ID = ? AND Appointment_ID != ? AND Start < ? AND End > ?;");
        SQL.setString(1, custId);
        SQL.setString(2, appId);
        SQL.setString(3, zdtUTCEnd.format(helper.getFormat()));
        SQL.setString(4, zdtUTCStart.format(helper.getFormat()));
        ResultSet rS = SQL.executeQuery();

        //if overlapping values are present flag and return true, else return false.
        if(rS.next()){
            helper.strAlert("Appointment is overlapping with an other appointment!");
            return true;
        }
        return false;
    }

    /**
     * initialize
     * Initilizes the UpdateAppointment stage.
     * Gets data from database and fills the corresponding fields.
     * @param url Stage location.
     * @param resourceBundle Resources.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            //get selected appointment ID
            tempId = Main.getiD();
            //Connect to database and pull record from specific ID
            Connection conn = JDBC.getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT a.Title, a.Description, a.Location, a.Type, " +
                    "a.User_ID, a.Customer_ID, a.Start, a.End, c.Contact_Name FROM appointments as a RIGHT OUTER JOIN " +
                    "contacts as c ON a.Contact_ID = c.Contact_ID WHERE Appointment_ID = "+tempId+";");
            ResultSet resultSet = statement.executeQuery();

            //Check if next and set text with correct values
            if (resultSet.next()) {
                titleText.setText(resultSet.getString("Title"));
                appointmentId.setText(tempId);
                descriptionText.setText(resultSet.getString("Description"));
                locationText.setText(resultSet.getString("Location"));
                typeText.setText(resultSet.getString("Type"));
                userIdCombo.setItems(helper.loadUserId());
                userIdCombo.setValue(resultSet.getString("User_ID"));
                customerIdCombo.setItems(helper.loadCustomerID());
                customerIdCombo.setValue(resultSet.getString("Customer_ID"));
                contactCombo.setItems(helper.loadContact());
                contactCombo.setValue(resultSet.getString("Contact_Name"));

                LocalDateTime startLDT = LocalDateTime.parse(resultSet.getString("Start"),
                        helper.getFormat());
                ZonedDateTime startUTCZDT = ZonedDateTime.of(startLDT, ZoneOffset.UTC);
                ZonedDateTime startLocalZDT = helper.timeConvert(startUTCZDT, 0);

                LocalDateTime endLDT = LocalDateTime.parse(resultSet.getString("End"),
                        helper.getFormat());
                ZonedDateTime endUTCZDT = ZonedDateTime.of(endLDT, ZoneOffset.UTC);
                ZonedDateTime endLocalZDT = helper.timeConvert(endUTCZDT, 0);

                dateDatePicker.setValue(startLocalZDT.toLocalDate());
                startHourSpinner.getValueFactory().setValue(startLocalZDT.getHour());
                startMinuteSpinner.getValueFactory().setValue(startLocalZDT.getMinute());
                endHourSpinner.getValueFactory().setValue(endLocalZDT.getHour());
                endMinuteSpinner.getValueFactory().setValue(endLocalZDT.getMinute());

            } else
                helper.strAlert("EMPTY RESULT SET!!! HIT CANCEL TO GO BACK.");


        } catch (SQLException throwables){
            throwables.printStackTrace();
        }

    }
}
