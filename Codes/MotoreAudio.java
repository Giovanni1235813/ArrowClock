import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Motore audio singleton.
 * Gestisce la riproduzione e l'interruzione immediata dei fischi.
 *
 * FIX #1 – Thread keepalive controllato: il loop while(true) è stato sostituito
 *           con un flag volatile; la SourceDataLine viene chiusa ordinatamente
 *           all'uscita tramite un blocco try-finally.
 *
 * FIX #2 – ExecutorService con awaitTermination: azzeraCodaFischi() attende
 *           fino a 500ms la terminazione dei task audio pendenti prima di
 *           ricreare l'executor, evitando il leak di thread e risorse native.
 *
 * FIX #3 – Shutdown hook: un hook registrato sulla JVM chiama spegni() per
 *           rilasciare tutte le risorse audio quando l'applicazione termina
 *           (incluse le chiamate a System.exit()).
 *
 * FIX #6 – Eccezioni loggate: i blocchi catch vuoti sono stati sostituiti con
 *           logging a livello WARNING, così i problemi sono diagnosticabili
 *           senza interrompere il flusso dell'applicazione.
 */
public class MotoreAudio {

    private static final Logger LOG = Logger.getLogger(MotoreAudio.class.getName());

    private static final MotoreAudio ISTANZA = new MotoreAudio();

    private ExecutorService esecutoreAudio = Executors.newSingleThreadExecutor();
    private SourceDataLine lineaAudioCorrente;
    private volatile int generazioneAudio = 0;
    private static final Object audioLock = new Object();

    // FIX #1 – flag per fermare il thread keepalive in modo pulito
    private volatile boolean keepAliveAttivo = false;
    private SourceDataLine lineaKeepAlive = null;

