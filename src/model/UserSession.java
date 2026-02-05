package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la sesión del usuario activo en la aplicación mediante el patrón Singleton.
 * Esta clase se encarga de mantener el estado del usuario logueado, así como de 
 * administrar el carrito de compra actual (pedido no finalizado) y su persistencia.
 * * @author alex
 * @version 1.0
 */
public class UserSession {

    // 1. La única instancia que existirá (static)
    /**
     * Instancia única de UserSession.
     */
    private static UserSession instance;

    // 2. El dato del usuario logueado
    /**
     * Perfil del usuario que ha iniciado sesión.
     */
    private Profile user;

    // 3. NUEVO: El carrito actual (Order)
    /**
     * Pedido actual que actúa como carrito de la compra del usuario.
     */
    private Order currentOrder;

    // 4. NUEVO: Instancia del DAO para guardar en caliente
    /**
     * Interfaz de acceso a datos para realizar operaciones de persistencia.
     */
    private final ClassDAO dao = new DBImplementation();

    // Constructor privado
    /**
     * Constructor privado para evitar la instanciación externa (Patrón Singleton).
     */
    private UserSession() {
    }

    // Singleton
    /**
     * Obtiene la instancia única de UserSession. Si no existe, la crea.
     * @return La instancia única de {@link UserSession}.
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // --- Getters y Setters de Usuario ---
    /**
     * Obtiene el perfil del usuario logueado.
     * @return El objeto {@link Profile} del usuario.
     */
    public Profile getUser() {
        return user;
    }

    /**
     * Establece el perfil del usuario al iniciar sesión.
     * @param user El objeto {@link Profile} a asignar.
     */
    public void setUser(Profile user) {
        this.user = user;
    }

    /**
     * Limpia la información de la sesión actual, incluyendo el usuario y el carrito.
     */
    public void cleanUserSession() {
        this.user = null;
        this.currentOrder = null; // Limpiamos también el carrito
    }

    /**
     * Comprueba si hay un usuario con sesión iniciada.
     * @return true si el usuario no es nulo, false en caso contrario.
     */
    public boolean isLoggedIn() {
        return user != null;
    }

    // --- NUEVO: Getter del pedido para usarlo en la vista de pago ---
    /**
     * Obtiene el pedido actual (carrito) del usuario.
     * @return El objeto {@link Order} actual.
     */
    public Order getCurrentOrder() {
        return currentOrder;
    }

    /**
     * LÓGICA PRINCIPAL: Añadir al carrito y persistir en BD.
     * Gestiona la creación de un nuevo pedido si no existe uno pendiente,
     * actualiza cantidades si el libro ya está en el carrito o añade una nueva línea.
     * @param book El objeto {@link Book} que se desea añadir al carrito.
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

                // --- PASO CLAVE ---
                // Guardamos YA para tener ID. Esto hace que saveOrUpdate futuro funcione bien.
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
            // Al tener currentOrder un ID real, el Contain se crea bien
            Contain nuevaLinea = new Contain(1, currentOrder, book);
            currentOrder.getListPreBuy().add(nuevaLinea);
        }

        // 4. GUARDAR
        // Al llamar a saveOrUpdate aquí, como currentOrder tiene ID, actualiza la lista.
        dao.saveOrder(currentOrder);
        System.out.println("Carrito guardado.");
    }

    /**
     * Recupera el carrito de la BD al loguearse (se llama desde
     * LogInWindowController)
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
     * Permite establecer o limpiar el pedido actual desde fuera de la clase.
     * @param order El objeto {@link Order} a asignar.
     */
    // Añade este método para poder limpiar el pedido desde fuera
    public void setOrder(Order order) {
        this.currentOrder = order; // Asegúrate de que tu variable se llame 'currentOrder'
    }

    /**
     * Método no soportado actualmente para obtener los libros del carrito.
     * @return No devuelve nada, lanza excepción.
     * @throws UnsupportedOperationException Siempre, ya que no está implementado.
     */
    public Object getLibrosCarrito() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Refresca el estado del pedido actual forzando una nueva carga desde la base de datos.
     * Se suele utilizar tras operaciones de borrado para mantener la integridad de los datos.
     */
    public void refreshOrderAfterDeletion() {
        // Forzamos a que la próxima vez que se añada algo, se busque de nuevo en la BD
        this.currentOrder = null; 
    }
}