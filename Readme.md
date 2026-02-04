\# 📚 Book\&Bugs - Gestión de Librería



!\[Logo](src/images/Book\&Bugs\_Logo.png)



\## 📖 Descripción

\*\*Book\&Bugs\*\* es una aplicación de escritorio desarrollada en \*\*Java\*\* utilizando \*\*JavaFX\*\* para la interfaz gráfica. Este sistema permite la gestión integral de una librería, ofreciendo funcionalidades tanto para clientes (compra de libros, comentarios, valoraciones) como para administradores (gestión de catálogo, usuarios y stock).



El proyecto implementa una arquitectura en capas (MVC), utiliza \*\*Hibernate\*\* como ORM para la persistencia de datos y \*\*JasperReports\*\* para la generación de informes.



---



\## 🚀 Características Principales



\### 👤 Usuarios (Clientes)

\* \*\*Registro e Inicio de Sesión:\*\* Sistema seguro de autenticación.

\* \*\*Catálogo de Libros:\*\* Visualización de portadas, sinopsis, precios y stock.

\* \*\*Carrito de Compra:\*\* Añadir libros, gestionar cantidades y realizar pedidos.

\* \*\*Historial de Compras:\*\* Consulta de pedidos anteriores.

\* \*\*Comunidad:\*\* Posibilidad de valorar (estrellas) y comentar libros. Edición y borrado de comentarios propios.



\### 🛠 Administradores

\* \*\*Gestión de Libros (CRUD):\*\* Crear, modificar y eliminar libros del catálogo.

\* \*\*Gestión de Usuarios:\*\* Visualizar y eliminar cuentas de usuario.

\* \*\*Informes:\*\* Generación de informes técnicos sobre el estado de la librería.



---



\## 🛠️ Stack Tecnológico



\* \*\*Lenguaje:\*\* Java (JDK 8+).

\* \*\*Interfaz Gráfica:\*\* JavaFX (FXML y CSS).

\* \*\*Base de Datos:\*\* MySQL.

\* \*\*Persistencia:\*\* Hibernate (ORM).

\* \*\*Informes:\*\* JasperReports.

\* \*\*Testing:\*\* JUnit 4 y TestFX para pruebas de interfaz.

\* \*\*Build Tool:\*\* Ant.



---



\## ⚙️ Configuración e Instalación



\### Prerrequisitos

1\.  Tener instalado \*\*Java JDK\*\*.

2\.  Tener instalado \*\*MySQL Server\*\*.

3\.  Un IDE compatible como \*\*NetBeans\*\* o IntelliJ.



\### Pasos

1\.  \*\*Clonar el repositorio:\*\*

&nbsp;   ```bash

&nbsp;   git clone \[https://github.com/AGL-12/Reto2\_Libreria](https://github.com/tu-usuario/BookAndBugs.git)

&nbsp;   ```

2\.  \*\*Base de Datos:\*\*

&nbsp;   \* Asegúrate de tener el servicio de MySQL corriendo.

&nbsp;   \* Crea la base de datos `bookstore`.

&nbsp;   \* Hibernate se encargará de generar las tablas automáticamente al iniciar la app.

3\.  \*\*Configuración de Credenciales:\*\*

&nbsp;   \* Edita el archivo `src/hibernate.cfg.xml` con tu usuario y contraseña de MySQL.





\### Ejecución

\* \*\*Desde NetBeans:\*\* Haz clic derecho en el proyecto > \*Run\*.

\* \*\*Generar JAR:\*\* Haz clic derecho > \*Clean and Build\*. El ejecutable se generará en la carpeta `dist/`.

&nbsp;   > \*\*Nota:\*\* Para ejecutar el JAR fuera del IDE, asegúrate de mantener la carpeta `lib/` junto al archivo `.jar`.



---



\## 🧪 Testing



El proyecto incluye una suite de pruebas automatizadas utilizando \*\*TestFX\*\* para simular la interacción del usuario con la interfaz gráfica.



Para ejecutar las pruebas:

1\.  Ve a la carpeta `test/controller`.

2\.  Ejecuta archivos como `BookViewControllerTest.java` o `MainBookStoreControllerTest.java`.



---



\## 👥 Autores - Equipo de Desarrollo



Proyecto realizado para el Reto 2 (2025).



\* \*\*Alex\*\*

\* \*\*Unai\*\*

\* \*\*Ander\*\*

\* \*\*Mikel\*\*



---



\## 📄 Licencia



Este proyecto es de uso educativo.

