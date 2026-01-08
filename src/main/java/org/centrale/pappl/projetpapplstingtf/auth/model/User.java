/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.model;

/**
 * Domain model representing a user in the system.
 * <p>
 * This class encapsulates the essential user information required for 
 * authentication and identification, including the login credentials 
 * and contact details.
 * </p>
 * @author srodr
 */
public class User {
    
    /**
     * The user's email address.
     */
    String mail;

    /**
     * The unique username or login identifier used for authentication.
     */
    String login;

    /**
     * The securely hashed password.
     * <p>
     * <b>Security Note:</b> This field stores the hash of the password, not the 
     * plain text password, ensuring security best practices.
     * </p>
     */
    String hashPassword; 

    /**
     * Constructs a new User with the specified details.
     * * @param mail The email address of the user.
     * @param login The username/login of the user.
     * @param hashPassword The password (already hashed) associated with the user.
     */
    public User(String mail, String login, String hashPassword) {
        this.mail = mail;
        this.login = login;
        this.hashPassword = hashPassword;
    }

    /**
     * Retrieves the user's email address.
     * @return The email string.
     */
    public String getMail() {
        return mail;
    }

    /**
     * Retrieves the user's login identifier.
     * @return The login string.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Retrieves the hashed password.
     * @return The string containing the password hash.
     */
    public String getHashPassword() {
        return hashPassword;
    }

    /**
     * Updates the user's email address.
     * @param mail The new email address to set.
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Updates the user's login identifier.
     * @param login The new username to set.
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Updates the user's password hash.
     * @param hashPassword The new hashed password string.
     */
    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }
    
    
    
}