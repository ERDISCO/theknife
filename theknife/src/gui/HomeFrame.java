package theknife.gui;

import theknife.client.ClientTK;
import theknife.common.*;
import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

/**
 * Schermata principale di TheKnife dopo login o accesso ospite.
 * <p>
 * La finestra è organizzata con un {@link CardLayout} che alterna le seguenti
 * schede a seconda della voce di menu selezionata nella sidebar:
 * <ul>
 *     <li>{@code HOME} — ricerca rapida per città e tabella dei ristoranti vicini;</li>
 *     <li>{@code RICERCA} — ricerca avanzata con filtri (città, cucina, raggio,
 *         fascia di prezzo, stelle minime, delivery, prenotazione online);</li>
 *     <li>{@code PREFERITI} — elenco dei ristoranti preferiti (solo utenti cliente);</li>
 *     <li>{@code MIE_REC} — recensioni scritte dall'utente cliente, con possibilità
 *         di modifica ed eliminazione;</li>
 *     <li>{@code GESTORE} — riepilogo dei ristoranti gestiti (solo utenti gestore);</li>
 *     <li>{@code REC_GESTORE} — recensioni ricevute dai ristoranti gestiti, con
 *         possibilità di rispondere o modificare la risposta.</li>
 * </ul>
 * Le sezioni visibili e le relative voci di menu dipendono dallo stato della
 * sessione corrente ({@link SessioneUtente}): ospite, cliente o gestore.
 * <p>
 * Le operazioni di rete verso il server (ricerca, caricamento preferiti,
 * caricamento recensioni, risposta alle recensioni, ecc.) vengono eseguite in
 * background tramite {@link SwingWorker} per non bloccare l'Event Dispatch
 * Thread, mostrando un cursore di attesa durante il caricamento.
 *
 * @author Ayoub Hammou                   761589 — sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 — sede di Varese
 * @version 2.0
 */
public class HomeFrame extends JFrame {

    private static final long   serialVersionUID = 1L;
    private static final String CARD_HOME      = "HOME";
    private static final String CARD_RICERCA   = "RICERCA";
    private static final String CARD_PREFERITI = "PREFERITI";
    private static final String CARD_GESTORE   = "GESTORE";
    private static final String CARD_REC_GESTORE = "REC_GESTORE";

    /** Sessione utente corrente (contiene stato di login, ruolo e connessione al server). */
    private final SessioneUtente sessione   = SessioneUtente.getInstance();
    /** Layout a schede usato per alternare le varie card del contenuto centrale. */
    private final CardLayout     cardLayout = new CardLayout();
    /** Pannello contenitore delle card, gestito da {@link #cardLayout}. */
    private final JPanel         cardPanel  = new JPanel(cardLayout);
    /** Elenco dei bottoni della sidebar, usato per evidenziare la voce di menu attiva. */
    private final List<JButton>  vociMenu   = new java.util.ArrayList<>();

