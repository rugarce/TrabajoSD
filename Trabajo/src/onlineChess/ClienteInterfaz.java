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
    
    public static boolean usuarioCorrecto = false;
    
    private static final Object lock = new Object();

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
        
        this.setVisible(true);

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
            	
            	while(!usuarioCorrecto) {
            		// Obtenemos el usuario escrito
            		nombreUsuario = campoUsuario.getText().trim();
                    Client.enviarNombreUsuarioDesdeClienteInterfaz(nombreUsuario);
                    
                    // Esperamos para ver si el usuario es correcto
                    esperaUsuarioCorrecto();
                    if (!usuarioCorrecto) {
                        etiquetaMensaje.setText("Nombre de usuario inválido");
                    } else {
                    	// Pantalla principal con opciones
                        panelOpciones = crearPanelOpciones();
                        panelPrincipal.add(panelOpciones, "Opciones");

                        add(panelPrincipal);
                        
                        cambiarAPanelOpciones();
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

    private void cambiarAPanelOpciones() {
        // Cambiar al panel de opciones
        CardLayout layout = (CardLayout) panelPrincipal.getLayout();
        layout.show(panelPrincipal, "Opciones");
    }

    private void unirseAPartida() {
    	
        Client.setOpcionDesdeClienteInterfaz("UNIRME A PARTIDA");
    }

    private void obtenerHistorial() {
    	Client.setOpcionDesdeClienteInterfaz("OBTENER HISTORIAL");
    }

    private void listadoPuntuaciones() {
    	Client.setOpcionDesdeClienteInterfaz("LISTADO PUNTUACIONES");
    }

    private void desconectarse() {
        JOptionPane.showMessageDialog(this, "Desconectándose...");
        Client.setOpcionDesdeClienteInterfaz("DESCONECTAR");
        dispose(); // Cierra la ventana
    }
    
    private static void esperaUsuarioCorrecto() {
    	synchronized (lock) {
	        try {
	            // Bloquea el ClienteInterfaz hasta que el servidor notifique si el nombre es correcto
	            lock.wait();
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            System.err.println("La espera fue interrumpida.");
	        }
	    }
    }
    
 // Método que la interfaz debe invocar para enviar un movimiento
 	public static void enviarUsuarioCorrectoDesdeCliente(boolean valor) {
 	    synchronized (lock) {
 	    	
 	        usuarioCorrecto = valor;

 	        // Notifica a la interfaz si el usuario es correcto o no
 	        lock.notify();
 	    }
 	}
}
