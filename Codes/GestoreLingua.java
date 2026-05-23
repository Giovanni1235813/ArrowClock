import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Registro centralizzato di tutte le stringhe localizzabili.
 * Pattern: Language Manager (alternativa leggera a ResourceBundle).
 *
 * Tutte le chiavi sono registrate staticamente nel blocco di inizializzazione.
 * Per aggiungere una nuova lingua basta aggiungere la costante enum e
 * una terza colonna in ogni chiamata a r().
 */
public class GestoreLingua {

    public enum Lingua { IT, EN }

    private static Lingua linguaCorrente = Lingua.EN;

    private static final Map<String, Map<Lingua, String>> dizionario = new HashMap<>();

    static {
        // ── Toolbar ──────────────────────────────────────────────────────────
        r("btn.lingua",             "Lingua (L)",                               "Language (L)");
        r("btn.gara.off",           "Gara in Corso: OFF",                   "Match in Progress: OFF");
        r("btn.gara.on",            "Gara in Corso: ON",                    "Match in Progress: ON");
        r("btn.formato.sec",        "FORMATO: SEC (M)",                     "FORMAT: SEC (M)");
        r("btn.formato.mmss",       "FORMATO: MM:SS (M)",                   "FORMAT: MM:SS (M)");
        r("btn.formato.inv",        "FORMATO: INVISIBILE (M)",              "FORMAT: INVISIBLE (M)");
        r("btn.suono.on",           "SUONO: ON (S)",                        "SOUND: ON (S)");
        r("btn.suono.off",          "SUONO: OFF (S)",                       "SOUND: OFF (S)");
        r("btn.tema",               "Tema Scuro/Chiaro (C)",                "Dark/Light Theme (C)");
        r("btn.display.off",        "Display Turni: OFF (D)",               "Turn Display: OFF (D)");
        r("btn.display.on",         "Display Turni: ON (D)",                "Turn Display: ON (D)");
        r("btn.idmonitor",          "MOSTRA ID MONITOR (I)",                "SHOW MONITOR ID (I)");
        r("lbl.sux",                " Su: ",                                " On: ");

        // ── Pulsanti di controllo ─────────────────────────────────────────────
        r("btn.start",
            "<html><center>START<br>(Spazio)</center></html>",
            "<html><center>START<br>(Space)</center></html>");
        r("btn.salta",
            "<html><center>SALTA<br>(Spazio)</center></html>",
            "<html><center>SKIP<br>(Space)</center></html>");
        r("btn.emergenza",
            "<html><center>EMERGENZA<br>(Enter)</center></html>",
            "<html><center>EMERGENCY<br>(Enter)</center></html>");
        r("btn.riprendi",
            "<html><center>RIPRENDI<br>(Enter)</center></html>",
            "<html><center>RESUME<br>(Enter)</center></html>");
        r("btn.recupero",
            "<html><center>RECUPERO<br>(R)</center></html>",
            "<html><center>RECOVERY<br>(R)</center></html>");
        r("btn.recupero.add40",
            "<html><center>+40s<br>(R)</center></html>",
            "<html><center>+40s<br>(R)</center></html>");
        r("btn.recupero.prenotato",
            "<html><center>RECUPERO<br>(R)<br><font color='red'>&#9650;</font></center></html>",
            "<html><center>RECOVERY<br>(R)<br><font color='red'>&#9650;</font></center></html>");
        r("btn.reset",
            "<html><center>RESET GARA<br>(G)</center></html>",
            "<html><center>RESET MATCH<br>(G)</center></html>");
        r("btn.fischio",
            "<html><center>FISCHIO<br>(F)</center></html>",
            "<html><center>WHISTLE<br>(F)</center></html>");
        r("btn.reimposta",
            "<html><center>REIMPOSTA<br>TEMPO<br>(Shift)</center></html>",
            "<html><center>RESET<br>TIME<br>(Shift)</center></html>");

        // ── Turni ─────────────────────────────────────────────────────────────
        r("btn.nomi",               "Nomi Squadre (N)",                     "Team Names (N)");
        r("btn.alterna.sx",         "Inizia Sinistra (T)",                  "Start Left (T)");
        r("btn.alterna.dx",         "Inizia Destra (T)",                    "Start Right (T)");
        r("btn.turno.disabilitato", "Disabilitato (T)",                     "Disabled (T)");
        r("btn.turno.unico",        "Turno Unico (T)",                      "Single Turn (T)");
        r("btn.turno.inizia",       "Inizia: %s (T)",                       "Starts: %s (T)");

        // ── Etichette statiche ────────────────────────────────────────────────
        r("lbl.t1",                 "T1 - Preparazione Rosso (secondi):",   "T1 - Red Preparation (seconds):");
        r("lbl.t2",                 "T2 - Tempo Tiro Totale (secondi):",    "T2 - Total Shooting Time (seconds):");
        r("lbl.t3",                 "T3 - Avviso Giallo (secondi):",        "T3 - Yellow Warning (seconds):");
        r("lbl.voleeprova",         "Volée di Prova:",                      "Trial End:");
        r("lbl.voleecorrente",      "Volée Corrente:",                      "Current End:");
        r("lbl.parte",              "Parte numero:",                        "Part number:");
        r("lbl.modalita",           "Seleziona Modalità:",                  "Select Mode:");
        r("lbl.tipo",               "Tipo:",                                "Type:");
        r("lbl.frecce",             "Frecce per Arciere:",                  "Arrows per Archer:");
        r("lbl.secfreccia",         "Secondi a Freccia:",                   "Seconds per Arrow:");

        // ── Titoli pannelli (TitledBorder) ────────────────────────────────────
        r("panel.tempi",            "Gestione Tempi",                       "Time Management");
        r("panel.display",          "Visualizzazione Arciere",              "Archer Display");
        r("panel.preset",           "Modalità Gara (Preset)",               "Match Mode (Preset)");
        r("panel.controllo",        "Controllo Gara",                       "Match Control");
        r("panel.scontro",          "Impostazioni Scontro / Shoot-Off",     "Match / Shoot-Off Settings");
        r("panel.lineare",          "Impostazioni Freccia",                 "Arrow Settings");

        // ── Testi display ─────────────────────────────────────────────────────
        r("display.recupero",       "RECUPERO",                             "RECOVERY");
        r("display.stop",           "STOP",                                 "STOP");

        // ── Dialog Nomi ───────────────────────────────────────────────────────
        r("dialog.nomi.header",     "IMPOSTA NOMI SQUADRE / ARCIERI",       "SET TEAM / ARCHER NAMES");
        r("dialog.nomi.sx",         "Tabellone di Sinistra:",               "Left Board:");
        r("dialog.nomi.dx",         "Tabellone di Destra:",                 "Right Board:");
        r("dialog.nomi.confirm",    "CONFERMA (Enter)",                     "CONFIRM (Enter)");
        r("dialog.nomi.cancel",     "ANNULLA (Esc)",                        "CANCEL (Esc)");

        // ── Dialog Tempo ──────────────────────────────────────────────────────
        r("dialog.tempo.preview",   "Nuovo Tempo Rimasto: ",                "New Remaining Time: ");
        r("dialog.tempo.frecce",    "Frecce rimaste:",                      "Remaining arrows:");
        r("dialog.tempo.sec",       "Sec/Freccia:",                         "Sec/Arrow:");
        r("dialog.tempo.calcola",   "Applica Calcolo",                      "Apply Calculation");
        r("dialog.tempo.salva",     "SALVA E CHIUDI (Enter)",               "SAVE & CLOSE (Enter)");
        r("dialog.tempo.annulla",   "ANNULLA (Esc)",                        "CANCEL (Esc)");

        // ── Voci ComboBox (il modello mantiene la chiave IT; il renderer traduce) ──
        r("combo.Manuale",      "Manuale",      "Manual");
        r("combo.SCONTRO",      "SCONTRO",      "MATCH");
        r("combo.- Nessuno -",  "- Nessuno -",  "- None -");
        r("combo.INDIVIDUALE",  "INDIVIDUALE",  "INDIVIDUAL");
        r("combo.SQUADRE",      "SQUADRE",      "TEAMS");

        // ── Log Eventi ────────────────────────────────────────────────────────
        r("log.session.start",  "=== NUOVA SESSIONE ARROWCLOCK AVVIATA ===", "=== NEW ARROWCLOCK SESSION STARTED ===");
        r("log.volee.prova.start", "--- INIZIO VOLÉE DI PROVA %d ---", "--- TRIAL END %d STARTED ---");
        r("log.parte.start",    ">>> INIZIO PARTE %d <<<", ">>> PART %d STARTED <<<");
        r("log.volee.start",    "--- INIZIO VOLÉE %d ---", "--- END %d STARTED ---");
        r("log.volee.prova.end", "--- FINE VOLÉE DI PROVA %d ---", "--- TRIAL END %d FINISHED ---");
        r("log.volee.end",      "--- FINE VOLÉE %d ---", "--- END %d FINISHED ---");
        r("log.turno.unico",    "Turno Unico: Inizia a tirare il gruppo %s", "Single Turn: Group %s starts shooting");
        r("log.turno.multi",    "Turno %d di %d: Inizia a tirare il gruppo %s", "Turn %d of %d: Group %s starts shooting");
        r("log.scontro.start",  "Scontro: Inizia a tirare %s", "Match: %s starts shooting");
        r("log.emergenza.on",   "!!! EMERGENZA ATTIVATA !!! Gara interrotta.", "!!! EMERGENCY ACTIVATED !!! Match paused.");
        r("log.emergenza.off",  "!!! EMERGENZA RISOLTA !!! Gara ripresa.", "!!! EMERGENCY RESOLVED !!! Match resumed.");
        r("log.manual.time",    "[MODIFICA MANUALE] %s: tempo variato da %ds a %ds", "[MANUAL EDIT] %s: time changed from %ds to %ds");
        r("log.recupero.stato", "[RECUPERO MATERIALE] Stato: %s", "[EQUIPMENT RECOVERY] Status: %s");

        // ── Componenti hardcoded dei Log ──────────────────────────────────────
        r("log.prefix.parte",           "Parte",                        "Part");

        // ── Stati Recupero Materiale ──────────────────────────────────────────
        r("log.state.iniziato",         "INIZIATO",                     "STARTED");
        r("log.state.attivato",         "ATTIVATO (40s)",               "ACTIVATED (40s)");
        r("log.state.prenotato",        "PRENOTATO",                    "BOOKED");
        r("log.state.concluso",         "CONCLUSO",                     "FINISHED");
        r("log.state.annullato",        "ANNULLATO",                    "CANCELLED");
        r("log.state.annullato.reset",  "ANNULLATO (Reset Gara)",       "CANCELLED (Match Reset)");

        // ── Notifiche di Sistema ──────────────────────────────────────────────
        r("log.notifica.fine_prova",    "FINE VOLÉE DI PROVA",          "TRIAL END FINISHED");
        r("log.notifica.fine_parte",    "FINE PARTE %d",                "PART %d FINISHED");
        r("log.notifica.add40",         "AGGIUNTI 40s AL RECUPERO (Tempo Totale: %ds)", "ADDED 40s TO RECOVERY (Total Time: %ds)");
    }

