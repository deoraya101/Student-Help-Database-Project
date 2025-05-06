//Janelle + Raya TP4
package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import databasePart1.*;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code RateReviewersPage} class provides a UI for students and instructors
 * to rate reviewers using upvote/downvote buttons and see current ratings.
 */
public class RateReviewersPage {
    private final DatabaseHelper databaseHelper;
    private final RatingDO ratingDO;

    /**
 * Constructs the ratings page.
 * @param databaseHelper reference to DB helper
 */
public RateReviewersPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.ratingDO = new RatingDO(databaseHelper);
    }

    /**
 * Displays the reviewer rating page.
 * @param primaryStage the JavaFX window
 * @param user the logged-in student or instructor
 */
public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Rate a Reviewer");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        layout.getChildren().add(titleLabel);

        try {
            List<User> reviewers = getAllReviewersFromDB();
            for (User reviewer : reviewers) {
                if (reviewer.getUserName().equals(user.getUserName())) continue;

                Rating rating = ratingDO.getRatingObject(user.getUserName(), reviewer.getUserName());
                if (rating == null) {
                    rating = new Rating(user.getUserName(), reviewer.getUserName(), 0, true, false, false);
                    ratingDO.createRating(rating);
                }

                StringProperty ratingString = new SimpleStringProperty(String.valueOf(ratingDO.getRating(reviewer.getUserName())));
                final Rating currentRating = rating;
                final StringProperty currentRatingString = ratingString;

                Label reviewerLabel = new Label(reviewer.getName() + " (" + reviewer.getUserName() + ")");
                Label scoreLabel = new Label();
                scoreLabel.textProperty().bind(ratingString);

                Button upvote = new Button("⬆");
                upvote.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                Button downvote = new Button("⬇");
                downvote.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                upvote.setOnAction(e -> {
                	try {
                    	
                    	if (user.getRoles().contains("instructor")) {
                    		currentRating.instructorUpvote();
                    	} else {
                    		currentRating.upvote();
                    	}
                        
                        ratingDO.updateRating(currentRating);
                        currentRatingString.set(String.valueOf(ratingDO.getRating(reviewer.getUserName())));
                    } catch (SQLException ex) { ex.printStackTrace(); }
                });

                downvote.setOnAction(e -> {
                	try {
                    	
                    	if (user.getRoles().contains("instructor")) {
                    		currentRating.instructorDownvote();
                    	} else {
                    		currentRating.downvote();
                    	}
                        
                        ratingDO.updateRating(currentRating);
                        currentRatingString.set(String.valueOf(ratingDO.getRating(reviewer.getUserName())));
                    } catch (SQLException ex) { ex.printStackTrace(); }
                });

                HBox reviewerBox = new HBox(10, reviewerLabel, upvote, scoreLabel, downvote);
                reviewerBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");
                layout.getChildren().add(reviewerBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            if (user.getRoles().contains("instructor"))
                new InstructorHomePage(databaseHelper).show(primaryStage, user);
            else
                new StudentHomePage(databaseHelper).show(primaryStage, user);
        });
        layout.getChildren().add(backButton);

        Scene scene = new Scene(layout, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviewer Ratings");
    }

    /**
 * Fetches all users with the reviewer role from the database.
 * @return list of reviewer users
 * @throws SQLException if DB access fails
 */
private List<User> getAllReviewersFromDB() throws SQLException {
        List<User> allUsers = new ArrayList<>();
        ResultSet rs = databaseHelper.getAllUsers();

        while (rs.next()) {
            String name = rs.getString("name");
            String email = rs.getString("email");
            String userName = rs.getString("userName");
            ArrayList<String> roles = databaseHelper.getUserRoles(userName);
            if (roles.contains("reviewer")) {
                User reviewer = new User(name, email, userName, "", roles);
                allUsers.add(reviewer);
            }
        }

        return allUsers;
    }
}
