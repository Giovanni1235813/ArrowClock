import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestisce la scrittura del log partita su file.
 *
 * FIX #3a – Writer bufferizzato persistente: invece di aprire e chiudere
 *            il file ad ogni riga, un BufferedWriter viene aperto una volta
 *            sola all'inizio della sessione e chiuso solo allo shutdown.
 *            Questo elimina il costo I/O ripetuto sul thread Swing.
 *
 * FIX #3b – Scrittura asincrona off-EDT: ogni chiamata a scriviLog() accoda
 *            il messaggio su un ExecutorService a thread singolo dedicato,
 *            così il thread Swing non viene mai bloccato da operazioni su disco.
 *
 * FIX #3c – Limite dimensione file: se il file supera MAX_LOG_BYTES (5 MB),
 *            viene ruotato automaticamente (rinominato con timestamp) e ne
 *            viene creato uno nuovo, prevenendo la crescita illimitata.
 *
 * FIX #4  – Percorso sicuro: ottieniPercorsoBase() verifica che il percorso
 *            calcolato dal ClassLoader sia scrivibile; se non lo è, ricade
 *            sulla home utente invece che su user.dir (che potrebbe essere
 *            una directory di sistema).
 *
 * FIX #6  – Eccezioni loggate con java.util.logging invece di essere silenziate.
 */
public class GestoreLog {

    private static final Logger LOG = Logger.getLogger(GestoreLog.class.getName());

    /** Soglia di rotazione: 5 MB */
    private static final long MAX_LOG_BYTES = 5L * 1024 * 1024;

    private final ArcherySoftwareMain app;

    // FIX #3a – Writer tenuto aperto per tutta la sessione
    private BufferedWriter writerCorrente;
    private String percorsoCorrente;

