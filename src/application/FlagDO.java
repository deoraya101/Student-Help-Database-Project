package application;
import databasePart1.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code FlagDO} class manages CRUD operations for {@link Flag} objects in the database.
 * It includes support for creating, reading, updating, and deleting flags on questions, answers, and private feedback.
 */

public class FlagDO {
    private final DatabaseHelper databaseHelper;

    /**
 * Constructs a FlagDO with the given database helper.
 * @param databaseHelper a database connection helper
 */
public FlagDO(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /** Create a new flag
     * 
     * @param flag
     * @throws SQLException
     */
    /**
 * Inserts a new flag into the database.
 * @param flag the flag object to insert
 * @throws SQLException if a database error occurs
 */
public void createFlag(Flag flag) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "INSERT INTO Flags (targetType, targetId, flagColor, flagMessage, staffUserName, staffName, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
        	pstmt.setString(1, flag.getTargetType());
            pstmt.setInt(2, flag.getTargetId());
            pstmt.setString(3, flag.getFlagColor());
            pstmt.setString(4, flag.getFlagMessage());
            pstmt.setString(5, flag.getStaffUserName());
            pstmt.setString(6, flag.getStaffName());
            pstmt.setTimestamp(7, flag.getTimestamp());
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    flag.setFlagId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /** Read all flags for a question or answer or private feedback
     * 
     * @param targetId this is the id of a question, answer, or private message
     * @param targetType this is a question, answer, or private message
     * @return a list of flags
     * @throws SQLException
     */
    public List<Flag> readFlags(int targetId, String targetType) throws SQLException {
        List<Flag> flags = new ArrayList<>();
        String query = "SELECT * FROM Flags WHERE targetId = ? AND targetType = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, targetId);
            pstmt.setString(2, targetType);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                flags.add(new Flag(
                	rs.getInt("id"),
                	rs.getString("targetType"),
                    rs.getInt("targetId"),
                    rs.getString("flagColor"),
                    rs.getString("flagMessage"), 
                    rs.getString("staffUserName"),
                    rs.getString("staffName"),
                    rs.getTimestamp("timestamp")
                ));
            }
        }
        return flags;
    }

    /** Update a flag
     * 
     * @param flag
     * @throws SQLException
     */
    /**
 * Updates the flag message for an existing flag.
 * @param flag the flag with updated message
 * @throws SQLException if a database error occurs
 */
public void updateFlagMessage(Flag flag) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "UPDATE Flags SET flagMessage = ? WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, flag.getFlagMessage());
            pstmt.setInt(2, flag.getFlagId());
            pstmt.executeUpdate();
        }
    }

    /** Delete a flag
     * 
     * @param flagId
     * @throws SQLException
     */
    /**
 * Deletes a flag from the database.
 * @param flagId the ID of the flag to delete
 * @throws SQLException if a database error occurs
 */
public void deleteFlag(int flagId) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "DELETE FROM Flags WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, flagId);
            pstmt.executeUpdate();
        }
    }

    /** Read all messages
     * 
     * @return a list of messages
     * @throws SQLException
     */
    public List<Message> readAllMessages() throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM Messages ORDER BY timestamp DESC";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
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
    
    /** get all flagged questions
     * 
     * @return a list of FlaggedContent
     * @throws SQLException
     */
    /**
 * Retrieves all flagged questions from the database.
 * @return list of flagged question content
 * @throws SQLException if a database error occurs
 */
public List<FlaggedContent> getFlaggedQuestions() throws SQLException {
        List<FlaggedContent> flaggedContents = new ArrayList<>();
        String query = "SELECT f.id, f.targetType, f.targetId, f.flagColor, f.flagMessage, " +
                      "f.staffUserName, f.staffName, f.timestamp as flaggedAt, " +
                      "q.authorName, q.title, q.description, q.timestamp as contentTimestamp " +
                      "FROM Flags f JOIN Questions q ON f.targetId = q.id " +
                      "WHERE f.targetType = 'question' " +
                      "ORDER BY f.timestamp DESC";
        
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                flaggedContents.add(new FlaggedContent(
                    rs.getInt("id"),
                    rs.getString("targetType"),
                    rs.getInt("targetId"),
                    rs.getString("flagColor"),
                    rs.getString("flagMessage"),
                    rs.getString("staffUserName"),
                    rs.getString("staffName"),
                    rs.getTimestamp("flaggedAt"),
                    rs.getString("authorName"),
                    rs.getString("title") + ": " + rs.getString("description"),
                    rs.getTimestamp("contentTimestamp")
                ));
            }
        }
        return flaggedContents;
    }
    
    // Taylor 4/20
    /** get all flagged answers
     * 
     * @return a list of FlaggedContent
     * @throws SQLException
     */
    /**
 * Retrieves all flagged answers from the database.
 * @return list of flagged answer content
 * @throws SQLException if a database error occurs
 */
public List<FlaggedContent> getFlaggedAnswers() throws SQLException {
        List<FlaggedContent> flaggedContents = new ArrayList<>();
        String query = "SELECT f.id, f.targetType, f.targetId, f.flagColor, f.flagMessage, " +
                      "f.staffUserName, f.staffName, f.timestamp as flaggedAt, " +
                      "a.authorName, a.content " +
                      "FROM Flags f JOIN Answers a ON f.targetId = a.id " +
                      "WHERE f.targetType = 'answer' " +
                      "ORDER BY f.timestamp DESC";
        
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                flaggedContents.add(new FlaggedContent(
                    rs.getInt("id"),
                    rs.getString("targetType"),
                    rs.getInt("targetId"),
                    rs.getString("flagColor"),
                    rs.getString("flagMessage"),
                    rs.getString("staffUserName"),
                    rs.getString("staffName"),
                    rs.getTimestamp("flaggedAt"),
                    rs.getString("authorName"),
                    rs.getString("content")
                ));
            }
        }
        return flaggedContents;
    }
    

    public Message getMessageById(int id) throws SQLException {
        String query = "SELECT * FROM Messages WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Message(
                    rs.getInt("id"),
                    rs.getString("senderUserName"),
                    rs.getString("receiverUserName"),
                    rs.getString("content"),
                    rs.getTimestamp("timestamp"),
                    rs.getBoolean("isRead")
                );
            }
        }
        return null;
    }

    public void dismissFlag(int flagId) throws SQLException {
        String query = "DELETE FROM Flags WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, flagId);
            pstmt.executeUpdate();
        }
    }
    
}