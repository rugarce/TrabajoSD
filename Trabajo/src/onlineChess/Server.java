package onlineChess;

import java.io.IOException;
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
