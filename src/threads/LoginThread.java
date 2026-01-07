package threads;

import controller.LogInWindowController;
import javafx.application.Platform;
import model.DBImplementation;
import model.Profile;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utilities.HibernateUtil;

public class LoginThread extends Thread {

    private final String user;
    private final String pass;
    private final LogInWindowController ventanaLogin; // Referencia a TU ventana
    private final DBImplementation dao;

    // CONSTRUCTOR: Pasamos los datos y LA VENTANA (this)
    public LoginThread(String user, String pass, LogInWindowController ventana) {
        this.user = user;
        this.pass = pass;
        this.ventanaLogin = ventana;
        this.dao = new DBImplementation(); // O se lo pasas también, como prefieras
    }

    @Override
    public void run() {
        Session session = null;
        Transaction tx = null;
        Profile resultado = null;

        try {
            // EL HILO ES EL JEFE: ÉL ABRE LA SESIÓN Y LA TRANSACCIÓN
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Llamamos al DAO pasándole la sesión activa
            resultado = dao.logIn(session, user, pass);

            tx.commit(); // Confirmamos

            // AVISAR A LA VENTANA
            final Profile perfilFinal = resultado;
            Platform.runLater(() -> ventanaLogin.finalizarLogin(perfilFinal));

            // RETENCIÓN (El requisito)
            System.out.println("Login OK. Reteniendo conexión 30s...");
            Thread.sleep(30000);
            System.out.println("liberado");
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            //Alex: borrar al final print... solo sirve para ver el error
            e.printStackTrace();
            Platform.runLater(() -> ventanaLogin.mostrarError(e.getMessage()));
        } catch (InterruptedException ex) {
            //nada por sleep
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
