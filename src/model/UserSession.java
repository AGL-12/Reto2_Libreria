package model;

import java.util.ArrayList;
import java.util.List;

public class UserSession {

    // 1. La única instancia que existirá (static)
    private static UserSession instance;

    // 2. El dato del usuario logueado
    private Profile user;
    
    // 3. NUEVO: El carrito actual (Order)
    private Order currentOrder; 
    
    // 4. NUEVO: Instancia del DAO para guardar en caliente
    private final ClassDAO dao = new DBImplementation();
    
    // Constructor privado
    private UserSession() {
    }

    // Singleton
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    // --- Getters y Setters de Usuario ---
    public Profile getUser() { return user; }
    
    public void setUser(Profile user) { this.user = user; }

    public void cleanUserSession() {
        this.user = null;
        this.currentOrder = null; // Limpiamos también el carrito
    }

    public boolean isLoggedIn() { return user != null; }
    
    // --- NUEVO: Getter del pedido para usarlo en la vista de pago ---
    public Order getCurrentOrder() { return currentOrder; }
    

    /**
     * LÓGICA PRINCIPAL: Añadir al carrito y persistir en BD
     */
public void addToCart(Book book) {
        if (!(this.user instanceof User)) return;
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
        if(currentOrder.getListPreBuy() == null) currentOrder.setListPreBuy(new ArrayList<>());

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
     * Recupera el carrito de la BD al loguearse (se llama desde LogInWindowController)
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
}