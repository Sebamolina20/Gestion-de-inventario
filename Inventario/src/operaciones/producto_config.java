package operaciones;

import modelo.producto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class producto_config {
    private Connection connection;

    public producto_config(String dbFile) {
        try {
            // Conexión a la base de datos SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            crearTablasSiNoExisten();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void crearTablasSiNoExisten() {
        // Crear la tabla productos si no existe
        String sqlProductos = "CREATE TABLE IF NOT EXISTS productos (" +
                      "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                      "nombre TEXT NOT NULL, " +
                      "tipo TEXT, " +
                      "serie TEXT, " +
                      "cantidad INTEGER, " +
                      "estado TEXT)";
        // Crear la tabla de ids eliminadas si no existe
        String sqlIdsEliminadas = "CREATE TABLE IF NOT EXISTS ids_eliminadas (" +
                                  "id INTEGER PRIMARY KEY)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlProductos);
            stmt.execute(sqlIdsEliminadas);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para insertar un nuevo producto, reutilizando IDs eliminadas si es posible
    public void insertarProducto(producto p) {
        int idProducto = obtenerIdDisponible();  // Intentamos obtener una ID reutilizable

        String sql = "INSERT INTO productos (id, nombre, tipo, serie, cantidad, estado) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idProducto);
            pstmt.setString(2, p.getNombre());
            pstmt.setString(3, p.getTipo());
            pstmt.setString(4, p.getSerie());
            pstmt.setInt(5, p.getCantidad());
            pstmt.setString(6, p.getEstado());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para actualizar un producto
    public void actualizarProducto(producto p) {
        String sql = "UPDATE productos SET nombre = ?, tipo = ?, serie = ?, cantidad = ?, estado = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, p.getNombre());
            pstmt.setString(2, p.getTipo());
            pstmt.setString(3, p.getSerie());
            pstmt.setInt(4, p.getCantidad());
            pstmt.setString(5, p.getEstado());
            pstmt.setInt(6, p.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para eliminar un producto por su ID y guardar la ID en la tabla de ids_eliminadas
    public void eliminarProducto(int id) {
        String sql = "DELETE FROM productos WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            // Guardar la ID eliminada en la tabla de ids_eliminadas
            guardarIdEliminada(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Método para obtener todos los productos
    public List<producto> obtenerTodosLosProductos() {
        List<producto> productos = new ArrayList<>();
        String sql = "SELECT id, nombre, tipo, serie, cantidad, estado FROM productos";
        try (Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
            int id = rs.getInt("id");
            String nombre = rs.getString("nombre");
            String tipo = rs.getString("tipo");
            String serie = rs.getString("serie");
            int cantidad = rs.getInt("cantidad");
            String estado = rs.getString("estado");
            producto p = new producto(id, nombre, tipo, serie, cantidad, estado);
            productos.add(p);}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }

    // Método para obtener una ID disponible reutilizable de la tabla ids_eliminadas
    private int obtenerIdDisponible() {
        String sql = "SELECT id FROM ids_eliminadas LIMIT 1";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int id = rs.getInt("id");

                // Eliminar la ID de la tabla ids_eliminadas para reutilizarla
                eliminarIdEliminada(id);
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return obtenerNuevoId();  // Si no hay IDs eliminadas, generamos un nuevo ID
    }

    // Método para eliminar una ID de la tabla ids_eliminadas
    private void eliminarIdEliminada(int id) {
        String sql = "DELETE FROM ids_eliminadas WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para guardar una ID eliminada en la tabla ids_eliminadas
    private void guardarIdEliminada(int id) {
        String sql = "INSERT INTO ids_eliminadas (id) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener un nuevo ID para los productos si no se encuentra un ID disponible
    private int obtenerNuevoId() {
        String sql = "SELECT MAX(id) FROM productos";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;  // Si no hay productos, asignamos el ID 1
    }

    // Cierra la conexión a la base de datos

    // Método estático para obtener solo los nombres de los productos
    public static List<String> obtenerNombresProductos() {
        List<String> nombres = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:inventario.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nombre FROM productos")) {
            while (rs.next()) {
                nombres.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nombres;
    }

    public void actualizarAsignacionesProducto(String nombreAnterior, String nombreNuevo) {
        String sql = "UPDATE asignaciones SET producto = ? WHERE producto = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nombreNuevo);
            pstmt.setString(2, nombreAnterior);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Se actualizaron " + rowsUpdated + " asignaciones con el nuevo nombre del producto.");
            } else {
                System.out.println("No se encontraron asignaciones que actualizar.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void actualizarEstadoProducto(int idProducto, String nuevoEstado) {
        String sql = "UPDATE productos SET estado = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idProducto);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean estaSerieDisponible(String nombreProducto, String numeroSerie) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
    
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:inventario.db");  // Ajusta si usas otra ruta
            String sql = "SELECT estado FROM productos WHERE nombre = ? AND numero_serie = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombreProducto);
            stmt.setString(2, numeroSerie);
            rs = stmt.executeQuery();
    
            if (rs.next()) {
                String estado = rs.getString("estado");
                return !estado.equalsIgnoreCase("Prestado");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    
        return false;
    }
    
    public boolean tieneStockDisponible(String nombreProducto) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
    
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:inventario.db");
            String sql = "SELECT cantidad, tipo FROM productos WHERE nombre = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombreProducto);
            rs = stmt.executeQuery();
    
            if (rs.next()) {
                int cantidad = rs.getInt("cantidad");
                String tipo = rs.getString("tipo");
    
                if ("cantidad".equalsIgnoreCase(tipo)) {
                    return cantidad > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    
        return false;
    }
    
    public String obtenerSerieDisponible(String nombreProducto) {
        String serieDisponible = null;
        String sql = "SELECT numero_serie FROM productos WHERE nombre = ? AND estado = 'Disponible' AND tipo = 'individual' LIMIT 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) { // Usa la conexión existente
            stmt.setString(1, nombreProducto);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    serieDisponible = rs.getString("numero_serie");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return serieDisponible;
    }    
    
    public void cerrarConexion() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}






