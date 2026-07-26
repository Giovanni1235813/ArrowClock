import javax.swing.*;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Banco di prova per i test automatici della macchina a stati di ArrowClock.
 *
 * <p>Costruisce un {@link ArcherySoftwareMain} completo di tutti i widget Swing
 * che i comandi leggono/scrivono, ma SENZA aprire alcuna finestra ({@code JFrame}).
 * In questo modo i test girano ovunque, anche in ambienti senza schermo o scheda
 * audio (integrazione continua compresa).</p>
 *
 * <p>I fischi non vengono riprodotti: vengono solo contati tramite il gancio
 * {@link MotoreAudio#osservatoreFischiTest}. I secondi si fanno avanzare a mano
 * con {@link #tick(int)}.</p>
 */
public class BancoDiProva {

    public final ArcherySoftwareMain app;
    private final List<Integer> fischi = new ArrayList<>();

    public BancoDiProva() {
        MotoreAudio.modalitaTest = true;
        MotoreAudio.osservatoreFischiTest = fischi::add;

        app = new ArcherySoftwareMain(true);
        app.gestoreLog  = new GestoreLog(app);
        app.motoreTimer = new MotoreTimerFinto(app);

        // Timer Swing "inerti": esistono ma con ritardo enorme, non scattano mai da soli.
        app.countdownTimer = new javax.swing.Timer(10_000_000, e -> { });
        app.flashTimer     = new javax.swing.Timer(10_000_000, e -> { });

        costruisciWidget();

        // Stato iniziale coerente con l'avvio reale.
        new ComandoApplicaPreset(app, "Manuale").esegui();
        new ComandoReset(app).esegui();
        azzeraFischi();
    }

    // ── API di guida dei test ────────────────────────────────────────────────

    /** Applica un preset di gara (Manuale, INDOOR, OUTDOOR, SCONTRO ALTERNATO, SCONTRO SIMULTANEO, SHOOT-OFF ALTERNATO, SHOOT-OFF SIMULTANEO). */
    public BancoDiProva applicaPreset(String preset) {
        app.comboPreset.setSelectedItem(preset);
        new ComandoApplicaPreset(app, preset).esegui();
        azzeraFischi();
        return this;
    }

    /** Simula la pressione di START/SALTA (Spazio). */
    public void premiStart()      { new ComandoAvviaOSalta(app).esegui(); }
    /** Simula la pressione di EMERGENZA/RIPRENDI (Enter). */
    public void premiEmergenza()  { new ComandoEmergenza(app).esegui(); }
    /** Simula la pressione di RECUPERO (R). */
    public void premiRecupero()   { new ComandoRecupero(app).esegui(); }
    /** Simula la pressione di RESET GARA (G). */
    public void premiReset()      { new ComandoReset(app).esegui(); }

    /** Fa passare {@code secondi} secondi di conto alla rovescia. */
    public void tick(int secondi) {
        for (int i = 0; i < secondi; i++) new ComandoTickTimer(app).esegui();
    }

    // ── Verifica dei fischi ──────────────────────────────────────────────────

    /** Elenco dei gruppi di fischi emessi dall'ultimo azzeramento (es. [2, 1, 3]). */
    public List<Integer> fischi() { return new ArrayList<>(fischi); }
    public void azzeraFischi()    { fischi.clear(); }

    /** Chiude il banco: rimuove il gancio audio statico. */
    public void chiudi() {
        MotoreAudio.osservatoreFischiTest = null;
        MotoreAudio.modalitaTest = false;
        if (app.countdownTimer != null) app.countdownTimer.stop();
        if (app.flashTimer != null) app.flashTimer.stop();
    }

    // ── Costruzione dei widget (nessun JFrame) ───────────────────────────────

    private void costruisciWidget() {
        // Spinner tempi
        app.spinT1 = spinner(10, 0, 99, 1);
        app.spinT2 = spinner(120, 0, 999, 1);
        app.spinT3 = spinner(30, 0, 99, 1);
        app.spinSecFrecciaLineare = spinner(40, 10, 60, 5);
        app.spinFrecce = spinner(1, 1, 6, 1);
        app.spinSecFreccia = spinner(20, 10, 60, 5);
        app.spinVolee = spinner(0, 0, 999, 1);
        app.spinVoleeProva = spinner(2, 1, 4, 1);
        app.spinParte = spinner(1, 1, 9999, 1);
        app.spinFrecceRecupero = spinner(1, 1, 6, 1);
        app.spinSecFrecciaRecupero = spinner(40, 10, 60, 5);

        // Combo (versioni semplici: nei test non serve la tematizzazione)
        app.comboPreset = new JComboBox<>(new String[]{"Manuale", "INDOOR", "OUTDOOR", "SCONTRO ALTERNATO", "SCONTRO SIMULTANEO", "SHOOT-OFF ALTERNATO", "SHOOT-OFF SIMULTANEO"});
        app.comboScontroType = new JComboBox<>(new String[]{"INDIVIDUALE", "SQUADRE", "MIX-TEAM"});
        app.comboTurni = new JComboBox<>(new String[]{
                "- Nessuno -", "ABC", "ABC - DEF", "AB - CD", "AB - CD - EF",
                "A - B", "C - D", "A - B - C", "A - B - C - D",
                "A - B - C - D - E", "A - B - C - D - E - F"});
        app.comboTurni.setSelectedItem("AB - CD");
        app.comboSelettoreDisplay = new JComboBox<>(new String[]{"Monitor 1"});

        // Pulsanti di controllo
        app.btnStartSkip = new JButton();
        app.btnEmergenza = new JButton();
        app.btnRecupero = new JButton();
        app.btnStopReset = new JButton();
        app.btnFischio = new JButton();
        app.btnFormatoTempo = new JButton();
        app.btnTema = new JButton();
        app.btnLingua = new JButton();
        app.btnAlternaMetata = new JButton();
        app.btnImpostaNomi = new JButton();
        app.btnAdd40s = new JButton();

        // Toggle
        app.btnGaraInCorso = new JToggleButton();
        app.btnToggleTurniSpecial = new JToggleButton();
        app.btnIdentificaMonitor = new JToggleButton();
        app.btnInno = new JToggleButton();

        // Pannelli semaforo (miniatura operatore) e relative etichette
        app.minLightSingolo = new JPanel();
        app.minLightSx = new JPanel();
        app.minLightDx = new JPanel();
        app.minTimerSingolo = new JLabel();
        app.minTurniSingolo = new JLabel();
        app.minTimerSx = new JLabel();
        app.minTurniSx = new JLabel();
        app.minTimerDx = new JLabel();
        app.minTurniDx = new JLabel();
        app.minEmergenzaTimeSingolo = new JLabel();
        app.minEmergenzaTimeSx = new JLabel();
        app.minEmergenzaTimeDx = new JLabel();
        app.emergenzaLabelsList.add(new JLabel[]{new JLabel(), new JLabel(), new JLabel()});

        app.btn4Fischi = new JButton();
        
        // Contenitore +40s (CardLayout con EMPTY/BUTTON)
        app.add40Container = new JPanel(new CardLayout());
        app.add40Container.add(app.btn4Fischi, "EMPTY");
        app.add40Container.add(app.btnAdd40s, "BUTTON");

        // Wrapper opzioni preset (CardLayout LINEARE/SCONTRO/VUOTO) per ComandoApplicaPreset
        JPanel wrapper = new JPanel(new CardLayout());
        app.linearOptionsPanel = new JPanel();
        app.scontroOptionsPanel = new JPanel();
        wrapper.add(app.linearOptionsPanel, "LINEARE");
        wrapper.add(app.scontroOptionsPanel, "SCONTRO");
        wrapper.add(new JPanel(), "VUOTO");

        // Bottom Left (CardLayout)
        app.bottomLeftCardLayout = new CardLayout();
        app.bottomLeftContainer = new JPanel(app.bottomLeftCardLayout);
        app.bottomLeftContainer.add(new JPanel(), "PRESET");
        app.bottomLeftContainer.add(new JPanel(), "RECUPERO");

        // Miniatura (CardLayout) usata da ComandoApplicaLayoutMonitor
        app.miniaturaCardLayout = new CardLayout();
        app.miniaturaContainer = new JPanel(app.miniaturaCardLayout);
        app.miniaturaContainer.add(new JPanel(), "SINGOLO");
        app.miniaturaContainer.add(new JPanel(), "SCONTRO");
        app.miniaturaContainer.add(new JPanel(), "IDENTIFICAZIONE");
        app.miniaturaContainer.add(new JPanel(), "BANDIERA");
    }

    private static JSpinner spinner(int val, int min, int max, int step) {
        return new JSpinner(new SpinnerNumberModel(val, min, max, step));
    }
}