package appchat;
   
import javax.swing.*;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * USUARIO 1 (Servidor). Ejecutar PRIMERO.
 * Espera a que el Cliente se conecte y luego ambos pueden chatear.
 */
public class Servidor {

    // Puerto donde escucha el servidor (el Cliente debe usar el MISMO)
    static final int PUERTO = 5000;

    public static void main(String[] args) {
        String nombre = JOptionPane.showInputDialog(null, "Tu nombre:", "Usuario 1");
        if (nombre == null || nombre.isBlank()) nombre = "Usuario 1";
        final String miNombre = nombre.trim();

        SwingUtilities.invokeLater(() -> {
            ChatVentana ventana = new ChatVentana("Servidor", miNombre);
            ventana.setVisible(true);

            // La espera (accept) se hace en otro hilo para no congelar la ventana
            new Thread(() -> {
                try (ServerSocket servidor = new ServerSocket(PUERTO)) {
                    String ip = ChatVentana.ipLocal();
                    ventana.setEstado("Esperando en " + ip + ":" + PUERTO, ChatVentana.AMARILLO);
                    ventana.agregarAviso("Comparte esta IP con tu compañero: " + ip + "  puerto " + PUERTO);
                    System.out.println("Servidor iniciado en " + ip + ":" + PUERTO);

                    Socket sc = servidor.accept();           // espera al cliente
                    System.out.println("Cliente conectado: " + sc.getInetAddress().getHostAddress());
                    ventana.conectar(sc);
                } catch (BindException ex) {
                    ventana.error("El puerto " + PUERTO + " ya está en uso. Cierra el otro programa o cambia PUERTO.");
                } catch (IOException ex) {
                    ventana.error("Error: " + ex.getMessage());
                }
            }).start();
        });
    }
}