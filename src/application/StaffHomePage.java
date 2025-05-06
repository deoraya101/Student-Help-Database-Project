package application;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import databasePart1.DatabaseHelper;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code StaffHomePage} class defines the interface for staff users to explore questions, 
 * answers, and private messages and submit flags for content with potential issues.
 */

/**
 * The {@code StaffHomePage} class defines the UI for staff users to review and flag
 * questions, answers, and messages, as well as manage admin requests.
 */
public class StaffHomePage {
	
	private final DatabaseHelper databaseHelper;
    private final QuestionsDO questionsDO;
    private final AnswerDO answerDO;
    private final FlagDO flagDO;
	
    /**
 * Constructs the staff home page.
 * @param databaseHelper the DB helper instance
 */
public StaffHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.questionsDO = new QuestionsDO(databaseHelper);
        this.answerDO = new AnswerDO(databaseHelper);
        this.flagDO = new FlagDO(databaseHelper);
    }
	
	 /**
 * Displays the staff dashboard interface.
 * @param primaryStage the main stage
 * @param user the logged-in staff user
 */
public void show(Stage primaryStage, User user) {
    	VBox layout = new VBox(10);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label staffLabel = new Label("Hello, Staff!");
	    staffLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	    // Buttons
        Button searchButton = new Button("Search Questions");
        Button allQuestionsButton = new Button("All Questions");
        Button recentQuestionsButton = new Button("Recent Questions");
        Button viewFeedbackButton = new Button("All Users Private Feedback");
        Button flaggedMessagesButton = new Button("View Flagged Messages"); // Rhea TP4
        Button flaggedQuestionsButton = new Button("View Flagged Questions"); //Rhea TP4
        Button requestAdminActionButton = new Button("Request Admin Action"); //Added by Shreya
        Button returnButton = new Button("Return");

        // Event Handlers for Buttons
        searchButton.setOnAction(e -> handleSearch(primaryStage, user));
        allQuestionsButton.setOnAction(e -> handleAllQuestions(primaryStage, user));
        recentQuestionsButton.setOnAction(e -> handleRecentQuestions(primaryStage, user));
        returnButton.setOnAction(e -> new RolePickMenuPage(databaseHelper).show(primaryStage, user));
        viewFeedbackButton.setOnAction(e -> handleAllMessages(primaryStage, user));
        
        flaggedQuestionsButton.setOnAction(e -> handleFlaggedQuestions(primaryStage, user));
        //Shreya
        requestAdminActionButton.setOnAction(e -> {
        	Stage requestStage = new Stage();
            new AdminRequestPage(databaseHelper, user).show(requestStage);
        });
        layout.getChildren().addAll(staffLabel, searchButton, allQuestionsButton, recentQuestionsButton, viewFeedbackButton, flaggedQuestionsButton, requestAdminActionButton, returnButton);
     // Set the scene to primary stage
        Scene staffScene = new Scene(layout, 800, 400);
        primaryStage.setScene(staffScene);
        primaryStage.setTitle("Staff Page");
    }
	 
	 /**
 * Opens a UI for flagging a question with a specific color.
 */
private void createQuestionFlag(Question question, String flagColor, Stage primaryStage, User user) {
	    	VBox layout = new VBox(10);
	        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

	        Label titleLabel = new Label("Create Flag");
	        // Add a text area for creating flag comments
	        TextArea commentArea = new TextArea();
	        commentArea.setPromptText("Create Flag Comment");

	        // Submit comment
	        Button submitButton = new Button("Submit Flag Comment");
	        submitButton.setOnAction(e -> {
	            String flagText = commentArea.getText();
	            if (!flagText.isEmpty()) {
	                try {
	                    Flag flag = new Flag(0, "question", question.getId(), flagColor, flagText, user.getUserName(), user.getName(), Timestamp.from(Instant.now()));
	                    flagDO.createFlag(flag);
	                    Alert alert = new Alert(AlertType.INFORMATION);
	                    alert.setTitle("Success");
	                    alert.setHeaderText(null);
	                    alert.setContentText("Your comment has been submitted.");
	                    alert.showAndWait();
	                    displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
	                } catch (SQLException ex) {
	                    ex.printStackTrace();
	                }
	            } else {
	                // Display error message if comment is empty
	                Alert alert = new Alert(AlertType.ERROR);
	                alert.setTitle("Error");
	                alert.setHeaderText(null);
	                alert.setContentText("Error message: comment cannot be empty.");
	                alert.showAndWait();
	            }
	        });
	    
	        Button backButton = new Button("Back");
	        backButton.setOnAction(e -> displayQuestionWithReplies(primaryStage, question, user));

	        layout.getChildren().addAll(titleLabel, commentArea, submitButton, backButton);

	        Scene scene = new Scene(layout, 800, 400);
	        primaryStage.setScene(scene);
	        primaryStage.setTitle("Flag Question");
	    }
	    
	    /**
 * Allows staff to flag an answer as inappropriate.
 */
