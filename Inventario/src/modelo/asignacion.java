package modelo;

public class asignacion {
    private int id;
    private String nombreProfesor;
    private String producto;
    private int cantidad;
    private String fechaHora;
    private String serie;  // Agregamos el campo serie

    // Constructor con los parámetros necesarios
    public asignacion(int id, String nombreProfesor, String producto, int cantidad, String fechaHora) {
        this.id = id;
        this.nombreProfesor = nombreProfesor;
        this.producto = producto;
        this.cantidad = cantidad;
        this.fechaHora = fechaHora;
        this.serie = null;  // Inicializamos la serie como null por defecto
    }

    // Constructor con todos los parámetros, incluyendo serie
    public asignacion(int id, String nombreProfesor, String producto, int cantidad, String fechaHora, String serie) {
        this.id = id;
        this.nombreProfesor = nombreProfesor;
        this.producto = producto;
        this.cantidad = cantidad;
        this.fechaHora = fechaHora;
        this.serie = serie;  // Asignamos la serie si está presente
    }

    // Métodos getter y setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }
}





