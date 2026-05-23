/**
 * Attiva o disattiva la modalità identificazione monitor.
 * Quando attiva, mostra su ogni schermo esterno il suo numero progressivo.
 * È attivabile solo da ATTESA.
 */
public class ComandoIdentificaMonitor implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoIdentificaMonitor(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.btnIdentificaMonitor.isSelected()) {
            attivaIdentificazione();
        } else {
            disattivaIdentificazione();
        }
    }

    private void attivaIdentificazione() {
        if (app.faseAttuale != Fase.ATTESA) {
            app.btnIdentificaMonitor.setSelected(false);
            return;
        }
        app.faseSalvata = app.faseAttuale;
        app.faseAttuale = Fase.IDENTIFICAZIONE_MONITOR;

        app.miniaturaCardLayout.show(app.miniaturaContainer, "IDENTIFICAZIONE");
        for (int i = 0; i < app.archerDisplays.size(); i++) {
            DisplayArciere da = app.archerDisplays.get(i);
            da.impostaTestoId(i + 1);
            da.displayCardLayout.show(da.displayContainer, "IDENTIFICAZIONE");
        }
        new ComandoBloccaInterfaccia(app, true).esegui();
        new ComandoAggiornaDisplay(app).esegui();
    }

    private void disattivaIdentificazione() {
        app.faseAttuale = app.faseSalvata;
        new ComandoApplicaLayoutMonitor(app).esegui();
        new ComandoBloccaInterfaccia(app, app.faseAttuale != Fase.ATTESA).esegui();
    }
}
