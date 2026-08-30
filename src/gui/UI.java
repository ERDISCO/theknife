package theknife.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Utility class per la creazione centralizzata di componenti visivi Swing
 * uniformati allo stile/palette grafica di TheKnife.
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class UI {

    // ── Costanti Palette ──────────────────────────────────────────────────
    public static final Color ROSSO_KNIFE    = new Color(0xE84040);
    public static final Color ROSSO_HOVER    = new Color(0xC42828);
    public static final Color SFONDO_SCURO   = new Color(0x0F0F11);
    public static final Color SFONDO_CARD    = new Color(0x1E1E24);
    public static final Color SFONDO_TABELLA = new Color(0x17171C);
    public static final Color TESTO_CHIARO   = new Color(0xF0F0F5);
    public static final Color TESTO_GRIGIO   = new Color(0x9898A8);
    public static final Color BORDO_INPUT    = new Color(0x38383F);
    public static final Color BORDO_FOCUS    = new Color(0xE84040);
    public static final Color VERDE_OK       = new Color(0x2ECC71);
    public static final Color GIALLO_VOTO    = new Color(0xFFB627);
    public static final Color ACCENTO_BLU    = new Color(0x4A90E2);
    public static final Color ACCENTO_TEAL   = new Color(0x1ABC9C);

    private UI() {}

    // ── Stato & Feedback ───────────────────────────────────────────────────

    /**
     * Aggiorna testo e colore di una JLabel di stato.
     * Centralizza la logica prima duplicata in ogni dialog/frame.
     *
     * @param lbl     la label da aggiornare
     * @param testo   il messaggio da mostrare
     * @param errore  true = rosso, false = verde OK
     */
    public static void aggiornaStato(JLabel lbl, String testo, boolean errore) {
        lbl.setText(testo);
        lbl.setForeground(errore ? new Color(0xFF6B6B) : VERDE_OK);
    }

    /**
     * Aggiorna testo e colore di una label inline nei form (es. lblErrore).
     * Usa rosso errore oppure grigio neutro.
     *
     * @param lbl la label da aggiornare
     * @param testo il messaggio di errore da mostrare
     */
    public static void aggiornaErroreForm(JLabel lbl, String testo) {
        lbl.setText(testo);
        lbl.setForeground(new Color(0xFF6B6B));
    }

    // ── Label ──────────────────────────────────────────────────────────────

    /**
     * Crea un'etichetta di titolo principale, in grassetto e testo chiaro.
     *
     * @param testo testo dell'etichetta
     * @return etichetta stilizzata
     */
    public static JLabel creaLabelTitolo(String testo) {
        JLabel lbl = new JLabel(testo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(TESTO_CHIARO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Crea un'etichetta di intestazione di sezione, piccola, grassetto e grigia.
     *
     * @param testo testo dell'etichetta
     * @return etichetta stilizzata
     */
    public static JLabel creaLabelSezione(String testo) {
        JLabel lbl = new JLabel(testo);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TESTO_GRIGIO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Crea un'etichetta generica grigia in grassetto.
     *
     * @param testo testo dell'etichetta
     * @return etichetta stilizzata
     */
    public static JLabel creaLabel(String testo) {
        JLabel lbl = new JLabel(testo);
        lbl.setForeground(TESTO_GRIGIO);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Crea un'etichetta informativa in corsivo, usata per messaggi di aiuto o stato.
     *
     * @param testo testo dell'etichetta
     * @return etichetta stilizzata
     */
    public static JLabel creaLabelInfo(String testo) {
        JLabel lbl = new JLabel(testo);
        lbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lbl.setForeground(TESTO_GRIGIO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Crea un'etichetta di stato centrata, inizialmente vuota, usata nei form di login/registrazione.
     *
     * @return etichetta di stato
     */
    public static JLabel creaLabelStato() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TESTO_GRIGIO);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    /**
     * Crea un'etichetta per un filtro, con testo seguito da ":".
     *
     * @param testo testo dell'etichetta, senza i due punti finali
     * @return etichetta stilizzata
     */
    public static JLabel creaLabelFiltro(String testo) {
        JLabel lbl = new JLabel(testo + ":");
        lbl.setForeground(TESTO_GRIGIO);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return lbl;
    }

    /**
     * Crea un badge testuale con sfondo colorato arrotondato (es. ruolo utente).
     *
     * @param testo testo del badge
     * @param coloreSfondo colore di sfondo del badge
     * @return etichetta stilizzata come badge
     */
    public static JLabel creaBadge(String testo, Color coloreSfondo) {
        JLabel badge = new JLabel(testo) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(coloreSfondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(Color.WHITE);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    // ── Assemblaggio Login ─────────────────────────────────────────────────

    /**
     * Header centrato con icona e titolo dell'applicazione (usato da LoginFrame).
     *
     * @return pannello dell'header
     */
    public static JPanel creaHeaderLogin() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel icona = new JLabel("🍽");
        icona.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        icona.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titolo = new JLabel("TheKnife");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titolo.setForeground(ROSSO_KNIFE);
        titolo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sottotitolo = new JLabel("La tua guida ai migliori ristoranti");
        sottotitolo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sottotitolo.setForeground(TESTO_GRIGIO);
        sottotitolo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(icona);
        panel.add(Box.createVerticalStrut(8));
        panel.add(titolo);
        panel.add(Box.createVerticalStrut(4));
        panel.add(sottotitolo);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    /**
     * Card con campi Username e Password per il form di login.
     *
     * @param campoUser campo di testo per l'username
     * @param campoPass campo password
     * @return pannello della sezione di login
     */
    public static JPanel assemblaSezioneLogin(JTextField campoUser, JPasswordField campoPass) {
        JPanel card = new JPanel();
        card.setBackground(SFONDO_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDO_INPUT, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension dimCard = new Dimension(380, 160);
        card.setPreferredSize(dimCard);
        card.setMinimumSize(dimCard);
        card.setMaximumSize(dimCard);
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = gbc();

        Dimension dimInput = new Dimension(340, 38);
        campoUser.setPreferredSize(dimInput);
        campoUser.setMaximumSize(dimInput);
        campoPass.setPreferredSize(dimInput);
        campoPass.setMaximumSize(dimInput);

        JLabel lblTitolo = creaLabelSezione("ACCEDI");
        lblTitolo.setAlignmentX(Component.CENTER_ALIGNMENT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 14, 0);
        card.add(lblTitolo, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 10, 0);
        card.add(campoUser, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 0, 0);
        card.add(campoPass, gbc);

        return card;
    }

    /**
     * Pannello verticale con i tre pulsanti principali di login.
     *
     * @param btnLogin pulsante di accesso
     * @param btnReg pulsante di registrazione
     * @param btnGuest pulsante di accesso come ospite
     * @return pannello dei pulsanti di azione
     */
    public static JPanel assemblaSezioneAzioni(JButton btnLogin, JButton btnReg, JButton btnGuest) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension dim = new Dimension(380, 38);
        for (JButton btn : new JButton[]{btnLogin, btnReg, btnGuest}) {
            btn.setPreferredSize(dim);
            btn.setMaximumSize(dim);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        panel.add(btnLogin);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnReg);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnGuest);
        return panel;
    }

    // ── Bottoni ────────────────────────────────────────────────────────────

    /**
     * Crea un pulsante stilizzato con angoli arrotondati ed effetto hover.
     *
     * @param testo etichetta del pulsante
     * @param coloreBase colore di sfondo normale
     * @param coloreHover colore di sfondo al passaggio del mouse
     * @return pulsante stilizzato
     */
    public static JButton creaBottone(String testo, Color coloreBase, Color coloreHover) {
        JButton btn = new JButton(testo) {
            private boolean mouseSopra = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { mouseSopra = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { mouseSopra = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? (mouseSopra ? coloreHover : coloreBase) : new Color(0x444446));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(TESTO_CHIARO);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Crea un pulsante secondario con la palette grigia standard.
     *
     * @param testo etichetta del pulsante
     * @return pulsante stilizzato
     */
    public static JButton creaBottoneSecondario(String testo) {
        return creaBottone(testo, new Color(0x3A3A3C), new Color(0x4A4A4C));
    }

    /**
     * Crea un pulsante di dimensioni ridotte, usato nelle card di recensione.
     *
     * @param testo etichetta del pulsante
     * @param sfondo colore di sfondo normale (lo stato hover è una versione più chiara)
     * @return pulsante stilizzato
     */
    public static JButton creaBottoneSmall(String testo, Color sfondo) {
        Color hover = sfondo.brighter();
        JButton btn = new JButton(testo) {
            private boolean mouseSopra = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { mouseSopra = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { mouseSopra = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mouseSopra ? hover : sfondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Campi di Testo & Area Testo ───────────────────────────────────────

    /**
     * Crea un campo di testo con placeholder disegnato manualmente quando vuoto e privo di focus.
     *
     * @param placeholder testo segnaposto mostrato quando il campo è vuoto
     * @return campo di testo stilizzato
     */
    public static JTextField creaInput(String placeholder) {
        JTextField campo = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TESTO_GRIGIO);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left + 2,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                    g2.dispose();
                }
            }
        };
        stilizzaInput(campo);
        return campo;
    }

    /**
     * Crea un campo password con placeholder disegnato manualmente quando vuoto e privo di focus.
     *
     * @param placeholder testo segnaposto mostrato quando il campo è vuoto
     * @return campo password stilizzato
     */
    public static JPasswordField creaInputPassword(String placeholder) {
        JPasswordField campo = new JPasswordField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getPassword().length == 0 && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TESTO_GRIGIO);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    Insets ins = getInsets();
                    g2.drawString(placeholder, ins.left + 2,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                    g2.dispose();
                }
            }
        };
        stilizzaInput(campo);
        return campo;
    }

    /**
     * Applica lo stile grafico standard (colori, font, bordo, altezza) a un campo di testo.
     *
     * @param campo campo di testo da stilizzare
     */
    public static void stilizzaInput(JTextField campo) {
        campo.setBackground(new Color(0x3A3A3C));
        campo.setForeground(TESTO_CHIARO);
        campo.setCaretColor(TESTO_CHIARO);
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDO_INPUT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        campo.setPreferredSize(new Dimension(0, 36));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    /**
     * Applica evidenziazione del bordo al focus su qualsiasi JTextField
     * (incluse JPasswordField). Prima duplicato in ogni classe, ora centralizzato.
     *
     * @param campo campo di testo su cui applicare l'evidenziazione
     */
    public static void applicaFocusHighlight(JTextField campo) {
        campo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDO_FOCUS, 1, true),
                    new EmptyBorder(5, 8, 5, 8)));
            }
            @Override public void focusLost(FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDO_INPUT, 1, true),
                    new EmptyBorder(5, 8, 5, 8)));
            }
        });
    }

    /**
     * Crea un'area di testo multilinea con placeholder disegnato manualmente quando vuota e priva di focus.
     *
     * @param placeholder testo segnaposto mostrato quando l'area è vuota
     * @return area di testo stilizzata
     */
    public static JTextArea creaTextArea(String placeholder) {
        JTextArea area = new JTextArea() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TESTO_GRIGIO);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    g2.drawString(placeholder, 10, 20);
                    g2.dispose();
                }
            }
        };
        area.setBackground(new Color(0x3A3A3C));
        area.setForeground(TESTO_CHIARO);
        area.setCaretColor(TESTO_CHIARO);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        area.setBorder(new EmptyBorder(6, 8, 6, 8));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setRows(4);
        return area;
    }

    // ── Checkbox e ComboBox ────────────────────────────────────────────────

    /**
     * Crea una checkbox stilizzata con sfondo trasparente.
     *
     * @param testo etichetta della checkbox
     * @return checkbox stilizzata
     */
    public static JCheckBox creaCheckbox(String testo) {
        JCheckBox cb = new JCheckBox(testo);
        cb.setOpaque(false);
        cb.setForeground(TESTO_CHIARO);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setFocusPainted(false);
        return cb;
    }

    /**
     * Applica lo stile grafico standard (colori, font, renderer delle voci) a una combo box di stringhe.
     *
     * @param combo combo box da stilizzare
     */
    public static void stilizzaCombo(JComboBox<String> combo) {
        combo.setBackground(new Color(0x3A3A3C));
        combo.setForeground(TESTO_CHIARO);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? new Color(0x4A4A4C) : new Color(0x3A3A3C));
                setForeground(TESTO_CHIARO);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
    }

    /**
     * Applica lo stile grafico standard (colori, bordo, pulsanti) a uno spinner numerico,
     * forzando lo sfondo scuro del campo di editing tramite un {@link BasicTextFieldUI} custom.
     *
     * @param spinner spinner da stilizzare
     */
    public static void stilizzaSpinner(JSpinner spinner) {
        Color sfondo = new Color(0x3A3A3C);
        Color bottoni = new Color(0x48484A);

        spinner.setBorder(BorderFactory.createLineBorder(BORDO_INPUT, 1));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            JFormattedTextField campo = de.getTextField();
            campo.setForeground(TESTO_CHIARO);
            campo.setCaretColor(TESTO_CHIARO);
            campo.setSelectionColor(new Color(0xE84040));
            campo.setSelectedTextColor(Color.WHITE);
            campo.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            campo.setHorizontalAlignment(JTextField.CENTER);

            // Sovrascrive paintComponent per forzare lo sfondo scuro
            // indipendentemente da cosa il L&F ridisegna
            campo.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
                @Override
                protected void paintBackground(Graphics g) {
                    g.setColor(sfondo);
                    g.fillRect(0, 0, campo.getWidth(), campo.getHeight());
                }
            });
        }

        for (Component c : spinner.getComponents()) {
            if (c instanceof JButton btn) {
                btn.setBackground(bottoni);
                btn.setForeground(TESTO_CHIARO);
                btn.setBorder(BorderFactory.createLineBorder(new Color(0x555558), 1));
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);
                btn.setOpaque(true);
            }
        }
    }
    // ── Contenitori & Panel ────────────────────────────────────────────────

    /**
     * Crea una card generica con bordo e padding standard.
     *
     * @return pannello della card
     */
    public static JPanel creaCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(SFONDO_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDO_INPUT, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    // ── Tabelle ────────────────────────────────────────────────────────────

    /**
     * Crea il modello di tabella standard per le liste di ristoranti (con colonna 0 nascosta).
     *
     * @return modello di tabella non editabile
     */
    public static DefaultTableModel creaModelloTabella() {
        return new DefaultTableModel(
            new String[]{"#", "Nome", "Città", "Cucina", "Prezzo", "Valutazione", "Recensioni", "Delivery", "Prenotazione"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    /**
     * Crea il modello di tabella per il riepilogo dei ristoranti gestiti (con colonna 0 nascosta).
     *
     * @return modello di tabella non editabile
     */
    public static DefaultTableModel creaModelloTabellaGestore() {
        return new DefaultTableModel(
            new String[]{"ID", "Nome", "Città", "Cucina", "Valutazione", "Recensioni"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    /**
     * Crea una tabella stilizzata (colori, font, renderer di selezione e allineamento)
     * a partire dal modello indicato, nascondendo la colonna 0 usata per dati interni.
     *
     * @param model modello di tabella da usare
     * @return tabella stilizzata
     */
    public static JTable creaTabella(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setBackground(SFONDO_TABELLA);
        table.setForeground(TESTO_CHIARO);
        table.setGridColor(new Color(0x25252C));
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(0xE84040, false));
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                if (sel) {
                    setBackground(ROSSO_KNIFE);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(row % 2 == 0 ? SFONDO_TABELLA : new Color(0x1C1C22));
                    setForeground(TESTO_CHIARO);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0x111116));
        header.setForeground(TESTO_GRIGIO);
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 34));

        // Nascondi colonna 0 (contiene oggetto Ristorante o ID grezzo)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            String col = table.getColumnName(i);
            if (col.equals("Prezzo") || col.equals("Recensioni")
                    || col.equals("Delivery") || col.equals("Prenotazione")) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        return table;
    }

    /**
     * Incapsula una tabella in uno scroll pane stilizzato con bordo e sfondo coerenti.
     *
     * @param tabella tabella da incapsulare
     * @return scroll pane contenente la tabella
     */
    public static JScrollPane stilizzaTabella(JTable tabella) {
        JScrollPane scroll = new JScrollPane(tabella);
        scroll.setBorder(BorderFactory.createLineBorder(BORDO_INPUT));
        scroll.getViewport().setBackground(SFONDO_TABELLA);
        return scroll;
    }

    // ── Utility Layout ─────────────────────────────────────────────────────

    /**
     * Crea un {@link GridBagConstraints} predefinito: fill orizzontale, weightx=1, gridx=0.
     *
     * @return vincoli di layout predefiniti
     */
    public static GridBagConstraints gbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx   = 0;
        gbc.insets  = new Insets(4, 0, 4, 0);
        return gbc;
    }

    /**
     * Aggiunge un tooltip semplice a qualsiasi componente.
     *
     * @param c componente a cui aggiungere il tooltip
     * @param testo testo del tooltip
     */
    public static void addTooltip(JComponent c, String testo) {
        c.setToolTipText(testo);
    }

    /**
     * Costruisce una riga etichetta + componente, usata nei pannelli di filtro.
     *
     * @param etichetta testo dell'etichetta
     * @param componente componente da affiancare all'etichetta
     * @return pannello della riga di filtro
     */
    public static JPanel creaRigaFiltro(String etichetta, JComponent componente) {
        JPanel riga = new JPanel(new BorderLayout(12, 0));
        riga.setOpaque(false);
        riga.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // Label descrittiva stilizzata
        JLabel lbl = new JLabel(etichetta);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(UI.TESTO_CHIARO); // Sostituisci con il colore chiaro della tua classe UI
        lbl.setPreferredSize(new Dimension(110, 30)); // Ampiezza fissa per allineare perfettamente i campi

        riga.add(lbl, BorderLayout.WEST);
        riga.add(componente, BorderLayout.CENTER);
        
        return riga;
    }

    /**
     * Costruisce un blocco titolo + sottotitolo, usato in cima alle card delle
     * varie schermate (es. "I miei preferiti" / "I ristoranti che hai salvato").
     *
     * @param titolo titolo principale
     * @param sottotitolo sottotitolo descrittivo
     * @return pannello con titolo e sottotitolo
     */
    public static JPanel creaCardTitolo(String titolo, String sottotitolo) {
        JPanel panelTitolo = new JPanel();
        panelTitolo.setLayout(new BoxLayout(panelTitolo, BoxLayout.Y_AXIS));
        panelTitolo.setOpaque(false);
        panelTitolo.setBorder(new EmptyBorder(0, 0, 14, 0));

        // Titolo principale in grassetto
        JLabel lblTitolo = new JLabel(titolo);
        lblTitolo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitolo.setForeground(UI.TESTO_CHIARO);
        lblTitolo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Sottotitolo descrittivo più piccolo e grigio
        JLabel lblSub = new JLabel(sottotitolo);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(UI.TESTO_GRIGIO); // Sostituisci con il colore grigio della tua classe UI
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panelTitolo.add(lblTitolo);
        panelTitolo.add(Box.createVerticalStrut(4));
        panelTitolo.add(lblSub);

        return panelTitolo;
    }
}
