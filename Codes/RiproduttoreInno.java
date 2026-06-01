// RiproduttoreInno.java
import javax.sound.sampled.*;
import java.io.File;

public class RiproduttoreInno {
    private static Clip clip;
    private static boolean inRiproduzione = false;

    public static String ottieniPercorsoBase() {
        try {
            return new java.io.File(RiproduttoreInno.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (Exception e) {
            return System.getProperty("user.dir"); // Fallback
        }
    }

    public static final String MEDIA_DIR = ottieniPercorsoBase() + java.io.File.separator + "ArrowClock_Media";
    public static final String FILE_INNO = MEDIA_DIR + java.io.File.separator + "anthem.wav";
    public static final String FILE_BANDIERA = MEDIA_DIR + java.io.File.separator + "flag.png";

    public static void inizializzaCartellaMedia() {
        File dir = new File(MEDIA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static boolean isInEsecuzione() {
        return inRiproduzione;
    }

    // MODIFICA: Ora passiamo 'app' per permettere al riproduttore di inviare il comando ai bottoni
    public static void toggleInno(ArcherySoftwareMain app) {
        try {
            // 1. Se sta già suonando, lo stoppiamo (l'aggiornamento UI avverrà in automatico grazie al Listener)
            if (clip != null && clip.isRunning()) {
                clip.stop();
                return;
            }

            // 2. Se non sta suonando, lo prepariamo per la riproduzione
            File audioFile = new File(FILE_INNO);
            if (!audioFile.exists()) {
                System.out.println("File inno non trovato: " + FILE_INNO);
                return;
            }

            if (clip != null) {
                clip.close();
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();

            //La Sentinella
            clip.addLineListener(event -> {
                // Appena la clip si ferma (o perché l'hai stoppata tu o perché la canzone è finita naturalmente)
                if (event.getType() == LineEvent.Type.STOP) {
                    inRiproduzione = false;

                    // Ordina all'interfaccia di aggiornarsi, usando un thread Swing sicuro
                    if (app != null) {
                        javax.swing.SwingUtilities.invokeLater(() -> {
                            new ComandoAggiornaBottoni(app).esegui();
                        });
                    }
                }
            });

            clip.open(audioStream);
            clip.start();
            inRiproduzione = true;
        } catch (Exception e) {
            e.printStackTrace();
            inRiproduzione = false;
        }
    }

    public static void fermaInno() {
        if (clip != null) {
            if (inRiproduzione) {
                clip.stop();
            }
            clip.close();
        }
        inRiproduzione = false;
    }
}