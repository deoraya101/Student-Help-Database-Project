package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;
import java.util.ArrayList;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
/**
 * The {@code SetupAccountPage} class handles user registration and account creation.
 * It validates inputs such as name, email, userName, password, and invitation code before creating the account.
 */

/**
 * The {@code SetupAccountPage} class handles new account creation,
 * including field validation and database registration logic.
 */
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    /**
 * Constructs the SetupAccountPage.
 * @param databaseHelper DB helper instance
 */
public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    /**
 * Displays the new user registration screen.
 * @param primaryStage JavaFX stage
 */
public void show(Stage primaryStage) {
    	// Input fields for name, userName, password, and invitation code
    	TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");
        nameField.setMaxWidth(250);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email address");
        emailField.setMaxWidth(250);
    	
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter InvitationCode");
        inviteCodeField.setMaxWidth(250);
        
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
            String code = inviteCodeField.getText();
            
            try {
            	// Check if the user already exists
            	if(!databaseHelper.doesUserExist(userName)) {
            		
            		// Check if the name is valid
            		if(NameValidator.checkForValidName(name) == "") {
            			
            			// Check if the email is valid
            			// For the email validation I adapted the Regex format from the website HowToDoInJava (https://howtodoinjava.com/java/regex/java-regex-validate-email-address/)
            			if(email.matches("^[A-Za-z0-9+_.-]+@(.+)$") && email.length() < 20) {
            			
		        			// Check if the userName and password are valid
		        			if(UserNameRecognizer.checkForValidUserName(userName) == "" && PasswordEvaluator.evaluatePassword(password) == "") {
		        		
			            		// Validate the invitation code
			            		if(databaseHelper.validateInvitationCode(code)) {
			            			// Create a new user and register them in the database
			            			ArrayList<String> roles = new ArrayList<String>();
					            	User user=new User(name, email, userName, password, roles);
			            			databaseHelper.register(user);
			            				
			            			// Navigate to the User Login Page
			    	                new UserLoginPage(databaseHelper).show(primaryStage);
					            
			            		}
			            		else {
			            			errorLabel.setText("Please enter a valid invitation code");
			            		}
		        			}
		        			// Set helpful error messages for the userName and password
		        			else {
		        				errorLabel.setText(UserNameRecognizer.checkForValidUserName(userName) + "\n" + PasswordEvaluator.evaluatePassword(password));
		        			}
            			}
            			// Set helpful error message for the email
            			else {
            				errorLabel.setText("An email must be in the format [A-Z,a-z,0-9-_.]@[A-Z,a-z].[A-Z,a-z] and less than 20 characters");
            			}
            		}
            		// Set helpful error messages for the name
            		else {
            			errorLabel.setText(NameValidator.checkForValidName(name));
            		}
            	}
            	else {
            		errorLabel.setText("This useruserName is taken!!.. Please use another to setup an account");
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new SetupLoginSelectionPage(databaseHelper).show(primaryStage));

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 10; -fx-alignment: center;");
        layout.getChildren().addAll(nameField, emailField, userNameField, passwordField,inviteCodeField, setupButton, errorLabel, backButton);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}