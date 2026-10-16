/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package appchat;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * ACTIVIDAD: Chat bidireccional entre dos usuarios en una sola ventana.
 * Usuario 1 actua como SERVIDOR (ServerSocket) y Usuario 2 como CLIENTE (Socket).
 * Cada mensaje viaja por la red (TCP) de un panel al otro.
 */
public class ChatDosUsuarios extends JFrame {

    // ---------- Datos de la conexion ----------
    static final String HOST = "127.0.0.1";
    static final int PUERTO = 5050;
    static final String ALUMNA = "Maria Fernanda Mori Cumpen";

    // ---------- Paleta de colores ----------
    static final Color FONDO     = new Color(0xEEF2F7);
    static final Color CABECERA  = new Color(0x1E3A8A);
    static final Color AZUL      = new Color(0x2563EB);
    static final Color MORADO    = new Color(0x7C3AED);
    static final Color BORDE     = new Color(0xD8DEE8);
    static final Color TEXTO     = new Color(0x0F172A);
    static final Color GRIS      = new Color(0x64748B);
    static final Color VERDE     = new Color(0x22C55E);
    static final Color AMARILLO  = new Color(0xF59E0B);
    static final Color ROJO      = new Color(0xEF4444);

    static final Font F_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    static final Font F_CHICA  = new Font("Segoe UI", Font.PLAIN, 11);

    private final PanelUsuario usuario1 = new PanelUsuario("Usuario 1", "Servidor", AZUL);
    private final PanelUsuario usuario2 = new PanelUsuario("Usuario 2", "Cliente", MORADO);
    private final JLabel lblEstado = new JLabel("Iniciando conexion...");
    private final JLabel punto = new JLabel("●");

    public ChatDosUsuarios() {
        setTitle("CHAT - " + ALUMNA);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);
        setContentPane(raiz);

        // ---------- Cabecera ----------
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(CABECERA);
        cabecera.setBorder(new EmptyBorder(14, 24, 14, 24));

