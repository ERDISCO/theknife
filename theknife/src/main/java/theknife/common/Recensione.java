package theknife.common;
import java.io.Serializable;

/**
 * Rappresenta una recensione scritta da un cliente per un ristorante.
 * Può contenere una risposta del gestore (massimo una).
 * @author Ayoub Hammou 761589
 * @author Esau Alessandro Argueta Zepeda 761748
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

    public int getId() { return id_recensione; }
    public int getRistoranteId() { return ristoranteId; }
    public int getUtenteId() { return utenteId; }
    public String getUsernameAutore() { return usernameAutore; }
    public int getStelle() { return stelle; }
    public String getTesto() { return testo; }
    public String getDataRecensione() { return dataRecensione; }
    public String getRispostaGestore() { return rispostaGestore; }
    public boolean haRisposta() { return rispostaGestore != null && !rispostaGestore.isEmpty(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recensione)) return false;
        return id_recensione == ((Recensione) o).id_recensione;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id_recensione); }
}
