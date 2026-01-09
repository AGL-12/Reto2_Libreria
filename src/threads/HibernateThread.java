package threads;

import javafx.application.Platform;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utilities.HibernateUtil;

/**
 *
 * @author Alexander
 */
public abstract class HibernateThread extends Thread{
    // --- MÉTODOS QUE TÚ RELLENARÁS AL USAR EL HILO ---
    
    // 1. Aquí pondrás la llamada al DAO (se ejecuta en segundo plano)
    public abstract Object ejecutarLogica(Session session) throws Exception;
    
    // 2. Aquí pondrás qué hacer con la ventana al terminar (se ejecuta en JavaFX)
    public abstract void actualizarInterfaz(Object resultado);

    // --------------------------------------------------

    @Override
    public void run() {
        Session session = null;
        Transaction tx = null;
        Object resultado = null;
        boolean exito = false;

        try {
            // 1. ABRIR SESIÓN (Ocupa un hueco en el Pool C3P0)
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // 2. EJECUTAR TU LÓGICA (Llamada al método abstracto)
            // Le pasamos la sesión para que el DAO trabaje con ella
            resultado = ejecutarLogica(session);

            tx.commit();
            exito = true;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            resultado = e; // El resultado será la excepción para mostrar el error
            exito = false;
        }

        // 3. ACTUALIZAR PANTALLA (Inmediatamente, no espera a los 30s)
        final Object respuestaFinal = resultado;
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                // Llamamos al método abstracto para que tu ventana se entere
                actualizarInterfaz(respuestaFinal);
            }
        });

        // 4. RETENCIÓN ACADÉMICA (Saturación del Pool)
        // La ventana ya se ha liberado, pero nosotros retenemos la conexión.
        if (session != null && session.isOpen()) {
            System.out.println("Hilo " + this.getId() + ": Operación visual terminada. Reteniendo conexión 30s...");
            try {
                Thread.sleep(30000); 
            } catch (InterruptedException ex) {
                // Ignorar interrupción
            }
            
            // 5. CERRAR SESIÓN (Liberar hueco en el Pool)
            session.close();
            System.out.println("Hilo " + this.getId() + ": Conexión liberada al Pool.");
        }
    }
    /* ejemplo para esto
    @FXML
    private void botonPulsado() {
        
        // Bloqueo visual
        miBoton.setDisable(true);

        // CREAR HILO USANDO LA PLANTILLA
        HiloGenerico hilo = new HiloGenerico() {
            
            @Override
            public Object ejecutarLogica(Session session) throws Exception {
                // AQUÍ LLAMAS A TU DAO
                // return dao.logIn(session, user, pass);
                // o
                // dao.signUp(session, nuevoUsuario); return true;
                return true; 
            }

            @Override
            public void actualizarInterfaz(Object resultado) {
                // AQUÍ RECIBES EL RESULTADO
                miBoton.setDisable(false);
                
                if (resultado instanceof Exception) {
                   // Mostrar error
                } else {
                   // Abrir siguiente ventana o mostrar éxito
                }
            }
        };

        // ARRANCAR
        hilo.start();
    }
    */
}
