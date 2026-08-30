package theknife.gui;

import com.google.gson.Gson;
import theknife.client.ClientTK;
import theknife.common.Messaggio;
import theknife.common.Ristorante;
import theknife.common.Utente;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Dialog unificato per la modifica del profilo utente e dei dati di un ristorante.
 * Viene aperto con un Utente (modifica profilo) oppure con un Ristorante (modifica ristorante).
 * Uno dei due argomenti sarà null.
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class ModificaDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final ClientTK     connessione;
    private final Utente       utente;
    private final Ristorante   ristorante;
    private final CardLayout   cards     = new CardLayout();
    private final JPanel       cardPanel = new JPanel(cards);

    private static final String CARD_UTENTE     = "UTENTE";
    private static final String CARD_RISTORANTE = "RISTORANTE";

    // ── Campi UTENTE ──────────────────────────────────────────────────────────
    private final JTextField     uNome      = UI.creaInput("Nome *");
    private final JTextField     uCognome   = UI.creaInput("Cognome *");
    private final JTextField     uUsername  = UI.creaInput("Username o e-mail *");
    private final JPasswordField uPass      = UI.creaInputPassword("Nuova password (lascia vuoto per non cambiarla)");
    private final JPasswordField uPass2     = UI.creaInputPassword("Conferma nuova password");
    private final JTextField     uDomicilio = UI.creaInput("Domicilio");
    private final JTextField     uData      = UI.creaInput("Data di nascita (gg/mm/aaaa)");
    private final JButton        btnSalvaUtente  = UI.creaBottone("Salva modifiche", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
    private final JLabel         lblStatoUtente  = UI.creaLabelInfo(" ");

    // ── Campi RISTORANTE ──────────────────────────────────────────────────────
    private final JTextField  rNome       = UI.creaInput("Nome del ristorante *");
    private final JTextField  rIndirizzo  = UI.creaInput("Indirizzo (via e numero) *");
    private final JTextField  rCitta      = UI.creaInput("Città *");
    private final JTextField  rNazione    = UI.creaInput("Nazione (es: Italia)");
    private final JTextField  rCucina     = UI.creaInput("Tipo di cucina *");
    private final JTextField  rTelefono   = UI.creaInput("Telefono");
    private final JTextField  rAward      = UI.creaInput("Award / Riconoscimento");
    private final JTextField  rGreenStar  = UI.creaInput("Riconoscimento sostenibilità");
    private final JTextField  rUrl        = UI.creaInput("Url Michelin");
    private final JTextField  rWebsite    = UI.creaInput("Url sito web");
    private final JTextField  rLat        = UI.creaInput("Latitudine (es: 45.4642)");
    private final JTextField  rLon        = UI.creaInput("Longitudine (es: 9.1900)");
    private final JComboBox<String> rComboPrezzo = new JComboBox<>(
            new String[]{"Economico", "Medio", "Caro", "Molto costoso"});
    private final JSpinner    rPrezzoMedio = new JSpinner(new SpinnerNumberModel(15.0, 0.0, 5000.0, 1.0));
    private final JCheckBox   rDelivery    = UI.creaCheckbox("Offre servizio di delivery");
    private final JCheckBox   rPren        = UI.creaCheckbox("Accetta prenotazioni online");
    private final JTextArea   rDesc        = UI.creaTextArea("Descrizione del ristorante...");
    private final JTextArea   rFac         = UI.creaTextArea("Tipo di servizi e strutture disponibili...");
    private final JButton     btnSalvaRisto   = UI.creaBottone("Salva modifiche", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
    private final JLabel      lblStatoRisto   = UI.creaLabelInfo(" ");

    // ── Costruttori ───────────────────────────────────────────────────────────

    /**
     * Apre il dialog in modalità modifica PROFILO UTENTE.
     *
     * @param parent finestra padre a cui il dialog è modale
     * @param utente utente di cui modificare il profilo
     * @param connessione connessione al server da usare per l'invio delle modifiche
     */
    public ModificaDialog(Frame parent, Utente utente, ClientTK connessione) {
        super(parent, "Modifica profilo", true);
        this.utente      = utente;
        this.ristorante  = null;
        this.connessione = connessione;
        inizializza();
        cards.show(cardPanel, CARD_UTENTE);
        precompilaUtente();
    }

    /**
     * Apre il dialog in modalità modifica RISTORANTE.
     *
     * @param parent finestra padre a cui il dialog è modale
     * @param ristorante ristorante di cui modificare i dati
     * @param connessione connessione al server da usare per l'invio delle modifiche
     */
    public ModificaDialog(Frame parent, Ristorante ristorante, ClientTK connessione) {
        super(parent, "Modifica ristorante", true);
        this.utente      = null;
        this.ristorante  = ristorante;
        this.connessione = connessione;
        inizializza();
        cards.show(cardPanel, CARD_RISTORANTE);
        precompilaRistorante();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    /**
     * Inizializza dimensioni, layout e card del dialog, collega le azioni
     * e aggiorna i limiti dello spinner del prezzo medio.
     */
    private void inizializza() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 680);
        setMinimumSize(new Dimension(500, 600));
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(UI.SFONDO_SCURO);

        cardPanel.setBackground(UI.SFONDO_SCURO);
        cardPanel.add(creaCardUtente(),     CARD_UTENTE);
        cardPanel.add(creaCardRistorante(), CARD_RISTORANTE);
        setContentPane(cardPanel);

        collegaAzioni();

        // aggiorna limiti spinner quando cambia la fascia prezzo
        rComboPrezzo.addActionListener(e -> aggiorneLimitiSpinner());
        aggiorneLimitiSpinner();
    }

    /** Precompila i campi del form utente con i dati dell'utente passato al costruttore. */
    private void precompilaUtente() {
        if (utente == null) return;
        uNome.setText(utente.getNome() != null ? utente.getNome() : "");
        uCognome.setText(utente.getCognome() != null ? utente.getCognome() : "");
        uUsername.setText(utente.getUsername() != null ? utente.getUsername() : "");
        uDomicilio.setText(utente.getLuogoDomicilio() != null ? utente.getLuogoDomicilio() : "");
        if (utente.getDataNascita() != null) {
            uData.setText(new SimpleDateFormat("dd/MM/yyyy").format(utente.getDataNascita()));
        }
        uPass.setText(""); uPass2.setText("");
    }

    /**
     * Precompila i campi del form ristorante con i dati del ristorante passato
     * al costruttore, separando città e nazione dal campo location.
     */
    private void precompilaRistorante() {
        if (ristorante == null) return;
        rNome.setText(v(ristorante.getNome()));
        // location contiene "città, nazione"
        String loc = ristorante.getLocation() != null ? ristorante.getLocation() : "";
        String[] parts = loc.split(",", 2);
        rCitta.setText(parts.length > 0 ? parts[0].trim() : "");
        rNazione.setText(parts.length > 1 ? parts[1].trim() : "");
        rIndirizzo.setText(v(ristorante.getIndirizzo()));
        rCucina.setText(v(ristorante.getTipoCucina()));
        rTelefono.setText(v(ristorante.getTelefono()));
        rAward.setText(v(ristorante.getAward()));
        rUrl.setText(v(ristorante.getUrl()));
        rLat.setText(String.valueOf(ristorante.getLatitudine()));
        rLon.setText(String.valueOf(ristorante.getLongitudine()));
        rDesc.setText(v(ristorante.getDescrizione()));
        rPrezzoMedio.setValue(ristorante.getPrezzoMedioStimato());
        rDelivery.setSelected(ristorante.isDelivery());
        rPren.setSelected(ristorante.isPrenotazioneOnline());
        // fascia prezzo -> indice combo
        String fp = ristorante.getFasciaPrezzo() != null ? ristorante.getFasciaPrezzo() : "";
        int livello = Math.max(0, Math.min(3, fp.length() - 1));
        rComboPrezzo.setSelectedIndex(livello);
    }

    /** Collega i pulsanti di salvataggio alle rispettive logiche di invio. */
    private void collegaAzioni() {
        btnSalvaUtente.addActionListener(e -> tentaSalvaUtente());
        btnSalvaRisto.addActionListener(e  -> tentaSalvaRistorante());
    }

    /**
     * Aggiorna i limiti minimo/massimo dello spinner del prezzo medio in base
     * alla fascia di prezzo selezionata, riportando il valore corrente entro
     * i nuovi limiti se necessario.
     */
    private void aggiorneLimitiSpinner() {
        double min, max;
        switch (rComboPrezzo.getSelectedIndex()) {
            case 0  -> { min =  0; max =  19; }
            case 1  -> { min = 20; max =  49; }
            case 2  -> { min = 50; max =  80; }
            default -> { min = 80; max = 5000; }
        }
        SpinnerNumberModel m = (SpinnerNumberModel) rPrezzoMedio.getModel();
        m.setMinimum(min); m.setMaximum(max);
        double val = ((Number) m.getValue()).doubleValue();
        if (val < min || val > max) m.setValue(min);
    }

    // ── Card UTENTE ───────────────────────────────────────────────────────────

    /**
     * Costruisce la card di modifica del profilo utente.
     *
     * @return pannello della card Utente
     */
    private JPanel creaCardUtente() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UI.SFONDO_SCURO);

        JLabel titolo = new JLabel("Modifica profilo");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titolo.setForeground(UI.ROSSO_KNIFE);
        titolo.setBorder(new EmptyBorder(16, 20, 8, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UI.SFONDO_CARD);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = UI.gbc();
        JComponent[] campi = {uNome, uCognome, uUsername, uPass, uPass2, uDomicilio, uData};
        for (int i = 0; i < campi.length; i++) {
            gbc.gridy = i;
            gbc.insets = new Insets(i == 0 ? 0 : 6, 0, 0, 0);
            form.add(campi[i], gbc);
        }

        for (JTextField f : new JTextField[]{uNome, uCognome, uUsername, uDomicilio, uData})
            UI.applicaFocusHighlight(f);

        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setBackground(UI.SFONDO_SCURO);
        bottom.setBorder(new EmptyBorder(12, 20, 16, 20));
        bottom.add(lblStatoUtente, BorderLayout.CENTER);
        bottom.add(btnSalvaUtente, BorderLayout.EAST);

        root.add(titolo, BorderLayout.NORTH);
        root.add(new JScrollPane(form), BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        return root;
    }

    // ── Card RISTORANTE ───────────────────────────────────────────────────────

    /**
     * Costruisce la card di modifica dei dati del ristorante, con tutti i
     * campi organizzati su più righe tramite {@link GridBagLayout}.
     *
     * @return pannello della card Ristorante
     */
    private JPanel creaCardRistorante() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UI.SFONDO_SCURO);

        JLabel titolo = new JLabel("Modifica ristorante");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titolo.setForeground(UI.ROSSO_KNIFE);
        titolo.setBorder(new EmptyBorder(16, 20, 8, 20));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UI.SFONDO_CARD);
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = UI.gbc();
        int row = 0;

        // Riga: Nome | Cucina
        gbc.gridy = row++; gbc.gridwidth = 1; gbc.weightx = 0.5;
        form.add(rNome, gbc);
        gbc.gridx = 1;
        form.add(rCucina, gbc);
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;

        // Indirizzo
        gbc.gridy = row++; gbc.insets = new Insets(6,0,0,0);
        form.add(rIndirizzo, gbc);

        // Città | Nazione
        gbc.gridy = row++; gbc.gridwidth = 1; gbc.weightx = 0.5;
        form.add(rCitta, gbc);
        gbc.gridx = 1;
        form.add(rNazione, gbc);
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;

        // Telefono
        gbc.gridy = row++;
        form.add(rTelefono, gbc);

        // Fascia prezzo | Prezzo medio
        gbc.gridy = row++; gbc.gridwidth = 1; gbc.weightx = 0.5;
        UI.stilizzaCombo(rComboPrezzo);
        form.add(rComboPrezzo, gbc);
        gbc.gridx = 1;
        UI.stilizzaSpinner(rPrezzoMedio);
        form.add(rPrezzoMedio, gbc);
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;

        // Checkbox
        gbc.gridy = row++; gbc.gridwidth = 1; gbc.weightx = 0.5;
        form.add(rDelivery, gbc);
        gbc.gridx = 1;
        form.add(rPren, gbc);
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;

        // Lat | Lon
        gbc.gridy = row++; gbc.gridwidth = 1; gbc.weightx = 0.5;
        form.add(rLat, gbc);
        gbc.gridx = 1;
        form.add(rLon, gbc);
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;

        // Award | GreenStar
        gbc.gridy = row++; gbc.gridwidth = 1; gbc.weightx = 0.5;
        form.add(rAward, gbc);
        gbc.gridx = 1;
        form.add(rGreenStar, gbc);
        gbc.gridx = 0; gbc.gridwidth = 2; gbc.weightx = 1.0;

        // URL
        gbc.gridy = row++;
        form.add(rUrl, gbc);
        gbc.gridy = row++;
        form.add(rWebsite, gbc);

        // Descrizione
        gbc.gridy = row++; gbc.weighty = 0.4; gbc.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(rDesc), gbc);
        gbc.gridy = row++; gbc.weighty = 0.3;
        form.add(new JScrollPane(rFac), gbc);
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL;

        for (JTextField f : new JTextField[]{rNome,rCucina,rIndirizzo,rCitta,rNazione,rTelefono,rAward,rGreenStar,rUrl,rWebsite,rLat,rLon})
            UI.applicaFocusHighlight(f);

        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setBackground(UI.SFONDO_SCURO);
        bottom.setBorder(new EmptyBorder(12, 20, 16, 20));
        bottom.add(lblStatoRisto, BorderLayout.CENTER);
        bottom.add(btnSalvaRisto, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        root.add(titolo, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        return root;
    }

    // ── Logica salvataggio ────────────────────────────────────────────────────

    /**
     * Valida i campi del form utente (obbligatorietà, corrispondenza e
     * lunghezza password) e invia in background la richiesta di modifica
     * profilo, aggiornando la sessione con l'utente restituito dal server.
     */
    private void tentaSalvaUtente() {
        String nome     = uNome.getText().trim();
        String cognome  = uCognome.getText().trim();
        String username = uUsername.getText().trim();
        String pass     = new String(uPass.getPassword()).trim();
        String pass2    = new String(uPass2.getPassword()).trim();
        String dom      = uDomicilio.getText().trim();
        String data     = uData.getText().trim();

        if (nome.isBlank() || cognome.isBlank() || username.isBlank()) {
            UI.aggiornaStato(lblStatoUtente, "Nome, cognome e username sono obbligatori", true);
            return;
        }
        if (!pass.isBlank() && !pass.equals(pass2)) {
            UI.aggiornaStato(lblStatoUtente, "Le password non coincidono", true);
            return;
        }
        if (!pass.isBlank() && pass.length() < 6) {
            UI.aggiornaStato(lblStatoUtente, "La password deve avere almeno 6 caratteri", true);
            return;
        }

        btnSalvaUtente.setEnabled(false);
        Messaggio req = new Messaggio(Messaggio.OP_MODIFICA_UTENTE);
        req.addParam("nome",        nome);
        req.addParam("cognome",     cognome);
        req.addParam("username",    username);
        req.addParam("password",    pass);
        req.addParam("domicilio",   dom);
        req.addParam("dataNascita", data);

        new SwingWorker<Messaggio, Void>() {
            @Override protected Messaggio doInBackground() throws Exception {
                return connessione.invia(req);
            }
            @Override protected void done() {
                try {
                    Messaggio resp = get();
                    if (resp.isOk()) {
                        Utente aggiornato = new Gson().fromJson(resp.getDatiJson(), Utente.class);
                        SessioneUtente.getInstance().avvia(connessione, aggiornato);
                        UI.aggiornaStato(lblStatoUtente, "Profilo aggiornato con successo", false);
                        Timer t = new Timer(1200, ev -> dispose());
                        t.setRepeats(false); t.start();
                    } else {
                        UI.aggiornaStato(lblStatoUtente, resp.getParam("errore"), true);
                    }
                } catch (Exception ex) {
                    UI.aggiornaStato(lblStatoUtente, "Errore: " + ex.getMessage(), true);
                } finally {
                    btnSalvaUtente.setEnabled(true);
                }
            }
        }.execute();
    }

    /**
     * Valida i campi obbligatori del form ristorante, ricava la fascia di
     * prezzo dal simbolo di valuta della località e invia in background la
     * richiesta di modifica del ristorante al server.
     */
    private void tentaSalvaRistorante() {
        if (ristorante == null) return;

        String nome = rNome.getText().trim();
        String citta = rCitta.getText().trim();
        String cucina = rCucina.getText().trim();

        if (nome.isBlank() || citta.isBlank() || cucina.isBlank()) {
            UI.aggiornaStato(lblStatoRisto, "Nome, città e tipo cucina sono obbligatori", true);
            return;
        }

        String luogo = citta + (rNazione.getText().isBlank() ? "" : ", " + rNazione.getText().trim());
        String simbolo = AggiungiRistoranteDialog.simboloValuta(luogo);
        int livello = rComboPrezzo.getSelectedIndex() + 1;
        String fasciaPrezzo = simbolo.repeat(livello);

        double prezzoMedioVal = ((Number) rPrezzoMedio.getValue()).doubleValue();
        double lat = 0, lon = 0;
        try { lat = Double.parseDouble(rLat.getText().trim()); } catch (NumberFormatException ignored) {}
        try { lon = Double.parseDouble(rLon.getText().trim()); } catch (NumberFormatException ignored) {}

        btnSalvaRisto.setEnabled(false);
        Messaggio req = new Messaggio(Messaggio.OP_MODIFICA_RISTORANTE);
        req.addParam("id",           String.valueOf(ristorante.getId()));
        req.addParam("nome",         nome);
        req.addParam("indirizzo",    rIndirizzo.getText().trim());
        req.addParam("citta",        citta);
        req.addParam("nazione",      rNazione.getText().trim());
        req.addParam("cucina",       cucina);
        req.addParam("telefono",     rTelefono.getText().trim());
        req.addParam("fasciaPrezzo", fasciaPrezzo);
        req.addParam("prezzoMedio",  String.valueOf(prezzoMedioVal));
        req.addParam("delivery",     String.valueOf(rDelivery.isSelected()));
        req.addParam("prenotazione", String.valueOf(rPren.isSelected()));
        req.addParam("descrizione",  rDesc.getText().trim());
        req.addParam("facilities",   rFac.getText().trim());
        req.addParam("award",        rAward.getText().trim());
        req.addParam("greenStar",    rGreenStar.getText().trim());
        req.addParam("url",          rUrl.getText().trim());
        req.addParam("website",      rWebsite.getText().trim());
        req.addParam("lat",          String.valueOf(lat));
        req.addParam("lon",          String.valueOf(lon));

        new SwingWorker<Messaggio, Void>() {
            @Override protected Messaggio doInBackground() throws Exception {
                return connessione.invia(req);
            }
            @Override protected void done() {
                try {
                    Messaggio resp = get();
                    if (resp.isOk()) {
                        UI.aggiornaStato(lblStatoRisto, "Ristorante aggiornato con successo", false);
                        Timer t = new Timer(1200, ev -> dispose());
                        t.setRepeats(false); t.start();
                    } else {
                        UI.aggiornaStato(lblStatoRisto, resp.getParam("errore"), true);
                    }
                } catch (Exception ex) {
                    UI.aggiornaStato(lblStatoRisto, "Errore: " + ex.getMessage(), true);
                } finally {
                    btnSalvaRisto.setEnabled(true);
                }
            }
        }.execute();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    /**
     * Restituisce la stringa passata, o una stringa vuota se {@code null}.
     *
     * @param s stringa da normalizzare
     * @return {@code s}, o "" se {@code s} è {@code null}
     */
    private static String v(String s) { return s != null ? s : ""; }
}
