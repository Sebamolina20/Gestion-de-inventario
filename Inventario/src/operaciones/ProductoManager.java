package operaciones;

import modelo.producto;

import java.sql.*;
import java.util.List;

public class ProductoManager {

    private producto_config dao;

    public ProductoManager() {
        this.dao = new producto_config("inventario.db");
    }

    // Método para conectar a la base de datos
    private Connection conectarBD() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Asegúrate de tener el driver
            conn = DriverManager.getConnection("jdbc:sqlite:inventario.db"); // Ruta de tu base de datos
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    // Método para agregar un nuevo producto
    public boolean agregarProducto(producto p) {
        try {
            if (!serieExiste(p.getSerie())) {
                dao.insertarProducto(p);
                System.out.println("Producto agregado exitosamente");
                return true;
            } else {
                return false; // Ya existe la serie
            }
        } catch (Exception e) {
            System.err.println("Error al agregar producto: " + e.getMessage());
            return false;
        }
    }

    // Método para modificar un producto
    public boolean modificarProducto(producto p, String nombreAnterior) {
        try {
            if (serieConNombreExiste(p.getNombre(), p.getSerie(), nombreAnterior)) {
                return false; // Ya existe producto con ese nombre y serie, no modificar
            } else {
                dao.actualizarProducto(p);
                dao.actualizarAsignacionesProducto(nombreAnterior, p.getNombre());
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error al modificar producto: " + e.getMessage());
            return false;
        }
    }
    
    // Método para eliminar un producto por ID
    public void eliminarProducto(int id) {
        try {
            dao.eliminarProducto(id); // Elimina el producto por ID
        } catch (Exception e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
        }
    }

    // Método para obtener todos los productos
    public List<producto> obtenerProductos() {
        try {
            return dao.obtenerTodosLosProductos();  // Obtiene todos los productos de la base de datos
        } catch (Exception e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            return null;
        }
    }

    // Método para cerrar la conexión con la base de datos
    public void cerrarConexion() {
        try {
            dao.cerrarConexion();  // Cierra la conexión con la base de datos
        } catch (Exception e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    // Método para verificar si un producto con la misma serie ya existe
    public boolean serieExiste(String serie) {
        String sql = "SELECT COUNT(*) FROM productos WHERE serie = ?";
        try (Connection conn = conectarBD();  // Usamos la conexión proporcionada por producto_config
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, serie);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;  // Si hay al menos uno, ya existe
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar serie: " + e.getMessage());
        }
        return false;
    }

    // Método para verificar si ya existe un producto con el mismo nombre y serie
    public boolean serieConNombreExiste(String nombre, String serie, String nombreAnterior) {
        String sql = "SELECT COUNT(*) FROM productos WHERE nombre = ? AND serie = ? AND nombre != ?";
        try (Connection conn = conectarBD();  // Usamos la conexión proporcionada por producto_config
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, serie);
            stmt.setString(3, nombreAnterior);  // Excluimos el producto que estamos modificando

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;  // Si hay al menos uno, ya existe un producto con ese nombre y serie
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar producto con nombre y serie: " + e.getMessage());
        }
        return false;
    }

    public void actualizarEstadoProducto(int id, String estado) {
        String sql = "UPDATE productos SET estado = ? WHERE id = ?";
        try (Connection conn = conectarBD();  // Usamos el método conectarBD() para obtener la conexión
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setInt(2, id);
            stmt.executeUpdate();  // Ejecuta la actualización
            System.out.println("Estado del producto actualizado con éxito.");
        } catch (SQLException e) {
            System.err.println("Error al actualizar el estado del producto: " + e.getMessage());
        }
    }    
    
}






