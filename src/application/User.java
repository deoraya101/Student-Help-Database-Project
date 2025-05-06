package application;

import java.util.ArrayList;

/**
 * The User class represents a user entity in the system.
 * It contains the user's details such as userName, password, and role.
 */
/**
 * The {@code User} class models a system user including credentials and assigned roles.
 * It provides accessors and mutators for user-related fields.
 */

public class User {
    private String name;
    private String email;
    private String userName;
    private String password;
    private ArrayList<String> roles;

    // Constructor to initialize a new User object with userName, password, and role.
    public User(String name, String email, String userName, String password, ArrayList<String> roles) {
        this.name = name;
        this.email = email;
    	this.userName = userName;
        this.password = password;
        this.roles = roles;
    }
    
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getUserName() { return userName; }
    public String getPassword() { return password; }
    public ArrayList<String> getRoles() { return roles; }

    

//Sets the role of the user.
	public void setRoles(ArrayList<String> roles) {
		this.roles = roles;
	}
	
	public void addRole(String role) {
		this.roles.add(role);
    }

}
