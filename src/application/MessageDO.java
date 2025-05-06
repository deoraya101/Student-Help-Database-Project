//Created by Shreya S.
package application;

import databasePart1.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code MessageDO} class provides database access methods for storing and retrieving messages.
 * It allows sending messages, reading unread messages, marking messages as read,
 * and counting unread messages.
 */

/**
 * The {@code MessageDO} class handles database operations for messages,
 * including sending, retrieving, and storing message objects.
 */
public class MessageDO {
    private final DatabaseHelper databaseHelper;

    /**
 * Constructs a MessageDO with access to the database.
 * @param databaseHelper database connection helper
 */
public MessageDO(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Create a new message
    public void createMessage(Message message) throws SQLException {
        String query = "INSERT INTO Messages (senderUserName, receiverUserName, content, timestamp, isRead) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, message.getSenderUserName());
            pstmt.setString(2, message.getReceiverUserName());
            pstmt.setString(3, message.getContent());
            pstmt.setTimestamp(4, message.getTimestamp());
            pstmt.setBoolean(5, message.isRead());
            pstmt.executeUpdate();
        }
    }

    // Read messages for a user
    public List<Message> readMessages(String receiverUserName) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM Messages WHERE receiverUserName = ? AND isRead = FALSE";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, receiverUserName);
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

    // Mark a message as read
    public void markMessageAsRead(int messageId) throws SQLException {
        String query = "UPDATE Messages SET isRead = TRUE WHERE id = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, messageId);
            pstmt.executeUpdate();
        }
    }
    
    //Added by Sohan R.
    // Count unread messages for a user
    public int countUnreadMessages(String receiverUserName) throws SQLException {
        String query = "SELECT COUNT(*) FROM Messages WHERE receiverUserName = ? AND isRead = FALSE";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, receiverUserName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}