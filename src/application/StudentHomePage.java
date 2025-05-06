package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import databasePart1.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * The {@code StudentHomePage} class provides the main dashboard for student users.
 * It allows access to ask questions, view answers, submit follow-ups, manage private messages,
 * request reviewer status, and more.
 */

/**
 * The {@code StudentHomePage} class provides the main dashboard for student users,
 * allowing them to interact with questions, replies, reviews, messages, and more.
 */
public class StudentHomePage {
    private final DatabaseHelper databaseHelper;
    private final QuestionsDO questionsDO;
    private final AnswerDO answerDO;
    private final ReviewDO reviewDO;

    /**
 * Constructs the student home page with database access.
 * @param databaseHelper shared database helper
 */
public StudentHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.questionsDO = new QuestionsDO(databaseHelper);
        this.answerDO = new AnswerDO(databaseHelper);
        this.reviewDO = new ReviewDO(databaseHelper);
    }

    /**
 * Displays the student homepage UI.
 * @param primaryStage the JavaFX stage
 * @param user the currently logged-in student
 */
public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to display Hello user
        Label studentLabel = new Label("Hello, Student!");
        studentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        //Label for displaying unread messages - Sohan S.
        Label unreadMessagesLabel = new Label("Loading unread messages...");

        // Buttons
        Button searchButton = new Button("Search");
        Button allQuestionsButton = new Button("All Questions"); // Renamed from unresolvedButton
        Button recentQuestionsButton = new Button("Recent Questions"); // Taylor added 2/19
        Button resolvedQuestionsButton = new Button("Resolved Questions"); //Rhea Soni TP2 2/20
        Button askButton = new Button("Ask");
        Button myQuestionsButton = new Button("My Questions");
      //Button askFollowUpButton = new Button("Ask a Follow-Up"); // added by Janelle (2/26) unfinished
        Button returnButton = new Button("Return");
        Button viewMessagesButton = new Button("View Messages"); //Added by Sohan S.
        Button requestReviewerButton = new Button("Request to be a Reviewer"); // New button
        Button viewRequestStatusButton = new Button("View Request Status"); // New button
        Button manageTrustedReviewersButton = new Button("Manage Trusted Reviewers");
        Button viewUnreadReviewsButton = new Button("View Unread Reviews"); // New button for unread reviews
        Button viewReviewersButton = new Button("View Available Reviewers"); // added by Sohan 4/20
        Button rateReviewersButton = new Button("Rate a Reviewer"); //Janelle + Raya TP4
        
        // Event Handlers for Buttons
        searchButton.setOnAction(e -> handleSearch(primaryStage, user));
        allQuestionsButton.setOnAction(e -> handleAllQuestions(primaryStage, user)); // Updated handler
        recentQuestionsButton.setOnAction(e -> handleRecentQuestions(primaryStage, user)); // Taylor added 2/19
        resolvedQuestionsButton.setOnAction(e -> handleResolvedQuestions(primaryStage, user)); //Rhea Soni TP2 2/20
        askButton.setOnAction(e -> handleAskQuestion(primaryStage, user));
      //askFollowUpButton.setOnAction(e -> handleAskFollowUpQuestion(primaryStage, user)); // added by Janelle (2/26) unfinished
        myQuestionsButton.setOnAction(e -> handleMyQuestions(primaryStage, user));
        returnButton.setOnAction(e -> new RolePickMenuPage(databaseHelper).show(primaryStage, user));
        manageTrustedReviewersButton.setOnAction(e -> new TrustedReviewerPage(databaseHelper, user).show(primaryStage));
        viewUnreadReviewsButton.setOnAction(e -> handleUnreadReviews(primaryStage, user)); // New handler
        
        //Added by Sohan R.
        viewMessagesButton.setOnAction(e -> openMessageWindow(primaryStage, user));
        requestReviewerButton.setOnAction(e -> handleReviewerRequest(primaryStage, user));
        viewRequestStatusButton.setOnAction(e -> checkRequestStatus(primaryStage, user));
        viewReviewersButton.setOnAction(e -> new ViewReviewersPage(databaseHelper, user).show(primaryStage)); //added by sohan 4/20
        rateReviewersButton.setOnAction(e -> new RateReviewersPage(databaseHelper).show(primaryStage, user)); //Janelle + Raya TP4
        
        new Thread(() -> {
            try {
                int unreadMessages = new MessageDO(databaseHelper).countUnreadMessages(user.getUserName());
                unreadMessagesLabel.setText("Unread Private Messages: " + unreadMessages);
            } catch (SQLException e) {
                unreadMessagesLabel.setText("Error fetching messages.");
                e.printStackTrace();
            }
        }).start();
        
        // Added by Raya D.
        
        
        // Add components to layout
        layout.getChildren().addAll(studentLabel, searchButton, allQuestionsButton, recentQuestionsButton, resolvedQuestionsButton, askButton, myQuestionsButton, returnButton, viewMessagesButton, manageTrustedReviewersButton,requestReviewerButton, viewRequestStatusButton, viewUnreadReviewsButton, rateReviewersButton, viewReviewersButton); // Taylor edited 2/19

        // Set scene and stage
        Scene studentScene = new Scene(layout, 800, 400);
        primaryStage.setScene(studentScene);
        primaryStage.setTitle("Student Page");
    }
    
    //Added by Sohan R.
    private void openMessageWindow(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Unread Private Messages");
        
        // Ensure UI modification happens on JavaFX Application Thread
        Platform.runLater(() -> layout.getChildren().add(titleLabel));

        new Thread(() -> {
            try {
                List<Message> messages = new MessageDO(databaseHelper).readMessages(user.getUserName());

                Platform.runLater(() -> {
                	for (Message message : messages) {
                		
                    	// (Janelle) Added this if statement to avoid users with both the Student and Reviewer role
                    	// being able to read "reviewer feedback" messages in the Student private messages page
                    	if (!message.getContent().contains("Feedback on \"")) {
                    		
	                    	HBox messageBox = new HBox(10);
	                        messageBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
	
	                        Label messageLabel = new Label("From: " + message.getSenderUserName() + " - " + message.getContent());
	                        Button replyButton = new Button("Reply");
	                        replyButton.setOnAction(e -> openReplyWindow(primaryStage, user, message.getSenderUserName()));
	                        messageBox.getChildren().addAll(messageLabel, replyButton);
	                        layout.getChildren().add(messageBox);
                    	}
                    }
                });

                // Mark messages as read in a separate thread (no UI modifications here)
                for (Message message : messages) {
                	if (!message.getContent().contains("Feedback on \"")) { // Janelle
                		new MessageDO(databaseHelper).markMessageAsRead(message.getId());
                	}
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));

        // Ensure UI modification happens on JavaFX Application Thread
        Platform.runLater(() -> layout.getChildren().add(backButton));

        Scene messageScene = new Scene(layout, 600, 400);
        primaryStage.setScene(messageScene);
    }
    
    //Added by Sohan S.
    private void openReplyWindow(Stage primaryStage, User user, String receiverUserName) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Reply to " + receiverUserName);
        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Type your reply here...");
        
        Button sendButton = new Button("Send Reply");
        sendButton.setOnAction(e -> {
            String replyText = replyArea.getText().trim();
            if (!replyText.isEmpty()) {
                new Thread(() -> {
                    try {
                        Timestamp timestamp = Timestamp.from(Instant.now());
                        Message replyMessage = new Message(0, user.getUserName(), receiverUserName, replyText, timestamp, false);
                        new MessageDO(databaseHelper).createMessage(replyMessage);

                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Success");
                            alert.setHeaderText(null);
                            alert.setContentText("Your reply has been sent.");
                            alert.showAndWait();
                            replyArea.clear();
                        });

                    } catch (SQLException ex) {
                        ex.printStackTrace();

                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText(null);
                            alert.setContentText("An error occurred while sending the message.");
                            alert.showAndWait();
                        });
                    }
                }).start();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error: Reply cannot be empty.");
                alert.showAndWait();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> openMessageWindow(primaryStage, user));

        layout.getChildren().addAll(titleLabel, replyArea, sendButton, backButton);

        Scene replyScene = new Scene(layout, 500, 300);
        Stage replyStage = new Stage();
        replyStage.setScene(replyScene);
        replyStage.setTitle("Reply to Private Message");
        replyStage.show();
    }
    
    //Remove edit and delete
    //check if resolved, if yes then dont display edit/delete
    //Rhea Soni TP2 2/20
    private void handleResolvedQuestions(Stage primaryStage, User user) {
        try {
            List<Question> resolvedQuestions = questionsDO.readResolvedQuestions();
            if (resolvedQuestions.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Resolved Questions");
                alert.setHeaderText(null);
                alert.setContentText("No resolved questions found.");
                alert.showAndWait();
            } else {
                displayQuestions(primaryStage, resolvedQuestions, "Resolved Questions", user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Event Handlers
    /**
 * Opens a keyword-based search for questions.
 */
private void handleSearch(Stage primaryStage, User user) {
        // Open a new window for search
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label searchLabel = new Label("Enter a keyword to search:");
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        Button searchButton = new Button("Search");
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
 * Displays all questions with filters and actions.
 */
private void handleAllQuestions(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Create filter controls
        HBox filterBox = new HBox(10);
        filterBox.setStyle("-fx-alignment: center;");
        
        Label filterLabel = new Label("Filter:");
        ToggleGroup filterGroup = new ToggleGroup();
        
        RadioButton allButton = new RadioButton("All");
        allButton.setToggleGroup(filterGroup);
        allButton.setSelected(true);
        
        RadioButton unresolvedButton = new RadioButton("Unresolved");
        unresolvedButton.setToggleGroup(filterGroup);
        
        RadioButton resolvedButton = new RadioButton("Resolved");
        resolvedButton.setToggleGroup(filterGroup);
        
        filterBox.getChildren().addAll(filterLabel, allButton, unresolvedButton, resolvedButton);

        // Questions list container
        VBox questionsContainer = new VBox(10);
        questionsContainer.setStyle("-fx-alignment: center;");

        // Function to load and display questions based on filter
        Runnable loadQuestions = () -> {
            questionsContainer.getChildren().clear(); // Clear previous content
            
            try {
                List<Question> questions;
                if (unresolvedButton.isSelected()) {
                    questions = questionsDO.readUnresolvedQuestions();
                } else if (resolvedButton.isSelected()) {
                    questions = questionsDO.readResolvedQuestions();
                } else {
                    questions = questionsDO.readQuestions();
                }
                
                if (questions.isEmpty()) {
                    Label noQuestionsLabel = new Label("No questions found matching this filter.");
                    noQuestionsLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                    questionsContainer.getChildren().add(noQuestionsLabel);
                } else {
                    // Display the questions
                    for (Question question : questions) {
                        HBox questionBox = new HBox(10);
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
                        
                        Button privateMessageButton = new Button("Send Private Message");
                        privateMessageButton.setOnAction(e -> openPrivateMessageWindow(primaryStage, question.getAuthor(), user));
                        
                        // Add Edit and Delete buttons only if the question is not resolved and the user is the author
                        if (!question.isResolved() && question.getAuthor().equals(user.getUserName())) {
                            Button editButton = new Button("Edit");
                            editButton.setOnAction(e -> editQuestion(primaryStage, question, user));

                            Button deleteButton = new Button("Delete");
                            deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                            deleteButton.setOnAction(e -> deleteQuestion(primaryStage, question, user));

                            questionBox.getChildren().addAll(resolvedLabel, questionButton, privateMessageButton, editButton, deleteButton);
                        } else {
                            questionBox.getChildren().addAll(resolvedLabel, questionButton, privateMessageButton);
                        }

                        questionsContainer.getChildren().add(questionBox);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                Label errorLabel = new Label("Error loading questions. Please try again.");
                errorLabel.setStyle("-fx-text-fill: red;");
                questionsContainer.getChildren().add(errorLabel);
            }
        };

        // Add listeners to radio buttons to refresh when selection changes
        filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadQuestions.run();
            }
        });

        // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));

        // Initial load
        loadQuestions.run();

        // Add components to layout
        layout.getChildren().addAll(filterBox, questionsContainer, backButton);

        // Set scene and stage
        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("All Questions");
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
 * Opens the interface for students to submit a new question.
 */
private void handleAskQuestion(Stage primaryStage, User user) {
        // Open a new window to ask a question
        AskQuestionPage askQuestionPage = new AskQuestionPage(databaseHelper, user);
        askQuestionPage.show(primaryStage);
    }

    /**
 * Displays all questions asked by the current student.
 */
private void handleMyQuestions(Stage primaryStage, User user) {
        // Display questions asked by the current user
        try {
            List<Question> myQuestions = questionsDO.readQuestions().stream()
                    .filter(q -> q.getAuthor().equals(user.getUserName()))
                    .collect(Collectors.toList());

            if (myQuestions.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("My Questions");
                alert.setHeaderText(null);
                alert.setContentText("You have not asked any questions yet.");
                alert.showAndWait();
            } else {
                displayMyQuestions(primaryStage, myQuestions, "My Questions", user); //Taylor edit 2/19/25
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //Taylor edit 2/19
    //Rhea edit 2/20
    
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
            
            Button privateMessageButton = new Button("Send Private Message");
            privateMessageButton.setOnAction(e -> openPrivateMessageWindow(primaryStage, question.getAuthor(), user));
            
          //Rhea TP2 2/20
            // Add Edit and Delete buttons only if the question is not resolved and the user is the author
            if (!question.isResolved() && question.getAuthor().equals(user.getUserName())) {
                Button editButton = new Button("Edit");
                editButton.setOnAction(e -> editQuestion(primaryStage, question, user));

                Button deleteButton = new Button("Delete");
                deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteButton.setOnAction(e -> deleteQuestion(primaryStage, question, user));

                questionBox.getChildren().addAll(resolvedLabel, questionButton, privateMessageButton, editButton, deleteButton);
            } else {
                questionBox.getChildren().addAll(resolvedLabel, questionButton, privateMessageButton);
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
    
    private void deleteReply(Stage primaryStage, Question question, Answer answer, User user) {
        try {
            answerDO.deleteAnswer(answer.getId()); // Use AnswerDO to delete the answer
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Your reply has been deleted.");
            alert.showAndWait();
            displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while deleting the reply.");
            alert.showAndWait();
        }
    }
    
  //Rhea 2/21
    private void displayQuestionWithReplies(Stage primaryStage, Question question, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Display question details
        Label askerLabel = new Label("Asker: " + question.getAuthorName());
        Label timestampLabel = new Label("Timestamp: " + question.getTimestamp());
        Label titleLabel = new Label("Title: " + question.getTitle());
        Label descriptionLabel = new Label("Question: " + question.getDescription());

        layout.getChildren().addAll(askerLabel, timestampLabel, titleLabel, descriptionLabel);
        
        // Taylor added 4/1/25
    	// button to see reviews for the question
    	Button viewQuestionReviews = new Button("View Question Reviews");
    	viewQuestionReviews.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
    	viewQuestionReviews.setOnAction(e -> handleQuestionReviews(primaryStage, user, question));
    	layout.getChildren().add(viewQuestionReviews);
        
        // Display all replies
        try {
            List<Answer> answers = answerDO.readAnswers(question.getId());
            if (answers.isEmpty()) {
                Label noAnswersLabel = new Label("No replies yet.");
                layout.getChildren().add(noAnswersLabel);
            } else {  
            	
              //Added by Raya** Allows user to see only answers that were marked correct
            	Button viewSolutionReplies = new Button("View Selected Answers");
            	viewSolutionReplies.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
            	viewSolutionReplies.setOnAction(e -> handleSolutions(primaryStage, user));
            	layout.getChildren().add(viewSolutionReplies);
            	
            	// Taylor moved 4/1/25
            	Label repliesLabel = new Label("Replies:");
                layout.getChildren().add(repliesLabel);
                
                for (Answer answer : answers) {
                    VBox replyBox = new VBox(10);
                    replyBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

                    // Display the reply author and content
                    Label answerLabel = new Label("Reply by " + answer.getAuthorName() + ": " + answer.getContent());
                    replyBox.getChildren().add(answerLabel);
                    
                   
                 // Add Edit and Delete buttons only for the author of the reply
                    if (answer.getAuthor().equals(user.getUserName())) {
                        HBox buttonBox = new HBox(5);
                        Button editButton = new Button("Edit");
                        editButton.setOnAction(e -> editReply(primaryStage, question, answer, user));

                        Button deleteButton = new Button("Delete");
                        deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                        deleteButton.setOnAction(e -> deleteReply(primaryStage, question, answer, user));
                        
                        buttonBox.getChildren().addAll(editButton, deleteButton);
                        replyBox.getChildren().add(buttonBox);
                    }
                    
                    Label solvedLabel = new Label();
                    HBox buttonBox = new HBox(5);
                    if(question.getAuthor().equals(user.getUserName())) {
                    	 //Added by Raya** Shows wich replies are marked as a solution
//                        Label solvedLabel = new Label();
                        if (answer.isSolution()) {
                            solvedLabel.setText("✓");
                            solvedLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            
                            buttonBox.getChildren().add(solvedLabel);
                            replyBox.getChildren().add(buttonBox);
                        } 
                        else {
                        	//Allows author to mark reply as a solution
                        	Button studentSolutionButton = new Button("This Answers My Question");
                            studentSolutionButton.setStyle("-fx-background-color: #008000; -fx-text-fill: white;");
                            studentSolutionButton.setOnAction(e -> {
                                try {
                                    answerDO.markAsSolution(answer.getId());
                                    Alert alert = new Alert(AlertType.INFORMATION);
                                    alert.setTitle("Success");
                                    alert.setHeaderText(null);
                                    alert.setContentText("The answer has been marked as the solution.");
                                    alert.showAndWait();
                                    displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                         });
                            
                            buttonBox.getChildren().add(studentSolutionButton);
                            replyBox.getChildren().add(buttonBox);
                        }
                    }
                    else {
                    	if (answer.isSolution()) {
                            solvedLabel.setText("✓");
                            solvedLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            
                            buttonBox.getChildren().add(solvedLabel);
                            replyBox.getChildren().add(buttonBox);
                        } 
                    }
                    
                    // Taylor added 4/1/25
                    //display reviews for the answer
                    try {
	                    List<Review> reviews = reviewDO.readReviews(answer.getId(), "answer");
	                    if (reviews.isEmpty()) {
	                        Label noReviewsLabel = new Label("No reviews yet.");
	                        replyBox.getChildren().add(noReviewsLabel);
	                    } else {
	                        Label reviewsLabel = new Label("Answer Reviews:");
	                        replyBox.getChildren().add(reviewsLabel);
	                        for (Review review : reviews) {
	                            HBox reviewBox = new HBox(10); // HBox layout added by Janelle
	                            reviewBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

	                            Label reviewLabel = new Label("Review by " + review.getReviewerName() + ": " + review.getContent());

	                            // (Janelle) A string made so that the reviewer feedback message will reference
	                            // the first 50 letters of the review in the text field, for context purposes
	                            String shortenedString = review.getContent();
	                            if (review.getContent().length() > 50) {
	                                shortenedString = review.getContent().substring(0, 50) + "...";
	                            }
	                            final String truncatedReviewContent = shortenedString;

	                            // (Janelle) Button allowing the student to immediately send a private message to the reviewer
	                            Button sendPMToReviewer = new Button("Send feedback message to this reviewer");
	                            sendPMToReviewer.setOnAction(e -> openReviewerFeedbackWindow(primaryStage, truncatedReviewContent, review.getReviewerName(), review.getReviewer(), user));

	                            // 🆕 NEW: Edit button for review owner
	                            if (user.getRoles().contains("reviewer") && review.getReviewer().equals(user.getUserName())) {

	                                Button editButton = new Button("Edit Review");
	                                editButton.setOnAction(e -> openReviewEditWindow(primaryStage, review, user));
	                                reviewBox.getChildren().add(editButton);
	                            }

	                            // 🆕 NEW: View Original button if originalContent exists
	                            if (review.getOriginalContent() != null && !review.getOriginalContent().isEmpty()) {
	                                Button viewOriginalBtn = new Button("View Original");
	                                viewOriginalBtn.setOnAction(e -> {
	                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
	                                    alert.setTitle("Original Review");
	                                    alert.setHeaderText("Original content of your review:");
	                                    alert.setContentText(review.getOriginalContent());
	                                    alert.showAndWait();
	                                });
	                                reviewBox.getChildren().add(viewOriginalBtn);
	                            }

	                            reviewBox.getChildren().addAll(reviewLabel, sendPMToReviewer);
	                            replyBox.getChildren().add(reviewBox);
	                        }
	                      }
                    }
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                    
                 // Conditional button to link back to previous question, for follow-ups
                 // (Janelle 2/28) Currently unused
                    if (question.isFollowUp() == true) {
                    	Button prevQuestionButton = new Button("View Previous Question");
                    	prevQuestionButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                    	//prevQuestionButton.setOnAction(e -> );
                    }
                    
                    layout.getChildren().add(replyBox);
            }
        }}
            catch (SQLException e) {
            e.printStackTrace();
        }

        // Add a text area for replying
        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Enter your reply");

        // Add Submit Reply button
        Button submitButton = new Button("Submit Reply");
        submitButton.setOnAction(e -> {
            String replyText = replyArea.getText();
            if (!replyText.isEmpty()) {
                try {
                    Answer answer = new Answer(0, question.getId(), replyText, user.getUserName(), user.getName(), false);
                    answerDO.createAnswer(answer);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your reply has been submitted.");
                    alert.showAndWait();
                    displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Display error message if reply is empty
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error message: reply cannot be empty.");
                alert.showAndWait();
            }
        });
        
     // (Janelle 2/28)
        // Follow-up question submit button, based on "Submit Reply" button above
        Button submitFollowUpButton = new Button("Submit Follow-Up Question");
        submitFollowUpButton.setOnAction(e -> {
            String questionText = replyArea.getText();
            if (!questionText.isEmpty()) {
                try {
                    // Creates a follow-up question with prevQuestionID set to the current question's ID
                    Question followUpQuestion = new Question(0, "A \"" + question.getTitle() + "\" Follow-up", questionText, user.getUserName(), user.getName(), question.getTimestamp(), question.isResolved());
                    followUpQuestion.setPrevQuestionID(question.getId());
                    questionsDO.createQuestion(followUpQuestion); // new QuestionsDO?
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your question has been submitted.");
                    alert.showAndWait();
                    
                    // Note from Janelle: If possible, display a button on this page linking back to previous question
                    displayQuestionWithReplies(primaryStage, followUpQuestion, user); // Refresh the page
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Display error message if reply is empty
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error message: question cannot be empty.");
                alert.showAndWait();
            }
        });
     //*****************************

        // Add Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> handleAllQuestions(primaryStage, user));

        // Add Resolve, Edit, and Delete buttons below the Submit Reply button
        if (question.getAuthor().equals(user.getUserName())) {
            HBox buttonBox = new HBox(10); // Use HBox to arrange buttons horizontally
            buttonBox.setStyle("-fx-alignment: center; -fx-padding: 10;");

            if (!question.isResolved()) {
                Button resolveButton = new Button("Resolve");
                resolveButton.setOnAction(e -> {
                    try {
                        questionsDO.markQuestionAsResolved(question.getId());
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText(null);
                        alert.setContentText("The question has been resolved.");
                        alert.showAndWait();
                        displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                Button editButton = new Button("Edit");
                editButton.setOnAction(e -> editQuestion(primaryStage, question, user));

                Button deleteButton = new Button("Delete");
                deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteButton.setOnAction(e -> deleteQuestion(primaryStage, question, user));

                buttonBox.getChildren().addAll(resolveButton, editButton, deleteButton);
            }

            layout.getChildren().add(buttonBox);
        }
      
        layout.getChildren().addAll(replyArea, submitButton, backButton);

        // Set the scene and stage
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Details");
    }
    
    
    // Taylor edit 2/18
    private void editReply(Stage primaryStage, Question question, Answer answer, User user) {
        if (!answer.getAuthor().equals(user.getUserName())) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("You do not have permission to edit this reply.");
            alert.showAndWait();
            return;
        }
        else {
        	VBox layout = new VBox(10);
            layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

            Label titleLabel = new Label("Edit Reply");
            TextArea replyArea = new TextArea(answer.getContent());
            replyArea.setPromptText("Edit your reply");

            Button saveButton = new Button("Save");
            saveButton.setOnAction(e -> {
                String updatedReply = replyArea.getText();
                if (!updatedReply.isEmpty()) {
                    try {
                        answer.setContent(updatedReply);
                        answerDO.updateAnswer(answer);
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText(null);
                        alert.setContentText("Your reply has been updated.");
                        alert.showAndWait();
                        displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            Button backButton = new Button("Back");
            backButton.setOnAction(e -> displayQuestionWithReplies(primaryStage, question, user));

            layout.getChildren().addAll(titleLabel, replyArea, saveButton, backButton);

            Scene scene = new Scene(layout, 800, 400);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Edit Reply");
        }


    }

    private void displayMyQuestions(Stage primaryStage, List<Question> questions, String title, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        for (Question question : questions) {
            HBox questionBox = new HBox(10);
            questionBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

            // Add a green check mark if the question is resolved
            Label resolvedLabel = new Label();
            if (question.isResolved()) {
                resolvedLabel.setText("✓");
                resolvedLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            }

            Button questionButton = new Button(question.getTitle());
            questionButton.setOnAction(e -> manageMyQuestion(primaryStage, question, user)); //Taylor edit 2/19/25

            // Add Delete button if the question belongs to the current user and is not resolved
            if (question.getAuthor().equals(user.getUserName())) {
                if (!question.isResolved()) {
                    Button deleteButton = new Button("Delete");
                    deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                    deleteButton.setOnAction(e -> deleteQuestion(primaryStage, question, user)); //Taylor edit 2/19/25
                    questionBox.getChildren().addAll(resolvedLabel, questionButton, deleteButton);
                } else {
                    questionBox.getChildren().addAll(resolvedLabel, questionButton);
                }
            } 
            layout.getChildren().add(questionBox);
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));
        layout.getChildren().add(backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
    }

    
    //Edited Rhea 2/21
    private void manageMyQuestion(Stage primaryStage, Question question, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Title: " + question.getTitle());
        Label descriptionLabel = new Label("Description: " + question.getDescription());

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
                    VBox replyBox = new VBox(5);
                    replyBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
                    
                    // Display the reply author and content
                    Label answerLabel = new Label("Reply by " + answer.getAuthorName() + ": " + answer.getContent());
                    replyBox.getChildren().add(answerLabel);

                    // Add Edit and Delete buttons only for the author of the reply
                    if (answer.getAuthor().equals(user.getUserName())) {
                        HBox buttonBox = new HBox(5);
                        Button editButton = new Button("Edit");
                        editButton.setOnAction(e -> editReply(primaryStage, question, answer, user));

                        Button deleteButton = new Button("Delete");
                        deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                        deleteButton.setOnAction(e -> deleteReply(primaryStage, question, answer, user));

                        buttonBox.getChildren().addAll(editButton, deleteButton);
                        replyBox.getChildren().add(buttonBox);
                    }

                    layout.getChildren().add(replyBox);
                    
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add Resolve, Edit, and Delete buttons only if the question is not resolved and belongs to the current user
        if (question.getAuthor().equals(user.getUserName())) {
            HBox buttonBox = new HBox(10); // Use HBox to arrange buttons horizontally
            buttonBox.setStyle("-fx-alignment: center; -fx-padding: 10;");

            if (!question.isResolved()) {
                // Add Resolve button
                Button resolveButton = new Button("Resolve");
                resolveButton.setOnAction(e -> {
                    try {
                        questionsDO.markQuestionAsResolved(question.getId());
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText(null);
                        alert.setContentText("The question has been resolved.");
                        alert.showAndWait();
                        manageMyQuestion(primaryStage, question, user); // Refresh the page
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                // Add Edit button
                Button editButton = new Button("Edit");
                editButton.setOnAction(e -> editQuestion(primaryStage, question, user));

                // Add Delete button
                Button deleteButton = new Button("Delete");
                deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteButton.setOnAction(e -> deleteQuestion(primaryStage, question, user));

                buttonBox.getChildren().addAll(resolveButton, editButton, deleteButton);
            }

            layout.getChildren().add(buttonBox);
        }

        // Add a text area for replying
        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Enter your reply");

        // Add Submit Reply button
        Button submitButton = new Button("Submit Reply");
        submitButton.setOnAction(e -> {
            String replyText = replyArea.getText();
            if (!replyText.isEmpty()) {
                try {
                    Answer answer = new Answer(0, question.getId(), replyText, user.getUserName(), user.getName(), false);
                    answerDO.createAnswer(answer);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your reply has been submitted.");
                    alert.showAndWait();
                    manageMyQuestion(primaryStage, question, user); // Refresh the page
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Display error message if reply is empty
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error message: reply cannot be empty.");
                alert.showAndWait();
            }
        });

        // Add Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> handleMyQuestions(primaryStage, user));

        layout.getChildren().addAll(titleLabel, descriptionLabel, replyArea, submitButton, backButton);

        // Set the scene and stage
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Manage Question");
    }

//Added by Raya** Displays Answers that are solutions when filter button is clicked    
    private void handleSolutions(Stage primaryStage, User user) {
        try {
            List<Answer> solutionAnswers = answerDO.readSolutionAnswers();
            if (solutionAnswers.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Correct Answers");
                alert.setHeaderText(null);
                alert.setContentText("No correct answers found.");
                alert.showAndWait();
            } else {
                displaySolutions(primaryStage, solutionAnswers, "Resolved Questions", user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void displaySolutions(Stage primaryStage, List<Answer> answers, String title, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        layout.getChildren().add(titleLabel);

        for (Answer answer : answers) {
            VBox answerBox = new VBox(5);
            answerBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

            // Create HBox for the checkmark and answer content
            HBox contentBox = new HBox(10);
            
            // Add checkmark
            Label checkmarkLabel = new Label("✓");
            checkmarkLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            
            // Add answer content
            Label answerLabel = new Label("Answer by " + answer.getAuthorName() + ": " + answer.getContent());
            
            contentBox.getChildren().addAll(checkmarkLabel, answerLabel);
            answerBox.getChildren().add(contentBox);
            
            // Add the answer box to the layout
            layout.getChildren().add(answerBox);
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
    
    // Taylor added 4/1/25
    // Display reviews for a question
    private void handleQuestionReviews(Stage primaryStage, User user, Question question) {
        try {
            List<Review> questionReviews = reviewDO.readReviews(question.getId(), "question");
            if (questionReviews.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Question Reviews");
                alert.setHeaderText(null);
                alert.setContentText("No reviews found.");
                alert.showAndWait();
            } else {
            	VBox layout = new VBox(10);
                layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

                Label titleLabel = new Label("Question Reviews");
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                
                List<Review> reviews = reviewDO.readReviews(question.getId(), "question");
                for (Review review : questionReviews) {
                    HBox reviewBox = new HBox(10);
                    reviewBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

                    Label reviewLabel = new Label("Review by " + review.getReviewerName() + ": " + review.getContent());

                    // Optional shortened version (if needed elsewhere)
                    String shortenedString = review.getContent();
                    if (review.getContent().length() > 50) {
                        shortenedString = review.getContent().substring(0, 50) + "...";
                    }
                    final String truncatedReviewContent = shortenedString;

                    // Button to PM reviewer (if applicable)
                    Button sendPMToReviewer = new Button("Send feedback message to this reviewer");
                    sendPMToReviewer.setOnAction(e -> openReviewerFeedbackWindow(primaryStage, truncatedReviewContent, review.getReviewerName(), review.getReviewer(), user));

                    // Show Edit button only if review belongs to this user
                    if (user.getRoles().contains("reviewer") && review.getReviewer().equals(user.getUserName())) {

                        Button editButton = new Button("Edit Review");
                        editButton.setOnAction(e -> openReviewEditWindow(primaryStage, review, user));
                        reviewBox.getChildren().add(editButton);
                    }

                    // Show "View Original" if it exists
                    if (review.getOriginalContent() != null && !review.getOriginalContent().isEmpty()) {
                        Button viewOriginalBtn = new Button("View Original");
                        viewOriginalBtn.setOnAction(e -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Original Review");
                            alert.setHeaderText("Original content of your review:");
                            alert.setContentText(review.getOriginalContent());
                            alert.showAndWait();
                        });
                        reviewBox.getChildren().add(viewOriginalBtn);
                    }

                    reviewBox.getChildren().addAll(reviewLabel, sendPMToReviewer);
                    layout.getChildren().add(reviewBox);
                }

                // Add a back button to return to the previous page
                Button backButton = new Button("Back");
                backButton.setOnAction(e -> displayQuestionWithReplies(primaryStage, question, user));
                layout.getChildren().add(backButton);

                // Set the scene and stage
                Scene scene = new Scene(layout, 800, 400);
                primaryStage.setScene(scene);
                primaryStage.setTitle("Question Reviews");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
  //***********************  
    
 // Duplicates of functions edited for follow-up questions
 // added by Janelle (2/26)
    // Edited from handleMyQuestions
    private void handleMyFollowUpQuestions(Stage primaryStage, User user) {
        // Display questions asked by the current user
        try {
            List<Question> myQuestions = questionsDO.readQuestions().stream()
                    .filter(q -> q.getAuthor().equals(user.getUserName()))
                    .collect(Collectors.toList());

            if (myQuestions.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("My Questions");
                alert.setHeaderText(null);
                alert.setContentText("You have not asked any questions yet.");
                alert.showAndWait();
            } else {
            	selectQuestionToFollowUp(primaryStage, myQuestions, "My Questions", user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    // Edited from displayMyQuestions
    private void selectQuestionToFollowUp(Stage primaryStage, List<Question> questions, String title, User user) { //Taylor edit 2/19/25
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
          
        
        for (Question question : questions) {
            
        	HBox questionBox = new HBox(10);
            questionBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

            Button questionButton = new Button(question.getTitle());
            questionButton.setOnAction(e -> manageMyQuestion(primaryStage, question, user)); // New change UNFINISHED
            
            layout.getChildren().add(questionBox);
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));
        layout.getChildren().add(backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
    }
    
  //***************  
    
//Added by Shreya S.
    private void openPrivateMessageWindow(Stage primaryStage, String receiverUserName, User senderUser) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Title label
        Label titleLabel = new Label("Send Private Message to " + receiverUserName);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Text area for typing the message
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Type your message here...");
        messageArea.setPrefHeight(150); // Set preferred height for the text area

        // Send button
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            String messageText = messageArea.getText().trim();
            if (!messageText.isEmpty()) {
                try {
                    // Create a new message
                    Timestamp timestamp = Timestamp.from(Instant.now());
                    
					Message message = new Message(0, senderUser.getUserName(), receiverUserName, messageText, timestamp, false);

                    // Save the message to the database
                    new MessageDO(databaseHelper).createMessage(message);

                    // Show success alert
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your private message has been sent.");
                    alert.showAndWait();

                    // Clear the text area after sending the message
                    messageArea.clear();
                } catch (SQLException ex) {
                    ex.printStackTrace();

                    // Show error alert if something goes wrong
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("An error occurred while sending the message.");
                    alert.showAndWait();
                }
            } else {
                // Show error alert if the message is empty
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error: Message cannot be empty.");
                alert.showAndWait();
            }
        });
        
     // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            // Close the private message window and return to the previous screen
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
        });

        // Add components to the layout
        layout.getChildren().addAll(titleLabel, messageArea, sendButton, backButton);

        // Create the scene and stage
        Scene scene = new Scene(layout, 400, 300); // Set preferred window size
        Stage privateMessageStage = new Stage();
        privateMessageStage.setScene(scene);
        privateMessageStage.setTitle("Private Message");
        privateMessageStage.show();
    }
    
    private void openReviewerFeedbackWindow(Stage primaryStage, String reviewContent, String receiverName, String receiverUserName, User senderUser) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Title label
        Label titleLabel = new Label("Send Private Feedback to Reviewer " + receiverUserName);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Text area for typing the message
        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Type your message here...");
        messageArea.setPrefHeight(150); // Set preferred height for the text area

        // Send button
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            String messageText = messageArea.getText().trim();
            if (!messageText.isEmpty()) {
                try {
                    // Create a new message
                    Timestamp timestamp = Timestamp.from(Instant.now());
                    
					Message message = new Message(0, senderUser.getUserName(), receiverUserName,

						// (Janelle) Appends part of the given review to the feedback private message
						// so that the reviewer knows what is being referred to
						"Feedback on \"" + reviewContent + "\"\n" + messageText,
						timestamp, false);
                    // Save the message to the database
                    new MessageDO(databaseHelper).createMessage(message);

                    // Show success alert
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your feedback has been sent to " + receiverUserName + ".");
                    alert.showAndWait();

                    // Clear the text area after sending the message
                    messageArea.clear();
                } catch (SQLException ex) {
                    ex.printStackTrace();

                    // Show error alert if something goes wrong
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("An error occurred while sending the message.");
                    alert.showAndWait();
                }
            } else {
                // Show error alert if the message is empty
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error: Message cannot be empty.");
                alert.showAndWait();
            }
        });
        
     // Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            // Close the private message window and return to the previous screen
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
        });

        // Add components to the layout
        layout.getChildren().addAll(titleLabel, messageArea, sendButton, backButton);

        // Create the scene and stage
        Scene scene = new Scene(layout, 400, 300); // Set preferred window size
        Stage privateMessageStage = new Stage();
        privateMessageStage.setScene(scene);
        privateMessageStage.setTitle("Private Message");
        privateMessageStage.show();

    }
    
    private void editQuestion(Stage primaryStage, Question question, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Edit Question");
        TextField titleField = new TextField(question.getTitle());
        TextArea descriptionArea = new TextArea(question.getDescription());

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String newTitle = titleField.getText();
            String newDescription = descriptionArea.getText();
            if (!newTitle.isEmpty() && !newDescription.isEmpty()) {
               
            	//add if for edit error message for resolved
            	try {
                    question.setTitle(newTitle);
                    question.setDescription(newDescription);
                    questionsDO.updateQuestion(question);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your question has been updated.");
                    alert.showAndWait();
                    handleAllQuestions(primaryStage, user); // Refresh the page
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> displayQuestionWithReplies(primaryStage, question, user));

        layout.getChildren().addAll(titleLabel, titleField, descriptionArea, saveButton, backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Edit Question");
    }

    
   
    private void deleteQuestion(Stage primaryStage, Question question, User user) {
        try {
            questionsDO.deleteQuestion(question.getId());
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("The question has been deleted.");
            alert.showAndWait();
            handleAllQuestions(primaryStage, user); // Refresh the page
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Add this new method to handle reviewer requests
    private void handleReviewerRequest(Stage primaryStage, User user) {
        try {
            // Check if user already has a pending request
            String checkQuery = "SELECT status FROM reviewer_requests WHERE username = ?";
            try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(checkQuery)) {
                pstmt.setString(1, user.getUserName());
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    String status = rs.getString("status");
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Request Status");
                    alert.setHeaderText(null);
                    alert.setContentText("You already have a " + status + " request to be a reviewer.");
                    alert.showAndWait();
                    return;
                }
            }

            // Insert new request
            String insertQuery = "INSERT INTO reviewer_requests (studentId, studentName, username) VALUES ((SELECT id FROM cse360users WHERE userName = ?), ?, ?)";
            try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(insertQuery)) {
                pstmt.setString(1, user.getUserName());
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getUserName());
                pstmt.executeUpdate();
                
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Your request to be a reviewer has been submitted successfully.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while submitting your request.");
            alert.showAndWait();
        }
    }

    // Add this new method to check request status
    private void checkRequestStatus(Stage primaryStage, User user) {
        try {
            String query = "SELECT status FROM reviewer_requests WHERE username = ?";
            try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
                pstmt.setString(1, user.getUserName());
                ResultSet rs = pstmt.executeQuery();
                
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Request Status");
                alert.setHeaderText(null);
                
                if (rs.next()) {
                    String status = rs.getString("status");
                    alert.setContentText("Your request to be a reviewer is currently " + status + ".");
                } else {
                    alert.setContentText("You have not submitted a request to be a reviewer yet.");
                }
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while checking your request status.");
            alert.showAndWait();
        }
    }

    // New method to handle unread reviews
    private void handleUnreadReviews(Stage primaryStage, User user) {
        try {
            // Get trusted reviewers for the student
            Map<String, Integer> trustedReviewers = databaseHelper.getTrustedReviewers(user.getUserName());
            System.out.println("Found trusted reviewers: " + trustedReviewers);

            // Get all questions
            List<Question> questions = questionsDO.readQuestions();
            System.out.println("Found " + questions.size() + " questions");

            VBox layout = new VBox(10);
            layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

            Label titleLabel = new Label("Unread Reviews from Trusted Reviewers");
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            layout.getChildren().add(titleLabel);

            boolean hasUnreadReviews = false;

            // Check each question for unread reviews
            for (Question question : questions) {
                // Get reviews for the question
                List<Review> questionReviews = reviewDO.readReviews(question.getId(), "question");
                System.out.println("Found " + questionReviews.size() + " reviews for question " + question.getId());
                
                List<Review> unreadQuestionReviews = questionReviews.stream()
                    .filter(review -> {
                        boolean isUnread = review.isUnread();
                        boolean isTrusted = trustedReviewers.containsKey(review.getReviewer());
                        System.out.println("Review from " + review.getReviewer() + " - unread: " + isUnread + ", trusted: " + isTrusted);
                        return isUnread && isTrusted;
                    })
                    .collect(Collectors.toList());
                
                System.out.println("Found " + unreadQuestionReviews.size() + " unread reviews from trusted reviewers for question " + question.getId());

                if (!unreadQuestionReviews.isEmpty()) {
                    hasUnreadReviews = true;
                    HBox questionBox = new HBox(10);
                    questionBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

                    Label questionLabel = new Label("Question: " + question.getTitle());
                    questionBox.getChildren().add(questionLabel);

                    for (Review review : unreadQuestionReviews) {
                        VBox reviewBox = new VBox(5);
                        reviewBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 5;");
                        
                        Label reviewerLabel = new Label("Reviewer: " + review.getReviewerName());
                        Label contentLabel = new Label("Review: " + review.getContent());
                        
                        // Add Mark as Read button
                        Button markAsReadButton = new Button("Mark as Read");
                        markAsReadButton.setOnAction(e -> {
                            try {
                                review.setUnread(false);
                                reviewDO.updateReview(review);
                                // Refresh the page to show updated state
                                handleUnreadReviews(primaryStage, user);
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                Alert alert = new Alert(AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setHeaderText(null);
                                alert.setContentText("Failed to mark review as read: " + ex.getMessage());
                                alert.showAndWait();
                            }
                        });
                        
                        reviewBox.getChildren().addAll(reviewerLabel, contentLabel, markAsReadButton);
                        questionBox.getChildren().add(reviewBox);
                    }
                    
                    layout.getChildren().add(questionBox);
                }

                // Get reviews for answers to this question
                List<Answer> answers = answerDO.readAnswers(question.getId());
                for (Answer answer : answers) {
                    List<Review> answerReviews = reviewDO.readReviews(answer.getId(), "answer");
                    System.out.println("Found " + answerReviews.size() + " reviews for answer " + answer.getId());
                    
                    List<Review> unreadAnswerReviews = answerReviews.stream()
                        .filter(review -> {
                            boolean isUnread = review.isUnread();
                            boolean isTrusted = trustedReviewers.containsKey(review.getReviewer());
                            System.out.println("Review from " + review.getReviewer() + " - unread: " + isUnread + ", trusted: " + isTrusted);
                            return isUnread && isTrusted;
                        })
                        .collect(Collectors.toList());
                    
                    System.out.println("Found " + unreadAnswerReviews.size() + " unread reviews from trusted reviewers for answer " + answer.getId());

                    if (!unreadAnswerReviews.isEmpty()) {
                        hasUnreadReviews = true;
                        HBox answerBox = new HBox(10);
                        answerBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

                        Label answerLabel = new Label("Answer to: " + question.getTitle());
                        answerBox.getChildren().add(answerLabel);

                        for (Review review : unreadAnswerReviews) {
                            VBox reviewBox = new VBox(5);
                            reviewBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 5;");
                            
                            Label reviewerLabel = new Label("Reviewer: " + review.getReviewerName());
                            Label contentLabel = new Label("Review: " + review.getContent());
                            
                            // Add Mark as Read button
                            Button markAsReadButton = new Button("Mark as Read");
                            markAsReadButton.setOnAction(e -> {
                                try {
                                    review.setUnread(false);
                                    reviewDO.updateReview(review);
                                    // Refresh the page to show updated state
                                    handleUnreadReviews(primaryStage, user);
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                    Alert alert = new Alert(AlertType.ERROR);
                                    alert.setTitle("Error");
                                    alert.setHeaderText(null);
                                    alert.setContentText("Failed to mark review as read: " + ex.getMessage());
                                    alert.showAndWait();
                                }
                            });
                            
                            reviewBox.getChildren().addAll(reviewerLabel, contentLabel, markAsReadButton);
                            answerBox.getChildren().add(reviewBox);
                        }
                        
                        layout.getChildren().add(answerBox);
                    }
                }
            }

            if (!hasUnreadReviews) {
                Label noReviewsLabel = new Label("No unread reviews from trusted reviewers.");
                layout.getChildren().add(noReviewsLabel);
            }

            Button backButton = new Button("Back");
            backButton.setOnAction(e -> show(primaryStage, user));
            layout.getChildren().add(backButton);

            Scene scene = new Scene(layout, 800, 400);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Unread Reviews");
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while loading unread reviews.");
            alert.showAndWait();
        }
    }
    
    private void openReviewEditWindow(Stage primaryStage, Review review, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Edit Review");
        TextArea editArea = new TextArea(review.getContent());
        editArea.setWrapText(true);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String updatedContent = editArea.getText().trim();
            if (!updatedContent.isEmpty()) {
                try {
                    new ReviewDO(databaseHelper).editReview(review.getReviewId(), updatedContent);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Review updated successfully.");
                    alert.showAndWait();
                    // Optional: Refresh the page
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText(null);
                error.setContentText("Review content cannot be empty.");
                error.showAndWait();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.close();
        });

        layout.getChildren().addAll(titleLabel, editArea, saveButton, backButton);
        Scene scene = new Scene(layout, 600, 300);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Edit Review");
        stage.show();
    }

    
    public void editReview(int reviewId, String newContent) throws SQLException {
        // Step 1: Check if originalContent already exists
        String selectQuery = "SELECT content, originalContent FROM Reviews WHERE id = ?";
        String updateQuery;
        boolean preserveOriginal = false;

        try (PreparedStatement selectStmt = databaseHelper.getConnection().prepareStatement(selectQuery)) {
            selectStmt.setInt(1, reviewId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                String original = rs.getString("originalContent");
                if (original == null || original.isEmpty()) {
                    preserveOriginal = true;
                }
            }
        }

        // Step 2: Build the correct update query
        if (preserveOriginal) {
            updateQuery = "UPDATE Reviews SET content = ?, originalContent = ?, lastUpdated = CURRENT_TIMESTAMP WHERE id = ?";
        } else {
            updateQuery = "UPDATE Reviews SET content = ?, lastUpdated = CURRENT_TIMESTAMP WHERE id = ?";
        }

        // Step 3: Execute update
        try (PreparedStatement updateStmt = databaseHelper.getConnection().prepareStatement(updateQuery)) {
            updateStmt.setString(1, newContent);
            if (preserveOriginal) {
                // Retrieve current content again for saving as original
                try (PreparedStatement requery = databaseHelper.getConnection().prepareStatement("SELECT content FROM Reviews WHERE id = ?")) {
                    requery.setInt(1, reviewId);
                    ResultSet rs = requery.executeQuery();
                    if (rs.next()) {
                        updateStmt.setString(2, rs.getString("content"));
                        updateStmt.setInt(3, reviewId);
                    }
                }
            } else {
                updateStmt.setInt(2, reviewId);
            }

            updateStmt.executeUpdate();
        }
    }
    
  //Rhea p4
    public List<Question> getFilteredQuestions(String filterType) throws SQLException {
        switch (filterType.toLowerCase()) {
            case "unresolved":
                return questionsDO.readUnresolvedQuestions();
            case "resolved":
                return questionsDO.readResolvedQuestions();
            case "all":
            default:
                return questionsDO.readQuestions();
        }
    }

}