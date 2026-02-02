/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.List;
import javax.persistence.*;

/**
 * Represents a standard user in the system. Extends Profile and adds gender and
 * card number attributes.
 */
@Entity
@Table(name = "user")
@PrimaryKeyJoinColumn(name = "user_code") // La FK que une con Profile es también la PK de User
public class User extends Profile {
    @Column(name="gender")
    private String gender;

    @Column(name = "card_number",columnDefinition = "CHAR(16)")
    private String cardNumber;

    // --- CAMBIO 1: CascadeType.ALL en Pedidos ---
    // Si borras el usuario, se borran sus pedidos (y las líneas de esos pedidos gracias a Order.java)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> purchaseList;

    // --- CAMBIO 2: CascadeType.ALL en Comentarios ---
    // Si borras el usuario, se borran sus comentarios automáticamente.
    // Esto soluciona tu error de "Foreign Key Constraint".
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Commentate> comments;

    public User(String gender, String cardNumber, String username, String password, String email, int userCode, String name, String telephone, String surname, List<Order> purchaseList) {
        super(username, password, email, userCode, name, telephone, surname);
        this.gender = gender;
        this.cardNumber = cardNumber;
        this.purchaseList = purchaseList;
    }

    public User() {
        super();
        this.gender = "";
        this.cardNumber = "";
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public List<Order> getPurchaseList() {
        return purchaseList;
    }

    public void setPurchaseList(List<Order> purchaseList) {
        this.purchaseList = purchaseList;
    }

    public List<Commentate> getComments() {
        return comments;
    }

    public void setComments(List<Commentate> comments) {
        this.comments = comments;
    }

    @Override
    public void logIn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Método extra para que el ComboBox muestre el nombre y no "model.User@..."
    @Override
    public String toString() {
        return this.getUsername();
    }
}