//Taylor created
package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import databasePart1.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.application.Platform;

/**
 * The {@code ReviewerHomePage} class defines the interface for reviewer users to explore questions,
 * submit reviews, manage their replies, and view private feedback.
 */

/**
 * The {@code ReviewerHomePage} class defines the interface for reviewer users to explore questions,
 * submit reviews, manage replies, and view feedback. 
 */
public class ReviewerHomePage {
    private final DatabaseHelper databaseHelper;
    private final QuestionsDO questionsDO;
    private final AnswerDO answerDO;
    private final ReviewDO reviewDO;

    /**
 * Constructs the homepage for reviewer role.
 * @param databaseHelper the shared database helper
 */
public ReviewerHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.questionsDO = new QuestionsDO(databaseHelper);
        this.answerDO = new AnswerDO(databaseHelper);
        this.reviewDO = new ReviewDO(databaseHelper);
    }

    /**
 * Displays the main reviewer homepage.
 * @param primaryStage the main application window
 * @param user the currently logged-in reviewer
 */
public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to display Hello user
        Label reviewerLabel = new Label("Hello, Reviewer!");
        reviewerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Buttons
        Button searchButton = new Button("Search");
        Button allQuestionsButton = new Button("All Questions");
        Button recentQuestionsButton = new Button("Recent Questions");
        Button viewFeedbackButton = new Button("View Private Feedback"); // (Janelle) For seeing messages from students
        Button returnButton = new Button("Return");
        Button myReviewsButton = new Button("My Reviews");
        Button viewProfileButton = new Button("View Profile"); //Raya TP4
        
        // Event Handlers for Buttons
        searchButton.setOnAction(e -> handleSearch(primaryStage, user));
        allQuestionsButton.setOnAction(e -> handleAllQuestions(primaryStage, user));
        recentQuestionsButton.setOnAction(e -> handleRecentQuestions(primaryStage, user));
        returnButton.setOnAction(e -> new RolePickMenuPage(databaseHelper).show(primaryStage, user));
        viewFeedbackButton.setOnAction(e -> openMessageWindow(primaryStage,user)); // (Janelle)
        myReviewsButton.setOnAction(e -> handleMyReviews(primaryStage, user)); //Rhea Soni 4/3 P3
        viewProfileButton.setOnAction(e -> new ReviewerProfilePage(databaseHelper).show(primaryStage, user, user)); //Raya TP4
        
        // Add components to layout
        layout.getChildren().addAll(reviewerLabel, viewProfileButton, searchButton, allQuestionsButton, recentQuestionsButton, myReviewsButton, viewFeedbackButton, returnButton);

        // Set scene and stage
        Scene studentScene = new Scene(layout, 800, 400);
        primaryStage.setScene(studentScene);
        primaryStage.setTitle("Reviewer Page"); // Taylor edit 4/17
    }
    
    /**
 * Opens the question search interface for reviewers.
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
 * Displays all questions from the database.
 */
