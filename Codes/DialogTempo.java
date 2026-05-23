import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Finestra modale per la correzione manuale del tempo rimasto durante l'emergenza.
 * Permette ±40s, calcolo automatico da frecce×secondi, e salva il nuovo valore.
 */
public class DialogTempo {

    private final ArcherySoftwareMain app;

    public DialogTempo(ArcherySoftwareMain app) {
        this.app = app;
    }

    public void apri() {
        boolean isIndiv     = "INDIVIDUALE".equals(String.valueOf(app.comboScontroType.getSelectedItem()));
        TempoIniziale ti    = determinaTempoIniziale(isIndiv);

        final int[] tempoModificato = { ti.valore };
        final int tempoOriginale    = ti.valore;

        JDialog dialog = new JDialog(app.operatorFrame, "Time Management", true);
        dialog.setUndecorated(true);
        dialog.setSize(1100, 700);
        dialog.setLocationRelativeTo(app.operatorFrame);
        dialog.setLayout(new BorderLayout());

        JLabel lblPreview = creaLabelPreview(tempoOriginale);
        Runnable aggiornaLabel = () -> lblPreview.setText(
                GestoreLingua.t("dialog.tempo.preview") + FormattatoreTempo.formatta(tempoModificato[0], app.statoFormatoTempo));

        JButton btnPlus  = new JButton("+40s (+)");
        JButton btnMinus = new JButton("-40s (-)");
        JSpinner spinFrecceR = creaSpinner(1,  1, 6, 1);
        int defaultSec = app.isScontroMode ? (int) app.spinSecFreccia.getValue() : (int) app.spinSecFrecciaLineare.getValue();
        JSpinner spinSec     = creaSpinner(defaultSec, 10, 60, 5);
        JButton btnCalc  = new JButton(GestoreLingua.t("dialog.tempo.calcola"));
        JButton btnApply = new JButton(GestoreLingua.t("dialog.tempo.salva"));
        JButton btnCancel = new JButton(GestoreLingua.t("dialog.tempo.annulla"));

        collegaAzioniBottoni(tempoModificato, tempoOriginale, isIndiv, ti.lato,
                btnPlus, btnMinus, spinFrecceR, spinSec, btnCalc, btnApply, btnCancel,
                aggiornaLabel, dialog);

        Font fontGrandi = new Font("Arial", Font.BOLD, 26);
        JPanel mainPanel = costruisciMainPanel(lblPreview, btnPlus, btnMinus, spinFrecceR, spinSec, btnCalc, btnApply, btnCancel, fontGrandi);
        dialog.add(mainPanel, BorderLayout.CENTER);

        collegaKeybindings(dialog, btnPlus, btnMinus, btnApply, btnCancel);

        new ComandoAggiornaTema(app).applicaRicorsivo(dialog.getContentPane(),
                app.isDarkMode ? new Color(30, 30, 30) : new Color(238, 238, 238),
                app.isDarkMode ? Color.WHITE : Color.BLACK);

        applicaDefocusSuClick(mainPanel, btnCancel);

        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { btnCancel.requestFocusInWindow(); }
        });

        dialog.setVisible(true);
    }

    // --- Struttura dati di supporto ---

    private record TempoIniziale(int valore, boolean lato) {}

    private TempoIniziale determinaTempoIniziale(boolean isIndiv) {
        if (!app.isScontroMode || app.faseSalvata == Fase.RECUPERO_TIRO) {
            return new TempoIniziale(app.timeRemainingSx, true);
        }
        if (app.faseSalvata == Fase.SCONTRO_TIRO_SX) {
            return new TempoIniziale(app.timeRemainingSx, true);
        }
        if (app.faseSalvata == Fase.SCONTRO_TIRO_DX) {
            return new TempoIniziale(app.timeRemainingDx, false);
        }
        return new TempoIniziale(app.timeRemainingSx, true);
    }

    // --- Costruzione UI ---

    private JLabel creaLabelPreview(int tempoIniziale) {
        JLabel lbl = new JLabel(
                GestoreLingua.t("dialog.tempo.preview") + FormattatoreTempo.formatta(tempoIniziale, app.statoFormatoTempo),
                SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.BOLD, 55));
        return lbl;
    }

    private JSpinner creaSpinner(int val, int min, int max, int step) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(val, min, max, step));
        s.setFont(new Font("Arial", Font.BOLD, 26));
        JSpinner.DefaultEditor ed = (JSpinner.DefaultEditor) s.getEditor();
        ed.getTextField().setFont(new Font("Arial", Font.BOLD, 26));
        ed.getTextField().setEditable(false);
        ed.getTextField().setFocusable(false);
        return s;
    }

    private JPanel costruisciMainPanel(JLabel lblPreview, JButton btnPlus, JButton btnMinus,
            JSpinner spinFrecceR, JSpinner spinSec, JButton btnCalc,
            JButton btnApply, JButton btnCancel, Font fontGrandi) {

        Color borderColor = app.isDarkMode ? Color.LIGHT_GRAY : Color.DARK_GRAY;
        JPanel main = new JPanel(new GridLayout(4, 1, 15, 15));
        main.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 5),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        btnPlus.setFont(fontGrandi);   btnPlus.setFocusable(false);
        btnMinus.setFont(fontGrandi);  btnMinus.setFocusable(false);
        btnCalc.setFont(fontGrandi);   btnCalc.setFocusable(false);

        btnApply.setFont(fontGrandi);  btnApply.setFocusable(false);
        btnApply.setBackground(new Color(100, 200, 100)); btnApply.setForeground(Color.BLACK);
        btnCancel.setFont(fontGrandi); btnCancel.setFocusable(false);
        btnCancel.setBackground(new Color(200, 100, 100)); btnCancel.setForeground(Color.WHITE);

        JPanel plusMinusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        plusMinusPanel.setOpaque(false);
        plusMinusPanel.add(btnMinus); plusMinusPanel.add(btnPlus);

        JPanel calcPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        calcPanel.setOpaque(false);
        JLabel lblFrecce = new JLabel(GestoreLingua.t("dialog.tempo.frecce")); lblFrecce.setFont(fontGrandi);
        JLabel lblSec    = new JLabel(GestoreLingua.t("dialog.tempo.sec"));    lblSec.setFont(fontGrandi);
        calcPanel.add(lblFrecce); calcPanel.add(spinFrecceR);
        calcPanel.add(lblSec);    calcPanel.add(spinSec);
        calcPanel.add(btnCalc);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        actionPanel.setOpaque(false);
        actionPanel.add(btnApply); actionPanel.add(btnCancel);

        main.add(lblPreview);
        main.add(plusMinusPanel);
        main.add(calcPanel);
        main.add(actionPanel);

        return main;
    }

    // --- Logica ---

    private void collegaAzioniBottoni(int[] tempo, int originale, boolean isIndiv, boolean lato,
            JButton btnPlus, JButton btnMinus, JSpinner spinFrecceR, JSpinner spinSec,
            JButton btnCalc, JButton btnApply, JButton btnCancel,
            Runnable aggiornaLabel, JDialog dialog) {

        final String stringaLatoLog = app.isScontroMode ? (lato ? "SISTEMA SX" : "SISTEMA DX") : "SISTEMA UNICO";

        btnPlus.addActionListener(e  -> { tempo[0] += 40; aggiornaLabel.run(); });
        btnMinus.addActionListener(e -> { tempo[0] = Math.max(0, tempo[0] - 40); aggiornaLabel.run(); });

        btnCalc.addActionListener(e -> {
            int calcoloWA = (int) spinFrecceR.getValue() * (int) spinSec.getValue();
            tempo[0] = Math.max(originale, calcoloWA);
            aggiornaLabel.run();
        });

        btnApply.addActionListener(e -> {
            salvaTempoModificato(tempo[0], isIndiv, lato);
            new ComandoAggiornaLabelEmergenza(app).esegui();
            app.operatorFrame.requestFocusInWindow();
            app.gestoreLog.logModificaManualeTempo(originale, tempo[0], stringaLatoLog);
            dialog.dispose();
        });

        btnCancel.addActionListener(e -> {
            if (tempo[0] != originale) {
                tempo[0] = originale;
                aggiornaLabel.run();
            } else {
                app.operatorFrame.requestFocusInWindow();
                dialog.dispose();
            }
        });
    }

    private void salvaTempoModificato(int nuovoTempo, boolean isIndiv, boolean latoSinistra) {
        if (!app.isScontroMode || app.faseSalvata == Fase.RECUPERO_TIRO) {
            app.timeRemainingSx = nuovoTempo;
        } else if (latoSinistra) {
            app.timeRemainingSx = nuovoTempo;
            if (!isIndiv) app.tempoSalvatoSx = nuovoTempo;
        } else {
            app.timeRemainingDx = nuovoTempo;
            if (!isIndiv) app.tempoSalvatoDx = nuovoTempo;
        }
    }

    private void collegaKeybindings(JDialog dialog, JButton btnPlus, JButton btnMinus,
            JButton btnApply, JButton btnCancel) {
        JRootPane root = dialog.getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,      0), "plus");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,     0), "plus");
        am.put("plus",    new AbstractAction() { public void actionPerformed(ActionEvent e) { btnPlus.doClick(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "minus");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,    0), "minus");
        am.put("minus",   new AbstractAction() { public void actionPerformed(ActionEvent e) { btnMinus.doClick(); } });

        im.put(KeyStroke.getKeyStroke("ENTER"),  "apply");
        am.put("apply",   new AbstractAction() { public void actionPerformed(ActionEvent e) { btnApply.doClick(); } });

        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        am.put("cancel",  new AbstractAction() { public void actionPerformed(ActionEvent e) { btnCancel.doClick(); } });
    }

    private void applicaDefocusSuClick(JPanel main, JButton btnCancel) {
        MouseAdapter defocus = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { btnCancel.requestFocusInWindow(); }
        };
        for (Component c : main.getComponents()) {
            if (c instanceof JPanel p) p.addMouseListener(defocus);
        }
        main.addMouseListener(defocus);
    }
}