    // ── API pubblica ──────────────────────────────────────────────────────────

    /** Restituisce true se esiste una traduzione per la chiave data. */
    public static boolean ha(String chiave) {
        return dizionario.containsKey(chiave);
    }

    /** Restituisce la traduzione corrente per la chiave data. */
    public static String t(String chiave) {
        Map<Lingua, String> mappa = dizionario.get(chiave);
        if (mappa == null) return "[?" + chiave + "?]";
        String testo = mappa.get(linguaCorrente);
        return testo != null ? testo : "[?" + chiave + "?]";
    }

    /** Versione con formattazione printf-style: GestoreLingua.tf("btn.turno.inizia", "AB") */
    public static String tf(String chiave, Object... args) {
        return String.format(t(chiave), args);
    }

    public static void setLingua(Lingua lingua)  { linguaCorrente = lingua; }
    public static Lingua getLingua()              { return linguaCorrente; }
    public static boolean isItaliano()            { return linguaCorrente == Lingua.IT; }

    /** Alterna tra IT e EN. */
    public static void toggle() {
        linguaCorrente = isItaliano() ? Lingua.EN : Lingua.IT;
    }

    // ── Registrazione interna ─────────────────────────────────────────────────

    private static void r(String chiave, String it, String en) {
        Map<Lingua, String> mappa = new EnumMap<>(Lingua.class);
        mappa.put(Lingua.IT, it);
        mappa.put(Lingua.EN, en);
        dizionario.put(chiave, mappa);
    }
}
