package theknife.common;

import java.io.Serializable;

/**
 * Rappresenta un ristorante nella piattaforma TheKnife.
 * Usata sia dal server (per query DB) che dal client (per visualizzazione).
 * 
 * @author Ayoub Hammou 761589
 * @author Esau Alessandro Argueta Zepeda 761748
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
    private Double prezzoMedio;
    private String descrizione;
    private double mediaVoto;          // Calcolata dal DB, non salvata direttamente
    private int    numeroRecensioni;    // Calcolata dal DB, non salvata direttamente

    // Costruttore completo
    public Ristorante(int id, String nome, String location,
                      String indirizzo, double lat, double lon,
                      String prezzo, boolean delivery, boolean prenotazione,
                      String cucina, int gestoreId, String telefono, String url, String award,
                      double prezzoMedio, String descrizione) {
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
        this.prezzoMedio = prezzoMedio;
        this.descrizione = descrizione;
    }

    // Getters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getLocation() { return location; }
    public String getIndirizzo() { return indirizzo; }
    public double getLatitudine() { return latitudine; }
    public double getLongitudine() { return longitudine; }
    public String getFasciaPrezzo() { return fasciaPrezzo; }
    public boolean isDelivery() { return delivery; }
    public boolean isPrenotazioneOnline() { return prenotazioneOnline; }
    public String getTipoCucina() { return tipoCucina; }
    public int getGestoreId() { return gestoreId; }
    public String getTelefono() { return telefono; }
    public String getUrl() { return url; }
    public String getAward() { return award; }
    public String getDescrizione() { return descrizione; }
    public double getMediaVoto() { return mediaVoto; }
    public double getPrezzoMedio() { return prezzoMedio; }
    public int getNumeroRecensioni() { return numeroRecensioni; }

    // Setters per dati calcolati
    public void setMediaVoto(double m) { this.mediaVoto = m; }
    public void setNumeroRecensioni(int n) { this.numeroRecensioni = n; }

    @Override
    public String toString() {
        return "Ristorante{id=" + id + ", nome='" + nome + "', location='" + location + "'}";
    }
}