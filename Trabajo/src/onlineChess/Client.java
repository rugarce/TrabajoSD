package onlineChess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		
		Socket s = null;
		
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		
		try {
			s = new Socket("localhost", 55555);

			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
			
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
		return scanner.nextLine();
	}
	
	static void play(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
		//AQUI EL USUARIO SE QUEDA A LA ESPERA AL MENSAJE DE CONFIRMACIÓN DE INICIO DE PARTIDA
		String roomResponse = ois.readLine(); 
		
		if(roomResponse.equals("START")) {
			//LEEMOS EL LADO DEL QUE JUGAREMOS
			Boolean lado = ois.readBoolean();
			
			//COMIENZA LA PARTIDA
			
			//La variable resultado nos va actualizando del estado de la partida, si es CONTINUA significa que la partida sigue en pie, de lo contrario recibiremos GANA o PIERDE
			String resultado = null;
			
			while((resultado = ois.readLine()) == "CONTINUA") {
				Board tablero = (Board) ois.readObject();
				
				/*AQUI LA INTERFAZ MUESTRA EL TABLERO, LAS POSICIONES DE TODAS LAS FICHAS*/
				/*MEDIANTE LOS METODOS DE tablero.getPiezas() y tablero.getMovimientosPosibles() MOSTRAMOS AL USUARIO LOS BOTONES DE LOS MOVIMIENTOS QUE PUEDE HACER*/
				/*UNA VEZ PULSADOS RECIBIMOS UN OBJETO Posicion FROM (posicion que queremos mover) Y OTRO OBJETO Posicion TO QUE INDICA A DONDE QUEREMOS MOVER*/
				boolean continuar = true;
				Posicion from = null;
				Posicion to = null;
				
				if(continuar) {
					oos.writeBytes("SEGUIR JUGANDO\n"); //le indicamos a la sala que seguimos jugando (esto hay que hacerlo ya que tenemos la opcion de parar la partida)
					oos.writeObject(from);
					oos.writeObject(to);
				}else {
					oos.writeBytes("DESCONECTAR\n"); //le indicamos a la sala que seguimos jugando (esto hay que hacerlo ya que tenemos la opcion de parar la partida)
				}
				oos.flush();
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
