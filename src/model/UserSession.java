package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la sesión del usuario actual y su carrito de la compra.
 * Implementa el patrón Singleton para asegurar que solo exista una instancia 
 * de la sesión durante la ejecución de la aplicación.
 */
public class UserSession {

    /** * La única instancia que existirá de la clase (Singleton).
     */
    private static UserSession instance;

    /** * El perfil del usuario que ha iniciado sesión actualmente.
     */
    private Profile user;

    /** * El pedido actual que funciona como carrito de la compra.
     */
    private Order currentOrder;

    /** * Instancia del DAO para persistir los cambios en la base de datos.
     */
    private final ClassDAO dao = new DBImplementation();

    /**
     * Constructor privado para evitar la instanciación externa.
     */
    private UserSession() {
    }

    /**
     * Obtiene la instancia única de UserSession. Si no existe, la crea.
     * @return La instancia única de la sesión.
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Obtiene el perfil del usuario logueado.
     * @return El objeto Profile del usuario actual.
     */
    public Profile getUser() {
        return user;
    }

    /**
     * Establece el usuario de la sesión actual.
     * @param user El perfil del usuario que inicia sesión.
     */
    public void setUser(Profile user) {
        this.user = user;
    }

    /**
     * Limpia la sesión actual, eliminando los datos del usuario y del carrito.
     */
    public void cleanUserSession() {
        this.user = null;
        this.currentOrder = null; 
    }

    /**
     * Comprueba si hay un usuario con sesión iniciada.
     * @return true si el usuario no es nulo, false en caso contrario.
     */
    public boolean isLoggedIn() {
        return user != null;
    }

    /**
     * Obtiene el pedido actual (carrito) para su uso en las vistas.
     * @return El objeto Order actual.
     */
    public Order getCurrentOrder() {
        return currentOrder;
    }

    /**
     * LÓGICA PRINCIPAL: Añade un libro al carrito y persiste los cambios en la BD.
     * Si no existe un pedido pendiente, crea uno nuevo. Si el libro ya está en el 
     * carrito, incrementa su cantidad.
     * * @param book El libro que se desea añadir al carrito.
     */
    public void addToCart(Book book) {
        if (!(this.user instanceof User)) {
            return;
        }
        User cliente = (User) this.user;

        // 1. OBTENER / CREAR PEDIDO
        if (currentOrder == null) {
            currentOrder = dao.getUnfinishedOrder(cliente); // Buscar en BD

            if (currentOrder == null) {
                // CREAR NUEVO
                currentOrder = new Order();
                currentOrder.setIdUsuer(cliente);
                currentOrder.setBought(false);
                currentOrder.setPurchaseDate(new java.sql.Timestamp(System.currentTimeMillis()));
                currentOrder.setListPreBuy(new ArrayList<>());

                // Guardamos para generar el ID en la BD
                dao.saveOrder(currentOrder);
            }
        }

        // 2. BUSCAR SI YA EXISTE EL LIBRO
        boolean encontrado = false;
        if (currentOrder.getListPreBuy() == null) {
            currentOrder.setListPreBuy(new ArrayList<>());
        }

        for (Contain linea : currentOrder.getListPreBuy()) {
            if (linea.getBook().getISBN() == book.getISBN()) {
                linea.setQuantity(linea.getQuantity() + 1);
                encontrado = true;
                break;
            }
        }

        // 3. AÑADIR SI ES NUEVO
        if (!encontrado) {
            Contain nuevaLinea = new Contain(1, currentOrder, book);
            currentOrder.getListPreBuy().add(nuevaLinea);
        }

        // 4. GUARDAR
        dao.saveOrder(currentOrder);
        System.out.println("Carrito guardado.");
    }

    /**
     * Recupera el carrito pendiente de la base de datos al iniciar sesión.
     */
    public void loadCartFromDB() {
        if (this.user != null && this.user instanceof User) {
            User cliente = (User) this.user;
            Order savedOrder = dao.getUnfinishedOrder(cliente);

            if (savedOrder != null) {
                this.currentOrder = savedOrder;
                System.out.println("Carrito recuperado con " + savedOrder.getListPreBuy().size() + " productos.");
            } else {
                this.currentOrder = null;
            }
        } else {
            this.currentOrder = null;
        }
    }

    /**
     * Permite establecer o actualizar el pedido actual desde un origen externo.
     * @param order El nuevo objeto Order para el carrito.
     */
    public void setOrder(Order order) {
        this.currentOrder = order; 
    }

    /**
     * Método no soportado actualmente para obtener los libros del carrito.
     * @return Lanza UnsupportedOperationException.
     */
    public Object getLibrosCarrito() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    /**
     * Fuerza el refresco del pedido anulando la instancia actual de currentOrder,
     * obligando a una nueva consulta a la base de datos en la próxima acción.
     */
    public void refreshOrderAfterDeletion() {
        this.currentOrder = null; 
    }
}