package model;

import java.io.Serializable;
import javax.persistence.*;

/**
 * Representa la entidad de relación entre un Pedido y un Libro.
 * Esta clase gestiona las líneas de detalle de cada pedido, incluyendo la cantidad
 * comprada y la vinculación con los objetos correspondientes.
 * * @author ander
 * @version 1.0
 */
@Entity
@Table(name = "contain_line") // "contain" a veces da problemas, mejor nombre explícito
public class Contain implements Serializable {

    // 1. Clave Compuesta incrustada
    /**
     * Clave primaria compuesta que identifica la línea de detalle.
     */
    @EmbeddedId
    private ContainId id;

    // 2. Atributos propios
    /**
     * Cantidad de ejemplares de un libro específico en el pedido.
     */
    @Column(name="quantity")
    private int quantity;

    /**
     * Atributo transitorio para cálculos de suma total (no persistido en BD).
     */
    @Transient
    private float sum;

    // 3 Relaciones con @MapsId
    /**
     * Relación con el pedido al que pertenece esta línea.
     */
    @ManyToOne
    @MapsId("idOrder") // <-- "Usa el campo 'idOrder' de ContainId para mi FK"
    @JoinColumn(name = "id_order")
    private Order order;

    /**
     * Relación con el libro incluido en esta línea de pedido.
     */
    @ManyToOne
    @MapsId("isbnBook") // <-- "Usa el campo 'isbnBook' de ContainId para mi FK"
    @JoinColumn(name = "isbn_book")
    private Book book;
    
    // --- CONSTRUCTOR VACÍO OBLIGATORIO PARA HIBERNATE ---
    /**
     * Constructor vacío requerido por el framework de persistencia.
     */
    public Contain() {
    }

    /**
     * Constructor parametrizado para crear una nueva línea de pedido.
     * @param quantity Cantidad de libros.
     * @param order Objeto pedido asociado.
     * @param book Objeto libro asociado.
     */
    public Contain(int quantity, Order order, Book book) {
        this.quantity = quantity;
        this.order = order;
        this.book = book;

        // Creamos el ID automáticamente usando los datos de los objetos
        this.id = new ContainId(order.getIdOrder(),book.getISBN());
    }

    /**
     * Obtiene el ID compuesto.
     * @return Objeto {@link ContainId}.
     */
    public ContainId getId() {
        return id;
    }

    /**
     * Establece el ID compuesto.
     * @param id Nuevo ID compuesto.
     */
    public void setId(ContainId id) {
        this.id = id;
    }

    /**
     * Obtiene el pedido asociado.
     * @return Objeto {@link Order}.
     */
    public Order getOrder() {
        return order;
    }

    /**
     * Establece el pedido y sincroniza la clave compuesta.
     * @param order Nuevo pedido asociado.
     */
    public void setOrder(Order order) {
        this.order = order;
        // Si cambias el objeto, actualiza también el ID
        if (id != null && order != null) {
            id.setIdOrder(order.getIdOrder());
        }
    }

    /**
     * Obtiene el libro asociado.
     * @return Objeto {@link Book}.
     */
    public Book getBook() {
        return book;
    }

    /**
     * Establece el libro y sincroniza la clave compuesta.
     * @param book Nuevo libro asociado.
     */
    public void setBook(Book book) {
        this.book = book;
        if (id != null && book != null) {
            id.setIsbnBook(book.getISBN());
        }
    }

    /**
     * Establece la cantidad.
     * @param quantity Cantidad de libros.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Establece la suma total.
     * @param sum Valor de la suma.
     */
    public void setSum(float sum) {
        this.sum = sum;
    }

    /**
     * Obtiene la cantidad.
     * @return Cantidad de libros.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Obtiene la suma total calculada.
     * @return Valor de la suma.
     */
    public float getSum() {
        return sum;
    }
    
    // ==========================================
    // MÉTODOS PARA LA VISTA (TABLA)
    // ==========================================

    /**
     * Puente para sacar el Título. La tabla llama aquí cuando pones: new
     * PropertyValueFactory("nombreLibro")
     * @return Título del libro asociado.
     */
    public String getNombreLibro() {
        // Gracias al JOIN FETCH, 'this.book' ya no es null, tiene datos.
        return this.book.getTitle();
    }

    /**
     * Puente para sacar el Precio Total. La tabla llama aquí cuando pones: new
     * PropertyValueFactory("totalEuros")
     * @return Precio total formateado con símbolo de Euro.
     */
    public String getTotalEuros() {
        // Multiplicamos el precio del libro (Book) por la cantidad (Contain)
        float total = this.book.getPrice() * this.quantity;
        return total + " €";
    }
}