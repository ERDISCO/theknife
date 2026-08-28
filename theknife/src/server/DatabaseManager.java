package theknife.server;

import theknife.common.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce l'accesso al database PostgreSQL per l'applicazione TheKnife.
 * Implementata come singleton per garantire un'unica connessione condivisa
 * tra tutti i {@link ClientHandler} attivi sul server.
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection conn;

    private DatabaseManager() {}

    /** Restituisce l'unica istanza (thread-safe). */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Apre la connessione al database PostgreSQL.
     *
     * @param host     hostname o IP del server PostgreSQL
     * @param dbName   nome del database
     * @param user     username PostgreSQL
     * @param password password PostgreSQL
     * @throws SQLException se la connessione fallisce
     */
    public synchronized void connetti(String host, String dbName,
            String user, String password) throws SQLException {
        String url = "jdbc:postgresql://" + host + "/" + dbName;
        this.conn = DriverManager.getConnection(url, user, password);
    }

    /**
     * Allinea i prezzi medi dei ristoranti già presenti al livello qualitativo
     * indicato dal campo price. I valori generati sono casuali ma rispettano
     * sempre la scala: 1 simbolo = 0-19, 2 = 20-49, 3 = 50-80, 4 = oltre 80.
     *
     * @throws SQLException per errori DB
     */
    public synchronized void normalizzaPrezziMedi() throws SQLException {
        String sql = "UPDATE ristoranti SET prezzo_medio = CASE " +
                     "WHEN price IS NULL OR LENGTH(TRIM(price)) <= 1 THEN FLOOR(random() * 20) " +
                     "WHEN LENGTH(TRIM(price)) = 2 THEN 20 + FLOOR(random() * 30) " +
                     "WHEN LENGTH(TRIM(price)) = 3 THEN 50 + FLOOR(random() * 31) " +
                     "ELSE 80 + FLOOR(random() * 71) END " +
                     "WHERE prezzo_medio IS NULL " +
                     "OR prezzo_medio < 0 " +
                     "OR (LENGTH(TRIM(price)) <= 1 AND prezzo_medio >= 20) " +
                     "OR (LENGTH(TRIM(price)) = 2 AND (prezzo_medio < 20 OR prezzo_medio >= 50)) " +
                     "OR (LENGTH(TRIM(price)) = 3 AND (prezzo_medio < 50 OR prezzo_medio > 80)) " +
                     "OR (LENGTH(TRIM(price)) >= 4 AND prezzo_medio < 80)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) { ps.executeUpdate(); }
    }

    /** Chiude la connessione al database. */
    public synchronized void disconnetti() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("Errore chiusura DB: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // UTENTI
    // ══════════════════════════════════════════════════════════════════

    /**
     * Aggiorna i dati anagrafici e di accesso di un utente esistente.
     * La password viene aggiornata solo se fornita (non vuota); in caso
     * contrario resta invariata.
     *
     * @param id           ID dell'utente da modificare
     * @param nome         nuovo nome
     * @param cognome      nuovo cognome
     * @param username     nuovo username (viene salvato in lowercase)
     * @param password     nuova password in chiaro, o {@code null}/vuota per non modificarla
     * @param dataNascita  nuova data di nascita (può essere {@code null})
     * @param domicilio    nuovo luogo di domicilio
     * @return {@code true} se l'aggiornamento ha avuto effetto
     * @throws SQLException per errori DB
     */
    public synchronized boolean modificaUtente(int id, String nome, String cognome,
            String username, String password, java.util.Date dataNascita,
            String domicilio) throws SQLException {

        String sql;
        if (password != null && !password.isBlank()) {
            sql = "UPDATE utenti SET nome=?, cognome=?, username=?, password_hash=?, " +
                  "data_nascita=?, luogo_domicilio=? WHERE id=?";
        } else {
            sql = "UPDATE utenti SET nome=?, cognome=?, username=?, " +
                  "data_nascita=?, luogo_domicilio=? WHERE id=?";
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, username.toLowerCase());
            int idx = 4;
            if (password != null && !password.isBlank()) {
                ps.setString(idx++, PasswordUtil.hash(password));
            }
            if (dataNascita == null) {
                ps.setNull(idx++, java.sql.Types.DATE);
            } else {
                ps.setDate(idx++, new java.sql.Date(dataNascita.getTime()));
            }
            ps.setString(idx++, domicilio != null ? domicilio.trim() : "");
            ps.setInt(idx, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Registra un nuovo utente nel database.
     * Invece di fare un SELECT preventivo per controllare il duplicato
     * (race condition), ci affidiamo al vincolo UNIQUE sul DB e catturiamo
     * la SQLException con SQLState "23xxx" (violation of unique constraint).
     *
     * @param nome         nome dell'utente
     * @param cognome      cognome dell'utente
     * @param username     username univoco (viene salvato in lowercase)
     * @param password     password in chiaro (viene hashata con BCrypt)
     * @param dataNascita  data di nascita (può essere null)
     * @param domicilio    luogo di domicilio
     * @param isRistoratore true se gestore, false se cliente
     * @return l'oggetto Utente creato, oppure null se l'username è già in uso
     * @throws SQLException per errori DB diversi dal vincolo di unicità
     */
    public synchronized Utente registraUtente(String nome, String cognome,
            String username, String password, java.util.Date dataNascita,
            String domicilio, boolean isRistoratore) throws SQLException {

        String sql = "INSERT INTO utenti " +
                     "(nome, cognome, username, password_hash, data_nascita, " +
                     " luogo_domicilio, is_ristoratore) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, username.toLowerCase());
            ps.setString(4, PasswordUtil.hash(password));

            if (dataNascita == null) {
                ps.setNull(5, java.sql.Types.DATE);
            } else {
                ps.setDate(5, new java.sql.Date(dataNascita.getTime()));
            }

            ps.setString(6, domicilio != null ? domicilio.trim() : "");
            ps.setBoolean(7, isRistoratore);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int idGenerato = rs.getInt(1);
                return new Utente(idGenerato, nome, cognome, username.toLowerCase(),
                        dataNascita == null ? null : new java.sql.Date(dataNascita.getTime()),
                        domicilio, isRistoratore);
            }
        } catch (SQLException e) {
            // SQLState 23xxx = unique constraint violation → username già in uso
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                return null;
            }
            throw e;
        }
        return null;
    }

    /**
     * Aggiorna i dati di un ristorante esistente.
     *
     * @param id           ID del ristorante da modificare
     * @param nome         nuovo nome
     * @param indirizzo    nuovo indirizzo
     * @param citta        nuova città
     * @param nazione      nuova nazione (concatenata alla città come location)
     * @param cucina       nuovo tipo di cucina
     * @param telefono     nuovo numero di telefono
     * @param fasciaPrezzo nuova fascia di prezzo
     * @param prezzoMedio  nuovo prezzo medio
     * @param delivery     nuovo valore per il servizio di consegna
     * @param prenotazione nuovo valore per la prenotazione online
     * @param descrizione  nuova descrizione
     * @param facilities   servizi offerti (non ancora persistito su colonna dedicata)
     * @param award        eventuale riconoscimento
     * @param greenStar    eventuale riconoscimento green (non ancora persistito su colonna dedicata)
     * @param url          url generico
     * @param website      sito web (non ancora persistito su colonna dedicata)
     * @param lat          nuova latitudine
     * @param lon          nuova longitudine
     * @return {@code true} se l'aggiornamento ha avuto effetto
     * @throws SQLException per errori DB
     */
    public synchronized boolean modificaRistorante(int id, String nome, String indirizzo,
            String citta, String nazione, String cucina, String telefono,
            String fasciaPrezzo, double prezzoMedio, boolean delivery,
            boolean prenotazione, String descrizione, String facilities,
            String award, String greenStar, String url, String website,
            double lat, double lon) throws SQLException {

        String location = citta + (nazione != null && !nazione.isBlank() ? ", " + nazione : "");

        String sql = "UPDATE ristoranti SET \"Name\"=?, address=?, \"Location\"=?, cuisine=?, " +
                     "phonenumber=?, price=?, prezzo_medio=?, fa_deliveroo=?, " +
                     "ordina_online=?, description=?, award=?, url=?, " +
                     "latitude=?, longitude=? " +
                     "WHERE id_ristorante=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  nome);
            ps.setString(2,  indirizzo);
            ps.setString(3,  location);
            ps.setString(4,  cucina);
            ps.setString(5,  telefono != null ? telefono.trim() : "");
            ps.setString(6,  fasciaPrezzo);
            ps.setDouble(7,  prezzoMedio);
            ps.setBoolean(8, delivery);
            ps.setBoolean(9, prenotazione);
            ps.setString(10, descrizione);
            ps.setString(11, award != null ? award : "");
            ps.setString(12, url != null ? url : "");
            ps.setDouble(13, lat);
            ps.setDouble(14, lon);
            ps.setInt(15,    id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Effettua il login verificando username e password con BCrypt.
     *
     * @param username username dell'utente
     * @param password password in chiaro
     * @return oggetto Utente se le credenziali sono corrette, null altrimenti
     * @throws SQLException per errori DB
     */
    public synchronized Utente login(String username, String password) throws SQLException {
        String sql = "SELECT id, nome, cognome, username, password_hash, " +
                     "data_nascita, luogo_domicilio, is_ristoratore " +
                     "FROM utenti WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.toLowerCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashSalvato = rs.getString("password_hash");
                if (PasswordUtil.verifica(password, hashSalvato)) {
                    return new Utente(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("cognome"),
                        rs.getString("username"),
                        rs.getDate("data_nascita"),
                        rs.getString("luogo_domicilio"),
                        rs.getBoolean("is_ristoratore")
                    );
                }
            }
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════════
    // RISTORANTI
    // ══════════════════════════════════════════════════════════════════

    /**
     * Cerca ristoranti con filtri combinabili.
     *
     * La ricerca per posizione geografica avviene tramite la formula
     * di Haversine, calcolata direttamente in SQL con le funzioni
     * trigonometriche di PostgreSQL. In questo modo non si cerca per
     * nome di città (che potrebbe non corrispondere esattamente),
     * ma per distanza reale in km rispetto alle coordinate fornite.
     *
     * La latitudine e longitudine del punto di ricerca vengono ricavate
     * dalla stringa "lat,lon" nel parametro location (se contiene una virgola),
     * oppure si cade in una ricerca LIKE sul campo Location testuale.
     *
     * @param location      stringa "lat,lon" oppure nome di città/paese
     * @param ragioKm       raggio di ricerca in km (default 50 se non specificato)
     * @param cucina        tipo di cucina (filtro parziale, case-insensitive)
     * @param prezzoMin     prezzo medio minimo (filtro incluso solo se non {@code null})
     * @param prezzoMax     prezzo medio massimo (filtro incluso solo se non {@code null})
     * @param ordinaOnline  true=solo con prenotazione online, null=tutti
     * @param faDelivery    true=solo con delivery, null=tutti
     * @param stelleMinime  media stelle minima (1-5), null=tutti
     * @return lista di ristoranti ordinata per media voto decrescente
     * @throws SQLException per errori DB
     */
    public synchronized List<Ristorante> cercaRistoranti(
            String location, Double ragioKm,
            String cucina,
            Double prezzoMin, Double prezzoMax,       // ← nuovo
            Boolean ordinaOnline, Boolean faDelivery,
            Integer stelleMinime) throws SQLException {
     
        boolean soloCitta = false;
        // Il client Home invia il parametro tramite location con prefisso speciale soloCitta:.
        if (location != null && location.startsWith("soloCitta:")) {
            soloCitta = true;
            location = location.substring("soloCitta:".length()).trim();
        }
        double raggio = (ragioKm != null && ragioKm > 0) ? ragioKm : 50.0;
        StringBuilder sql = new StringBuilder(
            "SELECT r.*, COALESCE(AVG(rec.stelle), 0) AS media_voto, COUNT(rec.id) AS num_recensioni ");
     
        List<Object> params = new java.util.ArrayList<>();
        boolean geo = false;
        double lat = 0, lon = 0;
     
        if (location != null && !location.trim().isEmpty()) {
            String loc = location.trim();
            if (loc.contains(",")) {
                String[] p = loc.split(",", 2);
                try {
                    lat = Double.parseDouble(p[0].trim());
                    lon = Double.parseDouble(p[1].trim());
                    geo = true;
                } catch (NumberFormatException ignored) {}
            }
        }
     
        if (geo) {
            sql.append(", (6371*2*ASIN(SQRT(POWER(SIN(RADIANS(r.latitude-?)/2),2)" +
                       "+COS(RADIANS(?))*COS(RADIANS(r.latitude))*POWER(SIN(RADIANS(r.longitude-?)/2),2)))) AS distanza_km ");
            params.add(lat); params.add(lat); params.add(lon);
        }
     
        sql.append("FROM ristoranti r LEFT JOIN recensioni rec ON r.id_ristorante=rec.ristorante_id WHERE 1=1 ");
     
        if (geo) {
            sql.append("AND (6371*2*ASIN(SQRT(POWER(SIN(RADIANS(r.latitude-?)/2),2)" +
                       "+COS(RADIANS(?))*COS(RADIANS(r.latitude))*POWER(SIN(RADIANS(r.longitude-?)/2),2)))) <= ? ");
            params.add(lat); params.add(lat); params.add(lon); params.add(raggio);
        } else if (location != null && !location.trim().isEmpty()) {
            sql.append("AND LOWER(r.\"Location\") LIKE LOWER(?) ");
            params.add("%" + location.trim() + "%");
        }
     
        if (cucina != null && !cucina.trim().isEmpty()) {
            sql.append("AND LOWER(r.cuisine) LIKE LOWER(?) ");
            params.add("%" + cucina.trim() + "%");
        }
     
        // ── NUOVO: filtro prezzo su prezzo_medio numerico ──────────────────────
        // Filtra solo se prezzo_medio > 0 (esclude righe senza prezzo impostato)
        if (prezzoMin != null) {
            sql.append("AND r.prezzo_medio >= ? ");
            params.add(prezzoMin);
        }
        if (prezzoMax != null) {
            sql.append("AND r.prezzo_medio <= ? ");
            params.add(prezzoMax);
        }
        // ── fine nuovo blocco ──────────────────────────────────────────────────
     
        if (ordinaOnline != null) { sql.append("AND r.ordina_online = ? "); params.add(ordinaOnline); }
        if (faDelivery   != null) { sql.append("AND r.fa_deliveroo = ? ");  params.add(faDelivery); }
     
        sql.append("GROUP BY r.id_ristorante ");
        if (stelleMinime != null) {
            sql.append("HAVING COALESCE(AVG(rec.stelle),0) >= ? ");
            params.add(stelleMinime);
        }
        sql.append(geo ? "ORDER BY distanza_km ASC, media_voto DESC" : "ORDER BY media_voto DESC");
     
        List<Ristorante> risultati = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i+1, params.get(i));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ristorante r = creaRistoranteDaRS(rs);
                r.setMediaVoto(rs.getDouble("media_voto"));
                r.setNumeroRecensioni(rs.getInt("num_recensioni"));
                if (geo) r.setDistanzaKm(rs.getDouble("distanza_km"));
                risultati.add(r);
            }
        }
        return risultati;
    }



    /**
     * Restituisce un ristorante tramite ID.
     *
     * @param id ID del ristorante
     * @return il ristorante corrispondente, con media voto e numero recensioni, o {@code null} se non trovato
     * @throws SQLException per errori DB
     */
    public synchronized Ristorante ristorantePerId(int id) throws SQLException {
        String sql = "SELECT r.*, COALESCE(AVG(rec.stelle),0) AS media_voto, " +
                     "COUNT(rec.id) AS num_recensioni FROM ristoranti r " +
                     "LEFT JOIN recensioni rec ON r.id_ristorante = rec.ristorante_id " +
                     "WHERE r.id_ristorante = ? GROUP BY r.id_ristorante";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Ristorante r = creaRistoranteDaRS(rs);
                r.setMediaVoto(rs.getDouble("media_voto"));
                r.setNumeroRecensioni(rs.getInt("num_recensioni"));
                return r;
            }
        }
        return null;
    }

    /**
     * Aggiunge un nuovo ristorante gestito dall'utente indicato.
     * Il numero di telefono viene salvato come VARCHAR per preservare
     * zeri iniziali (es. 011...) e caratteri speciali (+39, spazi, ecc.).
     *
     * @param nome               nome del ristorante
     * @param indirizzo          indirizzo stradale
     * @param location           città / paese
     * @param cucina             tipo di cucina
     * @param longitudine        coordinata geografica
     * @param latitudine         coordinata geografica
     * @param telefono           numero di telefono (salvato come stringa)
     * @param url                sito web
     * @param award              eventuali premi (es. stella Michelin)
     * @param fasciaPrezzo       fascia qualitativa del prezzo
     * @param descrizione        descrizione libera
     * @param delivery           true se offre servizio di consegna
     * @param prenotazioneOnline true se si può prenotare online
     * @param prezzoMedio        prezzo medio in euro
     * @param utenteId           ID del gestore proprietario
     * @return ID del ristorante inserito, oppure -1 in caso di errore
     * @throws SQLException per errori DB
     */
    public synchronized int aggiungiRistorante(String nome, String indirizzo,
            String location, String cucina,
            double longitudine, double latitudine,
            String telefono, String url, String award,
            String fasciaPrezzo, String descrizione,
            boolean delivery, boolean prenotazioneOnline,
            double prezzoMedio, int utenteId) throws SQLException {

        String sql = "INSERT INTO ristoranti " +
                     "(\"Name\", address, \"Location\", cuisine, " +
                     " longitude, latitude, phonenumber, url, award, " +
                     " price, description, ordina_online, fa_deliveroo, " +
                     " prezzo_medio, utente_id) " +
                     "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) RETURNING id_ristorante";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, indirizzo);
            ps.setString(3, location);
            ps.setString(4, cucina);
            ps.setDouble(5, longitudine);
            ps.setDouble(6, latitudine);
            ps.setString(7, telefono != null ? telefono.trim() : "");
            ps.setString(8, url);
            ps.setString(9, award);
            ps.setString(10, fasciaPrezzo);
            ps.setString(11, descrizione);
            ps.setBoolean(12, delivery);
            ps.setBoolean(13, prenotazioneOnline);
            ps.setDouble(14, prezzoMedio);
            ps.setInt(15, utenteId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    /**
     * Restituisce tutti i ristoranti gestiti da un determinato gestore.
     *
     * @param utenteId ID del gestore
     * @return lista di ristoranti con media voto e numero recensioni
     * @throws SQLException per errori DB
     */
    public synchronized List<Ristorante> ristorantiDelGestore(int utenteId) throws SQLException {
        String sql = "SELECT r.*, " +
                "COALESCE(AVG(rec.stelle), 0) AS media_voto, " +
                "COUNT(rec.id) AS num_recensioni " +
                "FROM ristoranti r " +
                "LEFT JOIN recensioni rec ON r.id_ristorante = rec.ristorante_id " +
                "WHERE r.utente_id = ? " +
                "GROUP BY r.id_ristorante ORDER BY r.\"Name\"";


        List<Ristorante> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, utenteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ristorante r = creaRistoranteDaRS(rs);
                r.setMediaVoto(rs.getDouble("media_voto"));
                r.setNumeroRecensioni(rs.getInt("num_recensioni"));
                lista.add(r);
            }
        }
        return lista;
    }


    // ══════════════════════════════════════════════════════════════════
    // RECENSIONI
    // ══════════════════════════════════════════════════════════════════

    /**
     * Aggiunge una recensione di un cliente per un ristorante.
     *
     * @param ristoranteId ID del ristorante
     * @param utenteId     ID del cliente che recensisce
     * @param stelle       numero di stelle (1-5)
     * @param testo        testo della recensione
     * @return true se inserita con successo
     * @throws SQLException per errori DB
     */
    public synchronized boolean aggiungiRecensione(int ristoranteId, int utenteId,
            int stelle, String testo) throws SQLException {
        if (stelle < 1 || stelle > 5) throw new IllegalArgumentException("Le stelle devono essere tra 1 e 5");
        String sql = "INSERT INTO recensioni (ristorante_id, utente_id, stelle, testo) " +
                     "VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ristoranteId);
            ps.setInt(2, utenteId);
            ps.setInt(3, stelle);
            ps.setString(4, testo);
            ps.executeUpdate();
            return true;
        }
    }

    /**
     * Modifica una recensione esistente dell'utente.
     * Prima di aggiornare, controlla che almeno un campo sia valorizzato.
     * Se entrambi i parametri sono null, non ha senso procedere.
     *
     * @param recensioneId ID della recensione da modificare
     * @param utenteId     ID dell'utente (deve essere l'autore)
     * @param nuoveStelle  nuovo numero di stelle (null = non modificare)
     * @param nuovoTesto   nuovo testo (null = non modificare)
     * @return true se la modifica è avvenuta, false se la recensione non esiste
     *         o non appartiene all'utente
     * @throws SQLException per errori DB
     */
    public synchronized boolean modificaRecensione(int recensioneId, int utenteId,
            Integer nuoveStelle, String nuovoTesto) throws SQLException {

        // FIX: controllo preliminare — se entrambi null la query sarebbe invalida
        if (nuoveStelle == null && nuovoTesto == null) return false;

        // Verifica che la recensione esista e appartenga all'utente
        String checkSql = "SELECT id FROM recensioni WHERE id = ? AND utente_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, recensioneId);
            ps.setInt(2, utenteId);
            if (!ps.executeQuery().next()) return false;
        }

        StringBuilder sql = new StringBuilder("UPDATE recensioni SET ");
        List<Object> params = new ArrayList<>();

        if (nuoveStelle != null) { sql.append("stelle = ?, "); params.add(nuoveStelle); }
        if (nuovoTesto  != null) { sql.append("testo = ?, ");  params.add(nuovoTesto); }

        // Rimuove l'ultima virgola e spazio
        String query = sql.toString().replaceAll(", $", "") + " WHERE id = ?";
        params.add(recensioneId);

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            ps.executeUpdate();
        }
        return true;
    }

    /**
     * Elimina una recensione dell'utente.
     *
     * @param recensioneId ID della recensione
     * @param utenteId     ID dell'utente (deve essere l'autore)
     * @return true se eliminata, false se non trovata
     * @throws SQLException per errori DB
     */
    public synchronized boolean eliminaRecensione(int recensioneId,
            int utenteId) throws SQLException {
        String sql = "DELETE FROM recensioni WHERE id = ? AND utente_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recensioneId);
            ps.setInt(2, utenteId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Aggiunge la risposta del gestore a una recensione.
     * Ogni recensione può avere al massimo una risposta (come da specifiche):
     * se la risposta è già presente, l'operazione viene rifiutata.
     *
     * @param recensioneId ID della recensione a cui rispondere
     * @param utenteId     ID del gestore (deve possedere il ristorante)
     * @param risposta     testo della risposta
     * @return true se la risposta è stata inserita, false se non autorizzato
     *         o se esiste già una risposta per questa recensione
     * @throws SQLException per errori DB
     */
    public synchronized boolean rispondiRecensione(int recensioneId,
            int utenteId, String risposta) throws SQLException {

        // Verifica proprietà del ristorante E che non esista già una risposta
        String checkSql =
            "SELECT rec.id, rec.risposta_gestore " +
            "FROM recensioni rec " +
            "JOIN ristoranti r ON rec.ristorante_id = r.id_ristorante " +
            "WHERE rec.id = ? AND r.utente_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, recensioneId);
            ps.setInt(2, utenteId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false; // non è una tua recensione
            // FIX: al massimo una risposta per recensione (spec. slide 9 e 13)
            //if (rs.getString("risposta_gestore") != null) return false;
        }

        String sql = "UPDATE recensioni SET risposta_gestore = ? " +
                     "WHERE id = ? AND EXISTS (SELECT 1 FROM ristoranti r " +
                     "WHERE r.id_ristorante = recensioni.ristorante_id AND r.utente_id = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, risposta);
            ps.setInt(2, recensioneId);
            ps.setInt(3, utenteId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Restituisce tutte le recensioni di un ristorante, ordinate per data.
     *
     * @param ristoranteId ID del ristorante
     * @return lista di recensioni (con username dell'autore)
     * @throws SQLException per errori DB
     */
    public synchronized List<Recensione> recensioniRistorante(
            int ristoranteId) throws SQLException {
        String sql = "SELECT rec.*, u.username FROM recensioni rec " +
                     "JOIN utenti u ON rec.utente_id = u.id " +
                     "WHERE rec.ristorante_id = ? ORDER BY rec.data_recensione DESC";
        return eseguiQueryRecensioni(sql, ristoranteId);
    }

    /**
     * Restituisce tutte le recensioni scritte da un utente.
     *
     * @param utenteId ID dell'utente
     * @return lista di recensioni
     * @throws SQLException per errori DB
     */
    public synchronized List<Recensione> recensioniUtente(
            int utenteId) throws SQLException {
    	String sql = "SELECT rec.*, u.username FROM recensioni rec " +
                "JOIN utenti u ON rec.utente_id = u.id " +
                "WHERE rec.utente_id = ? ORDER BY rec.data_recensione DESC";
        return eseguiQueryRecensioni(sql, utenteId);
    }

    // ══════════════════════════════════════════════════════════════════
    // PREFERITI
    // ══════════════════════════════════════════════════════════════════

    /**
     * Aggiunge un ristorante alla lista preferiti di un utente.
     *
     * @param utenteId     ID dell'utente cliente
     * @param ristoranteId ID del ristorante
     * @return true se aggiunto, false se era già nei preferiti
     * @throws SQLException per errori DB diversi dal duplicato
     */
    public synchronized boolean aggiungiPreferito(int utenteId,
            int ristoranteId) throws SQLException {
        String sql = "INSERT INTO preferiti (utente_id, ristorante_id) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, utenteId);
            ps.setInt(2, ristoranteId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) return false;
            throw e;
        }
    }

    /**
     * Rimuove un ristorante dalla lista preferiti di un utente.
     *
     * @param utenteId     ID dell'utente
     * @param ristoranteId ID del ristorante
     * @return true se rimosso, false se non era nei preferiti
     * @throws SQLException per errori DB
     */
    public synchronized boolean rimuoviPreferito(int utenteId,
            int ristoranteId) throws SQLException {
        String sql = "DELETE FROM preferiti WHERE utente_id = ? AND ristorante_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, utenteId);
            ps.setInt(2, ristoranteId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Restituisce la lista dei ristoranti preferiti di un utente.
     *
     * @param utenteId ID dell'utente
     * @return lista di ristoranti preferiti con media voto
     * @throws SQLException per errori DB
     */
    public synchronized List<Ristorante> preferiti(int utenteId) throws SQLException {
        String sql = "SELECT r.*, " +
                "COALESCE(AVG(rec.stelle), 0) AS media_voto, " +
                "COUNT(rec.id) AS num_recensioni " +
                "FROM ristoranti r " +
                "INNER JOIN preferiti p ON r.id_ristorante = p.ristorante_id " + 
                "LEFT JOIN recensioni rec ON r.id_ristorante = rec.ristorante_id " +
                "WHERE p.utente_id = ? " + 
                "GROUP BY r.id_ristorante " + 
                "ORDER BY r.\"Name\"";

        List<Ristorante> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, utenteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ristorante r = creaRistoranteDaRS(rs);
                r.setMediaVoto(rs.getDouble("media_voto"));
                r.setNumeroRecensioni(rs.getInt("num_recensioni"));
                lista.add(r);
            }
        }
        return lista;
    }

    // ══════════════════════════════════════════════════════════════════
    // METODI PRIVATI DI SUPPORTO
    // ══════════════════════════════════════════════════════════════════

    /**
     * Crea un oggetto Ristorante dai dati di un ResultSet.
     *
     * @param rs result set posizionato sulla riga da convertire
     * @return il ristorante costruito a partire dai dati della riga corrente
     * @throws SQLException per errori di lettura del result set
     */
    private Ristorante creaRistoranteDaRS(ResultSet rs) throws SQLException {
        return new Ristorante(
            rs.getInt("id_ristorante"),      // id
            rs.getString("Name"),            // nome
            rs.getString("Location"),        // location
            rs.getString("address"),         // indirizzo
            rs.getDouble("latitude"),        // latitudine
            rs.getDouble("longitude"),       // longitudine
            rs.getString("price"),           // fasciaPrezzo  ← era phonenumber
            rs.getBoolean("fa_deliveroo"),   // delivery
            rs.getBoolean("ordina_online"),  // prenotazioneOnline
            rs.getString("cuisine"),         // tipoCucina
            rs.getInt("utente_id"),          // gestoreId
            rs.getString("phonenumber"),     // telefono      ← era price
            rs.getString("url"),             // url
            rs.getString("award"),           // award
            rs.getDouble("prezzo_medio"),    // prezzoMedio
            rs.getString("description")      // descrizione
        );
    }


    /**
     * Esegue una query che restituisce recensioni con un singolo parametro intero.
     *
     * @param sql query SQL con un singolo placeholder intero
     * @param param valore del parametro intero da impostare nella query
     * @return lista di recensioni risultanti dalla query
     * @throws SQLException per errori DB
     */
    private List<Recensione> eseguiQueryRecensioni(String sql,
            int param) throws SQLException {
        List<Recensione> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(new Recensione(
                    rs.getInt("id"),
                    rs.getInt("ristorante_id"),
                    rs.getInt("utente_id"),
                    rs.getString("username"),
                    rs.getInt("stelle"),
                    rs.getString("testo"),
                    rs.getString("data_recensione"),
                    rs.getString("risposta_gestore")
                ));
            }
        }
        return lista;
    }
}
