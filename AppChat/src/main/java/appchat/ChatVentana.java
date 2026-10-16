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
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * Ventana de chat con diseño (cabecera, burbujas, hora y estado de conexión).
 * La usan tanto Servidor (Usuario 1) como Cliente (Usuario 2).
 */
public class ChatVentana extends JFrame {

    // ---------- Paleta de colores ----------
    static final Color FONDO        = new Color(0xEEF2F7);
    static final Color CABECERA     = new Color(0x1E3A8A);
    static final Color ACENTO       = new Color(0x2563EB);
    static final Color ACENTO_HOVER = new Color(0x1D4ED8);
    static final Color BURBUJA_OTRO = Color.WHITE;
    static final Color BORDE        = new Color(0xD8DEE8);
    static final Color TEXTO        = new Color(0x0F172A);
    static final Color GRIS         = new Color(0x64748B);
    static final Color VERDE        = new Color(0x22C55E);
    static final Color AMARILLO     = new Color(0xF59E0B);
    static final Color ROJO         = new Color(0xEF4444);

    static final Font FUENTE        = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font FUENTE_BOLD   = new Font("Segoe UI", Font.BOLD, 14);
    static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 18);
    static final Font FUENTE_CHICA  = new Font("Segoe UI", Font.PLAIN, 11);

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final String miNombre;
    private String otroNombre = "Otro usuario";

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;

    private final JPanel panelMensajes = new JPanel();
    private final JScrollPane scroll;
    private final JTextField txtMensaje = new JTextField();
    private final JButton btnEnviar = new JButton("Enviar");
    private final JLabel lblEstado = new JLabel("Iniciando...");
    private final JLabel puntoEstado = new JLabel("●");

    public ChatVentana(String rol, String miNombre) {
        this.miNombre = miNombre;
        setTitle("Chat - " + miNombre + " (" + rol + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 640);
        setMinimumSize(new Dimension(360, 480));
        setLocationByPlatform(true);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FONDO);
        setContentPane(raiz);

        // ---------- Cabecera ----------
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(CABECERA);
        cabecera.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel avatar = new Avatar(miNombre);
        JPanel textosCab = new JPanel();
        textosCab.setOpaque(false);
        textosCab.setLayout(new BoxLayout(textosCab, BoxLayout.Y_AXIS));
        textosCab.setBorder(new EmptyBorder(0, 12, 0, 0));

        JLabel lblTitulo = new JLabel("CHAT  ·  " + miNombre);
        lblTitulo.setFont(FUENTE_TITULO);
        lblTitulo.setForeground(Color.WHITE);

        JPanel filaEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        filaEstado.setOpaque(false);
        puntoEstado.setForeground(AMARILLO);
        puntoEstado.setFont(FUENTE_CHICA);
        lblEstado.setForeground(new Color(0xC7D2FE));
        lblEstado.setFont(FUENTE_CHICA);
        filaEstado.add(puntoEstado);
        filaEstado.add(lblEstado);
        ((FlowLayout) filaEstado.getLayout()).setHgap(4);

        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaEstado.setAlignmentX(Component.LEFT_ALIGNMENT);
        textosCab.add(lblTitulo);
        textosCab.add(Box.createVerticalStrut(2));
        textosCab.add(filaEstado);

        cabecera.add(avatar, BorderLayout.WEST);
        cabecera.add(textosCab, BorderLayout.CENTER);
        raiz.add(cabecera, BorderLayout.NORTH);

        // ---------- Zona de mensajes ----------
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBackground(FONDO);
        panelMensajes.setBorder(new EmptyBorder(12, 10, 12, 10));

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(FONDO);
        contenedor.add(panelMensajes, BorderLayout.NORTH); // mensajes pegados arriba

        scroll = new JScrollPane(contenedor);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        raiz.add(scroll, BorderLayout.CENTER);

        // ---------- Barra inferior ----------
        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setBackground(Color.WHITE);
        barra.setBorder(new CompoundBorder(
                new LineBorder(BORDE, 1),
                new EmptyBorder(10, 12, 10, 12)));

        txtMensaje.setFont(FUENTE);
        txtMensaje.setForeground(TEXTO);
        txtMensaje.setBorder(new CompoundBorder(
                new RoundedBorder(BORDE, 18),
                new EmptyBorder(8, 12, 8, 12)));
        txtMensaje.setEnabled(false);
        txtMensaje.addActionListener(e -> enviar()); // Enter envía

        btnEnviar.setUI(new BasicButtonUI());
        btnEnviar.setFont(FUENTE_BOLD);
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setBackground(ACENTO);
        btnEnviar.setFocusPainted(false);
        btnEnviar.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnEnviar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEnviar.setEnabled(false);
        btnEnviar.addActionListener(e -> enviar());
        btnEnviar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btnEnviar.setBackground(ACENTO_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btnEnviar.setBackground(ACENTO); }
        });

        barra.add(txtMensaje, BorderLayout.CENTER);
        barra.add(btnEnviar, BorderLayout.EAST);
        raiz.add(barra, BorderLayout.SOUTH);
    }

    // ================== Estados de la conexión ==================

    public void setEstado(String texto, Color colorPunto) {
        SwingUtilities.invokeLater(() -> {
            lblEstado.setText(texto);
            puntoEstado.setForeground(colorPunto);
        });
    }

    public void error(String texto) {
        setEstado("Error", ROJO);
        agregarAviso(texto);
    }

    /** Se llama cuando ya hay un Socket conectado (servidor o cliente). */
    public void conectar(Socket s) throws IOException {
        this.socket = s;
        in = new DataInputStream(s.getInputStream());
        out = new DataOutputStream(s.getOutputStream());

        // 1) Apenas se conecta, envía su nombre (así el otro panel ve quién entró)
        out.writeUTF(miNombre + " se conectó al chat");
        out.flush();

        setEstado("Conectado a " + s.getInetAddress().getHostAddress() + ":" + s.getPort(), VERDE);
        SwingUtilities.invokeLater(() -> {
            txtMensaje.setEnabled(true);
            btnEnviar.setEnabled(true);
            txtMensaje.requestFocusInWindow();
        });

        // 2) Hilo que escucha mensajes del otro usuario (bidireccional)
        Thread lector = new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    // Formato de cada mensaje: "Nombre: texto"
                    int sep = msg.indexOf(": ");
                    if (sep > 0) {
                        otroNombre = msg.substring(0, sep);
                        agregarMensaje(otroNombre, msg.substring(sep + 2), false);
                    } else {
                        agregarAviso(msg);            // p. ej. "Ana se conectó al chat"
                    }
                }
            } catch (IOException ex) {
                setEstado("Desconectado", ROJO);
                agregarAviso(otroNombre + " salió del chat");
                SwingUtilities.invokeLater(() -> {
                    txtMensaje.setEnabled(false);
                    btnEnviar.setEnabled(false);
                });
            }
        }, "lector-chat");
        lector.setDaemon(true);
        lector.start();
    }

    private void enviar() {
        String texto = txtMensaje.getText().trim();
        if (texto.isEmpty() || out == null) return;
        try {
            out.writeUTF(miNombre + ": " + texto);   // cada mensaje lleva tu nombre
            out.flush();
            agregarMensaje(miNombre, texto, true);
            txtMensaje.setText("");
        } catch (IOException ex) {
            error("No se pudo enviar: " + ex.getMessage());
        }
    }

    // ================== Dibujo de mensajes ==================

    public void agregarMensaje(String autor, String texto, boolean esMio) {
        SwingUtilities.invokeLater(() -> {
            Burbuja b = new Burbuja(esMio ? ACENTO : BURBUJA_OTRO, !esMio);

            JLabel lblAutor = new JLabel(esMio ? "Tú" : autor);
            lblAutor.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblAutor.setForeground(esMio ? new Color(0xBFDBFE) : ACENTO);

            String html = escapar(texto);
            String estilo = texto.length() > 32 ? "width:220px;" : "";
            JLabel lblTexto = new JLabel("<html><body style='" + estilo + "'>" + html + "</body></html>");
            lblTexto.setFont(FUENTE);
            lblTexto.setForeground(esMio ? Color.WHITE : TEXTO);

            JLabel lblHora = new JLabel(LocalTime.now().format(HORA));
            lblHora.setFont(FUENTE_CHICA);
            lblHora.setForeground(esMio ? new Color(0xBFDBFE) : GRIS);
            lblHora.setHorizontalAlignment(SwingConstants.RIGHT);

            b.add(lblAutor, BorderLayout.NORTH);
            b.add(lblTexto, BorderLayout.CENTER);
            b.add(lblHora, BorderLayout.SOUTH);

            JPanel fila = new JPanel(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
            fila.setOpaque(false);
            fila.add(b);
            agregarFila(fila);
        });
    }

    public void agregarAviso(String texto) {
        SwingUtilities.invokeLater(() -> {
            JLabel aviso = new JLabel(texto);
            aviso.setFont(FUENTE_CHICA);
            aviso.setForeground(GRIS);
            aviso.setBorder(new CompoundBorder(new RoundedBorder(BORDE, 14), new EmptyBorder(3, 10, 3, 10)));
            JPanel fila = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            fila.setOpaque(false);
            fila.add(aviso);
            agregarFila(fila);
        });
    }

    private void agregarFila(JPanel fila) {
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, fila.getPreferredSize().height));
        panelMensajes.add(fila);
        panelMensajes.add(Box.createVerticalStrut(8));
        panelMensajes.revalidate();
        panelMensajes.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private static String escapar(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ================== Utilidad: IP de esta PC ==================

    /** Devuelve la IP de red local (la que deben usar los demás para conectarse). */
    public static String ipLocal() {
        // Truco: un socket UDP "conectado" no envía nada, pero indica qué IP usa la PC para salir a la red.
        try (DatagramSocket ds = new DatagramSocket()) {
            ds.connect(InetAddress.getByName("8.8.8.8"), 80);
            String ip = ds.getLocalAddress().getHostAddress();
            if (!ip.startsWith("0.")) return ip;
        } catch (Exception ignored) { }
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (InetAddress a : Collections.list(ni.getInetAddresses())) {
                    if (a instanceof Inet4Address) return a.getHostAddress();
                }
            }
        } catch (SocketException ignored) { }
        return "127.0.0.1";
    }

    // ================== Componentes con diseño ==================

    /** Panel con esquinas redondeadas para cada mensaje. */
    static class Burbuja extends JPanel {
        private final Color color;
        private final boolean conBorde;

        Burbuja(Color color, boolean conBorde) {
            super(new BorderLayout(0, 2));
            this.color = color;
            this.conBorde = conBorde;
            setOpaque(false);
            setBorder(new EmptyBorder(8, 12, 6, 12));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            if (conBorde) {
                g2.setColor(BORDE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Círculo con la inicial del usuario. */
    static class Avatar extends JPanel {
        private final String inicial;

        Avatar(String nombre) {
            this.inicial = nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase();
            setOpaque(false);
            setPreferredSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int d = Math.min(getWidth(), getHeight());
            int y0 = (getHeight() - d) / 2;
            g2.setColor(ACENTO);
            g2.fillOval(0, y0, d, d);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(inicial, (d - fm.stringWidth(inicial)) / 2,
                    y0 + (d - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Borde redondeado para la caja de texto y los avisos. */
    static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radio;

        RoundedBorder(Color color, int radio) { this.color = color; this.radio = radio; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radio, radio);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(2, 2, 2, 2); }
    }
}