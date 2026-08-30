package theknife.gui;

import theknife.client.ClientTK;
import theknife.common.Messaggio;
import theknife.common.Utente;
import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;

/**
 * Schermata iniziale: login, registrazione e accesso ospite in un'unica finestra.
 * CardLayout interno: CARD_LOGIN ↔ CARD_REGISTRA.
 * RegistrazioneFrame è stata assorbita qui per ridurre il numero di classi.
 *
 * Flusso:
 *  1. Connessione automatica a localhost:12345 all'avvio.
 *  2. Se connessa: form attivo. Se fallisce: messaggio errore.
 *  3. Login → HomeFrame. Registrazione → login automatico → HomeFrame. Guest → HomeFrame senza utente.
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class LoginFrame extends JFrame {

    private static final long   serialVersionUID = 1L;
    private static final String HOST             = "localhost";
    private static final int    PORTA            = 12345;
    private static final String CARD_LOGIN       = "LOGIN";
    private static final String CARD_REGISTRA    = "REGISTRA";

    private final CardLayout cards     = new CardLayout();
    private final JPanel     cardPanel = new JPanel(cards);


    // ── Campi LOGIN ──────────────────────────────────────────────────────────
    private final JTextField     fUser   = UI.creaInput("Username o e-mail");
    private final JPasswordField fPass   = UI.creaInputPassword("Password");
    private final JButton        btnLogin    = UI.creaBottone("Accedi", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
    private final JButton        btnVaiReg   = UI.creaBottoneSecondario("Registrati");
    private final JButton        btnGuest    = UI.creaBottoneSecondario("Continua come ospite");
    private final JLabel         lblStatoL   = UI.creaLabelStato();

    // ── Campi REGISTRAZIONE ──────────────────────────────────────────────────
    private final JTextField     rNome    = UI.creaInput("Nome *");
    private final JTextField     rCognome = UI.creaInput("Cognome *");
    private final JTextField     rUser    = UI.creaInput("Username o e-mail *");
    private final JPasswordField rPass    = UI.creaInputPassword("Password * (min. 6 caratteri)");
    private final JPasswordField rPass2   = UI.creaInputPassword("Conferma password *");
    private final JTextField     rDom     = UI.creaInput("Luogo domicilio");
    private final JTextField     rData    = UI.creaInput("Data nascita (gg/mm/aaaa) — facoltativo");
    private final JCheckBox      rGestore = UI.creaCheckbox("Voglio gestire un ristorante");
    private final JButton        btnReg   = UI.creaBottone("Crea account", UI.ROSSO_KNIFE, UI.ROSSO_HOVER);
    private final JButton        btnTorna = UI.creaBottoneSecondario("← Torna al login");
    private final JLabel         lblStatoR = UI.creaLabelStato();

    private ClientTK connessione;

    /**
     * Costruttore con connessione esistente: riusa il socket senza riconnettersi
     * se già attivo, altrimenti tenta una nuova connessione.
     *
     * @param connessioneEsistente connessione già stabilita da riutilizzare, o {@code null}
     */
    public LoginFrame(ClientTK connessioneEsistente) {
        super("TheKnife");
        this.connessione = connessioneEsistente;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 520);
        setMinimumSize(new Dimension(380, 480));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(UI.SFONDO_SCURO);
        cardPanel.setBackground(UI.SFONDO_SCURO);
        cardPanel.add(creaCardLogin(),    CARD_LOGIN);
        cardPanel.add(creaCardRegistra(), CARD_REGISTRA);
        setContentPane(cardPanel);
        collegaAzioni();
        if (connessione != null && connessione.isConnessa()) {
            setSezioneAbilitata(true);
            UI.aggiornaStato(lblStatoL, "Connesso al server", false);
        } else {
            setSezioneAbilitata(false);
            tentaConnessione();
        }
    }


    /**
     * Costruisce la schermata di login e avvia automaticamente il tentativo
     * di connessione al server.
     */
    public LoginFrame() {
        super("TheKnife");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 520);
        setMinimumSize(new Dimension(380, 480));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(UI.SFONDO_SCURO);

        cardPanel.setBackground(UI.SFONDO_SCURO);
        cardPanel.add(creaCardLogin(),    CARD_LOGIN);
        cardPanel.add(creaCardRegistra(), CARD_REGISTRA);
        setContentPane(cardPanel);

        collegaAzioni();
        setSezioneAbilitata(false);
        tentaConnessione();
    }

    // ── Card LOGIN ───────────────────────────────────────────────────────────

    /**
     * Costruisce la card di login con header, campi username/password e
     * pulsanti di azione (accedi, registrati, ospite).
     *
     * @return pannello della card di login
     */
    private JPanel creaCardLogin() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UI.SFONDO_SCURO);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        Dimension d = new Dimension(380, 500);
        box.setPreferredSize(d); box.setMinimumSize(d); box.setMaximumSize(d);

        box.add(UI.creaHeaderLogin());
        box.add(Box.createVerticalStrut(20));
        box.add(UI.assemblaSezioneLogin(fUser, fPass));
        box.add(Box.createVerticalStrut(24));
        box.add(UI.assemblaSezioneAzioni(btnLogin, btnVaiReg, btnGuest));
        box.add(Box.createVerticalStrut(16));
        lblStatoL.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(lblStatoL);

        root.add(box, new GridBagConstraints());
        return root;
    }

    // ── Card REGISTRAZIONE ───────────────────────────────────────────────────

    /**
     * Costruisce la card di registrazione con header, form completo dei
     * dati anagrafici e pulsanti di conferma/ritorno al login.
     *
     * @return pannello della card di registrazione
     */
    private JPanel creaCardRegistra() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UI.SFONDO_SCURO);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        Dimension d = new Dimension(400, 560);
        box.setPreferredSize(d); box.setMinimumSize(d); box.setMaximumSize(d);

        // Header
        JLabel titolo = new JLabel("Crea un account");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titolo.setForeground(UI.ROSSO_KNIFE);
        titolo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Registrati gratuitamente su TheKnife");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(UI.TESTO_GRIGIO);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form card
        JPanel card = new JPanel();
        card.setBackground(UI.SFONDO_CARD);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setLayout(new GridBagLayout());
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension dc = new Dimension(380, 350);
        card.setPreferredSize(dc); card.setMinimumSize(dc); card.setMaximumSize(dc);

        GridBagConstraints gbc = UI.gbc();
        JComponent[] campi = {rNome, rCognome, rUser, rPass, rPass2, rDom, rData, rGestore};
        for (int i = 0; i < campi.length; i++) {
            gbc.gridy = i;
            gbc.insets = new Insets(i == 0 ? 0 : 5, 0, 0, 0);
            card.add(campi[i], gbc);
        }

        // Bottoni
        JPanel btns = new JPanel();
        btns.setLayout(new BoxLayout(btns, BoxLayout.Y_AXIS));
        btns.setOpaque(false);
        btns.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension db = new Dimension(380, 38);
        for (JButton b : new JButton[]{btnReg, btnTorna}) {
            b.setPreferredSize(db); b.setMaximumSize(db);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
        btns.add(btnReg);
        btns.add(Box.createVerticalStrut(8));
        btns.add(btnTorna);

        lblStatoR.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(titolo);
        box.add(Box.createVerticalStrut(4));
        box.add(sub);
        box.add(Box.createVerticalStrut(16));
        box.add(card);
        box.add(Box.createVerticalStrut(14));
        box.add(btns);
        box.add(Box.createVerticalStrut(10));
        box.add(lblStatoR);

        root.add(box, new GridBagConstraints());
        return root;
    }

    // ── Azioni ───────────────────────────────────────────────────────────────

    /**
     * Collega i listener dei pulsanti e dei campi alle rispettive azioni:
     * login, navigazione tra le card, accesso ospite e registrazione.
     */
    private void collegaAzioni() {
        btnLogin.addActionListener(e -> tentaLogin());
        fPass.addActionListener(e -> tentaLogin());
        btnVaiReg.addActionListener(e -> { pulisciCampiReg(); cards.show(cardPanel, CARD_REGISTRA); });
        btnTorna.addActionListener(e -> cards.show(cardPanel, CARD_LOGIN));
        btnGuest.addActionListener(e -> apriHome(null));
        btnReg.addActionListener(e -> tentaRegistrazione());
        UI.applicaFocusHighlight(fUser);
        UI.applicaFocusHighlight(fPass);
        UI.applicaFocusHighlight(rUser);
        UI.applicaFocusHighlight(rPass);
    }

    // ── Rete ─────────────────────────────────────────────────────────────────

    /**
     * Tenta in background la connessione al server, chiudendo prima
     * un'eventuale connessione precedente ancora attiva. Al termine,
     * abilita il form o mostra un messaggio d'errore.
     */
    private void tentaConnessione() {
        UI.aggiornaStato(lblStatoL, "Connessione al server in corso...", false);
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                if (connessione != null && connessione.isConnessa()) connessione.disconnetti();
                connessione = new ClientTK();
                connessione.connetti(HOST, PORTA);
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    UI.aggiornaStato(lblStatoL, "Connesso al server", false);
                    setSezioneAbilitata(true);
                    fUser.requestFocusInWindow();
                } catch (Exception ex) {
                    UI.aggiornaStato(lblStatoL, "Server non raggiungibile. Avvia prima il server.", true);
                    connessione = null;
                }
            }
        }.execute();
    }

    /**
     * Valida i campi di login e invia in background la richiesta di
     * autenticazione al server, aprendo la Home in caso di successo.
     */
    private void tentaLogin() {
        String username = fUser.getText().trim();
        String password = new String(fPass.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            UI.aggiornaStato(lblStatoL, "Inserisci username o e-mail e password", true); return;
        }
        setSezioneAbilitata(false);
        UI.aggiornaStato(lblStatoL, "Accesso in corso...", false);

        new SwingWorker<Messaggio, Void>() {
            @Override protected Messaggio doInBackground() throws Exception {
                Messaggio req = new Messaggio(Messaggio.OP_LOGIN);
                req.addParam("username", username);
                req.addParam("password", password);
                return connessione.invia(req);
            }
            @Override protected void done() {
                try {
                    Messaggio resp = get();
                    if (resp.isOk()) {
                        apriHome(new Gson().fromJson(resp.getDatiJson(), Utente.class));
                    } else {
                        UI.aggiornaStato(lblStatoL, resp.getParam("errore"), true);
                        fPass.setText(""); fPass.requestFocusInWindow();
                    }
                } catch (Exception ex) {
                    UI.aggiornaStato(lblStatoL, "Errore: " + ex.getMessage(), true);
                } finally { setSezioneAbilitata(true); }
            }
        }.execute();
    }

    /**
     * Valida i campi di registrazione (obbligatorietà, lunghezza password,
     * corrispondenza conferma password, formato data) e invia in background
     * la richiesta di registrazione, aprendo la Home in caso di successo.
     */
    private void tentaRegistrazione() {
        String nome     = rNome.getText().trim();
        String cognome  = rCognome.getText().trim();
        String username = rUser.getText().trim();
        String pass     = new String(rPass.getPassword());
        String pass2    = new String(rPass2.getPassword());
        String domicilio = rDom.getText().trim();
        String dataStr   = rData.getText().trim();
        boolean isGestore = rGestore.isSelected();

        if (nome.isEmpty() || cognome.isEmpty() || username.isEmpty() || pass.isEmpty()) {
            UI.aggiornaStato(lblStatoR, "Compila tutti i campi obbligatori (*)", true); return;
        }
        if (pass.length() < 6) {
            UI.aggiornaStato(lblStatoR, "La password deve essere di almeno 6 caratteri", true); return;
        }
        if (!pass.equals(pass2)) {
            UI.aggiornaStato(lblStatoR, "Le password non coincidono", true); return;
        }
        if (!dataStr.isEmpty()) {
            try { new SimpleDateFormat("dd/MM/yyyy").parse(dataStr); }
            catch (Exception e) { UI.aggiornaStato(lblStatoR, "Formato data non valido (usa gg/mm/aaaa)", true); return; }
        }

        btnReg.setEnabled(false);
        UI.aggiornaStato(lblStatoR, "Registrazione in corso...", false);

        new SwingWorker<Messaggio, Void>() {
            @Override protected Messaggio doInBackground() throws Exception {
                Messaggio req = new Messaggio(Messaggio.OP_REGISTRA);
                req.addParam("nome",           nome);
                req.addParam("cognome",        cognome);
                req.addParam("username",       username);
                req.addParam("password",       pass);
                req.addParam("domicilio",      domicilio);
                req.addParam("dataNascita",    dataStr.isEmpty() ? null : dataStr);
                req.addParam("isRistoratore",  String.valueOf(isGestore));
                return connessione.invia(req);
            }
            @Override protected void done() {
                btnReg.setEnabled(true);
                try {
                    Messaggio resp = get();
                    if (resp.isOk()) {
                        apriHome(new Gson().fromJson(resp.getDatiJson(), Utente.class));
                    } else {
                        UI.aggiornaStato(lblStatoR, resp.getParam("errore"), true);
                    }
                } catch (Exception ex) {
                    UI.aggiornaStato(lblStatoR, "Errore: " + ex.getMessage(), true);
                }
            }
        }.execute();
    }

    // ── Navigazione & utility ────────────────────────────────────────────────

    /**
     * Avvia la sessione utente con la connessione corrente e l'utente indicato
     * (o {@code null} per l'accesso come ospite), quindi apre la Home e chiude
     * questa finestra.
     *
     * @param utente utente autenticato, o {@code null} per l'accesso ospite
     */
    private void apriHome(Utente utente) {
        SessioneUtente.getInstance().avvia(connessione, utente);
        new HomeFrame().setVisible(true);
        dispose();
    }

    /**
     * Abilita o disabilita i componenti della card di login.
     *
     * @param a {@code true} per abilitare i componenti, {@code false} per disabilitarli
     */
    private void setSezioneAbilitata(boolean a) {
        for (JComponent c : new JComponent[]{fUser, fPass, btnLogin, btnVaiReg, btnGuest})
            c.setEnabled(a);
    }

    /** Svuota tutti i campi del form di registrazione e il relativo messaggio di stato. */
    private void pulisciCampiReg() {
        for (JTextField f : new JTextField[]{rNome, rCognome, rUser, rDom, rData})
            f.setText("");
        rPass.setText(""); rPass2.setText("");
        rGestore.setSelected(false);
        UI.aggiornaStato(lblStatoR, " ", false);
    }
}
