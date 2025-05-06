
package testing;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import databasePart1.DatabaseHelper;

import java.sql.SQLException;

public class RayaTest {

    private DatabaseHelper db;

    @Before
    public void setUp() throws SQLException {
        db = new DatabaseHelper();
        db.connectToDatabase();
    }

    @Test
    public void testSaveAndGetReviewerBio() throws SQLException {
        String username = "testReviewer1";
        String bio = "This is a test bio.";

        db.saveReviewerBio(username, bio);
        String result = db.getReviewerBio(username);

        assertEquals("Saved bio should match retrieved bio", bio, result);
    }

    @Test
    public void testUpdateReviewerBio() throws SQLException {
        String username = "testReviewer2";
        db.saveReviewerBio(username, "Old Bio");

        String newBio = "Updated reviewer bio.";
        db.saveReviewerBio(username, newBio);

        String result = db.getReviewerBio(username);
        assertEquals("Updated bio should match", newBio, result);
    }

    @Test
    public void testGetReviewerBioWhenNoneExists() throws SQLException {
        String username = "nonexistentUser";
        String result = db.getReviewerBio(username);

        assertTrue("Should return empty string for nonexistent bio", result == null || result.isEmpty());
    }

    @Test
    public void testSaveBioWithSpecialCharacters() throws SQLException {
        String username = "specialUser";
        String bio = "Line1\nLine2\tTabbed — 😀 emoji and symbols #!@";

        db.saveReviewerBio(username, bio);
        String result = db.getReviewerBio(username);

        assertEquals("Bio with special characters should persist correctly", bio, result);
    }
}
