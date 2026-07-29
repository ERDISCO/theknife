package theknife.common;
import java.io.Serializable;


/**
 * Rappresenta un ristorante nella piattaforma TheKnife.
 * Usata sia dal server (per query DB) che dal client (per visualizzazione).
 * @author Ayoub Hammou 					761589
 * @author Esau Alessandro Argueta Zepeda 	761748
 */
public class Ristorante implements Serializable {
	private static final long serialVersionUID = 2L;
	
    private int    id;
    private String nome;
    private String location;
    private String indirizzo;
    private double latitudine;
    private double longitudine;
    private int    fasciaPrezzo;
    private boolean delivery;
    private boolean prenotazioneOnline;
    private String tipoCucina;
    private int    gestoreId;
    private String telefono;
    private String url;
    private String award;
    private String pagamento;
    private String descrizione;
    private double mediaVoto;       // calcolata dal DB, non salvata
    private int    numeroRecensioni; // calcolata dal DB, non salvata


    // Costruttore completo
    public Ristorante(int id, String nome, String location,
                      String indirizzo, double lat, double lon,
                      int prezzo, boolean delivery, boolean prenotazione,
                      String cucina, int gestoreId, String telefono, String url, String award,
                      String pagamento, String descrizione) {
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
        this.pagamento = pagamento;
        this.descrizione = descrizione;
    }

    // Getters e setters per ogni campo
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getLocation() { return location; }
    public String getIndirizzo() { return indirizzo; }
    public double getLatitudine() { return latitudine; }
    public double getLongitudine() { return longitudine; }
    public int getFasciaPrezzo() { return fasciaPrezzo; }
    public boolean isDelivery() { return delivery; }
    public boolean isPrenotazioneOnline() { return prenotazioneOnline; }
    public String getTipoCucina() { return tipoCucina; }
    public int getGestoreId() { return gestoreId; }
    public String getTelefono() { return telefono; }
    public String getUrl() { return url; }
    public String getAward() { return award; }
    public String getPagamento() { return pagamento; }
    public String getDescrizione() { return descrizione; }
    public double getMediaVoto() { return mediaVoto; }
    public int getNumeroRecensioni() { return numeroRecensioni; }
    public void setMediaVoto(double m) { this.mediaVoto = m; }
    public void setNumeroRecensioni(int n) { this.numeroRecensioni = n; }
}