    // FIX #3b – Executor dedicato: tutte le scritture avvengono su questo thread,
    //           mai sul thread Swing (EDT).
    private final ExecutorService esecutoreLog = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "LogWriter");
        t.setDaemon(true);
        return t;
    });

    // ── Statistiche della sessione software (per il riepilogo di chiusura) ─────
    // Tutti i contatori vengono aggiornati SOLO mentre "Gara in Corso" è attiva,
    // così il riepilogo riflette la sola competizione effettiva e non i momenti
    // in cui il pulsante era spento.
    private String  istanteAvvioSoftware   = null;   // testo "yyyy-MM-dd HH:mm:ss"
    private long    durataGaraAccumulataMs = 0;       // somma dei soli periodi ON
    private long    istanteInizioGaraMs    = -1;      // -1 = gara attualmente ferma
    private boolean garaMaiAttivata        = true;
    private int     voleeGaraCompletate    = 0;
    private int     voleeProvaCompletate   = 0;
    private int     emergenzeTotali        = 0;
    private int     recuperiTotali         = 0;
    private int     maxParteRaggiunta      = 1;

    public GestoreLog(ArcherySoftwareMain app) {
        this.app = app;
    }

    // ── API pubblica ──────────────────────────────────────────────────────────

    public void scriviLog(String messaggio) {
        if (!app.isGaraInCorso) return;
        // Calcola la riga sul thread chiamante (EDT), poi delega la scrittura
        accodaRigaGrezza(formattaRiga(messaggio));
    }

    /**
     * Scrittura non soggetta al filtro "Gara in Corso": usata solo per le righe
     * di cornice della sessione software (avvio, chiusura, riepilogo), che devono
     * essere registrate anche a gara ferma.
     */
    private void scriviLogForzato(String riga) {
        accodaRigaGrezza(riga);
    }

    // FIX #3b – La scrittura su disco avviene sempre fuori dall'EDT
    private void accodaRigaGrezza(String riga) {
        esecutoreLog.submit(() -> {
            try {
                assicuraWriterAperto();
                ruotaSeNecessario();
                writerCorrente.write(riga);
                writerCorrente.newLine();
                writerCorrente.flush();
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Errore scrittura log", e);
            }
        });
    }

    // ── Sessione software: apertura, transizioni gara, riepilogo di chiusura ──

    /** Registra l'istante di avvio del software (scritto subito su file). */
    public void registraAvvioSoftware() {
        istanteAvvioSoftware = timestampCompleto();
        scriviLogForzato("");
        scriviLogForzato(GestoreLingua.tf("log.software.avvio", istanteAvvioSoftware));
    }

    /** Da chiamare quando "Gara in Corso" passa a ON: apre un intervallo di gara. */
    public void registraGaraOn() {
        garaMaiAttivata = false;
        istanteInizioGaraMs = System.currentTimeMillis();
    }

    /** Da chiamare quando "Gara in Corso" passa a OFF: chiude l'intervallo attivo. */
    public void registraGaraOff() {
        if (istanteInizioGaraMs >= 0) {
            durataGaraAccumulataMs += System.currentTimeMillis() - istanteInizioGaraMs;
            istanteInizioGaraMs = -1;
        }
    }

    /**
     * Scrive la cornice di chiusura e il riepilogo della gara effettiva.
     * Va chiamato prima di {@link #chiudi()}.
     */
    public void scriviRiepilogoChiusura() {
        finalizzaSessione();
        for (String riga : costruisciRigheRiepilogo()) scriviLogForzato(riga);
    }

    /** Chiude l'eventuale intervallo di gara aperto e aggiorna l'ultima parte raggiunta. */
    void finalizzaSessione() {
        registraGaraOff(); // se il software si chiude a gara ancora attiva
        if (app.spinParte != null) {
            maxParteRaggiunta = Math.max(maxParteRaggiunta, (int) app.spinParte.getValue());
        }
    }

    /**
     * Costruisce le righe del riepilogo di chiusura senza effetti collaterali.
     * Estratto per permettere ai test di verificarne il contenuto.
     */
    java.util.List<String> costruisciRigheRiepilogo() {
        java.util.List<String> righe = new java.util.ArrayList<>();
        righe.add("");
        righe.add(GestoreLingua.tf("log.software.chiusura", timestampCompleto()));
        righe.add(GestoreLingua.t("log.riepilogo.titolo"));
        if (garaMaiAttivata) {
            righe.add(GestoreLingua.t("log.riepilogo.nogara"));
        } else {
            righe.add(GestoreLingua.tf("log.riepilogo.durata", formattaDurata(durataGaraAccumulataMs)));
            righe.add(GestoreLingua.tf("log.riepilogo.parti", maxParteRaggiunta));
            righe.add(GestoreLingua.tf("log.riepilogo.volee", voleeGaraCompletate));
            righe.add(GestoreLingua.tf("log.riepilogo.prova", voleeProvaCompletate));
            righe.add(GestoreLingua.tf("log.riepilogo.emergenze", emergenzeTotali));
            righe.add(GestoreLingua.tf("log.riepilogo.recuperi", recuperiTotali));
        }
        righe.add("=========================================");
        return righe;
    }

    /** Chiude il writer e attende la fine dei task pendenti. Chiamare prima di System.exit(). */
    public void chiudi() {
        esecutoreLog.submit(() -> {
            if (writerCorrente != null) {
                try {
                    writerCorrente.flush();
                    writerCorrente.close();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Errore chiusura writer log", e);
                } finally {
                    writerCorrente = null;
                }
            }
        });
        esecutoreLog.shutdown();
        try {
            esecutoreLog.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void inizializzaSessione() {
        scriviLog("\n");
        scriviLog("=========================================");
        scriviLog(GestoreLingua.t("log.session.start"));
        scriviLog("=========================================");
    }

    public void logInizioVolee() {
        int v = (int) app.spinVolee.getValue();
        if (app.isGaraInCorso && app.spinParte != null) {
            maxParteRaggiunta = Math.max(maxParteRaggiunta, (int) app.spinParte.getValue());
        }
        scriviLog("\n");
        if (v == 0) {
            scriviLog(GestoreLingua.tf("log.volee.prova.start", app.attualeVoleeProva));
        } else {
            if (v == 1) {
                int parte = (app.spinParte != null) ? (int) app.spinParte.getValue() : 1;
                scriviLog(GestoreLingua.tf("log.parte.start", parte));
                scriviLog("\n");
            }
            scriviLog(GestoreLingua.tf("log.volee.start", v));
        }
    }

    public void logFineVolee() {
        int v = (int) app.spinVolee.getValue();
        if (app.isGaraInCorso) {
            if (v == 0) voleeProvaCompletate++;
            else        voleeGaraCompletate++;
        }
        if (v == 0) {
            scriviLog(GestoreLingua.tf("log.volee.prova.end", app.attualeVoleeProva));
        } else {
            scriviLog(GestoreLingua.tf("log.volee.end", v));
        }
        scriviLog("\n");
    }

    public void logCambioTurnoLineare() {
        String mod = String.valueOf(app.comboTurni.getSelectedItem());
        if (mod.equals("- Nessuno -") || mod.equals("ABC")) {
            scriviLog(GestoreLingua.tf("log.turno.unico", mod));
        } else {
            String[] gruppi = mod.split(" - ");
            int nGruppi = gruppi.length;
            int offset = app.indicePartenza % nGruppi;
            int indiceTurnoAttuale = app.turnoCorrente - 1;
            String gruppoAttivo = gruppi[(indiceTurnoAttuale + offset) % nGruppi];
            scriviLog(GestoreLingua.tf("log.turno.multi", app.turnoCorrente, app.totaleTurni, gruppoAttivo));
        }
    }

    public void logTurnoScontro(String nomeArciere) {
        scriviLog(GestoreLingua.tf("log.scontro.start", nomeArciere));
    }

    public void logEmergenza(boolean attivata) {
        if (attivata && app.isGaraInCorso) emergenzeTotali++;
        scriviLog("\n");
        scriviLog(GestoreLingua.t(attivata ? "log.emergenza.on" : "log.emergenza.off"));
        scriviLog("\n");
    }

    public void logInno(String stato) {
        switch (stato) {
            case "ATTIVATA"    -> scriviLog(GestoreLingua.t("log.inno.attivata"));
            case "DISATTIVATA" -> scriviLog(GestoreLingua.t("log.inno.disattivata"));
            case "PARTITO"     -> scriviLog(GestoreLingua.t("log.inno.partito"));
            case "FINITO"      -> scriviLog(GestoreLingua.t("log.inno.finito"));
        }
    }

    public void logModificaManualeTempo(int vecchio, int nuovo, String contesto) {
        scriviLog(GestoreLingua.tf("log.manual.time", contesto, vecchio, nuovo));
    }

    public void logRecupero(String stato) {
        // "INIZIATO" segna l'inizio effettivo del tiro di recupero (sia immediato
        // sia da prenotazione): è il momento giusto per contarne uno.
        if ("INIZIATO".equals(stato) && app.isGaraInCorso) recuperiTotali++;
        String statoTradotto = traduciStatoRecupero(stato);
        if (stato.equals("ATTIVATO (40s)") || stato.equals("PRENOTATO")) scriviLog("\n");
        scriviLog(GestoreLingua.tf("log.recupero.stato", statoTradotto));
        if (stato.equals("CONCLUSO") || stato.contains("ANNULLATO")) scriviLog("\n");
    }

    public void logNotificaParte(String messaggio) {
        String messaggioTradotto = traduciNotifica(messaggio);
        scriviLog("\n");
        scriviLog(">>> " + messaggioTradotto + " <<<");
        scriviLog("\n");
    }

    // ── Helpers interni (eseguiti sull'EDT, prima di accodare) ────────────────

    private String timestampCompleto() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String formattaDurata(long ms) {
        long s = ms / 1000;
        long h = s / 3600, m = (s % 3600) / 60, sec = s % 60;
        if (h > 0) return String.format("%dh %02dm %02ds", h, m, sec);
        if (m > 0) return String.format("%dm %02ds", m, sec);
        return String.format("%ds", sec);
    }

    private String formattaRiga(String messaggio) {
        if (messaggio.equals("\n")) return "";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        int parte = (app.spinParte != null) ? (int) app.spinParte.getValue() : 1;
        String prefissoParte = GestoreLingua.t("log.prefix.parte");
        return "[" + timestamp + "][" + prefissoParte + " " + parte + "] " + messaggio;
    }

    // ── Helpers interni (eseguiti sul thread LogWriter) ───────────────────────

    /** Apre il writer se non è già aperto o se il file è cambiato. */
    private void assicuraWriterAperto() throws IOException {
        String percorso = percorsoFileDiLog();
        if (writerCorrente == null || !percorso.equals(percorsoCorrente)) {
            if (writerCorrente != null) {
                try { writerCorrente.close(); } catch (IOException ignored) {}
            }
            percorsoCorrente = percorso;
            writerCorrente = new BufferedWriter(new FileWriter(percorso, true));
        }
    }

    /**
     * FIX #3c – Se il file supera MAX_LOG_BYTES lo rinomina con un timestamp
     * e apre un file nuovo, evitando la crescita illimitata su disco.
     */
    private void ruotaSeNecessario() throws IOException {
        File file = new File(percorsoCorrente);
        if (!file.exists() || file.length() < MAX_LOG_BYTES) return;

        // Chiude il writer corrente prima di rinominare
        try { writerCorrente.close(); } catch (IOException ignored) {}
        writerCorrente = null;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File archiviato = new File(file.getParent(), "ArrowClock_Log_" + timestamp + ".txt");
        if (!file.renameTo(archiviato)) {
            LOG.warning("Impossibile ruotare il file di log: " + file.getAbsolutePath());
        }

        // Riapre un writer sul file nuovo (vuoto)
        writerCorrente = new BufferedWriter(new FileWriter(percorsoCorrente, true));
        LOG.info("Log ruotato: " + archiviato.getName());
    }

    /**
     * FIX #4 – Verifica che il percorso ricavato dal ClassLoader sia
     * effettivamente scrivibile. Se non lo è, usa la home utente.
     */
    private String percorsoFileDiLog() {
        String baseDir = ottieniPercorsoBaseScrivibile();
        File logDir = new File(baseDir, "ArrowClock_Logs");
        if (!logDir.exists() && !logDir.mkdirs()) {
            LOG.warning("Impossibile creare la cartella log: " + logDir.getAbsolutePath());
        }
        return logDir.getAbsolutePath() + File.separator + "ArrowClock_Log.txt";
    }

    // FIX #4 – Restituisce un percorso base garantito scrivibile.
    private static String ottieniPercorsoBaseScrivibile() {
        // 1° tentativo: cartella del JAR
        try {
            File jarDir = new File(
                GestoreLog.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            ).getParentFile();
            if (jarDir != null && jarDir.canWrite()) {
                return jarDir.getAbsolutePath();
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Impossibile ricavare la cartella del JAR", e);
        }

        // 2° tentativo: home utente (sicura, sempre scrivibile)
        String home = System.getProperty("user.home");
        if (home != null && new File(home).canWrite()) {
            LOG.info("Percorso log ricaduto sulla home utente: " + home);
            return home;
        }

        // 3° tentativo: directory temporanea di sistema
        String tmp = System.getProperty("java.io.tmpdir");
        LOG.warning("Percorso log ricaduto sulla directory temporanea: " + tmp);
        return tmp;
    }

    // ── Traduttori ────────────────────────────────────────────────────────────

    private String traduciStatoRecupero(String stato) {
        return switch (stato) {
            case "INIZIATO"               -> GestoreLingua.t("log.state.iniziato");
            case "ATTIVATO (40s)"         -> GestoreLingua.t("log.state.attivato");
            case "PRENOTATO"              -> GestoreLingua.t("log.state.prenotato");
            case "CONCLUSO"               -> GestoreLingua.t("log.state.concluso");
            case "ANNULLATO"              -> GestoreLingua.t("log.state.annullato");
            case "ANNULLATO (Reset Gara)" -> GestoreLingua.t("log.state.annullato.reset");
            default -> stato;
        };
    }

    private String traduciNotifica(String messaggio) {
        if (messaggio.equals("FINE VOLÉE DI PROVA")) {
            return GestoreLingua.t("log.notifica.fine_prova");
        }
        if (messaggio.startsWith("FINE PARTE ")) {
            int p = Integer.parseInt(messaggio.replace("FINE PARTE ", ""));
            return GestoreLingua.tf("log.notifica.fine_parte", p);
        }
        if (messaggio.startsWith("AGGIUNTI 40s")) {
            String tempo = messaggio.substring(messaggio.lastIndexOf(": ") + 2).replace("s)", "");
            return GestoreLingua.tf("log.notifica.add40", Integer.parseInt(tempo));
        }
        return messaggio;
    }
}
