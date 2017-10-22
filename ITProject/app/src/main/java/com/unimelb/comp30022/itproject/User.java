package com.unimelb.comp30022.itproject;

/**
 * Created by RoryPowell.
 * Class to be stored in the database as a user object.
 * It holds information of each user entered into the database.
 */

public class User {


    private String username;
    private String firstname;
    private String lastname;
    private String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userName, String firstName, String lastName, String Email) {

        // each value is a line stored in the database, they are assigned by the constructor
        this.username = userName;
        this.firstname = firstName;
        this.lastname = lastName;
        this.email = Email;
    }

    // getters and setters
    public String getEmail() {
        return this.email;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public void setFirstname(String firstName) {
        this.firstname = firstName;
    }

    public String getLastname() {
        return this.lastname;
    }

    public void setLastname(String lastName) {
        this.lastname = lastName;
    }

    public void setEmail(String Email) {
        this.email = Email;
    }
}