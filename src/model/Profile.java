/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 * Abstract class representing a general profile in the system.
 * Contains common attributes such as username, password, email, and personal information.
 * All profile types (User, Admin) extend this class.
 * 
 * @author acer
 */
public abstract class Profile {
    private String username;
    private String password;
    private String email;
    private int userCode;
    private String name;
    private String telephone;
    private String surname;

    /**
     * Constructs a profile with the specified attributes.
     */
    public Profile(String username, String password, String email, int userCode, String name, String telephone, String surname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.userCode = userCode;
        this.name = name;
        this.telephone = telephone;
        this.surname = surname;
    }

    /**
     * Default constructor initializing attributes with default values.
     */
    public Profile() {
        this.username = "";
        this.password = "";
        this.email = "";
        this.userCode = 0;
        this.name = "";
        this.telephone = "";
        this.surname = "";
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public int getUserCode() { return userCode; }
    public String getName() { return name; }
    public String getTelephone() { return telephone; }
    public String getSurname() { return surname; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setUserCode(int userCode) { this.userCode = userCode; }
    public void setName(String name) { this.name = name; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setSurname(String surname) { this.surname = surname; }

    @Override
    public String toString() {
        return "Profile{" + "username=" + username + ", password=" + password + ", email=" + email +
                ", userCode=" + userCode + ", name=" + name + ", telephone=" + telephone + ", surname=" + surname + '}';
    }

    /**
     * Performs login logic for the profile.
     * Must be implemented by subclasses.
     */
    public abstract void logIn();
}
