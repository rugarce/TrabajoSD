package onlineChess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

public class Board implements Serializable {
	private Pieza[][] tablero;
	// tablero[i][j] representa la ficha del tablero en la fila i y columna j
	// la posicion [0][0] corresponde con la esquina izquierda inferior y la [7][7]
	// la derecha superior

	Board() {
		tablero = new Pieza[8][8];
		inicializarTablero();
	}

	private void inicializarTablero() {
		for (int i = 0; i < 8; i++) {
			tablero[1][i] = new Pieza(0, false, new Posicion(1, i));
			tablero[6][i] = new Pieza(0, true, new Posicion(6, i));
		}

		tablero[0][0] = new Pieza(5, false, new Posicion(0, 0)); // torre
		tablero[0][1] = new Pieza(1, false, new Posicion(0, 1)); // caballo
		tablero[0][2] = new Pieza(2, false, new Posicion(0, 2)); // alfil
		tablero[0][3] = new Pieza(3, false, new Posicion(0, 3)); // dama
		tablero[0][4] = new Pieza(4, false, new Posicion(0, 4)); // rey
		tablero[0][5] = new Pieza(2, false, new Posicion(0, 5)); // alfil
		tablero[0][6] = new Pieza(1, false, new Posicion(0, 6)); // caballo
		tablero[0][7] = new Pieza(5, false, new Posicion(0, 7)); // torre

		tablero[7][0] = new Pieza(5, true, new Posicion(7, 0)); // torre
		tablero[7][1] = new Pieza(1, true, new Posicion(7, 1)); // caballo
		tablero[7][2] = new Pieza(2, true, new Posicion(7, 2)); // alfil
		tablero[7][3] = new Pieza(3, true, new Posicion(7, 3)); // dama
		tablero[7][4] = new Pieza(4, true, new Posicion(7, 4)); // rey
		tablero[7][5] = new Pieza(2, true, new Posicion(7, 5)); // alfil
		tablero[7][6] = new Pieza(1, true, new Posicion(7, 6)); // caballo
		tablero[7][7] = new Pieza(5, true, new Posicion(7, 7)); // torre
	}

	Pieza getTablero(Posicion posicion) {
		return tablero[posicion.fila()][posicion.columna()];
	}

	public String toString() {
		String spacer = "_________________________" + "\n";
		String tablero = spacer;

		for (int i = 0; i < 8; i++) {
			String linea = "|";
			for (int j = 0; j < 8; j++) {
				Pieza p = getTablero(new Posicion(i, j));
				if (p != null) {
					linea += p.getCode() + "|";
				} else {
					linea += "  " + "|";
				}
			}
			tablero += linea + "\n";
			tablero += spacer + "\n";
		}

		return tablero;
	}

