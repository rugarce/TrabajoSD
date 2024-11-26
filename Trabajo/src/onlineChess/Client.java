package onlineChess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Client {
	
	private static Socket s = null;
	private static Interfaz interfaz = null;
	
	private static final Object lock = new Object();
	
	public Client() { // Creamos un cliente asociado a la interfaz
		this.interfaz = new Interfaz();
	}
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		
		try {
			s = new Socket("localhost", 55555);
			
			Client cliente = new Client(); // Creamos el cliente asociandolo a una interfaz y un socket
			
			ois = new ObjectInputStream(s.getInputStream());
			oos = new ObjectOutputStream(s.getOutputStream());
			
			System.out.println("Ingrese su nombre de usuario:");
			String username = scanner.nextLine();
			
			oos.writeBytes(username+"\n");
			oos.flush();
			
			String loginResponse = ois.readLine();
			
			if(loginResponse.startsWith("OK:")) {
				System.out.println(loginResponse);
			}else {
				if(loginResponse.startsWith("ERROR:")) {
					System.out.println(loginResponse);
					return;
				}else {
					System.out.println("ERROR:Error desconocido");
					return;
				}
			}
			
			String opcion = getOpcion(scanner); // Nos unimos a la partida automáticamente

			while (!opcion.equals("DESCONECTAR")) {
				switch (opcion) {
				case "UNIRME A PARTIDA":
					oos.writeBytes(opcion+"\n");
					oos.flush();
					
					play(ois, oos);
					
					break;
				case "OBTENER HISTORIAL":
					oos.writeBytes(opcion+"\n");
					oos.flush();
					break;
				case "LISTADO PUNTUACIONES":
					oos.writeBytes(opcion+"\n");
					oos.flush();
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(s != null) {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	static String getOpcion(Scanner scanner) {
		System.out.println("Qué desea hacer?");
		System.out.println("-UNIRME A PARTIDA");
		System.out.println("-OBTENER HISTORIAL");
		System.out.println("-LISTADO PUNTUACIONES");
		System.out.println("-DESCONECTAR");
		//return scanner.nextLine();
		return "UNIRME A PARTIDA";
	}
	
	static void play(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
		System.out.println("Conectandose a sala...");
		
		//AQUI EL USUARIO SE QUEDA A LA ESPERA AL MENSAJE DE CONFIRMACIÓN DE INICIO DE PARTIDA
		String roomResponse = ois.readLine(); 
		
		if(roomResponse.equals("START")) {
			oos.writeBytes("OK\n");
			oos.flush();
			
			System.out.println("Comienza la partida");
			
			//LEEMOS EL LADO DEL QUE JUGAREMOS
			Boolean lado = ois.readBoolean();
			if(lado) {
				System.out.println("Su lado son las blancas");
			}else {
				System.out.println("Su lado son las negras");
			}
			
			// ASIGNAMOS EL LADO EN EL QUE JUGAREMOS A LA INTERFAZ
			interfaz.asignarLado(lado);
			
			// ACTIVAMOS EL BOTÓN DE DESCONEXIÓN
			interfaz.activarBotonDesconectar();
			
			//COMIENZA LA PARTIDA
			
			//La variable resultado nos va actualizando del estado de la partida, si es CONTINUA significa que la partida sigue en pie, de lo contrario recibiremos GANA o PIERDE
			String resultado = null;
			while((resultado = ois.readLine()) != null) {
				if(resultado.equals("CONTINUA")) {
					System.out.println("Es su turno");
					
					// Leemos el tablero que proviene de la sala				
					Board tablero = (Board) ois.readObject();
					System.out.println("recibido tablero");
					
					interfaz.recibirActualizacionTablero(tablero);
					System.out.println("tablero actualizado");
					
					// Obtenemos el movimiento desde la interfaz
					obtenerMovimiento();

					System.out.println("movimiento obtenido");
					
					boolean continuar = !interfaz.getDesconexion();
					
					// GUARDAMOS LOS MOVIMIENTOS HECHOS EN LA INTERFAZ
					Posicion from = interfaz.getOrigen();
					Posicion to = interfaz.getDestino();
					
					if(continuar) {
						System.out.println("CONTINUAMOS");
						oos.writeBytes("SEGUIR JUGANDO\n"); //le indicamos a la sala que seguimos jugando (esto hay que hacerlo ya que tenemos la opcion de parar la partida)
						oos.writeObject(from);
						oos.reset();
						oos.writeObject(to);
						oos.reset();
						
						System.out.println("Enviado al servidor movimiento de " + from.toString() + " a " + to.toString());
						
						// RESETEAMOS LOS MOVIMIENTOS DE LA INTERFAZ
					}else {
						System.out.println("DESCONECTAMOS");
						oos.writeBytes("DESCONECTAR\n"); //le indicamos a la sala que seguimos jugando (esto hay que hacerlo ya que tenemos la opcion de parar la partida)
					}
					oos.flush();

					interfaz.setPosiciones(null, null);
				}
			}
			
			if(resultado.equals("PIERDE") || resultado.equals("GANA")) {
				if(resultado.equals("PIERDE")) {
					System.out.println("Ha ganado, felicidades");
				}
				
				if(resultado.equals("GANA")) {
					System.out.println("Ha perdido");
				}
			}
			
		}else {
			System.out.println("ERROR:Error en la conexión a la sala");
		}
	}
	
	// Método para esperar el movimiento desde la interfaz
	private static void esperarMovimientoDesdeInterfaz() {
	    synchronized (lock) {
	        try {
	            // Bloquea el cliente hasta que la interfaz notifique un movimiento
	            lock.wait();
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            System.err.println("La espera fue interrumpida.");
	        }
	    }
	}

	// Método que la interfaz debe invocar para enviar un movimiento
	public static void enviarMovimientoDesdeInterfaz(Posicion origen, Posicion destino) {
	    synchronized (lock) {
	    	
	        // ACTUALIZA POSICIONES
	        interfaz.setPosiciones(origen, destino);

	        // Notifica al cliente que hay un movimiento disponible
	        lock.notify();
	    }
	}

	// Método para obtener el movimiento en jugar()
	private static void obtenerMovimiento() {
		
	    esperarMovimientoDesdeInterfaz(); // Espera hasta que la interfaz notifique
	}
}
