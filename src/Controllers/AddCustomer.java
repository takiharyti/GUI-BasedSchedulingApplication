package Controllers;

import Model.Customer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import util.helper;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * AddCustomer
 * Controller class for the AddCustomer view.
 * Provides fields to input a customer into the database.
 *
 * @author Tenny Akihary
 */
public class AddCustomer implements Initializable {

    //Fields and buttons provided.
    @FXML
    private Button addButton;
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

    /**
     * addButtonClicked
     * Check the user's input for errors and
     * add the data to the database.
     * @param event addButton clicked
     * @throws SQLException
     * @throws IOException
     */
    public void addButtonClicked(ActionEvent event) throws SQLException, IOException {

        String cName = customerNameText.getText();
        String cAddr = addressText.getText();
        String post = postalText.getText();
        String phone = phoneText.getText();
        String country = countryCombo.getValue();
        String div = divisionCombo.getValue();

        if (cName.isBlank() || cAddr.isBlank() || post.isBlank() || phone.isBlank() || country.isBlank() || div.isBlank()){
            helper.strAlert("Make sure all fields are filled out and try again!");
        } else {
            Customer addCust = new Customer(cName, cAddr, post, phone, div, country);
            addCust.addToDB();
            helper.loadView(event, "main");
        }


    }

    /**
     * cancelButtonClicked
     * Loads the main view.
     * @param event cancelButton clicked.
     * @throws IOException
     */
    public void cancelButtonClicked(ActionEvent event) throws IOException {
        helper.strAlert("Nothing was added... Heading back...");
        helper.loadView(event, "main");
    }

    /**
     * conComboBoxPicked
     * Sets the items in the division combo box based on the selected country.
     * @param event selection in the country combo box.
     */
    public void conComboBoxPicked(ActionEvent event){
        try {
            divisionCombo.setItems(helper.loadDivs(countryCombo.getSelectionModel().getSelectedItem()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * initialize
     * Initializes the AddCustomer stage.
     * @param url Stage path.
     * @param resourceBundle Resources.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            countryCombo.setItems(helper.loadCountries());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
