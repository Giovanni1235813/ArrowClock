import java.awt.CardLayout;
import java.awt.Color;

/**
 * Attiva o risolve un'emergenza.
 * All'attivazione: congela il timer, avvia il lampeggio giallo/rosso
 * e mostra "STOP" su tutti i display.
 * Alla risoluzione: ripristina la fase salvata e riavvia il timer.
 */
public class ComandoEmergenza implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoEmergenza(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        if (app.faseAttuale == Fase.EMERGENZA) {
            risolviEmergenza();
        } else if (puoAttivarsi()) {
            attivaEmergenza();
        }
    }

    private boolean puoAttivarsi() {
        return app.faseAttuale != Fase.ATTESA
                && app.faseAttuale != Fase.RECUPERO_ATTESA
                && app.faseAttuale != Fase.IDENTIFICAZIONE_MONITOR;
    }

    private void risolviEmergenza() {
        app.flashTimer.stop();
        app.faseAttuale = app.faseSalvata;

        ((CardLayout) app.add40Container.getLayout()).show(app.add40Container, "EMPTY");
        new ComandoPulisciLabelEmergenza(app).esegui();
        new ComandoRipristinaColoriSemaforo(app).esegui();
        new ComandoAggiornaTestoTurno(app).esegui();

        MotoreAudio.istanza().azzeraCodaFischi();
        MotoreAudio.istanza().eseguiFischi(1, app.isSuonoAttivo);
        app.motoreTimer.avvia();
        new ComandoBloccaInterfaccia(app, app.faseAttuale != Fase.ATTESA).esegui();
        new ComandoAggiornaBottoni(app).esegui();

        new ComandoAggiornaDisplay(app).esegui(); // Trigger central sync
        app.gestoreLog.logEmergenza(false);
    }

    private void attivaEmergenza() {
        if (app.countdownTimer.isRunning()) app.countdownTimer.stop();
        MotoreAudio.istanza().azzeraCodaFischi();
        MotoreAudio.istanza().eseguiFischi(5, app.isSuonoAttivo);

        app.faseSalvata = app.faseAttuale;
        app.coloreSalvato = app.isScontroMode ? Color.RED : app.minLightSingolo.getBackground();
        app.faseAttuale = Fase.EMERGENZA;
        app.emergenzaGiallaAttiva = true;

        if (app.faseSalvata != Fase.PREPARAZIONE_ROSSO) {
            ((CardLayout) app.add40Container.getLayout()).show(app.add40Container, "BUTTON");
        }
        new ComandoAggiornaLabelEmergenza(app).esegui();

        app.flashTimer.start();
        new ComandoBloccaInterfaccia(app, true).esegui();

        new ComandoAggiornaDisplay(app).esegui(); // Trigger central sync
        app.gestoreLog.logEmergenza(true);
    }


}
