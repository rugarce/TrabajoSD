package onlineChess;

import java.awt.EventQueue; 

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;

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

public class VentanaInterfaz {

	private JFrame frmAjedrez;

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
		
		JPanel panelTablero = new JPanel();
		panelTablero.setForeground(new Color(255, 255, 255));
		frmAjedrez.getContentPane().add(panelTablero, BorderLayout.CENTER);
		panelTablero.setLayout(new GridLayout(8,8));
		
		JButton btn;
		Color clr;
		Pieza pieza;
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				btn = new JButton(""); // Creamos el botón para cada posición del tablero
				if((i%2==0 && j%2==0) || (i%2!=0 && j%2!=0)) {
					clr = new Color(255,255,255);
				}else {
					 clr = new Color(41,41,41);
				}
				btn.setBackground(clr);
				pieza = tablero.getTablero(new Posicion(i,j)); // Obtenemos la pieza de la posición
				if(pieza!=null) {
					asignarImagen(pieza,btn); // Asignamos la imagen de la pieza
				}
				panelTablero.add(btn);
			}
		}
	}

}
