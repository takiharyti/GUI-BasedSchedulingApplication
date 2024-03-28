package TennyAkiharyPA;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.JDBC;

import java.io.File;
import java.io.IOException;

public class Starter extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/Views/login.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public static void main(String[] args) throws IOException {
        //Loading the login screen, therefore creating a log file beforehand.
        File loginLog = new File("login_activity.txt");
        if (loginLog.createNewFile()) {
            System.out.println("New login activity File Created");
        } else {
            System.out.println("Login activity File already exists.");
        }
        JDBC.makeConnection();
        launch(args);
    }
}
