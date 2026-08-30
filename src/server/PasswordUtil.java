package theknife.server;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utility per la gestione sicura delle password con BCrypt.
 * BCrypt include automaticamente un salt casuale e un work-factor
 * regolabile (12 è il valore raccomandato per applicazioni moderne),
 * rendendolo resistente ad attacchi brute-force e rainbow table.
 * Il confronto avviene a tempo costante, eliminando i timing attack.
 *
 * @author Esau Alessandro Argueta Zepeda 761748 - sede di Varese
 * @author Ayoub Hammou                   761589 - sede di Varese
 * @version 2.0
 */
public class PasswordUtil {

    /** Work factor BCrypt: 2^12 = 4096 iterazioni (buon compromesso sicurezza/velocità). */
    private static final int WORK_FACTOR = 12;

    private PasswordUtil() {} // classe di utilità, non istanziabile

    /**
     * Calcola l'hash BCrypt della password in chiaro.
     * Ogni chiamata genera un salt diverso automaticamente,
     * quindi due hash della stessa password saranno sempre diversi.
     *
     * @param password la password in chiaro
     * @return stringa BCrypt da salvare nel DB (include salt e work-factor)
     */
    public static String hash(String password) {
        return BCrypt.withDefaults().hashToString(WORK_FACTOR, password.toCharArray());
    }

    /**
     * Verifica se una password in chiaro corrisponde all'hash BCrypt salvato.
     * Il confronto è a tempo costante (immune a timing attack).
     *
     * @param password    la password in chiaro da verificare
     * @param hashSalvato l'hash BCrypt salvato nel database
     * @return true se la password è corretta
     */
    public static boolean verifica(String password, String hashSalvato) {
        if (password == null || hashSalvato == null) return false;
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashSalvato);
        return result.verified;
    }
}
