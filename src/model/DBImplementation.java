package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import threads.SessionHolderThread;
import util.HibernateUtil;

public class DBImplementation implements ClassDAO {


    /**
     * metodo para hacer el login en la app
     * @param username credencial del login
     * @param password credencial para el login
     * @return 
     */
    @Override
    public Profile logIn(String username, String password) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        Profile userFound = null;
        try {
            tx = session.beginTransaction();
            String hql = "FROM Profile p WHERE p.username = :user AND p.password = :pass";
            userFound = session.createQuery(hql, Profile.class)
                    .setParameter("user", username)
                    .setParameter("pass", password)
                    .uniqueResult();
            tx.commit();
            new SessionHolderThread(session).start();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            if (session.isOpen()) {
                session.close();
            }
        }
        return userFound;
    }

    /**
     * metodo para hacer signUp
     * @param profile con los datos para crear el nuevo perfil
     */
    @Override
    public void signUp(Profile profile) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(profile);
            tx.commit();
            new SessionHolderThread(session).start();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            if (session.isOpen()) {
                session.close();
            }
            throw e;
        }
    }
    /**
     * Metodo para eliminar usuarios
     * @param profile con los datos del usuario a eliminar
     */
    @Override
    public void dropOutUser(Profile profile) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // Usamos merge por si el objeto viene desconectado de la sesión anterior
            session.delete(session.merge(profile));

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            // Si falla, lanzamos el error para que salga la alerta en la ventana
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Obtiene todos los usernames para el ComboBox del Admin (Versión
     * Hibernate).
     */
    @Override
    public List<String> comboBoxInsert() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<String> usernames = new ArrayList<>();
        try {
            usernames = session.createQuery("SELECT p.username FROM Profile p", String.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return usernames;
    }

    /**
     * metodo para crear un nuevo libro
     * @param book objeto con los datos del nuevo libro
     */
    @Override
    public void createBook(Book book) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(book); // Esta línea es la que faltaba: guarda el libro en BD
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error creando libro: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * metodo para modificar un libro ya existente
     * @param book el objeto que se quiere modificar
     */
    @Override
    public void modifyBook(Book book) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(book); // Actualiza los datos
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();

            throw new RuntimeException("Error modificando libro: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * metodo para eliminar libro aunque en la ejecucion no se usa
     * se usa en los test
     * @param isbn del libro a eliminar
     */
    @Override
    public void deleteBook(long isbn) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Book b = session.get(Book.class, isbn);
            if (b != null) {
                session.delete(b);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();

            throw new RuntimeException("Error eliminando libro: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * metodo para obtener los datos de los libros
     * @param isbn del libro que estamos buscando
     * @return devuelve el objeto de libro encontrado
     */
    @Override
    public Book getBookData(long isbn) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Book book = null;
        try {
            book = session.get(Book.class, isbn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return book;
    }

   /**
    * metodo para obtener todos los libros de la base de datos
    * @return una lista con todos los libros
    */
    @Override
    public List<Book> getAllBooks() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Book> libros = new ArrayList<>();
        try {
            libros = session.createQuery("FROM Book", Book.class).list();
            for (Book b : libros) {
                List<Commentate> comentarios = b.getComments();
                if (comentarios != null && !comentarios.isEmpty()) {
                    float suma = 0;
                    for (Commentate c : comentarios) {
                        suma += c.getValuation();
                    }
                    b.setAvgValuation(suma / comentarios.size());
                } else {
                    b.setAvgValuation(0f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return libros;
    }

    /**
     * metodo para buscar libros
     * @param busqueda puede ser un isbn, fragmento del titulo o el autor
     * @return devuelve una lista de libros con el filtro aplicado
     */
    @Override
    public List<Book> buscarLibros(String busqueda) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Book> resultados = null;
        try {
            if (busqueda == null || busqueda.trim().isEmpty()) {
                return session.createQuery("FROM Book", Book.class).list();
            }
            String search = "%" + busqueda.toLowerCase() + "%";
            // Nota: str(b.ISBN) convierte el long a string para buscar
            String hql = "FROM Book b WHERE lower(b.title) LIKE :q OR lower(b.author.name) LIKE :q OR str(b.ISBN) LIKE :q";
            resultados = session.createQuery(hql, Book.class).setParameter("q", search).list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }

        }
        return resultados;
    }

    /**
     * metodo para crear autor
     * si el autor existe devuelve el objeto de autor pero sino lo crea con el nombre y apellido
     * @param nombreAutor del autor a buscar/crear
     * @param apellidoAutor apellido del autor a buscar/crear
     * @return el autor completo
     */
    @Override

    public Author getOrCreateAuthor(String nombreAutor, String apellidoAutor) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        Author author = null;
        try {
            tx = session.beginTransaction();
            String hql = "FROM Author a WHERE lower(a.name) = :n AND lower(a.surname) = :s";
            author = session.createQuery(hql, Author.class)
                    .setParameter("n", nombreAutor.toLowerCase().trim())
                    .setParameter("s", apellidoAutor.toLowerCase().trim())
                    .uniqueResult();

            if (author == null) {
                author = new Author();
                author.setName(nombreAutor);
                author.setSurname(apellidoAutor);
                session.save(author);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Error gestionando autor: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return author;
    }

    /**
     * metodo para conseguir los comentarios de un libro
     * @param isbn se usa para buscar los comentarios de ese libro
     * @return devuelve una lista de libros
     */
    public List<Commentate> getCommentsByBook(long isbn) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Commentate> comments = new ArrayList<>();
        try {
            comments = session.createQuery("FROM Commentate c WHERE c.book.ISBN = :isbn", Commentate.class)
                    .setParameter("isbn", isbn).list();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }

        }
        return comments;
    }

    /**
     * busca los comentarios de un usuario
     * @param username del usuario que queremos buscar sus comentarios
     * @return devuelve una lista de comentarios
     */
    public List<Commentate> getCommentsByUser(String username) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Commentate> comments = new ArrayList<>();
        try {
            // HQL para traer los comentarios de un usuario específico
            String hql = "FROM Commentate c WHERE c.user.username = :usr";
            comments = session.createQuery(hql, Commentate.class)
                    .setParameter("usr", username)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return comments;
    }

    /**
     * metodo para añadir comentarios
     * @param comment el obejto de comentario a crear
     */
    @Override
    public void addComment(Commentate comment) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        Author author = null;
        try {
            tx = session.beginTransaction();
            session.save(comment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Error guardando comentario: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * metodo para eliminar comentario
     * @param comment objeto a eliminar
     */
    @Override
    public void deleteComment(Commentate comment) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(comment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Error borrando comentario: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * metodo para actualizar el comentario
     * @param comment objeto a modificar
     */
    @Override
    public void updateComment(Commentate comment) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(comment);
            tx.commit();

            new SessionHolderThread(session).start();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Error actualizando comentario: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }
    /**
     * metodo para coger los pedidos que no se han finalizado
     * @param user usuario al que pertenece el pedido
     * @return devuelve el objeto de la orden de ese usuario
     */
    // --- GESTIÓN DE PEDIDOS Y COMPRAS (TUS MÉTODOS MANTENIDOS) ---
    @Override
    public Order getUnfinishedOrder(User user) {
        Session session = null;
        Order order = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();

// Asumiendo que en tu clase User/Profile el atributo @Id se llama 'userCode'
            String hql = "FROM Order o WHERE o.user.userCode = :userId AND o.bought = false";

            order = (Order) session.createQuery(hql)
                    .setParameter("userId", user.getUserCode())
                    .uniqueResult();

            // --- ¡TRUCO IMPORTANTE! ---
            // Si encontramos pedido, forzamos a Hibernate a cargar la lista de libros
            // ANTES de cerrar la sesión. Si no hacemos esto, la lista llega vacía o rota.
            if (order != null) {
                Hibernate.initialize(order.getListPreBuy());
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return order;
    }

    /**
     * metodo que se encarga de guardar la orden actual
     * @param order el objeto que guarda
     */
    @Override
    public void saveOrder(Order order) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.saveOrUpdate(order);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * busca el historial de compra
     * @param id busca mediente el id del usuario
     * @return devuelve la lista de pedidos finalizados
     */
    @Override
    public List<Order> getHistory(int id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Order> orders = null;
        try {
            String hql = "FROM Order o WHERE o.user.id = :id AND o.bought = true";
            orders = session.createQuery(hql, Order.class).setParameter("id", id).list();
            if (orders != null) {
                for (Order o : orders) {
                    float precioCalculado = 0;
                    if (o.getListPreBuy() != null) {
                        for (Contain linea : o.getListPreBuy()) {
                            precioCalculado += (linea.getBook().getPrice() * linea.getQuantity());
                        }
                    }
                    o.setTotal(precioCalculado);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return orders;
    }

    /**
     * metodo para buscar un pedido en especifico
     * @param id del pedido a buscar
     * @return devuelve una lista con el contenido del pedido
     */
    @Override
    public List<Contain> getOrder(int id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Contain> contains = null;
        try {
            String hql = "FROM Contain c JOIN FETCH c.book WHERE c.order.idOrder = :id";
            contains = session.createQuery(hql, Contain.class).setParameter("id", id).list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return contains;
    }

    /**
     * metodo para cargar los articulos del carrito de compra
     * @param id del usuario
     * @return devuelve una lista con el contenido del carrito de compra
     */
    @Override
    public List<Contain> getCartItem(int id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Contain> cart = null;
        try {
            String hql = "FROM Contain c JOIN FETCH c.book WHERE c.order.user.id = :idUser AND c.order.bought = false";
            cart = session.createQuery(hql, Contain.class).setParameter("idUser", id).list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return cart;
    }

    @Override
    public Order cartOrder(int idUsuario) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Order cart = null;
        try {
            String hql = "FROM Order o LEFT JOIN FETCH o.listPreBuy l LEFT JOIN FETCH l.book WHERE o.user.userCode = :idUser AND o.bought = false";
            cart = session.createQuery(hql, Order.class).setParameter("idUser", idUsuario).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return cart;
    }
    /**
     * metodo para finalizar un pedido y comprarlo
     * @param order es el objeto de order que finaliza
     * @return un boolean para indicar que se ha comprado
     */
    @Override
    public boolean buy(Order order) {
        boolean comprado = false;
        Session session = HibernateUtil.getSessionFactory().openSession(); //
        Transaction tx = null;
        try {
            tx = session.beginTransaction(); //

            // 1. Cambiamos el estado del pedido a comprado y ponemos la fecha actual
            order.setBought(true); //
            order.setPurchaseDate(new Timestamp(System.currentTimeMillis())); //

            // 2. DESCONTAR STOCK REAL: Recorremos las líneas del pedido (Contain)
            if (order.getListPreBuy() != null) { //
                for (model.Contain linea : order.getListPreBuy()) { //
                    model.Book libro = linea.getBook(); //

                    // Obtenemos la cantidad que el usuario eligió en el carrito
                    int cantidadComprada = linea.getQuantity(); //

                    // Restamos esa cantidad específica al stock que hay en la base de datos
                    int nuevoStock = libro.getStock() - cantidadComprada; //

                    libro.setStock(nuevoStock); //
                    session.update(libro); // Actualizamos el libro con su nuevo stock
                }
            }

            session.update(order); // Actualizamos el pedido a comprado
            tx.commit(); // Guardamos todos los cambios
            comprado = true;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback(); // Si algo falla, no se toca el stock
            }
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) {
                session.close(); //
            }
        }
        return comprado;
    }

    @Override
    public int getOrderId(int id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        int idOrder = 0;
        try {
            String hql = "SELECT o.idOrder FROM Order o WHERE o.user.id = :id AND o.bought = false";
            Integer result = session.createQuery(hql, Integer.class).setParameter("id", id).uniqueResult();
            if (result != null) {
                idOrder = result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
        return idOrder;
    }

    /**
     * metodo para cargar todos los usuarios
     * @return devuelve una lista con los usuarios
     */
    @Override
    public List<User> getAllUsers() {
        System.out.println("sesion");
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<User> users = new ArrayList<>(); // Inicializamos la lista para que no sea null

        try {
            // TRUCO: Pedimos todos los perfiles (Profile es la clase padre que seguro está mapeada)
            List<Profile> allProfiles = session.createQuery("FROM Profile", Profile.class).getResultList();
            // Filtramos manualmente: Nos quedamos solo con los que son de clase 'User'
            for (Profile p : allProfiles) {
                if (p instanceof User) {
                    users.add((User) p);
                }
            }

            System.out.println("Usuarios encontrados en BD: " + users.size());

        } catch (Exception e) {
            System.err.println("Error buscando usuarios: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return users;
    }

    /**
     * meotdo para modificar un usuairo
     * @param profile perfil que se va a modificar
     */
    @Override
    public void modificarUser(Profile profile) {
        Session session = HibernateUtil.getSessionFactory().openSession(); //
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // merge actualiza el registro en la base de datos con el estado del objeto profile
            session.merge(profile);

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al actualizar el perfil: " + e.getMessage());
        } finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * metodo para eliminar un libro del contenido de un pedido sin comprar
     * @param contain objeto que se modifica
     */
    public void removeBookFromOrder(Contain contain) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            // Usamos delete directamente con el objeto que encontramos en el controlador
            session.delete(contain);

            tx.commit();
        } finally {
            session.close();
        }
    }

}
