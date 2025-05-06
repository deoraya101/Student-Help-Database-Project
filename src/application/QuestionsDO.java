package application;
import databasePart1.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code QuestionsDO} class handles database operations for the {@link Question} class.
 * It supports creation, retrieval, update, deletion, resolution marking, and searching of questions.
 */

/**
 * The {@code QuestionsDO} class handles all database operations for
 * question objects, including creation, retrieval, filtering, and deletion.
 */
public class QuestionsDO {
    private final DatabaseHelper databaseHelper;

    // Constructor
    /**
 * Constructs a QuestionsDO with database access.
 * @param databaseHelper helper for DB connection
 */
public QuestionsDO(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Create a new question
    /**
 * Saves a new question to the database.
 * @param question the question to insert
 * @throws SQLException if a DB error occurs
 */
public void createQuestion(Question question) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "INSERT INTO Questions (title, description, author, authorName, timestamp, isResolved, prevQuestionID) VALUES (?, ?, ?, ?, ?, ?, ?)"; //Taylor Edit 2/19
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) { //Taylor edit 4/22
            pstmt.setString(1, question.getTitle());
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getDescription());
            pstmt.setString(3, question.getAuthor());
            pstmt.setString(4, question.getAuthorName()); //Taylor Edit 2/19
            pstmt.setTimestamp(5, question.getTimestamp()); //Taylor Edit 2/19
            pstmt.setBoolean(6, false);
            pstmt.setInt(7, -1); // Default for prevQuestionID - Janelle
            pstmt.executeUpdate();
            
          //Taylor edit 4/22
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    question.setQuestionId(generatedKeys.getInt(1));
                }
            }
        }
    }
    
    /**
 * Retrieves a question by its ID.
 * @param questionId the ID of the question
 * @return the question object
 * @throws SQLException on DB error
 */
public Question getQuestionById(int questionId) throws SQLException {
        String query = "SELECT * FROM Questions WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Question(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("author"),
                    rs.getString("authorName"),
                    rs.getTimestamp("timestamp"),
                    rs.getBoolean("isResolved")
                );
            }
        }
        return null;
    }

    // Read all questions
    public List<Question> readQuestions() throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM Questions ORDER BY timestamp DESC";
        try (Statement stmt = databaseHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("author"),
                    rs.getString("authorName"), //Taylor Edit 2/19
                    rs.getTimestamp("timestamp"), // Taylor edit 2/19
                    rs.getBoolean("isResolved") //Rhea TP2 2/20
                ));
            }
        }
        return questions;
    }

    // Read all recently asked questions
    public List<Question> readRecentQuestions() throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM Questions WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '1' DAY ORDER BY timestamp DESC";
        try (Statement stmt = databaseHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("author"),
                    rs.getString("authorName"),
                    rs.getTimestamp("timestamp"),
                    rs.getBoolean("isResolved") //Rhea TP2 2/20
                ));
            }
        }
        return questions;
    }

    // Read all resolved questions
    public List<Question> readResolvedQuestions() throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        List<Question> resolvedQuestions = new ArrayList<>();
        String query = "SELECT * FROM Questions WHERE isResolved = TRUE ORDER BY timestamp DESC";
        try (Statement stmt = databaseHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                resolvedQuestions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("author"),
                    rs.getString("authorName"),
                    rs.getTimestamp("timestamp"),
                    rs.getBoolean("isResolved") //Rhea TP2 2/20
                ));
            }
        }
        return resolvedQuestions;
    }
    

    // Mark a question as resolved
    public void markQuestionAsResolved(int questionId) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "UPDATE Questions SET isResolved = TRUE WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        }
    }

    // Update a question
    public void updateQuestion(Question question) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "UPDATE Questions SET title = ?, description = ? WHERE id = ? AND isResolved = FALSE";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getDescription());
            pstmt.setInt(3, question.getId());
            pstmt.executeUpdate();
        }
    }

    // Delete a question
    /**
 * Deletes a question by ID.
 * @param questionId the ID of the question
 * @throws SQLException on DB error
 */
public void deleteQuestion(int questionId) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "DELETE FROM Questions WHERE id = ? AND isResolved = FALSE";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        }
    }
    
    // Search questions by keyword
    public List<Question> searchQuestions(String keyword) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM Questions WHERE LOWER(title) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("author"),
                    rs.getString("authorName"), //Taylor Edit 2/19
                    rs.getTimestamp("timestamp"), // Taylor edit 2/19
                    rs.getBoolean("isResolved") //Rhea TP2 2/20
                ));
            }
        }
        return questions;
    }
    
 // Get a single question; added by Janelle (2/28)
    // Currently unused and may or may not work properly
  	public Question getQuestion(int prevQuestionID) throws SQLException {
  		if (databaseHelper.getConnection() == null) {
             throw new SQLException("Database connection is not established.");
         }
  		
  		String query = "SELECT * FROM Questions WHERE prevQuestionID = ?";
  		
  		try(Statement stmt = databaseHelper.getConnection().createStatement();
  			ResultSet rs = stmt.executeQuery(query)) {
  		
  		Question question = new Question(
  				 rs.getInt("id"),
                 rs.getString("title"),
                 rs.getString("description"),
                 rs.getString("author"),
                 rs.getString("authorName"),
                 rs.getTimestamp("timestamp"),
                 rs.getBoolean("isResolved")
  				);
  		
  		return question;
  		}
  		catch (Exception e) {
  			return new Question();
  		}
  	}
  	
  //Rhea P4
  	public List<Question> readUnresolvedQuestions() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM Questions WHERE isResolved = FALSE ORDER BY timestamp DESC";
        try (PreparedStatement stmt = databaseHelper.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("author"),
                    rs.getString("authorName"),
                    rs.getTimestamp("timestamp"),
                    false
                ));
            }
        }
        return questions;
    }
}