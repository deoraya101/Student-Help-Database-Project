package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;
import java.util.ArrayList;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
/**
 * The AdminSetupPage class handles the setup of the first administrator account.
 * 
 * <p>This page is shown only during the initial configuration to allow a user to register as an admin.
 * It includes validations for name, email, username, and password.
 */


/**
 * The {@code AdminSetupPage} class allows the first user to create an admin account.
 *
 * <p>Includes UI for entering name, email, username, and password, and performs
 * validation before creating the admin account and navigating to the login page.</p>
 */

public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;

    /**
 * Constructs the setup page with access to the database.
 * @param databaseHelper database interface
 */
public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
 * Displays the admin setup page.
 * @param primaryStage the primary application window
 */
public void show(Stage primaryStage) {
    	// Input fields for name, email, userName and password
    	TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");
        nameField.setMaxWidth(250);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email address");
        emailField.setMaxWidth(250);
    	
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
        	String name = nameField.getText();
        	String email = emailField.getText();
            String userName = userNameField.getText();
            String password = passwordField.getText();
            try {
            	// Check if the name is valid
        		if(NameValidator.checkForValidName(name) == "") {
        			
        			// Check if the email is valid
        			// For the email validation I adapted the Regex format from the website HowToDoInJava (https://howtodoinjava.com/java/regex/java-regex-validate-email-address/)
        			if(email.matches("^[A-Za-z0-9+_.-]+@(.+)$") && email.length() < 20) {
        			
		        		// Check if the userName and password is valid
		        		if(UserNameRecognizer.checkForValidUserName(userName) == "" && PasswordEvaluator.evaluatePassword(password) == "") {
		        			// Create a new User object with admin role and register in the database
		        			ArrayList<String> roles = new ArrayList<String>();
		        			
		        		User user=new User(name, email, userName, password, roles);
			            //user.addRole("admin"); NOTE: This causes an error but works without
			            databaseHelper.register(user);
			            databaseHelper.addUserRole(user, "admin");
			            System.out.println("Administrator setup completed.");
			                 
			              

			                // Navigate to the User Login Page
			                new UserLoginPage(databaseHelper).show(primaryStage);
			                
		        		}
		        		else{
		        			errorLabel.setText(UserNameRecognizer.checkForValidUserName(userName) + " \n" + PasswordEvaluator.evaluatePassword(password));
		        		}
        			}
        			else {
        				errorLabel.setText("An email must be in the format [A-Z,a-z,0-9-_.]@[A-Z,a-z].[A-Z,a-z] and less than 20 characters");
        			}
        		}
        		else {
        			errorLabel.setText(NameValidator.checkForValidName(name));
        		}
            		
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10, nameField, emailField, userNameField, passwordField, setupButton, errorLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }
}