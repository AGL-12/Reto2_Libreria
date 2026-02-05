# 📚 Book&Bugs

## Descripción
**Book&Bugs** es una aplicación de escritorio desarrollada en **Java** con **JavaFX**. Permite la gestión integral de una librería, diferenciando entre clientes (compra, valoraciones) y administradores (gestión de catálogo y usuarios).

El proyecto sigue una arquitectura **MVC**, utiliza **Hibernate** para la persistencia de datos y **JasperReports** para informes.

---

## 🚀 Funcionalidades

### 👤 Cliente
* **Navegación:** Ventana principal intuitiva con cabecera dinámica.
* **Catálogo:** Visualización detallada de libros.
* **Carrito:** Gestión de cesta de la compra, cantidades y tramitación de pedidos.
* **Historial:** Consulta de pedidos pasados y sus detalles.
* **Social:** Sistema de valoración con estrellas y sección de comentarios.
* **Informes:** Generación de reportes técnicos del estado de la librería.


### 🛠 Administrador
* **Gestión de Libros:** Crear, modificar y eliminar libros (CRUD).
* **Gestión de Usuarios:** Control de cuentas registradas.
* **Informes:** Generación de reportes técnicos del estado de la librería.

---

## 🛠 Stack Tecnológico

* **Lenguaje:** Java (JDK 8+).
* **UI:** JavaFX (FXML + CSS).
* **Base de Datos:** MySQL.
* **ORM:** Hibernate.
* **Informes:** JasperReports.
* **Testing:** JUnit 4 + TestFX.


---

## ⚙️ Instalación y Ejecución

### Prerrequisitos
* Java JDK instalado.
* MySQL Server en ejecución.
* Necesario crear la base de datos previa al en MySQL mediante la query
  CREATE DATABASE BookStore;
* NetBeans.

### Otros
* Hay una precarga que cargará administrador y 7 libros

### Modificaciones en cuanto al reto previo
* Se ha añadido botón historial.
* Se ha modificado el botón logout ahora hace un return a la página principal.
* Ahora la aplicación se inicio en vez de en el login en la página principal.

### Pasos
1. **Clonar repositorio:**
   ```bash
   git clone [https://github.com/AGL-12/Reto2_Libreria.git](https://github.com/AGL-12/Reto2_Libreria.git)


## 👥 Equipo de Desarrollo

Proyecto realizado para el Reto 2 (2026). Distribución de responsabilidades:

* **Alex:** Ventana Principal, Componente Header y Componente de Estrellas (Valoración).
* **Ander:** Lógica del Carrito de Compra, Historial de Pedidos y Detalle de Pedido.
* **Mikel:** Gestión de Comentarios (`BookViewController`, `CommentViewController`), Lógica de Opiniones, menú, click derecho y jasper.
* **Unai:** Panel de Administración (Gestión de Libros y Usuarios).


