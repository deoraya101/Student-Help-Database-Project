
/**
 * The {@code ReviewerProfilePage} class provides a user interface for viewing
 * and editing a reviewer's profile including their name, bio, and total rating score.
 *
 * <p>Students and instructors can view the profile, while only the reviewer
 * themselves may edit their bio via a pop-up input dialog. The profile also displays
 * the total score calculated from all ratings across users.</p>
 *
 * <p>This class integrates with the {@code DatabaseHelper} and {@code RatingDO} for
 * persistent data handling.</p>
 */

package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.*;

import java.sql.SQLException;


public class ReviewerProfilePage {
    private final DatabaseHelper databaseHelper;

    /**
 * Constructs the ReviewerProfilePage with DB helper.
 * @param databaseHelper shared DB connection
 */
public ReviewerProfilePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
 * Displays the profile page for the specified reviewer.
 * @param primaryStage main JavaFX window
 * @param reviewer the reviewer whose profile is being viewed
 * @param currentUser the currently logged-in user
 */
public void show(Stage primaryStage, User reviewer, User currentUser) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label nameLabel = new Label(reviewer.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label bioLabel = new Label("Bio:");
        TextArea bioArea = new TextArea();
        bioArea.setWrapText(true);
        bioArea.setEditable(false);

        try {
            String bio = databaseHelper.getReviewerBio(reviewer.getUserName());
            if (bio != null) bioArea.setText(bio);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button saveBioButton = new Button("Edit Bio");
        saveBioButton.setDisable(!reviewer.getUserName().equals(currentUser.getUserName()));
        saveBioButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(bioArea.getText());
            dialog.setTitle("Edit Bio");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter your bio:");
            dialog.showAndWait().ifPresent(updatedBio -> {
                try {
                    databaseHelper.saveReviewerBio(reviewer.getUserName(), updatedBio);
                    bioArea.setText(updatedBio);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Bio updated successfully.");
                    alert.showAndWait();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        });

        Label scoreTitle = new Label("Score:");
        Label scoreValue = new Label("0");

        try {
            int score = new RatingDO(databaseHelper).getRating(reviewer.getUserName());
            scoreValue.setText(String.valueOf(score));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new ReviewerHomePage(databaseHelper).show(primaryStage, currentUser));

        layout.getChildren().addAll(nameLabel, bioLabel, bioArea, saveBioButton, scoreTitle, scoreValue, backButton);

        Scene scene = new Scene(layout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviewer Profile");
    }
}
