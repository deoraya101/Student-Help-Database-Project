//Created by Shreya S.
package application;

import java.sql.Timestamp;

/**
 * The {@code Message} class represents a private message between two users in the system.
 * It stores sender, receiver, content, timestamp, and read status.
 */

/**
 * The {@code Message} class models a message between users including sender,
 * recipient, timestamp, and message content.
 */
public class Message {
    private int id;
    private String senderUserName;
    private String receiverUserName;
    private String content;
    private Timestamp timestamp;
    private boolean isRead;

    public Message(int id, String senderUserName, String receiverUserName, String content, Timestamp timestamp, boolean isRead) {
        this.id = id;
        this.senderUserName = senderUserName;
        this.receiverUserName = receiverUserName;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public int getId() { return id; }
    /**
 * Gets sender's username.
 * @return username
 */
public String getSenderUserName() { return senderUserName; }
    public String getReceiverUserName() { return receiverUserName; }
    /**
 * Gets the content of the message.
 * @return message text
 */
public String getContent() { return content; }
    /**
 * Gets the timestamp when message was sent.
 * @return message timestamp
 */
public Timestamp getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }

    public void setRead(boolean read) { isRead = read; }
}