private void createAnswerFlag(Answer answer, Question question, String flagColor, Stage primaryStage, User user) {
	    	VBox layout = new VBox(10);
	        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

	        Label titleLabel = new Label("Create Flag");
	        // Add a text area for creating flag comments
	        TextArea commentArea = new TextArea();
	        commentArea.setPromptText("Create Flag Comment");

	        // Submit comment
	        Button submitButton = new Button("Submit Flag Comment");
	        submitButton.setOnAction(e -> {
	            String flagText = commentArea.getText();
	            if (!flagText.isEmpty()) {
	                try {
	                    Flag flag = new Flag(0, "answer", answer.getId(), flagColor, flagText, user.getUserName(), user.getName(), Timestamp.from(Instant.now()));
	                    flagDO.createFlag(flag);
	                    Alert alert = new Alert(AlertType.INFORMATION);
	                    alert.setTitle("Success");
	                    alert.setHeaderText(null);
	                    alert.setContentText("Your comment has been submitted.");
	                    alert.showAndWait();
	                    displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
	                } catch (SQLException ex) {
	                    ex.printStackTrace();
	                }
	            } else {
	                // Display error message if comment is empty
	                Alert alert = new Alert(AlertType.ERROR);
	                alert.setTitle("Error");
	                alert.setHeaderText(null);
	                alert.setContentText("Error message: comment cannot be empty.");
	                alert.showAndWait();
	            }
	        });
	    
	        Button backButton = new Button("Back");
	        backButton.setOnAction(e -> displayQuestionWithReplies(primaryStage, question, user));

	        layout.getChildren().addAll(titleLabel, commentArea, submitButton, backButton);

	        Scene scene = new Scene(layout, 800, 400);
	        primaryStage.setScene(scene);
	        primaryStage.setTitle("Flag Question");
	    }
	 
	 /**
 * Allows staff to flag a private message.
 */
private void createMessageFlag(Message message, String flagColor, Stage primaryStage, User user) {
	    	VBox layout = new VBox(10);
	        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

	        Label titleLabel = new Label("Create Flag");
	        // Add a text area for creating flag comments
	        TextArea commentArea = new TextArea();
	        commentArea.setPromptText("Create Flag Comment");

	        // Submit comment
	        Button submitButton = new Button("Submit Flag Comment");
	        submitButton.setOnAction(e -> {
	            String flagText = commentArea.getText();
	            if (!flagText.isEmpty()) {
	                try {
	                    Flag flag = new Flag(0, "message", message.getId(), flagColor, flagText, user.getUserName(), user.getName(), Timestamp.from(Instant.now()));
	                    flagDO.createFlag(flag);
	                    Alert alert = new Alert(AlertType.INFORMATION);
	                    alert.setTitle("Success");
	                    alert.setHeaderText(null);
	                    alert.setContentText("Your comment has been submitted.");
	                    alert.showAndWait();
	                    displayMessageWithFlags(primaryStage, message, user); // Refresh the page
	                } catch (SQLException ex) {
	                    ex.printStackTrace();
	                }
	            } else {
	                // Display error message if comment is empty
	                Alert alert = new Alert(AlertType.ERROR);
	                alert.setTitle("Error");
	                alert.setHeaderText(null);
	                alert.setContentText("Error message: comment cannot be empty.");
	                alert.showAndWait();
	            }
	        });
	        
	        Button backButton = new Button("Back");
	        backButton.setOnAction(e -> displayMessageWithFlags(primaryStage, message, user));

	        layout.getChildren().addAll(titleLabel, commentArea, submitButton, backButton);

	        Scene scene = new Scene(layout, 800, 400);
	        primaryStage.setScene(scene);
	        primaryStage.setTitle("Flag Question");
	    }
	 
	 /**
 * Opens the interface for editing a previously submitted flag.
 */
