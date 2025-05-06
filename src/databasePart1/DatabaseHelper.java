package databasePart1;
import java.sql.*;
import java.util.UUID;

import javax.swing.JOptionPane;

import application.AdminRequest;
import application.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
/**
 * The DatabaseHelper class provides methods to manage a connection to a relational
 * H2 database for user-based applications. It supports functionality such as user registration,
 * login, password reset, invitation code handling, user role assignment, and other administrative actions.
 *
 * <p>Core features include:
 * <ul>
 *   <li>Creating and maintaining tables for users, roles, questions, answers, messages, reviews, etc.</li>
 *   <li>Managing one-time passwords (OTPs) for secure user verification</li>
 *   <li>Tracking user roles and permissions via foreign key constraints</li>
 *   <li>Handling trusted reviewer relationships with adjustable weighting</li>
 *   <li>Database integrity checks and schema evolution</li>
 * </ul>
 *
 * <p>This class is designed to be used in educational or prototype applications and uses
 * plain text password storage for simplicity (not recommended in production).
 */

public class DatabaseHelper {
	

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}
		/**
 * Gets the current database connection.
 * @return the active JDBC connection
 */
public Connection getConnection() {
	        return this.connection;
	    }
	

	private void createTables() throws SQLException {
	    String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "name VARCHAR(255), "
	            + "email VARCHAR(255), "
	            + "userName VARCHAR(255) UNIQUE, "
	            + "password VARCHAR(255), "
	            + "role VARCHAR(20), "
	      + "otp VARCHAR(255), "
	      + "otpExpiry TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
	    statement.execute(userTable);

	    
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);
	    
	    //Create the UserRoles table if it doesn't exist already
	    String userRolesTable = "CREATE TABLE IF NOT EXISTS UserRoles ("
	    		  + "userId INT, "                     // ***User's ID, foreign key referencing cse360users
	              + "role VARCHAR(20), "              // ***User's role
	              + "PRIMARY KEY (userId, role), "    // ***Composite primary key (userId and role)
	              + "FOREIGN KEY (userId) REFERENCES cse360users(id))"; // Foreign key constraint
	    statement.execute(userRolesTable);
	    
	    // Create Questions table
	    String questionsTable = "CREATE TABLE IF NOT EXISTS Questions ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "title VARCHAR(255), "
	            + "description TEXT, "
	            + "author VARCHAR(255), "
	            + "authorName VARCHAR(255), "
	            + "timeStamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "isResolved BOOLEAN DEFAULT FALSE, "
        		+ "followUp BOOLEAN DEFAULT FALSE, "
        		+ "prevQuestionID INT) ";
	    statement.execute(questionsTable);

	    // Create Answers table
	    String answersTable = "CREATE TABLE IF NOT EXISTS Answers ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "questionId INT, "
	            + "content TEXT, "
	            + "authorUserName VARCHAR(255), "
	            + "authorName VARCHAR(255), "
	            + "isSolution BOOLEAN DEFAULT FALSE, "
	            + "FOREIGN KEY (questionId) REFERENCES Questions(id))";
	    statement.execute(answersTable);
	    
	    //Create Messages Table - Shreya S.
	    String messagesTable = "CREATE TABLE IF NOT EXISTS Messages ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "senderUserName VARCHAR(255), "
	            + "receiverUserName VARCHAR(255), "
	            + "content TEXT, "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
	            + "isRead BOOLEAN DEFAULT FALSE)";
	    statement.execute(messagesTable);
	    
	    //Taylor added 3/26
	    String reviewsTable = "CREATE TABLE IF NOT EXISTS Reviews ("
    		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
            + "targetId INT, "
    		+ "QorA VARCHAR(255), "
            + "content TEXT, "
            + "originalContent TEXT, "
            + "reviewerUserName VARCHAR(255), "
            + "reviewerName VARCHAR(255), "
            + "lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
            + "unread BOOLEAN DEFAULT TRUE)";
	    statement.execute(reviewsTable);
	    
	    // staff flags table
	    String flagsTable = "CREATE TABLE IF NOT EXISTS Flags ("
	    		+ "id INT AUTO_INCREMENT PRIMARY KEY, "
	    		+ "targetType VARCHAR(255), " // question, answer, or private feedback
	            + "targetId INT NOT NULL, "
	            + "flagColor VARCHAR(10), " // yellow or red
	            + "flagMessage TEXT, "
	            + "staffUserName VARCHAR(255), "
	            + "staffName VARCHAR(255), "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
		    statement.execute(flagsTable);
		    
            //Added by Shreya
		    
		    String sql = "CREATE TABLE IF NOT EXISTS admin_requests ("
		            + "id INT AUTO_INCREMENT PRIMARY KEY, "
		            + "requester_id INT NOT NULL, "
		            + "description TEXT NOT NULL, "
		            + "request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
		            + "is_closed BOOLEAN DEFAULT FALSE, "
		            + "admin_response TEXT, "
		            + "resolution_date TIMESTAMP, "
		            + "FOREIGN KEY (requester_id) REFERENCES cse360users(id))";
		        statement.execute(sql);
	    
	    // Check if the unread column exists in the Reviews table and add it if it doesn't
	    checkAndAddUnreadColumn();
	    // Check if the lastUpdated column exists in the Reviews table and add it if it doesn't
	    checkAndAddLastUpdatedColumn();
	    
	    String reviewerRequestsTable = "CREATE TABLE IF NOT EXISTS reviewer_requests ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "studentId INT NOT NULL, "
	            + "studentName VARCHAR(255) NOT NULL, "
	            + "username VARCHAR(255) NOT NULL, "
	            + "status ENUM('Pending', 'Approved', 'Denied') DEFAULT 'Pending')";
	    statement.execute(reviewerRequestsTable);
	    
	    String trustedReviewersTable = "CREATE TABLE IF NOT EXISTS TrustedReviewers (" 
	    	    + "studentUserName VARCHAR(255), " 
	    	    + "reviewerUserName VARCHAR(255), " 
	    	    + "weight INT, " 
	    	    + "PRIMARY KEY (studentUserName, reviewerUserName), " 
	    	    + "FOREIGN KEY (studentUserName) REFERENCES cse360users(userName), " 
	    	    + "FOREIGN KEY (reviewerUserName) REFERENCES cse360users(userName))";
	    statement.execute(trustedReviewersTable);
	    
	    String ratingsTable = "CREATE TABLE IF NOT EXISTS Ratings ("
	    		+ "voterUsername VARCHAR(255),"
	    		+ "reviewerUsername VARCHAR(255),"
	    		+ "vote INT CHECK (vote IN (-2, -1, 0, 1, 2)),"
	    		+ "PRIMARY KEY (voterUsername, reviewerUsername),"
	    		+ "FOREIGN KEY (voterUsername) REFERENCES cse360users(userName),"
	    		+ "FOREIGN KEY (reviewerUsername) REFERENCES cse360users(userName),"
	    		+ "neutral BOOLEAN DEFAULT TRUE,"
	    		+ "upvoted BOOLEAN DEFAULT FALSE,"
	    		+ "downvoted BOOLEAN DEFAULT FALSE)";
	    statement.execute(ratingsTable);
	    
	    //Raya
	    String reviewerBiosTable = "CREATE TABLE IF NOT EXISTS ReviewerBios ("
	            + "reviewerUserName VARCHAR(255) PRIMARY KEY, "
	            + "bio TEXT)";
	    statement.execute(reviewerBiosTable);

	}	  
	
	//Shreya
	
		public void saveAdminRequest(AdminRequest request) throws SQLException {
		    String query = "INSERT INTO admin_requests (requester_id, description) "
		        + "VALUES ((SELECT id FROM cse360users WHERE userName = ?), ?)";
		    try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
		        pstmt.setString(1, request.getRequester().getUserName());
		        pstmt.setString(2, request.getDescription());
		        pstmt.executeUpdate();
		        
		        try (ResultSet rs = pstmt.getGeneratedKeys()) {
		            if (rs.next()) {
		                request.setId(rs.getInt(1));
		            }
		        }
		    }
		}
		
		//Shreya
		
		public List<AdminRequest> getAdminRequestsByUser(String userName) throws SQLException {
		    List<AdminRequest> requests = new ArrayList<>();
		    String query = "SELECT ar.*, u.userName, u.name "
		        + "FROM admin_requests ar "
		        + "JOIN cse360users u ON ar.requester_id = u.id "
		        + "WHERE u.userName = ? "
		        + "ORDER BY ar.request_date DESC";
		    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
		        pstmt.setString(1, userName);
		        ResultSet rs = pstmt.executeQuery();
		        while (rs.next()) {
		            User requester = new User(
		                rs.getString("name"),
		                "", // email not needed
		                rs.getString("userName"),
		                "", // password not needed
		                new ArrayList<>()
		            );
		            
		            AdminRequest request = new AdminRequest(requester, rs.getString("description"));
		            request.setId(rs.getInt("id"));
		            request.setClosed(rs.getBoolean("is_closed"));
		            request.setAdminResponse(rs.getString("admin_response"));
		            request.setResolutionDate(rs.getTimestamp("resolution_date"));
		            
		            requests.add(request);
		        }
		    }
		    return requests;
		}
		
		//Shreya
		
		public List<AdminRequest> getAdminRequestsByStatus(boolean isClosed) throws SQLException {
		    List<AdminRequest> requests = new ArrayList<>();
		    String query = "SELECT ar.*, u.userName, u.name "
		        + "FROM admin_requests ar "
		        + "JOIN cse360users u ON ar.requester_id = u.id "
		        + "WHERE ar.is_closed = ? "
		        + "ORDER BY ar.request_date DESC";
		    
		    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
		        pstmt.setBoolean(1, isClosed);
		        ResultSet rs = pstmt.executeQuery();
		        while (rs.next()) {
		            User requester = new User(
		                rs.getString("name"),
		                "", // email not needed
		                rs.getString("userName"),
		                "", // password not needed
		                new ArrayList<>()
		            );
		            
		            AdminRequest request = new AdminRequest(requester, rs.getString("description"));
		            request.setId(rs.getInt("id"));
		            request.setClosed(rs.getBoolean("is_closed"));
		            request.setAdminResponse(rs.getString("admin_response"));
		            request.setResolutionDate(rs.getTimestamp("resolution_date"));
		            
		            requests.add(request);
		        }
		    }
		    return requests;
		}
		
		//Added by Shreya S.
		
		public void updateAdminRequest(AdminRequest request) throws SQLException {
		    String query = "UPDATE admin_requests SET " +
		            "is_closed = ?, " +
		            "admin_response = ?, " +
		            "resolution_date = ? " +
		            "WHERE id = ?";
		    
		    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
		        pstmt.setBoolean(1, request.isClosed());
		        pstmt.setString(2, request.getAdminResponse());
		        
		        // Handle null resolution date
		        if (request.getResolutionDate() != null) {
		            pstmt.setTimestamp(3, new Timestamp(request.getResolutionDate().getTime()));
		        } else {
		            pstmt.setNull(3, Types.TIMESTAMP);
		        }
		        
		        pstmt.setInt(4, request.getId());
		        pstmt.executeUpdate();
		    }
		}
		
		//Shreya
		
		public void deleteAdminRequest(int requestId) throws SQLException {
		    String query = "DELETE FROM admin_requests WHERE id = ?";
		    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
		        pstmt.setInt(1, requestId);
		        pstmt.executeUpdate();
		    }
		}
		
	
	public void setTrustedReviewer(String studentUserName, String reviewerUserName, int weight) throws SQLException {
	    String query = "MERGE INTO TrustedReviewers (studentUserName, reviewerUserName, weight) KEY(studentUserName, reviewerUserName) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, studentUserName);
	        pstmt.setString(2, reviewerUserName);
	        pstmt.setInt(3, weight);
	        pstmt.executeUpdate();
	    }
	}

	public Map<String, Integer> getTrustedReviewers(String studentUserName) throws SQLException {
	    Map<String, Integer> trusted = new HashMap<>();
	    String query = "SELECT reviewerUserName, weight FROM TrustedReviewers WHERE studentUserName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, studentUserName);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            trusted.put(rs.getString("reviewerUserName"), rs.getInt("weight"));
	        }
	    }
	    return trusted;
	} 
	
	public boolean setOneTimePassword(String userName, String otp) {
        String query = "UPDATE cse360users SET otp = ?, otpExpiry = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            Timestamp expiryTime = new Timestamp(System.currentTimeMillis() + 10 * 60 * 1000); // 10 minutes from now
        	pstmt.setString(1, otp);
            pstmt.setTimestamp(2, expiryTime); 
            pstmt.setString(3, userName);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
	
	public String getOneTimePassword(String userName) {
        String query = "SELECT otp FROM cse360users WHERE username = ? AND otpExpiry > CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("otp");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	public boolean changeAdminPassword(String userName, String oldPassword, String newPassword) {
        String checkPass = "SELECT password FROM cse360users WHERE username = ?";
        String updatePass = "UPDATE cse360users SET password = ? WHERE username = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkPass);
             PreparedStatement updateStmt = connection.prepareStatement(updatePass)) {
            checkStmt.setString(1, userName);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getString("password").equals(oldPassword)) {
                updateStmt.setString(1, newPassword);
                updateStmt.setString(2, userName);
                return updateStmt.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
	
	public boolean verifyOtpAndResetPassword(String username, String otp, String newPassword) {
	    // First, check the OTP
	    String otpQuery = "SELECT otp, otpExpiry FROM cse360users WHERE username = ? AND otp = ? AND otpExpiry > CURRENT_TIMESTAMP()";
	    try (PreparedStatement pstmt = connection.prepareStatement(otpQuery)) {
	        pstmt.setString(1, username);
	        pstmt.setString(2, otp);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	//check expiry time
	        	Timestamp expiry = rs.getTimestamp("otpExpiry");
	        	if (expiry != null && expiry.after(new Timestamp(System.currentTimeMillis()))) {
	        		// OTP is valid, now update the password
		            String updateQuery = "UPDATE cse360users SET password = ?, otp = NULL, otpExpiry = NULL WHERE username = ?";
		            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
		                updateStmt.setString(1, newPassword); // Consider using hashing for storing passwords
		                updateStmt.setString(2, username);
		                int affectedRows = updateStmt.executeUpdate();
		                return affectedRows == 1;  // Return true if the password update was successful
		            }
	            }
	            
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // Return false if the OTP validation fails or password update fails
	}

	
	public ResultSet getAllUsers() throws SQLException {
        String query = "SELECT name, userName, email FROM cse360users";
        return statement.executeQuery(query);
    }
	
	
	//***RS // SQL query to insert a new user role. Uses a subquery to get the userId from the cse360users table.
	public void addUserRole(User user, String role) throws SQLException {
	    String query = "INSERT INTO UserRoles (userId, role) VALUES ((SELECT id FROM cse360users WHERE userName = ?), ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, user.getUserName());  // ***Set the userName parameter in the query
	        pstmt.setString(2, role); // ***Set the role parameter in the query
	        pstmt.executeUpdate(); //***Execute the update to insert the new user role
	        
	       
	    }
	}


	// Check if the database is empty
	/**
 * Checks whether the users table in the database is empty.
 * @return true if no users exist, false otherwise
 */
public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	// Registers a new user in the database.
		public void register(User user) throws SQLException {
			String insertUser = "INSERT INTO cse360users (name, email, userName, password) VALUES (?, ?, ?, ?)";
			try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
				pstmt.setString(1, user.getName());
				pstmt.setString(2, user.getEmail());
				pstmt.setString(3, user.getUserName());
				pstmt.setString(4, user.getPassword());
				pstmt.executeUpdate();
			}
			
			for(String role : user.getRoles()) {
				addUserRole(user, role);
			}
		}


	// Validates a user's login credentials.
	/**
 * Attempts to log in a user using credentials.
 * @param user the user object with credentials
 * @return true if login is successful
 */
