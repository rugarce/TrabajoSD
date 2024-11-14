package onlineChess;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AtenderUsuario implements Runnable {

	Socket s;
	
	AtenderUsuario(Socket s){
		this.s = s;
	}
	
	@Override
	public void run() {
		System.out.println("Inicio");
		
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;

		try {
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
			
			
			String username = ois.readLine();

			System.out.println("Recibido el nombre de usuario "+username);
			
			if(Server.connectedUsers.contains(username)) {
				oos.writeBytes("ERROR:usuario ya conectado\n");
				oos.flush();
				return;
			}
			
			Usuario user = null;
			if(!Server.users.contains(username)) {
				user = new Usuario(username, 0);
				oos.writeBytes("OK:nuevo usuario registrado\n");
			}else {
				user = Server.users.get(username);
				oos.writeBytes("OK:usuario logeado con éxito\n");
			}
			oos.flush();
			
			/*
			AQUI SE DEBE RECIBIR UNA LÍNEA QUE INDIQUE QUE QUEREMOS HACER, SI QUEREMOS UNIRNOS A UNA PARTIDA, MOSTRAR LA PUNTUACION, OBTENER EL HISTORIAL DE PARTIDAS ETC 
			*/
			
			
			String action = action = ois.readLine();
		
			/*
			* 
			*/
			
			boolean waitingToFinish = false;
			
			while (action != "DESCONECTAR" && !waitingToFinish) {
				switch(action) {
					case ("UNIRME A PARTIDA"):
						Sala sala = null;
						Thread t = null;	
						if(Server.waitingSalas.size() != 0) {
							//si hay salas con usuarios esperando a ser emparejados se mete al usuario en la sala creada más vieja
							sala = Server.waitingSalas.get(0);
							t = Server.waitingSalasThreads.get(0);

							Server.runningSalas.add(sala);
							Server.runningSalasThreads.add(t);
							
							Server.waitingSalas.remove(0);
							Server.waitingSalasThreads.remove(0);

							sala.unirseNegras(s, user);
							//NOS UNIMOS AL HILO INICIADO Y EMPIEZA LA PARTIDA
							
							t.join();
							//LA PARTIDA HA ACABADO
						}else {
							//si no hay salas esperando se mete al usuario en una a la espera de emparejarse
							sala = new Sala(s, user);
							t = new Thread(sala);
							
							Server.waitingSalas.add(sala);
							Server.waitingSalasThreads.add(t);

							//INICIAMOS EL HILO
							System.out.println("Iniciando sala...");
							t.start();
							
							t.join();
							System.out.println("Partida finalizada...");
							//LA PARTIDA HA ACABADO
						}
					break;
					case("OBTENER HISTORIAL"):
						mostrarHistorial(s, user);
					break;
					case("LISTADO PUNTUACIONES"):
						listadoPuntuaciones(oos, user);
					break;
				}
				
				if(!waitingToFinish) {
					action = ois.readLine();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(s != null) {
				try {
					s.close();
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
			
			if(oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	static void mostrarHistorial(Socket s, Usuario user) {
		//a través del socket accede a un fichero llamado MUCHO OJO "nombredeusuario.txt" que estará almacenado en alguna carpeta que ceemos asociado al usuario (todavía no esta creado) que contiene su historial de partidas y le manda todo el contenido al usuario que de lo descargara 
		//enviaremos el fichero en forma de bytes, necesitamos crear un dataOutputStream a partir del socket y enviarle todo el fichero en forma de bytes con lo del buffer
	}
	
	static void listadoPuntuaciones(ObjectOutputStream oos, Usuario user) {
		//a través del socket, le mandará un Map<String, Integer> al usuario con la información de todos los usuarios y sus puntuaciones
		//OJO LE ENVIAREMOS UN OBJETO oos.writeObject(Map<String, Integer>);
		
	}

}
