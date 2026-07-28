package theknife.server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Utility per la gestione sicura delle password.
 * Usa SHA-256 con Base64 encoding.
 * @author esau alessandro argueta zepeda 761748
 * @author Ayoub Hammou 761589
 */
public class PasswordUtil {

    private PasswordUtil() {} // classe di utilità, niente costruttore

    /**
     * Calcola l'hash SHA-256 di una password.
     * @param password la password in chiaro
     * @return hash Base64 della password, o null se errore
     */
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Errore hashing password: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica se una password corrisponde a un hash.
     * @param password la password in chiaro da verificare
     * @param hashSalvato l'hash salvato nel database
     * @return true se la password è corretta
     */
    public static boolean verifica(String password, String hashSalvato) {
        String hashInput = hash(password);
        return hashInput != null && hashInput.equals(hashSalvato);
    }
}