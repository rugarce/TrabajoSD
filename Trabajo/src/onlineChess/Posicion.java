package onlineChess;

import java.io.Serializable;

public class Posicion implements Serializable{
	private int fila;
    private int columna;

    public Posicion(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }
    
    public int fila() {
    	return this.fila;
    }
    
    public int columna() {
    	return this.columna;
    }
    
    public boolean equals(Posicion posicion) {
    	return (this.fila == posicion.fila() && this.columna == posicion.columna());
    }
    
    public String toString() {
    	return "("+fila() + ", " + columna + ")";
    }
}
