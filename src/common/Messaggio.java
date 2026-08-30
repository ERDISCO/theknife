package theknife.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Contenitore JSON per la comunicazione Client-Server.
 * Il client manda un Messaggio con operazione + parametri.
 * Il server risponde con un Messaggio con esito + dati.
 *
 * @author Ayoub Hammou                     761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda   761748 - sede di Varese
 * @version 2.0
 */
public class Messaggio implements Serializable {
    private static final long serialVersionUID = 4L;

    // Costanti per le operazioni
    public static final String OP_LOGIN           = "LOGIN";
    public static final String OP_REGISTRA        = "REGISTRA";
    public static final String OP_CERCA_RISTORANTI = "CERCA_RISTORANTI";
    public static final String OP_DETTAGLIO_RISTO = "DETTAGLIO_RISTORANTE";
    public static final String OP_AGGIUNGI_RISTO  = "AGGIUNGI_RISTORANTE";
    public static final String OP_RECENSIONI_RISTO = "RECENSIONI_RISTORANTE";
    public static final String OP_AGGIUNGI_REC    = "AGGIUNGI_RECENSIONE";
    public static final String OP_MODIFICA_REC    = "MODIFICA_RECENSIONE";
    public static final String OP_ELIMINA_REC     = "ELIMINA_RECENSIONE";
    public static final String OP_RISPONDI_REC    = "RISPONDI_RECENSIONE";
    public static final String OP_AGGIUNGI_PREF   = "AGGIUNGI_PREFERITO";
    public static final String OP_RIMUOVI_PREF    = "RIMUOVI_PREFERITO";
    public static final String OP_PREFERITI       = "LISTA_PREFERITI";
    public static final String OP_MIE_RECENSIONI  = "MIE_RECENSIONI";
    public static final String OP_RIEPILOGO_RISTO = "RIEPILOGO_RISTORANTE";
    public static final String OP_LOGOUT          = "LOGOUT";
    public static final String OP_MODIFICA_UTENTE    = "MODIFICA_UTENTE";
    public static final String OP_MODIFICA_RISTORANTE = "MODIFICA_RISTORANTE";

    public static final String ESITO_OK    = "OK";
    public static final String ESITO_ERRORE = "ERRORE";

    private String operazione;
    private Map<String, String> params;
    private String esito;
    private String datiJson; // i dati serializzati come stringa JSON

    /** Costruisce un messaggio vuoto, senza operazione impostata. */
    public Messaggio() {
        this.params = new HashMap<>();
    }

    /**
     * Costruisce un messaggio associato a una specifica operazione.
     *
     * @param operazione nome dell'operazione richiesta (una delle costanti {@code OP_*})
     */
    public Messaggio(String operazione) {
        this.operazione = operazione;
        this.params = new HashMap<>();
    }

    /**
     * Aggiunge un parametro alla richiesta/risposta.
     * Se {@code chiave} è {@code null} la chiamata viene ignorata.
     * Un valore {@code null} viene memorizzato come stringa vuota per evitare
     * di inserire la stringa letterale "null" nei parametri.
     *
     * @param chiave nome del parametro
     * @param valore valore del parametro (può essere {@code null})
     */
    public void addParam(String chiave, String valore) {
        if (this.params == null) {
            this.params = new HashMap<>();
        }
        if (chiave != null) {
            // Evita di inserire "null" come stringa nei parametri
            this.params.put(chiave, valore != null ? valore.trim() : "");
        }
    }

    /**
     * Restituisce il valore di un parametro.
     *
     * @param chiave nome del parametro
     * @return valore associato alla chiave, o stringa vuota se assente
     */
    public String getParam(String chiave) {
        if (params == null) return "";
        String val = params.get(chiave);
        return val != null ? val : "";
    }

    /**
     * Verifica se un parametro esiste e non è vuoto.
     *
     * @param chiave nome del parametro
     * @return {@code true} se il parametro esiste ed è non vuoto
     */
    public boolean hasParam(String chiave) {
        if (params == null) return false;
        String val = params.get(chiave);
        return val != null && !val.trim().isEmpty();
    }

    /** @return nome dell'operazione richiesta */
    public String getOperazione() { return operazione; }
    /** @param o nome dell'operazione da impostare */
    public void setOperazione(String o) { this.operazione = o; }

    /** @return mappa dei parametri (mai {@code null}) */
    public Map<String, String> getParams() { 
        if (params == null) params = new HashMap<>();
        return params; 
    }
    /** @param params mappa dei parametri da impostare; se {@code null} viene usata una mappa vuota */
    public void setParams(Map<String, String> params) { 
        this.params = params != null ? params : new HashMap<>(); 
    }

    /** @return esito della risposta ({@link #ESITO_OK} o {@link #ESITO_ERRORE}) */
    public String getEsito() { return esito; }
    /** @param e esito da impostare */
    public void setEsito(String e) { this.esito = e; }

    /** @return dati aggiuntivi serializzati in formato JSON */
    public String getDatiJson() { return datiJson; }
    /** @param d dati serializzati in formato JSON */
    public void setDatiJson(String d) { this.datiJson = d; }

    /** @return {@code true} se l'esito è {@link #ESITO_OK} */
    public boolean isOk() { return ESITO_OK.equals(esito); }

    @Override
    public String toString() {
        return "Messaggio{" +
                "operazione='" + operazione + '\'' +
                ", params=" + params +
                ", esito='" + esito + '\'' +
                ", datiJson='" + datiJson + '\'' +
                '}';
    }
}
