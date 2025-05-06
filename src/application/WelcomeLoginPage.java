package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import databasePart1.*;
import java.sql.SQLException; //***RS
import java.util.ArrayList;

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
/**
 * The {@code WelcomeLoginPage} class greets logged-in users and allows them to navigate to role-specific pages.
 * It also offers options for viewing roles, logging out, quitting, or accessing invitation features for admins.
 */

/**
 * The {@code WelcomeLoginPage} class greets a logged-in user and allows navigation
 * to appropriate role-based home pages or log out/quit.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;

	/**
 * Constructs the welcome page with access to the DB.
 * @param databaseHelper database access class
 */
public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
 * Displays the welcome screen with navigation based on the user's roles.
 * @param primaryStage JavaFX primary stage
 * @param user the authenticated user
 */
public void show(Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome " + user.getName() + "!");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    // Button to navigate to the user's respective page based on their role
	    Button continueButton = new Button("Continue to your Page");
	    ArrayList<String> roles = user.getRoles();
	    continueButton.setOnAction(a -> {
	    	System.out.println(roles);
	    	
	    	// If the user has selected more than one role,
	    	// takes them to a separate menu - Janelle
	    	if (roles.size() > 1) {
	    		new RolePickMenuPage(databaseHelper).show(primaryStage, user);
	    	}
	    	else if(roles.contains("admin")) {
	    		new AdminHomePage(databaseHelper).show(primaryStage, user);
	    	}
	    	else if(roles.contains("student")) {
	    		 new StudentHomePage(databaseHelper).show(primaryStage, user); 
	    	}
	    	else if(roles.contains("instructor")) {
	    		new InstructorHomePage(databaseHelper).show(primaryStage, user);
	    	}
	    	else if(roles.contains("staff")) {
	    		new StaffHomePage(databaseHelper).show(primaryStage, user);
	    	}
	    	else if(roles.contains("reviewer")) {
	    		new ReviewerHomePage(databaseHelper).show(primaryStage, user);
	    	}
	    	else { // removed the check for role "user"
	    		new UserHomePage().show(primaryStage);
	    	}
	    });
	    
	    // Button to quit the application
	    Button quitButton = new Button("Quit");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });
	    
	    Button logout = new Button("Logout");
	    logout.setOnAction(a -> {
	    	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
	    });
	    
	  //Add a "Roles" button to view user roles
	    //Accesses the user roles from the database
	    Button rolesButton = new Button("Roles");
	    rolesButton.setOnAction(a -> {
	        try {
	            ArrayList<String> rolesFromDatabase = databaseHelper.getUserRoles(user.getUserName());
	            Alert alert = new Alert(Alert.AlertType.INFORMATION);
	            alert.setTitle("Your Roles");
	            alert.setHeaderText(null);
	            alert.setContentText("Your roles: " + rolesFromDatabase);
	            alert.showAndWait();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    });
	    
	    // "Invite" button for admin to generate invitation codes
	 // "Invite" button for admin to generate invitation codes
	    if (roles.contains("admin")) {
//	    	Button listUserTable = new Button("List Users");
//	    	listUserTable.setOnAction(a -> {
//	    		try {
//		            String allUsers = databaseHelper.getAllUsers();
//		            Alert alert = new Alert(Alert.AlertType.INFORMATION);
//		            alert.setTitle("Your Roles");
//		            alert.setHeaderText(null);
//		            alert.setContentText("Your roles: " + allUsers);
//		            alert.showAndWait();
//		        } catch (SQLException e) {
//		            e.printStackTrace();
//		        }
//	    	});
	    	
            Button inviteButton = new Button("Invite");
            inviteButton.setOnAction(a -> {
                new InvitationPage().show(databaseHelper, primaryStage, user); // Talyor edit 2/18
            });
            layout.getChildren().add(inviteButton);
        }

	    layout.getChildren().addAll(welcomeLabel, continueButton, logout, quitButton, rolesButton); //***RS //Added rolesButton
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}