package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyEvent;
import util.JDBC;
import util.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.util.ResourceBundle;

/**
 * Controller class for the login view.
 *
 * @author Tenny Akihary
 */
public class Login implements Initializable {

    //String variable to hold the username for additions and updates
    private static String uName;

    //resource bundle variable to use in failed login attempt
    ResourceBundle rB;

    //FXML field variables
    @FXML
    private Button loginButton;
    @FXML
    private Label title;
    @FXML
    private Label zoneID;
    @FXML
    private Label loginError;
    @FXML
    private Label userLabel;
    @FXML
    private Label passLabel;
    @FXML
    private TextField userText;
    @FXML
    private PasswordField passText;

    /**
     * loginClicked
     * Method for when login button is clicked.
     * LAMBDA USE: THe use of lambda in a KeyEvent handler. After a failed login attempt the loginError label
     * will be displayed. Once the user uses their keyboard to make another attempt, the event listener will trigger
     * and the loginError will be cleared until another login attempt is made.
     * @param event Clicked the button.
     * @throws SQLException info on database access error
     */
    public void loginClicked(ActionEvent event) throws SQLException {

        //values gathered from the textboxes that will be input or stored in the text file or checked for appointments in 15 minute proximity
        uName = userText.getText();
        String pass = passText.getText();
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime nowPlusFifteen = now.plusMinutes(15);
        ZoneId locale = ZoneId.systemDefault();

        //Get connection and query from DB
        Connection conn = JDBC.getConnection();
        PreparedStatement query = conn.prepareStatement("SELECT * FROM users WHERE User_Name = ? AND Password = ?");
        query.setString(1, uName);
        query.setString(2, pass);
        ResultSet r = query.executeQuery();

        try {

            //Check if the login was successful, granting access or else giving error. Logging attempt.
            FileWriter writingToFile = new FileWriter("login_activity.txt", true);
            if (r.next()) {
                writingToFile.append(String.valueOf(now)).append("UTC USERNAME: ").append(uName).append
                        ("...SUCCESS FROM ").append(String.valueOf(locale)).append("\n");
                writingToFile.close();

                //Get appointment within the next 15 minutes
                String userId = helper.getUserId(uName);
                Connection c = JDBC.getConnection();
                PreparedStatement q = c.prepareStatement("SELECT Appointment_ID, Start FROM " +
                        "appointments WHERE User_ID = ? AND Start BETWEEN ? AND ?;");
                q.setString(1, userId);
                q.setString(2, now.format(helper.getFormat()));
                q.setString(3, nowPlusFifteen.format(helper.getFormat()));
                ResultSet rS = q.executeQuery();

                //if there is an appointment within the next 15 minutes then create appointment object and notify the user
                if(rS.next()){

                    String appId = rS.getString("Appointment_ID");
                    String start = rS.getString("Start");
                    LocalDateTime startLDT = LocalDateTime.parse(start, helper.getFormat());
                    ZonedDateTime startZDT = ZonedDateTime.of(startLDT, ZoneOffset.UTC);
                    startZDT = helper.timeConvert(startZDT, 0);

                    helper.strAlert("You have an appointment within the next 15 minutes!!! \nAppointment ID:" +
                            " " + appId + "\n Appointment Date & Time: " + startZDT.format(helper.getFormat())
                            + " " + locale + "\n...CONTINUING TO LOGIN");

                } else {
                    helper.strAlert("No appointments in the next 15 minutes... CONTINUING TO LOGIN");
                }

                //load the main view
                util.helper.loadView(event, "main");

            } else {
                writingToFile.append(String.valueOf(now)).append("UTC- USERNAME: ").append(uName).append
                        ("...FAILED FROM ").append(String.valueOf(locale)).append("\n");
                writingToFile.close();
                loginError.setText(rB.getString("loginError"));
                userText.addEventFilter(KeyEvent.ANY, e -> loginError.setText(""));
            }
        } catch (IOException error) {
            error.printStackTrace();
        }


    }

    /**
     * Method for getting the username of the one that is logged in.
     * @return username
     */
    public static String getUser(){
        return uName;
    }

    /**
     * initialization of Login
     * @param url location
     * @param resourceBundle resource
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //Get the resource bundle
        resourceBundle = ResourceBundle.getBundle("login");

        //set rB so the resource bundle is accessible throughout the controller
        rB = resourceBundle;

        //Fill the text with language-dependent text
        zoneID.setText(ZoneId.systemDefault().toString());
        title.setText(resourceBundle.getString("title"));
        loginButton.setText(resourceBundle.getString("loginButton"));
        userLabel.setText(resourceBundle.getString("userLabel"));
        passLabel.setText(resourceBundle.getString("passLabel"));

    }

}