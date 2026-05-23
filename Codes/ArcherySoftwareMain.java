import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principale dell'applicazione ArrowClock.
 * Funge da contenitore dello stato condiviso tra tutti i comandi.
 * Il costruttore orchestra l'avvio nell'ordine corretto,
 * delegando ogni responsabilità all'oggetto specifico.
 *
 * Nota di design (Clean Code, cap. 3 e 10):
 * Questa classe non fa nulla da sola: contiene solo lo stato (campi)
 * e delega tutta la logica ai Comandi e ai collaboratori di istanza.
 */
@SuppressWarnings("SpellCheckingInspection")
public class ArcherySoftwareMain {

    // ─── Collaboratori di istanza ────────────────────────────────────────────
    GestoreLog  gestoreLog;
    MotoreTimer motoreTimer;

    // ─── Lista display esterni ───────────────────────────────────────────────
    final List<DisplayArciere> archerDisplays = new ArrayList<>();

    // ─── Componenti finestra operatore ──────────────────────────────────────
    JFrame   operatorFrame;
    JPanel   customTitleBar;
    JLabel   lblTitleBar;
    JSpinner spinT1, spinT2, spinT3, spinSecFrecciaLineare;
    JLabel   lblSecretLeft, lblSecretRight, lblDesigned;

    // ─── Miniatura display ───────────────────────────────────────────────────
    JPanel    miniaturaContainer;
    CardLayout miniaturaCardLayout;
    JPanel    minLightSingolo;
    JLabel    minTimerSingolo, minTurniSingolo;
    JPanel    minLightSx, minLightDx;
    JLabel    minTimerSx, minTimerDx;
    JLabel    minTurniSx, minTurniDx;

    // NUOVI CAMPI: Memorizzano i font dell'anteprima calcolati al volo al resize
    public Font fontMinNumeri;
    public Font fontMinStop;
    public Font fontMinTesti;

    JPanel minIdPanel;
    JLabel minIdLabel;

    // ─── Combo e pannelli preset ─────────────────────────────────────────────
    JComboBox<String> comboPreset, comboScontroType, comboTurni;
    JPanel scontroOptionsPanel, linearOptionsPanel;
    JSpinner spinFrecce, spinSecFreccia, spinVolee;

    // ─── Bottoni controllo ───────────────────────────────────────────────────
    JButton btnAlternaMetata;
    JButton btnImpostaNomi;
    String  nomeSx = "A";
    String  nomeDx = "B";
    JButton      btnStartSkip, btnEmergenza, btnStopReset, btnFischio, btnRecupero, btnFormatoTempo;
    JButton      btnAdd40s;
    JPanel       add40Container;

    // ─── Label emergenza ─────────────────────────────────────────────────────
    JLabel minEmergenzaTimeSingolo, minEmergenzaTimeSx, minEmergenzaTimeDx;
    final List<JLabel[]> emergenzaLabelsList = new ArrayList<>();

    // ─── Toolbar ─────────────────────────────────────────────────────────────
    JButton        btnSuono, btnTema;
    JToggleButton  btnToggleTurniSpecial, btnIdentificaMonitor;
    JComboBox<String> comboSelettoreDisplay;

    // ─── i18n – pulsante lingua ───────────────────────────────────────────────
    JButton       btnLingua;
    JLabel        lblSuMonitor;

    // ─── i18n – etichette statiche (aggiornate da ComandoAggiornaTesti) ──────
    JLabel lblT1desc, lblT2desc, lblT3desc;
    JLabel lblVoleeProvaDesc, lblVoleeCorrenteDesc, lblParteDesc;
    JLabel lblModalitaDesc;
    JLabel lblTipoDesc, lblFrecceDesc, lblSecFrecciaDesc;
    JLabel lblSecFrecciaLineareDesc;

    // ─── i18n – bordi titolati (aggiornati da ComandoAggiornaTesti) ──────────
    TitledBorder borderTempi, borderDisplay, borderPreset, borderControllo;
    TitledBorder borderScontroPanel, borderLinearePanel;

    // ─── Timer Swing ─────────────────────────────────────────────────────────
    Timer   countdownTimer;
    Timer   flashTimer;
    long    lastTickTime        = 0;
    double  accumulatoreSecondi = 0.0;

    // ─── Stato applicazione ──────────────────────────────────────────────────
    boolean emergenzaGiallaAttiva = true;
    boolean blinkState            = false;
    int     statoFormatoTempo     = 0;    // 0=Secondi, 1=MM:SS, 2=Invisibile
    boolean isDarkMode            = true;
    boolean isSuonoAttivo         = true;

    Fase  faseSalvata;
    int   tempoSalvatoSx, tempoSalvatoDx;
    Color coloreSalvato;

