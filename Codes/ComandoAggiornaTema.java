import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Applica il tema scuro o chiaro a tutta l'interfaccia operatore.
 * Percorre ricorsivamente il contenuto della finestra e aggiorna
 * sfondo, testo, bordi e ogni componente speciale.
 */
public class ComandoAggiornaTema implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoAggiornaTema(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        Color bg = app.isDarkMode ? new Color(30, 30, 30) : new Color(238, 238, 238);
        Color fg = app.isDarkMode ? Color.WHITE : Color.BLACK;

        app.customTitleBar.setBackground(app.isDarkMode ? new Color(15, 15, 15) : new Color(200, 200, 200));
        app.lblTitleBar.setForeground(app.isDarkMode ? Color.LIGHT_GRAY : Color.DARK_GRAY);

        applicaRicorsivo(app.operatorFrame.getContentPane(), bg, fg);
        aggiornaBotoniNeutri();
        aggiornaColoriSegreto();

        new ComandoRipristinaColoriSemaforo(app).esegui();
        app.operatorFrame.repaint();
    }

    private void aggiornaBotoniNeutri() {
        Color neutroBg = app.isDarkMode ? new Color(60, 60, 60) : new Color(220, 220, 220);
        Color neutroFg = app.isDarkMode ? Color.WHITE : Color.BLACK;
        Color disabledBg = app.isDarkMode ? new Color(40, 40, 40) : new Color(180, 180, 180);

        if (app.btnAlternaMetata != null) {
            app.btnAlternaMetata.setBackground(app.btnAlternaMetata.isEnabled() ? neutroBg : disabledBg);
            app.btnAlternaMetata.setForeground(app.btnAlternaMetata.isEnabled() ? neutroFg : Color.GRAY);
        }
        if (app.btnTema != null) {
            app.btnTema.setBackground(neutroBg);
            app.btnTema.setForeground(neutroFg);
        }
        if (app.btnIdentificaMonitor != null) {
            app.btnIdentificaMonitor.setBackground(app.btnIdentificaMonitor.isEnabled() ? neutroBg : disabledBg);
            app.btnIdentificaMonitor.setForeground(app.btnIdentificaMonitor.isEnabled() ? neutroFg : Color.GRAY);
        }
        if (app.btnToggleTurniSpecial != null) {
            if (app.btnToggleTurniSpecial.isSelected()) {
                app.btnToggleTurniSpecial.setBackground(new Color(100, 149, 237));
                app.btnToggleTurniSpecial.setForeground(Color.BLACK);
            } else {
                app.btnToggleTurniSpecial.setBackground(app.btnToggleTurniSpecial.isEnabled() ? neutroBg : disabledBg);
                app.btnToggleTurniSpecial.setForeground(app.btnToggleTurniSpecial.isEnabled() ? neutroFg : Color.GRAY);
            }
        }
    }

    private void aggiornaColoriSegreto() {
        Color secretColor = app.isDarkMode ? new Color(35, 35, 35) : new Color(230, 230, 230);
        if (app.lblSecretLeft  != null) app.lblSecretLeft.setForeground(secretColor);
        if (app.lblSecretRight != null) app.lblSecretRight.setForeground(secretColor);
        if (app.lblDesigned != null) {
            app.lblDesigned.setForeground(app.isDarkMode ? new Color(100, 100, 100) : new Color(130, 130, 130));
        }
    }

    public void applicaRicorsivo(Container container, Color bg, Color fg) {
        if (container == app.customTitleBar || container == app.minIdPanel) return;

        container.setBackground(bg);
        for (Component c : container.getComponents()) {
            if (c == app.customTitleBar || c == app.minIdPanel) continue;

            if (c instanceof JPanel p) {
                p.setBackground(bg);

                // Rimuove linee di separazione esplicite (es. MatteBorder nella miniatura Scontro)
                if (p.getBorder() instanceof javax.swing.border.MatteBorder) {
                    p.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                }

                // Appiattisce i TitledBorder: rimuove la linea ma mantiene il testo e i margini
                if (p.getBorder() instanceof javax.swing.border.TitledBorder tb) {
                    tb.setTitleColor(fg);
                    tb.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                }
                applicaRicorsivo(p, bg, fg);

            } else if (c instanceof JToolBar tb) {
                tb.setBackground(bg);
                tb.setBorder(BorderFactory.createEmptyBorder()); // Rende Flat la toolbar
                applicaRicorsivo(tb, bg, fg);

            } else if (c instanceof JComboBox<?> combo) {
                // Forza colori e bordi flat su tutte le tendine al cambio tema
                combo.setBorder(BorderFactory.createLineBorder(
                        app.isDarkMode ? new Color(70, 70, 70) : new Color(200, 200, 200)));
                combo.setBackground(app.isDarkMode ? new Color(50, 50, 50) : Color.WHITE);
                combo.setForeground(app.isDarkMode ? Color.WHITE : Color.BLACK);
                combo.updateUI(); // Ridisegna freccia e componenti interni

            } else if (c instanceof JSpinner spinner) {
                applicaTemaSpinner(spinner, fg);

            } else if (c instanceof JLabel label && nonELabelProtetta(label)) {
                label.setForeground(fg);
            }
        }
    }

    private void applicaTemaSpinner(JSpinner spinner, Color fg) {
        spinner.setBorder(BorderFactory.createLineBorder(
                app.isDarkMode ? new Color(70, 70, 70) : Color.GRAY));
        spinner.setOpaque(true);
        spinner.setBackground(app.isDarkMode ? new Color(50, 50, 50) : Color.WHITE);

        for (Component sc : spinner.getComponents()) {
            if (sc instanceof JButton sb) {
                sb.setBackground(app.isDarkMode ? new Color(60, 60, 60) : new Color(220, 220, 220));
            }
        }

        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            editor.getTextField().setBackground(app.isDarkMode ? new Color(50, 50, 50) : Color.WHITE);
            editor.getTextField().setForeground(fg);
            editor.getTextField().setCaretColor(fg);
            editor.getTextField().setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }
    }

    private boolean nonELabelProtetta(JLabel label) {
        return label != app.minTimerSingolo && label != app.minTurniSingolo
                && label != app.minTimerSx   && label != app.minTurniSx
                && label != app.minTimerDx   && label != app.minTurniDx
                && label != app.minIdLabel
                && label != app.lblSecretLeft && label != app.lblSecretRight
                && label != app.lblDesigned
                && label != app.minEmergenzaTimeSingolo
                && label != app.minEmergenzaTimeSx
                && label != app.minEmergenzaTimeDx;
    }
}