public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
	/**
 * Checks if a user exists in the system by username.
 * @param userName the username to check
 * @return true if the user exists
 */
public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the roles of a user from the database using their UserName.
	// Returns the roles in an ArrayList
	public ArrayList<String> getUserRoles(String userName) throws SQLException {
	    String query = "SELECT role FROM UserRoles WHERE userId = (SELECT id FROM cse360users WHERE userName = ?)";
	    ArrayList<String> roles = new ArrayList<>();
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        while(rs.next()) {
	        	roles.add(rs.getString("role"));
	        }
	    }
	    catch (SQLException e) {
	        e.printStackTrace();
	    }
	        return roles;
	}
	
	// Retrieves the name of a user from the database using their UserName.
	/**
 * Retrieves the full name of a user by their username.
 * @param userName the username
 * @return the full name
 */
public String getName(String userName) {
	    String query = "SELECT name FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("name"); // Return the name if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Retrieves the email of a user from the database using their UserName.
		public String getEmail(String userName) {
		    String query = "SELECT email FROM cse360users WHERE userName = ?";
		    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
		        pstmt.setString(1, userName);
		        ResultSet rs = pstmt.executeQuery();
		        
		        if (rs.next()) {
		            return rs.getString("email"); // Return the email if user exists
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		    return null; // If no user exists or an error occurs
		}
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

//Sohan's Code**********************************************************************************
	public void deleteUser(String userName) throws SQLException {
	    if (userName.equals("admin")) {
	        System.out.println("Admin account cannot be deleted.");
	        return;
	    }

	    // Show confirmation dialog
	    int confirmation = JOptionPane.showConfirmDialog(null, 
	            "Are you sure you want to delete the user '" + userName + "'?", 
	            "Confirm Deletion", JOptionPane.YES_NO_OPTION);

	    // Proceed only if "Yes" is selected
	    if (confirmation == JOptionPane.YES_OPTION) {
	        String deleteUserRoles = "DELETE FROM UserRoles WHERE userId = (SELECT id FROM cse360users WHERE userName = ?);";
	        String deleteUser = "DELETE FROM cse360users WHERE userName = ?;";

	        try (PreparedStatement roleStmt = connection.prepareStatement(deleteUserRoles);
	             PreparedStatement userStmt = connection.prepareStatement(deleteUser)) {
	            roleStmt.setString(1, userName);
	            roleStmt.executeUpdate();

	            userStmt.setString(1, userName);
	            userStmt.executeUpdate();

	            System.out.println("User '" + userName + "' has been deleted successfully.");
	        }
	    } else {
	        System.out.println("User deletion canceled.");
	    }
	}
	

	public void deleteUserRole(String userName, String role) throws SQLException {
	    if (role.equalsIgnoreCase("admin")) {
	        // Check if the user is trying to remove their own admin role
	        String checkAdminQuery = "SELECT COUNT(*) FROM UserRoles WHERE userId = (SELECT id FROM cse360users WHERE userName = ?) AND role = 'admin'";
	        try (PreparedStatement checkStmt = connection.prepareStatement(checkAdminQuery)) {
	            checkStmt.setString(1, userName);
	            ResultSet rs = checkStmt.executeQuery();
	            if (rs.next() && rs.getInt(1) == 1) {
	                System.out.println("You cannot remove your own admin role.");
	                return;
	            }
	        }

	        // Ensure there is at least one remaining admin
	        String countAdminQuery = "SELECT COUNT(*) FROM UserRoles WHERE role = 'admin'";
	        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(countAdminQuery)) {
	            if (rs.next() && rs.getInt(1) == 1) {
	                System.out.println("At least one admin must exist.");
	                return;
	            }
	        }
	    }

	    // Proceed with role deletion
	    String deleteRoleQuery = "DELETE FROM UserRoles WHERE userId = (SELECT id FROM cse360users WHERE userName = ?) AND role = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(deleteRoleQuery)) {
	        pstmt.setString(1, userName);
	        pstmt.setString(2, role);
	        int rowsAffected = pstmt.executeUpdate();

	        if (rowsAffected > 0) {
	            System.out.println("Role '" + role + "' removed from user '" + userName + "'.");
	        } else {
	            System.out.println("Role removal failed. The user might not have this role.");
	        }
	    }
	}
