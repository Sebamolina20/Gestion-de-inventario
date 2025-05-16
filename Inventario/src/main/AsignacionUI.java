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
            new Object[]{"ID", "Nombre Profesor", "Producto", "Cantidad", "Fecha y Hora", "Serie"}, 0
        );
        table = new JTable(tableModel);

        table.getColumnModel().getColumn(5).setMinWidth(0);
        table.getColumnModel().getColumn(5).setMaxWidth(0);
        table.getColumnModel().getColumn(5).setWidth(0);
        table.getColumnModel().getColumn(5).setPreferredWidth(0);

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
                a.getFechaHora(),
                a.getSerie(),  // Se muestra automáticamente, ya que se asigna en el momento de la creación
            });
        }
    }    

    private void abrirDialogoAgregar() {
    JDialog dialog = new JDialog(this, "Agregar Asignación", true);
    dialog.setSize(350, 250);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new GridLayout(6, 2, 5, 5)); // Más espacio entre componentes

    JTextField campoNombreProfesor = new JTextField();
    JComboBox<String> comboProducto = new JComboBox<>();
    JTextField campoCantidad = new JTextField();
    JComboBox<String> comboSerie = new JComboBox<>();  // <-- cambio aquí

    ((AbstractDocument) campoCantidad.getDocument()).setDocumentFilter(new IntegerFilter());

    // Cargar productos en combo box
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

    dialog.add(new JLabel("Serie:"));
    dialog.add(comboSerie);

    JButton btnGuardar = new JButton("Guardar");
    JButton btnCancelar = new JButton("Cancelar");
    dialog.add(btnGuardar);
    dialog.add(btnCancelar);

    comboSerie.setEnabled(false);  // deshabilitado al inicio
    campoCantidad.setEnabled(true);

    comboProducto.addActionListener(e -> {
        String productoSeleccionado = (String) comboProducto.getSelectedItem();
        comboSerie.removeAllItems();

        if (productoSeleccionado != null) {
            String tipo = producto_config.obtenerTipoProducto(productoSeleccionado);

            if ("individual".equalsIgnoreCase(tipo)) {
                comboSerie.setEnabled(true);
                campoCantidad.setText("1");
                campoCantidad.setEnabled(false);

                List<String> seriesDisponibles = asignacion_manager.obtenerSeriesDisponibles(productoSeleccionado);
                if (seriesDisponibles != null && !seriesDisponibles.isEmpty()) {
                    for (String serie : seriesDisponibles) {
                        comboSerie.addItem(serie);
                    }
                } else {
                    // Opcional: mostrar mensaje o dejar combo vacío
                    JOptionPane.showMessageDialog(dialog, "No hay series disponibles para este producto.");
                }
            } else {
                comboSerie.setEnabled(false);
                campoCantidad.setText("");
                campoCantidad.setEnabled(true);
            }
        }
    });

    btnGuardar.addActionListener(ev -> {
        String nombreProfesor = campoNombreProfesor.getText().trim();
        String producto = (String) comboProducto.getSelectedItem();
        String cantidadStr = campoCantidad.getText().trim();
        String serie = null;

        if (producto_config.obtenerTipoProducto(producto).equalsIgnoreCase("individual")) {
            serie = (String) comboSerie.getSelectedItem();
        }

        if (nombreProfesor.isEmpty() || producto == null || cantidadStr.isEmpty() || 
            (producto_config.obtenerTipoProducto(producto).equalsIgnoreCase("individual") && (serie == null || serie.isEmpty()))) {
            JOptionPane.showMessageDialog(dialog, "Todos los campos son obligatorios.");
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr);
            String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
            String tipo = producto_config.obtenerTipoProducto(producto);

            if ("individual".equalsIgnoreCase(tipo)) {
                boolean disponible = asignacion_manager.esProductoIndividualDisponible(producto, serie);
                if (!disponible) {
                    JOptionPane.showMessageDialog(dialog, "Esta serie ya está asignada.");
                    return;
                }
                asignacion nueva = new asignacion(0, nombreProfesor, producto, 1, fechaHora, serie);
                asignacion_manager.agregarAsignacion(nueva);
            } else {
                boolean disponible = asignacion_manager.esProductoDisponible(producto, cantidad);
                if (!disponible) {
                    JOptionPane.showMessageDialog(dialog, "No hay suficiente stock.");
                    return;
                }
                asignacion nueva = new asignacion(0, nombreProfesor, producto, cantidad, fechaHora, null);
                asignacion_manager.agregarAsignacion(nueva);
            }

            cargarAsignaciones();
            dialog.dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "La cantidad debe ser un número entero.");
        }
    });

    btnCancelar.addActionListener(ev -> dialog.dispose());

    dialog.setVisible(true);
}


