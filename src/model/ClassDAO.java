package model;

import exception.MyFormException;
import java.util.List;

/**
 * Interfaz que define las operaciones de acceso a datos (DAO) para el sistema.
 * Proporciona métodos para la gestión de usuarios, libros, comentarios y
 * pedidos.
 *
 */
public interface ClassDAO {

    /**
     * Autentica a un usuario en el sistema.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña.
     * @return El objeto Profile si las credenciales son correctas.
     * @throws exception.MyFormException para el controlador
     */
    public Profile logIn(String username, String password) throws MyFormException;

    /**
     * Registra un nuevo perfil en el sistema.
     *
     * @param profile El perfil a registrar.
     * @throws exception.MyFormException para el controlador
     */
    public void signUp(Profile profile) throws MyFormException;

    /**
     * Elimina un usuario del sistema.
     *
     * @param profile El perfil a dar de baja.
     */
    public void dropOutUser(Profile profile);

    /**
     * Modifica los datos de un usuario existente.
     *
     * @param profile El perfil con los datos actualizados.
     */
    public void modificarUser(Profile profile);

    /**
     * Obtiene una lista de todos los usuarios registrados.
     *
     * @return Lista de objetos User.
     */
    public List<User> getAllUsers();

    // --- GESTIÓN DE LIBROS (Book_) ---
    /**
     * Crea un nuevo libro en la base de datos.
     *
     * @param book El objeto libro a persistir.
     */
    public void createBook(Book book);

    /**
     * Actualiza la información de un libro existente.
     *
     * @param book El libro con la información modificada.
     */
    public void modifyBook(Book book);

    /**
     * Elimina un libro del sistema mediante su ISBN.
     *
     * @param isbn El identificador único del libro.
     */
    public void deleteBook(long isbn);

    /**
     * Recupera los datos de un libro específico.
     *
     * @param isbn El ISBN del libro a buscar.
     * @return El objeto Book encontrado.
     */
    public Book getBookData(long isbn);

    /**
     * Lista todos los libros disponibles en el catálogo.
     *
     * @return Lista de objetos Book.
     * @throws exception.MyFormException Para el controlador
     */
    public List<Book> getAllBooks() throws MyFormException;

    // --- GESTIÓN DE COMENTARIOS --
    /**
     * Obtiene todos los comentarios asociados a un libro.
     *
     * @param isbn ISBN del libro.
     * @return Lista de comentarios.
     */
    public List<Commentate> getCommentsByBook(long isbn);

    /**
     * Añade un nuevo comentario a un libro.
     *
     * @param comment Objeto Commentate a añadir.
     */
    public void addComment(Commentate comment);

    /**
     * Actualiza un comentario existente.
     *
     * @param comment El comentario modificado.
     * @throws exception.MyFormException Para el controlador
     */
    public void updateComment(Commentate comment) throws MyFormException;

    /**
     * Elimina un comentario del sistema.
     *
     * @param comment El comentario a borrar.
     */
    public void deleteComment(Commentate comment);

    /**
     * Obtiene los comentarios realizados por un usuario concreto.
     *
     * @param username Nombre del usuario.
     * @return Lista de sus comentarios.
     */
    public List<Commentate> getCommentsByUser(String username);

    /**
     * Busca libros que coincidan con un criterio de búsqueda.
     *
     * @param busqueda Cadena de texto a buscar.
     * @return Lista de libros coincidentes.
     */
    public List<Book> buscarLibros(String busqueda);

    /**
     * Obtiene datos para rellenar elementos de selección (ComboBox).
     *
     * @return Lista de datos genérica.
     */
    public List comboBoxInsert();

    //Historial Compras
    /**
     * Recupera el historial de pedidos de un usuario.
     *
     * @param id ID del usuario.
     * @return Lista de pedidos (Order).
     */
    public List<Order> getHistory(int id);

    /**
     * Obtiene el detalle de contenido de un pedido.
     *
     * @param id ID del pedido.
     * @return Lista de líneas de contenido (Contain).
     */
    public List<Contain> getOrder(int id);

    //Carrtito
    /**
     * Obtiene los elementos actuales en el carrito del usuario.
     *
     * @param id ID del usuario.
     * @return Lista de elementos en el carrito.
     */
    public List<Contain> getCartItem(int id);

    /**
     * Obtiene el pedido que actúa como carrito.
     *
     * @param id ID del usuario.
     * @return El objeto Order correspondiente al carrito.
     */
    public Order cartOrder(int id);

    /**
     * Procesa la compra de un pedido.
     *
     * @param order El pedido a comprar.
     * @return true si la operación tuvo éxito.
     */
    public boolean buy(Order order);

    /**
     * Obtiene el identificador de un pedido.
     *
     * @param id Parámetro de búsqueda del pedido.
     * @return ID del pedido.
     */
    public int getOrderId(int id);

    /**
     * Busca un pedido NO PAGADO (bought=false) de un usuario específico.
     *
     * @param user El usuario dueño del pedido.
     * @return El pedido pendiente encontrado.
     */
    public Order getUnfinishedOrder(User user);

    /**
     * Guarda o actualiza un pedido y sus líneas en la base de datos.
     *
     * @param order El pedido a guardar.
     */
    public void saveOrder(Order order);

    /**
     * Busca un autor existente o lo crea si no existe.
     *
     * @param nombreAutor Nombre del autor.
     * @param apellidoAutor Apellido del autor.
     * @return El objeto Author persistido.
     */
    public Author getOrCreateAuthor(String nombreAutor, String apellidoAutor);

    /**
     * Elimina una línea de contenido específica de un pedido.
     *
     * @param contain La línea de detalle a eliminar.
     */
    public void removeBookFromOrder(Contain contain);
}
