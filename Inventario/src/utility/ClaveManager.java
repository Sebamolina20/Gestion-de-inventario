package utility;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ClaveManager {

    private static final String RUTA_CLAVE = "utility/clave.txt";

    // Guardamos las claves en el archivo con este formato:
    // Primera línea: hash de clave normal
    // Segunda línea: hash de clave maestra

    private static String hashClave(String clave) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(clave.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public static boolean verificarClaveNormal(String claveIngresada) {
        try {
            String[] claves = leerClaves();
            if (claves == null) return false;
            String hashIngresado = hashClave(claveIngresada);
            return hashIngresado.equals(claves[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean verificarClaveMaestra(String claveIngresada) {
        try {
            String[] claves = leerClaves();
            if (claves == null) return false;
            String hashIngresado = hashClave(claveIngresada);
            return hashIngresado.equals(claves[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cambiar ambas claves (la normal y la maestra)
    public static boolean cambiarClaves(String nuevaClaveNormal, String nuevaClaveMaestra) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RUTA_CLAVE))) {
            writer.write(hashClave(nuevaClaveNormal));
            writer.newLine();
            writer.write(hashClave(nuevaClaveMaestra));
            return true;
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cambiar solo la clave normal
    public static boolean cambiarClaveNormal(String nuevaClaveNormal) {
        try {
            String[] claves = leerClaves();
            if (claves == null) return false;
            return cambiarClaves(nuevaClaveNormal, claves[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cambiar solo la clave maestra
    public static boolean cambiarClaveMaestra(String nuevaClaveMaestra) {
        try {
            String[] claves = leerClaves();
            if (claves == null) return false;
            return cambiarClaves(claves[0], nuevaClaveMaestra);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Leer las dos claves desde archivo
    private static String[] leerClaves() throws IOException {
        File archivo = new File(RUTA_CLAVE);
        if (!archivo.exists()) {
            return null;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String claveNormal = reader.readLine();
            String claveMaestra = reader.readLine();
            if (claveNormal == null || claveMaestra == null) return null;
            return new String[]{claveNormal, claveMaestra};
        }
    }
}

