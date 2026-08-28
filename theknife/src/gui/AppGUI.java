package theknife.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point dell'applicazione GUI TheKnife.
 *
 * Imposta il Look & Feel di sistema, applica il tema scuro globale
 * ai componenti Swing standard, poi lancia LoginFrame sull'EDT.
 *
 * Per avviare il client GUI:
 *   java -cp theknife.jar theknife.gui.AppGUI
 *
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @version 2.0
 */
public class AppGUI {

    /**
     * Punto di ingresso dell'applicazione client GUI: imposta le proprietà
     * di sistema per macOS, applica il tema scuro e avvia {@link LoginFrame}
     * sull'Event Dispatch Thread.
     *
     * @param args argomenti da riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        // Impostazioni macOS (ignorata su altri OS)
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "TheKnife");

        SwingUtilities.invokeLater(() -> {
            applicaTema();
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }

    /**
     * Applica un tema scuro coerente ai componenti Swing standard.
     * I frame custom usano già colori dipinti a mano; questo metodo
     * copre i dialogs di sistema (JOptionPane, JFileChooser ecc.).
     */
    private static void applicaTema() {
        try {
            // Prova prima Nimbus che supporta l'override dei colori
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Fallback al LaF di sistema — non critico
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
        }

        // Override colori globali Swing (usati da JOptionPane ecc.)
        Color sfondoScuro  = new Color(0x1C1C1E);
        Color sfondoCard   = new Color(0x2C2C2E);
        Color testoChiaro  = new Color(0xF5F5F5);
        Color testoGrigio  = new Color(0xAAAAAA);
        Color rosso        = new Color(0xC0392B);
        Color bordoInput   = new Color(0x444446);

        UIManager.put("Panel.background",                sfondoScuro);
        UIManager.put("OptionPane.background",           sfondoScuro);
        UIManager.put("OptionPane.messageForeground",    testoChiaro);
        UIManager.put("OptionPane.messageFont",
            new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont",
            new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("Button.background",               sfondoCard);
        UIManager.put("Button.foreground",               testoChiaro);
        UIManager.put("Button.font",
            new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("TextField.background",            new Color(0x3A3A3C));
        UIManager.put("TextField.foreground",            testoChiaro);
        UIManager.put("TextField.caretForeground",       testoChiaro);
        UIManager.put("TextField.border",
            BorderFactory.createLineBorder(bordoInput));
        UIManager.put("PasswordField.background",        new Color(0x3A3A3C));
        UIManager.put("PasswordField.foreground",        testoChiaro);
        UIManager.put("PasswordField.caretForeground",   testoChiaro);
        UIManager.put("TextArea.background",             new Color(0x3A3A3C));
        UIManager.put("TextArea.foreground",             testoChiaro);
        UIManager.put("TextArea.caretForeground",        testoChiaro);
        UIManager.put("Label.foreground",                testoChiaro);
        UIManager.put("Label.font",
            new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("CheckBox.background",             sfondoCard);
        UIManager.put("CheckBox.foreground",             testoChiaro);
        UIManager.put("CheckBox.font",
            new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("RadioButton.background",          sfondoCard);
        UIManager.put("RadioButton.foreground",          testoChiaro);
        UIManager.put("ComboBox.background",             new Color(0x3A3A3C));
        UIManager.put("ComboBox.foreground",             testoChiaro);
        UIManager.put("ComboBox.selectionBackground",    rosso);
        UIManager.put("ComboBox.selectionForeground",    Color.WHITE);
        UIManager.put("List.background",                 sfondoCard);
        UIManager.put("List.foreground",                 testoChiaro);
        UIManager.put("List.selectionBackground",        rosso);
        UIManager.put("List.selectionForeground",        Color.WHITE);
        UIManager.put("Table.background",                new Color(0x252527));
        UIManager.put("Table.foreground",                testoChiaro);
        UIManager.put("Table.selectionBackground",       rosso);
        UIManager.put("Table.selectionForeground",       Color.WHITE);
        UIManager.put("Table.gridColor",                 new Color(0x333335));
        UIManager.put("TableHeader.background",          new Color(0x1E1E20));
        UIManager.put("TableHeader.foreground",          testoGrigio);
        UIManager.put("TableHeader.font",
            new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("ScrollBar.background",            sfondoScuro);
        UIManager.put("ScrollBar.thumb",                 bordoInput);
        UIManager.put("ScrollBar.track",                 sfondoScuro);
        UIManager.put("ScrollPane.background",           sfondoScuro);
        UIManager.put("Viewport.background",             sfondoScuro);
        UIManager.put("TabbedPane.background",           sfondoScuro);
        UIManager.put("TabbedPane.foreground",           testoChiaro);
        UIManager.put("TabbedPane.selected",             sfondoCard);
        UIManager.put("Separator.foreground",            bordoInput);
        UIManager.put("ToolTip.background",              sfondoCard);
        UIManager.put("ToolTip.foreground",              testoChiaro);
        UIManager.put("ToolTip.border",
            BorderFactory.createLineBorder(bordoInput));

        // Anti-aliasing testo su tutti i componenti
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }
}
