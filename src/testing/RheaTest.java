package testing;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import application.Question;
import application.QuestionsDO;
import application.StudentHomePage;
import application.User;
import javafx.application.Platform;
import javafx.stage.Stage;


class RheaTest {
    private static DatabaseHelper databaseHelper;
    private static QuestionsDO questionsDO;
    private static StudentHomePage studentHomePage;
    
    @BeforeAll
    
        static void setupJavaFX() throws Exception {
            // Only initialize if not already initialized
            try {
                Platform.startup(() -> {});
            } catch (IllegalStateException e) {
                // Already initialized, ignore
            }
        databaseHelper = new DatabaseHelper();
        databaseHelper.connectToDatabase();
        questionsDO = new QuestionsDO(databaseHelper);
        studentHomePage = new StudentHomePage(databaseHelper);
    }

    @Test
    void testReadUnresolvedQuestions() throws SQLException {
        
        
         List<Question> unresolvedQuestions = questionsDO.readUnresolvedQuestions();
                
        for (Question question : unresolvedQuestions) {
            assertFalse(question.isResolved(), "All questions should be unresolved");
        }
        
        
        for (int i = 0; i < unresolvedQuestions.size() - 1; i++) {
            assertTrue(unresolvedQuestions.get(i).getTimestamp().compareTo(
                      unresolvedQuestions.get(i+1).getTimestamp()) >= 0,
                      "Questions should be sorted by timestamp in descending order");
        }
    }
    
    
    @Test
    void testHandleAllQuestions() throws SQLException {
        ArrayList<String> roles = new ArrayList<>();
        roles.add("student");
        User testUser = new User("Test User", "test@example.com", "testUser", "password", roles);
        
        
        Platform.runLater(() -> {
            Stage mockStage = new Stage();
            assertNotNull(testUser);
            assertNotNull(mockStage);
        });
    }
    @Test
    void testReadUnresolvedQuestionsWithNoUnresolved() throws SQLException {
       
        List<Question> unresolvedQuestions = questionsDO.readUnresolvedQuestions();
        
        
        
        assertTrue(unresolvedQuestions.isEmpty() || 
                 unresolvedQuestions.stream().noneMatch(Question::isResolved),
                 "Either no questions should exist or none should be resolved");
    }
    @Test
    void testGetFilteredQuestionsReturnsOnlyUnresolved() throws SQLException {
    	List<Question> filtered = studentHomePage.getFilteredQuestions("unresolved");


        assertTrue(filtered.stream().allMatch(q -> !q.isResolved()), "All questions should be unresolved");
    }


}