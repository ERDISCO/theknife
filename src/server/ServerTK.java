package theknife.server;

import javax.swing.*;

/**
 * Entry point del server TheKnife.
 * Lancia l'interfaccia grafica ServerGUI che gestisce
 * configurazione, avvio, log in tempo reale e spegnimento.
 *
 * Avvio: java -jar theknife-server-1.0.jar
 *
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @version 2.0
 */
public class ServerTK {

    /**
     * Punto di ingresso dell'applicazione server: imposta il look-and-feel
     * di sistema e avvia la GUI di controllo sull'Event Dispatch Thread.
     *
     * @param args argomenti da riga di comando (non utilizzati)
     */
    public static void main(String[] args) {
        // Imposta il look-and-feel di sistema per una resa migliore su ogni OS
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Lancia la GUI sull'Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
}
