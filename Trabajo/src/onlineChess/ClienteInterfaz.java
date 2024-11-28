package onlineChess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClienteInterfaz extends JFrame {
    private JPanel panelPrincipal;
    private JPanel panelOpciones;
    private JTextField campoUsuario;
    private JButton botonIniciarSesion;
    private JLabel etiquetaMensaje;
    private String nombreUsuario; // Variable para almacenar el nombre del usuario

    public ClienteInterfaz() {
        setTitle("Ajedrez en Línea");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configuración del panel principal con CardLayout
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new CardLayout());

        // Pantalla de inicio de sesión
        JPanel panelInicioSesion = crearPanelInicioSesion();
        panelPrincipal.add(panelInicioSesion, "InicioSesion");
        
        add(panelPrincipal);

    }

    private JPanel crearPanelInicioSesion() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel etiquetaUsuario = new JLabel("Introduce tu nombre de usuario:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(etiquetaUsuario, gbc);

        campoUsuario = new JTextField(15);
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(campoUsuario, gbc);

        botonIniciarSesion = new JButton("Iniciar Sesión");
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(botonIniciarSesion, gbc);

        etiquetaMensaje = new JLabel(" ");
        etiquetaMensaje.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(etiquetaMensaje, gbc);

        // Acción del botón de iniciar sesión
        botonIniciarSesion.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nombreUsuario = campoUsuario.getText().trim();
                if (nombreUsuario.isEmpty()) {
                    etiquetaMensaje.setText("El nombre de usuario no puede estar vacío.");
                } else {
                    // Simular validación del usuario con el servidor
                    boolean usuarioValido = validarUsuario(nombreUsuario);
                    if (usuarioValido) {
                    	
                    	// Pantalla principal con opciones
                        panelOpciones = crearPanelOpciones();
                        panelPrincipal.add(panelOpciones, "Opciones");

                        add(panelPrincipal);
                        
                        cambiarAPanelOpciones();
                    } else {
                        etiquetaMensaje.setText("Nombre de usuario inválido o ya en uso.");
                    }
                }
            }
        });

        return panel;
    }

    private JPanel crearPanelOpciones() {
        JPanel panel = new JPanel(new BorderLayout());

        // Etiqueta de bienvenida
        JLabel etiquetaBienvenida = new JLabel("Bienvenido al Ajedrez en línea, " + nombreUsuario + "!", SwingConstants.CENTER);
        etiquetaBienvenida.setFont(new Font("Serif", Font.BOLD, 20));
        etiquetaBienvenida.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        panel.add(etiquetaBienvenida, BorderLayout.NORTH);

        // Panel para los botones en formato de tablero
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton botonUnirsePartida = crearBotonTablero("Unirse a Partida", true);
        JButton botonObtenerHistorial = crearBotonTablero("Obtener Historial", false);
        JButton botonListadoPuntuaciones = crearBotonTablero("Listado de Puntuaciones", false);
        JButton botonDesconectarse = crearBotonTablero("Desconectarse", true);

        panelBotones.add(botonUnirsePartida);
        panelBotones.add(botonObtenerHistorial);
        panelBotones.add(botonListadoPuntuaciones);
        panelBotones.add(botonDesconectarse);

        panel.add(panelBotones, BorderLayout.CENTER);

        // Listeners para los botones
        botonUnirsePartida.addActionListener(e -> unirseAPartida());
        botonObtenerHistorial.addActionListener(e -> obtenerHistorial());
        botonListadoPuntuaciones.addActionListener(e -> listadoPuntuaciones());
        botonDesconectarse.addActionListener(e -> desconectarse());

        return panel;
    }

    private JButton crearBotonTablero(String texto, boolean esClaro) {
        JButton boton = new JButton(texto);
        boton.setPreferredSize(new Dimension(100, 100));
        boton.setBackground(esClaro ? new Color(255, 255, 255) : new Color(41, 41, 41));
        boton.setForeground(esClaro ? new Color(41,41,41) : new Color(255, 255, 255));
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setFocusPainted(false);
        return boton;
    }

    private boolean validarUsuario(String nombreUsuario) {
        // Aquí puedes añadir lógica para validar el usuario con el servidor
        return true; // Simula un usuario válido
    }

    private void cambiarAPanelOpciones() {
        // Cambiar al panel de opciones
        CardLayout layout = (CardLayout) panelPrincipal.getLayout();
        layout.show(panelPrincipal, "Opciones");
    }

    private void unirseAPartida() {
        JOptionPane.showMessageDialog(this, "Funcionalidad de Unirse a Partida no implementada aún.");
        // Aquí puedes añadir lógica para unirse a una partida
    }

    private void obtenerHistorial() {
        JOptionPane.showMessageDialog(this, "Funcionalidad de Obtener Historial no implementada aún.");
        // Aquí puedes añadir lógica para obtener el historial del usuario
    }

    private void listadoPuntuaciones() {
        JOptionPane.showMessageDialog(this, "Funcionalidad de Listado de Puntuaciones no implementada aún.");
        // Aquí puedes añadir lógica para obtener el listado de puntuaciones
    }

    private void desconectarse() {
        JOptionPane.showMessageDialog(this, "Desconectándose...");
        // Aquí puedes añadir lógica para desconectarse del servidor
        dispose(); // Cierra la ventana
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteInterfaz interfaz = new ClienteInterfaz();
            interfaz.setVisible(true);
        });
    }
}
