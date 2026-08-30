package theknife.gui;

import theknife.client.ClientTK;
import theknife.common.Messaggio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** 
 * Dialog per l'aggiunta di un nuovo ristorante. Accessibile solo ai gestori. 
 * Combina il layout moderno e i fix della versione standalone con il geocoding automatico.
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class AggiungiRistoranteDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JTextField  fNome      = UI.creaInput("Nome del ristorante *"),  fIndirizzo = UI.creaInput("Indirizzo (via e numero) *");
    private final JTextField  fCitta     = UI.creaInput("Citta *"),                fNazione   = UI.creaInput("Nazione (es: Italia)");
    private final JTextField  fCucina    = UI.creaInput("Tipo di cucina *"),        fTelefono  = UI.creaInput("Telefono");
    private final JTextField  fAward     = UI.creaInput("Award / Riconoscimento"), fGreenStar = UI.creaInput("Riconoscimento sostenibilita");
    private final JTextField  fLat       = UI.creaInput("Latitudine (es: 45.4642)"), fLon     = UI.creaInput("Longitudine (es: 9.1900)");
    private final JTextField  fUrl       = UI.creaInput("Url Michelin"),           fWebsite   = UI.creaInput("Url sito web ristorante");
    private final JComboBox<String> comboPrezzo = new JComboBox<>(new String[]{"Economico", "Medio", "Caro", "Molto costoso"});
    private final JSpinner prezzoMedio = new JSpinner(new SpinnerNumberModel(15.0, 0.0, 5000.0, 1.0));
    private final JCheckBox   cbDelivery = UI.creaCheckbox("Offre servizio di delivery");
    private final JCheckBox   cbPren     = UI.creaCheckbox("Accetta prenotazioni online");
    private final JTextArea   aDesc      = UI.creaTextArea("Descrizione del ristorante...");
    private final JTextArea   aFac       = UI.creaTextArea("Tipo di servizi e strutture disponibili...");
    private final JButton     btnSalva   = UI.creaBottone("Aggiungi ristorante", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
    private final JButton     btnAnn     = UI.creaBottone("Annulla", new Color(0x3A3A3C), new Color(0x4A4A4C));
    private final JLabel      lblStato   = UI.creaLabelInfo(" ");
    private final SessioneUtente sessione = SessioneUtente.getInstance();
    
    private boolean ristoranteSalvato = false;
    /** @return {@code true} se il ristorante è stato salvato con successo tramite questo dialog */
    public boolean isRistoranteSalvato() { return ristoranteSalvato; }

    /**
     * Costruisce il dialog di inserimento di un nuovo ristorante.
     *
     * @param parent finestra padre a cui il dialog è modale
     */
    public AggiungiRistoranteDialog(Frame parent) {
        super(parent, "Aggiungi ristorante", true);
        setSize(520, 680);
        setMinimumSize(new Dimension(460, 560));
        setLocationRelativeTo(parent);
        getContentPane().setBackground(UI.SFONDO_SCURO);
        // Ordine di focus visivo corretto
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        costruisciUI();
        collegaAzioni();
    }

    /** Costruisce la struttura generale del dialog: header, corpo scrollabile e barra dei pulsanti. */
    private void costruisciUI() {
        setLayout(new BorderLayout());
        add(creaHeader(), BorderLayout.NORTH);
        add(creaCorpo(),  BorderLayout.CENTER);
        add(creaBar(),    BorderLayout.SOUTH);
    }

    /**
     * Costruisce il pannello di intestazione con titolo e sottotitolo esplicativo.
     *
     * @return pannello dell'header
     */
    private JPanel creaHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(0x111113));
        h.setBorder(new EmptyBorder(18, 24, 14, 24));
        JLabel t = new JLabel("Nuovo ristorante");
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));
        t.setForeground(UI.TESTO_CHIARO);
        JLabel s = new JLabel("Compila i dati — Se lasci vuoti lat/lon verranno calcolati automaticamente");
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(UI.TESTO_GRIGIO);
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(t);
        p.add(Box.createVerticalStrut(3));
        p.add(s);
        h.add(p, BorderLayout.CENTER);
        return h;
    }

    /**
     * Costruisce il corpo scrollabile del dialog con tutte le sezioni del form
     * (dati principali, caratteristiche, servizi, descrizione, facilities).
     *
     * @return scroll pane contenente il form completo
     */
    private JScrollPane creaCorpo() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UI.SFONDO_SCURO);
        body.setBorder(new EmptyBorder(18, 24, 10, 24));

        body.add(sezione("DATI PRINCIPALI", r(fNome), r2(fCitta, fNazione), r(fIndirizzo), r2(fLat, fLon)));
        body.add(Box.createVerticalStrut(14));
        body.add(sezione("CARATTERISTICHE", r(fCucina), rl("Fascia prezzo", comboPrezzo),
                rl("Prezzo medio", prezzoMedio), r(fTelefono), r(fAward), r(fGreenStar), r(fUrl), r(fWebsite)));
        body.add(Box.createVerticalStrut(14));

        JPanel cServizi = UI.creaCardPanel();
        cServizi.setLayout(new BoxLayout(cServizi, BoxLayout.Y_AXIS));
        cServizi.add(UI.creaLabelSezione("SERVIZI"));
        cServizi.add(Box.createVerticalStrut(10));
        cServizi.add(cbDelivery);
        cServizi.add(Box.createVerticalStrut(6));
        cServizi.add(cbPren);
        body.add(cServizi);
        body.add(Box.createVerticalStrut(14));

        body.add(cardTextArea("DESCRIZIONE",           aDesc));
        body.add(Box.createVerticalStrut(14));
        body.add(cardTextArea("FACILITIES & SERVICES", aFac));
        body.add(Box.createVerticalStrut(10));

        lblStato.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(lblStato);

        JScrollPane sc = new JScrollPane(body);
        sc.setBorder(null);
        sc.getViewport().setBackground(UI.SFONDO_SCURO);
        sc.getVerticalScrollBar().setUnitIncrement(14);
        return sc;
    }

    /**
     * Costruisce la barra inferiore con i pulsanti Annulla e Aggiungi ristorante.
     *
     * @return pannello della barra dei pulsanti
     */
    private JPanel creaBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bar.setBackground(new Color(0x111113));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UI.BORDO_INPUT));
        btnAnn.setPreferredSize(new Dimension(100, 36));
        btnSalva.setPreferredSize(new Dimension(190, 36));
        bar.add(btnAnn);
        bar.add(btnSalva);
        return bar;
    }

    /**
     * Collega i listener ai componenti del form: pulsanti, focus highlight,
     * aggiornamento dinamico dei limiti dello spinner del prezzo medio e tooltip.
     */
    private void collegaAzioni() {
        btnAnn.addActionListener(e -> dispose());
        btnSalva.addActionListener(e -> tentaInvio());

        for (JTextField f : new JTextField[]{fNome, fIndirizzo, fCitta, fNazione,
                fCucina, fTelefono, fAward, fGreenStar, fUrl, fWebsite, fLat, fLon})
            UI.applicaFocusHighlight(f);

        UI.stilizzaCombo(comboPrezzo);
        UI.stilizzaSpinner(prezzoMedio);

        // Aggiorna i limiti dello spinner prezzoMedio in base alla fascia selezionata
        comboPrezzo.addActionListener(e -> {
            int idx = comboPrezzo.getSelectedIndex();
            double min, max, val;
            switch (idx) {
                case 0  -> { min =  0; max =  19; val = 15; }
                case 1  -> { min = 20; max =  49; val = 30; }
                case 2  -> { min = 50; max =  80; val = 55; }
                default -> { min = 80; max = 5000; val = 90; }
            }
            SpinnerNumberModel model = (SpinnerNumberModel) prezzoMedio.getModel();
            model.setMinimum(min);
            model.setMaximum(max);
            if (((Number) model.getValue()).doubleValue() < min
                    || ((Number) model.getValue()).doubleValue() > max) {
                model.setValue(val);
            }
        });
        // Imposta i limiti corretti per il valore iniziale (Economico)
        ((SpinnerNumberModel) prezzoMedio.getModel()).setMinimum(0.0);
        ((SpinnerNumberModel) prezzoMedio.getModel()).setMaximum(19.0);

        UI.addTooltip(fLat, "Formato decimale, es: 45.4642");
        UI.addTooltip(fLon, "Formato decimale, es: 9.1900");
        UI.addTooltip(fAward, "Es: Stella Michelin, Bib Gourmand...");
        UI.addTooltip(fGreenStar, "Riconoscimento Michelin per la sostenibilità");
    }

    /**
     * Valida i campi obbligatori e le coordinate inserite manualmente, quindi
     * invia la richiesta di creazione del ristorante al server in background.
     * Se latitudine e longitudine non sono fornite, vengono calcolate
     * automaticamente tramite geocoding a partire da città e nazione.
     */
    private void tentaInvio() {
        if (fNome.getText().trim().isEmpty() || fCitta.getText().trim().isEmpty()
                || fIndirizzo.getText().trim().isEmpty() || fCucina.getText().trim().isEmpty()) {
            UI.aggiornaStato(lblStato, "Compila tutti i campi obbligatori (*)", true);
            return;
        }
        
        String latT = fLat.getText().trim(), lonT = fLon.getText().trim();
        if (!latT.isEmpty() || !lonT.isEmpty()) {
            try {
                if (!latT.isEmpty()) Double.parseDouble(latT);
                if (!lonT.isEmpty()) Double.parseDouble(lonT);
            } catch (NumberFormatException ex) {
                UI.aggiornaStato(lblStato, "Latitudine e longitudine devono essere numeri decimali", true);
                return;
            }
        }
        
        btnSalva.setEnabled(false);
        UI.aggiornaStato(lblStato, "Salvataggio in corso...", false);

        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                Messaggio req = new Messaggio(Messaggio.OP_AGGIUNGI_RISTO);
                req.addParam("nome",         fNome.getText().trim());
                req.addParam("indirizzo",    fIndirizzo.getText().trim());
                req.addParam("citta",        fCitta.getText().trim());
                req.addParam("nazione",      fNazione.getText().trim());
                req.addParam("cucina",       fCucina.getText().trim());
                
                int livello = comboPrezzo.getSelectedIndex() + 1;
                double pMedio = ((Number) prezzoMedio.getValue()).doubleValue();
                if (!prezzoCoerente(livello, pMedio)) {
                    throw new IllegalArgumentException("Prezzo medio non coerente con la fascia selezionata");
                }
                
                String luogoPerValuta = fCitta.getText().trim()
                        + (fNazione.getText().isBlank() ? "" : ", " + fNazione.getText().trim());
                String simbolo = simboloValuta(luogoPerValuta);
                req.addParam("fasciaPrezzo", simbolo.repeat(livello));
                req.addParam("prezzoMedio", String.valueOf(pMedio));
                req.addParam("telefono",     fTelefono.getText().trim());
                req.addParam("award",        fAward.getText().trim());
                req.addParam("greenStar",    fGreenStar.getText().trim());  
                req.addParam("url",          fUrl.getText().trim());
                req.addParam("website",      fWebsite.getText().trim());   
                req.addParam("descrizione",  aDesc.getText().trim());
                req.addParam("facilities",   aFac.getText().trim());
                req.addParam("delivery",     String.valueOf(cbDelivery.isSelected()));
                req.addParam("prenotazione", String.valueOf(cbPren.isSelected()));
                
                double finaleLat = 0, finaleLon = 0;
                if (!latT.isEmpty()) finaleLat = Double.parseDouble(latT);
                if (!lonT.isEmpty()) finaleLon = Double.parseDouble(lonT);
                
                if (latT.isEmpty() && lonT.isEmpty()) {
                    String luogo = fCitta.getText().trim() + (fNazione.getText().isBlank() ? "" : ", " + fNazione.getText().trim());
                    double[] coord = ClientTK.geocodifica(luogo);
                    if (coord != null && coord.length >= 2) { 
                        finaleLat = coord[0]; 
                        finaleLon = coord[1]; 
                    }
                }
                
                req.addParam("latitudine",   String.valueOf(finaleLat));
                req.addParam("longitudine",  String.valueOf(finaleLon));
                // -----------------------------

                Messaggio resp = sessione.getConnessione().invia(req);
                if (!resp.isOk()) throw new Exception(resp.getParam("errore"));
                return true;
            }
            
            @Override protected void done() {
                try {
                    get();
                    ristoranteSalvato = true; 
                    UI.aggiornaStato(lblStato, "Ristorante aggiunto con successo!", false);
                    btnSalva.setText("Aggiunto");
                    Timer t = new Timer(1200, ev -> dispose());
                    t.setRepeats(false);
                    t.start();
                } catch (Exception ex) {
                    UI.aggiornaStato(lblStato,
                        ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage(), true);
                    btnSalva.setEnabled(true);
                }
            }
        }.execute();
    }

    /**
     * Verifica che il prezzo medio inserito sia coerente con la fascia di prezzo selezionata.
     *
     * @param livello livello della fascia di prezzo (1-4)
     * @param prezzo prezzo medio da validare
     * @return {@code true} se il prezzo rientra nell'intervallo previsto per il livello indicato
     */
    private boolean prezzoCoerente(int livello, double prezzo) {
        return switch (livello) {
            case 1 -> prezzo >= 0 && prezzo < 20;
            case 2 -> prezzo >= 20 && prezzo < 50;
            case 3 -> prezzo >= 50 && prezzo <= 80;
            default -> prezzo >= 80;
        };
    }

    /**
     * Determina il simbolo di valuta più appropriato per una data località,
     * riconoscendo nazioni e città principali tramite corrispondenza testuale.
     * Se la località non è riconosciuta, viene restituito l'euro come valore di default.
     *
     * @param location città e/o nazione da analizzare
     * @return simbolo di valuta corrispondente (es. "€", "$", "£"), "€" come fallback
     */
    public static String simboloValuta(String location) {
        if (location == null || location.isBlank()) return "€";
        String l = location.toLowerCase();
        if (l.contains("japan") || l.contains("giappone") || l.contains("tokyo")
         || l.contains("osaka") || l.contains("kyoto") || l.contains("sapporo")
         || l.contains("hiroshima") || l.contains("nagoya"))             return "¥";
        if (l.contains("china") || l.contains("cina") || l.contains("beijing")
         || l.contains("shanghai") || l.contains("shenzhen") || l.contains("guangzhou")
         || l.contains("chengdu") || l.contains("hong kong"))            return "¥";
        if (l.contains("united kingdom") || l.contains("uk") || l.contains("england")
         || l.contains("london") || l.contains("londra") || l.contains("inghilterra")
         || l.contains("scotland") || l.contains("wales") || l.contains("manchester")
         || l.contains("birmingham") || l.contains("edinburgh"))         return "£";
        if (l.contains("united states") || l.contains("usa") || l.contains("u.s.a")
         || l.contains("new york") || l.contains("los angeles") || l.contains("chicago")
         || l.contains("san francisco") || l.contains("miami") || l.contains("houston")
         || l.contains("las vegas") || l.contains("boston") || l.contains("seattle"))
                                                                          return "$";
        if (l.contains("canada") || l.contains("toronto") || l.contains("vancouver")
         || l.contains("montreal") || l.contains("calgary") || l.contains("ottawa"))
                                                                          return "CA$";
        if (l.contains("australia") || l.contains("sydney") || l.contains("melbourne")
         || l.contains("brisbane") || l.contains("perth") || l.contains("adelaide"))
                                                                          return "A$";
        if (l.contains("switzerland") || l.contains("svizzera") || l.contains("zurich")
         || l.contains("zurigo") || l.contains("geneva") || l.contains("ginevra")
         || l.contains("bern") || l.contains("berna") || l.contains("lausanne"))
                                                                          return "CHF";
        if (l.contains("sweden") || l.contains("svezia") || l.contains("stockholm")
         || l.contains("gothenburg") || l.contains("goteborg"))          return "kr";
        if (l.contains("norway") || l.contains("norvegia") || l.contains("oslo")
         || l.contains("bergen"))                                         return "kr";
        if (l.contains("denmark") || l.contains("danimarca") || l.contains("copenhagen")
         || l.contains("copenaghen"))                                     return "kr";
        if (l.contains("korea") || l.contains("corea") || l.contains("seoul")
         || l.contains("busan"))                                          return "₩";
        if (l.contains("india") || l.contains("mumbai") || l.contains("delhi")
         || l.contains("bangalore") || l.contains("chennai") || l.contains("kolkata"))
                                                                          return "₹";
        if (l.contains("brazil") || l.contains("brasile") || l.contains("brasil")
         || l.contains("sao paulo") || l.contains("rio de janeiro") || l.contains("brasilia"))
                                                                          return "R$";
        if (l.contains("turkey") || l.contains("turchia") || l.contains("istanbul")
         || l.contains("ankara") || l.contains("izmir"))                 return "₺";
        if (l.contains("dubai") || l.contains("abu dhabi") || l.contains("emirates")
         || l.contains("emirati") || l.contains("sharjah"))              return "AED";
        if (l.contains("saudi") || l.contains("arabia saudita") || l.contains("riyadh")
         || l.contains("jeddah"))                                         return "﷼";
        if (l.contains("thailand") || l.contains("tailandia") || l.contains("bangkok")
         || l.contains("phuket") || l.contains("chiang mai"))            return "฿";
        if (l.contains("mexico") || l.contains("messico") || l.contains("ciudad de mexico")
         || l.contains("guadalajara") || l.contains("monterrey"))        return "MX$";
        if (l.contains("argentina") || l.contains("buenos aires"))       return "AR$";
        if (l.contains("russia") || l.contains("mosca") || l.contains("moscow")
         || l.contains("saint petersburg") || l.contains("san pietroburgo")) return "₽";
        if (l.contains("singapore"))                                      return "S$";
        if (l.contains("new zealand") || l.contains("nuova zelanda")
         || l.contains("auckland") || l.contains("wellington"))          return "NZ$";
        return "€";
    }

    // ── Helper layout ──────────────────────────────────────────────────────

    /**
     * Costruisce una card con titolo di sezione e le righe di campi indicate.
     *
     * @param titolo titolo della sezione
     * @param righe pannelli di riga da aggiungere sotto il titolo
     * @return pannello della sezione
     */
    private JPanel sezione(String titolo, JPanel... righe) {
        JPanel c = UI.creaCardPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.add(UI.creaLabelSezione(titolo));
        for (JPanel r : righe) { c.add(Box.createVerticalStrut(8)); c.add(r); }
        return c;
    }

    /**
     * Costruisce una card contenente un titolo di sezione e un'area di testo scrollabile.
     *
     * @param titolo titolo della sezione
     * @param area area di testo da includere
     * @return pannello della card
     */
    private JPanel cardTextArea(String titolo, JTextArea area) {
        JPanel c = UI.creaCardPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.add(UI.creaLabelSezione(titolo));
        c.add(Box.createVerticalStrut(8));
        JScrollPane sc = new JScrollPane(area);
        sc.setBorder(BorderFactory.createLineBorder(UI.BORDO_INPUT));
        sc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        sc.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(sc);
        return c;
    }

    /**
     * Incapsula un singolo campo di testo in un pannello a riga singola.
     *
     * @param f campo di testo da incapsulare
     * @return pannello contenente il campo
     */
    private JPanel r(JTextField f) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(f, BorderLayout.CENTER);
        return p;
    }

    /**
     * Incapsula due campi di testo affiancati in un'unica riga.
     *
     * @param a primo campo di testo
     * @param b secondo campo di testo
     * @return pannello contenente i due campi affiancati
     */
    private JPanel r2(JTextField a, JTextField b) {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(a);
        p.add(b);
        return p;
    }

    /**
     * Costruisce una riga etichetta + componente generico.
     *
     * @param label testo dell'etichetta
     * @param comp componente da affiancare all'etichetta
     * @return pannello contenente etichetta e componente
     */
    private JPanel rl(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel l = new JLabel(label + ":");
        l.setForeground(UI.TESTO_GRIGIO);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    l.setPreferredSize(new Dimension(120, 36));
    comp.setPreferredSize(new Dimension(0, 34));
    p.add(l, BorderLayout.WEST);
    p.add(comp, BorderLayout.CENTER);
    return p;
}
}
