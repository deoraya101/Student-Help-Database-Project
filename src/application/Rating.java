// Added by Janelle (TP4)
package application;

/**
 * The <code>Rating</code> class is used to track ratings on Reviewers made by Students and Instructors.
 * A <code>Rating</code> object is created for each Reviewer, and both Students and Instructors can contribute to their rating
 * by upvoting or downvoting them from [NOT implemented yet: their Reviewer Profile].
 */
/**
 * The {@code Rating} class represents a user's rating of a reviewer.
 * Tracks scores, flags for whether a user has voted, and supports up/down logic.
 */
public class Rating {
	private String voterUsername;
	private String reviewerUsername;
	private int vote;
	private boolean neutral;
	private boolean upvoted;
	private boolean downvoted;
	
	public Rating(String voterUsername, String reviewerUsername, int vote, boolean neutral, boolean upvoted, boolean downvoted) {
		this.voterUsername = voterUsername;
		this.reviewerUsername = reviewerUsername;
		this.vote = vote;
		this.neutral = neutral;
		this.upvoted = upvoted;
		this.downvoted = downvoted;
	}
	
	public String getVoterUsername() { return voterUsername; }
	public String getReviewerUsername() { return reviewerUsername; }
	public int getVote() { return vote; }
	public boolean getNeutralStatus() { return neutral; }
	public boolean getUpvotedStatus() { return upvoted; }
	public boolean getDownvotedStatus() { return downvoted; }
	public void changeRating(int newValue) { vote += newValue; }
	
	// Voting functions
	
	/**
 * Increments the rating score by 1.
 */
public void upvote() {
		if (neutral) {
			vote++;
			neutral = false;
			upvoted = true;
			downvoted = false;
		} else if (upvoted) {
			vote--;
			upvoted = false;
			neutral = true;
			downvoted = false;
		} else if (downvoted) {
			vote += 2;
			downvoted = false;
			upvoted = true;
			neutral = false;
		}
	}
	
	/**
 * Decrements the rating score by 1.
 */
public void downvote() {
		if (neutral) {
			vote--;
			neutral = false;
			downvoted = true;
			upvoted = false;
		} else if (upvoted) {
			vote -= 2;
			upvoted = false;
			downvoted = true;
			neutral = false;
		} else if (downvoted) {
			vote++;
			downvoted = false;
			neutral = true;
			upvoted = false;
		}
	}
	
	// The methods below should be used only in InstructorHomePage
	
	/**
 * Increments score by 2 if instructor has not already voted.
 */
public void instructorUpvote() {
		if (neutral) {
			vote += 2;
			neutral = false;
			upvoted = true;
			downvoted = false;
		} else if (upvoted) {
			vote -= 2;
			upvoted = false;
			neutral = true;
			downvoted = false;
		} else if (downvoted) {
			vote += 4;
			downvoted = false;
			upvoted = true;
			neutral = false;
		}
	}
	
	/**
 * Decrements score by 2 if instructor has not already voted.
 */
public void instructorDownvote() {
		if (neutral) {
			vote -= 2;
			neutral = false;
			downvoted = true;
			upvoted = false;
		} else if (upvoted) {
			vote -= 4;
			upvoted = false;
			downvoted = true;
			neutral = false;
		} else if (downvoted) {
			vote += 2;
			downvoted = false;
			neutral = true;
			upvoted = false;
		}
	}
	
}