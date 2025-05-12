package utility;

import modelo.producto;
import operaciones.producto_config;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class Funciones{

    // Método para agregar un nuevo producto
    public static void agregarProducto(JDialog dialog, JTextField campoNombre, JComboBox<String> combo, 
                                       JTextField campoCantidad, producto_config dao, DefaultTableModel tableModel) {
        String nombre = campoNombre.getText().trim();
        String producto = (String) combo.getSelectedItem();
        String cantidadStr = campoCantidad.getText().trim();

        if (!nombre.isEmpty() && producto != null && !cantidadStr.isEmpty()) {
            try {
                int cantidad = Integer.parseInt(cantidadStr);
                int id = tableModel.getRowCount() + 1; // Suponiendo que el ID es el número de filas
                tableModel.addRow(new Object[]{id, nombre, producto, cantidad});
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Cantidad debe ser un número entero.");
            }
        } else {
            JOptionPane.showMessageDialog(dialog, "Todos los campos son obligatorios.");
        }
    }

    // Método para modificar un producto
    public static void modificarProducto(int fila, String nuevoNombre, String nuevoProducto, 
                                         String cantidadTexto, DefaultTableModel tableModel) {
        if (!nuevoNombre.isEmpty()) {
            tableModel.setValueAt(nuevoNombre, fila, 1);
        }
        if (nuevoProducto != null) {
            tableModel.setValueAt(nuevoProducto, fila, 2);
        }
        if (!cantidadTexto.isEmpty()) {
            try {
                int nuevaCantidad = Integer.parseInt(cantidadTexto);
                tableModel.setValueAt(nuevaCantidad, fila, 3);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "La cantidad debe ser un número entero.");
            }
        }
    }

    // Método para eliminar un producto
    public static void eliminarProducto(int fila, DefaultTableModel tableModel) {
        if (fila != -1) {
            tableModel.removeRow(fila);
        } else {
            JOptionPane.showMessageDialog(null, "Seleccione una fila para eliminar.");
        }
    }

    // Método para cargar productos en el combo
    public static void cargarProductosEnCombo(JComboBox<String> comboProducto, producto_config dao) {
        comboProducto.removeAllItems();
        List<producto> productos = dao.obtenerTodosLosProductos();
        for (producto p : productos) {
            comboProducto.addItem(p.getNombre());
        }
    }
}
