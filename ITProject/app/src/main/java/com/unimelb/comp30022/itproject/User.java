package com.unimelb.comp30022.itproject;

/**
 * Created by RoryPowell on 14/9/17.
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
        this.username = userName;
        this.firstname = firstName;
        this.lastname = lastName;
        this.email = Email;
    }

    public String getEmail() {
        return this.email;
    }

    public String getUsername() {
        //return "user class test";
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