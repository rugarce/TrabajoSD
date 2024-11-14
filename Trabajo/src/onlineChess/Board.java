package onlineChess;

import java.util.ArrayList;

public class Board {
	private Pieza[][] tablero;
	//tablero[i][j] representa la ficha del tablero en la fila i y columna j
	//la posicion [0][0] corresponde con la esquina izquierda inferior y la [7][7] la derecha superior
	
	Board(){
		tablero = new Pieza[8][8];
		inicializarTablero();
	}
	
	
	private void inicializarTablero() {
		for(int i = 0; i < 7; i++) {
			tablero[1][i] = new Pieza(0, false, new Posicion(1,i));
			tablero[6][i] = new Pieza(0, true, new Posicion(6,i));
		}
		

		tablero[0][0] = new Pieza(5, false, new Posicion(0,0));   //torre
		tablero[0][1] = new Pieza(1, false, new Posicion(0,2));   //caballo
		tablero[0][2] = new Pieza(2, false, new Posicion(0,3));   //alfil
		tablero[0][3] = new Pieza(3, false, new Posicion(0,4));   //dama
		tablero[0][4] = new Pieza(4, false, new Posicion(0,5));   //rey
		tablero[0][5] = new Pieza(2, false, new Posicion(0,6));   //alfil
		tablero[0][6] = new Pieza(1, false, new Posicion(0,7));   //caballo
		tablero[0][7] = new Pieza(5, false, new Posicion(0,8));   //torre
		
		tablero[7][0] = new Pieza(5, false, new Posicion(7,0));   //torre
		tablero[7][1] = new Pieza(1, false, new Posicion(7,1));   //caballo
		tablero[7][2] = new Pieza(2, false, new Posicion(7,2));   //alfil
		tablero[7][3] = new Pieza(3, false, new Posicion(7,3));   //dama
		tablero[7][4] = new Pieza(4, false, new Posicion(7,4));   //rey
		tablero[7][5] = new Pieza(2, false, new Posicion(7,5));   //alfil
		tablero[7][6] = new Pieza(1, false, new Posicion(7,6));   //caballo
		tablero[7][7] = new Pieza(5, false, new Posicion(7,7));   //torre
	}
	
	Pieza getTablero(Posicion posicion) {
		return tablero[posicion.fila()][posicion.columna()];
	}
	
	public String toString() {
		String spacer = "_________________________" + "\n";
		String tablero = spacer;
		
		for(int i = 0; i < 8; i++) {
			String linea = "|";
			for(int j = 0; j < 8; j++) {
				Pieza p = getTablero(new Posicion(i,j));
				if(p != null) {
					linea += p.getCode() + "|";
				}else {
					linea += "  " + "|";
				}
				
				tablero += linea + "\n";
				tablero += spacer + "\n";
			}
		}
		
		return tablero;
	}
	
	public ArrayList<Posicion> getMovimientosPosibles(Pieza p){
		//esta función devuelve una lista de movimientos posibles a los que se puede mover una pieza
		
		return null;
	}
	
	public ArrayList<Pieza> getPiezas(boolean lado){
		ArrayList<Pieza> piezas = new ArrayList<Pieza>();
		
		for(int i = 0; i < 8; i++) {
			for(int j = 0; j < 8; j++) {
				Pieza p = getTablero(new Posicion(i,j));
				if(p != null) {
					piezas.add(p);
				}
			}
		}
		
		return piezas;
	}
	
	public Pieza moverPieza(Pieza pieza, Posicion posicion) {
		Pieza target = getTablero(posicion);
		
		tablero[posicion.fila()][posicion.columna()] = pieza;
		tablero[pieza.getPosicion().fila()][pieza.getPosicion().columna()] = null;
		
		pieza.setPosicion(posicion);
		return target; //si devuelve null, sabemos que solo se ha movido, si no, devuelve la ficha que se ha comido
	}
	
	public Boolean quienGana() {
		ArrayList<Pieza> piezasN = getPiezas(false);
		
		boolean reyN = false;
		for (Pieza pieza : piezasN) {
		    if(pieza.getTipo() == 4) {
		    	reyN = true;
		    }
		}
		
		ArrayList<Pieza> piezasB = getPiezas(true);
		
		boolean reyB = false;
		for (Pieza pieza : piezasB) {
		    if(pieza.getTipo() == 4) {
		    	reyB = true;
		    }
		}
		
		if(!reyN && !reyB) {
			//ha habido un error;
		}
		
		if(!reyN && reyB) {
			return true;
		}
		
		if(reyN && !reyB) {
			return false;
		}

		return null;
	}
}
