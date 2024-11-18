package onlineChess;

import java.awt.EventQueue; 

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.BoxLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class VentanaInterfaz {

	private JFrame frmAjedrez;
	private Map<Posicion,JButton> casillas;
	JPanel panelTablero;
	private Board tablero;
	Pieza piezaEnMovimiento = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Board tablero = new Board();
					VentanaInterfaz window = new VentanaInterfaz(tablero);
					window.frmAjedrez.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public VentanaInterfaz(Board tablero) {
		this.tablero = tablero;
		initialize(tablero);
	}
	
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

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(Board tablero) {
		casillas = new HashMap<Posicion, JButton>();
		frmAjedrez = new JFrame();
		frmAjedrez.setTitle("Ajedrez");
		frmAjedrez.setBounds(100, 100, 451, 428);
		frmAjedrez.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panelDatos = new JPanel();
		frmAjedrez.getContentPane().add(panelDatos, BorderLayout.NORTH);
		
		JLabel lblUser = new JLabel("Usuario:");
		
		JLabel lblGameStatus = new JLabel("Estado de Partida: ");
		panelDatos.setLayout(new BoxLayout(panelDatos, BoxLayout.X_AXIS));
		panelDatos.add(lblUser);
		panelDatos.add(lblGameStatus);
		
		JLabel lblColor = new JLabel("Fichas: ");
		panelDatos.add(lblColor);
		
		panelTablero = new JPanel();
		panelTablero.setForeground(new Color(255, 255, 255));
		frmAjedrez.getContentPane().add(panelTablero, BorderLayout.CENTER);
		panelTablero.setLayout(new GridLayout(8,8));
		
		JButton btn;
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				btn = new JButton(""); // Creamos el botón para cada posición del tablero
				
				setColorCasilla(i,j,btn); // Le damos el color correspondiente a la casilla
				
				setPieza(i,j,btn); // Añadimos imagen y evento al botón (en caso de que sea necesario
				panelTablero.add(btn); // Añadimos el botón al tablero
				casillas.put(new Posicion(i,j), btn);	// Añadimos el botón al mapa (Posición, casilla)
				}
		}
	}
	
	public void actualizarTablero(Board tablero) {
		this.tablero = tablero; // Actualizamos el tablero de la partida
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				JButton btn = (JButton) panelTablero.getComponent(i*8+j); // Obtenemos el botónd de la posición en la que estamos
				
				setColorCasilla(i,j,btn); // Le damos el color correspondiente a la casilla
				
				setPieza(i,j,btn); // Añadimos imagen y evento al botón (en caso de que sea necesario)
			}
		}
	}
	
	// CONTROLADORES EVENTOS MOVIMIENTOS
	
	public void comenzarMovimiento(JButton btn) {
		
		piezaEnMovimiento = getPieza(btn); 
		
		// No hace falta comprobar si la pieza es nula (casilla sin pieza), porque solo se han creado eventos para las casillas con pieza
		ArrayList<Posicion> posiciones = tablero.getMovimientosPosibles(piezaEnMovimiento);
		
		if(posiciones.size() == 0) { // Si la pieza no puede hacer movimientos, finaliza el evento
			return;
		}
		
		for(JButton button: casillas.values()) { // Eliminamos los eventos existentes en el resto de casillas
			for(ActionListener a : button.getActionListeners()) {
				button.removeActionListener(a);
			}
			button.setBackground(new Color(245,0,0)); // Ponemos el color rojo en todas las casillas
		}
		
		for(Posicion pos: posiciones) {
			casillas.get(getPosicion(pos)).setBackground(new Color(58,168,50));
			casillas.get(getPosicion(pos)).addActionListener(new ActionListener() { // Añadimos un evento para cuando se haga clic en el botón
				public void actionPerformed(ActionEvent e) {
					finalizarMovimiento((JButton) e.getSource());
				}
			});
		}
	}
	
	public void finalizarMovimiento(JButton btn) {
		tablero.moverPieza(piezaEnMovimiento,getPosicionDeBoton(btn));
		// parar Reloj
		
		piezaEnMovimiento = null;
		
		for(JButton button: casillas.values()) { // Eliminamos los eventos existentes en las casillas
			for(ActionListener a : button.getActionListeners()) {
				button.removeActionListener(a);
			}
		}
		
		actualizarTablero(tablero); // actualizamos el tablero
	}
	
	/*
	 * Para un botón dado, devuelve la pieza que se encuentre en dicha casilla, null si está vacío
	 */
	public Pieza getPieza(JButton btn) {
		Posicion posicion = null;
		for(Entry<Posicion, JButton> entrada: casillas.entrySet()) {
			if(entrada.getValue().equals(btn)) {
				posicion = entrada.getKey();
				return tablero.getTablero((posicion));
			}
		}
		return null;
	}
	
	/*
	 * Para un botón dado, devuelve la posición en el mapa de casillas
	 */
	public Posicion getPosicionDeBoton(JButton btn) {
		for(Entry<Posicion, JButton> entrada: casillas.entrySet()) {
			if(entrada.getValue().equals(btn)) {
				return entrada.getKey();
			}
		}
		return null;
	}
	
	public Posicion getPosicion(Posicion posicion) {
		for(Entry<Posicion, JButton> entrada: casillas.entrySet()) {
			if(entrada.getKey().equals(posicion)) {
				return entrada.getKey();
			}
		}
		return null;
	}
	
	// MÉTODOS AUXILIARES PINTAR TABLERO
	
	public void setColorCasilla(int i, int j, JButton btn) {
		Color clr;
		if((i%2==0 && j%2==0) || (i%2!=0 && j%2!=0)) {
			clr = new Color(255,255,255);
		}else {
			 clr = new Color(41,41,41);
		}
		btn.setBackground(clr); // Le añadimos el color de casilla correspondiente
	}
	
	public void setPieza(int i, int j, JButton btn) {
		Pieza pieza = tablero.getTablero(new Posicion(i,j)); // Obtenemos la pieza de la posición
		if(pieza!=null) { // Comprueba que haya pieza
			asignarImagen(pieza,btn); // Asignamos la imagen de la pieza
			btn.addActionListener(new ActionListener() { // Añadimos un evento para cuando se haga clic en el botón
				public void actionPerformed(ActionEvent e) {
					comenzarMovimiento((JButton) e.getSource());
				}
			});
		}else {
			btn.setIcon(null); // Si no hay pieza en esa posición, la imagen debe ser nula
		}
	}

}