    private MotoreAudio() {
        // FIX #3 – Shutdown hook: garantisce il rilascio delle risorse audio
        // anche in caso di System.exit() o chiusura forzata della JVM.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                spegni();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Errore durante lo shutdown audio", e);
            }
        }, "AudioShutdownHook"));
    }

    public static MotoreAudio istanza() {
        return ISTANZA;
    }

    // FIX #1 – Il thread keepalive ora ha un flag di uscita e chiude la linea
    //          audio ordinatamente nel blocco finally.
    public void avviaMotoreAudioSilenzioso() {
        if (keepAliveAttivo) return; // già avviato, non riaprire
        keepAliveAttivo = true;

        Thread keepAliveThread = new Thread(() -> {
            SourceDataLine sdl = null;
            try {
                AudioFormat af = new AudioFormat(44100f, 8, 1, true, false);
                sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();

                synchronized (audioLock) {
                    lineaKeepAlive = sdl;
                }

                byte[] silenzioAssoluto = new byte[4410];
                // FIX #1 – Condizione di uscita controllata tramite flag volatile
                while (keepAliveAttivo && !Thread.currentThread().isInterrupted()) {
                    sdl.write(silenzioAssoluto, 0, silenzioAssoluto.length);
                }
            //} catch (InterruptedException e) {
            //    Thread.currentThread().interrupt();
                //Condizione di uscita controllata flag volatile
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Errore nel thread keepalive audio", e);
            } finally {
                // FIX #1 – La risorsa hardware viene sempre rilasciata
                synchronized (audioLock) {
                    lineaKeepAlive = null;
                }
                if (sdl != null) {
                    try {
                        sdl.stop();
                        sdl.close();
                    } catch (Exception ex) {
                        LOG.log(Level.WARNING, "Errore chiusura linea keepalive", ex);
                    }
                }
            }
        }, "AudioKeepAlive");
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }

    // FIX #2 – awaitTermination: attende la fine dei task audio pendenti
    //          prima di ricreare l'executor, prevenendo il leak di thread.
    public void azzeraCodaFischi() {
        generazioneAudio++;

        if (esecutoreAudio != null) {
            esecutoreAudio.shutdownNow();
            try {
                // Attende al massimo 500ms; se il task non termina, si procede comunque
                if (!esecutoreAudio.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                    LOG.warning("Il task audio non ha terminato entro 500ms");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        synchronized (audioLock) {
            if (lineaAudioCorrente != null) {
                try {
                    lineaAudioCorrente.stop();
                    lineaAudioCorrente.flush();
                    lineaAudioCorrente.close();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Errore chiusura linea audio corrente", e);
                }
                lineaAudioCorrente = null;
            }
        }

        esecutoreAudio = Executors.newSingleThreadExecutor();
    }

    public void eseguiFischi(int numeroFischi, boolean isSuonoAttivo) {
        if (!isSuonoAttivo) return;

        final int genAttuale = generazioneAudio;

        esecutoreAudio.submit(() -> {
            for (int i = 0; i < numeroFischi; i++) {
                if (genAttuale != generazioneAudio || Thread.currentThread().isInterrupted()) return;
                generaTono(750, 600, genAttuale);
                if (genAttuale != generazioneAudio || Thread.currentThread().isInterrupted()) return;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
    }

    /**
     * FIX #3 – Chiamato dallo shutdown hook e da CostruttoreOperatore prima
     * di System.exit(). Ferma il keepalive, l'executor e chiude le linee aperte.
     */
    public void spegni() {
        // Ferma il thread keepalive
        keepAliveAttivo = false;

        // Ferma e attende l'executor fischi
        if (esecutoreAudio != null) {
            esecutoreAudio.shutdownNow();
            try {
                esecutoreAudio.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Chiude eventuali linee audio ancora aperte
        synchronized (audioLock) {
            if (lineaAudioCorrente != null) {
                try {
                    lineaAudioCorrente.stop();
                    lineaAudioCorrente.close();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Errore chiusura linea in spegni()", e);
                }
                lineaAudioCorrente = null;
            }
            if (lineaKeepAlive != null) {
                try {
                    lineaKeepAlive.stop();
                    lineaKeepAlive.close();
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Errore chiusura keepalive in spegni()", e);
                }
                lineaKeepAlive = null;
            }
        }
    }

    private void generaTono(int hz, int msecs, int genAttuale) {
        SourceDataLine sdl = null;
        try {
            if (genAttuale != generazioneAudio) return;

            float sampleRate = 44100f;
            int length = (int) (msecs * sampleRate / 1000.0f);
            byte[] buffer = new byte[length];
            int fadeLength = (int) (15 * sampleRate / 1000.0f);
            double period = sampleRate / hz;

            for (int i = 0; i < length; i++) {
                double volume = ((i % period) < (period / 2.0)) ? 125 : -125;
                if (i < fadeLength) {
                    volume = volume * ((double) i / fadeLength);
                } else if (i > length - fadeLength) {
                    volume = volume * ((double) (length - i) / fadeLength);
                }
                buffer[i] = (byte) volume;
            }

            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
            sdl = AudioSystem.getSourceDataLine(af);

            synchronized (audioLock) {
                if (genAttuale != generazioneAudio) return;
                sdl.open(af);
                lineaAudioCorrente = sdl;
                sdl.start();
            }

            int chunkSize = (int) (10 * sampleRate / 1000.0f);
            for (int i = 0; i < length; i += chunkSize) {
                if (genAttuale != generazioneAudio || Thread.currentThread().isInterrupted()) {
                    return; // il finally chiuderà la linea
                }
                int bytesToWrite = Math.min(chunkSize, length - i);
                sdl.write(buffer, i, bytesToWrite);
            }

            if (genAttuale == generazioneAudio && !Thread.currentThread().isInterrupted()) {
                sdl.drain();
            }

        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Errore generazione tono", ex);
        } finally {
            // FIX #6 – La linea viene sempre chiusa, anche in caso di eccezione
            synchronized (audioLock) {
                if (lineaAudioCorrente != null) {
                    try {
                        lineaAudioCorrente.stop();
                        lineaAudioCorrente.close();
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Errore chiusura SDL in finally", e);
                    }
                    lineaAudioCorrente = null;
                }
            }
        }
    }
}
