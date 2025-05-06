//Created by Shreya

package application;


/**
 * A data object (DO) that stores raw admin request data retrieved from the database.
 *
 * <p>This class serves as a simple transport model for admin request fields including:
 * ID, requester ID, description, request timestamp, status, and resolution data.</p>
 */

public class AdminRequestDO {
    private int id;
    private int requesterId;
    private String description;
    private long requestDate;
    private boolean isClosed;
    private String adminResponse;
    private long resolutionDate;

    // Getters and Setters
    /**
 * Gets the ID of the request.
 * @return request ID
 */
public int getId() {
        return id;
    }

    /**
 * Sets the ID of the request.
 * @param id request ID
 */
public void setId(int id) {
        this.id = id;
    }

    /**
 * Gets the ID of the requesting user.
 * @return requester user ID
 */
public int getRequesterId() {
        return requesterId;
    }

    /**
 * Sets the ID of the requester.
 * @param requesterId requester user ID
 */
public void setRequesterId(int requesterId) {
        this.requesterId = requesterId;
    }

    /**
 * Gets the request description.
 * @return description text
 */
public String getDescription() {
        return description;
    }

    /**
 * Sets the request description.
 * @param description the request text
 */
public void setDescription(String description) {
        this.description = description;
    }

    /**
 * Gets the request creation timestamp.
 * @return timestamp as long
 */
public long getRequestDate() {
        return requestDate;
    }

    /**
 * Sets the request creation timestamp.
 * @param requestDate timestamp as long
 */
public void setRequestDate(long requestDate) {
        this.requestDate = requestDate;
    }

    /**
 * Returns whether the request is closed.
 * @return true if closed
 */
public boolean isClosed() {
        return isClosed;
    }

    /**
 * Sets the closed status of the request.
 * @param closed true to mark as closed
 */
public void setClosed(boolean closed) {
        isClosed = closed;
    }

    /**
 * Gets the admin's response.
 * @return admin response text
 */
public String getAdminResponse() {
        return adminResponse;
    }

    /**
 * Sets the admin's response.
 * @param adminResponse text of the admin reply
 */
public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    /**
 * Gets the resolution date as a timestamp.
 * @return resolution timestamp
 */
public long getResolutionDate() {
        return resolutionDate;
    }

    /**
 * Sets the resolution timestamp.
 * @param resolutionDate timestamp to set
 */
public void setResolutionDate(long resolutionDate) {
        this.resolutionDate = resolutionDate;
    }
}