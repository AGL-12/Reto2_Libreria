package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import threads.SessionHolderThread;
import utilities.HibernateUtil;

/**
 * Implementation of ClassDAO using database operations.
 */
public class DBImplementation implements ClassDAO {

    // --- GESTIÓN DE USUARIOS Y SESIÓN ---

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
            // Mantenemos tu lógica de hilos
            new SessionHolderThread(session).start();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            if (session.isOpen()) session.close();
        }
        return userFound;
    }

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
            if (tx != null) tx.rollback();
            if (session.isOpen()) session.close();
            throw e;
        }
    }

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
            if (tx != null) tx.rollback();
            e.printStackTrace();
            // Si falla, lanzamos el error para que salga la alerta en la ventana
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    /**
     * Elimina un usuario seleccionado por el administrador.
     */
    

    @Override
    public void modificarUser(Profile profile) {
        // Lógica de modificar
    }

    /**
     * Obtiene todos los usernames para el ComboBox del Admin (Versión Hibernate).
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
            if (session.isOpen()) session.close();
        }
        return usernames;
    }

    // --- GESTIÓN DE LIBROS ---

    @Override
    public void createBook(Book book) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(book);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error creando libro: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    @Override
    public void modifyBook(Book book) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(book);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error modificando libro: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
    }

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
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error eliminando libro: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    @Override
    public Book getBookData(long isbn) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Book book = null;
        try {
            book = session.get(Book.class, isbn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return book;
    }

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
            if (session.isOpen()) session.close();
        }
        return libros;
    }

    @Override
    public List<Book> buscarLibros(String busqueda) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Book> resultados = null;
        try {
            if (busqueda == null || busqueda.trim().isEmpty()) {
                return session.createQuery("FROM Book", Book.class).list();
            }
            String search = "%" + busqueda.toLowerCase() + "%";
            String hql = "FROM Book b WHERE lower(b.title) LIKE :q OR lower(b.author.name) LIKE :q";
            resultados = session.createQuery(hql, Book.class).setParameter("q", search).list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return resultados;
    }

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
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error gestionando autor: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
        return author;
    }

    // --- GESTIÓN DE COMENTARIOS ---

    @Override
    public List<Commentate> getCommentsByBook(long isbn) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Commentate> comments = null;
        try {
            String hql = "FROM Commentate c WHERE c.book.ISBN = :isbn";
            comments = session.createQuery(hql, Commentate.class).setParameter("isbn", isbn).list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return comments;
    }

    // NUEVO: Para la ventana de eliminar comentarios por usuario
    public List<Commentate> getCommentsByUser(String username) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Commentate> comments = new ArrayList<>();
        try {
            String hql = "FROM Commentate c JOIN FETCH c.book WHERE c.user.username = :usr";
            comments = session.createQuery(hql, Commentate.class)
                    .setParameter("usr", username)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return comments;
    }

    @Override
    public void addComment(Commentate comment) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(comment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error guardando comentario: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    @Override
    public void deleteComment(Commentate comment) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(comment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error borrando comentario: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    @Override
    public void updateComment(Commentate comment) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(comment);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error actualizando comentario: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
    }

    // --- GESTIÓN DE PEDIDOS Y COMPRAS (TUS MÉTODOS MANTENIDOS) ---

    @Override
    public Order getUnfinishedOrder(User user) {
        Session session = null;
        Order order = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            // Verifica que 'userCode' sea el ID correcto en tu modelo
            String hql = "FROM Order o WHERE o.user.userCode = :userId AND o.bought = false";
            order = (Order) session.createQuery(hql)
                    .setParameter("userId", user.getUserCode())
                    .uniqueResult();
            if (order != null) {
                Hibernate.initialize(order.getListPreBuy());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) session.close();
        }
        return order;
    }

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
            if (tx != null) tx.rollback();
            throw new RuntimeException(e);
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public boolean buy(Order order) {
        boolean comprado = false;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            order.setBought(true);
            order.setPurchaseDate(new java.sql.Timestamp(System.currentTimeMillis()));
            session.update(order);
            tx.commit();
            comprado = true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return comprado;
    }

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
            if (session.isOpen()) session.close();
        }
        return orders;
    }

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
            if (session.isOpen()) session.close();
        }
        return contains;
    }

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
            if (session.isOpen()) session.close();
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
            if (session.isOpen()) session.close();
        }
        return cart;
    }

    @Override
    public int getOrderId(int id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        int idOrder = 0;
        try {
            String hql = "SELECT o.idOrder FROM Order o WHERE o.user.id = :id AND o.bought = false";
            Integer result = session.createQuery(hql, Integer.class).setParameter("id", id).uniqueResult();
            if (result != null) idOrder = result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return idOrder;
    }

    @Override
    public List<User> getAllUsers() {
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
    
}