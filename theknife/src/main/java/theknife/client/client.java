package theknife.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import theknife.common.Messaggio;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Gestisce la connessione socket verso il server TheKnife.
 * Il client usa questa classe per mandare richieste e ricevere risposte.
 * Pattern: request/response sincrono (una richiesta alla volta).
 *
 * Nota: i metodi di invio/ricezione sono sincronizzati per evitare che
 * richieste concorrenti da thread diversi si mischino sullo stesso socket.
 *
 * @author Ayoub Hammou 761589
 * @author Esau Alessandro Argueta Zepeda 761748
 */
public class client {

    private static final int TIMEOUT_LETTURA_MS = 10_000;

    private Socket socket;
    private BufferedReader reader;
    private Writer writer;
    private Gson gson;

    public client() {
        this.gson = new Gson();
    }

    /**
     * Apre la connessione al server.
     *
     * @param host indirizzo del server
     * @param porta porta del server
     * @throws IOException se la connessione fallisce
     * @throws IllegalStateException se questa istanza è già connessa
     */
    public synchronized void connetti(String host, int porta) throws IOException {
        if (socket != null && !socket.isClosed()) {
            throw new IllegalStateException("Connessione già aperta: chiamare disconnetti() prima di riconnettersi");
        }
        socket = new Socket(host, porta);
        socket.setSoTimeout(TIMEOUT_LETTURA_MS);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Invia una richiesta al server e attende la risposta.
     * Questo metodo è bloccante (con timeout): aspetta la risposta prima di tornare.
     *
     * @param richiesta il messaggio da inviare
     * @return la risposta del server
     * @throws IOException se la comunicazione fallisce, il server chiude la
     *                      connessione, o si supera il timeout di lettura
     */
    public synchronized Messaggio invia(Messaggio richiesta) throws IOException {
        if (!isConnessa()) {
            throw new IOException("Non connesso al server");
        }

        String richiestaJson = gson.toJson(richiesta);
        writer.write(richiestaJson);
        writer.write("\n");
        writer.flush();

        String rispostaJson;
        try {
            rispostaJson = reader.readLine();
        } catch (SocketTimeoutException e) {
            throw new IOException("Timeout in attesa della risposta dal server", e);
        }

        if (rispostaJson == null) {
            // Il server ha chiuso la connessione dal suo lato
            disconnetti();
            throw new IOException("Connessione chiusa dal server");
        }

        try {
            Messaggio risposta = gson.fromJson(rispostaJson, Messaggio.class);
            if (risposta == null) {
                throw new IOException("Risposta del server vuota o non valida");
            }
            return risposta;
        } catch (JsonSyntaxException e) {
            throw new IOException("Risposta del server malformata: " + rispostaJson, e);
        }
    }

    /** Chiude la connessione, rilasciando tutte le risorse associate. */
    public synchronized void disconnetti() {
        try {
            if (reader != null) reader.close();
        } catch (IOException ignored) {
        }
        try {
            if (writer != null) writer.close();
        } catch (IOException ignored) {
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * Indica se il socket risulta aperto localmente. Nota: non garantisce che
     * il server dall'altra parte sia ancora raggiungibile; un fallimento verrà
     * comunque rilevato al prossimo invia().
     */
    public synchronized boolean isConnessa() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}