        JLabel titulo = new JLabel("CHAT");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);

        JLabel alumna = new JLabel("Alumna: " + ALUMNA);
        alumna.setFont(F_NORMAL);
        alumna.setForeground(new Color(0xC7D2FE));

        JPanel izq = new JPanel();
        izq.setOpaque(false);
        izq.setLayout(new BoxLayout(izq, BoxLayout.Y_AXIS));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        alumna.setAlignmentX(Component.LEFT_ALIGNMENT);
        izq.add(titulo);
        izq.add(alumna);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        der.setOpaque(false);
        punto.setForeground(AMARILLO);
        lblEstado.setFont(F_CHICA);
        lblEstado.setForeground(new Color(0xC7D2FE));
        der.add(punto);
        der.add(lblEstado);

        cabecera.add(izq, BorderLayout.WEST);
        cabecera.add(der, BorderLayout.EAST);
        raiz.add(cabecera, BorderLayout.NORTH);

        // ---------- Los dos usuarios ----------
        JPanel centro = new JPanel(new GridLayout(1, 2, 18, 0));
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(18, 18, 18, 18));
        centro.add(usuario1);
        centro.add(usuario2);
        raiz.add(centro, BorderLayout.CENTER);

        conectar();
    }

    /** Crea el servidor (Usuario 1) y conecta al cliente (Usuario 2). */
    private void conectar() {
        new Thread(() -> {
            try {
                ServerSocket servidor = new ServerSocket(PUERTO);       // Usuario 1 escucha
                Socket socketCliente = new Socket(HOST, PUERTO);         // Usuario 2 se conecta
                Socket socketServidor = servidor.accept();               // Usuario 1 acepta
                servidor.close();

                usuario1.conectar(socketServidor, "Usuario 2");
                usuario2.conectar(socketCliente, "Usuario 1");

                SwingUtilities.invokeLater(() -> {
                    punto.setForeground(VERDE);
                    lblEstado.setText("Conectado por TCP  " + HOST + ":" + PUERTO);
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    punto.setForeground(ROJO);
                    lblEstado.setText("Error: " + ex.getMessage());
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ChatDosUsuarios().setVisible(true));
    }

    // =====================================================================
    //  Panel de cada usuario: Mensaje + Enviar + Conversacion
    // =====================================================================
    static class PanelUsuario extends JPanel {
        private final String nombre;
        private final Color color;
        private String otro = "";
        private DataOutputStream out;

        private final JTextField txtMensaje = new JTextField();
        private final JButton btnEnviar = new JButton("Enviar");
        private final JPanel conversacion = new JPanel();
        private final JScrollPane scroll;

        PanelUsuario(String nombre, String rol, Color color) {
            this.nombre = nombre;
            this.color = color;
            setLayout(new BorderLayout(0, 12));
            setOpaque(false);
            setBorder(new EmptyBorder(16, 16, 16, 16));

            // --- Encabezado del usuario ---
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            top.setOpaque(false);
            top.add(new Avatar(nombre.substring(nombre.length() - 1), color));
            JPanel textos = new JPanel();
            textos.setOpaque(false);
            textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
            JLabel lblNombre = new JLabel(nombre);
            lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 18));
            lblNombre.setForeground(TEXTO);
            JLabel lblRol = new JLabel(rol);
            lblRol.setFont(F_CHICA);
            lblRol.setForeground(GRIS);
            textos.add(lblNombre);
            textos.add(lblRol);
            top.add(textos);

            // --- Fila Mensaje + Enviar ---
            JLabel lblMensaje = new JLabel("Mensaje");
            lblMensaje.setFont(F_BOLD);
            lblMensaje.setForeground(GRIS);

            txtMensaje.setFont(F_NORMAL);
            txtMensaje.setBorder(new CompoundBorder(new LineBorder(BORDE, 1, true), new EmptyBorder(7, 10, 7, 10)));
            txtMensaje.setEnabled(false);
            txtMensaje.addActionListener(e -> enviar());

            btnEnviar.setUI(new BasicButtonUI());
            btnEnviar.setFont(F_BOLD);
            btnEnviar.setForeground(Color.WHITE);
            btnEnviar.setBackground(color);
            btnEnviar.setBorder(new EmptyBorder(7, 16, 7, 16));
            btnEnviar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnEnviar.setEnabled(false);
            btnEnviar.addActionListener(e -> enviar());

            JPanel filaMensaje = new JPanel(new BorderLayout(8, 0));
            filaMensaje.setOpaque(false);
            filaMensaje.add(lblMensaje, BorderLayout.WEST);
            filaMensaje.add(txtMensaje, BorderLayout.CENTER);
            filaMensaje.add(btnEnviar, BorderLayout.EAST);

            JPanel norte = new JPanel(new BorderLayout(0, 14));
            norte.setOpaque(false);
            norte.add(top, BorderLayout.NORTH);
            norte.add(filaMensaje, BorderLayout.SOUTH);

            // --- Conversacion ---
            JLabel lblConv = new JLabel("Conversacion");
            lblConv.setFont(F_BOLD);
            lblConv.setForeground(GRIS);

            conversacion.setLayout(new BoxLayout(conversacion, BoxLayout.Y_AXIS));
            conversacion.setBackground(FONDO);
            conversacion.setBorder(new EmptyBorder(10, 8, 10, 8));
            JPanel envoltura = new JPanel(new BorderLayout());
            envoltura.setBackground(FONDO);
            envoltura.add(conversacion, BorderLayout.NORTH);

            scroll = new JScrollPane(envoltura);
            scroll.setBorder(new LineBorder(BORDE, 1, true));
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getVerticalScrollBar().setUnitIncrement(16);

            JPanel centro = new JPanel(new BorderLayout(0, 6));
            centro.setOpaque(false);
            centro.add(lblConv, BorderLayout.NORTH);
            centro.add(scroll, BorderLayout.CENTER);

            add(norte, BorderLayout.NORTH);
            add(centro, BorderLayout.CENTER);
        }

        /** Tarjeta blanca con esquinas redondeadas. */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 22, 22);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 22, 22);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth() - 3, 6, 6, 6);
            g2.dispose();
            super.paintComponent(g);
        }

        /** Recibe el socket ya conectado y empieza a escuchar mensajes. */
        void conectar(Socket socket, String nombreOtro) throws IOException {
            this.otro = nombreOtro;
            out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            Thread lector = new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();               // mensaje que llega del otro usuario
                        agregarBurbuja(otro, msg, false);
                    }
                } catch (IOException ex) {
                    agregarAviso("Conexion cerrada");
                }
            });
            lector.setDaemon(true);
            lector.start();

            SwingUtilities.invokeLater(() -> {
                txtMensaje.setEnabled(true);
                btnEnviar.setEnabled(true);
                agregarAviso("Conectado con " + otro);
            });
        }

        private void enviar() {
            String texto = txtMensaje.getText().trim();
            if (texto.isEmpty() || out == null) return;
            try {
                out.writeUTF(texto);                             // se envia por el socket
                out.flush();
                agregarBurbuja("Tu", texto, true);
                txtMensaje.setText("");
                txtMensaje.requestFocusInWindow();
            } catch (IOException ex) {
                agregarAviso("No se pudo enviar");
            }
        }

        private void agregarBurbuja(String autor, String texto, boolean mio) {
            SwingUtilities.invokeLater(() -> {
                Burbuja b = new Burbuja(mio ? color : Color.WHITE, !mio);
                JLabel a = new JLabel(autor);
                a.setFont(new Font("Segoe UI", Font.BOLD, 10));
                a.setForeground(mio ? new Color(255, 255, 255, 190) : color);
                String html = texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                String ancho = texto.length() > 28 ? "width:170px;" : "";
                JLabel t = new JLabel("<html><body style='" + ancho + "'>" + html + "</body></html>");
                t.setFont(F_NORMAL);
                t.setForeground(mio ? Color.WHITE : TEXTO);
                JLabel h = new JLabel(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
                h.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                h.setForeground(mio ? new Color(255, 255, 255, 190) : GRIS);
                h.setHorizontalAlignment(SwingConstants.RIGHT);
                b.add(a, BorderLayout.NORTH);
                b.add(t, BorderLayout.CENTER);
                b.add(h, BorderLayout.SOUTH);

                JPanel fila = new JPanel(new FlowLayout(mio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
                fila.setOpaque(false);
                fila.add(b);
                agregarFila(fila);
            });
        }

        private void agregarAviso(String texto) {
            SwingUtilities.invokeLater(() -> {
                JLabel l = new JLabel(texto);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                l.setForeground(GRIS);
                JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                fila.setOpaque(false);
                fila.add(l);
                agregarFila(fila);
            });
        }

        private void agregarFila(JPanel fila) {
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
            conversacion.add(fila);
            conversacion.add(Box.createVerticalStrut(6));
            conversacion.revalidate();
            conversacion.repaint();
            SwingUtilities.invokeLater(() -> {
                JScrollBar bar = scroll.getVerticalScrollBar();
                bar.setValue(bar.getMaximum());
            });
        }
    }

    // =====================================================================
    //  Componentes de diseño
    // =====================================================================
    static class Burbuja extends JPanel {
        private final Color fondo;
        private final boolean borde;

        Burbuja(Color fondo, boolean borde) {
            super(new BorderLayout(0, 1));
            this.fondo = fondo;
            this.borde = borde;
            setOpaque(false);
            setBorder(new EmptyBorder(6, 10, 5, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            if (borde) {
                g2.setColor(BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class Avatar extends JComponent {
        private final String letra;
        private final Color color;

        Avatar(String letra, Color color) {
            this.letra = letra;
            this.color = color;
            setPreferredSize(new Dimension(40, 40));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 0, 40, 40);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 17));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(letra, (40 - fm.stringWidth(letra)) / 2, (40 - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }
}