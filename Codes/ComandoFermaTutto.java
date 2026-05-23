import java.awt.CardLayout;
import java.awt.Color;

/**
 * Arresta timer e flash, riporta la fase ad ATTESA e azzera tutti i tempi.
 * È il nucleo atomico del reset: usato sia da ComandoReset che da altri comandi.
 */
public class ComandoFermaTutto implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoFermaTutto(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.countdownTimer != null) app.countdownTimer.stop();
        if (app.flashTimer != null) app.flashTimer.stop();

        app.faseAttuale = Fase.ATTESA;
        app.turnoCorrente = 1;
        app.timeRemainingSx = 0;
        app.timeRemainingDx = 0;
        app.emergenzaGiallaAttiva = true;

        new ComandoBloccaInterfaccia(app, false).esegui();
        new ComandoImpostaColoriSingoli(app, Color.RED).esegui();
        new ComandoImpostaColoriScontro(app, Color.RED, Color.RED).esegui();
        new ComandoApplicaLayoutMonitor(app).esegui();

        if (app.add40Container != null) {
            ((CardLayout) app.add40Container.getLayout()).show(app.add40Container, "EMPTY");
        }
        new ComandoPulisciLabelEmergenza(app).esegui();
    }
}
