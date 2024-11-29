package onlineChess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Interfaz extends JFrame {
	
    private static final int SIZE = 8;  // El tablero es de 8x8
    private JPanel panelTablero;  // Panel para el tablero
    private JButton[][] botonesTablero;  // Botones del tablero para representar las casillas
    private JLabel labelTurno;  // Muestra quién está jugando
    
    private Board board;  // El tablero de ajedrez
    
    private boolean esMiTurno = false;  // Para saber si es el turno del jugador
    private String turno = "Blancas";  // Oponente alternará entre "Blancas" y "Negras"
    private boolean miLado;
    
    private Posicion posicionSeleccionada = null;  // Almacena la pieza seleccionada
    
    private Posicion movimientoOrigen;
	private Posicion movimientoDestino;
	
	// Variables para el tiempo
	private long tiempoRestanteMs; // Tiempo restante en milisegundos
	private JLabel labelTimer;  // Muestra el tiempo restante
    private Timer timer;  // Temporizador para contar el tiempo
	
	private JButton btnDesconectar = null;
	private boolean desconexion = false;

    public Interfaz() {        

        setTitle("Ajedrez en Línea");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicializamos el tablero gráfico y los botones
        panelTablero = new JPanel(new GridLayout(SIZE, SIZE));
        botonesTablero = new JButton[SIZE][SIZE];

        // Crear los botones para el tablero
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                botonesTablero[i][j] = new JButton();
                botonesTablero[i][j].setPreferredSize(new Dimension(60, 60));
                botonesTablero[i][j].setBackground((i + j) % 2 == 0 ? Color.WHITE : new Color(41,41,41));
                final int fila = i; // Guardamos los valores de posición para asignar el evento correspondiente
                final int columna = j;
                
                // Añadimos los eventos de detección a los botones
                botonesTablero[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        moverPieza(fila, columna);
                    }
                });
                
                panelTablero.add(botonesTablero[i][j]); // Añadimos el botón al panel
            }
        }
        
        labelTurno = new JLabel("Esperando al oponente...", SwingConstants.CENTER);
        labelTurno.setFont(new Font("Arial", Font.PLAIN, 16));
        labelTurno.setPreferredSize(new Dimension(500, 30));
        
        getContentPane().add(panelTablero, BorderLayout.CENTER);
        getContentPane().add(labelTurno, BorderLayout.SOUTH);
        
        JPanel panelOpciones = new JPanel();
        getContentPane().add(panelOpciones, BorderLayout.NORTH);
        
        btnDesconectar = new JButton("Desconectar");
        btnDesconectar.setEnabled(false);
        btnDesconectar.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		desconectarseDePartida();
        	}
        });
        panelOpciones.add(btnDesconectar, BorderLayout.EAST);
        
        tiempoRestanteMs =  10 * 1000; // 1h en milisegundos
        labelTimer = new JLabel(formatTiempo(tiempoRestanteMs), SwingConstants.CENTER);
        labelTimer.setFont(new Font("Arial", Font.BOLD, 16));
        panelOpciones.add(labelTimer, BorderLayout.CENTER);

        timer = new Timer(10, new ActionListener() { // Actualiza cada 10 ms
            @Override
            public void actionPerformed(ActionEvent e) {
                if (esMiTurno) {
                    tiempoRestanteMs -= 10; // Reducimos 10 ms
                    labelTimer.setText(formatTiempo(tiempoRestanteMs));

                    if (tiempoRestanteMs <= 0) {
                        desconectarseDePartida();
                    }
                }
            }
        });
        
        setVisible(true);
        actualizarTablero(new Board());
    }
    
    private String formatTiempo(long tiempoMs) {
        long horas = tiempoMs / (60 * 60 * 1000);
        long minutos = (tiempoMs / (60 * 1000)) % 60;
        long segundos = (tiempoMs / 1000) % 60;
        long decimas = (tiempoMs / 10) % 100;

        return String.format("%02d:%02d:%02d.%02d", 
                             horas, minutos, segundos, decimas);
    }
    
    // ACTIVAR EL BOTÓN DE DESCONEXIÓN CUANDO SE EMPIEZA UNA PARTIDA
    public void activarBotonDesconectar() {
    	this.btnDesconectar.setEnabled(true);
    }
    
    // CUANDO EL CLIENTE QUIERE DESCONECTARSE DE LA PARTIDA
    public void desconectarseDePartida() {
    	desconexion = true;
    	
    	// NOTIFICAMOS AL CLIENTE QUE SE QUEREMOS DESCONECTARNOS, ENVIANDO UN MOVIMIENTO NULO
    	Client.enviarMovimientoDesdeInterfaz(null, null);
    }
    
    public boolean getDesconexion() {
    	return this.desconexion;
    }

    // Método para actualizar el tablero visualmente
    public void actualizarTablero(Board board) {
        this.board = board;  // Actualizamos el tablero interno
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                // Obtener el estado de la pieza (si existe) y actualizar el botón correspondiente
            	botonesTablero[i][j].setBackground((i + j) % 2 == 0 ? Color.WHITE : new Color(41,41,41));
            	Pieza pieza = board.getTablero(new Posicion(i, j));
                if (pieza != null) {
                    asignarImagen(pieza, botonesTablero[i][j]);  
                } else {
                    botonesTablero[i][j].setIcon(null);  // Casilla vacía
                    
                }
            }
        }
    }

    // Método para manejar los clics en las casillas del tablero
    private void moverPieza(int fila, int columna) {
        // Si es el turno del jugador, podemos hacer el movimiento
        if (esMiTurno) {
            if (posicionSeleccionada == null) { // No había sido seleccionada previamente una pieza
                // Seleccionamos una pieza
                posicionSeleccionada = new Posicion(fila, columna); // Guardamos la posición de la pieza
                
                Pieza piezaEnMovimiento = board.getTablero(posicionSeleccionada); // Obtenemos la pieza seleccionada
                
                if(piezaEnMovimiento != null && piezaEnMovimiento.getLado() == miLado) {
                	
                	ArrayList<Posicion> posiciones = board.getMovimientosPosibles(piezaEnMovimiento);
                	
                	if(posiciones.size() == 0) { // Si no se puede mover a ninguna posición, acabamos
                		posicionSeleccionada = null; //No se puede hacer ningún movimiento
                		
                		for(int i = 0; i < SIZE; i++) {
                    		for(int j = 0; j < SIZE; j++) {
                    			botonesTablero[i][j].setBackground(new Color(245,0,0)); // Ponemos el color de fondo a rojo
                    		}
                    	}
                		
                		return;
                	}
                	
                	for(int i = 0; i < SIZE; i++) {
                		for(int j = 0; j < SIZE; j++) {
                			botonesTablero[i][j].setBackground(new Color(245,0,0)); // Ponemos el color de fondo a rojo
                		}
                	}
                	
                	for(Posicion pos : posiciones) {
                		botonesTablero[pos.fila()][pos.columna()].setBackground(new Color(58,168,50)); // Ponemos el color verde en las posiciones a las que se pueda mover
                	}
                	
                }else { // Si la pieza es nula (casilla vacía) o es del contrincante, no hacemos nada
                	posicionSeleccionada = null; // Reseteamos la selección (no se puede hacer ningún movimiento con la casilla elegida)
                }
            } else {

                Posicion destino = new Posicion(fila, columna);
                
                if(botonesTablero[fila][columna].getBackground().equals(new Color(58,168,50))) {
	                // Intentamos mover la pieza (previamente guardada en piezaSeleccionada)
                
                	enviarMovimiento(posicionSeleccionada, destino);
                	posicionSeleccionada = null;  // Reseteamos la selección 
                }
                
                if(board.getTablero(destino) != null && board.getTablero(destino).getLado() == miLado) {
                	posicionSeleccionada = null;  // Reseteamos la selección 
                	moverPieza(fila, columna);
                }
                
            }
        }
    }

    // Método para enviar el movimiento de una pieza al servidor
    private void enviarMovimiento(Posicion origen, Posicion destino) {
        
    	// Enviamos el movimiento al cliente
    	Client.enviarMovimientoDesdeInterfaz(origen,destino);
		
		// ACTUALIZAMOS TABLERO INTERNO DE INTERFAZ (Movemos la pieza)
    	this.board.moverPieza(board.getTablero(posicionSeleccionada), destino);
    	
    	// EL CLIENTE ACTUALIZA LA INTERFAZ 
    }

    // Método para recibir la actualización del tablero desde el servidor
    public void recibirActualizacionTablero(Board tablero, boolean turno) {
        // ACTUALIZACIÓN DEL TABLERO (INTERNO Y GRÁFICO)
		actualizarTablero(tablero);
		
		// CAMBIO DE TURNO
		esMiTurno = turno;
		
		labelTurno.setText(esMiTurno ? "Es tu turno" : "Es el turno del oponente");
		
		// Manejo del temporizador
        if (esMiTurno) {
            timer.start();
        } else {
            timer.stop();
        }
    }
    
    public Posicion getOrigen() {
    	return this.movimientoOrigen;
    }
    
    public Posicion getDestino() {
    	return this.movimientoDestino;
    }
    
    public void setPosiciones(Posicion origen, Posicion destino) {
    	this.movimientoOrigen = origen;
    	this.movimientoDestino = destino;
    }
    
    // Asignar el lado en el que jugamos
    public void asignarLado(boolean lado) {
    	this.miLado = lado;
    }
    
    public Board getTablero() {
    	return this.board;
    }
    
    // Método para asignar las imágenes
    public void asignarImagen(Pieza pieza, JButton btn) {
		ImageIcon imageIcon;
		switch(pieza.getTipo()) {
		case 0:
			if(pieza.getLado()) {
				imageIcon = new ImageIcon("Images/white-pawn.png");
			}else {
				imageIcon = new ImageIcon("Images/black-pawn.png");
			}
			break;
		case 1:
			if(pieza.getLado()) {
				imageIcon = new ImageIcon("Images/white-knight.png");
			}else {
				imageIcon = new ImageIcon("Images/black-knight.png");
			}
			break;
		case 2:
			if(pieza.getLado()) {
				imageIcon = new ImageIcon("Images/white-bishop.png");
			}else {
				imageIcon = new ImageIcon("Images/black-bishop.png");
			}
			break;
		case 3:
			if(pieza.getLado()) {
				imageIcon = new ImageIcon("Images/white-queen.png");
			}else {
				imageIcon = new ImageIcon("Images/black-queen.png");
			}
			break;
		case 4:
			if(pieza.getLado()) {
				imageIcon = new ImageIcon("Images/white-king.png");
			}else {
				imageIcon = new ImageIcon("Images/black-king.png");
			}
			break;
		case 5:
			if(pieza.getLado()) {
				imageIcon = new ImageIcon("Images/white-rook.png");
			}else {
				imageIcon = new ImageIcon("Images/black-rook.png");
			}
			break;
		default:
			imageIcon = new ImageIcon();
		}
		Image img = imageIcon.getImage();
        Image imgScale = img.getScaledInstance(48, 48, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(imgScale);
		btn.setIcon(scaledIcon);
	}
}
