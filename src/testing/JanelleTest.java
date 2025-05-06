package testing;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.Rating;
import application.RatingDO;
import application.User;
import databasePart1.DatabaseHelper;
/**
 * This class is used to test the implementation of the rating/voting system as a metric for judging reviewers,
 * using the functions written in {@link Rating} and {@link RatingDO}.
 * <p>
 * Tests consist of various interactions with the database and
 * checking for consistent results between multiple users who may hold different roles.
 */
class JanelleTest {

	private static DatabaseHelper databaseHelper;
    private static RatingDO ratingDO;
	
    /** 
     * Sets up the database before every test.
     * @throws SQLException
     */ 
	@BeforeEach
    void setup() throws SQLException {
		databaseHelper = new DatabaseHelper();
		databaseHelper.connectToDatabase();
		ratingDO = new RatingDO(databaseHelper);
    }
	
	/**
	 * Closes the database before every test.
	 * @throws SQLException
	 */
	@AfterEach
	void reset() throws SQLException {
		databaseHelper.closeConnection();
	}
	
	/**
	 * Checks if the upvote function correctly adds 1 to the total rating for a reviewer.
	 * <p>
	 * This test compares the score within the <code>Rating</code> object to the <code>RatingDO</code> calculation of the total.
	 */
	@Test
	public void testUpvote() {
		
		try {
			// New student
			ArrayList<String> roles = new ArrayList<String>();
			roles.add("student");
	    	User student1 = new User("StudentOne", "st1@gmail.com", "student1", "Tester123!", roles);
	    	databaseHelper.register(student1);
	    	
	    	// New reviewer
	    	ArrayList<String> rev = new ArrayList<String>();
			rev.add("reviewer");
	    	User reviewer = new User("Reviewer", "rev@school.edu", "reviewer", "Tester123!", rev);
	    	databaseHelper.register(reviewer);
	    	
	    	// Creates rating, then adds it to the database
			Rating testRating = new Rating(student1.getUserName(), reviewer.getUserName(), 0, true, false, false);
	        ratingDO.createRating(testRating);
	        
	        // Updates rating object
			if (student1.getRoles().contains("instructor")) {
				testRating.instructorUpvote();
        	} else {
        		testRating.upvote();
        	}
			
			// Adds new change to database
			ratingDO.updateRating(testRating);
			
			// Student upvoted; rating count should be 1, equal to the singular vote
			assertEquals(ratingDO.getRating(reviewer.getUserName()), testRating.getVote());
			assertEquals(ratingDO.getRating(reviewer.getUserName()), 1);
			
		} catch (Exception e) {
			fail("Upvote didn't register correctly");
		}
	}
	
	/**
	 * Checks if the downvote function correctly subtracts 1 from the total rating for a reviewer.
	 */
	@Test
	public void testDownvote() {
		try {
			// New students
			ArrayList<String> roles = new ArrayList<String>();
			roles.add("student");
	    	User student2 = new User("StudentTwo", "st2@gmail.com", "student2", "Tester123!", roles);
	    	databaseHelper.register(student2);
	    	
	    	// New reviewer
	    	ArrayList<String> rev = new ArrayList<String>();
			rev.add("reviewer");
	    	User reviewer = new User("Reviewer", "rev@school.edu", "reviewer", "Tester123!", rev);
	    	databaseHelper.register(reviewer);
			
	    	// Creates rating, then adds it to the database
			Rating testRating2 = new Rating(student2.getUserName(), reviewer.getUserName(), 0, true, false, false);
	        ratingDO.createRating(testRating2);
	        
	        // Updates rating object
			if (student2.getRoles().contains("instructor")) {
				testRating2.instructorDownvote();
        	} else {
        		testRating2.downvote();
        	}
			
			// Adds new change to database
			ratingDO.updateRating(testRating2);
			
			// Student downvoted; rating count should be -1, equal to the singular vote
			assertEquals(ratingDO.getRating(reviewer.getUserName()), testRating2.getVote());
			assertEquals(ratingDO.getRating(reviewer.getUserName()), -1);
			
		} catch (Exception e) {
			fail("Downvote didn't register correctly");
		}
	}
	
	/**
	 * Checks if the instructor-specific upvote function correctly adds 2 to the total rating for a reviewer.
	 * <p>
	 * This test compares the score within the <code>Rating</code> object to the <code>RatingDO</code> calculation of the total.
	 */
	@Test
	public void testInstructorUpvote() {
		try {
    		// New instructor
    		ArrayList<String> inst = new ArrayList<String>();
    		inst.add("instructor");
    		User instructor = new User("Instructor", "inst@school.edu", "instructor", "Tester123!", inst);
    		databaseHelper.register(instructor);
	    	
	    	// New reviewer
	    	ArrayList<String> rev = new ArrayList<String>();
			rev.add("reviewer");
	    	User reviewer = new User("Reviewer", "rev@school.edu", "reviewer", "Tester123!", rev);
	    	databaseHelper.register(reviewer);
	    	
	    	// Creates rating, then adds it to the database
			Rating testRating = new Rating(instructor.getUserName(), reviewer.getUserName(), 0, true, false, false);
	        ratingDO.createRating(testRating);
	        
	        // Updates rating object
			if (instructor.getRoles().contains("instructor")) {
				testRating.instructorUpvote();
        	} else {
        		testRating.upvote();
        	}
			
			// Adds new change to database
			ratingDO.updateRating(testRating);
			
			// Instructor upvoted; count should be 2, equal to the singular vote
			assertEquals(ratingDO.getRating(reviewer.getUserName()), testRating.getVote());
			assertEquals(ratingDO.getRating(reviewer.getUserName()), 2);
			
		} catch (Exception e) {
			fail("Instructor upvote didn't register correctly");
		}
	}
	
