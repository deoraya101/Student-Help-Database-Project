// Created by Taylor 
package application;
import databasePart1.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code ReviewDO} class manages CRUD operations for {@link Review} objects in the database.
 * It includes support for reading, updating, and marking reviews as read.
 */

/**
 * The {@code ReviewDO} class provides database operations for review management,
 * including creation, editing, and retrieval of review entries.
 */
public class ReviewDO {
    private final DatabaseHelper databaseHelper;

    /**
 * Constructs a ReviewDO with a database connection helper.
 * @param databaseHelper database access class
 */
public ReviewDO(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Create a new review
    /**
 * Inserts a new review into the database.
 * @param review review object
 * @throws SQLException if database insert fails
 */
public void createReview(Review review) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        System.out.println("Creating review with unread status: " + review.isUnread());

        String query = "INSERT INTO Reviews (targetId, QorA, content, reviewerUserName, reviewerName, unread, lastUpdated) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, review.getTargetId());
            pstmt.setString(2, review.getQorA());
            pstmt.setString(3, review.getContent());
            pstmt.setString(4, review.getReviewer());
            pstmt.setString(5, review.getReviewerName());
            pstmt.setBoolean(6, review.isUnread());
            pstmt.setTimestamp(7, review.getLastUpdated());
            pstmt.executeUpdate();
            
            System.out.println("Review created successfully with unread status: " + review.isUnread());
        }
    }

    // Read all reviews for a question or answer
    public List<Review> readReviews(int targetId, String QorA) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT * FROM Reviews WHERE targetId = ? AND QorA = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, targetId);
            pstmt.setString(2, QorA);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                boolean unread = rs.getBoolean("unread");
                System.out.println("Reading review with unread status: " + unread);
                reviews.add(new Review(
                    rs.getInt("targetId"),
                    rs.getString("QorA"),
                    rs.getInt("id"),
                    rs.getString("content"),
                    rs.getString("originalContent"), 
                    rs.getString("reviewerUserName"),
                    rs.getString("reviewerName"),
                    unread,
                    rs.getTimestamp("lastUpdated")
                ));
            }
        }
        return reviews;
    }

    public void markReviewsAsRead(int targetId, String QorA) throws SQLException {
        try {
            String query = "UPDATE Reviews SET unread = FALSE WHERE targetId = ? AND QorA = ?";
            try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
                pstmt.setInt(1, targetId);
                pstmt.setString(2, QorA);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // If the error is due to the column not existing, try to add it
            if (e.getMessage().contains("Column \"UNREAD\" not found")) {
                try {
                    // Add the column if it doesn't exist
                    databaseHelper.getConnection().createStatement().execute(
                        "ALTER TABLE Reviews ADD COLUMN unread BOOLEAN DEFAULT TRUE"
                    );
                    // Try the update again
                    String query = "UPDATE Reviews SET unread = FALSE WHERE targetId = ? AND QorA = ?";
                    try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
                        pstmt.setInt(1, targetId);
                        pstmt.setString(2, QorA);
                        pstmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    // If we still can't do it, just log the error and continue
                    System.err.println("Could not add unread column: " + ex.getMessage());
                }
            } else {
                // For other SQL errors, rethrow
                throw e;
            }
        }
    }
    
    public List<Message> getFeedbackMessages(int reviewId, String reviewerUserName) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM Messages WHERE receiverUserName = ? AND content LIKE ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, reviewerUserName);
            pstmt.setString(2, "%Review ID: " + reviewId + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                    rs.getInt("id"),
                    rs.getString("senderUserName"),
                    rs.getString("receiverUserName"),
                    rs.getString("content"),
                    rs.getTimestamp("timestamp"),
                    rs.getBoolean("isRead")
                ));
            }
        }
        return messages;
    }
    //Rhea Soni 
    
    public List<Review> getReviewsByReviewer(String reviewerUserName) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String query = "SELECT * FROM Reviews WHERE reviewerUserName = ? ORDER BY lastUpdated DESC";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, reviewerUserName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reviews.add(new Review(
                    rs.getInt("targetId"),
                    rs.getString("QorA"),
                    rs.getInt("id"),
                    rs.getString("content"),
                    rs.getString("reviewerUserName"),
                    rs.getString("reviewerName"),
                    rs.getBoolean("unread"),
                    rs.getTimestamp("lastUpdated")
                ));
            }
        }
        return reviews;
    }
    
    //Rhea Soni 4/2 P3
    
    public int countUnreadMessagesForReviews(String reviewerUserName) throws SQLException {
        String query = "SELECT COUNT(*) FROM Messages WHERE receiverUserName = ? AND isRead = FALSE AND content LIKE '%review%'";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, reviewerUserName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
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


    // Update a review's fields
    /**
 * Updates an existing review with new content.
 * @param review the review to update
 * @throws SQLException if database update fails
 */
public void updateReview(Review review) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "UPDATE Reviews SET content = ?, unread = ?, lastUpdated = ? WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, review.getContent());
            pstmt.setBoolean(2, review.isUnread());
            pstmt.setTimestamp(3, review.getLastUpdated());
            pstmt.setInt(4, review.getReviewId());
            pstmt.executeUpdate();
        }
    }
    
    //Janelle + Raya TP4
    /**
 * Fetches all usernames of users who have submitted at least one review.
 * @return list of reviewer usernames
 * @throws SQLException on DB access error
 */
public List<String> getAllReviewerUsernames() throws SQLException {
        List<String> usernames = new ArrayList<>();
        String query = "SELECT DISTINCT reviewerUserName FROM Reviews";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                usernames.add(rs.getString("reviewerUserName"));
            }
        }
        return usernames;
    }


}