//Created by Rhea Soni
package application;
import databasePart1.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The {@code AnswerDO} (Data Object) class handles database operations related to answers.
 * 
 * <p>This includes:
 * <ul>
 *   <li>Creating, updating, and deleting answers</li>
 *   <li>Reading all answers or solution-only answers</li>
 *   <li>Sorting answers based on reviewer trust weights</li>
 * </ul>
 */

public class AnswerDO {
    private final DatabaseHelper databaseHelper;

    public AnswerDO(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Create a new answer
    public void createAnswer(Answer answer) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "INSERT INTO Answers (questionId, content, authorUserName, authorName, isSolution) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) { //Taylor edit 4/22 
            pstmt.setInt(1, answer.getQuestionId());
            pstmt.setString(2, answer.getContent());
            pstmt.setString(3, answer.getAuthor());
            pstmt.setString(4, answer.getAuthorName());
            pstmt.setBoolean(5, answer.isSolution());
            pstmt.executeUpdate();
            
          //Taylor edit 4/22
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    answer.setAnswerId(generatedKeys.getInt(1));
                }
            }
        }
        
    }
    /**
 * Retrieves an answer by its ID.
 * @param answerId the answer ID
 * @return the Answer object if found, otherwise null
 */
public Answer getAnswerById(int answerId) throws SQLException {
        String query = "SELECT * FROM Answers WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Answer(
                    rs.getInt("id"),
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("authorUserName"),
                    rs.getString("authorName"),
                    rs.getBoolean("isSolution")
                );
            }
        }
        return null;
    }

    // Read all answers for a question
    public List<Answer> readAnswers(int questionId) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String query = "SELECT * FROM Answers WHERE questionId = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                answers.add(new Answer(
                    rs.getInt("id"),
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("authorUserName"),
                    rs.getString("authorName"),
                    rs.getBoolean("isSolution")
                ));
            }
        }
        return answers;
    }

    // Update an answer
    /**
 * Updates an existing answer's content if it is not a solution.
 * @param answer the answer object with updated content
 */
public void updateAnswer(Answer answer) throws SQLException {
        String query = "UPDATE Answers SET content = ? WHERE id = ? AND isSolution = FALSE";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, answer.getContent());
            pstmt.setInt(2, answer.getId());
            pstmt.executeUpdate();
        }
    }

    // Delete an answer
    /**
 * Deletes an answer if it is not marked as a solution.
 * @param answerId ID of the answer to delete
 */
public void deleteAnswer(int answerId) throws SQLException {
        String query = "DELETE FROM Answers WHERE id = ? AND isSolution = FALSE";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            pstmt.executeUpdate();
        }
    }
    
    //Added by Raya*
    // Mark answer as resolved
    /**
 * Marks a specific answer as a solution in the database.
 * @param answerId the ID of the answer to update
 */
public void markAsSolution(int answerId) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "UPDATE Answers SET isSolution = TRUE WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            pstmt.executeUpdate();
        }
    }

//Added by Raya ** Assembles a list of answers that are solutions
    /**
 * Retrieves all answers that are marked as solutions.
 * @return list of solution answers
 */
public List<Answer> readSolutionAnswers() throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        List<Answer> solutionAnswers = new ArrayList<>();
        String query = "SELECT * FROM Answers WHERE isSolution = TRUE";
        try (Statement stmt = databaseHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                solutionAnswers.add(new Answer(
                    rs.getInt("id"),
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("authorUserName"),
                    rs.getString("authorName"),
                    rs.getBoolean("isSolution")
                ));
            }
        }
        return solutionAnswers;
    }
   
   // Add this method to your existing AnswerDO.java
    /**
 * Reads answers for a question and sorts them based on reviewer trust weights.
 * @param questionId the question ID
 * @param studentUserName the student's username
 * @return sorted list of answers
 */
public List<Answer> readAnswersSortedByReviewerWeight(int questionId, String studentUserName) throws SQLException {
        List<Answer> allAnswers = readAnswers(questionId); // existing fetch
        Map<String, Integer> trustedReviewers = databaseHelper.getTrustedReviewers(studentUserName);

        // Sort: higher weight first, unrated go to bottom
        allAnswers.sort((a1, a2) -> {
            int w1 = trustedReviewers.getOrDefault(a1.getAuthor(), -1);
            int w2 = trustedReviewers.getOrDefault(a2.getAuthor(), -1);

            if (w1 == -1 && w2 == -1) return 0;
            if (w1 == -1) return 1;
            if (w2 == -1) return -1;
            return Integer.compare(w2, w1); // DESC
        });

        return allAnswers;
    }
}