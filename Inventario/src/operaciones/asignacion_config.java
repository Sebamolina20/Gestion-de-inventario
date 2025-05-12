package operaciones;

import modelo.asignacion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class asignacion_config {
    private Connection connection;

    public asignacion_config(String dbFile) {
        try {
            // Conexión a la base de datos SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            crearTablasSiNoExisten();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void crearTablasSiNoExisten() {
        // Crear la tabla asignaciones si no existe
        String sqlAsignaciones = "CREATE TABLE IF NOT EXISTS asignaciones (" +
                                  "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                  "nombreProfesor TEXT NOT NULL, " +
                                  "producto TEXT NOT NULL, " +
                                  "cantidad INTEGER NOT NULL, " +
                                  "fechaHora TEXT NOT NULL)";  // Agregar la columna fechaHora
        // Crear la tabla de ids eliminadas si no existe
        String sqlIdsEliminadas = "CREATE TABLE IF NOT EXISTS ids_asignaciones_eliminadas (" +
                                  "id INTEGER PRIMARY KEY)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlAsignaciones);
            stmt.execute(sqlIdsEliminadas);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }    

    // Método para insertar una nueva asignación, reutilizando IDs eliminadas si es posible
    public synchronized void insertarAsignacion(asignacion a) {
        int idAsignacion = obtenerIdDisponible();  // Intentamos obtener una ID reutilizable
    
        // 🔸 Obtener la fecha y hora actual en formato dd-MM-yy HH:mm
        String fechaHoraActual = new java.text.SimpleDateFormat("dd-MM-yy HH:mm").format(new java.util.Date());
    
        // 🔸 Ahora agregamos fechaHora al INSERT
        String sql = "INSERT INTO asignaciones (id, nombreProfesor, producto, cantidad, fechaHora) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, idAsignacion);
            pstmt.setString(2, a.getNombreProfesor());
            pstmt.setString(3, a.getProducto());
            pstmt.setInt(4, a.getCantidad());
            pstmt.setString(5, fechaHoraActual); // Nueva línea para insertar fecha y hora
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para actualizar una asignación
    // Método para actualizar una asignación (incluyendo fechaHora)
public void actualizarAsignacion(asignacion a) {
    String sql = "UPDATE asignaciones SET nombreProfesor = ?, producto = ?, cantidad = ?, fechaHora = ? WHERE id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        pstmt.setString(1, a.getNombreProfesor());
        pstmt.setString(2, a.getProducto());
        pstmt.setInt(3, a.getCantidad());
        pstmt.setString(4, a.getFechaHora()); // Aquí actualizas la fecha y hora
        pstmt.setInt(5, a.getId());
        pstmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    // Método para eliminar una asignación por su ID y guardar la ID en la tabla de ids_asignaciones_eliminadas
    public void eliminarAsignacion(int id) {
        String sql = "DELETE FROM asignaciones WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            // Guardar la ID eliminada en la tabla de ids_asignaciones_eliminadas
            guardarIdEliminada(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener todos los registros de asignaciones
    public List<asignacion> obtenerTodasLasAsignaciones() {
        List<asignacion> asignaciones = new ArrayList<>();
        String sql = "SELECT id, nombreProfesor, producto, cantidad, fechaHora FROM asignaciones";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nombreProfesor = rs.getString("nombreProfesor");
                String producto = rs.getString("producto");
                int cantidad = rs.getInt("cantidad");
                String fechaHora = rs.getString("fechaHora");  // Obtener la fecha y hora
                asignacion a = new asignacion(id, nombreProfesor, producto, cantidad, fechaHora); // Crear la asignación con fechaHora
                asignaciones.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return asignaciones;
    }
    

    // Método para obtener una ID disponible reutilizable de la tabla ids_asignaciones_eliminadas
    private int obtenerIdDisponible() {
        String sql = "SELECT id FROM ids_asignaciones_eliminadas LIMIT 1";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int id = rs.getInt("id");

                // Eliminar la ID de la tabla ids_asignaciones_eliminadas para reutilizarla
                eliminarIdEliminada(id);
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return obtenerNuevoId();  // Si no hay IDs eliminadas, generamos un nuevo ID
    }

    // Método para eliminar una ID de la tabla ids_asignaciones_eliminadas
    private void eliminarIdEliminada(int id) {
        String sql = "DELETE FROM ids_asignaciones_eliminadas WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para guardar una ID eliminada en la tabla ids_asignaciones_eliminadas
    private void guardarIdEliminada(int id) {
        String sql = "INSERT INTO ids_asignaciones_eliminadas (id) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener un nuevo ID para las asignaciones si no se encuentra un ID disponible
    private int obtenerNuevoId() {
        String sql = "SELECT MAX(id) FROM asignaciones";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;  // Si no hay asignaciones, asignamos el ID 1
    }
    
    // Cierra la conexión a la base de datos
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


