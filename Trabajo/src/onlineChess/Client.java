package onlineChess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Client {
	
	private static Socket s = null;
	private static Interfaz interfaz = null;
	
	private static final Object lock = new Object();
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		
		try {
			s = new Socket("localhost", 55555);
			
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
					
					String estado = ois.readLine();
					if(estado.equals("EXISTE")) {

						System.out.println("\n\n------------HISTORIAL DE PARTIDAS------------");
						String linea = ois.readLine();
			            // Leemos el archivo y enviamos los datos en bloques
			            while (!linea.equals("FIN")) {
			                System.out.println(linea);
			                linea = ois.readLine();
			            }
						System.out.println("---------------------------------------------\n\n");
						
					}else{
						System.out.println("Usted no ha jugado ninguna partida, no dispone de historial");
					}
					break;
				case "LISTADO PUNTUACIONES":
					oos.writeBytes(opcion+"\n");
					oos.flush();
					
					System.out.println("\n\n------------RANKING------------");
					Map<String, Integer> puntuaciones = (Map<String, Integer>) ois.readObject();
					for (Map.Entry<String, Integer> m : puntuaciones.entrySet()) {
			            System.out.println(m.getKey() + ": " + m.getValue());
			        }
					System.out.println("-------------------------------\n\n");

					break;
				}

				opcion = getOpcion(scanner);
				
			}
			
			oos.writeBytes("DESCONECTAR\n");
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if(oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
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
		return scanner.nextLine();
		//return "UNIRME A PARTIDA";
	}
	
	static void play(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
		System.out.println("Conectandose a sala...");
		
		//AQUI EL USUARIO SE QUEDA A LA ESPERA AL MENSAJE DE CONFIRMACIÓN DE INICIO DE PARTIDA
		String roomResponse = ois.readLine(); 
		
		if(roomResponse.equals("START")) {
			oos.writeBytes("OK\n");
			oos.flush();
			
			// Creamos la interfaz del tablero
			interfaz = new Interfaz();
			
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
			
			Board tablero = null;
			
			//COMIENZA LA PARTIDA
			
			//La variable resultado nos va actualizando del estado de la partida, si es CONTINUA significa que la partida sigue en pie, de lo contrario recibiremos GANA o PIERDE
			String resultado = null;
			while((resultado = ois.readLine()) != null) {
				if(resultado.equals("CONTINUA")) {
					System.out.println("Es su turno");
					
					// Leemos el tablero que proviene de la sala				
					tablero = (Board) ois.readObject();
					
					interfaz.recibirActualizacionTablero(tablero, true);
					
					// Obtenemos el movimiento desde la interfaz
					obtenerMovimiento();
					
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
						
						System.out.println("Pieza movida de " + from.toString() + " a " + to.toString());
						
						// RESETEAMOS LOS MOVIMIENTOS DE LA INTERFAZ
					}else {
						System.out.println("DESCONECTAMOS");
						oos.writeBytes("DESCONECTAR\n"); //le indicamos a la sala que seguimos jugando (esto hay que hacerlo ya que tenemos la opcion de parar la partida)
						oos.flush();
						break;
					}
					oos.flush();

					interfaz.setPosiciones(null, null);
					
					// Actualizamos el turno y repintamos
					interfaz.recibirActualizacionTablero(interfaz.getTablero(), false);
				}else {
					break;
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
			
			interfaz.dispose();
		}else {
			System.out.println("Error en la conexión a la sala");
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
