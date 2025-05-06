package testing;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import databasePart1.DatabaseHelper;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import application.Flag;
import application.FlagDO;
import application.Answer;
import application.AnswerDO;
import application.Question;
import application.QuestionsDO;
import application.FlaggedContent;

import java.sql.Timestamp;
import java.time.Instant;

class TaylorTest {
	private static DatabaseHelper databaseHelper;
	private static FlagDO flagDO;
	private static AnswerDO answerDO;
	private static QuestionsDO questionDO;
	
	@BeforeAll
	static void setupDatabase() throws SQLException {
		databaseHelper = new DatabaseHelper(); 
	    databaseHelper.connectToDatabase();
	    flagDO = new FlagDO(databaseHelper);
	    answerDO = new AnswerDO(databaseHelper);
	    questionDO = new QuestionsDO(databaseHelper);
	}
	
	@BeforeEach
	void clearDatabase() throws SQLException {
	    try (Statement stmt = databaseHelper.getConnection().createStatement()) {
	        stmt.execute("DELETE FROM Flags");
	        stmt.execute("DELETE FROM Answers");
	        stmt.execute("DELETE FROM Questions");
	    }
	}
	
	@AfterEach
	void cleanupDatabase() throws SQLException {
	    try (Statement stmt = databaseHelper.getConnection().createStatement()) {
	        stmt.execute("DELETE FROM Flags");
	        stmt.execute("DELETE FROM Answers");
	        stmt.execute("DELETE FROM Questions");
	    }
	}

	@Test
	void testCreateYellowFlag() throws SQLException {
		// create a test flag and insert into database
		Timestamp timestamp = Timestamp.from(Instant.now());
		Flag flag = new Flag(0, "question", 1, "Yellow", "this might be inappropriate", "sammi", "Sammi", timestamp);
		flagDO.createFlag(flag);
		// read the flags for target id 1 and store in a List
		List<Flag> flags = flagDO.readFlags(1, "question");
		
		// check for correct number of flags and content
		assertEquals(1, flags.size());
		assertEquals("Yellow", flags.get(0).getFlagColor());
		assertEquals("this might be inappropriate", flags.get(0).getFlagMessage());
	}
	
	@Test
	void testCreateRedFlag() throws SQLException {
		// create a test flag and insert into database
		Timestamp timestamp = Timestamp.from(Instant.now());
		Flag flag = new Flag(0, "answer", 1, "Red", "this needs to be removed", "sammi", "Sammi", timestamp);
		flagDO.createFlag(flag);
		// read the flags for target id 1 and store in a List
		List<Flag> flags = flagDO.readFlags(1, "answer");
		
		// check for correct number of flags and content
		assertEquals(1, flags.size());
		assertEquals("Red", flags.get(0).getFlagColor());
		assertEquals("this needs to be removed", flags.get(0).getFlagMessage());
	}
	
	@Test
	void testUpdateFlag() throws SQLException {
		// create a test flag
		Timestamp timestamp = Timestamp.from(Instant.now());
		Flag testFlag = new Flag(0, "question", 10, "Yellow", "this is inappropriate", "sammi", "Sammi", timestamp);
		flagDO.createFlag(testFlag);
		
		// update the flag
		testFlag.setFlagMessage("this is bad");
		flagDO.updateFlagMessage(testFlag);
		
		// read back the flags
		List<Flag> flags = flagDO.readFlags(10, "question");
		
		// test for correct flag message
		assertEquals("this is bad", flags.get(0).getFlagMessage());
		assertNotEquals("this is inappropriate", flags.get(0).getFlagMessage());
	}
	
	@Test
	void testDeleteFlag() throws SQLException {
		// create a test flag
		Timestamp timestamp = Timestamp.from(Instant.now());
		Flag testFlag = new Flag(0, "question", 4, "Yellow", "this is inappropriate", "sammi", "Sammi", timestamp);
		flagDO.createFlag(testFlag);
		
		// delete the flag
		flagDO.deleteFlag(testFlag.getFlagId());
		
		// read back the flags
		List<Flag> flags = flagDO.readFlags(4, "question");
		
		// make sure the flag is gone
		assertTrue(flags.isEmpty());
	}
	