    Fase  fasePreRecupero;
    int   tempoSxPreRecupero, tempoDxPreRecupero;
    Color colorePreRecupero;

    Fase    faseAttuale           = Fase.ATTESA;
    boolean iniziaConPrimaParte   = true;
    int     indicePartenza        = 0;
    int     turnoCorrente         = 1;
    int     totaleTurni           = 1;

    boolean recuperoPrenotato = false;

    boolean isScontroMode       = false;
    int     timeRemainingSx     = 0;
    int     timeRemainingDx     = 0;
    int     scambiEffettuati    = 0;
    int     scambiTotaliScontro = 0;
    Image   appIcon             = null;

    JSpinner      spinVoleeProva;
    JSpinner      spinParte;
    JToggleButton btnGaraInCorso;
    boolean isGaraInCorso     = false;
    int     attualeVoleeProva = 1;

    // ─── Costruttore ─────────────────────────────────────────────────────────

    public ArcherySoftwareMain() {
        impostaColoriDisabilitatiUI();
        caricaIconaApp();

        MotoreAudio.istanza().avviaMotoreAudioSilenzioso();

        this.gestoreLog  = new GestoreLog(this);
        this.motoreTimer = new MotoreTimer(this);
        motoreTimer.configura();

        createArcherWindow();
        new CostruttoreOperatore(this).costruisci();
        new ComandoAggiornaTema(this).esegui();
        new GestoreScorciatoie(this).applica();
        new ComandoApplicaPreset(this, "Manuale").esegui();
        new ComandoReset(this).esegui();
        gestoreLog.inizializzaSessione();
    }

    // ─── Avvio ────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ArcherySoftwareMain::new);
    }

    // ─── Inizializzazione schermo ─────────────────────────────────────────────

    void createArcherWindow() {
        GraphicsEnvironment ge      = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[]    schermi = ge.getScreenDevices();

        if (schermi.length == 1) {
            aggiungiDisplayMonoschermo();
        } else {
            for (int i = 1; i < schermi.length; i++) {
                aggiungiDisplaySchermoPieno(schermi[i]);
            }
        }
    }

    private void aggiungiDisplayMonoschermo() {
        DisplayArciere da = new DisplayArciere();
        inizializzaLabelEmergenza(da, 60);
        if (appIcon != null) da.frame.setIconImage(appIcon);
        da.frame.setSize(1000, 600);
        da.frame.setLocation(50, 50);
        da.impostaFontGrandi(250, 250, 50);
        da.frame.setVisible(true);
        archerDisplays.add(da);
    }

    private void aggiungiDisplaySchermoPieno(GraphicsDevice schermo) {
        DisplayArciere da     = new DisplayArciere();
        Rectangle      bounds = schermo.getDefaultConfiguration().getBounds();
        inizializzaLabelEmergenza(da, 130);
        if (appIcon != null) da.frame.setIconImage(appIcon);
        da.frame.setUndecorated(true);
        da.frame.setLocation(bounds.x, bounds.y);
        da.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        da.impostaFontGrandi(250, 250, 50);
        da.frame.setVisible(true);
        archerDisplays.add(da);
    }

    void inizializzaLabelEmergenza(DisplayArciere da, int fontSize) {
        JLabel emSingolo = creaLabelEmergenza(fontSize);
        da.lightPanelSingolo.add(emSingolo, BorderLayout.NORTH);

        JLabel emSx = creaLabelEmergenza((int)(fontSize * 0.8));
        da.lightPanelSx.add(emSx, BorderLayout.NORTH);

        JLabel emDx = creaLabelEmergenza((int)(fontSize * 0.8));
        da.lightPanelDx.add(emDx, BorderLayout.NORTH);

        emergenzaLabelsList.add(new JLabel[]{ emSingolo, emSx, emDx });
    }

    private JLabel creaLabelEmergenza(int size) {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, size));
        label.setForeground(Color.BLACK);
        return label;
    }

    // ─── Setup UI di sistema ──────────────────────────────────────────────────

    private void impostaColoriDisabilitatiUI() {
        UIManager.put("Button.disabledText",       Color.DARK_GRAY);
        UIManager.put("ToggleButton.disabledText",  Color.DARK_GRAY);
    }

    private void caricaIconaApp() {
        try {
            java.net.URL iconURL = getClass().getResource("/logo.png");
            if (iconURL != null) {
                appIcon = new ImageIcon(iconURL).getImage();
                if (Taskbar.isTaskbarSupported()) {
                    Taskbar taskbar = Taskbar.getTaskbar();
                    if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                        taskbar.setIconImage(appIcon);
                    }
                }
            }
        } catch (Exception e) {
            // Icona non disponibile: si continua senza.
        }
    }
}
