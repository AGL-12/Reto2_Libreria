package model;

public class Author {

    private int idAuthor;
    private String nombre;
    private String apellido;

    public Author(int idAuthor, String nombre, String apellido) {
        this.idAuthor = idAuthor;
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public Author(){
        
    }

    public int getIdAuthor() {
        return idAuthor;
    }

    public void setIdAuthor(int idAuthor) {
        this.idAuthor = idAuthor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

}
