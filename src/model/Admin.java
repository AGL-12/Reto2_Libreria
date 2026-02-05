package model;

import javax.persistence.*;

/**
 * Representa un perfil de administrador en el sistema.
 * Esta clase extiende de {@link Profile} y añade atributos específicos para la gestión
 * administrativa, como la cuenta corriente.
 * @version 1.0
 */
@Entity
@Table(name = "admin")
@PrimaryKeyJoinColumn(name = "user_code")
public class Admin extends Profile {
    
    /**
     * Cuenta corriente asociada al administrador.
     * Mapeada como una columna CHAR(24) en la base de datos.
     */
    @Column(name = "current_account", columnDefinition = "CHAR(24)")
    private String currentAccount;

    /**
     * Constructor con todos los parámetros para crear un administrador.
     * @param currentAccount La cuenta corriente del administrador.
     * @param username El nombre de usuario único para el acceso.
     * @param password La contraseña de la cuenta.
     * @param email El correo electrónico del administrador.
     * @param userCode El código identificador único del perfil.
     * @param name El nombre de pila del administrador.
     * @param telephone El teléfono de contacto.
     * @param surname El apellido del administrador.
     */
    public Admin(String currentAccount, String username, String password, String email, int userCode, String name, String telephone, String surname) {
        super(username, password, email, userCode, name, telephone, surname);
        this.currentAccount = currentAccount;
    }

    /**
     * Constructor por defecto requerido por JPA.
     * Inicializa la cuenta corriente como una cadena vacía.
     */
    public Admin() {
        this.currentAccount = "";
    }

    /**
     * Obtiene la cuenta corriente del administrador.
     * @return String con la cuenta corriente.
     */
    public String getCurrentAccount() { return currentAccount; }

    /**
     * Establece la cuenta corriente del administrador.
     * @param currentAccount La nueva cuenta corriente.
     */
    public void setCurrentAccount(String currentAccount) { this.currentAccount = currentAccount; }

    /**
     * Implementación del método de inicio de sesión para administradores.
     * @throws UnsupportedOperationException Actualmente no está implementado.
     */
    @Override
    public void logIn() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Devuelve una representación en cadena del objeto Admin.
     * @return String que incluye la cuenta corriente.
     */
    @Override
    public String toString() {
        return "Admin{" + "currentAccount=" + currentAccount + '}';
    }
}