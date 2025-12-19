package model;

import java.util.List;

public class Book {

    private int ISBN;
    private String cover;
    private String titulo;
    private Author author;
    private int sheets;
    private int stock;
    private String sypnosis;
    private float price;
    private String editorial;
    private float avgValuation; //calculado

    public Book(int ISBN, String cover, String titulo, Author author, int sheets, int stock, String sypnosis, float price, String editorial, float avgValuation) {
        this.ISBN = ISBN;
        this.cover = cover;
        this.titulo = titulo;
        this.author = author;
        this.sheets = sheets;
        this.stock = stock;
        this.sypnosis = sypnosis;
        this.price = price;
        this.editorial = editorial;
        this.avgValuation = avgValuation;
    }

    public Book() {
    }

    
    public int getISBN() {
        return ISBN;
    }

    public void setISBN(int ISBN) {
        this.ISBN = ISBN;
    }

   public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public int getSheets() {
        return sheets;
    }

    public void setSheets(int sheets) {
        this.sheets = sheets;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getSypnosis() {
        return sypnosis;
    }

    public void setSypnosis(String sypnosis) {
        this.sypnosis = sypnosis;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getEditorial() {
        return editorial;
    }

    public void setEditorial(String editorial) {
        this.editorial = editorial;
    }

    public float getAvgValuation() {
        return avgValuation;
    }

    public void setAvgValuation(float avgValuation) {
        this.avgValuation = avgValuation;
    }
    
}
