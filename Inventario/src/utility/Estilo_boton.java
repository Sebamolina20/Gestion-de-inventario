package utility;

import javax.swing.*;
import java.awt.*;

public class Estilo_boton {

    public static void aplicarEstiloVerde(JButton boton) {
        boton.setBackground(new Color(76, 175, 80));
        boton.setForeground(Color.black);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setPreferredSize(new Dimension(100, 30));
    }

    public static void aplicarEstiloAzul(JButton boton) {
        boton.setBackground(new Color(33, 150, 243));
        boton.setForeground(Color.black);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setPreferredSize(new Dimension(100, 30));
    }

    public static void aplicarEstiloRojo(JButton boton) {
        boton.setBackground(new Color(244, 67, 54));
        boton.setForeground(Color.black);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setPreferredSize(new Dimension(100, 30));
    }

    public static void aplicarEstiloGris(JButton boton) {
        boton.setBackground(new Color(255, 255, 0));
        boton.setForeground(Color.black);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setPreferredSize(new Dimension(100, 30));
    }
}
