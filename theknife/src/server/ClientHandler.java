package theknife.server;

import com.google.gson.*;
import theknife.common.*;
import java.io.*;
import java.net.Socket;
import java.sql.Date;
import java.util.List;

/**
 * Gestisce la comunicazione con un singolo client su thread dedicato.
 * Il logging è delegato a un'interfaccia ServerLogger, che permette
 * di usare questa stessa classe sia con il server CLI che con la GUI.
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class ClientHandler implements Runnable {

    /**
     * Interfaccia di logging: implementata inline da Server (stdout)
     * e da ServerGUI (pannello log grafico).
     */
    public interface ServerLogger {
        /**
         * Registra un'operazione ricevuta da un client.
         *
         * @param ip indirizzo IP del client
         * @param operazione nome dell'operazione richiesta
         */
        void logOperazione(String ip, String operazione);

        /**
         * Registra un errore verificatosi durante la gestione di un client.
         *
         * @param ip indirizzo IP del client
         * @param messaggio descrizione dell'errore
         */
        void logErrore(String ip, String messaggio);

        /**
         * Notifica la disconnessione di un client.
         *
         * @param ip indirizzo IP del client disconnesso
         */
        void clientDisconnesso(String ip);
    }

    private final Socket          socket;
    private final String          ip;
    private final ServerLogger    logger;
    private final DatabaseManager db   = DatabaseManager.getInstance();
    private final Gson            gson = new Gson();
    private       Utente          utenteLoggato;

    /**
     * Costruisce un handler per un client appena connesso.
     *
     * @param socket socket TCP del client
     * @param ip indirizzo IP del client
     * @param logger interfaccia di logging da utilizzare
     */
    public ClientHandler(Socket socket, String ip, ServerLogger logger) {
        this.socket = socket;
        this.ip     = ip;
        this.logger = logger;
    }

    /**
     * Ciclo principale del thread: legge richieste dal client riga per riga,
     * le elabora e invia la risposta corrispondente, finché il client non
     * si disconnette o si verifica un errore di I/O.
     */
    @Override
    public void run() {
        try (BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

            String riga;
            while ((riga = in.readLine()) != null) {
                Messaggio risposta;
                try {
                    Messaggio req = gson.fromJson(riga, Messaggio.class);
                    logger.logOperazione(ip, req.getOperazione());
                    risposta = elabora(req);
                } catch (JsonSyntaxException e) {
                    logger.logErrore(ip, "JSON malformato: " + e.getMessage());
                    risposta = err("JSON malformato");
                } catch (Exception e) {
                    logger.logErrore(ip, "Errore interno: " + e.getMessage());
                    risposta = err("Errore interno del server");
                }
                out.println(gson.toJson(risposta));
            }
        } catch (IOException e) {
            // disconnessione normale del client
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
            logger.clientDisconnesso(ip);
        }
    }

    /**
     * Instrada la richiesta all'handler specifico in base al campo operazione.
     *
     * @param req richiesta ricevuta dal client
     * @return risposta da inviare al client
     */
    private Messaggio elabora(Messaggio req) {
        try {
            switch (req.getOperazione()) {
                case Messaggio.OP_LOGIN:            return gestisciLogin(req);
                case Messaggio.OP_REGISTRA:         return gestisciRegistrazione(req);
                case Messaggio.OP_CERCA_RISTORANTI: return gestisciCerca(req);
                case Messaggio.OP_DETTAGLIO_RISTO:  return gestisciDettaglioRisto(req);
                case Messaggio.OP_AGGIUNGI_RISTO:   return gestisciAggiungiRistorante(req);
                case Messaggio.OP_RECENSIONI_RISTO: return gestisciRecensioniRisto(req);
                case Messaggio.OP_AGGIUNGI_REC:     return gestisciAggiungiRec(req);
                case Messaggio.OP_MODIFICA_REC:     return gestisciModificaRec(req);
                case Messaggio.OP_ELIMINA_REC:      return gestisciEliminaRec(req);
                case Messaggio.OP_RISPONDI_REC:     return gestisciRispondiRec(req);
                case Messaggio.OP_AGGIUNGI_PREF:    return gestisciAggiungiPref(req);
                case Messaggio.OP_RIMUOVI_PREF:     return gestisciRimuoviPref(req);
                case Messaggio.OP_PREFERITI:        return gestisciPreferiti(req);
                case Messaggio.OP_MIE_RECENSIONI:   return gestisciMieRecensioni(req);
                case Messaggio.OP_RIEPILOGO_RISTO:  return gestisciRiepilogo(req);
                case Messaggio.OP_LOGOUT:             utenteLoggato = null; return ok("Logout effettuato");
                case Messaggio.OP_MODIFICA_UTENTE:      return gestisciModificaUtente(req);
                case Messaggio.OP_MODIFICA_RISTORANTE:  return gestisciModificaRistorante(req);
                default: return err("Operazione non riconosciuta: " + req.getOperazione());
            }
        } catch (Exception e) {
            logger.logErrore(ip, "Errore [" + req.getOperazione() + "]: " + e.getMessage());
            return err("Errore interno del server");
        }
    }

    // ── Handler ───────────────────────────────────────────────────────────────

    /**
     * Aggiorna i dati del profilo dell'utente correntemente loggato.
     *
     * @param req richiesta con i nuovi dati del profilo
     * @return esito dell'operazione, con l'utente aggiornato in caso di successo
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciModificaUtente(Messaggio req) throws Exception {
        if (utenteLoggato == null) return err("Non autenticato");
        String dataNascitaStr = param(req, "dataNascita");
        java.util.Date dataNascita = null;
        if (dataNascitaStr != null && !dataNascitaStr.isBlank()) {
            try {
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
                fmt.setLenient(false);
                dataNascita = fmt.parse(dataNascitaStr);
            } catch (Exception e) {
                return err("Formato data non valido (usa gg/mm/aaaa)");
            }
        }
        String nuovaPass = req.getParam("password");
        boolean ok = db.modificaUtente(
            utenteLoggato.getId(),
            param(req, "nome"), param(req, "cognome"), param(req, "username"),
            nuovaPass, dataNascita, param(req, "domicilio"));
        if (!ok) return err("Modifica non riuscita");
        Date sqlDataNascita = (dataNascita != null) ? new Date(dataNascita.getTime()) : null;
        utenteLoggato = new Utente(
                utenteLoggato.getId(),
                param(req, "nome"), param(req, "cognome"),
                param(req, "username").toLowerCase(),
                sqlDataNascita, param(req, "domicilio"), // <── Passa la variabile convertita correttamente
                utenteLoggato.getRuolo());
            return okJson(gson.toJson(utenteLoggato), "Profilo aggiornato");
        }

    /**
     * Aggiorna i dati di un ristorante gestito dall'utente loggato.
     *
     * @param req richiesta con i nuovi dati del ristorante
     * @return esito dell'operazione
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciModificaRistorante(Messaggio req) throws Exception {
        if (utenteLoggato == null || !utenteLoggato.getRuolo())
            return err("Non autorizzato");
        int id = Integer.parseInt(req.getParam("id"));
        double prezzoMedio = 0;
        try { prezzoMedio = Double.parseDouble(req.getParam("prezzoMedio")); } catch (Exception ignored) {}
        double lat = 0, lon = 0;
        try { lat = Double.parseDouble(req.getParam("lat")); } catch (Exception ignored) {}
        try { lon = Double.parseDouble(req.getParam("lon")); } catch (Exception ignored) {}
        boolean ok = db.modificaRistorante(id,
            param(req,"nome"), param(req,"indirizzo"), param(req,"citta"),
            param(req,"nazione"), param(req,"cucina"), param(req,"telefono"),
            param(req,"fasciaPrezzo"), prezzoMedio,
            "true".equals(req.getParam("delivery")),
            "true".equals(req.getParam("prenotazione")),
            param(req,"descrizione"), param(req,"facilities"),
            param(req,"award"), param(req,"greenStar"),
            param(req,"url"), param(req,"website"), lat, lon);
        if (!ok) return err("Modifica ristorante non riuscita");
        return ok("Ristorante aggiornato");
    }

    /**
     * Gestisce l'autenticazione di un utente.
     *
     * @param req richiesta con username e password
     * @return esito del login, con l'utente autenticato in caso di successo
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciLogin(Messaggio req) throws Exception {
        Utente u = db.login(req.getParam("username"), req.getParam("password"));
        if (u == null) return err("Username o password errati");
        utenteLoggato = u;
        return okJson(gson.toJson(u), "Login effettuato");
    }

    /**
     * Gestisce la registrazione di un nuovo utente.
     *
     * @param req richiesta con i dati di registrazione
     * @return esito della registrazione, con l'utente creato in caso di successo
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciRegistrazione(Messaggio req) throws Exception {
        String dataNascitaStr = param(req, "dataNascita");
        java.util.Date dataNascita = null;
        if (dataNascitaStr != null) {
            try {
                java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
                fmt.setLenient(false);
                dataNascita = fmt.parse(dataNascitaStr);
            } catch (Exception e) {
                return err("Formato data non valido (usa gg/mm/aaaa)");
            }
        }
        Utente u = db.registraUtente(
            param(req,"nome"), param(req,"cognome"), param(req,"username"),
            req.getParam("password"), dataNascita,
            param(req,"domicilio"), Boolean.parseBoolean(req.getParam("isRistoratore")));
        if (u == null) return err("Username gia in uso");
        utenteLoggato = u;
        return okJson(gson.toJson(u), "Registrazione avvenuta");
    }

    /**
     * Cerca i ristoranti in base ai filtri indicati e alle regole di
     * business applicabili al ruolo dell'utente (guest, cliente, gestore).
     *
     * @param req richiesta con i parametri di ricerca
     * @return esito della ricerca, con la lista dei ristoranti trovati
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciCerca(Messaggio req) throws Exception {
        String latStr  = param(req, "latitudine");
        String lonStr  = param(req, "longitudine");
        String cittaInput = param(req, "citta");
        String location;
        String cittaPulita = (cittaInput != null) ? cittaInput.trim() : "";
        boolean haCoordinateReali = latStr != null && lonStr != null && 
                !latStr.trim().isEmpty() && !lonStr.trim().isEmpty() &&
                !latStr.equals("0") && !latStr.equals("0.0");

        // --- APPLICAZIONE REGOLE DI BUSINESS RUOLI IN HOME ---
        if (!loggato()) {
            // Caso GUEST: blocco se non c'è input reale
            if (!haCoordinateReali && cittaPulita.isEmpty()) {
                return err("I visitatori (Guest) devono specificare obbligatoriamente una citta per effettuare la ricerca.");
            }
            location = haCoordinateReali ? latStr + "," + lonStr : cittaPulita;
        } else {
            // Caso CLIENTE / RISTORATORE: se non c'è input reale, forza l'uso del domicilio
            if (!haCoordinateReali && cittaPulita.isEmpty()) {
                String domicilioProfilo = utenteLoggato.getLuogoDomicilio();
                
                if (domicilioProfilo == null || domicilioProfilo.trim().isEmpty()) {
                    // Se non ha una residenza nel profilo, non possiamo fare la ricerca automatica
                    return err("La barra di ricerca è vuota. Inserisci una citta o aggiorna il domicilio nel tuo profilo.");
                }
                location = domicilioProfilo.trim();
            } else {
                // Se l'utente ha scritto attivamente qualcosa, usiamo il suo input
                location = haCoordinateReali ? latStr + "," + lonStr : cittaPulita;
            }
        }
        // -----------------------------------------------------
     
        List<Ristorante> lista = db.cercaRistoranti(
            location,
            parseDouble(param(req, "raggioKm")),
            param(req, "cucina"),
            parseDouble(req.getParam("prezzoMin")),   
            parseDouble(req.getParam("prezzoMax")),   
            parseBool(req.getParam("prenotazione")),
            parseBool(req.getParam("delivery")),
            parseInteger(req.getParam("stelleMinime")));
     
        return okJson(gson.toJson(lista), lista.size() + " ristoranti trovati");
    }


    /**
     * Aggiunge un nuovo ristorante di proprietà dell'utente gestore loggato.
     *
     * @param req richiesta con i dati del ristorante
     * @return esito dell'operazione
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciAggiungiRistorante(Messaggio req) throws Exception {
        if (!loggato())      return err("Devi essere loggato");
        if (!utenteLoggato.getRuolo()) return err("Solo i gestori possono aggiungere ristoranti");
        int id = db.aggiungiRistorante(
            param(req,"nome"), param(req,"indirizzo"), param(req,"citta"), param(req,"cucina"),
            parseDoubleZ(req.getParam("longitudine")), parseDoubleZ(req.getParam("latitudine")),
            param(req,"telefono").toString(), param(req,"url"), param(req,"award"),
            param(req,"fasciaPrezzo"), param(req,"descrizione"),
            Boolean.parseBoolean(req.getParam("delivery")),
            Boolean.parseBoolean(req.getParam("prenotazione")),
            prezzoMedioValido(req.getParam("fasciaPrezzo"), req.getParam("prezzoMedio")), utenteLoggato.getId());
        return id < 0 ? err("Errore aggiunta ristorante") : ok("Ristorante aggiunto con ID: " + id);
    }

    /**
     * Recupera il dettaglio di un ristorante tramite il suo identificativo.
     *
     * @param req richiesta con l'id del ristorante
     * @return esito dell'operazione, con il dettaglio del ristorante
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciDettaglioRisto(Messaggio req) throws Exception {
        int id = Integer.parseInt(req.getParam("ristoranteId"));
        Ristorante r = db.ristorantePerId(id);
        return r == null ? err("Ristorante non trovato") : okJson(gson.toJson(r), "Dettaglio ristorante");
    }

    /**
     * Recupera le recensioni relative a un ristorante.
     *
     * @param req richiesta con l'id del ristorante
     * @return esito dell'operazione, con la lista delle recensioni
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciRecensioniRisto(Messaggio req) throws Exception {
        List<Recensione> lista = db.recensioniRistorante(Integer.parseInt(req.getParam("ristoranteId")));
        return okJson(gson.toJson(lista), lista.size() + " recensioni");
    }

    /**
     * Aggiunge una recensione da parte dell'utente cliente loggato.
     *
     * @param req richiesta con i dati della recensione
     * @return esito dell'operazione
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciAggiungiRec(Messaggio req) throws Exception {
        if (!loggato())       return err("Devi essere loggato");
        if (utenteLoggato.getRuolo()) return err("Solo i clienti possono recensire");
        db.aggiungiRecensione(Integer.parseInt(req.getParam("ristoranteId")),
            utenteLoggato.getId(), Integer.parseInt(req.getParam("stelle")), req.getParam("testo"));
        return ok("Recensione aggiunta");
    }

    /**
     * Modifica una recensione esistente dell'utente loggato.
     *
     * @param req richiesta con l'id della recensione e i nuovi dati
     * @return esito dell'operazione
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciModificaRec(Messaggio req) throws Exception {
        if (!loggato()) return err("Devi essere loggato");
        String s = param(req, "stelle");
        boolean ok = db.modificaRecensione(Integer.parseInt(req.getParam("recensioneId")),
            utenteLoggato.getId(), s == null ? null : Integer.parseInt(s), param(req, "testo"));
        return ok ? ok("Recensione modificata") : err("Recensione non trovata");
    }

    /**
     * Elimina una recensione dell'utente loggato.
     *
     * @param req richiesta con l'id della recensione da eliminare
     * @return esito dell'operazione
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciEliminaRec(Messaggio req) throws Exception {
        if (!loggato()) return err("Devi essere loggato");
        boolean ok = db.eliminaRecensione(Integer.parseInt(req.getParam("recensioneId")), utenteLoggato.getId());
        return ok ? ok("Recensione eliminata") : err("Recensione non trovata");
    }

    /**
     * Aggiunge la risposta del gestore a una recensione.
     *
     * @param req richiesta con l'id della recensione e il testo della risposta
     * @return esito dell'operazione
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciRispondiRec(Messaggio req) throws Exception {
        if (!loggato())      return err("Devi essere loggato");
        if (!utenteLoggato.getRuolo()) return err("Solo i gestori possono rispondere");
        boolean ok = db.rispondiRecensione(Integer.parseInt(req.getParam("recensioneId")),
            utenteLoggato.getId(), req.getParam("risposta"));
        return ok ? ok("Risposta salvata") : err("Non puoi rispondere a questa recensione");
    }

    /**
     * Aggiunge un ristorante ai preferiti dell'utente loggato.
     *
     * @param req richiesta con l'id del ristorante
     * @return esito dell'operazione
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciAggiungiPref(Messaggio req) throws Exception {
        if (!loggato()) return err("Devi essere loggato");
        boolean ok = db.aggiungiPreferito(utenteLoggato.getId(), Integer.parseInt(req.getParam("ristoranteId")));
        return ok ? ok("Aggiunto ai preferiti") : err("Gia nei preferiti");
    }

    /**
     * Rimuove un ristorante dai preferiti dell'utente loggato.
     *
     * @param req richiesta con l'id del ristorante
     * @return esito dell'operazione
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciRimuoviPref(Messaggio req) throws Exception {
        if (!loggato()) return err("Devi essere loggato");
        boolean ok = db.rimuoviPreferito(utenteLoggato.getId(), Integer.parseInt(req.getParam("ristoranteId")));
        return ok ? ok("Rimosso dai preferiti") : err("Ristorante non nei preferiti");
    }

    /**
     * Recupera la lista dei ristoranti preferiti. L'utente viene identificato
     * preferibilmente dalla sessione del thread; se non disponibile, viene
     * usato l'id passato esplicitamente come parametro dal client.
     *
     * @param req richiesta, eventualmente con l'id utente come parametro
     * @return esito dell'operazione, con la lista dei preferiti
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciPreferiti(Messaggio req) throws Exception {
        int idUtente = -1;
        
        // Prova prima a recuperarlo dalla sessione del thread
        if (loggato()) {
            idUtente = utenteLoggato.getId();
        } else {
            // Se il thread non è sincronizzato, recuperalo dal parametro inviato dal client
            String utenteIdParam = req.getParam("utenteId");
            if (utenteIdParam != null && !utenteIdParam.isBlank()) {
                idUtente = Integer.parseInt(utenteIdParam.trim());
            }
        }
        
        // Se non lo trovi in nessun modo, allora dai errore
        if (idUtente == -1) return err("Devi essere loggato per vedere i preferiti");
        
        List<Ristorante> lista = db.preferiti(idUtente);
        return okJson(gson.toJson(lista), lista.size() + " preferiti");
    }


    /**
     * Recupera le recensioni scritte dall'utente loggato.
     *
     * @param req richiesta (nessun parametro aggiuntivo necessario)
     * @return esito dell'operazione, con la lista delle recensioni
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciMieRecensioni(Messaggio req) throws Exception {
        if (!loggato()) return err("Devi essere loggato");
        List<Recensione> lista = db.recensioniUtente(utenteLoggato.getId());
        return okJson(gson.toJson(lista), lista.size() + " recensioni");
    }

    /**
     * Recupera il riepilogo dei ristoranti gestiti dall'utente gestore loggato.
     *
     * @param req richiesta (nessun parametro aggiuntivo necessario)
     * @return esito dell'operazione, con la lista dei ristoranti gestiti
     * @throws Exception se si verifica un errore durante l'accesso al database
     */
    private Messaggio gestisciRiepilogo(Messaggio req) throws Exception {
        if (!loggato())      return err("Devi essere loggato");
        if (!utenteLoggato.getRuolo()) return err("Solo per gestori");
        List<Ristorante> lista = db.ristorantiDelGestore(utenteLoggato.getId());
        return okJson(gson.toJson(lista), lista.size() + " ristoranti");
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /**
     * Verifica che il prezzo medio indicato sia coerente con la fascia di
     * prezzo selezionata (numero di simboli).
     *
     * @param fascia fascia di prezzo (es. "$", "$$", "€€€")
     * @param valore prezzo medio proposto, come stringa
     * @return il prezzo medio validato
     * @throws IllegalArgumentException se il prezzo non è coerente con la fascia
     */
    private double prezzoMedioValido(String fascia, String valore) {
        double p = parseDoubleZ(valore);
        int livello = fascia == null ? 1 : Math.max(1, Math.min(4, fascia.trim().length()));
        boolean ok = switch (livello) {
            case 1 -> p >= 0 && p < 20;
            case 2 -> p >= 20 && p < 50;
            case 3 -> p >= 50 && p <= 80;
            default -> p >= 80;
        };
        if (!ok) throw new IllegalArgumentException("Prezzo medio non coerente con la fascia selezionata");
        return p;
    }

    /** @return {@code true} se un utente è attualmente loggato su questa connessione */
    private boolean loggato() { return utenteLoggato != null; }

    /**
     * Estrae un parametro dalla richiesta, normalizzandolo.
     *
     * @param req richiesta da cui estrarre il parametro
     * @param k chiave del parametro
     * @return valore del parametro (trim applicato), oppure {@code null} se assente o vuoto
     */
    private String  param(Messaggio req, String k) { String v = req.getParam(k); return (v == null || v.isBlank()) ? null : v.trim(); }

    /**
     * Converte una stringa in {@code Double}, restituendo {@code null} in caso di errore.
     *
     * @param v stringa da convertire
     * @return valore convertito, o {@code null} se {@code v} è {@code null} o non valida
     */
    private Double  parseDouble(String v)  { try { return v != null ? Double.parseDouble(v.trim())  : null; } catch (NumberFormatException e) { return null; } }

    /**
     * Converte una stringa in {@code double}, restituendo {@code 0.0} in caso di errore.
     *
     * @param v stringa da convertire
     * @return valore convertito, o {@code 0.0} se {@code v} è {@code null} o non valida
     */
    private double  parseDoubleZ(String v) { try { return v != null ? Double.parseDouble(v.trim())  : 0.0;  } catch (NumberFormatException e) { return 0.0; } }

    /**
     * Converte una stringa in {@code Boolean}, interpretando "true" o "t" (case-insensitive) come vero.
     *
     * @param v stringa da convertire
     * @return valore convertito, o {@code null} se {@code v} è {@code null} o vuota
     */
    private Boolean parseBool(String v) { return (v == null || v.isBlank()) ? null : v.trim().equalsIgnoreCase("true") || v.trim().equals("t"); }

    /**
     * Converte una stringa in {@code Integer}, restituendo {@code null} in caso di errore.
     *
     * @param v stringa da convertire
     * @return valore convertito, o {@code null} se {@code v} è {@code null} o non valida
     */
    private Integer parseInteger(String v) { try { return v != null ? Integer.parseInt(v.trim())    : null; } catch (NumberFormatException e) { return null; } }

    /**
     * Costruisce un messaggio di risposta con esito positivo.
     *
     * @param msg messaggio descrittivo dell'esito
     * @return messaggio di risposta con esito {@link Messaggio#ESITO_OK}
     */
    private Messaggio ok(String msg)                { Messaggio m = new Messaggio(); m.setEsito(Messaggio.ESITO_OK);     m.addParam("messaggio", msg); return m; }

    /**
     * Costruisce un messaggio di risposta con esito negativo.
     *
     * @param msg messaggio descrittivo dell'errore
     * @return messaggio di risposta con esito {@link Messaggio#ESITO_ERRORE}
     */
    private Messaggio err(String msg)               { Messaggio m = new Messaggio(); m.setEsito(Messaggio.ESITO_ERRORE); m.addParam("errore", msg);    return m; }

    /**
     * Costruisce un messaggio di risposta positivo con dati JSON allegati.
     *
     * @param json dati serializzati in formato JSON
     * @param msg messaggio descrittivo dell'esito
     * @return messaggio di risposta con esito positivo e dati JSON allegati
     */
    private Messaggio okJson(String json, String msg) { Messaggio m = ok(msg); m.setDatiJson(json); return m; }
}
