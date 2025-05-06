//Taylor 4/1/25

package application;

import java.sql.Timestamp;

/**
 * The {@code Review} class represents feedback provided by reviewers on questions or answers.
 * It contains metadata such as target ID, reviewer identity, unread status, and timestamp.
 */

/**
 * The {@code Review} class represents a peer review of a question submitted by a student.
 * Includes metadata about the reviewer, question, feedback, and timestamps.
 */
public class Review {
    private int targetId;
    private String QorA;
    private int reviewId;
    private String content;
    private String originalContent;
    private String reviewerUserName;
    private String reviewerName;
    private boolean unread;
    private Timestamp lastUpdated;

    public Review(int targetId, String QorA, int reviewId, String content, String originalContent, String reviewerUserName, String reviewerName, boolean unread, Timestamp lastUpdated) {
        this.targetId = targetId;
        this.QorA = QorA;
        this.reviewId = reviewId;
        this.content = content;
        this.originalContent = originalContent;
        this.reviewerUserName = reviewerUserName;
        this.reviewerName = reviewerName;
        this.unread = unread;
        this.lastUpdated = lastUpdated;
    }
    
    public Review(int targetId, String qorA, int id, String content, String reviewer, String reviewerName, boolean unread, Timestamp lastUpdated) {
        this.targetId = targetId;
        this.QorA = qorA;
        this.reviewId = id;
        this.content = content;
        this.reviewerUserName = reviewer;
        this.reviewerName = reviewerName;
        this.unread = unread;
        this.lastUpdated = lastUpdated;
    }

    public int getTargetId() { return targetId; }
    public String getQorA() {return QorA; };
    /**
 * Gets the ID of the review.
 * @return review ID
 */
public int getReviewId() { return reviewId; }
    public String getContent() { return content; }
    public String getReviewer() { return reviewerUserName; }
    /**
 * Gets the full name of the reviewer.
 * @return reviewer name
 */
public String getReviewerName() { return reviewerName; }
    public void setContent(String content) { this.content = content; }
    public String getOriginalContent() { return originalContent;}
    public void setOriginalContent(String originalContent) { this.originalContent = originalContent;}
    public boolean isUnread() { return unread; }
    public Timestamp getLastUpdated() { return lastUpdated; }
    public void setUnread(boolean unread) { this.unread = unread; }
}