private void modificarAsignacion() {
    int fila = table.getSelectedRow();
    if (fila == -1) {
        JOptionPane.showMessageDialog(this, "Seleccione una fila para modificar.");
        return;
    }

    int id = (int) table.getValueAt(fila, 0);
    String nombreProfesor = (String) table.getValueAt(fila, 1);
    String producto = (String) table.getValueAt(fila, 2);
    int cantidad = (int) table.getValueAt(fila, 3);
    String serie = (String) table.getValueAt(fila, 5);  // columna 5 = serie

    JDialog dialog = new JDialog(this, "Modificar Asignación", true);
    dialog.setSize(350, 250);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new GridLayout(6, 2, 5, 5));

    JTextField campoNombreProfesor = new JTextField(nombreProfesor);
    JComboBox<String> comboProducto = new JComboBox<>();
    JTextField campoCantidad = new JTextField(String.valueOf(cantidad));
    JComboBox<String> comboSerie = new JComboBox<>();

    ((AbstractDocument) campoCantidad.getDocument()).setDocumentFilter(new IntegerFilter());

    // Cargar productos en comboProducto
    List<String> productos = producto_config.obtenerNombresProductos();
    for (String nombre : productos) {
        comboProducto.addItem(nombre);
    }
    comboProducto.setSelectedItem(producto);

    Runnable actualizarCampos = () -> {
        String productoSel = (String) comboProducto.getSelectedItem();
        if (productoSel == null) return;

        String tipo = producto_config.obtenerTipoProducto(productoSel);

        if ("individual".equalsIgnoreCase(tipo)) {
            comboSerie.setEnabled(true);
            campoCantidad.setText("1");
            campoCantidad.setEnabled(false);

            List<String> seriesDisponibles = asignacion_manager.obtenerSeriesDisponibles(productoSel);

            // Agregar la serie actual si no está en la lista para que pueda seleccionarse
            if (serie != null && !serie.isEmpty() && !seriesDisponibles.contains(serie)) {
                seriesDisponibles.add(0, serie);
            }

            comboSerie.removeAllItems();
            for (String s : seriesDisponibles) {
                comboSerie.addItem(s);
            }
            comboSerie.setSelectedItem(serie);

        } else {
            comboSerie.removeAllItems();
            comboSerie.setEnabled(false);
            campoCantidad.setEnabled(true);
            campoCantidad.setText(String.valueOf(cantidad));
        }
    };

    actualizarCampos.run();

    comboProducto.addActionListener(e -> {
        // Al cambiar el producto, actualizar comboSerie y cantidad
        actualizarCampos.run();
    });

    dialog.add(new JLabel("Nombre Profesor:"));
    dialog.add(campoNombreProfesor);

    dialog.add(new JLabel("Producto:"));
    dialog.add(comboProducto);

    dialog.add(new JLabel("Cantidad:"));
    dialog.add(campoCantidad);

    dialog.add(new JLabel("Serie:"));
    dialog.add(comboSerie);

    JButton btnGuardar = new JButton("Guardar");
    JButton btnCancelar = new JButton("Cancelar");

    dialog.add(btnGuardar);
    dialog.add(btnCancelar);

    btnGuardar.addActionListener(ev -> {
        String nombreProfesorModificado = campoNombreProfesor.getText().trim();
        String productoSeleccionado = (String) comboProducto.getSelectedItem();
        String cantidadStr = campoCantidad.getText().trim();
        String serieModificada = (String) comboSerie.getSelectedItem();

        if (nombreProfesorModificado.isEmpty() || productoSeleccionado == null || cantidadStr.isEmpty() ||
            ("individual".equalsIgnoreCase(producto_config.obtenerTipoProducto(productoSeleccionado)) &&
             (serieModificada == null || serieModificada.isEmpty()))) {
            JOptionPane.showMessageDialog(dialog, "Todos los campos son obligatorios.");
            return;
        }

        try {
            int cantidadModificada = Integer.parseInt(cantidadStr);
            String nuevaFechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
            String tipo = producto_config.obtenerTipoProducto(productoSeleccionado);

            if ("individual".equalsIgnoreCase(tipo)) {
                if (!serieModificada.equals(serie)) {
                    boolean disponible = asignacion_manager.esProductoIndividualDisponible(productoSeleccionado, serieModificada);
                    if (!disponible) {
                        JOptionPane.showMessageDialog(dialog, "Esta serie ya está asignada.");
                        return;
                    }
                }
                // Cantidad siempre 1 para individual
                cantidadModificada = 1;

                asignacion asignacionModificada = new asignacion(id, nombreProfesorModificado, productoSeleccionado, cantidadModificada, nuevaFechaHora, serieModificada);
                asignacion_manager.modificarAsignacion(asignacionModificada);
            } else {
                boolean disponible = asignacion_manager.esProductoDisponible(productoSeleccionado, cantidadModificada);
                if (!disponible) {
                    JOptionPane.showMessageDialog(dialog, "No hay suficiente stock.");
                    return;
                }

                asignacion asignacionModificada = new asignacion(id, nombreProfesorModificado, productoSeleccionado, cantidadModificada, nuevaFechaHora, null);
                asignacion_manager.modificarAsignacion(asignacionModificada);
            }

            cargarAsignaciones();
            dialog.dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "La cantidad debe ser un número entero.");
        }
    });

    btnCancelar.addActionListener(ev -> dialog.dispose());

    dialog.setVisible(true);
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