private void editFlag(Stage primaryStage, Flag flag, User user) {
	        VBox layout = new VBox(10);
	        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

	        Label titleLabel = new Label("Edit Flag Message");
	        TextField flagMessageArea = new TextField(flag.getFlagMessage());
	        Button saveButton = new Button("Save");
	        saveButton.setOnAction(e -> {
	            String newMessage = flagMessageArea.getText();
	            if (!newMessage.isEmpty()) {
	            	try {
	                    flag.setFlagMessage(newMessage);
	                    flagDO.updateFlagMessage(flag);
	                    Alert alert = new Alert(AlertType.INFORMATION);
	                    alert.setTitle("Success");
	                    alert.setHeaderText(null);
	                    alert.setContentText("Your flag has been updated.");
	                    alert.showAndWait();
	                    show(primaryStage, user); // Refresh the page
	                } catch (SQLException ex) {
	                    ex.printStackTrace();
	                }
	            }
	        });

	        Button backButton = new Button("Back");
	        backButton.setOnAction(e -> {show(primaryStage, user);
	        });

	        layout.getChildren().addAll(titleLabel, flagMessageArea, saveButton, backButton);

	        Scene scene = new Scene(layout, 800, 400);
	        primaryStage.setScene(scene);
	        primaryStage.setTitle("Edit Question");
	    }

	    /**
 * Deletes a flag from the database.
 */
