//Taylor created
package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import databasePart1.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * The {@code InstructorHomePage} class defines the interface for instructor users to explore questions, answers, and private feedback,
 * submit reviews, read reviews from other staff and instructors, read flags, dismiss flags, and delete inappropriate content.
 */

/**
 * The {@code InstructorHomePage} class provides the interface for instructor actions,
 * including reviewing students' questions, reviewing messages, and managing ratings.
 */
public class InstructorHomePage {
    private final DatabaseHelper databaseHelper;
    private final QuestionsDO questionsDO;
    private final AnswerDO answerDO;
    private final ReviewDO reviewDO;
    private final FlagDO flagDO;

    /**
 * Constructs the InstructorHomePage with DB access.
 * @param databaseHelper reference to the shared DB helper
 */
public InstructorHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.questionsDO = new QuestionsDO(databaseHelper);
        this.answerDO = new AnswerDO(databaseHelper);
        this.reviewDO = new ReviewDO(databaseHelper);
        this.flagDO = new FlagDO(databaseHelper);
    }
    
    /** Set up the instructor home page
     * 
     * @param primaryStage
     * @param user
     */
    /**
 * Displays the instructor homepage view.
 * @param primaryStage the main JavaFX window
 * @param user the logged-in instructor
 */
public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to display Hello user
        Label instructorLabel = new Label("Hello, Instructor!");
        instructorLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Buttons
        Button searchButton = new Button("Search Questions");
        Button allQuestionsButton = new Button("All Questions");
        Button recentQuestionsButton = new Button("Recent Questions");
        Button viewFeedbackButton = new Button("My Private Message Inbox");
        Button viewAllUsersFeedbackButton = new Button("All Users Private Feedback");
        Button returnButton = new Button("Return");
        Button myReviewsButton = new Button("My Reviews");
        Button flaggedQuestionsButton = new Button("Flagged Questions");
        Button flaggedAnswersButton = new Button("Flagged Answers");
        Button reviewerReqButton = new Button("View Reviewer Requests");
        Button requestAdminActionButton = new Button("Request Admin Action");
        Button rateReviewersButton = new Button("Rate a Reviewer"); //Janelle + Raya TP4
        
        // Event Handlers for Buttons
        searchButton.setOnAction(e -> handleSearch(primaryStage, user));
        allQuestionsButton.setOnAction(e -> handleAllQuestions(primaryStage, user));
        recentQuestionsButton.setOnAction(e -> handleRecentQuestions(primaryStage, user));
        returnButton.setOnAction(e -> new RolePickMenuPage(databaseHelper).show(primaryStage, user));
        viewFeedbackButton.setOnAction(e -> openMessageWindow(primaryStage,user));
        myReviewsButton.setOnAction(e -> handleMyReviews(primaryStage, user));
        viewAllUsersFeedbackButton.setOnAction(e -> handleAllMessages(primaryStage, user));
        flaggedQuestionsButton.setOnAction(e -> handleFlaggedQuestions(primaryStage, user));
        flaggedAnswersButton.setOnAction(e -> handleFlaggedAnswers(primaryStage, user));
        reviewerReqButton.setOnAction(a -> {new ReviewRequestsPage(databaseHelper).show(primaryStage, user);});
        requestAdminActionButton.setOnAction(e -> {new AdminRequestPage(databaseHelper, user).show(new Stage());});
        rateReviewersButton.setOnAction(e -> new RateReviewersPage(databaseHelper).show(primaryStage, user)); //Janelle + Raya TP4
        
        // Add components to layout
        layout.getChildren().addAll(instructorLabel, flaggedQuestionsButton, flaggedAnswersButton, allQuestionsButton, recentQuestionsButton, searchButton, viewAllUsersFeedbackButton, myReviewsButton, viewFeedbackButton, reviewerReqButton, requestAdminActionButton, rateReviewersButton, returnButton);

        // Set scene and stage
        Scene studentScene = new Scene(layout, 800, 400);
        primaryStage.setScene(studentScene);
        primaryStage.setTitle("Instructor Page");
    }
    
    /** Search all questions by keyword
     * 
     * @param primaryStage
     * @param user
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
    
    /** Retrieve all questions and redirect page or show alert
     * 
     * @param primaryStage
     * @param user
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
    
    /** Retrieve all questions asked within that last 24 hours
     * 
     * @param primaryStage
     * @param user
     */
    private void handleRecentQuestions(Stage primaryStage, User user) {
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
    
    /** Retrieve all reviews written by the user or show alert
     * 
     * @param primaryStage
     * @param user
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
    
    /** Display my reviews with private feedback counts
     * 
     * @param primaryStage
     * @param reviews
     * @param user
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

   /** Display feedback for a review
    * 
    * @param primaryStage
    * @param review
    * @param user
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
    
    /** Display questions page
     * 
     * @param primaryStage
     * @param questions
     * @param title
     * @param user
     */
    private void displayQuestions(Stage primaryStage, List<Question> questions, String title, User user) {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

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
            contentBox.getChildren().add(questionBox);
        }
        
        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Add a back button to return to the previous page
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));
        layout.getChildren().addAll(titleLabel, scrollPane, backButton);

        // Set the scene and stage
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
    }
    
    /** Create a review for an answer
     * 
     * @param answer
     * @param question
     * @param primaryStage
     * @param user
     */
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
                    Review review = new Review(answer.getId(), "answer", 0, reviewText, user.getUserName(), user.getName() + " (Instructor)", true, Timestamp.from(Instant.now()));
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
    
    /** Create a review for a question
     * 
     * @param question
     * @param primaryStage
     * @param user
     */
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
                    Review review = new Review(question.getId(), "question", 0, reviewText, user.getUserName(), user.getName() + " (Instructor)", true, Timestamp.from(Instant.now()));
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
    
    /** Display question with answers, flags, and reviews
     * 
     * @param primaryStage
     * @param question
     * @param user
     */
    private void displayQuestionWithReplies(Stage primaryStage, Question question, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        // Display question details
        Label askerLabel = new Label("Asker: " + question.getAuthorName());
        Label timestampLabel = new Label("Timestamp: " + question.getTimestamp());
        Label titleLabel = new Label("Title: " + question.getTitle());
        Label descriptionLabel = new Label("Question: " + question.getDescription());

        contentBox.getChildren().addAll(askerLabel, timestampLabel, titleLabel, descriptionLabel);
        
        // button to see reviews for the question
    	Button viewQuestionReviews = new Button("View Question Reviews");
    	//viewQuestionReviews.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
    	viewQuestionReviews.setOnAction(e -> handleQuestionReviews(primaryStage, user, question));
    	contentBox.getChildren().add(viewQuestionReviews);
    	
    	// button to see flag comments for the question
    	Button viewQuestionFlags = new Button("View Question Flags");
    	//viewQuestionFlags.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
    	viewQuestionFlags.setOnAction(e -> displayQuestionFlags(primaryStage, user, question));
    	contentBox.getChildren().add(viewQuestionFlags);
        
        // Add a button to review the question
        Button reviewQuestionButton = new Button("Review Question");
        reviewQuestionButton.setOnAction(e -> createQuestionReview(question, primaryStage, user));
        contentBox.getChildren().add(reviewQuestionButton);
        
        // Add delete question button
        Button deleteQuestionButton = new Button("Delete Question");
        deleteQuestionButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        deleteQuestionButton.setOnAction(e -> {
        	Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        	confirmAlert.setTitle("Confirm Deletion");
        	confirmAlert.setHeaderText("Are you sure you want to delete this question?");
        	confirmAlert.setContentText("This will also delete all associated answers.");

        	// Yes and No buttons
        	ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        	ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
        	confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        	// Show and wait for user input
        	Optional<ButtonType> result = confirmAlert.showAndWait();
        	if (result.isPresent() && result.get() == yesButton) {
        	    deleteQuestion(primaryStage, question, user);
        	}
        });
        contentBox.getChildren().add(deleteQuestionButton);

        // Display all replies
        try {
            List<Answer> answers = answerDO.readAnswers(question.getId());
            if (answers.isEmpty()) {
                Label noAnswersLabel = new Label("No replies yet.");
                contentBox.getChildren().add(noAnswersLabel);
            } else {
                Label repliesLabel = new Label("Replies:");
                contentBox.getChildren().add(repliesLabel);
                
                for (Answer answer : answers) {
                    VBox replyBox = new VBox(10);
                    replyBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
                    
                    // display flag
                    boolean hasRed = false;
                    boolean hasYellow = false;
                    try {
        	            List<Flag> flags = flagDO.readFlags(answer.getId(), "answer");
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
                    	replyBox.getChildren().add(redFlag);
                    }
                    else if (hasYellow) {
                    	Label yellowFlag = new Label("🚩");
                    	yellowFlag.setStyle("-fx-text-fill: #ffd500; -fx-font-size: 20px; -fx-padding: 2px;");
                    	replyBox.getChildren().add(yellowFlag);
                    }

                    // Display the reply author and content
                    Label answerLabel = new Label("Reply by " + answer.getAuthorName() + ": " + answer.getContent());
                    replyBox.getChildren().add(answerLabel);
                    
                  //display reviews for the answer
                	Button viewAnswerReviews = new Button("View Answer Reviews");
                	//viewAnswerReviews.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
                	viewAnswerReviews.setOnAction(e -> displayAnswerReviews(primaryStage, user, question, answer));
                	replyBox.getChildren().add(viewAnswerReviews);
                	
                	// button to see flag comments for the answer
                	Button viewAnswerFlags = new Button("View Answer Flags");
                	//viewAnswerFlags.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
                	viewAnswerFlags.setOnAction(e -> displayAnswerFlags(primaryStage, user, question, answer));
                	replyBox.getChildren().add(viewAnswerFlags);
                    
                    // Add a button to review the answer
                    Button reviewButton = new Button("Review Answer");
                    reviewButton.setOnAction(e -> createAnswerReview(answer, question, primaryStage, user));
                    replyBox.getChildren().add(reviewButton);
                    
                    // delete answer
                    Button deleteAnswerButton = new Button("Delete Answer");
                    deleteAnswerButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                    deleteAnswerButton.setOnAction(e -> {
                    	Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    	confirmAlert.setTitle("Confirm Deletion");
                    	confirmAlert.setHeaderText("Are you sure you want to delete this answer?");

                    	// Yes and No buttons
                    	ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                    	ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
                    	confirmAlert.getButtonTypes().setAll(yesButton, noButton);

                    	// Show and wait for user input
                    	Optional<ButtonType> result = confirmAlert.showAndWait();
                    	if (result.isPresent() && result.get() == yesButton) {
                    	    deleteReply(primaryStage, question, answer, user);
                    	}
                    });
                    replyBox.getChildren().add(deleteAnswerButton);
                    
                    contentBox.getChildren().add(replyBox);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Add Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> handleAllQuestions(primaryStage, user));
      
        layout.getChildren().addAll(scrollPane, backButton);

        // Set the scene and stage
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Details");
    }
    
    private void deleteQuestion(Stage primaryStage, Question question, User user) {
        try {
            questionsDO.deleteQuestion(question.getId());
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("The question has been deleted.");
            alert.showAndWait();
            show(primaryStage, user); // Refresh the page
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    /** Delete an answer
     * 
     * @param primaryStage
     * @param question
     * @param answer
     * @param user
     */
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
    
    /** Display all flags for an answer
     * 
     * @param primaryStage
     * @param user
     * @param question
     */
    public void displayQuestionFlags(Stage primaryStage, User user, Question question){
    	try {
    		List<Flag> flags = flagDO.readFlags(question.getId(), "question");
            if (flags.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Question Flags");
                alert.setHeaderText(null);
                alert.setContentText("No flags found.");
                alert.showAndWait();
            } else {
            	VBox layout = new VBox(10);
                layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

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
    
    /** Display all flags for an answer
     * 
     * @param primaryStage
     * @param user
     * @param question
     * @param answer
     */
    public void displayAnswerFlags(Stage primaryStage, User user, Question question, Answer answer){
    	try {
    		List<Flag> flags = flagDO.readFlags(answer.getId(), "answer");
            if (flags.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Answer Flags");
                alert.setHeaderText(null);
                alert.setContentText("No flags found.");
                alert.showAndWait();
            } else {
            	VBox layout = new VBox(10);
                layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

                Label answerFlagsLabel = new Label("Answer Flags:");
                layout.getChildren().add(answerFlagsLabel);
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
    
    /** Display all reviews for an answer
     * 
     * @param primaryStage
     * @param user
     * @param question
     * @param answer
     */
    public void displayAnswerReviews(Stage primaryStage, User user, Question question, Answer answer) {
    	try {
            List<Review> answerReviews = reviewDO.readReviews(answer.getId(), "answer");
            if (answerReviews.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Answer Reviews");
                alert.setHeaderText(null);
                alert.setContentText("No reviews found.");
                alert.showAndWait();
            } else {
            	VBox layout = new VBox(10);
                layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

                Label titleLabel = new Label("Question Reviews");
                titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
                
                List<Review> reviews = reviewDO.readReviews(answer.getId(), "answer");
                for (Review review : answerReviews) {
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
    
    /** Page for unread private messages
     * 
     * @param primaryStage
     * @param user
     */
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
    
    /** Open window to reply to a private message
     * 
     * @param primaryStage
     * @param user
     * @param receiverUserName
     */
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
    
    /** Display reviews for a question
     * 
     * @param primaryStage
     * @param user
     * @param question
     */
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
    
    /** Open window to edit a review
     * 
     * @param primaryStage
     * @param review
     * @param user
     */
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
    
    /** Open window to send a private message to the reviewer
     * 
     * @param primaryStage
     * @param reviewContent
     * @param receiverName
     * @param receiverUserName
     * @param senderUser
     */
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
    
    /** Retrieve all messages and redirect page or show alert
     * 
     * @param primaryStage
     * @param user
     */
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
    
    /** Display all messages from all users
     * 
     * @param primaryStage
     * @param messages
     * @param title
     * @param user
     */
    public void displayAllMessages(Stage primaryStage, List<Message> messages, String title, User user) {
    	VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        for (Message message : messages) {
            HBox messageBox = new HBox(10); // Create an HBox for each message
            messageBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
            
            // Add a button to view the message
            Button questionButton = new Button("Message by " + message.getSenderUserName() + ": " + message.getContent());
            questionButton.setOnAction(e -> displayMessageWithFlags(primaryStage, message, user));
            messageBox.getChildren().add(questionButton);
            
            Button privateMessageButton = new Button("Private Message this Sender");
            privateMessageButton.setOnAction(e -> openPrivateMessageWindow(primaryStage, message.getSenderUserName(), user));
            messageBox.getChildren().add(privateMessageButton);
            
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
            contentBox.getChildren().add(messageBox);
        }
        
        scrollPane.setContent(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Add a back button to return to the previous page
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));
        layout.getChildren().addAll(titleLabel, scrollPane, backButton);

        // Set the scene and stage
        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
    }
    
    /** Open the private message window
     * 
     * @param primaryStage
     * @param receiverUserName
     * @param senderUser
     */
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
   
    /** Display message with all associated flags
     * 
     * @param primaryStage
     * @param message
     * @param user
     */
    private void displayMessageWithFlags(Stage primaryStage, Message message, User user) {
    	VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Display message details
        Label messageLabel = new Label("Message by " + message.getSenderUserName() + ": " + message.getContent());
        layout.getChildren().add(messageLabel);
        
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
    
    /** Retrieve flagged questions and redirect page or display alert message
     * 
     * @param primaryStage
     * @param user
     */
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
    
    /** Display flagged questions dashboard
     * 
     * @param primaryStage
     * @param flaggedQuestions
     * @param user
     */
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
    
    /** Retrieve flagged answers and redirect page or display alert message
     * 
     * @param primaryStage
     * @param user
     */
    private void handleFlaggedAnswers(Stage primaryStage, User user) {
        try {
            List<FlaggedContent> flaggedAnswers = flagDO.getFlaggedAnswers();
            if (flaggedAnswers.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Flagged Answers");
                alert.setHeaderText(null);
                alert.setContentText("No flagged questions found.");
                alert.showAndWait();
            } else {
                displayFlaggedAnswers(primaryStage, flaggedAnswers, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to retrieve flagged answers: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /** display flagged answers dashboard
     * 
     * @param primaryStage
     * @param flaggedAnswers
     * @param user
     */
    private void displayFlaggedAnswers(Stage primaryStage, List<FlaggedContent> flaggedAnswers, User user) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(15));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        Label titleLabel = new Label("Flagged Answers Dashboard");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");

        ScrollPane scrollPane = new ScrollPane();
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        for (FlaggedContent flagged : flaggedAnswers) {
            VBox flaggedBox = new VBox(10);
            flaggedBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 15;");
            flaggedBox.setMaxWidth(Double.MAX_VALUE);

            Label answerContent = new Label("Answer: " + flagged.getContent());
            answerContent.setWrapText(true);
            
            HBox answerInfo = new HBox(10);
            answerInfo.getChildren().addAll(
                new Label("Author: " + flagged.getAuthorName())
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
            
            Button viewButton = new Button("View Question & Answer");
            viewButton.setOnAction(e -> {
                try {
                    Answer answer = answerDO.getAnswerById(flagged.getContentId());
                    Question question = questionsDO.getQuestionById(answer.getQuestionId());
                    if (answer != null) {
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
                answerContent, answerInfo,
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
        primaryStage.setTitle("Flagged Answers Dashboard");
    }
    
}