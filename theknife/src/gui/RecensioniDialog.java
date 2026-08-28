package theknife.gui;

import com.google.gson.Gson;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import theknife.common.Messaggio;
import theknife.common.Recensione;
import theknife.common.Ristorante;

/**
 * Dialog per la visualizzazione delle recensioni di un ristorante.
 * Usata dal gestore per le recensioni ricevute (con possibilità di risposta)
 * e, in generale, per mostrare l'elenco recensioni con la media voti.
 * Sostituisce RecensioniGestoreDialog, MieRecensioniDialog e
 * RecensioniDelMioRistoranteDialog, unificandole in un'unica classe con
 * modalità diverse selezionate dal flag {@code modoGestore}.
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class RecensioniDialog extends JDialog {

        private final SessioneUtente sessione;
        private final Ristorante          ristorante;
        private final boolean             modoGestore;
        private final JPanel              panelRec = new JPanel();
        private final JLabel              lblStato = UI.creaLabelInfo(" ");

        /**
         * Costruisce il dialog delle recensioni per il ristorante indicato.
         *
         * @param parent finestra padre a cui il dialog è modale
         * @param ristorante ristorante di cui mostrare le recensioni
         * @param modoGestore {@code true} per abilitare la risposta alle recensioni (vista gestore)
         * @param sessione sessione utente corrente, usata per la connessione al server
         */
        public RecensioniDialog(Frame parent, Ristorante ristorante, boolean modoGestore, SessioneUtente sessione) {
            super(parent, "Recensioni - " + ristorante.getNome(), true);
            this.ristorante  = ristorante;
            this.modoGestore = modoGestore;
            this.sessione = sessione;
            setSize(680, 620);
            setMinimumSize(new Dimension(560, 460));
            setLocationRelativeTo(parent);
            getContentPane().setBackground(UI.SFONDO_SCURO);
            costruisci();
            carica();
        }

        /**
         * Costruisce la struttura statica del dialog: header con nome del
         * ristorante, area scrollabile per le recensioni e footer con stato e
         * pulsante di chiusura.
         */
        private void costruisci() {
            setLayout(new BorderLayout());

            // Header
            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(new Color(0x111113));
            header.setBorder(new EmptyBorder(18, 24, 14, 24));
            JLabel lblN = new JLabel(ristorante.getNome());
            lblN.setFont(new Font("Segoe UI", Font.BOLD, 20)); lblN.setForeground(UI.TESTO_CHIARO);
            JLabel lblS = new JLabel(modoGestore
                    ? "Puoi rispondere una sola volta a ogni recensione"
                    : "Le recensioni per questo ristorante");
            lblS.setFont(new Font("Segoe UI", Font.PLAIN, 12)); lblS.setForeground(UI.TESTO_GRIGIO);
            JPanel testi = new JPanel(); testi.setOpaque(false);
            testi.setLayout(new BoxLayout(testi, BoxLayout.Y_AXIS));
            testi.add(lblN); testi.add(Box.createVerticalStrut(4)); testi.add(lblS);
            header.add(testi, BorderLayout.CENTER);
            add(header, BorderLayout.NORTH);

            // Scroll
            panelRec.setLayout(new BoxLayout(panelRec, BoxLayout.Y_AXIS));
            panelRec.setBackground(UI.SFONDO_SCURO);
            panelRec.setBorder(new EmptyBorder(12, 24, 12, 24));
            JLabel loading = new JLabel("Caricamento..."); loading.setForeground(UI.TESTO_GRIGIO);
            panelRec.add(loading);
            JScrollPane scroll = new JScrollPane(panelRec);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(UI.SFONDO_SCURO);
            scroll.getVerticalScrollBar().setUnitIncrement(14);
            add(scroll, BorderLayout.CENTER);

            // Footer
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(new Color(0x111113));
            footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UI.BORDO_INPUT));
            footer.add(lblStato);
            JButton btnC = UI.creaBottone("Chiudi", new Color(0x3A3A3C), new Color(0x4A4A4C));
            btnC.setPreferredSize(new Dimension(90, 34));
            btnC.addActionListener(e -> dispose());
            footer.add(btnC);
            add(footer, BorderLayout.SOUTH);
        }

        /**
         * Carica in background le recensioni del ristorante dal server e,
         * al termine, popola il pannello dei risultati.
         */
        private void carica() {
            new SwingWorker<List<Recensione>, Void>() {
                @Override protected List<Recensione> doInBackground() throws Exception {
                    Messaggio req = new Messaggio(Messaggio.OP_RECENSIONI_RISTO);
                    req.addParam("ristoranteId", String.valueOf(ristorante.getId()));
                    Messaggio resp = sessione.getConnessione().invia(req);
                    if (!resp.isOk()) throw new Exception(resp.getParam("errore"));
                    if (resp.getDatiJson() == null || resp.getDatiJson().isBlank()) return List.of();
                    return Arrays.asList(new Gson().fromJson(resp.getDatiJson(), Recensione[].class));
                }
                @Override protected void done() {
                    try { popola(get()); }
                    catch (Exception ex) {
                        panelRec.removeAll();
                        JLabel err = new JLabel("Errore: " + ex.getMessage());
                        err.setForeground(new Color(0xFF6B6B));
                        panelRec.add(err); panelRec.revalidate();
                    }
                }
            }.execute();
        }

        /**
         * Popola il pannello delle recensioni con la lista ricevuta, mostrando
         * anche la media dei voti quando la lista non è vuota.
         *
         * @param lista lista di recensioni da mostrare
         */
        private void popola(List<Recensione> lista) {
            panelRec.removeAll();
            if (lista.isEmpty()) {
                JLabel v = new JLabel("Nessuna recensione per questo ristorante");
                v.setForeground(UI.TESTO_GRIGIO); v.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelRec.add(v);
            } else {
                // Media stelle
                double media = lista.stream().mapToInt(Recensione::getStelle).average().orElse(0);
                JLabel lblMedia = new JLabel(String.format("Media: %.1f / 5  (%d recensioni)", media, lista.size()));
                lblMedia.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lblMedia.setForeground(UI.GIALLO_VOTO);
                lblMedia.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelRec.add(lblMedia); panelRec.add(Box.createVerticalStrut(12));

                for (Recensione r : lista) {
                    panelRec.add(creaCard(r)); panelRec.add(Box.createVerticalStrut(10));
                }
            }
            panelRec.revalidate(); panelRec.repaint();
        }

        /**
         * Costruisce la card visuale di una singola recensione, con eventuale
         * risposta del gestore già presente oppure, in modalità gestore, il
         * pulsante per rispondere.
         *
         * @param rec recensione da rappresentare
         * @return pannello della card della recensione
         */
        private JPanel creaCard(Recensione rec) {
            JPanel card = UI.creaCardPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            JLabel lblTop = new JLabel("Stelle: " + rec.getStelle() + " / 5  -  " + rec.getUsernameAutore()
                    + "  -  " + (rec.getDataRecensione() != null ? rec.getDataRecensione().substring(0,10) : ""));
            lblTop.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTop.setForeground(UI.GIALLO_VOTO);

            JTextArea area = new JTextArea(rec.getTesto() != null ? rec.getTesto() : "");
            area.setEditable(false); area.setLineWrap(true); area.setWrapStyleWord(true);
            area.setOpaque(false); area.setForeground(UI.TESTO_CHIARO);
            area.setFont(new Font("Segoe UI", Font.PLAIN, 13)); area.setBorder(null);

            card.add(lblTop); card.add(Box.createVerticalStrut(6)); card.add(area);

            if (rec.haRisposta()) {
                JLabel lblR = new JLabel("" + rec.getRispostaGestore());
                lblR.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                lblR.setForeground(new Color(0x2E86AB));
                card.add(Box.createVerticalStrut(6)); card.add(lblR);
            } else if (modoGestore) {
                JButton btnRisp = UI.creaBottoneSmall("Rispondi", new Color(0x2E86AB));
                btnRisp.setAlignmentX(Component.LEFT_ALIGNMENT);
                btnRisp.addActionListener(e -> {
                    String risp = JOptionPane.showInputDialog(this, "Scrivi la tua risposta:", "Risposta", JOptionPane.PLAIN_MESSAGE);
                    if (risp == null || risp.isBlank()) return;
                    new SwingWorker<Boolean,Void>() {
                        @Override protected Boolean doInBackground() throws Exception {
                            Messaggio req = new Messaggio(Messaggio.OP_RISPONDI_REC);
                            req.addParam("recensioneId", String.valueOf(rec.getId()));
                            req.addParam("risposta", risp.trim());
                            Messaggio resp = sessione.getConnessione().invia(req);
                            if (!resp.isOk()) throw new Exception(resp.getParam("errore"));
                            return true;
                        }
                        @Override protected void done() {
                            try { get(); UI.aggiornaStato(lblStato, "Risposta pubblicata.", false); carica(); }
                            catch (Exception ex) { UI.aggiornaStato(lblStato, "Errore: " + ex.getMessage(), true); }
                        }
                    }.execute();
                });
                card.add(Box.createVerticalStrut(6)); card.add(btnRisp);
            }
            return card;
        }
    }
