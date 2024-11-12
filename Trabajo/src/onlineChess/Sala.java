package onlineChess;

import java.net.Socket;

public class Sala implements Runnable{

	private Socket socketUserB; //socket del ususario de las piezas blancas
	private Socket socketUserN; //socket del ususario de las piezas negras
	
	private Usuario userB;
	private Usuario userN;
	
	private Board board;
	
	//el primer usuario en entrar a la sala maneja las blancas
	Sala(Socket socketUserB, Usuario userB){
		this.socketUserB = socketUserB;
		this.userB = userB;
		
		this.board = new Board();
	}
	
	public void unirseNegras(Socket socketUserN, Usuario userN) {
		this.socketUserN = socketUserN;
		this.userN = userN;
	}
	
	@Override
	public void run() {
		//logica de la sala
		
		//AQUI EN UN WHILE SECUENCIALMENTE HAREMOS
		
		//COMIENZAN LAS BLANCAS
		//1: Se solicita a board las piezas del usuario que lleva las blancas
		//2: Para cada pieza se solicitan sus movimientos disponibles a board
		//3: Enviamos toda esa informacion a userB EN FORMA DE OBJETOS (podría ser, por ejemplo un ArrayList<Map<Pieza, Posicion>>)
		//4: Recibiremos del usuario un objeto Pieza y uno Posicion que nos indica qué pieza quiere mover a donde
		//5: Utilizando las funciones de board, movemos la pieza como desea el usuario
		//6: Comprobamos quienGana() para ver si la partida ha finalizado
		//7: Actualizamos la lista Users de Server con las nuevas puntuaciones
		
		//REALIZAMOS EL MISMO PROCESO CON LAS NEGRAS
		
		//AL DETECTAR SI ALGUIEN HA GANADO DE DESCONECTARÁN LOS USUARIOS
		//1:Mandamos a ambos usuarios un String informando del resultado
		//2:Cerramos ambos sockets
	}

}
