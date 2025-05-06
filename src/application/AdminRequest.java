//Created by Shreya

package application;

import java.util.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


/**
 * Represents an admin request submitted by a user.
 *
 * <p>Includes metadata like the requester's identity, description, date,
 * resolution status, and admin response.</p>
 *
 * <p>Used for viewing, managing, and resolving user-submitted requests.</p>
 */

public class AdminRequest {
    private int id;
    private User requester;
    private String description;
    private Date requestDate;
    private boolean isClosed;
    private String adminResponse;
    private Date resolutionDate;

    public AdminRequest(User requester, String description) {
        this.requester = requester;
        this.description = description;
        this.requestDate = new Date();
        this.isClosed = false;
    }

    // Getters
    /**
 * Gets the ID of the request.
 * @return request ID
 */
public int getId() {
        return id;
    }

    /**
 * Gets the user who submitted the request.
 * @return requester
 */
public User getRequester() {
        return requester;
    }

    /**
 * Gets the description of the request.
 * @return request description
 */
public String getDescription() {
        return description;
    }

    /**
 * Gets the date the request was made.
 * @return request date
 */
public Date getRequestDate() {
        return requestDate;
    }

    /**
 * Returns whether the request is closed.
 * @return true if closed, false if open
 */
public boolean isClosed() {
        return isClosed;
    }

    /**
 * Gets the admin's response.
 * @return admin response
 */
public String getAdminResponse() {
        return adminResponse;
    }

    /**
 * Gets the resolution date if closed.
 * @return resolution date or null
 */
public Date getResolutionDate() {
        return resolutionDate;
    }

    // Setters
    /**
 * Sets the request ID.
 * @param id the ID to set
 */
public void setId(int id) {
        this.id = id;
    }

    /**
 * Marks the request as closed or open and sets resolution date.
 * @param closed whether the request is closed
 */
public void setClosed(boolean closed) {
        isClosed = closed;
        if (closed) {
            this.resolutionDate = new Date();
        } else {
            this.resolutionDate = null;
        }
    }

    /**
 * Sets the admin's response to the request.
 * @param adminResponse the response message
 */
public void setAdminResponse(String adminResponse) {
        this.adminResponse = adminResponse;
    }

    /**
 * Sets the resolution date manually.
 * @param resolutionDate date the request was resolved
 */
public void setResolutionDate(Date resolutionDate) {
        this.resolutionDate = resolutionDate;
    }
    
    // Additional setter for Timestamp if needed
    /**
 * Sets the resolution date using a SQL Timestamp.
 * @param resolutionDate timestamp to convert and set
 */
public void setResolutionDate(Timestamp resolutionDate) {
        if (resolutionDate != null) {
            this.resolutionDate = new Date(resolutionDate.getTime());
        } else {
            this.resolutionDate = null;
        }
    }
    
    /**
 * Returns a formatted string representing the request's summary.
 * @return string summary of request
 */
@Override
public String toString() {
        String status = isClosed ? "CLOSED" : "OPEN";
        return String.format("[%s] %s (Requested by: %s on %s)",
                status,
                description,
                requester.getUserName(),
                new SimpleDateFormat("MMM dd, hh:mm a").format(requestDate));
    }
}