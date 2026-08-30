package theknife.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import theknife.common.Messaggio;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Gestisce la connessione socket verso il server TheKnife.
 * Pattern request/response sincrono. Metodi sincronizzati per thread-safety.
 *
 * Contiene anche GeocodingUtil come classe statica interna
 * per ridurre il numero di file nel package client.
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class ClientTK {

    private static final int TIMEOUT_MS = 10_000;

    private Socket         socket;
    private BufferedReader reader;
    private Writer         writer;
    private final Gson     gson = new Gson();

    /**
     * Apre la connessione al server.
     *
     * @param host indirizzo del server a cui connettersi
     * @param porta porta TCP su cui il server è in ascolto
     * @throws IOException se la connessione al server fallisce
     * @throws IllegalStateException se una connessione è già aperta
     */
    public synchronized void connetti(String host, int porta) throws IOException {
        if (socket != null && !socket.isClosed())
            throw new IllegalStateException("Connessione già aperta");
        socket = new Socket(host, porta);
        socket.setSoTimeout(TIMEOUT_MS);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
    }

    /**
     * Invia una richiesta e attende la risposta (bloccante con timeout).
     *
     * @param richiesta messaggio da inviare
     * @return risposta del server
     * @throws IOException se la comunicazione fallisce o scade il timeout
     */
    public synchronized Messaggio invia(Messaggio richiesta) throws IOException {
        if (!isConnessa()) throw new IOException("Non connesso al server");
        writer.write(gson.toJson(richiesta));
        writer.write("\n");
        writer.flush();
        String riga;
        try {
            riga = reader.readLine();
        } catch (SocketTimeoutException e) {
            throw new IOException("Timeout risposta server", e);
        }
        if (riga == null) { disconnetti(); throw new IOException("Connessione chiusa dal server"); }
        try {
            Messaggio r = gson.fromJson(riga, Messaggio.class);
            if (r == null) throw new IOException("Risposta vuota");
            return r;
        } catch (JsonSyntaxException e) {
            throw new IOException("Risposta malformata: " + riga, e);
        }
    }

    /** Chiude la connessione. */
    public synchronized void disconnetti() {
        try { if (reader != null) reader.close(); } catch (IOException ignored) {}
        try { if (writer != null) writer.close(); } catch (IOException ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    /**
     * Indica se il socket risulta aperto localmente.
     *
     * @return {@code true} se il socket è aperto localmente
     */
    public synchronized boolean isConnessa() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    // ══════════════════════════════════════════════════════════════════════
    // GEOCODING — Nominatim (OpenStreetMap), nessuna chiave API necessaria
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Converte un nome di luogo in coordinate geografiche tramite Nominatim.
     * @param luogo nome della città o indirizzo (es. "Milano", "Paris, France")
     * @return coordinate lat/lon, null se non trovato
     */
    public static double[] geocodifica(String luogo) {
        if (luogo == null || luogo.isBlank()) return null;
        try {
            String query = URLEncoder.encode(luogo.trim(), StandardCharsets.UTF_8);
            URL url = new URL("https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" + query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "TheKnifeApp/1.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            if (conn.getResponseCode() != 200) return null;

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String r; while ((r = br.readLine()) != null) sb.append(r);
            }
            String json = sb.toString().trim();
            if (json.isEmpty() || "[]".equals(json)) return null;

            com.google.gson.JsonArray arr = com.google.gson.JsonParser.parseString(json).getAsJsonArray();
            if (arr.size() == 0) return null;
            com.google.gson.JsonObject obj = arr.get(0).getAsJsonObject();
            return new double[]{ obj.get("lat").getAsDouble(), obj.get("lon").getAsDouble() };
        } catch (Exception e) {
            System.err.println("Geocoding fallito per '" + luogo + "': " + e.getMessage());
            return null;
        }
    }
}
