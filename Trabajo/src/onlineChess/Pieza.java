package onlineChess;

import java.io.Serializable;

public class Pieza implements Serializable {
	String[] piezasCodes = new String[] { "P", "C", "A", "D", "R", "T"};
	String[] piezasDict = new String[] { "Peon", "Caballo", "Alfil", "Dama", "Rey", "Torre"};
										// 0		1			2		3		4		5
	private int tipo;
	private boolean lado; //solo acepta "Negras"(false) o "Blancas"(true)
	private Posicion posicion;
	
	Pieza(int tipo, boolean lado, Posicion posicion){
		this.tipo = tipo;
		this.lado = lado;
		this.posicion = posicion;
	}
	
	public String getNombre() {
		return piezasDict[this.tipo];
	}
	
	public String getCode() {
		if(lado) {
			return piezasCodes[tipo] + "B";
		}else {
			return piezasCodes[tipo] + "N";
		}
	}
	
	public boolean getLado() {
		return this.lado;
	}
	
	public int getTipo() {
		return this.tipo;
	}
	
	public Posicion getPosicion() {
		return this.posicion;
	}
	
	public void setPosicion(Posicion posicion) {
		this.posicion = posicion;
	}
	
	public String toString() {
		return "("+ posicion.fila() + "," + posicion.columna() + ")";
	}
}