	@Test
	void testReadFlaggedAnswers() throws SQLException {
		Timestamp timestamp = Timestamp.from(Instant.now());
		
		// create a test question
		Question q1 = new Question(0, "Question1", "This is my q1", "emily", "Emily", timestamp, false);
		questionDO.createQuestion(q1);
		
		// create test answers and insert into database
		Answer answer1 = new Answer(1, q1.getId(), "this is answer1", "mary", "Mary", false);
		answerDO.createAnswer(answer1);
		Answer answer2 = new Answer(2, q1.getId(), "this is answer2", "ron", "Ron", true);
		answerDO.createAnswer(answer2);
		Answer answer3 = new Answer(3, q1.getId(), "this is answer3", "ron", "Ron", false);
		answerDO.createAnswer(answer3);
		
		// create flags on the answers
		Flag flag1 = new Flag(0, "answer", answer1.getId(), "Red", "this needs to be removed", "sammi", "Sammi", timestamp);
		flagDO.createFlag(flag1);
		Flag flag2 = new Flag(0, "answer", answer2.getId(), "Yellow", "this is bad", "tony", "Tony", timestamp);
		flagDO.createFlag(flag2);
		Flag flag3 = new Flag(0, "answer", answer3.getId(), "Red", "inappropriate", "sammi", "Sammi", timestamp);
		flagDO.createFlag(flag3);		
		
		// read flagged answers
		List<FlaggedContent> flaggedAnswers = flagDO.getFlaggedAnswers();
		
		boolean foundA1 = false;
		for (FlaggedContent f : flaggedAnswers) {
			if(f.getContent().equals("this is answer1")) {
				foundA1 = true;
			}
		}
		boolean foundA2 = false;
		for (FlaggedContent f : flaggedAnswers) {
			if(f.getContent().equals("this is answer2")) {
				foundA2 = true;
			}
		}
		boolean foundA3 = false;
		for (FlaggedContent f : flaggedAnswers) {
			if(f.getContent().equals("this is answer3")) {
				foundA3 = true;
			}
		}

		assertTrue(foundA1);
		assertTrue(foundA2);
		assertTrue(foundA3);
	}
	
	@Test
	void testDeleteQuestion() throws SQLException{
		Timestamp timestamp = Timestamp.from(Instant.now());
		
		// create a test question
		Question testQ = new Question(1, "Question1", "This is my q1", "emily", "Emily", timestamp, false);
		questionDO.createQuestion(testQ);
		
		// read the questions and ensure that the question exists in the database
		List<Question> q = questionDO.readQuestions();
		System.out.println(q.size());
		assertEquals(1, q.size());
		
		// delete the question from the database
		questionDO.deleteQuestion(testQ.getId());
		
		// read back the questions after delete
		List<Question> qs = questionDO.readQuestions();
		
		// make sure the question is gone
		assertEquals(0, qs.size());
	}
	
	@Test
	void testDeleteAnswer() throws SQLException{
		Timestamp timestamp = Timestamp.from(Instant.now());
		
		// create a test answer
		Question q1 = new Question(0, "Question1", "This is my q1", "emily", "Emily", timestamp, false);
		questionDO.createQuestion(q1);
		
		// create test answers and insert into database
		Answer answer1 = new Answer(0, q1.getId(), "this is answer1", "mary", "Mary", false);
		answerDO.createAnswer(answer1);
		
		answerDO.deleteAnswer(answer1.getId());
		
		// read back the answers
		List<Answer> as = answerDO.readAnswers(answer1.getId());
		
		// make sure the answer is gone
		assertTrue(as.isEmpty());
	}

}