package onlineChess;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class AtenderUsuario implements Runnable {

	Socket s;

	AtenderUsuario(Socket s) {
		this.s = s;
	}

	@Override
	public void run() {
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		String username = null;

		try {
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());

			username = ois.readLine();

			Usuario user = null;
			while(user == null) {
				if (existeUsuarioConectado(username)) { // Si el usuario introducido ya está conectado
					oos.writeBytes("ERROR:usuario ya conectado\n");
					oos.flush(); // Escribimos el error y leemos de nuevo el usuario
					username = ois.readLine();
				}else {
					if (!existeUsuario(username)) {
						user = new Usuario(username, 0);
						addUsuario(username, user);
						oos.writeBytes("OK:nuevo usuario registrado\n");
						System.out.println(user + " registrado como nuevo usuario");
					} else {
						user = getUsuario(username);
						oos.writeBytes("OK:usuario logeado con éxito\n");
						System.out.println(user + " logeado en el sistema");
					}
					oos.flush();
				}
			}
			
			addUsuarioConectado(username);

			/*
			 * AQUI SE DEBE RECIBIR UNA LÍNEA QUE INDIQUE QUE QUEREMOS HACER, SI QUEREMOS
			 * UNIRNOS A UNA PARTIDA, MOSTRAR LA PUNTUACION, OBTENER EL HISTORIAL DE
			 * PARTIDAS ETC
			 */

			String action = ois.readLine();

			boolean waitingToFinish = false;

			while (action != null && !action.equals("DESCONECTAR") && !waitingToFinish) {
				switch (action) {
				case ("UNIRME A PARTIDA"):
					Sala sala = null;
					Thread t = null;
					if (Server.waitingSalas.size() != 0) {
						// si hay salas con usuarios esperando a ser emparejados se mete al usuario en
						// la sala creada más vieja
						sala = getSalaEnEspera();
						t = getHiloSalaEnEspera();

						addSalaEnCurso(sala);
						addHiloSalaEnCurso(t);

						eliminarSalaEnEspera(sala);
						eliminarHiloSalaEnEspera(t);

						sala.unirseNegras(user, ois, oos);

						// NOS UNIMOS AL HILO INICIADO Y EMPIEZA LA PARTIDA

						t.join();
						// LA PARTIDA HA ACABADO
					} else {
						// si no hay salas esperando se mete al usuario en una a la espera de
						// emparejarse
						sala = new Sala(user, ois, oos);
						t = new Thread(sala);

						addSalaEspera(sala);
						addHiloSalaEspera(t);

						// INICIAMOS EL HILO
						System.out.println("Iniciando sala...");
						t.start();

						t.join();
						System.out.println("Partida finalizada...");
						// LA PARTIDA HA ACABADO

						eliminarSalaEnCurso(sala);
						eliminarHiloSalaEnCurso(t);
					}
					break;
				case ("OBTENER HISTORIAL"):
					mostrarHistorial(oos, user);
					break;
				case ("LISTADO PUNTUACIONES"):
					listadoPuntuaciones(oos, user);
					break;
				}

				if (!waitingToFinish) {
					action = ois.readLine();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			eliminarUsuarioConectado(username);
			if (s != null) {
				try {
					s.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//a través del socket accede a un fichero llamado MUCHO OJO "nombredeusuario.txt" que estará almacenado en alguna carpeta
	//que ceemos asociado al usuario (todavía no esta creado) que contiene su historial de partidas y le manda todo el contenido
	//al usuario que de lo descargara enviaremos el fichero en forma de bytes, necesitamos crear un dataOutputStream a partir
	//del socket y enviarle todo el fichero en forma de bytes con lo del buffer

	static void mostrarHistorial(ObjectOutputStream oos, Usuario user) {
		// Ruta del archivo del historial del usuario
		String rutaHistorial = user.getNombre() + ".txt";
		
		BufferedReader br = null;
		
		// Creamos un DataOutputStream para enviar datos al cliente
		try {
			File archivoHistorial = new File(rutaHistorial);
			
			// Verificamos si el archivo existe
			if (!archivoHistorial.exists()) {
				oos.writeBytes("NO EXISTE\n");

				oos.flush();
				return;
			}
			
			br = new BufferedReader(new InputStreamReader(new FileInputStream(rutaHistorial)));

			oos.writeBytes("EXISTE\n");
			oos.flush();

			// Usamos un buffer para leer y enviar el archivo en bloques de bytes
			byte[] buffer = new byte[1024];
			String linea;

			// Leemos el archivo y enviamos los datos en bloques
			while ((linea = br.readLine()) != null) {
				oos.writeBytes(linea + "\n");
			}
			oos.writeBytes("FIN\n");
			// Finalizamos la transferencia
			oos.flush();
			System.out.println("Historial de partidas enviado a " + user.getNombre());

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	static void listadoPuntuaciones(ObjectOutputStream oos, Usuario user) {
		// a través del socket, le mandará un Map<String, Integer> al usuario con la
		// información de todos los usuarios y sus puntuaciones
		// OJO LE ENVIAREMOS UN OBJETO oos.writeObject(Map<String, Integer>);
		Map<String, Usuario> usuarios = Server.users;

		Map<String, Integer> puntuaciones = new HashMap<>();

		// Llenamos el mapa de puntuaciones
		for (Map.Entry<String, Usuario> usuario : usuarios.entrySet()) {
			String nombreUsuario = usuario.getKey();
			Usuario usuarioActual = usuario.getValue();
			puntuaciones.put(nombreUsuario, usuarioActual.getPuntuacion());
		}

		try {
			oos.writeObject(puntuaciones);
			oos.reset();
			oos.flush();

			System.out.println("Puntuaciones enviadas al usuario " + user.getNombre());
		} catch (IOException e) {
			e.printStackTrace();
			try {
				oos.writeUTF("Error al enviar el listado de puntuaciones.");
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	// métodos synchronized de las salas en espera
	static Sala getSalaEnEspera() {
		synchronized (Server.waitingSalas) {
			return Server.waitingSalas.get(0);
		}
	}

	static Thread getHiloSalaEnEspera() {
		synchronized (Server.waitingSalasThreads) {
			return Server.waitingSalasThreads.get(0);
		}
	}

	static void addSalaEspera(Sala sala) {
		synchronized (Server.waitingSalas) {
			Server.waitingSalas.add(sala);
		}
	}

	static void addHiloSalaEspera(Thread t) {
		synchronized (Server.waitingSalasThreads) {
			Server.waitingSalasThreads.add(t);
		}
	}

	static void eliminarSalaEnEspera(Sala sala) {
		synchronized (Server.waitingSalas) {
			Server.waitingSalas.remove(sala);
		}
	}

	static void eliminarHiloSalaEnEspera(Thread t) {
		synchronized (Server.waitingSalasThreads) {
			Server.waitingSalasThreads.remove(t);
		}
	}

	// métodos synchronized de las salas en curso
	static void addSalaEnCurso(Sala sala) {
		synchronized (Server.runningSalas) {
			Server.runningSalas.add(sala);
		}
	}

	static void addHiloSalaEnCurso(Thread t) {
		synchronized (Server.runningSalasThreads) {
			Server.runningSalasThreads.add(t);
		}
	}

	static void eliminarSalaEnCurso(Sala sala) {
		synchronized (Server.runningSalas) {
			Server.runningSalas.remove(sala);
		}
	}

	static void eliminarHiloSalaEnCurso(Thread t) {
		synchronized (Server.runningSalasThreads) {
			Server.runningSalasThreads.remove(t);
		}
	}

	// métodos synchronized de los usuarios
	static Boolean addUsuarioConectado(String username) {
		synchronized (Server.connectedUsers) {
			return Server.connectedUsers.add(username);
		}
	}

	
	static Boolean existeUsuarioConectado(String username) {
		synchronized (Server.connectedUsers) {
			 System.out.println("Usuarios conectados:");
			    for (String username1 : Server.connectedUsers) {
			        System.out.println(username1);
			    }
			return Server.connectedUsers.contains(username);
		}
	}
	
	static void eliminarUsuarioConectado(String username) {
		synchronized (Server.connectedUsers) {
			Server.connectedUsers.remove(username);
		}
	}

	static Boolean existeUsuario(String username) {
		synchronized (Server.users) {
			return Server.users.containsKey(username);
		}
	}

	static Usuario getUsuario(String username) {
		synchronized (Server.users) {
			return Server.users.get(username);
		}
	}
	
	static void addUsuario(String username, Usuario user) {
		synchronized (Server.users) {
			Server.users.put(username, user);
			Server.writeUserList();
		}
	}
}
