package testing;

import databasePart1.DatabaseHelper;
import application.User;
import application.ViewReviewersPage;

import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@link ViewReviewersPage} class
 * using the real database connection provided by DatabaseHelper.
 * <p>
 * These tests seed users directly into the database with various roles
 * and invoke the private {@code getAllReviewers()} method via reflection
 * to verify that only users with the "reviewer" role are returned.
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SohanTest {

    private DatabaseHelper db;
    private ViewReviewersPage page;
    private User testStudentUser;

    /**
     * Establishes a database connection once before all tests.
     *
     * @throws SQLException if connecting to the database fails
     */
    @BeforeAll
    void setupAll() throws SQLException {
        db = new DatabaseHelper();
        db.connectToDatabase();
    }

    /**
     * Closes the database connection after all tests complete.
     */
    @AfterAll
    void tearDownAll() {
        db.closeConnection();
    }

    /**
     * Clears all data and initializes the ViewReviewersPage instance
     * before each test to ensure isolation.
     *
     * @throws SQLException if clearing the database fails
     */
    @BeforeEach
    void beforeEach() throws SQLException {
        db.clearAllData();
        testStudentUser = new User("Test Student", "stud@test.com", "stud1", "pw", new ArrayList<>());
        page = new ViewReviewersPage(db, testStudentUser);
    }

    /**
     * Seeds three users into the database:
     * <ul>
     *   <li>alice1 (role: reviewer)</li>
     *   <li>bob (role: student)</li>
     *   <li>charlie (roles: reviewer, student)</li>
     * </ul>
     * <p>
     * Verifies that {@code getAllReviewers()} returns only alice1 and charlie.
     * </p>
     *
     * @throws Exception if reflection or database operations fail
     */
    @Test
    void testGetAllReviewers_filtersByRole() throws Exception {
        // seed users with roles
        db.register(new User("Alice", "a@test.com", "alice1", "pw",
            new ArrayList<>(List.of("reviewer"))));
        db.register(new User("Bob",   "b@test.com", "bob",    "pw",
            new ArrayList<>(List.of("student"))));
        db.register(new User("Charlie","c@test.com","charlie","pw",
            new ArrayList<>(List.of("reviewer","student"))));

        // invoke private getAllReviewers
        Method method = ViewReviewersPage.class.getDeclaredMethod("getAllReviewers");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<User> reviewers = (List<User>) method.invoke(page);

        // assert only reviewer-role users are returned
        assertEquals(2, reviewers.size(),
            "Should return exactly those users with the reviewer role");
        List<String> names =
            reviewers.stream().map(User::getUserName).toList();
        assertTrue(names.contains("alice1"));
        assertTrue(names.contains("charlie"));
        assertFalse(names.contains("bob"));
    }

    /**
     * Verifies that {@code getAllReviewers()} returns an empty list
     * when the database contains no users.
     *
     * @throws Exception if reflection or database operations fail
     */
    @Test
    void testGetAllReviewers_emptyWhenNoUsers() throws Exception {
        // no users seeded
        Method method = ViewReviewersPage.class.getDeclaredMethod("getAllReviewers");
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<User> reviewers = (List<User>) method.invoke(page);

        assertTrue(reviewers.isEmpty(),
            "Should return an empty list when no users exist");
    }

    /**
     * Verifies that {@code getAllReviewers()} propagates SQLExceptions
     * when the database access layer fails (e.g., closed connection).
     *
     * @throws Exception if reflection fails
     */
    @Test
    void testGetAllReviewers_throwsOnSQLException() throws Exception {
        // isolate failure on a separate helper
        DatabaseHelper badDb = new DatabaseHelper();
        badDb.connectToDatabase();
        badDb.clearAllData();
        badDb.closeConnection();

        ViewReviewersPage badPage = new ViewReviewersPage(badDb, testStudentUser);
        Method method = ViewReviewersPage.class.getDeclaredMethod("getAllReviewers");
        method.setAccessible(true);

        // unwrap to assert the underlying SQLException
        SQLException thrown = assertThrows(SQLException.class, () -> {
            try {
                @SuppressWarnings({"unused","unchecked"})
                List<User> unused = (List<User>) method.invoke(badPage);
            } catch (java.lang.reflect.InvocationTargetException ite) {
                throw (SQLException) ite.getCause();
            }
        });
        assertNotNull(thrown.getMessage());
    }
}