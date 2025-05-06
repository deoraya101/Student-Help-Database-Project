//Created by Rhea Soni
package application;

import java.sql.Timestamp;

/**
 * The {@code Question} class represents a user-submitted question in the system.
 * It stores details such as title, description, author, timestamp, resolution status,
 * and if it's a follow-up question.
 */

/**
 * The {@code Question} class represents a question submitted by a user.
 * It includes metadata such as title, description, author, and state.
 */
public class Question {
    private int id;
    private String title;
    private String description;
    private String author;
    private String authorName; // Taylor Edit 2/19
    private Timestamp timestamp; // Taylor Edit 2/19
    private boolean resolved; //Rhea TP2 2/20
    private boolean followUp; // added by Janelle (2/24)
    private int prevQuestionID; // added by Janelle (2/24)
   

 // added by Janelle (2/26) Temporary/Not sure if necessary
    public Question() {
    	id = 0;
    	title = "";
    	description = "";
    	author = "";
    	authorName = "";
    	followUp = false;
    	prevQuestionID = -1;
    	
    }
    
    public Question(int id, String title, String description, String author, String authorName, Timestamp timestamp, boolean isResolved) { // Taylor Edit 2/19 //Rhea TP2 2/20
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.authorName = authorName; // Taylor Edit 2/19
        this.timestamp = timestamp; // Taylor Edit 2/19
        this.resolved = isResolved; //Rhea TP2 2/20
        
     // New parameters - Janelle
     // followUp is an extra variable used to determine if the question is following a previously made question
        this.followUp = false;
        this.prevQuestionID = -1;
    }
    
 // Secondary constructor with prevQuestionID parameter - Janelle
    public Question(int id, String title, String description, String author, String authorName, int prevQuestionID) { // Taylor Edit 2/19
        this.id = id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.authorName = authorName; // Taylor Edit 2/19
        
        // New parameters
        // followUp is an extra variable used to determine if the question is following a previously made question
        this.followUp = true;
        this.prevQuestionID = prevQuestionID;
      
    }

    /**
 * Returns the question ID.
 * @return question ID
 */
public int getId() { return id; }
    public void setQuestionId(int id) {this.id = id; } // Taylor Edit 4/22
    /**
 * Returns the question title.
 * @return question title
 */
public String getTitle() { return title; }
    /**
 * Returns the question description.
 * @return question description
 */
public String getDescription() { return description; }
    public String getAuthor() { return author; }
    public String getAuthorName() { return authorName; } // Taylor Edit 2/19
    /**
 * Returns the timestamp when the question was created.
 * @return creation timestamp
 */
public Timestamp getTimestamp() { return timestamp; } // Taylor Edit 2/19
    public boolean isResolved() { return resolved; } //Rhea TP2 2/20
    
 // New functions - Janelle
    public boolean isFollowUp() { return followUp; }
    public int getPrevQuestionID( ) { return prevQuestionID; }
  

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setResolved(boolean resolved) { this.resolved = resolved; } //Rhea TP2 2/20
    
 // New function to add another question ID for a follow-up question to reference (added by Janelle 2/26)
    public void setPrevQuestionID(int prevQuestionID) {
    	this.prevQuestionID = prevQuestionID;
    	if(prevQuestionID == -1) {
        	this.followUp = false;
        } else {
        	this.followUp = true;
        }
    }
   
    
    
}