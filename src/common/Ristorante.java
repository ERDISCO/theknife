package theknife.common;

import java.io.Serializable;

/**
 * Rappresenta un ristorante nella piattaforma TheKnife.
 * Usata sia dal server (per query DB) che dal client (per visualizzazione).
 *
 * @author Ayoub Hammou 761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class Ristorante implements Serializable {
    private static final long serialVersionUID = 2L;
    
    private int    id;
    private String nome;
    private String location;
    private String indirizzo;
    private double latitudine;
    private double longitudine;
    private String fasciaPrezzo;       // Es. "$", "$$", "€€€"
    private boolean delivery;
    private boolean prenotazioneOnline;
    private String tipoCucina;
    private int    gestoreId;
    private String telefono;
    private String url;
    private String award;
    private double prezzoMedio;
    private String descrizione;
    private double mediaVoto;          // Calcolata dal DB, non salvata direttamente
    private int    numeroRecensioni;    // Calcolata dal DB, non salvata direttamente
    private double distanzaKm;

    /**
     * Costruttore completo di un ristorante.
     * Se {@code prezzoMedio} è pari a {@code 0.0}, viene stimato automaticamente
     * in base alla fascia di prezzo tramite {@link #getPrezzoMedioStimato()}.
     *
     * @param id identificativo univoco del ristorante
     * @param nome nome del ristorante
     * @param location città o area geografica
     * @param indirizzo indirizzo completo
     * @param lat latitudine
     * @param lon longitudine
     * @param prezzo fascia di prezzo (es. "$", "$$", "€€€")
     * @param delivery {@code true} se il ristorante offre consegna a domicilio
     * @param prenotazione {@code true} se supporta la prenotazione online
     * @param cucina tipo di cucina offerta
     * @param gestoreId identificativo dell'utente gestore
     * @param telefono numero di telefono
     * @param url sito web del ristorante
     * @param award eventuale riconoscimento/premio ricevuto
     * @param prezzoMedio prezzo medio; se {@code 0.0} viene stimato dalla fascia di prezzo
     * @param descrizione descrizione testuale del ristorante
     */
    public Ristorante(int id, String nome, String location,
                      String indirizzo, double lat, double lon,
                      String prezzo, boolean delivery, boolean prenotazione,
                      String cucina, int gestoreId, String telefono, 
                      String url, String award, double prezzoMedio,String descrizione) {
        this.id = id;
        this.nome = nome;
        this.location = location;
        this.indirizzo = indirizzo;
        this.latitudine = lat;
        this.longitudine = lon;
        this.fasciaPrezzo = prezzo;
        this.delivery = delivery;
        this.prenotazioneOnline = prenotazione;
        this.tipoCucina = cucina;
        this.gestoreId = gestoreId;
        this.telefono = telefono;
        this.url = url;
        this.award = award;
        if (prezzoMedio == 0.0) {
            this.prezzoMedio = getPrezzoMedioStimato();
        } else {
            this.prezzoMedio = prezzoMedio;
        }
        this.descrizione = descrizione;
    }

    // Getters
    /** @return identificativo univoco del ristorante */
    public int getId() { return id; }
    /** @return nome del ristorante */
    public String getNome() { return nome; }
    /** @return città o area geografica */
    public String getLocation() { return location; }
    /** @return indirizzo completo */
    public String getIndirizzo() { return indirizzo; }
    /** @return latitudine */
    public double getLatitudine() { return latitudine; }
    /** @return longitudine */
    public double getLongitudine() { return longitudine; }
    /** @return fascia di prezzo (es. "$", "$$", "€€€") */
    public String getFasciaPrezzo() { return fasciaPrezzo; }
    /** @return {@code true} se il ristorante offre consegna a domicilio */
    public boolean isDelivery() { return delivery; }
    /** @return {@code true} se supporta la prenotazione online */
    public boolean isPrenotazioneOnline() { return prenotazioneOnline; }
    /** @return tipo di cucina offerta */
    public String getTipoCucina() { return tipoCucina; }
    /** @return identificativo dell'utente gestore */
    public int getGestoreId() { return gestoreId; }
    /** @return numero di telefono */
    public String getTelefono() { return telefono; }
    /** @return sito web del ristorante */
    public String getUrl() { return url; }
    /** @return eventuale riconoscimento/premio ricevuto */
    public String getAward() { return award; }
    /** @return descrizione testuale del ristorante */
    public String getDescrizione() { return descrizione; }
    /** @return media dei voti, calcolata dal DB */
    public double getMediaVoto() { return mediaVoto; }
    /** @return numero di recensioni, calcolato dal DB */
    public int getNumeroRecensioni() { return numeroRecensioni; }
    /** @return distanza in km dal punto di ricerca */
    public double getDistanzaKm() { return distanzaKm; }
    
    // Setters per dati calcolati
    /** @param m media dei voti da impostare */
    public void setMediaVoto(double m) { this.mediaVoto = m; }
    /** @param n numero di recensioni da impostare */
    public void setNumeroRecensioni(int n) { this.numeroRecensioni = n; }
    /** @param distanzaKm distanza in km da impostare */
    public void setDistanzaKm(double distanzaKm) { this.distanzaKm = distanzaKm; }

    @Override
    public String toString() {
        return "Ristorante{id=" + id + ", nome='" + nome + "', location='" + location + "'}";
    }

    /**
     * Stima il prezzo medio a partire dalla fascia di prezzo, quando
     * quest'ultimo non è disponibile esplicitamente.
     *
     * @return prezzo medio stimato in base al numero di simboli della fascia di prezzo
     */
    public double getPrezzoMedioStimato() {
        if (this.fasciaPrezzo == null || this.fasciaPrezzo.isBlank()) {
            return 0.0;
        }
        int livello = this.fasciaPrezzo.trim().length();
        return switch (livello) {
            case 1  -> 15.0;  // Economico (€)
            case 2  -> 30.0;  // Medio (€€)
            case 3  -> 55.0;  // Medio-Alto (€€€)
            case 4  -> 90.0;  // Lusso (€€€€)
            default -> 25.0;  // Fallback generico
        };
    }
}
