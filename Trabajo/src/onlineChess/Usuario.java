package onlineChess;

public class Usuario {
	private String nombre; //el nombre funciona como id
	private int puntuacion;
	
	Usuario(String nombre, int puntuacion){
		this.nombre = nombre;
		this.puntuacion = puntuacion;
	}
	
	public String getNombre() {
		return this.nombre;
	}
	
	public int getPuntuacion() {
		return this.puntuacion;
	}
	
	public void setPuntuacion(int puntuacion) {
		this.puntuacion = puntuacion;
	}
	
}
