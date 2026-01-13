package model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name = "book")
public class Book implements Serializable {

    @Id
    @Column(name = "isbn")
    private long ISBN; // Asumo que el ISBN lo pones tú manualmente, si no, añade @GeneratedValue
    private String cover;
    private String title;

    @ManyToOne
    @JoinColumn(name = "id_author")
    private Author author;
    private int sheets;
    private int stock;
    @Column(length = 1000) // Para textos largos
    private String sypnosis;
    private float price;
    private String editorial;
    // Lista inversa: Un libro tiene muchos comentarios.
    // fetch = FetchType.LAZY es el defecto (se cargan solo cuando los pides)
    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    private List<Commentate> comments;
    // CAMPO CALCULADO: No se guarda en BD
    @Transient
    private float avgValuation;

    public Book(long ISBN, String cover, String titulo, Author author, int sheets, int stock, String sypnosis, float price, String editorial, float avgValuation) {
        this.ISBN = ISBN;
        this.cover = cover;
        this.title = titulo;
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

    public long getISBN() {
        return ISBN;
    }

    public void setISBN(long ISBN) {
        this.ISBN = ISBN;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<Commentate> getComments() {
        return comments;
    }

    public void setComments(List<Commentate> comments) {
        this.comments = comments;
    }

    public float getAvgValuation() {
        return avgValuation;
    }

    public void setAvgValuation(float avgValuation) {
        this.avgValuation = avgValuation;
    }

}