    // ── Card HOME ──────────────────────────────────────────────────────────
    private final JTextField        campoCittaRapida = UI.creaInput("Città — per filtri avanzati usa «Cerca ristoranti»");
    private final JButton           btnCercaVicini   = UI.creaBottone("Cerca", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
    private final DefaultTableModel modelRapido      = UI.creaModelloTabella();
    private final JTable            tabellaRapida    = UI.creaTabella(modelRapido);

    // ── Card RICERCA ───────────────────────────────────────────────────────
    private final JTextField        filtroCitta       = UI.creaInput("Città");
    private final JTextField        filtroCucina      = UI.creaInput("Tipo cucina");
    private final JSpinner          filtroRaggio      = new JSpinner(new SpinnerNumberModel(50, 5, 500, 5));
    private final JComboBox<String> filtroPrezzo      = new JComboBox<>(
            new String[]{"Qualsiasi", "Meno di 20", "20 - 49", "50 - 80", "Più di 80"});
    private final JComboBox<String> filtroStelle      = new JComboBox<>(
            new String[]{"Qualsiasi", "1 minimo", "2 minimo", "3 minimo", "4 minimo"});
    private final JCheckBox         filtroDelivery    = UI.creaCheckbox("Delivery");
    private final JCheckBox         filtroPrenotazione = UI.creaCheckbox("Prenotazione online");
    private final JButton           btnCercaAvanzata  = UI.creaBottone("Cerca ristoranti", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
    private final DefaultTableModel modelRicerca      = UI.creaModelloTabella();
    private final JTable            tabellaRicerca    = UI.creaTabella(modelRicerca);
    private final JLabel            lblRisultati      = UI.creaLabelInfo("Doppio click sulla riga per aprire il dettaglio");

    // ── Card PREFERITI ─────────────────────────────────────────────────────
    private final DefaultTableModel modelPreferiti   = UI.creaModelloTabella();
    private final JTable            tabellaPreferiti = UI.creaTabella(modelPreferiti);
    private final JButton           btnRimuoviPref   = UI.creaBottone("Rimuovi dai preferiti", new Color(0x8E1C1C), new Color(0x6E1212));
    private final JButton           btnAggiornaPref  = UI.creaBottoneSecondario("Aggiorna");

    // ── Card GESTORE ───────────────────────────────────────────────────────
    private final DefaultTableModel modelGestore       = UI.creaModelloTabellaGestore();
    private final JTable            tabellaGestore     = UI.creaTabella(modelGestore);
    // RIMOSSO: btnVediRecensioni — esiste già la card "Recensioni ai miei ristoranti"
    private final JButton           btnAggiungiRisto   = UI.creaBottone("Aggiungi ristorante", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
    private final JButton           btnAggiornaGestore = UI.creaBottoneSecondario("Aggiorna");

    // ── Card MIEI (cliente) ───────────────────────────────────────────────
    private final DefaultTableModel modelMieRec   = new DefaultTableModel(
            new String[]{"#","Ristorante","Stelle","Testo","Data","Risposta"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable  tabellaMieRec   = UI.creaTabella(modelMieRec);
    private final JButton btnModRec       = UI.creaBottone("Modifica", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
    private final JButton btnDelRec       = UI.creaBottone("Elimina", new Color(0x8E1C1C), new Color(0x6E1212));
    private final JButton btnAggMieRec   = UI.creaBottoneSecondario("Aggiorna");
    private final JLabel  lblMieRec      = UI.creaLabelInfo(" ");
    /** Ultimo elenco di recensioni scritte dall'utente cliente, caricato dal server. */
    private List<Recensione>  mieRecensioni;
    /** Ristoranti corrispondenti a {@link #mieRecensioni}, usati per risolvere i nomi senza richieste aggiuntive. */
    private List<Ristorante> ristorantiMieRecensioni;

    // ── Card RECENSIONI AI MIEI RISTORANTI (gestore) ───────────────────────
    private final DefaultTableModel modelRecGestore = new DefaultTableModel(
            new String[]{"#", "Ristorante", "Cliente", "Stelle", "Testo", "Data", "Risposta"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabellaRecGestore = UI.creaTabella(modelRecGestore);
    private final JButton btnAggRecGestore = UI.creaBottoneSecondario("Aggiorna");
    private final JButton btnRispondiRecGestore = UI.creaBottone("Rispondi / Modifica", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
    private final JLabel lblRecGestore = UI.creaLabelInfo(" ");
    /** Ultimo elenco di recensioni ricevute dai ristoranti del gestore, caricato dal server. */
    private List<Recensione> recensioniGestore;
    /** Ristoranti del gestore corrispondenti a {@link #recensioniGestore}, usati per risolvere i nomi. */
    private List<Ristorante> ristorantiGestorePerRecensioni;

    /**
     * Costruisce e mostra la finestra principale dell'applicazione.
     * <p>
     * Imposta dimensioni, posizione, stato massimizzato e la chiusura della
     * sessione alla chiusura della finestra, quindi costruisce l'interfaccia
     * tramite {@link #costruisciUI()}. Se l'utente è un gestore, la card
     * {@code GESTORE} viene mostrata immediatamente e i suoi ristoranti
     * vengono caricati.
     */
    public HomeFrame() {
        super("TheKnife");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 650);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(UI.SFONDO_SCURO);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { sessione.termina(); }
        });
        costruisciUI();
        if (sessione.isGestore()) {
            cardLayout.show(cardPanel, CARD_GESTORE);
            aggiornaVoceAttiva(vociMenu.get(Math.max(0, vociMenu.size() - 2)));
            caricaRistorantiGestore();
        }
    }

    // ── UI principale ─────────────────────────────────────────────────────

    /**
     * Costruisce la struttura generale della finestra: barra superiore,
     * sidebar di navigazione e pannello centrale a schede. Le card
     * disponibili nel {@link #cardPanel} dipendono dal ruolo dell'utente
     * (cliente, gestore o ospite). Al termine collega tutti i listener
     * dei componenti tramite {@link #collegaAzioni()}.
     */
    private void costruisciUI() {
        setLayout(new BorderLayout());
        add(creaTopBar(), BorderLayout.NORTH);
        add(creaSidebar(), BorderLayout.WEST);
        cardPanel.setBackground(UI.SFONDO_SCURO);
        cardPanel.add(creaCardHome(),      CARD_HOME);
        cardPanel.add(creaCardRicerca(),   CARD_RICERCA);
        if (sessione.isCliente()) {
            cardPanel.add(creaCardPreferiti(), CARD_PREFERITI);
            cardPanel.add(creaCardMieRecensioni(), "MIE_REC");
        }
        if (sessione.isGestore()) {
            cardPanel.add(creaCardGestore(), CARD_GESTORE);
            cardPanel.add(creaCardRecensioniGestore(), CARD_REC_GESTORE);
        }
        add(cardPanel, BorderLayout.CENTER);
        collegaAzioni();
    }

    // ── Top bar ───────────────────────────────────────────────────────────

    /**
     * Crea la barra superiore con il logo dell'applicazione, il badge del
     * ruolo (cliente/gestore) se l'utente è loggato, il saluto personalizzato,
     * il bottone per modificare il profilo (se loggato) e il bottone di
     * logout/accesso.
     *
     * @return il pannello della barra superiore
     */
    private JPanel creaTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x111113));
        bar.setBorder(new EmptyBorder(10, 20, 10, 20));
        JLabel logo = new JLabel("TheKnife");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(UI.ROSSO_KNIFE);
        JPanel destra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        destra.setOpaque(false);
        if (sessione.isLoggato())
            destra.add(UI.creaBadge(sessione.isGestore() ? "GESTORE" : "CLIENTE",
                    sessione.isGestore() ? new Color(0x2E86AB) : new Color(0x27AE60)));
        JLabel lblSaluto = new JLabel(sessione.isLoggato() ? "Ciao, " + sessione.getNomeDisplay() : "Modalità ospite");
        lblSaluto.setForeground(UI.TESTO_GRIGIO);
        lblSaluto.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JButton btnLogout = UI.creaBottoneSecondario(sessione.isLoggato() ? "Logout" : "Accedi");
        btnLogout.addActionListener(e -> { dispose(); sessione.termina(); new LoginFrame().setVisible(true); });

        if (sessione.isLoggato()) {
            JButton btnModProfilo = UI.creaBottoneSecondario("Modifica profilo");
            btnModProfilo.addActionListener(e ->
                new ModificaDialog(HomeFrame.this, sessione.getUtente(), sessione.getConnessione()).setVisible(true));
            destra.add(btnModProfilo);
        }
        destra.add(lblSaluto);
        destra.add(btnLogout);
        bar.add(logo, BorderLayout.WEST);
        bar.add(destra, BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────

    /**
     * Crea la sidebar di navigazione laterale, con le voci sempre visibili
     * (Home, Cerca ristoranti) e le sezioni condizionali "Il mio account"
     * (per i clienti: preferiti e recensioni) e "Gestione" (per i gestori:
     * ristoranti e recensioni ricevute). Include anche l'etichetta
     * informativa in basso, che varia in base al ruolo dell'utente.
     *
     * @return il pannello della sidebar
     */
    private JPanel creaSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(new Color(0x161618));
        sb.setBorder(new EmptyBorder(20, 0, 20, 0));
        sb.setPreferredSize(new Dimension(210, 0));

        // Separatore sezione principale
        sb.add(creaSezioneLabel("NAVIGAZIONE"));
        sb.add(Box.createVerticalStrut(4));
        sb.add(voceMenu("Home",             CARD_HOME));
        sb.add(Box.createVerticalStrut(2));
        sb.add(voceMenu("Cerca ristoranti", CARD_RICERCA));

        if (sessione.isCliente()) {
            sb.add(Box.createVerticalStrut(12));
            sb.add(creaSezioneLabel("IL MIO ACCOUNT"));
            sb.add(Box.createVerticalStrut(4));
            sb.add(voceMenu("I miei preferiti",  CARD_PREFERITI));
            sb.add(Box.createVerticalStrut(2));
            sb.add(voceMenu("Le mie recensioni", "MIE_REC"));
        }

        if (sessione.isGestore()) {
            sb.add(Box.createVerticalStrut(12));
            sb.add(creaSezioneLabel("GESTIONE"));
            sb.add(Box.createVerticalStrut(4));
            sb.add(voceMenu("I miei ristoranti", CARD_GESTORE));
            sb.add(Box.createVerticalStrut(2));
            sb.add(voceMenu("Recensioni ricevute", CARD_REC_GESTORE));
        }

        aggiornaVoceAttiva(vociMenu.get(0));
        sb.add(Box.createVerticalGlue());

        // Footer info con linea separatrice
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2A2A2C));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE-10, 1));
        sb.add(sep);
        sb.add(Box.createVerticalStrut(20));

        JLabel info = new JLabel(!sessione.isLoggato() ? "Ospite: solo ricerca"
                : sessione.isGestore() ? "Gestisci il tuo locale" : "Cerca, recensisci, salva",
                SwingConstants.CENTER);
        info.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        info.setForeground(new Color(0x555558));
        info.setAlignmentY(Component.TOP_ALIGNMENT);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);
        info.setBorder(new EmptyBorder(0, 10, 0, 10));
        sb.add(info);
        return sb;
    }

    /**
     * Crea un'etichetta di intestazione per una sezione della sidebar
     * (es. "NAVIGAZIONE", "GESTIONE"), con stile grafico dedicato.
     *
     * @param testo il testo dell'intestazione di sezione
     * @return l'etichetta pronta per essere aggiunta alla sidebar
     */
    private JLabel creaSezioneLabel(String testo) {
        JLabel lbl = new JLabel(testo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(0x444448));
        lbl.setBorder(new EmptyBorder(6, 20, 2, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Crea un bottone della sidebar che, se cliccato, mostra la card
     * indicata e ne evidenzia la voce di menu corrispondente. Il bottone
     * ha un aspetto personalizzato (barra rossa laterale ed evidenziazione
     * al passaggio del mouse quando è la voce attiva) e viene registrato
     * in {@link #vociMenu}. Alla selezione, se necessario, avvia anche il
     * caricamento dei dati associati alla card (preferiti, ristoranti
     * gestore, recensioni ricevute o recensioni proprie).
     *
     * @param testo etichetta visualizzata sul bottone
     * @param card  identificativo della card da mostrare in {@link #cardPanel}
     * @return il bottone di menu creato e già registrato in {@link #vociMenu}
     */
    private JButton voceMenu(String testo, String card) {
        JButton btn = new JButton(testo) {
            private boolean sopra = false;
            { addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { sopra = true;  repaint(); }
                public void mouseExited (MouseEvent e) { sopra = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                boolean attivo = getFont().isBold();
                // Sfondo con gradiente leggero quando attivo
                if (attivo) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    GradientPaint gp = new GradientPaint(
                            0, 0, new Color(UI.ROSSO_KNIFE.getRed(), UI.ROSSO_KNIFE.getGreen(),
                                    UI.ROSSO_KNIFE.getBlue(), 30),
                            getWidth(), 0, new Color(0x2A2A2C));
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                } else if (sopra) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(0x222224));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
                // Barra rossa sinistra quando attivo
                if (attivo) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UI.ROSSO_KNIFE);
                    g2.fillRoundRect(0, 4, 3, getHeight() - 8, 3, 3);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setForeground(UI.TESTO_GRIGIO);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(new Color(0x161618));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(9, 20, 9, 17));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            aggiornaVoceAttiva(btn);
            cardLayout.show(cardPanel, card);
            if (card.equals(CARD_PREFERITI)) caricaPreferiti();
            if (card.equals(CARD_GESTORE))   caricaRistorantiGestore();
            if (card.equals(CARD_REC_GESTORE)) caricaRecensioniGestore();
            if (card.equals("MIE_REC"))      caricaMieRecensioni();
        });
        vociMenu.add(btn);
        return btn;
    }

    /**
     * Aggiorna lo stile grafico delle voci di menu della sidebar in modo che
     * solo il bottone indicato risulti evidenziato come voce attiva
     * (font in grassetto e colore del testo più chiaro).
     *
     * @param attivo il bottone della sidebar da marcare come selezionato
     */
    private void aggiornaVoceAttiva(JButton attivo) {
        for (JButton b : vociMenu) {
            boolean sel = b == attivo;
            b.setFont(new Font("Segoe UI", sel ? Font.BOLD : Font.PLAIN, 13));
            b.setForeground(sel ? UI.TESTO_CHIARO : UI.TESTO_GRIGIO);
            b.repaint();
        }
    }

    // ── Card HOME ─────────────────────────────────────────────────────────

    /**
     * Costruisce la card {@code HOME}, con titolo di benvenuto personalizzato
     * in base al ruolo/stato di login, il campo di ricerca rapida per città
     * e la tabella dei ristoranti trovati.
     *
     * @return il pannello della card Home
     */
    private JPanel creaCardHome() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UI.SFONDO_SCURO);
        p.setBorder(new EmptyBorder(28, 30, 20, 30));

        String hero = !sessione.isLoggato() ? "Scopri i migliori ristoranti"
                : sessione.isGestore() ? "Benvenuto, " + sessione.getUtente().getNome()
                : "Bentornato, " + sessione.getUtente().getNome();
        JLabel lblHero = new JLabel(hero);
        lblHero.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblHero.setForeground(UI.TESTO_CHIARO);
        lblHero.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel(!sessione.isLoggato() ? "Inserisci una città per iniziare a esplorare"
                : sessione.isGestore() ? "Gestisci il tuo ristorante dalla sidebar"
                : "Cosa vuoi mangiare oggi?");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(UI.TESTO_GRIGIO);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cerca = new JPanel(new BorderLayout(8, 0));
        cerca.setOpaque(false);
        cerca.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        cerca.setAlignmentX(Component.LEFT_ALIGNMENT);
        cerca.add(campoCittaRapida, BorderLayout.CENTER);
        btnCercaVicini.setPreferredSize(new Dimension(90, 38));
        cerca.add(btnCercaVicini, BorderLayout.EAST);

        p.add(lblHero); p.add(Box.createVerticalStrut(6)); p.add(lblSub);
        p.add(Box.createVerticalStrut(20)); p.add(cerca);
        p.add(Box.createVerticalStrut(16));
        p.add(UI.stilizzaTabella(tabellaRapida));
        return p;
    }

    // ── Card RICERCA ──────────────────────────────────────────────────────

    /**
     * Costruisce la card {@code RICERCA}, con il pannello dei filtri di
     * ricerca avanzata (città, cucina, raggio, fascia di prezzo, stelle
     * minime, delivery, prenotazione online) e la tabella dei risultati con
     * relativa etichetta di stato.
     *
     * @return il pannello della card Ricerca
     */
    private JPanel creaCardRicerca() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(UI.SFONDO_SCURO);
        p.setBorder(new EmptyBorder(20, 24, 16, 24));

        // Filtri
        JPanel filtri = new JPanel(new GridBagLayout());
        filtri.setBackground(UI.SFONDO_CARD);
        filtri.setBorder(new EmptyBorder(14, 16, 14, 16));
        GridBagConstraints gbc = UI.gbc();

        UI.stilizzaSpinner(filtroRaggio);
        UI.stilizzaCombo(filtroPrezzo);
        UI.stilizzaCombo(filtroStelle);

        JPanel rigaCitta = UI.creaRigaFiltro("Città *", filtroCitta);
        JPanel rigaCucina = UI.creaRigaFiltro("Cucina", filtroCucina);
        JPanel rigaRaggio = UI.creaRigaFiltro("Raggio (km)", filtroRaggio);
        JPanel rigaPrezzo = UI.creaRigaFiltro("Fascia prezzo", filtroPrezzo);
        JPanel rigaStelle = UI.creaRigaFiltro("Stelle min.", filtroStelle);

        JPanel checkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        checkRow.setOpaque(false);
        checkRow.add(filtroDelivery); checkRow.add(filtroPrenotazione);

        gbc.gridy = 0; filtri.add(rigaCitta, gbc);
        gbc.gridy = 1; filtri.add(rigaCucina, gbc);
        gbc.gridy = 2; filtri.add(rigaRaggio, gbc);
        gbc.gridy = 3; filtri.add(rigaPrezzo, gbc);
        gbc.gridy = 4; filtri.add(rigaStelle, gbc);
        gbc.gridy = 5; filtri.add(checkRow, gbc);
        gbc.gridy = 6; gbc.insets = new Insets(10, 0, 0, 0);
        btnCercaAvanzata.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        filtri.add(btnCercaAvanzata, gbc);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        bottom.setOpaque(false);
        lblRisultati.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.add(lblRisultati, BorderLayout.NORTH);
        bottom.add(UI.stilizzaTabella(tabellaRicerca), BorderLayout.CENTER);

        p.add(filtri, BorderLayout.NORTH);
        p.add(bottom, BorderLayout.CENTER);
        return p;
    }

    // ── Card PREFERITI ────────────────────────────────────────────────────

    /**
     * Costruisce la card {@code PREFERITI}, con l'elenco dei ristoranti
     * salvati dall'utente cliente e i bottoni per aggiornare la lista o
     * rimuovere il ristorante selezionato.
     *
     * @return il pannello della card Preferiti
     */
    private JPanel creaCardPreferiti() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UI.SFONDO_SCURO);
        p.setBorder(new EmptyBorder(20, 24, 16, 24));
        p.add(UI.creaCardTitolo("I miei preferiti", "I ristoranti che hai salvato"), BorderLayout.NORTH);
        p.add(UI.stilizzaTabella(tabellaPreferiti), BorderLayout.CENTER);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bar.setOpaque(false);
        btnAggiornaPref.setPreferredSize(new Dimension(100, 32));
        btnRimuoviPref.setPreferredSize(new Dimension(200, 32));
        bar.add(btnAggiornaPref); bar.add(btnRimuoviPref);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    // ── Card GESTORE ──────────────────────────────────────────────────────

    /**
     * Costruisce la card {@code GESTORE}, con la tabella riepilogativa dei
     * ristoranti gestiti (comprensiva di valutazione media) e i bottoni per
     * aggiornare l'elenco, modificare il ristorante selezionato o
     * aggiungerne uno nuovo.
     *
     * @return il pannello della card Gestore
     */
    private JPanel creaCardGestore() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UI.SFONDO_SCURO);
        p.setBorder(new EmptyBorder(20, 24, 16, 24));
        p.add(UI.creaCardTitolo("I miei ristoranti", "Riepilogo con valutazione media"), BorderLayout.NORTH);
        p.add(UI.stilizzaTabella(tabellaGestore), BorderLayout.CENTER);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bar.setOpaque(false);
        btnAggiornaGestore.setPreferredSize(new Dimension(100, 32));
        // RIMOSSO: btnVediRecensioni — usa la card "Recensioni ricevute" nella sidebar
        btnAggiungiRisto.setPreferredSize(new Dimension(200, 32));
        JButton btnModRisto = UI.creaBottoneSecondario("Modifica");
        if (sessione.isLoggato()) {
            btnModRisto.addActionListener(e -> {
                int row = tabellaGestore.getSelectedRow();
                if (row < 0) { UI.aggiornaStato(lblRecGestore, "Seleziona un ristorante da modificare", true); ripristinaCursoreNormale();return; }
                Ristorante r = (Ristorante) modelGestore.getValueAt(row, 0);
                new ModificaDialog(HomeFrame.this, r, sessione.getConnessione()).setVisible(true);
            });
        }
        bar.add(btnAggiornaGestore); bar.add(btnModRisto); bar.add(btnAggiungiRisto);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    // ── Card RECENSIONI AI MIEI RISTORANTI (gestore) ──────────────────────

    /**
     * Costruisce la card {@code REC_GESTORE}, con la tabella delle
     * recensioni ricevute da tutti i ristoranti del gestore e i bottoni per
     * aggiornare l'elenco o rispondere/modificare la risposta alla
     * recensione selezionata.
     *
     * @return il pannello della card Recensioni gestore
     */
    private JPanel creaCardRecensioniGestore() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UI.SFONDO_SCURO);
        p.setBorder(new EmptyBorder(20, 24, 16, 24));
        p.add(UI.creaCardTitolo("Recensioni ai miei ristoranti",
                "Tutte le recensioni ricevute dai ristoranti che gestisci"), BorderLayout.NORTH);
        p.add(UI.stilizzaTabella(tabellaRecGestore), BorderLayout.CENTER);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bar.setOpaque(false);
        lblRecGestore.setForeground(UI.TESTO_GRIGIO);
        btnAggRecGestore.setPreferredSize(new Dimension(100, 32));
        btnRispondiRecGestore.setPreferredSize(new Dimension(180, 32));
        bar.add(lblRecGestore);
        bar.add(Box.createHorizontalGlue());
        bar.add(btnAggRecGestore);
        bar.add(btnRispondiRecGestore);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    // ── Card MIE RECENSIONI (cliente) ─────────────────────────────────────

    /**
     * Costruisce la card {@code MIE_REC}, con la tabella delle recensioni
     * scritte dall'utente cliente e i bottoni per aggiornare l'elenco,
     * modificare o eliminare la recensione selezionata.
     *
     * @return il pannello della card Mie recensioni
     */
    private JPanel creaCardMieRecensioni() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UI.SFONDO_SCURO);
        p.setBorder(new EmptyBorder(20, 24, 16, 24));
        p.add(UI.creaCardTitolo("Le mie recensioni", "Tutte le recensioni che hai scritto"), BorderLayout.NORTH);
        p.add(UI.stilizzaTabella(tabellaMieRec), BorderLayout.CENTER);
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        bar.setOpaque(false);
        lblMieRec.setForeground(UI.TESTO_GRIGIO);
        btnAggMieRec.setPreferredSize(new Dimension(100, 32));
        btnModRec.setPreferredSize(new Dimension(120, 32));
        btnDelRec.setPreferredSize(new Dimension(100, 32));
        bar.add(lblMieRec); bar.add(Box.createHorizontalGlue());
        bar.add(btnAggMieRec); bar.add(btnModRec); bar.add(btnDelRec);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    // ── Azioni ───────────────────────────────────────────────────────────

    /**
     * Collega tutti i listener dei componenti interattivi delle varie card
     * (bottoni, campi di testo con invio, doppio click sulle righe delle
     * tabelle) alle rispettive azioni di logica applicativa.
     */
    private void collegaAzioni() {
        // HOME rapida
        btnCercaVicini.addActionListener(e -> cercaRapida());
        campoCittaRapida.addActionListener(e -> cercaRapida());
        tabellaRapida.addMouseListener(doppioClic(tabellaRapida, modelRapido));

        // RICERCA avanzata
        btnCercaAvanzata.addActionListener(e -> cercaAvanzata());
        tabellaRicerca.addMouseListener(doppioClic(tabellaRicerca, modelRicerca));

        // PREFERITI
        btnAggiornaPref.addActionListener(e -> caricaPreferiti());
        btnRimuoviPref.addActionListener(e -> rimuoviPreferito());
        tabellaPreferiti.addMouseListener(doppioClic(tabellaPreferiti, modelPreferiti));

        // GESTORE — btnVediRecensioni rimosso, si usa la sidebar
        btnAggiornaGestore.addActionListener(e -> caricaRistorantiGestore());
        btnAggiungiRisto.addActionListener(e ->
                new AggiungiRistoranteDialog(this).setVisible(true));
        tabellaGestore.addMouseListener(doppioClic(tabellaGestore, modelGestore));
        btnAggRecGestore.addActionListener(e -> caricaRecensioniGestore());
        btnRispondiRecGestore.addActionListener(e -> rispondiRecensioneGestoreSelezionata());
        tabellaRecGestore.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) rispondiRecensioneGestoreSelezionata();
            }
        });

        // MIE RECENSIONI
        btnAggMieRec.addActionListener(e -> caricaMieRecensioni());
        btnModRec.addActionListener(e -> modificaRecensione());
        btnDelRec.addActionListener(e -> eliminaRecensione());
    }

    // ── Logica dati ───────────────────────────────────────────────────────

    /**
     * Esegue la ricerca rapida dalla card Home: geocodifica la città
     * inserita nel campo di ricerca rapida (o, se vuoto, usa il domicilio
     * dell'utente loggato) e interroga il server per i ristoranti entro il
     * raggio impostato, senza applicare filtri avanzati. L'operazione viene
     * eseguita in background e la tabella {@link #modelRapido} viene
     * popolata al termine.
     */
    private void cercaRapida() {
    	
        String citta = campoCittaRapida.getText().trim();

        if (citta.isEmpty() && sessione.isLoggato())
            citta = sessione.getUtente().getLuogoDomicilio();

        if (citta.isEmpty()) {
            mostraInfo("Inserisci una città");
            return;
        }

        final String cittaFin = citta;

        new SwingWorker<List<Ristorante>, Void>() {

            @Override
            protected List<Ristorante> doInBackground() throws Exception {
            	 mostraCursoreCaricamento();
                double[] coord = ClientTK.geocodifica(cittaFin);

                if (coord == null) {
                    throw new Exception("Impossibile trovare la posizione di " + cittaFin);
                }

                Messaggio req = new Messaggio(Messaggio.OP_CERCA_RISTORANTI);

                req.addParam("latitudine", String.valueOf(coord[0]));
                req.addParam("longitudine", String.valueOf(coord[1]));

                // Ricerca base = sempre 50 km
                req.addParam("raggioKm", String.valueOf(filtroRaggio.getValue()));

                req.addParam("cucina", null);
                req.addParam("prezzoMin", null);
                req.addParam("prezzoMax", null);
                req.addParam("delivery", null);
                req.addParam("prenotazione", null);
                req.addParam("stelleMinime", null);

                Messaggio resp = sessione.getConnessione().invia(req);

                if (!resp.isOk())
                    throw new Exception(resp.getParam("errore"));

                if (resp.getDatiJson() == null || resp.getDatiJson().isBlank())
                    return List.of();

                return Arrays.asList(
                    new Gson().fromJson(
                        resp.getDatiJson(),
                        Ristorante[].class
                    )
                );
            }

            @Override
            protected void done() {
                try {
                    popolaTabella(modelRapido, get());
                } catch (Exception ex) {
                    mostraErrore("Errore ricerca: " + ex.getMessage());
                }
                ripristinaCursoreNormale();
            }

        }.execute();
    }
    

    /**
     * Esegue la ricerca avanzata dalla card Ricerca, leggendo tutti i filtri
     * impostati dall'utente (città obbligatoria, cucina, raggio, fascia di
     * prezzo, stelle minime, delivery, prenotazione online) e delegando
     * l'interrogazione del server a {@link #eseguiCerca}. Se la città non è
     * indicata, mostra un messaggio nell'etichetta dei risultati e interrompe
     * l'operazione.
     */
    private void cercaAvanzata() {
        String citta  = filtroCitta.getText().trim();
        String cucina = filtroCucina.getText().trim();
        if (citta.isEmpty()) { lblRisultati.setText("Inserisci almeno una città"); return; }

        int idx = filtroPrezzo.getSelectedIndex();
        int[] fascMin = {0,  0, 20, 50, 80};
        int[] fascMax = {0, 19, 49, 80,  0}; // 0 = nessun limite
        Double pMin = idx == 0 ? null : (double) fascMin[idx];
        Double pMax = idx == 0 ? null : fascMax[idx] == 0 ? null : (double) fascMax[idx];

        int stelleIdx = filtroStelle.getSelectedIndex();
        Integer stelle = stelleIdx == 0 ? null : stelleIdx;
        Boolean delivery = filtroDelivery.isSelected() ? true : null;
        Boolean pren     = filtroPrenotazione.isSelected() ? true : null;
        double raggio    = (double)(int) filtroRaggio.getValue();

        eseguiCerca(citta, raggio, cucina.isEmpty() ? null : cucina,
                pMin, pMax, delivery, pren, stelle, modelRicerca,
                "Ricerca in corso...", lblRisultati);
    }

    /**
     * Esegue in background una richiesta di ricerca ristoranti al server con
     * i criteri indicati e popola la tabella fornita con i risultati.
     * Tenta prima di geocodificare la città per effettuare una ricerca per
     * coordinate e raggio; se la geocodifica fallisce, invia la città
     * direttamente al server come parametro testuale. Aggiorna l'etichetta
     * di stato con il messaggio di caricamento prima della richiesta e con
     * l'esito (numero di risultati o errore) al termine.
     *
     * @param citta      città su cui centrare la ricerca
     * @param raggio     raggio di ricerca in km
     * @param cucina     tipo di cucina da filtrare, oppure {@code null} per non filtrare
     * @param pMin       prezzo minimo, oppure {@code null} per nessun limite
     * @param pMax       prezzo massimo, oppure {@code null} per nessun limite
     * @param delivery   {@code true} per richiedere solo ristoranti con delivery, {@code null} per non filtrare
     * @param pren       {@code true} per richiedere solo ristoranti con prenotazione online, {@code null} per non filtrare
     * @param stelle     numero minimo di stelle, oppure {@code null} per non filtrare
     * @param modello    modello della tabella da popolare con i risultati
     * @param msgCarica  messaggio da mostrare in {@code lblFeedback} durante il caricamento
     * @param lblFeedback etichetta di stato da aggiornare con l'esito, può essere {@code null}
     */
    private void eseguiCerca(String citta, double raggio, String cucina,
            Double pMin, Double pMax, Boolean delivery, Boolean pren, Integer stelle, DefaultTableModel modello, String msgCarica, JLabel lblFeedback) {
    	mostraCursoreCaricamento();
    	if (lblFeedback != null) lblFeedback.setText(msgCarica);
        
        new SwingWorker<List<Ristorante>, Void>() {
        	
            @Override protected List<Ristorante> doInBackground() throws Exception {
            	
                double[] coord = ClientTK.geocodifica(citta);
                Messaggio req = new Messaggio(Messaggio.OP_CERCA_RISTORANTI);
                if (coord != null) {
                    req.addParam("latitudine",  String.valueOf(coord[0]));
                    req.addParam("longitudine", String.valueOf(coord[1]));
                } else {
                    req.addParam("citta", citta);
                }
                req.addParam("raggioKm",    String.valueOf(raggio));
                req.addParam("cucina",      cucina);
                req.addParam("prezzoMin",   pMin != null ? String.valueOf(pMin.intValue()) : null);
                req.addParam("prezzoMax",   pMax != null ? String.valueOf(pMax.intValue()) : null);
                req.addParam("delivery",    delivery != null ? String.valueOf(delivery) : null);
                req.addParam("prenotazione",pren     != null ? String.valueOf(pren)     : null);
                req.addParam("stelleMinime",stelle   != null ? String.valueOf(stelle)   : null);
                Messaggio resp = sessione.getConnessione().invia(req);
                if (!resp.isOk()) throw new Exception(resp.getParam("errore"));
                if (resp.getDatiJson() == null || resp.getDatiJson().isBlank()) return List.of();
                return Arrays.asList(new Gson().fromJson(resp.getDatiJson(), Ristorante[].class));
            }
            @Override protected void done() {
                try {
                    List<Ristorante> lista = get();
                    popolaTabella(modello, lista);
                    if (lblFeedback != null)
                        lblFeedback.setText(lista.size() + " ristoranti trovati — doppio click per il dettaglio");
                } catch (Exception ex) {
                    if (lblFeedback != null) lblFeedback.setText("Errore: " + ex.getMessage());
                }ripristinaCursoreNormale();
            }
        }.execute();
    }

    /**
     * Carica in background l'elenco dei ristoranti preferiti dell'utente
     * cliente dal server e popola la tabella {@link #modelPreferiti}.
     * Durante il caricamento disabilita il bottone di aggiornamento e ne
     * cambia il testo, ripristinandolo al termine.
     */
    private void caricaPreferiti() {
    	
        btnAggiornaPref.setEnabled(false); btnAggiornaPref.setText("Caricamento...");
        mostraCursoreCaricamento();
        new SwingWorker<List<Ristorante>, Void>() {
            @Override protected List<Ristorante> doInBackground() throws Exception {
                Messaggio resp = sessione.getConnessione().invia(new Messaggio(Messaggio.OP_PREFERITI));
                if (!resp.isOk()) throw new Exception(resp.getParam("errore"));
                if (resp.getDatiJson() == null || resp.getDatiJson().isBlank()) return List.of();
                return Arrays.asList(new Gson().fromJson(resp.getDatiJson(), Ristorante[].class));
            }
            @Override protected void done() {
                btnAggiornaPref.setEnabled(true); btnAggiornaPref.setText("Aggiorna");
                try { popolaTabella(modelPreferiti, get()); }
                catch (Exception ex) { mostraErrore("Impossibile caricare i preferiti"); }
                ripristinaCursoreNormale();
            }
        }.execute();
    }

    /**
     * Carica in background il riepilogo dei ristoranti gestiti dall'utente
     * gestore (con relativa valutazione media e numero di recensioni) e
     * popola la tabella {@link #modelGestore}. Durante il caricamento
     * disabilita il bottone di aggiornamento e ne cambia il testo,
     * ripristinandolo al termine.
     */
    private void caricaRistorantiGestore() {
    	mostraCursoreCaricamento();
        btnAggiornaGestore.setEnabled(false); btnAggiornaGestore.setText("Caricamento...");
        new SwingWorker<List<Ristorante>, Void>() {
            @Override protected List<Ristorante> doInBackground() throws Exception {
                Messaggio resp = sessione.getConnessione().invia(new Messaggio(Messaggio.OP_RIEPILOGO_RISTO));
                if (!resp.isOk()) throw new Exception(resp.getParam("errore"));
                if (resp.getDatiJson() == null || resp.getDatiJson().isBlank()) return List.of();
                return Arrays.asList(new Gson().fromJson(resp.getDatiJson(), Ristorante[].class));
            }
            @Override protected void done() {
                btnAggiornaGestore.setEnabled(true); btnAggiornaGestore.setText("Aggiorna");
                try {
                    List<Ristorante> lista = get();
                    modelGestore.setRowCount(0);
                    for (Ristorante r : lista)
                    	modelGestore.addRow(new Object[]{
                    	        r,
                    	        r.getNome(),
                    	        r.getLocation(),
                    	        r.getTipoCucina(),
                    	        voto(r.getMediaVoto()),
                    	        r.getNumeroRecensioni()
                    	});
                } catch (Exception ex) { mostraErrore("Impossibile caricare i ristoranti"); }
                ripristinaCursoreNormale();
            }
        }.execute();
    }

   
    /**
     * Carica in background tutte le recensioni scritte dall'utente cliente e,
     * per ciascun ristorante distinto coinvolto, recupera il relativo
     * dettaglio dal server per poterne mostrare il nome senza richieste
     * ripetute. Popola quindi {@link #modelMieRec} e memorizza i risultati
     * in {@link #mieRecensioni} e {@link #ristorantiMieRecensioni}. Il testo
     * della recensione viene troncato a 50 caratteri nella tabella. Aggiorna
     * l'etichetta {@link #lblMieRec} con l'esito dell'operazione.
     */
    private void caricaMieRecensioni() {
    	
        btnAggMieRec.setEnabled(false);

        new SwingWorker<java.util.AbstractMap.SimpleEntry<List<Recensione>, List<Ristorante>>, Void>() {

            @Override
            protected java.util.AbstractMap.SimpleEntry<List<Recensione>, List<Ristorante>> doInBackground()
                    throws Exception {
            	mostraCursoreCaricamento();
                // 1. Recupera tutte le recensioni del cliente
            	Messaggio recResp = sessione.getConnessione().invia(
                        new Messaggio(Messaggio.OP_MIE_RECENSIONI)
                );
            	
                if (!recResp.isOk()) {
                    throw new Exception(recResp.getParam("errore"));
                }

                List<Recensione> recensioni =
                        recResp.getDatiJson() == null || recResp.getDatiJson().isBlank()
                        ? List.of()
                        : Arrays.asList(
                                new Gson().fromJson(
                                        recResp.getDatiJson(),
                                        Recensione[].class
                                )
                          );

                // 2. Recupera i ristoranti necessari per ottenere i nomi
                List<Ristorante> ristoranti = new java.util.ArrayList<>();
                
                java.util.Set<Integer> idGiaCaricati = new java.util.HashSet<>();

                for (Recensione rec : recensioni) {

                    int ristoranteId = rec.getRistoranteId();

                    if (idGiaCaricati.contains(ristoranteId)) {
                        continue;
                    }
                    idGiaCaricati.add(ristoranteId);
                    Messaggio req = new Messaggio(Messaggio.OP_DETTAGLIO_RISTO);
                    req.addParam(
                            "ristoranteId",
                            String.valueOf(ristoranteId)
                    );

                    Messaggio resp = sessione.getConnessione().invia(req);

                    if (resp.isOk() && resp.getDatiJson() != null && !resp.getDatiJson().isBlank()) {
                        Ristorante r = new Gson().fromJson(resp.getDatiJson(), Ristorante.class);
                        if (r != null && r.getId() != 0) {
                            ristoranti.add(r);
                        }
                    }
                }

                return new java.util.AbstractMap.SimpleEntry<>(
                        recensioni,
                        ristoranti
                );
            }

            @Override
            protected void done() {
                btnAggMieRec.setEnabled(true);

                try {
                    java.util.AbstractMap.SimpleEntry<List<Recensione>, List<Ristorante>> x = get();

                    mieRecensioni = x.getKey();

                    // Salva i ristoranti per poter recuperare il nome
                    // senza fare altre richieste al server
                    ristorantiMieRecensioni = x.getValue();

                    modelMieRec.setRowCount(0);

                    for (int i = 0; i < mieRecensioni.size(); i++) {

                        Recensione rec = mieRecensioni.get(i);

                        String nome = nomeRistoranteMieRecensione(
                                rec.getRistoranteId()
                        );

                        String testo = rec.getTesto() == null
                                ? ""
                                : rec.getTesto();

                        if (testo.length() > 50) {
                            testo = testo.substring(0, 47) + "...";
                        }

                        modelMieRec.addRow(new Object[]{
                                i + 1,
                                nome,
                                rec.getStelle() + " / 5",
                                testo,
                                rec.getDataRecensione(),
                                rec.haRisposta()
                                        ? rec.getRispostaGestore()
                                        : "Nessuna risposta"
                        });
                    }

                    UI.aggiornaStato(
                            lblMieRec,
                            mieRecensioni.size() + " recensioni",
                            false
                    );

                } catch (Exception ex) {
                    UI.aggiornaStato(
                            lblMieRec,
                            "Errore: " + ex.getMessage(),
                            true
                    ); ripristinaCursoreNormale();
                }ripristinaCursoreNormale();
            }
        }.execute();
    }

    /**
     * Restituisce il nome del ristorante corrispondente all'id indicato,
     * cercandolo nell'elenco {@link #ristorantiMieRecensioni} caricato in
     * precedenza da {@link #caricaMieRecensioni()}.
     *
     * @param ristoranteId identificativo del ristorante da cercare
     * @return il nome del ristorante se trovato, altrimenti la stringa
     *         generica {@code "Ristorante"}
     */
    private String nomeRistoranteMieRecensione(int ristoranteId) {
        if (ristorantiMieRecensioni == null) return "Ristorante";
        for (Ristorante r : ristorantiMieRecensioni) {
            if (r.getId() == ristoranteId) return r.getNome();
        }
        return "Ristorante";
    }


    /**
     * Carica in background tutte le recensioni ricevute dai ristoranti
     * gestiti dall'utente gestore: recupera prima l'elenco dei ristoranti
     * gestiti, quindi per ciascuno di essi le relative recensioni. Popola
     * {@link #modelRecGestore} e memorizza i risultati in
     * {@link #recensioniGestore} e {@link #ristorantiGestorePerRecensioni}.
     * Il testo della recensione viene troncato a 60 caratteri nella tabella.
     * Aggiorna l'etichetta {@link #lblRecGestore} con l'esito dell'operazione.
     */
    private void caricaRecensioniGestore() {
    	mostraCursoreCaricamento();
        btnAggRecGestore.setEnabled(false);
        new SwingWorker<java.util.AbstractMap.SimpleEntry<List<Recensione>, List<Ristorante>>, Void>() {
            @Override protected java.util.AbstractMap.SimpleEntry<List<Recensione>, List<Ristorante>> doInBackground() throws Exception {
                Messaggio rResp = sessione.getConnessione().invia(new Messaggio(Messaggio.OP_RIEPILOGO_RISTO));
                if (!rResp.isOk()) throw new Exception(rResp.getParam("errore"));
                List<Ristorante> ristoranti = rResp.getDatiJson() == null || rResp.getDatiJson().isBlank()
                        ? List.of() : Arrays.asList(new Gson().fromJson(rResp.getDatiJson(), Ristorante[].class));
                List<Recensione> tutte = new java.util.ArrayList<>();
                for (Ristorante r : ristoranti) {
                    Messaggio req = new Messaggio(Messaggio.OP_RECENSIONI_RISTO);
                    req.addParam("ristoranteId", String.valueOf(r.getId()));
                    Messaggio resp = sessione.getConnessione().invia(req);
                    if (resp.isOk() && resp.getDatiJson() != null && !resp.getDatiJson().isBlank())
                        tutte.addAll(Arrays.asList(new Gson().fromJson(resp.getDatiJson(), Recensione[].class)));
                }
                return new java.util.AbstractMap.SimpleEntry<>(tutte, ristoranti);
            }
            @Override protected void done() {
                btnAggRecGestore.setEnabled(true);
                try {
                    java.util.AbstractMap.SimpleEntry<List<Recensione>, List<Ristorante>> x = get();
                    recensioniGestore = x.getKey();
                    ristorantiGestorePerRecensioni = x.getValue();
                    modelRecGestore.setRowCount(0);
                    for (int i = 0; i < recensioniGestore.size(); i++) {
                        Recensione rec = recensioniGestore.get(i);
                        String nome = nomeRistoranteGestore(rec.getRistoranteId());
                        String testo = rec.getTesto() == null ? "" : rec.getTesto();
                        if (testo.length() > 60) testo = testo.substring(0, 57) + "...";
                        modelRecGestore.addRow(new Object[]{i + 1, nome, rec.getUsernameAutore(),
                                rec.getStelle() + " / 5", testo, rec.getDataRecensione(),
                                rec.haRisposta() ? rec.getRispostaGestore() : "Nessuna risposta"});
                    }
                    lblRecGestore.setText(recensioniGestore.size() + " recensioni");
                } catch (Exception ex) { lblRecGestore.setText("Errore: " + ex.getMessage()); }
                ripristinaCursoreNormale();
            }
        }.execute();
    }

    /**
     * Restituisce il nome del ristorante corrispondente all'id indicato,
     * cercandolo nell'elenco {@link #ristorantiGestorePerRecensioni}
     * caricato in precedenza da {@link #caricaRecensioniGestore()}.
     *
     * @param id identificativo del ristorante da cercare
     * @return il nome del ristorante se trovato, altrimenti la stringa
     *         generica {@code "Ristorante"}
     */
    private String nomeRistoranteGestore(int id) {
        if (ristorantiGestorePerRecensioni != null)
            for (Ristorante r : ristorantiGestorePerRecensioni) if (r.getId() == id) return r.getNome();
        return "Ristorante";
    }

    /**
     * Recupera la recensione selezionata nella tabella delle recensioni
     * ricevute dal gestore e apre il form di risposta tramite
     * {@link #apriFormRispostaGestore(Recensione)}. Se non è selezionata
     * alcuna riga, mostra un messaggio informativo all'utente.
     */
    private void rispondiRecensioneGestoreSelezionata() {
        int vRow = tabellaRecGestore.getSelectedRow();
        if (vRow < 0 || recensioniGestore == null) { mostraInfo("Seleziona una recensione");ripristinaCursoreNormale(); return; }
        Recensione rec = recensioniGestore.get(tabellaRecGestore.convertRowIndexToModel(vRow));
        apriFormRispostaGestore(rec);
    }

    /**
     * Apre una finestra di dialogo modale che permette al gestore di
     * scrivere o modificare la risposta a una recensione. Se la recensione
     * ha già una risposta, l'area di testo viene precompilata con il testo
     * esistente. Al salvataggio invia in background la richiesta
     * {@code OP_RISPONDI_REC} al server e, in caso di successo, chiude il
     * dialogo e ricarica l'elenco delle recensioni tramite
     * {@link #caricaRecensioniGestore()}.
     *
     * @param rec la recensione a cui rispondere o di cui modificare la risposta
     */
    private void apriFormRispostaGestore(Recensione rec) {
    	mostraCursoreCaricamento();
        JDialog form = new JDialog(this, rec.haRisposta() ? "Modifica risposta" : "Risposta alla recensione", true);
        form.setSize(460, 300); form.setLocationRelativeTo(this);
        form.getContentPane().setBackground(UI.SFONDO_SCURO);
        form.setLayout(new BorderLayout());
        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setBackground(UI.SFONDO_SCURO);
        body.setBorder(new EmptyBorder(20, 24, 16, 24));
        JTextArea area = UI.creaTextArea("Scrivi la tua risposta...");
        if (rec.haRisposta()) area.setText(rec.getRispostaGestore());
        body.add(area, BorderLayout.CENTER);
        form.add(body, BorderLayout.CENTER);
        JLabel stato = UI.creaLabelInfo(" ");
        JButton salva = UI.creaBottone(rec.haRisposta() ? "Salva modifica" : "Pubblica risposta", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
        salva.addActionListener(e -> {
            String risposta = area.getText().trim();
            if (risposta.isEmpty()) { UI.aggiornaErroreForm(stato, "Scrivi una risposta"); return; }
            salva.setEnabled(false);
            new SwingWorker<Boolean,Void>() {
                @Override protected Boolean doInBackground() throws Exception {
                	mostraCursoreCaricamento();
                    Messaggio req = new Messaggio(Messaggio.OP_RISPONDI_REC);
                    req.addParam("recensioneId", String.valueOf(rec.getId()));
                    req.addParam("risposta", risposta);
                    Messaggio resp = sessione.getConnessione().invia(req);
                    if (!resp.isOk()) throw new Exception(resp.getParam("errore"));
                    return true;
                }
                @Override protected void done() {
                    try { get(); form.dispose(); caricaRecensioniGestore(); }
                    catch (Exception ex) { UI.aggiornaErroreForm(stato, ex.getMessage()); salva.setEnabled(true); }  ripristinaCursoreNormale();
                }
            }.execute();
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        footer.setBackground(new Color(0x111113));
        footer.add(stato); footer.add(salva);
        JButton annulla = UI.creaBottoneSmall("Annulla", new Color(0x3A3A3C));
        annulla.addActionListener(e -> form.dispose());
        footer.add(annulla);
        form.add(footer, BorderLayout.SOUTH);
        form.setVisible(true);
        ripristinaCursoreNormale();
    }

    /**
     * Rimuove dai preferiti il ristorante selezionato nella tabella
     * {@link #tabellaPreferiti}, inviando in background la richiesta
     * {@code OP_RIMUOVI_PREF} al server e, in caso di successo, rimuovendo
     * la riga corrispondente dalla tabella. Se non è selezionata alcuna
     * riga, mostra un messaggio informativo all'utente.
     */
    private void rimuoviPreferito() {
    	mostraCursoreCaricamento();
        int vRow = tabellaPreferiti.getSelectedRow();
        if (vRow < 0) { mostraInfo("Seleziona un ristorante da rimuovere"); ripristinaCursoreNormale();return; }
        int row = tabellaPreferiti.convertRowIndexToModel(vRow);
        Ristorante r = ristoranteDaRiga(modelPreferiti, row);
        if (r == null) return;
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                Messaggio req = new Messaggio(Messaggio.OP_RIMUOVI_PREF);
                req.addParam("ristoranteId", String.valueOf(r.getId()));
                return sessione.getConnessione().invia(req).isOk();
            }
            @Override protected void done() {
                try { if (get()) modelPreferiti.removeRow(row); else mostraErrore("Impossibile rimuovere"); }
                catch (Exception ex) { mostraErrore(ex.getMessage()); }  ripristinaCursoreNormale();
            }
        }.execute();
    }

    /**
     * Apre una finestra di dialogo per modificare la recensione selezionata
     * nella tabella {@link #tabellaMieRec}, precompilata con stelle e testo
     * correnti. Se l'utente conferma, invia in background la richiesta
     * {@code OP_MODIFICA_REC} al server e, in caso di successo, ricarica
     * l'elenco delle recensioni tramite {@link #caricaMieRecensioni()}. Se
     * non è selezionata alcuna riga, mostra un messaggio informativo
     * all'utente.
     */
    private void modificaRecensione() {
    	mostraCursoreCaricamento();
        int vRow = tabellaMieRec.getSelectedRow();
        if (vRow < 0 || mieRecensioni == null) { mostraInfo("Seleziona una recensione");ripristinaCursoreNormale(); return; }
        Recensione rec = mieRecensioni.get(tabellaMieRec.convertRowIndexToModel(vRow));
        JSpinner spStelle = new JSpinner(new SpinnerNumberModel(rec.getStelle(), 1, 5, 1));
        JTextArea area = UI.creaTextArea(rec.getTesto() != null ? rec.getTesto() : "");
        area.setText(rec.getTesto() != null ? rec.getTesto() : "");
        UI.stilizzaSpinner(spStelle);
        JPanel form = new JPanel(new BorderLayout(0,8));
        form.setBackground(UI.SFONDO_SCURO);
        form.add(UI.creaRigaFiltro("Stelle:", spStelle), BorderLayout.NORTH);
        form.add(new JScrollPane(area), BorderLayout.CENTER);
        int r = JOptionPane.showConfirmDialog(this, form, "Modifica recensione",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (r != JOptionPane.OK_OPTION) {ripristinaCursoreNormale();return;}
        new SwingWorker<Boolean,Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                Messaggio req = new Messaggio(Messaggio.OP_MODIFICA_REC);
                req.addParam("recensioneId", String.valueOf(rec.getId()));
                req.addParam("stelle",       String.valueOf(spStelle.getValue()));
                req.addParam("testo",        area.getText().trim());
                return sessione.getConnessione().invia(req).isOk();
            }
            @Override protected void done() {
                try { if (get()) caricaMieRecensioni(); else mostraErrore("Impossibile modificare");ripristinaCursoreNormale(); }
                catch (Exception ex) { mostraErrore(ex.getMessage()); }  ripristinaCursoreNormale();
            }
        }.execute();
    }

    /**
     * Elimina la recensione selezionata nella tabella {@link #tabellaMieRec},
     * previa conferma dell'utente tramite una finestra di dialogo. Invia in
     * background la richiesta {@code OP_ELIMINA_REC} al server e, in caso di
     * successo, ricarica l'elenco delle recensioni tramite
     * {@link #caricaMieRecensioni()}. Se non è selezionata alcuna riga,
     * mostra un messaggio informativo all'utente.
     */
    private void eliminaRecensione() {
    	mostraCursoreCaricamento();
        int vRow = tabellaMieRec.getSelectedRow();
        if (vRow < 0 || mieRecensioni == null) { mostraInfo("Seleziona una recensione"); ripristinaCursoreNormale();return; }
        Recensione rec = mieRecensioni.get(tabellaMieRec.convertRowIndexToModel(vRow));
        if (JOptionPane.showConfirmDialog(this, "Eliminare questa recensione?", "Conferma",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {ripristinaCursoreNormale();return;}
        new SwingWorker<Boolean,Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                Messaggio req = new Messaggio(Messaggio.OP_ELIMINA_REC);
                req.addParam("recensioneId", String.valueOf(rec.getId()));
                return sessione.getConnessione().invia(req).isOk();
            }
            @Override protected void done() {
                try { if (get()) caricaMieRecensioni(); else mostraErrore("Impossibile eliminare"); }
                catch (Exception ex) { mostraErrore(ex.getMessage()); }  ripristinaCursoreNormale();
            }
        }.execute();
    }

    // ── Popolamento tabelle ───────────────────────────────────────────────

    /**
     * Svuota il modello di tabella indicato e lo ripopola con i dati dei
     * ristoranti forniti (oggetto ristorante nella colonna nascosta, nome,
     * località, tipo di cucina, fascia di prezzo, valutazione media, numero
     * di recensioni, disponibilità di delivery e di prenotazione online).
     *
     * @param m     modello della tabella da svuotare e ripopolare
     * @param lista elenco dei ristoranti da inserire nella tabella
     */
    private void popolaTabella(DefaultTableModel m, List<Ristorante> lista) {
    	mostraCursoreCaricamento();
        m.setRowCount(0);
        for (Ristorante r : lista)
        	m.addRow(new Object[]{r, r.getNome(), r.getLocation(), r.getTipoCucina(),
        	        r.getFasciaPrezzo(), voto(r.getMediaVoto()), r.getNumeroRecensioni(),
        	        r.isDelivery() ? "Sì" : "No", r.isPrenotazioneOnline() ? "Sì" : "No"});
        ripristinaCursoreNormale();
    }

    /**
     * Recupera l'oggetto {@link Ristorante} memorizzato nella colonna
     * nascosta (indice 0) della riga indicata di un modello di tabella.
     *
     * @param m   modello della tabella da cui leggere
     * @param row indice di riga (nel modello, non nella vista) da cui leggere
     * @return il ristorante associato alla riga, oppure {@code null} se
     *         l'indice non è valido o la cella non contiene un {@link Ristorante}
     */
    private Ristorante ristoranteDaRiga(DefaultTableModel m, int row) {
    	mostraCursoreCaricamento();
        if (row < 0) return null;
        Object cella = m.getValueAt(row, 0);
        ripristinaCursoreNormale();
        return cella instanceof Ristorante ? (Ristorante) cella : null;
    }
    
    /**
     * Crea un {@link MouseAdapter} da associare a una tabella che, al doppio
     * click su una riga, apre la finestra di dettaglio ({@link DettaglioDialog})
     * del ristorante corrispondente.
     *
     * @param tabella la tabella su cui intercettare il doppio click
     * @param modello il modello associato alla tabella, usato per risolvere
     *                il ristorante della riga cliccata
     * @return il listener da registrare sulla tabella
     */
    private MouseAdapter doppioClic(JTable tabella, DefaultTableModel modello) {
        return new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tabella.convertRowIndexToModel(tabella.rowAtPoint(e.getPoint()));
                    Ristorante r = ristoranteDaRiga(modello, row);
                    if (r != null) new DettaglioDialog(HomeFrame.this, r).setVisible(true);
                }
            }
        };
    }
    
    /**
     * Imposta il cursore della finestra sul cursore di attesa, per segnalare
     * visivamente un'operazione in corso (tipicamente una chiamata di rete
     * in background).
     */
    private void mostraCursoreCaricamento() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
     * Ripristina il cursore predefinito della finestra al termine di
     * un'operazione avviata con {@link #mostraCursoreCaricamento()}.
     */
    private void ripristinaCursoreNormale() {
        setCursor(Cursor.getDefaultCursor());
    }


    // ── Utility ───────────────────────────────────────────────────────────

    /**
     * Formatta una valutazione media come stringa nel formato {@code "X.X / 5"}.
     *
     * @param m valutazione media da formattare
     * @return la stringa formattata, con un decimale, seguita da {@code " / 5"}
     */
    private String voto(double m) { return String.format("%.1f / 5", m); }

    /**
     * Mostra una finestra di dialogo informativa con il messaggio indicato.
     *
     * @param msg testo del messaggio da mostrare
     */
    private void mostraInfo(String msg)   { JOptionPane.showMessageDialog(this, msg, "Info",   JOptionPane.INFORMATION_MESSAGE); }

    /**
     * Mostra una finestra di dialogo di errore con il messaggio indicato.
     *
     * @param msg testo del messaggio di errore da mostrare
     */
    private void mostraErrore(String msg) { JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE); }
       
    }
