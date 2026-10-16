package appchat;

import javax.swing.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * USUARIO 2 (Cliente). Ejecutar DESPUÉS del Servidor.
 */
public class Cliente {

    // "127.0.0.1"   -> si el Servidor corre en TU MISMA PC
    // "10.1.65.201" -> si el Servidor corre en la PC del profesor
    // otra IP       -> la que muestra la ventana del Servidor de tu compañero
    static final String HOST = "10.1.65.201";   // IP del profesor

    // Debe ser el MISMO puerto que usa el Servidor
    static final int PUERTO = 5000;

    public static void main(String[] args) {
        String nombre = JOptionPane.showInputDialog(null, "Tu nombre:", "Maria Fernanda Mori Cumpen");
        if (nombre == null || nombre.isBlank()) nombre = "Maria Fernanda Mori Cumpen";
        final String miNombre = nombre.trim();

        SwingUtilities.invokeLater(() -> {
            ChatVentana ventana = new ChatVentana("Cliente", miNombre);
            ventana.setVisible(true);

            new Thread(() -> {
                ventana.setEstado("Conectando a " + HOST + ":" + PUERTO + "...", ChatVentana.AMARILLO);
                try {
                    Socket sc = new Socket();
                    sc.connect(new InetSocketAddress(HOST, PUERTO), 5000); // 5 s de espera máx.
                    ventana.conectar(sc);
                } catch (ConnectException ex) {
                    ventana.error("No hay servidor escuchando en " + HOST + ":" + PUERTO
                            + ". ¿Iniciaste primero el Servidor?");
                } catch (SocketTimeoutException ex) {
                    ventana.error("Sin respuesta de " + HOST + ". Revisa que estén en la misma red o el firewall.");
                } catch (IOException ex) {
                    ventana.error("Error: " + ex.getMessage());
                }
            }).start();
        });
    }
}