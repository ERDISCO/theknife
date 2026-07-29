package theknife.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Contenitore JSON per la comunicazione Client-Server.
 * Il client manda un Messaggio con operazione + parametri.
 * Il server risponde con un Messaggio con esito + dati.
 *
 * @author Ayoub Hammou 761589
 * @author Esau Argueta 761748
 */
public class Messaggio implements Serializable {

    // Costanti per le operazioni (evita stringhe sparse nel codice)
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

    public static final String ESITO_OK    = "OK";
    public static final String ESITO_ERRORE = "ERRORE";

    private String operazione;
    private Map<String, String> params;
    private String esito;
    private String datiJson; // i dati serializzati come stringa JSON

    public Messaggio() {
        this.params = new HashMap<>();
    }

    public Messaggio(String operazione) {
        this.operazione = operazione;
        this.params = new HashMap<>();
    }

    public void addParam(String chiave, String valore) {
        params.put(chiave, valore);
    }

    public String getParam(String chiave) {
        return params.getOrDefault(chiave, "");
    }

    public String getOperazione() { return operazione; }
    public void setOperazione(String o) { this.operazione = o; }
    public Map<String, String> getParams() { return params; }
    public String getEsito() { return esito; }
    public void setEsito(String e) { this.esito = e; }
    public String getDatiJson() { return datiJson; }
    public void setDatiJson(String d) { this.datiJson = d; }

    public boolean isOk() { return ESITO_OK.equals(esito); }
}
