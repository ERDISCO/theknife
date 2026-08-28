package theknife.common;
import java.io.Serializable;

/**
 * Rappresenta una recensione scritta da un cliente per un ristorante.
 * Può contenere una risposta del gestore (massimo una).
 *
 * @author Ayoub Hammou                    761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda  761748 - sede di Varese
 * @version 2.0
 */

public class Recensione implements Serializable {
	private static final long serialVersionUID = 1L;
    private int    id_recensione;
    private int    ristoranteId;
    private int    utenteId;
    private String usernameAutore; //far vedere autore 
    private int    stelle;         
    private String testo;
    private String dataRecensione;
    private String rispostaGestore; // null se nessuna risposta

    /**
     * Costruisce una recensione completa.
     *
     * @param id identificativo univoco della recensione
     * @param ristoranteId identificativo del ristorante recensito
     * @param utenteId identificativo dell'utente autore
     * @param usernameAutore username visualizzato dell'autore
     * @param stelle valutazione in stelle
     * @param testo testo della recensione
     * @param data data della recensione
     * @param risposta eventuale risposta del gestore, {@code null} se assente
     */
    public Recensione(int id, int ristoranteId, int utenteId,
                      String usernameAutore, int stelle, String testo,
                      String data, String risposta) {
        this.id_recensione = id;
        this.ristoranteId = ristoranteId;
        this.utenteId = utenteId;
        this.usernameAutore = usernameAutore;
        this.stelle = stelle;
        this.testo = testo;
        this.dataRecensione = data;
        this.rispostaGestore = risposta;
    }

    /** @return identificativo univoco della recensione */
    public int getId() { return id_recensione; }
    /** @return identificativo del ristorante recensito */
    public int getRistoranteId() { return ristoranteId; }
    /** @return identificativo dell'utente autore */
    public int getUtenteId() { return utenteId; }
    /** @return username visualizzato dell'autore */
    public String getUsernameAutore() { return usernameAutore; }
    /** @return valutazione in stelle */
    public int getStelle() { return stelle; }
    /** @return testo della recensione */
    public String getTesto() { return testo; }
    /** @return data della recensione */
    public String getDataRecensione() { return dataRecensione; }
    /** @return risposta del gestore, {@code null} se assente */
    public String getRispostaGestore() { return rispostaGestore; }
    /** @return {@code true} se il gestore ha risposto alla recensione */
    public boolean haRisposta() { return rispostaGestore != null && !rispostaGestore.isEmpty(); }

    /**
     * Due recensioni sono considerate uguali se hanno lo stesso identificativo.
     *
     * @param o oggetto da confrontare
     * @return {@code true} se {@code o} è una {@code Recensione} con lo stesso id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recensione)) return false;
        return id_recensione == ((Recensione) o).id_recensione;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id_recensione); }
}
