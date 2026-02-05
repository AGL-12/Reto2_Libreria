package threads;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class SessionHolderThread extends Thread {

    private final Session session;
    private final Transaction tx;

    // Pasamos la sesión Y la transacción abierta
    public SessionHolderThread(Session session, Transaction tx) {
        this.session = session;
        this.tx = tx;
    }

    @Override
    public void run() {
        try {
            System.out.println("Hilo " + this.getId() + ": Reteniendo conexión física (Transacción abierta)...");
            Thread.sleep(30000); // 30 segundos de bloqueo
        } catch (InterruptedException e) {
        } finally {
            // AHORA cerramos la transacción y la sesión
            if (tx != null && tx.isActive()) {
                tx.commit(); // O rollback, da igual para el test
            }
            if (session != null && session.isOpen()) {
                session.close();
                System.out.println("Hilo " + this.getId() + ": Conexión liberada.");
            }
        }
    }
}