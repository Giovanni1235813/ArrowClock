import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Collega i tasti di scorciatoia della finestra operatore ai comandi corrispondenti.
 * Ogni binding delega all'apposito Comando, mantenendo questa classe focalizzata
 * su un'unica responsabilità: la mappatura tastiera → azione.
 */
public class GestoreScorciatoie {

    private final ArcherySoftwareMain app;

    public GestoreScorciatoie(ArcherySoftwareMain app) {
        this.app = app;
    }

    public void applica() {
        JPanel cp = (JPanel) app.operatorFrame.getContentPane();
        InputMap im = cp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = cp.getActionMap();

        registra(im, am, "SPACE",         "start",         () -> { if (app.btnStartSkip.isEnabled())          new ComandoAvviaOSalta(app).esegui(); });
        registra(im, am, "ENTER",         "emergenza",     () -> { if (app.btnEmergenza.isEnabled())           new ComandoEmergenza(app).esegui(); });
        registra(im, am, "G",             "reset",         () -> { if (app.btnStopReset.isEnabled())           new ComandoReset(app).esegui(); });
        registra(im, am, "R",             "recupero",      () -> { if (app.btnRecupero.isEnabled())            new ComandoRecupero(app).esegui(); });
        registra(im, am, "F",             "fischio",       () -> { if (app.btnFischio.isEnabled())             MotoreAudio.istanza().eseguiFischi(1, app.isSuonoAttivo); });
        registra(im, am, "M",             "formato",       () -> { if (app.btnFormatoTempo.isEnabled())        app.btnFormatoTempo.doClick(); });
        registra(im, am, "C",             "tema",          () -> { if (app.btnTema.isEnabled())                app.btnTema.doClick(); });
        registra(im, am, "S",             "suono",         () -> { if (app.btnSuono.isEnabled())               app.btnSuono.doClick(); });
        registra(im, am, "D",             "displayTurni",  () -> { if (app.btnToggleTurniSpecial.isEnabled())  app.btnToggleTurniSpecial.doClick(); });
        registra(im, am, "I",             "idMonitor",     () -> { if (app.btnIdentificaMonitor.isEnabled())   app.btnIdentificaMonitor.doClick(); });
        registra(im, am, "T",             "ciclaTurni",    () -> { if (app.btnAlternaMetata.isEnabled())       app.btnAlternaMetata.doClick(); });
        registra(im, am, "L",             "lingua",        () -> new ComandoCambiaLingua(app).esegui());

        registra(im, am, "UP",   "voleeUp",   this::incrementaVolee);
        registra(im, am, "DOWN", "voleeDown", this::decrementaVolee);

        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SHIFT, java.awt.event.InputEvent.SHIFT_DOWN_MASK), "reimposta");
        im.put(KeyStroke.getKeyStroke("pressed SHIFT"), "reimposta");
        am.put("reimposta", azione(() -> { if (app.btnAdd40s != null && app.btnAdd40s.isVisible()) app.btnAdd40s.doClick(); }));

        registra(im, am, "N", "nomiSquadre", () -> {
            if (app.btnImpostaNomi != null && app.btnImpostaNomi.isVisible() && app.btnImpostaNomi.isEnabled()) {
                app.btnImpostaNomi.doClick();
            }
        });
    }

    private void registra(InputMap im, ActionMap am, String keystroke, String chiave, Runnable azione) {
        im.put(KeyStroke.getKeyStroke(keystroke), chiave);
        am.put(chiave, azione(azione));
    }

    private AbstractAction azione(Runnable r) {
        return new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { r.run(); }
        };
    }

    private void incrementaVolee() {
        if (!app.spinVolee.isEnabled()) return;
        int current = (int) app.spinVolee.getValue();
        int max = (Integer) ((SpinnerNumberModel) app.spinVolee.getModel()).getMaximum();
        if (current < max) app.spinVolee.setValue(current + 1);
    }

    private void decrementaVolee() {
        if (!app.spinVolee.isEnabled()) return;
        int current = (int) app.spinVolee.getValue();
        if (current > 0) app.spinVolee.setValue(current - 1);
    }
}
