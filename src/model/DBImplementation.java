package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import threads.SessionHolderThread;
import utilities.HibernateUtil;

public class DBImplementation implements ClassDAO {

    // --- MÉTODOS DE USUARIO (LogIn, SignUp, etc.) ---
    // (Estos se mantienen igual que tu versión original)
    
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
            if (tx != null) tx.rollback();
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

    @Override public void dropOutUser(Profile p) {}
    @Override 
    public void dropOutAdmin(Profile profile) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            // Cargar el objeto persistente antes de borrarlo
            Profile p = session.get(Profile.class, profile.getUsername());
            if (p != null) {
                session.delete(p);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Error al eliminar el usuario: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
    }
    @Override public void modificarUser(Profile p) {}
    @Override 
    public List<String> comboBoxInsert() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<String> usernames = new ArrayList<>();
        try {
            // Obtenemos solo los nombres de usuario de la tabla Profile
            usernames = session.createQuery("SELECT p.username FROM Profile p", String.class).list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return usernames;
    }

    // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! MÉTODOS CRUD DE LIBRO RELLENADOS ---

    @Override
    public void createBook(Book book) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(book); // Esta línea es la que faltaba: guarda el libro en BD
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
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
            session.update(book); // Actualiza los datos
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
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
            Book book = session.get(Book.class, isbn); // Buscamos por ID (long)
            if (book != null) {
                session.delete(book); // Borramos
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
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
            // "Tocamos" el autor para inicializarlo si la carga es Lazy (perezosa)
            if (book != null && book.getAuthor() != null) {
                book.getAuthor().getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return book;
    }

    // --- RESTO DE MÉTODOS (Listar, Comentarios, Autores) ---
    // Se mantienen igual que antes, solo asegúrate de no borrar getOrCreateAuthor

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
                    for (Commentate c : comentarios) suma += c.getValuation();
                    b.setAvgValuation(suma / comentarios.size());
                } else {
                    b.setAvgValuation(0f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
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
            // Nota: str(b.ISBN) convierte el long a string para buscar
            String hql = "FROM Book b WHERE lower(b.title) LIKE :q OR lower(b.author.name) LIKE :q OR str(b.ISBN) LIKE :q";
            resultados = session.createQuery(hql, Book.class).setParameter("q", search).list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
        return resultados;
    }

    @Override
    public List<Commentate> getCommentsByBook(long isbn) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<Commentate> comments = new ArrayList<>();
        try {
            comments = session.createQuery("FROM Commentate c WHERE c.book.ISBN = :isbn", Commentate.class)
                    .setParameter("isbn", isbn).list();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session.isOpen()) session.close();
        }
        return comments;
    }

    @Override public void addComment(Commentate c) { /* Implementar si lo usas */ }
    @Override public void deleteComment(Commentate c) { /* Implementar si lo usas */ }
    @Override public void updateComment(Commentate c) { /* Implementar si lo usas */ }
    @Override public List<Commentate> getCommentsByUser(String u) { return new ArrayList<>(); }
    
    @Override
    public Author getOrCreateAuthor(String name, String surname) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        Author author = null;
        try {
            tx = session.beginTransaction();
            String hql = "FROM Author a WHERE lower(a.name) = :n AND lower(a.surname) = :s";
            author = session.createQuery(hql, Author.class)
                    .setParameter("n", name.toLowerCase())
                    .setParameter("s", surname.toLowerCase())
                    .uniqueResult();

            if (author == null) {
                author = new Author();
                author.setName(name);
                author.setSurname(surname);
                session.save(author);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            throw new RuntimeException("Error autor: " + e.getMessage());
        } finally {
            if (session.isOpen()) session.close();
        }
        return author;
    }
}