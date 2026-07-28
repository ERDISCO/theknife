package theknife.common;

import java.io.Serializable;
import java.sql.Date;

/**
 * Rappresenta un utente registrato (cliente o gestore).
 * La password NON viene mai inviata al client dopo il login.
 * @author Esau Argueta 761748 
 * @author Ayoub Hammou 761589
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

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String h) { this.passwordHash = h; }
    public Date getDataNascita() { return dataNascita; }
    public String getLuogoDomicilio() { return luogoDomicilio; }
    public Boolean getRuolo() { return is_ristoratore; }
}
