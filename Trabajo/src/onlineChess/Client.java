package onlineChess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		Socket s = null;
		
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
			

			String opcion = getOpcion(scanner);

			while (opcion != "DESCONECTAR") {
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
			
			//COMIENZA LA PARTIDA
			
			//La variable resultado nos va actualizando del estado de la partida, si es CONTINUA significa que la partida sigue en pie, de lo contrario recibiremos GANA o PIERDE
			String resultado = null;
			while((resultado = ois.readLine()) != null) {
				if(resultado.equals("CONTINUA")) {
					System.out.println("Es su turno");
					Board tablero = (Board) ois.readObject();
					
					ArrayList<Pieza> piezas = tablero.getPiezas(lado);
					ArrayList<Posicion> posiciones = tablero.getMovimientosPosibles(piezas.get(8));
					
					/*AQUI LA INTERFAZ MUESTRA EL TABLERO, LAS POSICIONES DE TODAS LAS FICHAS*/
					/*MEDIANTE LOS METODOS DE tablero.getPiezas() y tablero.getMovimientosPosibles() MOSTRAMOS AL USUARIO LOS BOTONES DE LOS MOVIMIENTOS QUE PUEDE HACER*/
					/*UNA VEZ PULSADOS RECIBIMOS UN OBJETO Posicion FROM (posicion que queremos mover) Y OTRO OBJETO Posicion TO QUE INDICA A DONDE QUEREMOS MOVER*/
					
					System.out.println(tablero.toString());
					
					for(int i = 0; i < piezas.size(); i++) {
						System.out.println(piezas.get(i).getNombre() + " " + piezas.get(i).getPosicion().toString());
					}
					
					boolean continuar = true;
					Posicion from = piezas.get(8).getPosicion();
					Posicion to = posiciones.get(0);
					
					System.out.println("Movido " + piezas.get(8).getNombre() + " de " + from.toString() + " a " + to.toString());
					
					if(continuar) {
						oos.writeBytes("SEGUIR JUGANDO\n"); //le indicamos a la sala que seguimos jugando (esto hay que hacerlo ya que tenemos la opcion de parar la partida)
						oos.writeObject(from);
						oos.reset();
						oos.writeObject(to);
						oos.reset();
					}else {
						oos.writeBytes("DESCONECTAR\n"); //le indicamos a la sala que seguimos jugando (esto hay que hacerlo ya que tenemos la opcion de parar la partida)
					}
					oos.flush();
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
}
