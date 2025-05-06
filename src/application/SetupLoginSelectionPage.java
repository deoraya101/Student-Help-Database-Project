package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import databasePart1.*;

/**
 * The SetupLoginSelectionPage class allows users to choose between setting up a new account
 * or logging into an existing account. It provides two buttons for navigation to the respective pages.
 */
/**
 * The {@code SetupLoginSelectionPage} class is a landing page where users can choose
 * to either create a new account or log in to an existing one.
 */

/**
 * The {@code SetupLoginSelectionPage} class is a landing screen that lets users
 * choose whether to register a new account or log in to an existing one.
 */
public class SetupLoginSelectionPage {
	
    private final DatabaseHelper databaseHelper;

    /**
 * Constructs the login/setup selection screen.
 * @param databaseHelper the shared database connection helper
 */
public SetupLoginSelectionPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
 * Displays the page where users choose between account setup or login.
 * @param primaryStage the application stage
 */
public void show(Stage primaryStage) {
        
    	// Buttons to select Login / Setup options that redirect to respective pages
        Button setupButton = new Button("SetUp");
        Button loginButton = new Button("Login");
        
        setupButton.setOnAction(a -> {
            new SetupAccountPage(databaseHelper).show(primaryStage);
        });
        loginButton.setOnAction(a -> {
        	new UserLoginPage(databaseHelper).show(primaryStage);
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(setupButton, loginButton);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
}