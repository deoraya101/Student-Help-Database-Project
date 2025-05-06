//Created by Rhea Soni
package application;

/**
 * The {@code Answer} class is a model representing an answer to a question in the system.
 * 
 * <p>It includes fields such as:
 * <ul>
 *   <li>Answer ID</li>
 *   <li>Associated question ID</li>
 *   <li>Answer content</li>
 *   <li>Author information</li>
 *   <li>Boolean indicating if it's a solution</li>
 * </ul>
 */

public class Answer {
    private int id;
    private int questionId;
    private String content;
    private String authorUserName;
    private String authorName;
    private boolean isSolution;

    public Answer(int id, int questionId, String content, String authorUserName, String authorName, boolean isSolution) {
        this.id = id;
        this.questionId = questionId;
        this.content = content;
        this.authorUserName = authorUserName; // Ensure this is correctly assigned
        this.authorName = authorName;
        this.isSolution = isSolution;
    }
    
    /**
 * Gets the answer's ID.
 * @return the answer ID
 */
public int getId() { return id; }
    /**
 * Sets the answer ID.
 * @param id the answer ID to set
 */
public void setAnswerId(int id) {this.id = id;} //Taylor edit 4/22
    /**
 * Gets the ID of the question associated with this answer.
 * @return the question ID
 */
public int getQuestionId() { return questionId; }
    /**
 * Gets the content of the answer.
 * @return the answer text
 */
public String getContent() { return content; }
    /**
 * Gets the username of the answer's author.
 * @return author username
 */
public String getAuthor() { return authorUserName; }
    /**
 * Gets the display name of the answer's author.
 * @return author name
 */
public String getAuthorName() { return authorName; }
    /**
 * Checks if this answer is marked as the solution.
 * @return true if solution
 */
public boolean isSolution() { return isSolution; }

    /**
 * Sets the content of the answer.
 * @param content the text to set
 */
public void setContent(String content) { this.content = content; }
    /**
 * Marks whether the answer is a solution.
 * @param solution true to mark as solution
 */
public void setSolution(boolean solution) { isSolution = solution; }
   
}