//**********************************************************************************

	// Closes the database connection and statement.
	/**
 * Closes the active database connection and statement.
 */
public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}

	// Method to check if the unread column exists in the Reviews table and add it if it doesn't
	private void checkAndAddUnreadColumn() {
	    try {
	        // Check if the unread column exists
	        ResultSet rs = statement.executeQuery("SELECT * FROM Reviews LIMIT 1");
	        ResultSetMetaData metaData = rs.getMetaData();
	        boolean unreadColumnExists = false;
	        
	        for (int i = 1; i <= metaData.getColumnCount(); i++) {
	            if (metaData.getColumnName(i).equalsIgnoreCase("UNREAD")) {
	                unreadColumnExists = true;
	                break;
	            }
	        }
	        
	        // If the unread column doesn't exist, add it
	        if (!unreadColumnExists) {
	            statement.execute("ALTER TABLE Reviews ADD COLUMN unread BOOLEAN DEFAULT TRUE");
	            System.out.println("Added 'unread' column to Reviews table");
	        }
	    } catch (SQLException e) {
	        System.err.println("Error checking/adding unread column: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	// Method to check if the lastUpdated column exists in the Reviews table and add it if it doesn't
	private void checkAndAddLastUpdatedColumn() {
	    try {
	        // Check if the lastUpdated column exists
	        ResultSet rs = statement.executeQuery("SELECT * FROM Reviews LIMIT 1");
	        ResultSetMetaData metaData = rs.getMetaData();
	        boolean lastUpdatedColumnExists = false;
	        
	        for (int i = 1; i <= metaData.getColumnCount(); i++) {
	            if (metaData.getColumnName(i).equalsIgnoreCase("LASTUPDATED")) {
	                lastUpdatedColumnExists = true;
	                break;
	            }
	        }
	        
	        // If the lastUpdated column doesn't exist, add it
	        if (!lastUpdatedColumnExists) {
	            statement.execute("ALTER TABLE Reviews ADD COLUMN lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
	            System.out.println("Added 'lastUpdated' column to Reviews table");
	        }
	    } catch (SQLException e) {
	        System.err.println("Error checking/adding lastUpdated column: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
	
	// added by Sohan 4/20
	public int getReviewCountByReviewer(String reviewerUserName) throws SQLException {
	    String query = "SELECT COUNT(*) FROM Reviews WHERE reviewerUserName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, reviewerUserName);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt(1);
	        }
	    }
	    return 0;
	}

	//Raya TP4
	// Adds or updates a reviewer's bio
    public void saveReviewerBio(String reviewerUserName, String bio) throws SQLException {
        String query = "MERGE INTO ReviewerBios (reviewerUserName, bio) KEY(reviewerUserName) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, reviewerUserName);
            pstmt.setString(2, bio);
            pstmt.executeUpdate();
        }
    }
    
    //Raya TP4
    // Retrieves a reviewer's bio
    public String getReviewerBio(String reviewerUserName) throws SQLException {
        String query = "SELECT bio FROM ReviewerBios WHERE reviewerUserName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, reviewerUserName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("bio");
            }
        }
        return "";
    }
    
    /** Sohan
	 * Deletes all rows from every application table so tests (or
	 * any client code) can start from a clean state.
	 *
	 * @throws SQLException if any SQL error occurs
	 */
	public void clearAllData() throws SQLException {
	    try (Statement stmt = connection.createStatement()) {
	        // Temporarily turn off FK constraints so we can truncate in any order
	        stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
	        
	        // Truncate all application tables
	        stmt.executeUpdate("TRUNCATE TABLE Reviews");
	        stmt.executeUpdate("TRUNCATE TABLE TrustedReviewers");
	        stmt.executeUpdate("TRUNCATE TABLE reviewer_requests");
	        stmt.executeUpdate("TRUNCATE TABLE Flags");
	        stmt.executeUpdate("TRUNCATE TABLE Messages");
	        stmt.executeUpdate("TRUNCATE TABLE Answers");
	        stmt.executeUpdate("TRUNCATE TABLE Questions");
	        stmt.executeUpdate("TRUNCATE TABLE UserRoles");
	        stmt.executeUpdate("TRUNCATE TABLE cse360users");
	        stmt.executeUpdate("TRUNCATE TABLE InvitationCodes");
	        
	        // Re-enable FK constraints
	        stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
	    }
	}

}