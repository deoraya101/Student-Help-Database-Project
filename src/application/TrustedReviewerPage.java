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
import java.util.Map;

/**
 * The {@code TrustedReviewerPage} class allows student users to manage their trusted reviewers.
 * Users can add or update reviewer weights used to influence answer rankings.
 */

public class TrustedReviewerPage {
    private final DatabaseHelper databaseHelper;
    private final User student;

    public TrustedReviewerPage(DatabaseHelper databaseHelper, User student) {
        this.databaseHelper = databaseHelper;
        this.student = student;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");

        Label title = new Label("Trusted Reviewers");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Map.Entry<String, Integer>> reviewerTable = new TableView<>();
        TableColumn<Map.Entry<String, Integer>, String> reviewerColumn = new TableColumn<>("Reviewer Username");
        reviewerColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Map.Entry<String, Integer>, String> weightColumn = new TableColumn<>("Weight");
        weightColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getValue())));

        reviewerTable.getColumns().addAll(reviewerColumn, weightColumn);
        reviewerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            Map<String, Integer> trustedReviewers = databaseHelper.getTrustedReviewers(student.getUserName());
            ObservableList<Map.Entry<String, Integer>> data = FXCollections.observableArrayList(trustedReviewers.entrySet());
            reviewerTable.setItems(data);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        TextField reviewerField = new TextField();
        reviewerField.setPromptText("Reviewer Username");

        Spinner<Integer> weightSpinner = new Spinner<>(1, 10, 5);
        weightSpinner.setEditable(true);

        Button addOrUpdateButton = new Button("Add / Update Reviewer");
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: green;");

        addOrUpdateButton.setOnAction(e -> {
            String reviewerUserName = reviewerField.getText().trim();
            int weight = weightSpinner.getValue();

            if (reviewerUserName.isEmpty()) {
                statusLabel.setText("Reviewer username cannot be empty.");
                return;
            }

            try {
                databaseHelper.setTrustedReviewer(student.getUserName(), reviewerUserName, weight);
                statusLabel.setText("Reviewer added/updated successfully.");
                Map<String, Integer> updatedList = databaseHelper.getTrustedReviewers(student.getUserName());
                reviewerTable.setItems(FXCollections.observableArrayList(updatedList.entrySet()));
            } catch (SQLException ex) {
                ex.printStackTrace();
                statusLabel.setText("Failed to add/update reviewer.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> new StudentHomePage(databaseHelper).show(primaryStage, student));

        layout.getChildren().addAll(title, reviewerTable, reviewerField, weightSpinner, addOrUpdateButton, statusLabel, backButton);

        Scene scene = new Scene(layout, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Trusted Reviewers");
        primaryStage.show();
    }
}