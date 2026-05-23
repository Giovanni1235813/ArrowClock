import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Costruisce l'intera interfaccia della finestra operatore.
 * Unica responsabilità: assemblare e collegare i componenti Swing
 * della finestra di controllo principale.
 */
public class CostruttoreOperatore {

    private final ArcherySoftwareMain app;

    public CostruttoreOperatore(ArcherySoftwareMain app) {
        this.app = app;
    }

    public void costruisci() {
        app.operatorFrame = new JFrame("ArrowClock");
        if (app.appIcon != null) app.operatorFrame.setIconImage(app.appIcon);
        app.operatorFrame.setUndecorated(true);
        app.operatorFrame.setSize(1150, 650);
        app.operatorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.operatorFrame.setLayout(new BorderLayout());

        app.operatorFrame.add(costruisciTitleBar(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.add(costruisciTop(), BorderLayout.NORTH);
        mainContent.add(costruisciCentro(), BorderLayout.CENTER);
        mainContent.add(costruisciBottom(), BorderLayout.SOUTH);
        app.operatorFrame.add(mainContent, BorderLayout.CENTER);

        applicaDefocusSuClick(app.operatorFrame.getContentPane());
        disabilitaEditSpinner();
        posizioneFinale();

        applicaListenerMiniatura();

        app.operatorFrame.setVisible(true);
        app.operatorFrame.requestFocusInWindow();
        new ComandoAggiornaBottoni(app).esegui();
    }

    // NEW METHODS: Responsive Font Update for the Miniature Panel
    private void applicaListenerMiniatura() {
        app.miniaturaContainer.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                aggiornaFontMiniatura();
            }
        });
    }

    private void aggiornaFontMiniatura() {
        int[] dim = MotoreFontDinamico.calcolaDimensioni(
                app.miniaturaContainer.getWidth(),
                app.miniaturaContainer.getHeight(),
                app.miniaturaContainer.getGraphicsConfiguration()
        );

        // Salviamo i font generati nei campi dell'applicazione
        app.fontMinNumeri = new Font("Arial", Font.BOLD, dim[0]);
        app.fontMinStop   = new Font("Arial", Font.BOLD, dim[1]);
        app.fontMinTesti  = new Font("Arial", Font.BOLD, dim[2]);

        // Eseguiamo l'applicazione immediata del font corretto in base allo stato attuale
        if (app.faseAttuale == Fase.EMERGENZA) {
            Font fTimerSide = new Font("Arial", Font.BOLD, (int)(app.fontMinStop.getSize() * 0.8));
            app.minTimerSingolo.setFont(app.fontMinStop);
            app.minTimerSx.setFont(fTimerSide);
            app.minTimerDx.setFont(fTimerSide);
        } else {
            Font fTimerSide = new Font("Arial", Font.BOLD, (int)(app.fontMinNumeri.getSize() * 0.8));
            app.minTimerSingolo.setFont(app.fontMinNumeri);
            app.minTimerSx.setFont(fTimerSide);
            app.minTimerDx.setFont(fTimerSide);
        }

        app.minTurniSingolo.setFont(app.fontMinTesti);
        app.minTurniSx.setFont(app.fontMinTesti);
        app.minTurniDx.setFont(app.fontMinTesti);

        if (app.minEmergenzaTimeSingolo != null) {
            Font fTurniSide = new Font("Arial", Font.BOLD, (int)(dim[2] * 0.8));
            app.minEmergenzaTimeSingolo.setFont(app.fontMinTesti);
            app.minEmergenzaTimeSx.setFont(fTurniSide);
            app.minEmergenzaTimeDx.setFont(fTurniSide);
        }
        if (app.minIdLabel != null) {
            app.minIdLabel.setFont(app.fontMinNumeri);
        }
    }

    // ─── Title bar ──────────────────────────────────────────────────────────

    private JPanel costruisciTitleBar() {
        app.customTitleBar = new JPanel(new BorderLayout());
        app.lblTitleBar = new JLabel("  ArrowClock-Controller");
        app.lblTitleBar.setFont(new Font("Arial", Font.BOLD, 12));
        app.customTitleBar.add(app.lblTitleBar, BorderLayout.WEST);
        app.customTitleBar.add(costruisciWindowControls(), BorderLayout.EAST);
        MouseAdapter drag = draggingListener();
        app.customTitleBar.addMouseListener(drag);
        app.customTitleBar.addMouseMotionListener(drag);
        return app.customTitleBar;
    }

    private JPanel costruisciWindowControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);

        JButton btnMin   = creaBottoneTitleBar(" _ ");
        JButton btnMax   = creaBottoneTitleBar(" [ ] ");
        JButton btnClose = creaBottoneTitleBar(" X ");

        btnMin.addActionListener(e -> app.operatorFrame.setState(Frame.ICONIFIED));
        btnMax.addActionListener(e -> toggleMassimizza(btnMax));
        btnClose.addActionListener(e -> System.exit(0));

        app.operatorFrame.addWindowStateListener(e ->
                btnMax.setText(((e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH)
                        ? " ] [ " : " [ ] "));

        controls.add(btnMin); controls.add(btnMax); controls.add(btnClose);
        return controls;
    }

    private JButton creaBottoneTitleBar(String testo) {
        JButton btn = new JButton(testo);
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return btn;
    }

    private void toggleMassimizza(JButton btnMax) {
        if (app.operatorFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            app.operatorFrame.setExtendedState(JFrame.NORMAL);
        } else {
            app.operatorFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private MouseAdapter draggingListener() {
        return new MouseAdapter() {
            Point pressPoint = null;
            @Override public void mousePressed(MouseEvent e) { pressPoint = e.getPoint(); }
            @Override public void mouseReleased(MouseEvent e) { pressPoint = null; }
            @Override public void mouseDragged(MouseEvent e) {
                if (pressPoint != null) {
                    Point curr = e.getLocationOnScreen();
                    app.operatorFrame.setLocation(curr.x - pressPoint.x, curr.y - pressPoint.y);
                }
            }
        };
    }

    // ─── Top area (logo + toolbar) ───────────────────────────────────────────

    private JPanel costruisciTop() {
        JPanel top = new JPanel(new BorderLayout());

        JLabel lblBrand = new JLabel("ArrowClock", SwingConstants.CENTER);
        lblBrand.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 32));
        lblBrand.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        top.add(lblBrand, BorderLayout.NORTH);
        top.add(costruisciToolbar(), BorderLayout.SOUTH);
        return top;
    }

    private JPanel costruisciToolbar() {
        // Container principale su due righe per evitare tagli su schermi piccoli
        JPanel toolbarContainer = new JPanel(new GridLayout(2, 1, 0, 5));
        toolbarContainer.setOpaque(false);

        // --- Prima riga ---
        JToolBar row1 = new JToolBar();
        row1.setFloatable(false); row1.setBorderPainted(false);
        row1.setOpaque(false); row1.setLayout(new FlowLayout(FlowLayout.CENTER));

        row1.add(costruisciBottoneGara());
        row1.addSeparator(new Dimension(30, 0));
        row1.add(costruisciBottoneFormato());
        row1.addSeparator(new Dimension(15, 0));
        row1.add(costruisciBottoneSuono());
        row1.addSeparator(new Dimension(15, 0));
        row1.add(costruisciBottoneTema());
        row1.addSeparator(new Dimension(15, 0));
        row1.add(costruisciBottoneLingua());

        // --- Seconda riga ---
        JToolBar row2 = new JToolBar();
        row2.setFloatable(false); row2.setBorderPainted(false);
        row2.setOpaque(false); row2.setLayout(new FlowLayout(FlowLayout.CENTER));

        row2.add(costruisciBottoneToggleTurni());
        app.lblSuMonitor = new JLabel(GestoreLingua.t("lbl.sux"));
        row2.add(app.lblSuMonitor);
        row2.add(costruisciComboSelettoreDisplay());
        row2.addSeparator(new Dimension(30, 0));
        row2.add(costruisciBottoneIdentifica());

        toolbarContainer.add(row1);
        toolbarContainer.add(row2);

        return toolbarContainer;
    }

    private JButton costruisciBottoneLingua() {
        app.btnLingua = new JButton(GestoreLingua.t("btn.lingua"));
        app.btnLingua.setFocusable(false);
        app.btnLingua.setBackground(new Color(70, 130, 180));
        app.btnLingua.setForeground(Color.WHITE);
        app.btnLingua.addActionListener(e -> new ComandoCambiaLingua(app).esegui());
        return app.btnLingua;
    }

    private JToggleButton costruisciBottoneGara() {
        app.btnGaraInCorso = new JToggleButton(GestoreLingua.t("btn.gara.off")) {
            @Override public void setEnabled(boolean b) {
                super.setEnabled(b);
                if (isSelected()) {
                    setBackground(b ? Color.GREEN : new Color(0, 100, 0));
                    setForeground(b ? Color.BLACK : Color.WHITE);
                } else {
                    setBackground(b ? Color.RED : new Color(120, 0, 0));
                    setForeground(b ? Color.WHITE : Color.LIGHT_GRAY);
                }
            }
        };
        app.btnGaraInCorso.setFocusable(false);
        app.btnGaraInCorso.setBackground(Color.RED);
        app.btnGaraInCorso.setForeground(Color.WHITE);
        app.btnGaraInCorso.addActionListener(e -> gestioneGaraInCorso());
        return app.btnGaraInCorso;
    }

    private void gestioneGaraInCorso() {
        app.isGaraInCorso = app.btnGaraInCorso.isSelected();
        app.btnGaraInCorso.setText(GestoreLingua.t(app.isGaraInCorso ? "btn.gara.on" : "btn.gara.off"));
        new ComandoBloccaInterfaccia(app, app.faseAttuale != Fase.ATTESA).esegui();

        if (app.isGaraInCorso) {
            app.btnGaraInCorso.setBackground(Color.GREEN);
            app.btnGaraInCorso.setForeground(Color.BLACK);            app.attualeVoleeProva = 1;
            if (app.spinVolee != null) app.spinVolee.setValue(0);
            if (app.spinParte != null) app.spinParte.setValue(1);
            app.turnoCorrente = 1;
            app.indicePartenza = 0;
            app.iniziaConPrimaParte = true;
            new ComandoAggiornaTestoTurno(app).esegui();
            app.gestoreLog.inizializzaSessione();
        } else {
            app.btnGaraInCorso.setBackground(Color.RED);
            app.btnGaraInCorso.setForeground(Color.WHITE);
        }
    }

    private JButton costruisciBottoneFormato() {
        app.btnFormatoTempo = new JButton(GestoreLingua.t("btn.formato.sec"));
        app.btnFormatoTempo.setFocusable(false);
        app.btnFormatoTempo.addActionListener(e -> {
            app.statoFormatoTempo = (app.statoFormatoTempo + 1) % 3;
            new ComandoAggiornaBottoni(app).esegui();
            new ComandoAggiornaDisplay(app).esegui();
        });
        return app.btnFormatoTempo;
    }

    private JButton costruisciBottoneSuono() {
        app.btnSuono = new JButton(GestoreLingua.t("btn.suono.on"));
        app.btnSuono.setBackground(Color.GREEN); app.btnSuono.setForeground(Color.BLACK);
        app.btnSuono.setFocusable(false);
        app.btnSuono.addActionListener(e -> {
            app.isSuonoAttivo = !app.isSuonoAttivo;
            if (!app.isSuonoAttivo) MotoreAudio.istanza().azzeraCodaFischi();
            app.btnSuono.setText(GestoreLingua.t(app.isSuonoAttivo ? "btn.suono.on" : "btn.suono.off"));
            app.btnSuono.setBackground(app.isSuonoAttivo ? Color.GREEN : Color.RED);
            app.btnSuono.setForeground(app.isSuonoAttivo ? Color.BLACK : Color.WHITE);
        });
        return app.btnSuono;
    }

    private JButton costruisciBottoneTema() {
        app.btnTema = new JButton(GestoreLingua.t("btn.tema"));
        app.btnTema.setFocusable(false);
        app.btnTema.addActionListener(e -> {
            app.isDarkMode = !app.isDarkMode;
            new ComandoAggiornaTema(app).esegui();
        });
        return app.btnTema;
    }

    private JToggleButton costruisciBottoneToggleTurni() {
        app.btnToggleTurniSpecial = new JToggleButton(GestoreLingua.t("btn.display.off")) {
            @Override public void setEnabled(boolean b) {
                super.setEnabled(b);
                Color n = app.isDarkMode ? new Color(60,60,60) : new Color(220,220,220);
                Color d = app.isDarkMode ? new Color(40,40,40) : new Color(180,180,180);
                Color f = app.isDarkMode ? Color.WHITE : Color.BLACK;
                if (isSelected()) { setBackground(new Color(100,149,237)); setForeground(Color.BLACK); }
                else              { setBackground(b ? n : d); setForeground(b ? f : Color.GRAY); }
            }
        };
        app.btnToggleTurniSpecial.setFocusable(false);
        app.btnToggleTurniSpecial.addActionListener(e -> {
            boolean on = app.btnToggleTurniSpecial.isSelected();
            app.btnToggleTurniSpecial.setText(GestoreLingua.t(on ? "btn.display.on" : "btn.display.off"));
            if (on) { app.btnToggleTurniSpecial.setBackground(new Color(100,149,237)); app.btnToggleTurniSpecial.setForeground(Color.BLACK); }
            else    { app.btnToggleTurniSpecial.setBackground(app.isDarkMode ? new Color(60,60,60) : new Color(220,220,220)); app.btnToggleTurniSpecial.setForeground(app.isDarkMode ? Color.WHITE : Color.BLACK); }
            app.comboSelettoreDisplay.setEnabled(!on);
            new ComandoApplicaLayoutMonitor(app).esegui();
        });
        return app.btnToggleTurniSpecial;
    }

    private JComboBox<String> costruisciComboSelettoreDisplay() {
        app.comboSelettoreDisplay = FabbricaCombo.crea(new String[0], app);

        // Assegna forzatamente il renderer localizzato per gestire il tema
        app.comboSelettoreDisplay.setRenderer(new RendererComboLocalizzato(app));

        for (int i = 0; i < Math.max(1, app.archerDisplays.size()); i++) {
            app.comboSelettoreDisplay.addItem("Monitor " + (i + 1));
        }
        app.comboSelettoreDisplay.addActionListener(e -> new ComandoApplicaLayoutMonitor(app).esegui());
        return app.comboSelettoreDisplay;
    }

    private JToggleButton costruisciBottoneIdentifica() {
        app.btnIdentificaMonitor = new JToggleButton(GestoreLingua.t("btn.idmonitor")) {
            @Override public void setEnabled(boolean b) {
                super.setEnabled(b);
                Color n = app.isDarkMode ? new Color(60,60,60) : new Color(220,220,220);
                Color d = app.isDarkMode ? new Color(40,40,40) : new Color(180,180,180);
                Color f = app.isDarkMode ? Color.WHITE : Color.BLACK;
                setBackground(b ? n : d); setForeground(b ? f : Color.GRAY);
            }
        };
        app.btnIdentificaMonitor.setFocusable(false);
        app.btnIdentificaMonitor.addActionListener(e -> new ComandoIdentificaMonitor(app).esegui());
        return app.btnIdentificaMonitor;
    }

    // ─── Centro (4 quadranti) ────────────────────────────────────────────────

    private JPanel costruisciCentro() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        grid.add(costruisciQuadranteTempi());
        grid.add(costruisciQuadranteDisplay());
        grid.add(costruisciQuadrantePreset());
        grid.add(costruisciQuadranteControllo());
        return grid;
    }

    private JPanel costruisciQuadranteTempi() {
        JPanel q = new JPanel(new GridLayout(3, 2, 10, 10));
        app.borderTempi = BorderFactory.createTitledBorder(GestoreLingua.t("panel.tempi"));
        q.setBorder(app.borderTempi);

        app.lblT1desc = new JLabel(GestoreLingua.t("lbl.t1"));
        app.spinT1 = new JSpinner(new SpinnerNumberModel(10, 0, 99, 1));
        q.add(app.lblT1desc); q.add(app.spinT1);

        app.lblT2desc = new JLabel(GestoreLingua.t("lbl.t2"));
        app.spinT2 = new JSpinner(new SpinnerNumberModel(120, 0, 999, 1));
        q.add(app.lblT2desc); q.add(app.spinT2);

        app.lblT3desc = new JLabel(GestoreLingua.t("lbl.t3"));
        app.spinT3 = new JSpinner(new SpinnerNumberModel(30, 0, 99, 1));
        q.add(app.lblT3desc); q.add(app.spinT3);
        return q;
    }

    private JPanel costruisciQuadranteDisplay() {
        JPanel q = new JPanel(new BorderLayout());
        app.borderDisplay = BorderFactory.createTitledBorder(GestoreLingua.t("panel.display"));
        q.setBorder(app.borderDisplay);

        app.miniaturaCardLayout = new CardLayout();
        app.miniaturaContainer  = new JPanel(app.miniaturaCardLayout);

        app.miniaturaContainer.add(costruisciMinSingolo(), "SINGOLO");
        app.miniaturaContainer.add(costruisciMinScontro(),  "SCONTRO");
        app.miniaturaContainer.add(costruisciMinId(),       "IDENTIFICAZIONE");

        q.add(app.miniaturaContainer, BorderLayout.CENTER);
        return q;
    }

    private JPanel costruisciMinSingolo() {
        app.minLightSingolo = new JPanel(new BorderLayout());
        app.minLightSingolo.setBackground(Color.RED);

        app.minTimerSingolo = new JLabel("00:00", SwingConstants.CENTER);
        app.minTimerSingolo.setFont(new Font("Arial", Font.BOLD, 40));

        app.minTurniSingolo = new JLabel("ABCD", SwingConstants.CENTER);
        app.minTurniSingolo.setFont(new Font("Arial", Font.BOLD, 20));

        app.minEmergenzaTimeSingolo = new JLabel("", SwingConstants.CENTER);
        app.minEmergenzaTimeSingolo.setFont(new Font("Arial", Font.BOLD, 20));
        app.minEmergenzaTimeSingolo.setForeground(Color.BLACK);

        app.minLightSingolo.add(app.minEmergenzaTimeSingolo, BorderLayout.NORTH);
        app.minLightSingolo.add(app.minTurniSingolo, BorderLayout.SOUTH);
        app.minLightSingolo.add(app.minTimerSingolo, BorderLayout.CENTER);
        return app.minLightSingolo;
    }

    private JPanel costruisciMinScontro() {
        JPanel container = new JPanel(new GridLayout(1, 2));

        app.minLightSx = new JPanel(new BorderLayout());
        app.minLightSx.setBackground(Color.RED);
        app.minLightSx.setBorder(BorderFactory.createEmptyBorder());
        app.minTimerSx = new JLabel("00:00", SwingConstants.CENTER);
        app.minTimerSx.setFont(new Font("Arial", Font.BOLD, 30));
        app.minTurniSx = new JLabel("A", SwingConstants.CENTER);
        app.minTurniSx.setFont(new Font("Arial", Font.BOLD, 20));
        app.minEmergenzaTimeSx = new JLabel("", SwingConstants.CENTER);
        app.minEmergenzaTimeSx.setFont(new Font("Arial", Font.BOLD, 15));
        app.minEmergenzaTimeSx.setForeground(Color.BLACK);
        app.minLightSx.add(app.minEmergenzaTimeSx, BorderLayout.NORTH);
        app.minLightSx.add(app.minTurniSx, BorderLayout.SOUTH);
        app.minLightSx.add(app.minTimerSx, BorderLayout.CENTER);

        app.minLightDx = new JPanel(new BorderLayout());
        app.minLightDx.setBackground(Color.RED);
        app.minTimerDx = new JLabel("00:00", SwingConstants.CENTER);
        app.minTimerDx.setFont(new Font("Arial", Font.BOLD, 30));
        app.minTurniDx = new JLabel("B", SwingConstants.CENTER);
        app.minTurniDx.setFont(new Font("Arial", Font.BOLD, 20));
        app.minEmergenzaTimeDx = new JLabel("", SwingConstants.CENTER);
        app.minEmergenzaTimeDx.setFont(new Font("Arial", Font.BOLD, 15));
        app.minEmergenzaTimeDx.setForeground(Color.BLACK);
        app.minLightDx.add(app.minEmergenzaTimeDx, BorderLayout.NORTH);
        app.minLightDx.add(app.minTurniDx, BorderLayout.SOUTH);
        app.minLightDx.add(app.minTimerDx, BorderLayout.CENTER);

        container.add(app.minLightSx);
        container.add(app.minLightDx);
        return container;
    }

    private JPanel costruisciMinId() {
        app.minIdPanel = new JPanel(new BorderLayout());
        app.minIdPanel.setBackground(Color.BLUE);
        app.minIdLabel = new JLabel("MOD. ID", SwingConstants.CENTER);
        app.minIdLabel.setForeground(Color.WHITE);
        app.minIdLabel.setFont(new Font("Arial", Font.BOLD, 30));
        app.minIdPanel.add(app.minIdLabel, BorderLayout.CENTER);
        return app.minIdPanel;
    }

    private JPanel costruisciQuadrantePreset() {
        JPanel q = new JPanel(new BorderLayout(5, 5));
        app.borderPreset = BorderFactory.createTitledBorder(GestoreLingua.t("panel.preset"));
        q.setBorder(app.borderPreset);

        JPanel presetTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        app.lblModalitaDesc = new JLabel(GestoreLingua.t("lbl.modalita"));
        presetTop.add(app.lblModalitaDesc);
        app.comboPreset = FabbricaCombo.crea(new String[]{"Manuale","INDOOR","OUTDOOR","SCONTRO","SHOOT-OFF"}, app);
        app.comboPreset.addActionListener(e -> new ComandoApplicaPreset(app, String.valueOf(app.comboPreset.getSelectedItem())).esegui());
        presetTop.add(app.comboPreset);
        q.add(presetTop, BorderLayout.NORTH);
        q.add(costruisciOptionsWrapper(), BorderLayout.CENTER);

        app.comboScontroType.addActionListener(e -> {
            if ("SHOOT-OFF".equals(String.valueOf(app.comboPreset.getSelectedItem()))) app.spinFrecce.setValue(1);
        });
        return q;
    }

    private JPanel costruisciOptionsWrapper() {
        JPanel wrapper = new JPanel(new CardLayout());
        wrapper.setOpaque(false);

        app.linearOptionsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        app.borderLinearePanel = BorderFactory.createTitledBorder(GestoreLingua.t("panel.lineare"));
        app.linearOptionsPanel.setBorder(app.borderLinearePanel);
        app.lblSecFrecciaLineareDesc = new JLabel(GestoreLingua.t("lbl.secfreccia"));
        app.spinSecFrecciaLineare = new JSpinner(new SpinnerNumberModel(40, 10, 60, 5));
        app.spinSecFrecciaLineare.addChangeListener(e -> aggiornaTotaleDaFrecciaLineare());
        app.linearOptionsPanel.add(app.lblSecFrecciaLineareDesc);
        app.linearOptionsPanel.add(app.spinSecFrecciaLineare);
        app.linearOptionsPanel.add(new JLabel("")); app.linearOptionsPanel.add(new JLabel(""));
        app.linearOptionsPanel.add(new JLabel("")); app.linearOptionsPanel.add(new JLabel(""));
        wrapper.add(app.linearOptionsPanel, "LINEARE");

        app.scontroOptionsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        app.borderScontroPanel = BorderFactory.createTitledBorder(GestoreLingua.t("panel.scontro"));
        app.scontroOptionsPanel.setBorder(app.borderScontroPanel);
        app.lblTipoDesc = new JLabel(GestoreLingua.t("lbl.tipo"));
        app.comboScontroType = FabbricaCombo.crea(new String[]{"INDIVIDUALE","SQUADRE","MIX-TEAM"}, app);
        app.scontroOptionsPanel.add(app.lblTipoDesc);
        app.scontroOptionsPanel.add(app.comboScontroType);
        app.lblFrecceDesc = new JLabel(GestoreLingua.t("lbl.frecce"));
        app.spinFrecce = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        app.scontroOptionsPanel.add(app.lblFrecceDesc);
        app.scontroOptionsPanel.add(app.spinFrecce);
        app.lblSecFrecciaDesc = new JLabel(GestoreLingua.t("lbl.secfreccia"));
        app.spinSecFreccia = new JSpinner(new SpinnerNumberModel(20, 10, 60, 5));
        app.scontroOptionsPanel.add(app.lblSecFrecciaDesc);
        app.scontroOptionsPanel.add(app.spinSecFreccia);
        wrapper.add(app.scontroOptionsPanel, "SCONTRO");

        JPanel empty = new JPanel(); empty.setOpaque(false);
        wrapper.add(empty, "VUOTO");
        return wrapper;
    }

    private void aggiornaTotaleDaFrecciaLineare() {
        String preset = String.valueOf(app.comboPreset.getSelectedItem());
        int sec = (int) app.spinSecFrecciaLineare.getValue();
        if ("INDOOR".equals(preset))  app.spinT2.setValue(sec * 3);
        else if ("OUTDOOR".equals(preset)) app.spinT2.setValue(sec * 6);
    }

    private JPanel costruisciQuadranteControllo() {
        JPanel q = new JPanel(new BorderLayout());
        app.borderControllo = BorderFactory.createTitledBorder(GestoreLingua.t("panel.controllo"));
        q.setBorder(app.borderControllo);
        q.add(costruisciPannelloTurniEVolee(), BorderLayout.NORTH);
        q.add(costruisciGrigliaAzioni(), BorderLayout.CENTER);
        return q;
    }

    private JPanel costruisciPannelloTurniEVolee() {
        JPanel top = new JPanel(new BorderLayout());

        JPanel turniPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        app.comboTurni = FabbricaCombo.crea(new String[]{
                "- Nessuno -",
                "ABC",
                "AB - CD",
                "AB - CD - EF",
                "A - B",
                "C - D",
                "A - B - C",
                "A - B - C - D",
                "A - B - C - D - E",
                "A - B - C - D - E - F"
        }, app);
        app.comboTurni.setSelectedItem("AB - CD");
        app.comboTurni.addActionListener(e -> {
            app.iniziaConPrimaParte = true;
            app.indicePartenza = 0;
            new ComandoAggiornaTestoTurno(app).esegui();
            new ComandoBloccaInterfaccia(app, app.faseAttuale != Fase.ATTESA).esegui();
        });
        turniPanel.add(app.comboTurni);
        turniPanel.add(costruisciBottoneAlternaMetata());
        turniPanel.add(costruisciBottoneImpostaNomi());

        JPanel voleePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        app.lblVoleeProvaDesc = new JLabel(GestoreLingua.t("lbl.voleeprova"));
        voleePanel.add(app.lblVoleeProvaDesc);
        app.spinVoleeProva = new JSpinner(new SpinnerNumberModel(2, 1, 4, 1));
        personalizzaSpinner(app.spinVoleeProva); voleePanel.add(app.spinVoleeProva);

        app.lblVoleeCorrenteDesc = new JLabel(GestoreLingua.t("lbl.voleecorrente"));
        voleePanel.add(app.lblVoleeCorrenteDesc);
        app.spinVolee = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        personalizzaSpinner(app.spinVolee);
        app.spinVolee.addChangeListener(e -> { if ((int)app.spinVolee.getValue() == 0) app.attualeVoleeProva = 1; });
        voleePanel.add(app.spinVolee);

        app.lblParteDesc = new JLabel(GestoreLingua.t("lbl.parte"));
        voleePanel.add(app.lblParteDesc);
        app.spinParte = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        personalizzaSpinner(app.spinParte); voleePanel.add(app.spinParte);

        top.add(turniPanel, BorderLayout.NORTH);
        top.add(voleePanel, BorderLayout.SOUTH);
        return top;
    }

    private JButton costruisciBottoneAlternaMetata() {
        app.btnAlternaMetata = new JButton("Inizia: AB (T)") {
            @Override public void setEnabled(boolean b) {
                super.setEnabled(b);
                Color n = app.isDarkMode ? new Color(60,60,60) : new Color(220,220,220);
                Color d = app.isDarkMode ? new Color(40,40,40) : new Color(180,180,180);
                Color f = app.isDarkMode ? Color.WHITE : Color.BLACK;
                setBackground(b ? n : d); setForeground(b ? f : Color.GRAY);
            }
        };
        app.btnAlternaMetata.setFocusable(false);
        app.btnAlternaMetata.addActionListener(e -> {
            if (app.isScontroMode) {
                app.iniziaConPrimaParte = !app.iniziaConPrimaParte;
            } else {
                String mod = String.valueOf(app.comboTurni.getSelectedItem());
                if (!mod.equals("- Nessuno -") && !mod.equals("ABC")) {
                    String[] gruppi = mod.split(" - ");
                    app.indicePartenza = (app.indicePartenza + 1) % gruppi.length;
                }
            }
            new ComandoAggiornaTestoTurno(app).esegui();
        });
        return app.btnAlternaMetata;
    }

    private JButton costruisciBottoneImpostaNomi() {
        app.btnImpostaNomi = new JButton(GestoreLingua.t("btn.nomi"));
        app.btnImpostaNomi.setFocusable(false);
        app.btnImpostaNomi.setVisible(false);
        app.btnImpostaNomi.addActionListener(e -> new DialogNomiScontro(app).apri());
        return app.btnImpostaNomi;
    }

    private JPanel costruisciGrigliaAzioni() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 5, 5));
        grid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        app.btnStartSkip  = new JButton(); app.btnStartSkip.setFocusable(false);
        app.btnStopReset  = new JButton(); app.btnStopReset.setFocusable(false);
        app.btnRecupero   = new JButton(); app.btnRecupero.setFocusable(false);
        app.btnFischio    = new JButton(); app.btnFischio.setFocusable(false);
        app.btnEmergenza  = new JButton(); app.btnEmergenza.setFocusable(false);

        app.btnAdd40s = new JButton();
        app.btnAdd40s.setFocusable(false);
        app.btnAdd40s.setBackground(Color.RED);
        app.btnAdd40s.setForeground(Color.BLACK);
        app.btnAdd40s.setText(GestoreLingua.t("btn.reimposta"));
        app.btnAdd40s.addActionListener(e -> new DialogTempo(app).apri());

        app.add40Container = new JPanel(new CardLayout());
        app.add40Container.setOpaque(false);
        JPanel empty = new JPanel(); empty.setOpaque(false);
        app.add40Container.add(empty, "EMPTY");
        app.add40Container.add(app.btnAdd40s, "BUTTON");

        app.btnStartSkip.addActionListener(e -> new ComandoAvviaOSalta(app).esegui());
        app.btnEmergenza.addActionListener(e -> new ComandoEmergenza(app).esegui());
        app.btnStopReset.addActionListener(e -> new ComandoReset(app).esegui());
        app.btnRecupero.addActionListener(e  -> new ComandoRecupero(app).esegui());
        app.btnFischio.addActionListener(e   -> MotoreAudio.istanza().eseguiFischi(1, app.isSuonoAttivo));

        grid.add(app.btnStartSkip); grid.add(app.btnFischio);    grid.add(app.btnStopReset);
        grid.add(app.btnRecupero);  grid.add(app.add40Container); grid.add(app.btnEmergenza);
        return grid;
    }

    // ─── Bottom bar ──────────────────────────────────────────────────────────

    private JPanel costruisciBottom() {
        JPanel bottom = new JPanel(new BorderLayout());

        app.lblSecretLeft = new JLabel(" 08025");
        app.lblSecretLeft.setFont(new Font("Arial", Font.PLAIN, 10));

        app.lblDesigned = new JLabel("Designed by Giovanni Zucchi from Arcieri Aquila Bianca in Modena", SwingConstants.CENTER);
        app.lblDesigned.setFont(new Font("Arial", Font.ITALIC, 11));

        app.lblSecretRight = new JLabel("134346 ");
        app.lblSecretRight.setFont(new Font("Arial", Font.PLAIN, 10));

        bottom.add(app.lblSecretLeft,  BorderLayout.WEST);
        bottom.add(app.lblDesigned,    BorderLayout.CENTER);
        bottom.add(app.lblSecretRight, BorderLayout.EAST);
        return bottom;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void applicaDefocusSuClick(Container container) {
        container.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { app.operatorFrame.requestFocusInWindow(); }
        });
        for (Component c : container.getComponents()) {
            if (c instanceof JPanel || c instanceof JLabel || c instanceof JToolBar) {
                applicaDefocusSuClick((Container) c);
            }
        }
    }

    private void disabilitaEditSpinner() {
        JSpinner[] spinners = {app.spinT1, app.spinT2, app.spinT3, app.spinSecFrecciaLineare,
                               app.spinFrecce, app.spinSecFreccia, app.spinVolee};
        for (JSpinner s : spinners) personalizzaSpinner(s);
    }

    private void personalizzaSpinner(JSpinner s) {
        if (s.getEditor() instanceof JSpinner.DefaultEditor ed) {
            ed.getTextField().setEditable(false);
            ed.getTextField().setFocusable(false);
        }
    }

    private void posizioneFinale() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.getScreenDevices().length > 1) {
            app.operatorFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            app.operatorFrame.setLocation(100, 100);
        }
    }
}