	public ArrayList<Posicion> getMovimientosPosibles(Pieza pieza) {
		ArrayList<Posicion> movimientos = new ArrayList<>();
		int tipo = pieza.getTipo(); // El tipo de pieza (0 = peón, 1 = caballo, 2 = alfil, 3 = dama, 4 = rey, 5 =
									// torre)
		boolean lado = pieza.getLado(); // true para blancas, false para negras
		int fila = pieza.getPosicion().fila();
		int columna = pieza.getPosicion().columna();

		switch (tipo) {
		case 0: // Peón
			int direccion = lado ? -1 : 1; // Blancas avanzan hacia arriba (-1), negras hacia abajo (+1)
			// Movimiento hacia adelante
			if (esPosicionValida(fila + direccion, columna) && tablero[fila + direccion][columna] == null) {
				movimientos.add(new Posicion(fila + direccion, columna));
				// Avance doble desde posición inicial
				if ((fila == 6 && lado) || (fila == 1 && !lado)) {
					if (esPosicionValida(fila + 2 * direccion, columna)
							&& tablero[fila + 2 * direccion][columna] == null
							&& !hayPiezaAliada(fila + direccion, columna, lado)) {
						movimientos.add(new Posicion(fila + 2 * direccion, columna));
					}
				}
			}
			// Capturas en diagonal
			if (esPosicionValida(fila + direccion, columna - 1)
					&& hayPiezaEnemiga(fila + direccion, columna - 1, lado)) {
				movimientos.add(new Posicion(fila + direccion, columna - 1));
			}
			if (esPosicionValida(fila + direccion, columna + 1)
					&& hayPiezaEnemiga(fila + direccion, columna + 1, lado)) {
				movimientos.add(new Posicion(fila + direccion, columna + 1));
			}
			break;

		case 1: // Caballo
			int[][] movimientosCaballo = { { -2, -1 }, { -2, 1 }, { 2, -1 }, { 2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 },
					{ 1, 2 } };
			for (int[] movimiento : movimientosCaballo) {
				int nuevaFila = fila + movimiento[0];
				int nuevaColumna = columna + movimiento[1];
				if (esPosicionValida(nuevaFila, nuevaColumna) && (!hayPiezaAliada(nuevaFila, nuevaColumna, lado))) {
					movimientos.add(new Posicion(nuevaFila, nuevaColumna));
				}
			}
			break;

		case 2: // Alfil
			agregarMovimientosDiagonales(movimientos, fila, columna, lado);
			break;

		case 3: // Dama
			agregarMovimientosDiagonales(movimientos, fila, columna, lado);
			agregarMovimientosLineales(movimientos, fila, columna, lado);
			break;

		case 4: // Rey
			int[][] movimientosRey = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 },
					{ 1, 1 } };
			for (int[] movimiento : movimientosRey) {
				int nuevaFila = fila + movimiento[0];
				int nuevaColumna = columna + movimiento[1];
				if (esPosicionValida(nuevaFila, nuevaColumna) && !hayPiezaAliada(nuevaFila, nuevaColumna, lado)) {
					movimientos.add(new Posicion(nuevaFila, nuevaColumna));
				}
			}
			break;

		case 5: // Torre
			agregarMovimientosLineales(movimientos, fila, columna, lado);
			break;