private void handleAllQuestions(Stage primaryStage, User user) {
        // Display all questions (both resolved and unresolved)
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
 * Shows questions asked within the last 24 hours.
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
 * Shows reviews submitted by the reviewer.
 */
private void handleMyReviews(Stage primaryStage, User user) {
        try {
            List<Review> myReviews = reviewDO.getReviewsByReviewer(user.getUserName());
            if (myReviews.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("My Reviews");
                alert.setHeaderText(null);
                alert.setContentText("You haven't submitted any reviews yet.");
                alert.showAndWait();
            } else {
                displayMyReviews(primaryStage, myReviews, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
 // Rhea Soni 4/2 P3  Display my reviews with private feedback counts
    //Rhea code start here
 
    /**
 * Displays the list of submitted reviews with feedback.
 */
private void displayMyReviews(Stage primaryStage, List<Review> reviews, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("My Reviews");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        try {
            for (Review review : reviews) {
                VBox reviewBox = new VBox(5);
                reviewBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

                // Get question/answer title
                String targetTitle = getTargetTitle(review);
                
                // Display review info
                String reviewType = review.getQorA().equals("question") ? "Question Review" : "Answer Review";
                Label reviewLabel = new Label(reviewType + " on: " + targetTitle);
                Label contentLabel = new Label("Your review: " + review.getContent());
                
               
                Button feedbackButton = new Button("View Feedback");
                feedbackButton.setOnAction(e -> showFeedbackForReview(primaryStage, review, user));

                reviewBox.getChildren().addAll(reviewLabel, contentLabel, feedbackButton);
                layout.getChildren().add(reviewBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading reviews");
            layout.getChildren().add(errorLabel);
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));
        layout.getChildren().addAll(backButton);

        Scene scene = new Scene(layout, 800, 600); // Increased height
        primaryStage.setScene(scene);
        primaryStage.setTitle("My Reviews");
    }

   
 
    /**
 * Displays private feedback messages for a specific review.
 */
private void showFeedbackForReview(Stage primaryStage, Review review, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Feedback for your review");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        try {
            List<Message> feedbackMessages = reviewDO.getFeedbackMessages(review.getReviewId(), user.getUserName());
            
            if (feedbackMessages.isEmpty()) {
                Label noFeedbackLabel = new Label("No feedback received yet for this review");
                layout.getChildren().add(noFeedbackLabel);
            } else {
                for (Message message : feedbackMessages) {
                    VBox messageBox = new VBox(5);
                    messageBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-padding: 10;");
                    
                    Label senderLabel = new Label("From: " + message.getSenderUserName());
                    Label timeLabel = new Label("Time: " + message.getTimestamp());
                    Label contentLabel = new Label("Feedback: " + message.getContent());
                    
                    messageBox.getChildren().addAll(senderLabel, timeLabel, contentLabel);
                    layout.getChildren().add(messageBox);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading feedback");
            layout.getChildren().add(errorLabel);
        }

        Button backButton = new Button("Back to My Reviews");
        backButton.setOnAction(e -> {
            try {
                displayMyReviews(primaryStage, reviewDO.getReviewsByReviewer(user.getUserName()), user);
            } catch (SQLException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to load reviews");
                alert.showAndWait();
            }
        });

        layout.getChildren().add(backButton);
        
        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Review Feedback");
    }
    // Helper method to get question/answer title
    private String getTargetTitle(Review review) throws SQLException {
        if (review.getQorA().equals("question")) {
            Question question = questionsDO.getQuestionById(review.getTargetId());
            return question != null ? question.getTitle() : "Deleted Question";
        } else {
            Answer answer = answerDO.getAnswerById(review.getTargetId());
            if (answer != null) {
                Question question = questionsDO.getQuestionById(answer.getQuestionId());
                return question != null ? question.getTitle() : "Deleted Question";
            }
            return "Deleted Answer";
        }
    }
    
    //Rhea Edit end here
    

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
    
    private void createAnswerReview(Answer answer, Question question, Stage primaryStage, User user) {
    	VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Create Review");
     // Add a text area for creating reviews
        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Enter your review");

        // Submit reviews for an answer
        Button submitButton = new Button("Submit Review");
        submitButton.setOnAction(e -> {
            String reviewText = reviewArea.getText();
            if (!reviewText.isEmpty()) {
                try {
                    Review review = new Review(answer.getId(), "answer", 0, reviewText, user.getUserName(), user.getName() + " (Reviewer)", true, Timestamp.from(Instant.now()));
                    reviewDO.createReview(review);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your review has been submitted.");
                    alert.showAndWait();
                    displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Display error message if review is empty
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error message: review cannot be empty.");
                alert.showAndWait();
            }
        });
    
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> displayQuestionWithReplies(primaryStage, question, user));

        layout.getChildren().addAll(titleLabel, reviewArea, submitButton, backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Review Answer");
    }
    
    private void createQuestionReview(Question question, Stage primaryStage, User user) {
    	VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Create Question Review");
        // Add a text area for creating reviews
        TextArea reviewArea = new TextArea();
        reviewArea.setPromptText("Enter your review");

        // Submit reviews for a question
        Button submitButton = new Button("Submit Review");
        submitButton.setOnAction(e -> {
            String reviewText = reviewArea.getText();
            if (!reviewText.isEmpty()) {
                try {
                    Review review = new Review(question.getId(), "question", 0, reviewText, user.getUserName(), user.getName() + " (Reviewer)", true, Timestamp.from(Instant.now())); //Taylor edit 4/17
                    reviewDO.createReview(review);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your review has been submitted.");
                    alert.showAndWait();
                    displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Display error message if review is empty
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error message: review cannot be empty.");
                alert.showAndWait();
            }
        });
    
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> displayQuestionWithReplies(primaryStage, question, user));

        layout.getChildren().addAll(titleLabel, reviewArea, submitButton, backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Review Question");
    }
    
    private void displayQuestionWithReplies(Stage primaryStage, Question question, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Display question details
        Label askerLabel = new Label("Asker: " + question.getAuthorName());
        Label timestampLabel = new Label("Timestamp: " + question.getTimestamp());
        Label titleLabel = new Label("Title: " + question.getTitle());
        Label descriptionLabel = new Label("Question: " + question.getDescription());

        layout.getChildren().addAll(askerLabel, timestampLabel, titleLabel, descriptionLabel);
        
        // Add a button to review the question
        Button reviewQuestionButton = new Button("Review Question");
        reviewQuestionButton.setOnAction(e -> createQuestionReview(question, primaryStage, user));
        layout.getChildren().add(reviewQuestionButton);

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
                    
                    // Display reviews for the answer - Shreya S.
                    try {
                        List<Review> reviews = reviewDO.readReviews(answer.getId(), "answer");
                        if (reviews.isEmpty()) {
                            Label noReviewsLabel = new Label("No reviews yet.");
                            replyBox.getChildren().add(noReviewsLabel);
                        } else {
                            Label reviewsLabel = new Label("Answer Reviews:");
                            replyBox.getChildren().add(reviewsLabel);
                            for (Review review : reviews) {
                                Label reviewLabel = new Label("Review by " + review.getReviewerName() + ": " + review.getContent());
                                replyBox.getChildren().add(reviewLabel);
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    
                    // Add a button to review the answer
                    Button reviewButton = new Button("Review Answer");
                    reviewButton.setOnAction(e -> createAnswerReview(answer, question, primaryStage, user));
                    replyBox.getChildren().add(reviewButton);
                    
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
    
 // (Janelle) Copied Sohan's method from StudentHomePage with modifications
    private void openMessageWindow(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Unread Feedback");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Ensure UI modification happens on JavaFX Application Thread
        Platform.runLater(() -> layout.getChildren().addAll(titleLabel));

        new Thread(() -> {
            try {
                List<Message> messages = new MessageDO(databaseHelper).readMessages(user.getUserName());

                Platform.runLater(() -> {
                    for (Message message : messages) {
                        HBox messageBox = new HBox(10);
                        messageBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

                        Label messageLabel = new Label("From: " + message.getSenderUserName() + " - " + message.getContent());
                        
                        // (Janelle) Dynamic box size increase, for the two lines in every feedback message
                        // Later I may add code allowing you to read longer messages without the "..." at the end
                        HBox.setHgrow(messageLabel, Priority.ALWAYS);
                        
                        messageBox.getChildren().add(messageLabel);
                        layout.getChildren().add(messageBox);
                    }
                     
                });

                // Mark messages as read in a separate thread (no UI modifications here)
                for (Message message : messages) {
                    new MessageDO(databaseHelper).markMessageAsRead(message.getId());
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
    
}