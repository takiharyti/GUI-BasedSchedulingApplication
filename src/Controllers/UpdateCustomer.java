package Controllers;

import Model.Customer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import util.JDBC;
import util.helper;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * UpdateCustomer
 * Controller class for the UpdateCustomer.fxml view
 *
 * @author Tenny Akihary
 */
public class UpdateCustomer implements Initializable {

    //Fields for user input/updates
    @FXML
    private Button updateButton;
    @FXML
    private Button cancelButton;
    @FXML
    private TextField customerId;
    @FXML
    private TextField customerNameText;
    @FXML
    private TextField addressText;
    @FXML
    private TextField postalText;
    @FXML
    private TextField phoneText;
    @FXML
    private ComboBox<String> countryCombo;
    @FXML
    private ComboBox<String> divisionCombo;

    //Variable to hold the country name
    private String country;

    //placeholder for the previously selected ID
    private String tempId;

    /**
     * updateButtonClicked
     * Checks user input/updates and updates into the database.
     * @param event updateButton clicked.
     * @throws SQLException
     * @throws IOException
     */
    public void updateButtonClicked(ActionEvent event) throws SQLException, IOException {

        //pull values from fields
        String cName = customerNameText.getText();
        String cAddr = addressText.getText();
        String post = postalText.getText();
        String phone = phoneText.getText();
        String country = countryCombo.getValue();
        String div = divisionCombo.getValue();

        //check for blank fields and update into database
        if (cName.isBlank() || cAddr.isBlank() || post.isBlank() || phone.isBlank() || country.isBlank() || div.isBlank()){
            helper.strAlert("Make sure all fields are filled out and try again!");
        } else {
            Customer updateCust = new Customer(tempId, cName, cAddr, post, phone, div, country);
            updateCust.updateToDB();
            helper.loadView(event, "main");
        }

    }

    /**
     * conComboBoxPicked
     * Updates division combo box based on country selection.
     * @param event conComboBox selected.
     */
    public void conComboBoxPicked(ActionEvent event){
        //checks for change in selection
        if(countryCombo.getSelectionModel().getSelectedItem() != country) {
            try {
                //updates division combobox
                divisionCombo.setItems(helper.loadDivs(countryCombo.getSelectionModel().getSelectedItem()));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    /**
     * cancelButtonClicked
     * Returns to main view.
     * @param event cancelButton clicked.
     * @throws IOException
     */
    public void cancelButtonClicked(ActionEvent event) throws IOException {
        helper.strAlert("Nothing was updated... Heading back...");
        helper.loadView(event, "main");
    }

    /**
     * initialize
     * Initializes the UpdateCustomer view.
     * Prefills the fields with selected customer from main view.
     * @param url Stage location.
     * @param resourceBundle Resources.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            //pulls id from selection from previous stage
            tempId = Main.getiD();
            //connects to database and executes query
            Connection conn = JDBC.getConnection();
            PreparedStatement statement = conn.prepareStatement("SELECT u.Customer_ID, u.Customer_Name, u.Address, u.Postal_Code, u.Phone, d.Division, o.Country \n" +
                    "FROM customers as u \n" +
                    "RIGHT OUTER JOIN first_level_divisions as d ON u.Division_ID = d.Division_ID\n" +
                    "RIGHT OUTER JOIN countries as o ON d.Country_ID = o.Country_ID\n" +
                    "WHERE Customer_ID = "+tempId+";");
            ResultSet resultSet = statement.executeQuery();
            //Checks for values
            if (resultSet.next()) {
                //Fills fields with values
                customerId.setText(resultSet.getString("Customer_ID"));
                customerNameText.setText(resultSet.getString("Customer_Name"));
                addressText.setText(resultSet.getString("Address"));
                postalText.setText(resultSet.getString("Postal_Code"));
                phoneText.setText(resultSet.getString("Phone"));
                divisionCombo.setValue(resultSet.getString("Division"));
                countryCombo.setItems(helper.loadCountries());
                country = resultSet.getString("Country");
                countryCombo.setValue(country);
                divisionCombo.setItems(helper.loadDivs(country));
            } else {
                //flags user for return
                helper.strAlert("ERROR LOADING DATA... RETURN TO MAIN");
            }

        } catch (SQLException throwables){
            throwables.printStackTrace();
        }

    }
}
