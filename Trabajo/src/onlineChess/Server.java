package onlineChess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

	static String userListPath = "userList.txt";
	
	static ConcurrentHashMap<String, Usuario> users = new ConcurrentHashMap<String, Usuario>();
	
	static ArrayList<String> connectedUsers = new ArrayList<String>();
	
	static ArrayList<Sala> runningSalas = new ArrayList<Sala>();
	static ArrayList<Thread> runningSalasThreads = new ArrayList<Thread>();
	static ArrayList<Sala> waitingSalas = new ArrayList<Sala>();
	static ArrayList<Thread> waitingSalasThreads = new ArrayList<Thread>();
	
	public static void main(String[] args) {
		ExecutorService pool = null;
		readUserList();
		
		try(ServerSocket soc = new ServerSocket(55555)) {
			pool = Executors.newCachedThreadPool();
			Socket s = null;
			
			while(!Thread.interrupted()) {
				try {
					s = soc.accept();
					
					//admitimos un nuevo usuario y AtenderUsuario le atiende
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
	
	public static void readUserList() {
		File f = new File(userListPath);
		
		if(f.exists()) {
			ObjectInputStream ois = null;
			
			try {
				ois = new ObjectInputStream(new FileInputStream(userListPath));
				users = (ConcurrentHashMap<String, Usuario>) ois.readObject();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				if(ois != null) {
					try {
						ois.close();
					} catch (IOException e) {
						e.printStackTrace();
					} 
				}
			}
		}
	}
	
	public static void writeUserList() {
		ObjectOutputStream oos = null;
		
		try {
			oos = new ObjectOutputStream(new FileOutputStream(userListPath));
			oos.writeObject(users);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		}
	}
	
}
