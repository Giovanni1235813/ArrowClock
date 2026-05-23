import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gestisce la scrittura del log partita su file.
 * Unica responsabilità: tradurre gli eventi di gara in righe di testo
 * e salvarle nel file di log dell'utente.
 */
public class GestoreLog {

    private final ArcherySoftwareMain app;

    public GestoreLog(ArcherySoftwareMain app) {
        this.app = app;
    }

    public void scriviLog(String messaggio) {
        if (!app.isGaraInCorso) return;

        String rigaLog;
        if (messaggio.equals("\n")) {
            rigaLog = "";
        } else {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            int parte = (app.spinParte != null) ? (int) app.spinParte.getValue() : 1;

            // Richiama dinamicamente la traduzione della parola "Parte"
            String prefissoParte = GestoreLingua.t("log.prefix.parte");
            rigaLog = "[" + timestamp + "][" + prefissoParte + " " + parte + "] " + messaggio;
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(percorsoFileDiLog(), true))) {
            out.println(rigaLog);
        } catch (IOException e) {
            System.err.println("Errore scrittura log: " + e.getMessage());
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
        scriviLog("\n");
        scriviLog(GestoreLingua.t(attivata ? "log.emergenza.on" : "log.emergenza.off"));
        scriviLog("\n");
    }

    public void logModificaManualeTempo(int vecchio, int nuovo, String contesto) {
        scriviLog(GestoreLingua.tf("log.manual.time", contesto, vecchio, nuovo));
    }

    public void logRecupero(String stato) {
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

    private String percorsoFileDiLog() {
        File logDir = new File(System.getProperty("user.home"), "ArrowClock_Logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        return logDir.getAbsolutePath() + File.separator + "ArrowClock_Log.txt";
    }

    // --- Metodi di supporto per intercettare i parametri hardcoded dai Comandi ---

    private String traduciStatoRecupero(String stato) {
        return switch (stato) {
            case "INIZIATO"               -> GestoreLingua.t("log.state.iniziato");
            case "ATTIVATO (40s)"         -> GestoreLingua.t("log.state.attivato");
            case "PRENOTATO"              -> GestoreLingua.t("log.state.prenotato");
            case "CONCLUSO"               -> GestoreLingua.t("log.state.concluso");
            case "ANNULLATO"              -> GestoreLingua.t("log.state.annullato");
            case "ANNULLATO (Reset Gara)" -> GestoreLingua.t("log.state.annullato.reset");
            default -> stato; // Fallback di sicurezza
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
            // Estrae il tempo inserito dinamicamente dai Comandi
            String tempo = messaggio.substring(messaggio.lastIndexOf(": ") + 2).replace("s)", "");
            return GestoreLingua.tf("log.notifica.add40", Integer.parseInt(tempo));
        }
        return messaggio; // Fallback
    }
}
