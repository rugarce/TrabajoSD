package onlineChess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
		
		ObjectInputStream oisB = null;
		ObjectInputStream oisN = null;
		
		ObjectOutputStream oosB = null;
		ObjectOutputStream oosN = null;
		
		try {
            latch.await(); // Espera hasta que el contador sea cero
            System.out.println("Ambos usuarios están presentes. Comienza la partida.");
            
            oisB = new ObjectInputStream(socketUserB.getInputStream());
    		oisN = new ObjectInputStream(socketUserN.getInputStream());
    		
    		oosB = new ObjectOutputStream(socketUserB.getOutputStream());
    		oosN = new ObjectOutputStream(socketUserB.getOutputStream());
            
    		//les indicamos a los usuarios que la partida empieza
    		oosB.writeBytes("START\n");
    		oosN.writeBytes("START\n");
    		
    		//les indicamos a cada usuario de que lado jugaran
    		oosB.writeBoolean(true);
    		oosN.writeBoolean(false);
    		
    		oosB.flush();
    		oosN.flush();
    		
    		boolean lado = false;
    		
    		while(board.quienGana() == null) {
    			if(lado) {
    				oosB.writeBytes("CONTINUA\n");
    				
    				//OJO enviamos el tablero solo para que lo lea, solo nos interesa la posición que nos devuelve
	    			oosB.writeObject(board);
	    			oosB.flush();
	    			
	    			String action = oisB.readLine();
	    			if(action == "DESCONECTAR") {
	    				System.out.println("Las blancas solicitan desconectarse");
	    				//AQUI GESTIONAMOS LA DESCONEXION
	    			}
	    			
	    			Posicion from = (Posicion) oisB.readObject();
	    			Posicion to = (Posicion) oisB.readObject();
	    			
	    			Pieza pieza = board.getTablero(from);
	    			Pieza piezaComida = board.moverPieza(pieza, to);

    				System.out.println("Las blancas mueven el " + pieza.getNombre() + " de " + from.toString() + " a " + to.toString());
	    			if(piezaComida != null) {
	    				System.out.println("Las blancas se comen a " + piezaComida.getNombre());
	    			}
    			}else {
    				oosN.writeBytes("CONTINUA\n");
    				
    				oosN.writeObject(board);
	    			oosN.flush();
	    			
	    			String action = oisN.readLine();
	    			if(action == "DESCONECTAR") {
	    				System.out.println("Las negras solicitan desconectarse");
	    				//AQUI GESTIONAMOS LA DESCONEXION
	    			}
	    		
	    			Posicion from = (Posicion) oisN.readObject();
	    			Posicion to = (Posicion) oisN.readObject();
	    			
	    			Pieza pieza = board.getTablero(from);
	    			Pieza piezaComida = board.moverPieza(pieza, to);
	    			
	    			System.out.println("Las negras mueven el " + pieza.getNombre() + " de " + from.toString() + " a " + to.toString());
	    			if(piezaComida != null) {
	    				System.out.println("Las negras se comen a " + piezaComida.getNombre());
	    			}
	    			
	    			//Almacenamos el nuevo movimiento en los ficheros nombreusuariobancas.txt y nombreusuationegras.txt
	        		
	    			//-- OJO--
	    			//Será importante al escribir en el fichero, no borrar lo que ya está escrito, el fichero funcionará como una especie de log
	    			//La estructura del contenido del fichero puede ser algo así como
	    			
	    			//nombreusuario.txt
	    			//NUEVA PARTIDA nombreusuarionegras VS nombreusuarioblancas
	    			//nombreusuarionegras Alfil (0,0) -> (1,2) 
	    			//nombreusuarioblancas Peon (1,1) -> (2,2) 
	    			
	    			//para hacer el tema del fichero creo que tendremos que utilizar un fileoutputstream
    			}
    			
    			lado = !lado;
    		}
    		
    		if(board.quienGana() == true) {
    			oosN.writeBytes("PIERDE\n");
    			oosB.writeBytes("GANA\n");
    			
    			Server.users.get(userN.getNombre()).setPuntuacion(userN.getPuntuacion() - 10);
    			Server.users.get(userB.getNombre()).setPuntuacion(userB.getPuntuacion() + 10);
    		}else {
    			oosN.writeBytes("GANA\n");
    			oosB.writeBytes("PIERDE\n");
    			
    			Server.users.get(userN.getNombre()).setPuntuacion(userN.getPuntuacion() + 10);
    			Server.users.get(userB.getNombre()).setPuntuacion(userB.getPuntuacion() - 10);
    		}
    		
    		//DESCONECTAMOS
            
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("El hilo fue interrumpido.");
        } catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