		default:
			break;
		}
		return movimientos;
	}

	private boolean esPosicionValida(int fila, int columna) {
		return fila >= 0 && fila < 8 && columna >= 0 && columna < 8;
	}

	private boolean hayPiezaEnemiga(int fila, int columna, boolean lado) {
		Pieza p = tablero[fila][columna];
		return p != null && p.getLado() != lado;
	}

	private boolean hayPiezaAliada(int fila, int columna, boolean lado) {
		Pieza p = tablero[fila][columna];
		return p != null && p.getLado() == lado;
	}

	private void agregarMovimientosLineales(ArrayList<Posicion> movimientos, int fila, int columna, boolean lado) {
		int[][] direcciones = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
		for (int[] direccion : direcciones) {
			int nuevaFila = fila + direccion[0];
			int nuevaColumna = columna + direccion[1];
			while (esPosicionValida(nuevaFila, nuevaColumna)) {
				if (tablero[nuevaFila][nuevaColumna] == null) {
					movimientos.add(new Posicion(nuevaFila, nuevaColumna));
				} else {
					if (hayPiezaEnemiga(nuevaFila, nuevaColumna, lado)) {
						movimientos.add(new Posicion(nuevaFila, nuevaColumna));
					}
					break;
				}
				nuevaFila += direccion[0];
				nuevaColumna += direccion[1];
			}
		}
	}

	private void agregarMovimientosDiagonales(ArrayList<Posicion> movimientos, int fila, int columna, boolean lado) {
		int[][] direcciones = { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
		for (int[] direccion : direcciones) {
			int nuevaFila = fila + direccion[0];
			int nuevaColumna = columna + direccion[1];
			while (esPosicionValida(nuevaFila, nuevaColumna)) {
				if (tablero[nuevaFila][nuevaColumna] == null) {
					movimientos.add(new Posicion(nuevaFila, nuevaColumna));
				} else {
					if (hayPiezaEnemiga(nuevaFila, nuevaColumna, lado)) {
						movimientos.add(new Posicion(nuevaFila, nuevaColumna));
					}
					break;
				}
				nuevaFila += direccion[0];
				nuevaColumna += direccion[1];
			}
		}
	}

	public ArrayList<Pieza> getPiezas(boolean lado) {
		ArrayList<Pieza> piezas = new ArrayList<Pieza>();

		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				Pieza p = getTablero(new Posicion(i, j));
				if (p != null) {
					piezas.add(p);
				}
			}
		}

		return piezas;
	}

	public Pieza moverPieza(Pieza pieza, Posicion posicion) {
		Pieza target = getTablero(posicion);

		// QUE HACER SI LA POSICION DE DESTINO NO ES VALIDA COMPROBAR DENTRO DEL METODO
		// O ANTES DE LLAMARLO

		tablero[posicion.fila()][posicion.columna()] = pieza;
		tablero[pieza.getPosicion().fila()][pieza.getPosicion().columna()] = null;

		// Comprobar si el peón ha llegado a la última fila para la promoción
		if (pieza.getTipo() == 0 && (posicion.fila() == 0 || posicion.fila() == 7)) {
			// Si el peón llega a la última fila, solicitamos al jugador elegir la pieza con
			// la que promocionar
			String nuevaPieza = elegirPiezaPromocion(pieza.getLado());

			// Actualizamos la pieza según la elección del jugador
			switch (nuevaPieza) {
			case "D": // Dama
				pieza.setTipo(3); // El tipo de Dama es 3
				break;
			case "T": // Torre
				pieza.setTipo(5); // El tipo de Torre es 5
				break;
			case "A": // Alfil
				pieza.setTipo(2); // El tipo de Alfil es 2
				break;
			case "C": // Caballo
				pieza.setTipo(1); // El tipo de Caballo es 1
				break;
			default:
				// Si el jugador no elige una opción válida, mantenemos el peón (esto es
				// opcional, podrías manejar esto con un mensaje de error)
				System.out.println("Elección no válida, el peón se mantendrá.");
				break;
			}
		}

		pieza.setPosicion(posicion);
		return target; // si devuelve null, sabemos que solo se ha movido, si no, devuelve la ficha que
						// se ha comido
	}
	
	public String elegirPiezaPromocion(boolean lado) {
	    // Asumimos que el lado es true para blancas y false para negras
	    String color = lado ? "Blanco" : "Negro";
	    
	    // Pedimos al jugador que elija la pieza a la que promover
	    System.out.println(color + ", elige una pieza para promocionar el peón:");
	    System.out.println("D - Dama");
	    System.out.println("T - Torre");
	    System.out.println("A - Alfil");
	    System.out.println("C - Caballo");

	    try (Scanner scanner = new Scanner(System.in)) {
			String eleccion = scanner.nextLine().toUpperCase(); // Convertimos a mayúsculas para evitar problemas con mayúsculas/minúsculas

			return eleccion; // Retornamos la elección del jugador
		}
	}

	public Boolean quienGana() {
		ArrayList<Pieza> piezasN = getPiezas(false);

		boolean reyN = false;
		for (Pieza pieza : piezasN) {
			if (pieza.getTipo() == 4) {
				reyN = true;
			}
		}

		ArrayList<Pieza> piezasB = getPiezas(true);

		boolean reyB = false;
		for (Pieza pieza : piezasB) {
			if (pieza.getTipo() == 4) {
				reyB = true;
			}
		}

		if (!reyN && !reyB) {
			// ha habido un error
		}

		if (!reyN && reyB) {
			return true;
		}

		if (reyN && !reyB) {
			return false;
		}

		return null;
	}
}
