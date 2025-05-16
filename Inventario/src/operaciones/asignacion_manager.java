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
    private List<asignacion> listaAsignaciones;

    public asignacion_manager() {
        this.dao = new asignacion_config("inventario.db");
        listaAsignaciones = new ArrayList<>();
        // cargar desde BD o iniciar vacía

    }

    public void agregarAsignacion(asignacion a) {
        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);

            // Insertar la asignación primero
            dao.insertarAsignacion(a);

            // Obtener tipo del producto y datos relevantes
            String sql = "SELECT tipo, cantidad, estado FROM productos WHERE nombre = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, a.getProducto());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String tipo = rs.getString("tipo");
                        int cantidadProducto = rs.getInt("cantidad");

                        if (tipo.equalsIgnoreCase("individual")) {
                            // Para productos individuales verificamos la serie
                            boolean asignado = asignarProductoPorSerie(conn, a.getProducto(), a.getSerie());
                            if (!asignado) {
                                System.out.println("La serie " + a.getSerie() + " ya está prestada o no disponible.");
                                conn.rollback();
                                return;
                            }
                        } else if (tipo.equalsIgnoreCase("cantidad")) {
                            // Si es por cantidad, verificar stock
                            if (cantidadProducto >= a.getCantidad()) {
                                int nuevaCantidad = cantidadProducto - a.getCantidad();
                                String nuevoEstado = (nuevaCantidad <= 0) ? "Sin stock" : "Disponible";
                                actualizarCantidadYEstado(conn, a.getProducto(), nuevaCantidad, nuevoEstado);
                            } else {
                                System.out.println("No hay stock suficiente para el producto " + a.getProducto());
                                conn.rollback();
                                return;
                            }
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try (Connection conn = conectar()) {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    public void modificarAsignacion(asignacion nuevaAsignacion) {
    try (Connection conn = conectar()) {
        conn.setAutoCommit(false);

        // Obtener datos anteriores de la asignación
        String sqlPrev = "SELECT producto, cantidad, serie FROM asignaciones WHERE id = ?";
        String productoAntiguo = null;
        int cantidadAntigua = 0;
        String serieAntigua = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sqlPrev)) {
            pstmt.setInt(1, nuevaAsignacion.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    productoAntiguo = rs.getString("producto");
                    cantidadAntigua = rs.getInt("cantidad");
                    serieAntigua = rs.getString("serie");
                } else {
                    System.out.println("Asignación no encontrada.");
                    return;
                }
            }
        }

        // Obtener tipo de producto
        String tipo = null;
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT tipo FROM productos WHERE nombre = ?")) {
            pstmt.setString(1, productoAntiguo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    tipo = rs.getString("tipo");
                }
            }
        }

        if (tipo == null) {
            System.out.println("Tipo de producto no encontrado.");
            conn.rollback();
            return;
        }

        // Si es individual, manejar series
        if (tipo.equalsIgnoreCase("individual")) {
            if (!serieAntigua.equals(nuevaAsignacion.getSerie())) {
                // Liberar serie antigua si no está asignada a otra persona
                if (!existeAsignacionActiva(conn, productoAntiguo, serieAntigua)) {
                    actualizarEstadoProductoPorSerie(conn, productoAntiguo, serieAntigua, "Disponible");
                }

                // Intentar asignar la nueva serie
                if (!asignarProductoPorSerie(conn, nuevaAsignacion.getProducto(), nuevaAsignacion.getSerie())) {
                    System.out.println("La nueva serie ya está prestada.");
                    conn.rollback();
                    return;
                }
            }
        }

        // Si es por cantidad, ajustar el stock
        else if (tipo.equalsIgnoreCase("cantidad")) {
            int diferencia = nuevaAsignacion.getCantidad() - cantidadAntigua;
            int stockActual = obtenerCantidad(conn, nuevaAsignacion.getProducto());

            if (diferencia > 0) {
                if (stockActual < diferencia) {
                    System.out.println("No hay suficiente stock.");
                    conn.rollback();
                    return;
                }
                actualizarCantidadYEstado(conn, nuevaAsignacion.getProducto(), stockActual - diferencia, "Disponible");
            } else if (diferencia < 0) {
                actualizarCantidadYEstado(conn, nuevaAsignacion.getProducto(), stockActual + Math.abs(diferencia), "Disponible");
            }
        }

        // Actualizar la asignación
        dao.actualizarAsignacion(conn, nuevaAsignacion);
        conn.commit();

    } catch (SQLException e) {
        e.printStackTrace();
        try (Connection conn = conectar()) {
            if (conn != null) conn.rollback();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}

    public void eliminarAsignacion(int id) {
        try (Connection conn = conectar()) {
            conn.setAutoCommit(false);

            // Obtener producto, cantidad y serie de la asignación
            String sqlSelect = "SELECT producto, cantidad, serie FROM asignaciones WHERE id = ?";
            String producto = null;
            int cantidadAsignada = 0;
            String serieAsignada = null;

            try (PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        producto = rs.getString("producto");
                        cantidadAsignada = rs.getInt("cantidad");
                        serieAsignada = rs.getString("serie");
                    }
                }
            }

            if (producto == null) {
                System.out.println("No se encontró la asignación para eliminar.");
                return;
            }

            // Consultar tipo producto
            String sqlTipo = "SELECT tipo FROM productos WHERE nombre = ?";
            String tipo = null;
            try (PreparedStatement pstmt2 = conn.prepareStatement(sqlTipo)) {
                pstmt2.setString(1, producto);
                try (ResultSet rs2 = pstmt2.executeQuery()) {
                    if (rs2.next()) {
                        tipo = rs2.getString("tipo");
                    }
                }
            }

            if (tipo == null) {
                System.out.println("Producto no encontrado para la asignación.");
                return;
            }

            if (tipo.equalsIgnoreCase("individual")) {
                // Actualizar estado de la serie a Disponible
                actualizarEstadoProductoPorSerie(conn, producto, serieAsignada, "Disponible");
            } else if (tipo.equalsIgnoreCase("cantidad")) {
                // Sumar stock de cantidad
                sumarCantidadProductoConConexion(conn, producto, cantidadAsignada);
            }

            // Eliminar la asignación
            String sqlDelete = "DELETE FROM asignaciones WHERE id = ?";
            try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDelete)) {
                pstmtDelete.setInt(1, id);
                pstmtDelete.executeUpdate();
            }

            // Verificar si quedan asignaciones del producto
            String sqlCheck = "SELECT COUNT(*) FROM asignaciones WHERE producto = ?";
            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
                pstmtCheck.setString(1, producto);
                try (ResultSet rsCheck = pstmtCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        int asignacionesRestantes = rsCheck.getInt(1);
                        if (asignacionesRestantes == 0) {
                            // Actualizar estado producto general a Disponible si no quedan asignaciones
                            String sqlEstado = "UPDATE productos SET estado = 'Disponible' WHERE nombre = ?";
                            try (PreparedStatement pstmtEstado = conn.prepareStatement(sqlEstado)) {
                                pstmtEstado.setString(1, producto);
                                pstmtEstado.executeUpdate();
                            }
                        }
                    }
                }
            }

            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            try (Connection conn = conectar()) {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public List<asignacion> obtenerAsignaciones() {
        return dao.obtenerTodasLasAsignaciones();
    }

    public List<asignacion> filtrarPorFecha(String fecha) {
        List<asignacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM asignaciones WHERE fechaHora LIKE ?";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fecha + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Connection conectar() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:inventario.db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
        return conn;
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

    public boolean esProductoIndividualDisponible(String producto, String serie) {
    // Ejemplo: recorres la lista de asignaciones para ver si ya existe ese producto + serie asignado
    for (asignacion a : listaAsignaciones) { // listaAsignaciones es la lista donde guardas las asignaciones actuales
        if (a.getProducto().equals(producto) && serie.equals(a.getSerie())) {
            // Ya está asignado
            return false;
        }
    }
    return true; // No está asignado aún, está disponible
}

    private void actualizarEstadoProductoPorSerie(Connection conn, String producto, String serie, String estado) throws SQLException {
        String sql = "UPDATE productos SET estado = ? WHERE nombre = ? AND serie = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estado);
            pstmt.setString(2, producto);
            pstmt.setString(3, serie);
            pstmt.executeUpdate();
        }
    }

    public boolean existeAsignacionActiva(Connection conn, String producto, String serie) throws SQLException {
    String sql = "SELECT COUNT(*) FROM asignaciones WHERE producto = ? AND serie = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, producto);
        pstmt.setString(2, serie);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
    }
    return false;
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

    public boolean esProductoDisponible(String producto, int cantidad) {
        try (Connection conn = conectar()) {
            String sql = "SELECT tipo, cantidad FROM productos WHERE nombre = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, producto);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        String tipo = rs.getString("tipo");
                        int cantidadDisponible = rs.getInt("cantidad");

                        if (tipo.equalsIgnoreCase("individual")) {
                            // Para producto individual, verificar si alguna serie está disponible
                            List<String> series = obtenerSeriesDisponibles(producto);
                            return !series.isEmpty();
                        }

                        if (tipo.equalsIgnoreCase("cantidad") && cantidadDisponible >= cantidad) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean asignarProductoPorSerie(Connection conn, String producto, String serie) throws SQLException {
        String sqlVerificar = "SELECT estado FROM productos WHERE nombre = ? AND serie = ?";
        String sqlActualizar = "UPDATE productos SET estado = 'Prestado' WHERE nombre = ? AND serie = ?";

        try (PreparedStatement pstmtVerificar = conn.prepareStatement(sqlVerificar)) {
            pstmtVerificar.setString(1, producto);
            pstmtVerificar.setString(2, serie);
            try (ResultSet rs = pstmtVerificar.executeQuery()) {
                if (rs.next()) {
                    String estado = rs.getString("estado");
                    if ("Disponible".equalsIgnoreCase(estado)) {
                        try (PreparedStatement pstmtActualizar = conn.prepareStatement(sqlActualizar)) {
                            pstmtActualizar.setString(1, producto);
                            pstmtActualizar.setString(2, serie);
                            pstmtActualizar.executeUpdate();
                            return true;
                        }
                    }
                }
            }
        }
        return false; // La serie está prestada o no existe
    }

    public List<String> obtenerSeriesDisponibles(String producto) {
        List<String> seriesDisponibles = new ArrayList<>();
        String sql = "SELECT serie FROM productos WHERE nombre = ? AND estado = 'Disponible'";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, producto);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    seriesDisponibles.add(rs.getString("serie"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return seriesDisponibles;
    }

    public String obtenerSerieAnterior(int idAsignacion) {
    String serie = "";
    try (Connection conn = DriverManager.getConnection("jdbc:sqlite:inventario.db");
         PreparedStatement stmt = conn.prepareStatement("SELECT serie FROM asignaciones WHERE id = ?")) {
        stmt.setInt(1, idAsignacion);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            serie = rs.getString("serie");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return serie;
}

public boolean existeAsignacionPorSerie(Connection conn, String producto, String serie) throws SQLException {
    String sql = "SELECT COUNT(*) FROM asignaciones WHERE producto = ? AND serie = ?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, producto);
        stmt.setString(2, serie);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    }
    return false;
}

private int obtenerCantidad(Connection conn, String producto) throws SQLException {
    String sql = "SELECT cantidad FROM productos WHERE nombre = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, producto);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("cantidad");
            }
        }
    }
    return 0;
}

}


