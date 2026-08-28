package theknife.gui;

import theknife.client.ClientTK;
import theknife.common.Utente;

/**
 * Singleton che mantiene lo stato globale della sessione GUI:
 *  - la connessione socket verso il server
 *  - l'utente attualmente loggato (null se guest)
 *
 * Viene inizializzata da LoginFrame e letta da tutti i pannelli.
 *
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @version 2.0
 */
public class SessioneUtente {

    private static SessioneUtente instance;

    private ClientTK connessione;
    private Utente utente;           // null se guest

    private SessioneUtente() {}

    /** @return l'unica istanza della sessione (thread-safe) */
    public static synchronized SessioneUtente getInstance() {
        if (instance == null) {
            instance = new SessioneUtente();
        }
        return instance;
    }

    /**
     * Chiamata da LoginFrame dopo login o scelta guest.
     *
     * @param connessione connessione al server da associare alla sessione
     * @param utente utente autenticato, o {@code null} per l'accesso ospite
     */
    public void avvia(ClientTK connessione, Utente utente) {
        this.connessione = connessione;
        this.utente      = utente;
    }

    /** Azzera solo l'utente, mantiene il socket aperto. */
    public void reset() { utente = null; }

    /** Chiamata da HomeFrame al logout. Chiude anche il socket. */
    public void termina() {
        if (connessione != null && connessione.isConnessa()) {
            connessione.disconnetti();
        }
        connessione = null;
        utente      = null;
    }



    // ── Getter ────────────────────────────────────────────────────────────────

    /** @return la connessione al server associata alla sessione corrente */
    public ClientTK getConnessione() { return connessione; }
    /** @return l'utente loggato, o {@code null} se l'accesso è come ospite */
    public Utente getUtente()      { return utente; }

    /** @return {@code true} se l'utente ha fatto login (non guest) */
    public boolean isLoggato()     { return utente != null; }

    /** @return {@code true} se l'utente loggato è un ristoratore/gestore */
    public boolean isGestore()     { return utente != null && utente.getRuolo(); }

    /** @return {@code true} se l'utente loggato è un cliente normale */
    public boolean isCliente()     { return utente != null && !utente.getRuolo(); }

    /** @return nome da mostrare in UI: "nome cognome", "Ospite" se guest */
    public String getNomeDisplay() {
        if (utente == null) return "Ospite";
        return utente.getNome() + " " + utente.getCognome();
    }
}
