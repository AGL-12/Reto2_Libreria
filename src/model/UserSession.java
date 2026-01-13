package model;

import java.util.ArrayList;
import java.util.List;

public class UserSession {

    // 1. La única instancia que existirá (static)
    private static UserSession instance;

    // 2. El dato que queremos guardar (el usuario logueado)
    private Profile user;
    
    // Este es el carrito actual (Order)
    private Order currentOrder; 
    
    // Instancia del DAO para guardar en caliente
    private final ClassDAO dao = new DBImplementation();
    
    // 3. Constructor privado para que nadie haga "new UserSession()"
    private UserSession() {
    }

    // 4. Método para obtener la instancia única (Si no existe, la crea)
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    
    // Getter para usar en la ventana de pago
    public Order getCurrentOrder() { return currentOrder; }
    
    // 5. Métodos para guardar y leer el usuario
    public Profile getUser() {
        return user;
    }

    public void setUser(Profile user) {
        this.user = user;
    }

    // 6. Método para cerrar sesión
    public void cleanUserSession() {
        this.user = null; // Borramos el usuario
    }

    // Helper para saber si hay alguien logueado rápido
    public boolean isLoggedIn() {
        return user != null;
    }
    
/**
     * LÓGICA PRINCIPAL: Añadir al carrito y persistir en BD
     */
    public void addToCart(Book book) {
        
        if (!(this.user instanceof User)) {
            System.err.println("ACCESO DENEGADO: Un administrador no puede tener carrito.");
            return; // Salimos inmediatamente. No se crea Order, no se guarda nada.
        }
        // Ahora que estamos seguros, hacemos el casting
        User cliente = (User) this.user;
        // 2. Si no hay carrito en memoria, lo creamos
        if (currentOrder == null) {
            currentOrder = new Order();
            // Aquí usamos la variable 'cliente' que ya es seguro de tipo User
            currentOrder.setIdUsuer(cliente); 
            currentOrder.setBought(false);
            currentOrder.setPurchaseDate(new java.sql.Timestamp(System.currentTimeMillis()));
            currentOrder.setListPreBuy(new ArrayList<>());
        }

        // 2. Comprobar si el libro ya está para sumar cantidad
        boolean encontrado = false;
        if (currentOrder.getListPreBuy() != null) {
            for (Contain linea : currentOrder.getListPreBuy()) {
                // Comparamos ISBNs (usando equals para Long)
                if (linea.getBook().getISBN()==book.getISBN()) {
                    linea.setQuantity(linea.getQuantity() + 1);
                    encontrado = true;
                    break;
                }
            }
        }

        // 3. Si no estaba, creamos la línea (Contain)
        if (!encontrado) {
            // Usamos tu constructor: Contain(cantidad, order, book)
            Contain nuevaLinea = new Contain(1, currentOrder, book);
            currentOrder.getListPreBuy().add(nuevaLinea);
        }
        
        // 4. PERSISTENCIA INMEDIATA
        // Guardamos en la base de datos para que no se pierda aunque cierres la app
        dao.saveOrder(currentOrder);
        System.out.println("Carrito guardado/actualizado en BD. ID Pedido: " + currentOrder.getIdOrder());
    }

/**
     * Recupera el carrito de la BD al loguearse
     */
    public void loadCartFromDB() {
        // 1. Verificamos que sea un usuario normal (User)
        if (this.user != null && this.user instanceof User) {
            
            // 2. Hacemos el casting seguro a User
            User cliente = (User) this.user;

            // 3. Llamamos al DAO pasando el objeto correcto
            Order savedOrder = dao.getUnfinishedOrder(cliente);

            if (savedOrder != null) {
                this.currentOrder = savedOrder;
                System.out.println("Carrito recuperado. Tiene " + savedOrder.getListPreBuy().size() + " productos.");
            } else {
                this.currentOrder = null;
            }
        } else {
            // Si es Admin o null, no hay carrito
            this.currentOrder = null;
        }
    }
    
    // Limpiar al cerrar sesión o terminar compra
    public void clearSession() {
        this.user = null;
        this.currentOrder = null;
    }
}
