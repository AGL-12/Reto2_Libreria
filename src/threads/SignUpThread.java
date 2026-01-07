package threads;

import controller.SignUpWindowController;
import javafx.application.Platform;
import model.DBImplementation;
import model.Profile;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utilities.HibernateUtil;

public class SignUpThread extends Thread {

    private final Profile nuevoPerfil; // El objeto User con todos los datos
    private final SignUpWindowController ventana;
    private final DBImplementation dao;

    public SignUpThread(Profile nuevoPerfil, SignUpWindowController ventana) {
        this.nuevoPerfil = nuevoPerfil;
        this.ventana = ventana;
        this.dao = new DBImplementation();
    }

    @Override
    public void run() {
        Session session = null;
        Transaction tx = null;
        boolean exito = false;
        String mensajeError = "";

        try {
            // 1. ABRIR SESIÓN
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // 2. GUARDAR EN BD
            dao.signUp(session, nuevoPerfil);

            // Hibernate ya le ha puesto el ID al objeto nuevoPerfil,
            // así que ya cuenta como "Logueado" y listo para usar.
            tx.commit();
            exito = true;

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            exito = false;
            mensajeError = e.getMessage(); // Capturamos error (ej: usuario duplicado)
            e.printStackTrace();
        }

        // 3. AVISAR PANTALLA
        final boolean finalExito = exito;
        final String finalMsg = mensajeError;

        Platform.runLater(() -> ventana.finalizarRegistro(finalExito, finalMsg, nuevoPerfil));

        // 4. RETENCIÓN (Requisito 30s)
        try {
            System.out.println("Registro visual finalizado. Reteniendo conexión 30s...");
            Thread.sleep(30000);
        } catch (InterruptedException e) {
        }

        // 5. CERRAR
        if (session != null && session.isOpen()) {
            session.close();
            System.out.println("Conexión de Registro liberada.");
        }
    }
}
