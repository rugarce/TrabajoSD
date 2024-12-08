package onlineChess;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class Sala implements Runnable{
	
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
            fileOutputB = new FileOutputStream(userB.getNombre() + ".txt", true); 
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
            fileOutputN = new FileOutputStream(userN.getNombre() + ".txt", true);
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
		
		try {
            latch.await(); // Espera hasta que el contador sea cero
            System.out.println("Ambos usuarios están presentes. Comienza la partida.");
    		
    		//les indicamos a los usuarios que la partida empieza
    		oosB.writeBytes("START\n");
    		oosN.writeBytes("START\n");

    		oosB.flush();
    		oosN.flush();
    		
    		System.out.println("Respuesta Acknowledge de las blancas:" + oisB.readLine());
    		System.out.println("Respuesta Acknowledge de las negras:" + oisN.readLine());
    		
    		//les indicamos a cada usuario de que lado jugaran
    		oosB.writeBoolean((Boolean)true);
    		oosN.writeBoolean((Boolean)false);
    		
    		oosB.flush();
    		oosN.flush();
    		
    		// Registrar inicio de la partida en los archivos de los usuarios
            escribirEnHistorial(fileOutputB, "NUEVA PARTIDA " + userB.getNombre() + " BLANCAS VS " + userN.getNombre() + " NEGRAS\n");
            escribirEnHistorial(fileOutputN, "NUEVA PARTIDA " + userB.getNombre() + " BLANCAS VS " + userN.getNombre() + " NEGRAS\n");
    		
    		boolean lado = true;
    		Boolean quienGana = board.quienGana();
    		
    		while(quienGana == null) {
    			if(lado) {
    				System.out.println("Turno blancas");
    				
    				oosB.writeBytes("CONTINUA\n");
    				
    				//OJO enviamos el tablero solo para que lo lea, solo nos interesa la posición que nos devuelve
	    			oosB.writeObject(board);
	    			oosB.reset();
	    			oosB.flush();
	    			
	    			String action = oisB.readLine();
	    			System.out.println("Leido "+action);
	    			if(action.equals("DESCONECTAR")) {
	    				System.out.println("Las blancas solicitan desconectarse");
	    				quienGana = false;
	    				break;
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
    				System.out.println("Turno negras");
    				
    				oosN.writeBytes("CONTINUA\n");
    				
    				oosN.writeObject(board);
    				oosN.reset();
	    			oosN.flush();
	    			
	    			String action = oisN.readLine();
	    			System.out.println("Leido "+action);
	    			if(action.equals("DESCONECTAR")) {
	    				System.out.println("Las negras solicitan desconectarse");
	    				quienGana = true;
	    				break;
	    			}else {
	    				System.out.println("Las negras solicitan continuar");
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
    			quienGana = board.quienGana();
    		}
    		
    		if(quienGana == true) {
    			oosN.writeBytes("PIERDE\n");
    			oosB.writeBytes("GANA\n");
    			
    			actualizarPuntuaciones(userN.getNombre(), userB.getNombre(), 0, 32);
    			
    			escribirEnHistorial(fileOutputB, "FIN DE LA PARTIDA: " + userB.getNombre() + " GANA\n");
                escribirEnHistorial(fileOutputN, "FIN DE LA PARTIDA: " + userN.getNombre() + " PIERDE\n");
    		}else {
    			oosN.writeBytes("GANA\n");
    			oosB.writeBytes("PIERDE\n");

    			actualizarPuntuaciones(userN.getNombre(), userB.getNombre(), 1, 32);
    			
    			escribirEnHistorial(fileOutputB, "FIN DE LA PARTIDA: " + userB.getNombre() + " PIERDE\n");
                escribirEnHistorial(fileOutputN, "FIN DE LA PARTIDA: " + userN.getNombre() + " GANA\n");
    		}
    		
    		oosN.flush();
    		oosB.flush();
    		
    		
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
	
	public static void actualizarPuntuaciones(String nombreA, String nombreB, double resultadoA, int kFactor) {
        double puntuacionA = Server.users.get(nombreA).getPuntuacion();
        double puntuacionB = Server.users.get(nombreB).getPuntuacion();

        double EA = 1 / (1 + Math.pow(10, (puntuacionB - puntuacionA) / 400));
        double EB = 1 / (1 + Math.pow(10, (puntuacionA - puntuacionB) / 400));

        double resultadoB = 1 - resultadoA;

        double nuevaPuntuacionA = puntuacionA + kFactor * (resultadoA - EA);
        double nuevaPuntuacionB = puntuacionB + kFactor * (resultadoB - EB);

        Server.users.get(nombreA).setPuntuacion((int) nuevaPuntuacionA);
        Server.users.get(nombreB).setPuntuacion((int) nuevaPuntuacionB);
    }
}
