package appchat;

import java.io.IOException;
import java.net.*;
import java.util.Collections;

/**
 * Ejecútalo ANTES del chat: muestra tu IP, qué puertos están libres
 * y si el servidor del profesor responde.
 */
public class VerificarRed {

    static final String IP_PROFESOR = "10.1.65.201";
    static final int[] PUERTOS = {5000, 1234, 8080, 6000, 7000};

    public static void main(String[] args) {
        System.out.println("===== 1. TUS IPs =====");
        System.out.println("IP principal (la que das a tus compañeros): " + ChatVentana.ipLocal());
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback()) continue;
                for (InetAddress a : Collections.list(ni.getInetAddresses())) {
                    if (a instanceof Inet4Address)
                        System.out.println("  - " + a.getHostAddress() + "   (" + ni.getDisplayName() + ")");
                }
            }
        } catch (SocketException e) {
            System.out.println("No se pudieron listar las interfaces: " + e.getMessage());
        }

        System.out.println("\n===== 2. PUERTOS EN TU PC =====");
        for (int p : PUERTOS) {
            try (ServerSocket ss = new ServerSocket(p)) {
                System.out.println("  Puerto " + p + ": LIBRE  -> puedes usarlo para tu Servidor");
            } catch (IOException e) {
                System.out.println("  Puerto " + p + ": OCUPADO -> ya lo usa otro programa");
            }
        }

        System.out.println("\n===== 3. SERVIDOR DEL PROFESOR (" + IP_PROFESOR + ") =====");
        for (int p : PUERTOS) {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress(IP_PROFESOR, p), 1500);
                System.out.println("  Puerto " + p + ": ABIERTO  -> usa HOST=\"" + IP_PROFESOR + "\" y PUERTO=" + p);
            } catch (IOException e) {
                System.out.println("  Puerto " + p + ": sin respuesta");
            }
        }
    }
}