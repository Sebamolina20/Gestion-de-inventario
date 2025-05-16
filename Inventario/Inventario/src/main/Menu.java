package main;

import javax.swing.*;
import java.awt.*;

public class Menu extends JFrame {
    public Menu() {
        setTitle("Menú Principal");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1));

        // Botones del menú
        JButton btnInventario = new JButton("Abrir Inventario");
        JButton btnAsignaciones = new JButton("Abrir Asignaciones");
        JButton btnAyuda = new JButton("Ayuda");
        JButton btnSalir = new JButton("Salir");

        // Añadir los botones a la ventana
        add(btnInventario);
        add(btnAsignaciones);
        add(btnAyuda);
        add(btnSalir);

        // Acción del botón "Abrir Inventario"
        btnInventario.addActionListener(e -> {
            new InventarioUI().setVisible(true);  // Abre la ventana de InventarioUI
            dispose(); // Cierra el menú
        });

        // Acción del botón "Abrir Asignaciones"
        btnAsignaciones.addActionListener(e -> {
            new AsignacionUI().setVisible(true);  // Abre la ventana de AsignacionUI
            dispose(); // Cierra el menú
        });

        // Acción del botón "Ayuda"
        btnAyuda.addActionListener(e ->
            JOptionPane.showMessageDialog(this, "Este es un sistema de gestión de inventario.")
        );

        // Acción del botón "Salir"
        btnSalir.addActionListener(e -> System.exit(0));
    }

    // Método principal para ejecutar la aplicación
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Menu().setVisible(true));
    }
}


