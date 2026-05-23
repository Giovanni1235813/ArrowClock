import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Motore audio singleton.
 * Gestisce la riproduzione e l'interruzione immediata dei fischi.
 * Sostituisce la classe statica AudioEngine con un'istanza singleton
 * che incapsula tutto lo stato audio.
 */
public class MotoreAudio {

    private static final MotoreAudio ISTANZA = new MotoreAudio();

    private ExecutorService esecutoreAudio = Executors.newSingleThreadExecutor();
    private SourceDataLine lineaAudioCorrente;
    private volatile int generazioneAudio = 0;
    private static final Object audioLock = new Object();

    private MotoreAudio() {}

    public static MotoreAudio istanza() {
        return ISTANZA;
    }

    public void avviaMotoreAudioSilenzioso() {
        Thread keepAliveThread = new Thread(() -> {
            try {
                AudioFormat af = new AudioFormat(44100f, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                byte[] silenzioAssoluto = new byte[4410];
                while (true) {
                    sdl.write(silenzioAssoluto, 0, silenzioAssoluto.length);
                }
            } catch (Exception e) {}
        });
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }

    public void azzeraCodaFischi() {
        generazioneAudio++;

        if (esecutoreAudio != null) {
            esecutoreAudio.shutdownNow();
        }

        synchronized (audioLock) {
            if (lineaAudioCorrente != null) {
                try {
                    lineaAudioCorrente.stop();
                    lineaAudioCorrente.flush();
                    lineaAudioCorrente.close();
                } catch (Exception e) {}
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

    private void generaTono(int hz, int msecs, int genAttuale) {
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
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);

            synchronized (audioLock) {
                if (genAttuale != generazioneAudio) return;
                sdl.open(af);
                lineaAudioCorrente = sdl;
                sdl.start();
            }

            int chunkSize = (int) (10 * sampleRate / 1000.0f);
            for (int i = 0; i < length; i += chunkSize) {
                if (genAttuale != generazioneAudio || Thread.currentThread().isInterrupted()) {
                    synchronized (audioLock) {
                        if (lineaAudioCorrente != null) {
                            lineaAudioCorrente.flush();
                            lineaAudioCorrente.close();
                            lineaAudioCorrente = null;
                        }
                    }
                    return;
                }
                int bytesToWrite = Math.min(chunkSize, length - i);
                sdl.write(buffer, i, bytesToWrite);
            }

            if (genAttuale == generazioneAudio && !Thread.currentThread().isInterrupted()) {
                sdl.drain();
            }

            synchronized (audioLock) {
                if (lineaAudioCorrente != null) {
                    lineaAudioCorrente.close();
                    lineaAudioCorrente = null;
                }
            }

        } catch (Exception ex) {}
    }
}
