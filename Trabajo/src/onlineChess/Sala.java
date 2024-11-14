package onlineChess;

import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class Sala implements Runnable{

	private Socket socketUserB; //socket del ususario de las piezas blancas
	private Socket socketUserN; //socket del ususario de las piezas negras
	
	private Usuario userB;
	private Usuario userN;
	
	private Board board;
	
	 private CountDownLatch latch = new CountDownLatch(1);
	
	//el primer usuario en entrar a la sala maneja las blancas
	Sala(Socket socketUserB, Usuario userB){
		this.socketUserB = socketUserB;
		this.userB = userB;
		
		this.board = new Board();
	}
	
	public synchronized  void unirseNegras(Socket socketUserN, Usuario userN) {
		this.socketUserN = socketUserN;
		this.userN = userN;
		
		latch.countDown(); //reducimos el contador, indicando que podemos comenzar la partida
	}
	
	@Override
	public void run() {
		System.out.println("SALA INCIADA, A LA ESPERA DE LLENARSE PARA EMPEZAR");
		try {
            latch.await(); // Espera hasta que el contador sea cero
            System.out.println("Ambos usuarios están presentes. Comienza la partida.");
            
            //logica de la sala
    		
    		//AQUI EN UN WHILE SECUENCIALMENTE HAREMOS
    		
    		//COMIENZAN LAS BLANCAS
    		//1: Se solicita a board las piezas del usuario que lleva las blancas
    		//2: Para cada pieza se solicitan sus movimientos disponibles a board
    		//3: Enviamos toda esa informacion a userB EN FORMA DE OBJETOS (podría ser, por ejemplo un ArrayList<Map<Pieza, Posicion>>)
    		//4: Recibiremos del usuario un objeto Pieza y uno Posicion que nos indica qué pieza quiere mover a donde
    		//5: Utilizando las funciones de board, movemos la pieza como desea el usuario
    		//6: Almacenamos el nuevo movimiento en los ficheros nombreusuariobancas.txt y nombreusuationegras.txt
    		
    			//-- OJO--
    			//Será importante al escribir en el fichero, no borrar lo que ya está escrito, el fichero funcionará como una especie de log
    			//La estructura del contenido del fichero puede ser algo así como
    			
    			//nombreusuario.txt
    			//NUEVA PARTIDA nombreusuarionegras VS nombreusuarioblancas
    			//nombreusuarionegras Alfil (0,0) -> (1,2) 
    			//nombreusuarioblancas Peon (1,1) -> (2,2) 
    			
    			//para hacer el tema del fichero creo que tendremos que utilizar un fileoutputstream
    		
    		
    		//6: Comprobamos quienGana() para ver si la partida ha finalizado
    		//7: Actualizamos la lista Users de Server con las nuevas puntuaciones
    		
    		//REALIZAMOS EL MISMO PROCESO CON LAS NEGRAS
    		
    		//AL DETECTAR SI ALGUIEN HA GANADO DE DESCONECTARÁN LOS USUARIOS
    		//1:Mandamos a ambos usuarios un String informando del resultado
    		//2:Cerramos ambos sockets
            
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("El hilo fue interrumpido.");
        }
	}

}
