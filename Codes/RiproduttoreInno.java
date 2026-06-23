// RiproduttoreInno.java
import javax.sound.sampled.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FIX #4 – ottieniPercorsoBase() ora verifica che il percorso ricavato
 *           dal ClassLoader sia effettivamente scrivibile prima di usarlo.
 *           Se non lo è, ricade sulla home utente (sicura) invece che su
 *           user.dir (che potrebbe essere una directory di sistema).
 *
 * FIX #6 – Le eccezioni non vengono più silenziate: vengono loggate con
 *           java.util.logging a livello WARNING, mantenendo la robustezza
 *           senza nascondere i problemi.
 */
public class RiproduttoreInno {

    private static final Logger LOG = Logger.getLogger(RiproduttoreInno.class.getName());

    private static Clip clip;
    private static boolean inRiproduzione = false;

    /**
     * FIX #4 – Restituisce il percorso base garantito scrivibile.
     * Priorità: cartella del JAR → home utente → directory temporanea.
     */
    public static String ottieniPercorsoBase() {
        // 1° tentativo: cartella del JAR
        try {
            File jarDir = new File(
                RiproduttoreInno.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            ).getParentFile();

            if (jarDir != null && jarDir.canWrite()) {
                return jarDir.getAbsolutePath();
            }
            LOG.warning("Cartella JAR non scrivibile: " +
                        (jarDir != null ? jarDir.getAbsolutePath() : "null"));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Impossibile ricavare la cartella del JAR", e);
        }

        // 2° tentativo: home utente (sempre disponibile per l'utente corrente)
        String home = System.getProperty("user.home");
        if (home != null && new File(home).canWrite()) {
            LOG.info("Percorso media ricaduto sulla home utente: " + home);
            return home;
        }

        // 3° tentativo: directory temporanea di sistema
        String tmp = System.getProperty("java.io.tmpdir");
        LOG.warning("Percorso media ricaduto sulla directory temporanea: " + tmp);
        return tmp;
    }

    public static final String MEDIA_DIR =
        ottieniPercorsoBase() + File.separator + "ArrowClock_Media";
    public static final String FILE_INNO =
        MEDIA_DIR + File.separator + "anthem.wav";
    public static final String FILE_BANDIERA =
        MEDIA_DIR + File.separator + "flag.png";

    public static void inizializzaCartellaMedia() {
        File dir = new File(MEDIA_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            LOG.warning("Impossibile creare la cartella media: " + dir.getAbsolutePath());
        }
    }

    public static boolean isInEsecuzione() {
        return inRiproduzione;
    }

    public static void toggleInno(ArcherySoftwareMain app) {
        try {
            // Se sta già suonando, lo stoppiamo
            if (clip != null && clip.isRunning()) {
                clip.stop();
                return;
            }

            File audioFile = new File(FILE_INNO);
            if (!audioFile.exists()) {
                LOG.warning("File inno non trovato: " + FILE_INNO);
                return;
            }

            if (clip != null) {
                clip.close();
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();

            // La Sentinella
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    inRiproduzione = false;
                    if (app != null) {
                        app.gestoreLog.logInno("FINITO");
                        javax.swing.SwingUtilities.invokeLater(() ->
                                new ComandoAggiornaBottoni(app).esegui()
                        );
                    }
                }
            });

            clip.open(audioStream);
            clip.start();
            inRiproduzione = true;

            if (app != null) {
                app.gestoreLog.logInno("PARTITO");
            }

        } catch (Exception e) {
            // FIX #6 – Loggato invece di stampato su stderr o silenziato
            LOG.log(Level.WARNING, "Errore riproduzione inno", e);
            inRiproduzione = false;
        }
    }

    public static void fermaInno() {
        if (clip != null) {
            try {
                if (inRiproduzione) clip.stop();
                clip.close();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Errore durante fermaInno()", e);
            }
        }
        inRiproduzione = false;
    }
}
