package operaciones;

import modelo.asignacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

public class asignacion_manager {
    private asignacion_config dao;

    public asignacion_manager() {
        this.dao = new asignacion_config("inventario.db");
    }

    public void agregarAsignacion(asignacion a) {
        // 1. Insertar la asignación
        dao.insertarAsignacion(a);
    
        // Usamos transacciones para garantizar que todo se ejecute correctamente
        try (Connection conn = conectar()) {
            // Inicia una transacción
            conn.setAutoCommit(false);  // Desactivamos el autocommit para manejar manualmente la transacción
            
            String sql = "SELECT tipo, cantidad, estado FROM productos WHERE nombre = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, a.getProducto());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String tipo = rs.getString("tipo");
                        int cantidadProducto = rs.getInt("cantidad");
                        String estadoActual = rs.getString("estado");
    
                        if (tipo.equalsIgnoreCase("individual")) {
                            // Si es individual y está disponible, cambiar a "Prestado"
                            if (estadoActual.equalsIgnoreCase("Disponible")) {
                                actualizarEstadoProducto(conn, a.getProducto(), "Prestado");
                            } else {
                                System.out.println("Este producto ya está prestado.");
                                conn.rollback();  // Revertimos los cambios si no se puede asignar
                                return;  // Salimos del método si no se puede asignar
                            }
                        } else if (tipo.equalsIgnoreCase("cantidad")) {
                            // Si es por cantidad, verificar si hay suficiente stock
                            if (cantidadProducto >= a.getCantidad()) {
                                int nuevaCantidad = cantidadProducto - a.getCantidad();
                                String nuevoEstado = (nuevaCantidad <= 0) ? "Sin stock" : "Disponible";
                                actualizarCantidadYEstado(conn, a.getProducto(), nuevaCantidad, nuevoEstado);
                            } else {
                                System.out.println("No hay stock suficiente.");
                                conn.rollback();  // Revertimos los cambios si no hay suficiente stock
                                return;  // Salimos del método si no se puede asignar
                            }
                        }
                    }
                }
                // Si todo ha ido bien, hacemos commit para confirmar la transacción
                conn.commit();
            } catch (SQLException e) {
                // En caso de error, hacemos rollback
                conn.rollback();
                e.printStackTrace();
            } finally {
                // Aseguramos que la conexión vuelva a su estado por defecto
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }    

    public void modificarAsignacion(asignacion a) {
        dao.actualizarAsignacion(a);  // Ahora pasa el objeto asignacion directamente
    }

    public void eliminarAsignacion(int id) {
        String producto = "";
        try (Connection conn = conectar()) {
            conn.setAutoCommit(false); // Inicia la transacción
    
            // 1. Obtener producto y cantidad de la asignación
            String sqlSelect = "SELECT producto, cantidad FROM asignaciones WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
    
                if (rs.next()) {
                    producto = rs.getString("producto");
                    int cantidadAsignada = rs.getInt("cantidad");
    
                    // 2. Consultar tipo de producto
                    String sqlTipo = "SELECT tipo FROM productos WHERE nombre = ?";
                    try (PreparedStatement pstmt2 = conn.prepareStatement(sqlTipo)) {
                        pstmt2.setString(1, producto);
                        ResultSet rs2 = pstmt2.executeQuery();
    
                        if (rs2.next()) {
                            String tipo = rs2.getString("tipo");
    
                            if (tipo.equals("individual")) {
                                // Si es individual ➔ cambiar estado a "Disponible"
                                actualizarEstadoProductoConConexion(conn, producto, "Disponible");
                            } else if (tipo.equals("cantidad")) {
                                // Si es por cantidad ➔ sumar stock
                                sumarCantidadProductoConConexion(conn, producto, cantidadAsignada);
                            }
                        }
                    }
                }
            }
    
            // 3. Eliminar la asignación
            String sqlDelete = "DELETE FROM asignaciones WHERE id = ?";
            try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDelete)) {
                pstmtDelete.setInt(1, id);
                pstmtDelete.executeUpdate();
            }
    
            // 4. Verificar si el producto aún tiene asignaciones y actualizar estado si es necesario
            String sqlCheck = "SELECT COUNT(*) FROM asignaciones WHERE producto = ?";
            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
                pstmtCheck.setString(1, producto);
                ResultSet rsCheck = pstmtCheck.executeQuery();
                if (rsCheck.next()) {
                    int asignacionesRestantes = rsCheck.getInt(1);
                    if (asignacionesRestantes == 0) {
                        // Si no hay más asignaciones, actualizar el estado del producto a "Disponible"
                        String sqlEstado = "UPDATE productos SET estado = 'Disponible' WHERE nombre = ?";
                        try (PreparedStatement pstmtEstado = conn.prepareStatement(sqlEstado)) {
                            pstmtEstado.setString(1, producto);
                            pstmtEstado.executeUpdate();
                        }
                    }
                }
            }
    
            conn.commit(); // Confirma todo si no hubo errores
        } catch (SQLException e) {
            e.printStackTrace();
            try (Connection conn = conectar()) {
                if (conn != null) {
                    conn.rollback(); // Revierte cambios si falló algo
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }    
    
    public List<asignacion> obtenerAsignaciones() {
        return dao.obtenerTodasLasAsignaciones();
    }

    private Connection conectar() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:inventario.db";  // Asegúrate de tener la ruta correcta de tu base de datos
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return conn;
    }

    public List<asignacion> filtrarPorFecha(String fecha) {
    List<asignacion> lista = new ArrayList<>();
    String sql = "SELECT * FROM asignaciones WHERE fechaHora LIKE ?";

    try (Connection conn = conectar();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, fecha + "%"); // Para incluir la hora también
        ResultSet rs = pstmt.executeQuery();

        while (rs.next()) {
            asignacion a = new asignacion(
                rs.getInt("id"),
                rs.getString("nombreProfesor"),
                rs.getString("producto"),
                rs.getInt("cantidad"),
                rs.getString("fechaHora")
            );
            lista.add(a);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return lista;
}

private void actualizarEstadoProducto(Connection conn, String nombreProducto, String nuevoEstado) throws SQLException {
    String sql = "UPDATE productos SET estado = ? WHERE nombre = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, nuevoEstado);
        pstmt.setString(2, nombreProducto);
        pstmt.executeUpdate();
    }
}

    private void actualizarCantidadYEstado(Connection conn, String nombreProducto, int nuevaCantidad, String nuevoEstado) throws SQLException {
        String sql = "UPDATE productos SET cantidad = ?, estado = ? WHERE nombre = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, nuevaCantidad);
            pstmt.setString(2, nuevoEstado);
            pstmt.setString(3, nombreProducto);
            pstmt.executeUpdate();
        }
    }

    private void actualizarEstadoProductoConConexion(Connection conn, String producto, String estado) throws SQLException {
        String sql = "UPDATE productos SET estado = ? WHERE nombre = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estado);
            pstmt.setString(2, producto);
            pstmt.executeUpdate();
        }
    }
    
    private void sumarCantidadProductoConConexion(Connection conn, String producto, int cantidad) throws SQLException {
        String sql = "UPDATE productos SET cantidad = cantidad + ?, estado = CASE WHEN cantidad + ? > 0 THEN 'Disponible' ELSE 'Sin stock' END WHERE nombre = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cantidad);
            pstmt.setInt(2, cantidad);
            pstmt.setString(3, producto);
            pstmt.executeUpdate();
        }
    }

    public boolean esProductoDisponible(String producto) {
        try (Connection conn = conectar()) {
            String sql = "SELECT estado FROM productos WHERE nombre = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, producto);
                ResultSet rs = pstmt.executeQuery();
    
                if (rs.next()) {
                    String estado = rs.getString("estado");
                    // Verificar si el producto está disponible
                    return estado.equalsIgnoreCase("Disponible");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }    
    
}

