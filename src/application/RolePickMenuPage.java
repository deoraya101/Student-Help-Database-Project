package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import databasePart1.DatabaseHelper;

// New file - added by Janelle 
/**
 * The RolePickMenuPage class allows users who have selected multiple roles to choose which page they would
 * like to access. It displays a screen with several buttons for navigation to the respective pages.
 */
/**
 * The {@code RolePickMenuPage} class allows users with multiple assigned roles to select
 * which role-specific homepage they would like to access.
 */

/**
 * The {@code RolePickMenuPage} class presents a menu for users with multiple roles
 * to pick which role-specific homepage they wish to access.
 */
public class RolePickMenuPage {
	
	private final DatabaseHelper databaseHelper;
	/**
 * Constructs the role pick menu page.
 * @param databaseHelper shared DB access
 */
public RolePickMenuPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
	
    /**
 * Displays the role selection interface and redirects to the appropriate homepages.
 * @param primaryStage JavaFX stage
 * @param user current user with multiple roles
 */
public void show(Stage primaryStage, User user) {
        
    	// Buttons with names of each role chosen by the given user to redirect to respective pages
	    // Accesses the user's roles from the User class
        Button studentButton = new Button("Student");
        Button instructorButton = new Button("Instructor");
        Button staffButton = new Button("Staff");
        Button reviewerButton = new Button("Reviewer");
        
        studentButton.setOnAction(a -> {
        	new StudentHomePage(databaseHelper).show(primaryStage, user);
        });
        instructorButton.setOnAction(a -> {
        	new InstructorHomePage(databaseHelper).show(primaryStage, user);
        });
        staffButton.setOnAction(a -> {
        	new StaffHomePage(databaseHelper).show(primaryStage, user); 
        });
        reviewerButton.setOnAction(a -> {
        	new ReviewerHomePage(databaseHelper).show(primaryStage, user);
        });
        

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
// ***RS Back button to go back to WelcomeLoginPage
        
        Button backButton = new Button("Back");
        backButton.setOnAction(a -> {
        	WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);
        	welcomeLoginPage.show(primaryStage, user);
        });

        ArrayList<String> roles = user.getRoles();

        if(roles.contains("student")) {
        	layout.getChildren().add(studentButton);
        }
        if(roles.contains("instructor")) {
        	layout.getChildren().add(instructorButton);
        }
        if(roles.contains("staff")) {
        	layout.getChildren().add(staffButton);
        }
        if(roles.contains("reviewer")) {
        	layout.getChildren().add(reviewerButton);
        }
        
        layout.getChildren().add(backButton);
        
        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}