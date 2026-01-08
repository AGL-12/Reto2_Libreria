package threads;

import org.hibernate.Session;

public class SessionHolderThread extends Thread {

    private final Session session;

    public SessionHolderThread(Session session) {
        this.session = session;
    }

    @Override
    public void run() {
        try {
            // la conexión física contra MySQL 30 segundos más.
            System.out.println("Hilo " + this.getId() + ": Reteniendo sesión en background...");
            Thread.sleep(30000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // AHORA LIBERAMOS EL HUECO EN EL POOL
            if (session != null && session.isOpen()) {
                session.close();
                System.out.println("Hilo " + this.getId() + ": Sesión cerrada definitivamente.");
            }
        }
    }
}