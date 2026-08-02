package theknife.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Punto di ingresso del server.
 * All'avvio chiede le credenziali del database e la porta.
 * Poi rimane in attesa di connessioni dai client.
 *
 * @author Ayoub Hammou 					761589
 * @author Esau Alessandro Argueta Zepeda 	761748
 */
public class Server {

    private static final int PORTA_DEFAULT = 12345;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("=== TheKnife Server ===");
      
        System.out.print("Username: ");
        String user = input.nextLine().trim();

        System.out.print("Password: ");
        String password = input.nextLine();

        // Connetti al database
        try {
            DatabaseManager.getInstance().connetti("localhost", "theknife_db", user, password);
            System.out.println("Connessione al database riuscita!");
        } catch (Exception e) {
            System.err.println("Avviso: Impossibile connettersi al database: " + e.getMessage());
        }

        // Avvia il server socket
        try (ServerSocket serverSocket = new ServerSocket(PORTA_DEFAULT)) {
            System.out.println("Server avviato sulla porta " + PORTA_DEFAULT);
            System.out.println("In attesa di connessioni...");

            // Loop infinito: accetta connessioni e lancia un thread per ognuna
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.setDaemon(true); // i thread terminano con il server
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Errore server: " + e.getMessage());
        } finally {
            DatabaseManager.getInstance().disconnetti();
        }
        input.close();
    }
}
