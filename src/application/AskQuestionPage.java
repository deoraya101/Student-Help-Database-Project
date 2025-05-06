package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import application.Question;
import application.QuestionsDO;
import java.sql.SQLException; 
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * The {@code AskQuestionPage} class provides the user interface for submitting new questions.
 * 
 * <p>Users can:
 * <ul>
 *   <li>Enter a question title and description</li>
 *   <li>Submit the question to the database</li>
 *   <li>Send private messages from this page</li>
 * </ul>
 */

public class AskQuestionPage {
    private final DatabaseHelper databaseHelper;
    private final User user;

    /**
 * Constructs the AskQuestionPage with database and user info.
 * @param databaseHelper the DB helper instance
 * @param user the currently logged-in user
 */
public AskQuestionPage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
    }

    /**
 * Displays the Ask Question page UI.
 * @param primaryStage the primary stage window
 */
public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Ask a New Question");
        TextField titleField = new TextField();
        titleField.setPromptText("Enter question title");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter question description");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            if (!title.isEmpty() && !description.isEmpty()) {
                try {
                	// get current timestamp
                	Timestamp timestamp = Timestamp.from(Instant.now()); // *Taylor edit 2/18
                	
                    Question question = new Question(0, title, description, user.getUserName(), user.getName(), timestamp, true); // *Taylor edit 2/18 //Rhea TP2 2/20
                    new QuestionsDO(databaseHelper).createQuestion(question);
                    new StudentHomePage(databaseHelper).show(primaryStage, user); // *Taylor edit 2/18
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Display error message if question is empty
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error message: question cannot be empty.");
                alert.showAndWait();
            }
        });
        
        //Added by Shreya S.   
        Button privateMessageButton = new Button("Send Private Message");
        privateMessageButton.setOnAction(e -> {
            // Open a new window for private messaging
            openPrivateMessageWindow(primaryStage, user.getUserName());
        });

        layout.getChildren().addAll(titleLabel, titleField, descriptionArea, submitButton, privateMessageButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ask a Question");
    }
    
//Added by Shreya S.
    /**
 * Opens the private message window to send a message to another user.
 * @param primaryStage the primary stage to return to after
 * @param receiverUserName the recipient's username
 */
private void openPrivateMessageWindow(Stage primaryStage, String receiverUserName) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Send Private Message to " + receiverUserName);
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Type your message here...");
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            String messageText = messageArea.getText().trim();
            if (!messageText.isEmpty()) {
                try {
                    Timestamp timestamp = Timestamp.from(Instant.now());
                    Message message = new Message(0, user.getUserName(), receiverUserName, messageText, timestamp, false);
                    new MessageDO(databaseHelper).createMessage(message);

                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your private message has been sent.");
                    alert.showAndWait();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error message: message cannot be empty.");
                alert.showAndWait();
            }
        });
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage));

        layout.getChildren().addAll(titleLabel, messageArea, sendButton, backButton);

        Scene scene = new Scene(layout, 600, 300);
        Stage privateMessageStage = new Stage();
        privateMessageStage.setScene(scene);
        privateMessageStage.setTitle("Private Message");
        privateMessageStage.show();

    }
}