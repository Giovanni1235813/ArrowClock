import java.awt.Color;

/**
 * Innesca il recupero materiale in seguito a una prenotazione avvenuta
 * durante la volée. Porta l'app in RECUPERO_ATTESA con 40 secondi iniziali.
 */
public class ComandoInnescaRecupero implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoInnescaRecupero(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.countdownTimer != null) app.countdownTimer.stop();
        if (app.flashTimer != null) app.flashTimer.stop();

        app.turnoCorrente = 1;
        app.timeRemainingSx = 0;
        app.timeRemainingDx = 0;
        app.emergenzaGiallaAttiva = true;

        app.fasePreRecupero = Fase.ATTESA;
        app.tempoSxPreRecupero = 0;
        app.tempoDxPreRecupero = 0;
        app.colorePreRecupero = Color.RED;

        app.faseAttuale = Fase.RECUPERO_ATTESA;
        app.timeRemainingSx = 40;

        new ComandoApplicaLayoutMonitor(app).esegui();
        new ComandoImpostaColoriSingoli(app, Color.RED).esegui();

        String testoRecupero = GestoreLingua.t("display.recupero");
        app.minTurniSingolo.setText(testoRecupero);
        for (DisplayArciere da : app.archerDisplays) {
            da.turniLabelSingolo.setText(testoRecupero);
            da.turniSpecialLabel.setText(testoRecupero);
        }

        new ComandoBloccaInterfaccia(app, true).esegui();
        new ComandoAggiornaDisplay(app).esegui();
    }
}
