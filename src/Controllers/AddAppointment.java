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
 * AddAppointment
 * Class controller for the AddAppointment view.
 *
 * @author Tenny Akihary
 */
public class AddAppointment implements Initializable {

    //Fields for user input
    @FXML
    private Button addButton;
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

    /**
     * addButtonClicked
     * class for when the appointment is ready to be added.
     * @param event
     */
    public void addButtonClicked(ActionEvent event) throws IOException, SQLException {

        //Get the input values from the fields
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

        //booleans set to true
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
            isOverlappingTime = checkOverlappingHours(custId, zdtStartSysDefault, zdtEndSystemDefault);

            //If statement to check hours and do nothing if either methods are true
            if (isValid || isOverlappingTime) {
                helper.strAlert("Review Hours and Try again.");
            } else {

                //create appointment object and add to the database
                Appointment appointment = new Appointment(title, desc, loc, type, userId, custId, contact,
                        helper.timeConvert(zdtStartSysDefault, 1), helper.timeConvert(zdtEndSystemDefault, 1));
                appointment.addToDB();
                helper.strAlert("Appointment was added! Heading back to main.");
                helper.loadView(event, "main");

            }
        } else //Flag the user in case an input is blank.
            helper.strAlert("Error, review input and try again.");

    }

    /**
     * validateHours
     * Method to validate whether hours are valid.
     * Start time has to be before the End time.
     * Start time can't be before 8am EST.
     * End time can't be before 10PM EST.
     * @return boolean true if invalid, false if valid.
     */
    private boolean validateHours(ZonedDateTime start, ZonedDateTime end) {

        //Set the open and closed business hours in the correct timezone and get the day selected
        ZonedDateTime openBusin = ZonedDateTime.of(start.toLocalDate(),
                LocalTime.of(8, 0), ZoneId.of("US/Eastern"));
        ZonedDateTime closeBusin = ZonedDateTime.of(end.toLocalDate(),
                LocalTime.of(22, 0), ZoneId.of("US/Eastern"));
        DayOfWeek dayOfWeek = start.getDayOfWeek();

        //Check if entire appointment is within business hours, no negative hours, and no weekends.
        if(start.isAfter(end) || start.isAfter(closeBusin) || end.isAfter(closeBusin) || start.isEqual(end) ||
        start.isBefore(openBusin) || dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY)){
            helper.strAlert("Invalid input, Start time is after end time or the hours are outside business hours!");
            return true;
        } else
            return false;

    }

    /**
     * checkOverlappingHours
     * Method for checking for overlapping hours.
     * @param start Start time in local time
     * @param end End time in local time
     * @return true if updated start/end is between an already scheduled start/end
     * @throws SQLException
     */
    private boolean checkOverlappingHours(String custId, ZonedDateTime start, ZonedDateTime end) throws SQLException {
        //convert start and end times to UTC for checking
        ZonedDateTime zdtUTCStart = helper.timeConvert(start, 1);
        ZonedDateTime zdtUTCEnd = helper.timeConvert(end, 1);

        //Establish connection and get all data that overlaps the start/end times
        Connection c = JDBC.getConnection();
        PreparedStatement SQL = c.prepareStatement("SELECT Start, End FROM appointments" +
                " WHERE Customer_ID = ? AND Start < ? AND End > ?;");
        SQL.setString(1, custId);
        SQL.setString(2, zdtUTCEnd.format(helper.getFormat()));
        SQL.setString(3, zdtUTCStart.format(helper.getFormat()));
        ResultSet rS = SQL.executeQuery();

        //check if any overlapping appointments were found
        if(rS.next()){
            helper.strAlert("Appointment is overlapping with an other appointment!");
            return true;
        }
        return false;
    }

    /**
     * cancelButtonClicked
     * Head back to the main view.
     * @param event cancelButton clicked.
     * @throws IOException
     */
    public void cancelButtonClicked(ActionEvent event) throws IOException {
        //flag the user then load next stage
        helper.strAlert("Nothing was added... Heading back...");
        helper.loadView(event, "main");
    }

    /**
     * initialize
     * Initializes the AddAppointment stage.
     * @param url Stage path.
     * @param resourceBundle Resources.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            //Load the Combo Boxes with selections.
            customerIdCombo.setItems(helper.loadCustomerID());
            userIdCombo.setItems(helper.loadUserId());
            contactCombo.setItems(helper.loadContact());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
