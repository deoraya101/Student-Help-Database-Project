// Added by Janelle (TP4)
package application;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import databasePart1.DatabaseHelper;

/**
 * 
 */
/**
 * The {@code RatingDO} class manages the persistence and retrieval of reviewer ratings.
 * It supports creation, updating, and fetching reviewer scores.
 */
public class RatingDO {
	
	private final DatabaseHelper databaseHelper;

    /**
 * Constructs a RatingDO with database access.
 * @param databaseHelper shared DB connection helper
 */
public RatingDO(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    /**
 * Inserts a new rating row for a reviewer.
 * @param rating the Rating object to store
 * @throws SQLException if DB error occurs
 */
public void createRating(Rating rating) throws SQLException {
    	String query = "INSERT INTO Ratings (voterUsername, reviewerUsername, vote, neutral, upvoted, downvoted) VALUES (?,?,?,?,?,?)";
    	try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
        	pstmt.setString(1, rating.getVoterUsername());
        	pstmt.setString(2, rating.getReviewerUsername());
        	pstmt.setInt(3, rating.getVote());
        	pstmt.setBoolean(4, rating.getNeutralStatus());
        	pstmt.setBoolean(5, rating.getUpvotedStatus());
        	pstmt.setBoolean(6, rating.getDownvotedStatus());
        	pstmt.executeUpdate();
        }
    }
	
	/**
 * Updates a previously stored rating with new data.
 * @param rating the updated rating
 * @throws SQLException on DB error
 */
public void updateRating(Rating rating) throws SQLException {
		String query = "UPDATE Ratings SET vote = ? WHERE voterUsername = ? AND reviewerUsername = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
        	pstmt.setInt(1, rating.getVote());
        	pstmt.setString(2, rating.getVoterUsername());
        	pstmt.setString(3, rating.getReviewerUsername());
        	pstmt.executeUpdate();
        }
	}
	
	
	// Adds up the votes of every user on a particular reviewer
	public int getRating(String reviewerUsername) throws SQLException {
		int rating = 0;
		String query = "SELECT vote FROM Ratings WHERE reviewerUsername = ?";
		try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, reviewerUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	System.out.println(rs.getInt("vote"));
            	rating += rs.getInt("vote");
            }
		}
		return rating;
	}
	
	// For retrieving Rating objects for a particular user. Can be used to check if there is no object yet (null)
	public Rating getRatingObject(String voterUsername, String reviewerUsername) throws SQLException {
		Rating rating = null;
		String query = "SELECT * FROM Ratings WHERE voterUsername = ? AND reviewerUsername = ?";
		try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
			pstmt.setString(1, voterUsername);
			pstmt.setString(2, reviewerUsername);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	rating = new Rating(
            			rs.getString("voterUsername"),
            			rs.getString("reviewerUsername"),
            			rs.getInt("vote"),
            			rs.getBoolean("neutral"),
            			rs.getBoolean("upvoted"),
            			rs.getBoolean("downvoted")
            			);
            }
		}
		return rating;
	}
	
}