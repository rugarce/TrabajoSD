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
	
	static void play(ObjectInputStream ois, ObjectOutputStream oos) throws IOException {
		//AQUI EL USUARIO SE QUEDA A LA ESPERA AL MENSAJE DE CONFIRMACIÓN DE INICIO DE PARTIDA
		String roomResponse = ois.readLine(); 
		
		if(roomResponse.equals("START")) {
			//COMIENZA LA PARTIDA
			
		}else {
			System.out.println("ERROR:Error en la conexión a la sala");
		}
	}
}
