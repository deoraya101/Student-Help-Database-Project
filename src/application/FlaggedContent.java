//Created by Rhea S. TP4
package application;



import java.sql.Timestamp;


/**
 * The {@code FlaggedContent} class holds detailed information about flagged
 * content in the system, such as questions or answers.
 *
 * <p>Includes flag metadata (color, reason, author), and original content details.</p>
 */

public class FlaggedContent {
    private int id;
    private String contentType;
    private int contentId;
    private String urgency;
    private String reason;
    private String flaggerUserName;
    private String flaggerName;
    private Timestamp flaggedAt;
    private String authorName;
    private String content;
    private Timestamp contentTimestamp;

    public FlaggedContent(int id, String contentType, int contentId, String urgency, 
                         String reason, String flaggerUserName, String flaggerName, 
                         Timestamp flaggedAt, String authorName, String content, 
                         Timestamp contentTimestamp) {
        this.id = id;
        this.contentType = contentType;
        this.contentId = contentId;
        this.urgency = urgency;
        this.reason = reason;
        this.flaggerUserName = flaggerUserName;
        this.flaggerName = flaggerName;
        this.flaggedAt = flaggedAt;
        this.authorName = authorName;
        this.content = content;
        this.contentTimestamp = contentTimestamp;
    }
    
    public FlaggedContent(int id, String contentType, int contentId, String urgency, 
            String reason, String flaggerUserName, String flaggerName, 
            Timestamp flaggedAt, String authorName, String content) {
		this.id = id;
		this.contentType = contentType;
		this.contentId = contentId;
		this.urgency = urgency;
		this.reason = reason;
		this.flaggerUserName = flaggerUserName;
		this.flaggerName = flaggerName;
		this.flaggedAt = flaggedAt;
		this.authorName = authorName;
		this.content = content;
	}

    // Getters
    /**
 * Gets the flag ID.
 * @return flag ID
 */
public int getId() { return id; }
    /**
 * Returns the type of flagged content (question, answer).
 * @return content type
 */
public String getContentType() { return contentType; }
    /**
 * Gets the ID of the flagged content.
 * @return content ID
 */
public int getContentId() { return contentId; }
    /**
 * Gets the urgency level of the flag (e.g., Yellow, Red).
 * @return urgency level
 */
public String getUrgency() { return urgency; }
    /**
 * Gets the reason or message of the flag.
 * @return flag reason
 */
public String getReason() { return reason; }
    /**
 * Gets the username of the staff who flagged the content.
 * @return flagger username
 */
public String getFlaggerUserName() { return flaggerUserName; }
    /**
 * Gets the full name of the flagger.
 * @return flagger name
 */
public String getFlaggerName() { return flaggerName; }
    /**
 * Gets the timestamp when the content was flagged.
 * @return timestamp of flag
 */
public Timestamp getFlaggedAt() { return flaggedAt; }
    /**
 * Gets the name of the author of the flagged content.
 * @return author name
 */
public String getAuthorName() { return authorName; }
    /**
 * Gets the content text.
 * @return content
 */
public String getContent() { return content; }
    /**
 * Gets the timestamp when the content was created.
 * @return content creation timestamp
 */
public Timestamp getContentTimestamp() { return contentTimestamp; }
}