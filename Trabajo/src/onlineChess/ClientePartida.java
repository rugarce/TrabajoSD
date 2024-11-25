package onlineChess;

import java.io.IOException;
import java.net.Socket;

public class ClientePartida {

    public static void main(String[] args) {
        // Dirección IP y puerto del servidor
        String serverAddress = "127.0.0.1"; // Cambiar si el servidor está en otra máquina
        int port = 55555;

        try {
            // Conectarse al servidor
            System.out.println("Conectando al servidor...");
            Socket socket = new Socket(serverAddress, port);
            System.out.println("Conexión establecida.");

            // Crear la ventana para el jugador
            Interfaz interfaz = new Interfaz(socket);

            // Comenzar la escucha de mensajes del servidor
            interfaz.recibirActualizacionTablero();

        } catch (IOException e) {
            System.err.println("Error al conectarse al servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
