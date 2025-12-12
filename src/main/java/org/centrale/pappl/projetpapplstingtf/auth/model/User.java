/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.centrale.pappl.projetpapplstingtf.auth.model;

/**
 *
 * @author srodr
 */
public class User {
    
    String mail;
    String login;
    String hashPassword; 

    public User(String mail, String login, String hashPassword) {
        this.mail = mail;
        this.login = login;
        this.hashPassword = hashPassword;
    }

    public String getMail() {
        return mail;
    }

    public String getLogin() {
        return login;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }
    
    
    
}
