package theknife.common;

import java.io.Serializable;
import java.sql.Date;

/**
 * Rappresenta un utente registrato (cliente o gestore).
 * La password NON viene mai inviata al client dopo il login.
 *
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @author Ayoub Hammou 761589 - sede di Varese
 * @version 2.0
 */
public class Utente implements Serializable {
    private int    id;
    private String nome;
    private String cognome;
    private String username;
    private String passwordHash; // solo lato server
    private Date dataNascita;
    private String luogoDomicilio;
    private boolean is_ristoratore; // "cliente" o "gestore"

    /**
     * Costruisce un utente registrato.
     *
     * @param id identificativo univoco dell'utente
     * @param nome nome dell'utente
     * @param cognome cognome dell'utente
     * @param username username scelto dall'utente
     * @param dataNascita data di nascita
     * @param domicilio luogo di domicilio
     * @param is_ristoratore {@code true} se l'utente è un gestore/ristoratore, {@code false} se è un cliente
     */
    public Utente(int id, String nome, String cognome, String username,
    		 Date dataNascita, String domicilio, Boolean is_ristoratore) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.dataNascita = dataNascita;
        this.luogoDomicilio = domicilio;
        this.is_ristoratore = is_ristoratore;
    }

    /** @return identificativo univoco dell'utente */
    public int getId() { return id; }
    /** @return nome dell'utente */
    public String getNome() { return nome; }
    /** @return cognome dell'utente */
    public String getCognome() { return cognome; }
    /** @return username dell'utente */
    public String getUsername() { return username; }
    /** @return hash della password, disponibile solo lato server */
    public String getPasswordHash() { return passwordHash; }
    /** @param h hash della password da impostare */
    public void setPasswordHash(String h) { this.passwordHash = h; }
    /** @return data di nascita dell'utente */
    public Date getDataNascita() { return dataNascita; }
    /** @return luogo di domicilio dell'utente */
    public String getLuogoDomicilio() { return luogoDomicilio; }
    /** @return {@code true} se l'utente è un gestore/ristoratore, {@code false} se è un cliente */
    public Boolean getRuolo() { return is_ristoratore; }
}
