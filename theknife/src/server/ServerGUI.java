package theknife.server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interfaccia grafica del server TheKnife.
 * Mostra log in tempo reale, numero di client connessi,
 * stato del database e permette lo spegnimento controllato del server.
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class ServerGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // ── Colori tema (coerente con il client) ──────────────────────────────────
    private static final Color SFONDO_SCURO   = new Color(0x1C1C1E);
    private static final Color SFONDO_PANEL   = new Color(0x2C2C2E);
    private static final Color SFONDO_CARD    = new Color(0x3A3A3C);
    private static final Color ROSSO_KNIFE    = new Color(0xC0392B);
    private static final Color VERDE_OK       = new Color(0x2ECC71);
    private static final Color GIALLO_WARN    = new Color(0xF39C12);
    private static final Color TESTO_CHIARO   = new Color(0xF2F2F7);
    private static final Color TESTO_GRIGIO   = new Color(0x8E8E93);
    private static final Color BORDO          = new Color(0x48484A);

    private static final Font FONT_MONO  = new Font("Courier New", Font.PLAIN, 13);
    private static final Font FONT_UI    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    // ── Stato runtime ─────────────────────────────────────────────────────────
    private ServerSocket       serverSocket;
    private Thread             acceptThread;
    private volatile boolean   running = false;
    private final AtomicInteger clientiConnessi = new AtomicInteger(0);

    // ── Componenti UI ─────────────────────────────────────────────────────────
    private JTextPane  logPane;
    private StyledDocument logDoc;

    private JLabel lblStato;
    private JLabel lblDB;
    private JLabel lblClienti;
    private JLabel lblPorta;

    private JButton btnAvvia;
    private JButton btnFerma;
    private JButton btnPulisci;

    private JTextField fHost;
    private JTextField fDB;
    private JTextField fUser;
    private JPasswordField fPass;
    private JTextField fPorta;

    // ── Singleton access per il logger ────────────────────────────────────────
    private static ServerGUI instance;
    /** @return l'istanza corrente della GUI, utile per l'accesso da parte del logger */
    public static ServerGUI getInstance() { return instance; }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Costruisce e inizializza la finestra principale del server:
     * layout, pannelli, listener di chiusura e messaggi di log iniziali.
     */
    public ServerGUI() {
        instance = this;
        setTitle("TheKnife — Server Control Panel");
        setSize(820, 640);
        setMinimumSize(new Dimension(700, 500));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(SFONDO_SCURO);
        setLayout(new BorderLayout(0, 0));

        add(creaHeader(),    BorderLayout.NORTH);
        add(creaCorpo(),     BorderLayout.CENTER);
        add(creaStatusBar(), BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                chiudiApplicazione();
            }
        });

        log("SERVER", "TheKnife Server Control Panel avviato.", LogTipo.INFO);
        log("SERVER", "Inserisci le credenziali e premi [Avvia Server].", LogTipo.INFO);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    /**
     * Costruisce il pannello di intestazione con logo, titolo e contatore
     * dei client connessi.
     *
     * @return pannello dell'header, comprensivo di separatore inferiore
     */
    private JPanel creaHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(0x111113));
        h.setBorder(new EmptyBorder(16, 24, 14, 24));

        // Logo + titolo
        JLabel titolo = new JLabel("TheKnife Server");
        titolo.setFont(FONT_TITLE);
        titolo.setForeground(ROSSO_KNIFE);

        JLabel sottotitolo = new JLabel("Control Panel  •  Laboratorio Interdisciplinare B");
        sottotitolo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sottotitolo.setForeground(TESTO_GRIGIO);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(titolo);
        left.add(Box.createVerticalStrut(2));
        left.add(sottotitolo);

        // Contatore clienti connessi (in evidenza nell'header)
        lblClienti = new JLabel("0 client");
        lblClienti.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblClienti.setForeground(TESTO_GRIGIO);
        lblClienti.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblClientiCaption = new JLabel("connessi");
        lblClientiCaption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblClientiCaption.setForeground(TESTO_GRIGIO);
        lblClientiCaption.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(lblClienti);
        right.add(lblClientiCaption);

        h.add(left,  BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDO);
        sep.setBackground(BORDO);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(0x111113));
        wrapper.add(h,   BorderLayout.CENTER);
        wrapper.add(sep, BorderLayout.SOUTH);
        return wrapper;
    }

    // ── Corpo principale ──────────────────────────────────────────────────────

    /**
     * Costruisce il corpo principale della finestra, suddiviso in un
     * pannello di configurazione a sinistra e il pannello di log a destra.
     *
     * @return split pane con configurazione e log
     */
    private JSplitPane creaCorpo() {
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                creaConfigurazione(),
                creaLogPanel()
        );
        split.setDividerLocation(280);
        split.setDividerSize(4);
        split.setBackground(SFONDO_SCURO);
        split.setBorder(null);
        split.setResizeWeight(0.0);
        return split;
    }

    // ── Pannello configurazione (sinistra) ────────────────────────────────────

    /**
     * Costruisce il pannello di configurazione: campi per database e rete,
     * indicatori di stato e pulsanti di avvio/arresto/pulizia log.
     *
     * @return pannello di configurazione
     */
    private JPanel creaConfigurazione() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(SFONDO_SCURO);
        p.setBorder(new EmptyBorder(16, 16, 16, 10));

        // ── Card DB ──
        JPanel cardDB = creaCard("DATABASE");

        fHost = creaField("localhost");
        fDB   = creaField("theknife_db");
        fUser = creaField("postgres");
        fPass = new JPasswordField("kebab");
        stilizzaField(fPass);

        cardDB.add(creaRiga("Host",     fHost));
        cardDB.add(Box.createVerticalStrut(8));
        cardDB.add(creaRiga("Database", fDB));
        cardDB.add(Box.createVerticalStrut(8));
        cardDB.add(creaRiga("Utente",   fUser));
        cardDB.add(Box.createVerticalStrut(8));
        cardDB.add(creaRiga("Password", fPass));

        // ── Card Rete ──
        JPanel cardNet = creaCard("RETE");
        fPorta = creaField("12345");
        cardNet.add(creaRiga("Porta TCP", fPorta));

        // ── Card Stato ──
        JPanel cardStato = creaCard("STATO");
        lblDB    = statoLabel("DB:     Non connesso", TESTO_GRIGIO);
        lblPorta = statoLabel("Porta:  —",            TESTO_GRIGIO);
        lblStato = statoLabel("Server fermo",      TESTO_GRIGIO);
        cardStato.add(lblStato);
        cardStato.add(Box.createVerticalStrut(6));
        cardStato.add(lblDB);
        cardStato.add(Box.createVerticalStrut(6));
        cardStato.add(lblPorta);

        // ── Bottoni ──
        btnAvvia  = creaBottone("Avvia Server",  ROSSO_KNIFE,            new Color(0xA93226));
        btnFerma  = creaBottone("Ferma Server",  new Color(0x3A3A3C),    new Color(0x48484A));
        btnPulisci = creaBottone("Pulisci log",  new Color(0x2C2C2E),   new Color(0x3A3A3C));
        btnFerma.setEnabled(false);

        btnAvvia.addActionListener(e -> avviaServer());
        btnFerma.addActionListener(e -> fermaServer());
        btnPulisci.addActionListener(e -> pulisciLog());

        p.add(cardDB);
        p.add(Box.createVerticalStrut(12));
        p.add(cardNet);
        p.add(Box.createVerticalStrut(12));
        p.add(cardStato);
        p.add(Box.createVerticalStrut(16));
        p.add(btnAvvia);
        p.add(Box.createVerticalStrut(8));
        p.add(btnFerma);
        p.add(Box.createVerticalStrut(8));
        p.add(btnPulisci);
        p.add(Box.createVerticalGlue());

        return p;
    }

    // ── Pannello log (destra) ─────────────────────────────────────────────────

    /**
     * Costruisce il pannello di log in tempo reale, con area di testo
     * stilizzata e scorrimento automatico.
     *
     * @return pannello di log
     */
    private JPanel creaLogPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SFONDO_SCURO);
        p.setBorder(new EmptyBorder(16, 10, 16, 16));

        JLabel titolo = new JLabel("LOG IN TEMPO REALE");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titolo.setForeground(TESTO_GRIGIO);
        titolo.setBorder(new EmptyBorder(0, 4, 8, 0));

        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setBackground(new Color(0x111113));
        logPane.setFont(FONT_MONO);
        logDoc = logPane.getStyledDocument();

        JScrollPane scroll = new JScrollPane(logPane);
        scroll.setBorder(BorderFactory.createLineBorder(BORDO, 1));
        scroll.getViewport().setBackground(new Color(0x111113));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        p.add(titolo, BorderLayout.NORTH);
        p.add(scroll,  BorderLayout.CENTER);
        return p;
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    /**
     * Costruisce la barra di stato inferiore con le informazioni di copyright.
     *
     * @return pannello della status bar
     */
    private JPanel creaStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        bar.setBackground(new Color(0x111113));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDO));

        JLabel copy = new JLabel("TheKnife Server  •  Lab. Interdisciplinare B  •  a.a. 2025/2026");
        copy.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        copy.setForeground(TESTO_GRIGIO);
        bar.add(copy);
        return bar;
    }

    // ── Logica server ─────────────────────────────────────────────────────────

    /**
     * Legge i parametri di configurazione inseriti dall'utente, valida i
     * campi obbligatori e avvia in background la connessione al database,
     * seguita (in caso di successo) dall'apertura della porta di ascolto.
     */
    private void avviaServer() {
        String host  = fHost.getText().trim();
        String db    = fDB.getText().trim();
        String user  = fUser.getText().trim();
        String pass  = new String(fPass.getPassword());
        int    porta;

        try {
            porta = Integer.parseInt(fPorta.getText().trim());
        } catch (NumberFormatException e) {
            log("CONFIG", "Porta non valida — deve essere un numero intero.", LogTipo.ERRORE);
            return;
        }

        if (host.isEmpty() || db.isEmpty() || user.isEmpty()) {
            log("CONFIG", "Host, database e utente sono obbligatori.", LogTipo.ERRORE);
            return;
        }

        btnAvvia.setEnabled(false);
        setInputsEnabled(false);
        log("DB", "Connessione a PostgreSQL su " + host + "/" + db + " ...", LogTipo.INFO);

        // Connessione DB in background
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            Exception errore;
            @Override
            protected Void doInBackground() {
                try {
                    DatabaseManager.getInstance().connetti(host, db, user, pass);
                } catch (Exception e) {
                    errore = e;
                }
                return null;
            }
            @Override
            protected void done() {
                if (errore != null) {
                    log("DB", "Connessione fallita: " + errore.getMessage(), LogTipo.ERRORE);
                    aggiornaStatoDB(false, "");
                    btnAvvia.setEnabled(true);
                    setInputsEnabled(true);
                    return;
                }
                log("DB", "Connessione al database riuscita.", LogTipo.OK);
                aggiornaStatoDB(true, host + "/" + db);
                avviaAccettazione(porta);
            }
        };
        worker.execute();
    }

    /**
     * Apre la {@link ServerSocket} sulla porta indicata e avvia il thread
     * di accettazione delle connessioni client, ciascuna delegata a un
     * nuovo {@link ClientHandler} eseguito su thread daemon dedicato.
     *
     * @param porta porta TCP su cui mettersi in ascolto
     */
    private void avviaAccettazione(int porta) {
        try {
            serverSocket = new ServerSocket(porta);
        } catch (IOException e) {
            log("RETE", "Impossibile aprire la porta " + porta + ": " + e.getMessage(), LogTipo.ERRORE);
            DatabaseManager.getInstance().disconnetti();
            aggiornaStatoDB(false, "");
            btnAvvia.setEnabled(true);
            setInputsEnabled(true);
            return;
        }

        running = true;
        aggiornaStatoServer(true, porta);
        log("RETE", "Server in ascolto sulla porta " + porta, LogTipo.OK);

        acceptThread = new Thread(() -> {
            while (running) {
                try {
                    Socket client = serverSocket.accept();
                    int n = clientiConnessi.incrementAndGet();
                    String ip = client.getInetAddress().getHostAddress();
                    log("CLIENT", "Connessione da " + ip + "  [connessi: " + n + "]", LogTipo.CLIENT);
                    aggiornaContatoreClienti(n);

                    Thread t = new Thread(new ClientHandler(client, ip, new ClientHandler.ServerLogger() {
                            @Override public void logOperazione(String ip, String op) {
                                ServerGUI.this.logOperazione(ip, op);
                            }
                            @Override public void logErrore(String ip, String msg) {
                                ServerGUI.this.log(ip, msg, LogTipo.ERRORE);
                            }
                            @Override public void clientDisconnesso(String ip) {
                                ServerGUI.this.clientDisconnesso(ip);
                            }
                        }));
                    t.setDaemon(true);
                    t.start();
                } catch (IOException e) {
                    if (running) {
                        log("RETE", "Errore accept: " + e.getMessage(), LogTipo.ERRORE);
                    }
                }
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    /**
     * Chiede conferma all'utente e, in caso affermativo, ferma il server.
     */
    private void fermaServer() {
        int scelta = JOptionPane.showConfirmDialog(
                this,
                "Fermare il server disconnetterà tutti i client attivi.\nContinuare?",
                "Conferma spegnimento",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (scelta != JOptionPane.YES_OPTION) return;

        spegniServer();
    }

    /**
     * Ferma il server: chiude il {@link ServerSocket}, disconnette il
     * database e ripristina lo stato iniziale dell'interfaccia.
     */
    private void spegniServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        DatabaseManager.getInstance().disconnetti();

        aggiornaStatoServer(false, 0);
        aggiornaStatoDB(false, "");
        aggiornaContatoreClienti(0);
        btnAvvia.setEnabled(true);
        btnFerma.setEnabled(false);
        setInputsEnabled(true);

        log("SERVER", "Server fermato correttamente.", LogTipo.WARN);
    }

    /**
     * Gestisce la chiusura dell'applicazione: se il server è attivo chiede
     * conferma all'utente prima di fermarlo e uscire.
     */
    private void chiudiApplicazione() {
        if (running) {
            int scelta = JOptionPane.showConfirmDialog(
                    this,
                    "Il server è ancora in esecuzione.\nSpegnerlo e uscire?",
                    "Esci",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (scelta != JOptionPane.YES_OPTION) return;
            spegniServer();
        }
        dispose();
        System.exit(0);
    }

    /**
     * Chiamato da ClientHandler quando un client si disconnette.
     *
     * @param ip indirizzo IP del client disconnesso
     */
    public void clientDisconnesso(String ip) {
        int n = clientiConnessi.decrementAndGet();
        SwingUtilities.invokeLater(() -> {
            log("CLIENT", "Disconnessione da " + ip + "  [connessi: " + n + "]", LogTipo.WARN);
            aggiornaContatoreClienti(n);
        });
    }

    /**
     * Chiamato da ClientHandler per ogni operazione ricevuta.
     *
     * @param ip indirizzo IP del client
     * @param operazione nome dell'operazione ricevuta
     */
    public void logOperazione(String ip, String operazione) {
        SwingUtilities.invokeLater(() ->
            log(ip, "→ " + operazione, LogTipo.OP)
        );
    }

    // ── Aggiornamenti UI (sempre su EDT) ──────────────────────────────────────

    /**
     * Aggiorna l'etichetta di stato del database.
     *
     * @param ok {@code true} se la connessione è attiva
     * @param info testo informativo da mostrare (es. host/database) quando {@code ok} è vero
     */
    private void aggiornaStatoDB(boolean ok, String info) {
        SwingUtilities.invokeLater(() -> {
            if (ok) {
                lblDB.setText("DB:     " + info);
                lblDB.setForeground(VERDE_OK);
            } else {
                lblDB.setText("DB:     Non connesso");
                lblDB.setForeground(TESTO_GRIGIO);
            }
        });
    }

    /**
     * Aggiorna le etichette di stato del server e della porta, e lo stato
     * abilitato del pulsante di arresto.
     *
     * @param on {@code true} se il server è attivo
     * @param porta porta su cui il server è in ascolto (ignorata se {@code on} è falso)
     */
    private void aggiornaStatoServer(boolean on, int porta) {
        SwingUtilities.invokeLater(() -> {
            if (on) {
                lblStato.setText("Server attivo");
                lblStato.setForeground(VERDE_OK);
                lblPorta.setText("Porta:  " + porta);
                lblPorta.setForeground(TESTO_CHIARO);
                btnFerma.setEnabled(true);
            } else {
                lblStato.setText("Server fermo");
                lblStato.setForeground(TESTO_GRIGIO);
                lblPorta.setText("Porta:  —");
                lblPorta.setForeground(TESTO_GRIGIO);
                btnFerma.setEnabled(false);
            }
        });
    }

    /**
     * Aggiorna il contatore dei client connessi mostrato nell'header.
     *
     * @param n numero di client attualmente connessi
     */
    private void aggiornaContatoreClienti(int n) {
        SwingUtilities.invokeLater(() -> {
            lblClienti.setText(String.valueOf(n));
            lblClienti.setForeground(n > 0 ? VERDE_OK : TESTO_GRIGIO);
        });
    }

    // ── Log colorato ──────────────────────────────────────────────────────────

    /** Categorie di log supportate, ciascuna con un proprio colore. */
    public enum LogTipo { INFO, OK, ERRORE, WARN, CLIENT, OP }

    /**
     * Aggiunge una riga colorata al pannello di log, con timestamp, fonte e
     * messaggio, effettuando lo scroll automatico in fondo.
     *
     * @param fonte etichetta della fonte del log (es. IP client, "SERVER", "DB")
     * @param messaggio testo del messaggio da registrare
     * @param tipo categoria del log, che ne determina il colore
     */
    public void log(String fonte, String messaggio, LogTipo tipo) {
        SwingUtilities.invokeLater(() -> {
            try {
                String ora = LocalTime.now().format(TIME_FMT);

                // Stile timestamp
                Style sTime = logPane.addStyle("time", null);
                StyleConstants.setForeground(sTime, TESTO_GRIGIO);
                StyleConstants.setFontFamily(sTime, "Courier New");
                StyleConstants.setFontSize(sTime, 12);

                // Stile fonte
                Style sFonte = logPane.addStyle("fonte", null);
                StyleConstants.setForeground(sFonte, coloreTag(tipo));
                StyleConstants.setBold(sFonte, true);
                StyleConstants.setFontFamily(sFonte, "Courier New");
                StyleConstants.setFontSize(sFonte, 12);

                // Stile messaggio
                Style sMsg = logPane.addStyle("msg", null);
                StyleConstants.setForeground(sMsg, coloreMessaggio(tipo));
                StyleConstants.setFontFamily(sMsg, "Courier New");
                StyleConstants.setFontSize(sMsg, 12);

                logDoc.insertString(logDoc.getLength(), ora + "  ", sTime);
                String tag = String.format("%-8s", "[" + fonte + "]");
                logDoc.insertString(logDoc.getLength(), tag + "  ", sFonte);
                logDoc.insertString(logDoc.getLength(), messaggio + "\n", sMsg);

                // Auto-scroll
                logPane.setCaretPosition(logDoc.getLength());
            } catch (BadLocationException ignored) {}
        });
    }

    /**
     * Determina il colore del tag/fonte in base al tipo di log.
     *
     * @param t categoria del log
     * @return colore associato al tag
     */
    private Color coloreTag(LogTipo t) {
        return switch (t) {
            case OK     -> VERDE_OK;
            case ERRORE -> ROSSO_KNIFE;
            case WARN   -> GIALLO_WARN;
            case CLIENT -> new Color(0x5AC8FA);
            case OP     -> new Color(0xBF5AF2);
            default     -> TESTO_GRIGIO;
        };
    }

    /**
     * Determina il colore del testo del messaggio in base al tipo di log.
     *
     * @param t categoria del log
     * @return colore associato al messaggio
     */
    private Color coloreMessaggio(LogTipo t) {
        return switch (t) {
            case ERRORE -> new Color(0xFF6B6B);
            case WARN   -> new Color(0xFFD166);
            default     -> TESTO_CHIARO;
        };
    }

    /**
     * Svuota il contenuto del pannello di log.
     */
    private void pulisciLog() {
        try {
            logDoc.remove(0, logDoc.getLength());
            log("SERVER", "Log pulito.", LogTipo.INFO);
        } catch (BadLocationException ignored) {}
    }

    // ── Helper costruzione UI ─────────────────────────────────────────────────

    /**
     * Costruisce una "card" con titolo, usata per raggruppare visivamente
     * un insieme di controlli correlati.
     *
     * @param titolo titolo della card, mostrato in alto
     * @return pannello della card, pronto per l'aggiunta di componenti
     */
    private JPanel creaCard(String titolo) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SFONDO_PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDO, 1),
                new EmptyBorder(12, 14, 14, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel lbl = new JLabel(titolo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TESTO_GRIGIO);
        lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
        card.add(lbl);
        return card;
    }

    /**
     * Crea un campo di testo stilizzato con testo iniziale.
     *
     * @param placeholder testo iniziale del campo
     * @return campo di testo stilizzato
     */
    private JTextField creaField(String placeholder) {
        JTextField f = new JTextField(placeholder);
        stilizzaField(f);
        return f;
    }

    /**
     * Applica lo stile grafico standard (colori, font, bordo) a un campo di testo.
     *
     * @param f campo di testo da stilizzare
     */
    private void stilizzaField(JTextField f) {
        f.setBackground(SFONDO_CARD);
        f.setForeground(TESTO_CHIARO);
        f.setCaretColor(TESTO_CHIARO);
        f.setFont(FONT_UI);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDO, 1),
                new EmptyBorder(5, 8, 5, 8)
        ));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
    }

    /**
     * Costruisce una riga etichetta + campo, usata all'interno delle card
     * di configurazione.
     *
     * @param etichetta testo dell'etichetta a sinistra
     * @param campo componente di input da affiancare all'etichetta
     * @return pannello contenente etichetta e campo
     */
    private JPanel creaRiga(String etichetta, JComponent campo) {
        JPanel r = new JPanel(new BorderLayout(6, 0));
        r.setOpaque(false);
        r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel lbl = new JLabel(etichetta);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TESTO_GRIGIO);
        lbl.setPreferredSize(new Dimension(70, 28));

        r.add(lbl,   BorderLayout.WEST);
        r.add(campo, BorderLayout.CENTER);
        return r;
    }

    /**
     * Crea un'etichetta di stato con font monospaziato e colore indicato.
     *
     * @param testo testo iniziale dell'etichetta
     * @param colore colore del testo
     * @return etichetta di stato
     */
    private JLabel statoLabel(String testo, Color colore) {
        JLabel l = new JLabel(testo);
        l.setFont(FONT_MONO);
        l.setForeground(colore);
        return l;
    }

    /**
     * Crea un pulsante stilizzato con effetto hover.
     *
     * @param testo etichetta del pulsante
     * @param bg colore di sfondo normale
     * @param hover colore di sfondo al passaggio del mouse
     * @return pulsante stilizzato
     */
    private JButton creaBottone(String testo, Color bg, Color hover) {
        JButton b = new JButton(testo);
        b.setFont(FONT_BOLD);
        b.setForeground(TESTO_CHIARO);
        b.setBackground(bg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (b.isEnabled()) b.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { if (b.isEnabled()) b.setBackground(bg); }
        });
        return b;
    }

    /**
     * Abilita o disabilita tutti i campi di configurazione (database e rete),
     * usato durante l'avvio/arresto del server per evitare modifiche concorrenti.
     *
     * @param enabled {@code true} per abilitare i campi, {@code false} per disabilitarli
     */
    private void setInputsEnabled(boolean enabled) {
        fHost.setEnabled(enabled);
        fDB.setEnabled(enabled);
        fUser.setEnabled(enabled);
        fPass.setEnabled(enabled);
        fPorta.setEnabled(enabled);
    }
}
