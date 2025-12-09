/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 * Represents an admin profile in the system.
 * Extends Profile and adds the currentAccount attribute.
 */
public class Admin extends Profile {
    private String currentAccount;

    public Admin(String currentAccount, String username, String password, String email, int userCode, String name, String telephone, String surname) {
        super(username, password, email, userCode, name, telephone, surname);
        this.currentAccount = currentAccount;
    }

    public Admin() {
        this.currentAccount = "";
    }

    public String getCurrentAccount() { return currentAccount; }
    public void setCurrentAccount(String currentAccount) { this.currentAccount = currentAccount; }

    @Override
    public void logIn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return "Admin{" + "currentAccount=" + currentAccount + '}';
    }
}