private void deleteFlag(Stage primaryStage, Flag flag, User user) {
	        try {
	            flagDO.deleteFlag(flag.getFlagId());
	            Alert alert = new Alert(AlertType.INFORMATION);
	            alert.setTitle("Success");
	            alert.setHeaderText(null);
	            alert.setContentText("The flag has been deleted.");
	            alert.showAndWait();
	            show(primaryStage, user); // Refresh the page
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	    }
	 
	 /**
 * Opens the question search window for staff.
 */
private void handleSearch(Stage primaryStage, User user) {
	        // Open a new window for search
	        VBox layout = new VBox(10);
	        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

	        Label searchLabel = new Label("Enter a keyword to search:");
	        TextField searchField = new TextField();
	        searchField.setPromptText("Search...");

	        Button searchButton = new Button("Search Questions");
	        searchButton.setOnAction(e -> {
	            String keyword = searchField.getText().trim();
	            if (!keyword.isEmpty()) {
	                try {
	                    List<Question> matchingQuestions = questionsDO.searchQuestions(keyword);
	                    if (matchingQuestions.isEmpty()) {
	                        Alert alert = new Alert(AlertType.INFORMATION);
	                        alert.setTitle("Search Results");
	                        alert.setHeaderText(null);
	                        alert.setContentText("No questions found containing: " + keyword);
	                        alert.showAndWait();
	                    } else {
	                        displayQuestions(primaryStage, matchingQuestions, "Search Results", user);
	                    }
	                } catch (SQLException ex) {
	                    ex.printStackTrace();
	                }
	            }
	        });

	        Button backButton = new Button("Back");
	        backButton.setOnAction(e -> show(primaryStage, user));

	        layout.getChildren().addAll(searchLabel, searchField, searchButton, backButton);

	        Scene searchScene = new Scene(layout, 800, 400);
	        primaryStage.setScene(searchScene);
	        primaryStage.setTitle("Search Questions");
	    }
	 
    /**
 * Displays all submitted questions.
 */
private void handleAllQuestions(Stage primaryStage, User user) {
        try {
            List<Question> allQuestions = questionsDO.readQuestions();
            if (allQuestions.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("All Questions");
                alert.setHeaderText(null);
                alert.setContentText("No questions found.");
                alert.showAndWait();
            } else {
                displayQuestions(primaryStage, allQuestions, "All Questions", user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Taylor added 2/19/25 
    /**
 * Loads questions submitted within the last 24 hours.
 */
private void handleRecentQuestions(Stage primaryStage, User user) {
        // Display all questions asked within that last 24 hours
        try {
            List<Question> recentQuestions = questionsDO.readRecentQuestions();
            if (recentQuestions.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("All Questions");
                alert.setHeaderText(null);
                alert.setContentText("No questions found.");
                alert.showAndWait();
            } else {
                displayQuestions(primaryStage, recentQuestions, "Recent Questions (asked in the last 24 hours)", user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
 * Renders a list of questions with flag indicators.
 */
private void displayQuestions(Stage primaryStage, List<Question> questions, String title, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        for (Question question : questions) {
            HBox questionBox = new HBox(10); // Create an HBox for each question
            questionBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
            
            // Add a green check mark if the question is resolved
            Label resolvedLabel = new Label();
            if (question.isResolved()) {
                resolvedLabel.setText("✓");
                resolvedLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            }
            
            // Add a button to view the question and its replies
            Button questionButton = new Button(question.getTitle());
            questionButton.setOnAction(e -> displayQuestionWithReplies(primaryStage, question, user));
            questionBox.getChildren().addAll(resolvedLabel, questionButton);
            
            // display flag
            boolean hasRed = false;
            boolean hasYellow = false;
            try {
	            List<Flag> flags = flagDO.readFlags(question.getId(), "question");
	            for (Flag flag : flags) {
	            	if(flag.getFlagColor().equals("Red")) {
	            		hasRed = true;
	            	} else if (flag.getFlagColor().equals("Yellow")) {
	            		hasYellow = true;
	            	}
	            }
            }
	        catch (SQLException ex) {
	            ex.printStackTrace();
	        }
            
            // display the most urgent flag
            if (hasRed) {
            	Label redFlag = new Label("🚩");
            	redFlag.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-padding: 2px;");
            	questionBox.getChildren().add(redFlag);
            }
            else if (hasYellow) {
            	Label yellowFlag = new Label("🚩");
            	yellowFlag.setStyle("-fx-text-fill: #ffd500; -fx-font-size: 20px; -fx-padding: 2px;");
            	questionBox.getChildren().add(yellowFlag);
            }

            // Add the question box to the layout
            layout.getChildren().add(questionBox);
        }

        // Add a back button to return to the previous page
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));
        layout.getChildren().add(backButton);

        // Set the scene and stage
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
    }
	 
    /**
 * Shows a specific question along with replies and associated flags.
 */
private void displayQuestionWithReplies(Stage primaryStage, Question question, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Display question details
        Label askerLabel = new Label("Asker: " + question.getAuthorName());
        Label timestampLabel = new Label("Timestamp: " + question.getTimestamp());
        Label titleLabel = new Label("Title: " + question.getTitle());
        Label descriptionLabel = new Label("Question: " + question.getDescription());

        layout.getChildren().addAll(askerLabel, timestampLabel, titleLabel, descriptionLabel);
        
        // Add flag buttons for the question
        Button yellowFlagButton = new Button("Yellow Flag Question");
        yellowFlagButton.setStyle("-fx-background-color: #fff600; -fx-text-fill: black;");
        yellowFlagButton.setOnAction(e -> createQuestionFlag(question, "Yellow", primaryStage, user));
        layout.getChildren().add(yellowFlagButton);
        
        Button redFlagButton = new Button("Red Flag Question");
        redFlagButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: black;");
        redFlagButton.setOnAction(e -> createQuestionFlag(question, "Red", primaryStage, user));
        layout.getChildren().add(redFlagButton);
        
        // display all flags for the question
        try {
            List<Flag> flags = flagDO.readFlags(question.getId(), "question");
            if (flags.isEmpty()) {
                Label noQuestionFlagsLabel = new Label("No flags yet.");
                layout.getChildren().add(noQuestionFlagsLabel);
            } else {
                Label questionFlagsLabel = new Label("Question Flags:");
                layout.getChildren().add(questionFlagsLabel);
                for (Flag flag : flags) {
                	HBox flagBox = new HBox(10);
                	if(flag.getFlagColor().equals("Yellow")) {
                		flagBox.setStyle("-fx-border-color: #fff600; -fx-border-width: 2; -fx-padding: 10;");
                	}
                	else if(flag.getFlagColor().equals("Red")) {
                		flagBox.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2; -fx-padding: 10;");
                	}
                    Label flagLabel = new Label(flag.getFlagColor() + " flag by " + flag.getStaffName() + ": " + flag.getFlagMessage());
                    flagBox.getChildren().add(flagLabel);
                    layout.getChildren().add(flagBox);
                    
                    // add edit and delete flag buttons
                    if (flag.getStaffUserName().equals(user.getUserName())) {
                    	Button editQFlag = new Button("Edit Flag");
                    	editQFlag.setOnAction(e -> editFlag(primaryStage, flag, user));
                    	flagBox.getChildren().add(editQFlag);
                    	
                    	Button deleteQFlag = new Button("Delete Flag");
                    	deleteQFlag.setOnAction(e -> deleteFlag(primaryStage, flag, user));
                    	flagBox.getChildren().add(deleteQFlag);
                    }
                }
            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

        // Display all replies
        try {
            List<Answer> answers = answerDO.readAnswers(question.getId());
            if (answers.isEmpty()) {
                Label noAnswersLabel = new Label("No replies yet.");
                layout.getChildren().add(noAnswersLabel);
            } else {
                Label repliesLabel = new Label("Replies:");
                layout.getChildren().add(repliesLabel);
                
                for (Answer answer : answers) {
                    VBox replyBox = new VBox(10);
                    replyBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

                    // Display the reply author and content
                    Label answerLabel = new Label("Reply by " + answer.getAuthorName() + ": " + answer.getContent());
                    replyBox.getChildren().add(answerLabel);
                    
                 // display all flags for the answer
                    try {
                        List<Flag> flags = flagDO.readFlags(answer.getId(), "answer");
                        if (flags.isEmpty()) {
                            Label noAnswerFlagsLabel = new Label("No flags yet.");
                            replyBox.getChildren().add(noAnswerFlagsLabel);
                        } else {
                            Label answerFlagsLabel = new Label("Answer Flags:");
                            replyBox.getChildren().add(answerFlagsLabel);
                            for (Flag flag : flags) {
                            	HBox flagBox = new HBox(10);
                            	if(flag.getFlagColor().equals("Yellow")) {
                            		flagBox.setStyle("-fx-border-color: #fff600; -fx-border-width: 2; -fx-padding: 10;");
                            	}
                            	else if(flag.getFlagColor().equals("Red")) {
                            		flagBox.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2; -fx-padding: 10;");
                            	}
                                Label flagLabel = new Label(flag.getFlagColor() + " flag by " + flag.getStaffName() + ": " + flag.getFlagMessage());
                                flagBox.getChildren().add(flagLabel);
                                replyBox.getChildren().add(flagBox);
                                
                                // add edit flag button
                                if (flag.getStaffUserName().equals(user.getUserName())) {
                                	Button editAFlag = new Button("Edit Flag");
                                	editAFlag.setOnAction(e -> editFlag(primaryStage, flag, user));
                                	flagBox.getChildren().add(editAFlag);
                                	
                                	Button deleteAFlag = new Button("Delete Flag");
                                	deleteAFlag.setOnAction(e -> deleteFlag(primaryStage, flag, user));
                                	flagBox.getChildren().add(deleteAFlag);
                                }
                            }
                        }
            	        } catch (SQLException e) {
            	            e.printStackTrace();
            	        }
                    
                    // Add a buttons to flag the answer
                    Button yellowAnswerFlagButton = new Button("Yellow Flag Answer");
                    yellowAnswerFlagButton.setStyle("-fx-background-color: #fff600; -fx-text-fill: black;");
                    yellowAnswerFlagButton.setOnAction(e -> createAnswerFlag(answer, question, "Yellow", primaryStage, user));
                    replyBox.getChildren().add(yellowAnswerFlagButton);
                    
                    Button redAnswerFlagButton = new Button("Red Flag Answer");
                    redAnswerFlagButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: black;");
                    redAnswerFlagButton.setOnAction(e -> createAnswerFlag(answer, question, "Red", primaryStage, user)); //Taylor edit 4/17
                    replyBox.getChildren().add(redAnswerFlagButton);
                    
                    layout.getChildren().add(replyBox);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }   

        // Add Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> handleAllQuestions(primaryStage, user));
      
        layout.getChildren().add(backButton);

        // Set the scene and stage
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Details");
    }
   
    private void handleAllMessages(Stage primaryStage, User user) {
        // Display all messages
        try {
            List<Message> allMessages = flagDO.readAllMessages();
            if (allMessages.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("All Messages");
                alert.setHeaderText(null);
                alert.setContentText("No messages found.");
                alert.showAndWait();
            } else {
            	displayAllMessages(primaryStage, allMessages, "All User Messages", user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void displayAllMessages(Stage primaryStage, List<Message> messages, String title, User user) {
    	VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        for (Message message : messages) {
            HBox messageBox = new HBox(10); // Create an HBox for each message
            messageBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
            
            // Add a button to view the message
            Button questionButton = new Button("Message by " + message.getSenderUserName() + ": " + message.getContent());
            questionButton.setOnAction(e -> displayMessageWithFlags(primaryStage, message, user));
            messageBox.getChildren().add(questionButton);
            
            // display flags
            boolean hasRed = false;
            boolean hasYellow = false;
            try {
	            List<Flag> flags = flagDO.readFlags(message.getId(), "message");
	            for (Flag flag : flags) {
	            	if(flag.getFlagColor().equals("Red")) {
	            		hasRed = true;
	            	} else if (flag.getFlagColor().equals("Yellow")) {
	            		hasYellow = true;
	            	}
	            }
            }
	        catch (SQLException ex) {
	            ex.printStackTrace();
	        }
            
            // display the most urgent flag
            if (hasRed) {
            	Label redFlag = new Label("🚩");
            	redFlag.setStyle("-fx-text-fill: red; -fx-font-size: 20px; -fx-padding: 2px;");
            	messageBox.getChildren().add(redFlag);
            }
            else if (hasYellow) {
            	Label yellowFlag = new Label("🚩");
            	yellowFlag.setStyle("-fx-text-fill: #ffd500; -fx-font-size: 20px; -fx-padding: 2px;");
            	messageBox.getChildren().add(yellowFlag);
            }

            // Add the question box to the layout
            layout.getChildren().add(messageBox);
        }

        // Add a back button to return to the previous page
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));
        layout.getChildren().add(backButton);

        // Set the scene and stage
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
    }
    
    private void displayMessageWithFlags(Stage primaryStage, Message message, User user) {
    	VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Display message details
        Label messageLabel = new Label("Message by " + message.getSenderUserName() + ": " + message.getContent());
        layout.getChildren().add(messageLabel);
        
     // Add flag buttons for the message
        Button yellowFlagButton = new Button("Yellow Flag Message");
        yellowFlagButton.setStyle("-fx-background-color: #fff600; -fx-text-fill: black;");
        yellowFlagButton.setOnAction(e -> createMessageFlag(message, "Yellow", primaryStage, user));
        layout.getChildren().add(yellowFlagButton);
        
        Button redFlagButton = new Button("Red Flag Message");
        redFlagButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: black;");
        redFlagButton.setOnAction(e -> createMessageFlag(message, "Red", primaryStage, user));
        layout.getChildren().add(redFlagButton);
        
        // display all flags for the message
        try {
            List<Flag> flags = flagDO.readFlags(message.getId(), "message");
            if (flags.isEmpty()) {
                Label noMessageFlagsLabel = new Label("No flags yet.");
                layout.getChildren().add(noMessageFlagsLabel);
            } else {
                Label messageFlagsLabel = new Label("Message Flags:");
                layout.getChildren().add(messageFlagsLabel);
                for (Flag flag : flags) {
                	HBox flagBox = new HBox(10);
                	if(flag.getFlagColor().equals("Yellow")) {
                		flagBox.setStyle("-fx-border-color: #fff600; -fx-border-width: 2; -fx-padding: 10;");
                	}
                	else if(flag.getFlagColor().equals("Red")) {
                		flagBox.setStyle("-fx-border-color: #ff4444; -fx-border-width: 2; -fx-padding: 10;");
                	}
                    Label flagLabel = new Label(flag.getFlagColor() + " flag by " + flag.getStaffName() + ": " + flag.getFlagMessage());
                    flagBox.getChildren().add(flagLabel);
                    layout.getChildren().add(flagBox);
                    
                    // add edit flag button
                    if (flag.getStaffUserName().equals(user.getUserName())) {
                    	Button editAFlag = new Button("Edit Flag");
                    	editAFlag.setOnAction(e -> editFlag(primaryStage, flag, user));
                    	flagBox.getChildren().add(editAFlag);
                    	
                    	Button deleteMFlag = new Button("Delete Flag");
                    	deleteMFlag.setOnAction(e -> deleteFlag(primaryStage, flag, user));
                    	flagBox.getChildren().add(deleteMFlag);
                    }
                }
            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

        // Add Back button
        try {
        	List<Message> allMessages = flagDO.readAllMessages();
        	Button backButton = new Button("Back");
            backButton.setOnAction(e -> displayAllMessages(primaryStage, allMessages, "All User Messages", user));
            layout.getChildren().add(backButton);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Set the scene and stage
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Details");
    }
 
    //Rhea TP4
    private void handleFlaggedQuestions(Stage primaryStage, User user) {
        try {
            List<FlaggedContent> flaggedQuestions = flagDO.getFlaggedQuestions();
            if (flaggedQuestions.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Flagged Questions");
                alert.setHeaderText(null);
                alert.setContentText("No flagged questions found.");
                alert.showAndWait();
            } else {
                displayFlaggedQuestions(primaryStage, flaggedQuestions, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to retrieve flagged questions: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void displayFlaggedQuestions(Stage primaryStage, List<FlaggedContent> flaggedQuestions, User user) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Label titleLabel = new Label("Flagged Questions Dashboard");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");

        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        for (FlaggedContent flagged : flaggedQuestions) {
            VBox flaggedBox = new VBox(10);
            flaggedBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 15;");
            flaggedBox.setMaxWidth(Double.MAX_VALUE);

            Label questionContent = new Label("Question: " + flagged.getContent());
            questionContent.setWrapText(true);
            
            HBox questionInfo = new HBox(10);
            questionInfo.getChildren().addAll(
                new Label("Author: " + flagged.getAuthorName()),
                new Label("Posted: " + flagged.getContentTimestamp())
            );

            Label flagHeader = new Label("Flagging Details:");
            flagHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #d9534f;");
            
            VBox flagDetails = new VBox(5);
            flagDetails.getChildren().addAll(
                new Label("Flagged by: " + flagged.getFlaggerName()),
                new Label("Urgency: " + flagged.getUrgency()),
                new Label("Reason: " + flagged.getReason()),
                new Label("Flagged at: " + flagged.getFlaggedAt())
            );

            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            Button viewButton = new Button("View Question");
            viewButton.setOnAction(e -> {
                try {
                    Question question = questionsDO.getQuestionById(flagged.getContentId());
                    if (question != null) {
                        displayQuestionWithReplies(primaryStage, question, user);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
            
            Button dismissButton = new Button("Dismiss Flag");
            dismissButton.setStyle("-fx-background-color: #5cb85c; -fx-text-fill: white;");
            dismissButton.setOnAction(e -> {
                try {
                    flagDO.dismissFlag(flagged.getId());
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Flag has been dismissed.");
                    alert.showAndWait();
                    handleFlaggedQuestions(primaryStage, user);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Failed to dismiss flag: " + ex.getMessage());
                    alert.showAndWait();
                }
            });

            buttonBox.getChildren().addAll(viewButton, dismissButton);
            
            flaggedBox.getChildren().addAll(
                questionContent, questionInfo,
                new Separator(),
                flagHeader, flagDetails,
                buttonBox
            );
            
            contentBox.getChildren().add(flaggedBox);
        }

        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));

        layout.getChildren().addAll(titleLabel, scrollPane, backButton);
        
        Scene scene = new Scene(layout, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Flagged Questions Dashboard");
    }
    public List<FlaggedContent> getFlaggedQuestions() throws SQLException {
        List<FlaggedContent> flaggedContents = new ArrayList<>();
        String query = "SELECT f.id, f.targetType, f.targetId, f.flagColor, f.flagMessage, " +
                      "f.staffUserName, f.staffName, f.timestamp as flaggedAt, " +
                      "q.authorName, q.title, q.description, q.timestamp as contentTimestamp " +
                      "FROM Flags f JOIN Questions q ON f.targetId = q.id " +
                      "WHERE f.targetType = 'question' " +
                      "ORDER BY f.timestamp DESC";
        
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                flaggedContents.add(new FlaggedContent(
                    rs.getInt("id"),
                    rs.getString("targetType"),
                    rs.getInt("targetId"),
                    rs.getString("flagColor"),
                    rs.getString("flagMessage"),
                    rs.getString("staffUserName"),
                    rs.getString("staffName"),
                    rs.getTimestamp("flaggedAt"),
                    rs.getString("authorName"),
                    rs.getString("title") + ": " + rs.getString("description"),
                    rs.getTimestamp("contentTimestamp")
                ));
            }
        }
        return flaggedContents;
    }
}