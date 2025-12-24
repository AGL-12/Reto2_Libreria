package model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Commentate {

    private int idUser;
    private int idBook;
    private String commentary;
    private Timestamp dateCreation;
    private float valuation;
    private String tempUsername;
    
    public Commentate() {
    }

    public Commentate(int idUser, int idBook, String commentary, Timestamp dateCreation, float valuation, String tempUsername) {
        this.idUser = idUser;
        this.idBook = idBook;
        this.commentary = commentary;
        this.dateCreation = dateCreation;
        this.valuation = valuation;
        this.tempUsername = tempUsername;
    }
    
    
    

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdBook() {
        return idBook;
    }

    public void setIdBook(int idBook) {
        this.idBook = idBook;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    public float getValuation() {
        return valuation;
    }

    public void setValuation(float valuation) {
        this.valuation = valuation;
    }

    public String getTempUsername() {
        return tempUsername;
    }

    public void setTempUsername(String tempUsername) {
        this.tempUsername = tempUsername;
    }
    
    
    public String getFormattedDate() {
        if (dateCreation != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(dateCreation);
        }
        return "";
    }
    
    

}
