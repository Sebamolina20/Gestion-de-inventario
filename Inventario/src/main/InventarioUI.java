package main;

import java.awt.event.ItemListener;
import modelo.producto;
import operaciones.ProductoManager;
import utility.IntegerFilter;
import utility.Estilo_boton;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;

import java.awt.*;
import java.util.List;

public class InventarioUI extends JFrame {
    private ProductoManager productoManager;
    private JTable table;
    private DefaultTableModel tableModel;

    public InventarioUI() {
        productoManager = new ProductoManager();
        configurarVentana();
        configurarTabla();
        configurarBotones();
    }

    private void configurarVentana() {
        setTitle("Gestión de Inventarios");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void configurarTabla() {
        tableModel = new DefaultTableModel(new Object[]{"ID", "Producto", "Tipo", "Número de Serie", "Cantidad", "Estado"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void configurarBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnAgregar = new JButton("Agregar");
        JButton btnModificar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnListar = new JButton("Listar");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnListar);

        JPanel contenedorCentro = new JPanel();
        contenedorCentro.setLayout(new BoxLayout(contenedorCentro, BoxLayout.Y_AXIS));
        contenedorCentro.add(new JScrollPane(table));  // Tabla
        contenedorCentro.add(panelBotones);            // Botones debajo de la tabla
        add(contenedorCentro, BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> agregarProducto());
        btnModificar.addActionListener(e -> modificarProducto());
        btnEliminar.addActionListener(e -> eliminarProducto());
        btnListar.addActionListener(e -> cargarProductos());

        Estilo_boton.aplicarEstiloVerde(btnAgregar);
        Estilo_boton.aplicarEstiloAzul(btnModificar);
        Estilo_boton.aplicarEstiloRojo(btnEliminar);
        Estilo_boton.aplicarEstiloGris(btnListar);

        JPanel panelNavegacion = new JPanel();
        JButton btnVolverMenu = new JButton("Volver al Menú");
        JButton btnIrAsignaciones = new JButton("Ir a Asignaciones");

        panelNavegacion.add(btnVolverMenu);
        panelNavegacion.add(btnIrAsignaciones);
        add(panelNavegacion, BorderLayout.NORTH);

        btnVolverMenu.addActionListener(e -> {
            new Menu().setVisible(true);  // Abre el menú principal
            dispose();  // Cierra la ventana actual
        });

        // Acción del botón "Ir a Asignaciones"
        btnIrAsignaciones.addActionListener(e -> {
            new AsignacionUI().setVisible(true);  // Abre la ventana de Asignaciones
            dispose();  // Cierra la ventana actual
        });
    }

    private void cargarProductos() {
        tableModel.setRowCount(0); // Limpiar la tabla
        
        List<producto> lista = productoManager.obtenerProductos(); // Obtener desde la BD
        for (producto p : lista) {
            tableModel.addRow(new Object[] {
                p.getId(),
                p.getNombre(),
                p.getTipo(),
                p.getSerie(),
                p.getCantidad(),
                p.getEstado()  // Aquí agregamos el estado del producto
            });
        }
    }

    private void agregarProducto() {
        JDialog dialog = new JDialog(this, "Agregar Producto", true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
    
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
    
        JTextField campoNombre = new JTextField();
        JTextField campoCantidad = new JTextField("1");  // Valor predeterminado para Individual
        JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Individual", "Cantidad"});
        JTextField campoSerie = new JTextField();
    
        JLabel lblSerie = new JLabel("Número de Serie (solo si es Individual):");
    
        panel.add(new JLabel("Nombre del Producto:"));
        panel.add(campoNombre);
        panel.add(Box.createVerticalStrut(10));
    
        panel.add(new JLabel("Tipo de Producto:"));
        panel.add(comboTipo);
        panel.add(Box.createVerticalStrut(10));
    
        panel.add(lblSerie);
        panel.add(campoSerie);
        panel.add(Box.createVerticalStrut(10));
    
        panel.add(new JLabel("Cantidad:"));
        panel.add(campoCantidad);
        panel.add(Box.createVerticalStrut(15));
    
        AbstractDocument docCantidad = (AbstractDocument) campoCantidad.getDocument();
        docCantidad.setDocumentFilter(new IntegerFilter());
    
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAgregar = new JButton("Agregar");
        JButton btnCancelar = new JButton("Cancelar");
    
        botones.add(btnAgregar);
        botones.add(btnCancelar);
    
        panel.add(botones);
        dialog.add(panel);
    
        // Mostrar/ocultar el campo de serie dependiendo del tipo
        ItemListener tipoListener = e -> {
            boolean esIndividual = comboTipo.getSelectedItem().equals("Individual");
            campoSerie.setEnabled(esIndividual);  // Campo serie en gris si es "Cantidad"
            if (esIndividual) {
                campoCantidad.setText("1");  // Si es "Individual", automáticamente poner cantidad en 1
                campoCantidad.setEnabled(false);  // Bloquear la cantidad
            } else {
                campoCantidad.setEnabled(true);  // Habilitar la cantidad si no es "Individual"
            }
        };
        comboTipo.addItemListener(tipoListener);
        tipoListener.itemStateChanged(null); // Ejecutar al inicio
    
        // Acción del botón Agregar
        btnAgregar.addActionListener(ev -> {
            String nuevoNombre = campoNombre.getText().trim();
            String tipoNuevo = (String) comboTipo.getSelectedItem();
            String serieNueva = campoSerie.getText().trim();
            String cantidadStr = campoCantidad.getText().trim();
        
            if (nuevoNombre.isEmpty() || cantidadStr.isEmpty() || tipoNuevo.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Todos los campos son obligatorios.");
                return;
            }
        
            if (tipoNuevo.equals("Individual") && serieNueva.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El número de serie es obligatorio para productos individuales.");
                return;
            }
        
            try {
                int nuevaCantidad = Integer.parseInt(cantidadStr);
                if (!tipoNuevo.equals("Individual")) {
                    serieNueva = null;
                }
        
                producto p = new producto(nuevoNombre, tipoNuevo, serieNueva, nuevaCantidad, "Disponible");
                boolean agregado = productoManager.agregarProducto(p);
        
                if (agregado) {
                    cargarProductos();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Producto agregado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Este producto ya está registrado.", "Error", JOptionPane.ERROR_MESSAGE);
                }
        
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "La cantidad debe ser un número válido.");
            }
        });        
    
        btnCancelar.addActionListener(e -> dialog.dispose());
    
        dialog.setVisible(true);
    }    

    private void modificarProducto() {
        int fila = table.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para modificar.");
            return;
        }
    
        int id = (int) table.getValueAt(fila, 0);
        String nombreAnterior = (String) table.getValueAt(fila, 1);
        String tipoAnterior = (String) table.getValueAt(fila, 2);
        String serieAnterior = (String) table.getValueAt(fila, 3);
        int cantidadAnterior = (int) table.getValueAt(fila, 4);
    
        JDialog dialog = new JDialog(this, "Modificar Producto", true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
    
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
    
        JTextField campoNombre = new JTextField(nombreAnterior);
        JTextField campoCantidad = new JTextField(String.valueOf(cantidadAnterior));
        JComboBox<String> comboTipo = new JComboBox<>(new String[]{"Individual", "Cantidad"});
        JTextField campoSerie = new JTextField(serieAnterior);
    
        JLabel lblSerie = new JLabel("Número de Serie (solo si es Individual):");
    
        comboTipo.setSelectedItem(tipoAnterior);
    
        panel.add(new JLabel("Nombre del Producto:"));
        panel.add(campoNombre);
        panel.add(Box.createVerticalStrut(10));
    
        panel.add(new JLabel("Tipo de Producto:"));
        panel.add(comboTipo);
        panel.add(Box.createVerticalStrut(10));
    
        panel.add(lblSerie);
        panel.add(campoSerie);
        panel.add(Box.createVerticalStrut(10));
    
        panel.add(new JLabel("Cantidad:"));
        panel.add(campoCantidad);
        panel.add(Box.createVerticalStrut(15));
    
        AbstractDocument docCantidad = (AbstractDocument) campoCantidad.getDocument();
        docCantidad.setDocumentFilter(new IntegerFilter());
    
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnModificar = new JButton("Modificar");
        JButton btnCancelar = new JButton("Cancelar");
    
        botones.add(btnModificar);
        botones.add(btnCancelar);
    
        panel.add(botones);
        dialog.add(panel);
    
        // Activar o desactivar campos según tipo
        ItemListener tipoListener = e -> {
            boolean esIndividual = comboTipo.getSelectedItem().equals("Individual");
            campoSerie.setEnabled(esIndividual);
            if (esIndividual) {
                campoCantidad.setText("1");
                campoCantidad.setEnabled(false);
            } else {
                campoCantidad.setEnabled(true);
            }
        };
        comboTipo.addItemListener(tipoListener);
        tipoListener.itemStateChanged(null);
    
        // Acción del botón Modificar
        btnModificar.addActionListener(ev -> {
            String nuevoNombre = campoNombre.getText().trim();
            String tipoNuevo = (String) comboTipo.getSelectedItem();
            String serieNueva = campoSerie.getText().trim();
            String cantidadStr = campoCantidad.getText().trim();
    
            if (nuevoNombre.isEmpty() || cantidadStr.isEmpty() || tipoNuevo.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Todos los campos son obligatorios.");
                return;
            }
    
            if (tipoNuevo.equals("Individual") && serieNueva.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "El número de serie es obligatorio para productos individuales.");
                campoSerie.requestFocus();
                return;
            }
    
            try {
                int nuevaCantidad = Integer.parseInt(cantidadStr);
                if (!tipoNuevo.equals("Individual")) {
                    serieNueva = null;
                }
    
                producto p = new producto(id, nuevoNombre, tipoNuevo, serieNueva, nuevaCantidad);
    
                boolean modificado = productoManager.modificarProducto(p, nombreAnterior);
    
                if (modificado) {
                    cargarProductos();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Producto modificado correctamente.");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Ya existe un producto con el mismo nombre y número de serie.", "Error", JOptionPane.ERROR_MESSAGE);
                }
    
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "La cantidad debe ser un número válido.");
            }
        });
    
        btnCancelar.addActionListener(e -> dialog.dispose());
    
        dialog.setVisible(true);
    }    

    private void eliminarProducto() {
        int fila = table.getSelectedRow();
        if (fila != -1) {
            int id = (int) table.getValueAt(fila, 0); // Obtener el ID del producto seleccionado
            
            // Mostrar un cuadro de diálogo de confirmación
            int confirmacion = JOptionPane.showConfirmDialog(
                this, 
                "¿Estás seguro de que quieres eliminar este producto?", 
                "Confirmar Eliminación", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE
            );
            
            if (confirmacion == JOptionPane.YES_OPTION) {
                // Si el usuario confirma la eliminación
                productoManager.eliminarProducto(id); // Llamar al método para eliminar el producto
                tableModel.removeRow(fila); // Eliminar la fila de la tabla
                JOptionPane.showMessageDialog(this, "Producto eliminado correctamente.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione una fila para eliminar.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventarioUI().setVisible(true));
    }
}

