package onlineChess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

	static ConcurrentHashMap<String, Usuario> users = new ConcurrentHashMap<String, Usuario>();
	
	static ArrayList<String> connectedUsers = new ArrayList<String>();
	
	static ArrayList<Sala> runningSalas = new ArrayList<Sala>();
	static ArrayList<Thread> runningSalasThreads = new ArrayList<Thread>();
	static ArrayList<Sala> waitingSalas = new ArrayList<Sala>();
	static ArrayList<Thread> waitingSalasThreads = new ArrayList<Thread>();
	
	/*public static void main(String[] args) {
		ExecutorService pool = null;
		try(ServerSocket soc = new ServerSocket(55555)) {
			pool = Executors.newCachedThreadPool();
			Socket s = null;
			ObjectInputStream ois = null;
			ObjectOutputStream oos = null;
			
			while(!Thread.interrupted()) {
				try {
					s = soc.accept();
					
					ois = new ObjectInputStream(s.getInputStream());
					oos = new ObjectOutputStream(s.getOutputStream());
					
					String username = ois.readLine();
					
					if(connectedUsers.contains(username)) {
						oos.writeBytes("ERROR:usuario ya conectado\n");
						oos.flush();
						break;
					}
					
					Usuario user = null;
					if(!users.contains(username)) {
						user = new Usuario(username, 0);
						oos.writeBytes("OK:nuevo usuario registrado\n");
					}else {
						user = users.get(username);
						oos.writeBytes("OK:usuario logeado con éxito\n");
					}
					oos.flush();
					
					String action = ois.readLine();
					
					switch(action) {
						case ("UNIRME A PARTIDA"):
							Sala sala = null;
							if(waitingSalas.size() != 0) {
								//si hay salas con usuarios esperando a ser emparejados se mete al usuario en la sala creada más vieja
								sala = waitingSalas.get(0);
								
								sala.unirseNegras(s, user);
								
								runningSalas.add(sala);
								waitingSalas.remove(0);
								
								pool.execute(sala);
								//EJECUTAMOS EL HILO PARA QUE EMPIECE LA PARTIDA
							}else {
								//si no hay salas esperando se mete al usuario en una a la espera de emparejarse
								sala = new Sala(s, user);
								waitingSalas.add(sala);
							}
						break;
						case("OBTENER HISTORIAL"):
							mostrarHistorial(s, user);
						break;
						case("LISTADO PUNTUACIONES"):
							listadoPuntuaciones(oos, user);
						break;
					}
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(pool != null) {
				pool.shutdown();
			}
		}
	}*/
	
	
	public static void main(String[] args) {
		ExecutorService pool = null;
		try(ServerSocket soc = new ServerSocket(55555)) {
			pool = Executors.newCachedThreadPool();
			Socket s = null;
			
			while(!Thread.interrupted()) {
				try {
					s = soc.accept();
					
					//admitimos un nuevo usuario y AtenderUsuario le atiende
					System.out.println("atendido");
					AtenderUsuario peticion = new AtenderUsuario(s);
								
					pool.execute(peticion);
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(pool != null) {
				pool.shutdown();
			}
		}
	}
	
}
