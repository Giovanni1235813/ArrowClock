import java.awt.Color;

/**
 * Avvia la fase di preparazione rossa per la modalità singola/lineare.
 * Carica T1 come tempo di preparazione e blocca i controlli.
 */
public class ComandoIniziaPrepSingola implements Comando {

    private final ArcherySoftwareMain app;

    public ComandoIniziaPrepSingola(ArcherySoftwareMain app) {
        this.app = app;
    }

    @Override
    public void esegui() {
        app.motoreTimer.sincronizzaEResettaAccumulatoreSingolo(); // FIXED: Clear fractional time
        app.faseAttuale = Fase.PREPARAZIONE_ROSSO;
        new ComandoBloccaInterfaccia(app, true).esegui();
        app.timeRemainingSx = (int) app.spinT1.getValue();
        new ComandoImpostaColoriSingoli(app, Color.RED).esegui();
        new ComandoAggiornaTestoTurno(app).esegui();
        new ComandoAggiornaDisplay(app).esegui();
    }
}
