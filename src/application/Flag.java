package application;

import java.sql.Timestamp;

/**
 * The {@code Flag} class represents a user-submitted flag in the system.
 * It stores details such as flag color, flag message, author, and timestamp.
 */

public class Flag {
	private int flagId;
	private String targetType;
	private int targetId;
	private String flagColor;
	private String flagMessage;
	private String staffUserName;
	private String staffName;
	private Timestamp timestamp;
	
	/** Flag constructor
	 * 
	 * @param flagId
	 * @param targetType this is a question, answer, or private message
	 * @param targetId this it the id of a question, answer, or private message
	 * @param flagColor this is Yellow or Red
	 * @param flagMessage
	 * @param staffUserName
	 * @param staffName
	 * @param timestamp
	 */
	public Flag(int flagId, String targetType, int targetId, String flagColor, String flagMessage, String staffUserName, String staffName, Timestamp timestamp) {
		this.flagId = flagId;
		this.targetType = targetType;
		this.targetId = targetId;
		this.flagColor = flagColor;
		this.flagMessage = flagMessage;
		this.staffUserName = staffUserName;
		this.staffName = staffName;
		this.timestamp = timestamp; 
	}
	
	/**
 * Returns the unique identifier of the flag.
 * @return flag ID
 */
public int getFlagId() { return flagId; }
	/**
 * Sets the flag ID.
 * @param flagId the flag ID to set
 */
public void setFlagId(int flagId) { this.flagId = flagId; }
	/**
 * Gets the type of target (question, answer, or private message).
 * @return target type
 */
public String getTargetType() {return targetType; }
	/**
 * Gets the target ID of the flagged item.
 * @return target ID
 */
public int getTargetId() { return targetId; }
	/**
 * Returns the color of the flag (Yellow or Red).
 * @return flag color
 */
public String getFlagColor() {return flagColor; }
	/**
 * Sets the color of the flag.
 * @param flagColor color to set (Yellow or Red)
 */
public void setFlagColor(String flagColor) {this.flagColor = flagColor; }
    /**
 * Gets the message/reason associated with the flag.
 * @return flag message
 */
public String getFlagMessage() { return flagMessage; }
    /**
 * Sets the flag message.
 * @param flagMessage message/reason for flagging
 */
public void setFlagMessage(String flagMessage) { this.flagMessage = flagMessage; }
    /**
 * Gets the username of the staff who created the flag.
 * @return staff username
 */
public String getStaffUserName() { return staffUserName; }
    /**
 * Gets the full name of the staff who flagged the content.
 * @return staff name
 */
public String getStaffName() { return staffName; }
    /**
 * Gets the timestamp when the flag was created.
 * @return flag timestamp
 */
public Timestamp getTimestamp() { return timestamp; }
}