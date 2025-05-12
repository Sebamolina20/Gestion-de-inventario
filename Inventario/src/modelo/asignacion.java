package modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class asignacion {
    private int id;
    private String nombreProfesor;
    private String producto;
    private int cantidad;
    private String fechaHora;

    // Constructor con ID y fechaHora
    public asignacion(int id, String nombreProfesor, String producto, int cantidad, String fechaHora) {
        this.id = id;
        this.nombreProfesor = nombreProfesor;
        this.producto = producto;
        this.cantidad = cantidad;
        this.fechaHora = fechaHora;
    }

    // Constructor sin ID (útil para insertar nuevos datos)
    // Establecer la fecha y hora actual cuando se crea la asignación
    public asignacion(String nombreProfesor, String producto, int cantidad) {
        this.nombreProfesor = nombreProfesor;
        this.producto = producto;
        this.cantidad = cantidad;
        this.fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public String getNombreProfesor() {
        return nombreProfesor;
    }

    public void setNombreProfesor(String nombreProfesor) {
        this.nombreProfesor = nombreProfesor;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }
}




