package onlineChess;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class Sala implements Runnable{

	//private Socket socketUserB; //socket del ususario de las piezas blancas
	//private Socket socketUserN; //socket del ususario de las piezas negras
	
	ObjectInputStream oisB = null;
	ObjectInputStream oisN = null;
	
	ObjectOutputStream oosB = null;
	ObjectOutputStream oosN = null;
	
	private Usuario userB;
	private Usuario userN;
	
	private Board board;
	
	private CountDownLatch latch = new CountDownLatch(1);
	 
	// Archivos de historial para cada jugador
    private FileOutputStream fileOutputB;
    private FileOutputStream fileOutputN;
	
	//el primer usuario en entrar a la sala maneja las blancas
	Sala(Usuario user, ObjectInputStream ois, ObjectOutputStream oos){
		//this.socketUserB = socketUserB;
		this.oisB = ois;
		this.oosB = oos;
		this.userB = user;
		
		this.board = new Board(); // Crea el tablero de la partida
		
		// Crear archivo de historial para el usuario de las blancas
		try {
            fileOutputB = new FileOutputStream(userB.getNombre() + "Blancas.txt", true); 
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public synchronized  void unirseNegras(Usuario user, ObjectInputStream ois, ObjectOutputStream oos) {
		this.oisN = ois;
		this.oosN = oos;
		this.userN = user;
		
		// Crear archivo de historial para el usuario de las negras
        try {
            fileOutputN = new FileOutputStream(userN.getNombre() + "Negras.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		latch.countDown(); //reducimos el contador, indicando que podemos comenzar la partida
	}
	
	// Método para escribir en el archivo de historial
    public void escribirEnHistorial(FileOutputStream fileOutputStream, String texto) {
        try {
            fileOutputStream.write(texto.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	@Override
	public void run() {
		System.out.println("SALA INCIADA, A LA ESPERA DE LLENARSE PARA EMPEZAR");
		
		/*ObjectInputStream oisB = null;
		ObjectInputStream oisN = null;
		
		ObjectOutputStream oosB = null;
		ObjectOutputStream oosN = null;*/
		
		try {
            latch.await(); // Espera hasta que el contador sea cero
            System.out.println("Ambos usuarios están presentes. Comienza la partida.");

    		//oosB = new ObjectOutputStream(socketUserB.getOutputStream());
    		//oosN = new ObjectOutputStream(socketUserN.getOutputStream());
    		
    		//les indicamos a los usuarios que la partida empieza
    		oosB.writeBytes("START\n");
    		oosN.writeBytes("START\n");

    		oosB.flush();
    		oosN.flush();
    		
            //oisB = new ObjectInputStream(socketUserB.getInputStream());
    		//oisN = new ObjectInputStream(socketUserN.getInputStream());
    		
    		System.out.println("Respuesta Acknowledge de las blancas:" + oisB.readLine());
    		System.out.println("Respuesta Acknowledge de las negras:" + oisN.readLine());
    		
    		//les indicamos a cada usuario de que lado jugaran
    		oosB.writeBoolean((Boolean)true);
    		oosN.writeBoolean((Boolean)false);
    		
    		oosB.flush();
    		oosN.flush();
    		
    		// Registrar inicio de la partida en los archivos de los usuarios
            escribirEnHistorial(fileOutputB, "NUEVA PARTIDA " + userB.getNombre() + " VS " + userN.getNombre() + "\n");
            escribirEnHistorial(fileOutputN, "NUEVA PARTIDA " + userB.getNombre() + " VS " + userN.getNombre() + "\n");
    		
    		boolean lado = false;
    		
    		while(board.quienGana() == null) {
    			if(lado) {
    				oosB.writeBytes("CONTINUA\n");
    				
    				//OJO enviamos el tablero solo para que lo lea, solo nos interesa la posición que nos devuelve
	    			oosB.writeObject(board);
	    			oosB.reset();
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
    				String movimiento = userB.getNombre() + " " + pieza.getNombre() + " " + from.toString() + " -> " + to.toString() + "\n";
                    escribirEnHistorial(fileOutputB, movimiento); // Registrar movimiento
	    			if(piezaComida != null) {
	    				System.out.println("Las blancas se comen a " + piezaComida.getNombre());
	    				String comida = userB.getNombre() + " se come a " + piezaComida.getNombre() + "\n";
                        escribirEnHistorial(fileOutputB, comida); // Registrar comida
	    			}
    			}else {
    				oosN.writeBytes("CONTINUA\n");
    				
    				oosN.writeObject(board);
    				oosN.reset();
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
	    			String movimiento = userN.getNombre() + " " + pieza.getNombre() + " " + from.toString() + " -> " + to.toString() + "\n";
                    escribirEnHistorial(fileOutputN, movimiento); // Registrar movimiento
	    			if(piezaComida != null) {
	    				System.out.println("Las negras se comen a " + piezaComida.getNombre());
	    				String comida = userN.getNombre() + " se come a " + piezaComida.getNombre() + "\n";
                        escribirEnHistorial(fileOutputN, comida); // Registrar comida
	    			}
    			}
    			
    			lado = !lado;
    		}
    		
    		if(board.quienGana() == true) {
    			oosN.writeBytes("PIERDE\n");
    			oosB.writeBytes("GANA\n");
    			
    			Server.users.get(userN.getNombre()).setPuntuacion(userN.getPuntuacion() - 10);
    			Server.users.get(userB.getNombre()).setPuntuacion(userB.getPuntuacion() + 10);
    			
    			escribirEnHistorial(fileOutputB, "FIN DE LA PARTIDA: " + userB.getNombre() + " GANA\n");
                escribirEnHistorial(fileOutputN, "FIN DE LA PARTIDA: " + userN.getNombre() + " PIERDE\n");
    		}else {
    			oosN.writeBytes("GANA\n");
    			oosB.writeBytes("PIERDE\n");
    			
    			Server.users.get(userN.getNombre()).setPuntuacion(userN.getPuntuacion() + 10);
    			Server.users.get(userB.getNombre()).setPuntuacion(userB.getPuntuacion() - 10);
    			
    			escribirEnHistorial(fileOutputB, "FIN DE LA PARTIDA: " + userB.getNombre() + " PIERDE\n");
                escribirEnHistorial(fileOutputN, "FIN DE LA PARTIDA: " + userN.getNombre() + " GANA\n");
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
