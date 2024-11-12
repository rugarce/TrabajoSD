package onlineChess;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

	ConcurrentHashMap<String, Usuario> users = new ConcurrentHashMap<String, Usuario>();
	
	ArrayList<Sala> runningSalas = new ArrayList<Sala>();
	ArrayList<Sala> waitingSalas = new ArrayList<Sala>();
	
	public static void main(String[] args) {
		try(ServerSocket soc = new ServerSocket(55555)) {
			while(!Thread.interrupted()) {
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
