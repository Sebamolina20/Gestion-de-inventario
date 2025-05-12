package main;
import operaciones.producto_config;
import utility.IntegerFilter;
import modelo.asignacion;
import operaciones.asignacion_manager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AsignacionUI extends JFrame {
    private asignacion_manager asignacion_manager;
    private JTable table;
    private DefaultTableModel tableModel;

    public AsignacionUI() {
        asignacion_manager = new asignacion_manager();
        configurarVentana();
        configurarTabla();
        configurarBotones();
    }

    private void configurarVentana() {
        setTitle("Gestión de Asignaciones");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void configurarTabla() {
        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Nombre Profesor", "Producto", "Cantidad", "Fecha y Hora"}, 0
        );
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }
    private void configurarBotones() {
        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Agregar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnListar = new JButton("Listar");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnListar);
        add(panelBotones, BorderLayout.SOUTH);
        
        btnAgregar.addActionListener(e -> abrirDialogoAgregar());
        btnModificar.addActionListener(e -> modificarAsignacion());
        btnEliminar.addActionListener(e -> eliminarAsignacion());
        btnListar.addActionListener(e -> cargarAsignaciones());

        JPanel panelNavegacion = new JPanel();
        JButton btnVolverMenu = new JButton("Volver al Menú");
        JButton btnIrProductos = new JButton("Ir a Inventario");

        panelNavegacion.add(btnVolverMenu);
        panelNavegacion.add(btnIrProductos);

        btnVolverMenu.addActionListener(e -> {
            new Menu().setVisible(true);  // Abre el menú principal
            dispose();  // Cierra la ventana actual
        });

        // Acción del botón "Ir a Asignaciones"
        btnIrProductos.addActionListener(e -> {
            new InventarioUI().setVisible(true);  // Abre la ventana de Asignaciones
            dispose();  // Cierra la ventana actual
        });

        // Panel para filtros de búsqueda
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField campoBuscarNombre = new JTextField(10);
        JTextField campoBuscarProducto = new JTextField(10);
        JTextField campoBuscarFecha = new JTextField(10);
        JButton btnBuscar = new JButton("Buscar");

        panelFiltros.add(new JLabel("Nombre:"));
        panelFiltros.add(campoBuscarNombre);
        panelFiltros.add(new JLabel("Producto:"));
        panelFiltros.add(campoBuscarProducto);
        panelFiltros.add(new JLabel("Fecha:"));
        panelFiltros.add(campoBuscarFecha);
        panelFiltros.add(btnBuscar);

        Box panelSuperior = Box.createVerticalBox();
        panelSuperior.add(panelNavegacion);  // Menú y Productos
        panelSuperior.add(panelFiltros);     // Filtros
        add(panelSuperior, BorderLayout.NORTH);

        btnBuscar.addActionListener(e -> {
            String nombreTexto = campoBuscarNombre.getText().trim().toLowerCase();
            String productoTexto = campoBuscarProducto.getText().trim();
            String fechaTexto = campoBuscarFecha.getText().trim();
        
            List<asignacion> asignaciones = asignacion_manager.obtenerAsignaciones();
            tableModel.setRowCount(0); // Limpiar la tabla
        
            for (asignacion a : asignaciones) {
                boolean coincideNombre = nombreTexto.isEmpty() || a.getNombreProfesor().toLowerCase().contains(nombreTexto);
                boolean coincideProducto = productoTexto.isEmpty() || a.getProducto().contains(productoTexto);
                boolean coincideFecha = fechaTexto.isEmpty() || a.getFechaHora().contains(fechaTexto);
                
                if (coincideNombre && coincideProducto && coincideFecha) {
                    tableModel.addRow(new Object[]{
                        a.getId(),
                        a.getNombreProfesor(),
                        a.getProducto(),
                        a.getCantidad(),
                        a.getFechaHora()
                    });
                }
            }
        });        

        JButton btnLimpiar = new JButton("Limpiar filtros");
        panelFiltros.add(btnLimpiar);
        btnLimpiar.addActionListener(e -> {
            campoBuscarNombre.setText("");
            campoBuscarProducto.setText("");
            campoBuscarFecha.setText("");
            // Volver a mostrar todas las asignaciones
            List<asignacion> asignaciones = asignacion_manager.obtenerAsignaciones();
            tableModel.setRowCount(0);
            for (asignacion a : asignaciones) {
                tableModel.addRow(new Object[]{
                    a.getId(),
                    a.getNombreProfesor(),
                    a.getProducto(),
                    a.getCantidad(),
                    a.getFechaHora()
                });
            }
        });
        
    }

    private void cargarAsignaciones() {
        List<asignacion> asignaciones = asignacion_manager.obtenerAsignaciones();
        tableModel.setRowCount(0); // Limpiar la tabla antes de cargar los datos
        for (asignacion a : asignaciones) {
            // Asegúrate de que la fecha esté en formato correcto (ya que se está asignando automáticamente)
            tableModel.addRow(new Object[]{
                a.getId(),
                a.getNombreProfesor(),
                a.getProducto(),
                a.getCantidad(),
                a.getFechaHora()  // Se muestra automáticamente, ya que se asigna en el momento de la creación
            });
        }
    }    

    private void abrirDialogoAgregar() {
    JDialog dialog = new JDialog(this, "Agregar Asignación", true);
    dialog.setSize(300, 200);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new GridLayout(4, 2));

    JTextField campoNombreProfesor = new JTextField();
    JComboBox<String> comboProducto = new JComboBox<>();
    JTextField campoCantidad = new JTextField();
    ((AbstractDocument) campoCantidad.getDocument()).setDocumentFilter(new IntegerFilter());

    // Llenar combo con productos
    List<String> productos = producto_config.obtenerNombresProductos();
    for (String nombre : productos) {
        comboProducto.addItem(nombre);
    }

    dialog.add(new JLabel("Nombre Profesor:"));
    dialog.add(campoNombreProfesor);
    dialog.add(new JLabel("Producto:"));
    dialog.add(comboProducto);
    dialog.add(new JLabel("Cantidad:"));
    dialog.add(campoCantidad);

    JButton btnGuardar = new JButton("Guardar");
    JButton btnCancelar = new JButton("Cancelar");

    dialog.add(btnGuardar);
    dialog.add(btnCancelar);

    btnGuardar.addActionListener(ev -> {
        String nombreProfesor = campoNombreProfesor.getText().trim();
        String producto = (String) comboProducto.getSelectedItem();
        String cantidadStr = campoCantidad.getText().trim();
    
        if (!nombreProfesor.isEmpty() && producto != null && !cantidadStr.isEmpty()) {
            try {
                int cantidad = Integer.parseInt(cantidadStr);
    
                // Obtener la fecha y hora actual
                String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    
                // Verificar si el producto es individual y ya está prestado
                boolean productoDisponible = asignacion_manager.esProductoDisponible(producto);
    
                if (productoDisponible) {
                    // Crear el objeto asignacion si el producto está disponible
                    asignacion nuevaAsignacion = new asignacion(0, nombreProfesor, producto, cantidad, fechaHora);  // Crear el objeto asignacion
                    asignacion_manager.agregarAsignacion(nuevaAsignacion);  // Pasar el objeto
                    cargarAsignaciones();  // Cargar las asignaciones nuevamente
                    dialog.dispose();  // Cerrar el diálogo
                } else {
                    // Si el producto está prestado, mostrar el mensaje
                    JOptionPane.showMessageDialog(dialog, "El producto ya está prestado y no puede ser asignado.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "La cantidad debe ser un número entero.");
            }
        } else {
            JOptionPane.showMessageDialog(dialog, "Todos los campos son obligatorios.");
        }
    });
    
    btnCancelar.addActionListener(ev -> dialog.dispose());
    
    dialog.setVisible(true);    
}
private void modificarAsignacion() {
    int fila = table.getSelectedRow();
    if (fila != -1) {
        // Obtener los valores de la fila seleccionada
        int id = (int) table.getValueAt(fila, 0);
        String nombreProfesor = (String) table.getValueAt(fila, 1);
        String producto = (String) table.getValueAt(fila, 2);
        int cantidad = (int) table.getValueAt(fila, 3);

        // Crear un cuadro de diálogo para modificar la asignación
        JDialog dialog = new JDialog(this, "Modificar Asignación", true);
        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridLayout(4, 2));

        JTextField campoNombreProfesor = new JTextField(nombreProfesor);
        JComboBox<String> comboProducto = new JComboBox<>();
        JTextField campoCantidad = new JTextField(String.valueOf(cantidad));
        ((AbstractDocument) campoCantidad.getDocument()).setDocumentFilter(new IntegerFilter());

        // Llenar combo con productos
        List<String> productos = producto_config.obtenerNombresProductos();
        for (String nombre : productos) {
            comboProducto.addItem(nombre);
        }

        // Seleccionar el producto previamente asignado
        comboProducto.setSelectedItem(producto);

        dialog.add(new JLabel("Nombre Profesor:"));
        dialog.add(campoNombreProfesor);
        dialog.add(new JLabel("Producto:"));
        dialog.add(comboProducto);
        dialog.add(new JLabel("Cantidad:"));
        dialog.add(campoCantidad);

        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        dialog.add(btnGuardar);
        dialog.add(btnCancelar);

        // Acción del botón "Guardar"
        btnGuardar.addActionListener(ev -> {
            String nombreProfesorModificado = campoNombreProfesor.getText().trim();
            String productoSeleccionado = (String) comboProducto.getSelectedItem();
            String cantidadStr = campoCantidad.getText().trim();

            if (!nombreProfesorModificado.isEmpty() && productoSeleccionado != null && !cantidadStr.isEmpty()) {
                try {
                    int cantidadModificada = Integer.parseInt(cantidadStr);

                    // Actualizar la fecha con la hora actual si es necesario.
                    String nuevaFechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

                    // Crear el objeto asignacion con los nuevos valores, incluyendo la nueva fecha
                    asignacion asignacionModificada = new asignacion(id, nombreProfesorModificado, productoSeleccionado, cantidadModificada, nuevaFechaHora);

                    // Llamar al método de modificación
                    asignacion_manager.modificarAsignacion(asignacionModificada);

                    // Actualizar la tabla
                    cargarAsignaciones();
                    dialog.dispose(); // Cerrar el cuadro de diálogo
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "La cantidad debe ser un número entero.");
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Todos los campos son obligatorios.");
            }
        });

        // Acción del botón "Cancelar"
        btnCancelar.addActionListener(ev -> dialog.dispose());

        dialog.setVisible(true);
    } else {
        JOptionPane.showMessageDialog(this, "Seleccione una fila para modificar.");
    }
}
    
    private void eliminarAsignacion() {
        int fila = table.getSelectedRow();
        if (fila != -1) {
            // Preguntar al usuario si está seguro de eliminar
            int respuesta = JOptionPane.showConfirmDialog(this,
                    "¿Estás seguro de que quieres eliminar esta asignación?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                    
    
            if (respuesta == JOptionPane.YES_OPTION) {
                // Obtener el ID de la asignación seleccionada
                int id = (int) table.getValueAt(fila, 0);
                asignacion_manager.eliminarAsignacion(id); // Eliminar la asignación
                cargarAsignaciones(); // Actualizar la tabla después de eliminar
                JOptionPane.showMessageDialog(this, "Asignación eliminada correctamente.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para eliminar.");
        }
    }    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AsignacionUI().setVisible(true));
    }
    

}






