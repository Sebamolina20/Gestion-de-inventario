package modelo;

public class producto {
    private int id;
    private String nombre;
    private String tipo;
    private String serie;
    private int cantidad;
    private String estado;

    // Constructor original sin estado
    public producto(String nombre, String tipo, String serie, int cantidad) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.serie = serie;
        this.cantidad = cantidad;
        this.estado = "Disponible";  // Por defecto, el estado es "disponible"
    }

    // Constructor con ID sin estado (para modificar producto)
    public producto(int id, String nombre, String tipo, String serie, int cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.serie = serie;
        this.cantidad = cantidad;
        this.estado = "Disponible";  // Por defecto, el estado es "disponible"
    }

    // Constructor con estado (para lectura desde DB o uso interno)
    public producto(int id, String nombre, String tipo, String serie, int cantidad, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.serie = serie;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    // Constructor con estado y sin ID
    public producto(String nombre, String tipo, String serie, int cantidad, String estado) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.serie = serie;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}