	/**
	 * Checks if the instructor-specific downvote function correctly subtracts 2 from the total rating for a reviewer.
	 * <p>
	 * This test compares the score within the <code>Rating</code> object to the <code>RatingDO</code> calculation of the total.
	 */
	@Test
	public void testInstructorDownvote() {
		try {
    		// New instructor
    		ArrayList<String> inst = new ArrayList<String>();
    		inst.add("instructor");
    		User instructor = new User("Instructor", "inst@school.edu", "instructor", "Tester123!", inst);
    		databaseHelper.register(instructor);
	    	
	    	// New reviewer
	    	ArrayList<String> rev = new ArrayList<String>();
			rev.add("reviewer");
	    	User reviewer = new User("Reviewer", "rev@school.edu", "reviewer", "Tester123!", rev);
	    	databaseHelper.register(reviewer);
	    	
	    	// Creates rating, then adds it to the database
			Rating testRating = new Rating(instructor.getUserName(), reviewer.getUserName(), 0, true, false, false);
	        ratingDO.createRating(testRating);
	        
	        // Updates rating object
			if (instructor.getRoles().contains("instructor")) {
				testRating.instructorDownvote();
        	} else {
        		testRating.downvote();
        	}
			
			// Adds new change to database
			ratingDO.updateRating(testRating);
			
			// Instructor downvoted; count should be -2, equal to the singular vote
			assertEquals(ratingDO.getRating(reviewer.getUserName()), testRating.getVote());
			assertEquals(ratingDO.getRating(reviewer.getUserName()), -2);
			
		} catch (Exception e) {
			fail("Instructor downvote didn't register correctly");
		}
		
	}
	
	/**
	 * Checks if the total is calculated correctly after two students upvote the same reviewer.
	 * <p>
	 * This test compares the added values within the two <code>Rating</code> objects to the <code>RatingDO</code> calculation of the total.
	 */
	@Test
	public void testMultipleUsersVote() {
		try {
			// New student
			ArrayList<String> roles = new ArrayList<String>();
			roles.add("student");
			User student1 = new User("StudentOne", "st1@gmail.com", "student1", "Tester123!", roles);
			databaseHelper.register(student1);
			User student2 = new User("StudentTwo", "st2@gmail.com", "student2", "Tester123!", roles);
			databaseHelper.register(student2);
			
			// New reviewer
			ArrayList<String> rev = new ArrayList<String>();
			rev.add("reviewer");
			User reviewer = new User("Reviewer", "rev@school.edu", "reviewer", "Tester123!", rev);
			databaseHelper.register(reviewer);

			// Creates rating for 1st user, then adds it to the database
			Rating testRating = new Rating(student1.getUserName(), reviewer.getUserName(), 0, true, false, false);
			ratingDO.createRating(testRating);
			
			// Creates rating for 2nd user, then adds it to the database
			Rating testRating2 = new Rating(student2.getUserName(), reviewer.getUserName(), 0, true, false, false);
			ratingDO.createRating(testRating2);
			
			testRating.upvote();
			testRating2.upvote();
			// Adds new changes to database
			ratingDO.updateRating(testRating);
			ratingDO.updateRating(testRating2);
			
			// Two students upvoted, so the count for this reviewer should be 2
			assertEquals(ratingDO.getRating(reviewer.getUserName()), testRating.getVote()+testRating2.getVote());
			assertEquals(ratingDO.getRating(reviewer.getUserName()), 2);
			
		} catch (Exception e) {
			fail("Upvotes didn't register correctly");
		}
	}
	
	/**
	 * Checks if the total is calculated correctly after a student and instructor vote on the same reviewer.
	 * <p>
	 * This test compares the added values within the two <code>Rating</code> objects to the <code>RatingDO</code> calculation of the total.
	 */
	@Test
	public void testStudentAndInstructorVote() {
		try {
			// New student
			ArrayList<String> roles = new ArrayList<String>();
			roles.add("student");
			User student1 = new User("StudentOne", "st1@gmail.com", "student1", "Tester123!", roles);
			databaseHelper.register(student1);
			// New instructor
			ArrayList<String> inst = new ArrayList<String>();
			inst.add("instructor");
			User instructor = new User("Instructor", "inst@school.edu", "instructor", "Tester123!", inst);
			databaseHelper.register(instructor);
			
			// New reviewer
			ArrayList<String> rev = new ArrayList<String>();
			rev.add("reviewer");
			User reviewer = new User("Reviewer", "rev@school.edu", "reviewer", "Tester123!", rev);
			databaseHelper.register(reviewer);

			// Creates rating for student, then adds it to the database
			Rating testRating = new Rating(student1.getUserName(), reviewer.getUserName(), 0, true, false, false);
			ratingDO.createRating(testRating);
			
			// Creates rating for instructor, then adds it to the database
			Rating testRating2 = new Rating(instructor.getUserName(), reviewer.getUserName(), 0, true, false, false);
			ratingDO.createRating(testRating2);
			
			testRating.upvote();
			testRating2.instructorDownvote();
			// Adds new changes to database
			ratingDO.updateRating(testRating);
			ratingDO.updateRating(testRating2);
			
			// Student upvoted and instructor downvoted, so the result should be -1
			assertEquals(ratingDO.getRating(reviewer.getUserName()), testRating.getVote()+testRating2.getVote());
			assertEquals(ratingDO.getRating(reviewer.getUserName()), -1);
			
		} catch (Exception e) {
			fail("Upvotes didn't register correctly");
		}
	}
	
} // End of file