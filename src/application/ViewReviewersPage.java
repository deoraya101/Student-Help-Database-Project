package application;

import databasePart1.DatabaseHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.*;

/**
 * A JavaFX page that displays all users with the "reviewer" role in a table,
 * along with the number of reviews they have given and any weight assigned by
 * the current student.
 * <p>
 * The table shows the following columns:
 * <ul>
 *   <li>Username</li>
 *   <li>Name</li>
 *   <li>Reviews Given</li>
 *   <li>Assigned Weight</li>
 * </ul>
 * </p>
 * <p>
 * Provides a "Back" button to return to the student home page.
 * </p>
 */
public class ViewReviewersPage {

    private final DatabaseHelper databaseHelper;
    private final User student;

    /**
     * Constructs the ViewReviewersPage.
     * @param databaseHelper
     * @param student
     */
    public ViewReviewersPage(DatabaseHelper databaseHelper, User student) {
        this.databaseHelper = databaseHelper;
        this.student = student;
    }

    /**
     * Builds and shows the JavaFX scene listing all reviewers.
     * @param primaryStage
     */
    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");

        Label title = new Label("Available Reviewers");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<User> reviewerTable = new TableView<>();

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUserName()));

        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));

        TableColumn<User, String> scoreCol = new TableColumn<>("Reviews Given");
        scoreCol.setCellValueFactory(cellData -> {
            try {
                int reviewCount = databaseHelper.getReviewCountByReviewer(cellData.getValue().getUserName());
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(reviewCount));
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        TableColumn<User, String> weightCol = new TableColumn<>("Assigned Weight");
        weightCol.setCellValueFactory(cellData -> {
            try {
                Map<String, Integer> weights = databaseHelper.getTrustedReviewers(student.getUserName());
                String reviewerUserName = cellData.getValue().getUserName();
                if (weights.containsKey(reviewerUserName)) {
                    return new javafx.beans.property.SimpleStringProperty(String.valueOf(weights.get(reviewerUserName)));
                } else {
                    return new javafx.beans.property.SimpleStringProperty("Unrated");
                }
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });

        reviewerTable.getColumns().addAll(usernameCol, nameCol, scoreCol, weightCol);
        reviewerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            List<User> reviewers = getAllReviewers();
            ObservableList<User> data = FXCollections.observableArrayList(reviewers);
            reviewerTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new StudentHomePage(databaseHelper).show(primaryStage, student));

        layout.getChildren().addAll(title, reviewerTable, backButton);

        Scene scene = new Scene(layout, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Reviewers");
        primaryStage.show();
    }

    /**
     * Queries the database for all users, filters to those with the "reviewer" role,
     * and returns a list of corresponding User objects.
     * @return
     * @throws SQLException
     */
    private List<User> getAllReviewers() throws SQLException {
        List<User> allUsers = new ArrayList<>();
        var rs = databaseHelper.getAllUsers();

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