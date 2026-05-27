import java.awt.Color;

/**
 * Aggiorna il testo, il colore e lo stato abilitato/disabilitato
 * di tutti i pulsanti di controllo in base alla fase di gara attuale.
 */
public class ComandoAggiornaBottoni implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoAggiornaBottoni(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        boolean isIdMode = (app.faseAttuale == Fase.IDENTIFICAZIONE_MONITOR);
        aggiornaBottoneStart(isIdMode);
        aggiornaBottoneEmergenza(isIdMode);
        aggiornaBottoneRecupero(isIdMode);
        aggiornaBottoneReset(isIdMode);
        aggiornaBottoneFischio(isIdMode);
        aggiornaBottoneFormato(isIdMode);
    }

    private void aggiornaBottoneStart(boolean isIdMode) {
        boolean enabled = (app.faseAttuale != Fase.EMERGENZA
                && app.faseAttuale != Fase.PREPARAZIONE_ROSSO
                && !isIdMode);
        app.btnStartSkip.setEnabled(enabled);
        app.btnStartSkip.setBackground(enabled ? Color.GREEN : new Color(130, 130, 130));
        app.btnStartSkip.setForeground(enabled ? Color.BLACK : new Color(60, 60, 60));

        if (app.faseAttuale == Fase.ATTESA || app.faseAttuale == Fase.RECUPERO_ATTESA) {
            app.btnStartSkip.setText(GestoreLingua.t("btn.start"));
        } else {
            app.btnStartSkip.setText(GestoreLingua.t("btn.salta"));
        }
    }

    private void aggiornaBottoneEmergenza(boolean isIdMode) {
        boolean enabled = (app.faseAttuale != Fase.ATTESA
                && app.faseAttuale != Fase.RECUPERO_ATTESA
                && !isIdMode);
        app.btnEmergenza.setEnabled(enabled);
        app.btnEmergenza.setBackground(enabled ? Color.RED : new Color(120, 60, 60));
        app.btnEmergenza.setForeground(enabled ? Color.BLACK : new Color(60, 40, 40));
        app.btnEmergenza.setText(app.faseAttuale == Fase.EMERGENZA
                ? GestoreLingua.t("btn.riprendi")
                : GestoreLingua.t("btn.emergenza"));
    }

    private void aggiornaBottoneRecupero(boolean isIdMode) {
        // MODIFICA: Aggiunto "&& !app.isScontroMode" per disattivare il recupero a schermo diviso
        boolean enabled = (app.faseAttuale != Fase.EMERGENZA
                && app.faseAttuale != Fase.RECUPERO_TIRO
                && !isIdMode
                && !app.isScontroMode);

        app.btnRecupero.setEnabled(enabled);
        app.btnRecupero.setBackground(enabled ? Color.YELLOW : new Color(130, 130, 90));
        app.btnRecupero.setForeground(enabled ? Color.BLACK : new Color(70, 70, 50));

        if (app.faseAttuale == Fase.RECUPERO_ATTESA) {
            app.btnRecupero.setText(GestoreLingua.t("btn.recupero.add40"));
        } else if (app.recuperoPrenotato) {
            app.btnRecupero.setText(GestoreLingua.t("btn.recupero.prenotato"));
        } else {
            app.btnRecupero.setText(GestoreLingua.t("btn.recupero"));
        }
    }

    private void aggiornaBottoneReset(boolean isIdMode) {
        boolean enabled = ((app.faseAttuale == Fase.ATTESA
                || app.faseAttuale == Fase.EMERGENZA
                || app.faseAttuale == Fase.RECUPERO_ATTESA)
                && !isIdMode);
        app.btnStopReset.setEnabled(enabled);
        app.btnStopReset.setBackground(enabled ? Color.WHITE : new Color(130, 130, 130));
        app.btnStopReset.setForeground(enabled ? Color.BLACK : new Color(60, 60, 60));
        app.btnStopReset.setText(GestoreLingua.t("btn.reset"));
    }

    private void aggiornaBottoneFischio(boolean isIdMode) {
        boolean enabled = !isIdMode;
        app.btnFischio.setEnabled(enabled);
        app.btnFischio.setBackground(enabled ? new Color(135, 206, 250) : new Color(80, 110, 130));
        app.btnFischio.setForeground(enabled ? Color.BLACK : new Color(50, 50, 50));
        app.btnFischio.setText(GestoreLingua.t("btn.fischio"));
    }

    private void aggiornaBottoneFormato(boolean isIdMode) {
        boolean enabled = (app.faseAttuale == Fase.ATTESA || app.faseAttuale == Fase.RECUPERO_ATTESA) && !isIdMode;
        app.btnFormatoTempo.setEnabled(enabled);

        Color neutroBg = app.isDarkMode ? new Color(60, 60, 60) : new Color(220, 220, 220);
        Color neutroFg = app.isDarkMode ? Color.WHITE : Color.BLACK;
        app.btnFormatoTempo.setBackground(enabled ? neutroBg : (app.isDarkMode ? new Color(40, 40, 40) : new Color(180, 180, 180)));
        app.btnFormatoTempo.setForeground(enabled ? neutroFg : Color.GRAY);

        if (app.statoFormatoTempo == 0)      app.btnFormatoTempo.setText(GestoreLingua.t("btn.formato.sec"));
        else if (app.statoFormatoTempo == 1) app.btnFormatoTempo.setText(GestoreLingua.t("btn.formato.mmss"));
        else                                 app.btnFormatoTempo.setText(GestoreLingua.t("btn.formato.inv"));
    }
}
