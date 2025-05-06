package application;

import javafx.application.Application;
import javafx.stage.Stage;
import java.sql.SQLException;

import databasePart1.DatabaseHelper;

/**
 * The {@code StartCSE360} class is the main entry point for the JavaFX application.
 * It initializes the database and navigates users to either setup or login depending on database state.
 */

public class StartCSE360 extends Application {

	private static final DatabaseHelper databaseHelper = new DatabaseHelper();
	
	public static void main( String[] args )
	{
		 launch(args);
	}
	
	@Override
    public void start(Stage primaryStage) {
        try {
            databaseHelper.connectToDatabase(); // Connect to the database
            if (databaseHelper.isDatabaseEmpty()) {
            	
            	new FirstPage(databaseHelper).show(primaryStage);
            } else {
            	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
                
            }
        } catch (SQLException e) {
        	System.out.println(e.getMessage());
        }
    }
}