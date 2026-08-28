package theknife.gui;

import com.google.gson.Gson;
import theknife.common.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Dialog di dettaglio di un ristorante: mostra informazioni principali,
 * legenda della fascia di prezzo, azioni disponibili in base al ruolo
 * dell'utente (preferiti, recensione) e l'elenco delle recensioni con
 * la possibilità per il gestore di rispondere.
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class DettaglioDialog extends JDialog {

    private final SessioneUtente sessione = SessioneUtente.getInstance();
    private final Ristorante     rist;
    private final JPanel         panelRec  = new JPanel();
    private final JPanel         panelInfo = new JPanel();
    private final JLabel         lblStato  = UI.creaLabelInfo(" ");

    /**
     * Costruisce il dialog di dettaglio per il ristorante indicato e avvia
     * il caricamento asincrono delle recensioni.
     *
     * @param parent finestra padre a cui il dialog è modale
     * @param ristorante ristorante di cui mostrare il dettaglio
     */
    public DettaglioDialog(Frame parent, Ristorante ristorante) {
        super(parent, ristorante.getNome(), true);
        this.rist = ristorante;
        setSize(700, 680);
        setMinimumSize(new Dimension(580, 500));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(UI.SFONDO_SCURO);
        costruisci();
        caricaDati();
    }

    /**
     * Costruisce la struttura statica del dialog: intestazione con nome e
     * location, area centrale scrollabile (info + recensioni) e barra inferiore.
     */
    private void costruisci() {
        setLayout(new BorderLayout());
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(0x111113));
        top.setBorder(new EmptyBorder(20, 24, 16, 24));
        JLabel lblN = new JLabel(rist.getNome());
        lblN.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblN.setForeground(UI.TESTO_CHIARO);
        JLabel lblL = new JLabel(rist.getLocation() + " — " + rist.getTipoCucina());
        lblL.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblL.setForeground(UI.TESTO_GRIGIO);
        JPanel infoTop = new JPanel();
        infoTop.setOpaque(false);
        infoTop.setLayout(new BoxLayout(infoTop, BoxLayout.Y_AXIS));
        infoTop.add(lblN); infoTop.add(Box.createVerticalStrut(4)); infoTop.add(lblL);
        top.add(infoTop, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(UI.SFONDO_SCURO);
        panelInfo.setBorder(new EmptyBorder(8, 24, 4, 24));

        panelRec.setLayout(new BoxLayout(panelRec, BoxLayout.Y_AXIS));
        panelRec.setBackground(UI.SFONDO_SCURO);
        panelRec.setBorder(new EmptyBorder(8, 24, 8, 24));
        JLabel caric = new JLabel("Caricamento...");
        caric.setForeground(UI.TESTO_GRIGIO);
        panelRec.add(caric);

        JPanel contenuto = new JPanel();
        contenuto.setLayout(new BoxLayout(contenuto, BoxLayout.Y_AXIS));
        contenuto.setBackground(UI.SFONDO_SCURO);
        contenuto.add(panelInfo); contenuto.add(panelRec);

        JScrollPane scroll = new JScrollPane(contenuto);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UI.SFONDO_SCURO);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(new Color(0x111113));
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UI.BORDO_INPUT));
        bottom.add(lblStato);
        JButton btnC = UI.creaBottone("Chiudi", new Color(0x3A3A3C), new Color(0x4A4A4C));
        btnC.setPreferredSize(new Dimension(90, 34));
        btnC.addActionListener(e -> dispose());
        bottom.add(btnC);
        add(bottom, BorderLayout.SOUTH);
    }

    /**
     * Carica in background le recensioni del ristorante dal server e,
     * al termine, popola i pannelli di informazioni e recensioni.
     */
    private void caricaDati() {
        new SwingWorker<List<Recensione>, Void>() {
            @Override protected List<Recensione> doInBackground() throws Exception {
                Messaggio req = new Messaggio(Messaggio.OP_RECENSIONI_RISTO);
                req.addParam("ristoranteId", String.valueOf(rist.getId()));
                Messaggio resp = sessione.getConnessione().invia(req);
                if (!resp.isOk()) throw new Exception(resp.getParam("errore"));
                if (resp.getDatiJson() == null || resp.getDatiJson().isBlank()) return List.of();
                return Arrays.asList(new Gson().fromJson(resp.getDatiJson(), Recensione[].class));
            }
            @Override protected void done() {
                try { popola(get()); }
                catch (Exception ex) {
                    panelRec.removeAll();
                    JLabel e = new JLabel("Errore: " + ex.getMessage());
                    e.setForeground(new Color(0xFF6B6B));
                    panelRec.add(e); panelRec.revalidate();
                }
            }
        }.execute();
    }

    /**
     * Popola i pannelli di informazioni e recensioni con i dati ricevuti.
     *
     * @param recensioni lista delle recensioni del ristorante da mostrare
     */
    private void popola(List<Recensione> recensioni) {
        panelInfo.removeAll();
        panelInfo.add(creaSezioneInfo());
        panelInfo.add(Box.createVerticalStrut(10));
        JPanel az = creaSezioneAzioni();
        if (az != null) panelInfo.add(az);
        panelInfo.revalidate(); panelInfo.repaint();

        panelRec.removeAll();
        JLabel lblR = new JLabel("Recensioni (" + recensioni.size() + ")");
        lblR.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblR.setForeground(UI.TESTO_CHIARO);
        lblR.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelRec.add(lblR); panelRec.add(Box.createVerticalStrut(10));
        if (recensioni.isEmpty()) {
            JLabel v = new JLabel("Nessuna recensione. Sii il primo!");
            v.setForeground(UI.TESTO_GRIGIO); v.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            v.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelRec.add(v);
        } else {
            for (Recensione r : recensioni) { panelRec.add(creaCardRec(r)); panelRec.add(Box.createVerticalStrut(10)); }
        }
        panelRec.revalidate(); panelRec.repaint();
    }

    /**
     * Costruisce la card con le informazioni principali del ristorante:
     * media voto, fascia di prezzo (con legenda) e dati di contatto.
     *
     * @return pannello con le informazioni del ristorante
     */
    private JPanel creaSezioneInfo() {
        JPanel card = UI.creaCardPanel();
        card.setLayout(new GridBagLayout());
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 4, 3, 12);
        gbc.weightx = 1.0;

        // =========================
        // VALUTAZIONE MEDIA
        // =========================

        JLabel lblVoto = new JLabel(
                String.format("%.1f / 5", rist.getMediaVoto())
        );

        lblVoto.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblVoto.setForeground(UI.GIALLO_VOTO);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        card.add(lblVoto, gbc);


        // =========================
        // FASCIA PREZZO
        // =========================

        String fasciaOriginale = rist.getFasciaPrezzo();

        String simboloValuta = "€";
        int livelloPrezzo = 1;

        if (fasciaOriginale != null && !fasciaOriginale.isBlank()) {

            fasciaOriginale = fasciaOriginale.trim();

            /*
             * La fascia può essere, ad esempio:
             *
             * €
             * €€
             * €€€
             * €€€€
             *
             * oppure:
             *
             * $
             * $$
             * ¥
             * ¥¥
             *
             * Prendiamo il primo simbolo presente
             * e contiamo quante volte compare.
             */

            StringBuilder simboli = new StringBuilder();

            for (int i = 0; i < fasciaOriginale.length(); i++) {
                char c = fasciaOriginale.charAt(i);

                if (!Character.isLetterOrDigit(c)
                        && !Character.isWhitespace(c)) {
                    simboli.append(c);
                }
            }

            if (simboli.length() > 0) {

                simboloValuta = String.valueOf(simboli.charAt(0));

                livelloPrezzo = simboli.length();

                if (livelloPrezzo < 1) {
                    livelloPrezzo = 1;
                }

                if (livelloPrezzo > 4) {
                    livelloPrezzo = 4;
                }
            }
        }


        // Fascia visualizzata
        String fascia = simboloValuta.repeat(livelloPrezzo);


        // =========================
        // DATI PRINCIPALI
        // =========================

        String[][] dati = {
            {"Indirizzo", rist.getIndirizzo()},
            {"Telefono", rist.getTelefono()},
            {"Fascia prezzi", fascia},
            {"Delivery", rist.isDelivery()
                    ? "Disponibile"
                    : "Non disponibile"},
            {"Prenotazione", rist.isPrenotazioneOnline()
                    ? "Online"
                    : "Non disponibile"},
            {"Award", rist.getAward()}
        };


        int riga = 1;

        for (String[] c : dati) {

            if (c[1] == null || c[1].isBlank()) {
                continue;
            }

            gbc.gridwidth = 1;
            gbc.gridx = 0;
            gbc.gridy = riga;
            gbc.weightx = 0;

            JLabel k = new JLabel(c[0] + ":");
            k.setForeground(UI.TESTO_GRIGIO);
            k.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            card.add(k, gbc);


            gbc.gridx = 1;
            gbc.weightx = 1.0;

            JLabel v = new JLabel(c[1]);
            v.setForeground(UI.TESTO_CHIARO);
            v.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            card.add(v, gbc);

            riga++;
        }


        // =========================
        // LEGENDA FASCIA PREZZI
        // =========================

        riga++;

        gbc.gridx = 0;
        gbc.gridy = riga;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 4, 3, 4);

        JLabel titoloLegenda = new JLabel("Legenda fascia prezzi");
        titoloLegenda.setForeground(UI.TESTO_CHIARO);
        titoloLegenda.setFont(new Font("Segoe UI", Font.BOLD, 12));

        card.add(titoloLegenda, gbc);


        String[] legenda = {
            simboloValuta + " = 0 - 19€",
            simboloValuta + simboloValuta + " = 20 - 49 €",
            simboloValuta + simboloValuta + simboloValuta + " = 50 - 80€",
            simboloValuta + simboloValuta + simboloValuta + simboloValuta + " = oltre 80€"
        };


        for (String voce : legenda) {

            riga++;

            gbc.gridx = 0;
            gbc.gridy = riga;
            gbc.gridwidth = 2;
            gbc.insets = new Insets(1, 4, 1, 4);

            JLabel lbl = new JLabel(voce);

            lbl.setForeground(UI.TESTO_GRIGIO);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            card.add(lbl, gbc);
        }


        return card;
    }

    /**
     * Costruisce il pannello delle azioni disponibili in base al ruolo
     * dell'utente: messaggio di invito al login per gli ospiti, pulsanti
     * "Preferiti"/"Recensione" per i clienti, nota informativa per il
     * gestore proprietario del ristorante.
     *
     * @return pannello delle azioni contestuali
     */
    private JPanel creaSezioneAzioni() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (!sessione.isLoggato()) {
            JLabel l = new JLabel("Accedi per aggiungere ai preferiti e scrivere recensioni");
            l.setFont(new Font("Segoe UI", Font.ITALIC, 12)); l.setForeground(UI.TESTO_GRIGIO);
            panel.add(l); return panel;
        }
        if (sessione.isCliente()) {
            JButton btnPref = UI.creaBottone("Preferiti", new Color(0x8E1C1C), new Color(0x6E1212));
            btnPref.setPreferredSize(new Dimension(160, 34));
            btnPref.addActionListener(e -> aggiungiPreferito(btnPref));
            JButton btnRec = UI.creaBottone("Recensione", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
            btnRec.setPreferredSize(new Dimension(160, 34));
            btnRec.addActionListener(e -> apriFormRec(null));
            panel.add(btnPref); panel.add(btnRec);
        }
        if (sessione.isGestore() && rist.getGestoreId() == sessione.getUtente().getId()) {
            JLabel inf = new JLabel("Questo è un tuo ristorante — puoi rispondere alle recensioni qui sotto");
            inf.setForeground(new Color(0x2E86AB)); inf.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            panel.add(inf);
        }
        return panel;
    }

    /**
     * Costruisce la card visuale di una singola recensione, con eventuali
     * pulsanti di modifica/eliminazione (per l'autore) o risposta (per il gestore).
     *
     * @param rec recensione da rappresentare
     * @return pannello della card della recensione
     */
    private JPanel creaCardRec(Recensione rec) {
        JPanel card = UI.creaCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JLabel lblTop = new JLabel(rec.getStelle() + " / 5  " + rec.getUsernameAutore()
                + "  —  " + (rec.getDataRecensione() != null ? rec.getDataRecensione().substring(0,10) : ""));
        lblTop.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTop.setForeground(UI.GIALLO_VOTO);

        JTextArea area = new JTextArea(rec.getTesto() != null ? rec.getTesto() : "");
        area.setEditable(false); area.setLineWrap(true); area.setWrapStyleWord(true);
        area.setOpaque(false); area.setForeground(UI.TESTO_CHIARO);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13)); area.setBorder(null);

        card.add(lblTop); card.add(Box.createVerticalStrut(6)); card.add(area);

        if (rec.haRisposta()) {
            JLabel lblRisp = new JLabel("Risposta: " + rec.getRispostaGestore());
            lblRisp.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            lblRisp.setForeground(new Color(0x2E86AB));
            card.add(Box.createVerticalStrut(6)); card.add(lblRisp);
        }

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btns.setOpaque(false);
        boolean miaRec = sessione.isLoggato() && rec.getUtenteId() == sessione.getUtente().getId();
        if (miaRec) {
            JButton bMod = UI.creaBottoneSmall("Modifica", new Color(0x3A3A3C));
            JButton bDel = UI.creaBottoneSmall("Elimina",  new Color(0x8E1C1C));
            bMod.addActionListener(e -> apriFormRec(rec));
            bDel.addActionListener(e -> eliminaRec(rec));
            btns.add(bMod); btns.add(bDel);
        }
        if (sessione.isGestore() && rist.getGestoreId() == sessione.getUtente().getId()) {
            JButton bRisp = UI.creaBottoneSmall(rec.haRisposta() ? "Modifica risposta" : "Rispondi", new Color(0x2E86AB));
            bRisp.addActionListener(e -> apriFormRisposta(rec));
            btns.add(bRisp);
        }
        if (btns.getComponentCount() > 0) { card.add(Box.createVerticalStrut(6)); card.add(btns); }
        return card;
    }

    /**
     * Invia la richiesta di aggiunta del ristorante corrente ai preferiti
     * dell'utente loggato.
     *
     * @param btn pulsante che ha scatenato l'azione, disabilitato durante l'invio
     */
    private void aggiungiPreferito(JButton btn) {
        btn.setEnabled(false);
        new SwingWorker<Boolean,Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                Messaggio req = new Messaggio(Messaggio.OP_AGGIUNGI_PREF);
                req.addParam("ristoranteId", String.valueOf(rist.getId()));
                return sessione.getConnessione().invia(req).isOk();
            }
            @Override protected void done() {
                try {
                    if (get()) UI.aggiornaStato(lblStato, "Aggiunto ai preferiti!", false);
                    else { UI.aggiornaStato(lblStato, "Già nei preferiti", true); btn.setEnabled(true); }
                } catch (Exception ex) { UI.aggiornaStato(lblStato, "Errore", true); btn.setEnabled(true); }
            }
        }.execute();
    }

    /**
     * Apre un dialog per scrivere una nuova recensione, oppure per
     * modificarne una esistente se {@code rec} non è {@code null}.
     *
     * @param rec recensione da modificare, o {@code null} per scriverne una nuova
     */
    private void apriFormRec(Recensione rec) {
        boolean isModifica = rec != null;
        JDialog form = new JDialog(this, isModifica ? "Modifica recensione" : "Scrivi recensione", true);
        form.setSize(400, 280); form.setLocationRelativeTo(this);
        form.getContentPane().setBackground(UI.SFONDO_SCURO);
        form.setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UI.SFONDO_SCURO);
        body.setBorder(new EmptyBorder(20, 24, 16, 24));

        JSpinner spStelle = new JSpinner(new SpinnerNumberModel(isModifica ? rec.getStelle() : 4, 1, 5, 1));
        UI.stilizzaSpinner(spStelle);
        JTextArea area = UI.creaTextArea("Racconta la tua esperienza...");
        if (isModifica && rec.getTesto() != null) area.setText(rec.getTesto());
        JLabel lblE = UI.creaLabelInfo(" ");

        body.add(UI.creaRigaFiltro("Stelle (1–5):", spStelle));
        body.add(Box.createVerticalStrut(8));
        body.add(new JScrollPane(area));
        body.add(Box.createVerticalStrut(6));
        body.add(lblE);
        form.add(body, BorderLayout.CENTER);

        JButton btnSalva = UI.creaBottone(isModifica ? "Aggiorna" : "Pubblica", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
        btnSalva.setPreferredSize(new Dimension(130, 34));
        btnSalva.addActionListener(e -> {
            String testo = area.getText().trim();
            if (testo.isEmpty()) { UI.aggiornaErroreForm(lblE, "Scrivi qualcosa!"); return; }
            btnSalva.setEnabled(false);
            new SwingWorker<Boolean,Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    Messaggio req;
                    if (isModifica) {
                        req = new Messaggio(Messaggio.OP_MODIFICA_REC);
                        req.addParam("recensioneId", String.valueOf(rec.getId()));
                    } else {
                        req = new Messaggio(Messaggio.OP_AGGIUNGI_REC);
                        req.addParam("ristoranteId", String.valueOf(rist.getId()));
                    }
                    req.addParam("stelle", String.valueOf(spStelle.getValue()));
                    req.addParam("testo",  testo);
                    Messaggio resp = sessione.getConnessione().invia(req);
                    if (!resp.isOk()) throw new Exception(resp.getParam("errore"));
                    return true;
                }
                @Override protected void done() {
                    try { get(); form.dispose(); caricaDati(); }
                    catch (Exception ex) { UI.aggiornaErroreForm(lblE, ex.getMessage()); btnSalva.setEnabled(true); }
                }
            }.execute();
        });
        JPanel ftrForm = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        ftrForm.setBackground(new Color(0x111113));
        JButton btnAnn = UI.creaBottoneSmall("Annulla", new Color(0x3A3A3C));
        btnAnn.addActionListener(e -> form.dispose());
        ftrForm.add(btnAnn); ftrForm.add(btnSalva);
        form.add(ftrForm, BorderLayout.SOUTH);
        form.setVisible(true);
    }

    /**
     * Chiede conferma ed elimina una recensione dell'utente loggato.
     *
     * @param rec recensione da eliminare
     */
    private void eliminaRec(Recensione rec) {
        if (JOptionPane.showConfirmDialog(this, "Eliminare la tua recensione?", "Conferma",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        new SwingWorker<Boolean,Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                Messaggio req = new Messaggio(Messaggio.OP_ELIMINA_REC);
                req.addParam("recensioneId", String.valueOf(rec.getId()));
                return sessione.getConnessione().invia(req).isOk();
            }
            @Override protected void done() {
                try {
                    if (get()) { UI.aggiornaStato(lblStato, "Recensione eliminata", false); caricaDati(); }
                    else UI.aggiornaStato(lblStato, "Impossibile eliminare", true);
                } catch (Exception ex) { UI.aggiornaStato(lblStato, "Errore", true); }
            }
        }.execute();
    }

    /**
     * Apre un dialog per inserire o modificare la risposta del gestore
     * a una recensione.
     *
     * @param rec recensione a cui rispondere o di cui modificare la risposta
     */
    private void apriFormRisposta(Recensione rec) {
        JDialog form = new JDialog(this, rec.haRisposta() ? "Modifica risposta" : "Risposta alla recensione", true);
        form.setSize(420, 270); form.setLocationRelativeTo(this);
        form.getContentPane().setBackground(UI.SFONDO_SCURO);
        form.setLayout(new BorderLayout());

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UI.SFONDO_SCURO);
        body.setBorder(new EmptyBorder(20, 24, 16, 24));

        JLabel lblO = new JLabel("<html><i>\"" + (rec.getTesto() != null ? rec.getTesto() : "") + "\"</i></html>");
        lblO.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblO.setForeground(UI.TESTO_GRIGIO);
        lblO.setBorder(new EmptyBorder(0, 0, 10, 0));
        JTextArea areaR = UI.creaTextArea("Scrivi la tua risposta...");
        if (rec.haRisposta()) areaR.setText(rec.getRispostaGestore());
        JLabel lblE = UI.creaLabelInfo(" ");
        body.add(lblO); body.add(new JScrollPane(areaR)); body.add(Box.createVerticalStrut(8)); body.add(lblE);
        form.add(body, BorderLayout.CENTER);

        JButton btnInvia = UI.creaBottone(rec.haRisposta() ? "Salva modifica" : "Pubblica risposta", new Color(0x2E86AB), new Color(0x1A5F7A));
        btnInvia.setPreferredSize(new Dimension(170, 34));
        btnInvia.addActionListener(e -> {
            String risp = areaR.getText().trim();
            if (risp.isEmpty()) { UI.aggiornaErroreForm(lblE, "Scrivi una risposta prima di pubblicare"); return; }
            btnInvia.setEnabled(false);
            new SwingWorker<Boolean,Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                    Messaggio req = new Messaggio(Messaggio.OP_RISPONDI_REC);
                    req.addParam("recensioneId", String.valueOf(rec.getId()));
                    req.addParam("risposta", risp);
                    Messaggio resp = sessione.getConnessione().invia(req);
                    if (!resp.isOk()) throw new Exception(resp.getParam("errore"));
                    return true;
                }
                @Override protected void done() {
                    try { get(); form.dispose(); caricaDati(); }
                    catch (Exception ex) { UI.aggiornaErroreForm(lblE, ex.getMessage()); btnInvia.setEnabled(true); }
                }
            }.execute();
        });
        JPanel ftr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        ftr.setBackground(new Color(0x111113));
        JButton btnAnn = UI.creaBottoneSmall("Annulla", new Color(0x3A3A3C));
        btnAnn.addActionListener(e -> form.dispose());
        ftr.add(btnAnn); ftr.add(btnInvia);
        form.add(ftr, BorderLayout.SOUTH);
        form.setVisible(true);
    	}
	}
